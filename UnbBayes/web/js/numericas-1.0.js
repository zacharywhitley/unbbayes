// JavaScript Document

/************************************************************************* ?NDICE *************************************************************************************
01) - limparNumero(campo)                           - Limpa os caracteres do array "limpador" do campo.
02) - retiraZeroEsquerda(campo)                     - Retira zero("0") a esquerda.
03) - formataReal(campo)                            - Acrescenta (,00) ao valor digitado no campo. Formata como moeda.
04) - validaNumero(campo, boolean)                  - Valida campos numericos como inteiros ou reais (este, usando ponto decimal) - Retorna Booleano.
05) - fPositivo(campo)                              - Retorna positivo para campos numericos com int. ou reais (este, usando (.)) - Se nao, retorna Booleano (false).
06) - fLength(campo)                                - Retorna o tamanho (length) do campo.
07) - ehInteiro(id, str)                            - Verifica se o campo possui um valor numerico inteiro e retorna um Booleano.
08) - bloqueiaNaoNumericos()                        - Permite somente a digitacao de numeros. [0-9]
09) - formataPercentual(campo, [0-9])               - Retorna o valor em formato de porcentagem(* 100 - acrescentando o simbolo "%")
10) - recuperaAsc(campo)                            - Retorna o valor da tabela asc para a tecla digitada.
11) - validaNumerico(id, tam, tipo, str)            - Retorna um Booleano para a validacao de um campo numerico. tipo = [inteiro | natural | numero]
12) - ehNumero(id, str)                             - Retorna um Booleano para a validacao de um campo numerico.
************************************************************************** ?NDICE *************************************************************************************/

/*(01)**( v 1.0 )*************************************************************************/
// FUNCAO:  Limpa os caracteres do array "limpador" do campo.
// EXEMPLO: onBlur="this.value = limparNumero(this.value)"
// AUTOR:   DBA Engenharia de Sistemas LTDA. (23/08/2006)  // TESTADA: IE / MF
//****************************************************************************************/

function limparNumero(strNumero) {
	var limpador = new Array ("."  , "," , "/" , "|" , "\\");
	
	for (var i=0; i < limpador.length; i++) {
		for (;strNumero.indexOf(limpador[i]) != -1;) {
			strNumero = strNumero.replace(limpador[i], "");
		}
	}
	return strNumero;
}


/*(02)**( v 1.0 )*************************************************************************/
// FUNCAO:  Retira zero("0") a esquerda.
// EXEMPLO: onBlur="this.value = retiraZeroEsquerda(this.value)"
// AUTOR:   DBA Engenharia de Sistemas LTDA. (--/--/----)  // TESTADA: IE / MF
//****************************************************************************************/

function retiraZeroEsquerda(strNumero) {	
	var nValor = strNumero;
	var flag = false;
	var primeiraVez = true;
	
	for (var i=0; i < strNumero.length ; i++) {
		if (primeiraVez) {
			primeiraVez = false;
			flag = true;
		}
		if (flag && strNumero.charAt(i) === 0) {
			nValor = nValor.substring(1);
		} else {
			break;
		}
	}
	return nValor;
}


/*(03)**( v 1.0 )*************************************************************************/
// FUNCAO:  Acrescenta (,00) ao valor digitado no campo. Formata como moeda.
// EXEMPLO: onBlur="this.value = formataReal(this.value)"
// AUTOR:   DBA Engenharia de Sistemas LTDA. (07/11/2003)  // TESTADA: IE / MF
//****************************************************************************************/

function formataReal(valorAFormatar) {
    var i;
    var decimalPointDelimiter = ",";
    var posDecPoint = parseInt("");
    var hasDecPoint = false;
    var s = new String(valorAFormatar);
    var sAux = new String("");
	
    for (i = 0; i < s.length; i++) {
    	var c = s.charAt(i);
		
    	if (c == '.') {
			c = decimalPointDelimiter; 
		    sAux += c;
		}
		if (c == decimalPointDelimiter) {
        	hasDecPoint = true;
        	posDecPoint = i;
        	break;
		}
    }
	
    for (var j = i+1; j < s.length; j++) sAux += s.charAt(j); {
		
		if (!hasDecPoint) {
			valorAFormatar = s + ",00";
		} else {
			s = sAux + '00';
			valorAFormatar= s.charAt(0);
			for (i = 1; i <= (posDecPoint+2); i++) {
				valorAFormatar += s.charAt(i);
			}
			if (posDecPoint === 0) {
				valorAFormatar = '0'+ valorAFormatar;		
			}
		}
	}
    return (valorAFormatar);
}



/*(04)**( v 1.0 )*******************************************************************************************/
// FUNCAO:  Valida campos numericos como inteiros ou reais (este, usando ponto decimal) - Retorna Booleano.
// EXEMPLO: onBlur="validaNumero(this.value, true)"
// AUTOR:   DBA Engenharia de Sistemas LTDA. - (--/--/----)  // TESTADA: IE / MF
//**********************************************************************************************************/

