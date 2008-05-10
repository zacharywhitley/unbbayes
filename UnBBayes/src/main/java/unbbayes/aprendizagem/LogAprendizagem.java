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
package unbbayes.aprendizagem;

import java.awt.Container;
import java.awt.Frame;
import java.util.ArrayList;

import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import unbbayes.prs.Node;
import unbbayes.prs.bn.LearningNode;



public class LogAprendizagem extends JDialog{

	/** Serialization runtime version number */
	private static final long serialVersionUID = 0;		
	
    private JTextArea caixaLog;
    private JScrollPane painel;


    public LogAprendizagem(java.util.List variaveis) {
        super(new Frame(),"Log",true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        Container container = getContentPane();
        caixaLog = new JTextArea();
        int tamanho = variaveis.size();
        caixaLog.setText("Lista com os pais de cada variável\n\n");
        for(int i = 0 ; i < tamanho; i++){
                LearningNode variavel = (LearningNode)variaveis.get(i);
                caixaLog.append("Variável : "+variavel.getName()+"\n");
                ArrayList<Node> pais = variavel.getPais();
                for(int j = 0 ; j < pais.size(); j++){
                    LearningNode variavel2 = (LearningNode)pais.get(j);
                    int k = j+1;
                    caixaLog.append("\tPai "+k+" : "+variavel2.getName()+"\n");
                }
                if(pais.size() == 0 ){
                    caixaLog.append("\tNão possui pais\n");
                }
        }
        painel = new JScrollPane(caixaLog);
        container.add(painel);
        setResizable(true);
        setBounds(100,100,500,500);
        setVisible(true);
    }
}