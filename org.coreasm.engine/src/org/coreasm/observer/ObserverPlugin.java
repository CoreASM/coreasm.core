/*
 * ObserverPlugin.java 		$Revision: 113 $
 * 
 * Copyright (c) 2008 Roozbeh Farahbod
 *
 * Last modified on $Date: 2009-12-15 20:17:04 +0100 (Di, 15 Dez 2009) $  by $Author: rfarahbod $
 * 
 * Licensed under the Academic Free License version 3.0 
 *   http://www.opensource.org/licenses/afl-3.0.php
 *   http://www.coreasm.org/afl-3.0.php
 *
 */


package org.coreasm.observer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.coreasm.engine.CoreASMEngine;
import org.coreasm.engine.CoreASMEngine.EngineMode;
import org.coreasm.engine.Specification;
import org.coreasm.engine.VersionInfo;
import org.coreasm.engine.absstorage.AbstractUniverse;
import org.coreasm.engine.absstorage.Location;
import org.coreasm.engine.absstorage.Update;
import org.coreasm.engine.plugin.ExtensionPointPlugin;
import org.coreasm.engine.plugin.InitializationFailedException;
import org.coreasm.engine.plugin.Plugin;
import org.coreasm.util.Logger;
import org.coreasm.util.Tools;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * Observer Plugin observes the simulation of the engine and produces an XML report of the 
 * initial state and updateset computed in every step. See the user manual for more details. 
 *   
 * @author Roozbeh Farahbod, 2008
 */

public class ObserverPlugin extends Plugin implements ExtensionPointPlugin {

	public static final String PLUGIN_NAME = ObserverPlugin.class.getSimpleName();
	public static final VersionInfo VERSION_INFO = new VersionInfo(0, 1, 0, "beta");

	/** Engine property name for the list of locations that observer should monitor */
	public static final String OBSERVER_LOCATIONS_OF_INTEREST = "Observer.LocationsOfInterest";

	/** Engine property name for the number of steps after which the output should be written to a file */
	public static final String OBSERVER_STEP_INTERVAL = "Observer.StepInterval";
	
	/** Engine property name for the name of the output file */
	public static final String OBSERVER_OUTPUT_FILE = "Observer.OutputFile";
	
	/** Default value of the output file name */
	private static final String OBSERVER_DEFAULT_OUTPUT_FILE = "observer-output.xml";
	
	protected Map<EngineMode, Integer> targetModes = null;
	protected Document output = null;
	protected Element coreasmrun = null;
	protected Transformer transformer = null;
	protected String locationListProperty = null;
	protected List<String> locationList = null;
	protected String specDir = null;
	protected String outputFileName = null;
	protected String outputFileNameProperty = null;
	
	int stepCounter = 0;
	
	@Override
	public void initialize() throws InitializationFailedException {
		try {
			stepCounter = 0;
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			output = builder.newDocument();
			coreasmrun = output.createElement("coreasmrun");
			output.appendChild(coreasmrun);
			TransformerFactory tFactory = TransformerFactory.newInstance();
			transformer = tFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");

			Specification spec = capi.getSpec();
			specDir = spec.getFileDir();
			
		} catch (ParserConfigurationException e) {
			throw new InitializationFailedException(this, "Cannot create an XML document.", e);
		} catch (TransformerConfigurationException e) {
			throw new InitializationFailedException(this, "Cannot create an XML transformer.", e);
		}
	}

	public VersionInfo getVersionInfo() {
		return VERSION_INFO;
	}

	public void fireOnModeTransition(EngineMode src, EngineMode target) {
		if (target.equals(CoreASMEngine.EngineMode.emStepSucceeded)) {
			ensureLocationListIsLoaded();
			
			// first things to do
			if (stepCounter == 0) {
				Comment comment = output.createComment("Locations of interest: " + locationList);
				coreasmrun.appendChild(comment);

				/*
				 * Print a copy of the initial state only if this is the first step 
				 * and user provided a list of locations to be monitored. 
				 */
				if (locationList != null && locationList.size() > 0) {
					coreasmrun.appendChild(state2XML());
				}
			}
			

			Element step = output.createElement("step");
			step.setAttribute("systime", String.valueOf(System.currentTimeMillis()));
			Element updateSet = getUpdateSetXML();
			// append the state only if there is any interesting update there
			if (updateSet.getFirstChild() != null) {
				step.appendChild(getUpdateSetXML());
				coreasmrun.appendChild(step);
			}

			stepCounter++;
		}
	}

	public void terminate() {
		setFileName();
		try {
			FileOutputStream stream = new FileOutputStream(outputFileName);
			DOMSource source = new DOMSource(output);
			StreamResult result = new StreamResult(stream);
			transformer.transform(source, result);
			stream.close();
		} catch (FileNotFoundException e1) {
			Logger.log(Logger.ERROR, Logger.plugins, "Cannot create the XML file.");
			Logger.log(Logger.ERROR, Logger.plugins, e1.getMessage());
		} catch (TransformerException e) {
			Logger.log(Logger.ERROR, Logger.plugins, "Cannot write the XML file.");
			Logger.log(Logger.ERROR, Logger.plugins, e.getMessage());
		} catch (IOException e) {
			Logger.log(Logger.ERROR, Logger.plugins, "Cannot close the XML file.");
			Logger.log(Logger.ERROR, Logger.plugins, e.getMessage());
		}
	}
	
