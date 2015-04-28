package org.coreasm.testing.loading;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoadingFactory {
	private Map<TestCaseLocation, TestCaseClasses> library;
	
	public LoadingFactory(){
		library = new HashMap<TestCaseLocation, TestCaseClasses>();
	}
	
	public Class<?> loadTestClass(String directory, String mainClass) throws ClassNotFoundException{
		TestCaseLocation l = new TestCaseLocation(directory, mainClass);
		if(library.get(l) == null){
			List<String> classNames = new ArrayList<String>();
			File root = new File(directory);
			for(File f : root.listFiles(new ClassFilter())){
				classNames.add(f.getName().replace(".class", ""));
			}

			List<Class<?>> loadedClasses = new ArrayList<Class<?>>();
			ReloadingLoader loader = new ReloadingLoader(LoadingFactory.class.getClassLoader(), classNames, directory);
			Class<?> mClass = null;
			for(String s : classNames){
				loadedClasses.add(loader.loadClass(s));
				if(s.equals(mainClass)){
					mClass = loadedClasses.get(loadedClasses.size() - 1);
				}
			}
			
			library.put(l, new TestCaseClasses(loader, loadedClasses, mClass));
			return mClass;
		}
		else{
			//should actually be some more here, but thats for later
		}
		return null;
	}
	
	public void unload(String directory, String mainClass){
		library.remove(new TestCaseLocation(directory, mainClass));
	}
}

class TestCaseLocation{
	public final String directory;
	public final String mainClass;
	
	public TestCaseLocation(String d, String c){
		this.directory = d;
		this.mainClass = c;
	}
}

class TestCaseClasses{
	public ReloadingLoader loader;
	public List<Class<?>> classes;
	public Class<?> mainClass;
	
	public TestCaseClasses(ReloadingLoader loader, List<Class<?>> classes, Class<?> mainClass){
		this.loader = loader;
		this.classes = classes;
		this.mainClass = mainClass;
	}
}

class ClassFilter implements FileFilter{
	@Override
	public boolean accept(File pathname) {
		return pathname.getName().endsWith(".class");
	}
}