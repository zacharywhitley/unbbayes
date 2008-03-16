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
		{"fileDirectoryType","Diretório"},
		{"fileARFFType","Arquivo Arff"},
		{"fileTXTType","Arquivo Texto TXT"},
		{"fileNETType","Arquivo de Rede Bayesiana NET"},
		{"fileGenericType","Arquivo Genérico"},
		
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
		{"powerloomFileFilter","Knowledge Base (.plm)"},
				
		{"xmlBIFFileFilter", "XMLBIF (.xml)"},		
		
		{"textFileFilter","Text (.txt)"},
		{"fileUntitled","SemNome.txt"},
		
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
		{"ordereableLabel", "É Ordenável"}, 
		{"descriptionLabel","Descrição"},
		{"compileToolTip","Compilar árvore de junção"},
		{"moreToolTip","Adicionar estado"},
		{"lessToolTip","Remover estado"},
		{"arcToolTip","Inserir arco"},
		{"probabilisticNodeInsertToolTip","Inserir variável de probabilidade"},
		{"decisionNodeInsertToolTip","Inserir variavel de decisão"},
		{"utilityNodeInsertToolTip","Inserir variável de utilidade"},
		{"contextNodeInsertToolTip","Inserir variável de contexto"},
		{"inputNodeInsertToolTip","Inserir variável de entrada"},
		{"ordinaryVariableInsertToolTip","Inserir variável ordinária"},
		{"selectObjectToolTip","Selecionar objeto"},
		{"residentNodeInsertToolTip","Inserir variável residente"},
		{"mFragInsertToolTip","Inserir MFrag"},	
		{"inputActiveToolTip","Input Node Selecionado"},  
		{"mFragActiveToolTip","MFrag Selecionada"}, 		
		{"contextActiveToolTip","Context Node Selecionado"}, 
		{"residentActiveToolTip","Resident Node Selecionado"}, 		
		{"addArgumentToolTip","Adicionar argumento"}, 
		{"editFormulaToolTip","Editar formula"},		
		{"selectToolTip","Selecionar vários nós e arcos"},
		{"printTableToolTip","Imprimir a tabela"},
		{"previewTableToolTip","Visualizar a impressão da tabela"},
		{"saveTableImageToolTip","Salvar a tabela como imagem gif"},
		{"newEntityToolTip","Criar nova entidade"},		
		{"delEntityToolTip","Deletar entidade"},
		{"newOVariableToolTip","Criar nova var. ordinária"},
		{"delOVariableToolTip", "Deletar var. ordinária"}, 
		{"newArgumentToolTip","Adicionar nova v. ord. à lista de argumentos"},
		{"delArgumentToolTip", "Remover var. ord. da lista de argumentos"}, 		
		{"downArgumentToolTip", "Aicionar a lista de argumentos v. ord. selecionada"}, 		
		{"mTheoryEditionTip", "Editar atributos da MTheory"}, 		
		{"isGloballyExclusive", "Exclusivo globalmente"}, 
		{"resetToolTip", "Resetar"},	
		{"deleteSelectedItemToolTip", "Deletar item selecionado"},	
		{"menuOpen", "Abrir"},	
		
		{"showMTheoryToolTip","Mostrar Árvore da MTheory"},	
		{"showEntitiesToolTip","Mostrar entidades da MTheory"},
		{"showOVariablesToolTip","Mostrar ovariables da MFrag"},
		{"showEntityInstancesToolTip","Mostrar painél de edição de instâncias de entidades"},			
		{"showFingingsToolTip","Mostrar painél de edição de findings"},
		
		{"executeQueryToolTip","Executar query"},
		{"turnToSSBNModeToolTip","Mudar para o Modo SSBN"},
		{"clearKBToolTip","Limpar Base de Conhecimento"},
		{"loadKBToolTip","Carregar Base de Conhecimento"},
		{"saveKBToolTip","Salvar Base de Conhecimento"},
		
		{"formula","Formula:"},	
		{"inputOf","Input de:"},	
		{"arguments", "Args: "}, 	
		{"statusReadyLabel","Pronto"},
		
		{"andToolTip", "operador 'e'"}, 
		{"orToolTip", "operador 'ou'"},
		{"notToolTip", "operador 'não'"},
		{"equalToToolTip", "operador 'igual'"},
		{"impliesToolTip", "operador 'implica'"},
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
		{"ResidentTabTitle", "Nó Residente"}, 
		{"InputTabTitle", "Nó de Input"}, 
		{"ContextTabTitle", "Nó de Contexto"}, 
		{"MTheoryTreeTitle", "Árvore MTheory"}, 
		{"EntityTitle", "Entidade"}, 
		{"OVariableTitle", "Variável Ord."},
		{"ArgumentTitle", "Argumentos"}, 
		{"StatesTitle", "Estados"}, 	
		{"FathersTitle", "Nós Pais"}, 		
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
       
        {"operationError","Erro na operação"},           
        {"oVariableAlreadyIsArgumentError","Variável ord. já é argumento deste nó!"},       
        {"properties","Propriedades..."},
        {"nameException","Erro no Nome"},
        {"nameDuplicated", "Nome já existe..."}, 
        {"siglaError","A sigla só pode ter letras e números."},
        {"descriptionError","A descrição só pode ter letras e números."},
        
        /* Query Panel*/
        {"argumentFault","Argumentos incompletos. Query não pode ser executada."},
        {"inconsistentArgument","Argumentos inconsistentes. Query não pode ser executada."}, 
        {"selectOneVariable","Selecione uma variável:"}, 
        {"selectArgsValues","Selecione os valores dos argumentos:"}, 
        
        /* FormulaTreeConstructionException */
        {"notOperator", "Não é permitido operador nesta posição"}, 
        
		{"sucess", "Sucesso"}, 
		{"error", "Erro"},
        
		/* Tips for buttons of the table edition */
		{"deleteTip", "Deleta o texto selecionado"}, 
		{"anyTip", "Inserir construção \"If any\""}, 
		{"allTip", "Inserir construção \"If all\""}, 
		{"else", "Inserir \"else\""}, 
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
		
        //Splash loader */
		{"loading" , "carregando"}
	};
}