package org.coreasm.eclipse.debug.ui;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import org.coreasm.eclipse.debug.core.model.ASMLineBreakpoint;
import org.coreasm.eclipse.debug.core.model.ASMMethodBreakpoint;
import org.coreasm.eclipse.debug.core.model.ASMWatchpoint;
import org.coreasm.eclipse.editors.ASMDocument;
import org.coreasm.eclipse.editors.ASMEditor;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.kernel.Kernel;
import org.coreasm.engine.plugins.signature.FunctionNode;
import org.coreasm.engine.plugins.signature.UniverseNode;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.ILineBreakpoint;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTarget;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.texteditor.IDocumentProvider;

/**
 * Adapter to support creation/deletion of breakpoints in ASM files
 * @author Michael Stegmaier
 *
 */
public class ASMBreakpointAdapter implements IToggleBreakpointsTarget {

	@Override
	public void toggleLineBreakpoints(IWorkbenchPart part, ISelection selection) throws CoreException {
		ASMEditor editor = getEditor(part);
		
		if (editor != null) {
			IResource resource = (IResource)editor.getEditorInput().getAdapter(IResource.class);
			int lineNumber = ((ITextSelection)selection).getStartLine();
			IBreakpoint[] breakpoints = DebugPlugin.getDefault().getBreakpointManager().getBreakpoints("org.coreasm.eclipse.debug");
			
			for (int i = 0; i < breakpoints.length; i++) {
				if (breakpoints[i] instanceof ILineBreakpoint && resource.equals(breakpoints[i].getMarker().getResource())) {
					if (((ILineBreakpoint)breakpoints[i]).getLineNumber() == (lineNumber + 1)) {
						breakpoints[i].delete();
						return;
					}
				}
			}
			
			for (ASTNode node : getASTNodesFromSelection(part, selection)) {
				if (ASTNode.RULE_CLASS.equals(node.getGrammarClass())) {
					DebugPlugin.getDefault().getBreakpointManager().addBreakpoint(new ASMLineBreakpoint(resource, lineNumber + 1));
					break;
				}
			}
		}
	}

	@Override
	public boolean canToggleLineBreakpoints(IWorkbenchPart part, ISelection selection) {
		return getEditor(part) != null && !canToggleWatchpoints(part, selection) && !canToggleMethodBreakpoints(part, selection);
	}

	@Override
	public void toggleMethodBreakpoints(IWorkbenchPart part, ISelection selection) throws CoreException {
		ASMEditor editor = getEditor(part);
		
		if (editor != null) {
			IResource resource = (IResource)editor.getEditorInput().getAdapter(IResource.class);
			String ruleName = getRuleName(part, selection);
			int lineNumber = ((ITextSelection)selection).getStartLine();
			IBreakpoint[] breakpoints = DebugPlugin.getDefault().getBreakpointManager().getBreakpoints("org.coreasm.eclipse.debug");
			
			for (int i = 0; i < breakpoints.length; i++) {
				if (breakpoints[i] instanceof ASMMethodBreakpoint && resource.equals(breakpoints[i].getMarker().getResource())) {
					if (((ASMMethodBreakpoint)breakpoints[i]).getRuleName().equals(ruleName)) {
						breakpoints[i].delete();
						return;
					}
				}
			}
			DebugPlugin.getDefault().getBreakpointManager().addBreakpoint(new ASMMethodBreakpoint(resource, lineNumber + 1, ruleName));
		}
	}

	@Override
	public boolean canToggleMethodBreakpoints(IWorkbenchPart part, ISelection selection) {
		return getRuleName(part, selection) != null;
	}

