package org.coreasm.compiler.plugins.kernel.code.bcode;

import java.util.ArrayList;
import java.util.List;

import org.coreasm.compiler.CodeType;
import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.CoreASMCompiler;
import org.coreasm.compiler.classlibrary.RuleClassFile;
import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.compiler.interfaces.CompilerCodeHandler;
import org.coreasm.compiler.plugins.kernel.CompilerKernelPlugin;
import org.coreasm.engine.interpreter.ASTNode;

public class KernelRuleDeclarationHandler implements CompilerCodeHandler {
	boolean tmp = false;
	
	
	@Override
	public void compile(CodeFragment result, ASTNode node, CompilerEngine engine)
			throws CompilerException {
		try{
			engine.getLogger().debug(CompilerKernelPlugin.class, "creating a rule for node");
	
			// first, find the signature
			ASTNode signature = node.getAbstractChildNodes().get(0);
			ASTNode body = node.getAbstractChildNodes().get(1);
	
			String ruleName = signature.getFirst().getToken();
			
			System.out.println("rule name is " + ruleName);
			
			List<String> ruleParameters = new ArrayList<String>();
			for (int i = 1; i < signature.getAbstractChildNodes().size(); i++) {
				ruleParameters.add(signature.getAbstractChildNodes().get(i)
						.getToken());
			}
	
			// compile the body
			CodeFragment cbody = CoreASMCompiler.getEngine().compile(body, CodeType.U);
			RuleClassFile r = new RuleClassFile(ruleName, ruleParameters,
					cbody);
			engine.getClassLibrary().addEntry(r);
	
			engine.getLogger().debug(CompilerKernelPlugin.class, "end rule creation");
			
			if(ruleName.equals("CloneIteratively") && tmp) throw new Exception();
			else if(ruleName.equals("CloneIteratively")) tmp = true;
		}
		catch(Exception e){
			e.printStackTrace();
			throw new CompilerException("error creating rule for node");
		}
	}

}
