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
import java.util.List;

import unbbayes.prs.Node;
import unbbayes.prs.bn.LearningNode;
import unbbayes.util.SetToolkit;


public abstract class LearningToolkit{

	    protected long caseNumber;
	    protected int[][] dataBase;
	    protected int[] vector;
	    protected boolean compacted;


	protected void getProbability(int[][] arrayNijk, LearningNode variable){
        List instanceVector;
        List instance = new ArrayList();
        float probability;
        int nij;
        int ri = variable.getEstadoTamanho();
        int nijLength  = getQ(variable.getPais());
        instanceVector  = getInstances(variable.getPais());
        for(int i = 0; i < nijLength;i++){
             nij = 0;
             for(int j = 0; j < ri ; j++){
                nij+= arrayNijk[j][i];
             }
             if(instanceVector.size() > 0 ){
                instance = (List)instanceVector.get(i);
             }
             for(int j = 0; j < ri; j++){
                  probability = (float)(1+ arrayNijk[j][i])/(ri+nij);
                  int coord[];
                  coord = new int[nijLength+1];
                  coord[0] = j;
                  for(int k = 1; k <= instance.size(); k++){
                      coord[k] = ((Integer)instance.get(k-1)).intValue();
                  }
                  variable.getProbabilidades().setValue(coord, probability);
             }
        }
    }

    protected  int[][] getFrequencies(LearningNode variable, ArrayList<Node> parents){
        LearningNode aux;
        int[][] ArrayNijk;
        int parentsLength;
        int position;
        if(parents == null){
           parents = new ArrayList<Node>();
        }
        parentsLength = parents.size();
        if(parentsLength == 0){
            ArrayNijk = new int[variable.getEstadoTamanho()][1];
        }else{
            ArrayNijk = new int[variable.getEstadoTamanho()][getQ(parents)];
        }
        byte positionVector[] = new byte[parentsLength];
        int maxVector[]      = new int[parentsLength];
        for (int i = 0; i < parentsLength; i++ ){
            aux = (LearningNode)parents.get(i);
            positionVector[i] = (byte)aux.getPos();
            maxVector[i] = aux.getEstadoTamanho();

        }
        int positionLength = positionVector.length;
        int pos = variable.getPos();
        int index =0;
        for (int i = 0 ; i < caseNumber ; i++){
            for (int j = positionLength - 1; j >=0; j-- ){
                position = positionVector[j];
                if(j != positionLength -1){
                    index += dataBase[i][position]*maxVector[j+1];
                    if(i == 0){
                        if(maxVector[j] < 0){
                            System.currentTimeMillis();
                        }
                        maxVector[j] *= maxVector[j+1];
                        
                    }
                }else{
                    index = dataBase[i][position];
                }
            }
            if(parentsLength == 0){
                if(! compacted){
                         ArrayNijk[dataBase[i][variable.getPos()]][0]++;
                    }else{
                         ArrayNijk[dataBase[i][variable.getPos()]][0] += vector[i];
                    }
            }else{
                  if(! compacted){
                       ArrayNijk[dataBase[i][pos]][index]++;
                  }else{                  	
                  	if(dataBase[i][pos] == -39 || index == -39){
                  		System.out.println("Break");
                  	}
                    ArrayNijk[dataBase[i][pos]][index] += vector[i];
                  }
            }
        }
        return ArrayNijk;
    }

    protected List getInstances(ArrayList<Node> list){
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
                for(int h = 0 ; h < aux.getEstadoTamanho(); h++){
                     if(h == 0){
                        array.add(new Integer(h));
                        listAux.add(array);
                     }else{
                        arrayAux = (ArrayList<Integer>)SetToolkit.clone((List)array);
                        arrayAux.remove(array.size()-1);
                        arrayAux.add(new Integer(h));
                        listAux.add(arrayAux);
                     }
                }
            }
            instances.clear();
            instances = (ArrayList<List<Integer>>)SetToolkit.clone((List)listAux);
            listAux.clear();
            if(instances.size() == 0){
                 for(int j = 0 ; j < aux.getEstadoTamanho(); j++){
                         array = new ArrayList<Integer>();
                         array.add(new Integer(j));
                         instances.add(array);
                 }
            }
        }
        return instances;
    }

    protected int getQ(ArrayList<Node> list) {
        LearningNode variable;
        int qi = 1;
        if(list != null){
            int length  = list.size();
            for (int i = 0; i < length; i++ ){
                variable  = (LearningNode)list.get(i);
                qi = qi* variable.getEstadoTamanho();
            }
        }
        return qi;
    }


    protected double log(double number){
        return Math.log(number)/Math.log(10);
    }

    protected double log2(double numero){
        return Math.log(numero)/Math.log(2);
    }


}
