<%@page import="linfca.*, 
		linfca.cadastro.tipousuario.*, 
		linfca.cadastro.tiposexo.*, 
		linfca.cadastro.semestre.*, 
		linfca.cadastro.usuario.*, 
		linfca.util.*,
		java.io.*,
		java.sql.*, 
		org.jdom.Element, 
		java.util.Iterator" 
		errorPage="/design/erro.jsp" %>
		<%@include file="/util.jsp" %> 


<%@include file =  "/design/cabecalho_gerencia.jsp"%>
        <tr>
          <td align="right" valign="top"><img height="86" src="<%=path%>/design/imagens/logo_usuario.gif" width="174" border="0" hspace="20" alt="Salvar Usuário"></td>
          <td>
		  <BR>
            <FORM name="paginaBusca" action="<%=path%>/geral/listar-usuarios.jsp" METHOD="get">
            <table width="100%" border="0" cellspacing="5" cellpadding="0" align="center">             
              <tr>
                <td colspan=2>
                  <P class="header">Digite as informações para efetuar uma busca no Linf:</P><br>
				</td>
			  </tr>
              <tr>
   	      </tr>
                <td width="50%"><P>no-laboratorio</P></td> 
                <td width="50%"><P>Identificação</P></td> 
              <tr>
                <td width="50%">
                                <INPUT TYPE=CHECKBOX NAME="boolean_no-laboratorio"> 
                		
                </td>
                <td width="50%">
                  <INPUT type=text maxLength=35 name="string_identificacao"> 
                </td>
              </tr>
              <tr>
                <td width="50%"><P>CPF</P></td>
                <td width="50%"><P>nome</P></td>
              </tr> 
              <tr>
		<td width="50%">
                  <INPUT type=text maxLength=35 name="string_cpf">
                </td>
		<td width="50%">
                  <INPUT type=text maxLength=35 name="string_nome">
                </td>                
              </tr>
              <tr>
                <td width="50%"><P>Sobrenome</P></td>
                <td width="50%"><P>Telefone</P></td>
              </tr> 
              <tr>
                <td width="50%">
                  <INPUT type=text maxLength=35 name="string_sobrenome">
                </td>
		<td width="50%">
                  <INPUT type=text maxLength=35 name="string_telefone">
                </td>
              </tr>
              <tr>
                <td width="50%"><P>e-mail</P></td>
                <td width="50%"><P>endereco</P></td>
              </tr> 
              <tr>                                
                <td width="50%">
                  <INPUT type=text maxLength=35 name="string_email">
                </td>
                <td colspan=2>
                  <INPUT type=text maxLength=60 name="string_endereco" size=50>
                </td>
              </tr>
              <tr>
                <td colspan=2>
                  <P><INPUT type="submit" value="Buscar">&nbsp;&nbsp;<INPUT type="reset" value="Limpar">
                  </P><br><br>				  
				</td>
				</form>
			  </tr>
			</table>
		  </td>
		</tr>
<%@include file =  "/design/rodape_gerencia.jsp"%>