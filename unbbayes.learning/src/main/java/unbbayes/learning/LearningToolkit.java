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

import unbbayes.io.CountCompatibleNetIO;

import unbbayes.prs.INode;
import unbbayes.prs.Node;
import unbbayes.prs.bn.LearningNode;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.ProbabilisticTable;
import unbbayes.util.SetToolkit;

/**
  * 
  * @author Shou Matsumoto Edited by Young
  * @author Bo
  */
public abstract class LearningToolkit{
	public List<LearningNode> global_variables; 
	public Map<String, LearningNode> data_variables = new HashMap<String, LearningNode>(); 
    
	Map<LearningNode, float[][]> dataBaseForEachRV = new HashMap<LearningNode, float[][]>();

    protected long caseNumber;
    protected int[][] dataBase;
    protected int[] vector;
    protected boolean compacted;
     
    ProbabilisticNetwork emptyNet = null;
    ProbabilisticNetwork learnedNet = null;
    
    /** Error margin used in float comparisons */
	public static final float TABLE_FLOAT_ERROR_MARGIN = 0.0001f;
		
	/**
	 * @deprecated use {@link #getProbability(float[][], LearningNode)} instead
	 */
    protected void getProbability(int[][] arrayNijk, LearningNode variable){
    	float[][] arrayNijkFloat = new float[arrayNijk.length][arrayNijk[0].length];
    	for (int i = 0; i < arrayNijk.length; i++) {
			for (int j = 0; j < arrayNijk[i].length; j++) {
				arrayNijkFloat[i][j] = arrayNijk[i][j];
			}
		}
    	this.getProbability(arrayNijkFloat, variable);
    }
    
