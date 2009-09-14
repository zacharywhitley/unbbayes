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
package unbbayes.datamining.discretize;

import java.util.ArrayList;

import unbbayes.prs.Node;
import unbbayes.prs.bn.LearningNode;
import unbbayes.util.SwingWorker;
import unbbayes.datamining.gui.preprocessor.janeladiscret;

/**
 * Algoritimo para discretiza��o m�ltipla
 * 
 * @author gabriel guimaraes - Aluno de IC 2005/2006
 * @orientador Marcelo Ladeira
 */
public class dalgo2 extends Thread {

	public ArrayList<Node> variables;

	public int[][] originalmatrix;

	public int[][] matrix1;

	public int mlines;

	public int mcolumns;

	public int[] margincolumn;

	public int[] marginline;

	public int total;

	public double c2p;

	public float limiteperda;

	public double alfa;

	public boolean dochi2;

	public boolean dowh;

	public boolean pesogeral;

	public janeladiscret controlador;

	private int var1, var2, e1, e2, nv1, nv2, nvar, resultado, totalciclos,
			progresso;

	private boolean continua;

	public void SetController(janeladiscret wc) {
		this.controlador = wc;
	}

	public void Setmatrix(int[][] tempmatrix) {
		mlines = lines(tempmatrix);
		mcolumns = columns(tempmatrix);

		// originalmatrix = new int[mlines][mcolumns]; // nao precisa
		// instanciar, j� que n�o est� criando uma c�pia abaixo
		originalmatrix = tempmatrix;
	}

	public void Setvariables(ArrayList<Node> vari) {
		variables = new ArrayList<Node>();
		variables.ensureCapacity(30);
		variables.addAll(vari);
		// variables=vari;
	}

	public dalgo2() {
		this.dowh = true;
		this.dochi2 = true;
		this.pesogeral = true;

	}

	public int[][] getcrosstable2(int var1, int ei, int e12, int var2,
			int estadosv2) {
		int[][] resultado = new int[2][estadosv2];
		int i, j;
		j = 0;

		for (i = 0; i < estadosv2; i++) {
			resultado[0][i] = countc(var1, ei, ei, var2, i);
			j = j + resultado[0][i];
		}
		this.marginline = new int[estadosv2];
		this.margincolumn = new int[2];
		this.margincolumn[0] = j;
		this.total = 0;
		int buf = 0;
		for (i = 0; i < estadosv2; i++) {
			resultado[1][i] = countc(var1, ei, e12, var2, i);
			j = j + resultado[0][i];
		}
		this.margincolumn[1] = j;
		for (i = 0; i < estadosv2; i++) {
			for (j = 0; j < 2; j++) {
				buf = buf + resultado[j][i];
			}
			this.marginline[i] = buf;
			this.total = this.total + buf;
		}

		return resultado;
	}

	public int lines(int[][] tempmatrix) {
		int i;
		i = tempmatrix.length;
		return i;
	}

	public int columns(int[][] tempmatrix) {
		int i;
		i = tempmatrix[0].length;
		return i;
	}

	/**
	 * Contagem condicional: se uma linha tem o estado e1 ou e2 de var 1 e evar2
	 * de var2 ent�o uma unidade � acrescida ao somat�rio.
	 * 
	 * @param var1
	 * @param e1
	 * @param e2
	 * @param var2
	 * @param evar2
	 * @return
	 */
	public int countc(int lvar1, int le1, int le2, int lvar2, int levar2) {
		int i, counter;
		counter = 0;
		for (i = 0; i < mlines; i++) {
			boolean found = false;
			if ((originalmatrix[i][lvar1] == le1)
					|| (originalmatrix[i][lvar1] == le2)) {
				if (originalmatrix[i][lvar2] == levar2)
					found = true;
			}
			if (found)
				counter++;
		}

		return counter;
	}

	public void setLimitePerda(float lim) {
		this.limiteperda = lim;
	}

	/**
	 * 
	 * @param number
	 * @return zero se o numero nao � impar, 1 se for.
	 */
	public int odd(int number) {
		return number % 2;
		/*
		int isodd = 0;
		if (Math.round(number / 2 + 0.00001) == number / 2)
			isodd = 1;
		return isodd;
		*/
	}

