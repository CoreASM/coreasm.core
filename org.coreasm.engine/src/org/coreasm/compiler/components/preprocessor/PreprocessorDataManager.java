package org.coreasm.compiler.components.preprocessor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.coreasm.compiler.components.preprocessor.InheritRule;
import org.coreasm.compiler.components.preprocessor.SynthesizeRule;
import org.coreasm.compiler.components.preprocessor.Trigger;
import org.coreasm.compiler.interfaces.CompilerPlugin;
import org.coreasm.compiler.interfaces.CompilerPreprocessorPlugin;
import org.coreasm.engine.interpreter.ASTNode;

/**
 * Helps the preprocessor to find transformers for a node more efficiently.
 * It stores all transformers and maps nodes to a list of triggering transformers.
 * Ignores default behaviours, they are still handled in the preprocessor.
 * @author Markus Brenner
 *
 */
public class PreprocessorDataManager {
	/**
	 * The storage stores responsible storages or a list of transformers.
	 * There are 4 stages of storages (Plugin-name, GrammarClass, GrammarRule, Token),
	 * where the last stage stores an actual list.
	 * <p>
	 * A storage stores other storages, which either
	 * <ul>
	 * <li>are responsible for a specific string or
	 * <li>trigger regardless of the input.
	 * </ul>
	 * @author Markus Brenner
	 *
	 */
	private class Storage{
		private HashMap<String, Storage> data;
		private Storage nullelement;
		private int layer;
		public List<SynthesizeRule> synthRules;
		public List<InheritRule> inheritRules;
		
		public Storage(){
			data = new HashMap<String, Storage>();
			layer = 4;
			nullelement = new Storage(layer - 1);
		}
		
		private Storage(int i){
			layer = i;
			if(i == 0){
				synthRules = new ArrayList<SynthesizeRule>();
				inheritRules = new ArrayList<InheritRule>();
			}
			else{
				data = new HashMap<String, Storage>();
				nullelement = new Storage(i - 1);
			}
		}
		
		public List<InheritRule> getInheritRules(String...value){
			return getInheritRules(0, value);
		}
		
		private List<InheritRule> getInheritRules(int pos, String...value){
			if(pos >= value.length) {
				return inheritRules;
			}
			List<InheritRule> result = new ArrayList<org.coreasm.compiler.components.preprocessor.InheritRule>();
			result.addAll(nullelement.getInheritRules(pos + 1, value));
			if(value[pos] != null){
				Storage nxt = data.get(value[pos]);
				if(nxt != null){
					result.addAll(nxt.getInheritRules(pos + 1, value));
				}
			}
			return result;			
		}
		
		public List<SynthesizeRule> getSynthesizeRules(String...value){
			return getSynthesizeRules(0, value);
		}
		
		private List<SynthesizeRule> getSynthesizeRules(int pos, String...value){
			if(pos >= value.length) {
				return synthRules;
			}
			List<SynthesizeRule> result = new ArrayList<org.coreasm.compiler.components.preprocessor.SynthesizeRule>();
			result.addAll(nullelement.getSynthesizeRules(pos + 1, value));
			if(value[pos] != null){
				Storage nxt = data.get(value[pos]);
				if(nxt != null){
					result.addAll(nxt.getSynthesizeRules(pos + 1, value));
				}
			}
			return result;			
		}
		
		public Storage get(String value){
			if(value == null) return nullelement;
			else{
				Storage tmp = data.get(value);
				if(tmp == null){
					tmp = new Storage(layer - 1);
					data.put(value, tmp);
				}

				return tmp;
			}
		}
	}
	
	
	private Storage root;
	
	/**
	 * Initializes a data manager with a list of preprocessor plugins.
	 * @param plugins A list of preprocessor plugins providing transformers.
	 */
	public PreprocessorDataManager(List<CompilerPlugin> plugins){
		List<SynthesizeRule> synth = new ArrayList<org.coreasm.compiler.components.preprocessor.SynthesizeRule>();
		List<InheritRule> inh = new ArrayList<org.coreasm.compiler.components.preprocessor.InheritRule>();
		for(CompilerPlugin p : plugins){
			CompilerPreprocessorPlugin cpp = (CompilerPreprocessorPlugin) p;
			if(cpp.getSynthesizeRules() != null) synth.addAll(cpp.getSynthesizeRules());
			if(cpp.getInheritRules() != null) inh.addAll(cpp.getInheritRules());
		}
		
		root = new Storage();
		
		for(SynthesizeRule s : synth){
			List<Trigger> triggers = s.getTriggers();
			for(Trigger t : triggers){
				root.get(t.plugin).get(t.grammarClass).get(t.grammarRule).get(t.token).synthRules.add(s);
			}
		}
		
		for(InheritRule s : inh){
			List<Trigger> triggers = s.getTriggers();
			for(Trigger t : triggers){
				root.get(t.plugin).get(t.grammarClass).get(t.grammarRule).get(t.token).inheritRules.add(s);
			}
		}
	}
	
	/**
	 * Determines all triggering synthesize rules for a node n.
	 * @param n A node in the parse tree
	 * @return A list of all triggering synthesize rules
	 */
	public List<SynthesizeRule> getSynthesizeRules(ASTNode n){
		try{
			List<SynthesizeRule> res = root.getSynthesizeRules(n.getPluginName(), n.getGrammarClass(), n.getGrammarRule(), n.getToken());
			return res;
		}
		catch(NullPointerException e){
			return new ArrayList<SynthesizeRule>();
		}
	}
	/**
	 * Determines all triggering inherit rules for a node n.
	 * @param n A node in the parse tree
	 * @return A list of all triggering inherit rules
	 */
	public List<InheritRule> getInheritRules(ASTNode n){
		try{
			List<InheritRule> res = root.getInheritRules(n.getPluginName(), n.getGrammarClass(), n.getGrammarRule(), n.getToken());
			return res;
		}
		catch(NullPointerException e){
			return new ArrayList<InheritRule>();
		}
	}
}
