<%@page import="linfca.*, 
		linfca.cadastro.usuario.*, 
		com.oreilly.servlet.*,
		linfca.util.*, 
		java.io.*,
		java.util.Enumeration,
        java.sql.*, 
        org.jdom.Element" 
        errorPage="" %> 
        
<%

   boolean inserir = true;
   
   Element in = new Element("in");
   
   MultipartRequest multi = new MultipartRequest(request, ".");   
   Enumeration params = multi.getParameterNames();
   
   while (params.hasMoreElements()) {
   
      String name = (String)params.nextElement();
      
	  if (name.equals("cod_usuario")) {
	     inserir = false;	  	  
	  }
	  
      String value = multi.getParameter(name);
	  Element element = new Element(name.replace('_', '-'));
	  element.setText(value);	  
	  in.getChildren().add(element);
      System.out.println(name + " = " + value);
      
   }
   
   String foto64 = null;   
   Enumeration files = multi.getFileNames();
   
   if (files.hasMoreElements()) {
   
      String name = (String)files.nextElement();
	  File f = multi.getFile(name);
	  
	  FileInputStream fis = new FileInputStream(f);
	  byte buffer[] = new byte[(int)f.length()];
	  fis.read(buffer);
	  foto64 = Base64.getString(Base64.encode(buffer));
	  
	  Element element = new Element("foto");
	  element.setText(foto64);
	  in.getChildren().add(element);
	  
	  fis.close();
	  
   }  
    
   Feature  salvarUsuarioF = new SalvarUsuarioFeature();
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
  <form name="form1" method="post" action="<%=request.getContextPath()%>/design/sucesso.jsp">
    <input type="hidden" name="menssagem" value="<%=mensagem%>">
  </form>
</body>
</html>
