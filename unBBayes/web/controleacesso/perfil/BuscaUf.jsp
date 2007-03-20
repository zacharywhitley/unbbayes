<html>
<head>
<script language="javascript">
	function limpaFormularios() {
		for(var i = 0; i < document.forms.length; i++) {
			for(var e = 0; e < document.forms[i].length; e++){
				if(document.forms[i].elements[e].type == "text") {
					document.forms[i].elements[e].value="";
				}
				if(document.forms[i].elements[e].type == "select-one") {
					document.forms[i].elements[e].value="";
				}
			}
		}
	}
</script>
</head>
<body>
<form>
	<table width="98%" border="0" align="center" cellpadding="0" cellspacing="0">
		<tr class="tr_cor_detalhe">
			<td class="sub_titulo">
				<b>Nome:</b><br>
				<input type="text" name="txtNomUf" maxlength="20" size="25" value="">
			</td>
		</tr>

		<tr class="tr_cor_detalhe">
			<td class="sub_titulo">
				<b>Sigla:</b><br>
				<input type="text" name="txtNomUf" maxlength="20" size="25" value="">
			</td>
		</tr>

	</table>
	<br>
	<table border="0" align="left" cellpadding="0" cellspacing="0">
		<tr class="tr_cor_detalhe">
			<td>
				&nbsp;
				<input type="button" name="btnSearch" id="btnSearch" value="Consultar" onclick="">

			</td>
			<td>
				&nbsp;&nbsp;&nbsp;
				<input type="button" name="btnLimpar" value="Limpar" onclick="javascript:limpaFormularios();" />
			</td>
		</tr>
	</table>
	<br>
</form>

		<!-- %%%%%%%%%%%%%%%%%%%%%% Resultado da Consulta - INICIO %%%%%%%%%%%%%%%%%%%%%% -->

<br>
		<table width="98%" border="0" align="center" cellpadding="0" cellspacing="0">
			<tr class="tr_cor_detalhe">
				<td NOWRAP class="resultado_titulo_hr">
					Resultado da Consulta
				</td>
			</tr>
		</table>

