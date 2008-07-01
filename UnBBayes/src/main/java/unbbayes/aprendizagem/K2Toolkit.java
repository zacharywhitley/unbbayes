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

import unbbayes.prs.Node;
import unbbayes.prs.bn.LearningNode;
import unbbayes.util.SetToolkit;


public abstract class K2Toolkit extends PonctuationToolkit{
	
     
    protected void constructPredecessors(ArrayList<Node> list){ 
        LearningNode aux;
        int length = list.size();
        for(int i = length - 1; i > 0  ; i--){
            aux = (LearningNode)list.get(i);
                for (int j = i-1; j > -1 ; j--){
                    aux.adicionaPredecessor((LearningNode)list.get(j));
                }
        }
    }    
    
    protected Object[] getZMax(LearningNode variable){    	
        LearningNode z = null;
        ArrayList<Node> parents;
        ArrayList<Node> zVector;
        double maxAux;
        maxAux = 0.0;
        double max = -1*Double.MAX_VALUE;       
        zVector = difference(SetToolkit.clone(variable.getPredecessores()), variable.getPais());
        int length = zVector.size();
        for (int i = 0 ; i < length; i++ ){
            parents = union(SetToolkit.clone(variable.getPais()), (LearningNode)zVector.get(i));
            maxAux  = getG(variable,parents);
            if (max < maxAux){
                max = maxAux;
                z = (LearningNode)zVector.get(i);
            }
        }
        return new Object[]{z,new Double(max)};        
    }    
    
    protected ArrayList<Node> union(ArrayList<Node> list, LearningNode variable){
        if (list == null){
            list = new ArrayList<Node>();
        }
        list.add(variable);
        return list;
    }
    
    protected ArrayList<Node> difference(ArrayList<Node> list1, ArrayList<Node> list2){
        LearningNode aux;
        LearningNode aux2;
        ArrayList<Node> listReturn = SetToolkit.clone(list1);
        for (int i = 0 ;  i < listReturn.size(); i++ ){
            aux = (LearningNode)listReturn.get(i);
            for (int j = 0 ; j < list2.size() ; j++ ){
                aux2 = (LearningNode)list2.get(j);
                if (aux2.getName().equals(aux.getName())){
                    listReturn.remove(i);
                }
            }
        }
        return listReturn;
    }
    
    
    

}
