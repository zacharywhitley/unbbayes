package unbbayes.aprendizagem;

import java.util.*;

import unbbayes.util.NodeList;
import unbbayes.util.SetToolkit;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:
 * @author    Danilo Custodio da Silva
 * @version 1.0
 */

public abstract class TAprendizagemTollKit {

    public static boolean compactado;
    protected byte[][] BaseDados;
    protected int[] vetor;    
    protected int numeroCaso;
    protected NodeList vetorVariaveis;

    /**
     * Funçao de qualidade : Comparando essa função que é possivel decidir
     * se uma determinada variável é ou não pai de um certo nó.
     * A funçao de qualidade possui tres implementações no pacote de aprendi
     * zagem do UnbBayes: GH, GHS, MDL
     *
     *@param variável (<code>TVariavel<code>)
     *@param pais lista de pais da variável(<code>List<code>)
     *@return double - Resultado da aplicaçào da função de qualidade
     *@see TVariavel
     */
    public abstract double g(TVariavel variavel, NodeList pais);

    protected double informacaoMutuaCond(int v1, int v2, ArrayList sep){
    	int qj = calculaQj(sep);
    	if(qj == 0 ){
    		return informacaoMutua((TVariavel)vetorVariaveis.get(v1),
    		                        (TVariavel)vetorVariaveis.get(v2));    		
    	}
    	int ri = ((TVariavel)vetorVariaveis.get(v1)).getEstadoTamanho();
    	int rk = ((TVariavel)vetorVariaveis.get(v2)).getEstadoTamanho();
    	double pjik;
    	double cpjik;
    	double im = 0.0;
    	int[] nj = new int[qj];
    	int[][][] njik = new int[qj][ri][rk];
    	int[][] nji = new int[qj][ri];
    	int[][] njk = new int[qj][rk];
    	double[][] pji = new double[qj][ri];
    	double[][] pjk = new double[qj][rk];
    	int[] mult = multiplicadores(sep); 
    	int j  = 0 ; 
    	int f; 
    	int il;
    	int kl;
    	int nt =0;  	
    	for(int id = 0 ; id < numeroCaso; id ++){
    		f = TAprendizagemTollKit.compactado?vetor[id]:1;
    		j = achaJ(sep,id,mult);
    		il = BaseDados[id][v1];    		
    		kl = BaseDados[id][v2];
    		njik[j][il][kl] += f;
    		nji[j][il] += f;
    		njk[j][kl] += f;
    		nj[j] += f;
    		nt += f;    		
    	}
    	for(j = 0 ; j < qj; j++){
    		for(il = 0 ; il < ri; il++){
    			pji[j][il] = (1+nji[j][il])/(double)(ri+nj[j]);   			
    		}
    		for(kl = 0 ; kl < rk ; kl++){
    		    pjk[j][kl] = (1+njk[j][kl])/(double)(rk+nj[j]);   			   
    		}
    		for(il = 0; il < ri; il ++){
                for(kl = 0 ; kl < rk; kl++){
                    pjik =  (1+njik[j][il][kl])/(double)(ri*rk*qj+nt);
                    cpjik =  (1+njik[j][il][kl])/(double)(ri*rk+nj[j]);
                    im += pjik*(log(cpjik) - log(pji[j][il]) - log(pjk[j][kl]));
                }
    		}                       		
    	}
    	return im;
    }     
    
    protected int achaJ(ArrayList cc, int id, int[] mult){
    	int j = 0; 
    	int im = 0;
    	for(int i = 0 ; i < cc.size(); i++){
    		j += BaseDados[id][((Integer)cc.get(i)).intValue()]*mult[im];
    		im++;
    	} 
    	return j;   	    	
    }
    
    protected int[] multiplicadores(ArrayList a){
    	TVariavel varAux;
    	int np = a.size();
    	int m = np==0?1:np;
    	int[] mult = new int[m];
    	mult[m-1] = 1;
    	for(int i = m-2; i >= 0; i--){
    	    varAux = (TVariavel)vetorVariaveis.get(((Integer)a.get(i+1)).intValue());
    	    mult[i] = varAux.getEstadoTamanho() * mult[i+1];	    		
    	}
    	return mult;
    }	
    
