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

package unbbayes.io;

import java.io.*;

import unbbayes.prs.*;
import unbbayes.prs.bn.*;
import unbbayes.util.NodeList;

import java.text.DecimalFormat;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Responsável por gerar o log de compilação da rede.
 * @author Rommel N. Carvalho
 * @author Michael S. Onishi
 * @version 1.0
 */
public class LogManager implements java.io.Serializable {
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
     *  Inicializa o arquivo de log de compilação da rede. Escreve o cabeçalho no
     *  arquivo.
     */
    public void reset() {
        clear();
        log.append(resource.getString("logHeader"));
    }

    public void append(String text) {
        log.append(text);
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

    public void finishLog(JunctionTree tree, NodeList nodes) {
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