	@Override
	public void toggleWatchpoints(IWorkbenchPart part, ISelection selection) throws CoreException {
		ASMEditor editor = getEditor(part);
		
		if (editor != null) {
			IResource resource = (IResource)editor.getEditorInput().getAdapter(IResource.class);
			String functionName = getFunctionName(part, selection);
			int lineNumber = ((ITextSelection)selection).getStartLine();
			IBreakpoint[] breakpoints = DebugPlugin.getDefault().getBreakpointManager().getBreakpoints("org.coreasm.eclipse.debug");
			
			for (int i = 0; i < breakpoints.length; i++) {
				if (breakpoints[i] instanceof ASMWatchpoint && resource.equals(breakpoints[i].getMarker().getResource())) {
					if (((ASMWatchpoint)breakpoints[i]).getFuctionName().equals(functionName)) {
						breakpoints[i].delete();
						return;
					}
				}
			}
			DebugPlugin.getDefault().getBreakpointManager().addBreakpoint(new ASMWatchpoint(resource, lineNumber + 1, functionName, true, true));
		}
	}

	@Override
	public boolean canToggleWatchpoints(IWorkbenchPart part, ISelection selection) {
		return getFunctionName(part, selection) != null;
	}
	
	private String getFunctionName(IWorkbenchPart part, ISelection selection) {
		List<ASTNode> nodes = getASTNodesFromSelection(part, selection);
		
		if (!nodes.isEmpty()) {
			ASTNode node = nodes.get(0);
			
			if (node != null) {
				node = node.getFirst();
				if (node instanceof FunctionNode || node instanceof UniverseNode) {
					for (ASTNode child : node.getAbstractChildNodes()) {
						if (Kernel.GR_ID.equals(child.getGrammarRule()))
							return child.getToken();
					}
				}
			}
		}
		return null;
	}
	
	private String getRuleName(IWorkbenchPart part, ISelection selection) {
		List<ASTNode> nodes = getASTNodesFromSelection(part, selection);
		
		if (!nodes.isEmpty()) {
			ASTNode node = nodes.get(0);
			
			if (node != null) {
				if (Kernel.GR_RULEDECLARATION.equals(node.getGrammarRule()))
					return node.getFirst().getFirst().getToken();
				else if (Kernel.GR_INITIALIZATION.equals(node.getGrammarRule()))
					return node.getFirst().getToken();
			}
		}
		return null;
	}
	
	private List<ASTNode> getASTNodesFromSelection(IWorkbenchPart part, ISelection selection) {
		ASMEditor editor = getEditor(part);
		
		if (editor != null && selection instanceof ITextSelection) {
			ITextSelection textSelection = (ITextSelection) selection;
			IDocumentProvider documentProvider = editor.getDocumentProvider();
			
			try {
				documentProvider.connect(this);
				return getASTNodesOnLine(documentProvider.getDocument(editor.getEditorInput()), textSelection.getStartLine());
			} catch (CoreException e) {
			} catch (BadLocationException e) {
			} finally {
				documentProvider.disconnect(this);
			}
		}
		return Collections.emptyList();
	}
	
	private List<ASTNode> getASTNodesOnLine(IDocument doc, int line) throws BadLocationException {
		ASMDocument asmDoc = (ASMDocument)doc;
		Stack<ASTNode> queue = new Stack<ASTNode>();
		List<ASTNode> nodes = new LinkedList<ASTNode>();
		ASTNode rootNode = (ASTNode)asmDoc.getRootnode();
		
		if (rootNode != null)
			queue.add(rootNode);
		while (!queue.isEmpty()) {
			ASTNode node = queue.pop();
			int offset = node.getScannerInfo().charPosition;
			
			if (doc.getLineOfOffset(offset) == line)
				nodes.add(node);
			for (ASTNode child : node.getAbstractChildNodes())
				queue.add(queue.size(), child);
		}
		return nodes;
	}
	
	/**
	 * Returns the editor being used to edit a CoreASM file, associated with the given part
	 *  
	 * @param part workbench part
	 * @return the editor being used to edit a CoreASM file, associated with the given part
	 */
	private ASMEditor getEditor(IWorkbenchPart part) {
		if (part instanceof ASMEditor) {
			ASMEditor editorPart = (ASMEditor) part;
			IResource resource = (IResource) editorPart.getEditorInput().getAdapter(IResource.class);
			
			if (resource != null && ("coreasm".equalsIgnoreCase(resource.getFileExtension()) || "casm".equalsIgnoreCase(resource.getFileExtension())))
				return editorPart;
		}
		return null;		
	}
}
