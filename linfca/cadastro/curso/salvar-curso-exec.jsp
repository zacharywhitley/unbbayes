<%@page import="linfca.*,
		linfca.cadastro.*,  
		java.util.Enumeration,
        org.jdom.Element" 
        errorPage="" %> 
      
<%

   boolean inserir = true;
   
   Element in = new Element("curso");
   
   Enumeration params = request.getParameterNames();
   
   while (params.hasMoreElements()) {
   
      String name = (String)params.nextElement();
      
	  if (name.equals("int_cod_curso")) {
	     inserir = false;
	     
	     Element where = new Element("where");	     
	     String value = request.getParameter(name);
	     Element element = new Element(name);
	     element.setText(value);	     
	     where.getChildren().add(element);	     
	     in.getChildren().add(where);	     
	     continue;	     
	  }
	  
      String value = request.getParameter(name);
	  Element element = new Element(name);
	  element.setText(value);
	  in.getChildren().add(element);
   }

   Feature  salvarF = new SalvarGenericoFeature();
   Element saida = salvarF.process(in);
   
   String mensagem = null;
   if (saida.getChild("ok") != null) {
      mensagem = (inserir) ? "O curso foi incluído com sucesso!"
	   						: "Os dados do curso foram alterados com sucesso!";	
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

