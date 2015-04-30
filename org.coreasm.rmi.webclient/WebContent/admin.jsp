<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>WebCoreASM - Administration</title>
<script src="//ajax.googleapis.com/ajax/libs/jquery/2.1.1/jquery.min.js"></script>
<script type="text/javascript">
	$(document).ready(function() {
		$("#ul1").on("click", "button", function (){
			$.post("Control", {
				command : "stop",
				engineId : $(this).val()
			})
	    });
		$.ajaxSetup({
			// Disable caching of AJAX responses
			cache : false
		});
		function count() {
			$("#ul1").load("AdminControl");
		}

		var auto_refresh = setInterval(function() {
			count()
		}, 10000);

		count();
	});
</script>
</head>
<body>
	<p>Enginelist</p>
	<div id="div1">
		<ul id="ul1"></ul>
	</div>
</body>
</html>