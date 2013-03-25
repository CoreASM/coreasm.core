package org.coreasm.eclipse.editors.errors;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.coreasm.eclipse.editors.ASMDocument;
import org.coreasm.eclipse.editors.ASMEditor;
import org.coreasm.eclipse.editors.ASMParser;
import org.coreasm.eclipse.editors.IconManager;
import org.coreasm.eclipse.editors.SlimEngine;
import org.coreasm.engine.ControlAPI;
import org.coreasm.engine.plugin.PackagePlugin;
import org.coreasm.engine.plugin.Plugin;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

/**
 * Checks an ASMDocument for the correct usage of "use" statements.
 * It checks for the following errors:
 * <ul>
 * <li>Is the use statement in its own line?</li>
 * <li>Is there is a plugin with the given name?</li>
 * <li>Has the plugin unresolved dependencies?</li>
 * <li>If the statement part of an multiline comment?</li> 
 * </ul>
 * @author Markus Müller
 */
public class PluginErrorRecognizer 
implements ITextErrorRecognizer
{
	// error code tags
	private static String CLASSNAME = PluginErrorRecognizer.class.getCanonicalName();
	private static String CODE_BEFORE = "CodeBefore";
	private static String CODE_AFTER = "CodeAfter";
	private static String MULTI_COMMENT = "MultiComment";
	private static String NO_PLUGIN = "NoPlugin";
	private static String DEPENDENCY = "Dependency";
	
	// PATTERN_RAW: recognizes any of: use plugin
	// PATTERN_CODE_OK: recognizes lines which contain only a use and whitespaces
	// PATTERN_COMMENT_OK: recognizes lines with an commented use statement: // use plugin
	private static final String PATTERN_RAW = "^[\\s\\t]*use[\\s&&[^\\n]]+(\\w+)";
	//@note inserted^[\\s\\t] to prevent to parse use statements inside comments
	//@warning every use statement must be the first statement of its row
	private static final String PATTERN_CODE_OK = "\\s*" + PATTERN_RAW + "\\s*";
	private static final String PATTERN_COMMENT_OK = "\\s*//.*";
	private static final String PATTERN_LINE = PATTERN_RAW + "[^\\n]*\\n";
	
	private static final String PATTERN_BEFORE = "\\s*";
	private static final String PATTERN_AFTER = "\\s*";
	
	public PluginErrorRecognizer(ASMParser parser) 
	{
		super();
	}

	@Override
	public void checkForErrors(ASMDocument document, List<AbstractError> errors)
	{	
		List<UseStatement> uses = getUses(document);
		for (UseStatement use: uses) {
			
			// build line string
			IRegion lineInfo = null;
			try {
				lineInfo = document.getLineInformationOfOffset(use.position);
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
			if (lineInfo == null)
				continue;
			String line = document.get().substring(lineInfo.getOffset(), lineInfo.getOffset()+lineInfo.getLength());

			// is line commented correctly?
			// -> don't perform any further test
			if (line.matches(PATTERN_COMMENT_OK)) {
				use.willbeloaded = false;
				continue;
			}
			
			// is keyword commented anyway (check partition type)
			// -> line is part of multiline comment
			if ( ! use.partitiontype.equals(ASMEditor.PARTITION_CODE) ) {
				use.willbeloaded = false;
				String title = "\"use\" statement inside multiline comment";
				String message = "An use statement was found inside a multiline comment.\nThe CoreASM engine will still load this plugin.\nUse single line comments to deactivate use statements.";;
				// Mark the whole use statement
				int pos = use.position;
				int length = use.source.length();
				AbstractError error = new SimpleError(title, message, pos, length, CLASSNAME, MULTI_COMMENT);
				errors.add(error);
				// don't perform any further tests
				continue;			
			}
		
			// is line correct (only whitespaces before keyword and after pluginname)
			// -> yes: check if plugin is existing
			// -> no: check if there are non-whitespace chars before keyword or after filename
			if (line.matches(PATTERN_CODE_OK)) {
				// YES:
				checkPluginAvailability(use, errors);
				if (use.plugin == null)
					use.willbeloaded = false;
				else
					use.willbeloaded = true;
			} else {
				// NO:
				checkLineWithError(use, line, errors);
				use.willbeloaded = false;
			}	
		}

		checkPluginDependencies(uses, errors);
	}
	
	/**
	 * Helper method for getting all use statements from the given document.
	 */
	private List<UseStatement> getUses(IDocument document)
	{
		List<UseStatement> uses = new LinkedList<UseStatement>();
		
		String strDoc = document.get();
		Pattern pattern = Pattern.compile(PATTERN_LINE);
		Matcher matcher = pattern.matcher(strDoc);
		
		while (matcher.find() == true) {
			String source = matcher.group(0);
			String name = matcher.group(1);
			source = source.substring(0, matcher.end(1)-matcher.start(0));
			int pos = matcher.start(0);
			int length = source.length();
			String partitiontype = "";
			try {
				ITypedRegion partition = document.getPartition(pos);
				partitiontype = partition.getType();
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
			UseStatement us = new UseStatement(source,name,partitiontype,pos,length);
			uses.add(us);
		}
		
		return uses;
				
	}
	
	
	/**
	 * Helper method for checking lines which have code or comments besides the
	 * use statement.
	 */
	private void checkLineWithError(UseStatement use, String line, List<AbstractError> errors)
	{
		int offset = line.indexOf("use");
		
		// Check if there is something except whitespaces before the use
		String before = line.substring(0, offset);
		if ( ! before.matches(PATTERN_BEFORE)) {
			String title = "Code before \"use\"";
			String message = "There is code or a multiline comment preciding an use statement within the same line.\nThe CoreASM engine will ignore this use statement";
			// Mark everything after the use statement, except leading and trailing whitespaces.
			int spacesbefore = 0;
			while (Character.isWhitespace(before.charAt(spacesbefore)))
				spacesbefore++;
			int spacesafter = 0;
			while (Character.isWhitespace(before.charAt(before.length()-1-spacesafter)))
				spacesafter++;
			int pos = use.position - (before.length() - spacesbefore);
			int length = before.length() - spacesbefore - spacesafter;
			AbstractError error = new SimpleError(title, message, pos, length, CLASSNAME, CODE_BEFORE);
			errors.add(error);
		}
		
		// Check if there is something except whitespaces after the use
		String after = line.substring(offset+use.length);
		if ( ! after.matches(PATTERN_AFTER)) {
			String title = "Code or comment after \"use\"";
			String message = "There is code or a comment following an use statement within the same line.\nThe CoreASM engine will treat this as part of the plugin name.";
			// Mark everything after the use statement, except leading and trailing whitespaces.
			int spacesbefore = 0;
			while (Character.isWhitespace(after.charAt(spacesbefore)))
				spacesbefore++;
			int spacesafter = 0;
			while (Character.isWhitespace(after.charAt(after.length()-1-spacesafter)))
				spacesafter++;
			int pos = use.position + use.length + spacesbefore;
			int length = after.length() - spacesbefore - spacesafter;
			AbstractError error = new SimpleError(title, message, pos, length, CLASSNAME, CODE_AFTER);
			errors.add(error);			
		}

	}


	/**
	 * Helper method to retrieve the Plugin instances for all used plugins.
	 */
	private void checkPluginAvailability(UseStatement use, List<AbstractError> errors)
	{
		String title = "plugin not found";
		
		if ( ! use.partitiontype.equals(ASMEditor.PARTITION_CODE))
			return;
		if (use.plugin == null)
			getPluginObject(use);
		if (use.plugin == null) {
			String msg = "plugin '" + use.pluginname + "' not found";
			int len = 4 + use.pluginname.length();
			AbstractError error = new SimpleError(title, msg, use.position, len, CLASSNAME, NO_PLUGIN);
			errors.add(error);
		}
	}
	
	/**
	 * Helper method to retrieve the Plugin instance for a given use statement.
	 */
	private void getPluginObject(UseStatement useStatement)
	{
		Plugin plugin = null;
		ControlAPI engine = SlimEngine.getFullEngine();
		// ... using full engine, because we want to find all plugins, 
		// including those which aren't loaded by the parser.
		if (engine != null) {
			plugin = engine.getPlugin(useStatement.pluginname);
			if (plugin == null)
				plugin = engine.getPlugin(useStatement.pluginname + "Plugin");
			if (plugin == null)
				plugin = engine.getPlugin(useStatement.pluginname + "Plugins");
		}
		useStatement.plugin = plugin;
	}
	
	/**
	 * Helper method for checking if all plugin dependencies are fullfiled
	 */
	private void checkPluginDependencies(List<UseStatement> uses, List<AbstractError> errors)
	{
		String title = "misssing plugin dependency";
		ControlAPI engine = SlimEngine.getFullEngine();
		// ...using full engine, see above
		
		Set<Plugin> avilPlugins = new HashSet<Plugin>();
		
		// 0. remove all plugins which won't be loaded
		ListIterator<UseStatement> i = uses.listIterator();
		while (i.hasNext()) {
			UseStatement u = i.next();
			if (!u.willbeloaded)
				i.remove();
		}

		// 1. get all plugins which will be loaded (don't forget unpacking PackagePlugins)
		for (UseStatement use: uses) {
			if (use.plugin == null)
				continue;
			if (use.plugin instanceof PackagePlugin) {
				PackagePlugin pp = (PackagePlugin) use.plugin;
				Set<String> plNames = pp.getEnclosedPluginNames();
				for (String name: plNames) {
					Plugin p = engine.getPlugin(name);
					if (p!=null)
						avilPlugins.add(p);
				}
				continue;
			}

			avilPlugins.add(use.plugin);							
		}
		
		// 2. check dependencies for each use
		for (UseStatement use: uses) {
			// 2.1 get dependencies for current use
			if (use.plugin == null)
				continue;
			Set<String> dependencies = new HashSet<String>();
			if (use.plugin instanceof PackagePlugin) {
				PackagePlugin pp = (PackagePlugin) use.plugin;
				for (String depName: pp.getEnclosedPluginNames()) {
					Plugin depPl = engine.getPlugin(depName);
						if (depPl != null)
							dependencies.addAll(depPl.getDependencyNames());
				}
			}
			else {
				dependencies.addAll(use.plugin.getDependencyNames());
			}
			
			// 2.2 check if all dependencies will be loaded
			HashSet<String> missingPl = new HashSet<String>();
			for (String depName: dependencies) {
				Plugin depPl = engine.getPlugin(depName);
				if ( ! avilPlugins.contains(depPl))
					missingPl.add(depName);
			}
			
			// 2.3 create error object with all missing dependencies
			if (missingPl.size() > 0) {
				int offset = use.source.indexOf(use.pluginname);
				int length = use.pluginname.length();
				String msg = "plugin '" + use.pluginname + "' has missing dependencies: ";
				for (String m: missingPl)
					msg += (m + ", ");
				msg = msg.substring(0, msg.length()-2);
				AbstractError error = new SimpleError(title, msg, use.position+offset, length, CLASSNAME, DEPENDENCY);
				errors.add(error);
			}
		}
	}
	
	/**
	 * Delivers a list of QuickFixes depending on the given error type.
	 * @param errorID	the tag of the error type of the error to be fix.
	 * @return			a list of QuickFixes for that error type.
	 */
	public static List<AbstractQuickFix> getQuickFixes(String errorID)
	{
		List<AbstractQuickFix> fixes = new LinkedList<AbstractQuickFix>();
		
		if (errorID.equals(MULTI_COMMENT)) {
			fixes.add(new AbstractQuickFix.QF_Replace("Delete", "", true));
		}
		if (errorID.equals(CODE_BEFORE)) {
			fixes.add(new AbstractQuickFix.QF_Replace("Delete", "", true));
		}
		if (errorID.equals(CODE_AFTER)) {
			fixes.add(new AbstractQuickFix.QF_Replace("Delete", "", true));
		}
		if (errorID.equals(NO_PLUGIN)) {
			fixes.add(new AbstractQuickFix.QF_Replace("Delete", "", true));
		}
		if (errorID.equals(DEPENDENCY)) {
			fixes.add(new QF_Dependency_AddAll());
		}
		
		return fixes;
	}

	
	/**
	 * Helper class for storing use statements with their relevant data
	 * @author Markus M�ller
	 */
	private class UseStatement
	{
		String source;				// the source of the whole use statement (use plugin)
		String pluginname;			// the name of the plugin
		String partitiontype;		// the content type the statement is standing in
		Plugin plugin;				// a reference to the Plugin object
		//Set<Plugin> dependencies;	// the plugins this plugin depends onto
		int position;				// the offset of the use statement within the document
		int length;					// the length of the use statement
		boolean willbeloaded;		// will the engine load this plugin?
		
		public UseStatement(String source, String pluginname, String partitiontype, int position, int length) {
			super();
			this.source = source;
			this.pluginname = pluginname;
			this.partitiontype = partitiontype;
			this.position = position;
			this.length = length;
			//this.dependencies = new HashSet<Plugin>();
			this.plugin = null;
			this.willbeloaded = true;
		}
		
	}
	
	
	/**
	 * Quick fix for adding uses for missing plugins
	 * 
	 * @author Markus
	 */
	public static class QF_Dependency_AddAll
	extends AbstractQuickFix
	{
		public QF_Dependency_AddAll()
		{
			super("Add all missing plugins", null);
		}

		@Override
		public void fix(AbstractError error, String choice)
		{
			if (error instanceof SimpleError) {
				SimpleError sError = (SimpleError) error;
				
				// Parse names of missing plugins out of hover message
				int pos = sError.getDescription().indexOf(':') + 1;
				String strList = sError.getDescription().substring(pos).trim();
				String[] plugins = strList.split(", ");
				
				StringBuilder sb = new StringBuilder();
				for (String pl: plugins) {
					String _pl = pl;
					if (pl.endsWith("Plugin"))
						_pl = pl.substring(0,pl.length()-6);
					else if (pl.endsWith("Plugins"))
						_pl = pl.substring(0,pl.length()-7);
					sb.append("use ").append(_pl).append("\n");
				}
				
				int begin = error.getPosition() - 4;
				int len = 0;
				try {
					error.getDocument().replace(begin,len,sb.toString());
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
				
			}
		}

		@Override
		public void collectProposals(AbstractError error, List<ICompletionProposal> proposals) {
			if (error instanceof SimpleError) {
				SimpleError sError = (SimpleError) error;
				
				// Parse names of missing plugins out of hover message
				int pos = sError.getDescription().indexOf(':') + 1;
				String strList = sError.getDescription().substring(pos).trim();
				String[] plugins = strList.split(", ");
				
				StringBuilder sb = new StringBuilder();
				for (String pl: plugins) {
					String _pl = pl;
					if (pl.endsWith("Plugin"))
						_pl = pl.substring(0,pl.length()-6);
					else if (pl.endsWith("Plugins"))
						_pl = pl.substring(0,pl.length()-7);
					sb.append("use ").append(_pl).append("\n");
				}
				
				proposals.add(new CompletionProposal(sb.toString(), error.getPosition() - 4, 0, 0, IconManager.getIcon("/icons/editor/bullet.gif"), prompt, null, null));
			}
		}
	}

	

}
