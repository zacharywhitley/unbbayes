<%@include file="/util.jsp" %> 
<%@include file =  "/design/cabecalho.jsp"%>

        <tr>
          <td align="right" valign="top"><img height="86" src="<%=path%>/design/imagens/logo_login.gif" width="174" border="0" hspace="20" alt="Log In / Log Out"></td>
          <td>
		  <BR>
            <FORM name="login" action="validar-gerente-exec.jsp" method="post">
            <table width="100%" border="0" cellspacing="5" cellpadding="0" align="center">
              <tr>
                <td colspan=2>
                  <P class="header">Digite as informações para seu login.</P><br>
		</td>
		</tr>
              <tr>
                <td width="30%">
                  <P>Identificação</P></td>
		  </tr>
              <tr>
                <td width="30%"><INPUT maxLength=35 name="identificacao"></td>
              <tr>
                <td>
                  <P>Senha</P></td>
                <td></td></tr> 
              <tr>
                <td width="30%"><INPUT type=password maxLength=35 name="senha"></td>
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