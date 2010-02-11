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
package unbbayes.learning.Gibbs.gui;

import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;

import unbbayes.gui.SimpleFileFilter;

/**
 * @author Administrador
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class GibbsFrame extends JFrame {

	/** Serialization runtime version number */
	private static final long serialVersionUID = 0;		
	
	public Container container;
	public JPanel gibbsPanel;
	public JButton btnChooseNet;
	public JButton btnContinue;
	public JButton btnCancel;
	public File file = null;
	
	public GibbsFrame(){
		super("Gibbs Sampler");
		container = super.getContentPane();
		container.add(getGibbsPanel());
		super.setVisible(true);			
		super.pack();
	}
	
	private JPanel getGibbsPanel(){
		gibbsPanel = new JPanel(new GridLayout(1,3));
		btnChooseNet = new JButton("Chosse Net");
		btnContinue = new JButton("Continue");
		btnCancel = new JButton("Cancel");
		gibbsPanel.add(btnChooseNet);
		gibbsPanel.add(btnContinue);
		gibbsPanel.add(btnCancel);	
		return gibbsPanel;			
	}
	
	ActionListener chooseNetListener = new ActionListener() {
	      public void actionPerformed(ActionEvent ae) {
	         //try {
	            String[] nets = new String[] { "net" };
	            JFileChooser chooser = new JFileChooser(".");
	            chooser.setMultiSelectionEnabled(false);
	            chooser.addChoosableFileFilter(
	               new SimpleFileFilter(nets, "Carregar .net"));
	            int option = chooser.showOpenDialog(null);
	            if (option == JFileChooser.APPROVE_OPTION) {
	               if (chooser.getSelectedFile() != null) {
	                  //pn = io.load(chooser.getSelectedFile());
	               }
	            }
	         /*} catch (LoadException le) {
	            le.printStackTrace();
	         } catch (IOException ie) {
	            ie.printStackTrace();
	         } catch (JAXBException je){
	        	je.printStackTrace(); 
	         }*/
	      }
	   };
	   
	   ActionListener cancelListener = new ActionListener() {
		      public void actionPerformed(ActionEvent ae) {
		         //gf.dispose();
		      }
		   };

		   ActionListener continueListener = new ActionListener() {
		      public void actionPerformed(ActionEvent ae) {
		         //gf.dispose();
				 //IOGibbs iog = new IOGibbs(data,variables);
				 //iog.makeFile();
		      }
		   };   
}