    /**
     * This method sets the probability of a given learning node using
     * an array.
     * @param arrayNijk
     * @param variable
     */
	protected void getProbability(float[][] arrayNijk, LearningNode variable){
        List<List<Integer>> instanceVector;
        List<Integer> instance = new ArrayList<>();
        float probability;
        int nij;
        int ri = getStateSize(variable);
        int nijLength  = getTotalParentStateSize(variable.getPais());
        instanceVector  = getInstancesAsNodes(variable.getPais());
        
        for (int i = 0; i < nijLength;i++) {
             nij = 0;
             
             for (int j = 0; j < ri ; j++) {
                nij+= arrayNijk[j][i];
             }
             
             if (instanceVector.size() > 0 ) {
                instance = instanceVector.get(i);
             }
             
             for (int j = 0; j < ri; j++) {
                  probability = (float)(1 + arrayNijk[j][i])/(ri+nij);
                  int coord[];
//                  coord = new int[nijLength+1];
                  coord = new int[instance.size()+1];
                  coord[0] = j;
                  
                  for(int k = 1; k <= instance.size(); k++){
                      coord[k] = ((Integer)instance.get(k-1)).intValue();
                  }
                  
                  variable.getProbabilidades().setValue(coord, probability);
                  // also fill table of counts
                  String tablePropName = CountCompatibleNetIO.DEFAULT_COUNT_TABLE_PREFIX + variable.getName();
                  PotentialTable countTable = (PotentialTable) learnedNet.getProperty(tablePropName );
                  if (countTable == null) {
                	  countTable = new ProbabilisticTable();
                	  for (int varIndex = 0; varIndex < variable.getProbabilidades().getVariablesSize(); varIndex++) {
                		  	INode var = variable.getProbabilidades().getVariableAt(varIndex);
                		  	countTable.addVariable(var);
                	  }
                  }
                  countTable.setValue(coord, arrayNijk[j][i]);
                  learnedNet.addProperty(tablePropName, countTable);
             }
             
             // ajusting probabilities above or below 100% caused by floating point's precision
             this.fixTotalProbability(variable);
        }
    }
	
	
	/**
	 * This method fixes the probability values, if the sum of all possible states
	 * is not equal to 1.
	 * The adjustment is usually done by doing the following procedure:
	 * 		1 - find out the probability sum and get its difference compared to 1;
	 * 		2 - if there were a difference (the sum was not equal to 1), ajust
	 * 			the value of the greatest probability (find out what value in the
	 * 			table has the greatest probability and subtract the difference
	 * 			from it).
	 * 		3 - if the difference was positive
	 * value within the table.
	 * @param variable
	 */
	protected void fixTotalProbability(LearningNode variable) {
		
		PotentialTable table = variable.getProbabilityFunction();	// table to be analyzed
		
		int lineCounter = variable.getStatesSize();	// total numbers of lines
		int columnCounter = 1;						// total numbers of columns in this variable's table
		
		// calculate the number of columns
		int parentSize = variable.getParentNodes().size();
		for (int k = 0; k < parentSize; k++) {
			columnCounter *= variable.getParents().get(k).getStatesSize();
		}
		
		// verify the sum of all probabilities for each column
		for (int j = 0; j < columnCounter; j++) {
			
			float sum = 0;					// sum
			float greatestValue = -1.0f;	// greatest probability value of a column
			int greatestIndex = -1;			// index of the greatest probability value of a column
			
			// calculate the sum and update greatest value/index by iterating on lines
			for (int i = 0; i < lineCounter; i++) {
				int index = j * lineCounter + i;		// get the index of the next line of the same column
				float value = table.getValue(index);	// probability value of the current cell
				sum += value;
				
				if (value > greatestValue) {
					greatestValue = value;
					greatestIndex = index;
				}
			}
			
			float offset = 1.0f - sum;
			if (Math.abs(offset) > TABLE_FLOAT_ERROR_MARGIN) {
//				Debug.println(this.getClass(), "[" + variable.getName() + "] The sum of all probabilities was not exactly 1: obtained = " + sum + ".");
				
				// alter the value of the greatest probability value, using offset
				float oldValue = table.getValue(greatestIndex);
				float newValue = oldValue + offset;
				
				if (newValue >= 0.0f && newValue <= 1.0f) {
//					Debug.println(this.getClass(),"Altering value of index " + greatestIndex + " from " + oldValue + " to " + newValue);
					table.setValue(greatestIndex, newValue);
				} else {
					System.err.println("[" + this.getClass()+"] Offset error on index " + greatestIndex + ". Old value = " + oldValue + ", new value = " + newValue);
				}
			}
		}
	}

	public int getPosInData(LearningNode node){
		LearningNode dataNode = data_variables.get(node.getName());
		return dataNode.getPos();
	}
	
