/*
 * Specification.java 	$Revision: 243 $
 *
 * Copyright (C) 2005 Roozbeh Farahbod 
 * 
 * Last modified by $Author: rfarahbod $ on $Date: 2011-03-29 02:05:21 +0200 (Di, 29 Mär 2011) $.
 *
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.engine;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.coreasm.engine.absstorage.BackgroundElement;
import org.coreasm.engine.absstorage.FunctionElement;
import org.coreasm.engine.absstorage.RuleElement;
import org.coreasm.engine.absstorage.UniverseElement;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.Node;
import org.coreasm.engine.plugin.ParserPlugin;
import org.coreasm.engine.plugin.Plugin;
import org.coreasm.engine.plugin.VocabularyExtender;
import org.coreasm.util.Tools;
import org.coreasm.util.odf.ODTImporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper around a CoreASM specification.
 * 
 * @author Roozbeh Farahbod
 * 
 */

public class Specification {

	/* buffer size for loading specification from a file */
	private static final int BUFFER_SIZE = 512 * 1024;

	private static final Logger logger = LoggerFactory.getLogger(Specification.class);
	
	/** is the specification modified? */
	public boolean isModified = false;
	
	/* source of the specification */
	private final Object source;
	
	/** Root node of the parsed specification */
	private ASTNode rootNode = null;
	
	/** Specification text */
	private String text;
	/** Specification lines */
	private List<SpecLine> lines; 
	
	/* link to the engine */
	private final ControlAPI engine;
	
	/* name of the specification file (computed based on the absolute path) */
	private String fileName = null;
	/* directory of the specification file (computed based on the absolute path) */ 
	private String fileDir = null;
	/* absolute path to the specification file */ 
	private String absolutePath = null;
	
	private String name = null;
	
	private Set<Plugin> requiredPlugins = null;
	private Set<String> requiredPluginNames = null;
	private Set<String> keywords = null;
	private Set<String> operators = null;
	private Set<FunctionInfo> functions = null;
	private Set<UniverseInfo> universes = null;
	private Set<BackgroundInfo> backgrounds = null;
	private Set<RuleInfo> rules = null;
	
	/* 
	 * list of all the nodes in the tree sorted 
	 * by the position in the text 
	 */
	private ArrayList<Node> sortedNodes = null;
	
	/**
	 * Creates a new CoreASM Specification object based on the given file.
	 * If the file is an ODT file (the name ends with ".odt", case insensitive), 
	 * this constructor will use the {@link ODTImporter} class to extract the 
	 * CoreASM specification from the ODT file.
	 *  
	 */
	public Specification(ControlAPI engine, File file) throws IOException {
		this.rootNode = null;
		this.absolutePath = file.getAbsolutePath();
		this.fileName = file.getName();
		this.fileDir = file.getAbsolutePath().substring(0, file.getAbsolutePath().lastIndexOf(File.separator));
		this.source = file;
		this.engine = engine;
		updateLines(loadSpec(file));
		logger.debug("New specification created from {}", this.absolutePath);
	}
	
	/* 
	 * Creates a new CoreASM Specification object with the given engine
	 * and the text of the specification.
	 *  
	 * If the file name is not null, it is recorded as the original file name of the specification.
	 *
	public Specification(ControlAPI engine, String text, String fileName) throws IOException {
		this.rootNode = null;
		this.source = text;
		this.engine = engine;
		if (fileName != null) {
			File file = new File(fileName);
			this.absolutePath = file.getAbsolutePath();
			this.fileName = file.getName();
			this.fileDir = file.getAbsolutePath().substring(0, file.getAbsolutePath().lastIndexOf(File.separator));
		}
		if (this.fileName == null)
			updateText(text, "Specification");
		else
			updateText(text, fileName);
		Logger.log(Logger.INFORMATION, Logger.controlAPI, "New specification created.");
	}
	*/
	
	/** 
	 * Creates a new CoreASM Specification object with the given engine
	 * and a reader providing the text of the specification.
	 *  
	 * If the file name is not null, it is recorded as the original file name of the specification.
	 */
	public Specification(ControlAPI engine, Reader reader, String fileName) throws IOException {
		this.rootNode = null;
		this.source = reader;
		this.engine = engine;
		if (fileName != null) {
			File file = new File(fileName);
			this.absolutePath = file.getAbsolutePath();
			this.fileName = file.getName();
			this.fileDir = file.getAbsolutePath().substring(0, file.getAbsolutePath().lastIndexOf(File.separator));
		}
		if (this.fileName == null)
			updateLines(loadSpec(reader, "Specification"));
		else
			updateLines(loadSpec(reader, this.fileName));
		logger.debug("New specification created.");
	}
	