    protected double informacaoMutua(TVariavel xi,TVariavel xk){    	
    	int nt = 0;
    	int il = 0;    	
    	int kl = 0;
    	double im = 0;    	
    	double pik = 0;
        int ri = xi.getEstadoTamanho();
        int rk = xk.getEstadoTamanho();
        int nik[][] = new int[ri][rk];
        int ni[] = new int[ri];
        int nk[] = new int[rk];
        double pi[] = new double[ri];
        double pk[] = new double[rk];
        int f = 0;
        for(int ic = 0; ic < numeroCaso; ic++){
    		f = TAprendizagemTollKit.compactado?vetor[ic]:1;
        	il = BaseDados[ic][xi.getPos()];
        	kl = BaseDados[ic][xk.getPos()];        	        	
        	nik[il][kl] += f;
        	ni[il] += f;
        	nk[kl] += f;    
        	nt += f;    	
        }
        for(il = 0 ; il < ri; il++){
        	pi[il] = (1+ni[il])/((double)ri+nt);
        	for(kl = 0 ; kl < rk; kl++){
        		pk[kl] = (1+nk[kl])/((double)rk+nt);
        		pik = (1+nik[il][kl])/((double)(ri*rk)+nt);        			
        		im += pik*(log(pik) - log(pi[il]) - log(pk[kl]));
        	}        		
        }        
        return im;    	
    }
    
    protected int calculaQj(ArrayList cc){
    	TVariavel varAux;
    	int ac = 1;
    	for(int i = 0 ; i < cc.size(); i++){
    		varAux = (TVariavel)vetorVariaveis.get(((Integer)cc.get(i)).intValue());    		
    		ac *= varAux.getEstadoTamanho();
    	}
    	if(cc.size() == 0){
    	    return 0;	
    	} 
    	return ac;    	
    }

    /**
     *  Esse método calcula as probabilidades dos nós da rede
     *  gerada pelo algortmo de aprendizagem.
     *
     * @param vetorNijk vetor que contem todas as configurações de pais em
     * conjunto com a variável (<code>List<code>)
     * @param vetorNij vetor que contem todas as configuraçoes de pais
     * (<code>List<code>)
     * @param variavel Variável na qual a probabilida sera inserida
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
     * Método calcula os vetores Nij e Nijk varrendo uma só vez a base de
     * dados. O vetorNij possui todas as configurações possíveis de pais
     * junto aos estados das variáveis, já o vetorNij possui todas as con
     * figurações possíveis dos pais.
     *
     * @param variavel
     */
    protected  int[][] calculaFrequencias(TVariavel variavel, NodeList pais){
        TVariavel variavelAux;
        int[][] ArrayNijk;
        int tamanhoPais, posicao;
        if(pais == null){
           pais = new NodeList();
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

    protected List montaInstancias(NodeList pais){
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
    * Método que calcula o número possível de permutações
    * entre os pais de um determinada variavel.
    * @param pais - pais da variável(<code>ArrayLis<code>)
    * @return int - Número de permutações
    * @see TVariavel
    */
    protected int calculaQi(NodeList pais) {
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
     * Método que retorna os objetos diferentes entre um vetor e outro,
     * ou seja, os objetos que estiverem no primeiro vetor e não
     * estiverem no segundos devem ser retornados em um vetor.
     * @param predecessores - Vetor de predecessore (<code>List<code>)
     * @param pais - Vetor de pais(<code>List<code>)
     * @return List - Vetor da diferença entre predecesores e pais
     * @see TVariavel
     */
     protected NodeList diferenca(NodeList predecessores, NodeList pais){
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

    /** Método que concatena uma variável a um vetor.
     *  @param pais - Vetor de pais de uma variável(<code>
     *  List<code>)
     *  @param z - Variável a ser concatenada no veto
     *  de pais(<code>TVariavel<code>)
     *  @return List - Vetor com a variável concatenada
     *  @see TVariavel
     */
    protected NodeList concatena(NodeList pais, TVariavel z){
        if (pais == null){
            pais = new NodeList();
        }
        pais.add(z);
        return pais;
    }

   /**
    * Método para o calculo do log na bese 10
    * @param double - O Número para o calculo do log (<code>
    * double<code>)
    * @return double - O Log do número
    */
    protected double log(double numero){
        return Math.log(numero)/Math.log(10);
    }

    /**
     * Método para montar os predecessores de todos os nós. Prede
     * cessores de uma variável são variáveis que tem um potencial
     * para ser um dos pais desta.
     * @param vetor - Lista de variaveis (<code>List<code>)
     * @see TVariavel
     */
    protected void montaEstruturaPredecessores(NodeList vetor){
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