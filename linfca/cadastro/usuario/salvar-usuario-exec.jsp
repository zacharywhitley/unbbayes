<%@page import="linfca.*, 
		linfca.cadastro.*, 
		com.oreilly.servlet.*,
		linfca.util.*, 
		java.io.*,
		java.util.Enumeration,
        java.sql.*, 
        org.jdom.Element" 
        errorPage="" %> 
        
<%

   boolean inserir = true;
   
   Element in = new Element("usuario");
   
   MultipartRequest multi = new MultipartRequest(request, ".");   
   Enumeration params = multi.getParameterNames();
   
   while (params.hasMoreElements()) {
   
      String name = (String)params.nextElement();
      
      String value = multi.getParameter(name);
	  Element element = new Element(name);
	  element.setText(value);
	  
	  if ((value.equals("")) || (name.equals("dia")) || (name.equals("mes")) || (name.equals("ano")) || (name.equals("foto")) || (name.equals("confirmacao_senha"))) {
	     continue;
	  }
      
	  if (name.equals("int_cod_usuario")) {
	     inserir = false;
	     
	     Element where = new Element("where");
	     where.getChildren().add(element);	     
	     in.getChildren().add(where);
	     
	     continue;
	  }
	        	  
	  in.getChildren().add(element);
      System.out.println(name + " = " + value);
      
   }
   System.out.println(multi.getParameter("ano") + "-" + multi.getParameter("mes") + "-" + multi.getParameter("dia"));
   Element data = new Element("date_data_nascimento");
   data.setText(multi.getParameter("ano") + "-" + multi.getParameter("mes") + "-" + multi.getParameter("dia"));   
   in.getChildren().add(data);
   
   String foto64 = null;   
   Enumeration files = multi.getFileNames();
   
   if (files.hasMoreElements()) {
      String name = (String)files.nextElement();
	  File f = multi.getFile(name);
	  
	  if (f != null) {
	     FileInputStream fis = new FileInputStream(f);
	     byte buffer[] = new byte[(int)f.length()];
	     fis.read(buffer);
	     foto64 = Base64.getString(Base64.encode(buffer));
	  
	     Element element = new Element("string_foto");
	     element.setText(foto64);
	     in.getChildren().add(element);
	  
	     fis.close();
	  }
	  
   }  
    
   Feature  salvarUsuarioF = new SalvarGenericoFeature();
   Element saida = salvarUsuarioF.process(in);
   
   String mensagem = null;
   if (saida.getChild("ok") != null) {
      mensagem = (inserir) ? "O usuário foi incluído com sucesso!"
	   						: "Os dados do usuário foram alterados com sucesso!";	
   } else {
      mensagem = "Ocorreu um erro!";   
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
