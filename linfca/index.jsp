<%@include file="/util.jsp" %> 
<%@include file =  "/design/cabecalho.jsp"%>

<script language="javascript">
	function submeter() {
		document.login.identificacao.value = document.login.id.value;
		document.login.id.value = "";
	}
</script>

        <tr>
          <td align="right" valign="top"><img height="86" src="<%=path%>/design/imagens/logo_login.gif" width="174" border="0" hspace="20" alt="Log In / Log Out"></td>
          <td>
		  <BR>
            <FORM name="login" action="validar-usuario-exec.jsp" method="post" onSubmit="javascript:submeter()">
            <table width="100%" border="0" cellspacing="5" cellpadding="0" align="center">
              <tr>
                <td colspan=2>
                  <P class="header">Passe sua carteira estudantil da UnB no leitor</P><br>
		</td>
		</tr>
              <tr>
                <td width="30%">
                  <P>Identificação</P></td>
                <td width="70%"></td>
	      </tr>
              <tr>
                <td width="30%"><INPUT type="text" maxLength=35 name="id"></td>
		<INPUT type="hidden" name="identificacao">
		<script>document.login.id.focus()</script>
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