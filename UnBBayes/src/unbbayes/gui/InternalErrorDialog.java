package unbbayes.gui;

import java.util.ResourceBundle;

import javax.swing.JOptionPane;

/**
 * Janela que informa para o usuário que ocorreu um erro interno. 
 * Este painel é mostrado quando ocorre um erro que não deveria ocorrer: 
 * alguma condição que foi aceita como verdadeira e na execução se mostrou falsa. 
 * Possibilita que o usuário envie para os desenvolvedores um e-mail com a 
 * descrição do erro para possível correção. 
 * 
 * @author Laecio Lima dos Santos
 * @version 1.0 06/05/07
 *
 */
public class InternalErrorDialog {

	/** Load resource file from this package */
  	private static ResourceBundle resource = ResourceBundle.getBundle("unbbayes.gui.resources.GuiResources");
	
	//TODO concluir o painel, colocando opções para reporte do erro... 
	public InternalErrorDialog(){
		JOptionPane.showMessageDialog(
				null, 
				resource.getString("internalError"), 
				resource.getString("error"), 
				JOptionPane.ERROR_MESSAGE);        	
	}
	
}
