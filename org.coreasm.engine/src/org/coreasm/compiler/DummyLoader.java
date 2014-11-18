package org.coreasm.compiler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.coreasm.compiler.exception.NotCompilableException;
import org.coreasm.compiler.interfaces.CompilerCodePlugin;
import org.coreasm.compiler.interfaces.CompilerExtensionPointPlugin;
import org.coreasm.compiler.interfaces.CompilerFunctionPlugin;
import org.coreasm.compiler.interfaces.CompilerInitCodePlugin;
import org.coreasm.compiler.interfaces.CompilerOperatorPlugin;
import org.coreasm.compiler.interfaces.CompilerPlugin;
import org.coreasm.compiler.interfaces.CompilerPreprocessorPlugin;
import org.coreasm.compiler.interfaces.CompilerVocabularyExtender;
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
 * Currently a temporary solution, as it replaces the plugins loaded
 * by the CoreASM Engine with newly created CompilerPlugins.
 *  
 * @author Markus Brenner
 *
 */
public class DummyLoader implements PluginLoader {
	private HashMap<String, CompilerPlugin> plugins;
	private HashMap<String, CompilerExtensionPointPlugin> extensions;
	private HashMap<String, CompilerVocabularyExtender> vocabextenders;
	private HashMap<String, CompilerPlugin> replacements;
	private HashMap<String, CompilerInitCodePlugin> initcode;
	private HashMap<String, CompilerOperatorPlugin> operatorplugins;
	private HashMap<String, CompilerFunctionPlugin> functionplugins;
	private HashMap<String, CompilerPreprocessorPlugin> preprocessorplugins;
	private HashMap<String, CompilerCodePlugin> compilercodeplugins;
	private List<String> notCompilable;
	
	/**
	 * Creates a new dummy loader
	 * and initializes the internal data structures
	 */
	public DummyLoader(){
		plugins = new HashMap<String, CompilerPlugin>();
		extensions = new HashMap<String, CompilerExtensionPointPlugin>();
		vocabextenders = new HashMap<String, CompilerVocabularyExtender>();
		initcode = new HashMap<String, CompilerInitCodePlugin>();
		operatorplugins = new HashMap<String, CompilerOperatorPlugin>();
		functionplugins = new HashMap<String, CompilerFunctionPlugin>();
		preprocessorplugins = new HashMap<String, CompilerPreprocessorPlugin>();
		compilercodeplugins = new HashMap<String, CompilerCodePlugin>();
		notCompilable = new ArrayList<String>();
		
		//until coreasmc is merged with coreasm,
		//add new compilable plugins to this list
		replacements = new HashMap<String, CompilerPlugin>();
		/*replacements.put("Kernel", new KernelPlugin());
		replacements.put("BlockRulePlugin", new BlockRulePlugin());
		replacements.put("ConditionalRulePlugin", new ConditionalRulePlugin());
		replacements.put("LetRulePlugin", new LetRulePlugin());
		replacements.put("ExtendRulePlugin", new ExtendRulePlugin());
		replacements.put("ChooseRulePlugin", new ChooseRulePlugin());
		replacements.put("ForallRulePlugin", new ForAllRulePlugin());
		replacements.put("CaseRulePlugin", new CaseRulePlugin());
		replacements.put("PredicateLogicPlugin", new PredicateLogicPlugin());
		replacements.put("NumberPlugin", new NumberPlugin());
		replacements.put("StringPlugin", new StringPlugin());
		replacements.put("CollectionPlugin", new CollectionPlugin());
		replacements.put("SetPlugin", new SetPlugin());
		replacements.put("ListPlugin", new ListPlugin());
		replacements.put("MapPlugin", new MapPlugin());
		replacements.put("IOPlugin", new IOPlugin());
		replacements.put("AbstractionPlugin", new AbstractionPlugin());
		replacements.put("OptionsPlugin", new OptionsPlugin());
		replacements.put("TurboASMPlugin", new TurboASMPlugin());
		replacements.put("SignaturePlugin", new SignaturePlugin());
		replacements.put("MathPlugin", new MathPlugin());
		replacements.put("TimePlugin", new TimePlugin());*/
	}
	
