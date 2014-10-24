package org.coreasm.eclipse.editors.errors;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.coreasm.eclipse.editors.ASMDocument;
import org.coreasm.eclipse.editors.ASMEditor;
import org.coreasm.eclipse.editors.FileManager;
import org.coreasm.eclipse.util.IconManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.ui.texteditor.MarkerUtilities;

/**
 * Checks an ASMDocument for the correct usage of "include" statements.
 * It checks for the following errors:
 * <ul>
 * <li>Is the include statement in its own line?</li>
 * <li>Is the filename a valid one, and does the file exist?</li>
 * <li>If the statement part of an multiline comment?</li> 
 * </ul>
 * @author Markus M�ller
 */
public class ModularityErrorRecognizer implements ITextErrorRecognizer {

	// error code tags
	private static final String CLASSNAME = ModularityErrorRecognizer.class.getCanonicalName();
	private static final String CODE_BEFORE = "CodeBefore";
	private static final String CODE_AFTER = "CodeAfter";
	private static final String INVALID_NAME = "InvalidName";
	private static final String FILE_NOT_FOUND = "FileNotFound";
	private static final String MULTI_COMMENT = "MultiComment";
	private static final String CHILD_ERROR = "ChildError";
	private static final String MISSING_PLUGINS = "MissingPlugins";
	private static final String OUT_OF_PROJECT = "OutOfProject";
	
	// PATTERN_RAW: recognizes any of: include "filename"
	// PATTERN_CODE_OK: recognizes lines which contains only an include and whitespaces
	// PATTERN_COMMENT_OK: recognizes lines with an commented include statement: // include "filename"
	private static final String PATTERN_RAW = "include\\s+\"([^\"\\n]+)\"";
	private static final String PATTERN_CODE_OK = "\\s*" + PATTERN_RAW + "\\s*";
	private static final String PATTERN_COMMENT_OK = "\\s*//.*";
	
	private static final String PATTERN_BEFORE = "\\s*";
	private static final String PATTERN_AFTER = "\\s*";
	
	private ASMEditor parentEditor;
	
	public ModularityErrorRecognizer(ASMEditor parentEditor) {
		super();
		this.parentEditor = parentEditor;
	}


	@Override
	public void checkForErrors(ASMDocument document, List<AbstractError> errors) 
	{	
		List<IncludeStatement> includes = getIncludes(document);
		for (IncludeStatement include: includes) {
			
			// build a string which contains the whole line
			IRegion lineInfo = null;
			try {
				lineInfo = document.getLineInformationOfOffset(include.position);
			}
			catch (BadLocationException e) {
				e.printStackTrace();
			}
			if (lineInfo == null)
				continue;
			String line = document.get().substring(lineInfo.getOffset(), lineInfo.getOffset()+lineInfo.getLength());

			// is line commented correctly? 
			// -> dont't perform any further tests
			if (line.matches(PATTERN_COMMENT_OK))
				continue;
			
			// is keyword commented anyway (check content type)
			// -> line is part of a multiline comment
			if ( ! include.partitiontype.equals(ASMEditor.PARTITION_CODE) ) {
				String title = "\"include\" statement inside multiline comment";
				String message = "An include statement was found inside a multiline comment.\nThe CoreASM engine will still include the specified file here.\nUse single line comments to deactivate include statements.";;
				// Mark the whole include statement
				int pos = include.position;
				int length = include.source.length();
				AbstractError error = new SimpleError(title, message, pos, length, CLASSNAME, MULTI_COMMENT);
				errors.add(error);
				// don't perform any further tests
				continue;			
			}
			
			// is line correct? (only whitespaces before keyword and after filename)
			// -> yes: check if filename is valid, if file is existing and if the file has errors
			// -> no: check if there are non-whitespace chars before keyword or after filename
			if (line.matches(PATTERN_CODE_OK)) {
				// YES:
				if (FileManager.isFilenameValid(include.filename) == false) {
					String title = "Invalid filename";
					String message = "The filname \"" + include.filename + "\" is not a valid filename.";
					// Mark filename
					int pos = include.position + include.source.indexOf("\"") + 1;
					int length = include.filename.length();
					AbstractError error = new SimpleError(title, message, pos, length, CLASSNAME, INVALID_NAME);
					errors.add(error);
				} else if (include.filenameProj == null) { 
					String title = "File out of project";
					String message = "The filename \"" + include.filename + "\" leads to a location out of the project";
					// Mark filename
					int pos = include.position + include.source.indexOf("\"") +1;
					int length = include.filename.length();
					AbstractError error = new SimpleError(title, message, pos, length, CLASSNAME, OUT_OF_PROJECT);
					errors.add(error);
				} else if (FileManager.fileExits(include.filenameProj, parentEditor.getInputFile().getProject()) == false) {
					String title = "File does not exist";
					String message = "The file \"" + include.filename + "\" does not exist.";
					// Mark filename
					int pos = include.position + include.source.indexOf("\"") + 1;
					int length = include.filename.length();
					AbstractError error = new SimpleError(title, message, pos, length, CLASSNAME, FILE_NOT_FOUND);
					errors.add(error);
				} else {
					IFile file = FileManager.getFile(include.filenameProj, parentEditor.getInputFile().getProject());
					IMarker[] markers = null;
					try {
						markers = file.findMarkers(ASMEditor.MARKER_TYPE_PROBLEM, false, IResource.DEPTH_ZERO);
						for (IMarker marker : markers) {
							if (MarkerUtilities.getSeverity(marker) == IMarker.SEVERITY_ERROR) {
								String title = "Included file has errors";
								String message = "The included file \"" + include.filename + "\" has errors";
								// Mark filename
								int pos = include.position + include.source.indexOf("\"") + 1;
								int length = include.filename.length();
								AbstractError error = new SimpleError(title, message, pos, length, CLASSNAME, CHILD_ERROR);
								errors.add(error);
								break;
							}
						}
					} catch (CoreException e) {
						e.printStackTrace();
					}
				
					checkIncludedPlugins(include, document, errors);
				}
			} else {
				// NO:
				checkLineWithError(include, line, errors);
			}
			
		}

	}

