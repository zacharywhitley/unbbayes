package unbbayes.aprendizagem;

import java.util.*;
import unbbayes.controlador.MainController;
import unbbayes.jprs.jbn.ProbabilisticNetwork;

/**
 * Classe que implementa a métrica GHS para o algoritmo K2.
 * @author Danilo Custodio da Silva
 * @version 1.0
 */
public class TAlgoritmoK2_GHS extends TAlgoritmoK2{

    private double[]  tabelaStirling = new double[50];

    /**
     * @see TAlgoritmoK2
     * @see TAprendizagemTollKit
     */

    TAlgoritmoK2_GHS(List variaveis, byte[][] BaseDados, int numeroCasos, int vetor[], int delta, ProbabilisticNetwork net){
        CalculaAlgoritmoK2(variaveis,BaseDados,numeroCasos,vetor, delta, net);
    }
	/*
	TAlgoritmoK2_GHS(List variaveis, byte[][] BaseDados, int numeroCasos, int vetor[], int delta, MainController controller){
        CalculaAlgoritmoK2(variaveis,BaseDados,numeroCasos,vetor, delta, controller);
    }
	*/

    /**
     * @see TAprendizagemTollKit
     */
    public double g(TVariavel variavel, List pais){/*
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