function validaNumero(strNum, alertFlag) {
	var regExp = /^(\d)+(\.(\d)+){0,1}$/;	
	//Checando se a string passada como parametro bate com a expressao regular
	var tmpArray = strNum.match(regExp);
	
	//Se o array for nulo, entao o n?mero nao eh valido
	if (tmpArray == null) {
		//Verificando se devemos mandar uma mensagem de erro
		if (alertFlag) {
			alert("Campo invalido! Por favor, digite apenas numeros.");
			//eval(strNum).focus();
		}
		//Retornando o resultado
		return(false);
	}
	return(true);
}

	
/*(05)**( v 1.0 )***********************************************************************************************************************************************/
// FUNCAO:  Retorna um valor positivo para campos numericos com inteiros ou reais (este, usando ponto decimal) - Caso contrario, retorna Booleano (false).
// EXEMPLO: onBlur="this.value = fPositivo(this.value)"
// AUTOR:   DBA Engenharia de Sistemas LTDA. - (--/--/----)  // TESTADA: IE / MF
//*************************************************************************************************************************************************************/	

function fPositivo(campo) {
	campo = campo.toLowerCase();
	var RefString = "0123456789.-";

	if (campo.length < 1) {
		return (false);
	}
	
	for (var i = 0; i < campo.length; i++) {
		var ch = campo.substr(i, 1);
		var a = RefString.indexOf(ch, 0);

		if (a == -1) {
			return (false);
		}
	}

	if (campo < 0) {
		return (campo * -1);
	}

	return campo;
}


	
/*(06)**( v 1.0 )***********************************************************************************************************************************************/
// FUNCAO:  Retorna o tamanho (length) do campo.
// EXEMPLO: onBlur="fLength(this.value)"
// AUTOR:   DBA Engenharia de Sistemas LTDA. - (--/--/----)  // TESTADA: IE / MF
//*************************************************************************************************************************************************************/	

function fLength(campo) {
	if (campo == null) {
		return (false);
	}
	return String(campo).length;
}



/*(07)**( v 1.0 )**********************************************************************************************/
// FUNCAO:  Verifica se o campo possui um valor numerico inteiro e retorna um Booleano.
// EXEMPLO: onBlur="ehInteiro(id, 'n?o ? um n?mero inteiro!\nPor favor corrija.')"
// AUTOR:   DBA Engenharia de Sistemas LTDA. - (--/--/----)  // TESTADA: IE / MF
//*************************************************************************************************************/

function ehInteiro(strControl, strMessage) {
	var strValue = document.getElementById(strControl).value;
	
	if (!isNaN(strValue)) {
		if (parseFloat(strValue) == parseInt(strValue,10)) {
			return true;
		}
	}
	alert("\"" + strValue + "\"" + " " + strMessage);
	return false;
}


/*(08)**( v 1.0 )****************************************************************************************************/
// FUNCAO:  Permite somente a digitacao de numeros. [0-9]
// EXEMPLO: onKeyDown="bloqueiaNaoNumericos()"
// AUTOR:   DBA Engenharia de Sistemas LTDA. (07/10/2003)  // TESTADA: IE NO MF substituir keyCode por which
//*******************************************************************************************************************/

function bloqueiaNaoNumericos() {
	if (event.keyCode == 46) {
		event.keyCode = 44;
	}
	if ((event.keyCode < 44) || 
		(event.keyCode > 44 && event.keyCode < 48) || 
		(event.keyCode > 57)) {
		
		event.returnValue = false;		
	}
}


/*(09)**( v 1.0 )***************************************************************************************************/
// FUNCAO:  Retorna o valor em formato de porcentagem(* 100 - acrescentando o simbolo "%")
// EXEMPLO: onBlur="this.value = formataPercentual(this.value, 0)"
// AUTOR:   DBA Engenharia de Sistemas LTDA. - (--/--/----)  // TESTADA: IE / MF
//******************************************************************************************************************/

