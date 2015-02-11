package org.coreasm.eclipse.preferences;

import org.coreasm.engine.EngineProperties;

/**
 * Constant definitions for plug-in preferences
 */
public class PreferenceConstants {

	// von Markus: Habe long-Variable "prefChanged" ersetzt mit boolean-Variable "flagPrefChanged"
	// (zur Vermeidung von mehrfachem Erzeugen neuer Engines beim Start)
	//private static volatile long prefChanged = 0;
	private static volatile boolean flagPrefChanged = false;
	
	public static final String ROOT_FOLDER = "rootFolder";

	public static final String ADDITIONAL_PLUGINS_FOLDERS = EngineProperties.PLUGIN_FOLDERS_PROPERTY;

	public static final String MAX_PROCESSORS = EngineProperties.MAX_PROCESSORS;
	
	//Strings used in ASMEditor for default values of bracket highlighting
	public final static String EDITOR_MATCHING_BRACKETS = "matchingBrackets";	
	public final static String EDITOR_MATCHING_BRACKETS_COLOR= "matchingBracketsColor";
	
	// Modifiziert zur Nutzung von flagPrefChanged (s.o.)
	public static synchronized void setDirtyBit(boolean isDirty) {
		//prefChanged = (new Date()).getTime();
		//System.out.println("setDirtyBit() " + prefChanged + " - " + (new Date(prefChanged)).toString() );
		flagPrefChanged = isDirty;
	}
	
// Ersetzt durch unten stehende Methode zur Nutzung von flagPrefChanged (s.o.)
//	public static synchronized boolean isPrefChanged(long time) {
//		if (prefChanged == 0)
//			setDirtyBit();
//		System.out.println("isPrefChanged() - " + time + " - " + prefChanged + " = " + (time-prefChanged));
//		return (time - 500) < prefChanged;
//	}
	
	public static synchronized boolean isPrefChanged() {
		return flagPrefChanged;
	}
}
