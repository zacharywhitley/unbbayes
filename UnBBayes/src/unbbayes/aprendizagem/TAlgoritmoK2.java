package unbbayes.aprendizagem;

import java.util.Date;
import java.util.List;

import unbbayes.fronteira.TJanelaEdicao;
import unbbayes.jprs.jbn.Node;
import unbbayes.jprs.jbn.PotentialTable;
import unbbayes.jprs.jbn.ProbabilisticNetwork;
import unbbayes.util.NodeList;
import unbbayes.util.SetToolkit;

/**
 *   Essa classe implementa os m�todos necess�rios para que
 *   o algoritmo k2 funcione .O algoritmo k2 � um
 *   algoritmo de aprendizagem que utiliza a busca em pontua�ao.
 *   @author     Danilo Cust�dio
 *   @version    1.0
 *   @see TAprendizagemTollKit
 */

public abstract class TAlgoritmoK2 extends TAprendizagemTollKit {

        protected TJanelaEdicao janela;
        private double max;
        protected double values[];
        protected double maxs[];
        public static long time;


        /*Vari�vel necess�rias para que os Threads funcionem*/
        private TVariavel variavelT1;
        private TVariavel variavelT2;
        private List arrayT2;
        private long matrizNijk[][][];


    /**
     * M�todo que representa a fun�ao principal do algoritmo k2.
     * Esse m�todo recebe um lista de vari�veis, uma matriz com
     * os dados do arquivo, e a partir disso monta a rede bayseana
     * correspondente aquele arquivo.
     *
     * @param variaveis Lista de vari�veis(<code>List</code>)
     * @param BaseDados Representa��o do arquivo em mem�ria(<code>byte[][]<code>)
     * @param vetor     Vetor que indica quantas vezes uma linha do arquivo se repete
     * (<code>int[]<code>)
     * @param delta     parametro de controle(<code>int</code>)
     * @see TVariavel
     * @see TJanelaEdicao
     * @see Tnij
     * @see TAprendizagemTollKit
     */
    protected void CalculaAlgoritmoK2(NodeList variaveis, byte[][] BaseDados, int numeroCasos, int vetor[], int delta, ProbabilisticNetwork net){
	//protected void CalculaAlgoritmoK2(List variaveis, byte[][] BaseDados, int numeroCasos, int vetor[], int delta, MainController controller){
        PotentialTable tabela;
        TVariavel z;
        int arrayNijk[][];
        TVariavel variavel;
        Date data = new Date();
        double pOld;
        double pNew;
        double variacao;
        long tempo;
        long tempoFinal;
        boolean continuar;
        this.vetorVariaveis = variaveis;
        this.BaseDados = BaseDados;
        this.vetor = vetor;
        this.numeroCaso = numeroCasos;
        variacao = Math.pow(10,delta);
        tempo = data.getTime();
        continuar = false;
        values = new double[variaveis.size()];
        montaEstruturaPredecessores(vetorVariaveis);
        //montaNijk(vetorVariaveis);
        int tamanho = variaveis.size();
        Thread t1 = new Thread(new Runnable(){
            public void run(){
                for(int j = 0 ; j < vetorVariaveis.size(); j++){
                    variavelT1 = (TVariavel)vetorVariaveis.get(j);
                    values[j] = g(variavelT1,null);
                }
            }
        });
        t1.start();
        /*
        Thread t2 = new Thread(new Runnable(){
            public void run(){
                for(int k = 0 ; k < vetorVariaveis.size();k++){
                }

            }
        });
        */
        for(int numeroVariaveis = 0; numeroVariaveis < tamanho; numeroVariaveis++){
            pOld = 0.0;
            continuar = true;
            variavel = (TVariavel)variaveis.get(numeroVariaveis);
            setPosicao(numeroVariaveis);
            setLabel(variavel.getName());
            int tamanhoPais = variavel.getNumeroMaximoPais();
            int cont = 0;
            while (continuar && variavel.getTamanhoPais() < tamanhoPais){
                z = calculaZ((TVariavel)variavel.clone());
                pNew = max;
                while(pOld == 0 && cont == 0){
                    pOld = values[numeroVariaveis];
                }
                cont++;
                if (pNew - pOld > variacao){
                    pOld = pNew;
                    concatena(variavel.getPais(), z);
                } else{
                    continuar = false;
                }
            }
            System.out.println("Variavel " + variavel.getName());
            for (int j = 0; j < variavel.getPais().size(); j++) {
                System.out.print(variavel.getPais().get(j) + " ");
            }
            System.out.println();
        }
        System.out.println("Time = " + TAlgoritmoK2.time);
        data = new Date();
        tempoFinal = data.getTime() - tempo;
        if (tempoFinal / 1000 <= 0){
           System.out.println("Tempo de execu��o em milisegubdos = " + tempoFinal);
        } else{
           System.out.println("Tempo de execu��o em segundos = " + tempoFinal);
        }
        janela = new TJanelaEdicao(variaveis, net);
		//janela = new TJanelaEdicao(variaveis, controller);
        for(int numeroVariaveis = 0; numeroVariaveis < tamanho; numeroVariaveis++) {
            variavel = (TVariavel)variaveis.get(numeroVariaveis);
            arrayNijk = calculaFrequencias(variavel,variavel.getPais());
            tabela = variavel.getProbabilidades();
            tabela.addVariable(variavel);
            int tamanhoPais = variavel.getTamanhoPais();
            for (int i = 0; i < tamanhoPais; i++) {
                tabela.addVariable(variavel.getPais().get(i));
            }
            calculaProbabilidade(arrayNijk, variavel);
        }

    }


    /**
     * M�todo que calcula a v�riavel que est� na diferen�a
     * entre os predecessores e os pais que m�xima a
     * fun��o de qualidade g;
     * @param vari�vel - Vari�vel que possui a lista de prede
     * cessore (<code>TVariavel<code>)
     * @return TVariavel - A variavel que maximiza a fun��o
     * @see TVariavel
     */
    protected TVariavel calculaZ(TVariavel variavel){
        TVariavel z = null;
        NodeList vetorPais;
        NodeList vetorZ;
        double maxAux;
        maxAux = 0.0;
        max = -1*Double.MAX_VALUE;
        vetorZ = diferenca(SetToolkit.clone(variavel.getPredecessores()), variavel.getPais());
        int tamanho = vetorZ.size();
        for (int numeroCandidatos = 0 ; numeroCandidatos < tamanho; numeroCandidatos ++ ){
            vetorPais = concatena(SetToolkit.clone(variavel.getPais()), (TVariavel)vetorZ.get(numeroCandidatos));
            maxAux = g(variavel,vetorPais);
            if (max < maxAux){
                max = maxAux;
                z = (TVariavel)vetorZ.get(numeroCandidatos);
            }
        }
        return z;
    }


}