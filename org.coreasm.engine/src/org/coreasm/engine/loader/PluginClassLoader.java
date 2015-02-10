package org.coreasm.engine.loader;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.coreasm.engine.ControlAPI;
import org.coreasm.engine.EngineException;
import org.coreasm.engine.EngineProperties;
import org.coreasm.engine.plugin.Plugin;
import org.coreasm.util.Tools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PluginClassLoader {
	private static final Logger logger = LoggerFactory.getLogger(PluginClassLoader.class);
	public static ClassLoader classLoader = null;
	
	/**
	 * Loads plugin catalog. This method looks for all available plugins, and
	 * creates a map of plugin names to plugin objects. This method does NOT
	 * initialize any plugin.
	 *
	 * @throws IOException
	 *
	 */
	public static Map<String, Plugin> loadCatalog(ControlAPI capi) throws IOException {
		Map<String, Plugin> result = new HashMap<String, Plugin>();
		logger.debug("Loading plugin catalog...");

		try {
			result.putAll(loadCatalog(PluginClassLoader.class, "plugins/"));
		}
		catch (URISyntaxException e) {
			e.printStackTrace();
		}

		// looking to the extended plugin folders
		String pluginFolders = capi.getProperty(EngineProperties.PLUGIN_FOLDERS_PROPERTY);
		if (pluginFolders != null) {
			for (String pluginFolder : Tools.tokenize(pluginFolders, EngineProperties.PLUGIN_FOLDERS_DELIM)) {
				File folder = new File(pluginFolder);
				if (folder.isDirectory()) {
					for (File file : folder.listFiles()){
						Plugin p = loadPluginClasses(file);
						if(p != null){
							result.put(p.getName(), p);
						}
					}
				}
			}
		}
		
		//set the capi of the loaded plugins
		for(Entry<String, Plugin> entry : result.entrySet()){
			entry.getValue().setControlAPI(capi);
		}
		
		return result;
	}

	private static Map<String, Plugin> loadCatalog(Class<?> clazz, String dir) throws URISyntaxException, IOException {
		Map<String, Plugin> resultMap = new HashMap<String, Plugin>();
		URL dirURL = clazz.getClassLoader().getResource(dir);
		//1st case: file
		if (dirURL != null && dirURL.getProtocol().equals("file")) {
			/* A file path: easy enough */
			File[] files = new File(dirURL.toURI()).listFiles();
			for (File file : files){
				Plugin p = loadPluginClasses(file);
				if(p != null){
					resultMap.put(p.getName(), p);
				}
			}
		}
		else {
			if (dirURL == null) {
				/*
				 * In case of a jar file, we can't actually find a directory.
				 * Have to assume the same jar as clazz.
				 */
				String me = clazz.getName().replace(".", "/") + ".class";
				dirURL = clazz.getClassLoader().getResource(me);
			}
	
			//2nd case: jar
			if (dirURL.getProtocol().equals("jar")) {
				/* A JAR path */
				String jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf("!")); //strip out only the JAR file
				JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
				Enumeration<JarEntry> entries = jar.entries(); //gives ALL entries in jar
				Set<JarEntry> result = new HashSet<JarEntry>(); //avoid duplicates in case it is a subdirectory
				while (entries.hasMoreElements()) {
					JarEntry entry = entries.nextElement();
					String name = entry.getName();
					if (name.startsWith(dir) && !name.substring(dir.length()).contains("/")) { //filter according to the path
						if (result.add(entry)){
							Plugin p = loadPluginClasses(jar, entry);
							if(p != null) resultMap.put(p.getName(), p);
						}
					}
				}
			}
			//3rd case: bundle resource
			else if (dirURL.getProtocol().equals("bundleresource")) {
				/* A JAR path */
				if (System.getProperty(Tools.COREASM_ENGINE_LIB_PATH) == null)
					throw new NullPointerException("path to engine library has not been set.");
				JarFile jar = new JarFile(System.getProperty(Tools.COREASM_ENGINE_LIB_PATH));
				Enumeration<JarEntry> entries = jar.entries(); //gives ALL entries in jar
				Set<JarEntry> result = new HashSet<JarEntry>(); //avoid duplicates in case it is a subdirectory
				while (entries.hasMoreElements()) {
					JarEntry entry = entries.nextElement();
					String name = entry.getName();
					if (name.startsWith(dir) && !name.substring(dir.length()).contains("/")) { //filter according to the path
						if (result.add(entry)){
							Plugin p = loadPluginClasses(jar, entry);
							if(p != null) resultMap.put(p.getName(), p);
						}
					}
				}
			}
			else
				throw new UnsupportedOperationException("Cannot list files for URL " + dirURL);
		}
		return resultMap;
	}
	
	/*
	 * Loads a single plugin
	 */
	private static Plugin loadPluginClasses(JarFile jar, JarEntry entry) {
		try {
			// checking if the path points to a directory (folder)
			if (entry.isDirectory())
				;
			//				loadPluginClassesFromDirectory(entry);
			else
			// checking if the path points to a ZIP file
			if (entry.getName().toUpperCase().endsWith(".ZIP"))
				return ZipLoader.loadPluginClassesFromZipFile(entry);
			else
			// checking if the path points to a JAR file
			if (entry.getName().toUpperCase().endsWith(".JAR"))
				return JarLoader.loadPluginClassesFromJarFile(jar, entry);
			else
				throw new EngineException("Cannot detect plugin.");
		}
		catch (EngineException e) {
			e.printStackTrace();
			logger.error("Cannot load plugin '{}'. Skipping this plugin. Error: {}", entry.getName(), e.getMessage());
		}
		return null;
	}
	
	/*
	 * Loads a single plugin
	 */
	private static Plugin loadPluginClasses(File file) {
		try {
			// checking if the path points to a directory (folder)
			if (file.isDirectory()){
				return DirectoryLoader.loadPluginClassesFromDirectory(file);
			}
			else
			// checking if the path points to a ZIP file
			if (file.getName().toUpperCase().endsWith(".ZIP")){
				return ZipLoader.loadPluginClassesFromZipFile(file);
			}
			else
			// checking if the path points to a JAR file
			if (file.getName().toUpperCase().endsWith(".JAR")){
				return JarLoader.loadPluginClassesFromJarFile(file);
			}
			else
				throw new EngineException("Cannot detect plugin.");
		} catch (EngineException e) {
			logger.error("Cannot load plugin '{}'. Skipping this plugin. Error: {}", file.getName(), e.getMessage());
		}
		return null;
	}
	
	/*
	 * Loads a single plugin class from the given list of resources.
	 */
	public static Plugin loadPlugin(String pName, String className, File... resources) throws EngineException {
		URL[] urls = new URL[resources.length];
		try {
			for (int i=0; i < resources.length; i++)
				urls[i] = resources[i].toURI().toURL();
		} catch (MalformedURLException e) {
			throw new EngineException("Cannot locate plugin.");
		}
		return loadPlugin(pName, className, urls);
	}

	/*
	 * Loads a single plugin class from the given list of resources.
	 */
	public static Plugin loadPlugin(String pName, String className, URL... urls) throws EngineException {
		URLClassLoader loader = null;
		if (classLoader == null)
			loader = new URLClassLoader(urls);
		else
			loader = new URLClassLoader(urls, classLoader);

		Object o = null;
		Class<?> pc = null;
		try {
			logger.debug( "Loading plugin: {}", className);

			//System.out.println("urls: ");
			//for(URL url : urls) System.out.println(url);
			//System.out.println(className);
			//System.out.println(pName);
			
			pc = loader.loadClass(className);
			loader.close();
			/*
			MinimumEngineVersion minVersion = (MinimumEngineVersion)pc.getAnnotation(MinimumEngineVersion.class);
			Object[] annots = pc.getAnnotations();
			if (minVersion != null) {
				VersionInfo vreq = VersionInfo.valueOf(minVersion.value());
				if (vreq != null && vreq.compareTo(this.VERSION_INFO) > 0) {
					Logger.log(Logger.ERROR, Logger.controlAPI,
							"Cannot load plugin '" + fullName +
							"' as it is built for a more recent version" +
							"of the engine (" + vreq + " and above). Skipping this plugin.");
					continue;
				}
			}
			*/
			o = pc.newInstance();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Cannot load plugin '{}'. Skipping this plugin. Error: {}", pName, e.getMessage());
			return null;
		}
		if (o instanceof Plugin) {
			Plugin p = (Plugin) o;
			//p.setControlAPI(this.capi);
			logger.debug("Plugin '{}' is usable.", p.getName());
			return p; //allPlugins.put(p.getName(), p);
		} else
			logger.error(
						"Invalid plugin '{}'. This class does not extend the CoreASM Plugin class.", className);
		
		return null;
	}
}
