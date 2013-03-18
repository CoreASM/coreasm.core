package org.coreasm.eclipse.editors;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import org.coreasm.eclipse.editors.ASMParser.ParsingResult;
import org.coreasm.eclipse.engine.debugger.EngineDebugger;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.kernel.Kernel;
import org.coreasm.engine.plugins.signature.DerivedFunctionNode;
import org.coreasm.engine.plugins.signature.EnumerationElement;
import org.coreasm.engine.plugins.signature.EnumerationNode;
import org.coreasm.engine.plugins.signature.FunctionNode;
import org.coreasm.engine.plugins.signature.UniverseNode;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

/**
 * The <code>DeclarationWatcher</code> manages the markers for declarations.
 * @author Michael Stegmaier
 *
 */
public class ASMDeclarationWatcher implements Observer {
	private static final String SEPERATOR = "\u25c9";
	
	private ASMEditor editor;
	private ASMParser parser;
	
	public ASMDeclarationWatcher(ASMEditor editor) {
		this.editor = editor;
		this.parser = editor.getParser();
	}
	
	@Override
	public void update(Observable o, Object arg) {
		if (o != parser || !(arg instanceof ParsingResult))
			return;
		ParsingResult result = (ParsingResult)arg;
		if (result.wasSuccessful) {
			String declarations = "";
			for (String declaration : getDeclarations(result.document)) {
				if (!declarations.isEmpty())
					declarations += SEPERATOR;
				declarations += declaration;
			}
			editor.createDeclarationsMark(declarations);
		}
	}
	
	public static Set<String> getDeclarations(IFile file, boolean includedDeclarations) {
		Set<String> declarations = new HashSet<String>();
		collectDeclarations(file, includedDeclarations, declarations);
		return declarations;
	}
	
	private static void collectDeclarations(IFile file, boolean includedDeclarations, Set<String> declarations) {
		try {
			IMarker[] declarationMarker = file.findMarkers(ASMEditor.MARKER_TYPE_DECLARATIONS, false, IResource.DEPTH_ZERO);
			if (declarationMarker.length > 0) {
				String markerDeclarations = declarationMarker[0].getAttribute("declarations", "");
				if (!markerDeclarations.isEmpty())
					declarations.addAll(Arrays.asList(markerDeclarations.split(SEPERATOR)));
			}
			if (includedDeclarations) {
				for (IFile includedFile : ASMIncludeWatcher.getIncludedFiles(file, true))
					declarations.addAll(getDeclarations(includedFile, includedDeclarations));
			}
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private Set<String> getDeclarations(ASMDocument document) {
		HashSet<String> declarations = new HashSet<String>();
		for (ASTNode node = ((ASTNode)document.getRootnode()).getFirst(); node != null; node = node.getNext()) {
			if (ASTNode.DECLARATION_CLASS.equals(node.getGrammarClass())) {
				if ("Signature".equals(node.getGrammarRule())) {
					for (ASTNode signature = node.getFirst(); signature != null; signature = signature.getNext()) {
						if (signature instanceof EnumerationNode) {
							EnumerationNode enumerationNode = (EnumerationNode)signature;
							String declaration = "Enumeration: " + enumerationNode.getName() + " = { ";
							for (EnumerationElement member : enumerationNode.getMembers()) {
								if (!declaration.endsWith("{ "))
									declaration += ", ";
								declaration += member;
							}
							declaration += " }";
							declarations.add(declaration);
							for (EnumerationElement member : enumerationNode.getMembers())
								declarations.add("Enumeration member: " + enumerationNode.getName() + "(" + member.getName() + ")");
						}
						else if (signature instanceof FunctionNode) {
							FunctionNode fNode = (FunctionNode)signature;
							declarations.add("Function: " + fNode.getName() + ": " + fNode.getDomain() + " -> " + fNode.getRange());
						}
						else if (signature instanceof UniverseNode) {
							UniverseNode universeNode = (UniverseNode)signature;
							String declaration = "Universe: " + universeNode.getName();
							if (EngineDebugger.getRunningInstance() == null) {
								declaration += " = { ";
								for (ASTNode member = universeNode.getFirst().getNext(); member != null; member = member.getNext()) {
									if (!declaration.endsWith("{ "))
										declaration += ", ";
									declaration += member.getToken();
								}
								declaration += " }";
							}
							declarations.add(declaration);
						}
						else if (signature instanceof DerivedFunctionNode) {
							ASTNode idNode = ((DerivedFunctionNode)signature).getNameSignatureNode().getFirst();
							String declaration = "Derived Function: " + idNode.getToken() + "(";
							for (ASTNode param = idNode.getNext(); param != null; param = param.getNext()) {
								if (!declaration.endsWith("("))
									declaration += ", ";
								declaration += param.getToken();
							}
							declaration = (declaration + ")").replace("()", "");
							String comment = getComment(document, node.getScannerInfo().charPosition);
							if (comment != null)
								declaration += "\n\n" + comment;
							declarations.add(declaration);
						}
					}
				}
				else if (Kernel.GR_RULEDECLARATION.equals(node.getGrammarRule())) {
					ASTNode idNode = node.getFirst().getFirst();
					String declaration = "Rule: " + idNode.getToken() + "(";
					for (ASTNode param = idNode.getNext(); param != null; param = param.getNext()) {
						if (!declaration.endsWith("("))
							declaration += ", ";
						declaration += param.getToken();
					}
					declaration = (declaration + ")").replace("()", "");
					String comment = getComment(document, node.getScannerInfo().charPosition);
					if (comment != null)
						declaration += "\n\n" + comment;
					declarations.add(declaration);
				}
			}
		}
		return declarations;
	}
	
	private String getComment(IDocument document, int offset) {
		try {
			String comment = "";
			String line;
			int lineNumber = document.getLineOfOffset(offset);
			boolean blockComment = false;
			do {
				lineNumber--;
				line = document.get(document.getLineOffset(lineNumber), document.getLineLength(lineNumber));
				if (line.startsWith("//"))
					comment = line.substring(2).trim() + "\n" + comment;
				else if (line.contains("*/")) {
					if (line.length() > 3)
						comment = line.replace("*/", "").trim() + "\n" + comment;
					blockComment = true;
				}
				else if (line.contains("/*")) {
					if (line.length() > 4)
						comment = line.replace("/*", "").trim() + "\n" + comment;
					blockComment = false;
				}
				else if (line.contains("/*") && line.contains("*/")) {
					if (line.length() > 4)
						comment = line.replace("/*", "").replace("*/", "").trim() + "\n" + comment;
					blockComment = false;
				}
				else if (blockComment)
					comment = line.replace("*", "").trim() + "\n" + comment;
			} while (line.startsWith("//") || blockComment);
			if (comment.isEmpty())
				return null;
			return comment;
		} catch (BadLocationException e) {
		}
		return null;
	}
}
