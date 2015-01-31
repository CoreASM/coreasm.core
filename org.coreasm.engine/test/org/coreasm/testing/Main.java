package org.coreasm.testing;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;

import org.coreasm.compiler.CodeType;
import org.coreasm.compiler.codefragment.CodeFragment;
import org.coreasm.engine.interpreter.ASTNode;
import org.coreasm.engine.plugins.conditionalrule.ConditionalRulePlugin;
import org.coreasm.testing.compiling.ClassCompiler;
import org.coreasm.testing.drivers.TestCaseDriver;
import org.coreasm.testing.drivers.TestCaseResult;
import org.coreasm.testing.loading.LoadingFactory;
import org.coreasm.testing.modules.BooleanTestingModule;
import org.coreasm.testing.value.BooleanProvider;
import org.coreasm.testing.value.ElementProvider;
import org.coreasm.testing.value.LocationProvider;
import org.coreasm.testing.value.ParameterProvider;
import org.coreasm.testing.value.UpdateProvider;

public class Main {
	private static int nodecount;
	public static void main(String[] args) throws Exception{
		TestCaseDriver driver = new TestCaseDriver();
		driver.init();
		TestCase test = new TestCase();
		test.testName = "ConditionalTest";
		test.testPlugin = new ConditionalRulePlugin();
		test.codeType = CodeType.U;
		test.specFile = new File(TestCaseDriver.getRootDir().getAbsolutePath() + "\\testing\\conditionaltest.coreasm");
		
		ParameterProvider trueval = new ParameterProvider(BooleanProvider.FALSE);
		ParameterProvider truerule = new ParameterProvider(new UpdateProvider(new LocationProvider("test"), BooleanProvider.TRUE, "UPDATE_ACTION"));
		ParameterProvider falserule = new ParameterProvider(new UpdateProvider(new LocationProvider("test"), BooleanProvider.FALSE, "UPDATE_ACTION"));
		
		test.parameters.put("guard", trueval);
		test.parameters.put("truerule", truerule);
		test.parameters.put("falserule", falserule);
		test.nodeResult = falserule;
		
		TestCaseResult result = driver.executeTestCase(test);
		driver.dispose();

		for(String s : result.compiler.messages){
			System.out.println(s);
		}
		if(result.compiler.error == null)
			System.out.println("compiler test succeeded");
		else{
			System.out.println("compiler test failed");
			result.compiler.error.printStackTrace();
		}
		
		for(String s : result.interpreter.messages){
			System.out.println(s);
		}
		if(result.interpreter.error == null)
			System.out.println("interpreter test succeeded");
		else{
			System.out.println("interpreter test failed");
			result.interpreter.error.printStackTrace();
		}
		
		/*TestDriver driver = new TestDriver();
		ASTNode root = driver.parseSpec("if PARAM guard then PARAM truerule else PARAM falserule");
		BufferedWriter bw =  new BufferedWriter(new FileWriter("C:\\Users\\Spellmaker\\git\\dotast\\CoreASM-DotAST\\Test.dot"));
		printDot(root, bw, 100);
		bw.close();
		driver.dispose();
		
		
		//try 1
		CompilerMock cdriver = new CompilerMock();
		
		cdriver.mockCode.put("guard", BooleanProvider.TRUE.compilerValue());
		cdriver.mockCode.put("truerule", "System.out.println(\"truerule\");\n");
		cdriver.mockCode.put("falserule", "System.out.println(\"falserule\");\n");
		
		CodeFragment cf = cdriver.compile(root, CodeType.U);
		String code = cf.generateCode();
		CodeBuilder cb = new CodeBuilder();
		cb.addModule(new BooleanTestingModule());	

		BufferedWriter output = new BufferedWriter(new FileWriter(new File("C:\\Users\\Spellmaker\\Desktop\\test\\CoreASMCTest.java")));
		output.write(cb.generateCode(code));
		output.close();
		
		ClassCompiler cc = new ClassCompiler();
		cc.addTask(new File("C:\\Users\\Spellmaker\\Desktop\\test\\CoreASMCTest.java"));
		try{
			cc.compile();
		}
		catch(Exception e){
			for(String s : cc.getErrors()) System.out.println(s);
			throw e;
		}
		
		LoadingFactory factory = new LoadingFactory();
		
		Class<?> clazz = factory.loadTestClass("C:\\Users\\Spellmaker\\Desktop\\test\\", "CoreASMCTest");
		
		Object o = clazz.getConstructor().newInstance();
		Method m = clazz.getMethod("eval");
		System.out.println(m.toString());
		m.invoke(o);
		
		//try2
		cdriver = new CompilerMock();
		
		cdriver.mockCode.put("guard", BooleanProvider.FALSE.compilerValue());
		cdriver.mockCode.put("truerule", "System.out.println(\"truerule\");\n");
		cdriver.mockCode.put("falserule", "System.out.println(\"falserule\");\n");
		
		cf = cdriver.compile(root, CodeType.U);
		code = cf.generateCode();
		cb = new CodeBuilder();
		cb.addModule(new BooleanTestingModule());	

		output = new BufferedWriter(new FileWriter(new File("C:\\Users\\Spellmaker\\Desktop\\test\\CoreASMCTest.java")));
		output.write(cb.generateCode(code));
		output.close();
		
		cc = new ClassCompiler();
		cc.addTask(new File("C:\\Users\\Spellmaker\\Desktop\\test\\CoreASMCTest.java"));
		try{
			cc.compile();
		}
		catch(Exception e){
			for(String s : cc.getErrors()) System.out.println(s);
			throw e;
		}
		//factory = new LoadingFactory();
		clazz = factory.loadTestClass("C:\\Users\\Spellmaker\\Desktop\\test\\", "CoreASMCTest");
		
		o = clazz.getConstructor().newInstance();
		m = clazz.getMethod("eval");
		System.out.println(m.toString());
		m.invoke(o);*/
		
		
		
		
		
	}
	
	
	
	
	
