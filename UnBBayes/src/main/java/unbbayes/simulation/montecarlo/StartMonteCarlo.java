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
package unbbayes.simulation.montecarlo;

import unbbayes.simulation.montecarlo.controller.MCMainController;

/**
 * 	Classe que que gera amostras baseadas em uma rede bayseana
 * 	� feito o carregamento de um arquivo contendo a descri��o de uma rede e de uma distribui��o de probabilidade
 *  Associada a ela
 *  S�o ent�o gerados um conjunto de casos baseado nesta rede e nessa distribui��o de probabilidade. Este conjunto de
 *  Casos representa a rede.
 *  Utiliza-se o algoritmo de montecarlo para gera��o das amostras. 
 * @author Danilo
 */
public class StartMonteCarlo {

	public static void main(String[] args){
		MCMainController cp = new MCMainController();		
	}
}
