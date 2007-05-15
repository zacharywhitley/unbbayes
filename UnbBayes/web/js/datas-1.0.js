// JavaScript Document

/************************************************************************** ÍNDICE *************************************************************************************
01) - validaData(this.value)                        - Valida data no formato dd/mm/aaaa. Critica ano bissexto.
02) - formatadorData()                              - Permite apenas a digitacao dos caracteres [/,0,1,2,3,4,5,6,7,8,9].
03) - anoBissexto(arg1.value)                       - Valida o campo ano(bissexto) de uma data, retornando um Booleano.
04) - verificaMM_AA(arg1.value)                     - Valida data no formato mm/aaaa. Retorna Booleano.
05) - comparaData(arg1.value, arg2.value)           - Retorna um Booleano (true) se a data inicial for menor/igual que a data final. 
06) - diferencaMinutos(arg1.value, arg2.value)      - Retorna a diferença em minutos entre duas datas. Aceita os formatos (mm/dd/aaaa); (mm/dd/aa) e (aaaa/mm/dd)
07)	- diferencaDatas(arg1.value, arg2.value)		- Retorna a diferença em SEMANAS, DIAS, HORAS, MINUTOS E SEGUNDOS entre duas datas.
08)	- diaDaSemana(this.value)               		- Retorna o dia da semana para uma data passada no formato dd/mm/aaaa.
************************************************************************** ÍNDICE *************************************************************************************/



/*(01)**( v 1.2 )*************************************************************************/
// FUNÇÃO:  Valida data no formato dd/mm/aaaa. Critíca ano bissexto.
// EXEMPLO: onBlur="validaData(this.value)"
// AUTOR:   DBA Engenharia de Sistemas LTDA. (01/09/2006)  // TESTADA: IE / MF
//***************************************************************************************/

function validaData(campo) {
	var dt;
	var i;
	var aux;
	var str;
	var charsValidos = "0123456789";
	var dia = campo.substring(0, 2);
	var barra1 = campo.substring(2, 3);
	var mes = campo.substring(3, 5);
	var barra2 = campo.substring(5, 6);
	var ano = campo.substring(6, 10);
	
	dt = dia + mes + ano;

	for (i = 0; i <= 7; i++) {
		aux = dt.substring(i, ++i);
		str = charsValidos.indexOf(aux);
		
		if (str == -1) {
			alert("O formato de data correto e dd/mm/aaaa");
			return(false);
		}
	}

	//Validacao do numero de dígitos
	if (campo.length < 1) {
		return (false);		
	}
	if (campo.length != 10) {
		alert("O formato de data correto e dd/mm/aaaa");
		return (false);
	} else { 
		if ((dia > 31) || (dia < 1)) {
			alert("Dia incorreto.\nPor favor, digite um dia valido!");
			return (false);		
		}
		if ((mes > 12) || (mes < 1)) {
			alert("Mes incorreto.\nPor favor, digite um mes valido!");
			return (false);		
		}
		if (barra1 != '/')  {
			alert("Separador invalido!\nO formato correto e dd/mm/aaaa");
			return(false);
		}
		if (barra2 != '/')  {
			alert("Separador invalido!\nO formato correto e dd/mm/aaaa");
			return(false);
		}
		if ((mes == 4 || mes == 6 || mes == 9 || mes == 11) && (dia == 31)) {
			alert("O mês " + mes + " não possui " + dia + " dias!\nPor favor, corrija a data.");
			return (false);		
		}		
		if (mes == 2) {
			aux = parseInt(ano/4);
			/*
			if (isNaN(aux)){
				alert("false")
				return(false);
			}
			*/
			if (dia > 29) {
				alert("Dia incorreto.\nPor favor, digite um dia valido!");
				return(false);
			}
			if (dia == 29 && ((ano/4) != parseInt(ano/4))) {
				alert("O ano " + ano + " nao e bissexto!");
				return(false);
			}
		}	
	}	
	//alert("Data Ok");
	return(true);
}


/*(02)**( v 1.0 )*************************************************************************/
// FUNCAO:  Permite apenas a digitacao dos caracteres [/,0,1,2,3,4,5,6,7,8,9].
// EXEMPLO: onKeyPress="blockDate()"
// AUTOR:   DBA Engenharia de Sistemas LTDA. (01/09/2006)  // TESTADA: IE / MF
//****************************************************************************************/

