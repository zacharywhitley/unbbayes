<%@ page import="com.miranteinfo.xml.util.*"%>
<%@ page import="org.w3c.dom.Element"%>
<%@ page import="java.util.GregorianCalendar"%>
<%@ page import="java.util.Calendar"%>




<%
/**
 * URL relativa da aplicação.
 */
String path = request.getContextPath();
%>







<%!
/**
 * Direciona o controle para a página de erro.
 * @author Tharsis Campos - Mirante
 */
private void mostrarErro(PageContext pageContext, Element xmlErro) throws Exception {
    mostrarErro(pageContext, xmlErro, null);
}

/**
 * Direciona o controle para a página de erro.
 * @author Tharsis Campos - Mirante
 */
private void mostrarErro(PageContext pageContext, Element xmlErro, String urlRedirect) throws Exception {
    if (pageContext == null || xmlErro == null) {
        return;
    }

    pageContext.getRequest().setAttribute("erro-resposta-xml", xmlErro);

    if (urlRedirect != null) {
        pageContext.getRequest().setAttribute("erro-url-back", urlRedirect);
    }

    pageContext.forward("/design/erro.jsp");
}




/**
 * Retorna um elemento HTML 'select' com todos os dias do mês.
 * O nome do elemento no formulario é 'nome'. O valor postado para esse elemento
 * é número do dia selecionado.
 */
private String selectDia(String nome) {
    return selectDia(nome, -1);
}

/**
 * @param diaSelecionado o numero do dia previamente selecionado
 */
private String selectDia(String nome, int diaSelecionado) {
    StringBuffer selectDia = new StringBuffer();
    int dia = diaSelecionado;

    if (dia < 1 || dia > 31) {
        dia = (new GregorianCalendar()).get(Calendar.DAY_OF_MONTH);
    }

   selectDia.append( "<select name='");
   selectDia.append(nome);
   selectDia.append("'>" );
   for (int i=1; i<32; i++) {
      selectDia.append( "<option value='" + i + "'" );
      if ( dia == i ) {
         selectDia.append( " selected" );
      }
      selectDia.append( ">" + i + "</option>" );
   }
   selectDia.append( "</select>" );

   return selectDia.toString();
}

/**
 * Retorna um elemento HTML 'select' com todos os meses do ano.
 * O nome do elemento no formulario é 'nome'. O valor postado para esse elemento
 * é número do mês selecionado (janeiro = 1).
 */
private String selectMes(String nome) {
    return selectMes(nome, -1);
}

/**
 * @param mesSelecionado o numero do mes previamente selecionado
 */
private String selectMes(String nome, int mesSelecionado) {
    StringBuffer selectMes = new StringBuffer();
    int mes = mesSelecionado;
    String[] meses = {"Janeiro", "Fevereiro", "Março", "Abril",
                      "Maio", "Junho", "Julho", "Agosto", "Setembro",
                      "Outubro", "Novembro", "Dezembro"};

    if (mes < 1 || mes > 12) {
        mes = (new GregorianCalendar()).get(Calendar.MONTH) + 1;
    }

    selectMes.append( "<select name='");
    selectMes.append(nome);
    selectMes.append("'>" );
    for (int i=1, len=meses.length+1; i<len; i++) {
        selectMes.append( "<option value='" + i + "'" );
        if ( mes == i ) {
            selectMes.append( " selected" );
        }
        selectMes.append( ">" + meses[i-1] + "</option>" );
    }
    selectMes.append( "</select>" );

    return selectMes.toString();
}

/**
 * Retorna um elemento HTML 'select' com todos os anos de 2000 a 2009.
 * O nome do elemento no formulario é 'nome'. O valor postado para esse elemento
 * é número do ano selecionado (ano 2000 = 2000);
 */
private String selectAno(String nome) {
    return selectAno(nome, -1);
}

/**
 * @param anoSelecionado o numero do ano previamente selecionado
 */
