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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.ResourceBundle;

import javax.swing.JInternalFrame;
import javax.xml.bind.JAXBException;

import unbbayes.controller.exception.InvalidFileNameException;
import unbbayes.controller.exception.NoObjectToBeSavedException;
import unbbayes.controller.oobn.OOBNController;
import unbbayes.gui.Configurations;
import unbbayes.gui.NetworkWindow;
import unbbayes.gui.SplashScreen;
import unbbayes.gui.UnBBayesFrame;
import unbbayes.io.BaseIO;
import unbbayes.io.DneIO;
import unbbayes.io.NetIO;
import unbbayes.io.XMLBIFIO;
import unbbayes.io.configurations.ConfigurationsIO;
import unbbayes.io.configurations.ConfigurationsIOInputStream;
import unbbayes.io.exception.LoadException;
import unbbayes.io.mebn.MebnIO;
import unbbayes.io.mebn.PrOwlIO;
import unbbayes.io.mebn.UbfIO;
import unbbayes.io.mebn.exceptions.IOMebnException;
import unbbayes.io.msbn.impl.DefaultMSBNIO;
import unbbayes.io.oobn.IObjectOrientedBayesianNetworkIO;
import unbbayes.io.oobn.impl.DefaultOOBNIO;
import unbbayes.prs.Edge;
import unbbayes.prs.Node;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.msbn.SingleAgentMSBN;
import unbbayes.prs.oobn.IObjectOrientedBayesianNetwork;
import unbbayes.prs.oobn.impl.ObjectOrientedBayesianNetwork;
import unbbayes.util.Debug;
import unbbayes.util.extension.UnBBayesModule;
import unbbayes.util.extension.builder.MEBNWindowBuilder;
import unbbayes.util.extension.builder.MSBNWindowBuilder;
import unbbayes.util.extension.builder.NamedWindowBuilder;
import unbbayes.util.extension.builder.NetworkWindowBuilder;
import unbbayes.util.extension.builder.OOBNWindowBuilder;
import edu.isi.powerloom.PLI;
import edu.stanford.smi.protegex.owl.ProtegeOWL;


