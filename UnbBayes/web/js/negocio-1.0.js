// JavaScript Document


/************************************************************************* INDICE *************************************************************************************
01) - capturaNavegador()                    - Captura nome e versao do navegador para tratar diferencas.
02) - validaEstado(campo, boolean)          - Validacao das siglas dos estados brasileiros.
03) - validaEmail(campo, boolean)           - Validacao de campo E-mail.
04) - validadorCEP(campo, boolean)          - Validacao de campo CEP.
05) - validadorCPF(id, str)                 - Retorna um Booleano para a validacao do campo CPF no formato "00000000000" (somente numeros).
06) - validadorCNPJ(id, str)                - Retorna um Booleano para a validacao do campo CNPJ no formato "00000000000000" (somente numeros).
07) - validadorCEI(campo)                   - Retorna um Booleano para a validacao do CEI.
08) - formataCoeficiente(campo, [0-9])      - Retorna um Booleano para a validacao do coeficiente.
09) - abrePopUp()                           - Abre uma janela pop-up.
************************************************************************** INDICE *************************************************************************************/

/*(01)**( v 1.0 )*************************************************************************************/
// FUNCAO:  Captura nome e versao do navegador para tratar diferencas.
// EXEMPLO: onClick="capturaNavegador()"
// AUTOR:   DBA Engenharia de Sistemas LTDA. (25/08/2006) -   // TESTADA: IE / MF
//***************************************************************************************************/

function capturaNavegador(){
	//alert ("Nome: " + navigator.appName + "\n\nVersao: " + navigator.appVersion);
	return navigator.appName + " " + navigator.appVersion;  
}



/*(02)**( v 1.0 )**********************************************************************/
// FUNCAO:  Validacao das siglas dos estados brasileiros.
// EXEMPLO: onBlur="validaEstado(this.value, true)"
// AUTOR:   DBA Engenharia de Sistemas LTDA. - --/--/----  // TESTADA: IE / MF 1.5
//*************************************************************************************/

function validaEstado(strEstado, alertFlag) {
	var regExp = /^(AC|AL|AP|AM|BA|CE|DF|ES|GO|MA|MT|MS|MG|PA|PB|PR|PE|PI|RJ|RN|RS|RO|RR|SC|SP|SE|TO)$/i;
	
	//Checando se a string passada como parametro bate com a expressao regular
	var tmpArray = strEstado.match(regExp);
	
	//Se o array for nulo, entao o estado nao eh valido
	if(tmpArray === null) {
		//Verificando se devemos mandar uma mensagem de erro
		if(alertFlag) {
			alert("Estado invalido! Por favor, corrija o erro");
		}		
		return(false);
	}
	return(true);
}



/*(03)**( v 1.0 )**********************************************************************/
// FUNCAO:  Validacao de campo E-mail.
// EXEMPLO: onBlur="validaEmail(this.value, true)"
// AUTOR:   DBA Engenharia de Sistemas LTDA. - --/--/----  // TESTADA: IE / MF 1.5
//*************************************************************************************/

function validaEmail(strEmail, alertFlag) {
	var regExp = /^[a-zç\d]((\.)?([a-zç\d_\-])+)+@([a-zç\d_\-])+(\.([a-zç\d_\-])+)+$/;
	
	//Checando se a string passada como parametro bate com a expressao regular
	var tmpArray = strEmail.match(regExp);
	
	//Se o array for nulo, entao o e-mail não eh valido
	if (tmpArray === null) {
		//Verificando se devemos mandar uma mensagem de erro
		if (alertFlag) {
			alert("Formato de E-mail incorreto!\nPor favor, corrija o erro.");
		}		
		//Retornando o resultado
		return(false);
	}
	return(true);
}



/*(04)**( v 1.0 )*************************************************************************/
// FUNCAO:  Verifica se campo CEP possui 8 digitos.
// EXEMPLO: onBlur="this.value = validadorCEP(this.value, true)"
// AUTOR:   DBA Engenharia de Sistemas LTDA. (--/--/----)  // TESTADA: IE / MF
//***************************************************************************************/

function validadorCEP(strCEP, alertFlag) {
	var regExp = /^[\d]{8}$/;
	//Checando se a string passada como parametro bate com a expressao regular
	var tmpArray = strCEP.match(regExp);
	
	//Se o array for nulo, entao o CEP nao eh valido
	if (tmpArray === null) {
		//Verificando se devemos mandar uma mensagem de erro
		if (alertFlag) {
			alert("CEP invalido! Por favor, corrija o erro.");
		}
		//Retornando o resultado
		return(false);
	}
	return(true);
}



