<!DOCTYPE html PUBLIC "-//W3C//DTD html 4.0 Transitional//EN">
<html>
<head>
  <title>LinfCA - Controle de Acesso do Linf</title>
  <meta name="resource-type" content="document">
  <meta name="author" content="Rommel N. Carvalho e Michael S. Onishi, 2002">
  <meta name="description" content="O laboratório de informática da UnB está cada dia melhor agora com controle de acesso feito em Java e muito mais!">
  <meta name="keywords" content="informática, informatic, computer, computador, laboratório, laboratory, universidade, university">
  <meta name="distribution" content="global">
  <meta name="robots" content="all">
  <meta http-equiv="Content-Type" content="text/html; charset=windows-1252">

<STYLE type=text/css>
a,a:visited {text-decoration: none}
a:hover {color: #6CA66C;
  text-decoration: underline;}
a.inpage:hover {color: white;
  text-decoration: underline;}
body {font-size: 10px;
  font-family: arial, helvetica, sans-serif}
p {font-weight: normal;
  font-size: 14px;
  margin: 0px;
  color: #000000;
  text-indent: 0px;
  font-family: arial, helvetica, sans-serif}
p.header {font-weight: bold;
  font-size: 14px;
  margin: 0px;
  color: #000000;
  text-indent: 0px;
  line-height: 2;
  font-family: arial, helvetica, sans-serif}
td {font-size: 10px;
  color: #000000;
  font-family: arial, helvetica, sans-serif}
</STYLE>

<script language="JavaScript">
<!--
function MM_swapImgRestore() { //v3.0
  var i,x,a=document.MM_sr; for(i=0;a&&i<a.length&&(x=a[i])&&x.oSrc;i++) x.src=x.oSrc;
}

function MM_preloadImages() { //v3.0
  var d=document; if(d.images){ if(!d.MM_p) d.MM_p=new Array();
    var i,j=d.MM_p.length,a=MM_preloadImages.arguments; for(i=0; i<a.length; i++)
    if (a[i].indexOf("#")!=0){ d.MM_p[j]=new Image; d.MM_p[j++].src=a[i];}}
}

function MM_findObj(n, d) { //v4.0
  var p,i,x;  if(!d) d=document; if((p=n.indexOf("?"))>0&&parent.frames.length) {
    d=parent.frames[n.substring(p+1)].document; n=n.substring(0,p);}
  if(!(x=d[n])&&d.all) x=d.all[n]; for (i=0;!x&&i<d.forms.length;i++) x=d.forms[i][n];
  for(i=0;!x&&d.layers&&i<d.layers.length;i++) x=MM_findObj(n,d.layers[i].document);
  if(!x && document.getElementById) x=document.getElementById(n); return x;
}

function MM_swapImage() { //v3.0
  var i,j=0,x,a=MM_swapImage.arguments; document.MM_sr=new Array; for(i=0;i<(a.length-2);i+=3)
   if ((x=MM_findObj(a[i]))!=null){document.MM_sr[j++]=x; if(!x.oSrc) x.oSrc=x.src; x.src=a[i+2];}
}
//-->
</script>
</head>

