package edu.gmu.ace.scicast;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import unbbayes.prs.bn.inference.extension.ZeroAssetsException;




/**
 * This is an interface with methods for accessing 
 * functionalities related to the Markov engine
 * of DAGGRE project. 
 * <br/><br/>
 * 
 * The objective of this interface is to provide a simplified way of accessing the UnBBayes' features
 * related to DAGGRE project (i.e. bayesian network management, soft evidences, and asset management), and
 * to encapsulate the complex data structure related to the bayesian network and asset tables (i.e.
 * instead of "seeing" the complex classes, access thoese functionalities by only using primitive datatypes and
 * standard classes).
 * <br/><br/>
 * 
 * Basically, this interface provides
 * methods for initializing a bayesian network and associated
 * asset structure, managing and updating those structures,
 * compute some secondary parameters (e.g. whether a user is
 * in a long/short position, compute edit limit, compute minimum and expected assets,
 * perform conditional bets, etc).
 * <br/><br/>
 * The methods here are based on the document 
 * <a href="https://docs.google.com/document/d/1_PsNiVARUx3bUUE9ffoXYM-o627fEGabbiI-bRSR2ho/edit">https://docs.google.com/document/d/1_PsNiVARUx3bUUE9ffoXYM-o627fEGabbiI-bRSR2ho/edit</a>,
 * 
 * <br/><br/>
 * Some methods present here were also based on document 
 * <a href="https://docs.google.com/document/d/1p1TY-paqEmJNshQYThr6H3SyR2-e6xmoXrI9HleqiPM/edit">https://docs.google.com/document/d/1p1TY-paqEmJNshQYThr6H3SyR2-e6xmoXrI9HleqiPM/edit</a>,
 * which indicates the following functionalities:<br/>
 * 
 * - Compute long/short position <br/>
 * - Compute edit limits <br/>
 * - Compute compute min-q value (or, alternatively, compute min asset value)<br/>
 * - Compute expected asset value
 * <br/><br/>
 * 
 * This interface also provides some methods for accessing the Bayesian Network data structure
 * and asset tables using only default types provided by the JRE, 
 * such as primary data types (e.g. int, float, String) and java Collections framework
 * (e.g. {@link java.util.List}).
 * <br/><br/>
 * 
 * As a general rule, methods using parameters int networkID, and long userID should
 * lock access to the shared bayesian network and user's asset network respectively.
 * 
 * @author Shou Matsumoto
 *
 */
public interface MarkovEngineInterface {
	
	/** Name of the property representing the total or partial score ev in the properties returned by {@link #getScoreSummary(long, Long, List, List)} */
	public static final String SCOREEV_PROPERTY = "SCOREEV_PROPERTY";
	
	/** Name of the property representing the cash in the 1st property returned by {@link #getScoreSummary(long, Long, List, List)} */
	public static final String CASH_PROPERTY = "CASH_PROPERTY";
	
	/** 
	 * Name of the property indicating how many elements in the list returned by {@link #getScoreSummary(long, Long, List, List)} 
	 * are positive contributions to the final score EV (the rest are intersections).
	 */
	public static final String SCORE_COMPONENT_SIZE_PROPERTY = "SCORE_COMPONENT_SIZE_PROPERTY";

	/** 
	 * Name of the property representing the comma-separated list of questions related to
	 * the score EV components returned by {@link #getScoreSummary(long, Long, List, List)} 
	 */
	public static final String QUESTIONS_PROPERTY = "QUESTIONS_PROPERTY";

	/** 
	 * Name of the property representing the comma-separated list of states related to
	 * the questions of the property {@link #QUESTIONS_PROPERTY} in the
	 * list returned by {@link #getScoreSummary(long, Long, List, List)} 
	 */
	public static final String STATES_PROPERTY = "STATES_PROPERTY";
	
	/**
	 * This operation indicates that the source system is requesting a complete restart/reset of all operations so all historical operations should be purged. 
	 * This will occur when a client resets and plans to resubmit all actions. 
	 * @return True if operation was successful.
	 */
	public boolean initialize();
	
	/**
	 * This function assumes that a series of time sequenced actions that may modify the network are expected to be made. 
	 * Any method using a transactionKey will be called between this method and {@link #commitNetworkActions(long)}.
	 * @return transactionKey to indicate a transaction that these sets of operations are in. 
	 * The transactionKey is needed to support multiple front-ends that may be operating to modify the network.
	 * @see #addCash(long, Date, long, float, String)
	 * @see #addQuestion(long, Date, long, int, List)
	 * @see #addQuestionAssumption(long, Date, long, long, List)
	 * @see #addTrade(long, Date, long, long, long, List, List, List, List, Boolean)
	 * @see #commitNetworkActions(long)
	 * @see #resolveQuestion(long, Date, long, int)
	 * @see #revertTrade(long, Date, Long, Long)
	 */
	public long startNetworkActions();
	
	/**
	 * This function notifies the MarkovEngine that the series of actions begun with startNetworkActions is completed.
	 * Implementations of this method shall be synchronized.
	 * Any method using a transactionKey will be called between {@link #startNetworkActions()} and this method.
	 * @param transactionKey : key returned by {@link #startNetworkActions()}
	 * @return True if operation was successful
	 * @throws IllegalArgumentException when transactionKey was invalid.
	 * @throws
	 * @see #addCash(long, Date, long, float, String)
	 * @see #addQuestion(long, Date, long, int, List)
	 * @see #addQuestionAssumption(long, Date, long, long, List)
	 * @see #addTrade(long, Date, long, long, long, List, List, List, List, Boolean)
	 * @see #startNetworkActions()
	 * @see #resolveQuestion(long, Date, long, int)
	 * @see #revertTrade(long, Date, Long, Long)
	 */
	public boolean commitNetworkActions(long transactionKey) throws IllegalArgumentException, ZeroAssetsException;
	

	/**
	 * This method adds a new question (i.e. a new node in a bayesian network).
	 * Implementations of this method must be synchronized.
	 * This function creates a new question in the network. 
	 * It is not required for the MarkovEngine to know the specific strings assigned to the states or question descriptions. 
	 * Implementations may require reinitialization of the bayesian network in order to guarantee efficiency,
	 * because uncontrolled inclusion of nodes in implementations based on junction trees may cause 
	 * the tree to become either too huge, sparse, or generate huge cliques.
	 * @param transactionKey : key returned by {@link #startNetworkActions()}
	 * @param occurredWhen : implementations of this interface may use this timestamp to store a history of modifications.
	 * @param questionId : the ID of the new question.
	 * @param numberStates : quantity of possible states (choices) for this question
	 * @param initProbs : the prior probability of this node. If set to null, uniform distribution shall be used.
	 * @return true if operation was successful.
	 * @throws IllegalArgumentException when a key or Id is not found or a range of values or cpd�fs is not legal (i.e. newValues > 100%)
	 */
	public boolean addQuestion(Long transactionKey, Date occurredWhen, long questionId, int numberStates, List<Float> initProbs) throws IllegalArgumentException;
	
	/**
	 * This method adds a new question (i.e. a new node in a bayesian network).
	 * Implementations of this method must be synchronized.
	 * This function creates a new question in the network. 
	 * It is not required for the MarkovEngine to know the specific strings assigned to the states or question descriptions. 
	 * Implementations may require reinitialization of the bayesian network in order to guarantee efficiency,
	 * because uncontrolled inclusion of nodes in implementations based on junction trees may cause 
	 * the tree to become either too huge, sparse, or generate huge cliques.
	 * @param transactionKey : key returned by {@link #startNetworkActions()}
	 * @param occurredWhen : implementations of this interface may use this timestamp to store a history of modifications.
	 * @param questionId : the ID of the new question.
	 * @param numberStates : quantity of possible states (choices) for this question
	 * @param initProbs : the prior probability of this node. If set to null, uniform distribution shall be used.
	 * @param structure : will be Null in most cases but for special structure cases will be an encoding containing advanced rules for the inference engine.
	 * For a ValueTree based structure (initially at least): 
	 * <br/>
	 * <br/>
	 * 1 - The numberStates will be the number of exposed states in the underlying structure, that must be assumable.
	 * <br/>
	 * <br/>
	 * 2 - The structure  will contain 3 parts,the kind (ValueTree), the physical structure of the network and the mappings to the exposed (shadow) states
	 * <br/>
	 * <br/>
	 * The structure is likely to be a list of lists describing the network, for example : 
	 * <br/>
	 * [3 [ 4 [ 3 [ 31 28 30] [..... ].....    3 years, 4 q in first year, 3 m in first quarter, 31, 28 30 days by month , etc...
	 * And then a list of mappings to the shadow nodes, for example if we exposed the quarters: 
	 * [0,0 ], [0,1],[0,2],[0,3],[1,0], [1,1],[1,2],[1,3],[2,0], [2,1],[2,2],[2,3]
	 * 
	 * @return true if operation was successful.
	 * @throws IllegalArgumentException when a key or Id is not found or a range of values or cpd�fs is not legal (i.e. newValues > 100%)
	 */
	public boolean addQuestion(Long transactionKey, Date occurredWhen, long questionId, int numberStates, List<Float> initProbs, String structure ) throws IllegalArgumentException;
	
	/**
	 * This function creates a new assumptive link in the network between 2 questions. 
	 * It is not required for the MarkovEngine to know the specific strings assigned to the states or question descriptions. 
	 * Implementations of this method must be synchronized.
	 * 
	 * This method adds a new direct dependency (i.e. an edge between nodes in a bayesian network).
	 * Note that this method may require
	 * recompilation of the bayesian network (i.e. may need to generate a new junction tree).
	 * <br/>
	 * By default, this is a lazy method (in a sense that it just changes the structure in a way that it causes minimum impact on user's asset tables, without
	 * optimizing the junction tree structure or user's asset table structures). 
	 * @param transactionKey : key returned by {@link #startNetworkActions()}, or null in order to immediately commit.
	 * @param occurredWhen : implementations of this interface may use this timestamp to store a history of modifications.
	 * @param childQuestionId : id of the child
	 * @param parentQuestionIds : ids of the parents. If cpd is set to null, then these questions will be ADDED as parents of childQuestionId.
	 * If cpd is non-null, then these parents will SUBSTITUTE the old parents.
	 * @param cpd : If a null/empty cpd is passed, values will be set by uniform distribution, and parentQuestionIds will
	 * be ADDED as parents of childQuestionId. If non-null, then parentQuestionIds will SUBSTITUTE the old parents of childQuestionId.
	 * <br/>
	 * This is a list (ordered collection) representing the conditional probability distribution after the edit. 
	 * For example, suppose T is the target random variable (i.e. question identified by questionID) with states t1 and t2, and A1 and A2 are assumptions with states (a11, a12), and (a21 , a22) respectively.
	 * Then, the list must be filled as follows:<br/>
	 * index 0 - P(T=t1 | A1=a11, A2=a21)<br/>
	 * index 1 - P(T=t2 | A1=a11, A2=a21)<br/>
	 * index 2 - P(T=t1 | A1=a12, A2=a21)<br/>
	 * index 3 - P(T=t2 | A1=a12, A2=a21)<br/>
	 * index 4 - P(T=t1 | A1=a11, A2=a22)<br/>
	 * index 5 - P(T=t2 | A1=a11, A2=a22)<br/>
	 * index 6 - P(T=t1 | A1=a12, A2=a22)<br/>
	 * index 7 - P(T=t2 | A1=a12, A2=a22)<br/>
	 * @return true if operation was successful
	 * @throws IllegalArgumentException
	 */
	public boolean addQuestionAssumption(Long transactionKey, Date occurredWhen, long childQuestionId, List<Long> parentQuestionIds,  List<Float> cpd) throws IllegalArgumentException;
	
	/**
	 * This is basically the opposite operation of {@link #addQuestionAssumption(Long, Date, long, List, List)}.
	 * In other words, this deletes links from the underlying model.
	 * @param transactionKey : key returned by {@link #startNetworkActions()}, or null in order to immediately commit.
	 * @param occurredWhen : implementations of this interface may use this timestamp to store a history of modifications.
	 * @param childQuestionId : id of the child
	 * @param parentQuestionIds : ids of the parents.
	 * @return true if operation was successful.
	 * @throws IllegalArgumentException
	 */
	public boolean removeQuestionAssumption(Long transactionKey, Date occurredWhen, long childQuestionId, List<Long> parentQuestionIds) throws IllegalArgumentException;
	
	/**
	 * This function will add EXTERNAL cash to a specific userId. 
	 * Usually this will be done externally at startup for each user and additionally as manna occurs. 
	 * This operation may block write access to the specific userId.
	 * Implementations shall be synchronized.
	 * Technically, this method modifies the contents of all cells in the asset table in order to guarantee
	 * that the min asset returned by {@link #getCash(long, Properties)} is increased (decreased, if negative value is provided)
	 * by value specified by the argument "value".
	 * @param transactionKey : key returned by {@link #startNetworkActions()}
	 * @param occurredWhen : implementations of this interface may use this timestamp to store a history of modifications.
	 * @param userId : the ID of the owner of the asset table.
	 * @param assets : the minimum asset value to be added into the current value (negative values will decrease the current value)
	 * @param description : a brief description of what this change of cash mean.
	 * @return true if operation was successful.
	 * @throws IllegalArgumentException when any argument was invalid (e.g. ids were invalid).
	 */
	public boolean addCash(Long transactionKey, Date occurredWhen, long userId, float assets, String description) throws IllegalArgumentException;
	
