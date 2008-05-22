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
package unbbayes.controller;

import java.awt.Cursor;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.ResourceBundle;

import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.xml.bind.JAXBException;

import unbbayes.gui.Configurations;
import unbbayes.gui.MSBNWindow;
import unbbayes.gui.NetworkWindow;
import unbbayes.gui.SplashScreen;
import unbbayes.gui.UnBBayesFrame;
import unbbayes.io.BaseIO;
import unbbayes.io.NetIO;
import unbbayes.io.XMLIO;
import unbbayes.io.configurations.ConfigurationsIO;
import unbbayes.io.configurations.ConfigurationsIOInputStream;
import unbbayes.io.mebn.MebnIO;
import unbbayes.io.mebn.PrOwlIO;
import unbbayes.io.mebn.UbfIO;
import unbbayes.prs.Edge;
import unbbayes.prs.Node;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.msbn.SingleAgentMSBN;
import unbbayes.util.Debug;
import edu.isi.powerloom.PLI;
import edu.stanford.smi.protegex.owl.ProtegeOWL;


/**
 *  This class is responsible for creating, loading and saving networks supported by UnBBayes.
 *
 * @author     Rommel Novaes Carvalho
 * @author     Michael S. Onishi
 * @created    June, 27th, 2001
 * @version    1.5 2006/09/14
 */
public class MainController {
	
	private UnBBayesFrame screen;
	
	private static boolean PRE_LOAD_PROTEGE; 
	private static boolean PRE_LOAD_POWERLOOM; 
	
	
	/** Load resource file from this package */
	private static ResourceBundle resource = ResourceBundle.getBundle("unbbayes.controller.resources.ControllerResources");
	
	/**
	 *  Contructs the main controller with the UnBBayes main frame.
	 */
	public MainController() {
//		eagleLoader(); 
		loadConfigurations(); 
		
		screen = new UnBBayesFrame(this);
	}
	
	public void loadConfigurations(){
		ConfigurationsController configController = ConfigurationsController.getInstance(); 
		ConfigurationsIO configurationsIO = new ConfigurationsIOInputStream(); 
		
		try {
			Configurations configurations = configurationsIO.load(new File(configController.getFileConfigurationsPath()));
			configController.setConfigurations(configurations); 
			configController.setFileOpenedSucessfull(true); 
		} catch (IOException e) {
			e.printStackTrace(); 
			configController.setFileOpenedSucessfull(false); 
		}
	}
	
	public void saveConfigurations(){
		ConfigurationsController configController = ConfigurationsController.getInstance(); 
		ConfigurationsIO configurationsIO = new ConfigurationsIOInputStream(); 
		
		try {
			configurationsIO.save(new File(configController.getFileConfigurationsPath()), configController.getConfigurations());
		} catch (IOException e) {
			e.printStackTrace(); 
		}
	}

	/**
	 * Pre-loading of api's for a better performance
	 */
	private void eagleLoader(){
		
		SplashScreen splashScreen = new SplashScreen(); 
		
		splashScreen.pack(); 
		splashScreen.setVisible(true); 
		
		initializeLoadConfigurations(); 
		
		Debug.println("Init loader Protege"); 
		
		//load Protege
		if(PRE_LOAD_PROTEGE){
		   ProtegeOWL.createJenaOWLModel();
		}
		
		Debug.println("Init loader Powerloom"); 
		
		//load Powerloom
		if(PRE_LOAD_POWERLOOM){
		   PLI.initialize();
		}
		
		Debug.println("Finish loader"); 
		
		splashScreen.dispose(); 
	}
	
