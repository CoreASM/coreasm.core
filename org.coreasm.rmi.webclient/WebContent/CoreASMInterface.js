/**
 * 
 */
var EngineId

function sendCommand(cmd) {
	$.post("Control", {
		command : cmd,
		engineId : EngineId
	})
}


function startASM() {
	sendCommand("start")
}
function stopASM() {
	sendCommand("stop")
}
function pauseASM() {
	sendCommand("pause")
}
function stepASM() {
	sendCommand("step")
}
function updateASM(update, agent) {
	$.post("Control", {
		command : "update",
		engineId : EngineId,
		value : update,
		agent : agent
	})
}

function getAgents(handler) {
	$.post("Control",					
			{
				command : "getAgents",
				engineId : EngineId
			},
			handler,
			"json"); 
}

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