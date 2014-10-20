<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Insert title here</title>
<script src="//ajax.googleapis.com/ajax/libs/jquery/2.1.1/jquery.min.js"></script>
<script type="text/javascript">
	$(document).ready(function() {
		function count() {

			$.ajax({
				cache : false,
				url : "Updates",
				success : function(data) {
					$("#ul1").append(data);
				}
			});
		}

		var auto_refresh = setInterval(function() {
			count()
		}, 5000);

		count();

		$(".command").click(function() {
			$.post("Control", {
				command : $(this).val()
			})
		});

	});
</script>
</head>
<body>
	<button class="command" value="start" type="submit">Start</button>
	<button class="command" value="stop" type="submit">Stop</button>
	<button class="command" value="pause" type="submit">Pause</button>
	<p>Updates</p>
	<div id="div1">
		<ul id="ul1"></ul>
	</div>
</body>
</body>
</html>