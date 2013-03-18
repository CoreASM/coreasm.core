package org.coreasm.eclipse.editors.errors;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.coreasm.eclipse.editors.ASMDocument;
import org.coreasm.eclipse.editors.AstTools;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.interpreter.Node;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

/**
 * Checks an ASMDocument for the correct usage of rule declaration.
 * It checks if several rules have the same name.
 * @author Markus Müller
 */
public class RuleErrorRecognizer 
implements ITreeErrorRecognizer
{
	// error code tags
	private static String CLASSNAME = RuleErrorRecognizer.class.getCanonicalName();
	private static String MULTI_NAME = "MultiName"; 

	@Override
	public void checkForErrors(ASMDocument document, List<AbstractError> errors)
	{
		List<ASTNode> rulenodes = getRules((ASTNode) document.getRootnode());
		if (rulenodes.size() > 0) {
			for (ASTNode n: rulenodes) {
				String msg = "There are multiple rules with this name";
				Node idNode = AstTools.findIdNode(n);
				int pos = idNode.getScannerInfo().charPosition;
				AbstractError error = new SimpleError("naming conflict", msg, pos, idNode.getToken().length(), CLASSNAME, MULTI_NAME);
				errors.add(error);
			}
		}

	}
	
	/**
	 * Helper method for getting a list with all rule declaration nodes from the
	 * syntax tree.
	 */
	private List<ASTNode> getRules(ASTNode root)
	{
		List<ASTNode> rulesToReturn = new LinkedList<ASTNode>();
		
		Set<String> allNames = new HashSet<String>();
		Set<String> multipleNames = new HashSet<String>();
		
		List<ASTNode> childnodes = AstTools.findChildNodes(root, "RuleDeclaration");
		for (ASTNode child: childnodes) {
			String childname = AstTools.findId(child);
			if (allNames.contains(childname))
				multipleNames.add(childname);
			else
				allNames.add(childname);
		}
		
		for (ASTNode child: childnodes) {
			String childname = AstTools.findId(child);
			if (multipleNames.contains(childname))
				rulesToReturn.add(child);
		}
		return rulesToReturn;
	}
	
	/**
	 * Delivers a list of QuickFixes depending on the given error type.
	 * @param errorID	the tag of the error type of the error to be fix.
	 * @return			a list of QuickFixes for that error type.
	 */
	public static List<AbstractQuickFix> getQuickFixes(String errorID)
	{
		List<AbstractQuickFix> fixes = new LinkedList<AbstractQuickFix>();
		
		if (errorID.equals(MULTI_NAME)) {
			fixes.add(new QF_MultiName_Rename());
		}
		
		return fixes;
	}
	
	
	/**
	 * Quick fix for renaming rules with the same name to get them an unique name.
	 * The rules are renamed by adding an integer to them
	 * (rule -> rule, rule_1, rule_2, ...)
	 * 
	 * @author Markus Müller
	 */
	public static class QF_MultiName_Rename
	extends AbstractQuickFix
	{
		public QF_MultiName_Rename()
		{
			super("Rename all rules", null);
		}
		
		@Override
		public void fix(AbstractError error, String choice)
		{
			if (error instanceof SimpleError && error.getDocument() instanceof ASMDocument)
			{
				ASMDocument doc = (ASMDocument) error.getDocument();
				Node rootnode = doc.getRootnode();
				List<ASTNode> rulenodes = AstTools.findChildNodes((ASTNode) rootnode, "RuleDeclaration");
				
				// collect the name of all rules & get the name of the rule
				// for which the hover was shown.
				Set<String> names = new HashSet<String>();
				String oldname = "";
				for (ASTNode rulenode: rulenodes) {
					Node idnode = AstTools.findIdNode(rulenode);
					names.add(idnode.getToken());
					if (idnode.getScannerInfo().charPosition == error.getPosition())
						oldname = idnode.getToken();
				}
			
				// create a new name for each rule declaration and store it
				int counter = 0;
				Map<ASTNode, String> newnames = new HashMap<ASTNode, String>();
				for (ASTNode rulenode: rulenodes) {
					Node idnode = AstTools.findIdNode(rulenode);
					String name = idnode.getToken();
					if (!name.equals(oldname))
						continue;
					if (counter == 0) {
						counter++;
						continue;
					}
					String newname;
					do {
						newname = oldname + "_" + counter;
						counter++;
					} while (names.contains(newname));
					names.add(newname);
					newnames.put(rulenode,newname);
				}
				
				// replace rule names backwards (to prevent offset changes because of the new names)
				for (int i=rulenodes.size()-1; i>=0; i--) {
					Node rulenode = rulenodes.get(i);
					if (!newnames.containsKey(rulenode))
						continue;
					int pos = AstTools.findIdNode(rulenode).getScannerInfo().charPosition;
					int len = oldname.length();
					String newname = newnames.get(rulenode);
					try {
						doc.replace(pos, len, newname);
					} catch (BadLocationException e) {
						e.printStackTrace();
					}
				}
				
			}
		}
		
		@Override
		public void collectProposals(AbstractError error, List<ICompletionProposal> proposals) {
			if (error instanceof SimpleError)
				proposals.add(new QuickFixProposal(this, error, null));
		}
	}

}
