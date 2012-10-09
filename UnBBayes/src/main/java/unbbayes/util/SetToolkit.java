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
package unbbayes.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;

import unbbayes.prs.Node;

/**
 * This class offers some utility methods related to operations
 * regaring {@link List} (e.g. union, intersection).
 *
 *@author     Michael e Rommel
 */
public class SetToolkit {
	
	/** Load resource file from this package */
  	private static ResourceBundle resource = unbbayes.util.ResourceController.newInstance().getBundle(
  			unbbayes.util.resources.UtilResources.class.getName());

    /**
     * Union of two lists.
     *
     *@param  listA  
     *@param  listB  
     *@return the union of the two lists
     */
    public static List union(List<?> listA, List<?> listB) {
        List<Object> result = (ArrayList<Object>)clone(listA);
        for (int c1 = 0; c1 < listB.size(); c1++) {
            if (! listA.contains(listB.get(c1))) {
                result.add(listB.get(c1));
            }
        }

        return result;
    }

    /**
     *@param  listA  
     *@param  listB 
     *@return the union between two lists
     */
    public static ArrayList<Node> union(ArrayList<Node> listA, ArrayList<Node> listB) {
    	ArrayList<Node> result = new ArrayList<Node>(listA.size() + listB.size());
        result.addAll(listA);
        for (int c1 = 0; c1 < listB.size(); c1++) {
            if (! listA.contains(listB.get(c1))) {
                result.add(listB.get(c1));
            }
        }

        return result;
    }

    /**
     *@param  listA
     *@param  listB
     *@return  intersection between the lists
     */
    public static List intersection(List<?> conjuntoA, List<?> conjuntoB) {
        List<Object> result = (ArrayList<Object>)clone(conjuntoA);
        result.retainAll(conjuntoB);
        return result;
    }
    
    
	/**
     *@param  listA
     *@param  listB
     *@return  intersection between the lists
     */
    public static  ArrayList<Node> intersection( ArrayList<Node> listA,  ArrayList<Node> listB) {
    	 ArrayList<Node> result = clone(listA);
        result.retainAll(listB);
        return result;
    }

    /**
     * 
     * @param collection
     * @return
     * @deprecated do not use this method anymore, because this does not implement type safety
     */
    public static List clone(Collection collection) {
        try {
            Class classe = collection.getClass();
            List result = (List)classe.newInstance();
            result.addAll(collection);
            return result;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(resource.getString("IllegalAccessException"));
        } catch (InstantiationException e) {
            throw new RuntimeException(resource.getString("InstantiationException"));
        }
    }
    
    public static ArrayList<Node> clone(ArrayList<Node> conjunto) {
    	return new ArrayList<Node>(conjunto);
    }
    
    /**
	 * Because  {@link List#contains(Object)} applied to nodes
	 * invokes {@link Node#equals(Object)}, which performs string comparison (slower),
	 * this method can be used in order to perform exact object comparison (faster).
	 * @see List#contains(Object)
	 */
	public static boolean containsExact(List collection, Object o) {
		for (Object object : collection) {
			if (object == o) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Because  {@link List#containsAll(Collection)} applied to nodes
	 * invokes {@link Node#equals(Object)}, which performs string comparison (slower),
	 * this method can be used in order to perform exact object comparison (faster).
	 * @see List#containsAll(Collection)
	 */
	public static boolean containsAllExact(List container, Collection contents) {
		boolean hasObject;
		for (Object content : contents) {
			hasObject = false;
			for (Object object : container) {
				if (object == content) {
					hasObject = true;
					break;
				}
			}
			if (!hasObject) {
				return false;
			}
		}
		return true;
	}
}

