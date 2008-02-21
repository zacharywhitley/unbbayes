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
package unbbayes.example;

import java.io.File;

import unbbayes.io.BaseIO;
import unbbayes.io.NetIO;
import java.util.ResourceBundle;

import unbbayes.prs.Edge;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.ProbabilisticNode;

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
public class TextMode {
	
	/** Load resource file from this package */
  	private static ResourceBundle resource = ResourceBundle.getBundle("unbbayes.example.resources.ExampleResources");

	public static void main(String[] args) throws Exception {

		ProbabilisticNetwork rede = null;

		try {
			BaseIO io = new NetIO();
			rede = io.load(new File("./examples/asia.net"));
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}

		ProbabilisticNode auxVP = new ProbabilisticNode();
		auxVP.setName(resource.getString("nodeName1"));
		auxVP.setDescription(resource.getString("nodeDescription"));
		auxVP.appendState(resource.getString("stateName0"));
		auxVP.appendState(resource.getString("stateName1"));
		PotentialTable auxTabPot = auxVP.getPotentialTable();
		auxTabPot.addVariable(auxVP);
		auxTabPot.addValueAt(0, 0.99f);
		auxTabPot.addValueAt(1, 0.01f);
		rede.addNode(auxVP);

		ProbabilisticNode auxVP2 = (ProbabilisticNode) rede.getNode(resource.getString("nodeName2"));
		Edge auxArco = new Edge(auxVP, auxVP2);
		rede.addEdge(auxArco);

		rede.compile();

		float likelihood[] = new float[auxVP.getStatesSize()];
		likelihood[0] = 1;
		likelihood[1] = 0.8f;

		auxVP.addLikeliHood(likelihood);

		try {
        	rede.updateEvidences();
        } catch (Exception exc) {
        	System.out.println(exc.getMessage());               	
        }
	}
}