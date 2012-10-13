/**
 * 
 */
package edu.gmu.ace.daggre;

import unbbayes.prs.bn.Clique;

/**
 * This extends {@link TradeSpecification}
 * in order to represent trades which are sensitive to
 * cliques.
 * A trade sensitive to clique can be a balancing trade (i.e.
 * an equalizer trade, or "exit" trade) which must make the assets
 * per state equals.
 * <br/><br/>
 * For example, let's use the following BN: E<-D->F
 * <br/><br/>
 * In the above BN, the cliques will be [D,E] and [D,F]. If both E and F resolves (and
 * are removed from the BN), then there will be 2 cliques with same contents ([D] and [D]).
 * {@link MarkovEngineInterface#getCash(long, java.util.List, java.util.List)},
 * {@link MarkovEngineInterface#scoreUserEv(long, java.util.List, java.util.List)}, and
 * {@link MarkovEngineInterface#getProbLists(java.util.List, java.util.List, java.util.List)}
 * won't have problems, because the junction tree of the probabilistic portion will be
 * globally consistent, cash will become globally consistent after min-propagation,
 * and expected scores will also be using global values (because of the product (sum,
 * in case of non-q space) of cliques divided (or subtracted,
 * in case of non-q space) by product (sum, in case of non-q space) of separators,
 * hence the value will be global.
 * Thus {@link MarkovEngineInterface#addTrade(Long, java.util.Date, String, TradeSpecification, boolean)}
 * won't have problems.
 * However {@link MarkovEngineInterface#addTrade(Long, java.util.Date, String, TradeSpecification, boolean)}
 * must equalize all cliques (in our example, it must equalize both cliques [D] and [D]),
 * so it must be able to pick both cliques and treat separately.
 * This interface supports the selection of the clique to consider.
 * <br/>
 * <br/>
 * {@link #isCliqueSensitive()} shall return true by default.
 * @author Shou Matsumoto
 */
public interface CliqueSensitiveTradeSpecification extends TradeSpecification {
	
	/**
	 * @return the clique to consider in a clique-sensitive operation
	 */
	public Clique getClique();

	/**
	 * @return Identication key of the clique. This may be an index.
	 */
	public Integer getCliqueId();
}
