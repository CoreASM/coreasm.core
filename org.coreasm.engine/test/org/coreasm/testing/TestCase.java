package org.coreasm.testing;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.coreasm.compiler.CodeType;
import org.coreasm.engine.plugin.Plugin;
import org.coreasm.testing.value.LocalProvider;
import org.coreasm.testing.value.ParameterProvider;
import org.coreasm.testing.value.StorageProvider;

public class TestCase {
	public String testName;
	public Plugin testPlugin;
	public CodeType codeType;
	public File specFile;
	public String spec;
	public Map<String, ParameterProvider> parameters;
	public List<StorageProvider> storageValues;
	public List<LocalProvider> localValues;
	//TODO: insert result definitions here
	public ParameterProvider nodeResult;
	
	public TestCase(){
		parameters = new HashMap<String, ParameterProvider>();
		storageValues = new ArrayList<StorageProvider>();
		localValues = new ArrayList<LocalProvider>();
	}
}
