package edu.gmu.ace.daggre;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;




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
 * Some methods present here are based on document 
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
public interface DAGGREUnBBayesFacade {
	
	/**
	 * Assets are managed by a data structure known as the asset tables
	 * (they are clique-tables containing non-normalized float values).
	 * When an asset table is instantiated (i.e. when a new user is created,
	 * and then a new asset table is created for that user), each
	 * cell of the asset table should be filled with default (uniform) values initially.
	 * <br/><br/>
	 * Note: the assets (S-values) and q-values (values actually stored in
	 * the tables) are related with a logarithm relationship
	 * S = b*log(q). So, the values in the asset tables may not actually be
	 * the values of assets directly.
	 * @return the value to be filled into each cell of the q-tables when the tables are initialized.
	 */
	public float getDefaultInitialQTableValue();
	

	/**
	 * Assets are managed by a data structure known as the asset tables
	 * (they are clique-tables containing non-normalized float values).
	 * When an asset table is instantiated (i.e. when a new user is created,
	 * and then a new asset table is created for that user), each
	 * cell of the asset table should be filled with default (uniform) values initially.
	 * <br/><br/>
	 * Note: the assets (S-values) and q-values (values actually stored in
	 * asset tables) are related with a logarithm relationship
	 * S = b*log(q). So, the values in the asset tables may not actually be
	 * the values of assets directly.
	 * @param defaultValue : the value to be filled into each cell of the q-tables when the tables are initialized.
	 */
	public void setDefaultInitialQTableValue(float defaultValue);
	
	/**
	 * Assets (S-values) and q-values (values actually stored in
	 * asset tables) are related with a logarithm relationship
	 * S = b*log(q) with log being a logarithm function of some basis. 
	 * @return the base of the current logarithm function used
	 * for converting q-values to assets.
	 * @see #setCurrentLogBase(double)
	 * @see #getCurrentCurrencyConstant()
	 * @see #setCurrentCurrencyConstant(double)
	 * @see #getCash(long, Properties)
	 */
	public double getCurrentLogBase();
	
	/**
	 * Assets (S-values) and q-values (values actually stored in
	 * asset tables) are related with a logarithm relationship
	 * S = b*log(q), with log being a logarithm function of some base. 
	 * @param base : the base of the current logarithm function used
	 * for converting q-values to assets.
	 * @see #getCurrentLogBase()
	 * @see #getCurrentCurrencyConstant()
	 * @see #setCurrentCurrencyConstant(double)
	 * @see #getCash(long, Properties)
	 */
	public void setCurrentLogBase(double base);
	
	/**
	 *  Assets (S-values) and q-values (values actually stored in
	 * asset tables) are related with a logarithm relationship
	 * S = b*log(q), with b being a constant for defining the "unit
	 * of currency" (more precisely, this constant defines how sensitive
	 * is the assets).
	 * @return the current value of b, the "unit of currency""
	 * @see #getCurrentLogBase()
	 * @see #setCurrentLogBase(double)
	 * @see #setCurrentCurrencyConstant(double)
	 * @see #getCash(long, Properties)
	 */
	public double getCurrentCurrencyConstant();
	

	/**
	 *  Assets (S-values) and q-values (values actually stored in
	 * asset tables) are related with a logarithm relationship
	 * S = b*log(q), with b being a constant for defining the "unit
	 * of currency" (more precisely, this constant defines how sensitive
	 * is the assets).
	 * @param b the current value of b to set, the "unit of currency""
	 * @see #getCurrentLogBase()
	 * @see #setCurrentLogBase(double)
	 * @see #getCurrentCurrencyConstant()
	 * @see #getCash(long, Properties)
	 */
	public void setCurrentCurrencyConstant(double b);
	
