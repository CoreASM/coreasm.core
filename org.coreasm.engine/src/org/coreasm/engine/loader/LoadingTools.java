package org.coreasm.engine.loader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class LoadingTools {
	/**
	 * Reads full class name of a plugin from a text file.
	 *
	 * @param stream
	 *            input stream of the text file (plugin identification file)
	 * @return full class name
	 * @throws IOException
	 *             in case of any IO error
	 */
	public static String getPluginClassName(InputStream stream) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
		String name = reader.readLine();
		return name;
	}
}