	/** 
	 * Updates the text of the specification with a new text. This does not change the {@link #source} 
	 * of the specification.
	 * 
	 * @param text new text
	 * @param filename name of the file providing the text
	 * @throws IOException 
	 */
	public void updateText(String text, String filename) throws IOException {
		this.text = text;
		this.lines = loadLines(text, filename);
		this.rootNode = null;
	}

	/** 
	 * Updates the text of the specification with a new text. This does not change the source 
	 * of the specification.
	 * 
	 * @param lines new text
	 */
	public void updateLines(List<SpecLine> lines) {
		this.lines = Collections.unmodifiableList(lines);
		StringBuffer txt = new StringBuffer();
		Iterator<SpecLine> it = lines.iterator();
		while (it.hasNext()) {
			txt.append(it.next().text);
			if ( it.hasNext() ) txt.append(Tools.getEOL());
		}
		this.text = txt.toString();
		this.rootNode = null;
	}
	
	/**
	 * Returns the source of the this specification.
	 */ 
	public Object getSource() {
		return source;
	}
	
	public String getFileName() {
		return fileName;
	}

	public String getFileDir() {
		return fileDir;
	}

	public String getAbsolutePath() {
		return absolutePath;
	}

	/*
	 * Sets the parsed root node of this specification;
	 * This method should only be called by the CoreASM Engine.
	 */
	protected void setRootNode(ASTNode node) {
		this.rootNode = node;
		functions = null;
		universes = null;
		backgrounds = null;
		rules = null;
	}
	
	/**
	 * Returns the root node of the parsed specification
	 */
	public Node getRootNode() {
		return rootNode;
	}
	
	/**
	 * Returns the text of the given specification.
	 */
	public String getText() {
		return text;
	}
	
	/**
	 * Returns the text of the given specification.
	 */
	public List<SpecLine> getLines() {
		return lines;
	}
	
	/**
	 * Returns a specific line of the spec.
	 * 
	 * @param line line number
	 * @return returns an instance of {@link SpecLine} or <code>null</code> if 
	 * the spec is empty. If the requested line index is beyond the number of 
	 * specification lines, returns the last line of the spec.
	 */
	public SpecLine getLine(int line) {
		SpecLine l = null;
		
		if (getLines() != null && getLines().size() > 0) {
			try {
				l = getLines().get(line - 1);
			} catch (IndexOutOfBoundsException e) {
				l = getLines().get(getLines().size() - 1);
			}
		}
		return l;
	}

	/**
	 * Returns the name of this specification.
	 */
	public String getName() {
		if (name == null) {
			if (rootNode != null && rootNode.getFirst() != null) {
				name = rootNode.getFirst().getToken();
			} else
				name = "CoreASM Specification";
		}
		return name;
	}
	
	/**
	 * Returns true if the specification is parsed.
	 */
	public boolean isParsed() {
		return rootNode != null;
	}

	/*
	 * Sets the name of required plugins in the specification.
	 * This method should only be called by the CoreASM Engine.
	 */
	protected void setPluginNames(Set<String> pluginNames) {
		if (requiredPluginNames != null)
			throw new IllegalStateException("The set of required plug-ins can be set only once for every specification."); 
		requiredPluginNames = Collections.unmodifiableSet(pluginNames);
	}
	
	/**
	 * Returns the names of plug-ins that are required by this 
	 * specification.
	 */
	public Set<String> getPluginNames() {
		return requiredPluginNames;
	}
	
	/**
	 * Read text from a file and returns an array of {@link SpecLine}.
	 * 
	 * @param mainSpec a link to the main specification (used to find the root directory)
	 * @param fileName the name of the file to be read; if the file name is absolute, the specification reference should be null. 
	 * 
	 * @throws IOException when a spec file cannot be opened or closed
	 * @throws FileNotFoundException when the spec file cannot be found
	 * @see #loadSpec(String)
	 */
	public static ArrayList<SpecLine> loadSpec(Specification mainSpec, String fileName) throws IOException {
		String specRoot = "";
		if (mainSpec != null) 
			specRoot = mainSpec.getFileDir() + File.separator;

		return loadSpec(specRoot + fileName);
	}

