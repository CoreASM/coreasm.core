package org.coreasm.compiler.components.pluginloader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.exception.NotCompilableException;
import org.coreasm.compiler.interfaces.CompilerPlugin;
import org.coreasm.engine.Engine;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.plugin.Aggregator;
import org.coreasm.engine.plugin.ExtensionPointPlugin;
import org.coreasm.engine.plugin.OperatorProvider;
import org.coreasm.engine.plugin.Plugin;
import org.coreasm.engine.plugin.SchedulerPlugin;
import org.coreasm.engine.plugin.VocabularyExtender;
import org.coreasm.engine.registry.ICoreASMPlugin;

/*import de.spellmaker.coreasmc.plugins.dummy.abstractionplugin.AbstractionPlugin;
import de.spellmaker.coreasmc.plugins.dummy.blockruleplugin.BlockRulePlugin;
import de.spellmaker.coreasmc.plugins.dummy.caseruleplugin.CaseRulePlugin;
import de.spellmaker.coreasmc.plugins.dummy.chooseruleplugin.ChooseRulePlugin;
import de.spellmaker.coreasmc.plugins.dummy.collection.CollectionPlugin;
import de.spellmaker.coreasmc.plugins.dummy.conditionalruleplugin.ConditionalRulePlugin;
import de.spellmaker.coreasmc.plugins.dummy.extendruleplugin.ExtendRulePlugin;
import de.spellmaker.coreasmc.plugins.dummy.forallruleplugin.ForAllRulePlugin;
import de.spellmaker.coreasmc.plugins.dummy.ioplugin.IOPlugin;
import de.spellmaker.coreasmc.plugins.dummy.kernel.KernelPlugin;
import de.spellmaker.coreasmc.plugins.dummy.letruleplugin.LetRulePlugin;
import de.spellmaker.coreasmc.plugins.dummy.listplugin.ListPlugin;
import de.spellmaker.coreasmc.plugins.dummy.mapplugin.MapPlugin;
import de.spellmaker.coreasmc.plugins.dummy.mathplugin.MathPlugin;
import de.spellmaker.coreasmc.plugins.dummy.numberplugin.NumberPlugin;
import de.spellmaker.coreasmc.plugins.dummy.optionsplugin.OptionsPlugin;
import de.spellmaker.coreasmc.plugins.dummy.predicatelogicplugin.PredicateLogicPlugin;
import de.spellmaker.coreasmc.plugins.dummy.setplugin.SetPlugin;
import de.spellmaker.coreasmc.plugins.dummy.signatureplugin.SignaturePlugin;
import de.spellmaker.coreasmc.plugins.dummy.stringplugin.StringPlugin;
import de.spellmaker.coreasmc.plugins.dummy.timeplugin.TimePlugin;
import de.spellmaker.coreasmc.plugins.dummy.turboasmplugin.TurboASMPlugin;*/


/**
 * Implementation of the Plugin Loader interface.
 * Loads and manages plugins used by a specification
 *  
 * @author Markus Brenner
 *
 */
public class DummyLoader implements PluginLoader {
	private Map<String, CompilerPlugin> allPlugins;
	private Map<Class<?>, Map<String, CompilerPlugin>> pluginMap;
	private HashMap<String, CompilerPlugin> replacements;
	private List<String> notCompilable;
	private CompilerEngine engine;
	
	/**
	 * Creates a new dummy loader
	 * and initializes the internal data structures
	 * @param engine The compiler engine supervising the compilation process
	 */
	public DummyLoader(CompilerEngine engine){
		this.engine = engine;
		allPlugins = new HashMap<String, CompilerPlugin>();
		pluginMap = new HashMap<Class<?>, Map<String,CompilerPlugin>>();
		notCompilable = new ArrayList<String>();
		
		//until coreasmc is merged with coreasm,
		//add new compilable plugins to this list
		replacements = new HashMap<String, CompilerPlugin>();
	}
	