	/**
	 * Initialize the load configurations of the plugins used by the UnBayes. 
	 * 
	 * TODO: This configurations will be in a external config file. 
	 */
	private void initializeLoadConfigurations(){
		PRE_LOAD_POWERLOOM = true; 
		PRE_LOAD_PROTEGE = true; 
	}
	
	
	/**
	 * This method is responsible for creating a new probabilistic network.
	 *
	 */
	public void newPN() {
		ProbabilisticNetwork net = new ProbabilisticNetwork(resource.getString("NewPNName"));
		NetworkWindow netWindow = new NetworkWindow(net);
		screen.addWindow(netWindow);
	}
	
	/**
	 * This method is responsible for creating a new MSBN.
	 *
	 */
	public void newMSBN() {
		SingleAgentMSBN msbn = new SingleAgentMSBN(resource.getString("NewMSBNName"));
		MSBNController controller = new MSBNController(msbn);
		screen.addWindow(controller.getPanel());
	}
	
	/**
	 * This method is responsible for creating a new MEBN.
	 *
	 */
	public void newMEBN() {
		MultiEntityBayesianNetwork mebn = new MultiEntityBayesianNetwork(resource.getString("NewMEBNName"));
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
		
		MebnIO ubfIo = UbfIO.getInstance();
		
		try {
			BaseIO io = null;
			PrOwlIO prOwlIo = null; 
			JInternalFrame window = screen.getSelectedWindow();
			
			if(window == null){
				JOptionPane.showMessageDialog(screen, resource.getString("windowDontExists") , resource.getString("error"), JOptionPane.ERROR_MESSAGE);
				return; 
			}
			
			if (file.isDirectory()) {
				io = new NetIO();
				if (!(window instanceof MSBNWindow)){
					JOptionPane.showMessageDialog(screen, resource.getString("msbnDontExists") , resource.getString("error"), JOptionPane.ERROR_MESSAGE);
				} else{
					io.saveMSBN(file, ((MSBNWindow) window).getMSNet());		
				}
			} else {
				String name = file.getName().toLowerCase();							
				if (name.endsWith("net")) {
					io = new NetIO();		
				} 
				else if (name.endsWith("xml")){
					io = new XMLIO();
				}
				else if (name.endsWith(UbfIO.fileExtension)) {
					ubfIo = UbfIO.getInstance();
					/*
					 } else if (name.endsWith("owl")){
					 prOwlIo = new PrOwlIO();
					 */
				}
				
				if (io != null)
					if (!(window instanceof NetworkWindow)){
						JOptionPane.showMessageDialog(screen, resource.getString("bnDontExists") , resource.getString("error"), JOptionPane.ERROR_MESSAGE);
					}
					else{
						io.save(file, ((NetworkWindow) window).getSingleEntityNetwork());
					}
				else { 
					if (ubfIo != null) {
						if (!(window instanceof NetworkWindow)){
							JOptionPane.showMessageDialog(screen, resource.getString("mebnDontExists") , resource.getString("error"), JOptionPane.ERROR_MESSAGE);
						}
						else{
							try {
								if(((NetworkWindow) window).getMultiEntityBayesianNetwork() == null){
									JOptionPane.showMessageDialog(screen, resource.getString("mebnDontExists") , resource.getString("error"), JOptionPane.ERROR_MESSAGE);
								}
								ubfIo.saveMebn(file, ((NetworkWindow) window).getMultiEntityBayesianNetwork()); 
								JOptionPane.showMessageDialog(screen, resource.getString("saveSucess") , resource.getString("sucess"), JOptionPane.INFORMATION_MESSAGE);
							} catch (Exception e) {
								JOptionPane.showMessageDialog(screen, e.getMessage(), "saveNetException", JOptionPane.ERROR_MESSAGE);        	
								e.printStackTrace(); 
							}
						}
					}
					else{
						JOptionPane.showMessageDialog(screen, resource.getString("withoutPosfixe") , resource.getString("error"), JOptionPane.ERROR_MESSAGE);							
					}
				}
			}
		} catch (IOException e) {
			JOptionPane.showMessageDialog(screen, e.getMessage(), "saveNetException", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		} catch (JAXBException je){
			je.printStackTrace(); 
		} /*catch (IOMebnException me){
		JOptionPane.showMessageDialog(screen, me.getMessage(), "saveNetException", JOptionPane.ERROR_MESSAGE);        	
		me.printStackTrace(); 
		}*/
		
		finally {
			screen.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}
	}
	
	
	/**
	 *  Loads the probabilistic network from both .net and .xml format, depending
	 *  on the file's extension, or loads the MSBN if the file given is a directory.
	 */
	public void loadNet(final File file) {
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
					ProbabilisticNetwork net = io.load(file);
					ConfigurationsController.getInstance().addFileToListRecentFiles(file); 
					window = new NetworkWindow(net);	
					((NetworkWindow)window).setFileName(name); 
				} else if (name.endsWith("xml")){
					io = new XMLIO();	
					ProbabilisticNetwork net = io.load(file);
					ConfigurationsController.getInstance().addFileToListRecentFiles(file); 
					window = new NetworkWindow(net);	
					((NetworkWindow)window).setFileName(name); 
				} else if (name.endsWith("owl")){

//					JProgressBar jpb = new JProgressBar();
//					jpb.setIndeterminate(true); 
//					JDialog jDialog = new JDialog(); 
//					JPanel jpanel = new JPanel();
//					jpanel.setLayout(new BorderLayout()); 
//					jpanel.add(jpb, BorderLayout.CENTER); 
//					jDialog.setContentPane(jpanel); 
//					jDialog.pack(); 
//					jDialog.setVisible(true); 
//					jpb.paintImmediately(0, 0, jpb.getWidth(), jpb.getHeight()); 
//					jDialog.repaint(0, 0, jDialog.getWidth(), jDialog.getHeight()); 
//					jDialog.setLocationRelativeTo(UnBBayesFrame.getIUnBBayes()); 
//					
//					MultiEntityBayesianNetwork mebn; 
//					
//					Runnable runnable = new Runnable(){
//
//						public void run() {
//							PrOwlIO prOwlIo = new PrOwlIO(); 
//							try {
//								MultiEntityBayesianNetwork mebn = prOwlIo.loadMebn(file);
//								JOptionPane.showMessageDialog(screen, resource.getString("JAXBExceptionFound"), resource.getString("loadNetException"), JOptionPane.ERROR_MESSAGE);
//							} catch (IOException e) {
//								// TODO Auto-generated catch block
//								e.printStackTrace();
//							} catch (IOMebnException e) {
//								// TODO Auto-generated catch block
//								e.printStackTrace();
//							}
//						}
//						
//					}; 
//					
//					Thread thread = new Thread(runnable); 
//					thread.start();
//					
//					Thread.sleep(10000); 
					
					MultiEntityBayesianNetwork mebn; 
					PrOwlIO prOwlIo = new PrOwlIO(); 
					mebn = prOwlIo.loadMebn(file);
					ConfigurationsController.getInstance().addFileToListRecentFiles(file); 
					window = new NetworkWindow(mebn);
					((NetworkWindow)window).setFileName(name); 
				
				}  else if (name.endsWith(UbfIO.fileExtension)) {        			
					MebnIO ubfIo = UbfIO.getInstance(); 
					MultiEntityBayesianNetwork mebn = ubfIo.loadMebn(file);
					ConfigurationsController.getInstance().addFileToListRecentFiles(file); 
					window = new NetworkWindow(mebn);	
					((NetworkWindow)window).setFileName(name); 
				    
				}
			}
			screen.addWindow(window);
		}  catch (JAXBException je){
			JOptionPane.showMessageDialog(screen, resource.getString("JAXBExceptionFound"), resource.getString("loadNetException"), JOptionPane.ERROR_MESSAGE);
			je.printStackTrace();        	
		}
		catch (Exception e){
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
	public ProbabilisticNetwork makeProbabilisticNetwork(ArrayList<Node> nodeList) {
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

