package org.coreasm.compiler.components.backend;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import org.coreasm.compiler.CompilerEngine;
import org.coreasm.compiler.components.classlibrary.LibraryEntry;
import org.coreasm.compiler.exception.CompilerException;
import org.coreasm.compiler.paths.CompilerPathConfig;

/**
 * Default compiler backend.
 * Implements the backend interfaces {@link CompilerFileWriter} and {@link CompilerPacker} to
 * provide the standard implementation of the CoreASMC backend.
 * Executing the backend in order will simply dump all files in the class library to the temporary
 * directory and later on pack them into an executable jar archive, assuming Main.java in the
 * root of the directory is the main entry point of the program.
 * @author Spellmaker
 *
 */
public class KernelBackend implements CompilerFileWriter, CompilerPacker {
	@Override
	public boolean packFiles(List<File> files, CompilerEngine engine) {
		try{
			JarPacker.packJar(engine.getOptions(), engine);
		}
		catch(CompilerException e){
			return false;
		}
		return true;
	}

	@Override
	public List<File> writeEntriesToDisk(List<LibraryEntry> entries,
			CompilerEngine engine) throws CompilerException{
		
		List<File> result = new ArrayList<File>();
		CompilerPathConfig path = engine.getPath();
		
		LibraryEntry current = null;
		for(int i = 0; i < entries.size(); i++){
			File f = null;
			try{
				current = entries.get(i);
				String entryName = path.getEntryName(current);		
				f = new File(engine.getOptions().tempDirectory.getAbsolutePath() + "\\" + path.getEntryPath(current));
				//make parent directory
				f.getParentFile().mkdirs();
				BufferedWriter bw = new BufferedWriter(new FileWriter(f));
				current.open(entryName);
				String s = "";
				while((s = current.readLine()) != null){
					bw.write(s + "\n");
				}
				bw.close();
				current.close();
				result.add(f);
			}
			catch(Exception e){
				String msg = "error writing entry '" +  f + "': '" + e.getMessage() + "'" + engine.getOptions().enginePath.getAbsolutePath();
				engine.addError(msg);
				throw new CompilerException(msg);
			}
		}
		
		
		return result;
	}
}