	/**
	 * This function will add a specific trade to the system. 
	 * Implementations shall be synchronized.
	 * This feature is also described in
	 * <a href="https://docs.google.com/document/d/1p1TY-paqEmJNshQYThr6H3SyR2-e6xmoXrI9HleqiPM/edit">https://docs.google.com/document/d/1p1TY-paqEmJNshQYThr6H3SyR2-e6xmoXrI9HleqiPM/edit</a>
	 * as follows:
	 * <br/><br/>
	 * We use a Bayesian network (BN) to represent the prediction market of our interest. 
	 * And we use the BN inference algorithm (Junction tree) to incorporate whatever edit P*(T=t|A=a), which is viewed as conditional soft evidence for the BN. 
	 * Our particular procedure is the following:
	 * <br/><br/>
	 * First, add a binary dummy node called Dummy to the network, with {T, A} as its parents. 
	 * The CPT for Dummy can be calculated by the equation: P*(T=t|A=a)/P(T=t|A=a). For P*(T=state other than t|A=a), we change it proportionally based on the original probabilities.
	 * <br/><br/>
	 * Note that Dummy is always observed at state 1. Call inference engine on this new network to update consensus probabilities for all cliques and separators, and save them into engine. 
	 * Note that implementations may also update all CPDs for the original network with the ones from the new network with dummy node to be observed at state 1
	 * (i.e. implementations are free to keep the dummy node or remove it, making sure that its remotion will not revert the changes). 
	 * <br/><br/>
	 * Identify the cliques containing A and/or T, update the q-table for the clique by Equation (4). 
	 * <br/><br/>
	 * Calculate the overall expected score after the edit. 
	 * <br/><br/>
	 * Calculate the global min-q value after the edit by min-q-propagation.
	 * 
	 * @param transactionKey : key returned by {@link #startNetworkActions()}
	 * @param occurredWhen : implementations of this interface may use this timestamp to store a history of modifications.
	 * @param tradeKey : revert and history functions can refer to specific trade actions easier, by referring to this key (identifier).
	 * @param userId : the ID of the user (i.e. owner of the assets).
	 * @param questionId : the id of the question to be edited (i.e. the random variable "T"  in the example)
	 * 
	 * @param newValues : this is a list (ordered collection) representing the probability values after the edit. 
	 * For example, suppose T is the target question (i.e. a random variable) with states t1 and t2, and A1 and A2 are assumptions with states (a11, a12), and (a21 , a22) respectively.
	 * Then, the list must be filled as follows:<br/>
	 * index 0 - P(T=t1 | A1=a11, A2=a21)<br/>
	 * index 1 - P(T=t2 | A1=a11, A2=a21)<br/>
	 * index 2 - P(T=t1 | A1=a12, A2=a21)<br/>
	 * index 3 - P(T=t2 | A1=a12, A2=a21)<br/>
	 * index 4 - P(T=t1 | A1=a11, A2=a22)<br/>
	 * index 5 - P(T=t2 | A1=a11, A2=a22)<br/>
	 * index 6 - P(T=t1 | A1=a12, A2=a22)<br/>
	 * index 7 - P(T=t2 | A1=a12, A2=a22)<br/>
	 * <br/>
	 * If the states of the conditions are specified in assumedStates, then this list will only specify the conditional
	 * probabilities of each states of questionID.
	 * E.g. Again, suppose T is the target question with states t1 and t2, and A1 and A2 are assumptions with states (a11, a12), and (a21 , a22) respectively.]
	 * Also suppose that assumedStates = (1,0). Then, the content of newValues must be: <br/>
	 * index 0 - P(T=t1 | A1=a12, A2=a21)<br/>
	 * index 1 - P(T=t2 | A1=a12, A2=a21)<br/>
	 * @param assumptionIds : list (ordered collection) representing the IDs of the questions to be assumed in this edit. The order is important,
	 * because the ordering in this list will be used in order to identify the correct indexes in "newValues".
	 * @param assumedStates : this shall be null if newValues contains full data (all cells of the conditional probability distribution).
	 * If not null, this list indicates which states the nodes in assumptionIds are.
	 * If negative, then "not" Math.abs(state + 1) will be considered as the state (i.e. the state Math.abs(state + 1) will be
	 * considered as 0%).
	 * @param allowNegative : If true (default is False), then checks for sufficient assets should be bypassed and we allow 
	 * the user to go into the hole
	 * @return the assets per state changed, if the user has sufficient assets 
	 * (as the values returned by {@link #getAssetsIfStates(int, long, long, int, List, List, Properties)}).
	 * If user doesn't have sufficient assets, it will return null.
	 * If the result of the trade cannot be previewed now (e.g. it is adding a trade to a question which is still going to be created in the same transaction), 
	 * it will return an empty list.
	 * @throws IllegalArgumentException when any argument was invalid (e.g. ids were invalid).
	 * @deprecated use {@link #addTrade(Long, Date, String, TradeSpecification, boolean)} instead
	 */
	public List<Float> addTrade(Long transactionKey, Date occurredWhen, String tradeKey, long userId, long questionId, List<Float> newValues, List<Long> assumptionIds, List<Integer> assumedStates,  boolean allowNegative) throws IllegalArgumentException;
	
	/**
	 * This function will add a specific trade to the system. 
	 * Implementations shall be synchronized.
	 * This feature is also described in
	 * <a href="https://docs.google.com/document/d/1p1TY-paqEmJNshQYThr6H3SyR2-e6xmoXrI9HleqiPM/edit">https://docs.google.com/document/d/1p1TY-paqEmJNshQYThr6H3SyR2-e6xmoXrI9HleqiPM/edit</a>
	 * as follows:
	 * <br/><br/>
	 * We use a Bayesian network (BN) to represent the prediction market of our interest. 
	 * And we use the BN inference algorithm (Junction tree) to incorporate whatever edit P*(T=t|A=a), which is viewed as conditional soft evidence for the BN. 
	 * Our particular procedure is the following:
	 * <br/><br/>
	 * First, add a binary dummy node called Dummy to the network, with {T, A} as its parents. 
	 * The CPT for Dummy can be calculated by the equation: P*(T=t|A=a)/P(T=t|A=a). For P*(T=state other than t|A=a), we change it proportionally based on the original probabilities.
	 * <br/><br/>
	 * Note that Dummy is always observed at state 1. Call inference engine on this new network to update consensus probabilities for all cliques and separators, and save them into engine. 
	 * Note that implementations may also update all CPDs for the original network with the ones from the new network with dummy node to be observed at state 1
	 * (i.e. implementations are free to keep the dummy node or remove it, making sure that its remotion will not revert the changes). 
	 * <br/><br/>
	 * Identify the cliques containing A and/or T, update the q-table for the clique by Equation (4). 
	 * <br/><br/>
	 * Calculate the overall expected score after the edit. 
	 * <br/><br/>
	 * Calculate the global min-q value after the edit by min-q-propagation.
	 * 
	 * @param transactionKey : key returned by {@link #startNetworkActions()}
	 * @param occurredWhen : implementations of this interface may use this timestamp to store a history of modifications.
	 * @param tradeKey : revert and history functions can refer to specific trade actions easier, by referring to this key (identifier).
	 * @param userId : the ID of the user (i.e. owner of the assets).
	 * @param questionId : the id of the question to be edited (i.e. the random variable "T"  in the example)
	 * 
	 * @param oldValues : this is a list (ordered collection) representing the probability values before the edit. 
	 * If the current probability is not this value, then a correction trade shall be made in order to set the current probability
	 * to this value.
	 * If null, the current probabilities will be used, as in {@link #addTrade(Long, Date, String, long, long, List, List, List, boolean)}.
	 * @param newValues : this is a list (ordered collection) representing the probability values after the edit. 
	 * For example, suppose T is the target question (i.e. a random variable) with states t1 and t2, and A1 and A2 are assumptions with states (a11, a12), and (a21 , a22) respectively.
	 * Then, the list must be filled as follows:<br/>
	 * index 0 - P(T=t1 | A1=a11, A2=a21)<br/>
	 * index 1 - P(T=t2 | A1=a11, A2=a21)<br/>
	 * index 2 - P(T=t1 | A1=a12, A2=a21)<br/>
	 * index 3 - P(T=t2 | A1=a12, A2=a21)<br/>
	 * index 4 - P(T=t1 | A1=a11, A2=a22)<br/>
	 * index 5 - P(T=t2 | A1=a11, A2=a22)<br/>
	 * index 6 - P(T=t1 | A1=a12, A2=a22)<br/>
	 * index 7 - P(T=t2 | A1=a12, A2=a22)<br/>
	 * <br/>
	 * If the states of the conditions are specified in assumedStates, then this list will only specify the conditional
	 * probabilities of each states of questionID.
	 * E.g. Again, suppose T is the target question with states t1 and t2, and A1 and A2 are assumptions with states (a11, a12), and (a21 , a22) respectively.]
	 * Also suppose that assumedStates = (1,0). Then, the content of newValues must be: <br/>
	 * index 0 - P(T=t1 | A1=a12, A2=a21)<br/>
	 * index 1 - P(T=t2 | A1=a12, A2=a21)<br/>
	 * @param assumptionIds : list (ordered collection) representing the IDs of the questions to be assumed in this edit. The order is important,
	 * because the ordering in this list will be used in order to identify the correct indexes in "newValues".
	 * @param assumedStates : this shall be null if newValues contains full data (all cells of the conditional probability distribution).
	 * If not null, this list indicates which states the nodes in assumptionIds are.
	 * If negative, then "not" Math.abs(state + 1) will be considered as the state (i.e. the state Math.abs(state + 1) will be
	 * considered as 0%).
	 * @param allowNegative : If true (default is False), then checks for sufficient assets should be bypassed and we allow 
	 * the user to go into the hole
	 * @return the assets per state changed, if the user has sufficient assets 
	 * (as the values returned by {@link #getAssetsIfStates(int, long, long, int, List, List, Properties)}).
	 * If user doesn't have sufficient assets, it will return null.
	 * If the result of the trade cannot be previewed now (e.g. it is adding a trade to a question which is still going to be created in the same transaction), 
	 * it will return an empty list.
	 * @throws IllegalArgumentException when any argument was invalid (e.g. ids were invalid).
	 * @deprecated use {@link #addTrade(Long, Date, String, TradeSpecification, boolean)} instead
	 */
	public List<Float> addTrade(Long transactionKey, Date occurredWhen, String tradeKey, long userId, long questionId, List<Float> oldValues, List<Float> newValues, List<Long> assumptionIds, List<Integer> assumedStates,  boolean allowNegative) throws IllegalArgumentException;

