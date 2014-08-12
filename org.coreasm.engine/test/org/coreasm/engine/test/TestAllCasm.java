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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.coreasm.util.Tools;

public class TestAllCasm {

	protected static List<File> testFiles = null;

	@BeforeClass
	public static void onlyOnce() {
		URL url = TestAllCasm.class.getClassLoader().getResource("./without_test_class");

		try {
			testFiles = new LinkedList<File>();
			getTestFiles(testFiles, new File(url.toURI()));
		}
		catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

	private final ByteArrayOutputStream logContent = new ByteArrayOutputStream();
	private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
	private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
	private final static PrintStream origOutput = System.out;
	private final static PrintStream origError = System.err;

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
		boolean succesful = true;
		//check if their are files for testing for this class
		if (testFiles.isEmpty()) {
			t = new TestReport(null, "no test file found!", -1, false);
			succesful = false;
		}
		//perform test for all test files, output result, and modify test result if test has been failed
		for (File testFile : testFiles) {
			t = runSpecification(testFile);
			if (!t.successful())
				succesful = false;
			t.print();
			t = null;
		}
		//report overall test result
		//test failed if at least one test has been failed
		if (!succesful)
			Assert.fail("Test failed for class: " + TestAllCasm.class.getSimpleName());

	}

	public TestReport runSpecification(File testFile) {

		List<String> requiredOutputList = getFilteredOutput(testFile, "@require");
		List<String> refusedOutputList = getFilteredOutput(testFile, "@refuse");
		List<String> minStepsList = getFilteredOutput(testFile, "@minsteps");
		List<String> maxStepsList = getFilteredOutput(testFile, "@maxsteps");
		int minSteps = 1;
		if (!minStepsList.isEmpty()) {
			try {
				minSteps = Integer.parseInt(minStepsList.get(0));
			}
			catch (NumberFormatException e) {
			}
		}
		int maxSteps = minSteps;
		if (!maxStepsList.isEmpty()) {
			try {
				maxSteps = Integer.parseInt(maxStepsList.get(0));
			}
			catch (NumberFormatException e) {
			}
		}
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
				//test if no error has been occured and maybe output error message
				if (!errContent.toString().isEmpty()) {
					failMessage = "An error occurred in " + testFile.getName() + ":" + errContent;
					return new TestReport(testFile, failMessage, steps, false);
				}
				//check if no refused output is contained
				for (String refusedOutput : refusedOutputList) {
					if (outContent.toString().contains(refusedOutput)) {
						failMessage = "refused output found in test file: " + testFile.getName()
								+ ", refused output: "
								+ refusedOutput
								+ ", actual output: " + outContent.toString();
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
						+ ", missing output: "
						+ requiredOutputList.get(0)
						+ ", actual output: " + outContent.toString();
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

	static class TestReport {
		private static ArrayList<TestReport> reports = new ArrayList<TestReport>();
		private File file;
		private String message;
		private int steps;
		private boolean successful;

		public TestReport(File file, int steps) {
			this(file, "", steps);
		}

		public TestReport(File file, String message, int steps) {
			this(file, message, steps, true);
		}

		public TestReport(File file, String message, int steps, boolean successful) {
			this.file = file;
			this.message = message;
			this.successful = successful;
			this.steps = steps;
			if (!TestReport.reports.isEmpty()
					&& TestReport.getLast().getFile() == this.file)
				origOutput
						.println("Last report has been for the same file. Check if your test produces a unique result.");
			reports.add(this);
		}

		private File getFile() {
			return this.file;
		}

		public void print() {
			if (this.successful) {
				String success = "Test of " + this.file.getName() + " successful after " + steps
						+ (steps == 1 ? " step" : " steps");
				origOutput.println(this.message.isEmpty() ? success : success + "; " + this.message);
			}
			else
				origError.println("An error occurred after " + steps + " steps in " + this.file.getName() + ": "
						+ this.message);
		}

		public void printTestReports() {
			for (TestReport report : reports) {
				report.print();
			}
		}

		public boolean successful() {
			return this.successful;
		}

		public static TestReport getLast() {
			if (reports.isEmpty())
				return null;
			else
				return reports.get(reports.size() - 1);
		}

		public String getMessage() {
			return this.message;
		}
	}

}
