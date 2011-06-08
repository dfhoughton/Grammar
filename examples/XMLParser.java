import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import dfh.grammar.Grammar;
import dfh.grammar.GrammarException;
import dfh.grammar.Match;

/**
 * This class implements a useful subset of the XML specification as a
 * {@link Grammar}. Due to extensive use of the possessive repetition modifier
 * this parser is almost entirely non-backtracking, so it is fairly efficient.
 * This isn't to say you should use this in lieu of existing SAX parsers, but it
 * shows that with {@link Grammar} one can construct a useful parser with
 * minimal effort.
 * <p>
 * <b>Creation date:</b> Mar 30, 2011
 * 
 * @author David Houghton
 * 
 */
public class XMLParser {
	private static final String[] rules = {
			//
			"       <ROOT> = <declaration>?+ <s>?+ <element> <s>?+",//
			"     <double> = '<' <tag> [ <ns> <attribute> ]*+ '>' :: [ <content> | <comment> | <element> ]*+ '</' 2 '>'",//
			"    <element> = <single> | <double>",//
			"     <single> = '<' <tag> [ <ns> <attribute> ]*+ <ns>?+ '/>'",//
			"  <attribute> = <tag> '=' [ \"'\" <squote> \"'\" | '\"' <dquote> '\"' ]",//
			"    <content> = [ <c> | <entity> ]++",//
			"          <c> = /[^&<]/",//
			"    <comment> = /(?s:<!--.*?-->)/",//
			"<declaration> = /<\\?xml[\\s&&[^\\n]]++version=([\"'])\\d++(?:\\.\\d++)?\\1[\\s&&[^\\n]]++encoding=([\"'])[\\w-]++\\2[\\s&&[^\\n]]*+\\?>\\n/",//
			"     <dquote> = /[^\\n\"]*+/",//
			"     <entity> = /&(?:#(?:\\d++|x[\\da-f]++)|\\w++);/",//
			"         <ns> = /[\\s&&[^\\n]]++/",//
			"          <s> = /\\s++/",//
			"     <squote> = /[^\\n']*+/",//
			"        <tag> = /\\b\\w++(:\\w++)*+/",//
	};

	/**
	 * Give this class a list of XML files and it will attempt to parse them.
	 * You will find an example in the examples directory itself.
	 * 
	 * @param args
	 * @throws GrammarException
	 * @throws IOException
	 */
	public static void main(String[] args) throws GrammarException, IOException {
		Grammar g = new Grammar(rules);
		for (String fileName : args) {
			File file = new File(fileName);
			if (!file.exists()) {
				System.err.println("there is no file " + fileName);
				continue;
			}
			if (!file.canRead()) {
				System.err.println("file " + fileName + " cannot be read");
				continue;
			}
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line = null;
			StringBuilder b = new StringBuilder();
			while ((line = reader.readLine()) != null)
				b.append(line).append('\n');
			String s = b.toString();
			Match m = g.matches(s).match();

			if (m != null) {
				// we pull out some content, this coule be made more efficient,
				// and
				// its not quite XPath, but it shows what can be done

				System.out.println("COMMENTS:");
				for (Match n : m.get("comment")) {
					System.out.println(s.substring(n.start(), n.end()));
				}
				System.out.println();
				System.out.println("ENTITIES:");
				for (Match n : m.get("entity")) {
					System.out.println(s.substring(n.start(), n.end()));
				}
				System.out.println();
				System.out.println("CONTENT:");
				for (Match n : m.get("content")) {
					System.out.println(s.substring(n.start(), n.end()));
				}
			} else
				System.out.println("invalid XML document");
		}
	}
}
