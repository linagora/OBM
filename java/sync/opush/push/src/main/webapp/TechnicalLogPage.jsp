<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@page import = "java.util.*" %>
<%@page import = "org.obm.push.bean.jaxb.*" %>
<%@page import = "org.obm.push.bean.jaxrs.*" %>
<%@page import = "org.joda.time.format.DateTimeFormatter" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<title>O-push Technical Log</title>
</head>
<body>
	<% Map it = (Map) request.getAttribute("it"); %>
	<% List<LogFile> logsList = (List<LogFile>) it.get("logFiles"); %>
	<% boolean active = (Boolean) it.get("appenderActive"); %>
	<% DateTimeFormatter dateTimeFormatter = (DateTimeFormatter) it.get("dateTimeFormatter"); %>

	<h1>Log Status</h1>	 
	<p id="activeField">Active : <%= active %></p>
	<script type="text/javascript">
		function action() {
			location.href='/TechnicalLog/status?status=' + <%= !active %>;
		}
	</script>
	
	<button type="button" id="statusButton"
		onclick="action()">
		<%= (active) ? "Stop" : "Start" %> Log
	</button>
	<br/>
	
	<h1>Logs Available</h1>
	<select id="selector" size="1">
		<% for (LogFile logFile : logsList) { %>
			<option><%= dateTimeFormatter.print(logFile.getDate()) %>
		<% } %>
	</select>
	
	<script type="text/javascript">
		function redirectTo(){
			var selectedElement = document.getElementById('selector')[document.getElementById('selector').selectedIndex].label;
			return '/TechnicalLog/File?selected=' + selectedElement;
		}
	</script>
	
	<button type="button" 
		onclick="location.href=redirectTo()">
		Load selected file
	</button>
</body>
</html>
