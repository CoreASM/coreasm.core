package org.coreasm.eclipse.editors.warnings;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.coreasm.eclipse.editors.ASMDocument;
import org.coreasm.eclipse.editors.SlimEngine;
import org.coreasm.engine.Specification.FunctionInfo;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.FunctionRuleTermNode;
import org.coreasm.engine.kernel.Kernel;
import org.coreasm.engine.plugins.chooserule.ChooseRuleNode;
import org.coreasm.engine.plugins.extendrule.ExtendRuleNode;
import org.coreasm.engine.plugins.forallrule.ForallRuleNode;
import org.coreasm.engine.plugins.letrule.LetRuleNode;
import org.coreasm.engine.plugins.signature.DerivedFunctionNode;
import org.coreasm.engine.plugins.signature.EnumerationElement;
import org.coreasm.engine.plugins.signature.EnumerationNode;
import org.coreasm.engine.plugins.signature.FunctionNode;
import org.coreasm.engine.plugins.signature.UniverseNode;
import org.coreasm.engine.plugins.turboasm.LocalRuleNode;
import org.coreasm.engine.plugins.turboasm.ReturnRuleNode;

public class UndefinedIdentifierWarningRecognizer implements IWarningRecognizer {
	private Set<String> pluginFunctionNames = null;

	@Override
	public List<AbstractWarning> checkForWarnings(ASMDocument document) {
		List<AbstractWarning> warnings = new LinkedList<AbstractWarning>();
		Set<String> functionNames = getFunctionNames(document);
		Stack<ASTNode> fringe = new Stack<ASTNode>();
		
		for (ASTNode declarationNode = ((ASTNode)document.getRootnode()).getFirst(); declarationNode != null; declarationNode = declarationNode.getNext()) {
			if (ASTNode.DECLARATION_CLASS.equals(declarationNode.getGrammarClass())) {
				if (Kernel.GR_RULEDECLARATION.equals(declarationNode.getGrammarRule())) {
					fringe.add(declarationNode);
					while (!fringe.isEmpty()) {
						ASTNode node = fringe.pop();
						if (ASTNode.FUNCTION_RULE_CLASS.equals(node.getGrammarClass()) && node instanceof FunctionRuleTermNode) {
							FunctionRuleTermNode frNode = (FunctionRuleTermNode)node;
							if (frNode.hasName()) {
								if (!frNode.hasArguments()) {
									if (!isEnvironmentFunction(frNode) && !isFunctionName(frNode, functionNames))
										warnings.add(new UndefinedIdentifierWarning(frNode.getName(), frNode.getScannerInfo().charPosition));
								}
								else if (!isFunctionName(frNode, functionNames))
									warnings.add(new UndefinedIdentifierWarning(frNode.getName(), frNode.getScannerInfo().charPosition));
							}
						}
						fringe.addAll(node.getAbstractChildNodes());
					}
				}
			}
		}
		
		return warnings;
	}
	
	private boolean isEnvironmentFunction(FunctionRuleTermNode frNode) {
		if (isParam(frNode))
			return true;
		if (isInLetVariableMap(frNode))
			return true;
		if (isLocalFunction(frNode))
			return true;
		if (isForallVariable(frNode))
			return true;
		if (isChooseVariable(frNode))
			return true;
		if (isExtendRuleVariable(frNode))
			return true;
		if (isReturnRuleExpression(frNode))
			return true;
		return false;
	}
	
	private boolean isParam(FunctionRuleTermNode frNode) {
		final ASTNode ruleNode = getParentRuleNode(frNode);
		if (ruleNode != null) {
			final ASTNode idNode = ruleNode.getFirst().getFirst();
			for (ASTNode paramNode = idNode.getNext(); paramNode != null; paramNode = paramNode.getNext()) {
				if (paramNode.getToken().equals(frNode.getName()))
					return true;
			}
		}
		return false;
	}
	
	private ASTNode getParentRuleNode(ASTNode node) {
		ASTNode parentRuleNode = node;
		while (parentRuleNode != null && !Kernel.GR_RULEDECLARATION.equals(parentRuleNode.getGrammarRule()) && !"DerivedFunctionDeclaration".equals(parentRuleNode.getGrammarRule()))
			parentRuleNode = parentRuleNode.getParent();
		return parentRuleNode;
	}
	
	private boolean isInLetVariableMap(FunctionRuleTermNode frNode) {
		final LetRuleNode letRuleNode = getParentLetRuleNode(frNode);
		if (letRuleNode != null) {
			try {
				return letRuleNode.getVariableMap().containsKey(frNode.getName());
			} catch (Exception e) {
			}
		}
		return false;
	}

	private LetRuleNode getParentLetRuleNode(ASTNode node) {
		ASTNode letRuleNode = node;
		while (letRuleNode != null && !(letRuleNode instanceof LetRuleNode) && !Kernel.GR_RULEDECLARATION.equals(letRuleNode.getGrammarRule()))
			letRuleNode = letRuleNode.getParent();
		if (letRuleNode instanceof LetRuleNode)
			return (LetRuleNode)letRuleNode;
		return null;
	}
	
	private boolean isLocalFunction(FunctionRuleTermNode frNode) {
		final LocalRuleNode localRuleNode = getParentLocalRuleNode(frNode);
		if (localRuleNode != null)
			return localRuleNode.getFunctionNames().contains(frNode.getName());
		return false;
	}
	
	private LocalRuleNode getParentLocalRuleNode(ASTNode node) {
		ASTNode localRuleNode = node;
		while (localRuleNode != null && !(localRuleNode instanceof LocalRuleNode) && !Kernel.GR_RULEDECLARATION.equals(localRuleNode.getGrammarRule()))
			localRuleNode = localRuleNode.getParent();
		if (localRuleNode instanceof LocalRuleNode)
			return (LocalRuleNode)localRuleNode;
		return null;
	}
	
