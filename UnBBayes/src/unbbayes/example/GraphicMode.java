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

package unbbayes.example;

import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ResourceBundle;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import unbbayes.io.BaseIO;
import unbbayes.io.NetIO;
import unbbayes.prs.bn.Edge;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.ProbabilisticNode;

/**
 * Title: Exemplo de Uso da API através de um Modo Gráfico
 * Description: Essa classe feita em JAVA abre um modo gráfico onde o usuário entra com o endereço e
 *              nome de um arquivo ".net". Depois esse arquivo é carregado, modificado em algumas
 *              partes e então compilado. Essa classe tem a função de apenas exemplificar como se
 *              pode usar a API desenvolvida para trabalhar com Redes Bayesianas.
 * Copyright:   Copyright (c) 2001
 * Company:     UnB - Universidade de Brasília
 * @author      Rommel Novaes Carvalho
 * @author      Michael S. Onishi
 * @version 1.0
 */
public class GraphicMode {
	
	/** Load resource file from this package */
  	private static ResourceBundle resource = ResourceBundle.getBundle("unbbayes.example.resources.ExampleResources");
	
    public static void main(String[] args) {
        JFrame frame = new JFrame(resource.getString("exampleTitle"));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Container pane = frame.getContentPane();
        pane.setLayout(new FlowLayout());

        JLabel labelArquivo = new JLabel(resource.getString("fileName"));
        final JTextField nomeArquivo = new JTextField(20);

        JButton botao = new JButton(resource.getString("compileTree"));
        pane.add(labelArquivo);
        pane.add(nomeArquivo);
        pane.add(botao);

        botao.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ProbabilisticNetwork rede = null;
                try {
                    BaseIO io = new NetIO();
                    rede = io.load(new File(nomeArquivo.getText()));
                } catch (Exception ex) {
                    ex.printStackTrace();
                    System.exit(1);
                }

                ProbabilisticNode auxVP = new ProbabilisticNode();
                auxVP.setName(resource.getString("nodeName1"));
                auxVP.setDescription(resource.getString("nodeDescription"));
                auxVP.appendState(resource.getString("stateName0"));
                auxVP.appendState(resource.getString("stateName1"));
                PotentialTable auxTabPot = auxVP.getPotentialTable();

                auxTabPot.addVariable(auxVP);
                auxTabPot.addValueAt(0, 0.30);
                auxTabPot.addValueAt(1, 0.70);
                rede.addNode(auxVP);

                ProbabilisticNode auxVP2 = (ProbabilisticNode)rede.getNode("A");
                Edge auxArco = new Edge(auxVP, auxVP2);
                rede.addEdge(auxArco);

                try {
                   rede.compile();
                } catch (Exception ex) {
                   System.out.println(ex.getMessage());
                   System.exit(1);
                }

                double likelihood[] = new double[auxVP.getStatesSize()];
                likelihood[0] = 1.0;
                likelihood[1] = 0.8;

                auxVP.addLikeliHood(likelihood);
                try {
                	rede.updateEvidences();
                } catch (Exception exc) {
                	System.out.println(exc.getMessage());               	
                }
            }
        });

        frame.pack();
        frame.setVisible(true);
    }
}