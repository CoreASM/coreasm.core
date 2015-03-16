package org.coreasm.engine.test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests the CoreASM compiler
 * @author Spellmaker
 *
 */
public class TestAllCCasm {
	//streams for in and output
	private final ByteArrayOutputStream logContent = new ByteArrayOutputStream();
	//private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
	private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
	final static PrintStream origOutput = System.out;
	final static PrintStream origError = System.err;
	//list of test cases
	protected static List<File> testFiles = null;
	
	@BeforeClass
	public static void onlyOnce() {
		//setup the test by finding the test specifications
		URL url = TestAllCasm.class.getClassLoader().getResource("./without_test_class");

		try {
			testFiles = new LinkedList<File>();
			//recursively search for specifications
			TestAllCasm.getTestFiles(testFiles, new File(url.toURI()));
		}
		catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
	
	@Before
	public void setUpStreams() {
		//redirect output
		System.setOut(new PrintStream(logContent));
		System.setErr(new PrintStream(errContent));
	}

	@After
	public void cleanUpStreams() {
		//reset in and output to defaults
		System.setOut(origOutput);
		System.setErr(origError);
	}
	
	@Test
	public void performTest(){
		TestReport t = null;
		boolean successful = true;
		//check if there are files for testing for this class
		if (testFiles.isEmpty()) {
			t = new TestReport(null, "no test file found!", -1, false);
			successful = false;
		}
		//perform test for all test files, output result, and modify test result if test has failed
		for (File testFile : testFiles) {
			t = CompilerDriver.runSpecification(testFile);
			if (!t.successful())
				successful = false;
			t.print();
			t = null;
		}
		//report overall test result
		//test failed if at least one test has failed
		if (!successful)
			Assert.fail("Test failed for class: " + TestAllCCasm.class.getSimpleName());
	}

	protected static void getTestFile(List<File> testFiles, File file, Class<?> clazz) {
		if (!testFiles.isEmpty())
			return;
		if (file != null && file.isDirectory())
			for (File child : file.listFiles(new FileFilter() {

				@Override
				public boolean accept(File file) {
					return (file.isDirectory()
							|| file.getName().toLowerCase().endsWith(".casm")
							|| file.getName().toLowerCase().endsWith(".coreasm"));
				}
			})) {
				getTestFile(testFiles, child, clazz);
			}
		else if (file != null
				&& file.getName().toLowerCase().matches(clazz.getSimpleName().replace("Compiler", "").toLowerCase() + "(.casm|.coreasm)"))
			testFiles.add(file);
	}
}
