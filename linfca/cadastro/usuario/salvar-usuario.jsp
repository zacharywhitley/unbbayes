<%@page import="linfca.*, 
		linfca.cadastro.usuario.*, 
		java.sql.*, 
		org.jdom.Element, 
		java.util.Iterator" 
		errorPage="" %>
		<%@include file="/util.jsp" %> 

<%

	String codUsuario = request.getParameter("cod_usuario");
	Element usuarioXML = null;

	if (codUsuario != null) {
	
		Element in = new Element("in");
		Element codUsuarioXML = new Element("cod-usuario");
		codUsuarioXML.setText(codUsuario);
		in.getChildren().add(codUsuarioXML);
		
		Feature  detalharUsuario = new DetalharUsuarioFeature();
		usuarioXML = detalharUsuario.process(in);
	
	}

%>

<%@include file =  "/design/cabecalho.jsp"%>
        <tr>
          <td align="right" valign="top"><img height="86" src="<%=path%>/design/imagens/logo_usuario.gif" width="174" border="0" hspace="20" alt="Log In / Log Out"></td>
          <td>
		  <BR>
            <FORM name="login" action="<%=path%>/cadastro/usuario/salvar-usuario-exec.jsp" METHOD="post" ENCTYPE="multipart/form-data">
            <table width="100%" border="0" cellspacing="5" cellpadding="0" align="center">
              <tr>
                <td colspan=2>
                  <P class="header">Digite as informa��es do novo usu�rio do Linf:</P><br>
				</td>
			  </tr>
              <tr>
                <td width="50%"><P>Identifica��o</P></td>
                <td width="50%"><P>Tipo de Usu�rio</P></td> 
			  </tr>
              <tr>
                <td width="50%">
                		<INPUT maxLength=35 name="identificacao" 
                		 value="<% if (usuarioXML != null) { %><%=usuarioXML.getChild("identificacao").getTextTrim()%><% } %>">
                </td>
                <td width="50%">
                  <select name="cod_tipo_usuario">
                  <% 
		             Feature  listarTipos = new ListarTipoUsuarioFeature();
			         Element tiposXML = listarTipos.process(null);
			         Iterator tipos = tiposXML.getChildren().iterator();
			         while (tipos.hasNext()) {
		  	            Element tipo = (Element) tipos.next();
		                %>
                     <option value="<%= ((Element)tipo.getChild("cod-tipo-usuario")).getText() %>" 
                      <% if ( ((Element)tipo.getChild("cod-tipo-usuario")).getTextTrim().equals(usuarioXML.getChild("cod-tipo-usuario").getTextTrim()) ) { %> selected <% } %> > 
                     <%= ((Element)tipo.getChild("descricao-tipo-usuario")).getText() %> </option>
                  <% }	%>
                  </select>
                </td>
              </tr>
              <tr>
                <td width="50%"><P>Senha</P></td>
                <td width="50%"><P>Redigite a Senha</P></td>
              </tr> 
              <tr>
                <td width="50%"><INPUT type=password maxLength=35 name="senha"></td>
                <td width="50%"><INPUT type=password maxLength=35 name="confirmacao_senha"></td>
              </tr>
              <tr>
                <td width="50%"><P>Nome</P></td>
                <td width="50%"><P>Sobrenome</P></td>
              </tr> 
              <tr>
                <td width="50%">
                  <INPUT type=text maxLength=35 name="nome"
                   value="<% if (usuarioXML != null) { %><%=usuarioXML.getChild("nome").getTextTrim()%><% } %>">
                </td>
                <td width="50%">
                  <INPUT type=text maxLength=35 name="sobrenome"
                   value="<% if (usuarioXML != null) { %><%=usuarioXML.getChild("sobrenome").getTextTrim()%><% } %>">
                </td>
              </tr>
              <tr>
                <td width="50%"><P>Data de Nascimento</P></td>
                <td width="50%"><P>Sexo</P></td>
              </tr> 
              <tr>
                <td width="50%">
                  <input maxLength=2 name="dia" type="text" size="2"
                   value="<% if (usuarioXML != null) { %><%=usuarioXML.getChild("dia").getTextTrim()%><% } %>">
                   &nbsp;<font size=4>/</font>&nbsp;
                   
                  <input maxLength=2 name="mes" type="text" size="2"
                   value="<% if (usuarioXML != null) { %><%=usuarioXML.getChild("mes").getTextTrim()%><% } %>">
                   &nbsp;<font size=4>/</font>&nbsp;
                   
                  <input maxLength=4 name="ano" type="text" size="4"
                   value="<% if (usuarioXML != null) { %><%=usuarioXML.getChild("ano").getTextTrim()%><% } %>">
                </td>
                <td width="50%">                
                  <% 
		             listarTipos = new ListarTipoSexoFeature();
			         tiposXML = listarTipos.process(null);
			         tipos = tiposXML.getChildren().iterator();
			         while (tipos.hasNext()) {
		  	            Element tipo = (Element) tipos.next();
		          %>
                     <input type="radio" name="cod_tipo_sexo" value="<%= ((Element)tipo.getChild("cod-tipo-sexo")).getText() %>"
                      <% if ( ((Element)tipo.getChild("cod-tipo-sexo")).getTextTrim().equals(usuarioXML.getChild("cod-tipo-sexo").getTextTrim()) ) { %> checked <% } %> >
                     <%= ((Element)tipo.getChild("descricao-tipo-sexo")).getText() %> 
                  <% }	%>                
                </td>
              </tr>
              <tr>
                <td width="50%"><P>Telefone</P></td>
                <td width="50%"><P>E-mail</P></td>
              </tr> 
              <tr>
                <td width="50%">
                  <INPUT type=text maxLength=35 name="telefone"
                   value="<% if (usuarioXML != null) { %><%=usuarioXML.getChild("telefone").getTextTrim()%><% } %>">
                </td>
                <td width="50%">
                  <INPUT type=text maxLength=35 name="email"
                   value="<% if (usuarioXML != null) { %><%=usuarioXML.getChild("email").getTextTrim()%><% } %>">
                </td>
              </tr>
              <tr>
                <td width="50%"><P>Foto</P></td>
                <td width="50%"><P>CPF</P></td>
              </tr> 
              <tr>                                
                <td width="50%"><INPUT type=file name="foto"></td>
                <td width="50%">
                  <INPUT type=text maxLength=11 name="cpf"
                   value="<% if (usuarioXML != null) { %><%=usuarioXML.getChild("cpf").getTextTrim()%><% } %>">
                </td>
              </tr>
              <tr>
                <td><P>Endere�o</P></td>
              </tr> 
              <tr>                
                <td>
                  <INPUT type=text maxLength=60 name="endereco" size=50
                   value="<% if (usuarioXML != null) { %><%=usuarioXML.getChild("endereco").getTextTrim()%><% } %>">
                </td>
              </tr>
              <% if (usuarioXML != null) { %>
                    <INPUT type="hidden" name="cod_usuario" value="<%=codUsuario%>">
              <% } %>
              <tr>
                <td colspan=2>
                  <P><INPUT type="submit" value="Processar">&nbsp;&nbsp;<INPUT type="reset" value="Limpar">
                  </P><br><br>				  
				</td>
				</form>
			  </tr>
			</table>
		  </td>
		</tr>
<%@include file =  "/design/rodape.jsp"%>