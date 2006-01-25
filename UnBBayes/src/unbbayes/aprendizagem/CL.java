package unbbayes.aprendizagem;

import unbbayes.aprendizagem.ConstructionController;
import unbbayes.util.NodeList;
//import unbbayes.aprendizagem.CBLToolkit;
//import unbbayes.aprendizagem.LearningToolkit;

public class CL {
	
	public NodeList variaveis;
	public int classe;
	public int caseNumber;
	public int raiz;
	protected byte[][] dataBase;
    protected int[] vector;
    protected boolean compacted;
    protected double [][] matrizinfo;
    protected int[] arvore;
    protected int[] enderecos;
    protected int[] melhorarvore;
    protected double melhorinfo;
    protected int notestado;
    protected int nvar;
    protected boolean ciclo,fimbusca;
	public ConstructionController controller;
	
	public int ln,cl;
	public CL(){
		
	}
	public void preparar(NodeList vetordevariaveis, int classei, int numerodecasos, int[] vetor,boolean comp,byte[][]dados) {
		int i;
		nvar=vetordevariaveis.size();
		this.variaveis=new NodeList();
		variaveis.ensureCapacity(nvar);
		for(i=0;i<nvar;i++)variaveis.add(vetordevariaveis.get(i));
		classe=classei;
		caseNumber=numerodecasos;
		vector=vetor;
		dataBase=dados;
		compacted=comp;
		matrizinfo=new double[nvar][nvar];
		calculainformacoes();
		calculaRaiz();
		arvore=new int[nvar-1];
		melhorarvore=new int[nvar-1];
		melhorinfo=0;
		enderecos=new int[nvar-1];
		int h=0;
		for(i=0;i<nvar;i++){
			if((i==classe)||(i==raiz)){
				
			if(i==raiz)enderecos[nvar-2]=i;
			}
			else{
				enderecos[h]=i;
				h++;
			}
		}
		for(i=0;i<nvar-2;i++)arvore[i]=0;
		fimbusca=false;
		double ii;
		while (!fimbusca){
			proxima();
			ii=infoatual();
			if(ii>melhorinfo){
				melhorinfo=ii;
				melhorarvore=arvore;
			}}
		for(i=0;i<nvar;i++){
			variaveis.ClearChildenFrom(i);
			variaveis.ClearParentsFrom(i);
		}
		//j=j-1;
		for(i=0;i<nvar-1;i++){
		if((i!=classe)&&(i!=raiz)){
		variaveis.AddParentTo(i,variaveis.get(enderecos[arvore[i]]));
		variaveis.AddChildTo(enderecos[arvore[i]],variaveis.get(i));}
		}
		//j=j+1;		
		for(i=0;i<nvar;i++){
			if(i!=classe){
			variaveis.AddParentTo(i,variaveis.get(classe));
			variaveis.AddChildTo(classe,variaveis.get(i));}
			}
	}
	
	public double log2(double numero){
        return Math.log(numero)/Math.log(2);
    }
	/*public int fatorial(int numero){
		int resultado=1;
		if(numero>1){for(int i=numero;i>1;i--)resultado=resultado*i;		}
		return resultado;
	}*/
	
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
		
	public void calculainformacoes() {
	for(int i=0;i<nvar;i++){
	for(int j=i+1;j<nvar;j++){
		if (i!=j){matrizinfo[i][j]=mutualInformation((TVariavel)variaveis.get(i),(TVariavel)variaveis.get(j));}}}
	}
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
	
	protected boolean proxima(){
	int i,k,l=0;k=0;
	for(i=0;i<nvar-2;i++){if(arvore[i]==nvar-2)l++;};
	for(i=0;i<nvar-2;i++){if((arvore[i]==nvar-1)&&(k==0))arvore[i]=0;			
		if((arvore[i]!=nvar-1)&&(k==0)){k++;arvore[i]++;}}
	valida();
	if(l==nvar-2)fimbusca=true;
	return (l==nvar-2);}
	
	protected void valida(){
		int i;
		boolean ciclou=false;
		for(i=0;i<nvar-2;i++){
			ciclo=false;
			notestado=i;
			paide(notestado);
			ciclou=ciclou || ciclo;	}
		if((ciclou)&&!(fimbusca))proxima();	}
	
	protected double infoatual(){
		double resultado=0;
		for(int i=0;i<nvar;i++){
			if((i!=classe)&&(i!=raiz))
			resultado=resultado+matrizinfo[i][enderecos[arvore[i]]];
		}
		return resultado;
	}
	
	protected int paide(int no){
		int resultado=arvore[no];
		if ((resultado==notestado)||(resultado==no)){ciclo=true;}
		if((resultado!=nvar-2)&&(!ciclo))resultado=paide(resultado);
		return resultado;
	}

}