	/**
	 * This method will add a specific trade to the system. 
	 * Implementations shall be synchronized.
	 * This feature is also described in
	 * <a href="https://docs.google.com/document/d/1p1TY-paqEmJNshQYThr6H3SyR2-e6xmoXrI9HleqiPM/edit">https://docs.google.com/document/d/1p1TY-paqEmJNshQYThr6H3SyR2-e6xmoXrI9HleqiPM/edit</a>
	 * as follows:
	 * <br/><br/>
	 * We use a Bayesian network (BN) to represent the prediction market of our interest. 
	 * And we use the BN inference algorithm (Junction tree) to incorporate whatever edit P*(T=t|A=a), which is viewed as conditional soft evidence for the BN. 
	 * Our particular procedure is the following:
	 * <br/><br/>
	 * First, add a binary dummy node called Dummy to the network, with {T, A} as its parents. 
	 * The CPT for Dummy can be calculated by the equation: P*(T=t|A=a)/P(T=t|A=a). For P*(T=state other than t|A=a), we change it proportionally based on the original probabilities.
	 * <br/><br/>
	 * Note that Dummy is always observed at state 1. Call inference engine on this new network to update consensus probabilities for all cliques and separators, and save them into engine. 
	 * Note that implementations may also update all CPDs for the original network with the ones from the new network with dummy node to be observed at state 1
	 * (i.e. implementations are free to keep the dummy node or remove it, making sure that its remotion will not revert the changes). 
	 * <br/><br/>
	 * Identify the cliques containing A and/or T, update the q-table for the clique by Equation (4). 
	 * <br/><br/>
	 * Calculate the overall expected score after the edit. 
	 * <br/><br/>
	 * Calculate the global min-q value after the edit by min-q-propagation.
	 * 
	 * @param transactionKey : key returned by {@link #startNetworkActions()}
	 * @param occurredWhen : implementations of this interface may use this timestamp to store a history of modifications.
	 * @param tradeKey : revert and history functions can refer to specific trade actions easier, by referring to this key (identifier).
	 * @param tradeSpecification : instance of {@link TradeSpecification} which specifies the user ID, question ID, 
	 * the probability values, the IDs of assumed questions and the assumed states.
	 * @param allowNegative : If true (default is False), then checks for sufficient assets should be bypassed and we allow 
	 * the user to go into the hole
	 * @return the assets per state changed, if the user has sufficient assets 
	 * (as the values returned by {@link #getAssetsIfStates(int, long, long, int, List, List, Properties)}).
	 * If user doesn't have sufficient assets, it will return null.
	 * If the result of the trade cannot be previewed now (e.g. it is adding a trade to a question which is still going to be created in the same transaction), 
	 * it will return an empty list.
	 * @throws IllegalArgumentException when any argument was invalid (e.g. ids were invalid).
	 * @see #addTrade(Long, Date, long, List, List, List, List, List)
	 */
	public List<Float> addTrade(Long transactionKey, Date occurredWhen, String tradeKey, TradeSpecification tradeSpecification,  boolean allowNegative) throws IllegalArgumentException;

	
	/**
	 * This function will add a specific trade to the system. 
	 * Implementations shall be synchronized.
	 * This feature is also described in
	 * <a href="https://docs.google.com/document/d/1p1TY-paqEmJNshQYThr6H3SyR2-e6xmoXrI9HleqiPM/edit">https://docs.google.com/document/d/1p1TY-paqEmJNshQYThr6H3SyR2-e6xmoXrI9HleqiPM/edit</a>
	 * as follows:
	 * <br/><br/>
	 * We use a Bayesian network (BN) to represent the prediction market of our interest. 
	 * And we use the BN inference algorithm (Junction tree) to incorporate whatever edit P*(T=t|A=a), which is viewed as conditional soft evidence for the BN. 
	 * Our particular procedure is the following:
	 * <br/><br/>
	 * First, add a binary dummy node called Dummy to the network, with {T, A} as its parents. 
	 * The CPT for Dummy can be calculated by the equation: P*(T=t|A=a)/P(T=t|A=a). For P*(T=state other than t|A=a), we change it proportionally based on the original probabilities.
	 * <br/><br/>
	 * Note that Dummy is always observed at state 1. Call inference engine on this new network to update consensus probabilities for all cliques and separators, and save them into engine. 
	 * Note that implementations may also update all CPDs for the original network with the ones from the new network with dummy node to be observed at state 1
	 * (i.e. implementations are free to keep the dummy node or remove it, making sure that its remotion will not revert the changes). 
	 * <br/><br/>
	 * Identify the cliques containing A and/or T, update the q-table for the clique by Equation (4). 
	 * <br/><br/>
	 * Calculate the overall expected score after the edit. 
	 * <br/><br/>
	 * Calculate the global min-q value after the edit by min-q-propagation.
	 * 
	 * @param transactionKey : key returned by {@link #startNetworkActions()}
	 * @param occurredWhen : implementations of this interface may use this timestamp to store a history of modifications.
	 * @param tradeKey : revert and history functions can refer to specific trade actions easier, by referring to this key (identifier).
	 * @param userId : the ID of the user (i.e. owner of the assets).
	 * @param questionId : the id of the question to be edited (i.e. the random variable "T"  in the example)
	 * 
	 * @param oldValues : this is a list (ordered collection) representing the probability values before the edit. 
	 * If the current probability is not this value, then a correction trade shall be made in order to set the current probability
	 * to this value.
	 * If null, the current probabilities will be used, as in {@link #addTrade(Long, Date, String, long, long, List, List, List, boolean)}.
	 * @param newValues : this is a list (ordered collection) representing the probability values after the edit. 
	 * For example, suppose T is the target question (i.e. a random variable) with states t1 and t2, and A1 and A2 are assumptions with states (a11, a12), and (a21 , a22) respectively.
	 * Then, the list must be filled as follows:<br/>
	 * index 0 - P(T=t1 | A1=a11, A2=a21)<br/>
	 * index 1 - P(T=t2 | A1=a11, A2=a21)<br/>
	 * index 2 - P(T=t1 | A1=a12, A2=a21)<br/>
	 * index 3 - P(T=t2 | A1=a12, A2=a21)<br/>
	 * index 4 - P(T=t1 | A1=a11, A2=a22)<br/>
	 * index 5 - P(T=t2 | A1=a11, A2=a22)<br/>
	 * index 6 - P(T=t1 | A1=a12, A2=a22)<br/>
	 * index 7 - P(T=t2 | A1=a12, A2=a22)<br/>
	 * <br/>
	 * If the states of the conditions are specified in assumedStates, then this list will only specify the conditional
	 * probabilities of each states of questionID.
	 * E.g. Again, suppose T is the target question with states t1 and t2, and A1 and A2 are assumptions with states (a11, a12), and (a21 , a22) respectively.]
	 * Also suppose that assumedStates = (1,0). Then, the content of newValues must be: <br/>
	 * index 0 - P(T=t1 | A1=a12, A2=a21)<br/>
	 * index 1 - P(T=t2 | A1=a12, A2=a21)<br/>
	 * <br/>
	 * <br/>
	 * In the case of a value Tree the newValues may be a specific probability (with other states being Null) or a full list of probabilities 
	 * for the nodes. 
	 * @param assumptionIds : list (ordered collection) representing the IDs of the questions to be assumed in this edit. The order is important,
	 * because the ordering in this list will be used in order to identify the correct indexes in "newValues".
	 * @param assumedStates : this shall be null if newValues contains full data (all cells of the conditional probability distribution).
	 * If not null, this list indicates which states the nodes in assumptionIds are.
	 * If negative, then "not" Math.abs(state + 1) will be considered as the state (i.e. the state Math.abs(state + 1) will be
	 * considered as 0%).
	 * @param allowNegative : If true (default is False), then checks for sufficient assets should be bypassed and we allow 
	 * the user to go into the hole.
	 * 
	 * @param targetPath : the path to target value tree node to make the trade, indicating the path from the root. If [1,0,2], then
	 * it is the child 2 of child 0 of child 1 of root (i.e. root->child1->child0->child2).
	 * 
	 * @param referencePath : the anchor value tree node to assume. the path (as a list of integer children to traverse the tree) 
	 * that we are conditioning the target on. 
	 * If [1,0,2], then it is the child 2 of child 0 of child 1 of root (i.e. root->child1->child0->child2).
	 * If null then the target’s direct parent is used.
	 * <br/>
	 * <br/>
	 * For a ValueTree based structure (initially at least):
	 * targetPath specified the path (as a list of integer children to traverse the tree) that we are trading on or a specific element. 
	 * In cases where the referencePath is not null, the newValues are a partial probability and the ME will calculate all other observations 
	 * required. For example, in our common example, if we tell it in that Jan 2013 | 2013 which was 30% is now 40% it will calculate 
	 * the change in other nodes to accommodate the decrease in the other nodes in the network.
	 * For other types: targetPath and referencePath are unused (null).
	 * @return the assets per state changed, if the user has sufficient assets 
	 * (as the values returned by {@link #getAssetsIfStates(int, long, long, int, List, List, Properties)}).
	 * If user doesn't have sufficient assets, it will return null.
	 * If the result of the trade cannot be previewed now (e.g. it is adding a trade to a question which is still going to be created in the same transaction), 
	 * it will return an empty list.
	 * @throws IllegalArgumentException when any argument was invalid (e.g. ids were invalid).
	 */
	public List<Float> addTrade(Long transactionKey, Date occurredWhen, long questionId, List<Integer> targetPath, List<Integer> referencePath, List<Float> newValues, List<Long> assumptionIds, List<Integer> assumedStates) throws IllegalArgumentException;
	
	/**
	 * This method makes a trade on whole joint probabilities of a set of variables.
	 * This is useful if you want to change the entire clique, if implementations are using Junction tree approaches.
	 * Implementations may impose additional restrictions on the list of variables (e.g. implementations based on junction tree may
	 * impose that the list of variables must be in same clique).
	 * Note: this is not compatible with {@link #addTrade(Long, Date, String, long, long, List, List, List, List, boolean)} family,
	 * because the interface does not expect assets to be handled.
	 * @param transactionKey : key returned by {@link #startNetworkActions()}
	 * @param occurredWhen : implementations of this interface may use this timestamp to store a history of modifications.
	 * @param targetVariables : list of variables (questions) to trade on joint probability. The order matters, because indexes are related
	 * to indexes of newValues. Questions need to exist in the market. States of first variables switches more often.
	 * @param newValues : joint probability distribution. States of first variables in targetVariables switches more often.
	 * For example, if targetVariables = [1,2] with 2 states each (s0 or s1), then 
	 * newValues = [(1=s0,2=s0); (1=s1,2=s0); (1=s0,2=s1); (1=s1,2=s1)].
	 * @return true in case the trade was successful. false otherwise.
	 * @throws IllegalArgumentException : if the arguments were found to be inconsistent.
	 * @throws RuntimeException : if any other inconsistent state of ME is detected.
	 */
	public boolean addJointTrade(Long transactionKey, Date occurredWhen, List<Long> targetVariables, List<Float> newValues);
	
	/**
	 * This function will settle a specific question.
	 * Implementations of this method shall be synchronized.
	 * @param transactionKey : key returned by {@link #startNetworkActions()}
	 * @param occurredWhen : implementations of this interface may use this timestamp to store a history of modifications.
	 * @param questionId : the id of the question to be settled.
	 * @param settledState : index of the state of the question (with ID questionID) to be settled.
	 * @return true if successful.
	 * @throws IllegalArgumentException when any argument was invalid (e.g. ids were invalid).
	 * @see #resolveQuestion(Long, Date, long, List)
	 */
	public boolean resolveQuestion(Long transactionKey, Date occurredWhen, long questionId, int settledState) throws IllegalArgumentException;
	
	/**
	 * This method is similar to {@link #resolveQuestion(Long, Date, long, int)}, but
	 * This function will settle a specific question (and its choices - states) to the probability specified in settlement, 
	 * which specifies the value (probability) to settle. 
	 * The indexes of the settlement list represents the choices (states) of the question. 
	 * Therefore, the 1st value in settlement  represents the probability of the 1st choice (state), and so on.
	 * Values in settlement outside the  [0.0 ; 1.0] interval (i.e. values like NaN, -1, null, Infinite, or 99, 
	 * which are not valid probabilities) will be considered as “unspecified” and shall be updated accordingly to the underlying algorithm. 
	 * A settlement using using only “unspecified” values is an invalid input.
	 * If transactionKey is null, then the action will be committed immediately.
	 * Settled questions will be virtually treated as nonexistent questions in posterior calls. 
	 * One exception for the above rule shall happen when settlement contains only 0.0 and values outside the [0.0 ; 1.0] interval 
	 * (i.e. “unspecified” choices). In such case, this function will settle the states marked with 0.0 to 0% and update the probabilities 
	 * of the other states (marked with invalid probabilities) accordingly to the underlying algorithm. 
	 * This is semantically equivalent to settling specific choices (states) of a question as “impossible to happen” and 
	 * disabling posterior changes in that choice.
	 * In the current specification, “unspecified” values cannot be used when settling the probability of some state to a value other than 
	 * 0% or 100%. Therefore, the usage of “unspecified” values will be typically for settling states to 0% and automatically 
	 * updating other states.
	 * Other functionalities, such as addTrade shall be used in order to set the probabilities to values other than 0% or 100% 
	 * and still guarantee that the questions will exist in posterior calls.
	 * If settlement is normalized (i.e sums up to 1) and contains only 1 occurrence of 1.0, then it is equivalent to 
	 * {@link #resolveQuestion(Long, Date, long, int)} whose the last argument is the index of the state to settle to 1.0.
	 * 
	 * @param transactionKey : key returned by {@link #startNetworkActions()}
	 * @param occurredWhen : implementations of this interface may use this timestamp to store a history of modifications.
	 * @param questionId : the id of the question to be settled.
	 * @param settlement
	 * @return true if successful.
	 * @throws IllegalArgumentException when any argument was invalid (e.g. ids were invalid).
	 * @see #resolveValueTreeQuestion(Long, Date, long, List, List, List)
	 */
	public boolean resolveQuestion(Long transactionKey, Date occurredWhen, long questionId, List<Float> settlement ) throws IllegalArgumentException;
	
	/**
	 * This is virtually equivalent to calling {@link #addTrade(Long, Date, long, List, List, List, List, List)}
	 * and then deleting (absorbing) the node.
	 * @param transactionKey : if null, then this action will be committed immediately.
	 * @param occurredWhen : when this action happened. It is used for the purpose of re-ordering actions if multiple actions are inserted in same transaction.
	 * @param questionID : the id of the question to resolve. Must be a root of a value tree
	 * @param targetPaths : path of value tree node to be settled. If content of settlements doesn't specify a single value, then children of this node will be affected,
	 * instead of the node identified by this path
	 * @param referencePaths : these nodes will be the anchor, so probabilities of these nodes won't be affected.
	 * @param settlements : each element in this list will specify the probability of target. If each element in this list is not a single float value,
	 * then children of target will have probabilities changed.
	 * @return : true if successful.
	 * @throws IllegalArgumentException : if any of the arguments were invalid. For example, paths are inconsistent.
	 * @see {@link #resolveQuestion(Long, Date, long, List)}
	 * @see #resolveValueTreeQuestion(Long, Date, long, List, List, List, boolean)
	 * @see #addTrade(Long, Date, long, List, List, List, List, List)
	 */
	public boolean resolveValueTreeQuestion(Long transactionKey, Date occurredWhen, long questionID, List<List<Integer>> targetPaths, List<List<Integer>> referencePaths,List<List<Float>> settlements) throws IllegalArgumentException;
	
	/**
	 * This does the same of {@link #resolveValueTreeQuestion(Long, Date, long, List, List, List)},
	 * but we can force whether we shall or shall not 
	 * @param isToForceQuestionRemoval : if true, node will be deleted for sure. If false, node will be deleted only if there is a node with 100% probability
	 * (i.e. if the question cannot be edited anymore).
	 * @see #resolveValueTreeQuestion(Long, Date, long, List, List, List)
	 */
	public boolean resolveValueTreeQuestion(Long transactionKey, Date occurredWhen, long questionID, List<List<Integer>> targetPaths, List<List<Integer>> referencePaths,List<List<Float>> settlements, boolean isToForceQuestionRemoval) throws IllegalArgumentException;
	
