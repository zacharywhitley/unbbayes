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
package unbbayes.simulation.montecarlo.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.xml.bind.JAXBException;

import unbbayes.gui.SimpleFileFilter;
import unbbayes.io.BaseIO;
import unbbayes.io.NetIO;
import unbbayes.io.XMLIO;
import unbbayes.io.exception.LoadException;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.simulation.montecarlo.gui.MCParametersPane;
import unbbayes.simulation.montecarlo.sampling.MonteCarloSampling;

/**
 * Classe que controla as a��es relativas a gera��o de amostras pelo algoritimo de montecarlo
 * 
 * @author Danilo
 */
public class MCMainController {
	
	private MCParametersPane tp;
	private BaseIO io;
	ProbabilisticNetwork redeProbabilistica;	
	
	public MCMainController(){	
		
		getNet();
		
		tp = new MCParametersPane();
		adicionarListeners();
	}	
	
	private void getNet(){			
		try{			
			String[] nets = new String[] { "net", "xml" };
			JFileChooser chooser = new JFileChooser(".");
			chooser.setMultiSelectionEnabled(false);				
			chooser.addChoosableFileFilter(
					//TODO utilizar resources...
				new SimpleFileFilter(nets,"Carregar .net, .xml"));
			int option = chooser.showOpenDialog(null);
			if (option == JFileChooser.APPROVE_OPTION) {
				if (chooser.getSelectedFile() != null) {
					String fileName = chooser.getSelectedFile().getName();
					if(fileName.endsWith(".net")){
						io = new NetIO();						
					}
					else{
						io = new XMLIO(); 
					}
					redeProbabilistica = io.load(chooser.getSelectedFile());
				}
			}
		}catch(LoadException le){
			le.printStackTrace();
		}catch(IOException ie){
			ie.printStackTrace();
		} catch (JAXBException je){
        	je.printStackTrace(); 
        }
	}
	
	public void adicionarListeners(){
		tp.adicionaOKListener(okListener);
	}
	
	private int validaNatural(String n){
		try{
			int numero = Integer.parseInt(n);
			if(numero >= 0){			
				return numero;
			}
			return -1;
		}catch(NumberFormatException nfe){
			return -1;			
		}		
	}
	
	ActionListener okListener = new ActionListener(){
		public void actionPerformed(ActionEvent ae){
			int n = validaNatural(tp.getNumeroCasos());
			if(n>= 0){								
				new MonteCarloSampling(redeProbabilistica,n);
				tp.dispose();
			}else{			
				JOptionPane.showMessageDialog(null,"O numero de casos deve ser um natural","ERROR",JOptionPane.ERROR_MESSAGE);
			}
		}	
	};
	
	

}