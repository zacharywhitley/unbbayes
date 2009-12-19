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
  	private static ResourceBundle resource = ResourceBundle.getBundle(
  			unbbayes.gui.resources.GuiResources.class.getName());
	
	//TODO concluir o painel, colocando op��es para reporte do erro... 
	public InternalErrorDialog(){
		JOptionPane.showMessageDialog(
				null, 
				resource.getString("internalError"), 
				resource.getString("error"), 
				JOptionPane.ERROR_MESSAGE);        	
	}
	
}