	/**
	 * This function will attempt to undo all trades >= the startingTradeId against this question. 
	 * The exact definition of undo will vary. 
	 * It may be either to creating matching trades that invert the previous ones or just to return all assets expended to all users.
	 * Implementations of this method shall be synchronized.
	 * <br/>
	 * CAUTION: this method may require reboot of all data structures (i.e. bayesian network and ), so this 
	 * method is expected to be VERY slow. Users of this interface shall try to minimize calls to this method.
	 * @param transactionKey : key returned by {@link #startNetworkActions()}
	 * @param occurredWhen : implementations of this interface may use this timestamp to store a history of modifications.
	 * @param tradesStartingWhen : all trades with date greater or equal to this id will be reverted.
	 * @param questionId : the id of the question to be reverted. If null, the trades to be reverted will not be filtered by question.
	 * @return true if successful.
	 * @throws IllegalArgumentException when any argument was invalid (e.g. ids were invalid).
	 */
	public boolean revertTrade(Long transactionKey, Date occurredWhen,  Date tradesStartingWhen, Long questionId) throws IllegalArgumentException;
	
	/**
	 * Returns probability across a list of states for a question given assumptions.
	 * This method is NOT expected to block other threads from accessing the probability values (except for 
	 * methods changing the values of such probabilities), because
	 * this will supposedly be one of the bottlenecks (in a sense that it is likely to be
	 * the most called method) when integrated with DAGGRE main system.
	 * @param questionId : id of the question to obtain probability.
	 * @param assumptionIds: (optional) list (ordered collection) of question IDs assumed when obtaining the estimated assets. If specified,
	 * the questions (i.e. random variables) with these IDs will be assumed to be in the states specified in the argument "assumedStates".
	 * @param assumedStates : (mandatory if assumptionIDs is specified - must have the same size of assumptionIDs) indexes
	 * of states (i.e. choices - if boolean, then it is either 0 or 1) of assumptionIDs to be assumed.
	 * If negative, then "not" Math.abs(state + 1) will be considered as the state (i.e. the state Math.abs(state + 1) will be
	 * considered as 0%).
	 * If a resolved assumption is passed and the state is not equal to the settled state, then this method will return a list
	 * filled with {@link Float#NaN}.
	 * @return the probability of a question (i.e. random variable) given assumptions.
	 * The order is important for identifying the states (i.e. 1st value is for the 1st state, and so on).
	 * @throws IllegalArgumentException when any argument was invalid (e.g. ids were invalid).
	 * @throws IllegalStateException : if the shared Bayesian network was not created/initialized yet.
	 * @see #getProbList(long, List, List)
	 */
	public List<Float> getProbList(long questionId, List<Long>assumptionIds, List<Integer> assumedStates) throws IllegalArgumentException, IllegalStateException;

	/**
	 * This is equivalent to {@link #getProbList(long, List, List)}
	 * but for nodes containing value trees.
	 * If the question is a ValueTree node then targetPath specifies the specific node we are referring to and referencePath is either null 
	 * (in which case it is assumed to be same as targetPath) or the specific ancestor of the target we are referring to.
	 * @param questionId
	 * @param targetPath
	 * @param referencePath
	 * @param assumptionIds
	 * @param assumedStates
	 * @return
	 * @throws IllegalArgumentException
	 */
	public List<Float> getProbList(long questionId, List<Integer> targetPath,  List<Integer> referencePath, List<Long>assumptionIds, List<Integer> assumedStates) throws IllegalArgumentException;
	
	/**
	 * Returns probability across a list of states for all questions given an assumption. 
	 * This is equivalent to calling getProbList for all possible questionId, but computation is supposed to be faster than calling 
	 * getProbList multiple times.
	 * @param questionIds : only marginals of nodes in this list will be returned.
	 * If null, marginals of all nodes will be returned.
	 * @param assumptionIds: (optional) list (ordered collection) of question IDs assumed when obtaining the estimated assets. If specified,
	 * the questions (i.e. random variables) with these IDs will be assumed to be in the states specified in the argument "assumedStates".
	 * @param assumedStates : (mandatory if assumptionIDs is specified - must have the same size of assumptionIDs) indexes
	 * of states (i.e. choices - if boolean, then it is either 0 or 1) of assumptionIDs to be assumed.
	 * If negative, then "not" Math.abs(state + 1) will be considered as the state (i.e. the state Math.abs(state + 1) will be
	 * considered as 0%).
	 * If a resolved assumption is passed and the state is not equal to the settled state, then this method will return a list
	 * filled with {@link Float#NaN}.
	 * @return a mapping from question ID to the probabilities of that question.
	 * The order is important for identifying the states (i.e. 1st value is for the 1st state, and so on).
	 * @throws IllegalArgumentException when any argument was invalid (e.g. ids were invalid).
	 * @throws IllegalStateException : if the shared Bayesian network was not created/initialized yet.
	 */
	public Map<Long,List<Float>> getProbLists(List<Long> questionIds, List<Long>assumptionIds, List<Integer> assumedStates) throws IllegalArgumentException;
	
	/**
	 * This will calculate the joint probability of the provided nodes.
	 * @param questionIds : the nodes to calculate joint probability.
	 * @param states : states to be considered when calculating the joint probability.
	 * The ordering is important, because the order is what identifies which state is related to which question in "questionIds".
	 * If negative, then "not" Math.abs(state + 1) will be considered as the state (i.e. the state Math.abs(state + 1) will be
	 * considered as 0%).
	 * @return the joint probability value.
	 * @throws IllegalArgumentException : if any invalid assumption or state is provided.
	 * @see #getJointProbability(List, List, List, List)
	 */
	public float getJointProbability(List<Long>questionIds, List<Integer> states) throws IllegalArgumentException;
	
	/**
	 * This is the equivalent of {@link #getJointProbability(List, List)}, but including nodes containing value trees.
	 *  If a ValueTree is used for a given question, then the relevant element in targetPaths and referencePaths will contain a 
	 *  list referencing that object, otherwise it will contain null. If no questions are ValueTrees then the targetPaths and 
	 *  referencePaths themselves may be null.
	 * @param questionIds
	 * @param questionStates
	 * @param targetPaths
	 * @param referencePaths
	 * @return
	 * @throws IllegalArgumentException
	 */
	public float getJointProbability(List<Long>questionIds, List<Integer> questionStates, List<List<Integer>> targetPaths, List<List<Integer>> referencePaths) throws IllegalArgumentException;
	
	/**
	 * Obtains the ids of the questions that are potential assumptions of a given question. 
	 * This method is necessary when the algorithm implemented by a class implementing this interface requires some restrictions 
	 * on the assumptions (e.g. the assumptions should be in a same clique).
	 * @param questionId  : the id of the random variable (question) to be analyzed.
	 * @param assumptiveQuestions : because cliques overlap, implementations based on cliques (e.g. potential assumptions are limited
	 * to the cliques containing a question with "questionID" as its ID) can initially have nodes in several cliques
	 * as assumptions, but after one assumption is selected, only cliques containing questionID and the new assumptions simultaneously
	 * can be selected after that. This parameter indicates what assumptions were selected so far.
	 * @return list of any possible assumptions that can be made on the question, with the content of assumptionIds inclusively.
	 * If questionId is not compatible with assumptionIds
	 * (if there is no possible assumptions satisfying questionId and all assumptionIds simultaneously), 
	 * an empty list will be returned.
	 * @throws IllegalArgumentException when any argument was invalid (e.g. ids were invalid).
	 * @throws IllegalStateException : if the shared Bayesian network was not created/initialized yet.
	 */
	public List<Long> getPossibleQuestionAssumptions(long questionId, List<Long>assumptionIds) throws IllegalArgumentException, IllegalStateException;
	
	/**
	 * This method is conceptually similar to calling {@link #getPossibleQuestionAssumptions(long, List)}
	 * for all possible questionIds and then removing redundancies.
	 * @return a list of any possible assumptions groups (likely cliques in some implementations). 
	 * This method is there because of implementation constraints and unfortunately does violate encapsulation.
	 */
	public List<List<Long>> getQuestionAssumptionGroups();
	
	/**
	 * This method implements the feature for obtaining the assets position of the user given conditions
	 * (i.e. extract the values in the asset tables).
	 * @param userId : the ID of the current user. 
	 * @param questionId : the id of the question to be edited (i.e. the random variable "T"  in the example)
	 * @param assumptionIds : a list (ordered collection) of question IDs which are the assumptions for T (i.e. random variable "A" in the example). The ordeer
	 * is important, because it will indicate which states of assumedStates are associated with which questions in assumptionIDs.
	 * @param assumedStates : a list (ordered collection) representing the states of assumptionIDs assumed.
	 * <br/>
	 * By providing a list with some null content (e.g. [1,0,null]) or a list with a size smaller than assumptionIds, this method will return a conditional asset table instead
	 * of the assets for each state of a question.
	 * <br/>
	 * For example, suppose questionId points to question X (with states x0 and x1), assumptionIds points to questions [Y,Z]
	 * (with states [y0,y1] and [z0,z1] respectively),  and assumedStates points to states [y0 , null], then the returned list will be:
	 * <br/>
	 * [Asset(X=x0|Y=y0,Z=z0) ; Asset(X=x1|Y=y0,Z=z0); Asset(X=x0|Y=y0,Z=z1); Asset(X=x1|Y=y0,Z=z1)]
	 * @return the change in user assets if a given states occurs if the specified assumptions are met. 
	 * The indexes are relative to the indexes of the states.
	 * In the case of a binary question this will return a [if_true, if_false] value, if multiple choice will return a [if_0, if_1, if_2...] value list
	 * For example, assuming that the question identified by questionID is a boolean question (and also assuming
	 * that state 0 indicates false and state 1 indicates true); then, index 0 contains the assets of 
	 * the question while it is in state "false" (given assumptions), and index 1 contains the assets of the
	 * question while it is in state "true".
	 * <br/>
	 * If assumedStates is filled with null values, then this method will return conditional asset table.
	 * <br/><br/>
	 * E.g. [Asset(X=x0|Y=y0,Z=z0) ; Asset(X=x1|Y=y0,Z=z0); Asset(X=x0|Y=y0,Z=z1); Asset(X=x1|Y=y0,Z=z1)]
	 * <br/><br/>
	 * If a resolved assumption is passed and the state is not equal to the settled state, then this method will return a list
	 * filled with {@link Float#NaN}.
	 * @throws IllegalArgumentException when any argument was invalid (e.g. inexistent question or state, or invalid assumptions).
	 * @throws IllegalStateException : if the shared Bayesian network was not created/initialized yet.
	 */
	public List<Float> getAssetsIfStates(long userId, long questionId, List<Long> assumptionIds, List<Integer> assumedStates) throws IllegalArgumentException, IllegalStateException;
	
	/**
	 * Returns the upper and lower bounds for a specific trade given the assumptions. This can be used to constrain a UI action.
	 * This method implements the feature for calculating the limits (upper and lower bounds) of an edit
	 * so that the minimum asset does not go below 0.
	 * An example of an algorithm for implementing this feature is described in
	 * <a href="https://docs.google.com/document/d/1p1TY-paqEmJNshQYThr6H3SyR2-e6xmoXrI9HleqiPM/edit">https://docs.google.com/document/d/1p1TY-paqEmJNshQYThr6H3SyR2-e6xmoXrI9HleqiPM/edit</a>
	 * as follows:
	 * <br/><br/>
	 * 
	 * First we need to calculate the minimum q-values under two cases {T=t, A=a}, and {T~=t, A=a} respectively, denoted as mt, and mt, by min-q-propagation. Then use Equation (1) to compute the edit limits.
	 * <br/><br/>
	 * 
	 * 1. Case 1 - given {T=t, A=a}: <br/>
	 * Absorbing the given condition by extracting elements from q-tables accordingly for states involving only the given conditions {T=t, A=a}. And save them into the q-tables. 
	 * Run min-q-propagation on updated q-tables to find min-q joint states.
	 * 
	 * Calculate the global min-q value on the min-q state using the formula:
	 * <br/><br/>
	 * q(x) = PRODUCT(qc(Xc)) / PRODUCT(qs(Xs));	
	 * <br/><br/>
	 * 
	 * where qc, and qsare elements in q-tables from cliques, and separators respectively; and Xc, and Xs, are corresponding states in cliques, and separators respectively; and q(X)is the global q-value associated to state X.
	 * <br/><br/>
	 * 
	 * Then, q(x) is saved as min_q1 (mt).
	 *  <br/><br/>
	 *  
	 *  2. Case 2 - given {T~=t, A=a}:
	 *  <br/>
	 *  Absorbing the given condition by extracting elements from q-tables accordingly for states involving only the given conditions {A=a}. And save them into the q-tables.
	 *  Replacing a big M (maximum of all elements in the table + 1) for the elements in q-tables involving the state we like to exclude {T=t}, so we ensure such a state will not be chosen in the process of min-q-propagation.
	 *  Run min-q-propagation on updated q-tables to find min-q joint states.
	 *  <br/>
	 *  Calculate the global min-q value on the min-q state using the formula  q(x) = PRODUCT(qc(Xc)) / PRODUCT(qs(Xs)). Saved as min_q2 (mt).
	 *  <br/><br/>
	 *  3. Calculate the edit limits by Equation:
	 *  <br/><br/>
	 *   [ p(T=t|A=a)/mt ; 1- (p(T=t|A=a)/m~t) ]; where mt, and m~t, are min-assets given {T=t, A=a}, and given {T ~= t, A=a}, respectively. 
	 * 
	 * @param userID : the ID of the current user. 
	 * @param questionId : the id of the question to be edited (i.e. the random variable "T"  in the example)
	 * @param questionState  : the index (or ID) of the value of the question identified by the questionID (i.e. the state "t" of random variable "T").
	 * Note that if a question is a boolean random variable (i.e. only 2 possible states), then the limits for T=true will be complementary to the limits for T=false (i.e. 
	 * LIMIT(T=false) = 1-LIMIT(T=true)) and LIMIT(T=true) = 1-LIMIT(T=false)).
	 * @param assumptionIDs : a list (ordered collection) of question IDs which are the assumptions for T (i.e. random variable "A" in the example). The ordeer
	 * is important, because it will indicate which states of assumedStates are associated with which questions in assumptionIDs.
	 * @param assumedStates : a list (ordered collection) representing the states of assumptionIDs assumed.
	 * @return a list (ordered collection) of size 2 representing respectively the lower and upper bounds for the allowed edit (allowed probability) of
	 * a state (referenced by argument "questionState") of a question (referenced by argument "questionID").
	 * @throws IllegalArgumentException when any argument was invalid (e.g. inexistent question or state, or invalid assumptions).
	 * @throws IllegalStateException : if the shared Bayesian network was not created/initialized yet.
	 */
	public List<Float> getEditLimits(long userId, long questionId, int questionState, List<Long>assumptionIds, List<Integer> assumedStates) throws IllegalArgumentException, IllegalStateException;
	
