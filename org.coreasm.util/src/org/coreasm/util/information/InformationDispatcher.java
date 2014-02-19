package org.coreasm.util.information;

import java.util.HashMap;

/**
 * @author Marcel Dausend
 *
 */
public class InformationDispatcher extends AbstractDispatcher {

	static HashMap<String, InformationDispatcher> infoDispatcher = new HashMap<String, InformationDispatcher>();

	/**
	 * @param stackTrace e.g. the plugin which will distribute some error information
	 */
	private InformationDispatcher(String sourceId) {
		super(sourceId);
		if ( infoDispatcher != null )
			infoDispatcher.put(sourceId, this);
	}

	/**
	 * used to send information to all observers
	 *
	 * @param informationSource
	 * @return informationDispatcher
	 */
	public static InformationDispatcher getInstance(String sourceId){

		if (sourceId != null &&
				infoDispatcher.get(sourceId) == null )
			new InformationDispatcher(sourceId);

		return infoDispatcher.get(sourceId);
	}

	/** register observer to all InformationDispatchers */
	public static void addObserver(InformationObserver observer){
		addSuperObserver(observer);
	}

	/** remove observer from all InformationDispatchers */
	public static void deleteObserver(InformationObserver observer){
		deleteSuperObserver(observer);
	}

	/** remove all observers from all InformationDispatchers */
	public static void deleteObservers(){
		deleteSuperObervers();
	}
}
