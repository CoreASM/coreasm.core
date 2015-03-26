package org.coreasm.compiler.components.classlibrary;

import java.util.List;

import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.codefragment.CodeFragmentException;
import org.coreasm.compiler.exception.EmptyContextStackException;
import org.coreasm.compiler.exception.LibraryEntryException;
import org.coreasm.compiler.paths.CompilerPathConfig;

/**
 * LibraryEntry describing a rule.
 * A rule consists of a name, a (possibly empty) list of arguments
 * and a piece of code representing its body.
 * Rules always have the {@link LibraryEntryType} Rule
 * @author Markus Brenner
 * 
 */
public class RuleClassFile extends MemoryInclude {
	private String ruleName;
	private List<String> arguments;
	private CodeFragment body;
	private CompilerEngine engine;

	/**
	 * Creates a new rule with the given parameters
	 * @param ruleName The name of the rule
	 * @param arguments The arguments of the rule, may not be null
	 * @param body The code of the rule
	 * @param engine The compiler engine supervising the compilation process
	 */
	public RuleClassFile(String ruleName, List<String> arguments, CodeFragment body, CompilerEngine engine){
		super(engine, ruleName, "Kernel", LibraryEntryType.RULE);
		this.ruleName = ruleName;
		this.arguments = arguments;
		this.body = body;
		this.engine = engine;
	}

	private String generateRule() throws LibraryEntryException {
		String creation = "";
		
		creation = "\t\tjava.util.Map<String, " + engine.getPath().runtimePkg() + ".RuleParam> ruleparams = new java.util.HashMap<String, " + engine.getPath().runtimePkg() + ".RuleParam>();\n";
		for (int pi = 0; pi < arguments.size(); pi++) {
			creation += "\t\truleparams.put(\"" + arguments.get(pi) + "\", params.get(" + pi + "));\n";
		}
		// a rule might have one extra parameter
		creation += "\t\tif(params.size() == " + arguments.size() + " + 1){\n";
		creation += "\t\truleparams.put(\"result\", params.get(params.size() - 1));\n";
		creation += "}\n";

		//self handling
		creation += "if(getAgent() != null) " + engine.getPath().runtimeProvider() + ".setSelf(Thread.currentThread(), getAgent());\n";
		creation += "\n//start of generated content\n";
		String ruleBody;
		try{
			engine.getVarManager().startContext();
			CodeFragment ruleFragment = new CodeFragment(part1 + ruleName + part2 + creation);
			ruleFragment.appendFragment(body);
			ruleFragment.appendLine(part3 + ruleName + part4);
			ruleBody = ruleFragment.generateCode(engine) + "public String toString(){\nreturn \"@" + ruleName + "\";\n}\n"
					+ "public boolean equals(Object o){\nreturn (o instanceof "
					+ ruleName + ");\n}"
					+ "public int parameterCount(){\nreturn " + arguments.size() + ";\n}\n";
			engine.getVarManager().endContext();
		}
		catch(CodeFragmentException cfe){
			String msg = "Incorrect code in rule body for rule '" + ruleName;
			engine.addError(msg);
			engine.getLogger().error(this.getClass(), msg);
			throw new LibraryEntryException(cfe);
		}
		catch(EmptyContextStackException ecse){
			//should never happen!
			String msg = "Unexpected error: Could not close final variable context in rule " + ruleName;
			engine.addError(msg);
			engine.getLogger().error(this.getClass(), msg);
			throw new LibraryEntryException(ecse);
		}
		return ruleBody;
	}

	@Override
	protected String buildContent(String entryName) throws LibraryEntryException{
		buildStrings(engine.getPath());
		String result = "package " + getPackage(entryName) + ";\n";
		result += "public class " + ruleName + " extends " + engine.getPath().runtimePkg() + ".Rule" + "{\n";
		result += generateRule();
		result += "}\n";
		
		return result;
	}
	
	private void buildStrings(CompilerPathConfig path){
		part1 = "\tpublic ";

		part2 = "(){super();}\n\t"
				+ "\tpublic " + engine.getPath().runtimePkg() + ".RuleResult call() throws Exception{\n"
				+ "\t\tlocalStack.pushLayer();\n";

		part3 = "//end of generated content\n\t\t\n"
				+ "\t\tlocalStack.popLayer();\n"
				+ "\t\t@decl(" + engine.getPath().runtimePkg() + ".UpdateList, ulist) = (" + engine.getPath().runtimePkg() + ".UpdateList) evalStack.pop();\n"
				+ "\t\t@decl(" + engine.getPath().runtimePkg() + ".Element, val) = " + engine.getPath().runtimePkg() + ".Element.UNDEF;\n"
				+ "\t\tfor(@decl(int,i) = @ulist@.size() - 1; @i@ >= 0; @i@--){\n"
				+ "\t\t\t@decl(" + engine.getPath().runtimePkg() + ".Update,u) = @ulist@.get(@i@);\n"
				+ "\t\t\n" + "\t\t\tif(@u@.loc.name.equals(\"result\")){\n"
				+ "\t\t\t\t@val@ = @u@.value;\n" + "\t\t\t\t@ulist@.remove(@i@);\n"
				+ "\t\t\t\tbreak;\n" + "\t\t\t}\n" + "\t\t}\n"
				+ "\t\treturn new " + engine.getPath().runtimePkg() + ".RuleResult(@ulist@, @val@);\n" 
				+ "\t}\n" + "\tpublic " + engine.getPath().runtimePkg() + ".Rule getCopy(){\n"
				+ "return new ";
		part4 = "();\n" + "}\n";
	}

	private String part1;
	private String part2;
	private String part3;
	private String part4;
}
