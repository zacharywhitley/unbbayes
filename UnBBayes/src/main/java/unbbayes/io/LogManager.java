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
package unbbayes.io;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import unbbayes.prs.Node;
import unbbayes.prs.bn.Clique;
import unbbayes.prs.bn.JunctionTree;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.Separator;
import unbbayes.util.Debug;

/**
 * Responsible for generating network compilation log
 * 
 * @author Rommel N. Carvalho
 * @author Michael S. Onishi
 * @version 1.0
 */
public class LogManager implements java.io.Serializable {
    
	/** Serialization runtime version number */
	private static final long serialVersionUID = 0;	
	
	public static final int DEFAULT_BUFFER_SIZE = 10 * 1024;
    public static final String DEFAULT_FILENAME = "aj.txt";

    private StringBuffer log;
    
    /** Load resource file from this package */
  	protected static ResourceBundle resource = ResourceBundle.getBundle("unbbayes.io.resources.IoResources");

    public LogManager(int bufferSize) {
        log = new StringBuffer(bufferSize);
        reset();
    }

    public LogManager() {
        this(DEFAULT_BUFFER_SIZE);
    }

    public void clear() {
        log.setLength(0);
    }

    public void reset() {
        clear();
    }

    public void append(String text) {
        log.append(text);
        Debug.print(text);
    }
    
    public void appendln(String text) {
        log.append(text);
        log.append("\n");

        Debug.println(text);
    }

    public String getLog() {
        return log.toString();
    }

    public void writeToDisk(String fileName, boolean append) throws IOException {
        BufferedWriter out = new BufferedWriter(new FileWriter(fileName, append));
        out.write(getLog());
        out.flush();
        out.close();
    }

}