package unbbayes.aprendizagem;

import java.util.Date;
import java.util.List;

import unbbayes.fronteira.TJanelaEdicao;
import unbbayes.jprs.jbn.ProbabilisticNetwork;
import unbbayes.util.NodeList;
import unbbayes.util.SetToolkit;

/**
 *   Essa classe implementa os métodos necessários para que
 *   o algoritmo B funcione .O algoritmo k2 é um
 *   algoritmo de aprendizagem que utiliza a busca em pontuaçao. *
 *   @author     Danilo Custódio
 *   @version    1.0
 *   @see TAprendizagemTollKit
 */

public abstract class TAlgoritmoB extends TAprendizagemTollKit{

  protected double[][] arranjo;
  private boolean[] ancestrais;
  private boolean[] descendentes;
  protected TJanelaEdicao janela;

    /**
    * Método que representa a funçao principal do algoritmo B.
    * Esse método recebe um lista de variáveis, uma matriz com
    * os dados do arquivo, uma matriz arranjo que possui a pontu
    * acao de cada para de elemento e a partir disso monta a rede
    * bayseana correspondente aquele arquivo.
    *
    * @param variaveis Lista de variáveis(<code>List</code>)
    * @param BaseDados Representaçào do arquivo em memória(<code>byte[][]<code>)
    * @param vetor     Vetor que indica quantas vezes uma linha do arquivo se repete
    * (<code>int[]<code>)
    * @see TVariavel
    * @see TJanelaEdicao
    * @see Tnij
    * @see TAprendizagemTollKit
    */
  public void calculaAlgoritmoB(NodeList variaveis, byte[][] BaseDados, int numeroCasos, int vetor[], ProbabilisticNetwork net) {
  //public void calculaAlgoritmoB(List variaveis, byte[][] BaseDados, int numeroCasos, int vetor[], MainController controller) {
    TVariavel variavel;
    NodeList vetorPaisAux;
    double gi;
    double gk;
    double gj;
    long tempo;
    long tempoFinal;

    Date data = new Date();
    tempo = data.getTime();
    vetorVariaveis = variaveis;
    descendentes = new boolean[variaveis.size()];
    ancestrais = new boolean[variaveis.size()];
    int vetorIJ[] = new int[2];
    this.BaseDados = BaseDados;
    this.vetor = vetor;
    this.numeroCaso = numeroCasos;

    arranjo = criaArranjo(vetorVariaveis);
    for(int i = 0; i < vetorVariaveis.size(); i++){
       variavel = (TVariavel)vetorVariaveis.get(i);
       vetorPaisAux = SetToolkit.clone(variavel.getPais());
       if (vetorPaisAux.size() == 0){
          vetorPaisAux = null;
       }
       gi = g(variavel,vetorPaisAux);
       for(int j = 0; j < vetorVariaveis.size(); j++){
         if(i != j){
            TVariavel variavelAux = (TVariavel)vetorVariaveis.get(j);
            if(membro(variavelAux, variavel.getPais())){
               arranjo[i][j]  = 0;
            }else{
                 vetorPaisAux = SetToolkit.clone(variavel.getPais());
                 vetorPaisAux.add(variavelAux);
                 gk = g(variavel,vetorPaisAux);
                 arranjo[i][j] = gk - gi;
            }
         }else{
            arranjo[i][j] = Double.NEGATIVE_INFINITY;
          }
       }
    }

    vetorIJ = maxMatriz();
    while(arranjo[vetorIJ[0]][vetorIJ[1]] > 0){
       if(arranjo[vetorIJ[0]][vetorIJ[1]] > 0){
           variavel = (TVariavel)vetorVariaveis.get(vetorIJ[0]);
           vetorPaisAux = variavel.getPais();
           vetorPaisAux.add(vetorVariaveis.get(vetorIJ[1]));
           gi = g(variavel,vetorPaisAux);
           achaAncestrais(variavel);
           achaDescendentes(variavel);
           for(int i = 0 ; i < ancestrais.length; i++){
               for(int j = 0 ; j < descendentes.length; j++){
                   if(ancestrais[i] && descendentes[j]){
                       arranjo[i][j] = Double.NEGATIVE_INFINITY;
                   }
               }
           }
           for(int i = 0; i < vetorVariaveis.size(); i++){
               if(arranjo[vetorIJ[0]][i] > Double.NEGATIVE_INFINITY){
                    if(membro((TVariavel)vetorVariaveis.get(i),variavel.getPais())){
                        arranjo[vetorIJ[0]][i] = 0;
                    } else{
                        vetorPaisAux = SetToolkit.clone(variavel.getPais());
                        vetorPaisAux.add(vetorVariaveis.get(i));
                        gj = g(variavel,vetorPaisAux);
                        arranjo[vetorIJ[0]][i] = gj - gi;
                    }
               }
           }
       }
       vetorIJ = maxMatriz();
    }
    data = new Date();
        tempoFinal = data.getTime() - tempo;
        if (tempoFinal / 1000 <= 0){
           System.out.println("Tempo de execução em milisegubdos = " + tempoFinal);
        } else{
           System.out.println("Tempo de execução em segundos = " + tempoFinal/1000);
        }
    janela = new TJanelaEdicao(vetorVariaveis, net);
	//janela = new TJanelaEdicao(vetorVariaveis, controller);
    /*
    for(int numeroVariaveis = 0; numeroVariaveis < vetorVariaveis.size(); numeroVariaveis++) {
      variavel = (TVariavel)variaveis.get(numeroVariaveis);
      List vetorFrequencias = calculaFrequencias(variavel,variavel.getPais());
      List vetorNij = (List)vetorFrequencias.get(0);
      List vetorNijk = (List)vetorFrequencias.get(1);
      TTabPot tabela = variavel.getProbabilidades();
      tabela.porVariavel(variavel);
      for (int i = 0; i < variavel.getTamanhoPais(); i++) {
         tabela.porVariavel((TNo)variavel.getPais().get(i));
      }
      calculaProbabilidade(vetorNijk, vetorNij, variavel);
    }
    */
  }

