package unbbayes.aprendizagem;

import java.util.*;
import unbbayes.util.SetToolkit;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:
 * @author
 * @version 1.0
 */

public abstract class TAprendizagemTollKit {

    public static boolean compactado;
    protected byte[][] BaseDados;
    protected int[] vetor;
    protected int numeroCaso;
    protected List vetorVariaveis;

    /**
     * Fun�ao de qualidade : Comparando essa fun��o que � possivel decidir
     * se uma determinada vari�vel � ou n�o pai de um certo n�.
     * A fun�ao de qualidade possui tres implementa��es no pacote de aprendi
     * zagem do UnbBayes: GH, GHS, MDL
     *
     *@param vari�vel (<code>TVariavel<code>)
     *@param pais lista de pais da vari�vel(<code>List<code>)
     *@return double - Resultado da aplica��o da fun��o de qualidade
     *@see TVariavel
     */
    public abstract double g(TVariavel variavel, List pais);


    /**
     *  Esse m�todo calcula as probabilidades dos n�s da rede
     *  gerada pelo algortmo de aprendizagem.
     *
     * @param vetorNijk vetor que contem todas as configura��es de pais em
     * conjunto com a vari�vel (<code>List<code>)
     * @param vetorNij vetor que contem todas as configura�oes de pais
     * (<code>List<code>)
     * @param variavel Vari�vel na qual a probabilida sera inserida
     * (<code>TVariavel<code>)
     * @see TVariavel
     * @see Tnij
     */
    protected void calculaProbabilidade(int[][] vetorNijk, TVariavel variavel){
        List vetorInstancias;
        List instancia = new ArrayList();
        double probabilidade;
        int nij;
        int ri = variavel.getEstadoTamanho();
        int tamanhoNij = calculaQi(variavel.getPais());
        vetorInstancias = montaInstancias(variavel.getPais());
        for(int i = 0; i < tamanhoNij;i++){
             nij = 0;
             for(int j = 0; j < ri ; j++){
                nij+= vetorNijk[j][i];
             }
             if(vetorInstancias.size() > 0 ){
                instancia = (List)vetorInstancias.get(i);
             }
             for(int j = 0; j < ri; j++){
                  probabilidade = (double)(1+ vetorNijk[j][i])/(ri+nij);
                  int coord[];
                  coord = new int[tamanhoNij+1];
                  coord[0] = j;
                  for(int k = 1; k <= instancia.size(); k++){
                      coord[k] = ((Integer)instancia.get(k-1)).intValue();
                  }
                  variavel.getProbabilidades().setValue(coord, probabilidade);
             }
        }
    }

    /**
     * M�todo calcula os vetores Nij e Nijk varrendo uma s� vez a base de
     * dados. O vetorNij possui todas as configura��es poss�veis de pais
     * junto aos estados das vari�veis, j� o vetorNij possui todas as con
     * figura��es poss�veis dos pais.
     *
     * @param variavel
     */
    protected  int[][] calculaFrequencias(TVariavel variavel, List pais){
        TVariavel variavelAux;
        int[][] ArrayNijk;
        int tamanhoPais, posicao;
        if(pais == null){
           pais = new ArrayList();
        }
        tamanhoPais = (short)pais.size();
        if(tamanhoPais == 0){
            ArrayNijk = new int[variavel.getEstadoTamanho()][1];
        }else{
            ArrayNijk = new int[variavel.getEstadoTamanho()][calculaQi(pais)];
        }
        short vetorPosicao[] = new short[tamanhoPais];
        short vetorMaximo[] = new short[tamanhoPais];
        for (int i = 0; i < tamanhoPais; i++ ){
            variavelAux = (TVariavel)pais.get(i);
            vetorPosicao[i] = (short)variavelAux.getPos();
            vetorMaximo[i] = (short)variavelAux.getEstadoTamanho();

        }
        int tamanhoPosicao = vetorPosicao.length;
        List vetorEstadosNij = new ArrayList();
        int pos = variavel.getPos();
        int indice =0;
        for (int i = 0 ; i < numeroCaso ; i++){
            for (int j = tamanhoPosicao-1; j >=0; j-- ){
                posicao = vetorPosicao[j];
                if(j != tamanhoPosicao -1){
                    indice += BaseDados[i][posicao]*vetorMaximo[j+1];
                    if(i == 0){
                        vetorMaximo[j] *= vetorMaximo[j+1];
                    }
                }else{
                    indice = BaseDados[i][posicao];
                }
            }
            /*
            for(int g = 0; g < instanciaPais.size(); g++){
                instancia = (List)instanciaPais.get(g);
                if(vetorEstadosNij.equals(instancia)){
                    if(! TAprendizagemTollKit.compactado){
                         ArrayNijk[BaseDados[i][pos]][g]++;
                    }else{
                         ArrayNijk[BaseDados[i][pos]][g] += vetor[i];
                    }
                    break;
                }
            }
            */
            if(tamanhoPais == 0){
                if(! TAprendizagemTollKit.compactado){
                         ArrayNijk[BaseDados[i][variavel.getPos()]][0]++;
                    }else{
                         ArrayNijk[BaseDados[i][variavel.getPos()]][0] += vetor[i];
                    }
            }else{
                  if(! TAprendizagemTollKit.compactado){
                       ArrayNijk[BaseDados[i][pos]][indice]++;
                  }else{
                       ArrayNijk[BaseDados[i][pos]][indice] += vetor[i];
                  }
            }
            vetorEstadosNij.clear();
        }
        return ArrayNijk;
    }

