package org.coreasm.compiler.classlibrary;

import java.io.File;

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
public class CodeWrapperEntry extends AbstractLibraryEntry {
	private static int count = 0;
	private CodeFragment body;
	private String name;
	private String responsible;
	private CompilerEngine engine;
	
	private CodeWrapperEntry(CodeFragment body, String responsible, CompilerEngine engine){
		this.body = body;
		this.name = "codewrapper_" + count;
		count++;
		this.responsible = responsible;
		this.engine = engine;
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
		LibraryEntry repl = new CodeWrapperEntry(body, responsible, engine);
		try{
			engine.getClassLibrary().addEntry(repl);
		}
		catch(EntryAlreadyExistsException e){
			throw new CompilerException(e);
		}
		
		String name = repl.getFullName();
		CodeFragment coderes = new CodeFragment("");
		coderes.appendLine("@decl(" + name + ", tmp) = new " + name + "(evalStack, localStack, ruleparams, getUpdateResponsible());\n");
		coderes.appendLine("@tmp@.eval();\n");
		
		return coderes;
	}
	
	
	@Override
	public String getFullName() {
		return "plugins.Kernel." + name;
	}

	@Override
	protected File getFile() {
		return new File(engine.getOptions().tempDirectory + "\\plugins\\Kernel\\" + name + ".java");
	}

	@Override
	protected String generateContent() throws LibraryEntryException {
		String result = "";
		result += "package plugins.Kernel;\n";
		result += "public class " + name + "{\n";
		result += "\t String responsible =\"" + responsible + "\";\n";
		result += "private CompilerRuntime.EvalStack evalStack;\n";
		result += "private CompilerRuntime.LocalStack localStack;\n";
		result += "private java.util.Map<String, CompilerRuntime.RuleParam> ruleparams;\n";
		result += "private CompilerRuntime.Rule updateResp;\n";
		result += "public " + name + "(CompilerRuntime.EvalStack evalStack, CompilerRuntime.LocalStack localStack, java.util.Map<String, CompilerRuntime.RuleParam> ruleparams, CompilerRuntime.Rule updateResp){\n";
		result += "this.evalStack = evalStack;\n";
		result += "this.localStack = localStack;\n";
		result += "this.ruleparams = ruleparams;\n";
		result += "this.updateResp = updateResp;\n";
		result += "}\n";
		result += "public CompilerRuntime.Rule getUpdateResponsible(){\n";
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
