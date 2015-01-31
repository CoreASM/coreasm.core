package org.coreasm.testing;

import java.io.File;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import org.coreasm.engine.CoreASMEngine;
import org.coreasm.engine.CoreASMEngineFactory;
import org.coreasm.engine.Engine;
import org.coreasm.engine.interpreter.ASTNode;

public class TestDriver {
	private CoreASMEngine casm;
	private static Map<String, ASTNode> astnodes = new HashMap<String, ASTNode>();
	
	public TestDriver(){
		init();
	}
	
	private void init() {
		casm = (Engine)CoreASMEngineFactory.createEngine();
		casm.initialize();
		
	}
	
	private String makeSpec(String s){
		String result = "CoreASM testing\n\nuse Standard\nuse Testing\ninit A\n\ntest = " +s;
		System.out.println(result);
		return result;
	}
	
	public ASTNode parseSpec(String testString){
		String specstring = makeSpec(testString);
		
		StringReader spec = new StringReader(specstring);
		casm.loadSpecification(spec);
		while(casm.isBusy());
		
		spec.close();
		
		if(casm.getSpec() == null) return null;
		else{
			//now find the root of the test
			ASTNode root = (ASTNode) casm.getSpec().getRootNode();
			
			for(int i = 0; i < root.getAbstractChildNodes().size(); i++){
				if(root.getAbstractChildNodes().get(i).getGrammarRule().equals("TestRule")){
					ASTNode testRoot = root.getAbstractChildNodes().get(i).getAbstractChildNodes().get(0);
					finalize(testRoot);
					return testRoot;
				}
			}
		}
		//should never occur; the loop should always find the constructed test node
		return null;
	}

	private ASTNode finalize(ASTNode astNode) {
		if(astNode.getGrammarRule().equals("PARAM")){
			return makeASTNode(astNode.getFirst().getToken());
		}
		else{
			for(int i = 0; i < astNode.getAbstractChildNodes().size(); i++){
				ASTNode node = astNode.getAbstractChildNodes().get(i);
				ASTNode repl = finalize(node);
				if(repl != null){
					node.replaceWith(repl);
				}
			}
			return null;
		}
	}

	public void dispose() {
		if(casm != null){
			casm.terminate();
		}
	}
	
	public static ASTNode makeASTNode(String tok){
		ASTNode r = astnodes.get(tok);
		if(r != null) return r;
		
		r = new ASTNode("JUnit", "JUnit", "JUnit", tok, null);
		astnodes.put(tok, r);
		
		return r;
	}
}
