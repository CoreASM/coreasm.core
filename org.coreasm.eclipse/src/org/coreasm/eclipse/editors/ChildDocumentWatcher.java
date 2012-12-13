package org.coreasm.eclipse.editors;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import org.coreasm.eclipse.editors.ASMParser.ParsingResult;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.Node;
import org.coreasm.engine.plugins.modularity.ModularityPlugin.IncludeNode;
import org.coreasm.util.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;

/**
 * This class watches any child documents of a certain specification for changes.
 * Each instance of this class is bound to an instance of ASMParser through the
 * Observer interface. After each parsing the document reads the names of the
 * included files out of the syntax tree.
 * Additionally each instance of this class is bound to the Workspace through the
 * IResourceChangeListener, so it gets notified for each change to the Workspace.
 * If one of this changes is one of the child documents, a reparse is scheduled.
 * @author Markus Müller
 */
public class ChildDocumentWatcher
implements Observer, IResourceChangeListener, IResourceDeltaVisitor
{
	ASMEditor editor;
	ASMParser parser;
	List<String> childdocs;
	boolean parsingNeeded = false;

	//TODO: merge list and map
	Map<String,String> pathmap;

	public ChildDocumentWatcher(ASMEditor editor)
	{
		this.editor = editor;
		this.parser = editor.getParser();
		childdocs = new LinkedList<String>();
		pathmap = new HashMap<String, String>();
	}

	@Override
	public void update(Observable o, Object arg) {

		// check if correctly called by a parser
		if ( ! (o == parser) ||
				! (arg instanceof ParsingResult))
			return;
		ParsingResult result = (ParsingResult) arg;

		// Read all include nodes from the syntax tree and
		// store their filenames.
		Node rootnode = result.document.getRootnode();
		if (rootnode != null) {
			childdocs.clear();
			List<Node> l = AstTools.findChildrenWithToken((ASTNode) rootnode, "include");
			for (Node node: l) {
				if (node instanceof IncludeNode)
					childdocs.add(((IncludeNode) node).getFilename());
			}
		}

		// create (invisible) include marker
		editor.createIncludeMark(new HashSet<String>(childdocs));

		// Update pathmap
		pathmap.clear();
		IFile inputfile = editor.getInputFile();
		for (String filenameFromSpec: childdocs) {
			String filenameFromProject = FileManager.getFilenameRelativeToProject(filenameFromSpec, inputfile);
			if (filenameFromProject != null)
				pathmap.put(filenameFromSpec, filenameFromProject);
		}
	}

	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		parsingNeeded = false;
		try {
			event.getDelta().accept(this);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		if (parsingNeeded == true) {
			parser.getJob().schedule(ASMEditor.REPARSE_DELAY);
		}
	}

	@Override
	public boolean visit(IResourceDelta delta) throws CoreException {
		IResource res = delta.getResource();
		// We're only interested in Files
		if (res instanceof IFile) {
			String filename = delta.getProjectRelativePath().toString();
			for (String child: childdocs) {
				if (child.equals(pathmap.get(filename))) {
					//System.out.println("Childdoc changed: " + filename);
					Logger.log(Logger.INFORMATION, ASMEditor.LOGGER_UI_DEBUG, "Recognized modified child document: " + filename);
					parsingNeeded = true;
				}
			}
		}
		return true;
	}

}
