package org.coreasm.testing.loading;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

public class ReloadingLoader extends ClassLoader {
	private List<String> classNames;
	private String classDir;
	
	public ReloadingLoader(ClassLoader parent, List<String> className, String classDir){ 
		super(parent);
		
		this.classNames = className;
		this.classDir = classDir;
	}
	
	public Class<?> loadClass(String name) throws ClassNotFoundException{
		if(!classNames.contains(name)){
			return super.loadClass(name);
		}
		
		DataInputStream in;
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			in = new DataInputStream(new FileInputStream(classDir +  name + ".class"));
			
			while(true){
				try{
					out.write(in.readByte());
				}
				catch(EOFException eof){
					break;
				}
			}
			
			in.close();
			byte[] classData = out.toByteArray();			
			return defineClass(name, classData, 0, classData.length);
		} catch (IOException e) {
			e.printStackTrace();
			throw new ClassNotFoundException();
		}
	}
}
