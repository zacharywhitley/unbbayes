/*
 *  UnBBayes
 *  Copyright (C) 2002, 2008 Universidade de Brasilia - http://www.unb.br
 *
 *  This file is part of UnBBayes.
 *
 *  UnBBayes is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  UnBBayes is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with UnBBayes.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package unbbayes.aprendizagem;

import java.util.ArrayList;

import unbbayes.prs.bn.LearningNode;
import unbbayes.util.NodeList;

/**
 * TAN
 * @version 1.0
 * @author Gabriel Guimarães - aluno de IC 2005-2006
 * @author Marcelo Ladeira - Orientador
 * @author Patricia Marinho
 */
public class CL extends CBLToolkit{
	/**
	 * Variaveis do objeto CL
	 */
	public NodeList variaveis;
	public int classe;
	public int caseNumber;
	public int raiz;
	protected int[][] dataBase;
    protected int[] vector;
    protected boolean compacted;
    protected double [][] matrizinfo;
    public int[] arvore,ramo;
    public int[] enderecos;
    public int[] melhorarvore;
    public double melhorinfo;
    protected int notestado,posicao;
    protected int nvar;
    protected boolean houveciclo;
    public ArrayList ls;
	public ConstructionController controller;
	/**
	 * Construtor
	 */
	public CL(){
		
	}
	/*
	 * Prepara e faz tudo.
	 */
	public void preparar(NodeList vetordevariaveis, int classei, int numerodecasos, int[] vetor,boolean comp,int[][]dados) {
		int i;
		//copiar os parametros
		nvar=vetordevariaveis.size();
		ramo=new int[nvar];
		this.variaveis=new NodeList();
		variaveis.ensureCapacity(nvar);
		for(i=0;i<nvar;i++)variaveis.add(vetordevariaveis.get(i));
		classe=classei;
		caseNumber=numerodecasos;
		vector=vetor;
		dataBase=dados;
		compacted=comp;
		
		prepara_memoria();
		detecta_arvore();
		desenharede();
		}
	/**
	 * log  base 2
	 * @param numero
	 * @return
	 */
	public double log2(double numero){
        return Math.log(numero)/Math.log(2);
    }
	/*
	 * Informação mutua da variavel xk para a xi
	 */	
	public double mutualInformation(LearningNode xi,LearningNode xk){    	
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
        int f = 0;
        double pi, pk;        
        for(int ic = 0; ic < caseNumber; ic++){    		
        	f  = compacted?vector[ic]:1;
        	il = dataBase[ic][xi.getPos()];
        	kl = dataBase[ic][xk.getPos()];        	        	
        	nik[il][kl] += f;
        	ni[il] += f;
        	nk[kl] += f;    
        	nt += f;    	
        }        
        for(il = 0 ; il < ri; il++){
        	pi = (1+ni[il])/((double)ri+nt);
        	for(kl = 0 ; kl < rk; kl++){
        		pk = (1+nk[kl])/((double)rk+nt);
        		pik = (1+nik[il][kl])/((double)(ri*rk)+nt);        			
        		im += pik*(log2(pik) - log2(pi) - log2(pk));
        	}        		
        }                
        return im;    	
    }
	
