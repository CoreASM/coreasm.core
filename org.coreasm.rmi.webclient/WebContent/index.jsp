<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
<head>
<script src="//ajax.googleapis.com/ajax/libs/jquery/2.1.1/jquery.min.js"></script>
<script type="text/javascript">
	$(document).ready(function() {
		$("#specSubmit").submit(function(e) {
			var formObj = $(this);
			var formData = new FormData(this);
			$.ajax({
				url : "Control",
				type : 'POST',
				data : formData,
				mimeType : "multipart/form-data",
				contentType : false,
				cache : false,
				processData : false
			});
			e.preventDefault();
		});
	});
</script>
</head>
<body>

	<form id="specSubmit" action="Control" method="post"
		enctype="multipart/form-data">
		<input type="file" name="file" /> <input type="submit" />
	</form>
	<form action="Control" method="post">
		<input type="text" name="engineId" /> <input type="hidden"
			name="command" value="join" /> <input type="submit" />
	</form>
</body>
</html>