package org.coreasm.engine.informationHandler;

public interface IInformationDispatchObserver {

	void informationCreated(InformationObject information);

	void clearInformation();
}