	@Override
	public void loadPlugins(Engine cae) throws NotCompilableException {	
		notCompilable.clear();
		plugins.clear(); // clear leftover plugins first, even though this should never be applicable
		extensions.clear();
		vocabextenders.clear();
		initcode.clear();
		operatorplugins.clear();
		functionplugins.clear();
		preprocessorplugins.clear();
		compilercodeplugins.clear();
		
		//add all plugins which provide code but won't appear in the
		//parse tree body
		for(ICoreASMPlugin icp : cae.getPlugins()){
			if(plugins.get(icp.getName()) != null) continue; //don't load plugins more than once
			
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
					CoreASMCompiler.getEngine().getLogger().error(DummyLoader.class, "plugin " + icp.getName() + " is not compilable but mandatory");
					CoreASMCompiler.getEngine().addError("plugin " + icp.getName() + " is not compilable but mandatory");
					notCompilable.add(icp.getName());
					//throw new NotCompilableException(icp.getName());
				}
				else if(icp instanceof ExtensionPointPlugin){
					CoreASMCompiler.getEngine().addWarning("plugin " + icp.getName() + " is not compilable and might not be required, continuing without it");
				}
			}
		}
		
		
		//walk through the tree and add used plugins
		//this will find Interpreter Plugins and Operator Providers
		List<Plugin> tmp = visitNode(cae, (ASTNode) cae.getSpec().getRootNode());
		for(int i = 0; i < tmp.size(); i++){
			if(plugins.get(tmp.get(i).getName()) == null){
				//add the plugin
				try{
					putPlugin(tmp.get(i), cae);
				}
				catch(NotCompilableException exc){
					if(tmp.get(i) instanceof ExtensionPointPlugin){
						CoreASMCompiler.getEngine().addWarning("plugin " + tmp.get(i).getName() + " is not compilable and might not be required, continuing without it");
					}
					else{
						CoreASMCompiler.getEngine().getLogger().error(DummyLoader.class, "plugin " + tmp.get(i).getName() + " is not compilable but mandatory");
						CoreASMCompiler.getEngine().addError("plugin " + tmp.get(i).getName() + " is not compilable but mandatory");
						notCompilable.add(tmp.get(i).getName());
					}
				}
				//process dependencies and add all dependencies to the list
				for(String dep : tmp.get(i).getDependencyNames()){
					if(plugins.get(dep) == null){
						ICoreASMPlugin depplugin = cae.getPlugin(dep);
						try{
							putPlugin(depplugin, cae);
						}
						catch(NotCompilableException e){
							if(depplugin instanceof ExtensionPointPlugin){
								CoreASMCompiler.getEngine().addWarning("plugin " + depplugin.getName() + " is not compilable and might not be required, continuing without it");
							}
							else{
								CoreASMCompiler.getEngine().getLogger().error(DummyLoader.class, "plugin " + depplugin.getName() + " is not compilable but mandatory");
								CoreASMCompiler.getEngine().addError("plugin " + depplugin.getName() + " is not compilable but mandatory");
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

		CoreASMCompiler.getEngine().getLogger().debug(DummyLoader.class, "Loaded all plugins successfully");
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
			CoreASMCompiler.getEngine().getLogger().error(DummyLoader.class, "replacing plugin " + name + " with dummy plugin");
			return result;
		}
		
		ICoreASMPlugin icap = cae.getPlugin(name);
		CompilerPlugin comp = icap.getCompilerPlugin();
		
		return comp;
		//if(icap instanceof CompilerPlugin) return (CompilerPlugin) icap;
		//return null;		
	}
	
	private void putPlugin(ICoreASMPlugin icap, Engine cae) throws NotCompilableException{
		CompilerPlugin cp = requestPlugin(icap.getName(), cae);
		
		if(cp == null){
			throw new NotCompilableException(null);
		}
		
		//add the compiler plugin to all relevant special plugin lists
		if(cp instanceof CompilerExtensionPointPlugin) extensions.put(icap.getName(), (CompilerExtensionPointPlugin) cp);
		if(cp instanceof CompilerVocabularyExtender) vocabextenders.put(icap.getName(), (CompilerVocabularyExtender) cp);
		if(cp instanceof CompilerInitCodePlugin) initcode.put(icap.getName(), (CompilerInitCodePlugin) cp);
		if(cp instanceof CompilerOperatorPlugin) operatorplugins.put(icap.getName(), (CompilerOperatorPlugin) cp);
		if(cp instanceof CompilerFunctionPlugin) functionplugins.put(icap.getName(), (CompilerFunctionPlugin) cp);
		if(cp instanceof CompilerPreprocessorPlugin) preprocessorplugins.put(icap.getName(), (CompilerPreprocessorPlugin) cp);
		if(cp instanceof CompilerCodePlugin) compilercodeplugins.put(icap.getName(), (CompilerCodePlugin) cp);
		
		//finally, add it to the result list and to the list of ICoreASMPlugins
		plugins.put(icap.getName(), cp);
		
		CoreASMCompiler.getEngine().getLogger().debug(DummyLoader.class, "loaded " + icap.getName());
	}
	
	@Override
	public CompilerPlugin getPlugin(String name) {
		if(name == null){
			CoreASMCompiler.getEngine().getLogger().debug(DummyLoader.class, "Warning: null name found, assuming kernel");
			
			return plugins.get("Kernel");
		}

		return plugins.get(name);
	}
	
	private List<Plugin> visitNode(Engine cae, ASTNode n){
		List<Plugin> pluginList = new ArrayList<Plugin>();
		
		if(n.getPluginName() != null){
			pluginList.add(cae.getPlugin(n.getPluginName()));
		}
		else{
			CoreASMCompiler.getEngine().getLogger().warn(DummyLoader.class, "Found null plugin, replacing with kernel");
			pluginList.add(cae.getPlugin("Kernel"));
		}
			
		for(Iterator<ASTNode> it = n.getAbstractChildNodes().iterator(); it.hasNext(); ){
			pluginList.addAll(visitNode(cae, it.next()));
		}
		
		return pluginList;
	}

	@Override
	public List<CompilerVocabularyExtender> getVocabularyExtenderPlugins() {
		List<CompilerVocabularyExtender> p = new ArrayList<CompilerVocabularyExtender>();
		for(Entry<String, CompilerVocabularyExtender> s : vocabextenders.entrySet()){
			p.add(s.getValue());
		}
		return p;
	}

	@Override
	public List<CompilerExtensionPointPlugin> getExtensionPointPlugins() {
		List<CompilerExtensionPointPlugin> p = new ArrayList<CompilerExtensionPointPlugin>();
		for(Entry<String, CompilerExtensionPointPlugin> s : extensions.entrySet()){
			p.add(s.getValue());
		}
		return p;
	}
	
	@Override
	public List<CompilerInitCodePlugin> getInitCodePlugins(){
		List<CompilerInitCodePlugin> p = new ArrayList<CompilerInitCodePlugin>();
		for(Entry<String, CompilerInitCodePlugin> s : initcode.entrySet()){
			p.add(s.getValue());
		}
		return p;
	}
	
	@Override
	public List<CompilerOperatorPlugin> getOperatorPlugins(){
		List<CompilerOperatorPlugin> p = new ArrayList<CompilerOperatorPlugin>();
		for(Entry<String, CompilerOperatorPlugin> s : operatorplugins.entrySet()){
			p.add(s.getValue());
		}
		return p;
	}
	
	@Override
	public List<CompilerFunctionPlugin> getFunctionPlugins(){
		List<CompilerFunctionPlugin> p = new ArrayList<CompilerFunctionPlugin>();
		for(Entry<String, CompilerFunctionPlugin> s : functionplugins.entrySet()){
			p.add(s.getValue());
		}
		return p;
	}
	
	@Override
	public List<CompilerPreprocessorPlugin> getPreprocessorPlugins(){
		List<CompilerPreprocessorPlugin> p = new ArrayList<CompilerPreprocessorPlugin>();
		for(Entry<String, CompilerPreprocessorPlugin> s : preprocessorplugins.entrySet()){
			p.add(s.getValue());
		}
		return p;
	}
	
	@Override
	public List<CompilerCodePlugin> getCompilerCodePlugins(){
		List<CompilerCodePlugin> p = new ArrayList<CompilerCodePlugin>();
		for(Entry<String, CompilerCodePlugin> s : compilercodeplugins.entrySet()){
			p.add(s.getValue());
		}
		return p;
	}
}
