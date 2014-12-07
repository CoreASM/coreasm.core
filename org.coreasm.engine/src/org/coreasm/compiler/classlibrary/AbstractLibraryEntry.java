package org.coreasm.compiler.classlibrary;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.coreasm.compiler.classlibrary.LibraryEntry;
import org.coreasm.compiler.exception.LibraryEntryException;

/**
 * Abstract implementation of the LibraryEntry interface.
 * Takes care of writing the file contents to the disk. The user
 * only needs to provide the target file object and the contents
 * of the file.
 * @author Markus Brenner
 *
 */
public abstract class AbstractLibraryEntry implements LibraryEntry {
	/**
	 * Returns a new File Object representing the target path.
	 * This method is called once by the writeFile method to determine
	 * the target of the write operation and to create directories.
	 * @return The target file of the write operation
	 */
	protected abstract File getFile();
	/**
	 * Generates the contents of the file.
	 * Is called once by the writeFile method to create the content
	 * of the class file.
	 * @return The contents of the file
	 * @throws LibraryEntryException If an error occurred while creating the content
	 */
	protected abstract String generateContent() throws LibraryEntryException;
	
	@Override
	public void writeFile() throws LibraryEntryException {
		File file = getFile();
		File directory = file.getParentFile();
		
		if(file.exists()){
			throw new LibraryEntryException(new Exception("file " + file.getPath() + " already exists"));
		}
		
		BufferedWriter bw = null;
		
		directory.mkdirs();

		try {
			file.createNewFile();
		
			bw = new BufferedWriter(new FileWriter(file));
		
			bw.write(this.generateContent());
		} 
		catch (IOException e) {
			throw new LibraryEntryException(e);
		} 
		finally{
			try{
				bw.close();
			}
			catch(IOException e){
			}
		}
	}

}