	private boolean isForallVariable(FunctionRuleTermNode frNode) {
		final ForallRuleNode forallRuleNode = getParentForallRuleNode(frNode);
		if (forallRuleNode != null)
			return forallRuleNode.getVariable().getToken().equals(frNode.getName());
		return false;
	}
	
	private ForallRuleNode getParentForallRuleNode(ASTNode node) {
		ASTNode forallRuleNode = node;
		while (forallRuleNode != null && !(forallRuleNode instanceof ForallRuleNode) && !Kernel.GR_RULEDECLARATION.equals(forallRuleNode.getGrammarRule()))
			forallRuleNode = forallRuleNode.getParent();
		if (forallRuleNode instanceof ForallRuleNode)
			return (ForallRuleNode)forallRuleNode;
		return null;
	}
	
	private boolean isChooseVariable(FunctionRuleTermNode frNode) {
		final ChooseRuleNode chooseRuleNode = getParentChooseRuleNode(frNode);
		if (chooseRuleNode != null)
			return chooseRuleNode.getVariable().getToken().equals(frNode.getName());
		return false;
	}
	
	private ChooseRuleNode getParentChooseRuleNode(ASTNode node) {
		ASTNode chooseRuleNode = node;
		while (chooseRuleNode != null && !(chooseRuleNode instanceof ChooseRuleNode) && !Kernel.GR_RULEDECLARATION.equals(chooseRuleNode.getGrammarRule()))
			chooseRuleNode = chooseRuleNode.getParent();
		if (chooseRuleNode instanceof ChooseRuleNode)
			return (ChooseRuleNode)chooseRuleNode;
		return null;
	}
	
	private boolean isExtendRuleVariable(FunctionRuleTermNode frNode) {
		final ExtendRuleNode extendRuleNode = getParentExtendRuleNode(frNode);
		if (extendRuleNode != null)
			return extendRuleNode.getIdNode().getToken().equals(frNode.getName());
		return false;
	}
	
	private ExtendRuleNode getParentExtendRuleNode(ASTNode node) {
		ASTNode extendRuleNode = node;
		while (extendRuleNode != null && !(extendRuleNode instanceof ExtendRuleNode) && !Kernel.GR_RULEDECLARATION.equals(extendRuleNode.getGrammarRule()))
			extendRuleNode = extendRuleNode.getParent();
		if (extendRuleNode instanceof ExtendRuleNode)
			return (ExtendRuleNode)extendRuleNode;
		return null;
	}
	
	private boolean isReturnRuleExpression(FunctionRuleTermNode frNode) {
		final ReturnRuleNode returnRuleNode = getParentReturnRuleNode(frNode);
		if (returnRuleNode != null)
			return returnRuleNode.getExpressionNode().getFirst().getToken().equals(frNode.getName());
		return false;
	}
	
	private ReturnRuleNode getParentReturnRuleNode(ASTNode node) {
		ASTNode returnRuleNode = node;
		while (returnRuleNode != null && !(returnRuleNode instanceof ReturnRuleNode) && !Kernel.GR_RULEDECLARATION.equals(returnRuleNode.getGrammarRule()))
			returnRuleNode = returnRuleNode.getParent();
		if (returnRuleNode instanceof ReturnRuleNode)
			return (ReturnRuleNode)returnRuleNode;
		return null;
	}
	
	private boolean isFunctionName(FunctionRuleTermNode frNode, Set<String> functionNames) {
		if (getPluginFunctionNames().contains(frNode.getName()))
			return true;
		return functionNames.contains(frNode.getName());
	}
	
	private Set<String> getPluginFunctionNames() {
		if (pluginFunctionNames == null) {
			pluginFunctionNames = new HashSet<String>();
			for (FunctionInfo functionInfo : SlimEngine.getFullEngine().getSpec().getDefinedFunctions())
				pluginFunctionNames.add(functionInfo.name);
			for (FunctionInfo functionInfo : SlimEngine.getFullEngine().getSpec().getDefinedUniverses())
				pluginFunctionNames.add(functionInfo.name);
			for (FunctionInfo functionInfo : SlimEngine.getFullEngine().getSpec().getDefinedBackgrounds())
				pluginFunctionNames.add(functionInfo.name);
		}
		return pluginFunctionNames;
	}
	
	private Set<String> getFunctionNames(ASMDocument document) {
		Set<String> functionNames = new HashSet<String>();
		for (ASTNode node = ((ASTNode)document.getRootnode()).getFirst(); node != null; node = node.getNext()) {
			if (ASTNode.DECLARATION_CLASS.equals(node.getGrammarClass())) {
				if ("Signature".equals(node.getGrammarRule())) {
					for (ASTNode signature = node.getFirst(); signature != null; signature = signature.getNext()) {
						if (signature instanceof EnumerationNode) {
							EnumerationNode enumerationNode = (EnumerationNode)signature;
							functionNames.add(enumerationNode.getName());
							for (EnumerationElement member : enumerationNode.getMembers())
								functionNames.add(member.getName());
						}
						else if (signature instanceof FunctionNode)
							functionNames.add(((FunctionNode)signature).getName());
						else if (signature instanceof UniverseNode)
							functionNames.add(((UniverseNode)signature).getName());
						else if (signature instanceof DerivedFunctionNode)
							functionNames.add(((DerivedFunctionNode)signature).getNameSignatureNode().getFirst().getToken());
					}
				}
				else if (Kernel.GR_RULEDECLARATION.equals(node.getGrammarRule()))
					functionNames.add(node.getFirst().getFirst().getToken());
			}
		}
		return functionNames;
	}
}
