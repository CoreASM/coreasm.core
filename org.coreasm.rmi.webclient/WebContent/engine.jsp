<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Insert title here</title>
<script src="//ajax.googleapis.com/ajax/libs/jquery/2.1.1/jquery.min.js"></script>
<script type="text/javascript">
	var states = [];
	var agents = [];
	
	function refreshStateTable(stateNr) {
		if (stateNr < states.length && stateNr > -1) {
			state2Print = states[stateNr];
			var output = ""
			for (var property in state2Print) {
			    if (state2Print.hasOwnProperty(property)) {
			       output += "<tr><td>" + property + "</td><td>" + state2Print[property] + "</td></tr>"
			    }
			}
			$("#stateTable").html(output);
		}
	};
	
	$(document).ready(
		function() {
			states[0] = {}
			$.ajaxSetup({
				cache : false
			});
			
			function update() {
				$.ajax({
					cache : false,
					dataType : "json",
					url : "Updates",
					data : {
						engineId : "${requestScope.EngineId}"
					},
					success : function(data) {						
						for (i = 0; i < data.length; i++) {
							var newState = JSON.parse(JSON.stringify(states[states.length-1]));
							var update = data[i];
							var newUpdatesHTML = "<li>";
							var newOutputHTML = "";
							var loc;
							newUpdatesHTML += "<table border=\"1\" style=\"width:100%\">";
							for (j = 0; j < update.length; j++) {
								loc = update[j].location.replace("(", "_");
								loc = loc.replace(")", "_");
								newState[loc] = update[j].value;
								newUpdatesHTML += "<tr>";
								newUpdatesHTML += "<td>"
										+ update[j].location
										+ "</td>";
								newUpdatesHTML += "<td>"
										+ update[j].value
										+ "</td>";
								newUpdatesHTML += "<td>"
										+ update[j].action
										+ "</td>";
								newUpdatesHTML += "</tr>";
								if (update[j].location == "output()") {
									newOutputHTML += "<li>";
									newOutputHTML += update[j].value;
									newOutputHTML += "</li>";
								}
							}
							
							var newOption = new Option(states.length, states.length);
							$("#stateSelect").append(newOption);
							states[states.length] = newState;
							
							
							newUpdatesHTML += "</table>";
							newUpdatesHTML += "</li>";
							$("#updateList").prepend(
									newUpdatesHTML);
							if (newOutputHTML.length > 0) {
								$("#outputList").prepend(
										newOutputHTML);
							}
							
						}
						if(data.length > 0) {
							refreshStateTable(states.length-1);
						}
					}
				});
				
				$.ajax({
					cache : false,
					dataType : "json",
					url : "Errors",
					data : {
						engineId : "${requestScope.EngineId}"
					},
					success : function(data) {						
						for (i = 0; i < data.length; i++) {
							var newErrorsHTML = "<li>" + data [i] + "</li>";
							$("#errorList").prepend(
									newErrorsHTML);							
						}
					}
				});
				
				$.post("Control",					
					{
						command : "getAgents",
						engineId : "${requestScope.EngineId}"
					},
					function(data) {
						if(data.length > 0) {
							$("#agentList").empty();
							for (i = 0; i < data.length; i++) {
								$("#agentList").append("<li> Agent" + (i+1) + ": " + data[i].name + "</li>");			
							}
						}
					},
					"json"); 
			}
			
 			 

			var auto_refresh = setInterval(function() {
				update()
			}, 1000);

			update();

			$(".command").click(function() {
				$.post("Control", {
					command : $(this).val(),
					engineId : "${requestScope.EngineId}"
				})
			});

			$("#updateBtn").click(function() {
				$.post("Control", {
					command : "update",
					engineId : "${requestScope.EngineId}",
					value : $("#value").val(),
					agent : $("#agent").val()
				})
			});
			
			$("#selectBtn").click(function() {
				refreshStateTable($("#stateSelect")[0].selectedIndex);	
			});

		});
</script>
</head>
<body>
	<p>Engine ID: ${requestScope.EngineId}</p>
	<button class="command" value="start" type="button">Start</button>
	<button class="command" value="stop" type="button">Stop</button>
	<button class="command" value="pause" type="button">Pause</button>
	<button class="command" value="step" type="button">Step</button>
	<div id="updateDiv">
		<label for="agent">Agent:</label> <input type="text" id="agent" /> <label for="value">Code:</label> <input type="text" id="value" />
		<button type="button" id="updateBtn">Update</button>
	</div>

	<div id="errors" style="width: 100%; height: 50px; overflow: auto">
		<ul id="errorList"></ul>
	</div>

	<div>
		<select id="stateSelect">
			<option value="0">0</option>
		</select>
		<button type="button" id="selectBtn">Select</button>
		<table id="stateTable" border="1" style="width: 100%"></table>
	</div>
	<div id="agents"
			style="height: 50px; overflow: auto">
			<ul id="agentList"></ul>
	</div>
	<div>
		<div id="updates"
			style="float: left; width: 50%; height: 500px; overflow: auto">
			<ul id="updateList"></ul>
		</div>
		<div id="output"
			style="float: left; width: 50%; height: 500px; overflow: auto">
			<ul id="outputList"></ul>
		</div>
	</div>
</body>
</html>