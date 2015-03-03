package org.coreasm.compiler.classlibrary;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.CompilerOptions;
import org.coreasm.compiler.classlibrary.ClassInclude;
import org.coreasm.compiler.classlibrary.LibraryEntry;
import org.coreasm.compiler.exception.LibraryEntryException;

/**
 * Represents an included class already existing somewhere on the disk.
 * The source file can either be in a jar archive or be directly accessible.
 * The class include will adapt the package declaration in the source file
 * during the copying process and is also capable of changing specific import
 * directives.
 * Note that the replacing process might produce unexpected result when importing
 * non-java files containing the keywords "import" and "package".
 * As the copy method performs additional operations, this class does not
 * extend AbstractLibraryEntry.
 * 
 * @author Markus Brenner
 *
 */
public class ClassInclude implements LibraryEntry{
	
	private File jarArchive;
	//private JarFile jarArchive;
	private String className;
	private File originalPath;
	private File targetDirectory;
	private File targetFile;
	private String packageName;
	private Map<String, String> importReplacements;
	private Map<String, String> importPackageReplacements;
	private CompilerEngine engine;
	
	/**
	 * The base path to the plugins. Used to make the merge with the coreasm project easier.
	 */
	public static final String PLUGIN_BASE = "src\\de\\spellmaker\\coreasmc\\plugins\\dummy\\".replace("\\", File.separator);
	
	/**
	 * Creates a new ClassInclude object referencing a file on the hard disk.
	 * @param path The path of the source file
	 * @param packageName The target package name of the include
	 */
	public ClassInclude(File path, String packageName, CompilerEngine engine){
		this.engine = engine;
		if (!path.exists()) {
			engine.addWarning("File " + path.getAbsolutePath() + " does not exist");
			engine.getLogger().warn(ClassInclude.class, "Error while constructing ClassInclude");
		}
		
		jarArchive = null;
		
		originalPath = path;
		
		CompilerOptions options = engine.getOptions();
		
		targetDirectory = new File(options.tempDirectory + File.separator + packageName.replace(".", File.separator));
		
		String tFile = originalPath.getName();
		if(!tFile.endsWith(".java")){
			tFile += ".java";
		}
		targetFile = new File(targetDirectory, tFile);
		
		className = originalPath.getName();
		
		this.packageName = packageName;
		importReplacements = new HashMap<String, String>();
		
		populatePackageReplacements();
	}
	
	/**
	 * Creates a new ClassInclude, referencing a file within a jar archive.
	 * @param jarPath The path to the jar archive containing the file
	 * @param className The path of the class in the jar archive
	 * @param packageName The target package name
	 * @throws IOException If the jar archive could not be accessed
	 */
	public ClassInclude(File jarPath, String className, String packageName, CompilerEngine engine) throws IOException{
		this.engine = engine;
		jarArchive = jarPath;
		//jarArchive = new JarFile(jarPath);
		this.className = className;
		originalPath = null;
		CompilerOptions options = engine.getOptions();
		targetDirectory = new File(options.tempDirectory + File.separator + packageName.replace(".", File.separator));
		
		String tFile = className.substring(Math.max(0, className.lastIndexOf("/")));
		if(!tFile.endsWith(".java")){
			tFile += ".java";
		}
		targetFile = new File(targetDirectory, tFile);
		this.packageName = packageName;
		importReplacements = new HashMap<String, String>();
		populatePackageReplacements();
	}
	
	private void populatePackageReplacements(){
		importPackageReplacements = new HashMap<String, String>();
		
		importPackageReplacements.put("org.coreasm.engine.absstorage", "CompilerRuntime");
		importPackageReplacements.put("org.coreasm.engine.CoreASMError", "CompilerRuntime.CoreASMError");
	}
	
	@Override
	public void writeFile() throws LibraryEntryException{
		targetDirectory.mkdirs();
		
		BufferedReader br = null;
		BufferedWriter bw = null;
		JarFile jarjar = null;
		
		try{
			if(jarArchive == null){
				br = new BufferedReader(new FileReader(originalPath));
				bw = new BufferedWriter(new FileWriter(targetFile));
			}
			else{				
				jarjar = new JarFile(jarArchive);
				JarEntry je = jarjar.getJarEntry(className);
				if(je == null){
					throw new IOException("jar entry " + className + " not found");
				}
	
				br = new BufferedReader(new InputStreamReader(jarjar.getInputStream(je)));
				bw = new BufferedWriter(new FileWriter(targetFile));
			}
			copyContent(br, bw);	
		}
		catch(IOException e){
			engine.addError("IO Error while processing include " + this.getFullName() + ": " + e.getMessage());
			engine.getLogger().error(ClassInclude.class, "Error while copying file");
			throw new LibraryEntryException(e);
		}
		finally{
			try{
				if (br != null) br.close();
				if (bw != null) bw.close();
				if (jarjar != null) jarjar.close();
			}
			catch(Exception e){
				//Note: Print Stack Trace is in here, as I don't really know when this error
				//could ever occur
				e.printStackTrace();
				engine.getLogger().error(ClassInclude.class, "Could not close in and out streams");
			}
		}
	}
	
	/**
	 * Adds an replacement for an import.
	 * The ClassInclude will replace the import declaration for original
	 * with replacement during the copy process.
	 * @param original The original import (not including the "import" and ";" symbols)
	 * @param replacement The target import
	 */
	public void addImportReplacement(String original, String replacement){
		this.importReplacements.put(original, replacement);
	}
	
	private void copyContent(BufferedReader br, BufferedWriter bw) throws IOException{
		boolean packageFound = false; 
		if(!packageName.equals("")) bw.write("package " + packageName + ";\n");

		String tmp = null;
		while((tmp = br.readLine()) != null){
			if(!packageFound && tmp.trim().startsWith("package")){
				packageFound = true;	
				continue;
			}
			if(tmp.trim().startsWith("import ")){
				String l = tmp.trim();
				l = l.substring(7, l.length() - 1);
				String r = importReplacements.get(l);
				if(r != null){
					tmp = "import " + r + ";";
				}
				else{
					for(String s : importPackageReplacements.keySet()){
						if(l.startsWith(s)){
							tmp = "import " + importPackageReplacements.get(s) + l.substring(s.length()) + ";";
						}
					}
				}
			}
			bw.write(tmp + "\n");
		}
		
		bw.flush();
		bw.close();
		br.close();
	}
	
	@Override
	public String getFullName(){
		String result = "";
		if(packageName.equals("")){
			result = "";
		}
		else{
			result = packageName + ".";
		}
		
		if(targetFile.getName().lastIndexOf(".") >= 0){
			result += targetFile.getName().substring(0, targetFile.getName().lastIndexOf("."));
		}
		else{

			result += targetFile.getName();
		}
		
		return result;
	}
}
