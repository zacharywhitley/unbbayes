<%@page import="linfca.*, 
        java.sql.*, 
        org.jdom.Element, 
        java.util.Iterator" 
        errorPage="" %> 
        
<%

   String codUsuario = request.getParameter("cod-usuario");   
   
   Element in = new Element("in");
   
   if (codUsuario != null)  {
      String codComputador = request.getParameter("cod_computador_disponivel"); 
	  Element usuario = new Element("cod-usuario");
	  Element computador = new Element("cod-computador");	   
	  usuario.setText(codUsuario);
	  computador.setText(codComputador);
      in.getChildren().add(usuario);
      in.getChildren().add(computador);
      in.getChildren().add(new Element("uso"));
   } else {
	  String codLancamento = request.getAttribute("cod-lancamento").toString();
      Element lancamento = new Element("cod-lancamento");
      lancamento.setText(codLancamento);
      in.getChildren().add(lancamento);   
   }
   
   Feature  lancamentoF = new LancamentoFeature();
   Element saida = lancamentoF.process(in);
   
   if (saida.getChild("ok") != null) {
   
   } else {
   
   }
%>

<html>
<head>
</head>

<body onLoad="javascript:document.form1.submit()">
  <form name="form1" method="post" action="<%=request.getContextPath()%>/lancamento-sucesso.jsp">
</body>
</html>
