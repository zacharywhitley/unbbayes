/**
 * 
 */
package unbbayes.prs.bn.inference.extension;

import unbbayes.prs.bn.Clique;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.bn.Separator;
import unbbayes.prs.id.UtilityNode;

/**
 * This class behaves the same way of {@link MaxProductJunctionTree},
 * but uses min product operation instead of {@link PotentialTable.MaxOperation}
 * @author Shou Matsumoto
 *
 */
public class MinProductJunctionTreeComplement extends MaxProductJunctionTree {

	private boolean initialized;

	public MinProductJunctionTreeComplement() {
		super();
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.JunctionTree#initBeliefs()
	 */
	public void initBeliefs() throws Exception {
		if (! this.isInitialized()) {
			Clique auxClique;
			PotentialTable auxTabPot;
			PotentialTable auxUtilTab;
	
			int sizeCliques = this.getCliques().size();
			for (int k = 0; k < sizeCliques; k++) {
				auxClique = this.getCliques().get(k);
				auxTabPot = auxClique.getProbabilityFunction();
				auxUtilTab = auxClique.getUtilityTable();
	
				int tableSize = auxTabPot.tableSize();
				for (int c = 0; c < tableSize; c++) {
					auxTabPot.setValue(c, 1);
				}
	
				ProbabilisticNode auxVP;
				int sizeAssociados = auxClique.getAssociatedProbabilisticNodes().size();
				for (int c = 0; c < sizeAssociados; c++) {
					auxVP = (ProbabilisticNode) auxClique.getAssociatedProbabilisticNodes().get(c);
					auxTabPot.opTab(auxVP.getProbabilityFunction(), PotentialTable.PRODUCT_OPERATOR);
				}
	
				tableSize = auxUtilTab.tableSize();
				for (int i = 0; i < tableSize; i++) {
					auxUtilTab.setValue(i, 0);
				}
				UtilityNode utilNode;
				sizeAssociados = auxClique.getAssociatedUtilityNodes().size();
				for (int i = 0; i < sizeAssociados; i++) {
					utilNode = (UtilityNode) auxClique.getAssociatedUtilityNodes().get(i);
					auxUtilTab.opTab(utilNode.getProbabilityFunction(), PotentialTable.PLUS_OPERATOR);
				}
			}
	
			for (Separator auxSep : getSeparators()) {
				auxTabPot = auxSep.getProbabilityFunction();
				int sizeDados = auxTabPot.tableSize();
				for (int c = 0; c < sizeDados; c++) {
					auxTabPot.setValue(c, 1);
				}
	
				auxUtilTab = auxSep.getUtilityTable();
				sizeDados = auxUtilTab.tableSize();
				for (int i = 0; i < sizeDados; i++) {
					auxUtilTab.setValue(i, 0);
				}
			}
			this.adjustPotentialsBeforeInitialConsistency();
			consistency();
			copyTableData();
			this.setInitialized(true);
		} else {
			restoreTableData();						
		}
		
	}
	
	/**
	 * Convert all clique/separator potentials to 1-originalValue
	 */
	protected void adjustPotentialsBeforeInitialConsistency(){
		for (Clique clique : this.getCliques()) {
			PotentialTable tab = clique.getProbabilityFunction();
			for (int i = 0; i < tab.tableSize(); i++) {
				// set to complementar value
				tab.setValue(i, 1 - tab.getDoubleValue(i));
			}
		}
//		for (int i = 0; i < this.getSeparatorsSize(); i++) {
//			Separator sep = this.getSeparatorAt(i);
//			PotentialTable tab = sep.getProbabilityFunction();
//			for (int j = 0; j < tab.tableSize(); j++) {
//				// set to complementar value
//				tab.setValue(j, 1 - tab.getValue(i));
//			}
//		}
	}
	
	protected void copyTableData() {
		int sizeCliques = this.getCliques().size();
		for (int k = 0; k < sizeCliques; k++) {
			Clique auxClique = (Clique) this.getCliques().get(k);
			auxClique.getProbabilityFunction().copyData();
			auxClique.getUtilityTable().copyData();
		}
		
		for (Separator auxSep : getSeparators()) {
			auxSep.getProbabilityFunction().copyData();
			auxSep.getUtilityTable().copyData();
		}
	}
	
	protected void restoreTableData() {
		int sizeCliques = this.getCliques().size();
		for (int k = 0; k < sizeCliques; k++) {
			Clique auxClique = (Clique) this.getCliques().get(k);
			auxClique.getProbabilityFunction().restoreData();
			auxClique.getUtilityTable().restoreData();
		}
		
		for (Separator auxSep : getSeparators()) {
			auxSep.getProbabilityFunction().restoreData();
			auxSep.getUtilityTable().restoreData();
		}
	}
	

	/**
	 * @return the initialized
	 */
	public boolean isInitialized() {
		return initialized;
	}

	/**
	 * @param initialized the initialized to set
	 */
	public void setInitialized(boolean initialized) {
		this.initialized = initialized;
	}
	
}
