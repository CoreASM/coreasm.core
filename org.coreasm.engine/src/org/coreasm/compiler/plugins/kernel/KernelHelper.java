package org.coreasm.compiler.plugins.kernel;

/**
 * Helps the kernel to build rule code
 * @author Markus Brenner
 *
 */
public class KernelHelper {
	/**
	 * Returns the initial part of the rule code
	 * @param constructor The name of the rule
	 * @return The initial part of a rule class
	 */
	public static String getFirstRulePart(String constructor){
		return "\t//Rule parameters\n" + 
				"\tprivate java.util.ArrayList<CompilerRuntime.RuleParam> params;\n" +
				"\t//local Stack for rule local parameters\n" +
				"\tprivate CompilerRuntime.LocalStack localStack;\n" +
				"\t//local evaluation stack;\n" +
				"\tprivate CompilerRuntime.EvalStack evalStack;\n" +
				"\t\n" +
				"\tpublic " + constructor + "(java.util.ArrayList<CompilerRuntime.RuleParam> params, CompilerRuntime.LocalStack ls){\n" +
				"\t\t//RuleParam is immutable, shallow copy is enough\n" +
				"\t\tthis.params = new java.util.ArrayList<>(params);\n" +
				"\t\t\n" +
				"\t\tif(ls == null){\n" +
				"\t\t\t//the rule is called / created by the scheduler\n" +
				"\t\t\tlocalStack = new CompilerRuntime.LocalStack();\n" +
				"\t\t}\n" +
				"\t\telse{\n" +
				"\t\t\t//the rule is called by another rule\n" +
				"\t\t\tlocalStack = ls;\n" +
				"\t\t}\n" +
				"\t\t\n" +
				"\t\tevalStack = new CompilerRuntime.EvalStack();\n" +
				"\t}\n" +
				"\t\n" +
				"\t@Override\n" +
				"\tpublic CompilerRuntime.UpdateList call(){\n" +
				"\t\tlocalStack.pushLayer();\n" + 
				"\t\tevalStack.push(new CompilerRuntime.UpdateList());";
	}
	/**
	 * Returns the second part of the rule class code
	 * @return The second part of the rule class code
	 */
	public static String getSecondRulePart(){
		return 	"\t\t\n" +
				"\t\tlocalStack.popLayer(); 		\n" +
				"\t\treturn (CompilerRuntime.UpdateList) evalStack.pop();\n" +
				"\t}\n";
	}
	
}
