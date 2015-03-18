/**
 * 
 */
package org.coreasm.rmi.server.remoteinterfaces;

import java.io.Serializable;

import org.coreasm.rmi.server.remoteinterfaces.EngineControl.EngineDriverStatus;

/**
 * @author Stephan
 *
 */
public class EngineDriverInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	private String id;
	private EngineDriverStatus status;
	
	public EngineDriverInfo(String engineId, EngineDriverStatus engineStatus) {
		setId(engineId);
		setStatus(engineStatus);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public EngineDriverStatus getStatus() {
		return status;
	}

	public void setStatus(EngineDriverStatus status) {
		this.status = status;
	}
}
