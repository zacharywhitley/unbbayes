/*
 * TwoLevelMap.java
 *
 * Created on 30 de Abril de 2002, 03:45
 */

package unbbayes.util;

import java.util.ArrayList;

/**
 *
 * @author  Paulo F. Duarte
 */
public class TwoLevelMap<P,K,V> {
    private ArrayMap<P,ArrayMap<K,V>> mainMap = new ArrayMap<P,ArrayMap<K,V>>();

	/*
	 * P -> chave principal
	 * K -> chave secund�ria
	 * V -> valor
	 */    
    
    public Object put(P mainKey, K subKey, V value) {
        ArrayMap<K,V> subMap = mainMap.get(mainKey);
        Object oldValue;
        if (subMap == null) {
            subMap = new ArrayMap<K,V>();
            mainMap.put(mainKey, subMap);
            subMap.put(subKey, value);
            return null;
        }
        oldValue = (String[])subMap.get(subKey);
        subMap.put(subKey, value);
        return oldValue;
    }
    public Object get(String mainKey, String subKey) {
        ArrayMap<K,V> subMap = mainMap.get(mainKey);
        if (subMap == null)
            return null;
        return subMap.get(subKey);
    }

    public ArrayList<P> getMainKeys() {
        return mainMap.getKeys();
    }

    public ArrayList<K> getSubKeys(String mainKey) {
        ArrayMap<K,V> subMap = mainMap.get(mainKey);
        if (subMap == null)
            return null;
        return subMap.getKeys();
    }

    public void clear() {
        mainMap.clear();
    }
}
