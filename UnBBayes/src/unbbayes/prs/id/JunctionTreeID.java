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

package unbbayes.prs.id;



import unbbayes.prs.bn.Clique;
import unbbayes.prs.bn.JunctionTree;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.Separator;
import unbbayes.util.NodeList;
import unbbayes.util.SetToolkit;

/**
 *  Classe que representa uma Árvore de Junção para Diagramas de Influencias.
 *
 *@author     Michael
 *@author     Rommel
 */
public class JunctionTreeID extends JunctionTree implements java.io.Serializable {


   /**
    * Overrides the method in JunctionTree.
    *
    * @param clique1
    * @param clique2
    */
    protected void absorb(Clique clique1, Clique clique2) {    	
        Separator separator = getSeparator(clique1, clique2);
        super.absorb(clique1, clique2);
//        clique1.absorb(clique2, separator.getPotentialTable());
        NodeList toDie = SetToolkit.clone(clique2.getNodes());
        toDie.removeAll(separator.getNodes());

        PotentialTable originalSeparatorUtilityTable = (PotentialTable) separator.getUtilityTable().clone();

        PotentialTable dummyTable = (PotentialTable) clique2.getUtilityTable().clone();
        dummyTable.directOpTab(clique2.getPotentialTable(), PotentialTable.PRODUCT_OPERATOR);
        for (int i = 0; i < toDie.size(); i++) {
            dummyTable.removeVariable(toDie.get(i));
        }

        for (int i = separator.getUtilityTable().tableSize()-1; i >= 0; i--) {
            separator.getUtilityTable().setValue(i, dummyTable.getValue(i));
        }
        separator.getUtilityTable().directOpTab(separator.getPotentialTable(), PotentialTable.DIVISION_OPERATOR);
        dummyTable = (PotentialTable) separator.getUtilityTable().clone();
        dummyTable.directOpTab(originalSeparatorUtilityTable, PotentialTable.MINUS_OPERATOR);
        clique1.getUtilityTable().opTab(dummyTable, PotentialTable.PLUS_OPERATOR);
    }
}