	public static void printDot(ASTNode s, BufferedWriter bw, int depth) throws Exception{
		bw.write("digraph " + "test" + "{");bw.newLine();
		bw.write("ratio=fill");bw.newLine();
		bw.write("size=\"8.27,11.7\"");bw.newLine();
		bw.write("ranksep=0.05");bw.newLine();
		bw.write("nodesep=0.05");bw.newLine();
		bw.write("page=\"8.5,11\"");bw.newLine();
		nodecount = 0;
		printDotNode(s, bw, depth);
		bw.write("}");bw.newLine();
	}
	
	public static int printDotNode(ASTNode n, BufferedWriter bw, int depth) throws Exception{
		if(depth == 0) return 0;
		
		int mycount = nodecount;
		
		String label = "n" + nodecount + 
				"[label=\"" + 
				"Node Type: " + n.getConcreteNodeType() + "\n" + 
				"Token: " + n.getToken() + "\n" + 
				"Plugin Name: " + n.getPluginName() + "\n" +
				"Grammar Class: " + n.getGrammarClass() + "\n" + 
				"Rule: " + n.getGrammarRule() + "\n";
		try{
			Map<String, Object> m = n.getProperties();
			for(Iterator<String> it = m.keySet().iterator(); it.hasNext(); ){
				String o = it.next();
				label = label + "Key: " + o.toString() + " | Value: " + m.get(o).toString() + "\n";
			}
		}
		catch(NullPointerException npe){
			label = label + "No properties\n";
		}

		label = label + "\"; fontsize=\"28\"]";
		
		bw.write(label);
		bw.newLine();
		for(int i = 0; i < n.getAbstractChildNodes().size(); i++){
			nodecount++;
			int tmp = printDotNode((ASTNode)n.getAbstractChildNodes().get(i), bw, depth - 1);
			bw.write("n" + mycount + " -> " + "n" + tmp); bw.newLine();
		}
		
		return mycount;
	}
}
