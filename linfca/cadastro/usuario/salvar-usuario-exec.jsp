<%@page import="linfca.*, 
		linfca.cadastro.usuario.*, 
		linfca.util.Base64, 
        java.sql.*, 
        org.jdom.Element, 
        java.util.Iterator" 
        errorPage="" %> 
        
<%

   String menssagem = "";
   
   String codUsuario       = request.getParameter("cod_usuario");         
   String codTipoUsuario   = request.getParameter("cod_tipo_usuario");   
   String codTipoSexo      = request.getParameter("cod_tipo_sexo");   
   String identificacao    = request.getParameter("identificacao");   
   String cpf              = request.getParameter("cpf");
   String nome             = request.getParameter("nome");   
   String sobrenome        = request.getParameter("sobrenome");   
   String senha            = request.getParameter("senha");
   String confirmacaoSenha = request.getParameter("confirmacao_senha");   
   String email            = request.getParameter("email");   
   String endereco         = request.getParameter("endereco");   
   String foto             = request.getParameter("foto");   
   String dia              = request.getParameter("dia");
   String mes              = request.getParameter("mes");
   String ano              = request.getParameter("ano");
   String dataNascimento   = ano + "/" + mes + "/" + dia;
      
   Element in = new Element("in");
   
   Element codTipoUsuarioE   = new Element("cod-tipo-usuario");
   Element codTipoSexoE      = new Element("cod-tipo-sexo");
   Element identificacaoE    = new Element("identificacao");   
   Element cpfE              = new Element("cpf");
   Element nomeE             = new Element("nome");
   Element sobrenomeE        = new Element("sobrenome");
   Element senhaE            = new Element("senha");
   Element confirmacaoSenhaE = new Element("confirmacao-senha");
   Element emailE            = new Element("email");
   Element enderecoE         = new Element("endereco");
   Element fotoE             = new Element("foto");
   Element dataNascimentoE   = new Element("data-nascimento");
   
   codTipoUsuarioE.setText(codTipoUsuario);
   codTipoSexoE.setText(codTipoSexo);
   identificacaoE.setText(identificacao);
   cpfE.setText(cpf);
   nomeE.setText(nome);
   sobrenomeE.setText(sobrenome);
   senhaE.setText(senha);
   confirmacaoSenhaE.setText(confirmacaoSenha);
   emailE.setText(email);
   enderecoE.setText(endereco);
   fotoE.setText(foto);
   dataNascimentoE.setText(dataNascimento);
   
   in.getChildren().add(codTipoUsuarioE);
   in.getChildren().add(codTipoSexoE);
   in.getChildren().add(identificacaoE);
   in.getChildren().add(cpfE);
   in.getChildren().add(nomeE);
   in.getChildren().add(sobrenomeE);
   in.getChildren().add(senhaE);
   in.getChildren().add(confirmacaoSenhaE);
   in.getChildren().add(emailE);
   in.getChildren().add(enderecoE);
   in.getChildren().add(fotoE);
   in.getChildren().add(dataNascimentoE);
   
   if (codUsuario != null)  {
   
      menssagem = "Os dados do usuário foram alterados com sucesso!";
            
	  Element codUsuarioE = new Element("cod-usuario");
	  
	  codUsuarioE.setText(codUsuario);
	  
      in.getChildren().add(codUsuarioE);
      
   } else {
   
      menssagem = "O usuário foi incluído com sucesso!";
      
   }
   
   Feature  salvarUsuarioF = new SalvarUsuarioFeature();
   Element saida = salvarUsuarioF.process(in);
   
   if (saida.getChild("ok") != null) {
   
   } else {
   
   }
%>

<html>
<head>
</head>

<body onLoad="javascript:document.form1.submit()">
  <form name="form1" method="post" action="<%=request.getContextPath()%>/design/sucesso.jsp">
    <input type="hidden" name="menssagem" value="<%=menssagem%>">
  </form>
</body>
</html>
