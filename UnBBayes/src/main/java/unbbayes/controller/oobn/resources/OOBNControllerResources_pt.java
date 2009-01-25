
/*
 *  UnBBayes
 *  Copyright (C) 2002, 2008 Universidade de Brasilia - http://www.unb.br
 *
 *  This file is part of UnBBayes.
 *
 *  UnBBayes is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  UnBBayes is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with UnBBayes.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package unbbayes.controller.oobn.resources;

import java.util.*;

/**
 * <p>Title: UnBBayes</p>
 * <p>Description: Arquivo de recurso para o pacote unbbayes.controller. Localization = portuguese.</p>
 * <p>Copyright: Copyright (c) 2001</p>
 * <p>Company: UnB</p>
 * @author Shou Matsumoto
 * @version 1.0
 * @since 16/11/2008
 */

public class OOBNControllerResources_pt extends ListResourceBundle {

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
		{"likelihoodException","Só tem zeros!"},
		{"statusEvidenceProbabilistic","Probabilidade da Evidência Total: "},
		{"statusEvidenceException","Evidências não consistentes ou underflow"},
		{"statusError","Erro!"},
		{"printLogToolTip","Imprimir o log de compilação"},
		{"previewLogToolTip","Visualizar a impressão"},
		{"okButtonLabel"," Ok "},
		{"closeButtonLabel","Fechar"},
		{"statusTotalTime","Tempo Total: "},
		{"statusSeconds"," segundos"},
		{"stateProbabilisticName","Estado "},
		{"stateDecisionName","Ação "},
		{"stateUtilityName","Utilidade "},
		{"firstStateProbabilisticName","Estado 0"},
		{"firstStateDecisionName","Ação 0"},
		{"nodeName","Nó: "},
		
		//Barra de status
		{"statusLoadingKB","Carregando base de conhecimento..."},
		{"statusSavingKB","Salvando base de conhecimento..."},
		{"statusGeneratingSSBN","Gerando SSBN..."},
		{"statusReady","Pronto"},
		{"statusEdittingClass","Editando a classe: "},
				
		
		//MainController
		{"NewPNName","Nova BN"},
		{"NewMSBNName","Nova MSBN"},
		{"NewMEBNName","NovaMEBN"},
		{"NewOOBNName","NovaOOBN"},
		
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
		
		{"copiedNodeName","Cópia do "},
		{"askTitle","Digite um rótulo para a rede"},
		{"informationText","Informação"},
		{"printException","Erro de Impressão: "},
		{"loadNetException","Erro ao Abrir a Rede"},
		{"cancelOption","Cancelar"},
		{"printerStatus","Status da Impressora"},
		{"initializingPrinter","Inicializando impressora..."},
		{"printingPage","Imprimindo página "},
		{"previewButtonLabel","Anterior"},
		{"nextButtonLabel","Próxima"},
		{"fitToPageButtonLabel","Ajustar para Página"},
		{"loading","Carregando "},
		{"cancel","Cancelar"},
		{"of"," de "},
		{"numberFormatError","O valor deve ser um número real."},
		
		
		/* Exceptions MEBN */
		{"withoutMFrag", "No existe nenhuma MFrag"}, 
		{"edgeInvalid", "Arco invalido"}, 		
		
		{"JAXBExceptionFound", "Erro de sintaxe..."},

		/* Numeric attribute node */
		{"mean", "Média"},
		{"stdDev", "Desv. Padrão"}, 
		
		/* Java helper */
		{"helperDialogTitle", "Ajuda"},

		//Network Controller
		{"logDialogTitle", "Log"},
		
		//Result Dialog
		{"ResultDialog", "Resultado"}, 
		
		/* load/save */
		{"saveSucess", "Arquivo salvo com sucesso!"},
		{"mebnDontExists", "Operao falhou: No há MEBN ativa"},
		{"bnDontExists", "Operao falhou: No há Rede Bayesiana ativa"},
		{"msbnDontExists", "Operao falhou: No há MSBN ativa"},
		{"windowDontExists", "Operao falhou: No há janela ativa"},
		{"sucess", "Sucesso"}, 
		{"error", "Erro"},
		{"loadHasError", "O arquivo foi carregado com alguns erros"},
		{"withoutPosfixe", "Tipo do arquivo não informado!"},
		
		/* Likelihood Weighting Inference */
		{"sampleSizeInputMessage", "Favor entrar com o tamanho da amostragem (número de casos)."},
		{"sampleSizeInputTitle", "Tamanho da amostragem"}, 
		{"sampleSizeInputError", "O tamanho da amostragem deve ser um número inteiro maior zero."},
		{"likelihoodWeightingNotApplicableError", "O algoritmo de Likelihood Weighting só pode ser usado com uma rede bayesiana. Favor escolher outro algoritmo."},

		// OOBN controller's error messages
		
		{"OOBNClassCycle", "Uma classe OOBN não pode conter a si mesmo"},
	};
}
