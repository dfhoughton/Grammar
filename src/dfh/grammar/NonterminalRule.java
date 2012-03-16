/*
 * dfh.grammar -- a recursive descent parser library for Java
 * 
 * Copyright (C) 2012 David F. Houghton
 * 
 * This software is licensed under the LGPL. Please see accompanying NOTICE file
 * and lgpl.txt.
 */
package dfh.grammar;

/**
 * A marker interface for rules that either contain subrules or, like
 * {@link BacktrackingBarrier barriers}, do not correspond to actual features of
 * the character sequence.
 * 
 * @author David Houghton
 */
public interface NonterminalRule {

}
