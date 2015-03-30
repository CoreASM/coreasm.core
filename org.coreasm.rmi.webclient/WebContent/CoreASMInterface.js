/**
 * uses: jQuery 2.x
 */
var EngineId = "";

/**
 * Sends the given command back to the server. Use only on parameterless
 * commands and if the possibly returned data is of no interest. Possible
 * commands with empty returned data: start, stop, pause, step, reset Possible
 * commands with returned data: join, getAgents
 * 
 * @param cmd
 *            Command send to the server
 */
function sendCommand(cmd) {
	$.post("Control", {
		command : cmd,
		engineId : EngineId
	})
}

/**
 * Send command to start the engine identified by the id stored in EngineId
 */
function startASM() {
	sendCommand("start")
}

/**
 * Send command to stop the engine identified by the id stored in EngineId
 */
function stopASM() {
	sendCommand("stop")
}

/**
 * Send command to pause the engine identified by the id stored in EngineId
 */
function pauseASM() {
	sendCommand("pause")
}

/**
 * Send command to the engine identified by the id stored in EngineId to take a
 * single step
 */
function stepASM() {
	sendCommand("step")
}

/**
 * Resets the engine and keeps its specification if keep is 'true';
 * @param keepSpec
 */
function reset(keep) {
	$.post("Control", {
		command : "reset",
		engineId : EngineId,
		keepSpec : keep
	})	
}
/**
 * @param code
 *            CoreASM Code you want to run.
 * @param agent
 *            Name of the agent the code should be run by.
 */
function updateASM(update, agent) {
	$.post("Control", {
		command : "update",
		engineId : EngineId,
		value : update,
		agent : agent
	})
}

/**
 * Applies a new value to a location.
 * 
 * @param location
 *            Location you want to have changed
 * @param value
 *            New Value after change
 */
function changeValue(location, value) {
	$.post("Control", {
		command : "changeValue",
		engineId : EngineId,
		location : location,
		value : value
	})
}

/**
 * Asks for a list of agent names.
 * 
 * @param handler
 *            Receives on success an array containing agent names as Objects
 *            {name: <agentName>}
 */
function getAgents(handler) {
	$.post("Control", {
		command : "getAgents",
		engineId : EngineId
	}, handler, "json");
}

/**
 * Asks for a list of new updates.
 * 
 * @param handler
 *            Receives on success an array containing an array of Objects
 *            {location: <locationString>, value: <valueOfLocation>, action:<executedAction>}
 *            per update produced since the last call
 */
function getUpdates(handler) {
	$.ajax({
		cache : false,
		dataType : "json",
		url : "Updates",
		data : {
			engineId : EngineId
		},
		success : handler
	})
}

/**
 * Asks for a list of new errors.
 * 
 * @param handler
 *            Receives on success an array containing all error messages
 *            produced since the last call
 */
function getErrors(handler) {
	$.ajax({
		cache : false,
		dataType : "json",
		url : "Errors",
		data : {
			engineId : EngineId
		},
		success : handler
	});
}

/**
 * Request a new engine loaded with the given specification. After it returns
 * successfully EngineId will be set.
 * 
 * @param spec
 *            Specification to load as FormData containing an 'spec' named
 *            specification.
 */
function getEngine(spec, handler) {
	$.ajax({
		url : "Control",
		type : 'POST',
		data : spec,
		mimeType : "multipart/form-data",
		contentType : false,
		cache : false,
		processData : false,
		success : function(data) {
			EngineId = data;
			if (handler != null) {
				handler(data);
			}
		}
	});
}

/**
 * Request a to join an existing engine. After it returns
 * successfully EngineId will be set.
 * 
 * @param engineId Id of the engine you want to join
 */
function join(engineId, handler) {
	$.ajax({
		url : "Control",
		type: 'POST',		
		data : {
			command : "join",
			engineId : engineId
		},
		success : function(data) {
			EngineId = data;
			if (handler != null) {
				handler(data);
			}
		}
	});
}