	/**
	 * 
	 * @param variable
	 * @param parents: this is specified as a list of nodes, but this is actually a list
	 * of {@link LearningNode}
	 * @return
	 */
	protected float[][] getFrequencies(LearningNode variable, List<Node> parents) {
		float[][] arrayNijk; 
		int position;
		LearningNode node;

		// get existing data from each RV
//		float[][] existingarrayNijk = dataBaseForEachRV.get(variable);
		 
		{
			if (parents == null) {
				parents = new ArrayList<Node>();
				arrayNijk = new float[getStateSize(variable)][1];
			} else {
				arrayNijk = new float[getStateSize(variable)][getTotalParentStateSize(parents)];
			}
			 
			dataBaseForEachRV.put(variable, arrayNijk);
		} 

		int parentsLength = parents.size();
		int stateVector[] = new int[parentsLength];
		int positionVector[] = new int[parentsLength];
		int maxVector[] = new int[parentsLength];

		for (int i = 0; i < parentsLength; i++) {
			node = (LearningNode) parents.get(i);
			positionVector[i] = (byte) getPosInData(node);
			maxVector[i] =  getStateSize(node);
			stateVector[i] = maxVector[i];
		}
		
		/*Posicao da Variável*/
		int pos = getPosInData(variable);
		int index = 0;
		/*Linhas do arquivo que possuem algum valor faltante ou na variavel ou nos pais dessa*/
		List<Integer> linhasFaltantes = new ArrayList<>();

		for (int i = 0; i < caseNumber; i++) {
			for (int j = positionVector.length - 1; j >= 0; j--) {
				position = positionVector[j];
				if (j != positionVector.length - 1) {
					index += dataBase[i][position] * maxVector[j + 1];
					if (i == 0) {
						maxVector[j] *= maxVector[j + 1];
					}
				} else {
					index = dataBase[i][position];
				}
			}
			if (!isMissingValue(positionVector, pos, i)) {
				if (parentsLength == 0) {
					if (!compacted) {
						arrayNijk[dataBase[i][getPosInData(variable)]][0]++;
					} else {
						arrayNijk[dataBase[i][getPosInData(variable)]][0] += vector[i];
					}
				} else {
					if (!compacted) {
						arrayNijk[dataBase[i][pos]][index]++;
					} else {
						arrayNijk[dataBase[i][pos]][index] += vector[i];
					}
				}
			} else {
				linhasFaltantes.add(new Integer(i));
			}
		}
		/*Se existir linhas faltantes*/
		if (linhasFaltantes.size() > 0) {
			float[][] arrayNijkMissing = null;
//			int[][] vetorRepeticao = null;
			if (parentsLength == 0) {
				arrayNijkMissing = new float[getStateSize(variable)][1];				
			} else {
				arrayNijkMissing = new float[getStateSize(variable)][getTotalParentStateSize(parents)];				
			}
			for (int i = 0; i < linhasFaltantes.size(); i++) {
				int line = ((Integer) linhasFaltantes.get(i)).intValue();
				/*Array que contem o valor dos pais da variável*/
				int[] missingVector = getMissingVector(positionVector, line);
				List<Integer> stateMissingVector = getStateMissingVector(stateVector, line, missingVector);
				List<List<Integer>> instances = getInstances(stateMissingVector);
				for (int j = 0; j < instances.size(); j++) {
					List<Integer> instance = instances.get(j);
					int cont = 0;
					int[] completeVector = copy(missingVector);
					for (int k = 0; k < missingVector.length; k++) {
						if (missingVector[k] == -1) {
							completeVector[k] = ((Integer) instance.get(cont)).intValue();
							cont++;
						}
					}
					int posJ = getJ(stateVector, completeVector);
					if (dataBase[line][getPosInData(variable)] == -1) {
						for (int k = 0; k < getStateSize(variable); k++) {							
							arrayNijkMissing[k][posJ] += (float) 1 / (getStateSize(variable) * instances.size());
						}
					} else {
						arrayNijkMissing[dataBase[line][getPosInData(variable)]][posJ] += (float) 1 / instances.size();						
					}

				}
				if (instances.size() == 0) {
					for (int k = 0; k < getStateSize(variable); k++) {						
						arrayNijkMissing[k][0] += (float) 1 / getStateSize(variable);
					}
				}
			}
			arrayNijk = getProbability(arrayNijkMissing, arrayNijk, variable);
		} 
		
		return arrayNijk;
	}

	protected int[] copy(int[] missingVector) {
		int[] completeVector = new int[missingVector.length];
		for (int i = 0; i < missingVector.length; i++) {
			completeVector[i] = missingVector[i];
		}
		return completeVector;
	}

	protected float[][] getProbability(float[][] arrayNijkMissing, float[][] arrayNijk, LearningNode variable) {
		double delta = Math.pow(10, -3);
		float nij;
		int ri = getStateSize(variable);
		float probability = 1 / ri;
		float initialProbability = 1 / ri;
		int nijLength = arrayNijk[0].length;
		float[][] arraySoma = new float[ri][nijLength];
		float somaMissing = 0;
		for (int i = 0; i < ri; i++) {
			for (int j = 0; j < nijLength; j++) {
				arraySoma[i][j] = arrayNijkMissing[i][j] + arrayNijk[i][j];
			}
		}
		for (int i = 0; i < nijLength; i++) {
			probability = initialProbability = (float) 1 / ri;
			nij = 0;
			somaMissing = 0;
			for (int j = 0; j < ri; j++) {
				nij += arraySoma[j][i];
				somaMissing += arrayNijkMissing[j][i];
			}
			do {
				initialProbability = probability;
				for (int j = 0; j < ri; j++) {
					probability = (float) (1 + arraySoma[j][i]) / (ri + nij);
					arraySoma[j][i] = arrayNijk[j][i] + (somaMissing * probability);
				}
			} while (Math.abs(probability - initialProbability) > delta);

		}
		return arraySoma;
	}

