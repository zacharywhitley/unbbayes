<%@page import="linfca.*, 
		linfca.cadastro.tiposituacao.TipoSituacao,
        org.jdom.Element, 
        java.util.Iterator" 
        errorPage="" %> 
        
<%

   String mensagem = "";
   String codUsuario = request.getParameter("cod-usuario");   
   System.out.println("Foi : " + codUsuario);
   Element in = new Element("in");
   
   if (codUsuario != null)  {
      mensagem = "Lançamento de uso de máquina concluído com sucesso!";
      String codComputador = request.getParameter("cod-equipamento");
      System.out.println("Foi : " + codComputador);
	  Element usuario = new Element("cod-usuario");
	  Element computador = new Element("cod-equipamento");	   
	  usuario.setText(codUsuario);
	  computador.setText(codComputador);
      in.getChildren().add(usuario);
      in.getChildren().add(computador);
   } else {
      mensagem = "Fechamento do lançamento de uso de máquina concluído com sucesso!";
	  String codLancamento = request.getAttribute("cod-lancamento-uso").toString();
      Element lancamento = new Element("cod-lancamento-uso");
      lancamento.setText(codLancamento);
      in.getChildren().add(lancamento);   
   }
   
   Feature  lancamentoF = new LancamentoUsoFeature();
   Element saida = lancamentoF.process(in);
   
   if (saida.getChild("ok") != null) {
   
   } else {
   
   }
%>

<html>
<head>
</head>

<body onLoad="javascript:document.form1.submit()">
  <form name="form1" method="post" action="<%=request.getContextPath()%>/index.jsp">
    <input type="hidden" name="mensagem" value="<%=mensagem%>">
  </form>
</body>
</html>
