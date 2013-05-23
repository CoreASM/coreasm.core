package org.coreasm.eclipse.editors.warnings;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.coreasm.eclipse.editors.ASMDeclarationWatcher;
import org.coreasm.eclipse.editors.ASMDeclarationWatcher.Declaration;
import org.coreasm.eclipse.editors.ASMDocument;
import org.coreasm.eclipse.editors.ASMEditor;
import org.coreasm.eclipse.editors.SlimEngine;
import org.coreasm.engine.Specification.FunctionInfo;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.FunctionRuleTermNode;
import org.coreasm.engine.interpreter.Node;
import org.coreasm.engine.kernel.Kernel;
import org.coreasm.engine.kernel.MacroCallRuleNode;
import org.coreasm.engine.kernel.RuleOrFuncElementNode;
import org.coreasm.engine.plugins.chooserule.ChooseRuleNode;
import org.coreasm.engine.plugins.chooserule.PickExpNode;
import org.coreasm.engine.plugins.extendrule.ExtendRuleNode;
import org.coreasm.engine.plugins.forallrule.ForallRuleNode;
import org.coreasm.engine.plugins.letrule.LetRuleNode;
import org.coreasm.engine.plugins.modularity.IncludeNode;
import org.coreasm.engine.plugins.predicatelogic.ExistsExpNode;
import org.coreasm.engine.plugins.predicatelogic.ForallExpNode;
import org.coreasm.engine.plugins.set.SetCompNode;
import org.coreasm.engine.plugins.signature.DerivedFunctionNode;
import org.coreasm.engine.plugins.signature.EnumerationElement;
import org.coreasm.engine.plugins.signature.EnumerationNode;
import org.coreasm.engine.plugins.signature.FunctionNode;
import org.coreasm.engine.plugins.signature.UniverseNode;
import org.coreasm.engine.plugins.turboasm.LocalRuleNode;
import org.coreasm.engine.plugins.turboasm.ReturnRuleNode;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;

/**
 * This WarningRecognizer searches an ASM document for undefined identifiers.
 * @author Michael Stegmaier
 *
 */
public class UndefinedIdentifierWarningRecognizer implements IWarningRecognizer {
	private final ASMEditor parentEditor;
	private Set<String> pluginFunctionNames = null;

	public UndefinedIdentifierWarningRecognizer(ASMEditor parentEditor) {
		this.parentEditor = parentEditor;
	}
	
