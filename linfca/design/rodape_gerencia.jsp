	  </table>
	</td>
  </tr>
  <tr>
	<td colspan=2 height=1 bgcolor="#336633"><img src="#" width=1 height=1></td>
  </tr>
  <tr>
	<td colspan=2 height=2 bgcolor="#6ca66c"><img src="#" width=1 height=2></td>
  </tr>
  <tr>
    <td height="40" colspan="2" align="center" bgcolor="#336633" valign="middle">
	  <font face="Arial, Helvetica, sans-serif" size="1" color="black">
	  <span style="font-size: 13px">
	  <a href="<%=path%>/cadastro/curso/salvar-curso.jsp">curso</a>&nbsp;&nbsp;&nbsp;|&nbsp;&nbsp;&nbsp; 
      <a href="<%=path%>/cadastro/equipamento/salvar-equipamento.jsp">equipamento</a>&nbsp;&nbsp;&nbsp;|&nbsp;&nbsp;&nbsp; 
      <a href="<%=path%>/cadastro/semestre/salvar-semestre.jsp">semestre</a>&nbsp;&nbsp;&nbsp;|&nbsp;&nbsp;&nbsp; 
	  <a href="<%=path%>/cadastro/usuario/selecionar-usuario.jsp">usuário</a>&nbsp;&nbsp;&nbsp;|&nbsp;&nbsp;&nbsp; 
      <a href="<%=path%>/gerencia/lancamento/filtrar-lancamento.jsp">lançamento</a>&nbsp;&nbsp;&nbsp;|&nbsp;&nbsp;&nbsp; 
      <a href="#">eventos</a>&nbsp;&nbsp;&nbsp;|&nbsp;&nbsp;&nbsp;       
      <a href="<%=path%>/downloads.jsp">downloads</a>
	  </span>
	</td>
  </tr>
  <tr>
	<td height="2" colspan="2" bgcolor="#6CA66C"><img src="#" width="1" height="2"></td>
  </tr>
  <tr>
	<td height="1" colspan="2" bgcolor="#336633"><img src="#" width="1" height="1"></td>
  </tr>
  <tr>
    <td colspan="2" bgcolor="#6CA66C" align="center"><br>Content &copy; 2002 Rommel & Michael<br>All Rights Reserved<br></td>
  </tr>
</table>
 <% 
   String mensagem = request.getParameter("mensagem");
   if (mensagem != null) {
 %>
	 <script language="JavaScript">
		top.window.status='<%= mensagem %>';
	 </script>
<% } %>

</body>
</html>
