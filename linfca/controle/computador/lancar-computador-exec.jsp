<%@page import="linfca.*, 
        java.sql.*, 
        org.jdom.Element, 
        java.util.Iterator" 
        errorPage="" %> 
        
<%

   String mensagem = "";
   String codUsuario = request.getParameter("cod-usuario");   
   System.out.println("Foi : " + codUsuario);
   Element in = new Element("in");
   
   if (codUsuario != null)  {
      mensagem = "Lan�amento de uso de m�quina conclu�do com sucesso!";
      String codComputador = request.getParameter("cod-computador");
      System.out.println("Foi : " + codComputador);
	  Element usuario = new Element("cod-usuario");
	  Element computador = new Element("cod-computador");	   
	  usuario.setText(codUsuario);
	  computador.setText(codComputador);
      in.getChildren().add(usuario);
      in.getChildren().add(computador);
      in.getChildren().add(new Element("uso"));
   } else {
      mensagem = "Fechamento do lan�amento de uso de m�quina conclu�do com sucesso!";
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
  <form name="form1" method="post" action="<%=request.getContextPath()%>/login.jsp">
    <input type="hidden" name="mensagem" value="<%=mensagem%>">
  </form>
</body>
</html>
