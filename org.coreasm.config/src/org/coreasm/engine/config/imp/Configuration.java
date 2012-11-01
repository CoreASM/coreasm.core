/**
 * Copyright (C) 2012 Roozbeh Farahbod 
 * 
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */

package org.coreasm.engine.config.imp;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.coreasm.engine.config.ConfigurationException;
import org.coreasm.engine.config.IConfiguration;
import org.coreasm.util.Tools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;

/**
 * The default implementation of a configuration component for CoreASM.
 * 
 * @author Roozbeh Farahbod
 */
public class Configuration implements IConfiguration {

	private static final Logger logger = LoggerFactory.getLogger(Configuration.class);

	/** Holds the default configuration values. */
	private Map<String, Object> defaultValues = new HashMap<String, Object>();

	/** Holds the configured values. */
	private Map<String, Object> properties = new HashMap<String, Object>();

	/*
	 * Preventing the instantiation of the class by other classes.
	 */
	protected Configuration(Class<?> mainClass) {
		try {
			logger.info("Initializing CoreASM configuration component{}.", 
					(mainClass == null ? "" : " with main class "
					+ mainClass.getName() + ""));
			setDefaultValues(mainClass);
		} catch (ConfigurationException e) {
			throw new RuntimeException(e);
		}
	};

	/**
	 * Sets the default property values.
	 * 
	 * @throws ConfigurationException
	 */
	private void setDefaultValues(Class<?> mainClass) throws ConfigurationException {
		if (mainClass != null) {
			defaultValues.put(CONF_MAIN_CLASS, mainClass);
		}
		getAppRootDirectory();
		loadConfiguration(defaultValues, this.getClass().getClassLoader(), DEFAULT_CONFIG_FILE_NAME);
	}
	
	/**
	 * Loads configuration into a configuration map.
	 */
	private void loadConfiguration(Map<String, Object> dest, ClassLoader classLoader, String fileName)
			throws ConfigurationException {
		InputStream in = null;
		try {
			in = Tools.findConfigFileAsInputStream(classLoader, 
					getAppRootDirectory(), DEFAULT_CONFIG_FOLDER_NAME, fileName);
		} catch (FileNotFoundException e) {
			logger.warn("Exception caught: {}", e);
		}

		if (in == null) {
			logger.warn("Cannot load configuration file '{}'.", fileName);
		} else {
			loadConfigFromStream(dest, in);
			applyConfiguration();
		}
	}

	/**
	 * Applies the current configuration.
	 */
	private void applyConfiguration() {
		configLogger();
	}

	/**
	 * Loads configuration from a stream into the destination
	 * configuration holder.
	 * 
	 * @param stream
	 *            the input stream
	 */
	private void loadConfigFromStream(Map<String, Object> dest, InputStream stream) throws ConfigurationException {
		Properties prop = new Properties();
		try {
			prop.load(stream);
		} catch (IOException e) {
			throw new ConfigurationException("Could not load configuration. (Reason: " + e.getMessage() + ")", e);
		}
		for (Entry<Object, Object> entry : prop.entrySet()) {
			if (dest == properties) {
				setProperty((String) entry.getKey(), entry.getValue());
			} else {
				dest.put((String) entry.getKey(), entry.getValue());
			}
		}
	}

	@Override
	public Object getProperty(String key) {
		Object value = properties.get(key);
		
		// if this property is not configured, check system properties
		if (value == null) {
			value = System.getProperty(key);
		}

		// if no value in system properties is found, check the default values
		if (value == null) {
			value = defaultValues.get(key);
		}

		return value;
	}

	@Override
	public String getPropertyAsStr(String key) {
		final Object value = getProperty(key);
		if (value != null) {
			return value.toString().trim();
		} else {
			return null;
		}
	}

	@Override
	public boolean getPropertyAsBoolean(String key, boolean defaultValue) {
		final String value = getPropertyAsStr(key);
		if (value == null) {
			return defaultValue;
		} else {
			return "true".equalsIgnoreCase(value.trim()) 
					|| "yes".equalsIgnoreCase(value.trim());
		}

	}

