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
package unbbayes.learning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

import unbbayes.prs.Node;
import unbbayes.prs.bn.LearningNode;

/**
 * CBL - Chang, Bell, Liu
 *
 */
public abstract class CBLToolkit extends LearningToolkit {

	protected ArrayList<Node> variablesVector;
	protected double epsilon;
	protected ArrayList<int[]> es;
	protected ArrayList<Object[]> separators;

	protected double mutualInformation(LearningNode xi, LearningNode xk) {
		double pi, pk, nt = 0, f;
		double pik = 0;
		int ri = xi.getEstadoTamanho();
		int rk = xk.getEstadoTamanho();
		List linhasFaltantes = new ArrayList();
		Map<Integer, FrequencyNode> map = new HashMap<Integer, FrequencyNode>();
		FrequencyNode fNode = new FrequencyNode(ri, rk);
		map.put(new Integer(0), fNode);
		nt = getFullSufficientStatistics(xi.getPos(), xk.getPos(), new ArrayList(), map, nt, linhasFaltantes);
		if (linhasFaltantes.size() > 0) {
			Map mapMissing = new HashMap();
			for (int i = 0; i < linhasFaltantes.size(); i++) {
				int line = ((Integer) linhasFaltantes.get(i)).intValue();
				FrequencyNode fMissing = getFrequencyNode(mapMissing, ri, rk, 0);
				int freq = compacted ? vector[line] : 1;				
				completeFrequencyNode(xi.getPos(), xk.getPos(), freq, fNode, line);
				nt += freq;
			}
			map = getSufficientStatistics(xi.getPos(), xk.getPos(), map, mapMissing);
		}
		fNode = (FrequencyNode)map.get(new Integer(0));
		return getMutualInformation(nt, ri, rk, fNode);
	}

	private double getMutualInformation(double nt, int ri, int rk, FrequencyNode fNode) {
		double pi;
		double pk;
		double pik;
		double im = 0;
		for (int il = 0; il < ri; il++) {
			pi = (1 + fNode.getNi(il)) / ((double) ri + nt);
			for (int kl = 0; kl < rk; kl++) {
				pk = (1 + fNode.getNk(kl)) / ((double) rk + nt);
				pik = (1 + fNode.getNik(il,kl)) / ((double) (ri * rk) + nt);
				im += pik * (log2(pik) - log2(pi) - log2(pk));
			}
		}
		return im;
	}
	
	// this is the old code, and was substituted by the 2 methods above
//	protected double mutualInformation(LearningNode xi, LearningNode xk) {
//		int nt = 0;
//		int il = 0;
//		int kl = 0;
//		double im = 0;
//		double pik = 0;
//		int ri = xi.getEstadoTamanho();
//		int rk = xk.getEstadoTamanho();
//		int nik[][] = new int[ri][rk];
//		int ni[] = new int[ri];
//		int nk[] = new int[rk];
//		int f = 0;
//		double pi, pk;
//		for (int ic = 0; ic < caseNumber; ic++) {
//			f = compacted ? vector[ic] : 1;
//			il = dataBase[ic][xi.getPos()];
//			kl = dataBase[ic][xk.getPos()];
//			nik[il][kl] += f;
//			ni[il] += f;
//			nk[kl] += f;
//			nt += f;
//		}
//		for (il = 0; il < ri; il++) {
//			pi = (1 + ni[il]) / ((double) ri + nt);
//			for (kl = 0; kl < rk; kl++) {
//				pk = (1 + nk[kl]) / ((double) rk + nt);
//				pik = (1 + nik[il][kl]) / ((double) (ri * rk) + nt);
//				im += pik * (log2(pik) - log2(pi) - log2(pk));
//			}
//		}
//		return im;
//	}

	protected void sort(ArrayList<double[]> ls) {
		double[] peace;
		double[] peace2;
		for (int i = 0; i < ls.size(); i++) {
			peace = (double[]) ls.get(i);
			for (int j = i + 1; j < ls.size(); j++) {
				peace2 = (double[]) ls.get(j);
				if (peace[0] < peace2[0]) {
					ls.add(i, peace2);
					ls.remove(i + 1);
					ls.add(j, peace);
					ls.remove(j + 1);
					i = i - 1;
					break;
				}
			}
		}
	}

	protected boolean isOpenWays(int v1, int v2, ArrayList<int[]> esx) {
		Stack<ArrayList<Integer>> stack = new Stack<ArrayList<Integer>>();
		ArrayList<Integer> peace = new ArrayList<Integer>();
		/* Inicializa o primeiro elemento da pilha [-1,v1] */
		peace.add(new Integer(-1));
		peace.add(new Integer(v1));
		stack.push(peace);
		ArrayList<Integer> parents;
		ArrayList<ArrayList<Integer>> list;
		int u;
		while (!stack.empty()) {
			parents = stack.pop();
			u = ((Integer) head(tail(parents))).intValue();
			if (u == v2) {
				return true;
			} else {
				list = expandOpens(parents, esx);
				for (int i = 0; i < list.size(); i++) {
					stack.push(list.get(i));
				}
			}
		}
		return false;
	}

	protected Object head(ArrayList list) {
		return list.get(0);
	}

	protected ArrayList tail(ArrayList list) {
		ArrayList auxList = (ArrayList) list.clone();
		auxList.remove(0);
		return auxList;
	}

