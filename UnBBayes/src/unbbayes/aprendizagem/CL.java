package unbbayes.aprendizagem;

import unbbayes.aprendizagem.ConstructionController;
import unbbayes.util.NodeList;

/**
 * TAN
 * @version 1.0
 * @author Gabriel Guimarães - aluno de IC 2005-2006
 * @author Marcelo Ladeira - Orientador
 * @author Patricia Marinho
 */
public class CL {
	/**
	 * Variaveis do objeto CL
	 */
	public NodeList variaveis;
	public int classe;
	public int caseNumber;
	public int raiz;
	protected byte[][] dataBase;
    protected int[] vector;
    protected boolean compacted;
    protected double [][] matrizinfo;
    protected int[] arvore,ramo;
    protected int[] enderecos;
    protected int[] melhorarvore;
    protected double melhorinfo;
    protected int notestado,posicao;
    protected int nvar;
    protected boolean houveciclo;
	public ConstructionController controller;
	/**
	 * Construtor
	 */
	public CL(){
		
	}
	/*
	 * Prepara e faz tudo.
	 */
	public void preparar(NodeList vetordevariaveis, int classei, int numerodecasos, int[] vetor,boolean comp,byte[][]dados) {
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
	 * log na base 2
	 * @param numero
	 * @return
	 */
	public double log2(double numero){
        return Math.log(numero)/Math.log(2);
    }
	/*
	 * Informação mutua da variavel xk para a xi
	 */	
	public double mutualInformation(TVariavel xi,TVariavel xk){    	
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
	/*
	 * Cria matriz de informações mutuas
	 */	
	public void calculainformacoes() {
	for(int i=0;i<nvar;i++){
	for(int j=i+1;j<nvar;j++){
		if (i!=j){matrizinfo[i][j]=mutualInformation((TVariavel)variaveis.get(i),(TVariavel)variaveis.get(j));}}}
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
				if(resultado>maiorResultadoAtual){
					maiorResultadoAtual=resultado;
					posicao=i;					
				}}raiz=posicao;	}}
	/**
	 * Detecta proxima árvore válida
	 * @return
	 */
	protected boolean proxima(){
	int i,k,l=0;k=0;
	for(i=0;i<nvar-3;i++){if(arvore[i]==nvar-2)l++;};
	for(i=0;i<nvar-3;i++){if((arvore[i]==nvar-2)&&(k==0))arvore[i]=0;			
		else if((arvore[i]!=nvar-2)&&(k==0)){k++;arvore[i]++;}		
	}
	valida_arvore();
	//Numero maximo=[nvar-2,nvar-2,nvar-2,nvar-2...]. Ele+1=[0,0,0..]
	//if(l==nvar-3)fimbusca=true;
	return (l==nvar-3);}
	/*
	 * Valida ou não uma árvore
	 */
	protected boolean valida_arvore(){
		int i;
		houveciclo=false;
		for(i=0;i<nvar-3;i++){
			posicao=0;
			ramo[0]=i;
			houveciclo=houveciclo||valida_ramo(i);
			}
		return !houveciclo;
		}
	/*
	 * Soma das informações mutuas entre cada filho e seu pai
	 */
	protected double infoatual(){
		double resultado=0;
		int j=0;
		for(int i=0;i<nvar;i++){
		 if((i!=classe)&&(i!=raiz))
			 try{
			resultado=resultado+matrizinfo[enderecos[j]][enderecos[arvore[j]]];
		 j++;
			 }
		 catch (java.lang.ArrayIndexOutOfBoundsException ee){
			 j--;
			 j++;
		 }
		}
		return resultado;
	}
	/*
	 * Para verificar ciclos nos ramos
	 */
	protected boolean valida_ramo(int no){
		boolean fim=false;
		posicao++;
		ramo[posicao]=arvore[no];
		if(arvore[no]!=nvar-2){
		for(int i=0;i<posicao-1;i++){
			for(int j=i+1;j<posicao;j++){
				houveciclo=houveciclo||(ramo[i]==ramo[j]);}}
		}
		else fim=true;
		if((!fim)&&(!houveciclo))valida_ramo(arvore[no]);
		return houveciclo;}

	private void desenharede(){
		for(int i=0;i<nvar;i++){
			variaveis.ClearChildenFrom(i);
			variaveis.ClearParentsFrom(i);
		}
		//desenhar a rede
		int aux=0;
		for(int i=0;i<nvar;i++){
		if((i!=classe)&&(i!=raiz)){
			aux++;
		variaveis.AddParentTo(enderecos[aux],variaveis.get(enderecos[arvore[aux]]));
		variaveis.AddChildTo(enderecos[arvore[aux]],variaveis.get(enderecos[aux]));}
		}
	}
	private void detecta_arvore(){
		for(int i=0;i<nvar-2;i++)arvore[i]=0;
		double ii;
		while (proxima()){
			ii=infoatual();
			if(ii>melhorinfo){
				melhorinfo=ii;
				melhorarvore=arvore;
			}}
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
