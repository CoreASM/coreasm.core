package org.coreasm.eclipse.editors.hovering;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.coreasm.eclipse.debug.core.model.ASMStackFrame;
import org.coreasm.eclipse.debug.core.model.ASMStorage;
import org.coreasm.eclipse.editors.ASMDocument;
import org.coreasm.eclipse.editors.ASMEditor;
import org.coreasm.eclipse.editors.FileManager;
import org.coreasm.eclipse.editors.SlimEngine;
import org.coreasm.eclipse.editors.errors.AbstractError;
import org.coreasm.eclipse.engine.debugger.EngineDebugger;
import org.coreasm.engine.Specification.FunctionInfo;
import org.coreasm.engine.absstorage.Element;
import org.coreasm.engine.absstorage.Enumerable;
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
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.debug.ui.contexts.IDebugContextListener;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextHoverExtension;
import org.eclipse.jface.text.ITextHoverExtension2;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension2;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.MarkerAnnotation;

/**
 * This class manages the retrieval of information from a marker which has
 * been hovered by the mouse cursor.
 * @author Markus Müller, Michael Stegmaier
 */
public class ASMTextHover
implements ITextHover, ITextHoverExtension, ITextHoverExtension2, IDebugContextListener
{
	private ASMStorage selectedState;
	private Map<String, FunctionInfo> pluginFunctions = null;

	IInformationControlCreator icc = null;
	
	public ASMTextHover() {
		DebugUITools.getDebugContextManager().getContextService(PlatformUI.getWorkbench().getActiveWorkbenchWindow()).addDebugContextListener(this);
	}
	
	@Override
	public IRegion getHoverRegion(ITextViewer textViewer, int offset) {
		Point selection = textViewer.getSelectedRange();
		if (selection.x <= offset && offset < selection.x + selection.y)
			return new Region(selection.x, selection.y);
		return new Region(offset, 0);
	}

	/**
	 * This method retrieves the marker which has been hovered and reads the
	 * error object from it. The object which is returned by this method will be
	 * delivered to the ASMInformationControl. The ASMInformationControl expects
	 * this object to be a Map<String,Object> with two entries:
	 * <ul>
	 * <li>"document": a reference to the document the hover belongs to</li>
	 * <li>"marker": a reference to the marker which has been hovered</li>
	 * </ul>
	 */
	@Override
	public Object getHoverInfo2(ITextViewer textViewer, IRegion hoverRegion)
	{
		Map<String, Object> hoverInfoMap = new HashMap<String,Object>();
		hoverInfoMap.put("document", textViewer.getDocument());
		
		IAnnotationModel model = null;
		if (textViewer instanceof ISourceViewer)
			model= ((ISourceViewer)textViewer).getAnnotationModel();
		if (model != null) {
			Iterator it;
			if (model instanceof IAnnotationModelExtension2)
				it = ((IAnnotationModelExtension2)model).getAnnotationIterator(hoverRegion.getOffset(), hoverRegion.getLength(), true, true);
			else
				it = model.getAnnotationIterator();
	
			while (it.hasNext()) {
				Annotation a = (Annotation) it.next();
				if (a instanceof MarkerAnnotation) {
					MarkerAnnotation ma = (MarkerAnnotation) a;
					IMarker m = ma.getMarker();
					hoverInfoMap.put("marker", m);
					return hoverInfoMap;
				}
			}
		}
		try {
			if (hoverRegion.getLength() > 0) {
				String value = getExpressionValue(textViewer.getDocument(), hoverRegion.getOffset(), hoverRegion.getLength());
				if (value != null)
					return value;
			}
			ASTNode hoverNode = null;
			List<ASTNode> nodes = getASTNodesOnLineOfOffset(textViewer.getDocument(), hoverRegion.getOffset());
			for (ASTNode node : nodes) {
				int offset = node.getScannerInfo().charPosition;
				if (hoverRegion.getOffset() >= offset) {
					if (hoverNode == null || offset >= hoverNode.getScannerInfo().charPosition)
						hoverNode = node;
					if (node instanceof FunctionRuleTermNode) {
						FunctionRuleTermNode frNode = (FunctionRuleTermNode)node;
						if (frNode.hasName() && hoverRegion.getOffset() + hoverRegion.getLength() < offset + frNode.getName().length()) {
							String value = getExpressionValue(textViewer.getDocument(), frNode.getName());
							if (isEnvironmentVariable(frNode))
								return "Environment Variable: " + frNode.getName() + (value != null ? " = " + value : "");
							FunctionInfo pluginFunction = getPluginFunction(frNode.getName());
							if (pluginFunction != null)
								return "Plugin Function: " + pluginFunction.plugin + "." + pluginFunction.name + (value != null ? " = " + value : "");
							String declaration = getFunctionDeclaration((ASMDocument)textViewer.getDocument(), frNode.getName());
							if (declaration == null)
								return frNode.toString();
							return declaration + (value != null ? " = " + value : "");
						}
					}
					else if (node instanceof RuleOrFuncElementNode) {
						RuleOrFuncElementNode ruleOrFuncElementNode = (RuleOrFuncElementNode)node;
						FunctionInfo pluginFunction = getPluginFunction(ruleOrFuncElementNode.getElementName());
						if (pluginFunction != null)
							return "Plugin Function: " + pluginFunction.plugin + "." + pluginFunction.name;
						String declaration = getFunctionDeclaration((ASMDocument)textViewer.getDocument(), ruleOrFuncElementNode.getElementName());
						if (declaration == null)
							return ruleOrFuncElementNode.toString();
						return declaration;
					}
				}
			}
			if (hoverNode == null || hoverNode instanceof FunctionRuleTermNode)
				return null;
			return hoverNode.toString();
		} catch (BadLocationException e) {
		}
		return null;
	}
	
	private List<ASTNode> getASTNodesOnLineOfOffset(IDocument doc, int offset) throws BadLocationException {
		return getASTNodesOnLine(doc, doc.getLineOfOffset(offset));
	}
	
	private List<ASTNode> getASTNodesOnLine(IDocument doc, int line) throws BadLocationException {
		ASMDocument asmDoc = (ASMDocument)doc;
		Stack<ASTNode> fringe = new Stack<ASTNode>();
		List<ASTNode> nodes = new LinkedList<ASTNode>();
		ASTNode rootNode = (ASTNode)asmDoc.getRootnode();
		
		if (rootNode != null)
			fringe.add(rootNode);
		while (!fringe.isEmpty()) {
			ASTNode node = fringe.pop();
			int offset = node.getScannerInfo().charPosition;
			
			if (doc.getLineOfOffset(offset) == line)
				nodes.add(node);
			for (ASTNode child : node.getAbstractChildNodes())
				fringe.add(fringe.size(), child);
		}
		return nodes;
	}
	
	private String getExpressionValue(IDocument doc, int offset, int length) {
		try {
			return getExpressionValue(doc, doc.get(offset, length));
		} catch (BadLocationException e) {
		}
		return null;
	}
	
	private String getExpressionValue(IDocument doc, String expression) {
		if (EngineDebugger.getRunningInstance() != null) {
			try {
				Element value = EngineDebugger.getRunningInstance().evaluateExpression(expression, selectedState);
				if (value instanceof Enumerable && value.toString().isEmpty())
					return ((Enumerable)value).enumerate().toString();
				return value.toString();
			} catch (Exception e) {
			}
		}
		return null;
	}
	
	private String getFunctionDeclaration(ASMDocument document, String functionName) {
		for (ASTNode node = ((ASTNode)document.getRootnode()).getFirst(); node != null; node = node.getNext()) {
			if (ASTNode.DECLARATION_CLASS.equals(node.getGrammarClass())) {
				if ("Signature".equals(node.getGrammarRule())) {
					for (ASTNode signature = node.getFirst(); signature != null; signature = signature.getNext()) {
						if (signature instanceof EnumerationNode) {
							EnumerationNode enumerationNode = (EnumerationNode)signature;
							if (enumerationNode.getName().equals(functionName)) {
								String declaration = "Enumeration: " + enumerationNode.getName() + " = { ";
								for (EnumerationElement member : enumerationNode.getMembers()) {
									if (!declaration.endsWith("{ "))
										declaration += ", ";
									declaration += member;
								}
								declaration += " }";
								return declaration;
							}
							for (EnumerationElement member : enumerationNode.getMembers()) {
								if (member.getName().equals(functionName))
									return "Enumeration member: " + enumerationNode.getName() + "(" + member.getName() + ")";
							}
						}
						else if (signature instanceof FunctionNode) {
							FunctionNode fNode = (FunctionNode)signature;
							if (fNode.getName().equals(functionName))
								return "Function: " + fNode.getName() + ": " + fNode.getDomain() + " -> " + fNode.getRange();
						}
						else if (signature instanceof UniverseNode) {
							UniverseNode universeNode = (UniverseNode)signature;
							if (universeNode.getName().equals(functionName)) {
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
								return declaration;
							}
						}
						else if (signature instanceof DerivedFunctionNode) {
							if (((DerivedFunctionNode)signature).getNameSignatureNode().getFirst().getToken().equals(functionName)) {
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
								return declaration;
							}
						}
					}
				}
				else if (Kernel.GR_RULEDECLARATION.equals(node.getGrammarRule())) {
					if (node.getFirst().getFirst().getToken().equals(functionName)) {
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
						return declaration;
					}
				}
			}
		}
		IEditorPart part = FileManager.getActiveEditor();
		if (part instanceof ASMEditor) {
			String path = ((ASMEditor)part).getInputFile().getProjectRelativePath().makeAbsolute().toString();
			path = path.substring(0, path.lastIndexOf(IPath.SEPARATOR) + 1);
			IProject project = FileManager.getActiveProject();
			for (Node node = document.getRootnode().getFirstCSTNode(); node != null; node = node.getNextCSTNode()) {
				if (node instanceof IncludeNode) {
					String includedFunctionDeclaration = getIncludedFunctionDeclaration(project, project.getFile(path + ((IncludeNode)node).getFilename()), functionName, new HashSet<IFile>());
					if (includedFunctionDeclaration != null)
						return includedFunctionDeclaration;
				}
			}
		}
		return null;
	}
	
	private String getIncludedFunctionDeclaration(IProject project, IFile file, String functionName, Set<IFile> includedFiles) {
		if (includedFiles.contains(file))
			return null;
		includedFiles.add(file);
		if (file != null) {
			try {
				IMarker[] declarationMarker = file.findMarkers(ASMEditor.MARKER_TYPE_DECLARATIONS, false, IResource.DEPTH_ZERO);
				if (declarationMarker.length > 0) {
					String declarations = declarationMarker[0].getAttribute("declarations", "");
					if (!declarations.isEmpty()) {
						for (String declaration : declarations.split("\u25c9")) {
							String fName = null;
							String type = declaration.trim().substring(0, declaration.indexOf(':'));
							fName = declaration.substring(type.length() + 2);
							if ("Universe".equals(type) || "Enumeration".equals(type))
								fName = fName.substring(0, fName.indexOf('=')).trim();
							else if ("Derived Function".equals(type) || "Rule".equals(type)) {
								int indexOfNewline = fName.indexOf('\n');
								if (indexOfNewline >= 0)
									fName = fName.substring(0, indexOfNewline);
								int indexOfBracket = fName.indexOf('(');
								if (indexOfBracket >= 0)
									fName = fName.substring(0, indexOfBracket);
							}
							else if ("Enumeration member".equals(type))
								fName = fName.substring(fName.indexOf('(') + 1, fName.indexOf(')'));
							else if ("Function".equals(type))
								fName = fName.substring(0, fName.indexOf(':'));
							if (functionName.equals(fName))
								return file.getProjectRelativePath() + "\n\n" + declaration;
						}
					}
				}
				IMarker[] includeMarker = file.findMarkers(ASMEditor.MARKER_TYPE_INCLUDE, false, IResource.DEPTH_ZERO);
				if (includeMarker.length > 0) {
					for (String include : includeMarker[0].getAttribute("includes", "").split(AbstractError.SEPERATOR_VAL)) {
						String includedFunctionDeclaration = getIncludedFunctionDeclaration(project, project.getFile(include), functionName, includedFiles);
						if (includedFunctionDeclaration != null)
							return includedFunctionDeclaration;
					}
				}
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		return null;
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
					if (line.length() > 5)
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
	
	private FunctionInfo getPluginFunction(String functionName) {
		if (pluginFunctions == null) {
			pluginFunctions = new HashMap<String, FunctionInfo>();
			for (FunctionInfo functionInfo : SlimEngine.getFullEngine().getSpec().getDefinedFunctions())
				pluginFunctions.put(functionInfo.name, functionInfo);
			for (FunctionInfo functionInfo : SlimEngine.getFullEngine().getSpec().getDefinedUniverses())
				pluginFunctions.put(functionInfo.name, functionInfo);
			for (FunctionInfo functionInfo : SlimEngine.getFullEngine().getSpec().getDefinedBackgrounds())
				pluginFunctions.put(functionInfo.name, functionInfo);
		}
		return pluginFunctions.get(functionName);
	}
	
	@Override
	public IInformationControlCreator getHoverControlCreator() 
	{
		if (icc == null)
			icc = new IInformationControlCreator() {
				public IInformationControl createInformationControl(Shell parent) {
					return new ASMInformationControl(parent);
				}
			};

			return icc;
	}

	
	
	/**
	 * This is the old getHoverInfo method from ITextHover, which only supports
	 * returning Strings. Because we use getHoverInfo2 from ITextHoverExtension2
	 * this method just returns null. 
	 */
	@Override
	public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion)
	{
		return null;
	}

	@Override
	public void debugContextChanged(DebugContextEvent event) {
		selectedState = null;
		ISelection context = event.getContext();
		if (context instanceof IStructuredSelection) {
			Object element = ((IStructuredSelection)context).getFirstElement();
			if (element instanceof ASMStackFrame) {
				ASMStackFrame frame = (ASMStackFrame)element;
				selectedState = frame.getState();
			}
		}
	}
	
}