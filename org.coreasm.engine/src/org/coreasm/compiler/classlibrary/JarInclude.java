package org.coreasm.compiler.classlibrary;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.exception.LibraryEntryException;

/**
 * Represents an import from a jar archive.
 * A jar include effectively copies an element from
 * an jar archive into the compilation unit.
 * The include will modify the package declaration on it's own
 * and can be used to modify import statements via package replacements.
 * A Package replacement consists of a source and a target string.
 * The source string can be a complete declaration (such as "java.util.ArrayList")
 * or only the start of a declaration ("java.util.") and will replace the corresponding
 * parts of all matching import statements.
 * @author Spellmaker
 *
 */
public class JarInclude extends LibraryEntry {
	private File sourceJar;
	private String sourceFile;
	private String sourcePlugin;
	private LibraryEntryType type;
	private CompilerEngine engine;
	private Map<String, String> packageReplacements;
	//reading related
	private JarFile jar;
	private JarEntry jarentry;
	private BufferedReader reader;
	private String finalName;
	private boolean packageFound;
	
	/**
	 * Creates a new jar include
	 * @param engine The compiler engine supervising the compilation process
	 * @param sourceJar The source archive
	 * @param sourceFile The source file in the archive ('/' is used as the separator)
	 * @param plugin The name of the source plugin
	 * @param type The type of the include
	 */
	public JarInclude(CompilerEngine engine, File sourceJar, String sourceFile, String plugin, LibraryEntryType type){
		this.sourceJar = sourceJar;
		this.sourceFile = sourceFile;
		this.type = type;
		this.sourcePlugin = plugin;
		this.engine = engine;
		this.packageReplacements = new HashMap<String, String>();
	}
	
	/**
	 * Adds a package replacement to this include
	 * @param original The original package declaration string
	 * @param replacement The replacement for the declaration
	 */
	public void addPackageReplacement(String original, String replacement){
		this.packageReplacements.put(original, replacement);
	}
	
	@Override
	public String getName() {
		String tFile = sourceFile.substring(Math.max(0, sourceFile.lastIndexOf("/") + 1));
		int pos = tFile.lastIndexOf(".");
		if(pos >= 0){
			tFile = tFile.substring(0, pos);
		}
		return tFile;
	}

	@Override
	public String getSource() {
		return sourcePlugin;
	}

	@Override
	public LibraryEntryType getType() {
		return type;
	}

	@Override
	public void open(String entryName) throws Exception{
		this.finalName = entryName;
		this.packageFound = false;
		try{
			jar = new JarFile(sourceJar);
			jarentry = jar.getJarEntry(sourceFile);
			if(jarentry == null){
				throw new IOException("jar entry " + sourceFile + " not found");
			}
	
			reader = new BufferedReader(new InputStreamReader(jar.getInputStream(jarentry)));
		}
		catch(IOException e){
			engine.addError("IO Error while processing include " + this.toString() + ": " + e.getMessage());
			engine.getLogger().error(JarInclude.class, "Error while copying file");
			throw new LibraryEntryException(e);
		}
	}

	@Override
	public String readLine() throws IOException{
		String s = reader.readLine();
		if(s == null) return s;
		
		if(!packageFound && s.trim().startsWith("package")){
			packageFound = true;	
			return "package " + getPackage(finalName) + ";\n";
		}
		if(s.trim().startsWith("import ")){
			String l = s.trim();
			l = l.substring(7, l.length() - 1);
			String r = packageReplacements.get(l);
			if(r != null){
				return "import " + r + ";\n";
			}
			else{
				for(String t : packageReplacements.keySet()){
					if(l.startsWith(t)){
						return "import " + packageReplacements.get(t) + l.substring(t.length()) + ";\n";
					}
				}
			}
		}
		return s + "\n";
	}
	
	@Override
	public void close() throws IOException{
		reader.close();
		jar.close();
	}
	
}
