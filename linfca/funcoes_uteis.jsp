<script language="javascript">
            function ehDigito(str) {
                if (str.length != 1) {
                    return false;
                }
                else {
                    digitos = "0123456789";
                    flag = false;

                    for (i=0; i < digitos.length; i++) {
                        if (digitos.charAt(i) == str) {
                            flag = true;
                        }
                    }
                    return flag;
                }
            }



            /* Esta funcao recebeu um numero com casas decimais e troca o separador por outro.
               Caso o numero nao possua casas decimais, nada eh feito nele.
            */
            function trocaSeparador(num, old, novo) {
                return num.toString().replace(old, novo);
            }


            /* Esta funcao avalia se um numero possui casas decimais verificando a existencia
            do separador
            */
            function possuiCasasDecimais(num, separador) {
                if (num.toString().indexOf(separador) == -1) {
                    return false;
                }
                else return true;
            }


            /* Esta funcao recebe um numero e retorna o mesmo com casas decimais e com o separador desejado.
               Caso o numero ja possua casas decimais, a funcao apenas troca o separador antigo pelo novo.
            */
            function colocaCasasDecimais(num, old, novo) {
                if (possuiCasasDecimais(num, old)) {
                    return trocaSeparador(num, old, novo);
                }
                else if (possuiCasasDecimais(num, novo)) {
                    return num;
                }
                else return num.toString() + novo + "00";
            }



            /* Esta funcao formata um numero com quantas casas decimais se quiser.
               Nao importa se o numero possui ou nao casas decimais.
            */
            function formataNumero(num, casas, sep_old, sep_novo) {
                num = colocaCasasDecimais(num, sep_old, sep_novo);
                aux = num.toString().indexOf(",");

                if (aux != -1) num = num.toString().substring(0, eval(aux + casas + 1));
                return num;
            }



            /* Este funcao soma dois numeros em que o separador eh virgula */
            function soma(num1, num2) {
                num1 = trocaSeparador(num1, ',', '.');
                num2 = trocaSeparador(num2, ',', '.');
                aux = eval(num1.toString() + " + " + num2.toString());
                return colocaCasasDecimais(aux, '.', ',');
            }



            /* Esta funcao recebe uma string e elimina todos os caracteres iguais a carac nela contida */
            function filtraString(strng, carac) {
                while (strng.indexOf(carac) != -1) {
                    index = strng.indexOf(carac);
                    if (index == "0") {
                            strng = strng.substring(1,strng.length);
                    }
                    else if (index == strng.length) {
                        strng = strng.substring(0, strng.length - 1);
                    }
                    else strng = strng.substring(0, index) + strng.substring(eval(index + 1), strng.length);
                }
                return strng;
            }



        // O separador de digitos padrao é a virgula!
            function incrementaSeparador (str, direcao) {
                separador = ","; //separador padrao
                indexSeparador = -1;

                indexVirgula = str.indexOf(",",0);
                indexPonto = str.indexOf(".", 0);
                if ((indexVirgula != -1) && (indexPonto != -1)){
                    return -1; // significa q a string possui ponto e virgula (erro!)
                }
                else if (indexVirgula != -1) {
                    separador = ",";
                    indexSeparador = indexVirgula;
                }
                else if (indexPonto != -1) {
                    separador = ".";
                    indexSeparador = indexPonto;
                }
                else separador = null;

                if (indexSeparador == -1) {
                    return str;
                }
                else if (direcao == "1"){
                    str = str.substring(0,indexSeparador) + str.charAt(indexSeparador + 1) + str.charAt(indexSeparador) + str.substring(indexSeparador + 2, str.length);
                    return str;
                }
                else if (direcao == "-1") {
                    if (indexSeparador == 1) {
                        return str.substring(0,1) + str.substring(2,3);
                    }
                    else return str.substring(0,indexSeparador-1) + str.substring(indexSeparador, indexSeparador + 1) + str.substring(indexSeparador-1, indexSeparador) + str.substring(indexSeparador + 1, str.length);
                }

            }




            /* Esta funcao gerencia a posicao da virgula
               conforme o usuario entra com digitos num input text.

               ex: f(123) = 1,23  , onde f(123) eh o resultado desta
                                        funcao aplicada ao valor 123.
                   Supondo agora q o usuario entra com "4", temos:

                   f(1,234) = 12,34

            */
            // O separador de digitos padrao é a virgula!
            function atualizaString(str) {
                separador = ",";  //separador padrao
                indexSeparador = str.indexOf(separador);
                if (indexSeparador == -1) {
                    if (str.length == 3) {
                        return str.substring(0,1) + separador + str.substring(1,3);
                    }
                    else {
                        return str;
                    }
                }
                else if (str.indexOf(separador, indexSeparador + 1) > indexSeparador) {
                    alert("Voce deve entrar apenas com digitos");
                    return str.substring(0,str.length - 1);
                }
                else if (numCasasDecimais(str) == "0") {
                    return str.substring(0,indexSeparador);
                }
                else if (numCasasDecimais(str) == "1") {
                    return incrementaSeparador(str, "-1");;
                }
                else {
                    return incrementaSeparador(str, "1");
                }

            }


            // funcao que retorna o numero de casas decimais de 'str'
            // atencao: separador padrao eh a virgula!
            function numCasasDecimais(str) {
                separador = ","; //separador padrao!
                indexSeparador = str.indexOf(separador);
                if ((indexSeparador == -1) || (indexSeparador == str.length-1)) {
                    return "0"
                }
                else return str.length - indexSeparador - 1;
            }



            /* Esta funcao gerencia a formatacao da data dinamicamente, ou seja,
               conforme a entrada de caracteres pelo usuario.

               Entrada: String data
               Saida: String data


               separador padrao: "/"
               formato padrao: "dd/mm/aaaaa"
            */
            function preencheDataDinamico(str, format) {
                if (str.length > 0) {
                    separador = "/";
                    formatoPadrao = "dd/mm/aaaa";
                    if (format == formatoPadrao) {
                       if (str.length < 2) {
                            return str;
                        }
                        else if ((str.length == 2) || (str.length == 5)) {
                            return str + separador;
                        }
                        else if (str.charAt(str.length-1) == separador) {
                                return str.substring(0,str.length-1);
                        }
                        else return str;
                    }
                    else {
                        alert("formato especificado nao aceito ainda");
                        return "";
                    }
                }
                else return str;
            }


</script>