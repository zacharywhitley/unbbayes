<%@page import="linfca.*,
		linfca.cadastro.*,  
		java.util.Enumeration,
        org.jdom.Element" 
        errorPage="/design/erro.jsp" %> 
      
<%
   String nomeTabela = request.getParameter("nome_tabela");
   boolean inserir = true;
   
   Element in = new Element(nomeTabela);
   
   Enumeration params = request.getParameterNames();
   
   while (params.hasMoreElements()) {   
      String name = (String)params.nextElement();
	  if (name.equals("nome_tabela")) {
	  	continue;
	  }
	  
	  Element element = new Element(name);      
      String value = request.getParameter(name);
	  element.setText(value);
	  
	  if (name.equals("int_cod_" + nomeTabela)) {
	     inserir = false;
	     Element where = new Element("where");	     
	     where.getChildren().add(element);
	     in.getChildren().add(where);
	  } else {	  
		  in.getChildren().add(element);
	  }
   }

   Feature  salvarF = new SalvarGenericoFeature();
   Element saida = salvarF.process(in);
   
   String mensagem = null;
   if (saida.getChild("ok") != null) {
      mensagem = (inserir) ? "O " + nomeTabela + " foi incluído com sucesso!"
	   						: "Os dados do " + nomeTabela + " foram alterados com sucesso!";	
   } else {
      mensagem = "Ocorreu um erro!";   
   }
%>

<html>
<head>
</head>

<body onLoad="javascript:document.form1.submit()">
  <form name="form1" method="post" action="<%=request.getContextPath()%>/design/sucesso.jsp">
    <input type="hidden" name="mensagem" value="<%=mensagem%>">
  </form>
</body>
</html>

