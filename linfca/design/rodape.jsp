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
