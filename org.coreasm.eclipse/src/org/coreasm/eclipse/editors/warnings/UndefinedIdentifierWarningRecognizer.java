package org.coreasm.eclipse.editors.warnings;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.coreasm.eclipse.editors.ASMDocument;
import org.coreasm.eclipse.editors.ASMEditor;
import org.coreasm.eclipse.editors.SlimEngine;
import org.coreasm.eclipse.editors.errors.AbstractError;
import org.coreasm.engine.Specification.FunctionInfo;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.FunctionRuleTermNode;
import org.coreasm.engine.interpreter.Node;
import org.coreasm.engine.kernel.Kernel;
import org.coreasm.engine.kernel.RuleOrFuncElementNode;
import org.coreasm.engine.plugins.chooserule.ChooseRuleNode;
import org.coreasm.engine.plugins.extendrule.ExtendRuleNode;
import org.coreasm.engine.plugins.forallrule.ForallRuleNode;
import org.coreasm.engine.plugins.letrule.LetRuleNode;
import org.coreasm.engine.plugins.modularity.ModularityPlugin.IncludeNode;
import org.coreasm.engine.plugins.predicatelogic.ForallExpNode;
import org.coreasm.engine.plugins.set.SetCompNode;
import org.coreasm.engine.plugins.signature.DerivedFunctionNode;
import org.coreasm.engine.plugins.signature.EnumerationElement;
import org.coreasm.engine.plugins.signature.EnumerationNode;
import org.coreasm.engine.plugins.signature.FunctionNode;
import org.coreasm.engine.plugins.signature.UniverseNode;
import org.coreasm.engine.plugins.turboasm.LocalRuleNode;
import org.coreasm.engine.plugins.turboasm.ReturnRuleNode;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

public class UndefinedIdentifierWarningRecognizer implements IWarningRecognizer {
	private final ASMEditor parentEditor;
	private Set<String> pluginFunctionNames = null;

	public UndefinedIdentifierWarningRecognizer(ASMEditor parentEditor) {
		this.parentEditor = parentEditor;
	}
	
