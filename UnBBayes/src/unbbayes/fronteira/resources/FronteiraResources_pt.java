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
	{	{"fileDirectoryType","Diret�rio"},
		{"fileJPGType","Imagem JPG"},
		{"fileGIFType","Imagem GIF"},
		{"fileTXTType","Arquivo Texto TXT"},
		{"fileNETType","Arquivo de Rede Bayesiana NET"},
		{"fileGenericType","Arquivo Gen�rico"},
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
		{"globalOptionTitle","Op��es Globais"},
		{"usaName","EUA"},
		{"chinaName","China"},
		{"japanName","Jap�o"},
		{"ukName","UK"},
		{"koreaName","Korea"},
		{"italyName","It�lia"},
		{"canadaName","Canada"},
		{"brazilName","Brasil"},
		{"nodeName","N�: "},
		{"radiusLabel","Raio:"},
		{"radiusToolTip","Raio do n�"},
		{"netLabel","Rede"},
		{"netToolTip","Tamanho da rede"},
		{"probabilisticNodeColorLabel","Cor do N� de Probabilidade"},
		{"probabilisticNodeColorToolTip","Selecionar a cor do n� de probabilidade"},
		{"decisionNodeColorLabel","Cor do N� de Decis�o"},
		{"decisionNodeColorToolTip","Selecionar a cor do n� de decis�o"},
		{"utilityNodeColorLabel","Cor do N� de Utilidade"},
		{"utilityNodeColorToolTip","Selecionar a cor do n� de utilidade"},
		{"nodeColorLabel","Cor do n�"},
		{"arcColorLabel","Cor do Arco"},
		{"arcColorToolTip","Selecionar a cor do arco"},
		{"selectionColorLabel","Cor de Sele��o"},
		{"selectionColorToolTip","Selecionar a cor de sele��o"},
		{"backgroundColorLabel","Cor de Fundo"},
		{"backgroundColorToolTip","Selecionar a cor de fundo"},
		{"confirmLabel","Confirmar"},
		{"confirmToolTip","Corfimar as altera��es"},
		{"cancelLabel","Cancelar"},
		{"cancelToolTip","Cancelar as altera��es"},
		{"resetLabel","Repor"},
		{"resetToolTip","Repor os valores padr�es"},
		{"decimalPatternTab","Padr�o Decimal"},
		{"colorControllerTab","Controle de Cor"},
		{"sizeControllerTab","Controle de Tamanho"},
		{"logTab","Log"},
		{"createLogLabel","Gerar Log"},
		{"nodeGraphName","N�"},
		{"LookAndFeelUnsupportedException","N�o suporta esse LookAndFeel: "},
		{"LookAndFeelClassNotFoundException","A classe desse LookAndFeel n�o foi encontrada: "},
		{"LookAndFeelInstantiationException","N�o foi poss�vel carregar esse LookAndFeel: "},
		{"LookAndFeelIllegalAccessException","Esse LookAndFeel n�o pode ser usado: "},
		{"statusReadyLabel","Pronto"},
		{"helpToolTip","Ajuda do UnBBayes"},
		{"propagateToolTip","Propagar as evid�ncias"},
		{"expandToolTip","Expandir a �rvore de evid�ncias"},
		{"collapseToolTip","Contrair a �rvore de evid�ncias"},
		{"editToolTip","Retornar ao modo de edi��o"},
		{"logToolTip","Informa��o sobre a compila��o (Log)"},
		{"resetCrencesToolTip","Reiniciar as cren�as"},
		{"printNetToolTip","Imprimir o grafo"},
		{"previewNetToolTip","Visualizar a impress�o do grafo"},
		{"saveNetImageToolTip","Salvar o grafo como imagem gif"},
		{"siglaLabel","Sigla:"},
		{"descriptionLabel","Descri��o:"},
		{"compileToolTip","Compilar �rovre de jun��o"},
		{"moreToolTip","Adicionar estado"},
		{"lessToolTip","Remover estado"},
		{"arcToolTip","Inserir arco"},
		{"probabilisticNodeInsertToolTip","Inserir vari�vel de probabilidade"},
		{"decisionNodeInsertToolTip","Inserir vari�vel de decis�o"},
		{"utilityNodeInsertToolTip","Inserir vari�vel de utilidade"},
		{"selectToolTip","Selecionar v�rios n�s e arcos"},
		{"printTableToolTip","Imprimir a tabela"},
		{"previewTableToolTip","Visualizar a impress�o da tabela"},
		{"saveTableImageToolTip","Salvar a tabela como imagem gif"},
		{"previewTitle","Pr� Visualiza��o"},
		{"filesText"," arquivos"},
		{"aprendizagemTitle","Edi��o da Rede de Aprendizagem"},
		{"calculateProbabilitiesFromLearningToEditMode","Remontar a estrutura da rede e voltar para o modo de edi��o"}
	};
}