	/**
	 * Creates a new network from a network structure specification.
	 * Different from {@link #addQuestion(int, String, String, List, List, List, Date, Properties)} or {@link #addAssumptiveQuestionLink(int, long, List, List, Date, Properties)},
	 * this method generates a completely new and independent structure, which is already optimized and clean.
	 * 
	 * @param questionIDAndDescription : a mapping from question ID (Long) to a brief description. The description is going to be used internally
	 * as the "name" of the random variable.
	 * @param questionIDToChoices : mapping between a question ID (Long) and its states (choices of the question). The order
	 * of the elements in the list will determine which values in cpd are related to which states.
	 * @param dependencyMapping : a mapping between the question ID (Long) and a list of its parents (dependencies). The order of
	 * the elements in the list will determine which values in cpd are related to which parents.
	 * 
	 * @param cpd : a mapping from the question ID (Long) to a list representing the conditional probability distribution (probabilities of
	 * a question - random variable - given its dependencies).
	 *  
	 * The order of the elements in the list are determined by the ordering of the lists in questionIDToChoices and dependencyMapping.
	 * For example, suppose T is the target random variable with question ID = 1; with states t1 and t2, and A1 and A2 are dependencies with states (a11, a12), and (a21 , a22) respectively.
	 * Then, cpd shall contain a mapping from key 1 (the question ID) to a list filled in the following way:<br/>
	 * index 0 - P(T=t1 | A1=a11, A2=a21)<br/>
	 * index 1 - P(T=t2 | A1=a11, A2=a21)<br/>
	 * index 2 - P(T=t1 | A1=a12, A2=a21)<br/>
	 * index 3 - P(T=t2 | A1=a12, A2=a21)<br/>
	 * index 4 - P(T=t1 | A1=a11, A2=a22)<br/>
	 * index 5 - P(T=t2 | A1=a11, A2=a22)<br/>
	 * index 6 - P(T=t1 | A1=a12, A2=a22)<br/>
	 * index 7 - P(T=t2 | A1=a12, A2=a22)<br/>
	 * @param dateTimeOfEdit : implementations of this interface may use this timestamp to store a history of modifications.
	 * @param properties : this object stores system properties of the markov engine.
	 * Classes implementing this interface may use these properties to set
	 * values of some attributes/parameters not explicitly declared in this interface.
	 * @return a handler ID which will be used by other methods in this interface in order to identify the loaded network.
	 * @throws IllegalArgumentException
	 */
	public int createNewNetwork( Map<Long, String> questionIDAndDescription, Map<Long, List<String>> questionIDToChoices,  Map<Long, List<Long>> dependencyMapping, Map<Long, List<Float>> cpd, Date dateTimeOfEdit, Properties properties) throws IllegalArgumentException;
	
	
	/**
	 * Disposes the network loaded by {@link #createNewNetwork(Map, Map, Map)}.
	 * This method only guarantees that the network identified by networkID will be
	 * removed from the datastructure which manages the loaded networks (e.g. remove
	 * the entry from a hash table).
	 * The network may be removed from the heap after the next garbage collection.
	 * @param networkID : the ID of the network to be disposed. This value is the one previously returned by {@link #createNewNetwork(Map, Map, Map, Map, Properties)}.
	 * @param properties : this object stores system properties of the markov engine.
	 * Classes implementing this interface may use these properties to set
	 * values of some attributes/parameters not explicitly declared in this interface.
	 * For instance, by adding a property {purge = false}, the network
	 * will be reset, but not removed from the hash table.
	 * @throws RuntimeException if the network could not be disposed.
	 */
	public void disposeLoadedNetwork(int networkID, Properties properties) throws RuntimeException;
	

	/**
	 * Instantiates and a new asset network (a data structure representing a user and
	 * the assets table) and puts it in a hash table for future reference.
	 * @param networkID :  the network in which the asset network (user + asset tables)
	 * will be based on. The asset network will manage the assets for the questions present in this network.
	 * @param userID : optional (may be set to null) - this is the desired ID for the user. If set to null,
	 * this method will return an automatically generated user ID.
	 * @param dateTimeOfEdit : implementations of this interface may use this timestamp to store a history of modifications.
	 * @param properties : this object stores system properties of the markov engine.
	 * Classes implementing this interface may use these properties to set
	 * values of some attributes/parameters not explicitly declared in this interface.
	 * For instance, setting the property {defaultInitialQTableValue = X} will be equivalent
	 * to calling {@link #setDefaultInitialQTableValue(float)} with the value X as its argument,
	 * and then {@link #createNewUser(int, Long, Properties)} with properties = null.
	 * @return the ID of the new user (value of userID, or generated automatically if userID == null).
	 */
	public long createNewUser(int networkID, Long userID, Date dateTimeOfEdit, Properties properties);
	
	/**
	 * Stores user's asset network and disposes it from the structure which manages the loaded users
	 * (e.g. remove entry from hash table). This method is useful when not all users are needed
	 * to be in the memory at once.
	 * @param userID : the ID of the user, which is also the identifier of the user's asset tables.
	 * Caution: if the asset table's structure was not previously created by using {@link #createNewUser(int, Long, Date, Properties)},
	 * then this method will save an 
	 * @param properties : this object stores system properties of the markov engine.
	 * Classes implementing this interface may use these properties to set
	 * values of some attributes/parameters not explicitly declared in this interface.
	 * For instance, by adding a property {purge = false}, the network
	 * will be reset, but not removed from the hash table.
	 * @throws IOException : if the saving process fails.
	 */
	public void saveAndDisposeUser(Long userID, Properties properties) throws IOException;
	
