<%@page import="linfca.*, java.sql.*, org.jdom.Element, java.util.Iterator" errorPage="" %>
<html>
<head>
<title>Inclus&atilde;o de Novo Usu&aacute;rio</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
</head>
<body bgcolor="#FFFFFF" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
<form name="form1" method="post" action="">
  <table width="90%" border="0" cellpadding="8" bgcolor="#FFFFFF">
    <tr> 
      <td colspan="3" bgcolor="#CCCCCC"> <b>Inclus&atilde;o de Novo Usu&aacute;rio</b></td>
    </tr>
    <tr valign="top"> 
      <td height="38" align="right" nowrap bgcolor="#CCCCCC">Tipo de Usu&aacute;rio</td>
      <td colspan="2" bgcolor="#FFFFFF"> <select name="tipo_usuario" id="tipo_usuario">
          <% 
		     Feature  listarTipos = new ListarTipoUsuarioFeature();
			 Element tiposXML = listarTipos.process(null);
			 Iterator tipos = tiposXML.getChildren().iterator();
			 while (tipos.hasNext()) {
		  	    Element tipo = (Element) tipos.next();
		        %>
          <option value="<%= ((Element)tipo.getChild("cod-tipo-usuario")).getText() %>"> 
          <%= ((Element)tipo.getChild("descricao-tipo-usuario")).getText() %> </option>
          <% }	%>
        </select> </td>
    </tr>
    <tr valign="top"> 
      <td width="20%" height="38" align="right" nowrap bgcolor="#CCCCCC"> Identifica&ccedil;&atilde;o<font size="-2">*</font></td>
      <td colspan="2" bgcolor="#FFFFFF"> <input name="id" type="text" id="id"> 
      </td>
    </tr>
    <tr valign="top"> 
      <td align="right" bgcolor="#CCCCCC"> Senha<font size="-2">*</font></td>
      <td colspan="2" bgcolor="#FFFFFF"> <input name="senha" type="password" id="senha"> 
      </td>
    </tr>
    <tr valign="top"> 
      <td height="54" align="right" bgcolor="#CCCCCC"> Redigite a senha</td>
      <td colspan="2" bgcolor="#FFFFFF"> <p> 
          <input name="resenha" type="password" id="resenha">
        </p></td>
    </tr>
    <tr valign="top"> 
      <td align="right" bgcolor="#CCCCCC"> Nome</td>
      <td width="24%" bgcolor="#FFFFFF"> <input name="nome" type="text" id="nome"> 
        <br> <font size="-1">Primeiro nome</font></td>
      <td width="56%" bgcolor="#FFFFFF"> <input name="sobrenome" type="text" id="sobrenome"> 
        <br> <font size="-1">Sobre nome</font></td>
    </tr>
    <tr valign="top">
      <td height="36" align="right" bgcolor="#CCCCCC">Telefone</td>
      <td colspan="2" bgcolor="#FFFFFF"> 
        <input type="text" name="telefone"></td>
    </tr>
    <tr valign="top"> 
      <td align="right" bgcolor="#CCCCCC"> Email</td>
      <td colspan="2" bgcolor="#FFFFFF"> <input name="email" type="text" id="email" size="50"> 
      </td>
    </tr>
    <tr valign="top"> 
      <td align="right" bgcolor="#CCCCCC"> Sexo</td>
      <td colspan="2" bgcolor="#FFFFFF"> <p> 
          <% 
		     listarTipos = new ListarTipoSexoFeature();
			 tiposXML = listarTipos.process(null);
			 tipos = tiposXML.getChildren().iterator();
			 while (tipos.hasNext()) {
		  	    Element tipo = (Element) tipos.next();
		        %>
          <input type="radio" name="sexo" value="<%= ((Element)tipo.getChild("cod-tipo-sexo")).getText() %>">
          <%= ((Element)tipo.getChild("descricao-tipo-sexo")).getText() %> 
          <% }	%>
    </tr>
    <tr valign="top"> 
      <td align="right" bgcolor="#CCCCCC"> Data de Nascimento</td>
      <td colspan="2" bgcolor="#FFFFFF"> <table border="0" cellspacing="2" cellpadding="0">
          <tr align="left"> 
            <td><input name="dia" type="text" id="dia" size="2"></td>
            <td><input name="mes" type="text" id="mes" size="2"></td>
            <td><input name="ano" type="text" id="ano" size="4"></td>
          </tr>
          <tr align="left"> 
            <td>DD</td>
            <td>MM</td>
            <td>YYYY</td>
          </tr>
        </table></td>
    </tr>
    <tr valign="top"> 
      <td colspan="3"> <p>Li e aceito os termos de compromisso para o uso do LINF.<br>
          <input type="radio" name="radiobutton2" value="radiobutton" checked>
          Sim <br>
          <input type="radio" name="radiobutton2" value="radiobutton">
          N&atilde;o</p></td>
    </tr>
    <tr valign="top" bgcolor="#FFFFFF"> 
      <td colspan="3"><input type="submit" name="Submit" value="Confirmar"></td>
    </tr>
  </table>
</form>
</body>
</html>
