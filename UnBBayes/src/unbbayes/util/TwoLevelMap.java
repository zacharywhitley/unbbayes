/*
 * TwoLevelMap.java
 *
 * Created on 30 de Abril de 2002, 03:45
 */

package unbbayes.util;

import java.util.*;

/**
 *
 * @author  Paulo F. Duarte
 */
public class TwoLevelMap {
    private ArrayMap mainMap = new ArrayMap();

    public Object put(Object mainKey, Object subKey, Object value) {
        ArrayMap subMap = (ArrayMap)mainMap.get(mainKey);
        Object oldValue;
        if (subMap == null) {
            subMap = new ArrayMap();
            mainMap.put(mainKey, subMap);
            subMap.put(subKey, value);
            return null;
        }
        oldValue = (String[])subMap.get(subKey);
        subMap.put(subKey, value);
        return oldValue;
    }
    public Object get(String mainKey, String subKey) {
        ArrayMap subMap = (ArrayMap)mainMap.get(mainKey);
        if (subMap == null)
            return null;
        return subMap.get(subKey);
    }

    public ArrayList getMainKeys() {
        return mainMap.getKeys();
    }

    public ArrayList getSubKeys(String mainKey) {
        ArrayMap subMap = (ArrayMap)mainMap.get(mainKey);
        if (subMap == null)
            return null;
        return subMap.getKeys();
    }

    public void clear() {
        mainMap.clear();
    }
}
