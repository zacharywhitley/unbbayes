<html>
<head>
</head>
<body>
<form>
		<table width="98%" border="0" align="center" cellpadding="0" cellspacing="0">

			<tr class="tr_cor_detalhe">
				<td class="sub_titulo">
					<b class="txt_06"> * </b>
					Nome:<br>
					<input type="text" name="txtNomUf" maxlength="20" size="30" value="" onkeyup="">
				</td>

			</tr>

			<tr class="tr_cor_detalhe">
				<td class="sub_titulo">
					<b class="txt_06"> * </b>
					Sigla:<br>
					<input type="text" name="txtSigUf" maxlength="2" size="30" value="" onkeyup="">
				</td>
			</tr>


		</table>
		<br>
</form>

		<br>

		<table border="0" align="left" cellpadding="0" cellspacing="4">
			<tr>
				<td>
					&nbsp;
					<input type="button" name="btnCreate" id="btnCreate" value="Confirmar" onclick="enviarAssinatura(0);">
				</td>

				<td>
					&nbsp;&nbsp;&nbsp;
					<input type="button" name="btnCancel" id="btnCancel" value="Cancelar" onclick="enviarAssinatura(1);">
				</td>
			</tr>
		</table>

</body>
</html>