<%@page import="linfca.gerencia.lancamento.*,
				linfca.*,
                                linfca.geral.*, 
				linfca.util.*,
				java.io.*,
				java.util.*,
				java.sql.*,				
				org.jdom.Element" 
       errorPage="/design/erro.jsp" %>
	   
	   


	   
<%@include file="/util.jsp" %> 

<%@include file =  "/design/cabecalho_gerencia.jsp"%>

        <tr>
          <td align="right" valign="top"><img height="86" src="<%=path%>/design/imagens/logo_lancamento.gif" width="174" border="0" hspace="20" alt="Usuários Encontrados"></td>
          <td>
		  <BR>
            <table width="100%" border="0" cellspacing="5" cellpadding="0" align="center">
            
              <tr>
              
                <td colspan=2>
                  <P class="header">Listar Usuários:</P><br>
				</td>
				
        	  </tr>
			  
			  <tr>
			  
			    <td colspan=2>
			      <table width="100%" border="1" cellspacing="0" cellpadding="10" align="center">           

  
  <%             

 		Element in = new Element("in");
		Element no_laboratorio = new Element("no-laboratorio");
		String temp = request.getParameter("boolean_no-laboratorio");		
                if(temp != null){
			in.getChildren().add(no_laboratorio);		
		}
		Element identificacao = new Element("identificacao");		
		temp = request.getParameter("string_identificacao");		
		if(! temp.equals("")){
			identificacao.setText(temp);
			in.getChildren().add(identificacao);		
		}		
	        Element cpf= new Element("cpf");		
		temp = request.getParameter("string_cpf");
		if(! temp.equals("")){
			cpf.setText(temp);
			in.getChildren().add(cpf);		
		}
		Element nome = new Element("nome");		
		temp = request.getParameter("string_nome");
		if(! temp.equals("")){
			nome.setText(temp);
			in.getChildren().add(nome);		
		}
		Element sobrenome = new Element("sobrenome");		
		temp = request.getParameter("string_sobrenome");
		if(! temp.equals("")){
			sobrenome.setText(temp);
			in.getChildren().add(sobrenome);		
		}
		Element telefone = new Element("telefone");		
		temp = request.getParameter("string_telefone");
		if(! temp.equals("")){
			identificacao.setText(temp);
			in.getChildren().add(telefone);		
		}
		Element email = new Element("email");		
		temp = request.getParameter("string_email");
		if(! temp.equals("")){
			identificacao.setText(temp);
			in.getChildren().add(email);		
		}
		Element endereco = new Element("endereco");		
		temp = request.getParameter("string_endereco");
		if(! temp.equals("")){
			endereco.setText(temp);
			in.getChildren().add(endereco);				
		}
		Feature f = new BuscarAlunoFeature();
		Element outXML = f.process(in);
		List children = outXML.getChildren();
		for (int i = 0; i < children.size(); i++) {								
			Element child = (Element) children.get(i);			                        
  %>
			  
              <tr>              
                <td align="center">
                  <P><%= child.getChildTextTrim("nome-completo")  %></P>
                </td>                                                
                <td align="center">
                  <%= child.getChildTextTrim("identificacao")  %>
                </td align="center">                
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
			  </tr>
			</table>
		  </td>
		</tr>

<%@include file =  "/design/rodape_gerencia.jsp"%>
