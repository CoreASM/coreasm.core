package org.coreasm.compiler.classlibrary;

import java.io.File;
import java.util.List;

import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.classlibrary.AbstractLibraryEntry;
import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.codefragment.CodeFragmentException;
import org.coreasm.compiler.exception.EmptyContextStackException;
import org.coreasm.compiler.exception.LibraryEntryException;

/**
 * Advanced LibraryEntry describing a rule.
 * A rule consists of a name, a (possibly empty) list of arguments
 * and a piece of code representing its body.
 * All Rules will be written to the Rules package. 
 * @author Markus Brenner
 * 
 */
public class RuleClassFile extends AbstractLibraryEntry {
	private String ruleName;
	private List<String> arguments;
	private CodeFragment body;
	private CompilerEngine engine;

	/**
	 * Creates a new rule with the given parameters
	 * @param ruleName The name of the rule
	 * @param arguments The arguments of the rule, may not be null
	 * @param body The code of the rule
	 */
	public RuleClassFile(String ruleName, List<String> arguments, CodeFragment body, CompilerEngine engine){
		this.ruleName = ruleName;
		this.arguments = arguments;
		this.body = body;
		this.engine = engine;
	}

	private String generateRule() throws LibraryEntryException {
		String creation = "";
		
		creation = "\t\tjava.util.Map<String, CompilerRuntime.RuleParam> ruleparams = new java.util.HashMap<String, CompilerRuntime.RuleParam>();\n";
		for (int pi = 0; pi < arguments.size(); pi++) {
			creation += "\t\truleparams.put(\"" + arguments.get(pi) + "\", params.get(" + pi + "));\n";
		}
		// a rule might have one extra parameter
		creation += "\t\tif(params.size() == " + arguments.size() + " + 1){\n";
		creation += "\t\truleparams.put(\"result\", params.get(params.size() - 1));\n";
		creation += "}\n";

		//self handling
		creation += "if(getAgent() != null) CompilerRuntime.RuntimeProvider.getRuntime().setSelf(Thread.currentThread(), getAgent());\n";
		creation += "\n//start of generated content\n";
		CodeFragment ruleBody = null;
		try {
			ruleBody = new CodeFragment(
					part1
							+ ruleName
							+ part2
							+ creation
							+ "@@"
							+ part3
							+ ruleName
							+ part4
							+ "public boolean equals(Object o){\nreturn (o instanceof "
							+ ruleName + ");\n}"
									+ "public int parameterCount(){\nreturn " + arguments.size() + ";\n}\n");
			ruleBody.fillSpace(0, this.body);
		} catch (Exception ice) {
			throw new LibraryEntryException("invalid rule body");
		}

		try {
			engine.getVarManager().startContext();
			String result = ruleBody.generateCode(engine);
			try {
				engine.getVarManager().endContext();
			} catch (EmptyContextStackException e) {
				//should never happen
			}
			return result;
		} catch (CodeFragmentException e) {
			throw new LibraryEntryException("invalid rule body");
		} 
	}
	
	@Override
	protected File getFile() {
		return new File(engine.getOptions().tempDirectory + File.separator + "Rules" + File.separator + ruleName + ".java");
	}

	@Override
	protected String generateContent() throws LibraryEntryException {
		String result = "package Rules;\n";
		result += "public class " + ruleName + " extends CompilerRuntime.Rule{\n";
		result += generateRule();
		result += "}\n";
		
		return result;
	}

	@Override
	public String getFullName() {
		return "Rules." + ruleName;
	}

	private final String part1 = "\tpublic ";

	private final String part2 = "(){super();}\n\t"
			+ "\tpublic CompilerRuntime.RuleResult call() throws Exception{\n"
			+ "\t\tlocalStack.pushLayer();\n";

	private final String part3 = "//end of generated content\n\t\t\n"
			+ "\t\tlocalStack.popLayer();\n"
			+ "\t\t@decl(CompilerRuntime.UpdateList, ulist) = (CompilerRuntime.UpdateList) evalStack.pop();\n"
			+ "\t\t@decl(CompilerRuntime.Element, val) = CompilerRuntime.Element.UNDEF;\n"
			+ "\t\tfor(@decl(int,i) = @ulist@.size() - 1; @i@ >= 0; @i@--){\n"
			+ "\t\t\t@decl(CompilerRuntime.Update,u) = @ulist@.get(@i@);\n"
			+ "\t\t\n" + "\t\t\tif(@u@.loc.name.equals(\"result\")){\n"
			+ "\t\t\t\t@val@ = @u@.value;\n" + "\t\t\t\t@ulist@.remove(@i@);\n"
			+ "\t\t\t\tbreak;\n" + "\t\t\t}\n" + "\t\t}\n"
			+ "\t\treturn new CompilerRuntime.RuleResult(@ulist@, @val@);\n" 
			+ "\t}\n" + "\tpublic CompilerRuntime.Rule getCopy(){\n"
			+ "return new ";
	private final String part4 = "();\n" + "}\n";
}
