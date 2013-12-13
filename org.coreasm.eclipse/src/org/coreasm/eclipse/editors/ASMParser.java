package org.coreasm.eclipse.editors;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Map;
import java.util.Observable;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Parsers;
import org.codehaus.jparsec.error.ParseErrorDetails;
import org.codehaus.jparsec.error.ParserException;
import org.coreasm.engine.ControlAPI;
import org.coreasm.engine.Specification;
import org.coreasm.engine.Specification.FunctionInfo;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.Node;
import org.coreasm.engine.kernel.Kernel;
import org.coreasm.engine.parser.GrammarRule;
import org.coreasm.engine.parser.ParserTools;
import org.coreasm.engine.parser.PositionMap;
import org.coreasm.engine.plugin.PackagePlugin;
import org.coreasm.engine.plugin.ParserPlugin;
import org.coreasm.engine.plugin.Plugin;
import org.coreasm.util.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

/**
 * This class organizes the parsing of CoreASM specifications. It is responsible
 * to create a parser from the CoreASM engine using the grammar of all used plugins,
 * and it is also responsible for running this parser, through its subclass ParsingJob.
 * The class extends the Observable class, so any views and other classes which
 * are interested in the result of each parsing can be notified after each
 * run of the parser.
 * 
 * @author Markus Müller, Michael Stegmaier
 */
public class ASMParser extends Observable implements org.coreasm.engine.parser.Parser
{
	private ASMEditor parentEditor;
	private ASMDocumentProvider documentProvider;
	private Set<String> plugins;		// contains the names of all plugins which will be loaded
	private Set<String> uses;			// contains the names of all plugins with a use clause
	private Parser<Node> rootParser;	// the parser being used for "CoreASM" specs
	private Parser<Node> moduleParser;	// the parser being used for "CoreModule" specs
	private ParsingJob parsingJob;		// the job which starts a new run of the parser.
	private Node rootnode;
	
	private Set<String> currentKeywords;
	private Set<String> currentIDs;
	
	private ControlAPI slimengine;		// for obtaining references for the plugins
		
	private PositionMap positionMap = null;
	
	public ASMParser(ASMEditor parentEditor)
	{
		this.parentEditor = parentEditor;
		this.documentProvider = (ASMDocumentProvider) parentEditor.getDocumentProvider();
		this.parsingJob = new ParsingJob();
		
		currentKeywords = new HashSet<String>();
		currentIDs = new HashSet<String>();
		uses = new HashSet<String>();
		
	}
	
	public ControlAPI getSlimEngine()
	{
		return slimengine;
	}


	// ====================================================
	//	\/ PARSING CODE \/
	// ==================================================== 
	
	/**
	 * Checks if a new parser must be created because the plugins have been changed
	 * since the last run of the parser. If necessary, the new parser is created.
	 */
	private void initParser()
	{		
		Set<String> newPlugins = getUsedPlugins();
		if (newPlugins.equals(plugins))
			return;
		
		Logger.log(Logger.INFORMATION, ASMEditor.LOGGER_UI_DEBUG, "A new parser is generated");
		
		parsingJob.pause();
		
		// create a new slim engine with all the plugins which are (and can be) used.
		slimengine = new SlimEngine(this, newPlugins);
		ControlAPI engine = slimengine;
		
		// The parser is created through the Kernel object, as a "side effect"
		// of the first call of Kernel.getParser()
		Kernel kernel = (Kernel) engine.getPlugin("Kernel");
		GrammarRule rootRule = kernel.getParsers().get("CoreASM");
		rootParser = rootRule.parser;
		
		// The old module parser must be discarded, a new one will be derived from 
		// the new root parser if needed. 
		moduleParser = null;
		
		// getting Keywords & IDs
		collectIDs(engine.getPlugins());
		currentKeywords.clear();
		for (Plugin plugin: engine.getPlugins())
		{
			if (plugin instanceof ParserPlugin)
				for (String keyword: ( (ParserPlugin)plugin).getKeywords() )
					currentKeywords.add(keyword);
		}
		
		parentEditor.setSyntaxHighlighting(currentKeywords, currentIDs);

		// remember the used plugins
		plugins = newPlugins;
		
		parsingJob.unpause();
		
	}
	