	/**
	 * @param userID : the ID of the owner of the asset table.
	 * @param assumptionIDs : the IDs of the questions to be assumed in this edit.
	 * @param assumedStates : a list (ordered collection) representing the states of assumptionIDs assumed.
	 * @return available cash (i.e. minimum assets) given a set of assumptions.
	 * @throws IllegalArgumentException when any argument was invalid (e.g. inexistent question or state, or invalid assumptions).
	 * @throws IllegalStateException : if the shared Bayesian network was not created/initialized yet.
	 */
	public float getCash(long userId, List<Long>assumptionIds, List<Integer> assumedStates) throws IllegalArgumentException, IllegalStateException;
	
	/**
	 * Obtains the expected assets (probability * asset).
	 * @param userId : the ID of the user (owner of the assets).
	 * @param questionId :  the ID of the question to be considered. If set to null or to a resolved question, this method shall be equivalent to {@link #scoreUserEv(long, List, List)}.
	 * @param assumptionIds : (optional) list (ordered collection) of question IDs assumed when obtaining the estimated assets. If specified,
	 * the questions (i.e. random variables) with these IDs will be assumed to be in the states specified in the argument "assumedStates".
	 * @param assumedStates : (mandatory if assumptionIDs is specified - must have the same size of assumptionIDs) indexes
	 * of states (i.e. choices - if boolean, then it is either 0 or 1) of assumptionIDs to be assumed.
	 * If it does not have the same size of assumptionIDs, MIN(assumptionIDs.size(), assumedStates.size()) shall be considered.
	 * If a resolved assumption is passed and the state is not equal to the settled state, then this method will return a list
	 * filled with {@link Float#NaN}.
	 * @return current expected value portion of user score given a set of assumptions. 
	 * @throws IllegalArgumentException when any argument was invalid (e.g. inexistent question or state, or invalid assumptions).
	 * @throws IllegalStateException : if the shared Bayesian network was not created/initialized yet.
	 * @deprecated use {@link #scoreUserEv(long, List, List)} or {@link #scoreUserQuestionEvStates(long, long, List, List)} instead
	 */
	public float scoreUserQuestionEv(long userId, Long questionId, List<Long>assumptionIds, List<Integer> assumedStates) throws IllegalArgumentException;
	
	/**
	 * @param userId : the ID of the user (owner of the assets).
	 * @param questionId :  the ID of the question to be considered. If this question was resolved, then the state which is not the settled state
	 * will have {@link Float#NaN} in its value.
	 * @param assumptionIds : (optional) list (ordered collection) of question IDs assumed when obtaining the estimated assets. If specified,
	 * the questions (i.e. random variables) with these IDs will be assumed to be in the states specified in the argument "assumedStates".
	 * @param assumedStates : (mandatory if assumptionIDs is specified - must have the same size of assumptionIDs) indexes
	 * of states (i.e. choices - if boolean, then it is either 0 or 1) of assumptionIDs to be assumed.
	 * If it does not have the same size of assumptionIDs, MIN(assumptionIDs.size(), assumedStates.size()) shall be considered.
	 * If a resolved assumption is passed and the state is not equal to the settled state, then this method will return a list
	 * filled with {@link Float#NaN}.
	 * @return a list of score expectations for each possible choice that could result. This is p(state)*user_assets(state).
	 * @throws IllegalArgumentException when any argument was invalid (e.g. inexistent question or state, or invalid assumptions).
	 * @throws IllegalStateException : if the shared Bayesian network was not created/initialized yet.
	 */
	public List<Float> scoreUserQuestionEvStates(long userId, long questionId, List<Long>assumptionIds, List<Integer> assumedStates) throws IllegalArgumentException;
	
	/**
	 * @param userId : the ID of the user (owner of the assets).
	 * @param questionId :  the ID of the question to be considered. 
	 * @param assumptionIds : (optional) list (ordered collection) of question IDs assumed when obtaining the estimated assets. If specified,
	 * the questions (i.e. random variables) with these IDs will be assumed to be in the states specified in the argument "assumedStates".
	 * @param assumedStates : (mandatory if assumptionIDs is specified - must have the same size of assumptionIDs) indexes
	 * of states (i.e. choices - if boolean, then it is either 0 or 1) of assumptionIDs to be assumed.
	 * If it does not have the same size of assumptionIDs, MIN(assumptionIDs.size(), assumedStates.size()) shall be considered.
	 * @param isToComputeLocally : if true, the method will not calculate global/joint expected assets.
	 * Implementations based on cliques will attempt to calculate the expected scores given states within the cliques.
	 * @return a list of score expectations for each possible choice that could result. This is p(state)*user_assets(state).
	 * @throws IllegalArgumentException when any argument was invalid (e.g. inexistent question or state, or invalid assumptions).
	 * @throws IllegalStateException : if the shared Bayesian network was not created/initialized yet.
	 * @deprecated use {@link #scoreUserQuestionEvStates(long, long, List, List)} instead
	 */
	public List<Float> scoreUserQuestionEvStates(long userId, long questionId, List<Long>assumptionIds, List<Integer> assumedStates, boolean isToComputeLocally) throws IllegalArgumentException;
	
	/**
	 * Method for calculating the global "conditional" expected score.
	 * @param userId : the ID of the user (owner of the assets).
	 * @param assumptionIds : (optional) list (ordered collection) of question IDs assumed when obtaining the estimated assets. If specified,
	 * the questions (i.e. random variables) with these IDs will be assumed to be in the states specified in the argument "assumedStates"
	 * (i.e. the probabilities will be re-calculated assuming these values).
	 * @param assumedStates : (mandatory if assumptionIDs is specified - must have the same size of assumptionIDs) indexes
	 * of states (i.e. choices - if boolean, then it is either 0 or 1) of assumptionIDs to be assumed.
	 * If it does not have the same size of assumptionIDs, MIN(assumptionIDs.size(), assumedStates.size()) shall be considered.
	 * If negative, then "not" Math.abs(state + 1) will be considered as the state (i.e. the state Math.abs(state + 1) will be
	 * considered as 0%).
	 * If a resolved assumption is passed and the state is not equal to the settled state, then this method will return a list
	 * filled with {@link Float#NaN}.
	 * @return TOTAL current expected value portion of across all questions given a set of assumptions.
	 * @throws IllegalArgumentException
	 * @see {@link #scoreUserQuestionEv(long, Long, List, List)}
	 * @throws IllegalStateException : if the shared Bayesian network was not created/initialized yet.
	 */
	public float scoreUserEv(long userId, List<Long>assumptionIds, List<Integer> assumedStates) throws IllegalArgumentException;
	
	
	/**
	 * This function will return the affect (assets per state) of this trade similar to the addTrade function,
	 * but is a proposed what-if (non-binding) calculation only that does not impact the network.
	 * If assets are not used by this engine, then this method may simply return changes in assets.
	 * @param userID : the ID of the user (i.e. owner of the assets).
	 * @param questionID : the id of the question to be edited (i.e. the random variable "T"  in the example)
	 * @param newValues : this is a list (ordered collection) representing the probability values after the edit. 
	 * For example, suppose T is the target question (i.e. a random variable) with states t1 and t2, and A1 and A2 are assumptions with states (a11, a12), and (a21 , a22) respectively.
	 * Then, the list must be filled as follows:<br/>
	 * index 0 - P(T=t1 | A1=a11, A2=a21)<br/>
	 * index 1 - P(T=t2 | A1=a11, A2=a21)<br/>
	 * index 2 - P(T=t1 | A1=a12, A2=a21)<br/>
	 * index 3 - P(T=t2 | A1=a12, A2=a21)<br/>
	 * index 4 - P(T=t1 | A1=a11, A2=a22)<br/>
	 * index 5 - P(T=t2 | A1=a11, A2=a22)<br/>
	 * index 6 - P(T=t1 | A1=a12, A2=a22)<br/>
	 * index 7 - P(T=t2 | A1=a12, A2=a22)<br/>
	 * <br/>
	 * If the states of the conditions are specified in assumedStates, then this list will only specify the conditional
	 * probabilities of each states of questionID.
	 * E.g. Again, suppose T is the target question with states t1 and t2, and A1 and A2 are assumptions with states (a11, a12), and (a21 , a22) respectively.]
	 * Also suppose that assumedStates = (1,0). Then, the content of newValues must be: <br/>
	 * index 0 - P(T=t1 | A1=a12, A2=a21)<br/>
	 * index 1 - P(T=t2 | A1=a12, A2=a21)<br/>
	 * @param assumptionIDs : list (ordered collection) representing the IDs of the questions to be assumed in this edit. The order is important,
	 * because the ordering in this list will be used in order to identify the correct indexes in "newValues".
	 * @param assumedStates : this shall be null if newValues contains full data (all cells of the conditional probability distribution).
	 * If not null, this list indicates which states the nodes in assumptionIds are.
	 * @return the assets per state changed, if the user has sufficient assets 
	 * (as the values returned by {@link #getAssetsIfStates(int, long, long, int, List, List, Properties)}).
	 * @throws IllegalArgumentException when any argument was invalid (e.g. ids were invalid).
	 * @throws IllegalStateException : if the shared Bayesian network was not created/initialized yet.
	 * @deprecated use {@link #previewTrade(TradeSpecification)} instead.
	 */
	public List<Float> previewTrade(long userId, long questionId, List<Float> newValues, List<Long> assumptionIds, List<Integer> assumedStates) throws IllegalArgumentException;
	
	/**
	 * This function will return the affect (assets per state) of this trade similar to the addTrade function,
	 * but is a proposed what-if (non-binding) calculation only that does not impact the network.
	 * If assets are not used by this engine, then this method may simply return changes in assets.
	 * @param tradeSpecification : instance of {@link TradeSpecification} which specifies the user ID, question ID, 
	 * the probability values, the IDs of assumed questions and the assumed states.
	 * @return the assets per state changed, if the user has sufficient assets 
	 * (as the values returned by {@link #getAssetsIfStates(int, long, long, int, List, List, Properties)}).
	 * @throws IllegalArgumentException when any argument was invalid (e.g. ids were invalid).
	 * @throws IllegalStateException : if the shared Bayesian network was not created/initialized yet.
	 */
	public List<Float> previewTrade(TradeSpecification tradeSpecification) throws IllegalArgumentException;
	
