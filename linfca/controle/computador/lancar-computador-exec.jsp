<%@page import="linfca.*, 
        java.sql.*, 
        org.jdom.Element, 
        java.util.Iterator" 
        errorPage="" %> 
        
<%

   String codUsuario = request.getParameter("cod-usuario");
   String codComputador = request.getParameter("cod_computador_disponivel");
   
   Element in = new Element("in");
   Element usuario = new Element("cod-usuario");
   Element computador = new Element("cod-computador");
   
   usuario.setText(codUsuario);
   computador.setText(codComputador);
   
   in.getChildren().add(usuario);
   in.getChildren().add(computador);
   in.getChildren().add(new Element("uso"));
   
   Feature  lancamento = new LancamentoFeature();
   Element tiposXML = lancamento.process(in);
   

%>

<html>
<head>
</head>

<body onLoad="javascript:document.form1.submit()">
  <form name="form1" method="post" action="<%=request.getContextPath()%>/log.jsp">
</body>
</html>
