<%@page import="linfca.*, 
		linfca.cadastro.tiposituacao.*, 
		linfca.cadastro.sala.*, 
		linfca.cadastro.equipamento.*, 
		org.jdom.Element, 
		java.util.Iterator" 
		errorPage="" %>
		<%@include file="/util.jsp" %> 

<%

	String codEquipamento = request.getParameter("cod_equipamento");
	Element equipamentoXML = null;
	if (codEquipamento != null) {
	
		Element in = new Element("in");
		Element codEquipamentoXML = new Element("cod-equipamento");
		codEquipamentoXML.setText(codEquipamento);
		in.getChildren().add(codEquipamentoXML);
		
		Feature  detalharEquipamento = new DetalharEquipamentoFeature();
		equipamentoXML = detalharEquipamento.process(in);	
	}
%>

<%@include file =  "/design/cabecalho.jsp"%>
        <tr>
          <td align="right" valign="top"><img height="86" src="<%=path%>/design/imagens/logo_equipamento.gif" width="174" border="0" hspace="20" alt="Salvar Equipamento"></td>
          <td>
		  <BR>
            <FORM name="form" action="<%=path%>/cadastro/equipamento/salvar-equipamento-exec.jsp" METHOD="post">
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
                		 value="<% if (equipamentoXML != null) { %><%=equipamentoXML.getChildTextTrim("nome-equipamento")%><% } %>">
                </td>
                <td width="50%">
				<textarea name="string_desc_equipamento"><% if (equipamentoXML != null) { %><%=equipamentoXML.getChildTextTrim("descricao-equipamento")%><% } %></textarea>
                </td>
              </tr>
              <tr>
                <td width="50%"><P>Número do Patrimônio</P></td>
                <td width="50%"><P>Valor do Equipamento</P></td>
              </tr> 
              <tr>
                <td width="50%"><INPUT name="string_numero_patrimonio_equipamento" type=text maxLength=15
										value="<% if (equipamentoXML != null) { %><%=equipamentoXML.getChildTextTrim("numero-patrimonio-equipamento")%><% } %>">
				</td>
                <td width="50%"><INPUT name="float_valor_equipamento" type=text maxLength=10
										value="<% if (equipamentoXML != null) { %><%=equipamentoXML.getChildTextTrim("valor-equipamento")%><% } %>">
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
                      <% if ( (equipamentoXML != null) && (tipo.getChildTextTrim("cod-tipo-situacao").equals(equipamentoXML.getChildTextTrim("cod-tipo-situacao")))) { %> selected <% } %> > 
                     <%= tipo.getChildTextTrim("descricao-tipo-situacao") %> </option>
                  <% }	%>
                  </select>
                </td>
				<td>
                  <select name="int_cod_sala">
                  <% 
		             listarTipos = new ListarSalaFeature();
			         tiposXML = listarTipos.process(null);
			         tipos = tiposXML.getChildren().iterator();
			         while (tipos.hasNext()) {
		  	            Element tipo = (Element) tipos.next();
		                %>
                     <option value="<%= tipo.getChildTextTrim("cod-sala") %>" 
                      <% if ( (equipamentoXML != null) && (tipo.getChildTextTrim("cod-sala").equals(equipamentoXML.getChildTextTrim("cod-sala")))) { %> selected <% } %> > 
                     <%= tipo.getChildTextTrim("nome-sala") %> </option>
                  <% }	%>
                  </select>
				</td>
              </tr>			  
		  
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
<%@include file =  "/design/rodape.jsp"%>