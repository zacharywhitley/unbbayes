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
	 *  Sobrescreve getContents e retorna um array, onde cada item no array eh
	 *	um par de objetos. O primeiro elemento do par eh uma String chave, e o
	 *	segundo eh o valor associado a essa chave.
	 *
	 * @return O conteudo dos recursos
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
		{"likelihoodException","S√≥ tem zeros!"},
		{"statusEvidenceProbabilistic","Probabilidade da Evid√™ncia Total: "},
		{"statusEvidenceException","Evid√™ncias n√£o consistentes ou underflow"},
		{"statusError","Erro!"},
		{"printLogToolTip","Imprimir o log de compila√ß√£o"},
		{"previewLogToolTip","Visualizar a impress√£o"},
		{"okButtonLabel"," Ok "},
		{"statusTotalTime","Tempo Total: "},
		{"statusSeconds"," segundos"},
		{"stateProbabilisticName","Estado "},
		{"stateDecisionName","A√ß√£o "},
		{"stateUtilityName","Utilidade "},
		{"firstStateProbabilisticName","Estado 0"},
		{"firstStateDecisionName","A√ß√£o 0"},
		{"nodeName","N√≥: "},
		
		{"probabilisticNodeName","C"},
		{"decisionNodeName","D"},
		{"utilityNodeName","U"},
		{"contextNodeName","CX"},
		{"residentNodeName","RX"},
		{"inputNodeName","IX"},		
		{"ordinaryVariableName", "OX"}, 	
		{"entityName", "EX"}, 	
		
		{"domainMFragName","DMFrag"},	
		{"findingMFragName","FMFrag"},				
		
		{"potentialTableException","N√£o √© um n√∫mero!"},
		{"copiedNodeName","C√≥pia do "},
		{"askTitle","Digite um r√≥tulo para a rede"},
		{"informationText","Informa√ß√£o"},
		{"printException","Erro de Impress√£o: "},
		{"loadNetException","Erro ao Abrir a Rede"},
		{"cancelOption","Cancelar"},
		{"printerStatus","Status da Impressora"},
		{"initializingPrinter","Inicializando impressora..."},
		{"printingPage","Imprimindo p√°gina "},
		{"previewButtonLabel","Anterior"},
		{"nextButtonLabel","Pr√≥xima"},
		{"fitToPageButtonLabel","Ajustar para P√°gina"},
		{"loading","Carregando "},
		{"cancel","Cancelar"},
		{"of"," de "},
		
		/* Exceptions MEBN */
		{"withoutMFrag", "N√£o existe nenhuma MFrag"}, 
		{"edgeInvalid", "Arco invalido"}, 		
		
		{"JAXBExceptionFound", "Erro de sintaxe..."},

		/* Numeric attribute node */
		{"mean", "M√©dia"},
		{"stdDev", "Desv. Padr√£o"}, 
		
		/* load/save */
		{"saveSucess", "Arquivo salvo com sucesso!"},
		{"mebnDontExists", "OperaÁ„o falhou: N„o h· MEBN ativa"},
		{"bnDontExists", "OperaÁ„o falhou: N„o h· Rede Bayesiana ativa"},
		{"msbnDontExists", "OperaÁ„o falhou: N„o h· MSBN ativa"},
		{"windowDontExists", "OperaÁ„o falhou: N„o h· janela ativa"},
		{"sucess", "Sucesso"}, 
		{"error", "Erro"},
		{"withoutPosfixe", "Tipo do arquivo n√£o informado!"}		
		
	};
}
