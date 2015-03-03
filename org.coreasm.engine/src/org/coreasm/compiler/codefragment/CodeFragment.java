package org.coreasm.compiler.codefragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.variablemanager.CompilerVariable;
import org.coreasm.compiler.variablemanager.VarManager;
import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.compiler.codefragment.CodeFragmentException;

/**
 * Wraps code creation for easier use.
 * A CodeFragment provides macros for variable creation and use without the
 * need to break a string.
 * <p>
 * The CodeFragment currently does not make use of the variable managers capability of
 * several visibility layers. As a consequence, the generated code can consist of pretty
 * high variable numbers (which is not actually relevant).
 * <p>
 * The following macros are currently supported:
 * <ul>
 * <li>{@literal @}{@literal @} to leave space in the fragment, which has to be filled later
 * <li>{@literal @}decl(type, name) prints and declares a variable of type type with the temporary name name
 * <li>{@literal @}name{@literal @} prints the final name of the variable with the temporary name name
 * </ul>
 * <p>
 * An usage example would be:
 * <p><blockquote><pre>
 * for({@literal @}decl(int,i)=0;{@literal @}i{@literal @} < 10; {@literal @}i{@literal @}++){
 * 	System.out.println({@literal @}i{@literal @});
 * }
 * </pre></blockquote>
 * <p>
 * will translate into:
 * <p><blockquote><pre>
 * for(var_int_0=0;var_int_0 < 10; var_int_0++){
 * 	System.out.println(var_int_0);
 * }
 * </pre></blockquote>
 * <p>
 * The temporary names have their own visibility. A temporary name is visible only in its own
 * CodeFragment object. Child CodeFragments (attached via appendFragment) won't see them
 * and can declare the same name again.
 * Additional lines of code added via appendLine can see the temporary name.
 * 
 * @author Markus Brenner
 *
 */
public class CodeFragment {
	private List<String> codeList;
	private Map<Integer, CodeFragment> children;
	private String generationInfo;
	private static int errorSnippet = 100; //how long the extracted code for an error text is
	private int sizeInBytes = 0;
	private int id;
	private static int count = 0;
	
	/**
	 * Creates a new CodeFragment with the given code.
	 * @param s A piece of code
	 */
	public CodeFragment(String s){
		this.id = count;
		count++;
		
		
		codeList = new ArrayList<String>();
		children = new HashMap<Integer, CodeFragment>();
		
		appendLine(s);
	}
	
	/**
	 * Creates a new CodeFragment with the given code and the given info string.
	 * The info string will be used to print a comment at the beginning and at the end of
	 * the CodeFragment containing the info string.
	 * @param s A piece of code
	 * @param info An info string
	 */
	public CodeFragment(String s, String info){
		this.id = count;
		count++;
		
		codeList = new ArrayList<String>();
		children = new HashMap<Integer, CodeFragment>();
		
		appendLine(s);
		this.generationInfo = info;
		sizeInBytes = this.generationInfo.getBytes().length + 10;
	}
	
	/**
	 * Creates an empty CodeFragment
	 */
	public CodeFragment(){
		this.id = count;
		count++;
		
		codeList = new ArrayList<String>();
		children = new HashMap<Integer, CodeFragment>();
		
		appendLine("");
	}
	
	private int tryFill(int count, CodeFragment code){
		for(int i = 0; i < codeList.size(); i++){
			if(children.get(i) == null){
				count--;
				if(count == -1){
					children.put(i, code);
					return -1;
				}
			}
			else{
				count = children.get(i).tryFill(count, code);
				if(count < 0) return count;
			}
		}
		return count;
	}
	
	/**
	 * Tries to fill the gap with the number count.
	 * The gaps are numbered as if all code of this and child fragments was
	 * already generated.
	 * @param count The index of the gap
	 * @param code The code for the gap
	 * @exception CodeFragmentException If there is no gap with the given index
	 */
	public void fillSpace(int count, CodeFragment code) throws CodeFragmentException{
		if(tryFill(count, code) != -1)
			throw new CodeFragmentException("CodeFragment has no gap with number " + count);
	}
	
	/**
	 * Appends a line of code.
	 * All declared temporary variable identifiers are valid in this line.
	 * @param s A line of code
	 */
	public void appendLine(String s) {
		String[] tmp = s.split("@@");
		
		if(codeList.size() >= 1){
			codeList.set(codeList.size() - 1, codeList.get(codeList.size() - 1) + tmp[0]);
		}
		else{
			codeList.add(tmp[0]);
		}
		
		for(int i = 1; i < tmp.length; i++){
			codeList.add(tmp[i]);
			children.put(codeList.size() - 1, null);
		}
		
		sizeInBytes += s.getBytes().length;
	}

	/**
	 * Appends a fragment as a child of this code.
	 * All new code lines will be appended after this fragment. Variable identifiers
	 * of the father will not be viable in this child fragment.
	 * @param code A CodeFragment
	 */
	public void appendFragment(CodeFragment code){		
		children.put(codeList.size() - 1, code);
		codeList.add("");
	}

