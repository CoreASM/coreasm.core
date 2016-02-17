package org.coreasm.eclipse.editors.warnings;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import org.coreasm.eclipse.editors.ASMDeclarationWatcher;
import org.coreasm.eclipse.editors.ASMDeclarationWatcher.Declaration;
import org.coreasm.eclipse.editors.ASMDeclarationWatcher.DerivedFunctionDeclaration;
import org.coreasm.eclipse.editors.ASMDeclarationWatcher.EnumerationDeclaration;
import org.coreasm.eclipse.editors.ASMDeclarationWatcher.FunctionDeclaration;
import org.coreasm.eclipse.editors.ASMDeclarationWatcher.RuleDeclaration;
import org.coreasm.eclipse.editors.ASMDeclarationWatcher.UniverseDeclaration;
import org.coreasm.eclipse.editors.ASMDocument;
import org.coreasm.eclipse.editors.ASMEditor;
import org.coreasm.engine.EngineException;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.FunctionRuleTermNode;
import org.coreasm.engine.kernel.Kernel;
import org.coreasm.engine.kernel.MacroCallRuleNode;
import org.coreasm.engine.plugins.chooserule.ChooseRuleNode;
import org.coreasm.engine.plugins.chooserule.PickExpNode;
import org.coreasm.engine.plugins.extendrule.ExtendRuleNode;
import org.coreasm.engine.plugins.forallrule.ForallRuleNode;
import org.coreasm.engine.plugins.letrule.LetRuleNode;
import org.coreasm.engine.plugins.list.ListCompNode;
import org.coreasm.engine.plugins.predicatelogic.ExistsExpNode;
import org.coreasm.engine.plugins.predicatelogic.ForallExpNode;
import org.coreasm.engine.plugins.set.SetCompNode;
import org.coreasm.engine.plugins.signature.DerivedFunctionNode;
import org.coreasm.engine.plugins.turboasm.LocalRuleNode;
import org.coreasm.engine.plugins.turboasm.ReturnTermNode;

public class NumberOfArgumentsWarningRecognizer implements IWarningRecognizer {
	private final ASMEditor parentEditor;
	
	public NumberOfArgumentsWarningRecognizer(ASMEditor parentEditor) {
		this.parentEditor = parentEditor;
	}

	@Override
	public List<AbstractWarning> checkForWarnings(ASMDocument document) {
		List<AbstractWarning> warnings = new LinkedList<AbstractWarning>();
		HashMap<String, Declaration> declarations = new HashMap<String, Declaration>();
		Stack<ASTNode> fringe = new Stack<ASTNode>();
		
		for (Declaration declaration : ASMDeclarationWatcher.getDeclarations(parentEditor.getInputFile(), true)) {
			if (!(declaration instanceof RuleDeclaration))
				declarations.put(declaration.getName(), declaration);
		}
		
		for (ASTNode declarationNode = ((ASTNode)document.getRootnode()).getFirst(); declarationNode != null; declarationNode = declarationNode.getNext()) {
			if (ASTNode.DECLARATION_CLASS.equals(declarationNode.getGrammarClass())) {
				if (Kernel.GR_RULEDECLARATION.equals(declarationNode.getGrammarRule())
				|| "Signature".equals(declarationNode.getGrammarRule()) && declarationNode.getFirst() instanceof DerivedFunctionNode) {
					fringe.add(declarationNode);
					while (!fringe.isEmpty()) {
						ASTNode node = fringe.pop();
						if (ASTNode.FUNCTION_RULE_CLASS.equals(node.getGrammarClass()) && node instanceof FunctionRuleTermNode) {
							if (node.getParent() instanceof MacroCallRuleNode)	// Wrong number of arguments for rules cause an error
								break;
							FunctionRuleTermNode frNode = (FunctionRuleTermNode)node;
							if (frNode.hasName()) {
								Declaration declaration = declarations.get(frNode.getName());
								if (declaration instanceof FunctionDeclaration) {
									FunctionDeclaration functionDeclaration = (FunctionDeclaration)declaration;
									if (!frNode.hasArguments()) {
										if (functionDeclaration.getDomain().size() > 0 && !isEnvironmentVariable(frNode) && !isLocalFunction(frNode))
											warnings.add(new NumberOfArgumentsWarning(frNode.getName(), 0, functionDeclaration.getDomain(), frNode, document));
									}
									else if (functionDeclaration.getDomain().size() != frNode.getArguments().size())
										warnings.add(new NumberOfArgumentsWarning(frNode.getName(), frNode.getArguments().size(), functionDeclaration.getDomain(), frNode, document));
								}
								else if (declaration instanceof UniverseDeclaration || declaration instanceof EnumerationDeclaration) {
									if (frNode.hasArguments()) {
										if (frNode.getArguments().size() > 1)
											warnings.add(new NumberOfArgumentsWarning(frNode.getName(), frNode.getArguments().size(), Arrays.asList(new String[] { "ELEMENT" }), frNode, document));
									}
								}
								else if (declaration instanceof DerivedFunctionDeclaration) {
									DerivedFunctionDeclaration functionDeclaration = (DerivedFunctionDeclaration)declaration;
									if (!frNode.hasArguments()) {
										if (functionDeclaration.getParams().size() > 0 && !isEnvironmentVariable(frNode) && !isLocalFunction(frNode))
											warnings.add(new NumberOfArgumentsWarning(frNode.getName(), 0, functionDeclaration.getParams(), frNode, document));
									}
									else if (functionDeclaration.getParams().size() != frNode.getArguments().size())
										warnings.add(new NumberOfArgumentsWarning(frNode.getName(), frNode.getArguments().size(), functionDeclaration.getParams(), frNode, document));
								}
							}
						}
						fringe.addAll(node.getAbstractChildNodes());
					}
				}
			}
		}
		
		return warnings;
	}

