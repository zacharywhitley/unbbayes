package unbbayes.controller.resources;

import java.util.*;

/**
 * <p>Title: UnBBayes</p>
 * <p>Description: Arquivo de recurso para o pacote unbbayes.controller. Localization = portuguese.</p>
 * <p>Copyright: Copyright (c) 2001</p>
 * <p>Company: UnB</p>
 * @author Rommel Novaes Carvalho, Michael Onishi
 * @version 1.0
 * @since 05/04/2002
 */

public class ControllerResources_pt extends ListResourceBundle {

    /**
	 *  Sobrescreve getContents e retorna um array, onde cada item no array �
	 *	um par de objetos. O primeiro elemento do par � uma String chave, e o
	 *	segundo � o valor associado a essa chave.
	 *
	 * @return O conte�do dos recursos
	 */
	public Object[][] getContents() {
		return contents;
	}

	/**
	 * Os recursos
	 */
	static final Object[][] contents =
	{	{"imageFileFilter","Imagem (*.gif)"},
		{"likelihoodName","Likelihood"},
		{"likelihoodException","S� tem zeros!"},
		{"statusEvidenceProbabilistic","Probabilidade da Evid�ncia Total: "},
		{"statusEvidenceException","Evid�ncias n�o consistentes ou underflow"},
		{"statusError","Erro!"},
		{"printLogToolTip","Imprimir o log de compila��o"},
		{"previewLogToolTip","Visualizar a impress�o"},
		{"okButtonLabel"," Ok "},
		{"statusTotalTime","Tempo Total: "},
		{"statusSeconds"," segundos"},
		{"stateProbabilisticName","Estado "},
		{"stateDecisionName","A��o "},
		{"stateUtilityName","Utilidade "},
		{"firstStateProbabilisticName","Estado 0"},
		{"firstStateDecisionName","A��o 0"},
		{"nodeName","N�: "},
		{"probabilisticNodeName","C"},
		{"decisionNodeName","D"},
		{"utilityNodeName","U"},
		{"potentialTableException","N�o � um n�mero!"},
		{"copiedNodeName","C�pia do "},
		{"askTitle","Digite um r�tulo para a rede"},
		{"informationText","Informa��o"},
		{"printException","Erro de Impress�o: "},
		{"loadNetException","Erro ao Abrir a Rede"},
		{"cancelOption","Cancelar"},
		{"printerStatus","Status da Impressora"},
		{"initializingPrinter","Inicializando impressora..."},
		{"printingPage","Imprimindo p�gina "},
		{"previewButtonLabel","Anterior"},
		{"nextButtonLabel","Pr�xima"},
		{"fitToPageButtonLabel","Ajustar para P�gina"}
	};
}