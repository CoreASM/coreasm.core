package org.coreasm.eclipse.editors.errors;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.coreasm.eclipse.editors.ASMDocument;
import org.coreasm.eclipse.editors.ASMParser;
import org.coreasm.eclipse.editors.IconManager;
import org.coreasm.eclipse.editors.SlimEngine;
import org.coreasm.engine.VersionInfo;
import org.coreasm.engine.plugin.PackagePlugin;
import org.coreasm.engine.plugin.Plugin;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

/**
 * This ErrorRecognizer searches an ASM document for plugin errors.
 * @author Michael Stegmaier
 */
public class PluginErrorRecognizer implements ITextErrorRecognizer
{
	private static String CLASSNAME = PluginErrorRecognizer.class.getCanonicalName();
	public static String NO_PLUGIN = "NoPlugin";
	private static String DEPENDENCY = "Dependency";
	
	public PluginErrorRecognizer(ASMParser parser) 
	{
		super();
	}

	@Override
	public void checkForErrors(ASMDocument document, List<AbstractError> errors)
	{
		Pattern usePattern = Pattern.compile("^[\\s]*[uU][sS][eE][\\s]+");
		
		Set<String> usedPlugins = new HashSet<String>();
		Map<String, Integer> usePositions = new HashMap<String, Integer>();
		Map<String, Integer> lengths = new HashMap<String, Integer>();
		
		for (int i = 0; i < document.getNumberOfLines(); i++) {
			try {
				int pos = document.getLineOffset(i);
				String line = document.get(pos, document.getLineLength(i));
				Matcher useMatcher = usePattern.matcher(line);
				if (useMatcher.find()) {
					String pluginName = useMatcher.replaceFirst("").trim();
					Plugin p = SlimEngine.getFullEngine().getPlugin(pluginName);

					if (p == null) {
						p = SlimEngine.getFullEngine().getPlugin(pluginName + "Plugins");
						if (p == null)
							p = SlimEngine.getFullEngine().getPlugin(pluginName + "Plugin");
					}

					if (p != null) {
						if (usedPlugins.add(p.getName())) {
							usePositions.put(p.getName(), pos + useMatcher.end());
							lengths.put(p.getName(), pluginName.length());
	
							if (p instanceof PackagePlugin)
								usedPlugins.addAll(((PackagePlugin)p).getEnclosedPluginNames());
						}
					}
					else
						errors.add(new SimpleError("Plugin not found", "Plugin '" + pluginName + "' cannot be found.", pos + useMatcher.end(), pluginName.length(), CLASSNAME, NO_PLUGIN));
				}
			} catch (BadLocationException e) {
			}
		}
		for (String pluginName : usedPlugins) {
			Plugin p = SlimEngine.getFullEngine().getPlugin(pluginName);
			Set<String> missingDependencies = checkPluginDependency(usedPlugins, p);
			if (!missingDependencies.isEmpty()) {
				String descr = "";
				for (String missingDependency : missingDependencies) {
					if (!descr.isEmpty())
						descr += ", ";
					descr += missingDependency;
				}
				descr = pluginName + " requires " + descr;
				errors.add(new SimpleError("Plugin Dependency Error", descr, usePositions.get(pluginName), lengths.get(pluginName), CLASSNAME, DEPENDENCY));
			}
		}
	}
	
	private Set<String> checkPluginDependency(Collection<String> usedPlugins, Plugin p) {
		Map<String, VersionInfo> depends = p.getDependencies();
		Set<String> missingDependencies = new HashSet<String>();

		if (depends != null) {
			for (String name : depends.keySet()) {
				if (!usedPlugins.contains(name)) {
					missingDependencies.add(name);
					Set<String> transitiveMissingDependencies = checkPluginDependency(usedPlugins, SlimEngine.getFullEngine().getPlugin(name));
					missingDependencies.addAll(transitiveMissingDependencies);
				}
			}
		}
		
		return missingDependencies;
	}
	
	public static List<AbstractQuickFix> getQuickFixes(String errorID)
	{
		List<AbstractQuickFix> fixes = new LinkedList<AbstractQuickFix>();
		
		if (errorID.equals(DEPENDENCY))
			fixes.add(new QF_Dependency_AddAll());
		
		return fixes;
	}

	/**
	 * Quick fix for adding use clauses for missing plugins
	 * 
	 * @author Michael Stegmaier
	 */
	public static class QF_Dependency_AddAll
	extends AbstractQuickFix
	{
		public QF_Dependency_AddAll()
		{
			super("Add missing dependencies", null);
		}

		@Override
		public void fix(AbstractError error, String choice)
		{
		}

		@Override
		public void collectProposals(AbstractError error, List<ICompletionProposal> proposals) {
			if (error instanceof SimpleError) {
				SimpleError sError = (SimpleError) error;
				
				int pos = sError.getDescription().indexOf("requires") + "requires".length();
				
				String useStatements = "";
				for (String pluginName: sError.getDescription().substring(pos).trim().split(", "))
					useStatements += "use " + pluginName.substring(0, pluginName.indexOf("Plugin")) + "\n";
				
				proposals.add(new CompletionProposal(useStatements, error.getPosition() - 4, 0, 0, IconManager.getIcon("/icons/editor/bullet.gif"), prompt, null, null));
			}
		}
	}

	

}