	/**
	 * This method will determine the states of a balancing trade which would minimize impact once the question is resolved
	 * Ideally this balancing trade is one where all assetsifStates states where equal so settling the question would have no effect. 
	 * <br/><br/>
	 * CAUTION: in a multi-thread environment, use {@link #doBalanceTrade(long, Date, String, long, long, List, List)} if you want to commit a trade
	 * which will balance the user's assets given assumptions, instead of using this method to calculate the balancing
	 * trade and then run {@link #addTrade(long, Date, String, long, long, List, List, List, boolean)}.
	 * @param userID: the ID of the user (i.e. owner of the assets).
	 * @param questionId : the id of the question to be balanced.
	 * @param assumptionIds : list (ordered collection) representing the IDs of the questions to be assumed in this edit. The order is important,
	 * because the ordering in this list will be used in order to identify the correct indexes in assumedStates.
	 * @param assumedStates : indicates the states of the nodes in assumptionIDs.
	 * If it does not have the same size of assumptionIDs,�@MIN(assumptionIDs.size(), assumedStates.size()) shall be considered. 
	 * @return value to be inserted as newValue in {@link #addTrade(long, Date, long, long, long, List, List, List, List, Boolean)} to balance the trade.
	 * For example, suppose T is the target question (i.e. a random variable) with states t1 and t2, and A1 and A2 are assumptions with states (a11, a12), and (a21 , a22) respectively.
	 * Also suppose that assumedStates is empty or null (i.e. the returned list will represent all cells in a conditional probability distribution).
	 * Then, the list is be filled as follows:<br/>
	 * index 0 - P(T=t1 | A1=a11, A2=a21)<br/>
	 * index 1 - P(T=t2 | A1=a11, A2=a21)<br/>
	 * index 2 - P(T=t1 | A1=a12, A2=a21)<br/>
	 * index 3 - P(T=t2 | A1=a12, A2=a21)<br/>
	 * index 4 - P(T=t1 | A1=a11, A2=a22)<br/>
	 * index 5 - P(T=t2 | A1=a11, A2=a22)<br/>
	 * index 6 - P(T=t1 | A1=a12, A2=a22)<br/>
	 * index 7 - P(T=t2 | A1=a12, A2=a22)<br/>
	 * <br/><br/>
	 * If assumedStates is set to {0, 0} (i.e. assumed A1 = a11 and A2 = a21), then:<br/>
	 * index 0 - P(T=t1 | A1=a11, A2=a21)<br/>
	 * index 1 - P(T=t2 | A1=a11, A2=a21)<br/>
	 * <br/><br/>
	 * If assumedStates is set to {1, null} (i.e. assumed A1 = a12 and no state assumed for A2), then:<br/>
	 * index 0 - P(T=t1 | A1=a12, A2=a21)<br/>
	 * index 1 - P(T=t2 | A1=a12, A2=a21)<br/>
	 * index 2 - P(T=t1 | A1=a12, A2=a22)<br/>
	 * index 3 - P(T=t2 | A1=a12, A2=a22)<br/>
	 * @throws IllegalArgumentException when any argument was invalid (e.g. ids were invalid).
	 * @throws IllegalStateException : if the shared Bayesian network was not created/initialized yet.
	 * @see #doBalanceTrade(long, Date, String, long, long, List, List)
	 * @deprecated use {@link #doBalanceTrade(Long, Date, String, long, long, List, List)} instead.
	 */
	@Deprecated
	public List<Float> previewBalancingTrade(long userId, long questionId, List<Long> assumptionIds, List<Integer> assumedStates) throws IllegalArgumentException;
	

	/**
	 * This method will determine the states of balancing trades which would minimize impact once the question is resolved.
	 * This is different from {@link #previewBalancingTrade(long, long, List, List)} in a sense that this method
	 * can return a set of trades in order to exit from a question given incomplete assumptions.
	 * Ideally this balancing trade is a set of trades where all assetsifStates states where equal so settling the question would have no effect. 
	 * <br/><br/>
	 * CAUTION: in a multi-thread environment, use {@link #doBalanceTrade(long, Date, String, long, long, List, List)} if you want to commit a trade
	 * which will balance the user's assets given assumptions, instead of using this method to calculate the balancing
	 * trade and then run {@link #addTrade(long, Date, String, long, long, List, List, List, boolean)}.
	 * @param userID: the ID of the user (i.e. owner of the assets).
	 * @param questionId : the id of the question to be balanced.
	 * @param assumptionIds : list (ordered collection) representing the IDs of the questions to be assumed in this edit. The order is important,
	 * because the ordering in this list will be used in order to identify the correct indexes in assumedStates.
	 * @param assumedStates : indicates the states of the nodes in assumptionIDs.
	 * If it does not have the same size of assumptionIDs, {@link Math#min(assumptionIDs.size(), assumedStates.size())} shall be considered. 
	 * @return a list of {@link TradeSpecification} which represents the sequence of trades to be executed in order to
	 * exit from this question given assumptions.
	 * @throws IllegalArgumentException when any argument was invalid (e.g. ids were invalid).
	 * @throws IllegalStateException : if the shared Bayesian network was not created/initialized yet.
	 * @see #doBalanceTrade(long, Date, String, long, long, List, List)
	 * @deprecated use {@link #doBalanceTrade(Long, Date, String, long, long, List, List)} instead
	 */
	@Deprecated
	public List<TradeSpecification> previewBalancingTrades(long userId, long questionId, List<Long> originalAssumptionIds, 
			List<Integer> originalAssumedStates) throws IllegalArgumentException;
	
	/**
	 * This is similar to doing {@link #previewBalancingTrade(long, long, List, List)} and then
	 * {@link #addTrade(long, Date, String, long, long, List, List, List, boolean)}.
	 * However, this method is safer than calling {@link #previewBalancingTrade(long, long, List, List)}
	 * and then {@link #addTrade(long, Date, String, long, long, List, List, List, boolean)},
	 * because it is going to be executed in a same transaction (i.e. it will be executed
	 * during {@link #commitNetworkActions(long)}).
	 * 
	 * @param transactionKey : key returned by {@link #startNetworkActions()}
	 * @param occurredWhen : implementations of this interface may use this timestamp to store a history of modifications.
	 * @param tradeKey : revert and history functions can refer to specific trade actions easier, by referring to this key (identifier).
	 * @param userID: the ID of the user (i.e. owner of the assets).
	 * @param questionId : the id of the question to be balanced.
	 * @param assumptionIds : list (ordered collection) representing the IDs of the questions to be assumed in this edit. The order is important,
	 * because the ordering in this list will be used in order to identify the correct indexes in assumedStates.
	 * @param assumedStates : indicates the states of the nodes in assumptionIDs.
	 * If it does not have the same size of assumptionIDs,�@MIN(assumptionIDs.size(), assumedStates.size()) shall be considered. 
	 * @throws IllegalArgumentException when any argument was invalid (e.g. ids were invalid).
	 */
	public boolean doBalanceTrade(Long transactionKey, Date occurredWhen, String tradeKey, long userId, long questionId, List<Long> assumptionIds, List<Integer> assumedStates) throws IllegalArgumentException;
	
	/**
	 * This function will return an ordered list of history objects that describes the changes in probabilities by time.
	 * In phase 1, this method may simply return the tradeId and history over time that directly impacted the question.
	 * @param questionId : filter for the history. Only history related to this question will be returned.
	 * If null, history not related to any question will be returned (e.g. those created by {@link #addCash(long, Date, long, float, String)},
	 * {@link #revertTrade(long, Date, Date, Long)} with no question specified, and any auxiliary changes).
	 * @param assumptionIds : filter for the history. Only histories related to questionID with these assumptions will be returned.
	 * @param assumedStates : filter for the history. Only histories related to assumptions with these states will be returned.
	 * If it does not have the same size of assumptionIDs,�@MIN(assumptionIDs.size(), assumedStates.size()) shall be considered.
	 * @return non-null list with the sequence of events.
	 * @throws IllegalArgumentException when any argument was invalid (e.g. states were invalid).
	 * @see {@link QuestionEvent}
	 */
	public List<QuestionEvent> getQuestionHistory (Long questionId, List<Long> assumptionIds, List<Integer> assumedStates) throws IllegalArgumentException;


	/**
	 * This method performs the same of {@link #getScoreSummary(long, Long, List, List)},
	 * but it returns a structured object instead of a list of generic Property objects.
	 * @param userId : ID of the user to be considered.
	 * @param questionId : (optional) ID of the main question to be used as filter. If null, all questions will be considered (recommended).
	 * @param assumptionIds : (optional) assumptions to be considered in obtaining the summary. 
	 * Use the same list passed to {@link #scoreUserEv(long, List, List)}.
	 * @param assumedStates : (optional) states of the assumptions. The order must be synchronized with assumptionIds.
	 * Use the same list passed to {@link #scoreUserEv(long, List, List)}.
	 * If negative, then "not" Math.abs(state + 1) will be considered as the state (i.e. the state Math.abs(state + 1) will be
	 * considered as 0%).
	 * @return object representing the summary (set of attributes to display TBD) that shows a summary view of how the current score of a user was determined. 
	 * @throws IllegalArgumentException when any argument was invalid (e.g. ids were invalid).
	 */
	public ScoreSummary getScoreSummaryObject(long userId, Long questionId, List<Long> assumptionIds, List<Integer> assumedStates) throws IllegalArgumentException;
	
	/**
	 * This method will return explanations of the 
	 * value returned by {@link #scoreUserEv(long, List, List)}.
	 * @param userId : ID of the user to be considered.
	 * @param questionId : (optional) ID of the main question to be used as filter. If null, all questions will be considered.
	 * @param assumptionIds : (optional) assumptions to be considered in obtaining the summary. 
	 * Use the same list passed to {@link #scoreUserEv(long, List, List)}.
	 * @param assumedStates : (optional) states of the assumptions. The order must be synchronized with assumptionIds.
	 * Use the same list passed to {@link #scoreUserEv(long, List, List)}.
	 * @return ordered list of score details (properties dictionary with parameters to display TBD) that shows a summary view of how the current score of a user was determined. 
	 * The first element in the list will contain the user cash in the property {@link #CASH_PROPERTY}, the user score in {@link #SCOREEV_PROPERTY},
	 * and the size (quantity) of score components contributing positively in property {@link #SCORE_COMPONENT_SIZE_PROPERTY} (let's say, the
	 * quentity is "N").
	 * The next N elements in the list will be the contributions to the score EV, and the rest are negative contributions to the score EV (they are 
	 * negative because they are intersections between the contributions, hence, adding all the contributions and then
	 * subtracting all the negative contributions will result in the score EV).
	 * These contributions are expressed in terms of a comma-separated list of questions (property {@link #QUESTIONS_PROPERTY})
	 * and respective states (property {@link #STATES_PROPERTY}) and the contributed value (property {@link #SCOREEV_PROPERTY}).  
	 * Both keys and values are String.
	 * <br/><br/>
	 * Example:
	 * <br/>
	 * 1st element in the list: <br/>
	 * {@value #CASH_PROPERTY} = "10"; {@value #SCOREEV_PROPERTY} = "10.5"; {@value #SCORE_COMPONENT_SIZE_PROPERTY} = "2".
	 * <br/><br/>
	 * 2nd element in the list: <br/>
	 * {@value #QUESTIONS_PROPERTY} = "1,2"; {@value #STATES_PROPERTY} = "10.5"; {@value #SCOREEV_PROPERTY} = 2.
	 * @throws IllegalArgumentException when any argument was invalid (e.g. ids were invalid).
	 * @deprecated use {@link #getScoreSummaryObject(long, Long, List, List)} instead
	 */
	@Deprecated
	public List<Properties> getScoreSummary(long userId, Long questionId, List<Long> assumptionIds, List<Integer> assumedStates) throws IllegalArgumentException;
	
	
	
	/**
	 * @param userId : ID of the user to be considered.
	 * @param assumptionIds : assumptions to be considered in obtaining the summary
	 * @param assumedStates : states of the assumptions. The order must be synchronized with assumptionIds.
	 * @param questionId : ID of the main question to be used as filter. If null, all questions will be considered.
	 * @return ordered list of score details (properties dictionary with parameters to display TBD) 
	 * that provides a detailed view of how the current score of a user was determined. 
	 * 
	 * @throws IllegalArgumentException when any argument was invalid (e.g. ids were invalid).
	 */
	public List<Properties> getScoreDetails(long userId, Long questionId, List<Long> assumptionIds, List<Integer> assumedStates) throws IllegalArgumentException;
	
	/**
	 * Exports the current Bayes net structure into a file, so that 
	 * it can be edited in some GUI and imported back.
	 * @param file : file to be written.
	 * @throws IOException : in case of Input/Output problem.
	 * @throws IllegalStateException : if this method was called when the Markov Engine was in an
	 * invalid state.
	 * @see #importNetwork(File)
	 * @deprecated use {@link #exportState()}
	 */
	@Deprecated
	public void exportNetwork(File file) throws IOException, IllegalStateException;
	
	/**
	 * This method is similar to {@link #exportNetwork(File)}, however
	 * the output is a string representation of the current (shared) probabilistic network's snapshot
	 * only, so it is not suitable to be used as a full dump of the current state of the engine, 
	 * but it is useful to backup the network status when only the probabilistic network
	 * is important (e.g. when assets and history are managed by external entities or by the callers).
	 * Because this is a snapshot, it may not contain all information needed
	 * in order to retrieve full information, like the history.
	 * @return a string representing the current
	 * @throws IllegalStateException : if this method was called when the Markov Engine was in an
	 * invalid state.
	 */
	public String exportState() throws IllegalStateException;
	
	/**
	 * Imports a network from a file.
	 * The actual definition of "import" may depend on the implementation.
	 * @param file : file to be read
	 * @throws IOException : in case of Input/Output problem.
	 * @throws IllegalStateException : if this method was called when the Markov Engine was in an
	 * invalid state.
	 * @see #exportNetwork(File)
	 * @deprecated use {@link #importState(String)}
	 */
	@Deprecated
	public void importNetwork(File file) throws IOException, IllegalStateException;
	
	/**
	 * This method is the counterpart of {@link #exportState()},
	 * and it can be used to retrieve the shared probabilistic bayes net
	 * (only the shared network, so the asset networks may become potentially 
	 * desynchronized with the shared net).
	 * @param netString : the string generated by {@link #exportState()}
	 * @throws IllegalArgumentException : if the provided string is not in a valid format.
	 * @throws IllegalStateException : if this import sets the state of the ME to an inconsistent state.
	 */
	public void importState(String netString) throws IllegalArgumentException, IllegalStateException;
	
	/**
	 * This method returns some statistics of the currently used
	 * probabilistic network.
	 * For example, the returned object will contain several information, including the size of the network
	 * organized by number of possible states of a node.
	 * In a clique-based implementation, this will also include
	 * information related to clique size.
	 * @return instance of {@link NetStatistics}
	 * @see NetStatistics
	 * @see NetStatisticsImpl
	 * @see #getComplexityFactor(Map)
	 * @see #getComplexityFactor(Long, List)
	 */
	public NetStatistics getNetStatistics();
	
