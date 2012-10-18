package org.coreasm.eclipse.engine.driver;

import org.eclipse.swt.graphics.RGB;

public interface IStreamsColorConstants {
	final RGB OUTPUT = new RGB(0, 0, 0);
	final RGB ERROR = new RGB(128, 0, 0);
	final RGB DUMP = new RGB(0, 128, 0);
	final RGB DEFAULT = OUTPUT;
}