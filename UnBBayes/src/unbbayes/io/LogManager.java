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

package unbbayes.io;

import java.io.*;
import unbbayes.jprs.jbn.*;
import java.text.DecimalFormat;
import java.util.List;

/**
 * Respons�vel por gerar o log de compila��o da rede.
 */
public class LogManager {
    public static final int DEFAULT_BUFFER_SIZE = 10 * 1024;
    public static final String DEFAULT_FILENAME = "aj.txt";

    private StringBuffer log;

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
     *  Inicializa o arquivo de log de compila��o da rede. Escreve o cabe�alho no
     *  arquivo.
     */
    public void reset() {
        clear();
        log.append("Essa descri��o � feita no processo de compila��o da rede.\n" +
                     "Ela disp�e de informa��es de como a �rvore de jun��o subjacente foi\n" +
                     "criada baseada na t�cnica de �rvore de jun��o com uso da heur�stica do\n" +
                     "peso m�nimo.\n\n");
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

    public void finishLog(JunctionTree tree, List nodes) {
        List clicks = tree.getCliques();
        Clique auxClique;
        Node node;
        PotentialTable auxTab;
        Separator auxSep;

        DecimalFormat df = new DecimalFormat();
        append("******************* cliques ******************\n");

        int sizeclicks1 = clicks.size();
        for (int c = 0; c < sizeclicks1; c++) {
            auxClique = (Clique) clicks.get(c);

            int sizenodes1 = auxClique.getNos().size();
            append("Clique " + c + "\n");
            for (int c2 = 0; c2 < sizenodes1; c2++) {
                append(((Node)auxClique.getNos().get(c2)).getName() + "-");
            }

            append("\nPotential Table\n");
            auxTab = auxClique.getPotentialTable();
            int sizeDados = auxTab.tableSize();
            for (int c2 = 0; c2 < sizeDados; c2++) {
                append(df.format(auxTab.getValue(c2)) + " ");
            }

            append("\nUtility Table\n");
            auxTab = auxClique.getUtilityTable();
            sizeDados = auxTab.tableSize();
            for (int c2 = 0; c2 < sizeDados; c2++) {
                append(df.format(auxTab.getValue(c2)) + " ");
            }
            append("\n\n");
        }

        append("**************** separators *****************\n");

        int sizeseparators = tree.getSeparatorsSize();
        for (int c = 0; c < sizeseparators; c++) {
            auxSep = tree.getSeparatorAt(c);
            append("Separador " + c + " ");
            append("entre " + clicks.indexOf(auxSep.getNo1()) + " e " +
                    clicks.indexOf(auxSep.getNo2()) + "\n");
            append("No(s): ");
            int sizenodes2 = auxSep.getNos().size();
            for (int c2 = 0; c2 < sizenodes2; c2++) {
                node = (Node) auxSep.getNos().get(c2);
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

        append("************ Potenciais associados aos cliques **************\n");
        int sizenodes3 = nodes.size();
        for (int c = 0; c < sizenodes3; c++) {
            node = (Node) nodes.get(c);
            int sizeclicks = clicks.size();
            for (int c2 = 0; c2 < sizeclicks; c2++) {
                auxClique = (Clique) clicks.get(c2);
                if (auxClique.getAssociatedProbabilisticNodes().contains(node) || auxClique.getAssociatedUtilityNodes().contains(node)) {
                    append("No(s): " + node.getName() + " Clique:" + c2 + "\n");
                    break;
                }
            }
        }
    }
}