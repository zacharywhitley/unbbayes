<%@page import="linfca.*, 
        java.sql.*, 
        org.jdom.Element, 
        java.util.Iterator" 
        errorPage="" %> 
<html>
<head>
<title>LinfCA - Selecionar Computador</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
</head>
<body bgcolor="#FFFFFF" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
<br>
<form name="form1" method="post" action="<%=request.getContextPath()%>/controle/computador/lancar-computador-exec.jsp">
  <table width="100%" border="0" cellspacing="0" cellpadding="1" bgcolor="#000000">
  
    <tr valign="top"> 
    
      <td> <table width="100%" border="0" cellspacing="0" cellpadding="4" bgcolor="#CCCCCC">
          <font color="#FFFFFF">
             <%= "Olá " + request.getAttribute("nome-usuario") %>
          </font>
        </table></td>
        
    </tr>
    
    <tr valign="middle">
    
      <td> <table width="100%" border="0" cellspacing="0" cellpadding="4" bgcolor="#FFFFFF">
          <select name="cod_computador_disponivel" id="tipo_computador_disponivel">
          <% 
		     Feature  listarComputadorDisponivel = new ListarComputadorDisponivelFeature();
			 Element tiposXML = listarComputadorDisponivel.process(null);
			 Iterator tipos = tiposXML.getChildren().iterator();
			 while (tipos.hasNext()) {
		  	    Element tipo = (Element) tipos.next();
		  %>
          <option value="<%= ((Element)tipo.getChild("cod-computador")).getText() %>"> 
          <%= ((Element)tipo.getChild("nome-sala")).getText() + " - " + 
              ((Element)tipo.getChild("descricao-computador")).getText() %> </option>
          <% }	%>
        </table></td>
         
    </tr>
    
    <tr valign="bottom">
    
      <td> <table width="100%" border="0" cellspacing="0" cellpadding="4" bgcolor="#FFFFFF">
          <input type="submit" name="prosseguir" value="Prosseguir">
        </table></td>
             
    </tr>
    
  </table>
  
  <input type="hidden" name="cod-usuario" value="<%=request.getAttribute("cod-usuario")%>">
  
</form>
</body>
</html>