	/**
	 * Transforma o valor do qui-quadrado em probabilidade de rejei��o de H0
	 * 
	 * @param chi2
	 *            -valor do qui-quadrado
	 * @param df -
	 *            graus de liberdade
	 * @return valor do p
	 */
	public double c2prob(double chi2, int df) {
		double pval = 0;

		if (chi2 < 1000) {
			int add = 0;
			double mult = 1;
			if (odd(df) < 0.5) {
				add = 1;
				mult = Math.sqrt(2 / chi2 / Math.PI);
			}
			float denominator = 1;
			int index;
			for (index = df; index > 1; index -= 2)
				denominator = denominator * index;
			double numerator = Math.pow(chi2, ((df * 1 + add * 1) / 2))
					* Math.exp(chi2 * -1 / 2);
			double sum = 1;
			double m = 1;
			int count;
			for (count = df * 1 + 2 * 1; m > 0.00000000001; count += 2) {
				m = (m * chi2) / count;
				sum = sum + m;
			}
			pval = 1 - sum * mult * numerator / denominator;
		}
		return pval;
	}

	/**
	 * Calcula o escore da variavel var1 se concatenada entre e1 e e2. Para o
	 * calculo do escore de e1 apenas basta usar o mesmo e1 para e2.
	 * 
	 * @param var1
	 *            Vari�vel a ser discretizada
	 * @param e1
	 *            estado 1 da variavel var1
	 * @param e2
	 *            estado 2 da variavel var 1
	 * @param var2
	 *            vari�vel limitante
	 * @param estadosv2
	 *            n�mero de estados de var2
	 * @return Escore (nao normalizado)
	 */
	public float score(int var1, int e1, int e2, int var2, int estadosv2) {
		int i, j;
		float score = 0;
		float tempscore = 0;
		for (i = 0; i < estadosv2 - 1; i++) {
			for (j = i + 1; j < estadosv2; j++) {
				tempscore = countc(var1, e1, e2, var2, i)
						- countc(var1, e1, e2, var2, j);
				if (tempscore < 0)
					tempscore = tempscore * (-1);
				score = score + tempscore;
			}
		}

		return score;
	}

	public double c2() {
		double result = 0;
		double partial = 0;
		double eij = 0;
		int i, j;
		for (i = 0; i < mlines; i++) {
			for (j = 0; j < mcolumns; j++) {
				eij = (margincolumn[i] * marginline[j]) / total;// (ni*nj)/n
				partial = (matrix1[i][j] - (eij * eij)) / eij;
				result = result + partial;
			}
			this.c2p = c2prob(result, ((mlines - 1) * (mcolumns - 1)));
		}

		return result;
	}

	public void contaciclos() {
		totalciclos = 0;
		nvar = variables.size();
		var2 = 0;
		for (var1 = 0; var1 < nvar; var1++) {
			nv1 = variables.get(var1).getStatesSize();
			for (e1 = 0; e1 < nv1 - 1; e1++) {
				for (var2 = 0; var2 < nvar; var2++) {
					if (var2 != var1)
						totalciclos++;
				}
			}

		}

	}

