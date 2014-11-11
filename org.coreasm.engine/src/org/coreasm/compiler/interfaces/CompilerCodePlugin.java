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
	private Map<CodeType, Map<String, Map<String, Map<String, CompilerCodeHandler>>>> handlers;

	protected void register(CompilerCodeHandler handler) throws CompilerException{
		if(handlers == null){
			handlers = new HashMap<CodeType, Map<String,Map<String,Map<String,CompilerCodeHandler>>>>();
		}
		
		CodeType type = handler.getType();
		Map<String, Map<String, Map<String, CompilerCodeHandler>>> typeMap = handlers.get(type);
		if(typeMap == null){
			typeMap = new HashMap<String, Map<String,Map<String,CompilerCodeHandler>>>();
			handlers.put(type, typeMap);
		}
	
		String gclass = handler.getGrammarClass();
		gclass = (gclass == null) ? "" : gclass;
		Map<String, Map<String, CompilerCodeHandler>> gclassMap = typeMap.get(gclass);
		if(gclassMap == null){
			gclassMap = new HashMap<String, Map<String,CompilerCodeHandler>>();
			typeMap.put(gclass, gclassMap);
		}
		
		String grule = handler.getGrammarRule();
		grule = (grule == null) ? "" : grule;
		Map<String, CompilerCodeHandler> gruleMap = gclassMap.get(grule);
		if(gruleMap == null){
			gruleMap = new HashMap<String, CompilerCodeHandler>();
			gclassMap.put(grule, gruleMap);
		}
		
		String token = handler.getToken();
		token = (token == null) ? "" : token;
		
		if(gruleMap.get(token) != null) throw new CompilerException("Handler already registered for (" + type + ", " + gclass + ", " + grule + ", " + token + ")");
		gruleMap.put(token, handler);
	}
	
	public CodeFragment compile(CodeType t, ASTNode n) throws CompilerException{
		List<CompilerCodeHandler> h = layer2(handlers.get(t), n);
		if(h.size() == 0){
			throw new CompilerException("no handler registered for (" + t + ", " + n.getGrammarClass() + ", " + n.getGrammarRule() + ", " + n.getToken() + ")");
		}
		else if(h.size() > 1){
			throw new CompilerException("two handlers registered for (" + t + ", " + n.getGrammarClass() + ", " + n.getGrammarRule() + ", " + n.getToken() + ")");			
		}
		
		CompilerCodeHandler current = h.get(0);
		CodeFragment result = new CodeFragment();
		
		current.compile(result, n, CoreASMCompiler.getEngine());
		
		return result;
	}
	
	private List<CompilerCodeHandler> layer2(Map<String, Map<String, Map<String, CompilerCodeHandler>>> map, ASTNode n){
		List<CompilerCodeHandler> result = new ArrayList<CompilerCodeHandler>();
		if(map == null) return result;
		
		result.addAll(layer3(map.get(n.getGrammarClass()), n));
		result.addAll(layer3(map.get(""), n));
		
		return result;
	}
	
	private List<CompilerCodeHandler> layer3(Map<String, Map<String, CompilerCodeHandler>> map, ASTNode n){
		List<CompilerCodeHandler> result = new ArrayList<CompilerCodeHandler>();
		if(map == null) return result;
		
		result.addAll(layer4(map.get(n.getGrammarRule()), n));
		result.addAll(layer4(map.get(""), n));
		
		return result;
	}

	private List<CompilerCodeHandler> layer4(Map<String, CompilerCodeHandler> map, ASTNode n){
		List<CompilerCodeHandler> result = new ArrayList<CompilerCodeHandler>();
		if(map == null) return result;
		
		CompilerCodeHandler r = map.get(n.getToken());
		if(r != null) result.add(r);
		
		r = map.get("");
		if(r != null) result.add(r);
		
		return result;
	}
}
