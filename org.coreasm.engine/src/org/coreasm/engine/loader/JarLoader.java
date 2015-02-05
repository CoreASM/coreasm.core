package org.coreasm.engine.loader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;

import org.coreasm.engine.EngineException;
import org.coreasm.engine.plugin.Plugin;

public class JarLoader {
	/*
	 * Loads a plugin from a Jar file.
	 */
	public static Plugin loadPluginClassesFromJarFile(File file) throws EngineException{
		String className = null;
		try {
			className = getJarPluginClassName(new FileInputStream(file));
		} catch (IOException e) {
			throw new EngineException("Cannot load the JAR file.");
		} catch (EngineException e) {
			throw e;
		}
		return PluginClassLoader.loadPlugin(file.getName(), className, file);//loader.loadPlugin(file.getName(), className, file);
	}

	/*
	 * Loads a plugin from a Jar file.
	 */
	public static Plugin loadPluginClassesFromJarFile(JarFile jar, JarEntry entry) throws EngineException {
		String className = null;
		try {
			className = getJarPluginClassName(jar.getInputStream(entry));
		}
		catch (IOException e) {
			throw new EngineException("Cannot load the JAR file.");
		}
		catch (EngineException e) {
			throw e;
		}
		try {
			return PluginClassLoader.loadPlugin(entry.getName(), className, new URL("jar", "", jar.getName() + "!/" + entry.getName()));//loader.loadPlugin(entry.getName(), className, new URL("jar", "", jar.getName() + "!/" + entry.getName()));
		}
		catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * Given a Jar file name, looks for the name of the plugin class.
	 * 
	 * @param inputStream
	 *            input stream from a jar file
	 * @return full class name of the plugin (e.g., "test.plugin.TestPlugin")
	 * @throws IOException
	 *             in case of any IO error
	 * @throws EngineException
	 *             if the plugin does not have an identification file
	 */
	private static String getJarPluginClassName(InputStream inputStream) throws IOException,
			EngineException {
		JarInputStream stream = new JarInputStream(inputStream);
		JarEntry jEntry = null;
		boolean found = false;
		do {
			jEntry = stream.getNextJarEntry();
			if (jEntry != null)
				if (jEntry.getName().equals("CoreASMPlugin.id")) {
					found = true;
					break;
				}
		} while (jEntry != null);
		String pluginClassName = null;
		if (found)
			pluginClassName = LoadingTools.getPluginClassName(stream);
		stream.close();
		if (pluginClassName == null)
			throw new EngineException("Invalid Plugin package (" + inputStream
					+ "). Cannot find the identification file.");
		return pluginClassName;
	}
}
