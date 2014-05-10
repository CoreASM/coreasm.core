package org.coreasm.eclipse.editors.warnings;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import org.coreasm.eclipse.editors.ASMDeclarationWatcher;
import org.coreasm.eclipse.editors.ASMDeclarationWatcher.Declaration;
import org.coreasm.eclipse.editors.ASMDeclarationWatcher.RuleDeclaration;
import org.coreasm.eclipse.editors.ASMDocument;
import org.coreasm.eclipse.editors.ASMEditor;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.FunctionRuleTermNode;
import org.coreasm.engine.interpreter.Node;
import org.coreasm.engine.kernel.Kernel;
import org.coreasm.engine.kernel.MacroCallRuleNode;
import org.coreasm.engine.plugins.modularity.IncludeNode;
import org.coreasm.engine.plugins.signature.DerivedFunctionNode;
import org.coreasm.engine.plugins.turboasm.ReturnResultNode;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;

public class ReturnResultWarningRecognizer implements IWarningRecognizer {
	private final ASMEditor parentEditor;
	
	public ReturnResultWarningRecognizer(ASMEditor parentEditor) {
		this.parentEditor = parentEditor;
	}

	@Override
	public List<AbstractWarning> checkForWarnings(ASMDocument document) {
		List<AbstractWarning> warnings = new LinkedList<AbstractWarning>();
		Stack<ASTNode> fringe = new Stack<ASTNode>();
		
		for (ASTNode declarationNode = ((ASTNode)document.getRootnode()).getFirst(); declarationNode != null; declarationNode = declarationNode.getNext()) {
			if (ASTNode.DECLARATION_CLASS.equals(declarationNode.getGrammarClass())) {
				if (Kernel.GR_RULEDECLARATION.equals(declarationNode.getGrammarRule())
				|| "Signature".equals(declarationNode.getGrammarRule()) && declarationNode.getFirst() instanceof DerivedFunctionNode) {
					fringe.add(declarationNode);
					while (!fringe.isEmpty()) {
						ASTNode node = fringe.pop();
						if (node instanceof ReturnResultNode) {
							ASTNode ruleNode = ((ReturnResultNode)node).getRuleNode();
							if (ruleNode instanceof FunctionRuleTermNode) {
								FunctionRuleTermNode frNode = (FunctionRuleTermNode)ruleNode;
								if (frNode.hasName()) {
									RuleDeclaration rule = getRuleDeclaration(document, frNode.getName());
									if (rule == null || rule.hasReturn())
										warnings.add(new ReturnResultWarning(frNode.getName(), node, document));
								}
							}
						}
						else if (node instanceof FunctionRuleTermNode && !(node.getParent() instanceof MacroCallRuleNode) && !(node.getParent() instanceof ReturnResultNode)) {
							FunctionRuleTermNode frNode = (FunctionRuleTermNode)node;
							if (frNode.hasName()) {
								RuleDeclaration rule = getRuleDeclaration(document, frNode.getName());
								if (rule != null && !rule.hasReturn())
									warnings.add(new NoReturnWarning(frNode.getName(), node, document));
							}
						}
						fringe.addAll(node.getAbstractChildNodes());
					}
				}
			}
		}
		
		return warnings;
	}

	private RuleDeclaration getRuleDeclaration(ASMDocument document, String ruleName) {
		for (Declaration declaration : ASMDeclarationWatcher.getDeclarations(parentEditor.getInputFile(), false)) {
			if (declaration instanceof RuleDeclaration && declaration.getName().equals(ruleName))
				return (RuleDeclaration)declaration;
		}
		IPath relativePath = parentEditor.getInputFile().getProjectRelativePath().removeLastSegments(1);
		IProject project = parentEditor.getInputFile().getProject();
		for (Node node = document.getRootnode().getFirstCSTNode(); node != null; node = node.getNextCSTNode()) {
			if (node instanceof IncludeNode) {
				for (Declaration declaration : ASMDeclarationWatcher.getDeclarations(project.getFile(relativePath.append(((IncludeNode)node).getFilename())), true)) {
					if (declaration instanceof RuleDeclaration && declaration.getName().equals(ruleName))
						return (RuleDeclaration)declaration;
				}
			}
		}
		return null;
	}
}
