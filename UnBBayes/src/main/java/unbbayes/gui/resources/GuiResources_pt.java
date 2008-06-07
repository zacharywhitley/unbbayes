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
package unbbayes.gui.resources;

import java.util.ListResourceBundle;

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
	{	
		//Types of files
		{"fileDirectoryType","Diretório"},
		{"fileARFFType","Arquivo Arff"},
		{"fileTXTType","Arquivo Texto TXT"},
		{"fileNETType","Arquivo de Rede Bayesiana NET"},
		{"fileGenericType","Arquivo Genérico"},
		
		{"netFileFilter","Net (.net), XMLBIF (.xml), PR-OWL (.owl), UnBBayes file (.ubf)"},
		{"netFileFilterSave","Net (.net), XMLBIF (.xml), Arquivo UnBBayes (.ubf)"},
		{"powerloomFileFilter","Base de Conhecimento (.plm)"},
		{"xmlBIFFileFilter", "XMLBIF (.xml)"},
		{"textFileFilter","Text (.txt)"},
		{"fileUntitled","SemNome.txt"},
		
		//Titles of the file choosers
		{"saveTitle","Salvar"}, 
		{"openTitle","Abrir"}, 
		
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
		
		{"globalOptionTitle","Opções Globais"},
		{"hierarchyToolTip","Definição de hierarquia"},
        
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
                {"arcColor","Cor do arco"},
                {"selectionColor","Cor de seleção"},
                {"backGroundColor","Cor de fundo"},
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
		{"closeButton", "Fechar"},
	
		{"likelihoodName", "Likelihood"},
		
		{"LookAndFeelUnsupportedException","Não suporta esse LookAndFeel: "},
		{"LookAndFeelClassNotFoundException","A classe desse LookAndFeel não foi encontrada: "},
		{"LookAndFeelInstantiationException","Não foi possível carregar esse LookAndFeel: "},
		{"LookAndFeelIllegalAccessException","Esse LookAndFeel não pode ser usado: "},
		{"nameError","Nome não aceito"},
		{"operationFail","Operação não aceita"},
		{"nameAlreadyExists","Já existe um objeto com este nome"},	
		{"objectEntityHasInstance","Há instâncias da entidade selecionada. Remova-as e tente novamente."},
		{"internalError","Erro interno... Reporte aos desenvolvedores"},
		{"error","Erro"},	
		{"argumentMissing","Faltando argumentos"},	
		{"stateUnmarked","Estado não marcado"},	
		
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
		{"nameLabel", "Nome:"}, 
		{"typeLabel", "Tipo:"}, 
		{"descriptionLabel","Descrição:"},
		{"ordereableLabel", "É Ordenável"}, 
		{"compileToolTip","Compilar árvore de junção"},
		{"moreToolTip","Adicionar estado"},
		{"lessToolTip","Remover estado"},
		{"arcToolTip","Inserir Arco"},
		{"probabilisticNodeInsertToolTip","Inserir variável de probabilidade"},
		{"decisionNodeInsertToolTip","Inserir variavel de decisão"},
		{"utilityNodeInsertToolTip","Inserir variável de utilidade"},
		{"contextNodeInsertToolTip","Inserir Nó de Contexto"},
		{"inputNodeInsertToolTip","Inserir Nó de Entrada"},
		{"ordinaryVariableInsertToolTip","Inserir Variável Ordinária"},
		{"selectObjectToolTip","Selecionar Objeto"},
		{"residentNodeInsertToolTip","Inserir Nó Residente"},
		{"mFragInsertToolTip","Inserir MFrag"},	
		{"inputActiveToolTip","Nó de Entrada Selecionado"},  
		{"mFragActiveToolTip","MFrag Selecionada"}, 		
		{"contextActiveToolTip","Nó de Contexto Selecionado"}, 
		{"residentActiveToolTip","Nó Residente Selecionado"}, 		
		{"addArgumentToolTip","Adicionar Argumento"}, 
		{"editFormulaToolTip","Editar Formula"},		
		{"selectToolTip","Selecionar vários nós e arcos"},
		{"printTableToolTip","Imprimir tabela"},
		{"previewTableToolTip","Visualizar impressão da tabela"},
		{"saveTableImageToolTip","Salvar a tabela como imagem gif"},
		{"newEntityToolTip","Criar Entidade"},		
		{"delEntityToolTip","Deletar Entidade"},
		{"newOVariableToolTip","Criar Variável Ordinária"},
		{"delOVariableToolTip", "Deletar var. ordinária"}, 
		{"newArgumentToolTip","Adicionar V. Ord. à lista de argumentos"},
		{"delArgumentToolTip", "Remover Var. Ord. da lista de argumentos"}, 		
		{"downArgumentToolTip", "Adicionar Var. Ord. à lista de argumentos"}, 		
		{"mTheoryEditionTip", "Editar MTheory"}, 		
		{"isGloballyExclusive", "Exclusivo Globalmente"}, 
		{"resetToolTip", "Resetar"},	
		{"deleteSelectedItemToolTip", "Deletar Item"},	
		{"menuOpen", "Abrir"},	
		
		{"showMTheoryToolTip","Árvore da MTheory"},	
		{"showEntitiesToolTip","Entidades"},
		{"showOVariablesToolTip","Variáveis Ordinárias"},
		{"showEntityInstancesToolTip","Instâncias de Entidades"},			
		{"showFingingsToolTip","Evidências"},
		
		{"executeQueryToolTip","Executar Query"},
		{"turnToSSBNModeToolTip","Mudar para o Modo SSBN"},
		{"clearKBToolTip","Limpar Base de Conhecimento"},
		{"loadKBToolTip","Carregar Base de Conhecimento"},
		{"saveKBToolTip","Salvar Base de Conhecimento"},
		
		{"formula","Formula:"},	
		{"inputOf","Input de:"},	
		{"arguments", "Args: "}, 	
		{"statusReadyLabel","Pronto"},
		
		{"andToolTip", "Operador 'E'"}, 
		{"orToolTip", "Operador 'OU'"},
		{"notToolTip", "Operador 'NÂO'"},
		{"equalToToolTip", "Operador 'IGUAL'"},
		{"impliesToolTip", "Operador 'IMPLICA'"},
		{"iffToolTip", "Operador 'SEE' "},
		{"forallToolTip", "Quantificador 'PARA TODO'"},
		{"existsToolTip", "Quantificador 'EXISTE'"},	
		
		//Menus MEBN
		{"menuDelete", "Delete"}, 
		{"menuAddContext", "Adic. Contexto"}, 
		{"menuAddInput", "Adic. Input"},
		{"menuAddResident", "Adic. Residente"}, 
		{"menuAddDomainMFrag", "Adic. MFrag"}, 
		
		//Titles for tab panel
		{"ResidentTabTitle", "Nó Residente"}, 
		{"InputTabTitle", "Nó de Input"}, 
		{"ContextTabTitle", "Nó de Contexto"}, 
		{"MTheoryTreeTitle", "Árvore MTheory"}, 
		{"EntityTitle", "Entidade"}, 
		{"OVariableTitle", "Variável Ord."},
		{"ArgumentTitle", "Argumentos"}, 
		{"StatesTitle", "Estados"}, 	
		{"FathersTitle", "Nós Pais"}, 	
		{"NodesTitle", "Nós"}, 		
		{"AddFinding", "Finding"}, 		
		
		//Label for buttons of tab selection
		/* Don't use names with more than fifteen letters */
		{"MTheoryButton", "MTheory"}, 
		{"ResidentButton", "Resident"}, 
		{"InputButton", "Input"}, 
		{"ContextButton", "Context"}, 
		{"MFragButton", "MFrag"}, 	
		{"ArgumentsButton", "Argumentos"}, 	
		{"OrdVariableButton", "Variável"}, 
		
		{"whithotMFragActive","Não há MFrag ativa"},			
		{"previewTitle","Pré visualização"},
		{"filesText"," arquivos"},
		{"aprendizagemTitle","Edição da Rede de Aprendizagem"},
		{"calculateProbabilitiesFromLearningToEditMode","Remontar a estrutura da rede e voltar para o modo de edição"},
        {"fileMenu","Arquivo"},
        {"recentFilesMenu","Arquivos Recentes"},
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
        {"tanItem","TAN"},
        {"banItem","BAN"},
        {"monteCarloItem","Monte Carlo"},
        {"GibbsItem","Gibbs"},
        {"ILearningItem","Aprendizagem Incremental"},
        {"tileItem","Lado a lado verticalmente"},
        {"helpItem","Ajuda"},
        {"aboutItem","Sobre o UnBBayes"},
        
        {"recentFilesMn", "R"},
        {"fileMenuMn","A"},
        {"newMenuMn","N"},
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
        {"cascadeItemMn","C"},
        {"tileItemMn","V"},
        {"helpItemMn","U"},
        {"aboutItemMn","S"},
        {"newBNMn","B"},
        {"newMSBNMn","M"},
        {"newMEBNMn","E"},
        
        {"learningItemMn","P"},
        {"tanItemMn","T"},
        {"banItemMn","B"},
        {"monteCarloItemMn","M"},
        {"GibbsItemMn","G"},
        {"ILearningItemMn","R"},       
        
        {"operationError","Erro na operação"},           
        {"oVariableAlreadyIsArgumentError","Variável ord. já é argumento deste nó!"},       
        {"properties","Propriedades..."},
        {"nameException","Erro no Nome"},
        {"nameEmpty","O nome não pode ser branco"},
        {"nameDuplicated", "Nome já existe..."}, 
        {"siglaError","A sigla só pode ter letras e números."},
        {"descriptionError","A descrição só pode ter letras e números."},
        
        /* Resident Panel */
        {"stateEditionTip", "Editar Estados"}, 
        {"argumentEditionTip", "Editar Argumentos"}, 
        {"tableEditionTip", "Editar Tabela"}, 
        {"existentStatesDialogTip", "Estados Existentes"}, 
        
        {"addSelectedStatesTip", "Adicionar estados selecionados"}, 
        
        /* Input Panel */
        {"inputOf", "Nó origem"},         
        
        /* Arguments Typed Pane */
        {"nodeLabel", "Nó"}, 
        {"openTip", "Abrir"}, 
        
        /* Formula Pane */
        {"addOVariableTip", "Adicionar var. ordinária"}, 
        {"addNodeTip", "Adicionar nó"}, 
        {"addEntityTip", "Adicionar entidade"}, 
        {"addSkolenTip", "Adicionar skolen"}, 
        
        /* Query Panel*/
        {"queryPanelTitle","Query"}, 
        {"queryBtnBack","Voltar"}, 
        {"queryBtnSelect","Selecionar"},
        {"queryBtnExecute","Executar"},
        
        {"argumentFault","Argumentos incompletos. Query não pode ser executada."},
        {"inconsistentArgument","Argumentos inconsistentes. Query não pode ser executada."}, 
        {"selectOneVariable","Selecione uma variável:"}, 
        {"selectArgsValues","Selecione os valores dos argumentos:"}, 
        
        /* Findings Panel */
        {"stateLabel","Valor:"}, 
        {"booleanLabel","Boleano"}, 
        {"categoricalLabel","Categorico"}, 
        
        /* FormulaTreeConstructionException */
        {"notOperator", "Não é permitido operador nesta posição"}, 
        
		{"sucess", "Sucesso"}, 
		{"error", "Erro"},
        
		/* Tips for buttons of the table edition */
		{"clear", "clear"}, 
		{"ifAny", "if any"}, 
		{"ifAll", "if all"}, 
		{"else", "else"}, 
		{"default", "default"}, 
		{"equal", " = "}, 
		{"and", " & "}, 
		{"or", " | "}, 
		{"not", " ~ "}, 
		{"card", "card"}, 
		{"max", "max"}, 
		{"min", "min"}, 
		
		{"deleteTip", "Apaga o texto selecionado"}, 
		{"anyTip", "Inserir construção \"If any\""}, 
		{"allTip", "Inserir construção \"If all\""}, 
		{"defaultTip", "Inserir construção \"disbribuição padrão\""},
		{"elseTip", "Inserir \"else\""}, 
		{"equalTip", "Inserir operador de igualdade"}, 
		{"andTip", "Inserir construção AND"}, 
		{"orTip", "Inserir construção OR"}, 
		{"notTip", "Inserir construção NOT"}, 
		{"cadinalityTip", "Inserir construção CARDINALITY"}, 
		{"maxTip", "Inserir contrução MAX"}, 
		{"minTip", "Inserir contrução MIN"}, 
		{"saveTip", "Salvar a tabela"}, 
		{"statesTip", "Mostrar estados do nó"}, 
		{"fatherTip", "Mostrar pais do nó"}, 
		{"argTip", "Mostrar argumentos do nó"}, 
		{"exitTip", "Sair sem salvar"}, 
		{"compileCPTTip", "Compilar tabela"},
		{"saveCPTTip", "Salvar tabela"},
		{"exitCPTTip", "Fechar tabela"},
		{"fatherCPTTip", "Abrir lista de pais/estados dos nós pais"},
		{"argumentCPTTip", "Abrir lista de argumentos"},
		{"statesCPTTip", "Abrir lista de estados do nó"},
		
		{"position", "Posição"},
		
		/* CPT Edition messages */
		{"compileCPT", "Compilar"},
		{"saveCPT", "Salvar"},
		{"exitCPT", "Fechar"},
		{"fatherCPT", "Pais"},
		{"argumentCPT", "Argumentos"},
		{"statesCPT", "Estados"},
		
		{"CptSaveOK", "Tabela salva com sucesso"},
		{"CptCompileOK", "Tabela compilada sem erros"},
		
		/* Exceptions MEBN */
		{"withoutMFrag", "Não existe nenhuma MFrag"}, 
		{"edgeInvalid", "Arco invalido"}, 
		
		/* Edition of states */
		{"insertBooleanStates", "Inserir estados booleanos"}, 
		{"categoryStatesTip", "Inserir estados categóricos"}, 
		{"objectStatesTip", "Inserir entidades como estados"}, 
		{"booleanStatesTip", "Inserir estados booleanos"}, 
		{"addStateTip", "Inserir estado(s)"}, 
		{"removeState", "Remover estado"},
		{"addPreDefinedState", "Adicionar um estado pré-criado"}, 
		{"confirmation", "Confirmação"}, 
		{"warningDeletStates", "Os estados anteriores serão removidos. Tem certeza que deseja realizar a operação?"}, 
		
		/* PLM file manager */
		/* TODO transfer it to IO package? */
		{"FileSaveOK" , "Arquivo armazenado com sucesso"},
		{"FileLoadOK" , "Arquivo carregado com sucesso"},
		{"NoSSBN" , "Não há SSBN gerada anteriormente! Modo não disponivel."},
		{"KBClean" , "Base de conhecimento limpa com sucesso"},
		{"NotImplemented" , "Funcionalidade ainda não implementada"}, 
		{"loadedWithErrors" , "Arquivo carregado, mas pode haver falhas"},
		
		/* Aboult pane */
		{"AboultPane" , "Sobre"},	
		
		{"ReadLicense" , "Licença"},
		{"Features" , "Características"},
		{"VersionHistory" , "Histórico"},
		{"CloseAboultPane" , "Fechar"},
		
		{"Collaborators" , "Colaboradores"},	
		
		{"Version" , "Versão"},	
		{"Buildid" , "ID do Produto"},	
		
        //Splash loader */
		{"loading" , "carregando"}
	};
}