private String selectAno(String nome, int anoSelecionado) {
    StringBuffer selectAno = new StringBuffer();
    int ano = anoSelecionado;

    if (ano < 2000 || ano > 2010) {
        ano = (new GregorianCalendar()).get(Calendar.YEAR);
    }

    selectAno.append( "<select name='");
    selectAno.append(nome);
    selectAno.append("'>" );
    for (int i=2000; i<2010; i++) {
        selectAno.append( "<option value='" + i + "'" );
        if ( ano == i ) {
            selectAno.append( " selected" );
        }
        selectAno.append( ">" + i + "</option>" );
    }
    selectAno.append( "</select>" );

    return selectAno.toString();
}

/**
 * Monta uma ComboBox.
 * Vide a versão mais completa do método para documentação.
 */
private String select(
    String nomeForm,
    Element raiz,
    String elemLista,
    String nomeItem,
    String valorItem) {

    return select(nomeForm, raiz, elemLista, nomeItem, valorItem, null, null, false, -1, null);
}

/**
 * Monta uma ComboBox.
 * Vide a versão mais completa do método para documentação.
 */
private String select(
    String nomeForm,
    Element raiz,
    String elemLista,
    String nomeItem,
    String valorItem,
    String itemVazio) {

    return select(nomeForm, raiz, elemLista, nomeItem, valorItem, itemVazio, null, false, -1, null);
}

/**
 * Monta uma ComboBox.
 * Vide a versão mais completa do método para documentação.
 */
private String select(
    String nomeForm,
    Element raiz,
    String elemLista,
    String nomeItem,
    String valorItem,
    String itemVazio,
    String valorPreSelec) {

    return select(nomeForm, raiz, elemLista, nomeItem, valorItem, itemVazio, valorPreSelec, false, -1, null);
}

private String select(
    String nomeForm,
    Element raiz,
    String elemLista,
    String nomeItem,
    String valorItem,
    String itemVazio,
    String valorPreSelec,
    boolean selecMultipla) {

    return select(nomeForm, raiz, elemLista, nomeItem, valorItem, itemVazio, valorPreSelec, selecMultipla, -1, null);
}

private String select(String nomeForm,
                      Element raiz,
                      String elemLista,
                      String nomeItem,
                      String valorItem,
                      String itemVazio,
                      String valorPreSelec,
                      boolean selecMultipla,
                      int itensVisiveis,
                      String nomeClass) {

    return select(nomeForm, raiz, elemLista, nomeItem, valorItem, itemVazio, valorPreSelec, selecMultipla, itensVisiveis, nomeClass, null);
}

private String select(
    String nomeForm,       // nome do select no formulario
    Element raiz,          // elemento raiz
    String elemLista,      // elemento que será percorrido no raiz
    String nomeItem,       // elemento que será exibido no select
    String valorItem,      // elemento com o valor dos itens exibidos
    String itemVazio,      // nome do item vazio do select
    String valorPreSelec,  // valor do item que sera previamente selecionado
    boolean selecMultipla, // 'true' para selecao múltipla
    int itensVisiveis,     // número de opçoes que estarão visíveis
    String nomeClass,      // valor da propriedade 'class'
    String onChange)      // valor da propriedade onChange
{
    // cabecalho do select
    StringBuffer html = new StringBuffer("<select name='" + nomeForm + "'");

    if (selecMultipla) {
        html.append(" multiple"); //lista com multiplas selecoes
    }

    if (itensVisiveis > 0) {
        html.append(" size='" + itensVisiveis + "'");
    }

    if (nomeClass != null) {
        html.append(" class='" + nomeClass + "'");
    }

    if (onChange != null) {
        html.append(" onchange='" + onChange + "'");
    }

    html.append(">");

    // corpo do select
    if (itemVazio != null) {
        html.append("<option value=''>" + itemVazio + "</option>");
    }

    PercorredorXML itens = new PercorredorXML(raiz, elemLista);
    String valor, nome;
    while (itens.next()) {

        valor = itens.getValor(valorItem);
        nome = itens.getValor(nomeItem);

        html.append( "<option value='" + valor + "'" );
        if (valorPreSelec != null && valorPreSelec.equals( valor ) ) {
            html.append( " selected" );
        }

        html.append( ">" + nome + "</option>" );
    }

    html.append( "</select>" );

    return html.toString();
}
%>