	/**
	 * Reloads to memory (and to the data structure managing the user's asset tables - e.g. a hash table) the asset tables
	 * disposed in {@link #saveAndDisposeUser(Integer, Long, Properties)}.
	 * This method, when used together with {@link #saveAndDisposeUser(Integer, Long, Properties)},
	 * is useful for reducing the memory usage when not all users are required to be in memory at once.
	 * @param properties : this object stores system properties of the markov engine.
	 * Classes implementing this interface may use these properties to set
	 * values of some attributes/parameters not explicitly declared in this interface.
	 * For instance, by adding a property {purge = false}, the network
	 * will be reset, but not removed from the hash table.
	 * @throws IOException : if loading process fails
	 */
	public void reloadUser(Long userID, Properties properties) throws IOException;
	
	
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
	 * 
	 * @param networkID : id of the network loaded by {@link #createNewNetwork(Map, Map, Map, Map, Properties)}, which contains the question (random variable) "T" and
	 * assumption "A"  of the example.
	 * 
	 * @param questionID : the id of the question to be edited (i.e. the random variable "T"  in the example)
	 * 
	 * @param questionState : the index (or ID) of the value of the question identified by the questionID (i.e. the state "t" of random variable "T")
	 * 
	 * @param assumptionIDs : a list (ordered collection) of question IDs which are the assumptions for T (i.e. random variable "A" in the example). The ordeer
	 * is important, because it will indicate which states of assumedStates are associated with which questions in assumptionIDs.
	 * 
	 * @param assumedStates : a list (ordered collection) representing the states of assumptionIDs assumed.
	 * 
	 * @param properties : this object stores system properties of the markov engine.
	 * Classes implementing this interface may use these properties to set
	 * values of some attributes/parameters not explicitly declared in this interface.
	 * 
	 * @return list of expected assets of the states (choices) of a question, given the assumptions. The indexes are
	 * relative to the indexes of the state.
	 * For example, assuming that the question identified by questionID is a boolean question (and also assuming
	 * that state 0 indicates false and state 1 indicates true); then, index 0 contains the expected assets of 
	 * the question while it is in state "false" (given assumptions), and index 1 contains the expected assets of the
	 * question while it is in state "true".
	 * 
	 * 
	 * @throws IllegalStateException when the network identified by networkID was not in a valid state (e.g. it was not loaded by {@link #createNewNetwork(Map, Map, Map, Map, Properties)}),
	 * 
	 * @throws IllegalArgumentException when any argument was invalid (e.g. inexistent question or state, or invalid assumptions).
	 */
	public List<Float> getAssetsIfStates(int networkID, long userID, long questionID, int questionState, List<Long> assumptionIDs, List<Integer> assumedStates, Properties properties) throws IllegalStateException, IllegalArgumentException;
	
	/**
	 * This method implements the feature for calculating the limits (upper and lower bounds) of an edit
	 * so that the minimum asset does not go below 0 (i.e. the min-q value does not go below 1).
	 * This feature is also described in
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
	 * @param userID : the ID of the current user. Users shall be managed by a hash table.
	 * 
	 * @param networkID : id of the network loaded by {@link #createNewNetwork(Map, Map, Map, Map, Properties)}, which contains the question (random variable) "T" and
	 * assumption "A"  of the example.
	 * 
	 * @param questionID : the id of the question to be edited (i.e. the random variable "T"  in the example)
	 * 
	 * @param questionState : the index (or ID) of the value of the question identified by the questionID (i.e. the state "t" of random variable "T").
	 * Note that if a question is a boolean random variable (i.e. only 2 possible states), then the limits for T=true will be complementary to the limits for T=false (i.e. 
	 * LIMIT(T=false) = 1-LIMIT(T=true)) and LIMIT(T=true) = 1-LIMIT(T=false)).
	 * 
	 * @param assumptionIDs : a list (ordered collection) of question IDs which are the assumptions for T (i.e. random variable "A" in the example). The ordeer
	 * is important, because it will indicate which states of assumedStates are associated with which questions in assumptionIDs.
	 * 
	 * @param assumedStates : a list (ordered collection) representing the states of assumptionIDs assumed.
	 * 
	 * @param properties : this object stores system properties of the markov engine.
	 * Classes implementing this interface may use these properties to set
	 * values of some attributes/parameters not explicitly declared in this interface.
	 * 
	 * @return a list (ordered collection) of size 2 representing respectively the lower and upper bounds for the allowed edit (allowed probability) of
	 * a state (referenced by argument "questionState") of a question (referenced by argument "questionID").
	 * 
	 * @throws IllegalStateException when the network identified by networkID was not in a valid state (e.g. it was not loaded by {@link #createNewNetwork(Map, Map, Map, Map, Properties)}),
	 * 
	 * @throws IllegalArgumentException when any argument was invalid (e.g. inexistent question or state, or invalid assumptions).
	 */
	public List<Float> getEditLimits(int networkID, long userID, long questionID, int questionState, List<Long> assumptionIDs, List<Integer> assumedStates, Properties properties) throws IllegalStateException, IllegalArgumentException;
	
	/**
	 *  This method implements the feature for actually doing an edit and update all the data structures (i.e. cliques
	 *  and separators of probabilities and assets).
	 *  
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
	 * 
	 * @param userID : the ID of the current user. Users shall be managed by a hash table. 
	 * 
	 * @param networkID : id of the network loaded by {@link #createNewNetwork(Map, Map, Map, Map, Properties)}, which contains the question (random variable) "T" and
	 * assumption "A"  of the example.
	 * 
	 * @param questionID : the id of the question to be edited (i.e. the random variable "T"  in the example)
	 * 
	 * @param oldValues :  this is a list (ordered collection) representing the probability values before the edit. 
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
	 * 
	 * 
	 * @param newValues : similarly to oldValues, this is a list (ordered collection) representing the probability values after the edit. 
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
	 * 
	 * @param assumptionIDs : list (ordered collection) representing the IDs of the questions to be assumed in this edit. The order is important,
	 * because the ordering in this list will be used in order to identify the correct indexes in "oldValues" and "newValues".
	 * 
	 * @param assumedStates : this is not necessary if oldValues and newValues contains full data (all cells of the conditional probability distribution),
	 * however, classes implementing this method may provide special treatment when this parameter is non-null. By default, implementations will ignore this parameter,
	 * so null should be passed.
	 * 
	 * @param isToAllowNegativeAssets : if true, assets are allowed to be negative after this trade. False if assets should never
	 * go below zero.
	 * 
	 * @param dateTimeOfEdit : implementations of this interface may use this timestamp to store a history of modifications.
	 * 
	 * @param properties : this object stores system properties of the markov engine.
	 * Classes implementing this interface may use these properties to set
	 * values of some attributes/parameters not explicitly declared in this interface.
	 * 
	 * @return true if trade was added successfully. False if insufficient assets for user and isToAllowNegative == false.
	 * 
	 * @throws IllegalStateException when the network identified by networkID was not in a valid state (e.g. it was not loaded by {@link #createNewNetwork(Map, Map, Map, Map, Properties)})
	 * 
	 * @throws IllegalArgumentException when any argument was invalid (e.g. inexistent question or state, invalid assumptions, or invalid probability bounds).
	 */
	public boolean addTrade(int networkID, long userID, long questionID, List<Float> oldValues, List<Float> newValues, List<Integer> assumptionIDs, List<Integer> assumedStates, boolean isToAllowNegativeAssets, Date dateTimeOfTrade, Properties properties) throws IllegalStateException, IllegalArgumentException;
	
	/**
	 * Calculates the values to be provided to {@link #addTrade(int, long, long, List, List, List, List, boolean, Date, Properties)}
	 * (related to the "newValues" argument) in order to exit (balance, or make the assets independent of a question given assumptions).
	 * 
	 * @param userID : the ID of the current user. Users shall be managed by a hash table.
	 * @param networkID : id of the network loaded by {@link #createNewNetwork(Map, Map, Map, Map, Properties)}, which contains the question (random variable) "T" and
	 * assumption "A"  of the example.
	 * @param questionID : the id of the question to be edited (i.e. the random variable "T"  in the example)
	 * @param assumptionIDs : list (ordered collection) representing the IDs of the questions to be assumed in this edit. The order is important,
	 * because the ordering in this list will be used in order to identify the correct indexes in "assumedStates" and in the returned list.
	 * @param assumedStates : states (choices) assumed in the questions provided in the argument "assumptionIDs". If this is set to null or empty, this method will return full data (e.g. all cells of a conditional probability distribution).
	 * @param properties : this object stores system properties of the markov engine.
	 * Classes implementing this interface may use these properties to set
	 * values of some attributes/parameters not explicitly declared in this interface.
	 * 
	 * @return a list (ordered collection) representing the probability values in a conditional edit. 
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
	 * 
	 * @throws IllegalStateException when the network identified by networkID was not in a valid state (e.g. it was not loaded by {@link #createNewNetwork(Map, Map, Map, Map, Properties)})
	 * @throws IllegalArgumentException when any argument was invalid (e.g. inexistent question or state, invalid assumptions, or invalid probability bounds).
	 */
	public List<Float> getValidProbValuesForExitingQuestion(int networkID, long userID, long questionID, List<Integer> assumptionIDs, List<Integer> assumedStates, Properties properties) throws IllegalStateException, IllegalArgumentException;
	
	/**
	 * @return list of objects representing entries of the history. Edits related
	 * to trades will be instances of {@link TradeHistory}. Edits
	 * related to changes in the structure of bayesian networks or asset tables
	 * will be instances of {@link StructureChangeHistory}.
	 * @param userID : (optional) if set to a non null value, the history will contain
	 * only changes related to this user.
	 * @param timestampFrom : date and time representing a timestamp. All trades starting after timestampFrom and before timestampTo will be considered.
	 * @param timestampTo : date and time representing a timestamp. All trades starting after timestampFrom and before timestampTo will be considered.
	 * @param properties : this object stores system properties of the markov engine.
	 * Classes implementing this interface may use these properties to set
	 * values of some attributes/parameters not explicitly declared in this interface.
	 * @throws IllegalArgumentException when any argument was invalid (e.g. invalid user or timestamp).
	 * @throws IOException when data related to history could not be retrieved.
	 */
	public List<EditHistory> getHistory(Long userID, Date timestampFrom, Date timestampTo, Properties properties) throws IOException, IllegalArgumentException;
	
	/**
	 * This method is virtually equivalent to calling {@link #addQuestion(int, String, String, List, List, List, Date, Properties)},
	 * returning {@link #getAssetsIfStates(int, long, long, int, List, List, Properties)}, and then reverting the trade.
	 * This method is useful for estimating the effects of a trade. More precisely, to estimate the long/short position after a trade 
	 * (but without actually doing the trade).
	 * 
	 * @param userID : the ID of the current user. Users shall be managed by a hash table. 
	 * @param networkID : id of the network loaded by {@link #createNewNetwork(Map, Map, Map, Map, Properties)}, which contains the question (random variable) "T" and
	 * assumption "A"  of the example.
	 * @param questionID : the id of the question to be edited (i.e. the random variable "T"  in the example)
	 * @param oldValues :  this is a list (ordered collection) representing the probability values before the edit. 
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
	 * 
	 * 
	 * @param newValues : similarly to oldValues, this is a list (ordered collection) representing the probability values after the edit. 
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
	 * 
	 * @param assumptionIDs : list (ordered collection) representing the IDs of the questions to be assumed in this edit. The order is important,
	 * because the ordering in this list will be used in order to identify the correct indexes in "oldValues" and "newValues".
	 * @param assumedStates : this is not necessary if oldValues and newValues contains full data (all cells of the conditional probability distribution),
	 * however, classes implementing this method may provide special treatment when this parameter is non-null. By default, implementations will ignore this parameter,
	 * so null should be passed.
	 * @param isToAllowNegativeAssets : if true, assets are allowed to be negative after this trade. False if assets should never
	 * go below zero.
	 * @param dateTimeOfEdit : implementations of this interface may use this timestamp to store a history of modifications.
	 * @param properties : this object stores system properties of the markov engine.
	 * Classes implementing this interface may use these properties to set
	 * values of some attributes/parameters not explicitly declared in this interface.
	 * 
	 * @return list of expected assets of the states (choices) of a question, given the assumptions. The indexes are
	 * relative to the indexes of the state.
	 * For example, assuming that the question identified by questionID is a boolean question (and also assuming
	 * that state 0 indicates false and state 1 indicates true); then, index 0 contains the expected assets of 
	 * the question while it is in state "false" (given assumptions), and index 1 contains the expected assets of the
	 * question while it is in state "true".
	 * 
	 * @throws IllegalStateException when the network identified by networkID was not in a valid state (e.g. it was not loaded by {@link #createNewNetwork(Map, Map, Map, Map, Properties)}).
	 * @throws IllegalArgumentException when any argument was invalid (e.g. inexistent question or state, invalid assumptions, or invalid probability bounds).
	 */
	public List<Float> previewTrade(int networkID, long userID, long questionID, List<Float> oldValues, List<Float> newValues, List<Integer> assumptionIDs, List<Integer> assumedStates, boolean isToAllowNegativeAssets, Date dateTimeOfTrade, Properties properties) throws IllegalStateException, IllegalArgumentException;
	
	// Resolve Question
	
	/**
	 * This method sets a question as "resolved" (it virtually removes a question from the bayesian network, after performing some auxiliary operations).
	 * 
	 * <br/>
	 * By default, this is a lazy method (in a sense that it just changes the data in a way that it causes minimum impact on user's asset tables and
	 * bayesian network, and without optimizing the junction tree structure). Call {@link #reassembleNetworkStructure(int, List, Date, Properties)}
	 * if you want to guarantee that the junction tree structure and user's asset structure are reorganized and clean.
	 * 
	 * @param networkID : id of the network loaded by {@link #createNewNetwork(Map, Map, Map, Map, Properties)}.
	 * @param questionID : the id of the question to be resolved.
	 * @param choice : the index of the value (state) of the question identified by the questionID (i.e. the resolved question).
	 * @param dateTimeOfEdit : implementations of this interface may use this timestamp to store a history of modifications.
	 * @param properties : this object stores system properties of the markov engine.
	 * Classes implementing this interface may use these properties to set
	 * values of some attributes/parameters not explicitly declared in this interface.
	 * @throws IllegalStateException when the network identified by networkID was not in a valid state (e.g. it was not loaded by {@link #createNewNetwork(Map, Map, Map, Map, Properties)}).
	 * @throws IllegalArgumentException when any argument was invalid (e.g. inexistent question or state).
	 */
	public void resolveQuestion(int networkID, long questionID, int choice, Date dateTimeOfEdit, Properties properties) throws IllegalStateException, IllegalArgumentException;
	
	// Question Management
	
	/**
	 * Reverts trades performed between two timestamps.
	 * This method can be used when a question was supposed to be solved, but it was not solved, and some users
	 * added trades on that question.
	 * @param networkID : the bayesian network containing the question to be reverted.
	 * @param userID : (optional) the user to have asset tables reverted. If set to null, all users will be considered.
	 * @param questionID : the question to be reverted.
	 * @param timestampFrom : date and time representing a timestamp. All trades starting after timestampFrom and before timestampTo will be reverted.
	 * @param timestampTo : date and time representing a timestamp. All trades starting after timestampFrom and before timestampTo will be reverted.
	 * @param assumptionIDs : (optional) if set to a non-null and non-empty value, only trades assuming these assumptions will be reverted.
	 * The order is important, because the order will determine which states in assumedStates are related to which questions.
	 * @param assumedStates : (optional) if set to a non-null and non-empty value, only trades with assumptions set to these states (i.e. choices)
	 * will be reverted.
	 * @param isToAllowNegativeAssets : if true, assets are allowed to be negative after reverting the trades. False if assets should never
	 * go below zero.
	 * @param properties : this object stores system properties of the markov engine.
	 * Classes implementing this interface may use these properties to set
	 * values of some attributes/parameters not explicitly declared in this interface.
	 * @return true if trades were reverted successfully. False if isToAllowNegativeAssets == false and the assets of any user went to negative.
	 * @throws IllegalStateException when the network identified by networkID was not in a valid state (e.g. it was not loaded by {@link #createNewNetwork(Map, Map, Map, Map, Properties)}).
	 * @throws IllegalArgumentException when any argument was invalid (e.g. inexistent question or state).
	 */
	public boolean revertTrades(int networkID, Long userID, long questionID, Date timestampFrom, Date timestampTo, List<Integer> assumptionIDs, List<Integer> assumedStates, boolean isToAllowNegativeAssets, Properties properties)  throws IllegalStateException, IllegalArgumentException;
	
	
	/**
	 * This method adds a new question (i.e. a new node in a bayesian network).
	 * Implementations may require reinitialization of the bayesian network in order to guarantee efficiency,
	 * because uncontrolled inclusion of nodes in implementations based on junction trees may cause 
	 * the tree to become either too huge, sparse, or generate huge cliques.
	 * @param networkID : id of the network loaded by {@link #createNewNetwork(Map, Map, Map, Map, Properties)} where the new question should be added. 
	 * @param name : brief name of the question. This is going to be an internal identifier of a node in the bayesian network.
	 * @param description : brief description of the question. This is like a comment, and may be ignored.
	 * @param states : the possible choices of the new question (i.e. the states of a random variable). If it is a boolean question,
	 * this list should be filled with something like ["true", "false"].
	 * @param assumptiveQuestionIDs : IDs of the questions to be considered as dependencies (parents of the new node in the bayesian network).
	 * It's better for the dependencies to be specified during the creation of a node, because this is what
	 * determines the size of a clique, or where to add a node if the cliques are already present.
	 * 
	 * 
	 * @param cpd : this is a list (ordered collection) representing the conditional probability distribution after the edit. 
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
	 * 
	 * <br/>
	 * By default, this is a lazy method (in a sense that it just changes the structure in a way that it causes minimum impact on user's asset tables, without
	 * optimizing the junction tree structure or user's asset table structures). Call {@link #reassembleNetworkStructure(int, List, Date, Properties)}
	 * if you want to guarantee that the junction tree structure and user's asset structure are reorganized and clean.
	 * 
	 * @param dateTimeOfEdit : implementations of this interface may use this timestamp to store a history of modifications.
	 * @param properties : this object stores system properties of the markov engine.
	 * Classes implementing this interface may use these properties to set
	 * values of some attributes/parameters not explicitly declared in this interface.
	 * @return the ID of the new question.
	 * @throws IllegalStateException when the network identified by networkID was not in a valid state (e.g. it was not loaded by {@link #createNewNetwork(Map, Map, Map, Map, Properties)}).
	 * @throws IllegalArgumentException when any argument was invalid (e.g. name or description was invalid).
	 */
	public long addQuestion(int networkID, String name, String description, List<String> states, List<Long> assumptiveQuestionIDs, List<Float> cpd, Date dateTimeOfEdit, Properties properties) throws IllegalStateException, IllegalArgumentException;
	
	/**
	 * This method adds a new direct dependency (i.e. an edge between nodes in a bayesian network).
	 * Note that this method may require
	 * recompilation of the bayesian network (i.e. may need to generate a new junction tree).
	 * <br/>
	 * By default, this is a lazy method (in a sense that it just changes the structure in a way that it causes minimum impact on user's asset tables, without
	 * optimizing the junction tree structure or user's asset table structures). Call {@link #reassembleNetworkStructure(int, List, Date, Properties)}
	 * if you want to guarantee that the junction tree structure and user's asset structure are reorganized and clean.
	 *
	 * @param networkID : id of the network loaded by {@link #createNewNetwork(Map, Map, Map, Map, Properties)}, where the new edge will be added.
	 * @param questionID : the id of the random variable (question) to become the child node.
	 * @param assumptiveQuestionIDs : ids of the random variables (questions) to become the parent nodes (i.e. the dependencies).
	 * 
	 * @param cpd : this is a list (ordered collection) representing the conditional probability distribution after the edit. 
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
	 * 
	 * @param dateTimeOfEdit : implementations of this interface may use this timestamp to store a history of modifications.
	 * @param properties : this object stores system properties of the markov engine.
	 * Classes implementing this interface may use these properties to set
	 * values of some attributes/parameters not explicitly declared in this interface.
	 * @throws IllegalStateException when the network identified by networkID was not in a valid state (e.g. it was not loaded by {@link #createNewNetwork(Map, Map, Map, Map, Properties)})
	 * @throws IllegalArgumentException when any argument was invalid (e.g. ids were invalid).
	 */
	public void addAssumptiveQuestionLink(int networkID, long questionID, List<Long> assumptiveQuestionIDs, List<Float> cpd, Date dateTimeOfEdit, Properties properties) throws IllegalStateException, IllegalArgumentException;
	
	/**
	 * Given an existing question, converts its choices into more detailed ones (i.e. adds more possible choices to a question).
	 * For example, given a question with choices {Virginia, Washington DC}, this method can be used to 
	 * convert the choices into {North of Virginia, South of Virginia, North of Washington DC,  South of Washington DC} (i.e. 2 choices
	 * were "split" to 4).  
	 * <br/>
	 * By default, this is a lazy method (in a sense that it just changes the structure in a way that it causes minimum impact on user's asset tables, without
	 * optimizing the junction tree structure or user's asset table structures). Call {@link #reassembleNetworkStructure(int, List, Date, Properties)}
	 * if you want to guarantee that the junction tree structure and user's asset structure are reorganized and clean.
	 * @param networkID : id of the network loaded by {@link #createNewNetwork(Map, Map, Map, Map, Properties)} containing the question to be modified. 
	 * @param questionID : the id of the random variable (question) whose possible states (i.e. choices) will be changed.
	 * @param states : the possible choices of the new question (i.e. the states of a random variable).
	 * @param cpd : this is a list (ordered collection) representing the conditional probability distribution after the edit. 
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
	 * @param dateTimeOfEdit : implementations of this interface may use this timestamp to store a history of modifications.
	 * @param properties : this object stores system properties of the markov engine.
	 * Classes implementing this interface may use these properties to set
	 * values of some attributes/parameters not explicitly declared in this interface.
	 * @throws IllegalStateException
	 * @throws IllegalArgumentException
	 */
	public void splitQuestion(int networkID, long questionID, List<String> states, List<Float> cpd, Date dateTimeOfEdit, Properties properties) throws IllegalStateException, IllegalArgumentException;
	
	
	/**
	 * This method performs the best effort in order to optimize the structure of bayesian network and its associated user's asset structures.
	 * Basically, it tries the following:
	 * reassemble the junction tree structure (including the asset structures);
	 * disposes resolved questions from the memory;
	 * deletes virtual nodes, if present.
	 * 
	 * @param networkID : the network to be optimized. It must be loaded by {@link #createNewNetwork(Map, Map, Map, Map, Date, Properties)}
	 * @param users : (optional) only the asset tables of these users will be reassembled. If null, all users will be assembled. If empty, no users will
	 * be reassembled.
	 * @param dateTimeOfEdit : implementations of this interface may use this timestamp to store a history of modifications.
	 * @param properties
	 * @throws IllegalStateException
	 * @throws IllegalArgumentException
	 */
	public void reassembleNetworkStructure(int networkID, List<Long> users, Date dateTimeOfEdit, Properties properties) throws IllegalStateException, IllegalArgumentException;
	
	/**
	 * Obtains the ids of the questions that are potential assumptions of a given question. 
	 * This method is necessary when the algorithm implemented by a class implementing this interface requires some restrictions 
	 * on the assumptions (e.g. the assumptions should be in a same clique).
	 * @param networkID : id of the network loaded by {@link #createNewNetwork(Map, Map, Map, Map, Properties)}.
	 * @param questionID : the id of the random variable (question) to be analyzed.
	 * @param assumptions : because cliques overlap, implementations based on cliques (e.g. potential assumptions are limited
	 * to the cliques containing a question with "questionID" as its ID) can initially have nodes in several cliques
	 * as assumptions, but after one assumption is selected, only cliques containing questionID and the new assumptions simultaneously
	 * can be selected after that. This parameter indicates what assumptions were selected so far.
	 * @param properties : this object stores system properties of the markov engine.
	 * Classes implementing this interface may use these properties to set
	 * values of some attributes/parameters not explicitly declared in this interface.
	 * @return a list (ordered collection) of ids of the random variables (i.e. questions) that can be potential assumptions of a trade.
	 * @throws IllegalStateException when the network identified by networkID was not in a valid state (e.g. it was not loaded by {@link #createNewNetwork(Map, Map, Map, Map, Properties)}).
	 * @throws IllegalArgumentException when any argument was invalid (e.g. ids were invalid).
	 * @see #addTrade(int, long, long, List, List, List, List, Properties)
	 */
	public List<Long> getDirectQuestionAssumptions(int networkID, long questionID, List<Long> assumptions, Properties properties) throws IllegalStateException, IllegalArgumentException;
	
	/**
	 * @param networkID : id of the network loaded by {@link #createNewNetwork(Map, Map, Map, Map, Properties)}.
	 * @param questionID : the id of the random variable (question) to be analyzed.
	 * @param properties : this object stores system properties of the markov engine.
	 * Classes implementing this interface may use these properties to set
	 * values of some attributes/parameters not explicitly declared in this interface.
	 * @return true if the question is a multiple-choice question. False if it is boolean.
	 */
	public boolean isMulti(int networkID, long questionID, Properties properties);
	
	/**
	 * @param networkID : id of the network loaded by {@link #createNewNetwork(Map, Map, Map, Map, Properties)}.
	 * @param questionID : the id of the random variable (question) to be analyzed.
	 * @param properties : this object stores system properties of the markov engine.
	 * Classes implementing this interface may use these properties to set
	 * values of some attributes/parameters not explicitly declared in this interface.
	 * @return a list (ordered collection) of the possible states (choices) of the random variable (i.e. question).
	 * The order is important, because it represents the indexes of the states (choices), which may be used in methods like
	 * {@link #resolveQuestion(int, long, int, Properties)} or  {@link #isLongPosition(int, long, long, int, List, List, Properties)}
	 * whose states are represented as indexes instead of its actual String contents.
	 */
	public List<String> getQuestionChoices(int networkID, long questionID, Properties properties);
	
	// Manage Balance 
	
	/**
	 * Obtains the minimum asset value, which is the minimum q-value with the log operation applied.
	 * @param userID : the ID of the owner of the asset table.
	 * @param assumptionIDs : the IDs of the questions to be assumed in this edit.
	 * @param properties : this object stores system properties of the markov engine.
	 * Classes implementing this interface may use these properties to set
	 * values of some attributes/parameters not explicitly declared in this interface.
	 * Implementations of this method may set the property {assyncrhonous = true} to
	 * trigger fast cash calculation algorithm, and do polling to repeatedly call this method
	 * to obtain more detailed values. Setting property {assyncrhonous = false} or property {abort = true}
	 * should stop fast cash calculation algorithm.
	 * @return the minimum asset value obtained by executing a min calibration algorithm in the asset tables.
	 * @throws IllegalArgumentException when any argument was invalid (e.g. ids were invalid).
	 * @see #getCurrentLogBase()
	 * @see #getCurrentCurrencyConstant()
	 */
	public float getCash(long userID, List<Long> assumptionIDs, Properties properties) throws IllegalArgumentException;
	
	/**
	 * Modifies the contents of all cells in the asset table in order to guarantee
	 * that the min asset returned by {@link #getCash(long, Properties)} is increased (decreased, if negative value is provided)
	 * by value specified by the argument "value".
	 * @param userID : the ID of the owner of the asset table.
	 * @param value : the minimum asset value to be added into the current value (negative values will decrease the current value)
	 * @param dateTimeOfEdit : implementations of this interface may use this timestamp to store a history of modifications.
	 * @param isToAllowNegativeAssets : if true, assets are allowed to be negative. False if assets should never
	 * go below zero.
	 * @param properties : this object stores system properties of the markov engine.
	 * Classes implementing this interface may use these properties to set
	 * values of some attributes/parameters not explicitly declared in this interface.
	 * @return true if cash was added successully. False if isToAllowNegativeAssets == false and assets has gone below zero.
	 * @throws IllegalArgumentException when any argument was invalid (e.g. ids were invalid).
	 * @see #getCurrentLogBase()
	 * @see #getCurrentCurrencyConstant()
	 */
	public boolean addCash(long userID, float value, Date dateTimeOfEdit, boolean isToAllowNegativeAssets, Properties properties) throws IllegalArgumentException;
	
	
	/**
	 * Obtains the q-table of a user. This method encapsulates {@link #assetsCommittedByUser(long, Properties)}
	 * and offers a way to access assets without using the {@link unbbayes.prs.bn.AssetNetwork} class.
	 * Basically, questionID and assumptionIDs
	 * 
	 * 
	 * @param userID : the ID of the owner of the asset table.
	 * @param questionID :  the ID of the question to be considered.
	 * @param assumptionIDs : list (ordered collection) of question IDs assumed when obtaining the q-tables. This list will indicate
	 * which cells in the q-table will be considered.
	 * @param assumedStates : indexes of the states of the questions specified in assumptionIDs. Naturally, it must have the same size of assumptionIDs. 
	 * @param properties : this object stores system properties of the markov engine.
	 * Classes implementing this interface may use these properties to set
	 * values of some attributes/parameters not explicitly declared in this interface.
	 * @return the assets of each possible states (i.e. choice - in case of boolean question, the size will be always 2) of the 
	 * question identified by questionID. The order indicates which value is related to which state (e.g the 1st value is associated
	 * with the 1st state, and so on).
	 * @throws IllegalArgumentException when any argument was invalid (e.g. ids were invalid, sizes of the lists were unsynchronized, etc.).
	 * @see #assetsCommittedByUser(long, Properties)
	 */
	public List<Float> assetsCommittedByUserQuestion(long userID, long questionID, List<Long> assumptionIDs, List<Integer> assumedStates,  Properties properties) throws IllegalArgumentException;

	// Score Users

	/**
	 * Obtains the expected assets (probability * asset).
	 * @param networkID : id of the network loaded by {@link #createNewNetwork(Map, Map, Map, Map, Properties)}.
	 * @param userID : the ID of the owner of the asset table.
	 * @param questionID :  the ID of the question to be considered. 
	 * @param stateIndex :  the index of the state in the question (identified by questionID) to be considered.
	 * @param assumptionIDs : (optional) list (ordered collection) of question IDs assumed when obtaining the estimated assets. If specified,
	 * the questions (i.e. random variables) with these IDs will be assumed to be in the states specified in the argument "assumedStates".
	 * @param assumedStates : (mandatory if assumptionIDs is specified - must have the same size of assumptionIDs) indexes
	 * of states (i.e. choices - if boolean, then it is either 0 or 1) of assumptionIDs to be assumed.
	 * @param properties : this object stores system properties of the markov engine.
	 * Classes implementing this interface may use these properties to set
	 * values of some attributes/parameters not explicitly declared in this interface.
	 * @return the expected assets.
	 * @throws IllegalStateException when the network identified by networkID was not in a valid state (e.g. it was not loaded by {@link #createNewNetwork(Map, Map, Map, Map, Properties)}),
	 * @throws IllegalArgumentException when any argument was invalid (e.g. inexistent question or state, or invalid assumptions).
	 */
	public float scoreUserEV(long networkID, long userID, long questionID, int stateIndex, List<Long> assumptionIDs, List<Integer> assumedStates, Properties properties) throws IllegalStateException, IllegalArgumentException;
	
    //Get Probabilities
	
	/**
	 * This method is for obtaining the probabilities of a question given assumptions. 
	 * This method is NOT expected to block other threads from accessing the probability values (except for 
	 * methods changing the values of such probabilities), because
	 * this method will supposedly be one of the bottlenecks (in a sense that it is likely to be
	 * the most called method) when integrated with DAGGRE main system.
	 * @return the probability of a question (i.e. random variable) given assumptions.
	 * The order is important for identifying the states (i.e. 1st value is for the 1st state, and so on).
	 * If the full info about the bayesian network is needed, use {@link #getBayesianNetwork(long, Properties)} instead.
	 * @param networkID : id of the network loaded by {@link #createNewNetwork(Map, Map, Map, Map, Properties)}.
	 * @param questionID : id of the question to obtain probability.
	 * @param assumptionIDs : (optional) list (ordered collection) of question IDs assumed when obtaining the estimated assets. If specified,
	 * the questions (i.e. random variables) with these IDs will be assumed to be in the states specified in the argument "assumedStates".
	 * @param assumedStates : (mandatory if assumptionIDs is specified - must have the same size of assumptionIDs) indexes
	 * of states (i.e. choices - if boolean, then it is either 0 or 1) of assumptionIDs to be assumed.
	 * @param properties : this object stores system properties of the markov engine.
	 * Classes implementing this interface may use these properties to set
	 * values of some attributes/parameters not explicitly declared in this interface.
	 * @throws IllegalStateException when the network identified by networkID was not in a valid state (e.g. it was not loaded by {@link #createNewNetwork(Map, Map, Map, Map, Properties)}),
	 * @throws IllegalArgumentException when any argument was invalid (e.g. inexistent question or state, or invalid assumptions).
	 */
	public List<Float> getProbList(long networkID, long questionID, List<Long> assumptionIDs, List<Integer> assumedStates, Properties properties) throws IllegalStateException, IllegalArgumentException;

}
