package unbbayes.monteCarlo;

import unbbayes.monteCarlo.controlador.ControladorPrincipal;

/**
 * 	Classe que que gera amostras baseadas em uma rede bayseana
 * 	È feito o carregamento de um arquivo contendo a descrição de uma rede e de uma distribuição de probabilidade
 *  Associada a ela
 *  São então gerados um conjunto de casos baseado nesta rede e nessa distribuição de probabilidade. Este conjunto de
 *  Casos representa a rede.
 *  Utiliza-se o algoritmo de montecarlo para geração das amostras. 
 * @author Danilo
 */
public class StartMonteCarlo {

	public static void main(String[] args){
		new ControladorPrincipal();		
	}
}
