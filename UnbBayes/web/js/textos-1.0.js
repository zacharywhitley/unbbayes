// JavaScript Document

/************************************************************************* INDICE *************************************************************************************
01) - primMaiuscula(campo)                        - Transforma 1a letra do texto em MAIUSCULA e o restante em MINUSCULAS.
02) - primMinuscula(campo)                        - Transforma 1a letra do texto em  MINUSCULA.
03) - minusculas(campo)                           - Transforma o texto em MINUSCULAS.
04) - removeNaoNumericos(campo)                   - Retira caracteres nao-numericos de uma String.
05) - ehString(campo)                             - Verifica se a string so contem letras e retorna um Booleano. (Nao inclui caracteres especiais)
06) - validaBranco(campo)                         - Verifica se a string esta vazia e retorna um Booleano.
07) - substituir(campo1, campo2, campo3)          - Modifica os valores de um campo (Expression) em funcao dos criterios passados nos outros dois (Find, Replace).
08) - trim(campo)                                 - Retira espaços em branco do inicio e do fim do campo usando rTrim() e lTrim().
09) - lTrim(campo)                                - Retira espaços em branco do inicio do campo.
10) - rTrim(campo)                                - Retira espaços em branco do fim do campo.
11) - validaCampoTexto(campo, boolean)            - Verifica se a string so contém  caracteres alfa-numericos, espaços e underscores e retorna um Booleano.
12) - existeNaString(campo1, campo2)              - Retorna um Long especificando a posicao 1a ocorrência da String2 na String1.
************************************************************************** INDICE *************************************************************************************/



/*(01)**( v 1.0 )*************************************************************************/
// FUNCAO:  Transforma 1a letra do texto em MAIUSCULA e o restante em MINUSCULAS.
// EXEMPLO: onBlur="this.value = primMaiuscula(this.value)"
// AUTOR:   DBA Engenharia de Sistemas LTDA. (24/08/2006)  // TESTADA: IE / MF 1.5 1.5
//***************************************************************************************/

function primMaiuscula(campo){
	var texto = campo;
	var prim  = texto.charAt (0).toUpperCase();
	var resto = texto.substring (1).toLowerCase();	
	var formt = prim + resto;
		
	if (texto.length === 0) {
		return texto;
	} else {
		return formt;
	}
}



/*(02)**( v 1.0 )**********************************************************************/
// FUNCAO:  Transforma 1a letra do texto em MINUSCULA.
// EXEMPLO: onBlur="this.value = primMinuscula(this.value)"
// AUTOR:   DBA Engenharia de Sistemas LTDA. (24/08/2006)  // TESTADA: IE / MF 1.5
//*************************************************************************************/

function primMinuscula(campo){
	var texto = campo;
	var prim  = texto.charAt(0).toLowerCase();
	var resto = texto.substring (1);	
	var formt = prim + resto;
		
	if (texto.length === 0) {
		return texto;
	} else {
		return formt;
	}
}	



/*(03)**( v 1.0 )**********************************************************************/
// FUNCAO:  Transforma o texto em MINUSCULAS.
// EXEMPLO: onBlur="this.value = minusculas(this.value)"
// AUTOR:   DBA Engenharia de Sistemas LTDA. (24/08/2006)  // TESTADA: IE / MF 1.5
//*************************************************************************************/

function minusculas(campo){
	var texto  = campo;
	var textoF = campo.substring (0).toLowerCase();
	
	if (texto.length === 0) {
		return texto;
	} else {
		return textoF;
	}
}	



/*(04)**( v 1.0 )**********************************************************************/
// FUNCAO:  Retira caracteres nao-numericos de uma String.
// EXEMPLO: onBlur="this.value = removeNaoNumericos(this.value)"
// AUTOR:   DBA Engenharia de Sistemas LTDA. - 25/05/2000  // TESTADA: IE / MF 1.5
//*************************************************************************************/

function removeNaoNumericos(campo) {
	var aux = '';
	for(var i = 0 ; i<campo.length ; i++) {
		if(campo.charAt(i)>='0' && campo.charAt(i)<='9') {
			aux = aux + campo.charAt(i);
		}
	}
	return aux;
}



/*(05)**( v 1.0 )***********************************************************************/
// FUNCAO:  Verifica se a string so contem letras e retorna um Booleano. (Nao inclui caracteres especiais)
// EXEMPLO: ""
// AUTOR:   DBA Engenharia de Sistemas LTDA. - 24/03/2000  // TESTADA: IE / MF 1.5
//**************************************************************************************/

function ehString(campo) {
   var indice;
   
   for (indice = 0 ; indice < campo.length ; indice++) {
      if ( ( ( campo.charAt(indice) < "a" ) || ( campo.charAt(indice) > "z" ) ) && 
		   ( ( campo.charAt(indice) < "A" ) || ( campo.charAt(indice) > "Z" ) ) ) {
			return (false);
		}
	}
	return (true) ;
}



