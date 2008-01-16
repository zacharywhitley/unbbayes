/*
 *  UnbBayes
 *  Copyright (C) 2002 Universidade de Bras�lia
 *
 *  This file is part of UnbBayes.
 *
 *  UnbBayes is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  UnbBayes is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with UnbBayes; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package unbbayes.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;

/**
 *  Classe que fornece m�todos est�ticos para opera��es (uni�o e intersecao)
 *  entre conjuntos (List). A opera��o de subtra��o e de pertin�ncia s�o
 *  feitas utilizando m�todos do pr�prio List.
 *
 *@author     Michael e Rommel
 */
public class SetToolkit {
	
	/** Load resource file from this package */
  	private static ResourceBundle resource = ResourceBundle.getBundle("unbbayes.util.resources.UtilResources");

    /**
     *  Realiza a uni�o entre dois conjuntos.
     *
     *@param  conjuntoA  conjunto A
     *@param  conjuntoB  conjunto B
     *@return            A uni�o B
     */
    public static List union(List<?> conjuntoA, List<?> conjuntoB) {
        List<Object> result = (ArrayList<Object>)clone(conjuntoA);
        for (int c1 = 0; c1 < conjuntoB.size(); c1++) {
            if (! conjuntoA.contains(conjuntoB.get(c1))) {
                result.add(conjuntoB.get(c1));
            }
        }

        return result;
    }


    /**
     *  Realiza a uni�o entre dois conjuntos.
     *
     *@param  conjuntoA  conjunto A
     *@param  conjuntoB  conjunto B
     *@return            A uni�o B
     */
    public static NodeList union(NodeList conjuntoA, NodeList conjuntoB) {
        NodeList result = new NodeList(conjuntoA.size() + conjuntoB.size());
        result.addAll(conjuntoA);
        for (int c1 = 0; c1 < conjuntoB.size(); c1++) {
            if (! conjuntoA.contains(conjuntoB.get(c1))) {
                result.add(conjuntoB.get(c1));
            }
        }

        return result;
    }


    /**
     *  Realiza a interse��o entre dois conjuntos.
     *
     *@param  conjuntoA  conjunto A
     *@param  conjuntoB  conjunto B
     *@return            A interse��o B
     */
    public static List intersection(List<?> conjuntoA, List<?> conjuntoB) {
        List<Object> result = (ArrayList<Object>)clone(conjuntoA);
        result.retainAll(conjuntoB);
        return result;
    }
    
    
	/**
     *  Realiza a interse��o entre dois conjuntos.
     *
     *@param  conjuntoA  conjunto A
     *@param  conjuntoB  conjunto B
     *@return            A interse��o B
     */
    public static NodeList intersection(NodeList conjuntoA, NodeList conjuntoB) {
        NodeList result = clone(conjuntoA);
        result.retainAll(conjuntoB);
        return result;
    }

    public static List clone(Collection conjunto) {
        try {
            Class classe = conjunto.getClass();
            List result = (List)classe.newInstance();
            result.addAll(conjunto);
            return result;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(resource.getString("IllegalAccessException"));
        } catch (InstantiationException e) {
            throw new RuntimeException(resource.getString("InstantiationException"));
        }
    }
    
    
    public static NodeList clone(NodeList conjunto) {
    	NodeList result = new NodeList(conjunto.size());
    	result.addAll(conjunto);
    	return result;
    }
}

