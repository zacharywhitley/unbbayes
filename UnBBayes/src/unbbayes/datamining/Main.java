/*
 *  UnbBayes
 *  Copyright (C) 2005 Universidade de Brasília
 *
 *  This file is part of UnBMiner.
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

package unbbayes.datamining;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Locale;

import unbbayes.datamining.gui.InvokerMain;

/**
 *  This class starts UnBMiner
 *
 *@author     Mario.
 *@version    22/05/2005
 */
public class Main {

    /**
     *  Starts UnBMiner.
     */
    public static void main(String[] args) throws Exception {
        // default states
    	int defaultStates = 40;
    	// default confidence limit
        int confidenceLimit = 100;
        String defaultLanguage = "English";
        String defaultLaf = "Metal";
    	
    	BufferedReader r = new BufferedReader(new FileReader(new File("DataMining.ini")));
            String header = r.readLine();
            if (header.equals("[data mining]"))
            {   // Número de estados permitidos
                String states = r.readLine();
                if ((states.substring(0,17)).equals("Maximum states = "))
                {   defaultStates = Integer.parseInt(states.substring(17));
                }
                // Intervalo de confiança
                String confidence = r.readLine();
                if ((confidence.substring(0,19)).equals("Confidence limit = "))
                {   confidenceLimit = Integer.parseInt(confidence.substring(19));
                }
                // Opção de língua
                String language = r.readLine();
                if ((language.substring(0,11)).equals("Language = "))
                {   language = language.substring(11);
                    if (language.equals("English"))
                    {   Locale.setDefault(new Locale("en",""));
                        defaultLanguage = language;
                    }
                    else if (language.equals("Potuguese"))
                    {   Locale.setDefault(new Locale("pt",""));
                        defaultLanguage = language;
                    }
                }
                // Opção de look and feel
                String laf = r.readLine();
                if ((laf.substring(0,16)).equals("Look and Feel = "))
                {   laf = laf.substring(16);
                    if (laf.equals("Metal"))
                    {   defaultLaf = laf;
                    }
                    else if (laf.equals("Motif"))
                    {   defaultLaf = laf;
                    }
                    else if (laf.equals("Windows"))
                    {   defaultLaf = laf;
                    }
                }
            }

        InvokerMain main = new InvokerMain(defaultStates,confidenceLimit,defaultLanguage,defaultLaf);
        main.setVisible(true);
    }
}
