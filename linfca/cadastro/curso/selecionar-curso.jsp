<%@page errorPage="/design/erro.jsp" %> 
<%@include file="/util.jsp" %>

<script language="JavaScript">
   location.href = "<%=path%>/cadastro/selecionar-generico.jsp?nome_tabela=curso&campo=cod_opcao&campo=desc_curso";
</script>