	protected int getJ(int[] stateVector, int[] completeVector) {
		int index = 0;
		int acumulador = 1;
		for (int i = stateVector.length - 1; i >= 0; i--) {
			index += completeVector[i] * acumulador;
			acumulador *= stateVector[i];
		}
		return index;
	}

	protected List<Integer> getStateMissingVector(int[] stateVector, int line, int[] missingVector) {
		List<Integer> stateList = new ArrayList<Integer>();
		for (int i = 0; i < missingVector.length; i++) {
			if (missingVector[i] == -1) {
				stateList.add(new Integer(stateVector[i]));
			}
		}
		return stateList;
	}

	/**
	 * 
	 * @param posVector
	 * @param line
	 * @return int[]
	 */
	protected int[] getMissingVector(int[] posVector, int line) {
		int[] missingVector = new int[posVector.length];
		for (int i = 0; i < posVector.length; i++) {
			missingVector[i] = dataBase[line][posVector[i]];
		}
		return missingVector;
	}

	/**
	 * Verify if is there any missing value on the currenty line of the data base    *
	 * @param parentPos
	 * @param variablePos
	 * @param line
	 * @return boolean
	 */
	protected boolean isMissingValue(int[] parentPos, int variablePos, int line) {
		/*Se a variável tiver valor faltante*/
		if (dataBase[line][variablePos] == -1) {
			return true;
		}
		/*Se algum dos pais da variável tiver valor faltante*/
		for (int i = 0; i < parentPos.length; i++) {
			if (dataBase[line][parentPos[i]] == -1) {
				return true;
			}
		}
		return false;
	}

//    protected  int[][] getFrequencies(LearningNode variable, List<Node> parents){
//        LearningNode aux;
//        int[][] ArrayNijk;
//        int parentsLength;
//        int position;
//        if(parents == null){
//           parents = new ArrayList<Node>();
//        }
//        parentsLength = parents.size();
//        if(parentsLength == 0){
//            ArrayNijk = new int[variable.getEstadoTamanho()][1];
//        }else{
//            ArrayNijk = new int[variable.getEstadoTamanho()][getQ(parents)];
//        }
//        byte positionVector[] = new byte[parentsLength];
//        int maxVector[]      = new int[parentsLength];
//        for (int i = 0; i < parentsLength; i++ ){
//            aux = (LearningNode)parents.get(i);
//            positionVector[i] = (byte)aux.getPos();
//            maxVector[i] = aux.getEstadoTamanho();
//
//        }
//        int positionLength = positionVector.length;
//        int pos = variable.getPos();
//        int index =0;
//        for (int i = 0 ; i < caseNumber ; i++){
//            for (int j = positionLength - 1; j >=0; j-- ){
//                position = positionVector[j];
//                if(j != positionLength -1){
//                    index += dataBase[i][position]*maxVector[j+1];
//                    if(i == 0){
//                        if(maxVector[j] < 0){
//                            System.currentTimeMillis();
//                        }
//                        maxVector[j] *= maxVector[j+1];
//                        
//                    }
//                }else{
//                    index = dataBase[i][position];
//                }
//            }
//            if(parentsLength == 0){
//                if(! compacted){
//                         ArrayNijk[dataBase[i][variable.getPos()]][0]++;
//                    }else{
//                         ArrayNijk[dataBase[i][variable.getPos()]][0] += vector[i];
//                    }
//            }else{
//                  if(! compacted){
//                       ArrayNijk[dataBase[i][pos]][index]++;
//                  }else{                  	
//                  	if(dataBase[i][pos] == -39 || index == -39){
//                  		System.out.println("Break");
//                  	}
//                    ArrayNijk[dataBase[i][pos]][index] += vector[i];
//                  }
//            }
//        }
//        return ArrayNijk;
//    }

	
	@SuppressWarnings({ "deprecation", "unchecked" })
	protected List<List<Integer>> getInstances(List<Integer> list) {
		List<List<Integer>> instances = new ArrayList<>();
		List<List<Integer>> listAux = new ArrayList<>();
		List<Integer> array;
		List<Integer> arrayAux;
		if (list.size() == 0) {
			return instances;
		}
		for (int i = 0; i < list.size(); i++) {
			int tamanho = list.get(i).intValue();
			for (int k = 0; k < instances.size(); k++) {
				array = (List<Integer>) instances.get(k);
				for (int h = 0; h < tamanho; h++) {
					if (h == 0) {
						array.add(new Integer(h));
						listAux.add(array);
					} else {
						arrayAux = SetToolkit.clone(array);
						arrayAux.remove(array.size() - 1);
						arrayAux.add(new Integer(h));
						listAux.add(arrayAux);
					}
				}
			}
			instances.clear();
			instances = SetToolkit.clone(listAux);
			listAux.clear();
			if (instances.size() == 0) {
				for (int j = 0; j < tamanho; j++) {
					array = new ArrayList<>();
					array.add(new Integer(j));
					instances.add(array);
				}
			}
		}
		return instances;
	}
	