	protected ArrayList<ArrayList<Integer>> expandOpens(
			ArrayList<Integer> parents, ArrayList<int[]> esx) {
		Integer direction = (Integer) parents.get(0);
		parents.remove(0);
		Integer u = (Integer) head(parents);
		ArrayList as = (ArrayList) esx.clone();
		ArrayList<ArrayList<Integer>> fs = new ArrayList<ArrayList<Integer>>();
		ArrayList<Integer> way = new ArrayList<Integer>();
		while (as.size() > 0) {
			int[] ab = (int[]) as.get(0);
			as.remove(0);
			if (ab[0] == u.intValue()) {
				way.add(new Integer(1));
				way.add(new Integer(ab[1]));
				for (int i = 0; i < parents.size(); i++) {
					way.add(parents.get(i));
				}
				fs.add((ArrayList<Integer>) way.clone());
			} else if (ab[1] == u.intValue() && direction.intValue() < 0) {
				way.add(new Integer(-1));
				way.add(new Integer(ab[0]));
				for (int i = 0; i < parents.size(); i++) {
					way.add(parents.get(i));
				}
				fs.add((ArrayList<Integer>) way.clone());
			}
			way.clear();
		}
		return fs;
	}

	protected boolean needConnect(int v1, int v2, ArrayList<int[]> esx, int type) {
		// System.out.println("Inicia precisa conectar");
		int n = this.variablesVector.size();
		ArrayList Z;
		double m;
		// System.out.println("Inicia Separador");
		Z = separator(v1, v2, esx, n, type);
		// System.out.println("Acaba Separador");
//		System.out.println("Inicia IMC");
		m = conditionalMutualInformation(v1, v2, Z);
//		System.out.println("Acaba IMC");
		if (m < epsilon) {
			Object[] sep = new Object[2];
			sep[0] = new int[] { v1, v2 };
			sep[1] = Z;
			separators.add(sep);
			return false;
		}
		while (Z.size() > 1) {
			int k = 0;
			double arrayM[] = new double[Z.size()];
			double min = Double.MAX_VALUE;
			for (int i = 0; i < Z.size(); i++) {
				ArrayList ZAux = (ArrayList) Z.clone();
				ZAux.remove(i);
				m = conditionalMutualInformation(v1, v2, ZAux);
				arrayM[i] = m;
				if (m < min) {
					min = m;
					k = i;
				}
			}
			if (min < epsilon) {
				Object[] sep = new Object[2];
				sep[0] = new int[] { v1, v2 };
				Z.remove(k);
				sep[1] = Z;
				separators.add(sep);
				return false;
			}
			if (min > epsilon) {
				return true;
			} else {
				m = arrayM[k];
				Z.remove(k);
			}
		}
//		System.out.println("Acaba precisa conectar");
		return true;
	}

	protected boolean needConnect(int v1, int v2, ArrayList esx, int type,
			int classex) {
		// System.out.println("Inicia precisa conectar");
		int n = this.variablesVector.size();
		ArrayList Z;
		double m;
		// System.out.println("Inicia Separador");
		Z = separator(v1, v2, esx, n, type);
		// System.out.println("Acaba Separador");
//		System.out.println("Inicia IMC");
		m = conditionalMutualInformation(v1, v2, Z, classex);
//		System.out.println("Acaba IMC");
		if (m < epsilon) {
			Object[] sep = new Object[2];
			sep[0] = new int[] { v1, v2 };
			sep[1] = Z;
			separators.add(sep);
			return false;
		}
		while (Z.size() > 1) {
			int k = 0;
			double arrayM[] = new double[Z.size()];
			double min = Double.MAX_VALUE;
			for (int i = 0; i < Z.size(); i++) {
				ArrayList ZAux = (ArrayList) Z.clone();
				ZAux.remove(i);
				m = conditionalMutualInformation(v1, v2, ZAux, classex);
				arrayM[i] = m;
				if (m < min) {
					min = m;
					k = i;
				}
			}
			if (min < epsilon) {
				Object[] sep = new Object[2];
				sep[0] = new int[] { v1, v2 };
				Z.remove(k);
				sep[1] = Z;
				separators.add(sep);
				return false;
			}
			if (min > epsilon) {
				return true;
			} else {
				m = arrayM[k];
				Z.remove(k);
			}
		}
//		System.out.println("Acaba precisa conectar");
		return true;
	}

	protected ArrayList separator(int v1, int v2, ArrayList<int[]> esx, int n,
			int type) {
		ArrayList<int[]> esAnc;
		ArrayList<int[]> esAncMor;
		if (type == 0) {
			esAnc = findForeSubgraph(v1, v2, esx);
			esAncMor = esAnc;
			// esAncMor = moralize(esAnc);
			/*
			 * int[] peace; for(int i = 0 ; i < esAncMor.size(); i++){ peace =
			 * (int[])esAncMor.get(i); System.out.println("Nó 1 = "+ peace[0] + "
			 * Nó 2 = "+ peace[1]); }
			 */
		} else {
			esAncMor = (ArrayList) esx.clone();
		}
		return getSep(esAncMor, v1, v2);
	}

