<%@include file="/util.jsp" %> 
<%@include file =  "/design/cabecalho.jsp"%>
        <tr>
          <td align="right" valign="top"><img height="86" src="<%=path%>/design/imagens/logo_downloads.gif" width="174" border="0" hspace="20" alt="Log In / Log Out"></td>
          <td>
		  <BR>
            <FORM name="login" action="javascript:history.back()" method="post">
            <table width="100%" border="0" cellspacing="5" cellpadding="0" align="center">
              <tr>
                <td colspan=2>
                  <P class="header">Arquivos utilizados para desenvolvimento:</P><br>
				</td>
			  </tr>
			  <span style="font-size: 13px">
              <tr>
                <td width="30%">
                  <P><a href="<%=path%>/geral/controle de acesso 2_3.cdm">Modelo BD</a></P>
                </td>
                <td width="30%">
                  <P><a href="<%=path%>/geral/myodbc-2.50.39-nt.zip">myodbc-2.50.39-nt.zip</a></P>
                </td>
                <td width="40%">
                  <P><a href="<%=path%>/geral/mm.mysql-2.0.13-you-must-unjar-me.jar">mm.mysql-2.0.13-you-must-unjar-me.jar</a></P>
                </td>
			  </tr>
              <tr>
                <td width="30%">
                  <P><a href="<%=path%>/geral/jdom-b8.zip">jdom-b8.zip</a></P>
                </td>
                <td width="30%">
                  <P><a href="<%=path%>/geral/script2.sql">script2.sql</a></P>
                </td>
                <td width="40%">
                  <P><a href="<%=path%>/geral/tomcatPluginV096.zip">tomcatPluginV096.zip</a></P>
                </td>
			  </tr>
			  </span>
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