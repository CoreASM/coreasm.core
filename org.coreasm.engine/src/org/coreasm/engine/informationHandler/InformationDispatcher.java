package org.coreasm.engine.informationHandler;

import java.util.HashMap;

public class InformationDispatcher extends AbstractDispatcher {

	static HashMap<String, InformationDispatcher> infoDispatcher  = new HashMap<String, InformationDispatcher>();;

	/**
	 * @param stackTrace e.g. the plugin which will distribute some error information
	 */
	private InformationDispatcher(String sourceId) {
		super();
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
	public static void addObserver(IInformationDispatchObserver observer){
		for ( InformationDispatcher disp : infoDispatcher.values()) {
			((AbstractDispatcher)disp).addSuperObserver(observer);
		}
	}

	/** remove observer from all InformationDispatchers */
	public static void deleteObserver(IInformationDispatchObserver observer){
		for ( InformationDispatcher disp : infoDispatcher.values()) {
			((AbstractDispatcher)disp).deleteSuperObserver(observer);
		}
	}

	/** remove all observers from all InformationDispatchers */
	public static void deleteObservers(){
		for ( InformationDispatcher disp : infoDispatcher.values()) {
			((AbstractDispatcher)disp).deleteSuperObervers();
		}
	}
}
