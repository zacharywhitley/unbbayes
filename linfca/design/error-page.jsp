<%@page isErrorPage="true" %>
<html>
<head>
<title>Erro!</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
</head>
<body>
   O seguinte erro foi encontrado:<BR>
 <b><%= exception %></b>
 <% exception.printStackTrace(out); %>
</body>
</html>
