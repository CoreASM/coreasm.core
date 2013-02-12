package org.coreasm.eclipse.editors;

import java.util.HashSet;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import org.coreasm.eclipse.editors.ASMParser.ParsingResult;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.Node;
import org.coreasm.engine.plugins.modularity.ModularityPlugin.IncludeNode;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * The <code>ChildDocumentWatcher</code> watches included specifications (child documents).
 * Whenever an included specification changes, the parent specification gets reparsed.
 * @author Michael Stegmaier
 *
 */
public class ChildDocumentWatcher
implements Observer, IResourceChangeListener, IResourceDeltaVisitor
{
	private ASMEditor editor;
	private ASMParser parser;
	private Set<IPath> childdocs;
	private boolean parsingNeeded = false;

	public ChildDocumentWatcher(ASMEditor editor)
	{
		this.editor = editor;
		this.parser = editor.getParser();
		childdocs = new HashSet<IPath>();
	}

	@Override
	public void update(Observable o, Object arg) {
		if (o != parser || !(arg instanceof ParsingResult))
			return;
		ParsingResult result = (ParsingResult) arg;

		Node rootnode = result.document.getRootnode();
		if (rootnode != null) {
			childdocs.clear();
			for (Node node: AstTools.findChildrenWithToken((ASTNode)rootnode, "include")) {
				if (node instanceof IncludeNode)
					childdocs.add(new Path(((IncludeNode)node).getFilename()));
			}
		}

		editor.createIncludeMark(childdocs);
	}

	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		parsingNeeded = false;
		try {
			event.getDelta().accept(this);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		if (parsingNeeded)
			parser.getJob().schedule(ASMEditor.REPARSE_DELAY);
	}

	@Override
	public boolean visit(IResourceDelta delta) throws CoreException {
		if (delta.getResource() instanceof IFile) {
			IPath path = delta.getProjectRelativePath();
			if (childdocs.contains(path))
				parsingNeeded = true;
		}
		return true;
	}
}
