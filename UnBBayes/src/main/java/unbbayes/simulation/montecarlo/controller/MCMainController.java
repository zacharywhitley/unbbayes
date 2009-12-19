/*
 *  UnBBayes
 *  Copyright (C) 2002, 2009 Universidade de Brasilia - http://www.unb.br
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
import java.util.ResourceBundle;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import unbbayes.gui.LongTaskProgressBar;
import unbbayes.gui.SimpleFileFilter;
import unbbayes.io.BaseIO;
import unbbayes.io.NetIO;
import unbbayes.io.XMLBIFIO;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.simulation.montecarlo.gui.MCParametersPane;
import unbbayes.simulation.montecarlo.io.MonteCarloIO;
import unbbayes.simulation.montecarlo.sampling.IMonteCarloSampling;

/**
 * Class responsible for controlling the user action for generating samples for a BN.
 * 
 * @author Danilo CustÃ³dio (danilocustodio@gmail.com)
 * @author Rommel Carvalho (rommel.carvalho@gmail.com)
 */
public class MCMainController {
	
	/** Load resource file from this package */
  	private static ResourceBundle resource = ResourceBundle.getBundle(
  			unbbayes.simulation.montecarlo.resources.MCResources.class.getName());
	
	private MCParametersPane paramPane;
	private BaseIO io;
	private ProbabilisticNetwork pn;
	private IMonteCarloSampling mc;
	
	public MCMainController(IMonteCarloSampling mc){	
		this.mc = mc;
		getNet();
		paramPane = new MCParametersPane();
		addListeners();
	}	
	
	private void getNet(){			
		try{			
			String[] nets = new String[] { "net", "xml" };
			JFileChooser chooser = new JFileChooser(".");
			chooser.setMultiSelectionEnabled(false);				
			chooser.addChoosableFileFilter(
				new SimpleFileFilter(nets, resource.getString("netFileFilter")));
			int option = chooser.showOpenDialog(null);
			if (option == JFileChooser.APPROVE_OPTION) {
				if (chooser.getSelectedFile() != null) {
					String fileName = chooser.getSelectedFile().getName();
					if(fileName.endsWith(".net")){
						io = new NetIO();						
					}
					else{
						io = new XMLBIFIO(); 
					}
					pn = (ProbabilisticNetwork)io.load(chooser.getSelectedFile());
				}
			}
		}catch(Exception e){
        	JOptionPane.showMessageDialog(paramPane, resource.getString("loadNetException"), resource.getString("error"), JOptionPane.ERROR_MESSAGE);
        }
	}
	
	public void addListeners(){
		paramPane.addOKListener(okListener);
	}
	
	private int validaNatural(String n){
		try{
			int numero = Integer.parseInt(n);
			if(numero > 0){			
				return numero;
			}
			return -1;
		}catch(NumberFormatException nfe){
			return -1;			
		}		
	}
	
	ActionListener okListener = new ActionListener(){
		public void actionPerformed(ActionEvent ae){
			// to do on pressing OK
			new MonteCarloThread(MCMainController.this); // calling thread interface
		}	
	};
	
	public void startMC() {
		int n = validaNatural(paramPane.getSampleSize());
		if (n != -1) {
			// Start progress bar
	    	LongTaskProgressBar pb = new LongTaskProgressBar("Sampling", true);
	    	// Register progress bar as observer of the long task mc
	    	mc.registerObserver(pb);
	    	// Add thread to progress bar to allow canceling the operation
	    	pb.setThread(MonteCarloThread.t);
	    	// Run the long task
			mc.start(pn, n);
			// Hides the frame of the progress bar
			pb.hideProgressBar(); 
			try {
				MonteCarloIO io = new MonteCarloIO(mc.getSampledStatesMatrix());
				io.makeFile(mc.getSamplingNodeOrderQueue());
				JOptionPane.showMessageDialog(paramPane, resource
						.getString("saveSuccess"), resource
						.getString("success"), JOptionPane.INFORMATION_MESSAGE);
			} catch (IOException e) {
				JOptionPane.showMessageDialog(paramPane, resource
						.getString("saveException"), resource
						.getString("error"), JOptionPane.ERROR_MESSAGE);
			}
		} else {
			JOptionPane.showMessageDialog(paramPane, resource
					.getString("sampleSizeException"), resource
					.getString("error"), JOptionPane.ERROR_MESSAGE);
		}
		
		
	}

}