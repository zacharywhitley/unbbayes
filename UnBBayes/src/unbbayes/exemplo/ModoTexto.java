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

package unbbayes.exemplo;

import unbbayes.jprs.jbn.*;
import unbbayes.io.*;
import java.io.*;

/**
 * Title: Exemplo de Uso da API atrav�s de um Modo de Texto
 * Description: Essa classe feita em JAVA abre um arquivo ".net", "asia.net". Depois esse arquivo �
 *              carregado, modificado em algumas partes e ent�o compilado. Essa classe tem a fun��o
 *              de apenas exemplificar como se pode usar a API desenvolvida para trabalhar com
 *              Redes Bayesianas.
 * Copyright:   Copyright (c) 2001
 * Company:     UnB - Universidade de Bras�lia
 * @author      Rommel Novaes Carvalho
 * @author      Michael S. Onishi
 */
public class ModoTexto {

    public static void main(String[] args) {

        ProbabilisticNetwork rede = null;

        try {
            BaseIO io = new NetIO();
            rede = io.load(new File("./exemplos/asia.net"));
        } catch (Exception e) {
           e.printStackTrace();
           System.exit(1);
        }

        ProbabilisticNode auxVP = new ProbabilisticNode();
        auxVP.setName("K");
        auxVP.setDescription("Vari�vel de Teste");
        auxVP.appendState("Estado 0");
        auxVP.appendState("Estado 1");
        PotentialTable auxTabPot = auxVP.getPotentialTable();
        auxTabPot.addVariable(auxVP);
        auxTabPot.addValueAt(0, 0.99);
        auxTabPot.addValueAt(1, 0.01);
        rede.addNode(auxVP);

        ProbabilisticNode auxVP2 = (ProbabilisticNode)rede.getNode("A");
        Edge auxArco = new Edge(auxVP, auxVP2);
        rede.addEdge(auxArco);

        try {
            rede.compile();
        } catch (Exception e) {
           System.out.println(e.getMessage());
           System.exit(1);
        }


        double likelihood[] = new double[auxVP.getStatesSize()];
        likelihood[0] = 1.0;
        likelihood[1] = 0.8;

        auxVP.addLikeliHood(likelihood);

		try {
	        rede.updateEvidences();
		} catch (Exception e) {
			e.printStackTrace();			
		}
    }
}