	protected ArrayList<int[]> findForeSubgraph(int v1, int v2,
			ArrayList<int[]> esx) {
		ArrayList<int[]> esAnc = new ArrayList<int[]>();
		boolean[] fore1 = new boolean[this.variablesVector.size()];
		boolean[] fore2 = new boolean[this.variablesVector.size()];
		int[] peace;
		findForefathers(v1, fore1);
		findForefathers(v2, fore2);
		for (int i = 0; i < fore1.length; i++) {
			if (fore1[i] || fore2[i]) {
				fore1[i] = true;
			}
		}
		for (int j = 0; j < esx.size(); j++) {
			peace = (int[]) esx.get(j);
			if (fore1[peace[0]] && fore1[peace[1]]) {
				esAnc.add(new int[] { peace[0], peace[1] });
			}
		}
		return esAnc;
	}

	protected void findForefathers(int v, boolean[] fore) {
		fore[v] = true;
		int[] peace;
		for (int i = 0; i < es.size(); i++) {
			peace = (int[]) es.get(i);
			if (peace[1] == v) {
				findForefathers(peace[0], fore);
			}
		}
	}

	protected ArrayList<int[]> moralize(ArrayList<int[]> esAnc) {
		ArrayList<int[]> esAncMor = (ArrayList) esAnc.clone();
		int[] peace;
		int[] peace2;
		for (int i = 0; i < esAnc.size(); i++) {
			peace = (int[]) esAnc.get(i);
			for (int j = 0; j < esAnc.size() && j != i; j++) {
				peace2 = (int[]) esAnc.get(j);
				if (peace2[1] == peace[1]) {
					esAncMor.add(new int[] { peace2[0], peace[0] });
				}
			}
		}
		return esAncMor;
	}

	protected ArrayList getSep(ArrayList<int[]> esAncMor, int v1, int v2) {
		ArrayList<Object> sep = new ArrayList<Object>();
		ArrayList<Object> sep2 = new ArrayList<Object>();
		/*
		 * é usada uma estrutura de Set para que não aja elementos repetidos
		 */
		TreeSet<Integer> nn1 = new TreeSet<Integer>();
		TreeSet<Integer> nn2 = new TreeSet<Integer>();
		ArrayList<Integer> nnAux;
		// System.out.println("Inicia pegar os separadores");
		// System.out.println("Inicia Achar caminhos");
		ArrayList ways = findWays(v1, v2, esAncMor);
		// System.out.println("Acaba Achar caminhos");
		ArrayList<Integer> neighbors1 = findNeighbors(v1, esAncMor);
		ArrayList<Integer> neighbors2 = findNeighbors(v2, esAncMor);
		// neighborsFilter(neighbors1, ways);
		// neighborsFilter(neighbors2, ways);
		/* Acha os vizinhos dos vizinhos */
		for (int i = 0; i < neighbors1.size(); i++) {
			nnAux = findNeighbors(((Integer) neighbors1.get(i)).intValue(),
					esAncMor);
			for (int j = 0; j < nnAux.size(); j++) {
				if (((Integer) nnAux.get(j)).intValue() != v2
						&& ((Integer) nnAux.get(j)).intValue() != v1) {
					nn1.add(nnAux.get(j));
				}
			}
			nnAux.clear();
		}
		Object[] a = nn1.toArray();
		for (int i = 0; i < a.length; i++) {
			sep.add(a[i]);
		}
		// neighborsFilter(sep,ways);
		/* Faz a uniao dos vizinhos, com os vizinhos dos vizinhos */
		boolean flag = false;
		for (int i = 0; i < neighbors1.size(); i++) {
			for (int j = 0; j < sep.size(); j++) {
				if (neighbors1.get(i).equals(sep.get(j))) {
					flag = true;
					break;
				}
			}
			if (!flag) {
				sep.add(neighbors1.get(i));
			}
			flag = false;
		}
		for (int i = 0; i < neighbors2.size(); i++) {
			nnAux = findNeighbors(((Integer) neighbors2.get(i)).intValue(),
					esAncMor);
			for (int j = 0; j < nnAux.size(); j++) {
				if (((Integer) nnAux.get(j)).intValue() != v2
						&& ((Integer) nnAux.get(j)).intValue() != v1) {
					nn2.add(nnAux.get(j));
				}
			}
			nnAux.clear();
		}
		a = nn2.toArray();
		for (int i = 0; i < a.length; i++) {
			sep2.add(a[i]);
		}
		// neighborsFilter(sep2,ways);
		for (int i = 0; i < neighbors2.size(); i++) {
			for (int j = 0; j < sep2.size(); j++) {
				if (neighbors2.get(i).equals(sep2.get(j))) {
					flag = true;
				}
			}
			if (!flag) {
				sep2.add(neighbors2.get(i));
			}
			flag = false;
		}
		// System.out.println("Acaba pegar os separadores");
		if (sep.size() < sep2.size()) {
			return sep;
		} else {
			return sep2;
		}
	}

