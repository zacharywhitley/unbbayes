package unbbayes.aprendizagem;

import java.util.*;
import unbbayes.controlador.MainController;
import unbbayes.jprs.jbn.ProbabilisticNetwork;
import unbbayes.util.NodeList;

/**
 * Classe que implementa métrica GHS no algoritmo B.
 * @author Danilo Custodio da Silva
 * @version 1.0
 */
public class TAlgoritmoB_GHS extends TAlgoritmoB{

    private double[]  tabelaStirling = new double[50];

    /**
     * @see TAlgoritmoB
     * @see TAprendizagemTollKit
     */

    TAlgoritmoB_GHS(NodeList variaveis, byte[][] BaseDados, int numeroCasos, int vetor[], ProbabilisticNetwork net){
        calculaAlgoritmoB(variaveis,BaseDados,numeroCasos,vetor,net);
    }
	/*
	TAlgoritmoB_GHS(List variaveis, byte[][] BaseDados, int numeroCasos, int vetor[], MainController controller){
        calculaAlgoritmoB(variaveis,BaseDados,numeroCasos,vetor, controller);
    }
	*/

    /**
     * @see TAprendizagemTollKit
     */
    public double g(TVariavel variavel, NodeList pais){
        /*
        montarTabelaStirling();
        double somatorioRi = 0;
        double somatorioQi = 0;
        double resposta = 0 ;
        int nij = 0;
        int nijk =  0;
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
                    somatorioRi += (nijk+1/2)*log(nijk) - nijk*log(Math.E);
                    //somatorioRi = somatorioRi + (nijk*(log(nijk)-log(nij)));
                }
            }
            somatorioQi +=  somatorioRi + nij*log(Math.E) - (ri+nij-(1/2))*log(ri+nij-1);
            //somatorioQi = somatorioQi + somatorioRi;
            nij = 0;
            somatorioRi = 0;
        }
        resposta = somatorioQi + ((qi*(ri-(1/2)))*log(ri - 1))+  ri/2*log((2*Math.PI));
        //return qi*(ri-(1/2))*log(ri - 1) + ((qi*ri)/2)*log(Math.PI*2) + somatorioQi;
        //return somatorioQi; //- 0.5*qi*(ri -1)*log(numeroCaso);
        return resposta;
        */
        return 0;
    }

    private void montarTabelaStirling()
    {
        for(int i = 0; i < 50; i++)
            tabelaStirling[i] = Math.sqrt(Math.PI*2)*Math.exp(-i)*Math.pow(i,(i+1/2));
    }
}