function formatadorData() {
    //Permite apenas os caracteres [/,0,1,2,3,4,5,6,7,8,9]
	if ((event.keyCode < 47) || (event.keyCode > 57)) {
		event.returnValue = false;
	}
}


/*(03)**( v 1.0 )*************************************************************************/
// FUNCAO:  Valida o campo ano(bissexto) de uma data, retornando um Booleano.
// EXEMPLO: onBlur="anoBissexto(this.value)"
// AUTOR:   DBA Engenharia de Sistemas LTDA. (08/02/2000)  // TESTADA: IE / MF
//****************************************************************************************/

function anoBissexto(Ano) {
	if (Ano.substring(Ano.length-2,Ano.length) == "00") {
		if (Ano.substring(0,Ano.length-2) % 4 === 0) {
			return (true);
		} else {
			return (false);
		}
    } else {
		if (Ano % 4 === 0) {
			return(true);
		} else {
		  return(false);
		}
	}
}


/*(04)**( v 1.0 )*************************************************************************/
// FUNCAO:  Valida data no formato mm/aaaa. Retorna Booleano.
// EXEMPLO: ""
// AUTOR:   DBA Engenharia de Sistemas LTDA. (--/--/----)  // TESTADA: IE / MF
//****************************************************************************************/

function verificaMM_AA(data) {
	var dt;
	var i;
	var aux;
	var str;
	var charsValidos = "0123456789";
	var mes = data.substring(0, 2);
	var barra2 = data.substring(2, 3);
	var ano = data.substring(3, 7);
	
	if (data.length === 0) {
		return(true);
	}
	
	if (data.length != 7) {
		return(false);
	}
	
	mes = data.substring(0, 2);
	barra2 = data.substring(2, 3);
	ano = data.substring(3, 7);
	dt = mes + ano;
	
	for (i = 0; i <= 7; i++) {
		aux = dt.substring(i, ++i);
		str = charsValidos.indexOf(aux);
		
		if (str == -1) {
			return(false);
		}
	}
	
	if (mes < 1 || mes > 12) {
		return(false);
	}
	
	if (barra2 != '/') {
		return(false);
	}
	
	if (ano < 1) {
		return(false);
	}	
	return(true);
}



/*(05)**( v 1.0 )****************************************************************************/
// FUNCAO:  Retorna um Booleano (true) se a data inicial for menor/igual que a data final.
// EXEMPLO: onBlur="comparaData(this.value, data2.value)"
// AUTOR:   DBA Engenharia de Sistemas LTDA. (adaptação) (11/09/2006)  // TESTADA: IE / MF
//*******************************************************************************************/

function comparaData(dtinicial,dtfinal) {
	var diaini = parseFloat(dtinicial.substring(0,2));
	var mesini = parseFloat(dtinicial.substring(3,5));
	var anoini = parseFloat(dtinicial.substring(6,10));
	//alert (diaini + " " + mesini + " " + anoini)
	
	var diafin = parseFloat(dtfinal.substring(0,2));
	var mesfin = parseFloat(dtfinal.substring(3,5));
	var anofin = parseFloat(dtfinal.substring(6,10));
	//alert (diafin + " " + mesfin + " " + anofin)	
	
	if (anoini < anofin) {
		return (true);
	} else {
		if (anoini == anofin) {
			if (mesini < mesfin) {
				return (true);
			} else {
				if (mesini == mesfin) {
					if (diaini <= diafin) {
						return (true);
					} else {
						return (false);
					}	
				} else {
					return (false);
				}
			}		
		} else {
			return (false);
		}
	}
}



/*(06)**( v 1.0 )****************************************************************************/
// FUNCAO:  Retorna a diferença em minutos entre duas datas. 
//          Aceita os formatos (mm/dd/aaaa); (mm/dd/aa) e (aaaa/mm/dd)
// EXEMPLO: onBlur="dif.value = diferencaMinutos(this.value, data2.value)"
// AUTOR:   DBA Engenharia de Sistemas LTDA. (--/--/----)  // TESTADA: IE / MF
//*******************************************************************************************/

