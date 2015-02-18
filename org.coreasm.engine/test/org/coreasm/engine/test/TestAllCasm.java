package org.coreasm.engine.test;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.Assert;

import org.coreasm.util.Tools;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestAllCasm {

	protected static List<File> testFiles = null;

	@BeforeClass
	public static void onlyOnce() {
		//setup the test by finding the test specifications
		URL url = TestAllCasm.class.getClassLoader().getResource("./without_test_class");

		try {
			testFiles = new LinkedList<File>();
			//recursively search for specifications
			getTestFiles(testFiles, new File(url.toURI()));
		}
		catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

	private final ByteArrayOutputStream logContent = new ByteArrayOutputStream();
	private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
	private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
	final static PrintStream origOutput = System.out;
	final static PrintStream origError = System.err;

	public static List<String> getFilteredOutput(File file, String filter) {
		List<String> filteredOutputList = new LinkedList<String>();
		BufferedReader input = null;
		Pattern pattern = Pattern.compile(filter + ".*");
		try {
			input = new BufferedReader(new FileReader(file));
			String line = null; //not declared within while loop
			while ((line = input.readLine()) != null) {
				Matcher matcher = pattern.matcher(line);
				if (matcher.find()) {
					int first = line.indexOf("\"", matcher.start()) + 1;
					int last = line.indexOf("\"", first);
					if (last > first)
						filteredOutputList.add(Tools.convertFromEscapeSequence(line.substring(first, last)));
				}
			}
			input.close();
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return filteredOutputList;
	}
	
	public static int getParameter(File file, String name) {
		int value = -1;
		BufferedReader input = null;
		Pattern pattern = Pattern.compile("@" + name + "\\s*(\\d+)");
		try {
			input = new BufferedReader(new FileReader(file));
			String line = null;
			while ((line = input.readLine()) != null) {
				Matcher matcher = pattern.matcher(line);
				if (matcher.find()) {
					value = Integer.parseInt(matcher.group(1));
					break;
				}
			}
			input.close();
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return value;
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
				&& file.getName().toLowerCase().matches(clazz.getSimpleName().toLowerCase() + "(.casm|.coreasm)"))
			testFiles.add(file);
	}

	static void getTestFiles(List<File> testFiles, File file) {
		if (file != null && file.isDirectory())
			for (File child : file.listFiles(new FileFilter() {

				@Override
				public boolean accept(File file) {
					return (file.isDirectory()
							|| file.getName().toLowerCase().endsWith(".casm")
							|| file.getName().toLowerCase().endsWith(".coreasm"));
				}
			})) {
				getTestFiles(testFiles, child);
			}
		else if (file != null)
			testFiles.add(file);
	}

	@Before
	public void setUpStreams() {
		System.setOut(new PrintStream(logContent));
		System.setErr(new PrintStream(errContent));
	}

	@After
	public void cleanUpStreams() {
		System.setOut(origOutput);
		System.setErr(origError);
	}

	@Test
	public void performTest() {
		TestReport t = null;
		boolean successful = true;
		//check if there are files for testing for this class
		if (testFiles.isEmpty()) {
			t = new TestReport(null, "no test file found!", -1, false);
			successful = false;
		}
		//perform test for all test files, output result, and modify test result if test has failed
		for (File testFile : testFiles) {
			t = runSpecification(testFile);
			if (!t.successful())
				successful = false;
			t.print();
			t = null;
		}
		//report overall test result
		//test failed if at least one test has failed
		if (!successful)
			Assert.fail("Test failed for class: " + TestAllCasm.class.getSimpleName());

	}

	public TestReport runSpecification(File testFile) {

		List<String> requiredOutputList = getFilteredOutput(testFile, "@require");
		List<String> refusedOutputList = getFilteredOutput(testFile, "@refuse");
		int minSteps = getParameter(testFile, "minsteps");
		if (minSteps <= 0)
			minSteps = 1;
		int maxSteps = getParameter(testFile, "maxsteps");
		if (maxSteps < minSteps)
			maxSteps = minSteps;
		TestEngineDriver td = null;
		String failMessage = "";
		int steps = 0;
		try {
			outContent.reset();
			errContent.reset();
			td = TestEngineDriver.newLaunch(testFile.getAbsolutePath());
			if (TestEngineDriver.TestEngineDriverStatus.stopped.equals(td.getStatus()))
				return new TestReport(
						testFile, "engine is stopped!", steps, false);

			td.setOutputStream(new PrintStream(outContent));
			for (steps = minSteps; steps <= maxSteps; steps++) {
				td.executeSteps(minSteps);
				minSteps = 1;
				//test if no error has occurred and maybe output error message
				if (!errContent.toString().isEmpty()) {
					failMessage = "An error occurred in " + testFile.getName() + ":" + errContent;
					return new TestReport(testFile, failMessage, steps, false);
				}
				//check if no refused output is contained
				for (String refusedOutput : refusedOutputList) {
					if (outContent.toString().contains(refusedOutput)) {
						failMessage = "refused output found in test file: " + testFile.getName()
								+ "\nrefused output:\n"
								+ refusedOutput
								+ "\nactual output:\n" + outContent.toString();
						return new TestReport(testFile, failMessage, steps, false);
					}
				}
				for (String requiredOutput : new LinkedList<String>(requiredOutputList)) {
					if (outContent.toString().contains(requiredOutput))
						requiredOutputList.remove(requiredOutput);
				}
				if (requiredOutputList.isEmpty())
					break;
			}
			//check if no required output is missing
			if (!requiredOutputList.isEmpty()) {
				failMessage = "missing required output for test file: " + testFile.getName()
						+ "\nmissing output:\n"
						+ requiredOutputList.get(0)
						+ "\nactual output:\n" + outContent.toString();
				return new TestReport(testFile, failMessage, steps - 1, false);
			}
		}
		catch (Exception e) {
			e.printStackTrace(origOutput);
		}
		finally {
			td.stop();
		}
		if (td.isRunning()) {
			failMessage = testFile.getName() + " has a running instance but is stopped!";
			return new TestReport(testFile, failMessage, steps, false);
		}
		else if (steps <= maxSteps /* only if successful */)
			return new TestReport(testFile, steps);
		else {
			failMessage = "No test result for test class " + TestAllCasm.class.getSimpleName();
			return new TestReport(testFile, failMessage, steps, false);
		}
	}

}
