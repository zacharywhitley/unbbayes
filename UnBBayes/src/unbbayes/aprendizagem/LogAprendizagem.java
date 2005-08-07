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
 package unbbayes.aprendizagem;

import javax.swing.*;
import unbbayes.util.NodeList;

import java.awt.*;



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
                TVariavel variavel = (TVariavel)variaveis.get(i);
                caixaLog.append("Variável : "+variavel.getName()+"\n");
                NodeList pais = variavel.getPais();
                for(int j = 0 ; j < pais.size(); j++){
                    TVariavel variavel2 = (TVariavel)pais.get(j);
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