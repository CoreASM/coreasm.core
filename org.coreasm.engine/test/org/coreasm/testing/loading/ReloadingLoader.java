package org.coreasm.testing.loading;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReloadingLoader extends ClassLoader {
	private List<String> classNames;
	private Map<String, Class<?>> loadedClasses;
	private String classDir;
	
	public ReloadingLoader(ClassLoader parent, List<String> className, String classDir){ 
		super(parent);
		
		this.classNames = className;
		this.classDir = classDir;
		this.loadedClasses = new HashMap<String, Class<?>>();
	}
	
	public Class<?> loadClass(String name) throws ClassNotFoundException{
		if(!classNames.contains(name)){
			return super.loadClass(name);
		}
		
		Class<?> lC = loadedClasses.get(name);
		if(lC != null) return lC;
		
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
			
			Class<?> cl = defineClass(name, classData, 0, classData.length);
			loadedClasses.put(name, cl);
			return cl;
		} catch (IOException e) {
			e.printStackTrace();
			throw new ClassNotFoundException();
		}
	}
}
