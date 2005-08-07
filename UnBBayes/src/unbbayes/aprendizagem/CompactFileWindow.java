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
 */package unbbayes.aprendizagem;

import java.awt.*;

import javax.swing.*;
import unbbayes.util.NodeList;

import java.awt.event.*;

/**
 * Classe que constroi uma tela onde usuário devera
 * informar ao programa se o arquivo é o do tipo
 * compactado ou não.
 * @author Danilo Custodio da Silva
 * @version 1.0
 */
public class CompactFileWindow extends JDialog
{
	/** Serialization runtime version number */
	private static final long serialVersionUID = 0;

	private NodeList variablesVector;
    private JPanel centerPanel;
    private JButton yes;
    private JButton no;
    private CompactInterationController compactController;
    /**
     * Constrói a tela essa tela possui um label
     * que pergunta ao usuário se o arquivo que ele
     * está tratando é compactado ou não, e dois botões.
     * @param vetorVariaveis - As variáveis presentes na
     * (<code>java.util.List<code>)
     * @see TVariavel
     * @see Container
     * @see JDialog
     */
    CompactFileWindow(NodeList variablesVector){
        super(new Frame(), "UnBBayes - Learning Module", true);
        Container container = getContentPane();
        this.variablesVector = variablesVector;
        container.add(new JLabel("Do you want to use compacted file? "),BorderLayout.NORTH);
        container.add(getCenterPanel(),BorderLayout.CENTER);
        compactController = new CompactInterationController(this);
        setResizable(false);
        pack();
        setVisible(true);        
    }

    private JPanel getCenterPanel(){
        centerPanel         = new JPanel(new GridLayout(1,2,5,5));
        yes                 = new JButton("Yes");
        no                  = new JButton("No");
        centerPanel.add(yes);
        centerPanel.add(no);
        yes.addActionListener(ActionYes);
        no.addActionListener(ActionNo);
        return centerPanel;
    }
    
    ActionListener ActionYes = new ActionListener(){
        public void actionPerformed(ActionEvent ae){            
            compactController.actionYes(variablesVector);
        }
    };

    ActionListener ActionNo = new ActionListener(){
        public void actionPerformed(ActionEvent ae){
        	compactController.actionNo();
        }
    };
}