package unbbayes.prs.mebn.ssbn.pruner.impl;

import java.util.ArrayList;
import java.util.List;

import unbbayes.prs.mebn.ssbn.SSBN;
import unbbayes.prs.mebn.ssbn.pruner.IPruneStructure;
import unbbayes.prs.mebn.ssbn.pruner.IPruner;

/**
 * Class to prune SSBN using pseudo-visitor patten
 * 
 * In order to use this class, we should add instances of IPruner to
 * a list of pruners and set it to this object using {@link #setListOfPruners(List)}.
 * 
 * By calling {@link #pruneStructure(SSBN)}, all instances of IPruner will be called.
 * 
 * @author Shou Matsumoto
 *
 */
public class PruneStructureImpl implements IPruneStructure{

	private List<IPruner> listOfPruners = null;
	
	/**
	 * default constructor is protected in order to make it easy to extend it
	 */
	protected PruneStructureImpl() {
		super();
		this.listOfPruners = new ArrayList<IPruner>();
	}
	
	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.mebn.ssbn.pruner.IPruneStructure#pruneStructure(unbbayes.prs.mebn.ssbn.SSBN)
	 */
	public void pruneStructure(SSBN ssbn) {
		for (IPruner pruner : this.getListOfPruners()) {
			try{
				pruner.prune(ssbn);
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Default construction method.
	 * Initializes default list of pruning methods.
	 * @return
	 */
	public static IPruneStructure newInstance() {
		PruneStructureImpl instance = new PruneStructureImpl();
		List<IPruner> pruners = new ArrayList<IPruner>(2);
		pruners.add(BarrenNodePruner.newInstance());
		pruners.add(DSeparationPruner.newInstance());
		instance.setListOfPruners(pruners);
		return instance;
	}
	
	/**
	 * Construction method initializing a list of pruners being called.
	 * 
	 * The list of pruners are called by this class sequentially,
	 * in order to prune the SSBN.
	 * By adding several IPruner into this list,
	 * you can customize this class' behavior.
	 * 
	 * Ex.
	 * 
	 *  By setting d-separation pruner and barren node pruner, it should remove d-separated nodes and barren nodes.
	 *  By setting only d-separation pruner, it should only remove d-separated nodes.
	 *  By setting an empty list, no prune would be done.
	 * 
	 * @return
	 */
	public static IPruneStructure newInstance(List<IPruner> listOfPruners){
		PruneStructureImpl instance = new PruneStructureImpl();
		instance.setListOfPruners(listOfPruners);
		return instance;
	}

	/**
	 * This list of pruners are called by this class sequentially,
	 * in order to prune the SSBN.
	 * By adding several IPruner into this list,
	 * you can customize this class' behavior.
	 * 
	 * Ex.
	 * 
	 *  By setting d-separation pruner and barren node pruner, it should remove d-separated nodes and barren nodes.
	 *  By setting only d-separation pruner, it should only remove d-separated nodes.
	 *  By setting an empty list, no prune would be done.
	 * @return the listOfPruners
	 */
	public List<IPruner> getListOfPruners() {
		return listOfPruners;
	}

	/**
	 *  This list of pruners are called by this class sequencially,
	 * in order to prune the SSBN.
	 * By adding several IPruner into this list,
	 * you can customize this class' behavior.
	 * 
	 * Ex.
	 * 
	 *  By setting d-separation pruner and barren node pruner, it should remove d-separated nodes and barren nodes.
	 *  By setting only d-separation pruner, it should only remove d-separated nodes.
	 *  By setting an empty list, no prune would be done.
	 * @param listOfPruners the listOfPruners to set
	 */
	public void setListOfPruners(List<IPruner> listOfPruners) {
		this.listOfPruners = listOfPruners;
	}

}
