/*	
 * GrammarProduction.java 	1.0 	$Revision: 243 $
 * 
 * Last modified on $Date: 2011-03-29 02:05:21 +0200 (Di, 29 Mrz 2011) $
 *
 * Copyright (C) 2005 Mashaal Memon
 * Copyright (c) 2007 Roozbeh Farahbod
 *
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */

package org.coreasm.engine.parser;

import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;

import org.codehaus.jparsec.Parser;
import org.coreasm.engine.interpreter.Node;
import org.coreasm.engine.parser.GrammarRule.GRType;
import org.coreasm.util.CoreASMGlobal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/** 
 *	This class represents a grammar production composed of potentially many grammar
 * production rules.
 *   
 *  @author  Mashaal Memon, Roozbeh Farahbod
 *  
 */
public class GrammarProduction {

	private static final Logger logger = LoggerFactory.getLogger(GrammarProduction.class);

	private String nonterminal = null;
	private final Hashtable<String, BodySegmentContributor> bodyContributors = new Hashtable<String,BodySegmentContributor>();
	private Parser<Node> parser; 
	private GRType type = null;
	
	/**
	 * Create a grammar production given a nonterminal name.
	 * 
	 * @param nonterminal the <code>String</code> representing the nonterminal for the production.
	 */
	public GrammarProduction(String nonterminal) {
		
		setNonterminal(nonterminal);
	}
	
	/**
	 * Create a grammar production given an entire grammar rule.
	 * 
	 * @param gr the <code>GrammarRule</code> which starts the production.
	 */
	public GrammarProduction(GrammarRule gr) {
		super();
		
		addGrammarRule(gr);
	}
	
	/**
	 * This method adds a grammar rule to the production
	 *
	 * @param gr the <code>GrammarRule</code> which is to be added to the production.
	 */
	public void addGrammarRule(GrammarRule gr)
	{
		setNonterminal(gr.name);
		addBodySegmentContributor(gr.body, gr.pluginName);
		setType(gr.type);
		setParser(gr.parser);
	}
	
	/**
	 * Sets the parser for this grammar production.
	 * 
	 * @param parser a {@link jfun.parsec.Parser}.
	 */
	public void setParser(Parser<Node> parser) {
		if (this.parser == null)
			this.parser = parser;
		else
			if (parser != null && this.parser != parser)
			 logger.error("Production for '{}' was given multiple parsers.", nonterminal);
	}
	
	/**
	 * Get nonterminal grammer production
	 *
	 * @return nonterminal as <code>String</code>
	 */
	public String getNonterminal()
	{
			return nonterminal;
	}
	
	/**
	 * Returns the parser of this grammar production.
	 */
	public Parser<Node> getParser() {
		return parser;
	}
	
	/**
	 * Get type for grammer production
	 *
	 * @return type value as <code>GRType</code>
	 */
	public GRType getType()
	{
			return type;
	}
	
	// FIXME change the comment!
	/**
	 * Get production as string so that it may be used with the parser generator.
	 * The string is produced in RIT OOPS "Rfc" format.
	 * 
	 * @return String represeinting entire production
	 *
	 * @throws ParserException when no observer has been given for this production
	 * 
	 * @see <a href="http://www.cs.rit.edu/~ats/projects/oops/edu/doc/edu/rit/cs/oops/pg/package-summary.html#specify%20grammar">Notation Definition</a>
	 */ 
	public String getAsString() throws ParserException
	{
		// these are based on the "Rfc" synatx.
		// FIXME: can be removed later -- Roozbeh
		String nonterminalBodyDelimiter = " "+CoreASMGlobal.getProperty("parser.grammar.production.nonterminalBodyDelimiter")+" "; // delimiters to be used between nonterminal and production body
		String bodyContributorDelimiter = "\n\t "+CoreASMGlobal.getProperty("parser.grammar.production.optionDelimiter")+" "; // delimiter to be used between different body/contributor segments
		String productionDelimiter = "\n\t "+CoreASMGlobal.getProperty("parser.grammar.production.productionDelimiter")+" "; // delimiters to be used between productions
		
		Iterator<BodySegmentContributor> itBC =  bodyContributors.values().iterator(); // iterator
		String production = ""; // production being built
			
		if (parser == null)
		{
			logger.error("Production for \""+nonterminal+"\" has no parser.");
			throw new ParserException("Production for \""+nonterminal+"\" has no parser.");
		}
				
		// add delimiter between nonterminal and body
		production = nonterminal + nonterminalBodyDelimiter;
		
		// add body delimiter between body's
		int i = 0;
		while (itBC.hasNext())
		{
			// add body/contributor delimiter if not first body/contributor appended
			if (i > 0)
				production += bodyContributorDelimiter;
				
			// add body and contributors
			production += itBC.next().getAsString();

			i++;
		}
		
		// add production delimiter to end of production
		production += productionDelimiter;
		
		return production;
	}
	