/*(05)**( v 1.0 )***********************************************************************************************************************************************/
// FUNCAO:  Retorna um Booleano para a validaçao do campo CPF no formato "00000000000" (somente numeros).
// EXEMPLO: onBlur="validadorCPF(id, 'é um CPF inválido!\nPor favor corrija')"
// AUTOR:   DBA Engenharia de Sistemas LTDA. (--/--/----)  // TESTADA: IE / MF
//*************************************************************************************************************************************************************/	

function validadorCPF(strControl, strMessage) {
	var sim;
	var strValue = document.getElementById(strControl).value;

	if (strValue.length != 11) {
		sim = false;
	} else {
		sim = true;
	}
	// valida o primeiro digito
	if (sim) {
		for (var i=0; ((i<=(strValue.length-1))&& sim); i++) {
 			var val = strValue.charAt(i);
			if (!isNaN(strValue)) {
				sim = true;
				if (parseFloat(strValue) != parseInt(strValue,10)) {
					sim = false;
				}
			}
		}
		if (sim) {
			var soma = 0;
			for (i=0;i<=8;i++) {
				val = eval(strValue.charAt(i));
				soma = soma + (val*(i+1));
			}
			var resto = soma % 11;
			if (resto>9) {
			 	var dig = resto -10;
			} else {
				dig = resto;
			}
			if ( dig != eval(strValue.charAt(9)) ) {
				sim=false;
			} else { // valida o segundo digito
				soma = 0;
				for (i=0;i<=7;i++) {
					val = eval(strValue.charAt(i+1));
					soma = soma + (val*(i+1));
				}
				soma = soma + (dig * 9);
				resto = soma % 11;
				if ( resto > 9 ) {
					dig = resto - 10;
				} else {
					dig = resto;
				}
				if (dig != eval(strValue.charAt(10)) ) {
					sim = false;
				} else {
					sim = true;
				}
			}
		}
	}
	if (sim) {
		// alert("OK" );
		return true;
	} else {
		alert("\"" + strValue + "\" " + strMessage);
		return false;
	}
}



/*(06)**( v 1.0 )**************************************************************************************************/
// FUNCAO:  Retorna um Booleano para a validacao do campo CNPJ no formato "00000000000" (somente numeros).
// EXEMPLO: onBlur="this.value = validadorCNPJ(id, 'e um CNPJ invalido!\nPor favor corrija')"
// AUTOR:   DBA Engenharia de Sistemas LTDA. (--/--/----)  // TESTADA: IE / MF
//****************************************************************************************************************/	

function validadorCNPJ(strControl, strMessage) {
 	var sim;
	var strValue = document.getElementById(strControl).value;
	
	// verifica o tamanho
	if (strValue.length != 14) {
		sim = false;
		//alert ("Tamanho Invalido de CNPJ")
	} else {
		sim = true;
	}
	// verifica se e numero
	if (sim) {
		if (!isNaN(strValue)) {
			sim = true;
			if (parseFloat(strValue) != parseInt(strValue,10)) {
				sim = false;
			}
		}

		if (sim) {
			var m2 = 2;
			var soma1 = 0;
			var soma2 = 0;
			for (var i = 11; i>=0; i--) {
			
				var val = eval(strValue.charAt(i));
				var m1 = m2;
				
				if (m2 < 9) {
					m2 = m2+1;
				} else {
					m2 = 2;
				}				
				soma1 = soma1 + (val * m1);
				soma2 = soma2 + (val * m2);
			}
 
            soma1 = soma1 % 11;
			
			if (soma1 < 2) {
				var d1 = 0;
			} else {
				d1 = 11 - soma1;
			}

			soma2 = ( soma2 + (2 * d1) ) % 11;
			
			if ( soma2 < 2 ) {
				var d2 = 0;
			} else {
				d2 = 11 - soma2;
			}
			
			if ((d1 == strValue.charAt(12)) && 
				(d2 == strValue.charAt(13))) {
				//alert("CNPJ Valido");
				return true;
			} else {
				alert(strValue + " " + strMessage);
				return false;
			}
		}
	}
}



/*(07)**( v 1.0 )*********************************************************************************************/
// FUNCAO:  Retorna um Booleano para a validacao do CEI.
// EXEMPLO: onblur="this.value = validadorCEI(this.value)"
// AUTOR:   DBA Engenharia de Sistemas LTDA. (--/--/----)  // NAO TESTADA: IE / MF
//************************************************************************************************************/

