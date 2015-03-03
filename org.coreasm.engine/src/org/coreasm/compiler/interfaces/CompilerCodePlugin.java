package org.coreasm.compiler.interfaces;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import org.coreasm.compiler.CodeType;
import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.engine.interpreter.ASTNode;

/**
 * Base class for code providing plugins.
 * Code providing plugins create and register handlers for code constructs. This base class
 * encapsulates the management of the handlers, activating them as the compiler requests compilation
 * of nodes in the syntax tree.
 * @author Spellmaker
 *
 */
public abstract class CompilerCodePlugin implements CompilerPlugin{
	private Mapper handlers;
	protected CompilerEngine engine;
	
	/**
	 * Registers code handlers of this plugin
	 * @throws CompilerException
	 */
	public abstract void registerCodeHandlers() throws CompilerException;
	
	/**
	 * Registers a code handler in the data structures of the abstract base class.
	 * This method is called by the compiler when initializing the code plugins.
	 * Code handlers will be activated to produce code, when the node handed over by the compiler
	 * matches the pattern the handler was registered for.
	 * Only one handler may match a pattern.
	 * @param handler The code handler instance
	 * @param type The CodeType for which this handler will activate, or null, if irrelevant
	 * @param gClass The grammar class for which this handler will activate or null, if irrelevant
	 * @param gRule The grammar rule for which this handler will activate or null, if irrelevant
	 * @param token The token for which this handler will activate or null, if irrelevant
	 * @throws CompilerException If the compilation had errors
	 */
	protected void register(CompilerCodeHandler handler, CodeType type, String gClass, String gRule, String token) throws CompilerException{
		if(handlers == null){
			handlers = new Mapper();
		}
		
		if(!handlers.insert(handler, type, gClass, gRule, token)){
			throw new CompilerException("Handler already registered for (" + type + ", " + gClass + ", " + gRule + ", " + token + ")");
		}
	}
	
	/**
	 * Compiles the given node.
	 * The abstract base class will search for a handler registered for the node pattern and will
	 * throw an exception, if not exactly one handler is responsible for the node
	 * @param t The requested CodeType
	 * @param n The node for which code is to be produced
	 * @return Code for the compiled node
	 * @throws CompilerException If the compilation failed or if less or more than one handler was found for the node
	 */
	public CodeFragment compile(CodeType t, ASTNode n) throws CompilerException{
		List<Object> h = handlers.find(t, n.getGrammarClass(), n.getGrammarRule(), n.getToken());
		
		if(h.size() == 0){
			
			ASTNode parent = n.getParent();
			//System.out.println("father: (" + parent.toString() + ")");
			
			//System.out.println(parent.getAbstractChildNodes().get(0).getAbstractChildNodes().get(0));
			
			
			for(int i = 0; i < parent.getAbstractChildNodes().size(); i++){
				if(parent.getAbstractChildNodes().get(i).equals(n))
					System.out.println("child " + i + ": (" + parent.getAbstractChildNodes().get(i) + ") [ERR]");
				else
					System.out.println("child " + i + ": (" + parent.getAbstractChildNodes().get(i) + ")");
			}
			
			
			throw new CompilerException("no handler registered for (" + this.getClass().getName() + ", " + t + ", " + n.getGrammarClass() + ", " + n.getGrammarRule() + ", " + n.getToken() + ")");
		}
		else if(h.size() > 1){
			throw new CompilerException("two handlers registered for (" + this.getClass().getName() + ", " + t + ", " + n.getGrammarClass() + ", " + n.getGrammarRule() + ", " + n.getToken() + ")");			
		}
		
		CompilerCodeHandler current = (CompilerCodeHandler) h.get(0);
		CodeFragment result = new CodeFragment();
		
		current.compile(result, n, engine);
		
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