	/**
	 * Read text from a file (either plain text or ODT file) and returns an array 
	 * of {@link SpecLine}. If the file name ends in ".odt" (case insensitive), 
	 * it is treated as an ODT file.
	 * 
	 * @param fileName the full name of the file to be read. 
	 * 
	 * @throws IOException when a spec file cannot be opened or closed
	 * @throws FileNotFoundException when the spec file cannot be found
	 * 
	 * @see #loadSpec(File)
	 */
	public static ArrayList<SpecLine> loadSpec(String fileName) throws IOException {
		return loadSpec(new File(fileName));
	}

	
	/**
	 * Read text from a file (either plain text or ODT file) and returns an array 
	 * of {@link SpecLine}. If the file name ends in ".odt" (case insensitive), 
	 * it is treated as an ODT file.
	 * 
	 * @param file a file handle
	 * 
	 * @throws IOException when a spec file cannot be opened or closed
	 * @throws FileNotFoundException when the spec file cannot be found
	 */
	public static ArrayList<SpecLine> loadSpec(File file) throws IOException {
		String fname = file.getAbsolutePath();
		
		if (fname.toLowerCase().endsWith(".odt")) {
			String coreasmSpec = ODTImporter.importODT(fname) + Tools.getEOL();
			StringReader reader = new StringReader(coreasmSpec);
			return loadSpec(reader, fname);
		} else
			return loadSpec(new InputStreamReader(getInputStream(file)), fname);
	}

	/**
	 * Read specification text from a reader that provides plain text.
	 * 
	 * @param reader an instance of a {@link Reader}
	 * @param fileName the full name of the file to be read. 
	 * 
	 * @return an array of {@link SpecLine}
	 * 
	 * @throws IOException when a spec file cannot be opened or closed
	 * @throws FileNotFoundException when the spec file cannot be found
	 */
	public static ArrayList<SpecLine> loadSpec(Reader reader, String fileName) throws IOException {
		// buffered reader to be used to read spec file;
		BufferedReader specFileReader;
		
		// open specification stream/reader;
		specFileReader = new BufferedReader(reader);
		
		// Create new list
		ArrayList<SpecLine> specText = new ArrayList<SpecLine>();
			
		// while not at end of file, read a line and
		String line = null;
		int c = 1;
		while ((line = specFileReader.readLine()) != null) {
			// add line to vector
			specText.add(new SpecLine(line, fileName, c));
			c++;
		}
		// close the specification stream/reader
		specFileReader.close();
		
		return specText;
	}


	/**
	 * Extracts lines of text
	 */
	private ArrayList<SpecLine> loadLines(String text, String fileName) throws IOException {
		// buffered reader to be used to read spec file;
		BufferedReader specFileReader;
		
		// open specification stream/reader;
		specFileReader = new BufferedReader(new StringReader(text));
		
		// Create new list
		ArrayList<SpecLine> specText = new ArrayList<SpecLine>();
			
		// while not at end of file, read a line and
		String line = null;
		int c = 1;
		while ((line = specFileReader.readLine()) != null) {
			// add line to vector
			specText.add(new SpecLine(line, fileName, c));
			c++;
		}
		// close the specification stream/reader
		closeSpec(specFileReader);
		
		return specText;
	}

	/**
	 * Loads the specification file to an input stream.
	 * 
	 * @param file specification file
	 * @throws FileNotFoundException
	 */
	private static InputStream getInputStream(File file) throws FileNotFoundException
	{
		InputStream result = null;
		try	{
			result = new BufferedInputStream(new FileInputStream(file), BUFFER_SIZE);
		} catch (FileNotFoundException e) {
			logger.error("CoreASM specification file \"" + file.getAbsolutePath() + "\" cannot be found.");
			throw e;
		}		
		return result;
	}

	/**
	 * Close file for specification given reader to close.
	 *
	 * @param a <code>BufferedReader</code> for the spec file stream, which we would like to close
	 *
	 * @throws IOException while specfile cannot be closed.
	 */
	private void closeSpec(BufferedReader specFileReader) throws IOException
	{
		try	{
			specFileReader.close();
		} catch (IOException e){
			logger.error("CoreASM specification could not be closed.");
			throw e;
		}
	}

