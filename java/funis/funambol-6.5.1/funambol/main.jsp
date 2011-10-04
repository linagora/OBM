<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">

<% 
   String main = request.getParameter("main"); 
   if (main == null || main.equals("")) {
%>
       <jsp:forward page='index.html'/>
<%
   }

%> 


<html>
<head>
  <title>The Funambol Data Synchronization Server</title>
  
  <meta name    = "keywords"
        content = "syncml,server syncml,syncml.org, data synchronization,palm,pda,phone,truesync,
                   wireless,mobile,open source,nokia,ericsson,motorola,j2me,wap,bluetooth,3g,gprs,gsm,device management,
                   application provisioning">
<link rel="stylesheet" href="css/home.css" type="text/css">
</head>

<body>

<table border="0" cellspacing="0" cellpadding="0" width="100%">
<tbody>
<tr>
<td valign="top" align="center">
<div align="center"><a href="http://www.funambol.com" TARGET="funambol_website"><img src="imgs/funambol.gif" alt="Funambol Data Synchronization Server" width="146" height="118" border="0"></a><br>
</td>
</tr>
<jsp:include page='/admin/version.html'/>
<tr>
<td>
<br>
<div align="center"><a href="/webdemo">Web Demo Client</a>
</td>
</tr>
</tbody>  
</table> 
</body>
</html>
<!-- $Id: main.jsp,v 1.1 2007/07/04 09:41:47 luigiafassina Exp $ -->
