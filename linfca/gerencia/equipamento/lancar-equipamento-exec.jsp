<%@page import="linfca.*, 
		linfca.gerencia.lancamento.*, 
		linfca.cadastro.tiposituacao.*, 
        java.sql.*, 
        org.jdom.Element, 
        java.util.Iterator" 
        errorPage="/design/erro.jsp" %> 
        
<%

   String identificacao = request.getAttribute("identificacao").toString();
   String nome = request.getAttribute("nome").toString();

   String mensagem = nome + " (" + identificacao + ")";
   
   Element in = new Element("in");
   
   if (request.getAttribute("cod-usuario") != null) {
	String codUsuario = request.getAttribute("cod-usuario").toString();
      mensagem += " entrou no LINF com sucesso!";
	Element usuario = new Element("cod-usuario");
	usuario.setText(codUsuario);
      in.getChildren().add(usuario);
   } else {
      mensagem += " saiu do LINF com sucesso!";
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