	private boolean isEnvironmentVariable(FunctionRuleTermNode frNode) {
		if (isParam(frNode))
			return true;
		if (isInLetVariableMap(frNode))
			return true;
		if (isForallRuleVariable(frNode))
			return true;
		if (isForallExpVariable(frNode))
			return true;
		if (isExistsExpVariable(frNode))
			return true;
		if (isChooseVariable(frNode))
			return true;
		if (isPickExpVariable(frNode))
			return true;
		if (isExtendRuleVariable(frNode))
			return true;
		if (isSetComprehensionConstrainerVariable(frNode))
			return true;
		if (isListComprehensionVariable(frNode))
			return true;
		if (isImportRuleVariable(frNode))
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
		ASTNode parentRuleNode = node.getParent();
		while (parentRuleNode != null && !Kernel.GR_RULEDECLARATION.equals(parentRuleNode.getGrammarRule()) && !"DerivedFunctionDeclaration".equals(parentRuleNode.getGrammarRule()))
			parentRuleNode = parentRuleNode.getParent();
		return parentRuleNode;
	}
	
	private boolean isInLetVariableMap(FunctionRuleTermNode frNode) {
		for (LetRuleNode letRuleNode = getParentLetRuleNode(frNode); letRuleNode != null; letRuleNode = getParentLetRuleNode(letRuleNode)) {
			try {
				if (letRuleNode.getVariableMap().containsKey(frNode.getName()))
					return true;
			} catch (Exception e) {
			}
		}
		return false;
	}

	private LetRuleNode getParentLetRuleNode(ASTNode node) {
		ASTNode letRuleNode = node.getParent();
		while (letRuleNode != null && !(letRuleNode instanceof LetRuleNode))
			letRuleNode = letRuleNode.getParent();
		if (letRuleNode instanceof LetRuleNode)
			return (LetRuleNode)letRuleNode;
		return null;
	}
	
	private boolean isLocalFunction(FunctionRuleTermNode frNode) {
		for (LocalRuleNode localRuleNode = getParentLocalRuleNode(frNode); localRuleNode != null; localRuleNode = getParentLocalRuleNode(localRuleNode)) {
			if (localRuleNode.getFunctionNames().contains(frNode.getName()))
				return true;
		}
		if (isReturnTermExpression(frNode))
			return true;
		return false;
	}
	
