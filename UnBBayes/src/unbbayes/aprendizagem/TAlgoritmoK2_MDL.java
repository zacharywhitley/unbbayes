package unbbayes.aprendizagem;

import java.util.*;
import unbbayes.controlador.MainController;
import unbbayes.jprs.jbn.ProbabilisticNetwork;

/**
 * Classe que implementa métrica MDL no algoritmo K2.
 * @author Danilo Custodio da Silva
 * @version 1.0
 */
public class TAlgoritmoK2_MDL extends TAlgoritmoK2 {

    /**
     * @see TAlgoritmoK2
     * @see TAprendizagemTollKit
     */

    TAlgoritmoK2_MDL(List variaveis, byte[][] BaseDados, int numeroCasos, int vetor[], int delta, ProbabilisticNetwork net){
        CalculaAlgoritmoK2(variaveis,BaseDados,numeroCasos,vetor, delta, net);
    }
	/*
	TAlgoritmoK2_MDL(List variaveis, byte[][] BaseDados, int numeroCasos, int vetor[], int delta, MainController controller){
        CalculaAlgoritmoK2(variaveis,BaseDados,numeroCasos,vetor, delta, controller);
    }
	*/

    /**
     * @see TAprendizagemTollKit
     */
    public double g(TVariavel variavel, List pais){

        double somatorioRi = 0;
        double somatorioQi = 0;
        int  nij = 0;
        int nijk = 0;
        int ri = variavel.getEstadoTamanho();
        int qi = 1;
        int ArrayNijk[][] = calculaFrequencias(variavel,pais);
        if (pais != null){
           qi = calculaQi(pais);
        }
        for (int j = 0 ; j < qi ; j++ ){
            for(int k = 0 ; k < ri ; k++){
               nij+= ArrayNijk[k][j];
            }
            for (int k = 0; k < ri  ; k++ ){
                nijk = ArrayNijk[k][j];
                if(nij != 0 && nijk != 0){
                    somatorioRi = somatorioRi + (nijk*(log(nijk)-log(nij)));
                }
            }
            somatorioQi = somatorioQi + somatorioRi;
            nij = 0;
            somatorioRi = 0;
        }
        somatorioQi -= 0.5*qi*(ri -1)*log(numeroCaso);
        return somatorioQi;
    }
}