	@Override
	public List<AbstractWarning> checkForWarnings(ASMDocument document) {
		List<AbstractWarning> warnings = new LinkedList<AbstractWarning>();
		Set<String> functionNames = getFunctionNames(document);
		Stack<ASTNode> fringe = new Stack<ASTNode>();
		
		for (ASTNode declarationNode = ((ASTNode)document.getRootnode()).getFirst(); declarationNode != null; declarationNode = declarationNode.getNext()) {
			if (ASTNode.DECLARATION_CLASS.equals(declarationNode.getGrammarClass())) {
				if (Kernel.GR_RULEDECLARATION.equals(declarationNode.getGrammarRule())
				|| "Signature".equals(declarationNode.getGrammarRule()) && declarationNode.getFirst() instanceof DerivedFunctionNode) {
					fringe.add(declarationNode);
					while (!fringe.isEmpty()) {
						ASTNode node = fringe.pop();
						if (ASTNode.FUNCTION_RULE_CLASS.equals(node.getGrammarClass()) && node instanceof FunctionRuleTermNode) {
							FunctionRuleTermNode frNode = (FunctionRuleTermNode)node;
							if (frNode.hasName()) {
								if (!frNode.hasArguments()) {
									if (!isEnvironmentVariable(frNode) && !isFunctionName(frNode.getName(), functionNames))
										warnings.add(new UndefinedIdentifierWarning(frNode.getName(), frNode.getScannerInfo().charPosition));
								}
								else if (!isFunctionName(frNode.getName(), functionNames))
									warnings.add(new UndefinedIdentifierWarning(frNode.getName(), frNode.getScannerInfo().charPosition));
							}
						}
						else if (ASTNode.EXPRESSION_CLASS.equals(node.getGrammarClass())) {
							if (node instanceof RuleOrFuncElementNode) {
								RuleOrFuncElementNode ruleOrFuncElementNode = (RuleOrFuncElementNode)node;
								if (!isFunctionName(ruleOrFuncElementNode.getElementName(), functionNames))
									warnings.add(new UndefinedIdentifierWarning(ruleOrFuncElementNode.getElementName(), node.getScannerInfo().charPosition));
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
		if (isLocalFunction(frNode))
			return true;
		if (isForallRuleVariable(frNode))
			return true;
		if (isForallExpVariable(frNode))
			return true;
		if (isChooseVariable(frNode))
			return true;
		if (isExtendRuleVariable(frNode))
			return true;
		if (isSetComprehensionConstrainerVariable(frNode))
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
		for (ForallExpNode ForallExpNode = getParentForallExpNode(frNode); ForallExpNode != null; ForallExpNode = getParentForallExpNode(ForallExpNode)) {
			if (ForallExpNode.getVariable().getToken().equals(frNode.getName()))
				return true;
		}
		return false;
	}
	
	private ForallExpNode getParentForallExpNode(ASTNode node) {
		ASTNode ForallExpNode = node.getParent();
		while (ForallExpNode != null && !(ForallExpNode instanceof ForallExpNode))
			ForallExpNode = ForallExpNode.getParent();
		if (ForallExpNode instanceof ForallExpNode)
			return (ForallExpNode)ForallExpNode;
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
		String path = parentEditor.getInputFile().getProjectRelativePath().makeAbsolute().toString();
		path = path.substring(0, path.lastIndexOf(IPath.SEPARATOR) + 1);
		IProject project = parentEditor.getInputFile().getProject();
		for (Node node = document.getRootnode().getFirstCSTNode(); node != null; node = node.getNextCSTNode()) {
			if (node instanceof IncludeNode)
				functionNames.addAll(getIncludedDeclarations(project, project.getFile(path + ((IncludeNode)node).getFilename()), new HashSet<IFile>()));
		}
		return functionNames;
	}
	
	private Set<String> getIncludedDeclarations(IProject project, IFile file, Set<IFile> includedFiles) {
		Set<String> declarations = new HashSet<String>();
		if (includedFiles.contains(file))
			return declarations;
		includedFiles.add(file);
		if (file != null) {
			try {
				IMarker[] declarationMarker = file.findMarkers(ASMEditor.MARKER_TYPE_DECLARATIONS, false, IResource.DEPTH_ZERO);
				if (declarationMarker.length > 0) {
					String declarationsFromMarker = declarationMarker[0].getAttribute("declarations", "");
					if (!declarationsFromMarker.isEmpty()) {
						for (String declaration : declarationsFromMarker.split("\u25c9")) {
							String functionName = null;
							String type = declaration.trim().substring(0, declaration.indexOf(':'));
							functionName = declaration.substring(type.length() + 2);
							if ("Universe".equals(type) || "Enumeration".equals(type))
								functionName = functionName.substring(0, functionName.indexOf('=')).trim();
							else if ("Derived Function".equals(type) || "Rule".equals(type)) {
								int indexOfNewline = functionName.indexOf('\n');
								if (indexOfNewline >= 0)
									functionName = functionName.substring(0, indexOfNewline);
								int indexOfBracket = functionName.indexOf('(');
								if (indexOfBracket >= 0)
									functionName = functionName.substring(0, indexOfBracket);
							}
							else if ("Enumeration member".equals(type))
								functionName = functionName.substring(functionName.indexOf('(') + 1, functionName.indexOf(')'));
							else if ("Function".equals(type))
								functionName = functionName.substring(0, functionName.indexOf(':'));
							if (functionName != null)
								declarations.add(functionName);
						}
					}
				}
				IMarker[] includeMarker = file.findMarkers(ASMEditor.MARKER_TYPE_INCLUDE, false, IResource.DEPTH_ZERO);
				if (includeMarker.length > 0) {
					for (String include : includeMarker[0].getAttribute("includes", "").split(AbstractError.SEPERATOR_VAL))
						declarations.addAll(getIncludedDeclarations(project, project.getFile(include), includedFiles));
				}
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		return declarations;
	}
}
