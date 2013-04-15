/**
 * 
 */
package unbbayes.prs.bn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import unbbayes.prs.INode;
import unbbayes.prs.Node;
import unbbayes.prs.bn.PotentialTable.ISumOperation;
import unbbayes.prs.bn.inference.extension.MinProductJunctionTree;
import unbbayes.prs.bn.inference.extension.ZeroAssetsException;

/**
 * This is an extension of {@link MinProductJunctionTree}
 * for values in logarithmic scale. That is, instead of using
 * multiplications and divisions during the method {@link #absorb(Clique, Clique)}, 
 * it uses addition and subtraction respectively.
 * @author Shou Matsumoto
 *
 */
public class LogarithmicMinProductJunctionTree extends MinProductJunctionTree {
	
	
	private static final long serialVersionUID = 4298854952629357037L;
	
	/** Instance called when doing marginalization. This instance will min-out values in {@link PotentialTable#removeVariable(INode)} */
	public static final ISumOperation DEFAULT_MIN_OUT_OPERATION = new LogarithmicMinProductJunctionTree().new MinOperation();

	/**
	 * If false, {@link #getSeparatorsMap()} will not be used
	 * as an index for the separators given cliques (so that
	 * we can reduce memory usage by not using {@link #getSeparatorsMap()}).
	 * If true, {@link #addSeparator(Separator)},
	 * {@link #getSeparators()}, and {@link #removeSeparator(Separator)}
	 * will simply delegate to superclass.
	 */
	private boolean isToUseSeparatorsMap = true;
	
	/** If true, {@link #absorb(Clique, Clique)} will use singleton temporary lists in order to reduce garbage */
	private static boolean isToUseSingletonListsInAbsorb = true;
	
	/** This is the singleton temporary list used by {@link #absorb(Clique, Clique)} when {@link #isToUseSingletonListsInAbsorb()} == true */
	private static List<Node> singletonListForAbsorb = new ArrayList<Node>(7);
	
	/**
	 *  This is an extension of {@link MinProductJunctionTree}
	 * for values in logarithmic scale. That is, instead of using
	 * multiplications and divisions during the method {@link #absorb(Clique, Clique)}, 
	 * it uses addition and subtraction respectively.
	 */
	public LogarithmicMinProductJunctionTree() {
		setMaxOperation(DEFAULT_MIN_OUT_OPERATION);
		if (!isToUseSeparatorsMap()) {
			// disable the index of separators given cliques
			this.setSeparatorsMap(null);
		} else {
//			this.setSeparatorsMap(new SeparatorMap());
		}
	}
	
	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.JunctionTree#absorb(unbbayes.prs.bn.Clique, unbbayes.prs.bn.Clique)
	 */
	protected void absorb(Clique clique1, Clique clique2) {
		Separator sep = getSeparator(clique1, clique2);
		if (sep == null) {
			// cliques are disconnected (they are separated subnets of a disconnected network)
			return;
		}
		// table of separator
		PotentialTable sepTab = sep.getProbabilityFunction();
		if (sepTab.tableSize() <= 0) {
			// cliques are disconnected (they are separated subnets of a disconnected network)
			return;	// there is nothing to propagate
		}
		// who are going to be removed 
		List<Node> minOut = null;
		if (isToUseSingletonListsInAbsorb) {
			minOut = singletonListForAbsorb;
			minOut.clear();
			minOut.addAll(clique2.getNodes());
			minOut.removeAll(sep.getNodes());
		} else {
			minOut = new ArrayList<Node>(clique2.getNodes());
			minOut.removeAll(sep.getNodes());
		}

		// temporary table for ratio calculation
		PotentialTable dummyTable =
			(PotentialTable) clique2.getProbabilityFunction().getTemporaryClone();
		
		for (int i = 0; i < minOut.size(); i++) {
			PotentialTable.ISumOperation backupOp = dummyTable.getSumOperation();	// backup real op
			// TODO store maximum values so that we can update content of dummyTable with max instead of marginal
			dummyTable.setSumOperation(getMaxOperation());	// substitute op w/ operator for comparison (max) instead of sum (marginal)
			// remove maxout (this will automatically marginalize)
			dummyTable.removeVariable(minOut.get(i));	// no normalization, 
			// TODO the removal did a marginalization. Do max right now
			dummyTable.setSumOperation(backupOp);	// restore previous op
		}
		

		PotentialTable originalSeparatorTable =
			(PotentialTable) sepTab.getTemporaryClone();

		for (int i = sepTab.tableSize() - 1; i >= 0; i--) {
			if (Float.isInfinite(dummyTable.getValue(i))) {
				// force any infinity to be positive
				sepTab.setValue(i, Float.POSITIVE_INFINITY);
			} else {
				sepTab.setValue(i, dummyTable.getValue(i));
			}
		}

//		dummyTable.directOpTab(
//			originalSeparatorTable,
//			PotentialTable.MINUS_OPERATOR);
		// the following code performs the above, but it forces that Float.POSITIVE_INFINITY
		// is treated specially: any operation with Float.POSITIVE_INFINITY will result in Float.POSITIVE_INFINITY.
		for (int k = dummyTable.tableSize()-1; k >= 0; k--) {
//			if (originalSeparatorTable.dataPT.data[k] == Float.POSITIVE_INFINITY) {
			if (Float.isInfinite(originalSeparatorTable.dataPT.data[k]) || Float.isInfinite(dummyTable.dataPT.data[k])) {
				// force a subtraction with infinity to result in positive infinity
				dummyTable.dataPT.data[k] = Float.POSITIVE_INFINITY;
			} else {
				dummyTable.dataPT.data[k] -= originalSeparatorTable.dataPT.data[k];
				if (dummyTable.dataPT.data[k] == Float.NEGATIVE_INFINITY) {
					throw new ZeroAssetsException("Overflow detected when absorbing " 
							+ clique2 + " in " + clique1 + ": " + dummyTable.dataPT.data);
				}
			}
		}
		
		/*
		 * Evidences are set by setting other states to very huge value (positive infinity).
		 * Currently, implementation is just multiplying the assets with Float.POSITIVE_INFINITY.
		 * This may result in negative finding if assets were negative.
		 * So, convert negative findings to positive findings here.
		 */
		for (int i = 0; i < clique1.getProbabilityFunction().tableSize(); i++) {
			if (clique1.getProbabilityFunction().getValue(i) == Float.NEGATIVE_INFINITY) {
				clique1.getProbabilityFunction().setValue(i, Float.POSITIVE_INFINITY);
			}
		}
		clique1.getProbabilityFunction().opTab(dummyTable, PotentialTable.PLUS_OPERATOR);
	}
	
