package org.coreasm.engine.test;

import java.io.File;
import java.util.ArrayList;

public class TestReport {
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
			TestAllCasm.origOutput
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
			TestAllCasm.origOutput.println(this.message.isEmpty() ? success : success + "; " + this.message);
		}
		else
			TestAllCasm.origError.println("An error occurred after " + steps + " steps in " + this.file.getName() + ": "
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