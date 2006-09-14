/*
 *  UnbBayes
 *  Copyright (C) 2002 Universidade de Bras�lia
 *
 *  This file is part of UnbBayes.
 *
 *  UnbBayes is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  UnbBayes is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with UnbBayes; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package unbbayes.controller;

import java.awt.Cursor;
import java.io.File;
import java.io.IOException;
import java.util.ResourceBundle;

import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.xml.bind.JAXBException;

import unbbayes.gui.MSBNWindow;
import unbbayes.gui.NetworkWindow;
import unbbayes.gui.UnBBayesFrame;
import unbbayes.io.BaseIO;
import unbbayes.io.NetIO;
import unbbayes.io.XMLIO;
import unbbayes.prs.Edge;
import unbbayes.prs.Node;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.msbn.SingleAgentMSBN;
import unbbayes.util.NodeList;

/**
 *  This class is responsible for creating, loading and saving networks supported by UnBBayes.
 *
 * @author     Rommel Novaes Carvalho
 * @author     Michael S. Onishi
 * @created    27 de Junho de 2001
 * @version    1.5 2006/09/14
 */
public class MainController {

    private UnBBayesFrame screen;

	/** Load resource file from this package */
  	private static ResourceBundle resource = ResourceBundle.getBundle("unbbayes.controller.resources.ControllerResources");

    /**
     *  Contructs the main controller with the UnBBayes main frame.
     */
    public MainController() {
        screen = new UnBBayesFrame(this);
    }

    /**
     * This method is responsible for creating a new probabilistic network.
     *
     */
    public void newPN() {
        ProbabilisticNetwork net = new ProbabilisticNetwork("New PN");
		NetworkWindow netWindow = new NetworkWindow(net);
		screen.addWindow(netWindow);
    }
    
    /**
     * This method is responsible for creating a new MSBN.
     *
     */
    public void newMSBN() {
    	SingleAgentMSBN msbn = new SingleAgentMSBN("New MSBN");
    	MSBNController controller = new MSBNController(msbn);
    	screen.addWindow(controller.getPanel());
    }
    
    /**
     * This method is responsible for creating a new MEBN.
     *
     */
    public void newMEBN() {
    	MultiEntityBayesianNetwork mebn = new MultiEntityBayesianNetwork("New MEBN");
    	NetworkWindow netWindow = new NetworkWindow(mebn);
		screen.addWindow(netWindow);
    }

    /**
     *  Saves the probabilistic network in both .net and .xml format, depending
     *  on the file's extension, or saves the MSBN if the file given is a directory.
     *
     * @param file The file where to save the network.
     */
    public void saveNet(File file) {
        screen.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        try {
			BaseIO io = null;
			JInternalFrame window = screen.getSelectedWindow();
			if (file.isDirectory()) {
				io = new NetIO();
				io.saveMSBN(file, ((MSBNWindow) window).getMSNet());								
			} else {
				String name = file.getName().toLowerCase();							
				if (name.endsWith("net")) {
					io = new NetIO();		
				} else if (name.endsWith("xml")){
					io = new XMLIO();
				}
				io.save(file, ((NetworkWindow) window).getRede());
        	}
        } catch (IOException e) {
            JOptionPane.showMessageDialog(screen, e.getMessage(), "saveNetException", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } catch (JAXBException je){
        	je.printStackTrace(); 
        } finally {
        	screen.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
    }


    /**
     *  Loads the probabilistic network from both .net and .xml format, depending
     *  on the file's extension, or loads the MSBN if the file given is a directory.
     */
    public void loadNet(File file) {
        screen.setCursor(new Cursor(Cursor.WAIT_CURSOR));        
        try {
        	JInternalFrame window = null;
			BaseIO io = null;
        	if (file.isDirectory()) { //MSBN
        		io = new NetIO();
        		SingleAgentMSBN msbn = io.loadMSBN(file);	
        		MSBNController controller = new MSBNController(msbn);
        		window = controller.getPanel();
        	} else {
				String name = file.getName().toLowerCase();				
				if (name.endsWith("net")) {
					io = new NetIO();					
				} else if (name.endsWith("xml")){
					io = new XMLIO();					
				}
				ProbabilisticNetwork net = io.load(file);
				window = new NetworkWindow(net);
        	}
			screen.addWindow(window);
        } catch (Exception e){
            JOptionPane.showMessageDialog(screen, e.getMessage(), resource.getString("loadNetException"), JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } finally {
        	screen.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
    }


    /**
     * Method responsible for creating a network based on its variables.
     *
     * @param nodeList List of nodes to create the network.
     * @return The probabilistic network created.
     */
    public ProbabilisticNetwork makeProbabilisticNetwork(NodeList nodeList) {
        screen.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        ProbabilisticNetwork net = new ProbabilisticNetwork("learned net");
        Node noFilho = null;
        Node noPai = null;
        Edge arcoAux = null;
        Node aux;
        boolean direction = true;
        for (int i = 0; i < nodeList.size(); i++) {
            noFilho = nodeList.get(i);
            net.addNode(noFilho);
            for (int j = 0; j < noFilho.getParents().size(); j++) {
            	noPai = (Node)noFilho.getParents().get(j);
            	noPai.getChildren().add(noFilho);
                arcoAux = new Edge(noPai, noFilho);
            	for(int k = 0 ; k < noPai.getParents().size() && direction; k++){
            	    aux = (Node)noPai.getParents().get(k);
            	    if(aux == noFilho){
            	        noPai.getParents().remove(k);
            	        direction = false;
            	    }                      		
            	}                 
                arcoAux = new Edge(noPai, noFilho);                
              	arcoAux.setDirection(direction);                	
              	direction = true;
                net.getEdges().add(arcoAux);
            }
        }        		
		return net;
    }
    
    /**
     * Shows the given probabilistic network in edition or compilation mode.
     *  
     * @param net
     */
    public void showProbabilisticNetwork(ProbabilisticNetwork net){
    	NetworkWindow netWindow = new NetworkWindow(net);
		if (! netWindow.getNetworkController().compileNetwork()) {
            netWindow.changeToPNEditionPane();            
            
        } else{
            netWindow.changeToPNCompilationPane();		
		}		
		screen.addWindow(netWindow);
        screen.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));    	
    }

}

