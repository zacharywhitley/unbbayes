package unbbayes.gui;

import unbbayes.aprendizagem.TVariavel;
import unbbayes.util.NodeList;
import unbbayes.util.SwingWorker;
import unbbayes.gui.janeladiscret;

public class dalgo extends Thread {
public NodeList variables;
public byte[][] originalmatrix;
public int[][] matrix1;
public int mlines;
public int mcolumns;
public int [] margincolumn;
public int [] marginline;
public int total;
public double c2p;
public float limiteperda;
public double alfa;
public boolean dochi2;
public boolean dowh;
public janeladiscret controlador;
private int var1,var2,e1,e2,nv1,nv2,nvar,resultado,totalciclos,progresso;
private boolean continua;

public void SetController(janeladiscret wc){
	this.controlador=wc;
}

public void Setmatrix(byte[][] tempmatrix){
	 mlines=lines(tempmatrix);
	 mcolumns=columns(tempmatrix);
	 
	originalmatrix = new byte[mlines][mcolumns];
	originalmatrix=tempmatrix;
	 	  }
public void Setvariables(NodeList vari){
	variables=new NodeList();
	variables.ensureCapacity(30);
	variables.addAll(vari);
	//variables=vari;
	}
public dalgo(){
	this.dowh=true;
	this.dochi2=true;

}

public int[][] getcrosstable2(int var1, int ei, int e12, int var2, int estadosv2){
	int [][] resultado= new int[2][estadosv2];
	int i,j;
	j=0;
	
	for(i=0;i<estadosv2;i++){
		resultado[0][i]=countc(var1,ei,ei,var2,i);
		j=j+resultado[0][i];
	}
	this.marginline = new int[estadosv2];
	this.margincolumn=new int[2];
	this.margincolumn[0]=j;
	this.total=0;
	int buf=0;
	for(i=0;i<estadosv2;i++){
		resultado[1][i]=countc(var1,ei,e12,var2,i);
		j=j+resultado[0][i];
	}
	this.margincolumn[1]=j;
	for(i=0;i<estadosv2;i++){
		for(j=0;j<2;j++){
			buf=buf+resultado[j][i];
		}
		this.marginline[i]=buf;
		this.total=this.total+buf;
	}
		
	return resultado;
}
public int lines(byte[][] tempmatrix){
	 int i;
	 i=tempmatrix.length;
	 return i;
 }
public int columns(byte[][] tempmatrix){
	 int i;
	 i=tempmatrix[0].length;
	 return i;
 }
 /** Contagem condicional: se uma linha tem o estado e1 ou e2 de var 1 e
  * evar2 de var2 então uma unidade é acrescida ao somatório.
  * @param var1
  * @param e1
  * @param e2
  * @param var2
  * @param evar2
  * @return
  */
public int countc(int lvar1,int le1,int le2, int lvar2, int levar2){
		int i,counter;
		counter=0;
		for(i=0;i<mlines;i++){
			boolean found=false;
			if ((originalmatrix[i][lvar1]==le1)||(originalmatrix[i][lvar1]==le2)){
				if (originalmatrix[i][lvar2]==levar2)found=true;
			}
			if(found)counter++;
		}
		
		return counter;
	}
/**
 * 
 * @param number
 * @return zero se o numero nao é impar, 1 se for.
 */
public int odd(int number)
{int isodd=0;
if (Math.round(number/2+0.00001)==number/2)  isodd=1;
return isodd;}
/**
 * Transforma o valor do qui-quadrado em probabilidade de rejeição de H0
 * @param chi2 -valor do qui-quadrado
 * @param df - graus de liberdade
 * @return valor do p
 */
public double c2prob(double chi2,int df){
	double pval=0;

	if (chi2<1000)
	{
	int add=0;
	double mult=1;
	if (odd(df)<0.5)
	 {
	      add=1;
	      mult=Math.sqrt(2/chi2/Math.PI);
	   }
	float denominator=1;
	int index;
	for (index=df;index>1;index-=2)
	         denominator=denominator*index;
	double numerator= Math.pow(chi2,((df*1+add*1)/2))*Math.exp(chi2*-1/2);
	double sum=1;
	double m=1;
	int count;
	for (count=df*1+2*1;m>0.00000000001;count+=2)
	   {
	      m=(m*chi2)/count;
	      sum=sum+m;
	   }
	pval=1-sum*mult*numerator/denominator;
	}
	return pval;
	}
	
/** Calcula o escore da variavel var1 se concatenada entre e1 e e2.
	 * Para o calculo do escore de e1 apenas basta usar o mesmo e1 para e2.
	 * @param var1 Variável a ser discretizada
	 * @param e1 estado 1 da variavel var1
	 * @param e2 estado 2 da variavel var 1
	 * @param var2 variável limitante
	 * @param estadosv2 número de estados de var2
	 * @return Escore (nao normalizado)
	 */
public float score(int var1, int e1, int e2, int var2, int estadosv2){
		int i,j;
		float score=0;float tempscore=0;
		for(i=0;i<estadosv2-1;i++){
			for(j=i+1;j<estadosv2;j++){
				tempscore= countc(var1,e1,e2,var2,i)-countc(var1,e1,e2,var2,j);
				if (tempscore<0)tempscore=tempscore*(-1);
				score = score+tempscore;
			}
		}
		
		return score;
	}
public double c2(){
	double result=0;
	double partial=0;
	double eij=0;
	int i,j;
	for (i=0;i<mlines;i++){
		for(j=0;j<mcolumns;j++){
			eij=(margincolumn[i]*marginline[j])/total;//(ni*nj)/n
			partial=(matrix1[i][j]-(eij*eij))/eij;
			result=result+partial;
		}
		this.c2p=c2prob(result,((mlines-1)*(mcolumns-1)));
	}
	
	return result;
}
public void contaciclos(){
	totalciclos=0;
	nvar=variables.size();
	var2=0;
for(var1=0;var1<nvar-1;var1++){
	nv1=variables.get(var1).getStatesSize();
 for(e1=0;e1<nv1-1;e1++){
	var2=var1+1;
	nv2=variables.get(var2).getStatesSize();
	totalciclos++;
 }
 
}
	
}
public int doonce(){
//int nvar;
//int resultado=0;
//boolean continua=true;
	progresso=0;
nvar=variables.size();
//	int var1,var2,e1,e2,nv1,nv2;
	var2=0;
for(var1=0;var1<nvar-1;var1++){
	nv1=variables.get(var1).getStatesSize();

 for(e1=0;e1<nv1-1;e1++){
	 e2=e1+1;
	 continua=true;
	var2=var1+1;
	nv2=variables.get(var2).getStatesSize();
	progresso++;
	while ((var2<nvar) && (continua)){
		//para cada combinação de duas variáveis
		//fazer: para cada tentativa de aglomerar e1,e2
		//verificar se há perda de informação significante
		
		matrix1 = new int[2][nv2];
		matrix1=this.getcrosstable2(var1,e1,e2,var2,nv2);
		float score1,score2,score12,perda;
		//calcula escore de e1,v2
		score1=this.score(var1,e1,e1,var2,nv2);
		//calcula escore de e2,v2
		score2=this.score(var1,e2,e2,var2,nv2);
		//calcula escore de e12,v2
		score12=this.score(var1,e1,e2,var2,nv2);
		//calcula perda pela concatenação
		if(this.dowh)perda=(score1+score2-score12)/this.total;
		else perda=(score1/this.total+score2/this.total-score12/this.total);
		if(perda>this.limiteperda){
			continua=false;
		}
		else {
		
		//verifica se houve perda significante (p<alfa) de informação
		//calcula qui² das tabelas: se p < alfa continua=falso
		if (this.dochi2){	
		matrix1 = new int[2][nv2];
		matrix1=this.getcrosstable2(var1,e1,e2,var2,nv2);
		c2();
		double c1=this.c2p;
		matrix1=this.getcrosstable2(var1,e2,e1,var2,nv2);
		c2();
		double c2=this.c2p;
		if (c2<=this.alfa || c1<=this.alfa){
		continua=false;
		}
		}//dochi2
			
		}//else perda
				
		controlador.mensagem(String.valueOf(progresso)+"/"+String.valueOf(totalciclos)+" Concatenadas: "+String.valueOf(this.resultado));
		controlador.repaint();
		var2++;
	}//while v2
    
	//todas as variáveis já foram consideradas por var1
	if (continua){
		this.concatena(var1,(byte)e1);
		resultado=resultado+1;
		}
	//se continua entao concatena e1,e2
	  // e resultado=verdadeiro
	nv1=variables.get(var1).getStatesSize();
 }//todos os estados de var1 já foram aglomerados, quando possivel
}//for var1
 return resultado;
}//doonce

public void concatena(int var, byte estado){
int i;
String novonome;
novonome=String.valueOf(variables.get(var).getStateAt(estado))+"_"+String.valueOf(variables.get(var).getStateAt(estado+1));
variables.get(var).setStateAt(novonome,estado);
System.out.println(variables.get(var).getName()+": "+novonome);
for(i=0;i<mlines;i++){
	if(this.originalmatrix[i][var]==(estado+1))this.originalmatrix[i][var]=estado;
}
try{
	if(variables.get(var).getStatesSize()>2){
		((TVariavel)variables.get(var)).removestate(estado);
	}
	else{
	//	variables.remove(var);
	}
}
catch (IndexOutOfBoundsException ee){
	System.out.println(ee.toString());
		
}

}
public void start(){
	int resp=1; 
//	while(resp>0){
	contaciclos();
		final SwingWorker worker = new SwingWorker() {
	        public Object construct() {
	        	int resp2;
	            resp2=doonce();
	            return 0;
	        }
	    };
	    worker.start();
		
		
		controlador.matriz=this.originalmatrix;
		controlador.variaveis=this.variables;
//	}
	
}

}//obj