<body bgcolor="#6CA66C" text="#000000" link="#000000" vlink="#000000" alink="#000000" style="margin: 0px" marginheight="0" marginwidth="0" border="0" onLoad="MM_preloadImages('images/homeON.gif', ,'images/contactON.gif','images/privacy.policyON.gif','design/imagens/nome_unbON.gif','design/imagens/nome_cicON.gif','design/imagens/nome_linfcaON.gif','design/imagens/nome_nossa_equipeON.gif','design/imagens/nome_eventosON.gif','design/imagens/nome_downloadsON.gif')">
<table width="100%" border="0" cellspacing="0" cellpadding="0" align="left" bgcolor="#6CA66C">
  <tr>
    <td bgcolor="#6CA66C" colspan="2"><a href="http://www.unb.br"><img src="<%=path%>/design/imagens/logo_unb.gif" height="32" width="210" vspace="10" hspace="10" border="0" alt="Universidade de Brasília."></a></td>
  </tr>
  <tr>
	<td height="1" colspan="2" bgcolor="#3C783C"><img src="images/spacer.gif" width=1 height=1></td>
  </tr>
  <tr>
	<td height="2" colspan="2" bgcolor="#6CA66C"><img src="images/spacer.gif" width=1 height=2></td>
  </tr>
  <tr>
	<td height="3" colspan="2" bgcolor="#3C783C"><img src="images/spacer.gif" width=1 height=3></td>
  </tr>
  <tr>
    <td colspan="2" bgcolor="#336633">
      <table align="center" cellspacing="0" cellpadding="5" border="0" name="menu_table">
        <tr>
          <td align="center"><a href="http://www.unb.br" onMouseOut="MM_swapImgRestore()" onMouseOver="MM_swapImage('UnB','','<%=path%>/design/imagens/nome_unbON.gif',1)"><img name="UnB" border="0" src="<%=path%>/design/imagens/nome_unb.gif" width="44" height="20"></a></td>
          <td align="center"><a href="http://www.cic.unb.br" onMouseOut="MM_swapImgRestore()" onMouseOver="MM_swapImage('CIC','','<%=path%>/design/imagens/nome_cicON.gif',1)"><img name="CIC" border="0" src="<%=path%>/design/imagens/nome_cic.gif" width="44" height="20"></a></td>
		  <td align="center"><a href="http://www.linf.unb.br" onMouseOut="MM_swapImgRestore()" onMouseOver="MM_swapImage('Linf','','<%=path%>/design/imagens/nome_linfON.gif',1)"><img name="Linf" border="0" src="<%=path%>/design/imagens/nome_linf.gif" width="44" height="20"></a></td>
          <td align="center"><a href="http://rommel.myip.org:8080/linfca" onMouseOut="MM_swapImgRestore()" onMouseOver="MM_swapImage('LinfCA','','<%=path%>/design/imagens/nome_linfcaON.gif',1)"><img name="LinfCA" border="0" src="<%=path%>/design/imagens/nome_linfca.gif" width="68" height="16"></a></td>
          <td align="center"><a href="#" onMouseOut="MM_swapImgRestore()" onMouseOver="MM_swapImage('Nossa Equipe','','<%=path%>/design/imagens/nome_nossa_equipeON.gif',1)"><img name="Nossa Equipe" border="0" src="<%=path%>/design/imagens/nome_nossa_equipe.gif" width="133" height="21"></a></td>
          <td align="center"><a href="#" onMouseOut="MM_swapImgRestore()" onMouseOver="MM_swapImage('Eventos','','<%=path%>/design/imagens/nome_eventosON.gif',1)"><img name="Eventos" border="0" src="<%=path%>/design/imagens/nome_eventos.gif" width="75" height="15"></a></td>
          <td align="center"><a href="#" onMouseOut="MM_swapImgRestore()" onMouseOver="MM_swapImage('Downloads','','<%=path%>/design/imagens/nome_downloadsON.gif',1)"><img name="Downloads" border="0" src="<%=path%>/design/imagens/nome_downloads.gif" width="105" height="16"></a></td>
		</tr>
	  </table>
	</td>
  </tr>
  <tr>
    <td width="248" bgcolor="#FFFFFF"><img  src="<%=path%>/design/imagens/logo_linfca.jpg" width="181" border="0"></td>
    <td bgcolor="#FFFFFF"><img src="<%=path%>/design/imagens/nome_logo_linfca.gif" border="0" alt="Partnership in Business"></td>
  </tr>
  <tr>
	<td height="8" colspan="2" bgcolor="#336633"><img src="images/spacer.gif" width="1" height="8" border="0"></td>
  </tr>
  <tr>
	<td height="2" colspan="2" bgcolor="#6CA66C"><img src="images/spacer.gif" width="1" height="2" border="0"></td>
  </tr>
  <tr>
	<td height="1" colspan="2" bgcolor="#336633"><img src="images/spacer.gif" width="1" height="1" border="0"></td>
  </tr>
  <tr>
    <td colspan="2" align="center">
      <table width="95%" border="0" cellspacing="5" cellpadding="5" align="left" valign="top">