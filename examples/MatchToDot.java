import static dfh.grammar.util.Dotify.appendGraph;
import static dfh.grammar.util.Dotify.endDot;
import static dfh.grammar.util.Dotify.startDot;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

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
	 * Applies the grammar to the file, creating a dot file with graphs of all
	 * matches. The output file is named by appending ".dot" to the name of the
	 * text file.
	 * 
	 * @param args
	 *            grammar file and text file
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws GrammarException
	 */
	public static void main(String[] args) throws GrammarException,
			FileNotFoundException, IOException {
		File[] returnAr = validateArgs(args);
		File gf = returnAr[0];
		File inf = returnAr[1];
		Grammar g = new Grammar(gf);
		String text = fileToText(inf);
		Matcher m = g.find(text);
		StringBuilder b = startGraph(inf);
		Match n;
		int[] index = { 1 };
		while ((n = m.match()) != null) {
			if (!n.zeroWidth())
				appendGraph(b, n, text, index, null);
		}
		endDot(b);
		dotFile(inf, b);
	}

	private static void dotFile(File inf, StringBuilder b) throws IOException {
		File outf = new File(inf.getPath() + ".dot");
		BufferedWriter writer = new BufferedWriter(new FileWriter(outf));
		writer.write(b.toString());
		writer.close();
	}

	private static StringBuilder startGraph(File inf) {
		String name = inf.getName();
		return startDot(name);
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

	private static File[] validateArgs(String[] args) {
		if (args.length != 2)
			usage("two arguments expected");
		String grammarDoc = args[0];
		File grammarFile = new File(grammarDoc);
		if (!(grammarFile.exists() && grammarFile.canRead()))
			usage(grammarDoc + " must be a readable grammar file");
		String fileToMatch = args[1];
		File matchingFile = new File(fileToMatch);
		if (!(matchingFile.exists() && matchingFile.canRead()))
			usage(matchingFile + " must be a readable text file");
		File[] returnAr = { grammarFile, matchingFile };
		return returnAr;
	}

	private static void usage(String string) {
		System.err.println("ERROR: " + string);
		System.err.println("USAGE: <executable> <grammar file> <text file>");
		System.exit(1);
	}

}
