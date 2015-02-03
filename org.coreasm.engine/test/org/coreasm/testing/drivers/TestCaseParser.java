package org.coreasm.testing.drivers;

import java.io.StringReader;

import org.coreasm.engine.CoreASMEngine;
import org.coreasm.engine.CoreASMEngineFactory;
import org.coreasm.engine.Engine;
import org.coreasm.engine.Specification;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.testing.TestCase;
import org.coreasm.testing.TestingNode;
import org.coreasm.util.CoreASMGlobal;

/**
 * Part of the testing framework, used to parse test specification.
 * A test specification is a minimal CoreASM specification using the TestingPlugin
 * and will contain only a minimal CoreASM construct.
 * @author Spellmaker
 *
 */
public class TestCaseParser {
	
	/**
	 * Initializes the class for further use
	 * Needs to be called before any operation
	 */
	public void init(){
	}
	
	/**
	 * Parses a TestCase specification file
	 * @param specFile A CoreASM specification with the test case
	 * @return The root node of the test construct
	 */
	public ASTNode parseSpec(TestCase test) {
		CoreASMEngine engine = (Engine) CoreASMEngineFactory.createEngine();
		engine.initialize();
		//wait for the initialization to complete
		while(engine.isBusy());
		
		if(test.specFile != null){
			engine.loadSpecification(test.specFile.getAbsolutePath());
		}
		else{
			engine.loadSpecification(new StringReader(test.spec));
		}	
		engine.waitWhileBusy();
		Specification spec = engine.getSpec();
		engine.terminate();
		engine.waitWhileBusy();
		
		if(spec == null) return null;
		else{
			ASTNode root = (ASTNode) spec.getRootNode();
			
			for(int i = 0; i < root.getAbstractChildNodes().size(); i++){
				if(root.getAbstractChildNodes().get(i).getGrammarRule().equals("TestRule")){
					ASTNode testRoot = root.getAbstractChildNodes().get(i).getAbstractChildNodes().get(0);
					finalizeTestTree(testRoot);
					return testRoot;
				}
			}
		}
		return null; //should never happen in a well formed test spec
	}
	
	/**
	 * Dispose the driver
	 * Frees all resources claimed by the parser driver.
	 * In particular, this will stop the engine thread
	 */
	public void dispose(){
	}
	
	private ASTNode finalizeTestTree(ASTNode astNode) {
		if(astNode.getGrammarRule().equals("PARAM")){
			return new TestingNode(astNode.getFirst().getToken());
		}
		else{
			for(int i = 0; i < astNode.getAbstractChildNodes().size(); i++){
				ASTNode node = astNode.getAbstractChildNodes().get(i);
				ASTNode repl = finalizeTestTree(node);
				if(repl != null){
					node.replaceWith(repl);
				}
			}
			return null;
		}
	}
}
