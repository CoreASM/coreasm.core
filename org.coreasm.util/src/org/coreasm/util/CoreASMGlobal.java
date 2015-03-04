/*	
 * CoreASMGlobal.java 	1.0 	$Revision: 243 $
 * 
 *
 * Copyright (C) 2005 Roozbeh Farahbod 
 * 
 * Last modified by $Author: rfarahbod $ on $Date: 2011-03-29 02:05:21 +0200 (Di, 29 Mrz 2011) $.
 *
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */
 
package org.coreasm.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 
 *	Provides global services for CoreASM engine and its components.
 *   
 *  @author  Roozbeh Farahbod
 *  
 */
public class CoreASMGlobal {
	
	protected static final Logger logger = LoggerFactory.getLogger(CoreASMGlobal.class);

	/** The binary folder of CoreASM */
	//private static final String BIN_FOLDER = "/bin";
	
	/** The config folder of CoreASM */
	private static final String CONFIG_FOLDER = "/config";
	
	/** The kernel configuration file. */
	private static final String KERNEL_CONF_FILE_NAME = "kernel.conf";
	
    /** The environment variable giving the CoreASM root directory. */
    //private static final String COREASM_ROOT_ENV_VAR = "COREASM_HOME";
    
	/** Holds the absolute path to the root folder, if it is not
	 * defined in the global properties
	 */
	//private static String ROOT_FOLDER = null;
	
	/** If set, defines the root fodler of the engine. */
	public static final String ROOT_FOLDER_PROPERTY = "engine.rootFolder";

	/** Holds global engine properties */
	private static Properties globalProperties = null;
	
	/**
	 * Sets the root folder for the CoreASM engine.
	 * 
	 * @param rootFolder string pointing to the root folder
	 */
	public synchronized static void setRootFolder(String rootFolder) {
		//ROOT_FOLDER = rootFolder;
	}

	/**
	 * Returns general-purpose properties read from a config file.
	 */
	public synchronized static Properties getProperties() {
		if (globalProperties == null) {
			String rootFolder = Tools.getRootFolder();
			globalProperties = getDefaultProperties();
			try {
				FileInputStream stream = new FileInputStream(rootFolder + CONFIG_FOLDER + "/" + KERNEL_CONF_FILE_NAME);
				globalProperties.load(stream);
			} catch (FileNotFoundException e) {
				logger.warn("Kernel configuration file not found. Saving defaults.");
				saveGeneralProperties();
			} catch (IOException e) {
				logger.error("Cannot load kernel configuration file (" + e.getMessage() + ").");
			}
		}
		return globalProperties;
	}

	/**
	 * Returns the value of a general-purpose property
	 * @param key the property
	 * @return value of the property
	 */
	public synchronized static String getProperty(String key) {
		if (globalProperties == null)
			getProperties();
		return globalProperties.getProperty(key);
	}
	
	/**
	 * Sets the value of a general-purpose property
	 * 
	 * @param key the property
	 * @param value value of the property
	 */
	public synchronized static void setProperty(String key, String value) {
		if (globalProperties == null)
			getProperties();
		globalProperties.setProperty(key, value);
	}
	
	/*
	/**
	 * Tries to find the value of root folder. It assumes that
	 * the binary files are located in a <code>[CoreASM]/bin</code> folder. 
	 * /
	private synchronized static void findsRootFolder() {
		// Finding data folder
		String sampleClassFile = "/org/coreasm/util/CoreASMGlobal.class";
		CoreASMGlobal tempObject = new CoreASMGlobal();
		ROOT_FOLDER = tempObject.getClass().getResource(sampleClassFile).toString();
		
		// Mashaal 2005-12-21: to ensure that path with spaces is NOT url encoded
		try
		{
			ROOT_FOLDER = URLDecoder.decode(ROOT_FOLDER,"UTF-8");
		}
		catch (UnsupportedEncodingException e)
		{
			logger.error("UTF-8 Encoding not supported");
		}
		
		if (ROOT_FOLDER.indexOf("file:") > -1) {
			/* old way * /
			// ROOT_FOLDER = ROOT_FOLDER.replaceFirst("file:", "").replaceFirst(BIN_FOLDER + sampleClassFile, "");
			/* new way * /
			ROOT_FOLDER = ROOT_FOLDER.replaceFirst("file:", "").replaceFirst(sampleClassFile, "");
			ROOT_FOLDER = ROOT_FOLDER.substring(0, ROOT_FOLDER.lastIndexOf('/'));
		} 
		if (ROOT_FOLDER.indexOf("jar:") > -1) {
			ROOT_FOLDER = ROOT_FOLDER.replaceFirst("jar:", "");
			ROOT_FOLDER = ROOT_FOLDER.replaceFirst("!" + sampleClassFile, "");
			/* old way * /
			// ROOT_FOLDER = ROOT_FOLDER.substring(0, ROOT_FOLDER.lastIndexOf(BIN_FOLDER)+1);
			/* new way * /
			ROOT_FOLDER = ROOT_FOLDER.substring(0, ROOT_FOLDER.lastIndexOf('/'));
			logger.debug("Root folder is detected as {}.", ROOT_FOLDER);
		}
	}*/
	
	/**
	 * Provides default kernel configuration in form of a <code>Properties</code> object.
	 */
	private static Properties getDefaultProperties() {
		Properties defaults = new Properties();
		defaults.setProperty("kernelName", "CoreASM-Kernel-Alpha");
		/* ADD DEFAULTS HERE! */
		return defaults;
	}

	/** 
	 * Saves general-purpose properties to a file. 
	 */
	private static void saveGeneralProperties() {
		try {
			File configFolder = new File(Tools.getRootFolder() + CONFIG_FOLDER);
			if (!configFolder.exists()) {
				if (!configFolder.mkdirs()) {
					logger.error("Cannot create 'data' folder. Saving kernel configuration is aborted.");
					return;
				}
			}
			FileOutputStream stream = new FileOutputStream(Tools.getRootFolder() + CONFIG_FOLDER + "/" + KERNEL_CONF_FILE_NAME);
			globalProperties.store(stream, "CoreASM Kernel Properties");
			stream.close();
		} catch (FileNotFoundException e) {
			logger.error("Cannot create kernel config file.");			
		} catch (IOException e) {
			logger.error("Cannot write to kernel config file.");			
		}
	}
}
