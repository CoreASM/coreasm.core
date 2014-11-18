package org.coreasm.compiler;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import org.coreasm.compiler.exception.CompilerException;


/**
 * Wrapper class providing a method to pack compiled class in a jar archive.
 * Code is heavily based on http://stackoverflow.com/questions/1281229/how-to-use-jaroutputstream-to-create-a-jar-file
 * @author Markus Brenner
 *
 */
public class JarPacker {	
	/**
	 * Packs the contents of the temporary directory as an executable jar archive.
	 * Assumes that all needed code files reside in the temporary directory or
	 * sub-directories of it and that the main entry point is the file Main.java in
	 * the root of the temporary directory
	 * @param options Options for the compilation process
	 * @throws CompilerException If the jar archive could not be packed
	 */
	public static void packJar(CompilerOptions options) throws CompilerException{
		JarOutputStream target = null;
		try{
			Manifest manifest = new Manifest();
			manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
			manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, "Main");
			target = new JarOutputStream(new FileOutputStream(options.outputFile), manifest);
			File root = new File(options.tempDirectory);
			for(File f : root.listFiles()){
				addFile(f, target, options);
			}
		}
		catch(Exception e){
			CoreASMCompiler.getEngine().addError("Could not pack jar: " + e.getMessage());
			throw new CompilerException(e);
		}
		finally{
			if(target != null)
				try {
					target.close();
				} catch (IOException e) {
					CoreASMCompiler.getEngine().getLogger().error(JarPacker.class, "Could not close jar stream");
				}
		}
	}
	
	private static void addFile(File source, JarOutputStream target, CompilerOptions options) throws IOException{
		BufferedInputStream in = null;
		try{
			if (source.isDirectory()){
				String name = source.getPath().replace(options.tempDirectory + "\\", "").replace("\\", "/");
		    	if (!name.isEmpty()){
		    		if (!name.endsWith("/")) name += "/";
	    			JarEntry entry = new JarEntry(name);
	    			entry.setTime(source.lastModified());
	    			target.putNextEntry(entry);
	    			target.closeEntry();
	    		}
		    	
	    		for (File nestedFile: source.listFiles()){
	    			addFile(nestedFile, target, options);
	    		}
		    }
			else if(!source.getName().endsWith(".java")){
			    JarEntry entry = new JarEntry(source.getPath().replace(options.tempDirectory + "\\", "").replace("\\", "/"));
			    entry.setTime(source.lastModified());
			    target.putNextEntry(entry);
			    in = new BufferedInputStream(new FileInputStream(source));
	
			    byte[] buffer = new byte[1024];
			    while (true){
			    	int count = in.read(buffer);
			    	if (count == -1) break;
			    	
			    	target.write(buffer, 0, count);
			    }
			    target.closeEntry();
			}
		}
		finally{
			if (in != null) in.close();
		}
	}
}