	private LocalRuleNode getParentLocalRuleNode(ASTNode node) {
		ASTNode localRuleNode = node.getParent();
		while (localRuleNode != null && !(localRuleNode instanceof LocalRuleNode))
			localRuleNode = localRuleNode.getParent();
		if (localRuleNode instanceof LocalRuleNode)
			return (LocalRuleNode)localRuleNode;
		return null;
	}
	
	private boolean isForallRuleVariable(FunctionRuleTermNode frNode) {
		for (ForallRuleNode forallRuleNode = getParentForallRuleNode(frNode); forallRuleNode != null; forallRuleNode = getParentForallRuleNode(forallRuleNode)) {
			if (forallRuleNode.getVariableMap().containsKey(frNode.getName()))
				return true;
		}
		return false;
	}
	
	private ForallRuleNode getParentForallRuleNode(ASTNode node) {
		ASTNode forallRuleNode = node.getParent();
		while (forallRuleNode != null && !(forallRuleNode instanceof ForallRuleNode))
			forallRuleNode = forallRuleNode.getParent();
		if (forallRuleNode instanceof ForallRuleNode)
			return (ForallRuleNode)forallRuleNode;
		return null;
	}
	
	private boolean isForallExpVariable(FunctionRuleTermNode frNode) {
		for (ForallExpNode forallExpNode = getParentForallExpNode(frNode); forallExpNode != null; forallExpNode = getParentForallExpNode(forallExpNode)) {
			if (forallExpNode.getVariableMap().containsKey(frNode.getName()))
				return true;
		}
		return false;
	}
	
	private ForallExpNode getParentForallExpNode(ASTNode node) {
		ASTNode forallExpNode = node.getParent();
		while (forallExpNode != null && !(forallExpNode instanceof ForallExpNode))
			forallExpNode = forallExpNode.getParent();
		if (forallExpNode instanceof ForallExpNode)
			return (ForallExpNode)forallExpNode;
		return null;
	}
	
	private boolean isExistsExpVariable(FunctionRuleTermNode frNode) {
		for (ExistsExpNode existsExpNode = getParentExistsExpNode(frNode); existsExpNode != null; existsExpNode = getParentExistsExpNode(existsExpNode)) {
			if (existsExpNode.getVariableMap().containsKey(frNode.getName()))
				return true;
		}
		return false;
	}
	
	private ExistsExpNode getParentExistsExpNode(ASTNode node) {
		ASTNode existsExpNode = node.getParent();
		while (existsExpNode != null && !(existsExpNode instanceof ExistsExpNode))
			existsExpNode = existsExpNode.getParent();
		if (existsExpNode instanceof ExistsExpNode)
			return (ExistsExpNode)existsExpNode;
		return null;
	}
	
	private boolean isChooseVariable(FunctionRuleTermNode frNode) {
		for (ChooseRuleNode chooseRuleNode = getParentChooseRuleNode(frNode); chooseRuleNode != null; chooseRuleNode = getParentChooseRuleNode(chooseRuleNode)) {
			if (chooseRuleNode.getVariableMap().containsKey(frNode.getName()))
				return true;
		}
		return false;
	}
	
	private ChooseRuleNode getParentChooseRuleNode(ASTNode node) {
		ASTNode chooseRuleNode = node.getParent();
		while (chooseRuleNode != null && !(chooseRuleNode instanceof ChooseRuleNode))
			chooseRuleNode = chooseRuleNode.getParent();
		if (chooseRuleNode instanceof ChooseRuleNode)
			return (ChooseRuleNode)chooseRuleNode;
		return null;
	}
	
	private boolean isPickExpVariable(FunctionRuleTermNode frNode) {
		for (PickExpNode pickExpNode = getParentPickExpNode(frNode); pickExpNode != null; pickExpNode = getParentPickExpNode(pickExpNode)) {
			if (pickExpNode.getVariable().getToken().equals(frNode.getName()))
				return true;
		}
		return false;
	}
	
	private PickExpNode getParentPickExpNode(ASTNode node) {
		ASTNode pickExpNode = node.getParent();
		while (pickExpNode != null && !(pickExpNode instanceof PickExpNode))
			pickExpNode = pickExpNode.getParent();
		if (pickExpNode instanceof PickExpNode)
			return (PickExpNode)pickExpNode;
		return null;
	}
	
