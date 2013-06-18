package org.coreasm.engine.informationHandler;

import java.util.Date;

class DispatcherContext {

	final String action;
	final InformationObject info;

	final Date timestamp;

	public DispatcherContext(InformationObject info, String action) {

		/** elementary data of the InformationObject */
		this.action = action;
		this.info = info;

		/** additional helpful data of the information object */
		this.timestamp = new Date();
	}

	public Date getTimeStamp() {
		return this.timestamp;
	}

	public String getAction() {
		return this.action;
	}

	public InformationObject getInformation() {
		return this.info;
	}

}