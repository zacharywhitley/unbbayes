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

package unbbayes.controlador;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

import javax.swing.*;

import unbbayes.fronteira.*;
import unbbayes.io.*;
import unbbayes.jprs.jbn.*;
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
    private BaseIO io;

    private List copia;
    private List copiados;
    private boolean bColou;

	/** Load resource file from this package */
  	private static ResourceBundle resource = ResourceBundle.getBundle("unbbayes.controlador.resources.ControllerResources");

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
        io = new NetIO();
    }

    public void newNet() {
        ProbabilisticNetwork net = new ProbabilisticNetwork();
        //screen.addWindow(new NetWindow(net));
		NetWindow netWindow = new NetWindow(net);
		JInternalFrame jif = new JInternalFrame(net.getName(), true, true, true, true);
		jif.getContentPane().add(netWindow);
		jif.setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
		screen.addWindow(jif);
    }

    /**
     *  Salva a rede Bayesiana desenhada no com o nome de arquivo desejado.
     *
     * @param  arquivo  nome do aqruivo que representa a rede a ser salvada.
     * @see             String
     */
    public void saveNet(File arquivo) {
        screen.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        io.save(arquivo, screen.getSelectedWindow().getRede());
        screen.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
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
            ProbabilisticNetwork net = io.load(file);
            //screen.addWindow(new NetWindow(net));
			NetWindow netWindow = new NetWindow(net);
			JInternalFrame jif = new JInternalFrame(net.getName(), true, true, true, true);
			jif.getContentPane().add(netWindow);
			jif.setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
			screen.addWindow(jif);
        } catch (Exception e){
            JOptionPane.showMessageDialog(screen, e.getMessage(), resource.getString("loadNetException"), JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            screen.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            return;
        }
        screen.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }


    /**
     * Método utilizado pelo módulo de aprendizagem, que monta e mostra a rede
     * em uma nova janela.
     *
     * @param variaveis lista das variáveis.
     */
    public ProbabilisticNetwork makeNetwork(NodeList variaveis) {
        screen.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        ProbabilisticNetwork net = new ProbabilisticNetwork();
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
                net.getArcos().add(arcoAux);
            }
        }
        //screen.addWindow(new NetWindow(net));
		NetWindow netWindow = new NetWindow(net);
		JInternalFrame jif = new JInternalFrame(net.getName(), true, true, true, true);
		jif.getContentPane().add(netWindow);
		jif.setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
		screen.addWindow(jif);
        screen.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

		return net;
    }

}