	/* 
	 * Caching plugin lookups.
	 */
	private Set<Plugin> getRequiredPlugins() {
		if (engine == null) {
			logger.error("The engine reference of the specification is null." +
							" Cannot compute the set of required plugins.");
			return Collections.emptySet();
		}
		if (requiredPluginNames == null) 
			return null;
		if (requiredPlugins == null) {
			requiredPlugins = new HashSet<Plugin>();
			for (String pname: requiredPluginNames) {
				Plugin p = engine.getPlugin(pname);
				requiredPlugins.add(p);
			}
		}
		return requiredPlugins;
	}
	
	/**
	 * Returns the list of keywords defined by the plug-ins of this 
	 * specifications.
	 */
	public Set<String> getKeywords() {
		if (keywords == null) {
			if (getRequiredPlugins() == null)
				return Collections.emptySet();
			else {
				keywords = new HashSet<String>();
				for (Plugin p: requiredPlugins) {
					if (p instanceof ParserPlugin)
						for (String kw: ((ParserPlugin)p).getKeywords())
							keywords.add(kw);
				}
			}
		}
		return keywords;
	}
	
	/**
	 * Returns the list of operators defined by the plug-ins of this 
	 * specifications.
	 */
	public Set<String> getOperators() {
		if (operators == null) {
			if (getRequiredPlugins() == null)
				return Collections.emptySet();
			else {
				operators = new HashSet<String>();
				for (Plugin p: requiredPlugins) {
					if (p instanceof ParserPlugin)
						for (String kw: ((ParserPlugin)p).getKeywords())
							operators.add(kw);
				}
			}
		}
		return operators;
	}

	/**
	 * Returns information on the functions defined for this specification.
	 * The result may vary depending on the specification being parsed 
	 * or not.
	 */
	public Set<FunctionInfo> getDefinedFunctions() {
		if (functions == null) {
			if (getRequiredPlugins() == null)
				return Collections.emptySet();
			else {
				functions = new HashSet<FunctionInfo>();
				for (Plugin p: requiredPlugins) {
					if (p instanceof VocabularyExtender) {
						Map<String, FunctionElement> map = ((VocabularyExtender)p).getFunctions();
						if (map == null)
							continue;
						for (Entry<String, FunctionElement> fe: map.entrySet()) {
							if (fe.getValue() != null)
								functions.add(new FunctionInfo(p.getName(), fe.getKey(), fe.getValue()));
						}
					}
				}
			}
		}
		return functions;
	}
	
	/**
	 * Returns information on the universes defined for this specification.
	 * The result may vary depending on the specification being parsed 
	 * or not.
	 */
	public Set<UniverseInfo> getDefinedUniverses() {
		if (universes == null) {
			if (getRequiredPlugins() == null)
				return Collections.emptySet();
			else {
				universes= new HashSet<UniverseInfo>();
				for (Plugin p: requiredPlugins) {
					if (p instanceof VocabularyExtender) {
						Map<String, UniverseElement> map = ((VocabularyExtender)p).getUniverses();
						if (map == null)
							continue;
						for (Entry<String, UniverseElement> fe: map.entrySet()) {
							if (fe.getValue() != null)
								universes.add(new UniverseInfo(p.getName(), fe.getKey(), fe.getValue()));
						}
					}
				}
			}
		}
		return universes;
	}
	
	/**
	 * Returns information on the backgrounds defined for this specification.
	 * The result may vary depending on the specification being parsed 
	 * or not.
	 */
	public Set<BackgroundInfo> getDefinedBackgrounds() {
		if (backgrounds == null) {
			if (getRequiredPlugins() == null)
				return Collections.emptySet();
			else {
				backgrounds = new HashSet<BackgroundInfo>();
				for (Plugin p: requiredPlugins) {
					if (p instanceof VocabularyExtender) {
						Map<String, BackgroundElement> map = ((VocabularyExtender)p).getBackgrounds();
						if (map == null)
							continue;
						for (Entry<String, BackgroundElement> fe: map.entrySet()) {
							if (fe.getValue() != null)
								backgrounds.add(new BackgroundInfo(p.getName(), fe.getKey(), fe.getValue()));
						}
					}
				}
			}
		}
		return backgrounds;
	}
	
	/**
	 * Returns information on the rules defined for this specification.
	 * The result may vary depending on the specification being parsed 
	 * or not.
	 */
	public Set<RuleInfo> getDefinedRules() {
		if (rules == null) {
			if (getRequiredPlugins() == null)
				return Collections.emptySet();
			else {
				rules = new HashSet<RuleInfo>();
				for (Plugin p: requiredPlugins) {
					if (p instanceof VocabularyExtender) {
						Map<String, RuleElement> map = ((VocabularyExtender)p).getRules();
						if (map == null)
							continue;
						for (Entry<String, RuleElement> re: map.entrySet()) {
							if (re.getValue() != null)
								rules.add(new RuleInfo(p.getName(), re.getKey(), re.getValue()));
						}
					}
				}
			}
		}
		return rules;
	}

