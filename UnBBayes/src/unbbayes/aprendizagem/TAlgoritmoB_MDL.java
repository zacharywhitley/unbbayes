package unbbayes.aprendizagem;

import java.util.*;
import unbbayes.controlador.MainController;
import unbbayes.jprs.jbn.ProbabilisticNetwork;
import unbbayes.util.NodeList;


/**
 * Classe que implementa métrica MDL no algoritmo B.
 * @author Danilo Custodio da Silva
 * @version 1.0
 */
public class TAlgoritmoB_MDL extends TAlgoritmoB{

    /**
     * @see TAlgoritmoB
     * @see TAprendizagemTollKit
     */

    TAlgoritmoB_MDL(NodeList variaveis, byte[][] BaseDados, int numeroCasos, int vetor[], ProbabilisticNetwork net){
        calculaAlgoritmoB(variaveis,BaseDados,numeroCasos,vetor,net);
    }
	/*
	TAlgoritmoB_MDL(List variaveis, byte[][] BaseDados, int numeroCasos, int vetor[], MainController controller){
        calculaAlgoritmoB(variaveis,BaseDados,numeroCasos,vetor, controller);
    }
	*/

    /**
     * @see TAprendizagemTollKit
     */
    public double g(TVariavel variavel, NodeList pais){
        /*
        double somatorioRi = 0;
        double somatorioQi = 0;
        int  nij = 0;
        int nijk = 0;
        int qi = 1;
        int ri = variavel.getEstadoTamanho();
        Tnij nijAux;
        List vetorFrequencias = calculaFrequencias(variavel,pais);
        List vetorNij = (List)vetorFrequencias.get(0);
        List vetorNijk = (List)vetorFrequencias.get(1);
        if (pais != null){
           qi = calculaQi(pais);
        }
        for (int j = 1 ; j <= qi ; j++ ){
            if (j <= vetorNij.size()){
                nijAux = (Tnij)vetorNij.get(j-1);
                nij = nijAux.getRepeticoes();
            }
            for (int k = 1; k <= ri  ; k++ ){
                nijk = calculaT(vetorNijk, vetorNij, j, k);
                if (pais == null ){
                    nij = numeroCaso;
                }
                if (nij != 0 && nijk != 0){
                    somatorioRi = somatorioRi + (nijk*(log(nijk)-log(nij)));
                }
            }
            somatorioQi = somatorioQi + somatorioRi;
            nij = 0;
            somatorioRi = 0;
        }
        return somatorioQi - 0.5*qi*(ri -1)*log(numeroCaso);
        */
        return 0;
    }
};