<br>
		<center>


			<table width="98%" border="0" cellPadding="0" cellSpacing="0" class="bordatabela">
				<tr class="tr_resultado_titulo">
					<td class="td_resultado_titulo" width='10%'>
						&nbsp;
					</td>
					<td class="td_resultado_titulo">
						Nome
					</td>
					<td class="td_resultado_titulo">
						Sigla
					</td>


				</tr>
				<tr class="tr_cor_sim" onMouseOut="this.className='tr_cor_sim';" onMouseOver="this.className='tr_cor_mouse';" >
					<td class="td_resultado_titulo" width='10%'>
						<input type='checkbox'>
					</td>
					<td class="bg_01">
						<a href="ShowDetailUf.do?keyparameter=121&pager=0:page-0">a</a>
					</td>
					<td class="bg_01">
						<a href="ShowDetailUf.do?keyparameter=121&pager=0:page-0">a</a>

					</td>



				</tr><tr class="tr_cor_nao" onMouseOut="this.className='tr_cor_nao';" onMouseOver="this.className='tr_cor_mouse';" >
					<td class="td_resultado_titulo" width='10%'>
						<input type='checkbox'>
					</td>

					<td class="bg_01">
						<a href="ShowDetailUf.do?keyparameter=93&pager=0:page-0">Acre</a>
					</td>
					<td class="bg_01">

						<a href="ShowDetailUf.do?keyparameter=93&pager=0:page-0">AC</a>
					</td>



				</tr><tr class="tr_cor_sim" onMouseOut="this.className='tr_cor_sim';" onMouseOver="this.className='tr_cor_mouse';" >

					<td class="td_resultado_titulo" width='10%'>
						<input type='checkbox'>
					</td>
					<td class="bg_01">
						<a href="ShowDetailUf.do?keyparameter=94&pager=0:page-0">Alagoas</a>
					</td>

					<td class="bg_01">
						<a href="ShowDetailUf.do?keyparameter=94&pager=0:page-0">AL</a>
					</td>



				</tr><tr class="tr_cor_nao" onMouseOut="this.className='tr_cor_nao';" onMouseOver="this.className='tr_cor_mouse';" >

					<td class="td_resultado_titulo" width='10%'>
						<input type='checkbox'>
					</td>
					<td class="bg_01">
						<a href="ShowDetailUf.do?keyparameter=95&pager=0:page-0">Amapá</a>

					</td>
					<td class="bg_01">
						<a href="ShowDetailUf.do?keyparameter=95&pager=0:page-0">AP</a>
					</td>



				</tr><tr class="tr_cor_sim" onMouseOut="this.className='tr_cor_sim';" onMouseOver="this.className='tr_cor_mouse';" >

					<td class="td_resultado_titulo" width='10%'>
						<input type='checkbox'>
					</td>
					<td class="bg_01">

						<a href="ShowDetailUf.do?keyparameter=96&pager=0:page-0">Amazonas</a>
					</td>
					<td class="bg_01">
						<a href="ShowDetailUf.do?keyparameter=96&pager=0:page-0">AM</a>
					</td>



				</tr><tr class="tr_cor_nao" onMouseOut="this.className='tr_cor_nao';" onMouseOver="this.className='tr_cor_mouse';" >

					<td class="td_resultado_titulo" width='10%'>
						<input type='checkbox'>
					</td>
					<td class="bg_01">
						<a href="ShowDetailUf.do?keyparameter=97&pager=0:page-0">Bahia</a>
					</td>
					<td class="bg_01">
						<a href="ShowDetailUf.do?keyparameter=97&pager=0:page-0">BA</a>
					</td>


				</tr><tr class="tr_cor_sim" onMouseOut="this.className='tr_cor_sim';" onMouseOver="this.className='tr_cor_mouse';" >

					<td class="td_resultado_titulo" width='10%'>
						<input type='checkbox'>
					</td>
					<td class="bg_01">
						<a href="ShowDetailUf.do?keyparameter=98&pager=0:page-0">Ceará</a>
					</td>
					<td class="bg_01">
						<a href="ShowDetailUf.do?keyparameter=98&pager=0:page-0">CE</a>
					</td>



				</tr><tr class="tr_cor_nao" onMouseOut="this.className='tr_cor_nao';" onMouseOver="this.className='tr_cor_mouse';" >

					<td class="td_resultado_titulo" width='10%'>
						<input type='checkbox'>
					</td>
					<td class="bg_01">
						<a href="ShowDetailUf.do?keyparameter=35&pager=0:page-0">Distrito Federal</a>
					</td>
					<td class="bg_01">
						<a href="ShowDetailUf.do?keyparameter=35&pager=0:page-0">DF</a>

					</td>



				</tr><tr class="tr_cor_sim" onMouseOut="this.className='tr_cor_sim';" onMouseOver="this.className='tr_cor_mouse';" >

					<td class="td_resultado_titulo" width='10%'>
						<input type='checkbox'>
					</td>
					<td class="bg_01">
						<a href="ShowDetailUf.do?keyparameter=99&pager=0:page-0">Espírito Santo</a>
					</td>
					<td class="bg_01">

						<a href="ShowDetailUf.do?keyparameter=99&pager=0:page-0">ES</a>
					</td>



				</tr><tr class="tr_cor_nao" onMouseOut="this.className='tr_cor_nao';" onMouseOver="this.className='tr_cor_mouse';" >

					<td class="td_resultado_titulo" width='10%'>
						<input type='checkbox'>
					</td>
					<td class="bg_01">
						<a href="ShowDetailUf.do?keyparameter=100&pager=0:page-0">Goiás</a>
					</td>

					<td class="bg_01">
						<a href="ShowDetailUf.do?keyparameter=100&pager=0:page-0">GO</a>
					</td>



				</tr>
			</table>



		</center>

		<br>

		<table border="0" align="left" cellpadding="0" cellspacing="0">
			<tr>

				<td>
					&nbsp;
					<input type="button" name="btnIncluir" id="btnIncluir" value="Incluir" onclick="">
				</td>
				<td>
					&nbsp;&nbsp;&nbsp;
					<input type="button" name="btnExcluir" id="btnExcluir" value="Excluir" onclick="">
				</td>
			</tr>

		</table>

</body>
</html>