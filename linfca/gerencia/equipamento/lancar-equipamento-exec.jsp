<%@page import="linfca.*, 
		linfca.gerencia.lancamento.*, 
		linfca.cadastro.tiposituacao.*, 
        java.sql.*, 
        org.jdom.Element, 
        java.util.Iterator" 
        errorPage="/design/erro.jsp" %> 
        
<%

   String mensagem = "";
   String codUsuario = request.getParameter("cod-usuario");   

   Element in = new Element("in");
   
   if (codUsuario != null)  {
      mensagem = "Lançamento de uso de máquina concluído com sucesso!";
      String codEquipamento = request.getParameter("cod-equipamento");

	  Element usuario = new Element("cod-usuario");
	  Element equipamento = new Element("cod-equipamento");	   	  
	  usuario.setText(codUsuario);
	  equipamento.setText(codEquipamento);
      in.getChildren().add(usuario);
      in.getChildren().add(equipamento);
   } else {
      mensagem = "Fechamento do lançamento de uso de máquina concluído com sucesso!";
	  String codLancamento = request.getAttribute("cod-lancamento-uso").toString();
      Element lancamento = new Element("cod-lancamento-uso");
      lancamento.setText(codLancamento);
      in.getChildren().add(lancamento);
	  	  
	  Element equipamento = new Element("cod-equipamento");
	  String codEquipamento = request.getAttribute("cod-equipamento").toString();
	  equipamento.setText(codEquipamento);
	  in.getChildren().add(equipamento);
/*	  	  
      Element descricao = new Element("desc-tipo-situacao");
      descricao.setText(TipoSituacao.DISPONIVEL);
	  in.getChildren().add(descricao);
*/	  
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