    @SuppressWarnings({ "deprecation", "unchecked" })
	protected List<List<Integer>> getInstancesAsNodes(List<Node> list){
        List<List<Integer>> instances = new ArrayList<List<Integer>>();
        List<List<Integer>> listAux = new ArrayList<List<Integer>>();;
        LearningNode aux;
        List<Integer> array;
        List<Integer> arrayAux;
        if(list.size() == 0){
            return instances;
        }
        for(int i = 0; i < list.size(); i++){
            aux = (LearningNode)list.get(i);
            for(int k = 0 ; k < instances.size(); k++){
                     array = (List<Integer>)instances.get(k);
                for(int h = 0 ; h < getStateSize(aux); h++){
                     if(h == 0){
                        array.add(new Integer(h));
                        listAux.add(array);
                     }else{
                        arrayAux = (List<Integer>) SetToolkit.clone(array);
                        arrayAux.remove(array.size()-1);
                        arrayAux.add(new Integer(h));
                        listAux.add(arrayAux);
                     }
                }
            }
            instances.clear();
            instances = (List<List<Integer>>) SetToolkit.clone(listAux);
            listAux.clear();
            if(instances.size() == 0){
                 for(int j = 0 ; j < getStateSize(aux); j++){
                         array = new ArrayList<Integer>();
                         array.add(new Integer(j));
                         instances.add(array);
                 }
            }
        }
        return instances;
    }

    
    /**
     * This method delegate to {@link #getTotalParentStateSize(List)
     * @param list: parents which must be instance of {@link LearningNode}
     * @return: size of joint states
     * @deprecated
     */
    protected int getQ(List<Node> list) {
    		return this.getTotalParentStateSize(list);
    		
    }
    
    protected int getTotalParentStateSize(List<Node> list) {
        LearningNode variable;
        int qi = 1;
        if(list != null){
            int length  = list.size();
            for (int i = 0; i < length; i++ ){
                variable  = (LearningNode)list.get(i); 
                qi = qi* getStateSize(variable);
            }
        }
        return qi;
    }

    protected int getStateSize(LearningNode variable) {
    	return emptyNet.getNode(variable.getName()).getStatesSize();
    }
    

    protected double log(double number){
        return Math.log(number)/Math.log(10);
    }

    protected double log2(double numero){
        return Math.log(numero)/Math.log(2);
    }


}
