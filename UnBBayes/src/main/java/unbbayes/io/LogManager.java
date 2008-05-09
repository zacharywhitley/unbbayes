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

/**
 * Responsible for generating network compilation log
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
  	private static ResourceBundle resource = ResourceBundle.getBundle("unbbayes.io.resources.IoResources");

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

    /**
     *  Initializes the logfile of network compilation. Writes the header inside the file.
     */
    public void reset() {
        clear();
        log.append(resource.getString("logHeader"));
    }

    public void append(String text) {
        log.append(text);
        System.out.print(text);
    }
    
    public void appendln(String text) {
        log.append(text);
        log.append("\n");

        System.out.println(text);
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

    public void finishLog(JunctionTree tree, ArrayList<Node> nodes) {
        List clicks = tree.getCliques();
        Clique auxClique;
        Node node;
        PotentialTable auxTab;
        Separator auxSep;

        DecimalFormat df = new DecimalFormat();
        append(resource.getString("cliqueHeader"));

        int sizeclicks1 = clicks.size();
        for (int c = 0; c < sizeclicks1; c++) {
            auxClique = (Clique) clicks.get(c);

            int sizenodes1 = auxClique.getNodes().size();
            append(resource.getString("cliqueName") + c + "\n");
            for (int c2 = 0; c2 < sizenodes1; c2++) {
                append((auxClique.getNodes().get(c2)).getName() + "-");
            }

            append(resource.getString("potentialTableName"));
            auxTab = auxClique.getPotentialTable();
            int sizeDados = auxTab.tableSize();
            for (int c2 = 0; c2 < sizeDados; c2++) {
                append(df.format(auxTab.getValue(c2)) + " ");
            }

            append(resource.getString("utilityTableName"));
            auxTab = auxClique.getUtilityTable();
            sizeDados = auxTab.tableSize();
            for (int c2 = 0; c2 < sizeDados; c2++) {
                append(df.format(auxTab.getValue(c2)) + " ");
            }
            append("\n\n");
        }

        append(resource.getString("separatorHeader"));

        int sizeseparators = tree.getSeparatorsSize();
        for (int c = 0; c < sizeseparators; c++) {
            auxSep = tree.getSeparatorAt(c);
            append(resource.getString("separatorName") + c + " ");
            append(resource.getString("betweenName") + clicks.indexOf(auxSep.getClique1()) + 
            		resource.getString("andName") + clicks.indexOf(auxSep.getClique2()) + "\n");
            append(resource.getString("nodeName"));
            int sizenodes2 = auxSep.getNodes().size();
            for (int c2 = 0; c2 < sizenodes2; c2++) {
                node = (Node) auxSep.getNodes().get(c2);
                append(node.getName() + "-");
            }
            append("\n");
            auxTab = auxSep.getPotentialTable();
            if (auxTab != null) {
                int sizeDados = auxTab.tableSize();
                for (int c2 = 0; c2 < sizeDados; c2++) {
                    append(df.format(auxTab.getValue(c2)) + " ");
                }
                append("\n\n");
            }
        }

        append(resource.getString("potentialAssociatedHeader"));
        int sizenodes3 = nodes.size();
        for (int c = 0; c < sizenodes3; c++) {
            node = (Node) nodes.get(c);
            int sizeclicks = clicks.size();
            for (int c2 = 0; c2 < sizeclicks; c2++) {
                auxClique = (Clique) clicks.get(c2);
                if (auxClique.getAssociatedProbabilisticNodes().contains(node) || auxClique.getAssociatedUtilityNodes().contains(node)) {
                    append(resource.getString("nodeName") + node.getName() + 
                    resource.getString("cliqueLabel") + c2 + "\n");
                    break;
                }
            }
        }
    }
}