	@Override
	public void loadPlugins(Engine cae) throws NotCompilableException {	
		notCompilable.clear();
		allPlugins.clear();
		pluginMap.clear();
		
		//add all plugins which provide code but won't appear in the
		//parse tree body
		for(ICoreASMPlugin icp : cae.getPlugins()){
			if(allPlugins.get(icp.getName()) != null) continue; //don't load plugins more than once
			
			try{
				//try to load the plugin
				putPlugin(icp, cae);
			}
			catch(NotCompilableException e){
				//only throw an error, if the uncompilable plugin is of one of the following types
				//otherwise, the plugin is not relevant for the compilation anyway
				if(icp instanceof Aggregator ||
						icp instanceof SchedulerPlugin ||
						icp instanceof OperatorProvider ||
						icp instanceof VocabularyExtender){		
					engine.getLogger().error(DummyLoader.class, "plugin " + icp.getName() + " is not compilable but mandatory");
					engine.addError("plugin " + icp.getName() + " is not compilable but mandatory");
					notCompilable.add(icp.getName());
					//throw new NotCompilableException(icp.getName());
				}
				else if(icp instanceof ExtensionPointPlugin){
					engine.addWarning("plugin " + icp.getName() + " is not compilable and might not be required, continuing without it");
				}
			}
		}
		
		
		//walk through the tree and add used plugins
		//this will find Interpreter Plugins and Operator Providers
		List<Plugin> tmp = visitNode(cae, (ASTNode) cae.getSpec().getRootNode());
		for(int i = 0; i < tmp.size(); i++){
			if(allPlugins.get(tmp.get(i).getName()) == null){
				//add the plugin
				try{
					putPlugin(tmp.get(i), cae);
				}
				catch(NotCompilableException exc){
					if(tmp.get(i) instanceof ExtensionPointPlugin){
						engine.addWarning("plugin " + tmp.get(i).getName() + " is not compilable and might not be required, continuing without it");
					}
					else{
						engine.getLogger().error(DummyLoader.class, "plugin " + tmp.get(i).getName() + " is not compilable but mandatory");
						engine.addError("plugin " + tmp.get(i).getName() + " is not compilable but mandatory");
						notCompilable.add(tmp.get(i).getName());
					}
				}
				//process dependencies and add all dependencies to the list
				for(String dep : tmp.get(i).getDependencyNames()){
					if(allPlugins.get(dep) == null){
						ICoreASMPlugin depplugin = cae.getPlugin(dep);
						try{
							putPlugin(depplugin, cae);
						}
						catch(NotCompilableException e){
							if(depplugin instanceof ExtensionPointPlugin){
								engine.addWarning("plugin " + depplugin.getName() + " is not compilable and might not be required, continuing without it");
							}
							else{
								engine.getLogger().error(DummyLoader.class, "plugin " + depplugin.getName() + " is not compilable but mandatory");
								engine.addError("plugin " + depplugin.getName() + " is not compilable but mandatory");
								notCompilable.add(depplugin.getName());
							}
						}
					}
				}
			}
		}
		
		if(notCompilable.size() > 0){
			throw new NotCompilableException(notCompilable);
		}

		engine.getLogger().debug(DummyLoader.class, "Loaded all plugins successfully");
	}

	/**
	 * request a plugin from the engine, replacing it with a replacement if necessary
	 * @param name The name of the plugin
	 * @return The plugin or null, if not of type CompilerPlugin
	 */
	private CompilerPlugin requestPlugin(String name, Engine cae){
		CompilerPlugin result = replacements.get(name);
		if(result != null){
			//adding a warning for this only makes the output unreadable in the current implementation
			//Main.getEngine().addWarning("replaced plugin " + name + " with a replacement plugin");
			engine.getLogger().error(DummyLoader.class, "replacing plugin " + name + " with dummy plugin");
			return result;
		}
		
		ICoreASMPlugin icap = cae.getPlugin(name);
		CompilerPlugin comp = icap.getCompilerPlugin();
		
		return comp;
		//if(icap instanceof CompilerPlugin) return (CompilerPlugin) icap;
		//return null;		
	}
	
	private void addToMap(Class<?> type, CompilerPlugin plugin){
		Map<String, CompilerPlugin> m = pluginMap.get(type);
		if(m == null){
			m = new HashMap<String, CompilerPlugin>();
			pluginMap.put(type, m);
		}
		m.put(plugin.getName(), plugin);
	}
	
	private void putPlugin(ICoreASMPlugin icap, Engine cae) throws NotCompilableException{
		CompilerPlugin cp = requestPlugin(icap.getName(), cae);
		
		if(cp == null){
			throw new NotCompilableException(null);
		}
		
		//add the plugin to the pluginMap, allowing to choose plugins by plugin interface / class
		Class<?> pluginClass = cp.getClass();
		
		//handle superclasses
		Class<?> superClass = pluginClass.getSuperclass();
		while(!superClass.equals(Object.class)){
			addToMap(superClass, cp);
			superClass = superClass.getSuperclass();
		}
		//handle interfaces
		for(Class<?> c : pluginClass.getInterfaces()){
			addToMap(c, cp);
		}
		
		//finally, add it to the allPlugin list and to the list of ICoreASMPlugins
		allPlugins.put(icap.getName(), cp);
		cp.init(engine);
		
		engine.getLogger().debug(DummyLoader.class, "loaded " + icap.getName());
	}
	
	@Override
	public CompilerPlugin getPlugin(String name) {
		if(name == null){
			//NOTE: This actually hides a bug in the parser. In some cases, the plugin name field is null instead of kernel
			//engine.getLogger().warn(DummyLoader.class, "Warning: null name found, assuming kernel");
			
			return allPlugins.get("Kernel");
		}

		return allPlugins.get(name);
	}
	
	private List<Plugin> visitNode(Engine cae, ASTNode n){
		List<Plugin> pluginList = new ArrayList<Plugin>();
		
		if(n.getPluginName() != null){
			pluginList.add(cae.getPlugin(n.getPluginName()));
		}
		else{
			//engine.getLogger().warn(DummyLoader.class, "Found null plugin, replacing with kernel");
			pluginList.add(cae.getPlugin("Kernel"));
		}
			
		for(Iterator<ASTNode> it = n.getAbstractChildNodes().iterator(); it.hasNext(); ){
			pluginList.addAll(visitNode(cae, it.next()));
		}
		
		return pluginList;
	}

	@Override
	public List<CompilerPlugin> getPluginByType(Class<?> type){
		Map<String, CompilerPlugin> m = pluginMap.get(type);
		if(m == null){
			return new ArrayList<CompilerPlugin>();
		}
		List<CompilerPlugin> result = new ArrayList<CompilerPlugin>(m.size());
		for(Entry<String, CompilerPlugin> s : m.entrySet()){
			result.add(s.getValue());
		}
		return result;
	}
	
}
