package unbbayes.gui;

import java.util.ResourceBundle;

import javax.swing.JOptionPane;

/**
 * Janela que informa para o usu�rio que ocorreu um erro interno. 
 * Este painel � mostrado quando ocorre um erro que n�o deveria ocorrer: 
 * alguma condi��o que foi aceita como verdadeira e na execu��o se mostrou falsa. 
 * Possibilita que o usu�rio envie para os desenvolvedores um e-mail com a 
 * descri��o do erro para poss�vel corre��o. 
 * 
 * @author Laecio Lima dos Santos
 * @version 1.0 06/05/07
 *
 */
public class InternalErrorDialog {

	/** Load resource file from this package */
  	private static ResourceBundle resource = ResourceBundle.getBundle("unbbayes.gui.resources.GuiResources");
	
	//TODO concluir o painel, colocando op��es para reporte do erro... 
	public InternalErrorDialog(){
		JOptionPane.showMessageDialog(
				null, 
				resource.getString("internalError"), 
				resource.getString("error"), 
				JOptionPane.ERROR_MESSAGE);        	
	}
	
}
