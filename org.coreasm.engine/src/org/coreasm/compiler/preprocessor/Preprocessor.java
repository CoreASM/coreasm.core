package org.coreasm.compiler.preprocessor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.interfaces.CompilerPreprocessorPlugin;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.compiler.preprocessor.Information;
import org.coreasm.compiler.preprocessor.InheritRule;
import org.coreasm.compiler.preprocessor.Preprocessor;
import org.coreasm.compiler.preprocessor.PreprocessorDataManager;
import org.coreasm.compiler.preprocessor.SynthesizeRule;

/**
 * The preprocessor provides an analysis framework based on attributed grammars.
 * Plugins can contribute synthetization or inheritance rules and build complex analyses for
 * error detection/correction or optimization.
 * The plugin developer has to ensure that no cyclic rules are introduced into the analysis,
 * as the preprocessor doesn't check for cycles and might fail to terminate.
 * Plugins can also contribute default rules for analyses, to bridge possibly unimplemented parts
 * arising from newly implemented plugins.
 * 
 * @author Markus Brenner
 *
 */
public class Preprocessor {
	private Map<ASTNode, Map<String, Information>> informationMap;
	private Map<String, SynthesizeRule> defaultSynthBehaviour;
	private Map<String, InheritRule> defaultInheritBehaviour;
	private ASTNode root;
	private PreprocessorDataManager manager;
	private CompilerEngine engine;

	/**
	 * Initializes a new preprocessor.
	 * The preprocessor is not ready to provide information, until the loadPlugins and preprocessSpecification methods have been called.
	 */
	public Preprocessor(CompilerEngine engine) {
		this.engine = engine;
		informationMap = new HashMap<ASTNode, Map<String, Information>>();
		defaultSynthBehaviour = new HashMap<String, SynthesizeRule>();
		defaultInheritBehaviour = new HashMap<String, InheritRule>();
		root = null;
	}
	
	/**
	 * Finds the information mapping stored about the given node.
	 * Returns an empty Map if there is no information stored.
	 * @param n A node in the parse tree
	 * @return A Map containing information
	 */
	public Map<String, Information> getNodeInformation(ASTNode n){
		Map<String, Information> result = informationMap.get(n);
		if(result == null) return new HashMap<String, Information>();
		
		return result;
	}
	
	/**
	 * Loads a list of preprocessor plugins providing rules and default behaviours.
	 * @param list A list of preprocessor plugins
	 * @throws Exception If a plugin tries to register a default behaviour for a name which has already been registered
	 */
	public void loadPlugins(List<CompilerPreprocessorPlugin> list) throws Exception {
		engine.getLogger().debug(Preprocessor.class, "loading preprocessor plugins");
		manager = new PreprocessorDataManager(list);
		
		for (CompilerPreprocessorPlugin p : list) {
			//transformers.addAll(p.getTransformers());
			if(p.getSynthDefaultBehaviours() != null){
				//Main.getEngine().addWarning("warning : plugin " + ((CompilerPlugin) p).getName() + " provided a null synth behaviour list");
				for (Entry<String, SynthesizeRule> d : p.getSynthDefaultBehaviours().entrySet()) {
					if (defaultSynthBehaviour.containsKey(d.getKey())){
						engine.addError("Preprocessor: a default synth behaviour for information '"	+ d.getKey() + "' was already registered");
						throw new Exception("a default synth behaviour for information '"	+ d.getKey() + "' was already registered");
					}
					engine.getLogger().debug(Preprocessor.class, "loaded default synthesize behaviour for entry '" + d.getKey() + "'");
					defaultSynthBehaviour.put(d.getKey(), d.getValue());
				}
			}
			
			if(p.getInheritDefaultBehaviours() != null){
				//Main.getEngine().addWarning("warning : plugin " + ((CompilerPlugin) p).getName() + " provided a null inherit behaviour list");
				for (Entry<String, InheritRule> d : p.getInheritDefaultBehaviours().entrySet()) {
					if (defaultInheritBehaviour.containsKey(d.getKey())){
						engine.addError("Preprocessor: a default inherit behaviour for information '"	+ d.getKey() + "' was already registered");
						throw new Exception("a default inherit behaviour for information '"	+ d.getKey() + "' was already registered");
					}
					engine.getLogger().debug(Preprocessor.class, "loaded default inheritance behaviour for entry '" + d.getKey() + "'");
					defaultInheritBehaviour.put(d.getKey(), d.getValue());
				}
			}
			
		}
	}
	
	/**
	 * Gets the information mapping of the root node
	 * @return The mapping of the root node
	 */
	public Map<String, Information> getGeneralInfo(){
		return this.informationMap.get(root);
	}
	
	private boolean mergeInformation(Map<String, Information> original, Map<String, Information> additional){
		//merge all changes in the map additional into the map original and return true, if any changes to original were made
		boolean result = false;
		for(Entry<String, Information> e : additional.entrySet()){
			if(original.containsKey(e.getKey())){
				//when a key already exists, only put it, if it differs from the previous value				
				Information newInf = e.getValue();
				Information oldInf = original.get(e.getKey());
		
				Information comp = newInf;
				Information other = oldInf;
				if(comp == null){
					comp = oldInf;
					other = null;
				}
				
				if(comp != null && !comp.equals(other)){
					result = true;
					
					original.put(e.getKey(), e.getValue());
				}
			}
			else{
				original.put(e.getKey(), e.getValue());
				result = true;
			}
		}
		return result;
	}

