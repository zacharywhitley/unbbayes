package unbbayes.fronteira.resources;

import java.util.*;

/**
 * <p>Title: UnBBayes</p>
 * <p>Description: Arquivo de recurso para o pacote unbbayes.fronteira. Localization = portuguese.</p>
 * <p>Copyright: Copyright (c) 2001</p>
 * <p>Company: UnB</p>
 * @author Rommel Novaes Carvalho, Michael Onishi
 * @version 1.0
 * @since 05/04/2002
 */

public class FronteiraResources_pt extends ListResourceBundle {

    /**
	 *  Sobrescreve getContents e retorna um array, onde cada item no array é
	 *	um par de objetos. O primeiro elemento do par é uma String chave, e o
	 *	segundo é o valor associado a essa chave.
	 *
	 * @return O conteúdo dos recursos
	 */
	public Object[][] getContents() {
		return contents;
	}

	/**
	 * Os recursos
	 */
	static final Object[][] contents =
	{	{"fileDirectoryType","Diretório"},
		{"fileJPGType","Imagem JPG"},
		{"fileGIFType","Imagem GIF"},
		{"fileTXTType","Arquivo Texto TXT"},
		{"fileNETType","Arquivo de Rede Bayesiana NET"},
		{"fileGenericType","Arquivo Genérico"},
		{"unbbayesTitle","UnBBayes"},
		{"newToolTip","Nova rede"},
		{"openToolTip","Abrir rede"},
		{"saveToolTip","Salvar rede"},
		{"learningToolTip","Modo de aprendizagem"},
		{"metalToolTip","Usa Metal Look And Feel"},
		{"motifToolTip","Usa Motif Look And Feel"},
		{"windowsToolTip","Usa Windows Look And Feel"},
		{"tileToolTip","Organizar as janelas em bloco"},
		{"cascadeToolTip","Organizar as janelas em cascata"},
		{"netFileFilter","Net (.net)"},
		{"textFileFilter","Text (.txt)"},		
		{"fileUntitled","SemNome.txt"},
		{"globalOptionTitle","Opções Globais"},
		{"usaName","EUA"},
		{"chinaName","China"},
		{"japanName","Japão"},
		{"ukName","UK"},
		{"koreaName","Korea"},
		{"italyName","Itália"},
		{"canadaName","Canada"},
		{"brazilName","Brasil"},
		{"nodeName","Nó: "},
		{"radiusLabel","Raio:"},
		{"radiusToolTip","Raio do nó"},
		{"netLabel","Rede"},
		{"netToolTip","Tamanho da rede"},
		{"probabilisticDescriptionNodeColorLabel","Descrição"},
		{"probabilisticDescriptionNodeColorToolTip","Selecionar a cor do nó de descrição de probabilidade"},
		{"probabilisticExplanationNodeColorLabel","Explanação"},
		{"probabilisticExplanationNodeColorToolTip","Selecionar a cor do nó de explanação de probabilidade"},
		{"decisionNodeColorLabel","Decisão"},
		{"decisionNodeColorToolTip","Selecionar a cor do nó de decisão"},
		{"utilityNodeColorLabel","Utilidade"},
		{"utilityNodeColorToolTip","Selecionar a cor do nó de utilidade"},
		{"nodeColorLabel","Cor do nó"},
		{"arcColorLabel","Arco"},
		{"arcColorToolTip","Selecionar a cor do arco"},
		{"selectionColorLabel","Seleção"},
		{"selectionColorToolTip","Selecionar a cor de seleção"},
		{"backgroundColorLabel","Fundo"},
		{"backgroundColorToolTip","Selecionar a cor de fundo"},
		{"confirmLabel","Confirmar"},
		{"confirmToolTip","Corfimar as alterações"},
		{"cancelLabel","Cancelar"},
		{"cancelToolTip","Cancelar as alterações"},
		{"resetLabel","Repor"},
		{"resetToolTip","Repor os valores padrões"},
		{"decimalPatternTab","Padrão Decimal"},
		{"colorControllerTab","Controle de Cor"},
		{"sizeControllerTab","Controle de Tamanho"},
		{"logTab","Log"},
		{"createLogLabel","Gerar Log"},
		{"nodeGraphName","Nó"},
		{"LookAndFeelUnsupportedException","Não suporta esse LookAndFeel: "},
		{"LookAndFeelClassNotFoundException","A classe desse LookAndFeel não foi encontrada: "},
		{"LookAndFeelInstantiationException","Não foi possível carregar esse LookAndFeel: "},
		{"LookAndFeelIllegalAccessException","Esse LookAndFeel não pode ser usado: "},
		{"statusReadyLabel","Pronto"},
		{"helpToolTip","Ajuda do UnBBayes"},
		{"propagateToolTip","Propagar as evidências"},
		{"expandToolTip","Expandir a árvore de evidências"},
		{"collapseToolTip","Contrair a árvore de evidências"},
		{"editToolTip","Retornar ao modo de edição"},
		{"logToolTip","Informação sobre a compilação (Log)"},
		{"resetCrencesToolTip","Reiniciar as crenças"},
		{"printNetToolTip","Imprimir o grafo"},
		{"previewNetToolTip","Visualizar a impressão do grafo"},
		{"saveNetImageToolTip","Salvar o grafo como imagem gif"},
		{"siglaLabel","Sigla:"},
		{"descriptionLabel","Descrição:"},
		{"compileToolTip","Compilar árovre de junção"},
		{"moreToolTip","Adicionar estado"},
		{"lessToolTip","Remover estado"},
		{"arcToolTip","Inserir arco"},
		{"probabilisticNodeInsertToolTip","Inserir variável de probabilidade"},
		{"decisionNodeInsertToolTip","Inserir variável de decisão"},
		{"utilityNodeInsertToolTip","Inserir variável de utilidade"},
		{"selectToolTip","Selecionar vários nós e arcos"},
		{"printTableToolTip","Imprimir a tabela"},
		{"previewTableToolTip","Visualizar a impressão da tabela"},
		{"saveTableImageToolTip","Salvar a tabela como imagem gif"},
		{"previewTitle","Pré Visualização"},
		{"filesText"," arquivos"},
		{"aprendizagemTitle","Edição da Rede de Aprendizagem"},
		{"calculateProbabilitiesFromLearningToEditMode","Remontar a estrutura da rede e voltar para o modo de edição"}
	};
}