function diferencaMinutos(Date1, Date2) {
	var date1 = new Date();
	var date2 = new Date();
	var diff  = new Date();
	
	var date1temp = new Date(Date1);
	alert (date1temp);
	date1.setTime(date1temp.getTime());

	var date2temp = new Date(Date2);
	date2.setTime(date2temp.getTime());

	diff.setTime(Math.abs(date1.getTime() - date2.getTime()));
	var timediff = diff.getTime();

	var weeks = Math.floor(timediff / (1000 * 60 * 60 * 24 * 7));
	timediff -= weeks * (1000 * 60 * 60 * 24 * 7);

	var days = Math.floor(timediff / (1000 * 60 * 60 * 24)); 
	timediff -= days * (1000 * 60 * 60 * 24);

	var hours = Math.floor(timediff / (1000 * 60 * 60)); 
	timediff -= hours * (1000 * 60 * 60);

	var mins = Math.floor(timediff / (1000 * 60)); 
	timediff -= mins * (1000 * 60);

	var secs = Math.floor(timediff / 1000); 
	timediff -= secs * 1000;

	return (mins + (hours * 60) + (days * 24 * 60) + (weeks * 7 * 24 * 60));
}



/*(07)**( v 1.0 )****************************************************************************/
// FUNCAO:  Retorna a diferença em SEMANAS, DIAS, HORAS, MINUTOS E SEGUNDOS entre duas datas. 
//          Aceita os formatos (mm/dd/aaaa); (mm/dd/aa) e (aaaa/mm/dd)
// EXEMPLO: onBlur="dif.value = DateDiff(this.value, data2.value)"
// AUTOR:   DBA Engenharia de Sistemas LTDA. (adaptacao) (11/09/2006)  // TESTADA: IE / MF
//*******************************************************************************************/

function diferencaDatas(Date1, Date2) {
	var date1 = new Date();
	var date2 = new Date();
	var diff  = new Date();

	var date1temp = new Date(Date1);
	date1.setTime(date1temp.getTime());
	var date2temp = new Date(Date2);
	date2.setTime(date2temp.getTime());

	diff.setTime(Math.abs(date1.getTime() - date2.getTime()));
	var timediff = diff.getTime();

	var weeks = Math.floor(timediff / (1000 * 60 * 60 * 24 * 7));
	timediff -= weeks * (1000 * 60 * 60 * 24 * 7);

	var days = Math.floor(timediff / (1000 * 60 * 60 * 24));
	timediff -= days * (1000 * 60 * 60 * 24);

	var hours = Math.floor(timediff / (1000 * 60 * 60));
	timediff -= hours * (1000 * 60 * 60);

	var mins = Math.floor(timediff / (1000 * 60));
	timediff -= mins * (1000 * 60);

	var secs = Math.floor(timediff / 1000);
	timediff -= secs * 1000;

	return (weeks + " semanas, " + days + " dias, " + hours + " horas, " + mins + " minutos, e " + secs + " segundos.");
}



/*(08)**( v 1.0 )****************************************************************************/
// FUNCAO:  Retorna o dia da semana para uma data passada no formato dd/mm/aaaa.
// EXEMPLO: onBlur="campo1.value = diaDaSemana(this.value)"
// AUTOR:   DBA Engenharia de Sistemas LTDA. (adaptação) (14/09/2006)  // TESTADA: IE / MF
//*******************************************************************************************/

function diaDaSemana(data) {
	var pdia = data.substring(3,5);
	var pmes = data.substring(0,2);
	var pano = data.substring(6,10);
	var dataFormatada = pdia + "/" + pmes + "/" + pano;	
	var dia = new Array();
		dia[0] = "Domingo";
		dia[1] = "Segunda";
		dia[2] = "Terça";
		dia[3] = "Quarta";
		dia[4] = "Quinta";
		dia[5] = "Sexta";
		dia[6] = "Sabado";
	
	var hoje = new Date(dataFormatada);
	
	if (dataFormatada.length < 1) {
		hoje = new Date();
		return false;
	}
	
	var teste = (hoje.getDate() + " "  + (hoje.getMonth())+1 + " " + hoje.getYear());
	var diaSemana = hoje.getDay();
	
	return dia[diaSemana];
}