	/**
	 * Gets a list with all include statements from the given doument's syntax
	 * tree.
	 */
	private List<IncludeStatement> getIncludes(IDocument document)
	{
		List<IncludeStatement> includes = new LinkedList<IncludeStatement>();
		
		Pattern pattern = Pattern.compile(PATTERN_RAW);
		Matcher matcher = pattern.matcher(document.get());
		
		while (matcher.find() == true) {
			String source = matcher.group(0);
			String name = matcher.group(1);
			int pos = matcher.start(0);
			int length = source.length();
			String partitiontype = "";
			try {
				ITypedRegion partition = document.getPartition(pos);
				partitiontype = partition.getType();
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
			IncludeStatement is = new IncludeStatement(pos, length, source, name, partitiontype);
			includes.add(is);
		}
		
		return includes;
	}
		
	/**
	 * Helper method for checking incorrect include lines, which contain code or
	 * comments before or after the include statement
	 */
		private void checkLineWithError(IncludeStatement include, String line, List<AbstractError> errors)
	{
		int offset = line.indexOf("include");
		
		// Check if there is something except whitespaces before the include
		String before = line.substring(0, offset);
		if ( ! before.matches(PATTERN_BEFORE)) {
			String title = "Code before \"include\"";
			String message = "There is code or a multiline comment preciding an include statement within the same line.\nThe CoreASM engine will ignore this include statement";
			// Mark everything before the include statement, except leading and trailing whitespaces.
			int spacesbefore = 0;
			while (Character.isWhitespace(before.charAt(spacesbefore)))
				spacesbefore++;
			int spacesafter = 0;
			while (Character.isWhitespace(before.charAt(before.length()-1-spacesafter)))
				spacesafter++;
			int pos = include.position - (before.length() - spacesbefore);
			int length = before.length() - spacesbefore - spacesafter;
			AbstractError error = new SimpleError(title, message, pos, length, CLASSNAME, CODE_BEFORE);
			errors.add(error);
		}

		// Check if there is something except whitespaces after the include
		String after = line.substring(offset+include.length);
		if ( ! after.matches(PATTERN_AFTER)) {
			String title = "Code or comment after \"include\"";
			String message = "There is code or a comment following an include statement within the same line.\nThis causes the CoreASM engine to throw an error.";
			// Mark everything after the include statement, except leading and trailing whitespaces.
			int spacesbefore = 0;
			while (Character.isWhitespace(after.charAt(spacesbefore)))
				spacesbefore++;
			int spacesafter = 0;
			while (Character.isWhitespace(after.charAt(after.length()-1-spacesafter)))
				spacesafter++;
			int pos = include.position + include.length + spacesbefore;
			int length = after.length() - spacesbefore - spacesafter;
			AbstractError error = new SimpleError(title, message, pos, length, CLASSNAME, CODE_AFTER);
			errors.add(error);			
		}
		
	}
		
	/**
	 * Helper method for checking if this specification uses all plugins which
	 * are used by a child specification.
	 */
	private void checkIncludedPlugins(IncludeStatement include, ASMDocument document, List<AbstractError> errors)
	{
		
		// Get the names of all plugins being used by this specification
		// and store them in pluginsThis.
		// TODO will be done for each include!
		Set<String> pluginsThis = new HashSet<String>();
		IMarker[] markers = null;
		try {
			markers = parentEditor.getInputFile().findMarkers(ASMEditor.MARKER_TYPE_PLUGINS, false, IResource.DEPTH_ZERO);
		} catch (CoreException e1) {
			e1.printStackTrace();
		}
		if (markers != null && markers.length > 0) {
			IMarker marker = markers[0];
			String str = "";
			try {
				str = (String) marker.getAttribute("plugins");
			} catch (CoreException e) {
				e.printStackTrace();
			}
			for (String p: str.split("/"))
				pluginsThis.add(p);
		}
		
		// Get the names of all plugins being used by the included specification
		// and store them in pluginsIncluded
		IFile file = FileManager.getFile(include.filenameProj, parentEditor.getInputFile().getProject());
		try {
			markers = file.findMarkers(ASMEditor.MARKER_TYPE_PLUGINS, false, IResource.DEPTH_ZERO);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		
		if (markers != null && markers.length > 0) {
			IMarker marker = markers[0];
			String str = "";
			try {
				str = (String) marker.getAttribute("plugins");
			} catch (CoreException e) {
				e.printStackTrace();
			}
			String[] pluginsIncluded = str.split("/");			
			
			// Compare pluginsIncluded and pluginsThis, and store
			// all missing plugins in pluginsMissing
			List<String> pluginsMissing = new LinkedList<String>();
			for (String plugin: pluginsIncluded) {
				if (pluginsThis.contains(plugin) == false)
					pluginsMissing.add(plugin);
			}
			
			// Throw out all plugins from pluginsMissing which are already
			// loaded because of a PackagePlugin (i.e. Number through Standard)
			ListIterator<String> it = pluginsMissing.listIterator();
			while (it.hasNext()) {
				String plugin = it.next();
				if (parentEditor.getParser().isPluginLoaded(plugin) == true)
					it.remove();
			}
			
			// If there are missing plugins, create an error object.
			if (pluginsMissing.size() > 0) {
				String title = "Missing plugins";
				StringBuilder message = new StringBuilder(250);
				message.append(include.filename);
				message.append(" uses plugins which are not used in this specification.\n");
				message.append("The CoreASM engine will ignore these plugins.\nMissing plugins: ");
				for (int i=0; i<pluginsMissing.size(); i++) {
					message.append(pluginsMissing.get(i));
					if (i < pluginsMissing.size()-1)
						message.append(", ");
				}
				int position = include.position;
				int length = include.source.length();
				AbstractError error = new SimpleError(title, message.toString(), position, length, CLASSNAME, MISSING_PLUGINS);
				errors.add(error);
			}
			
		}
		
	}
	
	/**
	 * Delivers a list of QuickFixes depending on the given error type.
	 * @param errorID	the tag of the error type of the error to be fix.
	 * @return			a list of QuickFixes for that error type.
	 */
	public static List<AbstractQuickFix> getQuickFixes(String errorID)
	{
		List<AbstractQuickFix> fixes = new LinkedList<AbstractQuickFix>();
		
		if (errorID.equals(MULTI_COMMENT)) {
			fixes.add(new AbstractQuickFix.QF_Replace("Delete", "", true));
		}
		if (errorID.equals(CODE_BEFORE)) {
			fixes.add(new AbstractQuickFix.QF_Replace("Delete", "", true));
		}
		if (errorID.equals(CODE_AFTER)) {
			fixes.add(new AbstractQuickFix.QF_Replace("Delete", "", true));
		}
		if (errorID.equals(FILE_NOT_FOUND)) {
			fixes.add(new QF_FileNotFound_Create());
		}
		if (errorID.equals(CHILD_ERROR)) {
			fixes.add(new QF_ChildError_Open());
		}
		if (errorID.equals(MISSING_PLUGINS)) {
			fixes.add(new QF_Dependency_AddAll());
		}

		return fixes;
	}
	
		
	/**
	 * Helper class for storing include statements with their relevant data
	 * @author Markus M�ller
	 */
	private class IncludeStatement
	{
		final int position;		// the offset of the statement within the document
		final int length;		// the length of the include statement
		final String source;	// the whole statement (include "filename")
		final String filename;	// the filename of the statement
		final String filenameProj;	// the filename, relative to the project
		final String partitiontype; // the content type the statement is standing in
		
		private IncludeStatement(int position, int length, String source, String filename,
				String partitiontype) {
			super();
			this.position = position;
			this.length = length;
			this.source = source;
			this.filename = filename;
			this.filenameProj = FileManager.getFilenameRelativeToProject(filename, parentEditor.getInputFile());
			this.partitiontype = partitiontype;
		}
		
	}
	
	
	/**
	 * QuickFix for creating a new file if there is an include statement referring
	 * to a non-existing file. Also opens an editor for the new file.
	 */
	public static class QF_FileNotFound_Create
	extends AbstractQuickFix
	{
		public QF_FileNotFound_Create()
		{
			super("Create file", null);
		}
		
		@Override
		public void fix(AbstractError error, String choice)
		{
			if (error instanceof SimpleError) {
				SimpleError sError = (SimpleError) error;
				
				// Parse names of the filename out of hover message
				int pos1 = sError.getDescription().indexOf('"') + 1;
				int pos2 = sError.getDescription().indexOf('"', pos1);
				String filename = sError.getDescription().substring(pos1, pos2);
				
				// Get the filename relative to the project
				ASMEditor editor = (ASMEditor) FileManager.getActiveEditor();
				String filenameProj = FileManager.getFilenameRelativeToProject(filename, editor.getInputFile());
				
				FileManager.createFile(filenameProj, FileManager.getActiveProject());
				FileManager.openEditor(filenameProj, FileManager.getActiveProject());
			}
		}
		
		@Override
		public void collectProposals(AbstractError error, List<ICompletionProposal> proposals) {
			if (error instanceof SimpleError)
				proposals.add(new QuickFixProposal(this, error, null));
		}
	}
	
	
	/**
	 * QuickFix for opening an included document if this document contains errors.
	 * @author Markus M�ller
	 */
	public static class QF_ChildError_Open
	extends AbstractQuickFix
	{
		public QF_ChildError_Open()
		{
			super("Open", null);
		}
		
		@Override
		public void fix(AbstractError error, String choice)
		{
			if (error instanceof SimpleError)
			{
				SimpleError sError = (SimpleError) error;
				
				// Parse names of the filename out of hover message
				int pos1 = sError.getDescription().indexOf('"') + 1;
				int pos2 = sError.getDescription().indexOf('"', pos1);
				String filename = sError.getDescription().substring(pos1, pos2);
				ASMEditor editor = (ASMEditor)FileManager.getActiveEditor();
				
				FileManager.openEditor(editor.getInputFile().getProjectRelativePath().removeLastSegments(1).append(filename).toString(), FileManager.getActiveProject());
			}
		}
		
		@Override
		public void collectProposals(AbstractError error, List<ICompletionProposal> proposals) {
			if (error instanceof SimpleError)
				proposals.add(new QuickFixProposal(this, error, null));
		}
	}

	
	/**
	 * Quick fix for adding uses for missing plugins
	 * 
	 * @author Markus
	 */
	public static class QF_Dependency_AddAll
	extends AbstractQuickFix
	{
		public QF_Dependency_AddAll()
		{
			super("Add all missing plugins", null);
		}

		@Override
		public void fix(AbstractError error, String choice)
		{
			if (error instanceof SimpleError) {
				SimpleError sError = (SimpleError) error;
				
				// Parse names of missing plugins out of hover message
				int pos = sError.getDescription().indexOf(':') + 1;
				String strList = sError.getDescription().substring(pos).trim();
				String[] plugins = strList.split(", ");
				
				StringBuilder sb = new StringBuilder();
				for (String pl: plugins) {
					String _pl = pl;
					if (pl.endsWith("Plugin"))
						_pl = pl.substring(0,pl.length()-6);
					else if (pl.endsWith("Plugins"))
						_pl = pl.substring(0,pl.length()-7);
					sb.append("use ").append(_pl).append("\n");
				}
				
				int begin = error.getPosition();
				int len = 0;
				try {
					error.getDocument().replace(begin,len,sb.toString());
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
				
			}
		}
		
		@Override
		public void collectProposals(AbstractError error, List<ICompletionProposal> proposals) {
			if (error instanceof SimpleError) {
				SimpleError sError = (SimpleError) error;
				
				// Parse names of missing plugins out of hover message
				int pos = sError.getDescription().indexOf(':') + 1;
				String strList = sError.getDescription().substring(pos).trim();
				String[] plugins = strList.split(", ");
				
				StringBuilder sb = new StringBuilder();
				for (String pl: plugins) {
					String _pl = pl;
					if (pl.endsWith("Plugin"))
						_pl = pl.substring(0,pl.length()-6);
					else if (pl.endsWith("Plugins"))
						_pl = pl.substring(0,pl.length()-7);
					sb.append("use ").append(_pl).append("\n");
				}
				
				proposals.add(new CompletionProposal(sb.toString(), error.getPosition(), 0, 0, IconManager.getIcon("/icons/editor/bullet.gif"), prompt, null, null));
			}
		}
	}
}
