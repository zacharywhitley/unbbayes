<%@page import="linfca.*, 
        java.sql.*, 
        org.jdom.Element, 
        java.util.Iterator" 
        errorPage="" %> 
        
<%

   out.println(request.getParameter("cod-usuario"));
   out.println(request.getParameter("cod-computador"));

%>

<html>
<head>
</head>

<body onLoad="javascript:document.form1.submit()">
  <form name="form1" method="post" action="log.jsp">
</body>
</html>