	protected ArrayList<ArrayList<Integer>> findWays(int v1, int v2,
			ArrayList<int[]> esAncMor) {
		ArrayList<ArrayList<Integer>> rs = new ArrayList<ArrayList<Integer>>();
		ArrayList<Integer> cs = new ArrayList<Integer>();
		ArrayList<Object> queue = new ArrayList<Object>();
		ArrayList<ArrayList<Integer>> fs;
		cs.add(new Integer(v2));
		queue.add(cs.clone());
		Integer last;
		while (queue.size() > 0) {
			cs = (ArrayList) queue.get(0);
			queue.remove(0);
			last = (Integer) cs.get(cs.size() - 1);
			if (last.intValue() == v1) {
				rs.add(cs);
			} else {
				fs = expand(cs, esAncMor);
				for (int j = 0; j < fs.size(); j++) {
					queue.add(fs.get(j));
				}
			}
		}
		return rs;
	}
	
	protected boolean isWays(int v1, int v2, ArrayList esAncMor) {
		ArrayList cs = new ArrayList();
		ArrayList queue = new ArrayList();
		ArrayList fs;
		cs.add(new Integer(v2));
		queue.add(cs.clone());
		Integer last;
		while (queue.size() > 0) {
			cs = (ArrayList) queue.get(0);
			queue.remove(0);
			last = (Integer) cs.get(cs.size() - 1);
			if (last.intValue() == v1) {
				return true;
			} else {
				fs = expand(cs, esAncMor);
				for (int j = 0; j < fs.size(); j++) {
					queue.add(fs.get(j));
				}
			}
		}
//		System.out.println("NAO EXISTE");
		return false;
	}

	protected ArrayList<ArrayList<Integer>> expand(ArrayList<Integer> cs,
			ArrayList<int[]> esAncMor) {
		ArrayList<ArrayList<Integer>> fs = new ArrayList<ArrayList<Integer>>();
		int[] peace;
		Integer x;
		Integer y;
		for (int i = 0; i < esAncMor.size(); i++) {
			peace = (int[]) esAncMor.get(i);
			if (peace[0] == ((Integer) cs.get(cs.size() - 1)).intValue()
					&& !isMember(cs, peace[1])) {
				ArrayList<Integer> csClone = (ArrayList<Integer>) cs.clone();
				csClone.add(new Integer(peace[1]));
				fs.add(csClone);
			} else if (peace[1] == ((Integer) cs.get(cs.size() - 1)).intValue()
					&& !isMember(cs, peace[0])) {
				ArrayList<Integer> csClone = (ArrayList<Integer>) cs.clone();
				csClone.add(new Integer(peace[0]));
				fs.add(csClone);
			}
		}
		return fs;
	}

	protected boolean isMember(ArrayList cs, int x) {
		Integer c;
		for (int i = 0; i < cs.size(); i++) {
			c = (Integer) cs.get(i);
			if (c.intValue() == x) {
				return true;
			}
		}
		return false;
	}

	protected ArrayList<Integer> findNeighbors(int v1, ArrayList<int[]> esAncMor) {
		ArrayList<Integer> neighbors = new ArrayList<Integer>();
		int[] peace;
		for (int i = 0; i < esAncMor.size(); i++) {
			peace = (int[]) esAncMor.get(i);
			if (peace[0] == v1) {
				neighbors.add(new Integer(peace[1]));
			} else if (peace[1] == v1) {
				neighbors.add(new Integer(peace[0]));
			}
		}
		return neighbors;
	}

	protected void neighborsFilter(ArrayList neighbors, ArrayList ways) {
		ArrayList way;
		Integer step;
		Integer nei;
		boolean flag = false;
		;
		ArrayList neighborAux = new ArrayList();
		boolean[] neighbor = new boolean[neighbors.size()];
		for (int i = 0; i < neighbors.size(); i++) {
			nei = (Integer) neighbors.get(i);
			for (int j = 0; j < ways.size() && !neighbor[i]; j++) {
				way = (ArrayList) ways.get(j);
				for (int k = 0; k < way.size() && !neighbor[i]; k++) {
					step = (Integer) way.get(k);
					if (step.intValue() == nei.intValue()) {
						flag = true;
					}
				}
			}
			if (!flag) {
				neighbors.remove(i);
				i--;
			}
			flag = false;
		}
	}