/**
 *  This class is responsible for creating, loading and saving networks supported by UnBBayes.
 *
 * @author     Rommel Novaes Carvalho
 * @author     Michael S. Onishi
 * @created    June, 27th, 2001
 * @version    1.5 2006/09/14
 * 
 * 
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
		
		try {
			loadConfigurations();
		} catch (IOException e) {
			e.printStackTrace();
		} 
		
		screen = new UnBBayesFrame(this);
	}
	
	/**
	 * Load the configuration file and setting the configurations object in the 
	 * ConfigurationsController. 
	 * 
	 * @throws IOException Exception try to read the configurations file
	 */
	public void loadConfigurations() throws IOException{
		ConfigurationsController configController = ConfigurationsController.getInstance(); 
		ConfigurationsIO configurationsIO = new ConfigurationsIOInputStream(); 
		
		try {
			Configurations configurations = configurationsIO.load(new File(configController.getFileConfigurationsPath()));
			configController.setConfigurations(configurations); 
			configController.setFileOpenedSucessfull(true); 
		} catch (IOException e) { 
			configController.setFileOpenedSucessfull(false);
			throw e; 
		}
	}
	
	public void saveConfigurations() throws IOException{
		ConfigurationsController configController = ConfigurationsController.getInstance(); 
		ConfigurationsIO configurationsIO = new ConfigurationsIOInputStream(); 
		configurationsIO.save(new File(configController.getFileConfigurationsPath()), configController.getConfigurations());
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
		NamedWindowBuilder builder = new NetworkWindowBuilder();
		builder.setName(resource.getString("NewPNName"));
		screen.addWindow(builder.buildUnBBayesModule());
	}
	
	/**
	 * This method is responsible for creating a new MSBN.
	 *
	 */
	public void newMSBN() {
		NamedWindowBuilder builder = new MSBNWindowBuilder();
		builder.setName(resource.getString("NewMSBNName"));
		screen.addWindow(builder.buildUnBBayesModule());
	}
	
	/**
	 * This method is responsible for creating a new MEBN.
	 *
	 */
	public void newMEBN() {
		NamedWindowBuilder builder = new MEBNWindowBuilder();
		builder.setName(resource.getString("NewMEBNName"));
		screen.addWindow(builder.buildUnBBayesModule());
	}
	
	/**
	 * This method is responsible for creating a new OOBN.
	 *
	 */
	public void newOOBN() {
		NamedWindowBuilder builder = new OOBNWindowBuilder();
		builder.setName(resource.getString("NewOOBNName"));
		screen.addWindow(builder.buildUnBBayesModule());
	}
	
	/**
	 *  Saves the probabilistic network in both .net and .xml format, depending
	 *  on the file's extension, or saves the MSBN if the file given is a directory.
	 *
	 * @param file The file where to save the network.
	 * @param moduleToUse : what module/plugin we shall use in order to store the file
	 * @throws JAXBException 
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public boolean saveNet(File file, UnBBayesModule moduleToUse) throws NoObjectToBeSavedException, 
	                    IOMebnException, InvalidFileNameException, FileNotFoundException, 
	                    IOException, Exception{
		
		screen.setCursor(new Cursor(Cursor.WAIT_CURSOR));
		
		try {
			if (!moduleToUse.getIO().supports(file, false)) {
				throw new InvalidFileNameException(this.resource.getString("cannotHandleFileFormat"));
			}
			moduleToUse.getIO().save(file, moduleToUse.getPersistingGraph());
		} catch (NullPointerException e) {
			throw new NoObjectToBeSavedException(this.resource.getString("cannotHandleFileFormat"),e);
		} finally {
			screen.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}
		
		return true;
		
//		MebnIO ubfIo = null;
//		IObjectOrientedBayesianNetworkIO oobnIO = null;
//		
//		// TODO stop using this sequences of if-else conditionals and start using object binding conditionals
//		
//		try{
//			BaseIO io = null;
////			PrOwlIO prOwlIo = null; 
//			JInternalFrame window = screen.getSelectedWindow();
//			
//			if(window == null){
//				throw new NoObjectToBeSavedException(resource.getString("windowDontExists")); 
//			}
//			
//			if (file.isDirectory()) {
//				io = DefaultMSBNIO.newInstance();
//				if (!(window instanceof MSBNWindow)){
//					throw new NoObjectToBeSavedException(resource.getString("msbnDontExists"));
//				} else{
//					io.save(file, ((MSBNWindow) window).getMSNet());	
//					return true; 
//				}
//			} else {
//				
//				String name = file.getName().toLowerCase();							
//				if (name.endsWith("net")) {
//					io = new NetIO();		
//				} 
//				else if (name.endsWith("xml")){
//					io = new XMLBIFIO();
//				}
//				else if (name.endsWith(UbfIO.FILE_EXTENSION)) {
//					ubfIo = UbfIO.getInstance();
//				}
//				else if (name.endsWith(IObjectOrientedBayesianNetworkIO.fileExtension)) {
//					if (window instanceof OOBNWindow) {
//						oobnIO = DefaultOOBNIO.newInstance(((OOBNWindow)window).getController().getOobn());
//					}
//				}
//				
//				if (io != null)
//					if (!(window instanceof NetworkWindow)){
//						throw new NoObjectToBeSavedException(resource.getString("bnDontExists"));
//					}
//					else{
//						io.save(file, ((NetworkWindow) window).getSingleEntityNetwork());
//						return true; 
//					}
//				else { 
//					if (ubfIo != null) {
//						if (!(window instanceof NetworkWindow)){
//							throw new NoObjectToBeSavedException(resource.getString("mebnDontExists"));
//						}
//						else{
//							if(((NetworkWindow) window).getMultiEntityBayesianNetwork() == null){
//								throw new NoObjectToBeSavedException(resource.getString("mebnDontExists"));
//							}
//							ubfIo.saveMebn(file, ((NetworkWindow) window).getMultiEntityBayesianNetwork()); 
//							return true; 
//
//						}
//					} else if (oobnIO != null) {
//						if (window instanceof OOBNWindow) {
//							oobnIO.saveOOBNClass(file, ((OOBNWindow)window).getController().getActive().getController().getControlledClass());
//							return true;
//						}
//						return false;
//					}
//					else{
//						throw new InvalidFileNameException(resource.getString("withoutPosfixe"));							
//					}
//				}
//			}
//		}
//		finally {
//			screen.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
//		}
	}
	
	
	/**
	 *  Loads the probabilistic network from both .net and .xml format, depending
	 *  on the file's extension, or loads the MSBN if the file given is a directory.
	 * @param file : file to load
	 * @param moduleToUse : the module/plugin to use in order to load file.
	 * @return the new created module filled the network described by file
	 * @throws IOException 
	 */
	public UnBBayesModule loadNet(final File file, UnBBayesModule moduleToUse) throws IOException {
		
//		screen.setCursor(new Cursor(Cursor.WAIT_CURSOR));   
		UnBBayesModule mod = null;
		try {
			mod = moduleToUse.openFile(file);
		} finally {
//			screen.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}
		
		return mod;
		
//		try {
//			javax.swing.JInternalFrame window = null;
//			BaseIO io = null;
//			if (file.isDirectory()) { //MSBN
//				io = DefaultMSBNIO.newInstance();
//				SingleAgentMSBN msbn = (SingleAgentMSBN)io.load(file);	
//				MSBNController controller = new MSBNController(msbn);
//				window = controller.getPanel();
//			} else {
//				String name = file.getName().toLowerCase();				
//				
//				if (name.endsWith("net")) {
//					io = new NetIO();	
//					ProbabilisticNetwork net = (ProbabilisticNetwork)io.load(file);
//					ConfigurationsController.getInstance().addFileToListRecentFiles(file); 
//					window = new NetworkWindow(net);	
//					((NetworkWindow)window).setFileName(name); 
//				} else if (name.endsWith("xml")){
//					io = new XMLBIFIO();	
//					ProbabilisticNetwork net = (ProbabilisticNetwork)io.load(file);
//					ConfigurationsController.getInstance().addFileToListRecentFiles(file); 
//					window = new NetworkWindow(net);	
//					((NetworkWindow)window).setFileName(name); 
//				} else if (name.endsWith("dne")){
//					io = new DneIO();	
//					ProbabilisticNetwork net = (ProbabilisticNetwork)io.load(file);
//					ConfigurationsController.getInstance().addFileToListRecentFiles(file); 
//					window = new NetworkWindow(net);	
//					((NetworkWindow)window).setFileName(name); 
//				} else if (name.endsWith("owl")){
//
////					JProgressBar jpb = new JProgressBar();
////					jpb.setIndeterminate(true); 
////					JDialog jDialog = new JDialog(); 
////					JPanel jpanel = new JPanel();
////					jpanel.setLayout(new BorderLayout()); 
////					jpanel.add(jpb, BorderLayout.CENTER); 
////					jDialog.setContentPane(jpanel); 
////					jDialog.pack(); 
////					jDialog.setVisible(true); 
////					jpb.paintImmediately(0, 0, jpb.getWidth(), jpb.getHeight()); 
////					jDialog.repaint(0, 0, jDialog.getWidth(), jDialog.getHeight()); 
////					jDialog.setLocationRelativeTo(UnBBayesFrame.getIUnBBayes()); 
////					
////					MultiEntityBayesianNetwork mebn; 
////					
////					Runnable runnable = new Runnable(){
////
////						public void run() {
////							PrOwlIO prOwlIo = new PrOwlIO(); 
////							try {
////								MultiEntityBayesianNetwork mebn = prOwlIo.loadMebn(file);
////								JOptionPane.showMessageDialog(screen, resource.getString("JAXBExceptionFound"), resource.getString("loadNetException"), JOptionPane.ERROR_MESSAGE);
////							} catch (IOException e) {
////								// TODO Auto-generated catch block
////								e.printStackTrace();
////							} catch (IOMebnException e) {
////								// TODO Auto-generated catch block
////								e.printStackTrace();
////							}
////						}
////						
////					}; 
////					
////					Thread thread = new Thread(runnable); 
////					thread.start();
////					
////					Thread.sleep(10000); 
//					
//					MultiEntityBayesianNetwork mebn; 
//					PrOwlIO prOwlIo = new PrOwlIO(); 
//					mebn = prOwlIo.loadMebn(file);
//					ConfigurationsController.getInstance().addFileToListRecentFiles(file); 
//					window = new NetworkWindow(mebn);
//					((NetworkWindow)window).setFileName(name); 
//				
//				}  else if (name.endsWith(UbfIO.FILE_EXTENSION)) {        			
//					MebnIO ubfIo = UbfIO.getInstance(); 
//					MultiEntityBayesianNetwork mebn = ubfIo.loadMebn(file);
//					ConfigurationsController.getInstance().addFileToListRecentFiles(file); 
//					window = new NetworkWindow(mebn);	
//					((NetworkWindow)window).setFileName(name); 
//				    
//				} else if (name.endsWith(IObjectOrientedBayesianNetworkIO.fileExtension)) {
//					IObjectOrientedBayesianNetworkIO oobnIO = DefaultOOBNIO.newInstance(ObjectOrientedBayesianNetwork.newInstance(""));
//					IObjectOrientedBayesianNetwork oobn = oobnIO.loadOOBN(file);	
//					ConfigurationsController.getInstance().addFileToListRecentFiles(file); 
//					OOBNController controller = OOBNController.newInstance(oobn);
//					window = controller.getPanel();
//				}
//			}
//			screen.addWindow(window);
//		} finally {
//			screen.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
//		}
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

