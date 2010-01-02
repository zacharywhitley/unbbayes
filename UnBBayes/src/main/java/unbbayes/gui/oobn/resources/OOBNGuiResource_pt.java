/**
 * 
 */
package unbbayes.gui.oobn.resources;

import java.util.ListResourceBundle;

/**
 * <p>Title: UnBBayes</p>
 * <p>Description: Resources file for unbbayes.gui.oobn package. Localization = portuguese.</p>
 * <p>Copyleft: LGPL 2008</p>
 * <p>Company: UnB</p>
 * @author Shou Matsumoto
 * @version 1.0
 * @since 12/11/2008
 */
public class OOBNGuiResource_pt extends ListResourceBundle {

	/**
	 *  Override getContents and provide an array, where each item in the array is a pair
	 *	of objects. The first element of each pair is a String key,
	 *	and the second is the value associated with that key.
	 *
	 * @return The resources' contents
	 */
	public Object[][] getContents() {
		return contents;
	}

	/**
	 * The resources
	 */
	static final Object[][] contents =
	{	
		{"OOBNPopupMenuMessage","Alterar o tipo do nó para:"},
		{"changeNodeToPrivate","Alterar para Nó Privado"},
		{"changeNodeToOutput","Alterar para Nó de Saída (Output)"},
		{"changeNodeToInput","Alterar para Nó de Entrada (Input)"},
		{"OOBNPopupMenuTooltipMessage","Selecione um tipo de nó"},
		{"openClassFromFile","Abrir nova classe a partir de um arquivo"},
		

		{"ErrorLoadingClass","Houve um erro ao carregar classe"},
		
		{"editionToolTip","Passar para o modo de edição"},
		{"removeToolTip","Remove a classe do projeto"},
		{"newToolTip","Adiciona uma nova classe ao projeto"},
		{"newFromFileToolTip","Carrega nova classe a partir de um arquivo"},
		{"status","Estado:"},
		{"newOOBNClass","NovaClasseOOBN"},
		{"renameClass", "Renomeando a classe oobn"},
		{"oobnFileFilter","Net (.net), Net para OOBN (.oobn)"},
		{"NoClassSelected","Nenhuma classe OOBN foi selecionada"},
		{"compilationError" , "Erro de Compilação"},
		{"DuplicatedClassName","Nome duplicado para a classe"},
		
		{"CannotDragNDrop", "Não foi possível arrastar a classe"},
		{"dragNDropToAddInstance", "Arraste uma classe daqui para adicionar uma instância"},

		{"compileToolTip","Compilar a OOBN usando a classe selecionada"},
		{"statusReadyLabel", "Pronto"},
		

		{"classNavigationPanelLabel", "Lista de classes"},
		

		{"leftClickToChangeNodeType", "Clique com o botão direito para alterar o tipo do nó"},
		

		{"changeNodeType", "Alterar o tipo do nó selecionado"},
		
		{"saveTitle", "Armazenar a classe atual"},
		
		{"unsupportedGraphFormat" , "Este módulo/plugin não possui suporte a este formato."},
		

		{"resizeToFitText" , "Ajustar tamanho"},
		{"changeColor" , "Alterar Cor"},
		
	};
}
