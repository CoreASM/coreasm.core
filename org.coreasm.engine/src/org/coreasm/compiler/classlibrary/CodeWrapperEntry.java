package org.coreasm.compiler.classlibrary;

import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.codefragment.CodeFragmentException;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.compiler.exception.EntryAlreadyExistsException;
import org.coreasm.compiler.exception.LibraryEntryException;

/**
 * Represents a code encapsulating class.
 * CodeWrappers can be generated to avoid large code blocks, which can exceed the java byte limit for methods.
 * @author Spellmaker
 *
 */
public class CodeWrapperEntry extends MemoryInclude {
	private static int count = 0;
	private CodeFragment body;
	private String name;
	private String responsible;
	
	private CodeWrapperEntry(CodeFragment body, String responsible, CompilerEngine engine){
		super(engine, "codewrapper_" + count, "Kernel", LibraryEntryType.STATIC);
		this.body = body;
		this.name = "codewrapper_" + count;
		count++;
		this.responsible = responsible;
	}
	
	/**
	 * Builds a new code wrapper.
	 * The code wrapper is inserted into the class library and a new {@link CodeFragment} is returned.
	 * The code wrapper will share the environment of the calling code and will therefore modify
	 * the same evalStack
	 * @param body The body of the code wrapper
	 * @param responsible A string denoting the class responsible for the creation of this wrapper for debugging
	 * @return A code fragment which executes the code wrapper
	 * @throws CompilerException If an error occured during the creation
	 */
	public static CodeFragment buildWrapper(CodeFragment body, String responsible, CompilerEngine engine) throws CompilerException{
		CodeWrapperEntry repl = new CodeWrapperEntry(body, responsible, engine);
		try{
			engine.getClassLibrary().addEntry(repl);
		}
		catch(EntryAlreadyExistsException e){
			throw new CompilerException(e);
		}
		
		String name = engine.getPath().pluginStaticPkg() + ".Kernel." + repl.name;
		CodeFragment coderes = new CodeFragment("");
		coderes.appendLine("@decl(" + name + ", tmp) = new " + name + "(evalStack, localStack, ruleparams, getUpdateResponsible());\n");
		coderes.appendLine("@tmp@.eval();\n");
		
		return coderes;
	}

	@Override
	protected String buildContent(String entryName) throws LibraryEntryException {
		String result = "";
		result += "package " + getPackage(entryName) + ";\n";
		result += "public class " + name + "{\n";
		result += "\t String responsible =\"" + responsible + "\";\n";
		result += "private " + runtimePkg() + ".EvalStack evalStack;\n";
		result += "private " + runtimePkg() + ".LocalStack localStack;\n";
		result += "private java.util.Map<String, " + runtimePkg() + ".RuleParam> ruleparams;\n";
		result += "private " + runtimePkg() + ".Rule updateResp;\n";
		result += "public " + name + "(" + runtimePkg() + ".EvalStack evalStack, " + runtimePkg() + ".LocalStack localStack, java.util.Map<String, " + runtimePkg() + ".RuleParam> ruleparams, " + runtimePkg() + ".Rule updateResp){\n";
		result += "this.evalStack = evalStack;\n";
		result += "this.localStack = localStack;\n";
		result += "this.ruleparams = ruleparams;\n";
		result += "this.updateResp = updateResp;\n";
		result += "}\n";
		result += "public " + runtimePkg() + ".Rule getUpdateResponsible(){\n";
		result += "return this.updateResp;\n";
		result += "}\n";
		result += "public void eval() throws Exception{\n";
		result += "//start of generated content\n";
		try{
			result += body.generateCode(engine);
		}
		catch(CodeFragmentException e){
			throw new LibraryEntryException(e);
		}
		result += "//end of generated content\n";
		result += "}\n";		
		result += "}\n";
		
		return result;
	}

}
