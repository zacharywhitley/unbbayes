<%@page import="java.io.*"
		isErrorPage="true"  %>
<%@include file="/util.jsp" %> 
<%@include file =  "/design/cabecalho.jsp"%>
        <tr>
          <td align="right" valign="top"><img height="86" src="<%=path%>/design/imagens/logo_erro.gif" width="174" border="0" hspace="20" alt="Erro"></td>
          <td>
		  <BR>
            <FORM name="login" action="javascript:history.back()" method="post">
            <table width="100%" border="0" cellspacing="5" cellpadding="0" align="center">
              <tr>
                <td colspan=2>
                  <P class="header"><%= exception.getMessage() %></P><br>
				</td>
			  </tr>
			  <tr>
                <td colspan=2>
                  <% exception.printStackTrace(new PrintWriter(out)); %><br>
				</td>
			  </tr>
              <tr>
                <td colspan=2>
                  <P><INPUT type="submit" value="Voltar">
                  </P><br><br>				  
				</td>
				</form>
			  </tr>
			</table>
		  </td>
		</tr>
<%@include file =  "/design/rodape.jsp"%>