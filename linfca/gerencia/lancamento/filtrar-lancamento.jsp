<%@page import="linfca.gerencia.lancamento.*,
				linfca.*,
				linfca.util.*,
				java.io.*,
				java.util.*,
				java.sql.*,				
				org.jdom.Element" 
       errorPage="/design/erro.jsp" %>

<%@include file="/util.jsp" %> 

<%@include file =  "/design/cabecalho.jsp"%>

        <tr>
          <td align="right" valign="top"><img height="86" src="<%=path%>/design/imagens/logo_lancamento.gif" width="174" border="0" hspace="20" alt="Filtro de Lançamentos"></td>
          <td>
		  <BR>
            <FORM name="filtrar" method="post" action="<%=path%>/gerencia/lancamento/listar-lancamento.jsp">
            
            <table width="100%" border="0" cellspacing="5" cellpadding="0" align="center">
            
              <tr>
              
                <td colspan=2>
                  <P class="header">Filtro de Lançamentos:</P><br>
				</td>
				
			  </tr>
			  
			  <tr>
			    
			    <td>
                  <P>Data Hora Início</P>
				</td>
				
				<td>
                  <P>Data Hora Fim</P>
				</td>
			    
			  </tr>
			  
			  <tr>
			    
			    <td>
                  <input name="data-hora-inicio" type="text">
				</td>
				
				<td>
                  <input name="data-hora-fim" type="text">
				</td>
			    
			  </tr>
			  
			  <tr>
			    
			    <td>
                  <P><input name="abertos" type="checkbox" value=""> Abertos</P>
				</td>
				
				<td>
                  <P>Entre com a Data Hora no formato AAAA-MM-DD HH:MM:SS. Ex: 2002-01-15 10:15:30</P>
				</td>
			    
			  </tr>
			  
              <tr>
                <td colspan=2>
                  <P><input value="Processar" type="submit">
                  </P><br><br>
				</td>
				</form>
			  </tr>
			</table>
		  </td>
		</tr>

<%@include file =  "/design/rodape.jsp"%>