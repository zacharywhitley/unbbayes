package edu.gmu.ace.daggre;

import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.UUID;




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
	 * @see #addCash(long, Date, long, float, String)
	 * @see #addQuestion(long, Date, long, int, List)
	 * @see #addQuestionAssumption(long, Date, long, long, List)
	 * @see #addTrade(long, Date, long, long, long, List, List, List, List, Boolean)
	 * @see #startNetworkActions()
	 * @see #resolveQuestion(long, Date, long, int)
	 * @see #revertTrade(long, Date, Long, Long)
	 */
	public boolean commitNetworkActions(long transactionKey) throws IllegalArgumentException;
	

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
	 * @throws IllegalArgumentException when a key or Id is not found or a range of values or cpdÅfs is not legal (i.e. newValues > 100%)
	 */
	public boolean addQuestion(long transactionKey, Date occurredWhen, long questionId, int numberStates, List<Float> initProbs) throws IllegalArgumentException;
	
	
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
	 * @param transactionKey : key returned by {@link #startNetworkActions()}
	 * @param occurredWhen : implementations of this interface may use this timestamp to store a history of modifications.
	 * @param sourceQuestionId : id of the child
	 * @param assumptiveQuestionIds : ids of the parents. If cpd is set to null, then these questions will be ADDED as parents of sourceQuestionId.
	 * If cpd is non-null, then these parents will SUBSTITUTE the old parents.
	 * @param cpd : If a null/empty cpd is passed, values will be set by uniform distribution, and assumptiveQuestionIds will
	 * be ADDED as parents of sourceQuestionId. If non-null, then assumptiveQuestionIds will SUBSTITUTE the old parents of sourceQuestionId.
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
	public boolean addQuestionAssumption(long transactionKey, Date occurredWhen, long sourceQuestionId, List<Long> assumptiveQuestionIds,  List<Float> cpd) throws IllegalArgumentException;
	
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
	public boolean addCash(long transactionKey, Date occurredWhen, long userId, float assets, String description) throws IllegalArgumentException;
	
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
	 * @param tradeUUID : revert and history functions can refer to specific trade actions easier, by referring to this UUID.
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
	 * @param assumptionIds : list (ordered collection) representing the IDs of the questions to be assumed in this edit. The order is important,
	 * because the ordering in this list will be used in order to identify the correct indexes in "newValues".
	 * @param assumedStates : this is not necessary if newValues contains full data (all cells of the conditional probability distribution),
	 * however, classes implementing this method may provide special treatment when this parameter is non-null. By default, implementations will ignore this parameter,
	 * so null should be passed.
	 * @param allowNegative : If true (default is False), then checks for sufficient assets should be bypassed and we allow 
	 * the user to go into the hole
	 * @return the assets per state changed, if the user has sufficient assets 
	 * (as the values returned by {@link #getAssetsIfStates(int, long, long, int, List, List, Properties)}).
	 * @throws IllegalArgumentException when any argument was invalid (e.g. ids were invalid).
	 */
	public List<Float> addTrade(long transactionKey, Date occurredWhen, UUID tradeUUID, long userId, long questionId, List<Float> newValues, List<Long> assumptionIds, List<Integer> assumedStates,  Boolean allowNegative) throws IllegalArgumentException;

	/**
	 * This function will settle a specific question.
	 * Implementations of this method shall be synchronized.
	 * @param transactionKey : key returned by {@link #startNetworkActions()}
	 * @param occurredWhen : implementations of this interface may use this timestamp to store a history of modifications.
	 * @param questionID : the id of the question to be settled.
	 * @param settledState : index of the state of the question (with ID questionID) to be settled.
	 * @return true if successful.
	 * @throws IllegalArgumentException when any argument was invalid (e.g. ids were invalid).
	 */
	public boolean resolveQuestion(long transactionKey, Date occurredWhen, long questionID, int settledState) throws IllegalArgumentException;
	
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
	 * @param questionID : the id of the question to be reverted.
	 * @return true if successful.
	 * @throws IllegalArgumentException when any argument was invalid (e.g. ids were invalid).
	 */
	public boolean revertTrade(long transactionKey, Date occurredWhen,  Date tradesStartingWhen, Long questionId) throws IllegalArgumentException;
	
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
	 * @return the probability of a question (i.e. random variable) given assumptions.
	 * The order is important for identifying the states (i.e. 1st value is for the 1st state, and so on).
	 * @throws IllegalArgumentException when any argument was invalid (e.g. ids were invalid).
	 */
	public List<Float> getProbList(long questionId, List<Long>assumptionIds, List<Integer> assumedStates) throws IllegalArgumentException;
	
	
	/**
	 * Obtains the ids of the questions that are potential assumptions of a given question. 
	 * This method is necessary when the algorithm implemented by a class implementing this interface requires some restrictions 
	 * on the assumptions (e.g. the assumptions should be in a same clique).
	 * @param questionId  : the id of the random variable (question) to be analyzed.
	 * @param assumptiveQuestions : because cliques overlap, implementations based on cliques (e.g. potential assumptions are limited
	 * to the cliques containing a question with "questionID" as its ID) can initially have nodes in several cliques
	 * as assumptions, but after one assumption is selected, only cliques containing questionID and the new assumptions simultaneously
	 * can be selected after that. This parameter indicates what assumptions were selected so far.
	 * @return list of any possible assumptions that can be made on the question
	 * @throws IllegalArgumentException when any argument was invalid (e.g. ids were invalid).
	 */
	public List<Long> getPossibleQuestionAssumptions(long questionId, List<Long>assumptionIds) throws IllegalArgumentException;
	
	/**
	 * This method implements the feature for estimating whether a user is in a long or short position.
	 * The feature is also described in
	 * <a href="https://docs.google.com/document/d/1p1TY-paqEmJNshQYThr6H3SyR2-e6xmoXrI9HleqiPM/edit">https://docs.google.com/document/d/1p1TY-paqEmJNshQYThr6H3SyR2-e6xmoXrI9HleqiPM/edit</a>
	 * as follows:
	 * <br/><br/>
	 * 
	 * The intended edit is P(T=t|A=a). Before the edit, we tell the user his/her long/short position by calculating the expected score given {T=t, A=a}, and {T~=t, A=a}, respectively. 
	 * <br/><br/>
	 * Case 1 - given {T=t, A=a}: <br/>
	 * we change the corresponding cells of potential tables in cliques and separators, to be zeros where T~=t, or A~=a. Then, normalize all tables affected.
	 * Transform q-tables to be asset tables by S = b*log(q).
	 * Multiply asset tables with corresponding potential tables cell by cell, save results into L tables.
	 * Using Equation (2), compute expected score (S1) as sum_clq(sum_cell(L)) - sum_sep(sum_cell(L)).
	 * <br/><br/>
	 * Case 2 - given {T~=t, A=a}: <br/>
	 * similar to Case 1 above, except in step a, make the cells to be zeros where T=t, or A~=a. Other steps are exactly same. Return S2.
	 * If S1>S2, the user has long position on the intended edit; otherwise, he/she has short position on the intended edit.
	 * 
	 * @param userID : the ID of the current user. Users shall be managed by a hash table. 
	 * @param questionID : the id of the question to be edited (i.e. the random variable "T"  in the example)
	 * @param assumptionIDs : a list (ordered collection) of question IDs which are the assumptions for T (i.e. random variable "A" in the example). The ordeer
	 * is important, because it will indicate which states of assumedStates are associated with which questions in assumptionIDs.
	 * @param assumedStates : a list (ordered collection) representing the states of assumptionIDs assumed.
	 * 
	 * @param properties : this object stores system properties of the markov engine.
	 * Classes implementing this interface may use these properties to set
	 * values of some attributes/parameters not explicitly declared in this interface.
	 * 
	 * @return the change in user assets if a given states occurs if the specified assumptions are met. 
	 * The indexes are relative to the indexes of the states.
	 * In the case of a binary question this will return a [if_true, if_false] value, if multiple choice will return a [if_0, if_1, if_2...] value list
	 * For example, assuming that the question identified by questionID is a boolean question (and also assuming
	 * that state 0 indicates false and state 1 indicates true); then, index 0 contains the assets of 
	 * the question while it is in state "false" (given assumptions), and index 1 contains the assets of the
	 * question while it is in state "true".
	 * @throws IllegalArgumentException when any argument was invalid (e.g. inexistent question or state, or invalid assumptions).
	 */
	public List<Float> getAssetsIfStates(long userID, long questionID, List<Long> assumptionIDs, List<Integer> assumedStates) throws IllegalArgumentException;
	
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
	 */
	public List<Float> getEditLimits(long userId, long questionId, int questionState, List<Long>assumptionIds, List<Integer> assumedStates) throws IllegalArgumentException;
	
	/**
	 * @param userID : the ID of the owner of the asset table.
	 * @param assumptionIDs : the IDs of the questions to be assumed in this edit.
	 * @param assumedStates : a list (ordered collection) representing the states of assumptionIDs assumed.
	 * @return available cash (i.e. minimum assets) given a set of assumptions.
	 * @throws IllegalArgumentException when any argument was invalid (e.g. inexistent question or state, or invalid assumptions).
	 */
	public float getCash(long userId, List<Long>assumptionIds, List<Integer> assumedStates) throws IllegalArgumentException;
	
	/**
	 * Obtains the expected assets (probability * asset).
	 * @param userId : the ID of the user (owner of the assets).
	 * @param questionId :  the ID of the question to be considered. If set to null, this method shall be equivalent to {@link #scoreUserEv(long, List, List)}.
	 * @param assumptionIds : (optional) list (ordered collection) of question IDs assumed when obtaining the estimated assets. If specified,
	 * the questions (i.e. random variables) with these IDs will be assumed to be in the states specified in the argument "assumedStates".
	 * @param assumedStates : (mandatory if assumptionIDs is specified - must have the same size of assumptionIDs) indexes
	 * of states (i.e. choices - if boolean, then it is either 0 or 1) of assumptionIDs to be assumed.
	 * If it does not have the same size of assumptionIDs, MIN(assumptionIDs.size(), assumedStates.size()) shall be considered.
	 * @return current expected value portion of user score given a set of assumptions. 
	 * If questionId is set to null, then TOTAL current expected value portion of across all questions given a set of assumptions.
	 * @throws IllegalArgumentException when any argument was invalid (e.g. inexistent question or state, or invalid assumptions).
	 */
	public float scoreUserQuestionEv(long userId, Long questionId, List<Long>assumptionIds, List<Integer> assumedStates) throws IllegalArgumentException;
	
	/**
	 * @param userId : the ID of the user (owner of the assets).
	 * @param questionId :  the ID of the question to be considered. 
	 * @param assumptionIds : (optional) list (ordered collection) of question IDs assumed when obtaining the estimated assets. If specified,
	 * the questions (i.e. random variables) with these IDs will be assumed to be in the states specified in the argument "assumedStates".
	 * @param assumedStates : (mandatory if assumptionIDs is specified - must have the same size of assumptionIDs) indexes
	 * of states (i.e. choices - if boolean, then it is either 0 or 1) of assumptionIDs to be assumed.
	 * If it does not have the same size of assumptionIDs, MIN(assumptionIDs.size(), assumedStates.size()) shall be considered.
	 * @return a list of score expectations for each possible choice that could result. This is p(state)*user_assets(state).
	 * @throws IllegalArgumentException when any argument was invalid (e.g. inexistent question or state, or invalid assumptions).
	 */
	public List<Float> scoreUserQuestionEvStates(long userId, long questionId, List<Long>assumptionIds, List<Integer> assumedStates) throws IllegalArgumentException;
	
	/**
	 * THIS IS NOT REQUIRED BUT MAY BE A GOOD IDEA TO PROVIDE FOR EFFICIENCY AS A OPTIMIZED OPERATION.
	 * @param userId : the ID of the user (owner of the assets).
	 * @param assumptionIds : (optional) list (ordered collection) of question IDs assumed when obtaining the estimated assets. If specified,
	 * the questions (i.e. random variables) with these IDs will be assumed to be in the states specified in the argument "assumedStates".
	 * @param assumedStates : (mandatory if assumptionIDs is specified - must have the same size of assumptionIDs) indexes
	 * of states (i.e. choices - if boolean, then it is either 0 or 1) of assumptionIDs to be assumed.
	 * If it does not have the same size of assumptionIDs, MIN(assumptionIDs.size(), assumedStates.size()) shall be considered.
	 * @return TOTAL current expected value portion of across all questions given a set of assumptions.
	 * @throws IllegalArgumentException
	 * @see {@link #scoreUserQuestionEv(long, Long, List, List)}
	 */
	public float scoreUserEv(long userId, List<Long>assumptionIds, List<Integer> assumedStates) throws IllegalArgumentException;
	
	/**
	 * THIS IS NOT REQUIRED BUT MAY BE A GOOD IDEA TO PROVIDE FOR EFFICIENCY AS A OPTIMIZED OPERATION
	 * @param userId : the ID of the user (owner of the assets).
	 * @param assumptionIds : (optional) list (ordered collection) of question IDs assumed when obtaining the estimated assets. If specified,
	 * the questions (i.e. random variables) with these IDs will be assumed to be in the states specified in the argument "assumedStates".
	 * @param assumedStates : (mandatory if assumptionIDs is specified - must have the same size of assumptionIDs) indexes
	 * of states (i.e. choices - if boolean, then it is either 0 or 1) of assumptionIDs to be assumed.
	 * If it does not have the same size of assumptionIDs, MIN(assumptionIDs.size(), assumedStates.size()) shall be considered.
	 * @return TOTAL user score (expected_value + cash) across all questions given a set of assumptions.
	 * @throws IllegalArgumentException when any argument was invalid (e.g. inexistent question or state, or invalid assumptions).
	 */
	public float scoreUser(long userId, List<Long>assumptionIds, List<Integer> assumedStates) throws IllegalArgumentException;
	
	/**
	 * This function will return the affect (assets per state) of this trade similar to the addTrade function,
	 * but is a proposed what-if (non-binding) calculation only that does not impact the network.
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
	 * @param assumptionIDs : list (ordered collection) representing the IDs of the questions to be assumed in this edit. The order is important,
	 * because the ordering in this list will be used in order to identify the correct indexes in "newValues".
	 * @param assumedStates : this is not necessary if newValues contains full data (all cells of the conditional probability distribution),
	 * however, classes implementing this method may provide special treatment when this parameter is non-null. By default, implementations will ignore this parameter,
	 * so null should be passed.
	 * If it does not have the same size of assumptionIDs,Å@MIN(assumptionIDs.size(), assumedStates.size()) shall be considered.
	 * @return the assets per state changed, if the user has sufficient assets 
	 * (as the values returned by {@link #getAssetsIfStates(int, long, long, int, List, List, Properties)}).
	 * @throws IllegalArgumentException when any argument was invalid (e.g. ids were invalid).
	 */
	public List<Float> previewTrade(long userID, long questionID, List<Float> newValues, List<Integer> assumptionIDs, List<Integer> assumedStates) throws IllegalArgumentException;
	
	/**
	 * This method will determine the states of a balancing trade which would minimize impact once the question is resolved
	 * Ideally this balancing trade is one where all assetsifStates states where equal so settling the question would have no effect. 
	 * @param userID: the ID of the user (i.e. owner of the assets).
	 * @param questionID : the id of the question to be balanced.
	 * @param assumptionIDs : list (ordered collection) representing the IDs of the questions to be assumed in this edit. The order is important,
	 * because the ordering in this list will be used in order to identify the correct indexes in "newValues".
	 * @param assumedStates : this is not necessary if newValues contains full data (all cells of the conditional probability distribution),
	 * however, classes implementing this method may provide special treatment when this parameter is non-null. By default, implementations will ignore this parameter,
	 * so null should be passed.
	 * If it does not have the same size of assumptionIDs,Å@MIN(assumptionIDs.size(), assumedStates.size()) shall be considered. 
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
	 */
	public List<Float> determineBalancingTrade(long userID, long questionID, List<Integer> assumptionIDs, List<Integer> assumedStates) throws IllegalArgumentException;
	
	/**
	 * This function will return an ordered list of events that explain how the current probability of a question was determined. 
	 * In phase 1, this method may simply return the tradeId and history over time that directly impacted the question.
	 * @param questionID : filter for the history. Only history related to this question will be returned.
	 * @param assumptionIDs : filter for the history. Only histories related to questionID with these assumptions will be returned.
	 * @param assumedStates : filter for the history. Only histories related to assumptions with these states will be returned.
	 * If it does not have the same size of assumptionIDs,Å@MIN(assumptionIDs.size(), assumedStates.size()) shall be considered.
	 * @return the sequence of events.
	 * @throws IllegalArgumentException when any argument was invalid (e.g. ids were invalid).
	 * @see {@link QuestionEvent}
	 */
	public List<QuestionEvent> getQuestionHistory (Long questionID, List<Long> assumptionIDs, List<Integer> assumedStates) throws IllegalArgumentException;


	/**
	 * @param userId : ID of the user to be considered.
	 * @param assumptionIds : assumptions to be considered in obtaining the summary
	 * @param assumedStates : states of the assumptions. The order must be synchronized with assumptionIds.
	 * @return ordered list of score details (properties dictionary with parameters to display TBD) that shows a summary view of how the current score of a user was determined. 
	 * @throws IllegalArgumentException when any argument was invalid (e.g. ids were invalid).
	 */
	public List<Properties> getScoreSummary(long userId, List<Long> assumptionIds, List<Integer> assumedStates) throws IllegalArgumentException;
	
	/**
	 * @param userId : ID of the user to be considered.
	 * @param assumptionIds : assumptions to be considered in obtaining the summary
	 * @param assumedStates : states of the assumptions. The order must be synchronized with assumptionIds.
	 * @return ordered list of score details (properties dictionary with parameters to display TBD) 
	 * that provides a detailed view of how the current score of a user was determined. 
	 * @throws IllegalArgumentException when any argument was invalid (e.g. ids were invalid).
	 */
	public List<Properties> getScoreDetails(long userId, List<Long> assumptionIds, List<Integer> assumedStates) throws IllegalArgumentException;
}