    protected List montaInstancias(List pais){
        List instancias = new ArrayList();
        List aux = new ArrayList();;
        TVariavel variavelAux;
        List array;
        List arrayAux;
        if(pais.size() == 0){
            return instancias;
        }
        for(int i = 0; i < pais.size(); i++){
            variavelAux = (TVariavel)pais.get(i);
            for(int k = 0 ; k < instancias.size(); k++){
                     array = (List)instancias.get(k);
                for(int h = 0 ; h < variavelAux.getEstadoTamanho(); h++){
                     if(h == 0){
                        array.add(new Integer(h));
                        aux.add(array);
                     }else{
                        arrayAux = SetToolkit.clone((List)array);
                        arrayAux.remove(array.size()-1);
                        arrayAux.add(new Integer(h));
                        aux.add(arrayAux);
                     }
                }
            }
            instancias.clear();
            instancias = SetToolkit.clone((List)aux);
            aux.clear();
            if(instancias.size() == 0){
                 for(int j = 0 ; j < variavelAux.getEstadoTamanho(); j++){
                         array = new ArrayList();
                         array.add(new Integer(j));
                         instancias.add(array);
                 }

            }
        }
        return instancias;
    }

   /**
    * M�todo que calcula o n�mero poss�vel de permuta��es
    * entre os pais de um determinada variavel.
    * @param pais - pais da vari�vel(<code>ArrayLis<code>)
    * @return int - N�mero de permuta��es
    * @see TVariavel
    */
    protected int calculaQi(List pais){
        TVariavel variavel;
        int qi = 1;
        if(pais != null){
            int tamanho  = pais.size();
            for (int numeroPais = 0; numeroPais < tamanho ; numeroPais++ ){
                variavel  = (TVariavel)pais.get(numeroPais);
                qi = qi* variavel.getEstadoTamanho();
            }
        }
        return qi;
    }

    /**
     * M�todo que retorna os objetos diferentes entre um vetor e outro,
     * ou seja, os objetos que estiverem no primeiro vetor e n�o
     * estiverem no segundos devem ser retornados em um vetor.
     * @param predecessores - Vetor de predecessore (<code>List<code>)
     * @param pais - Vetor de pais(<code>List<code>)
     * @return List - Vetor da diferen�a entre predecesores e pais
     * @see TVariavel
     */
     protected List diferenca(List predecessores, List pais){
        TVariavel variavelAux;
        TVariavel variavelAux2;
        for (int i = 0 ;  i < predecessores.size(); i++ ){
            variavelAux = (TVariavel)predecessores.get(i);
            for (int j = 0 ; j < pais.size() ; j++ ){
                variavelAux2 = (TVariavel)pais.get(j);
                if (variavelAux2.getName().equals(variavelAux.getName())){
                    predecessores.remove(i);
                }
            }
        }
        return predecessores;
    }

    /** M�todo que concatena uma vari�vel a um vetor.
     *  @param pais - Vetor de pais de uma vari�vel(<code>
     *  List<code>)
     *  @param z - Vari�vel a ser concatenada no veto
     *  de pais(<code>TVariavel<code>)
     *  @return List - Vetor com a vari�vel concatenada
     *  @see TVariavel
     */
    protected List concatena(List pais, TVariavel z){
        if (pais == null){
            pais = new ArrayList();
        }
        pais.add(z);
        return pais;
    }

   /**
    * M�todo para o calculo do log na bese 10
    * @param double - O N�mero para o calculo do log (<code>
    * double<code>)
    * @return double - O Log do n�mero
    */
    protected double log(double numero){
        return Math.log(numero)/Math.log(10);
    }

    /**
     * M�todo para montar os predecessores de todos os n�s. Prede
     * cessores de uma vari�vel s�o vari�veis que tem um potencial
     * para ser um dos pais desta.
     * @param vetor - Lista de variaveis (<code>List<code>)
     * @see TVariavel
     */
    protected void montaEstruturaPredecessores(List vetor){
        TVariavel variavelAux;
        int tamanho = vetor.size();
        for(int i = tamanho - 1; i > 0  ; i--){
            variavelAux = (TVariavel)vetor.get(i);
                for (int j = i-1; j > -1 ; j--){
                    variavelAux.adicionaPredecessor((TVariavel)vetor.get(j));
                }
        }
    }
    public void setPosicao(int pos){
        TOrdenacao1.progress.setValue((100/(vetorVariaveis.size()))*(pos+1));
        TOrdenacao1.progress.setString(""+(100/(vetorVariaveis.size()))*(pos+1)+"%");
    }

    public void setLabel(String label){
       TOrdenacao1.label.setText("Variavel : "+label);
    }
}