function formataPercentual(valor, NumDigitosDepoisDecimal) {
	var iNumDecimals = NumDigitosDepoisDecimal;
	var dbInVal = valor * 100;
	var bNegative = false;
	var iInVal = 0;
	var strInVal
	var strWhole = "", strDec = "";
	var strTemp = "", strOut = "";
	var iLen = 0;

	if (dbInVal < 0) {
		bNegative = true;
		dbInVal *= -1;
	}

	dbInVal = dbInVal * Math.pow(10, iNumDecimals)
	iInVal = parseInt(dbInVal);
	
	if ((dbInVal - iInVal) >= .5) {
		iInVal++;
	}
	
	strInVal = iInVal + "";
	strWhole = strInVal.substring(0, (strInVal.length - iNumDecimals));
	strDec = strInVal.substring((strInVal.length - iNumDecimals), strInVal.length);
	
	while (strDec.length < iNumDecimals) {
		strDec = "0" + strDec;
	}
	
	iLen = strWhole.length;
	
	if (iLen >= 3) {
		while (iLen > 0) {
			strTemp = strWhole.substring(iLen - 3, iLen);
			
			if (strTemp.length == 3) {
				strOut = "," + strTemp + strOut;
				iLen -= 3;
			} else {
				strOut = strTemp + strOut;
				iLen = 0;
			}
		}
		
		if (strOut.substring(0, 1) == ",") {
			strWhole = strOut.substring(1, strOut.length);
		} else {
			strWhole = strOut;
		}
	}
	
	if (bNegative) {
		return "-" + strWhole + " " + strDec + "%";
	} else {
		return strWhole + " " + strDec + "%";
	}
}


/*(10)**( v 1.0 )*************************************************************************************/
// FUNCAO:  Retorna o valor da tabela asc para a tecla digitada.
// EXEMPLO: onBlur="this.value = recuperaAsc(this.value)"
// AUTOR:   DBA Engenharia de Sistemas LTDA. - (--/--/----)  // TESTADA: IE / MF
//****************************************************************************************************/


function recuperaAsc(campo) {
	var symbols = " !\"#$%&'()*+'-./0123456789:;<=>?@?";
	var loAZ = "abcdefghijklmnopqrstuvwxyz";
	var loc;
	
	symbols += loAZ.toUpperCase();
	symbols += "[\\]^_`";
	symbols += loAZ;
	symbols += "{|}~";
	loc = symbols.indexOf(campo);
	
	if (loc > -1) { 
		Ascii_Decimal = 32 + loc;
		return (32 + loc);
	}
	return (0);
}



/*(11)**( v 1.0 )*************************************************************************************************/
// FUNCAO:  Retorna um Booleano para a validacao de um campo numerico. tipo = [inteiro | natural | numero]
// EXEMPLO: onBlur="validaNumerico(this.id, 2, 'inteiro', '\nEste campo nao suporta valores neste formato!')"
// AUTOR:   DBA Engenharia de Sistemas LTDA. - (--/--/----)  // TESTADA: IE / MF
//****************************************************************************************************************/

function validaNumerico(strControl, tam, tipo, strMessage) {
	var strValue = document.getElementById(strControl).value;
		
	if (tipo == "numero") {
		//Usar o replace para tratar a virgula
		//Replace(strValue, ",", ".");
		if (!isNaN(strValue)) {
			if (strValue.length <= tam) {
				return true;
			} else {
				strMessage = "Comprimento do campo numerico excedido.";
				alert(strMessage);
				//errorMessage(strControl, strMessage);
				return false;
			}
		}
	}

	if (tipo == "inteiro") {
		if (!isNaN(strValue)) {
			if (parseFloat(strValue) == parseInt(strValue,10)) {
				if (strValue.length <= tam) {
					return true;
				} else {
					strMessage = "Comprimento do campo num?rico excedido.";
					alert(strMessage);
					//errorMessage(strControl, strMessage);
					return false;
				}
			}
		}
	}

	if (tipo == "natural") {
		if (!isNaN(strValue)) {
			//Inclusao do zero para atender ao sistema.
			if (parseFloat(strValue) == parseInt(strValue,10) && parseInt(strValue,10) >= 0) {
				if (strValue.length <= tam) {
					return true;
				} else {
					strMessage = "Comprimento do campo num?rico excedido.xxx";
					alert(strMessage);
					//errorMessage(strControl, strMessage);
					return false;
				}
			}
		}
	}
	
	alert(strMessage);
	//errorMessage(strControl, strMessage);
	return false;
}


/*(12)**( v 1.0 )*************************************************************************************************/
// FUNCAO:  Retorna um Booleano para a validacao de um campo numerico.
// EXEMPLO: onBlur="ehNumero(this.id, 'n?o ? n?merico\nPor favor, digite um valor num?rico!')"
// AUTOR:   DBA Engenharia de Sistemas LTDA. - (--/--/----)  // TESTADA: IE / MF
//****************************************************************************************************************/

function ehNumero(strControl, strMessage) {
	var strValue = document.getElementById(strControl).value;
	
	if (!isNaN(strValue)) {
		return true;
	}
	
	alert("\"" + strValue + "\"" + " " + strMessage);
	return false;
}

/* Fun??o que verifica se o conte?do do campo ? num?rico.
	Esta fun??o deve ser colocada no onKeyUp do input='text'
	txtField - Nome do campo no formul?rio
	value - valor do campo 
*/
function cNumber(txtField,value) {
	if ( value.length == 0 )
	{
		return ;
	} else if ( isNaN( value ) ) {
		var length = value.length;
		value = value.substring(0,length-1);
		eval('document.forms[0].'+txtField+'.value = value');
	}
}
