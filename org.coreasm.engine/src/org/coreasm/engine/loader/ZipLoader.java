package org.coreasm.engine.loader;

import java.io.File;
import java.util.jar.JarEntry;

import org.coreasm.engine.EngineException;

public class ZipLoader {
	/*
	 * Loads a plugin from a Zip file.
	 */
	public static void loadPluginClassesFromZipFile(File file) throws EngineException {
		throw new EngineException("Plugin ZIP files are not supported." + file.getName());
	}

	/*
	 * Loads a plugin from a Zip file.
	 */
	public static void loadPluginClassesFromZipFile(JarEntry entry) throws EngineException {
		throw new EngineException("Plugin ZIP files are not supported. " + entry.getName());
	}
}
