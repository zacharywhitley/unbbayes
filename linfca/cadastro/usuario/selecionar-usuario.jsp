<%@page import="linfca.*,
		linfca.cadastro.usuario.*,  
        java.sql.*, 
        org.jdom.Element, 
        java.util.Iterator" 
        errorPage="" %> 
<%@include file="/util.jsp" %> 
<%@include file =  "/design/cabecalho.jsp"%>

        <tr>
          <td align="right" valign="top"><img height="86" src="<%=path%>/design/imagens/logo_usuario.gif" width="174" border="0" hspace="20" alt="Sele��o de Usu�rio"></td>
          <td>
		  <BR>
            <FORM name="selecionar" action="<%=path%>/cadastro/usuario/salvar-usuario.jsp" method="post">
            
            <table width="100%" border="0" cellspacing="5" cellpadding="0" align="center">
            
              <tr>
              
                <td colspan=2>
                  <P class="header">Escolha o usu�rio o qual deseja alterar os dados:</P><br>
				</td>
				
			  </tr>
			  
              <tr>
              
                <td>
                  <P>Usu�rio</P>
                </td>
                
			  </tr>
			  
              <tr>
              
                <td>
                  <select name="cod_usuario" size="20">
                  <% 
		             Feature  listarUsuario = new ListarUsuarioFeature();
			         Element usuariosXML = listarUsuario.process(null);
			         Iterator usuarios = usuariosXML.getChildren().iterator();
			         while (usuarios.hasNext()) {
		  	            Element usuario = (Element) usuarios.next();
		  	            System.out.println("Nome: " + ((Element)usuario.getChild("nome-completo")).getText());
		  	            //System.out.println("SobreNome: " + ((Element)usuario.getChild("sobrenome")).getText());
		          %>		          
                  <option value="<%= ((Element)usuario.getChild("cod-usuario")).getText() %>"> 
                  <%= ((Element)usuario.getChild("identificacao")).getText() + " - " + 
                      ((Element)usuario.getChild("nome-completo")).getText() %> </option>
                  <% }	%>
                </td>
                  
              </tr>
                
              <tr>
                <td colspan=2>
                  <P><INPUT type="submit" value="Salvar">
                  </P><br><br>				  
				</td>
				</form>
			  </tr>
			</table>
		  </td>
		</tr>
		
<%@include file =  "/design/rodape.jsp"%>