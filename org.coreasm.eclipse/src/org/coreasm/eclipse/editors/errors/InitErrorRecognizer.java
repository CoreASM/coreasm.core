package org.coreasm.eclipse.editors.errors;

import java.util.LinkedList;
import java.util.List;

import org.coreasm.eclipse.editors.ASMDocument;
import org.coreasm.eclipse.editors.ASMEditor;
import org.coreasm.eclipse.editors.AstTools;
import org.coreasm.eclipse.editors.IconManager;
import org.coreasm.engine.ControlAPI;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.Node;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

/**
 * Checks an ASMDocument for the correct usage of "init" statements.
 * It checks for the following errors:
 * <ul>
 * <li>Is there at least one init statement?</li>
 * <li>Are there more than one init statements?</li>
 * <li>Is there a rule with the given name?</li> 
 * </ul>
 * @author Markus Müller
 */
public class InitErrorRecognizer 
implements ITreeErrorRecognizer
{
	// error code tags
	private static String CLASSNAME = InitErrorRecognizer.class.getCanonicalName();
	private static String NO_INIT = "NoInit";
	private static String MULTI_INITS = "MultiInits";
	private static String UNKN_INIT = "UnknInit";
	
	private ASMEditor parentEditor;
	
	public InitErrorRecognizer(ASMEditor parentEditor) {
		super();
		this.parentEditor = parentEditor;
	}

	@Override
	public void checkForErrors(ASMDocument document, List<AbstractError> errors)
	{
		ControlAPI capi = parentEditor.getParser().getSlimEngine();
		ASTNode root = (ASTNode) document.getRootnode();
		
		// get a list with all initialization nodes
		List<ASTNode> initList = AstTools.findChildNodes(root, AstTools.GRAMMAR_INIT);
		
		// check if there is at least one init node
		if (initList.size() == 0 && !((ASMDocument) document).isIncludedSpecification()) {
			String id = AstTools.findId(root);
			// Mark "CoreASM ID" for the error
			int pos = root.getScannerInfo().charPosition;
			int length = 8 + id.length();
			AbstractError error = new SimpleError("No \"init\" statement", "specification \"" + id + "\" contains no \"init\" statement", root, capi, document, length, CLASSNAME, NO_INIT);
			errors.add(error);
		}
		// check if there are more than one init nodes
		else if (initList.size() > 1) {
			for (ASTNode inode: initList) {
				String id = AstTools.findId(inode);
				// Mark the whole init statement ("init" and ID)
				int pos = inode.getScannerInfo().charPosition;
				int length = 5 + id.length();
				AbstractError error = new SimpleError("Multiple \"init\" statements", "specification contains multiple \"init\" statements", inode, capi, document, length, CLASSNAME, MULTI_INITS);
				errors.add(error);
			}
		}
		
		// check if the rule for each init node is existing
		for (ASTNode inode: initList) {
			ASTNode rnode = getInitRuleDefinition(root, inode);
			if (rnode == null) {
				String id = AstTools.findId(inode);
				// Mark only the ID
				int pos = inode.getScannerInfo().charPosition + 5;
				int length = id.length();
				String name = AstTools.findId(inode);
				String msg = "There is no rule \"" + name + "\"";
				AbstractError error = new SimpleError("Undeclared initalization rule", msg, inode, capi, document, length, CLASSNAME, UNKN_INIT);
				errors.add(error);
			}	
		}
		
	}
	
	/**
	 * Helper method for getting a list of all rule declaration nodes.
	 */
	private ASTNode getInitRuleDefinition(ASTNode root, ASTNode init)
	{
		String initName = AstTools.findId(init); 
		
		ASTNode rulenode = null;
		List<ASTNode> ruleNodes = AstTools.findChildNodes(root, AstTools.GRAMMAR_RULE);
		for (ASTNode node: ruleNodes) {
			if (AstTools.findId(node).equals(initName)) {
				rulenode = node;
				break;
			}
		}
			
		return rulenode;
	}
	
	
	/**
	 * Delivers a list of QuickFixes depending on the given error type.
	 * @param errorID	the tag of the error type of the error to be fix.
	 * @return			a list of QuickFixes for that error type.
	 */
	public static List<AbstractQuickFix> getQuickFixes(String errorID)
	{
		LinkedList<AbstractQuickFix> list = new LinkedList<AbstractQuickFix>();
		
		if (errorID.equals(UNKN_INIT)) {
			list.add(new QF_UnknInit_Create());
			list.add(new QF_UnknInit_Delete());			
			list.add(new QF_UnknInit_Replace());
		}
		if (errorID.equals(NO_INIT)) {
			list.add(new QF_NoInit_Add());
		}
		if (errorID.equals(MULTI_INITS)) {
			list.add(new QF_MultiInits_KeepOne());
		}
				
		return list;
	}
	
	
	/**
	 * Quick fix for adding an init for an existing rule declaration
	 * 
	 * @author Markus Müller
	 */
	public static class QF_NoInit_Add
	extends AbstractQuickFix
	{
		public QF_NoInit_Add()
		{
			super("Add init for", null);
		}
		
		/**
		 * This QuickFix offers the name of each declared rule as a choice
		 * for the new init statement. 
		 */
		@Override
		public void initChoices(AbstractError error)
		{
			if (error instanceof SimpleError && error.getDocument() instanceof ASMDocument)
			{
				ASMDocument doc = (ASMDocument) error.getDocument();
				Node rootnode = doc.getRootnode();
				List<ASTNode> rulenodes = AstTools.findChildNodes((ASTNode) rootnode, AstTools.GRAMMAR_RULE);
				for (ASTNode rulenode: rulenodes)
					choices.add(AstTools.findId(rulenode));
			}
		}
		
		/**
		 * This QuickFix can only be offered to the user if there is at least
		 * one declared rule.
		 */
		@Override
		public boolean checkValidility(AbstractError error) {
			if ( !(error instanceof SimpleError) )
				return false;
			Node rootnode = ((ASMDocument) error.getDocument()).getRootnode();
			List<ASTNode> rulenodes = AstTools.findChildNodes((ASTNode)rootnode, AstTools.GRAMMAR_RULE);
			if (rulenodes.size() == 0)
				return false;
			return true;
		}

		/**
		 * Inserts a new init statement before the first rule declaration.
		 * @param choice The name of the rule which should be declared as init rule.
		 */
		@Override
		public void fix(AbstractError error, String choice)
		{
			if (error instanceof SimpleError && error.getDocument() instanceof ASMDocument) {
				ASMDocument doc = (ASMDocument) error.getDocument();
				Node rootnode = doc.getRootnode();
				Node firstRuleNode = AstTools.findFirstChildNode((ASTNode) rootnode, AstTools.GRAMMAR_RULE);
				int begin = firstRuleNode.getScannerInfo().charPosition;
				String strInit = "init " + choice + "\n\n";
				try {
					doc.replace(begin, 0, strInit);
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
			}
		}
		
		@Override
		public void collectProposals(AbstractError error, List<ICompletionProposal> proposals) {
			if (error instanceof SimpleError && error.getDocument() instanceof ASMDocument) {
				ASMDocument doc = (ASMDocument) error.getDocument();
				Node rootnode = doc.getRootnode();
				Node firstRuleNode = AstTools.findFirstChildNode((ASTNode) rootnode, AstTools.GRAMMAR_RULE);
				int begin = firstRuleNode.getScannerInfo().charPosition;
				for (String choice : choices)
					proposals.add(new CompletionProposal("init " + choice + "\n\n", begin, 0, 0, IconManager.getIcon("/icons/editor/bullet.gif"), prompt + " '" + choice + "'", null, null));
			}
		}
	}
	
	
	/**
	 * Quick fix for deleting all init rules but one which was selected
	 * by the user.
	 * 
	 * @author Markus
	 */
	public static class QF_MultiInits_KeepOne
	extends AbstractQuickFix
	{
		public QF_MultiInits_KeepOne()
		{
			super("Keep: @, delete the others", null);
		}
		
		/**
		 * This QuickFix offers the name of each found init statement as a choice
		 * for the user.
		 */
		@Override
		public void initChoices(AbstractError error)
		{
			if (error instanceof SimpleError && error.getDocument() instanceof ASMDocument)
			{
				ASMDocument doc = (ASMDocument) error.getDocument();
				Node rootnode = doc.getRootnode();
				List<ASTNode> initnodes = AstTools.findChildNodes((ASTNode) rootnode, AstTools.GRAMMAR_INIT);
				for (ASTNode initnode: initnodes)
					choices.add(AstTools.findId(initnode));
			}
		}
		
		/**
		 * Deletes all init statements except the one which was chosen by the user.
		 * @param choice The name of the init statement which is not deleted.
		 */
		@Override
		public void fix(AbstractError error, String choice)
		{
			if (error instanceof SimpleError && error.getDocument() instanceof ASMDocument)
			{
				ASMDocument doc = (ASMDocument) error.getDocument();
				Node rootnode = doc.getRootnode();
				List<ASTNode> initnodes = AstTools.findChildNodes((ASTNode) rootnode, AstTools.GRAMMAR_INIT);
				// walk through list & delete inits backwards, so the indexes
				// of the other inits don't get messed up because of the deletions. 
				for (int i=initnodes.size()-1; i>=0; i--) {
					ASTNode initnode = initnodes.get(i);
					String id = AstTools.findId(initnode);
					if (id.equals(choice))	// skip the chosen init
						continue;
					int begin = initnode.getScannerInfo().charPosition;
					int len = 5 + id.length();
					// check if the whole line can be deleted (when there are only whitespaces left)
					try {
						int lineNr = doc.getLineOfOffset(begin);
						int lineBegin = doc.getLineOffset(lineNr);
						int lineEnd = doc.getLineOffset(lineNr+1);
						String sBefore = doc.get().substring(lineBegin,begin);
						String sEnd = doc.get().substring(begin+len,lineEnd);
						if (sBefore.matches("\\s*") && sEnd.matches("\\s*")) {
							begin = lineBegin;
							len = lineEnd-lineBegin;
						}
					} catch (BadLocationException e1) {
						e1.printStackTrace();
					}
					try {
						doc.replace(begin, len, "");
					} catch (BadLocationException e) {
						e.printStackTrace();
					}
				}
			}
		}
		
		@Override
		public void collectProposals(AbstractError error, List<ICompletionProposal> proposals) {
			if (error instanceof SimpleError && error.getDocument() instanceof ASMDocument)
			{
				for (String choice : choices)
					proposals.add(new QuickFixProposal(this, error, choice));
			}
		}
	}
	
	
	/**
	 * Quick fix for deleting an init statement.
	 * 
	 * @author Markus Müller
	 */
	public static class QF_UnknInit_Delete
	extends AbstractQuickFix
	{
		public QF_UnknInit_Delete() 
		{
			super("Delete", null);
		}
		
		@Override
		public void fix(AbstractError error, String choice) 
		{
			if (error instanceof SimpleError) {
				SimpleError sError = (SimpleError) error;
				int begin = error.getPosition() - 5;
				int len = error.getLength();
				try {
					sError.getDocument().replace(begin, len, "");
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
			}
		}
		
		@Override
		public void collectProposals(AbstractError error, List<ICompletionProposal> proposals) {
			if (error instanceof SimpleError)
				proposals.add(new CompletionProposal("", error.getPosition() - 5, error.getLength(), 0, IconManager.getIcon("/icons/editor/bullet.gif"), prompt, null, null));
		}
	}
	
	
	/** 
	 * Quick fix for creating a new rule with the name of the given
	 * include statement if a rule with that name doesn't exist.
	 * 
	 * @author Markus Müller
	 */
	public static class QF_UnknInit_Create
	extends AbstractQuickFix
	{
		public QF_UnknInit_Create()
		{
			super("Create", null);
		}
		
		public void fix(AbstractError error, String choice)
		{
			if (error instanceof SimpleError) {
				SimpleError sError = (SimpleError) error;
				ASMDocument document = (ASMDocument) sError.getDocument();
				
				// Read rule name from document
				String rulename = document.get().substring(error.getPosition(), error.getPosition() + error.getLength());
				
				// The new rule should be inserted before the first existing rule
				ASTNode firstRuleNode = AstTools.findFirstChildNode((ASTNode) document.getRootnode(), AstTools.GRAMMAR_RULE);
				int offset = document.getLength();
				if (firstRuleNode != null) {
					offset = firstRuleNode.getScannerInfo().charPosition;
				}
				String replacement = "\n\nrule " + rulename + " =\n\tskip\n\n";
				try {
					document.replace(offset, 0, replacement);
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
			}
		}
		
		@Override
		public void collectProposals(AbstractError error, List<ICompletionProposal> proposals) {
			if (error instanceof SimpleError) {
				SimpleError sError = (SimpleError) error;
				ASMDocument document = (ASMDocument) sError.getDocument();
				
				// Read rule name from document
				String rulename = document.get().substring(error.getPosition(), error.getPosition() + error.getLength());
				
				// The new rule should be inserted before the first existing rule
				ASTNode firstRuleNode = AstTools.findFirstChildNode((ASTNode) document.getRootnode(), AstTools.GRAMMAR_RULE);
				int offset = document.getLength();
				if (firstRuleNode != null)
					offset = firstRuleNode.getScannerInfo().charPosition;
				proposals.add(new CompletionProposal("\n\nrule " + rulename + " =\n\tskip\n\n", offset, 0, 0, IconManager.getIcon("/icons/editor/bullet.gif"), prompt, null, null));
			}
		}
	}
	
	
	/** 
	 * Quick fix for replacing the rule name of an init statement with the
	 * name of an existing rule.
	 *  
	 * @author Markus
	 */
	public static class QF_UnknInit_Replace
	extends AbstractQuickFix
	{
		public QF_UnknInit_Replace()
		{
			super("Relpace", null);
		}
		
		/**
		 * This QuickFix can only be offered to the user if there is at least
		 * one declared rule.
		 */
		@Override
		public boolean checkValidility(AbstractError error) {
			if ( !(error instanceof SimpleError) )
				return false;
			Node rootnode = ((ASMDocument) error.getDocument()).getRootnode();
			List<ASTNode> rulenodes = AstTools.findChildNodes((ASTNode)rootnode, AstTools.GRAMMAR_RULE);
			if (rulenodes.size() == 0)
				return false;
			return true;			
		}

		/**
		 * This QuickFix offers the name of each declared rule as a choice
		 * for the new init statement. 
		 */
		@Override
		public void initChoices(AbstractError error)
		{
			if (error instanceof SimpleError && error.getDocument() instanceof ASMDocument) {
				ASMDocument doc = (ASMDocument) error.getDocument();
				Node rootnode = doc.getRootnode();
				List<ASTNode> rulenodes = AstTools.findChildNodes((ASTNode)rootnode, AstTools.GRAMMAR_RULE);
				for (ASTNode rulenode: rulenodes)
					choices.add(AstTools.findId(rulenode));
			}
		}

		@Override
		public void fix(AbstractError error, String choice)
		{
			if (error instanceof SimpleError) {
				int begin = error.getPosition();
				int len = error.getLength();
				try {
					error.getDocument().replace(begin, len, choice);
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
			}
		}
		
		@Override
		public void collectProposals(AbstractError error, List<ICompletionProposal> proposals) {
			if (error instanceof SimpleError) {
				for (String choice : choices)
					proposals.add(new CompletionProposal(choice, error.getPosition(), error.getLength(), 0, IconManager.getIcon("/icons/editor/bullet.gif"), prompt + " with '" + choice + "'", null, null));
			}
		}
	}


}
