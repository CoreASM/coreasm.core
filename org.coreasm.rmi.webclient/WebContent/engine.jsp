<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Insert title here</title>
<script src="//ajax.googleapis.com/ajax/libs/jquery/2.1.1/jquery.min.js"></script>
<script type="text/javascript">
	$(document).ready(
	function() {
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
						var update = data[i];
						var newUpdatesHTML = "<li>";
						newUpdatesHTML += "<table border=\"1\" style=\"width:100%\">";
						for (j = 0; j < update.length; j++) {
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
						}
						newUpdatesHTML += "</table>";
						newUpdatesHTML += "</li>";
						/* var newUpdatesHTML = "";
						for (j=0; j<update.length; j++) {
							if(update[j].location == "output()") {
								newUpdatesHTML += "<li>";
								newUpdatesHTML += update[j].value;
								newUpdatesHTML += "</li>";
							}
						} */
						$("#ul1").append(newUpdatesHTML);
					}
				}
			});
		}

		var auto_refresh = setInterval(function() {
			update()
		}, 500);

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
				location : $("#location").val(),
				value :  $("#value").val()
			})
		});

	});

</script>
</head>
<body>
	<p>Engine ID: ${requestScope.EngineId}</p>
	<button class="command" value="start" type="button">Start</button>
	<button class="command" value="stop" type="button">Stop</button>
	<button class="command" value="pause" type="button">Pause</button>
	<div id="updateDiv">
		<input type="text" id="location" />
		<input type="text" id="value" />
		<button type="button" id="updateBtn" type ="button"> Update </button>
	</div>
	<p>Updates</p>
	<div id="div1">
		<ul id="ul1"></ul>
	</div>
</body>
</html>