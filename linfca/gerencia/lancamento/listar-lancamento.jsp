<%@page import="linfca.gerencia.lancamento.*,
				linfca.*,
				linfca.util.*,
				java.io.*,
				java.util.*,
				java.sql.*,				
				org.jdom.Element" 
       errorPage="/design/erro.jsp" %>
	   
	   
<script language="Javascript">
<!--
//var URL   = "listar-lancamento.jsp"
var speed = 10000


function reload() {
location.reload()
}

setTimeout("reload()", speed);
//-->
</script>

	   
<%@include file="/util.jsp" %> 

<%@include file =  "/design/cabecalho_gerencia.jsp"%>

        <tr>
          <td align="right" valign="top"><img height="86" src="<%=path%>/design/imagens/logo_lancamento.gif" width="174" border="0" hspace="20" alt="Histórico de Lançamentos"></td>
          <td>
		  <BR>
            <FORM name="listar" action="" method="post">
            
            <table width="100%" border="0" cellspacing="5" cellpadding="0" align="center">
            
              <tr>
              
                <td colspan=2>
                  <P class="header">Histórico de Lançamentos:</P><br>
				</td>
				
			  </tr>
			  
			  <tr>
			  
			    <td colspan=2>
			      <table width="100%" border="1" cellspacing="0" cellpadding="10" align="center">
			      

            
              <tr> 
              
                <td align="center">
                  Usu&aacute;rio
                </td>
                
                <td align="center">
                  Foto
                </td>
                
                <td align="center">
                  Data-Hora In&iacute;cio
                </td>
                
                <td align="center">
                  Data-Hora Fim
                </td>
                
              </tr>
  
  <% 
  
   		File diretorio = new File(config.getServletContext().getRealPath("") + "/tmp");
		File[] arquivos = diretorio.listFiles();
		if (arquivos != null) {
			for (int i = 0; i < arquivos.length; i++) {
			    if (arquivos[i].lastModified() < System.currentTimeMillis() - 10000)		
					arquivos[i].delete();		
			}
		}
  
 		Element in = new Element("in");
		Element dataInicio = new Element("data-hora-inicio");
		String temp = request.getParameter("data-hora-inicio");
		if (temp != null && ! temp.equals(""))  {
			dataInicio.setText(temp);
		} else {
			dataInicio.setText(new Timestamp(System.currentTimeMillis() - 100000L).toString());
		}
	
		in.getChildren().add(dataInicio);
		
		Element dataFim = new Element("data-hora-fim");		
		temp = request.getParameter("data-hora-fim");
		if (temp != null && ! temp.equals(""))  {
			dataFim.setText(temp);
		} else {
			dataFim.setText(new Timestamp(System.currentTimeMillis()).toString());
		}
		in.getChildren().add(dataFim);
		
		if (request.getParameter("abertos") != null) {
			in.getChildren().add(new Element("abertos"));
		}
		
		Feature f = new ListarLancamentoUsoFeature();
		Element outXML = f.process(in);
		List children = outXML.getChildren();
		for (int i = 0; i < children.size(); i++) {									
			Element lan = (Element) children.get(i);
			
			byte[] buffer = Base64.decode(Base64.getBinaryBytes(lan.getChildTextTrim("foto-usuario")));
			File foto = File.createTempFile("usuario", null, new File(config.getServletContext().getRealPath("") + "/tmp"));
			FileOutputStream fos = new FileOutputStream(foto);
			fos.write(buffer);
			fos.close();
			foto.deleteOnExit();
  %>
			  
              <tr>
              
                <td align="center">
                  <P><%= lan.getChildTextTrim("nome-usuario")  %></P>
                </td>
                
                <td align="center">
                  <img src="<%=path + "/tmp/" + foto.getName() %>" border="0" hspace="20" height="240" alt="Foto do Usuário">
                </td>
                
                <td align="center">
                  <%= lan.getChildTextTrim("data-hora-inicio-uso")  %>
                </td align="center">
                
                <td align="center">
                  <%= lan.getChildTextTrim("data-hora-fim-uso")  %>
                </td>
                
			  </tr>			  
		
  <%     }  %>
  
			      </table>  
			    </td>
			    
			  </tr>  
			  
              <tr>
                <td colspan=2>
                  <P>
                  </P><br><br>
				</td>
				</form>
			  </tr>
			</table>
		  </td>
		</tr>

<%@include file =  "/design/rodape_gerencia.jsp"%>
