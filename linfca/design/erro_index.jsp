<%@page import="java.io.*"
		isErrorPage="true"  %>
<%@include file="/util.jsp" %> 
<html>
<body onLoad="javascript:document.form1.submit()">
        <% exception.printStackTrace(); %>
	<form name="form1" method="post" action="<%=request.getContextPath()%>/index.jsp">
	    <input type="hidden" name="mensagem" value="<%=exception.getMessage()%>">
            <input type="hidden" name="erro" value="ERRO">
	</form>
</body>
</html>

