package unbbayes.gui.resources;

import java.util.*;

/**
 * <p>Title: UnBBayes</p>
 * <p>Description: Arquivo de recurso para o pacote unbbayes.gui. Localization = portuguese.</p>
 * <p>Copyright: Copyright (c) 2001</p>
 * <p>Company: UnB</p>
 * @author Rommel Novaes Carvalho, Michael Onishi
 * @version 1.0
 * @since 05/04/2002
 */

public class GuiResources_pt extends ListResourceBundle {

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
	{	
		{"fileDirectoryType","Diret�rio"},
		{"fileARFFType","Arquivo Arff"},
		{"fileTXTType","Arquivo Texto TXT"},
		{"fileNETType","Arquivo de Rede Bayesiana NET"},
		{"fileGenericType","Arquivo Gen�rico"},
		
		{"unbbayesTitle","UnBBayes"},
		
		//main toll bar tips 		
		{"newToolTip","Nova rede"},
		{"newMsbnToolTip", "Nova MSBN"}, 
		{"newMebnToolTip", "Nova MEBN"}, 		
		{"openToolTip","Abrir rede"},
		{"saveToolTip","Salvar rede"},
		{"learningToolTip","Modo de aprendizagem"},
		{"metalToolTip","Usa Metal Look And Feel"},
		{"motifToolTip","Usa Motif Look And Feel"},
		{"windowsToolTip","Usa Windows Look And Feel"},
		{"tileToolTip","Organizar as janelas em bloco"},
		{"cascadeToolTip","Organizar as janelas em cascata"},
		
		{"netFileFilter","Net (.net), XMLBIF (.xml), PR-OWL (.owl), UnBBayes file (.ubf)"},
		{"netFileFilterSave","Net (.net), XMLBIF (.xml), Arquivo UnBBayes (.ubf)"},
		
		{"xmlBIFFileFilter", "XMLBIF (.xml)"},		
		
		{"textFileFilter","Text (.txt)"},
		{"fileUntitled","SemNome.txt"},
		
		{"globalOptionTitle","Op��es Globais"},
		{"hierarchyToolTip","Defini��o de hierarquia"},
        
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
		{"probabilisticDescriptionNodeColorLabel","Descri��o"},
		{"probabilisticDescriptionNodeColorToolTip","Selecionar a cor do n� de descri��o de probabilidade"},
		{"probabilisticExplanationNodeColorLabel","Explana��o"},
		{"probabilisticExplanationNodeColorToolTip","Selecionar a cor do n� de explana��o de probabilidade"},
		{"decisionNodeColorLabel","Decis�o"},
		{"decisionNodeColorToolTip","Selecionar a cor do n� de decis�o"},
		{"utilityNodeColorLabel","Utilidade"},
		{"utilityNodeColorToolTip","Selecionar a cor do n� de utilidade"},
		{"nodeColorLabel","Cor do n�"},
                {"arcColor","Cor do arco"},
                {"selectionColor","Cor de sele��o"},
                {"backGroundColor","Cor de fundo"},
                {"arcColorLabel","Arco"},
		{"arcColorToolTip","Selecionar a cor do arco"},
		{"selectionColorLabel","Sele��o"},
		{"selectionColorToolTip","Selecionar a cor de sele��o"},
		{"backgroundColorLabel","Fundo"},
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
		{"nameError","Nome n�o aceito"},
		
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
		{"nameLabel", "Nome:"}, 
		{"typeLabel", "Tipo:"}, 
		{"descriptionLabel","Descri��o"},
		{"compileToolTip","Compilar �rvore de jun��o"},
		{"moreToolTip","Adicionar estado"},
		{"lessToolTip","Remover estado"},
		{"arcToolTip","Inserir arco"},
		{"probabilisticNodeInsertToolTip","Inserir vari�vel de probabilidade"},
		{"decisionNodeInsertToolTip","Inserir vari�vel de decis�o"},
		{"utilityNodeInsertToolTip","Inserir vari�vel de utilidade"},
		{"contextNodeInsertToolTip","Inserir vari�vel de contexto"},
		{"inputNodeInsertToolTip","Inserir vari�vel de entrada"},
		{"residentNodeInsertToolTip","Inserir vari�vel residente"},
		{"mFragInsertToolTip","Inserir MFrag"},	
		{"inputActiveToolTip","Input Node Selecionado"},  
		{"mFragActiveToolTip","MFrag Selecionada"}, 		
		{"contextActiveToolTip","Context Node Selecionado"}, 
		{"residentActiveToolTip","Resident Node Selecionado"}, 		
		{"addArgumentToolTip","Adicionar argumento"}, 
		{"editFormulaToolTip","Editar formula"},		
		{"selectToolTip","Selecionar v�rios n�s e arcos"},
		{"printTableToolTip","Imprimir a tabela"},
		{"previewTableToolTip","Visualizar a impress�o da tabela"},
		{"saveTableImageToolTip","Salvar a tabela como imagem gif"},
		{"newEntityToolTip","Criar nova entidade"},		
		{"delEntityToolTip","Deletar entidade"},
		{"newOVariableToolTip","Criar nova var. ordin�ria"},
		{"delOVariableToolTip", "Deletar var. ordin�ria"}, 
		{"newArgumentToolTip","Adicionar nova v. ord. � lista de argumentos"},
		{"delArgumentToolTip", "Remover var. ord. da lista de argumentos"}, 		
		{"downArgumentToolTip", "Aicionar a lista de argumentos v. ord. selecionada"}, 		
		{"mTheoryEditionTip", "Editar atributos da MTheory"}, 		
		
		{"showMTheoryToolTip","Mostrar �rvore da MTheory"},	
		{"showEntitiesToolTip","Mostrar entidades da MTheory"},
		{"showOVariablesToolTip","Mostrar ovariables da MFrag"},			
		
		{"formula","Formula:"},	
		{"inputOf","Input de:"},	
		{"arguments", "Args: "}, 	
		{"statusReadyLabel","Pronto"},
		
		{"andToolTip", "operador 'e'"}, 
		{"orToolTip", "operador 'ou'"},
		{"notToolTip", "operador 'n�o'"},
		{"equalToToolTip", "operador 'igual'"},
		{"impliesToolTip", "operador 'implicaca'"},
		{"iffToolTip", "operador 'see' "},
		{"forallToolTip", "quantificador 'para todo'"},
		{"existsToolTip", "quantificador 'existe'"},	
		
		//Menus MEBN
		{"menuDelete", "Delete"}, 
		{"menuAddContext", "Add Context"}, 
		{"menuAddInput", "Add Input"},
		{"menuAddResident", "Add Resident"}, 
		{"menuAddDomainMFrag", "Add Domain MFrag"}, 
		{"menuAddFindingMFrag", "Add Finding MFrag"}, 
		
		//Titles for tab panel
		{"ResidentTabTitle", "N� Residente"}, 
		{"InputTabTitle", "N� de Input"}, 
		{"ContextTabTitle", "N� de Contexto"}, 
		{"MTheoryTreeTitle", "�rvore MTheory"}, 
		{"EntityTitle", "Entidade"}, 
		{"OVariableTitle", "Variavel Ord."},
		{"ArgumentTitle", "Argumentos"}, 
		{"StatesTitle", "Estados"}, 	
		{"FathersTitle", "N�s Pais"}, 		
		
		//Label for buttons of tab selection
		{"MTheoryButton", "MTheory"}, 
		{"ResidentButton", "Resident"}, 
		{"InputButton", "Input"}, 
		{"ContextButton", "Context"}, 
		{"MFragButton", "MFrag"}, 	
		{"ArgumentsButton", "Argumentos"}, 	
		{"OrdVariableButton", "Var. Ordin�ria"}, 
		
		{"whithotMFragActive","N�o h� MFrag ativa"},			
		{"previewTitle","Pr� Visualiza��o"},
		{"filesText"," arquivos"},
		{"aprendizagemTitle","Edi��o da Rede de Aprendizagem"},
		{"calculateProbabilitiesFromLearningToEditMode","Remontar a estrutura da rede e voltar para o modo de edi��o"},
        {"fileMenu","Arquivo"},
        {"lafMenu","Look and Feel"},
        {"viewMenu","Exibir"},
        {"tbMenu","Barras de Ferramentas"},
        {"toolsMenu","Ferramentas"},
        {"windowMenu","Janela"},
        {"helpMenu","Ajuda"},
        {"newMenu","Novo..."},
        {"newBN","Nova BN"},
        {"newMSBN","Nova MSBN"},
        {"newMEBN","Nova MEBN"},
        {"openItem","Abrir..."},
        {"saveItem","Salvar como..."},
        {"exitItem","Sair"},
        {"tbFile","Barra de Ferramenta de Arquivo"},
        {"tbView","Barra de Ferramenta de Exibir"},
        {"tbTools","Barra de Ferramenta de Ferramentas"},
        {"tbWindow","Barra de Ferramenta de Janela"},
        {"tbHelp","Barra de Ferramenta de Ajuda"},
        {"metalItem","Metal"},
        {"motifItem","Motif"},
        {"windowsItem","Windows"},
        {"learningItem","Aprendizagem"},
        {"cascadeItem","Em cascata"},
        {"tileItem","Lado a lado verticalmente"},
        {"helpItem","Ajuda"},
        {"aboutItem","Sobre o UnBBayes"},
        
        {"fileMenuMn","A"},
        {"lafMenuMn","L"},
        {"viewMenuMn","X"},
        {"tbMenuMn","B"},
        {"toolsMenuMn","F"},
        {"windowMenuMn","J"},
        {"helpMenuMn","U"},
        {"newItemMn","N"},
        {"openItemMn","A"},
        {"saveItemMn","S"},
        {"exitItemMn","R"},
        {"metalItemMn","M"},
        {"motifItemMn","O"},
        {"windowsItemMn","W"},
        {"learningItemMn","P"},
        {"cascadeItemMn","C"},
        {"tileItemMn","V"},
        {"helpItemMn","U"},
        {"aboutItemMn","S"},
       
        {"operationError","Erro na opera��o"},           
        {"oVariableAlreadyIsArgumentError","Vari�vel ord. j� � argumento deste n�!"},       
        {"properties","Propriedades..."},
        {"nameException","Erro no Nome"},
        {"nameDuplicated", "Nome j� existe..."}, 
        {"siglaError","A sigla s� pode ter letras e n�meros."},
        {"descriptionError","A descri��o s� pode ter letras e n�meros."},
        
        /* FormulaTreeConstructionException */
        {"notOperator", "N�o � permitido operador nesta posi��o"}, 
        
		{"sucess", "Sucesso"}, 
		{"error", "Erro"},
        
		/* Tips for buttons of the table edition */
		{"deleteTip", "Deleta o texto selecionado"}, 
		{"anyTip", "Inserir constru��o \"If any\""}, 
		{"allTip", "Inserir constru��o \"If all\""}, 
		{"else", "Inserir \"else\""}, 
		{"equalTip", "Inserir operador de igualdade"}, 
		{"andTip", "Inserir constru��o AND"}, 
		{"orTip", "Inserir constru��o OR"}, 
		{"notTip", "Inserir constru��o NOT"}, 
		{"cadinalityTip", "Inserir constru��o CARDINALITY"}, 
		{"maxTip", "Inserir contru��o MAX"}, 
		{"minTip", "Inserir contru��o MIN"}, 
		{"saveTip", "Salvar a tabela"}, 
		{"statesTip", "Mostrar estados do n�"}, 
		{"fatherTip", "Mostrar pais do n�"}, 
		{"argTip", "Mostrar argumentos do n�"}, 
		{"exitTip", "Sair sem salvar"}, 
		
		/* Exceptions MEBN */
		{"withoutMFrag", "N�o existe nenhuma MFrag"}, 
		{"edgeInvalid", "Arco invalido"} 		
	};
}