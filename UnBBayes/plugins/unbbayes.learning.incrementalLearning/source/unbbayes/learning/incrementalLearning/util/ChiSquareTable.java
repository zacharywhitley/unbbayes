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
package unbbayes.learning.incrementalLearning.util;

import java.util.HashMap;
import java.util.Map;

/**
 * @author custodio
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ChiSquareTable {
    
    private static Object[] tabelaCompleta = 
    {new Object[]{new String("1"),new String[]{"3.84","5.02","6.63"}}
    ,new Object[]{new String("2"),new String[]{"5.99","7.38","9.21"}}
    ,new Object[]{new String("3"),new String[]{"7.81","9.35","11.3"}}
    ,new Object[]{new String("4"),new String[]{"9.49","11.1","13.3"}}
    ,new Object[]{new String("5"),new String[]{"11.1","12.8","15.1"}}
    ,new Object[]{new String("6"),new String[]{"12.6","14.4","16.8"}}
    ,new Object[]{new String("7"),new String[]{"14.1","16.0","18.5"}}
    ,new Object[]{new String("8"),new String[]{"15.5","17.5","20.1"}}
    ,new Object[]{new String("9"),new String[]{"16.9","19.0","21.7"}}
    ,new Object[]{new String("10"),new String[]{"18.3","20.5","23.2"}}
    ,new Object[]{new String("11"),new String[]{"19.7","21.9","24.7"}}
    ,new Object[]{new String("12"),new String[]{"21.0","23.3","26.2"}}
    ,new Object[]{new String("13"),new String[]{"22.4","24.7","27.7"}}
    ,new Object[]{new String("14"),new String[]{"23.7","26.1","29.1"}}
    ,new Object[]{new String("15"),new String[]{"25.0","27.5","30.6"}}
    ,new Object[]{new String("16"),new String[]{"26.3","28.8","32.0"}}
    ,new Object[]{new String("17"),new String[]{"27.6","30.2","33.4"}}
    ,new Object[]{new String("18"),new String[]{"28.9","31.5","34.8"}}
    ,new Object[]{new String("19"),new String[]{"30.1","32.9","36.2"}}
    ,new Object[]{new String("20"),new String[]{"31.4","34.2","37.6"}}
    ,new Object[]{new String("21"),new String[]{"32.7","35.5","38.9"}}
    ,new Object[]{new String("22"),new String[]{"33.9","36.8","40.3"}}
    ,new Object[]{new String("23"),new String[]{"35.2","38.1","41.6"}}
    ,new Object[]{new String("24"),new String[]{"36.4","39.4","43.0"}}
    ,new Object[]{new String("25"),new String[]{"37.7","40.6","44.3"}}
    ,new Object[]{new String("26"),new String[]{"38.9","41.9","45.6"}}
    ,new Object[]{new String("27"),new String[]{"40.1","43.2","47.0"}}
    ,new Object[]{new String("28"),new String[]{"41.3","44.5","48.3"}}
    ,new Object[]{new String("29"),new String[]{"42.6","45.7","49.6"}}
    ,new Object[]{new String("30"),new String[]{"43.8","47.0","50.9"}}
    ,new Object[]{new String("40"),new String[]{"55.8","59.3","63.7"}}
    ,new Object[]{new String("50"),new String[]{"67.5","71.4","76.2"}}
    ,new Object[]{new String("60"),new String[]{"79.1","83.3","88.4"}}
    ,new Object[]{new String("70"),new String[]{"90.5","95.0","100"}}
    ,new Object[]{new String("80"),new String[]{"102","107","112"}}
    ,new Object[]{new String("90"),new String[]{"113","118","124"}}
    ,new Object[]{new String("100"),new String[]{"124","130","136"}}};
    
    private static Map grausLiberdade;
    
    public static double getArea(int grauLiberdade, double nivelConfianca){
        if(grausLiberdade == null){
            construirMapa();
        }
        String[] valores;
        if(grausLiberdade.containsKey(""+grauLiberdade)){
            valores = (String[])grausLiberdade.get(""+grauLiberdade);
        }else{
            grauLiberdade /= 10;
            grauLiberdade *= 10;
            if(grausLiberdade.containsKey(""+grauLiberdade)){
                valores = (String[])grausLiberdade.get(""+grauLiberdade);
            }else{
                valores = (String[])grausLiberdade.get("100");
            }
        }
        if(nivelConfianca == 0.05){
            return Double.parseDouble(valores[0]);
        }else if(nivelConfianca == 0.025){
            return Double.parseDouble(valores[1]);
        }else if(nivelConfianca == 0.01){
            return Double.parseDouble(valores[2]);
        }
        return 0.0;
    }
    
    private static void construirMapa(){
        grausLiberdade = new HashMap();
        for(int i = 0; i < tabelaCompleta.length; i++){
            grausLiberdade.put((String)((Object[])tabelaCompleta[i])[0],((Object[])tabelaCompleta[i])[1]);
        }
    }
    
    
    

}
