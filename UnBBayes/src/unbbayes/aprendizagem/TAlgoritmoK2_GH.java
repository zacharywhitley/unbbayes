package unbbayes.aprendizagem;

import java.util.*;
import unbbayes.controlador.MainController;
import unbbayes.jprs.jbn.ProbabilisticNetwork;

/**
 * Classe que implementa a métrica GH para o algoritmo K2.
 * @author Danilo Custodio da Silva
 * @version 1.0
 */
public class TAlgoritmoK2_GH extends TAlgoritmoK2{
    /**
     * @see TAlgoritmoK2
     * @see TAprendizagemTollKit
     */

    TAlgoritmoK2_GH(List variaveis, byte[][] BaseDados, int numeroCasos, int vetor[], int delta, ProbabilisticNetwork net){
        CalculaAlgoritmoK2(variaveis,BaseDados,numeroCasos,vetor, delta, net);
    }
	/*
	TAlgoritmoK2_GH(List variaveis, byte[][] BaseDados, int numeroCasos, int vetor[], int delta, MainController controller){
        CalculaAlgoritmoK2(variaveis,BaseDados,numeroCasos,vetor, delta, controller);
    }
	*/

    /**
     * @see TAprendizagemTollKit
     */
    public double g(TVariavel variavel , List pais){
        /*
        double somatorioR;
        double somatorioS;
        double somatorioT;
        double somatorioQi = 0;
        int R, S, T;
        int qi = 1;
        int ri  = variavel.getEstadoTamanho();
        List vetorFrequencias = calculaFrequencias(variavel,pais);
        List vetorNij = (List)vetorFrequencias.get(0);
        List vetorNijk = (List)vetorFrequencias.get(1);
        if (pais != null){
            qi = calculaQi(pais);
        }
        for (int j = 1 ; j <= qi ; j++ ){
            R = ri - 1;
            somatorioR = 0;
            for (int r = 1 ; r <= R  ; r++ ){
                somatorioR = somatorioR + log(r);
            }
            if(pais == null){
                S = numeroCaso + R;
            } else{
                if (j < vetorNij.size() +1){
                    Tnij nij = (Tnij)vetorNij.get(j - 1);
                    S =  nij.getRepeticoes() + R;
                } else{
                    S = R;
                }
            }
            somatorioS = 0;
            for (int s = 1; s <= S ; s++ ){
                somatorioS = somatorioS + log(s);
            }
            ri = R + 1;
            somatorioT = 0;
            for (int k = 1 ; k <= ri ; k++){
                T = calculaT(vetorNijk,vetorNij, j, k);
                for (int t = 1 ; t <= T ; t++ ){
                    somatorioT = somatorioT + log(t);
                }
            }
            somatorioQi = somatorioQi +(somatorioR - somatorioS + somatorioT);
        }
        return somatorioQi;
        */
        return 0;
    }

}
