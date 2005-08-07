package unbbayes.monteCarlo;

import unbbayes.monteCarlo.controlador.ControladorPrincipal;

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
		new ControladorPrincipal();		
	}
}