  private void achaDescendentes(TVariavel variavel){
     for(int j = 0 ; j < vetorVariaveis.size(); j++)
           if(variavel.getName().equals(((TVariavel)vetorVariaveis.get(j)).getName())){
                descendentes[j] = true;
                break;
     }
     for(int i = 0 ; i < vetorVariaveis.size();i++){
         TVariavel variavelAux = (TVariavel)vetorVariaveis.get(i);
         List arrayAux = (List)variavelAux.getPais();
         for(int j = 0 ; j < arrayAux.size(); j++){
            TVariavel variavelAux2 = (TVariavel)arrayAux.get(j);
            if(variavel.getName().equals(variavelAux2.getName())){
                achaDescendentes(variavelAux);
            }
         }
     }
  }

  private void achaAncestrais(TVariavel variavel){
        TVariavel ancestral;
        List pais;
        pais = (List)variavel.getPais();
        for(int j = 0 ; j < vetorVariaveis.size(); j++)
           if(variavel.getName().equals(((TVariavel)vetorVariaveis.get(j)).getName())){
                ancestrais[j] = true;
                break;
        }
        for(int i = 0 ; i < pais.size();i++){
            ancestral = (TVariavel)pais.get(i);
            achaAncestrais(ancestral);
        }
  }

  private int[] maxMatriz(){
     double max;
     double maxAux;
     int vetor[] = new int[2];
     max = Double.NEGATIVE_INFINITY;

     for(int i = 0 ; i < vetorVariaveis.size(); i++){
        for(int j = 0 ; j < vetorVariaveis.size(); j++){
             maxAux = arranjo[i][j];
             if(maxAux >= max){
                 vetor[0] = i;
                 vetor[1] = j;
                 max = maxAux;
             }
        }
     }
     return vetor;
  }

  private double[][] criaArranjo(NodeList vetorVariaveis){
      arranjo = new double[vetorVariaveis.size()][vetorVariaveis.size()];
      return arranjo;
  }

  private boolean membro(TVariavel variavel, NodeList pais){
      for(int i = 0 ; i < pais.size(); i++){
        if(variavel.getName().equals(((TVariavel)pais.get(i)).getName())){
           return true;
        }
      }
      return false;
  }
}