function validadorCEI(VsCEI) {
    var vsAux;
    var vsNumVerCalc;
    var vsAuxCEI;
    var vsAuxNumVer;
    var vsMult;
    var viConcat;
    var viDigitoAux;
    var viDigito;
    var i;
    
    //gfunCalcCEI = False
    //CEI tem que ter 12 caracteres
    if (VsCEI.length != 12) {
		return false;
	}
		
        
    vsNumVerCalc = "74185216374";
	viConcat = 0;
    for (i=0; i < 11; i++) {
        vsAuxCEI = VsCEI.charAt(i);
        vsAuxNumVer = vsNumVerCalc.charAt(i);
        vsMult = eval(vsAuxCEI) * eval(vsAuxNumVer);
        viConcat = viConcat + vsMult;       
    }

	//tranforma para string	
	viConcat = viConcat + "";
			
    //Pega o Último(1) e o Penultimo(2) número da variavel concatenada para formar digito
    viDigitoAux = eval(viConcat.charAt(viConcat.length - 1)) + eval(viConcat.charAt(viConcat.length - 2));
    
    if ((viDigitoAux >= 11) && (viDigitoAux <= 18)) {
        viDigito = 20 - viDigitoAux;
    } else {
 		if (viDigitoAux == 10) {
		    viDigito = 1;
		} else {
			viDigito = 10 - viDigitoAux;
    	}
    }   
        
    //Condição para atender ao SFG. Aceitar o digito informado (0 ou 1),
    //mesmo que o resultado do calculo seja igual a 1.
    if ((viDigitoAux == 10) && (VsCEI.charAt(11) < "2")) {
        viDigito = VsCEI.charAt(11);
    }
	
    //verifica se o CEI informado é = ao calculo efetuado
    if (VsCEI.charAt(11) != viDigito) {
        return false;
    }
    return true;
}



/*(08)**( v 1.0 )*********************************************************************/
// FUNCAO:  Retorna um Booleano para a validacao do coeficiente.
// EXEMPLO: onblur="this.value = formataCoeficiente(this.value, 2)"
// AUTOR:   DBA Engenharia de Sistemas LTDA. (--/--/----)  // 
//************************************************************************************/

function formataCoeficiente(coeficiente, numCasas) {
    var sufixo = coeficiente.substring(coeficiente.length - numCasas);
    var prefixo = coeficiente.substring(0, coeficiente.length - sufixo.length);
    var buf ="";
    var i = 0;
	
	sufixo = "," + sufixo;
	
    do {
		if(i >= prefixo.length) {
            break;
        }
        if(prefixo.charAt(i) != '0') {
            buf = buf + prefixo.substring(i);
            break;
        }
        i++;
    } while(true);
		if(buf.length === 0) {
        	buf = buf + "0";
    	}
    return buf + sufixo;
}


/*(09)**( v 1.3 )*************************************************************************************/
// FUNÇÃO:  Abre pop-up.
// EXEMPLO: onClick="abrePopUp()"
// AUTOR:   DBA Engenharia de Sistemas LTDA. (09/10/2006)  // TESTADA: IE6+ / MF1.5+
//***************************************************************************************************/

// Utilize os valores (yes|no).

function abrePopUp() {
	
	if (navigator.appName.substring(0,5) == "Netsc") {
		netscape.security.PrivilegeManager.enablePrivilege("UniversalBrowserWrite");
	}	
	
  var vDependent = "dependent=no";    // Torna a janela popup dependente de sua origem. Fechando a origem, a popup tambem fecha. (somente no MF) 
   var vLocation = "location=yes";      // Habilita a exibicao a barra de endereco.
    var vMenubar = "menubar=yes";       // Habilita a exibicao da barra de menu (Arquivo - Editar - Exibir ...).
  var vResizable = "resizable=yes";     // Permite que o usuario altere o tamanho da janela popup. 
 var vScrollbars = "scrollbars=yes";    // Habilita a exibicao das barras de rolagem.
     var vStatus = "status=yes";        // Habilita a exibicao das barras status.
    var vToolbar = "toolbar=yes";       // Habilita a exibicao das barras de ferramentas.
      var vModal = "modal=yes";        // Define o foco na janela poppu enquanto a mesma existir. (somente no MF) 
    
  
/* Utilize valores numericos sempre com (px) no final. */
	
        var vTop = "top=10px";          // Define posicao inicial da janela em relacao ao topo. 
       var vLeft = "left=10px";         // Define posicao inicial da janela em relacao ao canto esquerdo.
      var vWidth = "width=800px";       // Define a largura inicial da janela.
     var vHeight = "height=600px";      // Define a altura inicial da janela. (no IE define a janela inteira. no MF define a area de exibicao).
     

	var propriedades = "'" + vDependent + 
					   "," + vLocation + 
					   "," + vMenubar + 
					   "," + vResizable + 
					   "," + vScrollbars + 
					   "," + vStatus + 
					   "," + vToolbar + 
					   "," + vModal +		
					   "," + vTop + 
					   "," + vLeft + 
					   "," + vWidth + 
					   "," + vHeight + 
					   "'";
		   
	if (navigator.appName.substring(0,5) == "Netsc") {
		window.open('http://www.dba.com.br','',propriedades);
	} else if (navigator.appName.substring(0,5) == "Micro") {
		window.showModalDialog('http://www.dba.com.br','',propriedades);
	} else {
		alert ("Este navegador nao e suportado!")
	}
}