	/**
	 * Set the nonterminal of the production. If the production has a nonterminal, and this particular
	 * nonterminal is different than the one given, give an error.
	 * 
	 * @param nonterminal a nonterminal for a grammar rule to be included in this production
	 */
	public void setNonterminal(String nonterminal)
	{
		// if no nonterminal specified
		if (this.nonterminal == null)
			this.nonterminal = nonterminal;
		// else if nonterminal IS given, but not the same as set observer
		else if (nonterminal != null && this.nonterminal.equals(nonterminal) != true)
			logger.error("Production for \""+this.nonterminal+"\" was given an grammar rule for nonterminal \""+nonterminal+"\".");	
	}
	
	/**
	 * Add the given body segment and its contributor to the production.
	 * 
	 * @param body a body segment which is to be added to this production.
	 * @param contributor the contributor of the given body segment.
	 */
	public void addBodySegmentContributor(String body, String contributor)
	{
		// if body segment exists, it was already added by a different contributor
		if (bodyContributors.containsKey(body))
		{
			logger.debug("Production for \""+nonterminal+"\" contains duplicate body segment \""+body+"\", readded by \""+contributor+"\".");
			
			// add the other contributor for this body segment
			bodyContributors.get(body).addContributor(contributor);
		}
		// else add body segment and its contributor
		else
			bodyContributors.put(body, new BodySegmentContributor(body, contributor));	
	}
	
	/**
	 * Add the given body segment and its *collection* of contributors to the production.
	 * 
	 * @param body a body segment which is to be added to this production.
	 * @param contributors the contributors of the given body segment.
	 * 
	 * @see org.coreasm.engine.parser.GrammarProduction#addBodySegmentContributor(String, String)
	 */
	public void addBodySegmentContributor(String body, Collection<String> contributors)
	{
		// if body segment exists, it was already added by a different contributor
		if (bodyContributors.containsKey(body))
		{
			logger.debug("Production for \""+nonterminal+"\" contains duplicate body segment \""+body+"\", readded by \""+contributors.toString()+"\".");
			
			// add the other contributors for this body segment
			bodyContributors.get(body).addContributor(contributors);
		}
		// else add body segment and its contributors
		else
			bodyContributors.put(body, new BodySegmentContributor(body, contributors));	
	}
	
	/**
	 * Set the given grammar rule type for the rule
	 * 
	 * @param type a type for a grammar rule to be included in this production
	 */
	public void setType(GRType type)
	{
		// if no nonterminal specified
		if (this.type == null)
			this.type = type;
		// else if type IS given, but not the same as set type
		else if (type != null && this.type.equals(type) != true)
			logger.error("Production for \""+this.nonterminal+"\" was given multiple rule types \""+this.type.toString()+"\", \""+type.toString()+"\".");	
	}
	
	/** 
	 *	This class represents a production body segment and its contributor(s) (if any). The object
	 *  used to keep this information together ultimately for information/debugging purposes
	 *  (i.e. knowing who contributed a particular portion of the body of a production).
	 *   
	 *  @author  Mashaal Memon
	 *  
	 */
	private class BodySegmentContributor {
		
		private final String body;
		private final HashSet<String> contributors = new HashSet<String>(); // a body may potentially have multiple contributors
		
		/**
		 * Create an object holding a production body segment and its contributor.
		 * 
		 * @param body the <code>String</code> representing one of the body segments making the production.
		 * @param contributor the <code>String</code> representing it's contributor .
		 */
		public BodySegmentContributor(String body, String contributor) {
			super();
			this.body = body;
			addContributor(contributor);
			
		}
		
		/**
		 * Create an object holding a production body segment and its *collection* of contributors.
		 * 
		 * @param body the <code>String</code> representing one of the body segments making the production.
		 * @param contributors the <code>Collection &lt;String&gt;</code> representing a *collection* of contributors
		 */
		public BodySegmentContributor(String body, Collection<String> contributors) {
			super();
			this.body = body;
			addContributor(contributors);
			
		}
		
		/**
		 * Add the given body segment contributor.
		 * 
		 * @param contributor the <code>String</code> representing a contributor
		 */
		public void addContributor(String contributor)
		{
			contributors.add(contributor);
		}
		
		/**
		 * Add the given collection of body segment contributors.
		 * 
		 * @param contributors the <code>Collection &lt;String&gt;</code> representing a *collection* of contributors
		 */
		public void addContributor(Collection<String> contributors)
		{
			this.contributors.addAll(contributors);
		}
		
		/**
		 * Get body segment, with contributors in a comment at the end
		 * 
		 * @returns String representing the body segment and contributors
		 */ 
		public String getAsString()
		{
			// single line comment prefix
			String singleLineCommentPrefix = " "+CoreASMGlobal.getProperty("parser.grammar.linecomment")+" ";
			
			// this is to make contributor output cleaner
			String contributorDelimiter = ", ";
			
			Iterator<String> itContributors =  contributors.iterator(); // iterator
			String bodyContributors = ""; // string to be built
					
			// add body
			bodyContributors = body;
			
			// add contributors
			bodyContributors += singleLineCommentPrefix;
			int i = 0;
			while (itContributors.hasNext())
			{
				// add contributor delimiter if not first contributor appended
				if (i > 0)
					bodyContributors += contributorDelimiter;
					
				// add contributor
				bodyContributors += itContributors.next();
					
				i++;
			}
			
			return bodyContributors;
		}
	}
	
	

}
