package org.coreasm.engine.loader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import org.coreasm.engine.EngineException;
import org.coreasm.util.Tools;

public class DirectoryLoader {
	/*
	 * Loads a single plugin from a directory
	 */
	public static void loadPluginClassesFromDirectory(File file, PluginLoader loader) throws EngineException {
		String[] contents = file.list();
		if (contents == null)
			throw new EngineException("Plugin folder is empty.");
		else {
			// if there is a properties file
			if (Tools.find(PluginLoader.PLUGIN_PROPERTIES_FILE_NAME, contents) > -1) {
				Properties properties = new Properties();

				// load the properties
				try {
					properties.load(
							new FileInputStream(
									file.getAbsolutePath() + File.separator + PluginLoader.PLUGIN_PROPERTIES_FILE_NAME
									)
							);
				} catch (IOException e) {
					throw new EngineException("Cannot load plugin properties file.");
				}

				// get the main class name
				final String className = properties.getProperty(PluginLoader.PLUGIN_ID_PROPERTY_NAME);
				if (className == null | className.length() == 0)
					throw new EngineException("Plugin class file name is invalid.");

				// get the classpath
				final String classpath = properties.getProperty(PluginLoader.PLUGIN_CLASSPATH_PROPERTY_NAME);
				ArrayList<File> pathList = new ArrayList<File>();
				for (String folder: Tools.tokenize(classpath, PluginLoader.PLUGIN_CLASSPATH_SEPARATOR))
					if (folder.length() != 0)
						pathList.add(new File(file.getAbsolutePath() + File.separator + folder));

				// load the plugin
				loader.loadPlugin(file.getName(), className, pathList.toArray(new File[]{}));

			} else
				if (Tools.find(PluginLoader.PLUGIN_ID_FILE_NAME, contents) > -1) {
					String className = "";
					try {
						className = LoadingTools.getPluginClassName(
								new FileInputStream(file.getAbsolutePath() + File.separator + PluginLoader.PLUGIN_ID_FILE_NAME));
					} catch (IOException e) {
						throw new EngineException("Cannot read plugin identification file.");
					}
					loader.loadPlugin(file.getName(), className, file);
				} else
					throw new EngineException("Cannot detect plugin.");
		}
	}
}
