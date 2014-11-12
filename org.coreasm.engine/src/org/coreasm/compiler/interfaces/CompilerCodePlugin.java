package org.coreasm.compiler.interfaces;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import org.coreasm.compiler.CodeType;
import org.coreasm.compiler.CoreASMCompiler;
import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.engine.interpreter.ASTNode;

public abstract class CompilerCodePlugin {
	private Mapper handlers;
	
	public abstract void registerCodeHandlers() throws CompilerException;
	
	protected void register(CompilerCodeHandler handler, CodeType type, String gClass, String gRule, String token) throws CompilerException{
		if(handlers == null){
			handlers = new Mapper();
		}
		
		if(!handlers.insert(handler, type, gClass, gRule, token)){
			throw new CompilerException("Handler already registered for (" + type + ", " + gClass + ", " + gRule + ", " + token + ")");
		}
	}
	
	public CodeFragment compile(CodeType t, ASTNode n) throws CompilerException{
		List<Object> h = handlers.find(t, n.getGrammarClass(), n.getGrammarRule(), n.getToken());
		
		if(h.size() == 0){
			throw new CompilerException("no handler registered for (" + t + ", " + n.getGrammarClass() + ", " + n.getGrammarRule() + ", " + n.getToken() + ")");
		}
		else if(h.size() > 1){
			throw new CompilerException("two handlers registered for (" + t + ", " + n.getGrammarClass() + ", " + n.getGrammarRule() + ", " + n.getToken() + ")");			
		}
		
		CompilerCodeHandler current = (CompilerCodeHandler) h.get(0);
		CodeFragment result = new CodeFragment();
		
		current.compile(result, n, CoreASMCompiler.getEngine());
		
		return result;
	}
}

class Mapper{
	private Object def;
	private Map<Object, Object> mappings;
	
	public Mapper(){
		mappings = new HashMap<Object, Object>();
	}
	
	private List<Object> find(int pos, Object...keys){
		List<Object> result = new ArrayList<Object>();
		
		if(def != null){
			if(pos == keys.length - 1) result.add(def);
			else{
				result.addAll(((Mapper)def).find(pos + 1, keys));
			}
		}
		
		Object o = mappings.get(keys[pos]);
		if(o != null){
			if(pos == keys.length - 1) result.add(o);
			else{
				result.addAll(((Mapper)o).find(pos + 1, keys));
			}
		}
		
		return result;
	}
	
	public List<Object> find(Object... keys){
		return find(0, keys);
	}
	
	private boolean insert(int pos, Object o, Object...keys){
		Object k = keys[pos];
		if(pos == keys.length - 1){
			if(k == null){
				if(def != null) return false;
				def = o;
				return true;
			}
			else{	
				if(mappings.get(k) != null) return false;
				mappings.put(k, o);
				return true;
			}
		}
		else{
			if(k == null){
				if(def == null) def = new Mapper();
				
				return ((Mapper) def).insert(pos + 1, o, keys);
			}
			else{
				Object tmp = mappings.get(keys[pos]);
				if(tmp == null){
					tmp = new Mapper();
					mappings.put(keys[pos], tmp);
				}
				
				return ((Mapper) tmp).insert(pos + 1, o, keys);
			}
		}
	}
	
	public boolean insert(Object o, Object... keys){
		return insert(0, o, keys);
	}
}