	public int doonce() {
		progresso = 0;
		nvar = variables.size();
		var2 = 0;
		for (int i = 0, k = 0; i < nvar; i++) {
			variables.get(i).atualizatamanhoinfoestados();
			k = variables.get(i).getStatesSize();
			for (int j = 0; j < k; j++) {
				k = k + 0; // o que eh isso?
				variables.get(i).infoestados[j] = 0;
			}
		}

		for (var1 = 0; var1 < nvar; var1++) {
			nv1 = variables.get(var1).getStatesSize();

			for (e1 = 0; e1 < nv1 - 1; e1++) {
				e2 = e1 + 1;
				continua = true;
				for (var2 = 0; var2 < nvar; var2++) {

					if (var2 == var1) {
						var2++;
						progresso++;
					}
					progresso++;
					if (var2 < nvar && continua) {
						// if(var2==var1)var2++;

						nv2 = variables.get(var2).getStatesSize();

						// if(var2==var1)var2++;
						matrix1 = new int[2][nv2];
						matrix1 = getcrosstable2(var1, e1, e2, var2, nv2);
						float score1, score2, score12, perda;
						score1 = score(var1, e1, e1, var2, nv2);
						score2 = score(var1, e2, e2, var2, nv2);
						score12 = score(var1, e1, e2, var2, nv2);

						if (dowh) {
							if (!pesogeral) {
								perda = (score1 + score2 - score12)
										/ (float) total;

							} else {
								perda = (score1 + score2 - score12)
										/ (float) total;

							}
						} else
							perda = score1 / ((float) total) + score2
									/ ((float) total) - score12
									/ ((float) total);
						if (perda > limiteperda) {
							continua = false;

							if (variables.get(var1).infoestados[e1] == 0)
								variables.get(var1).infoestados[e1] = 1;
						} else {

						}// else perda

						controlador.mensagem(String.valueOf(progresso) + "/"
								+ String.valueOf(totalciclos)
								+ " Concatenadas: "
								+ String.valueOf(this.resultado));
						controlador.repaint();
					}// while v2

				}
			}// todos os estados de var1 j� foram aglomerados, quando
			// possivel
		}// for var1
		concatena2();
		System.out.println("Limite de perda = " + limiteperda);
		String bbuf = "";
		LearningNode auxx;
		for (int i = 0; i < nvar; i++) {
			auxx = (LearningNode) variables.get(i);
			if (!auxx.getRep()) {
				for (int j = 0; j < variables.get(i).getStatesSize(); j++) {
					bbuf = variables.get(i).getStateAt(j);
					if ((bbuf.charAt(0) != 'D') || (bbuf.charAt(1) != 'e')) {
						bbuf = "De" + bbuf;
						variables.get(i).setStateAt(bbuf, j);
					}
					System.out.println(variables.get(i).getName() + ": "
							+ variables.get(i).getStateAt(j));
				}
			}
		}
		return resultado;
	}// doonce

	public void concatena2() {
		nvar = variables.size();
		int j;
		int nest;

		for (int i = 0; i < nvar; i++) {
			j = 0;
			nest = variables.get(i).getStatesSize() - 1;
			while (j < nest) {
				if (variables.get(i).infoestados[j] == 0) {
					concatena(i, (byte) j);
					nest--;
				}
				j++;
			}

		}

	}

	public void concatena(int var, byte estado) {
		int i;
		String novonome;
		if (estado < variables.get(var).getStatesSize() - 1) {
			novonome = parte(variables.get(var).getStateAt(estado), 0) + "_"
					+ parte(variables.get(var).getStateAt(estado + 1), 1);

			// variables.setnodestateat(var, novonome, estado);
			variables.get(var).setStateAt(novonome, estado);

			System.out.println(variables.get(var).getName() + ": " + novonome);

			for (i = 0; i < mlines; i++) {
				if (originalmatrix[i][var] > (estado))
					originalmatrix[i][var]--;
			}

			variables.get(var).removeStateAt(estado + 1);
		}

	}

	public String parte(String nome, int p) {
		int tam = nome.length();
		int enc = 0, pos = 0;
		String resultado = "";
		if (p == 0) {
			for (int i = 0; i < tam; i++) {
				if ((enc == 0) && (nome.charAt(i) == '_')) {
					enc = 1;
					pos = i;
				}
			}
			if (enc == 1) {
				for (int i = 0; i < pos; i++)
					resultado = resultado + nome.charAt(i);
			} else {
				resultado = nome;
			}

		} else {
			for (int i = tam - 1; i > -1; i--) {
				if ((enc == 0) && (nome.charAt(i) == '_')) {
					enc = 1;
					pos = i;
				}
			}
			if (enc == 1) {
				for (int i = pos + 1; i < tam; i++)
					resultado = resultado + nome.charAt(i);
			} else
				resultado = nome;

		}
		return resultado;
	}

	public void start() {
		// int resp=1;
		// while(resp>0){
		contaciclos();
		final SwingWorker worker = new SwingWorker() {
			public Object construct() {
				// int resp2;
				doonce();
				return 0;
			}

			public void finished() {
				controlador.mensagem("acabou");
				controlador.repaint();
				controlador.setdalgoresp(variables, mlines, originalmatrix);
			}
		};
		worker.start();

		controlador.matriz = this.originalmatrix;
		controlador.variaveis = this.variables;
		// }

	}

	// public void finished
}// obj