	private boolean isExtendRuleVariable(FunctionRuleTermNode frNode) {
		for (ExtendRuleNode extendRuleNode = getParentExtendRuleNode(frNode); extendRuleNode != null; extendRuleNode = getParentExtendRuleNode(extendRuleNode)) {
			if (extendRuleNode.getIdNode().getToken().equals(frNode.getName()))
				return true;
		}
		return false;
	}
	
	private ExtendRuleNode getParentExtendRuleNode(ASTNode node) {
		ASTNode extendRuleNode = node.getParent();
		while (extendRuleNode != null && !(extendRuleNode instanceof ExtendRuleNode))
			extendRuleNode = extendRuleNode.getParent();
		if (extendRuleNode instanceof ExtendRuleNode)
			return (ExtendRuleNode)extendRuleNode;
		return null;
	}
	
	private boolean isSetComprehensionConstrainerVariable(FunctionRuleTermNode frNode) {
		for (SetCompNode setCompNode = getParentSetCompNode(frNode); setCompNode != null; setCompNode = getParentSetCompNode(setCompNode)) {
			try {
				if (setCompNode.getVarBindings().containsKey(frNode.getName()))
					return true;
			} catch (EngineException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return false;
	}
	
	private SetCompNode getParentSetCompNode(ASTNode node) {
		ASTNode setCompNode = node.getParent();
		while (setCompNode != null && !(setCompNode instanceof SetCompNode))
			setCompNode = setCompNode.getParent();
		if (setCompNode instanceof SetCompNode)
			return (SetCompNode)setCompNode;
		return null;
	}
	
	private boolean isListComprehensionVariable(FunctionRuleTermNode frNode) {
		for (ListCompNode listCompNode = getParentListCompNode(frNode); listCompNode != null; listCompNode = getParentListCompNode(listCompNode)) {
			try {
				if (listCompNode.getVarBindings().containsKey(frNode.getName()))
					return true;
			} catch (EngineException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return false;
	}
	
	private ListCompNode getParentListCompNode(ASTNode node) {
		ASTNode listCompNode = node.getParent();
		while (listCompNode != null && !(listCompNode instanceof ListCompNode))
			listCompNode = listCompNode.getParent();
		if (listCompNode instanceof ListCompNode)
			return (ListCompNode)listCompNode;
		return null;
	}
	
	private boolean isReturnTermExpression(FunctionRuleTermNode frNode) {
		for (ReturnTermNode returnTermNode = getParentReturnTermNode(frNode); returnTermNode != null; returnTermNode = getParentReturnTermNode(returnTermNode)) {
			ASTNode expression = returnTermNode.getExpressionNode();
			if (expression instanceof FunctionRuleTermNode && ((FunctionRuleTermNode)expression).getName().equals(frNode.getName()))
				return true;
		}
		return false;
	}
	
	private ReturnTermNode getParentReturnTermNode(ASTNode node) {
		ASTNode returnTermNode = node.getParent();
		while (returnTermNode != null && !(returnTermNode instanceof ReturnTermNode))
			returnTermNode = returnTermNode.getParent();
		if (returnTermNode instanceof ReturnTermNode)
			return (ReturnTermNode)returnTermNode;
		return null;
	}
	
	private boolean isImportRuleVariable(FunctionRuleTermNode frNode) {
		for (ASTNode importRuleNode = getParentImportRuleNode(frNode); importRuleNode != null; importRuleNode = getParentImportRuleNode(importRuleNode)) {
			if (importRuleNode.getFirst().getToken().equals(frNode.getName()))
				return true;
		}
		return false;
	}
	
	private ASTNode getParentImportRuleNode(ASTNode node) {
		ASTNode importRuleNode = node.getParent();
		while (importRuleNode != null && !"ImportRule".equals(importRuleNode.getGrammarRule()))
			importRuleNode = importRuleNode.getParent();
		if (importRuleNode != null && "ImportRule".equals(importRuleNode.getGrammarRule()))
			return importRuleNode;
		return null;
	}
}