	/**
	 * If true, {@link #addTrade(Long, Date, String, long, long, List, List, List, List, boolean)}
	 * and {@link #addTrade(Long, Date, String, TradeSpecification, boolean)}
	 * will attempt to use house account to run corrective trades when the old probabilities provided by the caller is different
	 * from the actual probabilities retrieved from the engine before trade.
	 * @param isToUseCorrectiveTrades : true to use the corrective trade feature. False otherwise.
	 * @see QuestionEvent#isCorrectiveTrade()
	 * @see TradeSpecification#setOldProbabilities(List)
	 */
	public void setToUseCorrectiveTrades(boolean isToUseCorrectiveTrades);

//	/**
//	 * Note: Not required in the 1st iteration. 
//	 * This is similar to {@link #exportNetwork(File)}.
//	 * However, this method exports the current Bayes net structure into an output stream, so that 
//	 * the exported network can be redirected to some other medium (e.g. network stream).
//	 * @param stream : stream to be written.
//	 * @throws IOException : in case of Input/Output problem.
//	 * @throws IllegalStateException : if this method was called when the Markov Engine was in an
//	 * invalid state.
//	 * @see #exportNetwork(File)
//	 * @see #importNetwork(File)
//	 * @see #importNetwork(InputStream)
//	 */
//	public void exportNetwork(OutputStream stream) throws IOException, IllegalStateException;

//	/**
//	 * Note: Not required in the 1st iteration. 
//	 * This is similar to {@link #importNetwork(File)}.
//	 * However, this method imports the Bayes net structure from an input stream, so that 
//	 * the network can be redirected from some other medium (e.g. network stream).
//	 * @param stream : stream to be read
//	 * @throws IOException : in case of Input/Output problem.
//	 * @throws IllegalStateException : if this method was called when the Markov Engine was in an
//	 * invalid state.
//	 */
//	public void importNetwork(InputStream stream)  throws IOException, IllegalStateException;
	
	/**
	 * @return simply a text containing versioning information.
	 */
	public String getVersionInfo();
	
	/**
	 * This method can be used to get some metric that is related to the complexity (e.g. time complexity) of the 
	 * underlying algorithm after adding new arcs. For instance, in a Junction tree algorithm, treewidth is a plausible metric for this purpose.
	 * The size (state space) of the largest clique is also a plausible metric.
	 * @param newDependencies : a map representing arcs to be added to current Bayes net before calculating a metric for complexity.
	 * The keys are questions/nodes the arcs will be pointing to (i.e. child nodes), and values will be questions/nodes the arcs
	 * will be coming from (i.e. parent nodes).
	 * If the specified question/node did not exist, they shall be created before calculating the complexity metric.
	 * Setting this to null will return the complexity metric of current Bayes net.
	 * This method shall not actually modify the original Bayes net.
	 * If an arc exists (regardless of the direction), then this method will return the complexity factor after REMOVING such link.
	 * @param isLocal : if this is true and the underlying model has disconnected subnets, then this method
	 * will calculate the metrics of complexity only for the subnets containing the questions provided in the other argument.
	 * If this is false, then the entire network (in other words, all the disconnected subnets) will be considered.
	 * If no questions were specified, the result will be equivalent to when this argument is <code>false</code> anyway.
	 * @param isSum : if this is true, then this method will return the complexity metric identified by the key {@link #COMPLEXITY_FACTOR_SUM_CLIQUE_TABLE_SIZE}
	 * in the map returned by the method {@link #getComplexityFactors(Map, boolean, boolean)}. If false,
	 * then the metric identified by the key {@link #getDefaultComplexityFactorName()} will be used by default.
	 * @param isComplexityBeforeModification : if this is true, then the questions and links will not actually be included to the network model
	 * for calculating the complexity metrics. This is only useful when estimating the metrics locally for the disconnected subnet (i.e. when isLocal == true)
	 * without changing the network structure.
	 * @return a number indicating the complexity after changing network structure (i.e. after adding new arcs). In Junction tree algorithm, this will be the tree width.
	 * @see #getNetStatistics()
	 * @see #getComplexityFactor(List, List)
	 * @see #getComplexityFactor(Long, List)
	 * @throws IllegalArumentException if the new arcs to be added are invalid.
	 */
	public int getComplexityFactor(Map<Long, Collection<Long>> newDependencies, boolean isLocal, boolean isSum, boolean isComplexityBeforeModification);
	
	/**
	 * This method is equivalent to {@link #getComplexityFactor(Map, boolean, boolean, boolean)},
	 * with the 3 boolean arguments having the value <code>false</code>.
	 */
	public int getComplexityFactor(Map<Long, Collection<Long>> newDependencies);
	
	/**
	 * This is just an adaptor/wrapper for {@link #getComplexityFactor(Map)}, for a single question,
	 * created just in order to keep the signature compatible with {@link #addQuestionAssumption(Long, Date, long, List, List)}.
	 * @param childQuestionId : the question to be considered as the argument childQuestionId in {@link #addQuestionAssumption(Long, Date, long, List, List)}.
	 * If this is null, then it will return {@link #getComplexityFactor(Map)} with null argument.
	 * If an arc exists (regardless of the direction), then this method will return the complexity factor after REMOVING such link.
	 * @param parentQuestionIds : the questions to be considered as the "parents" (dependencies) in {@link #addQuestionAssumption(Long, Date, long, List, List)}
	 * If the arc exists, then the returned value will be a metric of the complexity
	 * after removing the existing arc.
	 * @param isLocal : if this is true and the underlying model has disconnected subnets, then this method
	 * will calculate the metrics of complexity only for the subnets containing the questions provided in the other argument.
	 * If this is false, then the entire network (in other words, all the disconnected subnets) will be considered.
	 * If no questions were specified, the result will be equivalent to when this argument is <code>false</code> anyway.
	 * @param isSum : if this is true, then this method will return the complexity metric identified by the key {@link #COMPLEXITY_FACTOR_SUM_CLIQUE_TABLE_SIZE}
	 * in the map returned by the method {@link #getComplexityFactors(Map, boolean, boolean)}. If false,
	 * then the metric identified by the key {@link #getDefaultComplexityFactorName()} will be used by default.
	 * @param isComplexityBeforeModification : if this is true, then the questions and links will not actually be included to the network model
	 * for calculating the complexity metrics. This is only useful when estimating the metrics locally for the disconnected subnet (i.e. when isLocal == true)
	 * without changing the network structure.
	 * @return  a number indicating the complexity after changing network structure. In Junction tree algorithm, this will be the tree width.
	 * @see #getNetStatistics()
	 * @see #getComplexityFactor(List, List)
	 * @see #getComplexityFactor(Map)
	 * @throws IllegalArumentException if the new arcs to be added are invalid.
	 */
	
	public int getComplexityFactor(Long childQuestionId, List<Long> parentQuestionIds, boolean isLocal, boolean isSum, boolean isComplexityBeforeModification);
	
	/**
	 * This method is equivalent to {@link #getComplexityFactor(List, List, boolean, boolean, boolean)},
	 * with the 3 boolean arguments having the value <code>false</code>.
	 */
	public int getComplexityFactor(Long childQuestionId, List<Long> parentQuestionIds);
	
	/**
	 * This is another wrapper for {@link #getComplexityFactor(Map)}.
	 * It is similar to {@link #getComplexityFactor(Long, List)} but this specifies arcs one-by-one, 
	 * so the method signature is not very compatible with {@link #addQuestionAssumption(Long, Date, long, List, List)}.
	 * <br/>
	 * <br/>
	 * The 1st element in childQuestionIds is related to 1st element in parentQuestionIds, the 2nd with 2nd, and so on.
	 * In other words, the following arcs will be created:
	 * <pre>
	 *  arc from parentQuestionIds.get(0) to childQuestionIds.get(0);
	 *  arc from parentQuestionIds.get(1) to childQuestionIds.get(1);
	 *  arc from parentQuestionIds.get(2) to childQuestionIds.get(2);
	 *  arc from parentQuestionIds.get(3) to childQuestionIds.get(3);
	 *  (...)
	 *  arc from parentQuestionIds.get(Math.min(parentQuestionIds.size(), childQuestionIds.size())) to childQuestionIds.get(Math.min(parentQuestionIds.size(), childQuestionIds.size()));
	 * </pre>
	 * Implementations may consider a special case when childQuestionIds is larger than parentQuestionIds.
	 * In this case, one option is to check if there are inexisting nodes in the rest of childQuestionIds and create them if so.
	 * If an arc exists (regardless of the direction), then this method will return the complexity factor after REMOVING such link.
	 * @param childQuestionIds : arcs will be pointing to this node. If the arc exists, then the returned value will be a metric of the complexity
	 * after removing the existing arc.
	 * @param parentQuestionIds : arcs will come from this node. If the arc exists, then the returned value will be a metric of the complexity
	 * after removing the existing arc.
	 * @param isLocal : if this is true and the underlying model has disconnected subnets, then this method
	 * will calculate the metrics of complexity only for the subnets containing the questions provided in the other argument.
	 * If this is false, then the entire network (in other words, all the disconnected subnets) will be considered.
	 * If no questions were specified, the result will be equivalent to when this argument is <code>false</code> anyway.
	 * @param isSum : if this is true, then this method will return the complexity metric identified by the key {@link #COMPLEXITY_FACTOR_SUM_CLIQUE_TABLE_SIZE}
	 * in the map returned by the method {@link #getComplexityFactors(Map, boolean, boolean)}. If false,
	 * then the metric identified by the key {@link #getDefaultComplexityFactorName()} will be used by default.
	 * @param isComplexityBeforeModification : if this is true, then the questions and links will not actually be included to the network model
	 * for calculating the complexity metrics. This is only useful when estimating the metrics locally for the disconnected subnet (i.e. when isLocal == true)
	 * without changing the network structure.
	 * @return a number indicating the complexity after changing network structure. In Junction tree algorithm, this will be the tree width.
	 * @see #getNetStatistics()
	 * @see #getComplexityFactor(Long, List)
	 * @see #getComplexityFactor(Map)
	 * @throws IllegalArumentException if the new arcs to be added are invalid.
	 */
	public int getComplexityFactor(List<Long> childQuestionIds, List<Long> parentQuestionIds, boolean isLocal, boolean isSum, boolean isComplexityBeforeModification);
	
	/**
	 * This method is equivalent to {@link #getComplexityFactor(List, List, boolean, boolean, boolean)},
	 * with the 3 boolean arguments having the value <code>false</code>.
	 */
	public int getComplexityFactor(List<Long> childQuestionIds, List<Long> parentQuestionIds);
	
	/**
	 * Estimates the complexity factor of arcs/links like in {@link #getComplexityFactor(Long, List)},
	 * but the estimation will be for each arcs/links, one-by-one, instead of making a joint estimate of all specified links.
	 * If a flag is on, the result will be sorted from the link with smallest complexity factor to largest complexity factor, 
	 * therefore this method can be used to provide a sorted list which indicates which assumptions are less likely to compromise performance
	 * (the first few assumptions have less impact -- i.e. better -- and the last ones have greater impact -- i.e. worse). 
	 * <br/> <br/>
	 * If an arc exists (regardless of the direction), then this method will return the complexity factor after REMOVING such link.
	 * @param childIds : the questions in which arc/link will be pointing to. In a Bayes net structure, this will identify the child node.
	 * If this is set to null, then all arcs/links related to assumptionIds will be considered.
	 * If this is null and assumptionIds is also null, then all existing arcs will be considered.
	 * <br/>
	 * The indexes of this list must be synchronized with the indexes of parentIds, because the i-th link is identified as
	 * a link from parentIds.get(i) to childId.get(i).
	 * @param parentIds : questions the arcs/links will be starting from. In a Bayes net structure, this will identify the parent nodes.
	 * If childIds is null, then all arcs/links related to these nodes will be considered.
	 * If this is null and childIds is also null, then all existing arcs will be considered.
	 * <br/>
	 * The indexes of this list must be synchronized with the indexes of childIds, because the i-th link is identified as
	 * a link from parentIds.get(i) to childId.get(i).
	 * @param complexityFactorLimit : assumptions with complexity factors strictly larger than this threshold will not be included in returned list.
	 * Use {@link Integer#MAX_VALUE} to virtually disable this option.
	 * @param sortByComplexityFactor : 
	 * if true, the returned list will be sorted by complexity factor. 
	 * If false, the ordering of the input argument (assumptionIds) will be kept.
	 * @param isLocal : if this is true and the underlying model has disconnected subnets, then this method
	 * will calculate the metrics of complexity only for the subnets containing the questions provided in the other argument.
	 * If this is false, then the entire network (in other words, all the disconnected subnets) will be considered.
	 * If no questions were specified, the result will be equivalent to when this argument is <code>false</code> anyway.
	 * @param isSum : if this is true, then this method will return the complexity metric identified by the key {@link #COMPLEXITY_FACTOR_SUM_CLIQUE_TABLE_SIZE}
	 * in the map returned by the method {@link #getComplexityFactors(Map, boolean, boolean)}. If false,
	 * then the metric identified by the key {@link #getDefaultComplexityFactorName()} will be used by default.
	 * @param isComplexityBeforeModification : if this is true, then the questions and links will not actually be included to the network model
	 * for calculating the complexity metrics. This is only useful when estimating the metrics locally for the disconnected subnet (i.e. when isLocal == true)
	 * without changing the network structure.
	 * @return a list with pairs (pair of nodes -- i.e. the link ; complexity factor). 
	 * {@link Entry#getKey()} is the pair or nodes representing the link, and {@link Entry#getValue()} is the complexity factor.
	 * In the link, {@link Entry#getKey()}  is the parent -- the node an arc is coming from -- and {@link Entry#getValue()} is the child. 
	 * @see #getComplexityFactor(Long, List)
	 * @see #getComplexityFactors(Map)
	 * @see #getComplexityFactorPerAssumption(Long, List, int, boolean)
	 */
	public List<Entry<Entry<Long,Long>, Integer>> getComplexityFactorPerAssumption(List<Long> childIds, List<Long> parentIds, int complexityFactorLimit, boolean sortByComplexityFactor,
			boolean isLocal, boolean isSum, boolean isComplexityBeforeModification);
	