	protected double conditionalMutualInformation(int v1, int v2, int classe){
    	
		//int qj = getQ(sep);
		//this.classe = classe;
		//int qj = classe;
    	//if(qj == 0 ){
		  //return mutualInformation((LearningNode)variaveis.get(v1),
    		//                        (LearningNode)variaveis.get(v2));    		
    	//}    	;
    	
    	int ri = ((LearningNode)variaveis.get(v1)).getEstadoTamanho();
    	int rk = ((LearningNode)variaveis.get(v2)).getEstadoTamanho();
    	int rj = ((LearningNode)variaveis.get(classe)).getEstadoTamanho();
    	double pjik;
    	double cpjik;
    	double im = 0.0;    	
    	int[] nj = new int[rj];
    	int[][][] njik = new int[rj][ri][rk];
    	int[][] nji = new int[rj][ri];    	
    	int[][] njk = new int[rj][rk];
    	double[][] pji = new double[rj][ri];
    	double[][] pjk = new double[rj][rk];
    	int j  = 0 ; 
    	int f; 
    	int il;
    	int kl;
    	int nt =0;  	
    	for(int id = 0 ; id < caseNumber; id ++){
    		f = compacted?vector[id]:1;
    		il = dataBase[id][v1];    		
    		kl = dataBase[id][v2];
    		j=dataBase[id][classe];
    		njik[j][il][kl] += f;
    		nji[j][il] += f;
    		njk[j][kl] += f;
    		nj[j] += f;
    		nt += f;    		
    	}
    	for(j = 0 ; j < rj; j++){
    		for(il = 0 ; il < ri; il++){
    			pji[j][il] = (1+nji[j][il])/(double)(ri+nj[j]);   			
    		}
    		for(kl = 0 ; kl < rk ; kl++){
    		    pjk[j][kl] = (1+njk[j][kl])/(double)(rk+nj[j]);   			   
    		}
    		for(il = 0; il < ri; il ++){
                for(kl = 0 ; kl < rk; kl++){
                    pjik =  (1+njik[j][il][kl])/(double)(ri*rk*rj+nt);
                    cpjik =  (1+njik[j][il][kl])/(double)(ri*rk+nj[j]);
                    im += pjik*(log2(cpjik) - log2(pji[j][il]) - log2(pjk[j][kl]));
                }
    		}                       		
    	}
    	nj = null;
    	njk = null;
    	nji = null;
    	njik = null;
    	pji = null;
    	pjk = null;    	
    	return im;
    }
    
	
	/*
	 * Cria matriz de informações mutuas
	 */	
	public void calculainformacoes() {
	for(int i=0;i<nvar;i++){
	for(int j=0;j<nvar;j++){
		if (i!=j){matrizinfo[i][j]=conditionalMutualInformation(i, j, classe);
	//	System.out.println(variaveis.get(i).getName()+" e "+variaveis.get(j).getName()+" = "+String.valueOf(matrizinfo[i][j]));
		}}
	}
	}
	/*
	 * Seleciona melhor variavel independente para raiz da arvore
	 */
	public void calculaRaiz(){
		double maiorResultadoAtual=0,resultado=0;
		int posicao=0;
		for(int i=0;i<nvar;i++){
			if(i!=classe){
				resultado=matrizinfo[classe][i];
				//System.out.println(variaveis.get(i).getName()+" = "+String.valueOf(matrizinfo[classe][i]));
				if(resultado>maiorResultadoAtual){
					maiorResultadoAtual=resultado;
					posicao=i;					
				}}raiz=posicao;	}}
	/**
	 * Detects the next valid tree
	 * @return
	 */
	protected boolean proxima(){
	int i,k,l=0;k=0;
	for(i=0;i<nvar-3;i++){if(arvore[i]==nvar-2)l++;};
	for(i=0;i<nvar-3;i++){if((arvore[i]==nvar-2)&&(k==0))arvore[i]=0;			
		else if((arvore[i]!=nvar-2)&&(k==0)){k++;arvore[i]++;}		
	}
	valida_arvore();
	
	return (l!=nvar-3);}
	/*
	 * Valida ou não uma Árvore
	 */
	protected boolean valida_arvore(){
		int i;
		houveciclo=true;
		for(i=0;i<nvar-3;i++){
			posicao=0;
			ramo[0]=i;
			houveciclo=houveciclo&&valida_ramo(i);
			}
		if(houveciclo){
		//	for(int d=0;d<nvar-2;d++)System.out.print(String.valueOf(arvore[d])+" ");
		//	System.out.println();
		}
		return !houveciclo;
		}
	/*
	 * Soma das informações mutuas entre cada filho e seu pai
	 */
	protected double infoatual(){
		double resultado=0;
		int j=0;
		//for(int i=0;i<nvar;i++){
		for(int i=0;i<nvar-3;i++){
		// if((i!=classe)&&(i!=raiz))
			 try{
			resultado=resultado+matrizinfo[enderecos[arvore[i]]][enderecos[i]];
		 //j++;
			 }
		 catch (java.lang.ArrayIndexOutOfBoundsException ee){
			 j--;
			 j++;
		 }
		}
		return resultado;
	}
	
	
	/*
	 * Para verificar ciclos nodeList ramos
	 */
	protected boolean valida_ramo(int no){
		boolean fim=false;
		posicao++;
		boolean naoteveciclo=true;
		try{
		ramo[posicao]=arvore[no];
		if(arvore[no]!=nvar-2){
		for(int i=0;i<posicao;i++){
			for(int j=i+1;j<posicao;j++){
				naoteveciclo=naoteveciclo&&(!(ramo[i]==ramo[j]));}}
		}
		else fim=true;
		if((!fim)&&(naoteveciclo))valida_ramo(arvore[no]);
		}
		catch (Exception ee){
			System.out.println("");
		}
		return naoteveciclo;}

	private void desenharede(){
		for(int i=0;i<nvar;i++){
//			TODO Is this correct???
//			variaveis.ClearChildrenFrom(i);
//			variaveis.ClearParentsFrom(i);
			variaveis.remove(i);
		}
		//desenhar a rede
		int aux=0;
		for(int i=0;i<nvar;i++){
		if((i!=classe)&&(i!=raiz)){
			aux++;
//			TODO Is this correct???
//			variaveis.AddParentTo(enderecos[aux],variaveis.get(enderecos[melhorarvore[aux]]));
//			variaveis.AddChildTo(enderecos[melhorarvore[aux]],variaveis.get(enderecos[aux]));
			variaveis.add(enderecos[aux],variaveis.get(enderecos[melhorarvore[aux]]));
			variaveis.add(enderecos[melhorarvore[aux]],variaveis.get(enderecos[aux]));
			}
		}
	}
	private void detecta_arvore(){
		melhorinfo=0;
		for(int i=0;i<nvar-2;i++)arvore[i]=0;
		double ii;
		while (proxima()){
			ii=infoatual();
			if(ii>melhorinfo){
				melhorinfo=ii;
				//this.melhorarvore=arvore;
				for(int d=0;d<nvar-2;d++)this.melhorarvore[d]=arvore[d];
				//System.out.println(String.valueOf(melhorinfo)+":");
				//for(int d=0;d<nvar-3;d++)System.out.println(variaveis.get(enderecos[d])+" pai = "+variaveis.get(enderecos[melhorarvore[d]])+ " ");				
			}}
		
		ii=infoatual();
	}
	private void prepara_memoria(){
		int h=0;
		enderecos=new int[nvar-1];
		matrizinfo=new double[nvar][nvar];
		calculainformacoes();
		calculaRaiz();
		arvore=new int[nvar-1];
		melhorarvore=new int[nvar-1];
		melhorinfo=0;
		for(int i=0;i<nvar;i++){
			if((i==classe)||(i==raiz)){
				
			if(i==raiz)enderecos[nvar-2]=i;
			}
			else{
				enderecos[h]=i;
				h++;
			}
		}
		//reservar memoria		
	}
	
	
}//objeto
