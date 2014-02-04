package org.coreasm.util.information;

public interface InformationObserver {

	void informationCreated(InformationObject information);

	void clearInformation();
}