	/**
	 * {@link #operate(float, float)} returns the minimum,
	 * without ignoring zeros or negatives.
	 * @author Shou Matsumoto
	 */
	public class MinOperation extends MinProductJunctionTree.MinOperation {
		/**
		 * Return the minimum, but ignores values less than or equals to 0.0f (except when both values are 0.0f).
		 * @return (arg1 < arg2)?arg1:arg2
		 */
		public float operate(float arg1, float arg2) {
			if (Float.isInfinite(arg1)) {
				return  arg2;
			} else if (Float.isInfinite(arg2)) {
				return arg1;
			}
			return (arg1 < arg2)?(arg1):(arg2);
		}
		
	}

	/**
	 * @see unbbayes.prs.bn.JunctionTree#addSeparator(unbbayes.prs.bn.Separator)
	 * @see LogarithmicMinProductJunctionTree#isToUseSeparatorsMap()
	 */
	public void addSeparator(Separator sep) {
		if (isToUseSeparatorsMap()) {
			super.addSeparator(sep);
		} else {
			getSeparators().add(sep);
		}
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.JunctionTree#removeSeparator(unbbayes.prs.bn.Separator)
	 * @see LogarithmicMinProductJunctionTree#isToUseSeparatorsMap()
	 */
	public void removeSeparator(Separator sep) {
		if (isToUseSeparatorsMap()) {
			super.removeSeparator(sep);
		} else {
			getSeparators().remove(sep);
		}
	}

	/**
	 * If {@link #isToUseSeparatorsMap()} is false, then it performs linear search. If true, then it delegates to superclass.
	 * @see unbbayes.prs.bn.JunctionTree#getSeparator(unbbayes.prs.bn.Clique, unbbayes.prs.bn.Clique)
	 * @see LogarithmicMinProductJunctionTree#isToUseSeparatorsMap()
	 */
	public Separator getSeparator(Clique clique1, Clique clique2) {
		if (isToUseSeparatorsMap()) {
			return super.getSeparator(clique1, clique2);
		} else {
			// do linear search. Call setToUseSeparatorsMap(true) and recompile Junction tree if you want to use indexes
			for (Separator separator : getSeparators()) {
				if (((separator.getClique1().getInternalIdentificator() == clique1.getInternalIdentificator()) && (separator.getClique2().getInternalIdentificator() == clique2.getInternalIdentificator()))
					|| ((separator.getClique2().getInternalIdentificator() == clique1.getInternalIdentificator()) && (separator.getClique1().getInternalIdentificator() == clique2.getInternalIdentificator()))) {
					return separator;
				}
			}
		}
		return null;
	}


	/**
	 * If false, {@link #getSeparatorsMap()} will not be used
	 * as an index for the separators given cliques (so that
	 * we can reduce memory usage by not using {@link #getSeparatorsMap()}).
	 * If true, {@link #addSeparator(Separator)},
	 * {@link #getSeparators()}, and {@link #removeSeparator(Separator)}
	 * will simply delegate to superclass.
	 * @return the isToUseSeparatorsMap
	 */
	public boolean isToUseSeparatorsMap() {
		return isToUseSeparatorsMap;
	}

	/**
	 * If false, {@link #getSeparatorsMap()} will not be used
	 * as an index for the separators given cliques (so that
	 * we can reduce memory usage by not using {@link #getSeparatorsMap()}).
	 * If true, {@link #addSeparator(Separator)},
	 * {@link #getSeparators()}, and {@link #removeSeparator(Separator)}
	 * will simply delegate to superclass.
	 * @param isToUseSeparatorsMap the isToUseSeparatorsMap to set
	 */
	public void setToUseSeparatorsMap(boolean isToUseSeparatorsMap) {
		if (!this.isToUseSeparatorsMap && isToUseSeparatorsMap) {
			// initialize separator map if necessary
			this.initializeSeparatorMap();
		} else if (this.isToUseSeparatorsMap && !isToUseSeparatorsMap) {
			// dispose separator map in this case
			this.setSeparatorsMap(null);
		}
		this.isToUseSeparatorsMap = isToUseSeparatorsMap;
	}

	/**
	 * Fills {@link #getSeparatorsMap()} properly.
	 * @see #setToUseSeparatorsMap(boolean)
	 * @see #getSeparators()
	 * @see #getSeparator(Clique, Clique)
	 */
	protected void initializeSeparatorMap() {
		// this is the new map to initialize
		Map<Clique, Set<Separator>> separatorMap = new HashMap<Clique, Set<Separator>>();
//		Map<Clique, Set<Separator>> separatorMap = new SeparatorMap();
		// simply iterate over all separators and fill separatorMap
		for (Separator sep : getSeparators()) {
			Set<Separator> seps = getSeparatorsMap().get(sep.getClique1());
			if (seps == null) {
				seps = new HashSet<Separator>();
				seps.add(sep);
				getSeparatorsMap().put(sep.getClique1(), seps);
			} else {
				seps.add(sep);
			}
			seps = getSeparatorsMap().get(sep.getClique2());
			if (seps == null) {
				seps = new HashSet<Separator>();
				seps.add(sep);
				getSeparatorsMap().put(sep.getClique2(), seps);
			} else {
				seps.add(sep);
			}
		}
		// update attribute
		this.setSeparatorsMap(separatorMap);
	}
	
//	/**
//	 * This is a map which is actually composed of two array lists: one for cliques, and another for separators.
//	 * It basically represents a mapping from {@link IRandomVariable#getInternalIdentificator()}
//	 * to a set of {@link IRandomVariable}
//	 * This is used in order to represent a mapping from integer to a set of objects.
//	 * The integer key is supposed to be fully sorted and can be negative.
//	 * The expected values of the Keys are: 
//	 * <br/> 
//	 * <br/> 
//	 * Positive {@link IRandomVariable#getInternalIdentificator()} (used by separators): 0,1,2,3... These will be stored in {@link #sepList}
//	 * <br/> 
//	 * CAUTION: {@link IRandomVariable#getInternalIdentificator()}  are expected to be a sequence with no "jumps" (i.e. if "3" is present, then 2,1, and 0 are always present; if
//	 * -3 is present, then -2 and -1 are always present).
//	 * This map replaces {@link HashMap} in order to reduce memory usage.
//	 * @author Shou Matsumoto
//	 */
//	public class SeparatorMap implements Map<Clique, Set<Separator>> {
//		/** This is the list representing the mapping of cliques to separators */
//		protected List<Set<Separator>> cliqueList = new ArrayList<Set<Separator>>(0);
//		
//		public int size() {
//			return cliqueList.size();
//		}
//
//		public boolean isEmpty() {
//			return cliqueList.isEmpty();
//		}
//
//		public boolean containsKey(Object key) {
//			if (key == null || !(key instanceof Clique)) {
//				return false;
//			}
//			int index = ((Clique)key).getInternalIdentificator();
//			if (index < 0) {
//				return false;
//			}
//			return index < cliqueList.size();
//		}
//
//		public boolean containsValue(Object value) {
//			return cliqueList.contains(value);
//		}
//
//		public Set<Separator> get(Object key) {
//			// check if key is valid
//			if (key == null || !(key instanceof Clique)) {
//				return null;
//			}
//			// extract index
//			int index = ((Clique)key).getInternalIdentificator();
//			if (index >= 0 && index < cliqueList.size()) {
//				// index is positive and within the size of cliqueList
//				return cliqueList.get(index);
//			} 
//			return null;
//		}
//
//		public Set<Separator> put(Clique clique, Set<Separator> value) {
//			List<Set<Separator>> listToAdd = cliqueList; // this is the list to be used to add new element
//			int index = clique.getInternalIdentificator();
//			while (index >= listToAdd.size()) {
//				// guarantee that index of the list is synchronized with key
//				listToAdd.add(null);
//			}
//			return listToAdd.set(index, value); // this will return null if index was greater than size
//		}
//
//		public Set<Separator> remove(Object key) {
//			// check if key is valid
//			if (key == null || !(key instanceof Clique)) {
//				return null;
//			}
//			// extract index
//			int index = ((Clique)key).getInternalIdentificator();
//			List<Set<Separator>> listToDelete = cliqueList; // this is the list to be used to add new element
//			if (index < 0 ) { 
//				// index is negative, so remove from sepList
//				listToDelete = sepList;
//				index = (-index - 1);	// convert to index of sepList
//			}
//			if (index < listToDelete.size()) {
//				return listToDelete.remove(index);
//			}
//			return null;
//		}
//
//		/**
//		 * Just iterates over m and calls {@link #put(Clique, Set<Separator>)}
//		 * @see java.util.Map#putAll(java.util.Map)
//		 */
//		public void putAll(Map<? extends Clique, ? extends Set<Separator>> m) {
//			for (Clique key : m.keySet()) {
//				put(key, m.get(key));
//			}
//		}
//
//		public void clear() {
//			cliqueList.clear();
//			sepList.clear();
//		}
//
//		/**
//		 * Creates a new set containing indexes of non-null elements in {@link #list}, and returns it
//		 * @see java.util.Map#keySet()
//		 */
//		public Set<Clique> keySet() {
//			Set<Clique> ret = new HashSet<Clique>(size());
//			for (int i = 0; i < cliqueList.size(); i++) {
//				if (cliqueList.get(i) != null) {
//					ret.add(i);
//				}
//			}
//			for (int i = 0; i < sepList.size(); i++) {
//				if (sepList.get(i) != null) {
//					ret.add(-(i+1));	// 0->-1; 1->-2; 2->-3...
//				}
//			}
//			return ret;
//		}
//
//		/**
//		 * returns content of {@link #cliqueList} concatenated with {@link #sepList}
//		 * @see java.util.Map#values()
//		 */
//		public Collection<Set<Separator>> values() {
//			Collection<Set<Separator>> ret = new ArrayList<Set<Separator>>(size());
//			ret.addAll(cliqueList);
//			ret.addAll(sepList);
//			return ret;
//		}
//
//		/**
//		 * Creates a new set containing indexes and values of non-null elements in {@link #list} and returns.
//		 * @see java.util.AbstractMap#entrySet()
//		 */
//		public Set<java.util.Map.Entry<Clique, Set<Separator>>> entrySet() {
//			Set<java.util.Map.Entry<Clique, Set<Separator>>> ret = new HashSet<Map.Entry<Clique,Set<Separator>>>();
//			for (int i = 0; i < cliqueList.size(); i++) {
//				Set<Separator> value = cliqueList.get(i);
//				if (value != null) {
//					ret.add(new SimpleEntry(i, value));
//				}
//			}
//			for (int i = 0; i < sepList.size(); i++) {
//				Set<Separator> value = sepList.get(i);
//				if (value != null) {
//					ret.add(new SimpleEntry(-(i+1), value)); // the index is converted as following: 0->-1; 1->-2; 2->-3...
//				}
//			}
//			return ret;
//		}
//	}
	
	/**
	 * If true, {@link #absorb(Clique, Clique)} will use singleton temporary lists in order to reduce garbage
	 * Turn this to false in concurrent calls of {@link #absorb(Clique, Clique)}
	 * @return the isToUseSingletonListsInAbsorb
	 */
	public static boolean isToUseSingletonListsInAbsorb() {
		return isToUseSingletonListsInAbsorb;
	}

	/**
	 * If true, {@link #absorb(Clique, Clique)} will use singleton temporary lists in order to reduce garbage.
	 * Turn this to false in concurrent calls of {@link #absorb(Clique, Clique)}
	 * @param isToUseSingletonListsInAbsorb the isToUseSingletonListsInAbsorb to set
	 */
	public static void setToUseSingletonListsInAbsorb(
			boolean isToUseSingletonListsInAbsorb) {
		LogarithmicMinProductJunctionTree.isToUseSingletonListsInAbsorb = isToUseSingletonListsInAbsorb;
	}

}
