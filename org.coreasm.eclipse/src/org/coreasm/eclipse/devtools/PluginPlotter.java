package org.coreasm.eclipse.devtools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.coreasm.eclipse.engine.driver.EditorEngineDriver;
import org.coreasm.engine.Engine;
import org.coreasm.engine.VersionInfo;
import org.coreasm.engine.interpreter.Node;
import org.coreasm.engine.parser.GrammarRule;
import org.coreasm.engine.parser.OperatorRule;
import org.coreasm.engine.plugin.InitializationFailedException;
import org.coreasm.engine.plugin.OperatorProvider;
import org.coreasm.engine.plugin.PackagePlugin;
import org.coreasm.engine.plugin.ParserPlugin;
import org.coreasm.engine.plugin.Plugin;
import org.coreasm.engine.plugin.VocabularyExtender;

public class PluginPlotter
{
	public static void plotPlugins(EditorEngineDriver driver, Set<String> plugins)
	{
		PrintWriter out = new PrintWriter(new OutputStreamWriter(System.out));
		plotPlugins(driver, plugins, out);
		out.close();
	}
	
	public static void plotPlugins(EditorEngineDriver driver, Set<String> plugins, File outputFile)
	{
		System.out.println("Plotting plugin data to " + outputFile.getAbsolutePath());
		PrintWriter out;
		try {
			out = new PrintWriter(new FileWriter(outputFile));
			plotPlugins(driver, plugins, out);
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void plotPlugins(EditorEngineDriver driver, Set<String> plugins, PrintWriter out)
	{
		Engine engine = (Engine) driver.getEngine();
		
		out.println();
		out.print("Used Plugins: ");
		for (String pluginname: plugins)
			out.print(pluginname + ", ");
		out.println();
		
		for (String pluginname: plugins)
		{
			Plugin plugin = engine.getPlugin(pluginname);
			out.println("\n-----------------------------------------------\n");
			out.println(pluginname);
			
			if (plugin instanceof ParserPlugin)
			{
				out.println("\n** ParserPlugin **");
				ParserPlugin pp = (ParserPlugin) plugin;
				Map<String, GrammarRule> parsers = pp.getParsers();
				for (String key: parsers.keySet()) {
					GrammarRule rule = parsers.get(key);
					out.println(rule.name + "/" /*+ rule.parser.getName()*/ + ": \t" + rule.body);
					//Parser<Node> parser = rule.parser;
					//out.println(parser.getClass().toString());
				}
			}
			
			if (plugin instanceof VocabularyExtender)
			{
				out.println("\n** VocabularyExtender **");
				VocabularyExtender ve = (VocabularyExtender) plugin;
				out.print("BackgroundNames: ");
				try {
					for (String s: ve.getBackgroundNames())
						out.print(s + ", ");
				} catch (Exception e) {
					out.print("**EXECPTION**");
				}
				out.println();
				out.print("FunctionNames: ");
				try {
					for (String s: ve.getFunctionNames())
						out.print(s + ", ");
				} catch (Exception e) {
					out.print("**EXECPTION**");
				}
				out.println();
				out.print("RuleNames: ");
				try {
					for (String s: ve.getRuleNames())
						out.print(s + ", ");
				} catch (Exception e) {
					out.print("**EXECPTION**");
				}
				out.println();
				out.print("UniverseNames: ");
				try {
					for (String s: ve.getUniverseNames())
						out.print(s + ", ");
				} catch (Exception e) {
					out.print("**EXECPTION**");
				}
				out.println();
			}
			
			if (plugin instanceof OperatorProvider)
			{
				out.println("\n** OperatorProvider **");
				OperatorProvider op = (OperatorProvider) plugin;
				Collection<OperatorRule> oprules = op.getOperatorRules();
				for (OperatorRule oprule: oprules) {
					out.println("Op: " + oprule.getOp() + " -- Op2: " + oprule.getOp2() + " -- Prec: " + oprule.getPrec() + " -- Type: " + oprule.getType());
				}		
			}
		}				

	}
	
	public static void plotPluginDepenencies(EditorEngineDriver driver, List<Plugin> plugins)
	{
		Engine engine = (Engine) driver.getEngine();
		List<Plugin> dependentPlugins = new Vector<Plugin>(plugins);
		
		for (int i=0; i<dependentPlugins.size(); i++) {
			Plugin plugin = dependentPlugins.get(i);
			try {
				plugin.initialize();
			} catch (InitializationFailedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			Set<String> dependencies = new HashSet<String>();
			if (plugin instanceof PackagePlugin)
				dependencies.addAll( ((PackagePlugin) plugin).getEnclosedPluginNames() );
			dependencies.addAll( plugin.getDependencyNames() );
			
			for (String depName: dependencies) {
				Plugin depPlugin = engine.getPlugin(depName);
				if ( ! dependentPlugins.contains(depPlugin)) {
					dependentPlugins.add(depPlugin);
				}
			}
		}
		dependentPlugins.add(dependentPlugins.size(), engine.getPlugin("Kernel"));
		
		for (Plugin p: dependentPlugins)
			System.out.print(p.getName() + ", ");
		System.out.println();
	}
	
}
