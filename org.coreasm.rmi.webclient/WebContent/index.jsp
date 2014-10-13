<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
<head>
<script
	src="http://ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js"></script>
<script>
	$(document).ready(function() {
		function count() {
			$('#ul1').append($('<li>').load('Updates'));
		}
		var auto_refresh = setInterval(function() {
			count()
		}, 5000);
		count();
	});
</script>
</head>
<body>
	<form action="Control" method="post" enctype="multipart/form-data">
		<input type="file" name="file" />
		<input type="submit" />
	</form>
	<form action="Control" method="post" >
		<button name="command" value="start" type="submit">Start</button>
		<button name="command" value="stop" type="submit">Stop</button>
		<button name="command" value="pause" type="submit">Pause</button>
	</form>
	<p>Updates</p>
	<div id="div1">
		<ul id="ul1"></ul>
	</div>
</body>
</html>