	private String buildErrorString(String[] tmp, int pos){
		String errorstring = tmp[pos];
		int errorlength = errorSnippet;
		for(int k = pos; (k < tmp.length) && (errorstring.length() < errorlength); k++){
			errorstring = errorstring + tmp[k];
		}						
		if(errorstring.length() > errorlength){
			errorstring = errorstring.substring(0, errorlength);
		}
		return errorstring;
	}
	
	/**
	 * Prints the code of this CodeFragment.
	 * The process will replace all macros, generating actual variable names.
	 * @return The generated string of this CodeFragment
	 * @throws CodeFragmentException If a CodeFragment was incomplete or incorrect.
	 */
	public String generateCode(CompilerEngine engine) throws CodeFragmentException{
		List<Integer> ids = new ArrayList<Integer>();
		
		
		String result = genCode(ids, engine);
		
		if(result.contains("@")){
			String warn = result.substring(result.indexOf("@"));
			if(warn.length() > errorSnippet){
				warn = warn.substring(0, errorSnippet);
			}
			String text = "Warning: '@' symbol detected in generated code near '" + warn + "' - maybe a java annotation?";
			engine.getLogger().warn(CodeFragment.class, text);
			engine.addWarning(text);
		}
		
		return result;
	}
	
	//internal function for code generation. Introduced to decide recursive calls
	//to child fragments from the initial generate call
	private String genCode(List<Integer> ids, CompilerEngine engine) throws CodeFragmentException{
		List<String> names = new ArrayList<String>();
		
		VarManager vman = engine.getVarManager();
		
		StringBuilder result = new StringBuilder();
		
		List<String> myCodeList = new ArrayList<String>();
		myCodeList.addAll(codeList);
		
		
		for(int i = 0; i < myCodeList.size(); i++){
			//replace declarations in own namespace
			String[] tmp = myCodeList.get(i).split("@decl");
			result.append(tmp[0]);
			for(int j = 1; j < tmp.length; j++){
				String declaration,type,name;
				try{
					//extract the variable declaration
					declaration = tmp[j].substring(0, tmp[j].indexOf(")"));
					type = declaration.substring(1, declaration.lastIndexOf(",")).trim();
					name = declaration.substring(declaration.lastIndexOf(",") + 1).trim();
				}
				catch(IndexOutOfBoundsException e){
					//catch errors with incorrect declarations
					throw new CodeFragmentException("incorrect @decl near '@decl" + buildErrorString(tmp, j) + "'");
				}
				if(names.contains(name)){
					throw new CodeFragmentException("trying to redeclare the variable '" + name + "' near '" + buildErrorString(tmp, j) + "'");
				}
				//add the variable to the list of already declared names
				names.add(name);  
				
				CompilerVariable cv = vman.createVariable(type);
				//replace all occurences in the same codeList entry
				for(int k = j; k < tmp.length; k++){						
					tmp[k] = tmp[k].replaceAll("@" + name + "@", cv.toString());
				}
				//replace all occurences in the following codeList entries
				for(int k = i + 1; k < myCodeList.size(); k++){
					myCodeList.set(k, myCodeList.get(k).replaceAll("@" + name + "@", cv.toString()));
				}
				
				result.append(cv.declare());
				result.append(tmp[j].substring(tmp[j].indexOf(")") + 1));
			}
			if(children.get(i) == null){
				if(i == codeList.size() - 1){
					continue;
				}
				else{
					String error = codeList.get(i);
					if(i != codeList.size() - 1){
						error += "@@" + codeList.get(i + 1);
					}
					
					if(error.length() > errorSnippet){
						error = error.substring(0, errorSnippet);
					}
					
					
					throw new CodeFragmentException("Code Fragment has an open gap near '" + error + "'");
				}
			}
			if(generationInfo != null){
				result.insert(0, "//-----------------" + generationInfo + "-----------------------\n");
			}
			result.append(children.get(i).genCode(ids, engine));
			if(generationInfo != null){
				result.append("//--------------------------------------------------------------\n");
			}
		}
		return result.toString();
	}
	
	@Override
	public String toString(){
		StringBuilder result = new StringBuilder();
		for(int i = 0; i < codeList.size(); i++){
			result.append(codeList.get(i));
			if(children.get(i) == null){
				if(i == codeList.size() - 1) continue;
				result.append("\n[NULL]\n");
			}
			else{
				result.append(children.get(i).toString());
			}
		}
		return result.toString();
	}

	/**
	 * Approximates the size of the code represented by this object
	 * @return The size of the code in bytes
	 */
	public int getByteCount(){
		int result = sizeInBytes;
		for(CodeFragment c : children.values()){
			result = result + c.getByteCount();
		}
		return result;
	}
	
	/**
	 * Returns the id of the codefragment.
	 * Used for debugging purposes. The id of a specific code fragment
	 * should stay the same over different compiler runs with the same input,
	 * as the code generation is deterministic.
	 * The debugger can than stop upon the creation of a specific code fragment.
	 * @return The id of the code fragment
	 */
	public int getId(){
		return this.id;
	}
}