	@Override
	public long getPropertyAsLong(String key, long defaultValue) {
		final String value = getPropertyAsStr(key);
		if (value == null) {
			return defaultValue;
		} else {
			return Long.parseLong(value.trim());
		}
	}

	@Override
	public int getPropertyAsInteger(String key, int defaultValue) {
		final String value = getPropertyAsStr(key);
		if (value == null) {
			return defaultValue;
		} else {
			return Integer.parseInt(value.trim());
		}
	}

	@Override
	public void setProperty(String key, Object value) {
		properties.put(key, value);
	}

	@Override
	public Object getDefaultValue(String key) {
		return defaultValues.get(key);
	}

	@Override
	public void loadConfiguration(String fileName) throws ConfigurationException {
		loadConfiguration(properties, this.getClass().getClassLoader(), fileName);
	}

	@Override
	public void loadConfiguration(String fileName, ClassLoader classLoader) throws ConfigurationException {
		loadConfiguration(properties, classLoader, fileName);
	}

	@Override
	public String getAppRootDirectory() {
		String result = getPropertyAsStr(CONF_APP_ROOT_FOLDER);
		if (result == null) {
			result = System.getenv(ENV_COREASM_HOME);
			if (result == null) {
				result = Tools.getRootFolder(getMainClass());
			}
			setProperty(CONF_APP_ROOT_FOLDER, result);
		}
		return result;
	}

	private Class<?> getMainClass() {
		return (Class<?>) getProperty(CONF_MAIN_CLASS);
	}

	@Override
	public String getApplicationName() {
		return getPropertyAsStr(CONF_APP_NAME);
	}

	@Override
	public String getAppConfDirectory() {
		return Tools.concatFileName(getAppRootDirectory(), DEFAULT_CONFIG_FOLDER_NAME);
	}

	@Override
	public void configLogger() {
		// The following code loads the logback config file using
		// JoranConfigurator.
		// Alternatively, you can specify the location of the config file using
		// the system property 'logback.configurationFile'
		// e.g.,
		// $ java -Dlogback.configurationFile=/path/to/config.xml ...

		String fileName = getPropertyAsStr(CONF_LOGGER_CONFIG_FILE_NAME);
		if (fileName != null) {
			LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
			try {
				logger.debug("Configuring logback using '{}'...", fileName);

				JoranConfigurator configurator = new JoranConfigurator();
				configurator.setContext(lc);
				// the context was probably already configured by default
				// configuration
				// rules
				lc.reset();

				configurator
						.doConfigure(Tools.findConfigFileAsInputStream(
								ClassLoader.getSystemClassLoader(), getAppRootDirectory(), 
								null, fileName));
			} catch (JoranException je) {
				logger.warn(
						"Failed loading the logback configuration file. Using default configuration. Error message: {}",
						je.getMessage());
			} catch (FileNotFoundException e) {
				logger.warn(
						"Failed loading the logback configuration file. Configuration file cannot be opened. ('{}')",
						fileName);
			}

			logger.debug("Logback configured.");
		}
	}

	@Override
	public void writeConfiguration(String fileName) throws IOException {
		Properties props = new Properties();

		logger.debug("Writing configuration file to {}...", fileName);

		for (Entry<String, Object> e : defaultValues.entrySet()) {
			copyConfigItem(e, props);
		}

		for (Entry<String, Object> e : properties.entrySet()) {
			copyConfigItem(e, props);
		}

		props.store(new FileOutputStream(fileName), "CoreASM Configuration");

		logger.debug("Configuration file written to {}.", fileName);
	}
	
	private void copyConfigItem(Entry<String, Object> e, Properties destination) {
		if (e.getValue() instanceof Number || e.getValue() instanceof Boolean || e.getValue() instanceof String) {
			destination.setProperty(e.getKey(), e.getValue().toString());
		} else {
			logger.debug("Skipping configuration item '{}'.", e.getKey());
		}

	}

	@Override
	public void setApplicationName(String appName) {
		setProperty(CONF_APP_NAME, appName);
	}

}
