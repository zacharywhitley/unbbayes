<%@page import="linfca.*,
		linfca.cadastro.*,  
		com.oreilly.servlet.*,
		linfca.util.*, 
		java.io.*,
		java.util.Enumeration,
        java.sql.*, 
        org.jdom.Element" 
        errorPage="/design/erro.jsp" %> 
        
<%

   boolean inserir = true;
   
   Element in = new Element("semestre");
   
   MultipartRequest multi = new MultipartRequest(request, ".");   
   Enumeration params = multi.getParameterNames();
   
   while (params.hasMoreElements()) {
   
      String name = (String)params.nextElement();
      
	  if (name.equals("int_cod_semestre")) {
	     inserir = false;	     
	     
	     Element where = new Element("where");	     
	     String value = multi.getParameter(name);
	     Element element = new Element(name);
	     element.setText(value);	     
	     where.getChildren().add(element);	     
	     in.getChildren().add(where);
	     
	     continue;	     
	     
	  }
	  
      String value = multi.getParameter(name);
	  Element element = new Element(name);
	  element.setText(value);
	  in.getChildren().add(element);
      System.out.println(name + " = " + value);
      
   }

   Feature  salvarSemestreF = new SalvarGenericoFeature();
   Element saida = salvarSemestreF.process(in);
   
   String mensagem = null;
   if (saida.getChild("ok") != null) {
      mensagem = (inserir) ? "O semestre foi incluído com sucesso!"
	   						: "Os dados do semestre foram alterados com sucesso!";	
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
