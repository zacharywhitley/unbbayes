<%@ taglib uri="/WEB-INF/lib/taglib/c.tld" prefix="c" %>
<%@page errorPage="/design/erro.jsp" %> 
<%@include file="/util.jsp" %>

<script language="JavaScript">
   location.href = "<%=path%>/cadastro/selecionar-generico.jsp?nome_tabela=usuario&campo=identificacao&campo=nome&campo=sobrenome";
</script>   