/*(06)**( v 1.0 )***********************************************************************/
// FUNCAO:  Verifica se a string esta vazia e retorna um Booleano.
// EXEMPLO: validaBranco(this.value)
// AUTOR:   DBA Engenharia de Sistemas LTDA. - --/--/----  // TESTADA: IE / MF 1.5
//**************************************************************************************/

function validaBranco(strTexto) {
	var regExp = /^(\s)*$/;
	
	//Checando se a string passada como parametro bate com a expressao regular
	var tmpArray = strTexto.match(regExp);
	
	//Se o array for nulo, entao a string nao esta vazia nem contem somente espaços
	if(tmpArray === null) {
		return(false);
	}
	return(true);
}



/*(07)**( v 1.0 )*************************************************************************************************************/
// FUNCAO:  Modifica os valores de um campo (Expression) em funcao dos criterios passados nos outros dois (procurar, substituir).
// EXEMPLO: onClick="campo.value = fSubstituir(campo.value, procurar.value, substituir.value)"
// AUTOR:   DBA Engenharia de Sistemas LTDA. - --/--/----  // TESTADA: IE / MF 1.5
//****************************************************************************************************************************/

function substituir(campo, procurar, substituir) { 
	var temp = campo;
	var a = 0;
	var b = 0;

	for (var i = 0; i < campo.length; i++)	{
		a = temp.indexOf(procurar, a + b);
		if (a == -1) {
			break;
		} else {
			b = substituir.length;
			temp = temp.substring(0, a) + substituir + temp.substring((a + procurar.length));
		}
	}
	return temp;
}



/*(08)**( v 1.0 )************************************************************************************************************/
// FUNCAO:  Retira espaços em branco do início e do fim do campo usando rTrim() e lTrim().
// EXEMPLO: onBlur="this.value = trim(this.value)"
// AUTOR:   DBA Engenharia de Sistemas LTDA. - --/--/----  // TESTADA: IE / MF 1.5
//***************************************************************************************************************************/

function trim(string) {
	if (string === null) {
		return (false);
	}
	return rTrim(lTrim(string));
}



/*(09)**( v 1.0 )************************************************************************************************************/
// FUNCAO:  Retira espaços em branco do inicio do campo.
// EXEMPLO: ""
// AUTOR:   DBA Engenharia de Sistemas LTDA. - --/--/----  // TESTADA: IE / MF 1.5
//***************************************************************************************************************************/

function lTrim(string) {
	var i = 0;
	var j = string.length - 1;

	if (string === null) {
		return (false);
	}

	for (i = 0; i < string.length; i++)	{
		if (string.substr(i, 1) != ' ' && string.substr(i, 1) != '\t') {
			break;
		}
	}

	if (i <= j) {
		return (string.substr(i, (j+1)-i));
	} else {
		return ('');
	}
}



/*(10)**( v 1.0 )************************************************************************************************************/
// FUNCAO:  Retira espaços em branco do fim do campo.
// EXEMPLO: ""
// AUTOR:   DBA Engenharia de Sistemas LTDA. - --/--/----  // TESTADA: IE / MF 1.5
//***************************************************************************************************************************/

function rTrim(string) {
	var i = 0;
	var j = string.length - 1;

	if (string === null) {
		return (false);
	}

	for (j = string.length - 1; j >= 0; j--) {
		if (string.substr(j, 1) != ' ' && string.substr(j, 1) != '\t') {
		break;
		}
	}

	if (i <= j) {
		return (string.substr(i, (j+1)-i));
	} else {
		return ('');
	}
}



/*(11)**( v 1.0 )***********************************************************************************************************/
// FUNCAO:  Verifica se a string so contem caracteres alfa-numericos, espaços e underscores e retorna um Booleano.
// EXEMPLO: onBlur="validaCampoTexto(this.value, true)"
// AUTOR:   DBA Engenharia de Sistemas LTDA. - --/--/----  // TESTADA: IE / MF 1.5
//***************************************************************************************************************************/

function validaCampoTexto(strTexto, alertFlag) {	
	var regExp = /^[\w\s\/\.\-]+$/;
	
	//Checando se a string passada como parâmetro bate com a expressao regular
	var tmpArray = strTexto.match(regExp);
	
	//Se o array for nulo, entao o texto nao e valido
	if (tmpArray === null) {
		//Verificando se temos que mandar uma mensagem de erro
		if (alertFlag) {
			alert("Campo invalido!\nPor favor, digite apenas caracteres alfa-numericos.");
		}
		//Retornando o resultado
		return(false);
	}
	return(true);
}



/*(12)**( v 1.0 )******************************************************************************/
// FUNCAO:  Retorna um Long especificando a posicao 1a ocorrencia da String2 na String1.
// EXEMPLO: ""
// AUTOR:   DBA Engenharia de Sistemas LTDA. - (--/--/----)  // TESTADA: IE / MF
//*********************************************************************************************/

function existeNaString(String1, String2) {
	var a = 0;
	
	if (String1 === null || String2 === null) {
		return (false);
	}

	String1 = String1.toLowerCase();
	String2 = String2.toLowerCase();	
	a = String1.indexOf(String2);
	
	if (a == -1) {
		return 0;
	} else {
		return a + 1;
	}
}