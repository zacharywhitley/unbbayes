<%@page errorPage="" %>
<html>
<head>
<title>Sucesso</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
</head>

<body>
<h2>Lançamento processado com sucesso!</h2>
<form name="form1" method="post" action="<%=request.getContextPath()%>/log.jsp">
  <input type="submit" name="Submit" value="Voltar">
</form>
<p>&nbsp;</p>

</body>
</html>
