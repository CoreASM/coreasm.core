package org.coreasm.eclipse.editors.contentassist;

import org.eclipse.jface.text.templates.GlobalTemplateVariables;

/**
 * @author Tobias
 * 
 * The context type "coreasm" describes what templates to grab when opening 
 * the default template.xml files (e.g coreasm-templtes.xml)
 */
public class ASMTemplateContextType extends org.eclipse.jface.text.templates.TemplateContextType {

	public static final String CONTEXT_TYPE = "coreasm";
	  
	public ASMTemplateContextType() {		
		addResolver(new GlobalTemplateVariables.Cursor());
		addResolver(new GlobalTemplateVariables.WordSelection());
		addResolver(new GlobalTemplateVariables.LineSelection());
		addResolver(new GlobalTemplateVariables.Dollar());
	}
}
