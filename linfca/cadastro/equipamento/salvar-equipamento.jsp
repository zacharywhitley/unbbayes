<%@page import="linfca.*,
		linfca.cadastro.*,
		linfca.cadastro.tiposituacao.*, 
		org.jdom.Element, 
		java.util.Iterator" 
		errorPage="/design/erro.jsp" %>
		<%@include file="/util.jsp" %> 

<%
	String nomeTabela = "equipamento";
	String codEquipamento = request.getParameter("cod_elemento");
	Element equipamentoXML = null;
	if (codEquipamento != null) {
	
		Element in = new Element("in");
		
		Element nomeTabelaXML = new Element("nome-tabela");
		nomeTabelaXML.setText(nomeTabela);
		in.getChildren().add(nomeTabelaXML);
		
		Element codEquipamentoXML = new Element("cod-elemento");
		codEquipamentoXML.setText(codEquipamento);
		in.getChildren().add(codEquipamentoXML);
		
		Feature  detalharEquipamento = new DetalharGenericoFeature();
		equipamentoXML = detalharEquipamento.process(in);	
	}
%>

<%@include file =  "/design/cabecalho_gerencia.jsp"%>
        <tr>
          <td align="right" valign="top"><img height="86" src="<%=path%>/design/imagens/logo_equipamento.gif" width="174" border="0" hspace="20" alt="Salvar Equipamento"></td>
          <td>
		  <BR>
            <FORM name="form" action="<%=path%>/cadastro/salvar-generico-exec.jsp" METHOD="post">
            <table width="100%" border="0" cellspacing="5" cellpadding="0" align="center">
              <tr>
                <td colspan=2>
                  <P class="header">Digite as informações do equipamento do Linf:</P><br>
				</td>
			  </tr>
              <tr>
                <td width="50%"><P>Nome</P></td>
                <td width="50%"><P>Descrição</P></td> 
			  </tr>
              <tr>
                <td width="50%">
                		<INPUT maxLength=30 name="string_nome_equipamento" 
                		 value="<% if (equipamentoXML != null) { %><%=equipamentoXML.getChildTextTrim("nome_equipamento")%><% } %>">
                </td>
                <td width="50%">
				<textarea name="string_desc_equipamento"><% if (equipamentoXML != null) { %><%=equipamentoXML.getChildTextTrim("desc_equipamento")%><% } %></textarea>
                </td>
              </tr>
              <tr>
                <td width="50%"><P>Número do Patrimônio</P></td>
                <td width="50%"><P>Valor do Equipamento</P></td>
              </tr> 
              <tr>
                <td width="50%"><INPUT name="string_numero_patrimonio_equipamento" type=text maxLength=15
										value="<% if (equipamentoXML != null) { %><%=equipamentoXML.getChildTextTrim("numero_patrimonio_equipamento")%><% } %>">
				</td>
                <td width="50%"><INPUT name="float_valor_equipamento" type=text maxLength=10
										value="<% if (equipamentoXML != null) { %><%=equipamentoXML.getChildTextTrim("valor_equipamento")%><% } %>">
				</td>
              </tr>
              <tr>
                <td width="50%"><P>Situação</P></td>
                <td width="50%"><P>Sala</P></td>
              </tr> 
			  <tr>                
                <td>
                  <select name="int_cod_tipo_situacao">
                  <% 
		             Feature listarTipos = new ListarTipoSituacaoFeature();
			         Element tiposXML = listarTipos.process(null);
			         Iterator tipos = tiposXML.getChildren().iterator();
			         while (tipos.hasNext()) {
		  	            Element tipo = (Element) tipos.next();
		                %>
                     <option value="<%= tipo.getChildTextTrim("cod-tipo-situacao") %>" 
                      <% if ( (equipamentoXML != null) && (tipo.getChildTextTrim("cod-tipo-situacao").equals(equipamentoXML.getChildTextTrim("cod_tipo_situacao")))) { %> selected <% } %> > 
                     <%= tipo.getChildTextTrim("descricao-tipo-situacao") %> </option>
                  <% }	%>
                  </select>
                </td>
				<td>
                  <select name="int_cod_sala">
                  <% 
		             listarTipos = new ListarGenericoFeature();
					 Element in = new Element("in");
					 Element nomeXML = new Element("nome-tabela");
					 nomeXML.setText("sala");
					 in.getChildren().add(nomeXML);
					 
					 nomeXML = new Element("campo");
					 nomeXML.setText("nome_sala");
					 in.getChildren().add(nomeXML);					 
					 
			         tiposXML = listarTipos.process(in);
			         tipos = tiposXML.getChildren().iterator();
			         while (tipos.hasNext()) {
		  	            Element tipo = (Element) tipos.next();
						String codSala = tipo.getChildTextTrim("cod-elemento"); 
		                %>
                     <option value="<%= codSala %>" 
                      <% if ( (equipamentoXML != null) && (codSala.equals(equipamentoXML.getChildTextTrim("cod_sala")))) { %> selected <% } %> > 
                     <%= tipo.getChildTextTrim("nome_sala") %> </option>
                  <% }	%>
                  </select>
				</td>
              </tr>			  
		  	  <INPUT type="hidden" name="nome_tabela" value="<%=nomeTabela%>">
              <% if (equipamentoXML != null) { %>
                    <INPUT type="hidden" name="int_cod_equipamento" value="<%=codEquipamento%>">
              <% } %>
              <tr>
                <td colspan=2>
                  <P><INPUT type="submit" value="Salvar">&nbsp;&nbsp;<INPUT type="reset" value="Limpar">
                  </P><br><br>				  
				</td>
				</form>
			  </tr>
			</table>
		  </td>
		</tr>
<%@include file =  "/design/rodape_gerencia.jsp"%>