/*
 *  UnbBayes
 *  Copyright (C) 2002 Universidade de Brasília
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

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

import javax.swing.*;

import unbbayes.gui.*;
import unbbayes.io.*;
import unbbayes.prs.*;
import unbbayes.prs.bn.*;
import unbbayes.prs.msbn.SingleAgentMSBN;
import unbbayes.util.*;

/**
 *  Essa classe implementa o <code>KeyListener</code> e o <code>
 *  AdjustmentListener</code> para tratar eventos de tecla do <code>TDesenhaRede
 *  </code>e de ajuste do scroll do <code>jspDesenho</code> . Essa classe é
 *  responsável principalmente por fazer a ligação entre interface e lógica.
 *
 * @author     Rommel Novaes Carvalho
 * @author     Michael S. Onishi
 * @created    27 de Junho de 2001
 * @see        KeyListener
 * @see        AdjustmentListener
 * @version    1.0 24/06/2001
 */
public class MainController {

    private IUnBBayes screen;
    private List copia;
    private List copiados;
    private boolean bColou;

	/** Load resource file from this package */
  	private static ResourceBundle resource = ResourceBundle.getBundle("unbbayes.controller.resources.ControllerResources");

    /**
     *  Constrói o controlador responsável pela criação da rede Bayesiana ( <code>
     *  TRP</code> ) e da tela principal ( <code>IUnBBayes</code> ). Além
     *  disso, este construtor também adiciona AdjustmentListener para os
     *  JScrollBars do <code>jspDesenho</code> .
     *
     * @since
     * @see      KeyListener
     */
    public MainController() {
        screen = new IUnBBayes(this);
        copia = new ArrayList();
        copiados = new ArrayList();
    }

    public void newBN() {
        ProbabilisticNetwork net = new ProbabilisticNetwork("New BN");
        //screen.addWindow(new NetWindow(net));
		NetWindow netWindow = new NetWindow(net);
		screen.addWindow(netWindow);
    }
    
    public void newMSBN() {
    	SingleAgentMSBN msbn = new SingleAgentMSBN("New MSBN");
    	MSBNController controller = new MSBNController(msbn);
    	screen.addWindow(controller.getPanel());
    }

    /**
     *  Salva a rede Bayesiana desenhada no com o nome de arquivo desejado.
     *
     * @param  arquivo  nome do aqruivo que representa a rede a ser salvada.
     * @see             String
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
				io.save(file, ((NetWindow) window).getRede());
        	}
        } catch (IOException e) {
            JOptionPane.showMessageDialog(screen, e.getMessage(), "saveNetException", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } finally {
        	screen.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
    }


    /**
     *  Carrega a rede Bayesiana de um arquivo desejado.
     *
     * @param  arquivo  aqruivo que representa a rede a ser carregada.
     * @see             String
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
				window = new NetWindow(net);
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
     * Método utilizado pelo módulo de aprendizagem, que monta e mostra a rede
     * em uma nova janela.
     *
     * @param variaveis lista das variáveis.
     */
    public ProbabilisticNetwork makeNetwork(NodeList variaveis) {
        screen.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        ProbabilisticNetwork net = new ProbabilisticNetwork("learned net");
        Node noFilho = null;
        Node noPai = null;
        Edge arcoAux = null;
        Node aux;
        boolean direction = true;
        for (int i = 0; i < variaveis.size(); i++) {
            noFilho = variaveis.get(i);
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
    
    public void showNetwork(ProbabilisticNetwork net){
    	NetWindow netWindow = new NetWindow(net);
		if (! netWindow.getWindowController().compileNetwork()) {
            netWindow.changeToNetEdition();            
            
        } else{
            netWindow.changeToNetCompilation();		
		}		
		screen.addWindow(netWindow);
        screen.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));    	
    }
    
    public IUnBBayes getScreen(){
    	return screen;    	
    }

}

