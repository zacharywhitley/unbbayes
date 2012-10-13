/**
 * 
 */
package edu.gmu.ace.daggre;

import java.util.List;

import unbbayes.prs.bn.Clique;


public class CliqueSensitiveTradeSpecificationImpl extends TradeSpecificationImpl implements CliqueSensitiveTradeSpecification{

	
	private static final long serialVersionUID = 8887490592103710973L;
	
	private Clique clique;
	
	/**
	 * Default constructor with no fields.
	 * This shall remain public, in order to adhere to Beans design pattern
	 */
	public CliqueSensitiveTradeSpecificationImpl() {
		super();
	}


	/**
	 * Default constructor initializing fields
	 * @param userId
	 * @param questionId
	 * @param probabilities
	 * @param assumptionIds
	 * @param assumedStates
	 * @param Clique
	 * @see TradeSpecificationImpl
	 */
	public CliqueSensitiveTradeSpecificationImpl(Long userId, Long questionId,
			List<Float> probabilities, List<Long> assumptionIds,
			List<Integer> assumedStates, Clique clique) {
		super(userId, questionId, probabilities, assumptionIds, assumedStates);
		this.clique = clique;
	}
	


	/**
	 * @return the clique to consider in a clique-sensitive operation
	 */
	public Clique getClique() {
		return clique;
	}


	/**
	 * @param clique : the clique to consider in a clique-sensitive operation
	 */
	public void setClique(Clique clique) {
		this.clique = clique;
	}

	/**
	 * Just returns the {@link Clique#getInternalIdentificator()}
	 * @see edu.gmu.ace.daggre.CliqueSensitiveTradeSpecificationImpl#getCliqueId()
	 */
	public Integer getCliqueId() {
		return this.clique.getInternalIdentificator();
	}


	
}

