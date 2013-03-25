package org.coreasm.eclipse.editors;

import java.util.HashSet;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import org.coreasm.eclipse.editors.ASMParser.ParsingResult;
import org.coreasm.eclipse.editors.errors.AbstractError;
import org.coreasm.engine.interpreter.Node;
import org.coreasm.engine.plugins.modularity.ModularityPlugin.IncludeNode;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

/**
 * The <code>IncludeWatcher</code> watches included specifications.
 * Whenever an included specification changes, the parent specification gets reparsed.
 * @author Michael Stegmaier
 *
 */
public class ASMIncludeWatcher
implements Observer, IResourceChangeListener, IResourceDeltaVisitor
{
	private final ASMEditor editor;
	private boolean shouldReparse = false;

	public ASMIncludeWatcher(ASMEditor editor)
	{
		this.editor = editor;
	}
	
	/**
	 * Returns the specifications that are included in the given file.
	 * @param file file to return the included specifications of
	 * @param transitive <code>true</code> if specifications should be collected transitively; <code>false</code> otherwise
	 * @return the set of included files
	 */
	public static Set<IFile> getIncludedFiles(IFile file, boolean transitive) {
		Set<IFile> includedFiles = new HashSet<IFile>();
		collectIncludedFiles(file, transitive, includedFiles);
		return includedFiles;
	}
	
	/**
	 * Collects the specifications that are included in the given file.
	 * @param file file to collect the included specifications of
	 * @param transitive <code>true</code> if specifications should be collected transitively; <code>false</code> otherwise
	 * @param includedFiles the set to collect the included files into
	 */
	private static void collectIncludedFiles(IFile file, boolean transitive, Set<IFile> includedFiles) {
		if (file == null)
			return;
		try {
			IProject project = file.getProject();
			if (file.exists()) {
				IMarker[] includeMarker = file.findMarkers(ASMEditor.MARKER_TYPE_INCLUDE, false, IResource.DEPTH_ZERO);
				if (includeMarker.length > 0) {
					for (String include : includeMarker[0].getAttribute("includes", "").split(AbstractError.SEPERATOR_VAL)) {
						IFile includedFile = project.getFile(include);
						if (!includedFiles.contains(includedFile)) {
							includedFiles.add(includedFile);
							if (transitive)
								collectIncludedFiles(includedFile, transitive, includedFiles);
						}
					}
				}
			}
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void update(Observable o, Object arg) {
		if (o != editor.getParser() || !(arg instanceof ParsingResult))
			return;
		ParsingResult result = (ParsingResult)arg;

		Node rootNode = result.document.getRootnode();
		if (rootNode != null) {
			Set<IPath> includedFiles = new HashSet<IPath>();
			IPath relativePath = editor.getInputFile().getProjectRelativePath().removeLastSegments(1);
			for (Node node = rootNode.getFirstCSTNode(); node != null; node = node.getNextCSTNode()) {
				if (node instanceof IncludeNode)
					includedFiles.add(relativePath.append(((IncludeNode)node).getFilename()));
			}
			editor.createIncludeMark(includedFiles);
		}
	}

	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		shouldReparse = false;
		try {
			event.getDelta().accept(this);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		if (shouldReparse)
			editor.getParser().getJob().schedule(ASMEditor.REPARSE_DELAY);
	}

	@Override
	public boolean visit(IResourceDelta delta) throws CoreException {
		IResource resource = delta.getResource();
		if (resource instanceof IFile && !resource.equals(editor.getInputFile())) {
			if (getIncludedFiles(editor.getInputFile(), true).contains(resource))
				shouldReparse = true;
		}
		return true;
	}
}
