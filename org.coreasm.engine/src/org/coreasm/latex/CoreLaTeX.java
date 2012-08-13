/*
 * CoreLaTeX.java 		$Revision: 80 $
 * 
 * Copyright (c) 2009 Roozbeh Farahbod
 *
 * Last modified on $Date: 2009-07-24 16:25:41 +0200 (Fr, 24 Jul 2009) $  by $Author: rfarahbod $
 * 
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */


package org.coreasm.latex;

import java.util.Set;

import org.coreasm.engine.interpreter.Node;

/**
 * A very simple module to generate LaTeX output from 
 * CoreASM specifications.
 * 
 * This code is quick hack I did one day while writing my PhD thesis; it is
 * far from a perfect solution.
 *   
 * @author Roozbeh Farahbod
 *
 */

public class CoreLaTeX {

	public static String toLatex(Node rootNode, Set<String> knownIds) {
		LaTeXFormatStringMapper formatter = new LaTeXFormatStringMapper(knownIds);
		return rootNode.unparseTree(formatter).replace("_", "\\_");
	}
	
}
