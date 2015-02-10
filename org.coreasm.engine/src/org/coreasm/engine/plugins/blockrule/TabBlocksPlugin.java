/*  
 * TabBlocksPlugin.java    1.0     03-May-2006
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

package org.coreasm.engine.plugins.blockrule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.coreasm.engine.CoreASMEngine.EngineMode;
import org.coreasm.engine.SpecLine;
import org.coreasm.engine.VersionInfo;
import org.coreasm.engine.plugin.ExtensionPointPlugin;
import org.coreasm.engine.plugin.Plugin;
import org.coreasm.util.Tools;

/** 
 * A plugin that eliminates the need to have 'par' and 'endpar'. It produces a pair 
 * of 'par' and 'endpar' for every tabbed indent.  
 *   
 *  @author  Roozbeh Farahbod
 *  
 */

public class TabBlocksPlugin extends Plugin implements ExtensionPointPlugin {

	public static final VersionInfo VERSION_INFO = new VersionInfo(0, 3, 0, "alpha");

	private Map<EngineMode, Integer> targetModes = null;
	
	/**
	 * @return {{@link EngineMode#emParsingSpec} -> 10}.
	 */
    public Map<EngineMode, Integer> getTargetModes() {
    	if (targetModes == null) {
    		targetModes = new HashMap<EngineMode, Integer>();
    		targetModes.put(EngineMode.emParsingSpec, 10);
    	}
    	return targetModes;
    }

    public Map<EngineMode, Integer> getSourceModes() {
        return Collections.emptyMap();
    }

    @Override
    public void initialize() {
    }

    private boolean preserveLineNumbers() {
    	return false;
    }

    public void fireOnModeTransition(EngineMode source, EngineMode target) {
    	boolean ruleReached = false;
    	
    	if (target == EngineMode.emParsingSpec) {
//    		Logger.log(Logger.WARNING, Logger.plugins, "TabBlockPlugin started modifying the spec...");
    		System.out.println("** TabBlockPlugin :  started modifying the spec...");
    		List<SpecLine> spec = capi.getSpec().getLines();
    		ArrayList<SpecLine> newSpec = new ArrayList<SpecLine>();
    		
    		int currentTabs = 0;
    		int lineTabs = 0;
    		SpecLine prevLine = null;
    		boolean presLNo = preserveLineNumbers();
    		for (SpecLine line: spec) {
    			if (line.text.indexOf("rule ") == 0) 
    				ruleReached = true;
    			
    			if (ruleReached) {
	    			lineTabs = countTabs(line.text);
	    			
	    			// ignore empty lines
	    			if (lineTabs == -1)
	    				lineTabs = currentTabs;
	    			
	    			if (lineTabs > currentTabs) {
	    				for (int j=0; j < (lineTabs - currentTabs); j++) {
	    					if (presLNo) 
	    						prevLine = new SpecLine(prevLine + " par", prevLine.fileName, prevLine.line);
	    					else
	    						newSpec.add(new SpecLine(produceTabs(currentTabs + j) + "par", "", 0));
	    				}
	    			}
	    			
	    			if (lineTabs < currentTabs) {
	    				for (int j=0; j < (currentTabs - lineTabs); j++) {
	    					if (presLNo)
	    						prevLine = new SpecLine(prevLine + " endpar", prevLine.fileName, prevLine.line);
	    					else
	    						newSpec.add(new SpecLine(produceTabs(currentTabs - j - 1) + "endpar", "", 0));
	    				}
	    			}
    			}
    			if (presLNo && prevLine != null) 
    				newSpec.set(newSpec.size()-1, prevLine);
    			newSpec.add(line);
    			currentTabs = lineTabs;
    			prevLine = line;
    		}
    		
    		for (int i=0; i < currentTabs; i++)
				newSpec.add(new SpecLine(produceTabs(i) + "endpar", "", 0));
    			
    		capi.getSpec().updateLines(newSpec);

    		/**/
    		System.out.println("** TabBlockPlugin :  Specification is modified as follows:");
    		System.out.println("** TabBlockPlugin :  -------------------------------------");
    		int i = 1;
    		for (SpecLine line: newSpec) {
        		System.out.println("** TabBlockPlugin : " + Tools.lFormat(i, 3) + "  " + line.text);
        		i++;
    		}
    		System.out.println("** TabBlockPlugin :  -------------------------------------");
    		/**/
    	}
    }
    
    /*
     * Returns the number of tabs in the line.
     * If the line is empty, returns -1.
     */
    private int countTabs(String str) {
    	int i = 0;
    	while (i < str.length() && str.charAt(i) == '\t') {
    		i++;
    	}
    	if (str.trim().length() == 0)
    		i = -1;
    	
    	return i;
    }

    private String produceTabs(int i) {
    	StringBuffer str = new StringBuffer();
    	while (i > 0) {
    		str.append('\t');
    		i--;
    	}
    	return str.toString();
    }

	public VersionInfo getVersionInfo() {
		return VERSION_INFO;
	}

}
