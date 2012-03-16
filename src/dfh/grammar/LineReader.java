/*
 * dfh.grammar -- a recursive descent parser library for Java
 * 
 * Copyright (C) 2012 David F. Houghton
 * 
 * This software is licensed under the LGPL. Please see accompanying NOTICE file
 * and lgpl.txt.
 */
package dfh.grammar;

import java.io.IOException;

/**
 * Basic line iterator interface so we can parse {@link Grammar Grammars} out of
 * both arrays and various other streamable data sources.
 * <p>
 * <b>Creation date:</b> Mar 18, 2011
 * 
 * @author David Houghton
 * 
 */
public interface LineReader {
	/**
	 * @return one line from source
	 * @throws IOException
	 */
	String readLine() throws IOException;

	/**
	 * Returns line number currently being read. Used in generating error
	 * messages during compilation.
	 * 
	 * @return line number currently being read
	 */
	int lineNumber();
}