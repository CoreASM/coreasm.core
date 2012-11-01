/**
 * Copyright (C) 2012 Roozbeh Farahbod 
 * 
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
package org.coreasm.engine.config;

import java.io.IOException;

/**
 * The interface of the Configuration component of CoreASM.
 * 
 * @author Roozbeh Farahbod
 * 
 */
public interface IConfiguration {

	String ENV_COREASM_HOME = "COREASM_HOME";

	String CONF_LOGGER_CONFIG_FILE_NAME = "coreasm.config.loggerConfigFileName";

	String CONF_APP_NAME = "coreasm.config.applicationName";

	String CONF_MAIN_CLASS = "coreasm.config.mainClass";

	/** Holds the path to the root folder of CoreASM. */
	String CONF_APP_ROOT_FOLDER = "coreasm.config.rootFolder";

	/**
	 * Holds the path to the plugins folder, relative to the root folder of
	 * CoreASM.
	 */
	String CONF_PLUGINS_DIRECTORIES = "coreasm.config.pluginsDirs";

	/**
	 * Holds the folder name for configuration files relative to the application
	 * root folder.
	 */
	String DEFAULT_CONFIG_FOLDER_NAME = "config";

	String DEFAULT_CONFIG_FILE_NAME = "coreasm-defaults.conf";

	String DIR_SEPARATOR = ":";

	/**
	 * Returns the configured value of the given property in CoreASM. The value
	 * of the property is first looked up in the runtime CoreASM configuration,
	 * if there is no value defined there, then the system properties is
	 * checked, and if no value is found, the default value will be returned. If
	 * there is no default value defined for the given property, it returns
	 * null.
	 * 
	 * @param key
	 *            property key
	 * @return Returns the configured value of the given property.
	 */
	Object getProperty(String key);

	/**
	 * Returns the configured value of the given property as a String. This
	 * method calls the {@link Object#toString()} of the property value and is
	 * for convenience only. If the given property is not set, it returns
	 * <code>null</code>.
	 * 
	 * @param key
	 *            property key
	 * @return Returns the configured value of the given property as a String.
	 * 
	 * @see #getProperty(String)
	 */
	String getPropertyAsStr(String key);

	/**
	 * Returns the configured value of the given property as a Boolean value.
	 * This method uses the {@link #getPropertyAsStr(String)} and interprets
	 * values 'yes' and 'true' (case insensitive) as a Boolean <code>true</code>
	 * value and all other values as <code>false</code>. If the value of the
	 * given property is <code>null</code> it returns the passed default value.
	 * 
	 * @param key
	 *            property key
	 * @param defaultValue
	 *            the default value returned in case of a null property value
	 * 
	 * @return the value of the given property as a boolean
	 * 
	 * @see #getProperty(String)
	 */
	boolean getPropertyAsBoolean(String key, boolean defaultValue);

	/**
	 * Returns the configured value of the given property as a Long value. This
	 * method uses the {@link Long.#parseLong(String)} to interpret the values.
	 * If the value of the given property is <code>null</code> it returns the
	 * passed default value.
	 * 
	 * @param key
	 *            property key
	 * @param defaultValue
	 *            the default value returned in case of a null property value
	 * 
	 * @return the value of the given property as a long
	 * 
	 * @see #getProperty(String)
	 */
	long getPropertyAsLong(String key, long defaultValue);

	/**
	 * Returns the configured value of the given property as an Integer value.
	 * This method uses the {@link Integer.#parseInt(String)} to interpret the
	 * values. If the value of the given property is <code>null</code> it
	 * returns the passed default value.
	 * 
	 * @param key
	 *            property key
	 * @param defaultValue
	 *            the default value returned in case of a null property value
	 * 
	 * @return the value of the given property as an int
	 * 
	 * @see #getProperty(String)
	 */
	int getPropertyAsInteger(String key, int defaultValue);

	/**
	 * Sets the value of the given property for the current execution.
	 * 
	 * @param key
	 *            property key
	 * @param value
	 *            property value
	 */
	void setProperty(String key, Object value);

	/**
	 * Returns the default value of the given property.
	 * 
	 * @param key
	 *            property key
	 * 
	 * @return Returns the default value of the given property.
	 */
	Object getDefaultValue(String key);

	/**
	 * Loads user-level configurations from a file. If the file name is not an
	 * absolute path, the file is searched in the following locations:
	 * <ol>
	 * <li>the {@value #DEFAULT_CONFIG_FOLDER_NAME} directory,</li>
	 * <li>current folder,</li>
	 * <li>the {@value #DEFAULT_CONFIG_FOLDER_NAME} directory in classpath,</li>
	 * <li>and finally the classpath.</li>
	 * </ol>
	 * where classpath is determined by the system class loader. See
	 * {@link #loadConfiguration(String, ClassLoader)} for loading default
	 * configuration providing a class loader.
	 * 
	 * The configuration is loaded in an incremental fashion; i.e., the loaded
	 * configuration will be added to (and overriding) the existing default
	 * configuration.
	 * <p>
	 * See {@link #getAppRootDirectory()} and {@link #getDefaultValue(String)}.
	 * 
	 * @param fileName
	 *            the name of a properties file
	 * @throws ConfigurationException
	 *             if initializing the configuration fails
	 */
	void loadConfiguration(String fileName) throws ConfigurationException;

	/**
	 * Loads user-level configurations from a file name. If the file name is not
	 * an absolute path, the file is searched in the following places:
	 * <ol>
	 * <li>the {@value #DEFAULT_CONFIG_FOLDER_NAME} directory,</li>
	 * <li>current folder,</li>
	 * <li>the {@value #DEFAULT_CONFIG_FOLDER_NAME} directory in classpath,</li>
	 * <li>and finally the classpath.</li>
	 * </ol>
	 * where classpath is determined by the given class loader.
	 * 
	 * The configuration is loaded in an incremental fashion; i.e., the loaded
	 * configuration will be added to (and overriding) the existing default
	 * configuration.
	 * <p>
	 * See {@link #getAppRootDirectory()} and {@link #getDefaultValue(String)}.
	 * 
	 * @param classLoader
	 *            an instance of a class loader
	 * @param fileName
	 *            the name of a properties file
	 * @throws ConfigurationException
	 *             if initializing the configuration fails
	 */
	void loadConfiguration(String fileName, ClassLoader classLoader) throws ConfigurationException;

	/**
	 * @return Returns the application root directory.
	 */
	String getAppRootDirectory();

	/**
	 * @return Returns the application name for this executable instance.
	 */
	String getApplicationName();

	/**
	 * @return Returns the application's configuration directory.
	 */
	String getAppConfDirectory();

	/**
	 * Sets the application name for this executable instance.
	 * 
	 * @param appName
	 *            an application name
	 */
	void setApplicationName(String appName);

	/**
	 * Configures the logger based on the current 
	 * configuration. 
	 */
	void configLogger();
	
	/**
	 * Writes the current configuration values into a file.
	 * 
	 * @param fileName
	 *            the name of the file
	 * @throws IOException
	 *             if exporting the configuration fails
	 */
	void writeConfiguration(String fileName) throws IOException;

}