	protected double mutualInformation(int v1, int v2, int classe) {

		int ri = ((LearningNode) variablesVector.get(v1)).getEstadoTamanho();
		int rk = ((LearningNode) variablesVector.get(v2)).getEstadoTamanho();
		int rj = ((LearningNode) variablesVector.get(classe)).getEstadoTamanho();
		double pjik;
		double cpjik;
		double im = 0.0;
		int[] nj = new int[rj];
		int[][][] njik = new int[rj][ri][rk];
		int[][] nji = new int[rj][ri];
		int[][] njk = new int[rj][rk];
		double[][] pji = new double[rj][ri];
		double[][] pjk = new double[rj][rk];
		int j = 0;
		int f;
		int il;
		int kl;
		int nt = 0;
		for (int id = 0; id < caseNumber; id++) {
			f = compacted ? vector[id] : 1;
			il = dataBase[id][v1];
			kl = dataBase[id][v2];
			j = dataBase[id][classe];
			njik[j][il][kl] += f;
			nji[j][il] += f;
			njk[j][kl] += f;
			nj[j] += f;
			nt += f;
		}
		for (j = 0; j < rj; j++) {
			for (il = 0; il < ri; il++) {
				pji[j][il] = (1 + nji[j][il]) / (double) (ri + nj[j]);
			}
			for (kl = 0; kl < rk; kl++) {
				pjk[j][kl] = (1 + njk[j][kl]) / (double) (rk + nj[j]);
			}
			for (il = 0; il < ri; il++) {
				for (kl = 0; kl < rk; kl++) {
					pjik = (1 + njik[j][il][kl]) / (double) (ri * rk * rj + nt);
					cpjik = (1 + njik[j][il][kl]) / (double) (ri * rk + nj[j]);
					im += pjik
							* (log2(cpjik) - log2(pji[j][il]) - log2(pjk[j][kl]));
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

	protected double conditionalMutualInformation1(int v1, int v2, ArrayList sep) {
		int qj = getQ(sep);
		if (qj == 0) {
//			System.out.println("SAIIIIIIIIII");
			return mutualInformation((LearningNode) variablesVector.get(v1),
					(LearningNode) variablesVector.get(v2));
		}
		;
		int ri = ((LearningNode) variablesVector.get(v1)).getEstadoTamanho();
		int rk = ((LearningNode) variablesVector.get(v2)).getEstadoTamanho();
		double pjik;
		double cpjik;
		double im = 0.0;
		int[] nj = new int[qj];
		int[][][] njik = new int[qj][ri][rk];
		int[][] nji = new int[qj][ri];
		int[][] njk = new int[qj][rk];
//		System.out.println("ENTROUUUUUUUUUU = " + qj + "   SIZE =  "
//				+ sep.size());
		double[][] pji = new double[qj][ri];
		double[][] pjk = new double[qj][rk];
		int[] mult = multipliers(sep);
		int j = 0;
		int f;
		int il;
		int kl;
		int nt = 0;
		for (int id = 0; id < caseNumber; id++) {
			f = compacted ? vector[id] : 1;
			j = findJ(sep, id, mult);
			il = dataBase[id][v1];
			kl = dataBase[id][v2];
			njik[j][il][kl] += f;
			nji[j][il] += f;
			njk[j][kl] += f;
			nj[j] += f;
			nt += f;
		}
//		System.out.println("SAIIIIIIIIII");
		for (j = 0; j < qj; j++) {
			for (il = 0; il < ri; il++) {
				pji[j][il] = (1 + nji[j][il]) / (double) (ri + nj[j]);
			}
			for (kl = 0; kl < rk; kl++) {
				pjk[j][kl] = (1 + njk[j][kl]) / (double) (rk + nj[j]);
			}
			for (il = 0; il < ri; il++) {
				for (kl = 0; kl < rk; kl++) {
					pjik = (1 + njik[j][il][kl]) / (double) (ri * rk * qj + nt);
					cpjik = (1 + njik[j][il][kl]) / (double) (ri * rk + nj[j]);
					im += pjik
							* (log2(cpjik) - log2(pji[j][il]) - log2(pjk[j][kl]));
				}
			}
		}
		nj = null;
		njk = null;
		nji = null;
		njik = null;
		mult = null;
		pji = null;
		pjk = null;
		return im;
	}


	/**
	 * This method calculates the mutual information between two variables, given a 
	 * set of separators.
	 * @param v1
	 * @param v2
	 * @param sep
	 * @return
	 */
	protected double conditionalMutualInformation(int v1, int v2, ArrayList sep) {
		Map njik = new HashMap();
		Map njikMissing = new HashMap();
		int qj = getQ(sep);
		if (qj == 0) {
			return mutualInformation((LearningNode) variablesVector.get(v1), (LearningNode) variablesVector.get(v2));
		}
		int ri = ((LearningNode) variablesVector.get(v1)).getEstadoTamanho();
		int rk = ((LearningNode) variablesVector.get(v2)).getEstadoTamanho();
		double pjik, cpjik, im = 0.0, pi, pk = 0.0;
		/* f  significa quantas vezes um determinado caso se repete
		 * il estado da variável 1 em algum caso
		 * kl estado da variável 2 em algum caso
		 * nt somátorio das frequências f		   
		 */
		int j, il = 0, kl = 0;
		double f, nt = 0.0d;
		//		Array que verifica as linhas aonde faltam algum valor
		List missingLines = new ArrayList();
		nt = getFullSufficientStatistics(v1, v2, sep, njik, nt, missingLines);
		if (missingLines.size() > 0) {
			//Vetor que contém o número de  estados de cada nó do conjunto separador			
			int stateVector[] = new int[sep.size()];
			//Vetor que contém as posições reais das variáveis do conjunto separador
			int positionVector[] = new int[stateVector.length];
			//Inicialização do vetor de posições, do vetor de estado e do vetor de número de estados
			initializeVectors(sep, stateVector, positionVector);
			/*Inicializa procedimento de complementação, fase da ESPERANÇA(E-Step)*/
			for (int i = 0; i < missingLines.size(); i++) {
				/*Linha atual do banco de casos*/
				int line = ((Integer) missingLines.get(i)).intValue();
				/*Array que contem o estado do conjuto separador da variáveis no caso da linha line*/
				int[] missingVector = getMissingVector(positionVector, line);
				/*Array que contem os números de estados onde existe valores faltantes*/
				List stateMissingVector = getStateMissingVector(stateVector, line, missingVector);
				/*Array que contem as possíveis instâncias baseado nos valores faltantes do caso*/
				List instances = getInstances(stateMissingVector);
				int freq = compacted ? vector[line] : 1;
				for (int k = 0; k < instances.size(); k++) {
					j = getJ(stateVector, missingVector, instances, k);
					f = (double) freq / instances.size();
					FrequencyNode fn = getFrequencyNode(njikMissing, ri, rk, j);
					completeFrequencyNode(v1, v2, f, fn, line);					
				}
				nt+= freq;
			}
			njik = getSufficientStatistics(v1, v2, njik, njikMissing);
		}

		return getConditionalMutualInformation(njik, qj, ri, rk, im, nt);

	}

	private double getFullSufficientStatistics(int v1, int v2, ArrayList sep, Map njik, double nt, List linhasFaltantes) {
		int j;
		int il;
		int kl;
		double f;
		//Array de multiplicadores baseado no conjunto separador para o calculo direto do J*/
		int[] mult = multipliers(sep);
		for (int i = 0; i < caseNumber; i++) {
			f = compacted ? vector[i] : 1;
			j = findJ(sep, i, mult);
			il = dataBase[i][v1];
			kl = dataBase[i][v2];
			if (j == -1 || il == -1 || kl == -1) {
				linhasFaltantes.add(new Integer(i));
				continue;
			}
			FrequencyNode fn = getFrequencyNode(njik, variablesVector.get(v1).getStatesSize(), variablesVector.get(v2).getStatesSize(), j);
			fn.addFrequency(il, kl, f);
			nt += f;
		}
		return nt;
	}

	protected void initializeVectors(ArrayList sep, int[] stateVector, int[] positionVector) {
		LearningNode aux;
		for (int i = 0; i < sep.size(); i++) {
			aux = (LearningNode) variablesVector.get(((Integer) sep.get(i)).intValue());
			positionVector[i] = (byte) aux.getPos();
			stateVector[i] = aux.getEstadoTamanho();
		}
	}

	protected void completeFrequencyNode(int v1, int v2, double f, FrequencyNode fn, int line) {
		int ri = variablesVector.get(v1).getStatesSize();
		int rk = variablesVector.get(v2).getStatesSize();
		int freq = compacted ? vector[line] : 1;
		if (dataBase[line][v1] == -1) {
			if (dataBase[line][v2] == -1) {
				for (int i = 0; i < rk; i++) {
					fn.addNkFrequency(i, (double) freq / rk);
					for (int j = 0; j < ri; j++) {
						if (i == 0) {
							fn.addNiFrequency(i, (double) freq / ri);
						}
						fn.addNikFrequency(j, i, (double) freq / (ri * rk));
					}
				}
			} else {
				fn.addNkFrequency(dataBase[line][v2], freq);
				for (int i = 0; i < ri; i++) {
					fn.addNiFrequency(i, (double) freq / ri);
					fn.addNikFrequency(i, dataBase[line][v2], (double) freq / ri);
				}
			}
		} else {
			fn.addNiFrequency(dataBase[line][v1], freq);
			if (dataBase[line][v2] == -1) {
				for (int i = 0; i < rk; i++) {
					fn.addNkFrequency(i, (double) freq / rk);
					fn.addNikFrequency(dataBase[line][v1], i, (double) freq / rk);
				}
			} else {
				fn.addNkFrequency(dataBase[line][v2], freq);
				fn.addNikFrequency(dataBase[line][v1], dataBase[line][v2], (double) freq / rk);
			}
		}
		fn.addFFrequency(f);
	}

	protected double getConditionalMutualInformation(Map njik, int qj, int ri, int rk, double im, double nt) {
		double pjik;
		double cpjik;
		double pi;
		double pk;
		int j;
		int il;
		int kl;
		for (il = 0; il < ri; il++) {
			pi = 1 / (double) ri;
			for (kl = 0; kl < rk; kl++) {
				pk = 1 / (double) rk;
				pjik = 1 / (double) (ri * rk * qj + nt);
				cpjik = 1 / (double) (ri * rk);
				im += pjik * (log2(cpjik) - log2(pi) - log2(pk));
			}
		}
		im *= (qj - njik.size() - 1);
		Set nijk = njik.keySet();
		Object[] arrayNijk = nijk.toArray();
		for (int k = 0; k < nijk.size(); k++) {
			j = ((Integer) arrayNijk[k]).intValue();
			for (il = 0; il < ri; il++) {
				pi = (1 + getNji(njik, j, il)) / (double) (ri + getNj(njik, j));
				for (kl = 0; kl < rk; kl++) {
					pk = (1 + getNjk(njik, j, kl)) / (double) (rk + getNj(njik, j));
					pjik = (1 + getNjik(njik, j, il, kl)) / (double) (ri * rk * qj + nt);
					cpjik = (1 + getNjik(njik, j, il, kl)) / (double) (ri * rk + getNj(njik, j));
					im += pjik * (log2(cpjik) - log2(pi) - log2(pk));
				}
			}
		}
		return im;
	}

	private Map getSufficientStatistics(int v1, int v2, Map njik, Map njikMissing) {
		double delta = Math.pow(10, -3);
		int ri = variablesVector.get(v1).getStatesSize();
		int rk = variablesVector.get(v2).getStatesSize();
		double initialProbability;
		Map sumMap = new HashMap();
		createSumMap(njik, njikMissing, sumMap);
		Set sumNijk = sumMap.keySet();
		Object[] arraySumNijk = sumNijk.toArray();
		Set sumMissingNijk = njikMissing.keySet();
		Object[] arraySumMissingNjik = sumMissingNijk.toArray();
		FrequencyNode fNode;
		FrequencyNode fSumNode;
		FrequencyNode fNodeMissing;
		Integer jValue;
		for (int i = 0; i < arraySumNijk.length; i++) {
			jValue = (Integer) arraySumNijk[i];
			fNode = (FrequencyNode) njik.get(jValue);
			fSumNode = (FrequencyNode) sumMap.get(jValue);
			fNodeMissing = (FrequencyNode) njikMissing.get(jValue);
			double sumMissing;
			double nikj;
			emNiVector(delta, ri, fNode, fSumNode, fNodeMissing);
			emNkVector(delta, rk, fNode, fSumNode, fNodeMissing);
			emNikVector(delta, rk, ri, fNode, fSumNode, fNodeMissing);
		}
		return sumMap;
	}

	private void emNikVector(double delta, int ri, int rk, FrequencyNode fNode, FrequencyNode fSumNode, FrequencyNode fNodeMissing) {
		double initialProbability;
		double sumMissing = 0;
		double nikj = 0.0;
		double probability = (double) 1 / ri * rk;
		for (int j = 0; j < ri; j++) {
			for (int k = 0; k < rk; k++) {
				nikj += fSumNode.getNik(j, k);
				sumMissing += fNodeMissing.getNik(j, k);
			}
		}
		do {
			initialProbability = probability;
			for (int j = 0; j < ri; j++) {
				for (int k = 0; k < rk; k++) {
					probability = (double) (1 + fSumNode.getNik(j, k) / (rk + nikj));
					fSumNode.setNikFrequency(j, k, fNode.getNik(j, k) + (sumMissing * probability));
				}
			}
		} while (Math.abs(probability - initialProbability) > delta);
	}

	private void emNkVector(double delta, int rk, FrequencyNode fNode, FrequencyNode fSumNode, FrequencyNode fNodeMissing) {
		double initialProbability;
		double sumMissing = 0;
		double nikj = 0.0;
		double probability = (double) 1 / rk;
		for (int j = 0; j < rk; j++) {
			nikj += fSumNode.getNk(j);
			sumMissing += fNodeMissing.getNk(j);
		}
		do {
			initialProbability = probability;
			for (int j = 0; j < rk; j++) {
				probability = (double) (1 + fSumNode.getNk(j) / (rk + nikj));
				fSumNode.setNkFrequency(j, fNode.getNk(j) + (sumMissing * probability));
			}
		} while (Math.abs(probability - initialProbability) > delta);
	}

	private void emNiVector(double delta, int ri, FrequencyNode fNode, FrequencyNode fSumNode, FrequencyNode fNodeMissing) {
		double initialProbability;
		double sumMissing = 0;
		double nikj = 0.0;
		double probability = (double) 1 / ri;
		for (int j = 0; j < ri; j++) {
			nikj += fSumNode.getNi(j);
			sumMissing += fNodeMissing.getNi(j);
		}
		do {
			initialProbability = probability;
			for (int j = 0; j < ri; j++) {
				probability = (double) (1 + fSumNode.getNi(j) / (ri + nikj));
				fSumNode.setNiFrequency(j, fNode.getNi(j) + (sumMissing * probability));
			}
		} while (Math.abs(probability - initialProbability) > delta);
	}

	protected void createSumMap(Map njik, Map njikMissing, Map sumMap) {
		Set nijk = njik.keySet();
		Object[] arrayNijk = nijk.toArray();
		Integer j;

		for (int k = 0; k < nijk.size(); k++) {
			j = ((Integer) arrayNijk[k]);
			FrequencyNode fFull = (FrequencyNode) njik.get(j);
			FrequencyNode fMissing = (FrequencyNode) njikMissing.get(j);
			sumMap.put(j, sumFrequencyNodes(fFull, fMissing));
		}
	}

	private FrequencyNode sumFrequencyNodes(FrequencyNode f1, FrequencyNode f2) {
		int ri = f1.getRi();
		int rk = f1.getRk();
		FrequencyNode fSum = new FrequencyNode(ri, rk);
		for (int i = 0; i < ri; i++) {
			fSum.addNiFrequency(i, f1.getNi(i) + f2.getNi(i));
			for (int j = 0; j < rk; j++) {
				if (i == 0) {
					fSum.addNiFrequency(i, f1.getNk(j) + f2.getNk(j));
				}
				fSum.addNikFrequency(i, j, f1.getNik(i, j) + f2.getNik(i, j));
			}
		}
		fSum.addFFrequency(f1.getJ() + f2.getJ());
		return fSum;
	}

	private FrequencyNode getFrequencyNode(Map njikMissing, int ri, int rk, int j) {
		FrequencyNode fn;
		if (!njikMissing.containsKey(new Integer(j))) {
			fn = new FrequencyNode(ri, rk);
			njikMissing.put(new Integer(j), fn);
		} else {
			fn = (FrequencyNode) njikMissing.get(new Integer(j));
		}
		return fn;
	}

	private int getJ(int[] stateVector, int[] missingVector, List instances, int k) {
		int j;
		List instance = (List) instances.get(k);
		int cont = 0;
		/*Vetor que deve ser completado baseado na instancia k*/
		int[] completeVector = copy(missingVector);
		/*Verifica as posições aonde o vetor de completude 
		 *possue valores faltantes e as completa com a instancia k
		*/
		for (int l = 0; l < missingVector.length; l++) {
			if (missingVector[l] == -1) {
				completeVector[l] = ((Integer) instance.get(cont)).intValue();
				cont++;
			}
		}
		/*Pega o j de acordo com o vetor de completude e o vetor de estados*/
		j = getJ(stateVector, completeVector);
		return j;
	}

	private double getNj(Map njik, int j) {
		if (njik.containsKey(new Integer(j))) {
			return ((FrequencyNode) njik.get(new Integer(j))).getJ();
		}
		return 0;
	}

	private double getNji(Map njik, int j, int il) {
		if (njik.containsKey(new Integer(j))) {
			return ((FrequencyNode) njik.get(new Integer(j))).getNi(il);
		}
		return 0;
	}

	private double getNjk(Map njik, int j, int kl) {
		if (njik.containsKey(new Integer(j))) {
			return ((FrequencyNode) njik.get(new Integer(j))).getNk(kl);
		}
		return 0;
	}

	private double getNjik(Map njik, int j, int il, int kl) {
		if (njik.containsKey(new Integer(j))) {
			return ((FrequencyNode) njik.get(new Integer(j))).getNik(il, kl);
		}
		return 0;
	}
	

	protected double conditionalMutualInformation(int v1, int v2,
			ArrayList sep, int classex) {
		int qj = getQ(sep);
		if (qj == 0) {
//			System.out.println("SAIIIIIIIIII");
			return mutualInformation(v1, v2, classex);
		}
		;
		int ri = ((LearningNode) variablesVector.get(v1)).getEstadoTamanho();
		int rk = ((LearningNode) variablesVector.get(v2)).getEstadoTamanho();
		double pjik;
		double cpjik;
		double im = 0.0;
		int[] nj = new int[qj];
		int[][][] njik = new int[qj][ri][rk];
		int[][] nji = new int[qj][ri];
		int[][] njk = new int[qj][rk];
//		System.out.println("ENTROUUUUUUUUUU = " + qj + "   SIZE =  "
//				+ sep.size());
		double[][] pji = new double[qj][ri];
		double[][] pjk = new double[qj][rk];
		int[] mult = multipliers(sep);
		int j = 0;
		int f;
		int il;
		int kl;
		int nt = 0;
		for (int id = 0; id < caseNumber; id++) {
			f = compacted ? vector[id] : 1;
			j = findJ(sep, id, mult);
			il = dataBase[id][v1];
			kl = dataBase[id][v2];
			njik[j][il][kl] += f;
			nji[j][il] += f;
			njk[j][kl] += f;
			nj[j] += f;
			nt += f;
		}
//		System.out.println("SAIIIIIIIIII");
		for (j = 0; j < qj; j++) {
			for (il = 0; il < ri; il++) {
				pji[j][il] = (1 + nji[j][il]) / (double) (ri + nj[j]);
			}
			for (kl = 0; kl < rk; kl++) {
				pjk[j][kl] = (1 + njk[j][kl]) / (double) (rk + nj[j]);
			}
			for (il = 0; il < ri; il++) {
				for (kl = 0; kl < rk; kl++) {
					pjik = (1 + njik[j][il][kl]) / (double) (ri * rk * qj + nt);
					cpjik = (1 + njik[j][il][kl]) / (double) (ri * rk + nj[j]);
					im += pjik
							* (log2(cpjik) - log2(pji[j][il]) - log2(pjk[j][kl]));
				}
			}
		}
		nj = null;
		njk = null;
		nji = null;
		njik = null;
		mult = null;
		pji = null;
		pjk = null;
		return im;
	}

	@Override
	protected int getQ(List cc) {
		LearningNode aux;
		int ac = 1;
		for (int i = 0; i < cc.size(); i++) {
			aux = (LearningNode) variablesVector.get(((Integer) cc.get(i))
					.intValue());
			ac *= aux.getEstadoTamanho();
		}
		if (cc.size() == 0) {
			return 0;
		}
		return ac;
	}

	protected int[] multipliers(ArrayList a) {
		LearningNode aux;
		int np = a.size();
		int m = np == 0 ? 1 : np;
		int[] mult = new int[m];
		mult[m - 1] = 1;
		for (int i = m - 2; i >= 0; i--) {
			aux = (LearningNode) variablesVector.get(((Integer) a.get(i + 1))
					.intValue());
			mult[i] = aux.getEstadoTamanho() * mult[i + 1];
		}
		return mult;
	}

	/**
	 * Finds the J value related to the case and a list of separators. 
	 * @param cc
	 * @param id
	 * @param mult
	 * @return -1 if some value is missing.
	 */
	protected int findJ(ArrayList cc, int id, int[] mult) {
		int j = 0;
		int im = 0;
		for (int i = 0; i < cc.size(); i++) {
			j += dataBase[id][((Integer) cc.get(i)).intValue()] * mult[im];
			im++;
		}
		return j;
	}

}
