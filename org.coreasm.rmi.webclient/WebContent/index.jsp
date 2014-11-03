<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
<head>
</head>
<body>
	<form action="Control" method="post" enctype="multipart/form-data">
		<input type="file" name="file" />
		<input type="submit" />
	</form>
	<form action="Control" method="post">
		<input type="text" name="engineId" />
		<input type="hidden" name="command" value="join" />
		<input type="submit" />
	</form>
</body>
</html>