	/**
	 * This method runs the parser once. Before running the parser it runs initParser()
	 * to ensure the availability of the correct parser. After parsing it notifies
	 * its observers.
	 */
	private void parseDocument()
	{
		long tStart = System.currentTimeMillis();
		String filename = parentEditor.getInputFile().getProjectRelativePath().toString();
		//System.out.print("=====================================\nparsing... (" + filename + ") ");

		StringBuilder logmsg = new StringBuilder(100);
		logmsg.append("parsed: ").append(filename).append(" ... ");

		ASMDocument doc = (ASMDocument) documentProvider.getDocument(parentEditor.getInput());
		ParsingResult result = null;

		// forget if the document is a module or not, so it will be rechecked
		// on the next call of isIncludedSpecification()
		doc.resetInclusionState();	

		initParser();	// create a new parser if necessary 
		
		positionMap = null;
		try {
			((SlimEngine)slimengine).setSpec(new Specification(slimengine, new StringReader(doc.get()), parentEditor.getInputFile().getLocation().toFile().getAbsolutePath()));
		} catch (IOException e) {
		}
		((SlimEngine)slimengine).notifyEngineParsing();
		
		try {
			ParserTools parserTools = ParserTools.getInstance(slimengine);			
			Parser<Node> parser;
			if (doc.isIncludedSpecification())
				// specification is a module -> run module parser
				parser = getModuleParser().from(parserTools.getTokenizer(), parserTools.getIgnored());
			else
				// specification is not a module -> run root parser
				parser = rootParser.from(parserTools.getTokenizer(), parserTools.getIgnored());
			rootnode = parser.parse(slimengine.getSpec().getText());
			doc.setRootnode(rootnode);
			doc.setControlAPI(slimengine);
			
			//System.out.println("correct");
			logmsg.append("correct ");
			result = new ParsingResult(true, doc, null);	// result for the observers
			
		} catch (ParserException pe) {
			// if we reach this catch block there was an exception during parsing.
			
			ParseErrorDetails perr = pe.getErrorDetails();
			
			rootnode = null;
			doc.setRootnode(null);
			
			if (perr != null) {
				// SYNTAX ERROR (there is a ParseErrorDetails object)
				//System.out.println("SYNTAX ERROR");
				logmsg.append("SYNTAX ERROR ");
			}
			else {
				// UNKNOWN ERROR (there is no ParseErrorDetails object)
				// an unknown error should not appear and usually is an indication
				// for an error in the implementation of the parser.
				// The stack trace is printed for information about the error.
				//System.out.println("unknown error");
				logmsg.append("UNKNOWN ERROR ");
				pe.printStackTrace();
			}
					
			result = new ParsingResult(false, doc, pe);	// result for the observers
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		parentEditor.createPluginMark(uses);
		
		// notify observers
		if (result.wasSuccessful)
			((SlimEngine)slimengine).notifyEngineParsingFinished();
		setChanged();
		notifyObservers(result);

		long tStop = System.currentTimeMillis();
		long t = tStop - tStart;
		//System.out.println("t=" + t + "ms");
		logmsg.append('(').append(t).append("ms)");
		Logger.log(Logger.INFORMATION, Logger.ui, logmsg.toString());
	}
	
	/**
	 * This method derives a module parser from the current root parser.
	 * The EBNF rule for the module parser is
	 * <code>
	 * CoreModule := "CoreModule" ID (UseClause | Header | RuleDeclaration)*
	 * </code>
	 */
	private Parser<Node> getModuleParser()
	{
		if (moduleParser == null) {
			ParserTools parserTools = ParserTools.getInstance(slimengine);
			Kernel kernel = (Kernel) slimengine.getPlugin(AstTools.PLUGIN_KERNEL);
			Map<String,GrammarRule> kernelrules = kernel.getParsers();
			
			Parser<Node> useClauseParser = kernelrules.get(AstTools.PARSER_USE).parser;
			Parser<Node> headerParser = kernel.getParser(AstTools.PARSER_HEADER);
			Parser<Node> ruleParser = kernelrules.get(AstTools.PARSER_RULE).parser;
			
	    	moduleParser = Parsers.array(new Parser[] {
	    			parserTools.getKeywParser("CoreModule", AstTools.PLUGIN_KERNEL),
	    			parserTools.getIdParser(),
	    			parserTools.star(
	    					Parsers.or(
	    							useClauseParser,
	    							headerParser,
	    							//initParser,
	    							ruleParser
	    						)
	    				)
	    			}).map(new org.coreasm.engine.plugins.modularity.CoreModuleParseMap())
	    			.followedBy(Parsers.EOF);
		}
		
		return moduleParser;
	}
	
	/**
	 * This method determines all plugins which will be loaded by the engine if
	 * the specification is run. It only recognizes use clauses which are in their
	 * own line and are not commented. Furthermore it checks all recognized plugins 
	 * for their dependencies and ignores those who have unfulfilled dependencies
	 * (including indirect dependencies). For modules the usage of the Modularity,
	 * String and Number plugins are implicitly assumed (Modularity depends on
	 * String and Number).
	 * 
	 * @return A set of strings with all usable plugin names.
	 */
	private Set<String> getUsedPlugins()
	{
		ControlAPI engine = SlimEngine.getFullEngine();
		
		IDocument doc = documentProvider.getDocument(parentEditor.getInput());
		String strDoc = doc.get();
		
		uses.clear();
		
		Set<String> pluginnames = new HashSet<String>();
		pluginnames.add("Kernel");
		if (((ASMDocument)doc).isIncludedSpecification()) {
			pluginnames.add("ModularityPlugin");
			pluginnames.add("StringPlugin");		// Modularity depends on String
			pluginnames.add("NumberPlugin");		// String depends on Number			
		}
		
		// get plugin names from correct use statements
		// find only those use statements who are in their own line.
		Pattern p = Pattern.compile("^\\s*use\\s+(\\w+)\\s*$", Pattern.MULTILINE);
		Matcher m = p.matcher(strDoc);
		while (m.find() == true) {
			try {
				// ignore commented uses
				if ( ! doc.getContentType(m.start()).equals(ASMEditor.PARTITION_CODE))
					continue;
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
			
			String pluginname = m.group(1);
			pluginnames.add(pluginname);
		}
				
		// get plugin objects for the found plugin names
		Set<Plugin> plugins = new HashSet<Plugin>();
		for (String pluginname: pluginnames) {
			Plugin plugin = engine.getPlugin(pluginname);
			if (plugin == null)
				plugin = engine.getPlugin(pluginname + "Plugin");
			if (plugin == null)
				plugin = engine.getPlugin(pluginname + "Plugins");
			if (plugin != null) {
				plugins.add(plugin);
				uses.add(plugin.getName());
			}
		}
		
		// unpack package plugins
		// collect unpacked plugins in a separate set to prevent ConcurrentModificationException
		// (occurs when adding elements to a HashSet while iterating over it)
		Set<Plugin> unpackedPlugins = new HashSet<Plugin>();
		for (Plugin plugin: plugins) {
			if (plugin instanceof PackagePlugin) {
				PackagePlugin pp = (PackagePlugin) plugin;
				Set<String> set = pp.getEnclosedPluginNames();
				for (String s: set) {
					Plugin pl = engine.getPlugin(s);
					if (pl != null)
						unpackedPlugins.add(pl);
				}
			}
		}
		plugins.addAll(unpackedPlugins);
		
		// The ok-list collects all plugins with fulfilled dependencies
		Set<Plugin> oklist = new HashSet<Plugin>();
		
		// get all plugins with no dependencies and add them to the ok-list
		for (Plugin plugin: plugins) {
			if (plugin.getDependencyNames().size() == 0)
				oklist.add(plugin);
		}
		
		// get all plugins whose plugins are already fulfilled
		// loop this step until no more plugins are found
		boolean flagChanged;
		do {
			flagChanged = false;
			for (Plugin plugin : plugins) {
				if (oklist.contains(plugin))
					continue;
				Set<String> depPl = plugin.getDependencyNames();
				boolean foundAll = true;
				for (String name : depPl) {
					Plugin pl = engine.getPlugin(name);
					if (pl == null || !oklist.contains(pl))
						foundAll = false;
				}
				if (foundAll == true) {
					oklist.add(plugin);
					flagChanged = true;
				}
			}
		} while (flagChanged == true);
		
		Set<String> okNameList = new HashSet<String>();
		for (Plugin pl: oklist)
			okNameList.add(pl.getName());
		
		return okNameList;
	}
	
	/**
	 * Collects all IDs from a set of plugins and stores them in the currentIDs set.
	 */
	private void collectIDs(Set<Plugin> plugins)
	{
		currentIDs.clear();
		for (FunctionInfo functionInfo : SlimEngine.getFullEngine().getSpec().getDefinedFunctions())
			currentIDs.add(functionInfo.name);
		for (FunctionInfo functionInfo : SlimEngine.getFullEngine().getSpec().getDefinedUniverses())
			currentIDs.add(functionInfo.name);
		for (FunctionInfo functionInfo : SlimEngine.getFullEngine().getSpec().getDefinedBackgrounds())
			currentIDs.add(functionInfo.name);
	}
	
	/**
	 * This method checks if a plugin with the given name is currently loaded.
	 * If "String" is passed as parameter, it will return true if at least 
	 * one of "String", "StringPlugin" and "StringPlugins" is contained in
	 * the set of loaded plugins.
	 *  
	 * @param plugin	the name of a plugin
	 * @return			true, if the plugin is currently loaded; false otherwise
	 */
	public boolean isPluginLoaded(String plugin)
	{
		String pluginP = plugin + "Plugin";
		String pluginPP = plugin + "Plugins";
		
		if (plugins.contains(plugin))
			return true;
		if (plugins.contains(pluginP))
			return true;
		if (plugins.contains(pluginPP));
		
		return false;
	}
	
	
	public Set<String> getCurrentKeywords()
	{
		return currentKeywords;
	}
	
	public Set<String> getCurrentIDs()
	{
		return currentIDs;
	}
	
	public ASTNode getRootNode()
	{
		return (ASTNode)rootnode;
	}
	
	public ParsingJob getJob()
	{
		return parsingJob;
	}
	
	
	/**
	 * This class stores the result of a parser run. It is delivered to the
	 * observers of the parser.
	 * @author Markus Müller
	 */
	public class ParsingResult
	{
		public final boolean wasSuccessful;
		public final ASMDocument document;
		public final ParserException exception;
		
		public ParsingResult(boolean wasSuccessful, ASMDocument document,
				ParserException exception) {
			super();
			this.wasSuccessful = wasSuccessful;
			this.document = document;
			this.exception = exception;
		}
		
	}
	
	
	// ====================================================
	//	\/ CODE FOR SCHEDULING SUBCLASS \/
	// ==================================================== 
	
	/**
	 * This class manages the running of the parser. It is derived from the
	 * Job class, so it can be managed by the Eclipse job scheduler. The job
	 * doesn't reschedule itself after parsing, this must be done manually,
	 * and this is done by the ASMEditor class each time an edit occurs.
	 * @author Markus Müller
	 */
	public class ParsingJob
	extends Job 
	{
		private boolean paused;
		private boolean interrupted;
		
		public ParsingJob()
		{
			super("CoreASM Parser");
			this.setPriority(DECORATE);
			this.pause();
		}

		@Override
		protected IStatus run(IProgressMonitor monitor)
		{
			if ( !isPaused() ) 
				parseDocument();

			if ( !isInterrupted() )
				;//this.schedule(UPDATE_CYCLE);

			return Status.OK_STATUS;
		}
		
		public synchronized void pause()
		{
			paused = true;
		}
		
		public synchronized void unpause()
		{
			paused = false;
		}
		
		public synchronized boolean isPaused()
		{
			return paused;
		}
		
		public synchronized void interrupt()
		{
			interrupted = true;
		}
		
		public synchronized boolean isInterrupted()
		{
			return interrupted;
		}

	}


	@Override
	public void setSpecification(Specification specification) {
		throw new UnsupportedOperationException();
		
	}

	@Override
	public void parseHeader() throws org.coreasm.engine.parser.ParserException {
		throw new UnsupportedOperationException();
		
	}

	@Override
	public Set<String> getRequiredPlugins() {
		throw new UnsupportedOperationException();
		
	}

	@Override
	public void parseSpecification()
			throws org.coreasm.engine.parser.ParserException {
		throw new UnsupportedOperationException();
		
	}

	@Override
	public PositionMap getPositionMap() {
		if (positionMap == null) {
			positionMap = new PositionMap(slimengine.getSpec().getText(), 1, 1);
		}
		return positionMap;
	}
	
}
