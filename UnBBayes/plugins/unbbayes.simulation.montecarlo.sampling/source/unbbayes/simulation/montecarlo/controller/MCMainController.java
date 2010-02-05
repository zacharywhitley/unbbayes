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
 * @author Danilo Custódio (danilocustodio@gmail.com)
 * @author Rommel Carvalho (rommel.carvalho@gmail.com)
 */
public class MCMainController {
	
	/** Load resource file from this package */
  	private static ResourceBundle resource = unbbayes.util.ResourceController.newInstance().getBundle(
  			unbbayes.simulation.montecarlo.resources.MCResources.class.getName());
	
	private MCParametersPane paramPane;
	private BaseIO io;
	private ProbabilisticNetwork pn;
	private IMonteCarloSampling mc;
	
	/**
	 * Constructs this controller and run immediately
	 * @param mc
	 */
	public MCMainController(IMonteCarloSampling mc){	
		this(mc,true);
	}	
	
	/**
	 * Constructor using fields
	 * @param mc : the sampling to be used by thread
	 * @param run : if true, tells this controller to run the program immediately
	 */
	public MCMainController(IMonteCarloSampling mc, boolean run){	
		this.mc = mc;
		if (run) {
			loadNet();
			this.startupParameters();
		}
	}	
	
	private void loadNet(){			
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
					this.setPn((ProbabilisticNetwork)io.load(chooser.getSelectedFile()));
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
			if (getPn() != null) {
				new MonteCarloThread(MCMainController.this); // calling thread interface
			} else {
				throw new IllegalStateException(resource.getString("selectFile"));
			}
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

	/**
	 * @return the paramPane
	 */
	public MCParametersPane getParamPane() {
		return paramPane;
	}

	/**
	 * @param paramPane the paramPane to set
	 */
	public void setParamPane(MCParametersPane paramPane) {
		this.paramPane = paramPane;
	}

	/**
	 * 
	 * @return
	 */
	public ProbabilisticNetwork getPn() {
		return pn;
	}

	/**
	 * Sets the value of Probabilistic network 
	 * @param pn
	 */
	public void setPn(ProbabilisticNetwork pn) {
		this.pn = pn;
	}
	
	/**
	 * Opens the parameter panel.
	 */
	public void startupParameters() {
		if (getPn() == null) {
			throw new IllegalStateException(resource.getString("selectFile"));
		}
		paramPane = new MCParametersPane();
		addListeners();
	}

}