	private boolean processBottomUp(ASTNode root){
		//process the specification bottom up, generating synthesized information
		
		boolean changed = false;
		//preprocess the child nodes first
		List<Map<String, Information>> children = new ArrayList<Map<String, Information>>();
		for(ASTNode n : root.getAbstractChildNodes()){
			changed = processBottomUp(n) || changed;
			children.add(informationMap.get(n));
		}
		
		Map<String, Information> current = new HashMap<String, Information>();
		List<String> colliding = new ArrayList<String>();
		
		for(SynthesizeRule t : manager.getSynthesizeRules(root)){
			Map<String, Information> res = t.transform(root, children);
			if(res == null) continue;
			colliding.addAll(findColliding(current, res));
			current.putAll(res);
		}
		
		for(String s : colliding){
			System.out.println("warning, polluted entry for " + s);
			engine.addWarning("warning: colliding entries in preprocessor for entry " + s);
			current.remove(s);
		}
		colliding.clear();
		
		for(Map<String, Information> c : children){
			for(Entry<String, Information> e : c.entrySet()){
				if(!current.containsKey(e.getKey())){
					SynthesizeRule def = defaultSynthBehaviour.get(e.getKey());
					if(def != null){
						Map<String, Information> generated = def.transform(root, children);
						if(generated == null) continue;
						colliding.addAll(findColliding(current, generated));
						current.putAll(generated);
					}
				}
			}
		}
		
		for(String s : colliding){
			current.remove(s);
		}
		
		//merge the new information with the generated information
		Map<String, Information> old = informationMap.get(root);
		if(old == null) old = new HashMap<String, Information>();
		
		changed = mergeInformation(old, current) || changed;
		
		informationMap.put(root, old);
		
		return changed;
	}
	
	private boolean processTopDown(ASTNode root){
		//process the specification top down, generating inherited information
		Map<String, Information> currentNode = informationMap.get(root);
		if(currentNode == null){
			currentNode = new HashMap<String, Information>();
		}
		
		boolean changed = false;
		
		List<Map<String, Information>> current = new ArrayList<Map<String, Information>>();
		List<List<String>> colliding = new ArrayList<List<String>>();
		for(int i = 0; i < root.getAbstractChildNodes().size(); i++){
			current.add(new HashMap<String, Information>());
			colliding.add(new ArrayList<String>());
		}
		
		//apply rules
		for(InheritRule t : manager.getInheritRules(root)){
			List<Map<String, Information>> res = t.transform(root, currentNode);
			if(res == null || res.size() != root.getAbstractChildNodes().size()){
				engine.addWarning("warning: wrong size of return list in preprocessor inherit rule for node (" + root.getPluginName() + ", " + root.getGrammarClass() + ", " + root.getGrammarRule() + ", " + root.getToken() + ")");
				continue;
			}
			else{
				for(int i = 0; i < res.size(); i++){
					Map<String, Information> m = res.get(i);
					if(m == null) continue;
					
					//sort the generated information into the list					
					colliding.get(i).addAll(findColliding(current.get(i), m));
					current.get(i).putAll(m);
				}
			}
		}
		
		//remove colliding entries
		for(int i = 0; i < colliding.size(); i++){
			for(String s : colliding.get(i)){
				System.out.println("warning, polluted entry for " + s);
				engine.addWarning("warning: colliding entries in preprocessor for entry " + s);
				current.get(i).remove(s);
			}
			colliding.get(i).clear();
		}
		
		//default behaviours; note that inherit default behaviours cannot create colliding entries
		for(Entry<String, Information> e : currentNode.entrySet()){
			InheritRule def = defaultInheritBehaviour.get(e.getKey());
			if(def != null){
				List<Map<String, Information>> defres = def.transform(root, currentNode);
				if(defres == null || defres.size() != root.getAbstractChildNodes().size()) continue;
				
				for(int i = 0; i < defres.size(); i++){
					if(defres.get(i) == null) continue;
					//this loop should actually have only one run, as the returned entry by the
					//default behaviour should only fill the gap left for e.getKey()
					for(Entry<String, Information> defentry : defres.get(i).entrySet()){
						if(!current.get(i).containsKey(defentry.getKey())){
							current.get(i).put(defentry.getKey(), defentry.getValue());
						}
					}
				}
			}
		}
		
		//finally, merge the information with the child nodes and proceed further down the three
		for(int i = 0; i < root.getAbstractChildNodes().size(); i++){
			ASTNode next = root.getAbstractChildNodes().get(i);
			Map<String, Information> nmap = informationMap.get(next);
			if(nmap == null) nmap = new HashMap<String, Information>();
			
			changed = mergeInformation(nmap, current.get(i)) || changed;
			informationMap.put(next, nmap);
			changed = processTopDown(next) || changed;
		}
		
		return changed;
	}
	
	/**
	 * Preprocesses a specification starting at the given node.
	 * Might not terminate, if a cycle exists in the rules provided by plugins
	 * @param specRoot The root of the specification
	 */
	public void preprocessSpecification(ASTNode specRoot) throws Exception{
		if(root == null) root = specRoot;
		int runCounter = 0;
		boolean changed = false;
		do{
			changed = false;
			changed = processBottomUp(specRoot) || changed;
			changed = processTopDown(specRoot) || changed;
			
			runCounter++;
			if(runCounter == engine.getOptions().preprocessorRuns){
				engine.addError("Preprocessor exceeded maximum run duration");
				throw new Exception("Preprocessor exceeded maximum run duration");
			}
		}while(changed);
	}

	private List<String> findColliding(Map<String, Information> current,
			Map<String, Information> generated) {
		List<String> polluted = new ArrayList<String>();
		for (Entry<String, Information> e : generated.entrySet()) {
			if (current.containsKey(e.getKey())
					&& !current.get(e.getKey()).equals(e.getValue())) {
				if (!polluted.contains(e.getKey()))
					polluted.add(e.getKey());
			}
		}
		return polluted;
	}
}
