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

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Set;

/**
 *
 * @author  Paulo Fernando Barbosa Duarte
 * @version 3 de Abril de 2002

/**
 * Classe que implementa um Map baseado em ArrayList.
 */
public class ArrayMap<K,V> extends AbstractMap implements java.io.Serializable {
    
    private ArrayList<K> keys = new ArrayList<K>();
    private ArrayList<V> values = new ArrayList<V>();

    /**
     *  Insere no ArrayMap um par chave/valor.
     *
     * @param key chave a ser inserida.
     * @param value valor a ser inserida.
     * @return null se key n�o foi anteriormente inserida, sen�o o valor anterior.
     */
    public V put(K key, V value) {
        V result = get(key);
        if(result == null) {
            keys.add(key);
            values.add(value);
        } else
            values.set(keys.indexOf(key), value);
        return result;
    }

    /**
     *  Recupera do ArrayMap o valor associado a uma chave.
     *
     * @param key chave qual se deseja recuperar o valor.
     * @return valor associado a key.
     */
    public V get(Object key) {
        return keys.contains(key) ? values.get(keys.indexOf(key)) : null;
    }
    
    /**
     *  Retorna um ArrayList das chaves.
     *
     * @return array de chaves.
     */
    public ArrayList<K> getKeys() {
        ArrayList<K> keysArrayList = new ArrayList<K>();
        keysArrayList.addAll(keys);
        return keysArrayList;
    }
    
    /**
     *  Retorna o n�mero de pares chave/valor.
     *
     * @return n�mero de pares chave/valor.
     */
    public int size() {
        return keys.size();
    }
    
    /**
     *  Apaga um elemento do ArrayMap.
     *
     *@param key
     *@return antigo valor associado a key.
     */
    public V remove(Object key) {
        V value = get(key);
        if (value != null) {
            keys.remove(key);
            values.remove(value);
        }
        return value;
    }

    /**
     *  Apaga todos os elementos do ArrayMap.
     */
    public void clear() {
        keys.clear();
        values.clear();
    }

    public Set<Map.Entry<K,V>> entrySet() {return null;}
}