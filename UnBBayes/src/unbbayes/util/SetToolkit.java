/*
 *  UnbBayes
 *  Copyright (C) 2002 Universidade de Brasília
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

import java.util.*;

/**
 *  Classe que fornece métodos estáticos para operações (união e intersecao)
 *  entre conjuntos (List). A operação de subtração e de pertinência são
 *  feitas utilizando métodos do próprio List.
 *
 *@author     Michael e Rommel
 */
public class SetToolkit {

    /**
     *  Realiza a união entre dois conjuntos.
     *
     *@param  conjuntoA  conjunto A
     *@param  conjuntoB  conjunto B
     *@return            A união B
     */
    public static List union(List conjuntoA, List conjuntoB) {
        List result = clone(conjuntoA);
        for (int c1 = 0; c1 < conjuntoB.size(); c1++) {
            if (! conjuntoA.contains(conjuntoB.get(c1))) {
                result.add(conjuntoB.get(c1));
            }
        }

        return result;
    }


    /**
     *  Realiza a interseção entre dois conjuntos.
     *
     *@param  conjuntoA  conjunto A
     *@param  conjuntoB  conjunto B
     *@return            A interseção B
     */
    public static List intersection(List conjuntoA, List conjuntoB) {
        List result = clone(conjuntoA);
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
            throw new RuntimeException("erro na rotina clone. sem acesso");
        } catch (InstantiationException e) {
            throw new RuntimeException("erro na rotina clone. instanciacao");
        }
    }
}

