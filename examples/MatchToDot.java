import static dfh.grammar.util.Dotify.appendGraph;
import static dfh.grammar.util.Dotify.endDot;
import static dfh.grammar.util.Dotify.startDot;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import dfh.cli.Cli;
import dfh.cli.Cli.Opt;
import dfh.cli.coercions.FileCoercion;
import dfh.grammar.Grammar;
import dfh.grammar.GrammarException;
import dfh.grammar.Match;
import dfh.grammar.Matcher;
import dfh.grammar.util.Dotify;

/**
 * For making graphs showing the structure of matches. The output must be
 * processed with GraphViz or something else that can make graph images from
 * .dot files.
 * 
 * This class basically provides a command line interface to {@link Dotify}, a
 * debugging utility.
 * 
 * <b>Creation date:</b> Apr 2, 2011
 * 
 * @author David Houghton
 * 
 */
public class MatchToDot {

	/**
	 * Run with --help option to see usage information.
	 * 
	 * @param args
	 *            files to match against
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws GrammarException
	 */
	public static void main(String[] args) throws GrammarException,
			FileNotFoundException, IOException {
		Object[][][] spec = {
				//
				{ { Opt.NAME, MatchToDot.class.getName() } },//
				{ { Opt.ARGS, "file", Opt.STAR } },//
				{ { "grammar", 'g', FileCoercion.C }, { "grammar file" },
						{ Cli.Res.REQUIRED } },//
				{ { "out", 'o', FileCoercion.C }, { "file to receive output" } },//
				{ {
						Opt.USAGE,
						"convert dfh.grammar matches to GraphViz graphs",
						MatchToDot.class.getName()
								+ " converts text to .dot format text suitable for converting into a graph with GraphViz (http://www.graphviz.org/),\n"
								+ "or any other utility that can read this format. If no file arguments are provided , it will expect input from STDIN.\n"
								+ "If no output file is provided, it will write its output to STDOUT." } },//
		};
		Cli cli = new Cli(spec);
		cli.parse(args);
		Grammar g = null;
		File gf = (File) cli.object("grammar");
		try {
			g = new Grammar(gf);
		} catch (Exception e) {
			cli.die("could not compile grammar: " + e);
		}
		StringBuilder b;
		if (cli.argList().isEmpty()) {
			b = startDot("STDIN");
		} else if (cli.argList().size() == 1) {
			String name = new File(cli.argument("file")).getName();
			b = startDot(name);
		} else {
			b = startDot("multiple_files");
		}
		int[] index = { 1 };
		if (cli.argList().isEmpty()) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] buf = new byte[1024];
			int i;
			while ((i = System.in.read(buf)) > -1)
				baos.write(buf, 0, i);
			String text = new String(baos.toByteArray());
			baos = null;
			Matcher m = g.find(text);
			Match n;
			while ((n = m.match()) != null) {
				if (!n.zeroWidth())
					appendGraph(b, n, index, null);
			}
		} else {
			for (String fn : cli.argList()) {
				File f = new File(fn);
				String text = fileToText(f);
				Matcher m = g.find(text);
				Match n;
				while ((n = m.match()) != null) {
					if (!n.zeroWidth())
						appendGraph(b, n, index, null);
				}
			}
		}
		endDot(b);
		if (cli.isSet("out")) {
			File outf = (File) cli.object("out");
			BufferedWriter writer = new BufferedWriter(new FileWriter(outf));
			writer.write(b.toString());
			writer.close();
		} else
			System.out.println(b);
	}

	private static String fileToText(File inf) throws FileNotFoundException,
			IOException {
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(
				inf));
		int i;
		StringBuilder b = new StringBuilder();
		while ((i = bis.read()) > -1) {
			b.append((char) i);
		}
		String text = b.toString();
		return text;
	}
}