	public Map<EngineMode, Integer> getSourceModes() {
		return Collections.emptyMap();
	}

	public Map<EngineMode, Integer> getTargetModes() {
		if (targetModes == null) {
			targetModes = new HashMap<EngineMode, Integer>();
			targetModes.put(CoreASMEngine.EngineMode.emStepSucceeded, ExtensionPointPlugin.DEFAULT_PRIORITY);
		}
		return targetModes;
	}

	/*
	 * Creates an XML element of the state. 
	 */
	private Element state2XML() {
		Element state = output.createElement("state");
		state.setAttribute("systime", String.valueOf(System.currentTimeMillis()));
		//AbstractStorage storage = capi.getStorage();
		
		/*
		 * Add universes
		 */
		//Map<String,AbstractUniverse> universes = storage.getUniverses();
		//for (Entry<String, AbstractUniverse> e: universes.entrySet()) {
			
			// TODO INCOMPLETE!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
			
		//}
				
		return state;
	}
	
	/*
	 * Loads the names of the locations of interest from its corresponding
	 * engine property. 
	 */
	private void ensureLocationListIsLoaded() {
		if (locationListProperty == null) {
			// This is the first time we are here
			locationListProperty = capi.getProperty(OBSERVER_LOCATIONS_OF_INTEREST);
			locationListProperty = Tools.trimDoubleQuotes(locationListProperty);

			if (locationListProperty != null) {
				/*
				 * if the user specified any value for the locations of interest,
				 * then locationList should be initialized. As a result, by
				 * setting an empty string as the value of locations of 
				 * interest property, monitoring is turned off. 
				 */ 
				locationList = new ArrayList<String>();
				StringTokenizer tokenizer = new StringTokenizer(locationListProperty, " ");
				while (tokenizer.hasMoreElements()) {
					locationList.add(tokenizer.nextToken());
				}
			} else {
				locationListProperty = "";
				locationList = null;
			}
		}
	}
	
	/*
	 * Sets the name of the output file
	 */
	private void setFileName() {
		String temp = capi.getProperty(OBSERVER_OUTPUT_FILE);
		
		// if there is no user-defined value
		if (temp == null) {
			outputFileNameProperty = null;
			outputFileName = OBSERVER_DEFAULT_OUTPUT_FILE;
			if (specDir != null)
				outputFileName = specDir + File.separator + outputFileName;
		} else {
		// if there is a user-defined value

			// i.e., if the engine property is changed since last time checked
			if (outputFileNameProperty == null || !temp.equals(outputFileNameProperty)) {
				temp = Tools.trimDoubleQuotes(temp);
				outputFileNameProperty = temp;
				outputFileName = temp;
				if (specDir != null) {
					File f = new File(outputFileName);
					if (!f.isAbsolute())
						outputFileName = specDir + File.separator + outputFileName;
				}
			}
		}
	}

	/*
	 * Creates an XML subtree that represents the last update-set
	 */
	private Element getUpdateSetXML() {
		Element result = output.createElement("updateset");
		
		Set<Update> updateset = capi.getUpdateSet(0);
		if (updateset.size() > 0) {
			for (Update u: updateset) {
				if (locationList == null || locationList.contains(u.loc.name)) {
					Element updateElement = output.createElement("update");
					for (org.coreasm.engine.absstorage.Element a: u.agents) {
						updateElement.appendChild(agentToXML(a));
					}
					updateElement.appendChild(locationToXML(u.loc));
					updateElement.appendChild(valueToXML(u.value));
					result.appendChild(updateElement);
				}
			}
		}
		
		return result;
	}

	/*
	 * Returns an XML representation of the given location.
	 */
	private Element locationToXML(Location loc) {
		Element result = output.createElement("location");
		result.setAttribute("name", loc.name);
		int i = 1;
		for (org.coreasm.engine.absstorage.Element arg: loc.args) {
			Element argElement = output.createElement("arg");
			argElement.setAttribute("index", String.valueOf(i));
			argElement.appendChild(valueToXML(arg));
			result.appendChild(argElement);
		}
		return result;
	}
	
	/*
	 * Returns an XML representation of the given agent.
	 */
	private Element agentToXML(org.coreasm.engine.absstorage.Element agent) {
		Element result = output.createElement("agent");
		result.appendChild(valueToXML(agent));
		return result;
	}
	
	/*
	 * Returns an XML representation of the given value. 
	 */
	private Element valueToXML(org.coreasm.engine.absstorage.Element value) {
		Element result = output.createElement("value");
		result.setAttribute("type", value.getClass().getName());
		AbstractUniverse bkg = capi.getStorage().getUniverse(value.getBackground());
		result.setAttribute("background", bkg.getClass().getName());
		result.setTextContent(value.denotation());
		return result;
	}

}