	@Override
	public List<AbstractWarning> checkForWarnings(ASMDocument document) {
		List<AbstractWarning> warnings = new LinkedList<AbstractWarning>();
		Set<String> functionNames = getDeclaredNames(document);
		Stack<ASTNode> fringe = new Stack<ASTNode>();
		
		for (ASTNode declarationNode = ((ASTNode)document.getRootnode()).getFirst(); declarationNode != null; declarationNode = declarationNode.getNext()) {
			if (ASTNode.DECLARATION_CLASS.equals(declarationNode.getGrammarClass())) {
				if (Kernel.GR_RULEDECLARATION.equals(declarationNode.getGrammarRule())
				|| "Signature".equals(declarationNode.getGrammarRule()) && declarationNode.getFirst() instanceof DerivedFunctionNode) {
					fringe.add(declarationNode);
					while (!fringe.isEmpty()) {
						ASTNode node = fringe.pop();
						if (ASTNode.FUNCTION_RULE_CLASS.equals(node.getGrammarClass()) && node instanceof FunctionRuleTermNode) {
							if (node.getParent() instanceof MacroCallRuleNode)	// Undefined rules cause an error
								break;
							FunctionRuleTermNode frNode = (FunctionRuleTermNode)node;
							if (frNode.hasName()) {
								if (!frNode.hasArguments()) {
									if (!isEnvironmentVariable(frNode) && !isFunctionName(frNode.getName(), functionNames) && !isLocalFunction(frNode))
										warnings.add(new UndefinedIdentifierWarning(frNode.getName(), Collections.<ASTNode>emptyList(), frNode.getScannerInfo().charPosition));
								}
								else if (!isFunctionName(frNode.getName(), functionNames) && !isLocalFunction(frNode))
									warnings.add(new UndefinedIdentifierWarning(frNode.getName(), frNode.getArguments(), frNode.getScannerInfo().charPosition));
							}
						}
						else if (ASTNode.EXPRESSION_CLASS.equals(node.getGrammarClass())) {
							if (node instanceof RuleOrFuncElementNode) {
								RuleOrFuncElementNode ruleOrFuncElementNode = (RuleOrFuncElementNode)node;
								if (!isFunctionName(ruleOrFuncElementNode.getElementName(), functionNames))
									warnings.add(new UndefinedIdentifierWarning(ruleOrFuncElementNode.getElementName(), Collections.<ASTNode>emptyList(), node.getScannerInfo().charPosition + 1));
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
		if (isReturnRuleExpression(frNode))
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
			if (forallRuleNode.getVariable().getToken().equals(frNode.getName()))
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
			if (forallExpNode.getVariable().getToken().equals(frNode.getName()))
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
			if (existsExpNode.getVariable().getToken().equals(frNode.getName()))
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
			if (chooseRuleNode.getVariable().getToken().equals(frNode.getName()))
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
			if (setCompNode.getConstrainerVar().equals(frNode.getName()))
				return true;
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
	
	private boolean isReturnRuleExpression(FunctionRuleTermNode frNode) {
		for (ReturnRuleNode returnRuleNode = getParentReturnRuleNode(frNode); returnRuleNode != null; returnRuleNode = getParentReturnRuleNode(returnRuleNode)) {
			if (returnRuleNode.getExpressionNode().getFirst().getToken().equals(frNode.getName()))
				return true;
		}
		return false;
	}
	
	private ReturnRuleNode getParentReturnRuleNode(ASTNode node) {
		ASTNode returnRuleNode = node.getParent();
		while (returnRuleNode != null && !(returnRuleNode instanceof ReturnRuleNode))
			returnRuleNode = returnRuleNode.getParent();
		if (returnRuleNode instanceof ReturnRuleNode)
			return (ReturnRuleNode)returnRuleNode;
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
	
	private boolean isFunctionName(String functionName, Set<String> functionNames) {
		if (getPluginFunctionNames().contains(functionName))
			return true;
		return functionNames.contains(functionName);
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
	
	private Set<String> getDeclaredNames(ASMDocument document) {
		Set<String> declaredNames = new HashSet<String>();
		for (ASTNode node = ((ASTNode)document.getRootnode()).getFirst(); node != null; node = node.getNext()) {
			if (ASTNode.DECLARATION_CLASS.equals(node.getGrammarClass())) {
				if ("Signature".equals(node.getGrammarRule())) {
					for (ASTNode signature = node.getFirst(); signature != null; signature = signature.getNext()) {
						if (signature instanceof EnumerationNode) {
							EnumerationNode enumerationNode = (EnumerationNode)signature;
							declaredNames.add(enumerationNode.getName());
							for (EnumerationElement member : enumerationNode.getMembers())
								declaredNames.add(member.getName());
						}
						else if (signature instanceof FunctionNode)
							declaredNames.add(((FunctionNode)signature).getName());
						else if (signature instanceof UniverseNode) {
							UniverseNode universeNode = (UniverseNode)signature;
							declaredNames.add(((UniverseNode)signature).getName());
							for (ASTNode member = universeNode.getFirst().getNext(); member != null; member = member.getNext())
								declaredNames.add(member.getToken());
						}
						else if (signature instanceof DerivedFunctionNode)
							declaredNames.add(((DerivedFunctionNode)signature).getNameSignatureNode().getFirst().getToken());
					}
				}
				else if (Kernel.GR_RULEDECLARATION.equals(node.getGrammarRule()))
					declaredNames.add(node.getFirst().getFirst().getToken());
			}

		}
		IPath relativePath = parentEditor.getInputFile().getProjectRelativePath().removeLastSegments(1);
		IProject project = parentEditor.getInputFile().getProject();
		for (Node node = document.getRootnode().getFirstCSTNode(); node != null; node = node.getNextCSTNode()) {
			if (node instanceof IncludeNode) {
				for (Declaration declaration : ASMDeclarationWatcher.getDeclarations(project.getFile(relativePath.append(((IncludeNode)node).getFilename())), true))
					declaredNames.add(declaration.getName());
			}
		}
		return declaredNames;
	}
}