	/**
	 * This method is equivalent to {@link #getComplexityFactorPerAssumption(List, List, int, boolean, boolean, boolean, boolean)},
	 * with the last 3 boolean arguments having the value <code>false</code>.
	 */
	public List<Entry<Entry<Long,Long>, Integer>> getComplexityFactorPerAssumption(List<Long> childIds, List<Long> parentIds, int complexityFactorLimit, boolean sortByComplexityFactor);
	
	/**
	 * This is just a wrapper for {@link #getComplexityFactorPerAssumption(List, List, int, boolean)} for a single child Id.
	 * <br/>
	 * This is equivalent to {@link #getComplexityFactorPerAssumption(List, List, int, boolean)} with all entries in the first list having
	 * the same value (childId).
	 * @param childId : the question in which arc/link will be pointing to. In a Bayes net structure, this will identify the child node.
	 * If this is set to null, then all arcs/links related to assumptionIds will be considered.
	 * If this is null and assumptionIds is also null, then all existing arcs will be considered.
	 * @param parentIds : questions the arcs/links will be starting from. In a Bayes net structure, this will identify the parent nodes.
	 * If childId is null, then all arcs/links related to these nodes will be considered.
	 * If this is null and childId is also null, then all existing arcs will be considered.
	 * @param complexityFactorLimit : assumptions with complexity factors strictly larger than this threshold will not be included in returned list.
	 * Use {@link Integer#MAX_VALUE} to virtually disable this option.
	 * @param sortByComplexityFactor : 
	 * if true, the returned list will be sorted by complexity factor. 
	 * If false, the ordering of the input argument (assumptionIds) will be kept.
	 * @param isLocal : if this is true and the underlying model has disconnected subnets, then this method
	 * will calculate the metrics of complexity only for the subnets containing the questions provided in the other argument.
	 * If this is false, then the entire network (in other words, all the disconnected subnets) will be considered.
	 * If no questions were specified, the result will be equivalent to when this argument is <code>false</code> anyway.
	 * @param isSum : if this is true, then this method will return the complexity metric identified by the key {@link #COMPLEXITY_FACTOR_SUM_CLIQUE_TABLE_SIZE}
	 * in the map returned by the method {@link #getComplexityFactors(Map, boolean, boolean)}. If false,
	 * then the metric identified by the key {@link #getDefaultComplexityFactorName()} will be used by default.
	 * @param isComplexityBeforeModification : if this is true, then the questions and links will not actually be included to the network model
	 * for calculating the complexity metrics. This is only useful when estimating the metrics locally for the disconnected subnet (i.e. when isLocal == true)
	 * without changing the network structure.
	 * @return a list with pairs (pair of nodes -- i.e. the link ; complexity factor). 
	 * {@link Entry#getKey()} is the pair or nodes representing the link, and {@link Entry#getValue()} is the complexity factor.
	 * In the link, {@link Entry#getKey()}  is the parent -- the node an arc is coming from -- and {@link Entry#getValue()} is the child. 
	 * @see #getComplexityFactorPerAssumption(List, List, int, boolean)
	 */
	public List<Entry<Entry<Long,Long>, Integer>>  getComplexityFactorPerAssumption(Long childId, List<Long> parentIds, int complexityFactorLimit, boolean sortByComplexityFactor,
			boolean isLocal, boolean isSum, boolean isComplexityBeforeModification);
	
	/**
	 * This method is equivalent to {@link #getComplexityFactorPerAssumption(Long, List, int, boolean, boolean, boolean, boolean)},
	 * with the last 3 boolean arguments having the value <code>false</code>.
	 */
	public List<Entry<Entry<Long,Long>, Integer>>  getComplexityFactorPerAssumption(Long childId, List<Long> parentIds, int complexityFactorLimit, boolean sortByComplexityFactor);
	
	/**
	 * This method can be used to get a tuple of complexity before adding arc, complexity after adding arc (whichever direction yielding a better result),
	 * and the direction of the arc which yielded better result (parent is {@link LinkSuggestion#getSuggestedParentId()}, and child is {@link LinkSuggestion#getSuggestedChildId()}).
	 * @param questionIds1 : one of the questions to be considered in the arc. The arc will be constituted by the question in index <code>i</code> of this list and in index
	 * <code>i</code> of questionIds2.
	 * @param questionIds2 : the other question to be considered in the arc. The arc will be constituted by the question in index <code>i</code> of this list and in index
	 * <code>i</code> of questionIds1.
	 * @param complexityFactorLimit : links with complexity factor exceeding this amount will be ignored.
	 * See {@link LinkSuggestion#getSuggestedChildId()} and {@link LinkSuggestion#getSuggestedParentId()} to retrieve which arcs were not ignored.
	 * Use {@link Integer#MAX_VALUE} here if you don't want arcs to be ignored.
	 * @param isToForceDirection : if this is true, then questionIds1 will always be considered as the parent.
	 * @param sortByComplexityFactor : if true, the returned list will be sorted by {@link LinkSuggestion#getPosteriorComplexity()}.
	 * @return : a list containing instances of {@link LinkSuggestion}, which is a tuple of complexity before adding arc {@link LinkSuggestion#getPriorComplexity()}, 
	 * after adding arc ({@link LinkSuggestion#getPosteriorComplexity()}) in a direction yielding better result, and the direction of the arc which yielded
	 * better result (direction: {@link LinkSuggestion#getSuggestedParentId()} -> {@link LinkSuggestion#getSuggestedChildId()})
	 * @see LinkSugestionImpl
	 * @see #getComplexityFactor(Long, List, boolean, boolean, boolean)
	 */
	public List<LinkSuggestion> getLinkComplexitySuggestions(List<Long> questionIds1, List<Long> questionIds2, int complexityFactorLimit, boolean isToForceDirection, boolean sortByComplexityFactor);
	
	/**
	 * This is just a wrapper for {@link #getLinkComplexitySuggestions(List, List, int, boolean, boolean)} with only 1 pair of question ids.
	 */
	public LinkSuggestion getLinkComplexitySuggestion(Long questionId1, Long questionId2, int complexityFactorLimit, boolean isToForceDirection);
	
	
	/**
	 * This method can be used to get a collection of metrics that are related to the complexity (e.g. time complexity) of the 
	 * underlying algorithm after adding new arcs. For instance, in a Junction tree algorithm, 
	 * the size (state space) of the largest clique is a plausible metric, and the sum of all cliques' table sizes can be also
	 * included.
	 * @param newDependencies : a map representing arcs to be added to current Bayes net before calculating a metric for complexity.
	 * The keys are questions/nodes the arcs will be pointing to (i.e. child nodes), and values will be questions/nodes the arcs
	 * will be coming from (i.e. parent nodes).
	 * If the specified question/node did not exist, they shall be created before calculating the complexity metric.
	 * Setting this to null will return the complexity metric of current Bayes net.
	 * This method shall not actually modify the original Bayes net.
	 * @param isLocal : if this is true and the underlying model has disconnected subnets, then this method
	 * will calculate the metrics of complexity only for the subnets containing the questions provided in the other argument.
	 * If this is false, then the entire network (in other words, all the disconnected subnets) will be considered.
	 * If no questions were specified, the result will be equivalent to when this argument is <code>false</code> anyway.
	 * @param isComplexityBeforeModification : if this is true, then the questions and links will not actually be included to the network model
	 * for calculating the complexity metrics. This is only useful when estimating the metrics locally for the disconnected subnet (i.e. when isLocal == true)
	 * without changing the network structure.
	 * @return a map from a name of the metric to the value of the metric. 
	 * For instance, an implementation using junction trees would return the following mapping:
	 * <br/> <br/>
	 * 
	 * {@value #COMPLEXITY_FACTOR_MAX_CLIQUE_TABLE_SIZE} -> 50;   (this is the size of the table of the largest clique)
	 * <br/>
	 * {@value #COMPLEXITY_FACTOR_SUM_CLIQUE_TABLE_SIZE}" -> 1048; (this is the sum of clique table size)
	 * @see #getNetStatistics()
	 * @see #getComplexityFactor(List, List)
	 * @see #getComplexityFactor(Long, List)
	 * @see #getComplexityFactor(Map)
	 * @see #COMPLEXITY_FACTOR_MAX_CLIQUE_TABLE_SIZE
	 * @see #COMPLEXITY_FACTOR_SUM_CLIQUE_TABLE_SIZE
	 * @throws IllegalArumentException if the new arcs to be added are invalid.
	 */
	public Map<String,Double> getComplexityFactors(Map<Long, Collection<Long>> newDependencies, boolean isLocal, boolean isComplexityBeforeModification);

	/**
	 * This method is equivalent to {@link #getComplexityFactors(Map, boolean, boolean)},
	 * with the 2 boolean arguments having the value <code>false</code>.
	 */
	public Map<String,Double> getComplexityFactors(Map<Long, Collection<Long>> newDependencies);
	
	
	/** This is the name of the key in the map returned by {@link #getComplexityFactors(Map)} which represents the maximum clique table size. */
	public static final String COMPLEXITY_FACTOR_MAX_CLIQUE_TABLE_SIZE = "MaxCliqueTableSize";
	/** This is the name of the key in the map returned by {@link #getComplexityFactors(Map)} which represents the sum of clique table sizes. */
	public static final String COMPLEXITY_FACTOR_SUM_CLIQUE_TABLE_SIZE = "SumCliqueTableSize";
	
	

	/**
	 * @return the key of {@link #getComplexityFactors(Map)} returned by default by {@link #getComplexityFactor(Map)}.
	 * @see #getComplexityFactor(List, List)
	 * @see #getComplexityFactor(Long, List)
	 * @see MarkovEngineInterface#COMPLEXITY_FACTOR_MAX_CLIQUE_TABLE_SIZE
	 * @see MarkovEngineInterface#COMPLEXITY_FACTOR_SUM_CLIQUE_TABLE_SIZE
	 */
	public String getDefaultComplexityFactorName();

	/**
	 * @param defaultComplexityFactorName : the key of {@link #getComplexityFactors(Map)} returned by default by {@link #getComplexityFactor(Map)}.
	 * @see #getComplexityFactor(List, List)
	 * @see #getComplexityFactor(Long, List)
	 * @see MarkovEngineInterface#COMPLEXITY_FACTOR_MAX_CLIQUE_TABLE_SIZE
	 * @see MarkovEngineInterface#COMPLEXITY_FACTOR_SUM_CLIQUE_TABLE_SIZE
	 */
	public void setDefaultComplexityFactorName(String defaultComplexityFactorName);
	
	/**
	 * This method estimates the strength of a link between two questions.
	 * The link doesn't have to exist.
	 * Implementations may use metrics like mutual information for this value.
	 * @param questionId1
	 * @param questionId2
	 * @return a numeric value which is close to zero if the link is weak.
	 */
	public float getLinkStrength(Long questionId1, Long questionId2);
	
	/**
	 * This method is similar to {@link #getLinkStrength(Long, Long)}, but it 
	 * calculates the strength of a collection of links.
	 * @param questionIds1 : list of one of the questions. The i-th element of this list
	 * will be matched with the i-th element of the other list.
	 * @param questionIds2 : the other list of questions. The i-th element of this list
	 * will be matched with the i-th element of the other list.
	 * @return a list of numeric values which are close to zero if link is weak.
	 * The i-th element in this list represents the metric for the i-th question in questionIds1 and
	 * i-th question in questionIds2.
	 */
	public List<Float> getLinkStrengthList(List<Long> questionIds1, List<Long> questionIds2);
	
	/**
	 * @return : this method is similar to {@link #getLinkStrength(Long, Long)} or {@link #getLinkStrengthList(List, List)},
	 * but it is executed for all arcs currently present in the system.
	 * @see #getLinkStrength(Long, Long)
	 * @see #getLinkStrengthList(List, List)
	 */
	public List<LinkStrength> getLinkStrengthAll();
	
	
	/**
	 * This method is a convenience method for {@link #getLinkStrengthAll()} and {@link #getComplexityFactor(Long, List)}.
	 * It virtually returns the link strength of all arcs present in the system (as in {@link #getLinkStrengthAll()}),
	 * but the returned objects will also contain a field representing the complexity factor you would get by removing such link,
	 * (as in {@link #getComplexityFactor(Long, List)}).
	 * @return a list of {@link LinkStrengthComplexity}
	 * @see LinkStrengthComplexity#getComplexityFactor()
	 */
	public List<LinkStrengthComplexity> getLinkStrengthComplexityAll();
}