	/**
	 * Returns the Node (in the abstract syntax tree) 
	 * at the given location in the specification.
	 * If the specification is not parsed (see {@link #isParsed()})
	 * this method returns null.
	 *  
	 * @param index character index in the specification text
	 */
	public ASTNode getNodeAt(int index) {
		if (!isParsed())
			return null;
		
		if (sortedNodes == null) {
			sortedNodes = new ArrayList<Node>();
			loadSortedNodes(rootNode);
		}
		
		Node result = sortedNodes.get(findLastNode(sortedNodes, index, 0, sortedNodes.size()-1));
		while (!(result instanceof ASTNode))
			result = result.getParent();
		
		return (ASTNode)result;
	}
	
	/**
	 * Two specifications are equal if they have exactly the same text.
	 * 
	 * @see Object#equals(Object)
	 */
	public boolean equals(Object obj) {
		if (obj instanceof Specification) {
			return this.text.equals(((Specification)obj).text);
		} else
			return false;
	}

	public String toString() {
		String result = "CoreASM Specification";
		if (absolutePath != null)
			result += ": " + absolutePath;
		return result;
	}
	
	/*
	 * Creates a list of nodes sorted by their
	 * character position
	 */
	private void loadSortedNodes(Node node) {
		insertInSortedList(sortedNodes, node);
		if (node.getFirstCSTNode() != null)
			loadSortedNodes(node.getFirstCSTNode());
		if (node.getNextCSTNode() != null)
			loadSortedNodes(node.getNextCSTNode());
	}
	
	/*
	 * Insert a new node into the sorted list
	 */
	private void insertInSortedList(List<Node> list, Node node) {
		int lastPos = list.size()-1;
		final int pos = node.getScannerInfo().charPosition;
		if (list.isEmpty() || pos >= list.get(lastPos).getScannerInfo().charPosition)
			list.add(node);
		else {
			lastPos = findLastNode(list, pos, 0, list.size() - 1);
			list.add(lastPos, node);
		}
	}
	
	/*
	 * Finds a position in the current list of sorted nodes that 
	 * that is the largest position equal or smaller than the given 'pos'.
	 */
	private int findLastNode(List<Node> list, int pos, int start, int end) {
		if (start == end)
			return start;
		else {
			int midpoint = (start + end) / 2;
			int midNodePos = list.get(midpoint).getScannerInfo().charPosition;
			if (midNodePos > pos) 
				return findLastNode(list, pos, start, midpoint);
			else 
				if (midNodePos == pos)
					return midpoint;
				else 
					if (list.size() > midpoint + 1 && list.get(midpoint+1).getScannerInfo().charPosition > pos)
						return midpoint;
					else
						return findLastNode(list, pos, midpoint + 1, end);
		}
	}
	
	/* 
	 * ------------------------------------
	 *  Static Classes
	 * ------------------------------------
	 */
	
	/**
	 * Metadata for functions.    
	 */
	public static class FunctionInfo {
		
		/** name of the function */
		public final String name;
		
		/** name of the plug-in that provides this function */
		public final String plugin;
		
		/** whether this function is modifiable or not */
		public final boolean isModifiable;
		
		public FunctionInfo(String plugin, String name, FunctionElement f) {
			this.plugin = plugin;
			this.name = name;
			this.isModifiable = f.isModifiable();
		}
	}

	/**
	 * Metadata for universes.    
	 */
	public static class UniverseInfo extends FunctionInfo {
		
		public UniverseInfo(String plugin, String name, UniverseElement u) {
			super(plugin, name, u);
		}
	}

	/**
	 * Metadata for backgrounds.    
	 */
	public static class BackgroundInfo extends FunctionInfo {
		
		public BackgroundInfo(String plugin, String name, BackgroundElement u) {
			super(plugin, name, u);
		}
	}

	/**
	 * Metadata for rules.    
	 */
	public static class RuleInfo {
		
		/** name of the function */
		public final String name;
		
		/** name of the plug-in that provides this function */
		public final String plugin;
		
		public RuleInfo(String plugin, String name, RuleElement r) {
			this.plugin = plugin;
			this.name = name;
		}
	}
	
}


