package edu.gmu.ace.daggre;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import unbbayes.prs.bn.AssetNetwork;
import unbbayes.prs.bn.Clique;
import unbbayes.prs.bn.JunctionTree;
import unbbayes.prs.bn.ProbabilisticNetwork;

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
	 * Loads the bayesian network representing the questions.
	 * The loaded networks shall be managed by a hash table. Hence, if a network was already loaded,
	 * the ID of the previously loaded network will be returned, if it was not disposed yet.
	 * Classes implementing this method may provide different behavior depending on the file's extension
	 * or header.
	 * @param networkFile : file representing the bayesian network. The file shall be closed at the end of this method.
	 * @return a handler ID which will be used by other methods in this interface in order to identify the loaded network.
	 * @throws IOException when the network could not be loaded
	 */
	public int loadNetworkFromFile(File networkFile) throws IOException;
	
	public int createNewNetwork( Map<Long, String> questionIDAndDescription, Map<Long, List<String>> questionIDToChoices,  Map<Long, List<Long>> dependencyMapping, Map<Long, List<Float>> cpd, Properties properties) throws IllegalArgumentException;
	
	
	/**
	 * Disposes the network loaded by {@link #loadNetworkFromFile(File)} or {@link #createNewNetwork(Map, Map, Map)}.
	 * This method only guarantees that the network identified by networkID will be
	 * removed from the datastructure which manages the loaded networks (e.g. remove
	 * the entry from a hash table).
	 * The network may be removed from the heap after the next garbage collection.
	 * @param networkID : the ID of the network to be disposed. This value is the one previously returned by {@link #loadNetworkFromFile(File)}
	 * or by {@link #createNewNetwork(Map, Map, Map, Map, Properties)}.
	 * @param properties : this object stores system properties of the markov engine.
	 * Classes implementing this interface may use these properties to set
	 * values of some attributes/parameters not explicitly declared in this interface.
	 * For instance, by adding a property {purge = false}, the network
	 * will be reset, but not removed from the hash table.
	 * @throws RuntimeException if the network could not be disposed.
	 */
	public void disposeLoadedNetwork(int networkID, Properties properties) throws RuntimeException;
	

	public long createNewUser(int networkID, Properties properties);
	
	
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
	 * @param userID : the ID of the current user. Users shall be managed by a hash table. If a new user is identified, the new user shall be automatically
	 * created.
	 * 
	 * @param networkID : id of the network loaded by {@link #loadNetworkFromFile(File)} or {@link #createNewNetwork(Map, Map, Map, Map, Properties)}, which contains the question (random variable) "T" and
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
	 * @return true if user is in a "long" state for that question (given assumptions). False otherwise.
	 * 
	 * @throws IllegalStateException when the network identified by networkID was not in a valid state (e.g. it was not loaded by {@link #loadNetworkFromFile(File)} or {@link #createNewNetwork(Map, Map, Map, Map, Properties)}),
	 * or a new user (i.e. set of asset cliques and separators) could not be created automatically.
	 * 
	 * @throws IllegalArgumentException when any argument was invalid (e.g. inexistent question or state, or invalid assumptions).
	 */
	public boolean isLongPosition(int networkID, long userID, long questionID, int questionState, List<Long> assumptionIDs, List<Integer> assumedStates, Properties properties) throws IllegalStateException, IllegalArgumentException;
	
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
	 * @param userID : the ID of the current user. Users shall be managed by a hash table. If a new user is identified, the new user shall be automatically
	 * created.
	 * 
	 * @param networkID : id of the network loaded by {@link #loadNetworkFromFile(File)} or {@link #createNewNetwork(Map, Map, Map, Map, Properties)}, which contains the question (random variable) "T" and
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
	 * @return an array with size 2, representing respectively the upper and lower bounds for the edit.
	 * 
	 * @throws IllegalStateException when the network identified by networkID was not in a valid state (e.g. it was not loaded by {@link #loadNetworkFromFile(File)} or {@link #createNewNetwork(Map, Map, Map, Map, Properties)}),
	 * or a new user (i.e. set of asset cliques and separators) could not be created automatically.
	 * 
	 * @throws IllegalArgumentException when any argument was invalid (e.g. inexistent question or state, or invalid assumptions).
	 */
	public float[] getEditLimits(int networkID, long userID, long questionID, int questionState, List<Long> assumptionIDs, List<Integer> assumedStates, Properties properties) throws IllegalStateException, IllegalArgumentException;
	
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
	 * @param userID : the ID of the current user. Users shall be managed by a hash table. If a new user is identified, the new user shall be automatically
	 * created.
	 * 
	 * @param networkID : id of the network loaded by {@link #loadNetworkFromFile(File)} or {@link #createNewNetwork(Map, Map, Map, Map, Properties)}, which contains the question (random variable) "T" and
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
	 * @param properties : this object stores system properties of the markov engine.
	 * Classes implementing this interface may use these properties to set
	 * values of some attributes/parameters not explicitly declared in this interface.
	 * 
	 * @throws IllegalStateException when the network identified by networkID was not in a valid state (e.g. it was not loaded by {@link #loadNetworkFromFile(File)} or {@link #createNewNetwork(Map, Map, Map, Map, Properties)})
	 * or a new user (i.e. set of asset cliques and separators) could not be created automatically.
	 * 
	 * @throws IllegalArgumentException when any argument was invalid (e.g. inexistent question or state, or invalid assumptions).
	 */
	public void addTrade(int networkID, long userID, long questionID, List<Float> oldValues, List<Float> newValues, List<Integer> assumptionIDs, List<Integer> assumedStates, Properties properties) throws IllegalStateException, IllegalArgumentException;
	
	/**
	 * This method implements the feature for estimating the effects of a trade (i.e. what are
	 * the estimated score and cash after an edit).
	 *   
	 * The basic functionality of this method is basically identical to the one in {@link #addTrade(int, long, long, List, List, List, List, Properties)}.
	 * However, The bayesian network and the user's asset tables shall remain unchanged.
	 * 
	 * @param userID : the ID of the current user. Users shall be managed by a hash table. If a new user is identified, the new user shall be automatically
	 * created.
	 * 
	 * @param networkID : id of the network loaded by {@link #loadNetworkFromFile(File)} or {@link #createNewNetwork(Map, Map, Map, Map, Properties)}, which contains the question (random variable) "T" and
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
	 * @param properties : this object stores system properties of the markov engine.
	 * Classes implementing this interface may use these properties to set
	 * values of some attributes/parameters not explicitly declared in this interface.
	 * 
	 * @throws IllegalStateException when the network identified by networkID was not in a valid state (e.g. it was not loaded by {@link #loadNetworkFromFile(File)} or {@link #createNewNetwork(Map, Map, Map, Map, Properties)})
	 * or a new user (i.e. set of asset cliques and separators) could not be created automatically.
	 * 
	 * @throws IllegalArgumentException when any argument was invalid (e.g. inexistent question or state, or invalid assumptions).
	 */
	public Map<String, Double> previewTrade(int networkID, long userID, long questionID, List<Float> oldValues, List<Float> newValues, List<Integer> assumptionIDs, List<Integer> assumedStates, Properties properties) throws IllegalStateException, IllegalArgumentException;
	
	// Resolve Question
	
	/**
	 * This method sets a question as "resolved" (it virtually removes a question from the bayesian network, after performing some auxiliary operations).
	 * @param networkID : id of the network loaded by {@link #loadNetworkFromFile(File)} or {@link #createNewNetwork(Map, Map, Map, Map, Properties)}.
	 * @param questionID : the id of the question to be resolved.
	 * @param choice : the index of the value (state) of the question identified by the questionID (i.e. the resolved question).
	 * @param properties : this object stores system properties of the markov engine.
	 * Classes implementing this interface may use these properties to set
	 * values of some attributes/parameters not explicitly declared in this interface.
	 * @throws IllegalStateException when the network identified by networkID was not in a valid state (e.g. it was not loaded by {@link #loadNetworkFromFile(File)} or {@link #createNewNetwork(Map, Map, Map, Map, Properties)}).
	 * @throws IllegalArgumentException when any argument was invalid (e.g. inexistent question or state).
	 */
	public void resolveQuestion(int networkID, long questionID, int choice, Properties properties) throws IllegalStateException, IllegalArgumentException;
	
	// Question Management
	
	/**
	 * This method adds a new question (i.e. a new node in a bayesian network).
	 * Implementations may require reinitialization of the bayesian network in order to guarantee efficiency,
	 * because uncontrolled inclusion of nodes in implementations based on junction trees may cause 
	 * the tree to become either too huge, sparse, or generate huge cliques.
	 * @param networkID : id of the network loaded by {@link #loadNetworkFromFile(File)} or {@link #createNewNetwork(Map, Map, Map, Map, Properties)} where the new question should be added. 
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
	 * @param properties : this object stores system properties of the markov engine.
	 * Classes implementing this interface may use these properties to set
	 * values of some attributes/parameters not explicitly declared in this interface.
	 * @return the ID of the new question.
	 * @throws IllegalStateException when the network identified by networkID was not in a valid state (e.g. it was not loaded by {@link #loadNetworkFromFile(File)} or {@link #createNewNetwork(Map, Map, Map, Map, Properties)}).
	 * @throws IllegalArgumentException when any argument was invalid (e.g. name or description was invalid).
	 */
	public long addQuestion(int networkID, String name, String description, List<String> states, List<Long> assumptiveQuestionIDs, List<Float> cpd, Properties properties) throws IllegalStateException, IllegalArgumentException;
	
	/**
	 * This method adds a new direct dependency (i.e. an edge between nodes in a bayesian network).
	 * Note that this method may require
	 * recompilation of the bayesian network (i.e. may need to generate a new junction tree).
	 * @param networkID : id of the network loaded by {@link #loadNetworkFromFile(File)} or {@link #createNewNetwork(Map, Map, Map, Map, Properties)}, where the new edge will be added.
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
	 * @param properties : this object stores system properties of the markov engine.
	 * Classes implementing this interface may use these properties to set
	 * values of some attributes/parameters not explicitly declared in this interface.
	 * @throws IllegalStateException when the network identified by networkID was not in a valid state (e.g. it was not loaded by {@link #loadNetworkFromFile(File)}  or {@link #createNewNetwork(Map, Map, Map, Map, Properties)})
	 * @throws IllegalArgumentException when any argument was invalid (e.g. ids were invalid).
	 */
	public void addAssumptiveQuestionLink(int networkID, long questionID, List<Long> assumptiveQuestionIDs, List<Float> cpd, Properties properties) throws IllegalStateException, IllegalArgumentException;
	
	/**
	 * Obtains the ids of the questions that are potential assumptions of a given question. 
	 * This method is necessary when the algorithm implemented by a class implementing this interface requires some restrictions 
	 * on the assumptions (e.g. the assumptions should be in a same clique).
	 * @param networkID : id of the network loaded by {@link #loadNetworkFromFile(File)} or {@link #createNewNetwork(Map, Map, Map, Map, Properties)}.
	 * @param questionID : the id of the random variable (question) to be analyzed.
	 * @param properties : this object stores system properties of the markov engine.
	 * Classes implementing this interface may use these properties to set
	 * values of some attributes/parameters not explicitly declared in this interface.
	 * @return a list (ordered collection) of ids of the random variables (i.e. questions) that can be potential assumptions of a trade.
	 * @throws IllegalStateException when the network identified by networkID was not in a valid state (e.g. it was not loaded by {@link #loadNetworkFromFile(File)} or {@link #createNewNetwork(Map, Map, Map, Map, Properties)}).
	 * @throws IllegalArgumentException when any argument was invalid (e.g. ids were invalid).
	 * @see #addTrade(int, long, long, List, List, List, List, Properties)
	 */
	public List<Long> getPossibleQuestionAssumptions(int networkID, long questionID, Properties properties) throws IllegalStateException, IllegalArgumentException;
	
	/**
	 * @param networkID : id of the network loaded by {@link #loadNetworkFromFile(File)} or {@link #createNewNetwork(Map, Map, Map, Map, Properties)}.
	 * @param questionID : the id of the random variable (question) to be analyzed.
	 * @param properties : this object stores system properties of the markov engine.
	 * Classes implementing this interface may use these properties to set
	 * values of some attributes/parameters not explicitly declared in this interface.
	 * @return true if the question is a multiple-choice question. False if it is boolean.
	 */
	public boolean isMulti(int networkID, long questionID, Properties properties);
	
	/**
	 * @param networkID : id of the network loaded by {@link #loadNetworkFromFile(File)} or {@link #createNewNetwork(Map, Map, Map, Map, Properties)}.
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
	public float getCash(long userID, Properties properties) throws IllegalArgumentException;
	
	/**
	 * Modifies the contents of all cells in the asset table in order to guarantee
	 * that the min asset returned by {@link #getCash(long, Properties)} is set
	 * to the value specified by the argument "value".
	 * <br/> <br/>
	 * How to use:
	 * <br/><br/>
	 * Example1 : the current min asset of a user (i.e. cash returned by {@link #getCash(long, Properties)}) is 80, and we want to update it to 100.
	 * Then, a call to {@link #setCash(long, Properties, float)} with value = 100 will multiply all cells of the asset table by 100/80, guaranteeing
	 * that the new cash is 100, and the expected asssets are updated proportionally.
	 * <br/> <br/>
	 * Example 2 : suppose we want to adjust the assets of an existing user as if the base (i.e {@link #getDefaultInitialQTableValue()})
	 * was 125 instead of 100 (i.e. a user was created {@link #getDefaultInitialQTableValue()} == 100, but due to changes in policy, new users
	 * are going to start from 125, and the assets of old users must be updated as if it has started from 125 instead of 100). In such case,
	 * we can use the following formula in order to estimate the value to be passed to {@link #setCash(long, float, Properties)}: 7
	 * <br/> <br/>
	 * oldCash + delta * oldCash/oldBase = newCash
	 * <br/> <br/>
	 * "oldCash" is the value returned by {@link #getCash(long, Properties)};
	 * "delta" is the difference between the new base and old base (in our example, it is 125-100 = 25);
	 * "oldBase" is the old value of {@link #getDefaultInitialQTableValue()} (it is 100 in our case)
	 * "newCash" is the value to be passed to {@link #setCash(long, float, Properties)}.
	 * <br/>
	 * 
	 * <br/> <br/>
	 * @param userID : the ID of the owner of the asset table.
	 * @param value : the minimum asset value to set
	 * @param properties : this object stores system properties of the markov engine.
	 * Classes implementing this interface may use these properties to set
	 * values of some attributes/parameters not explicitly declared in this interface.
	 * @return the minimum asset value obtained by executing a min calibration algorithm in the asset tables.
	 * @throws IllegalArgumentException when any argument was invalid (e.g. ids were invalid).
	 * @see #getCurrentLogBase()
	 * @see #getCurrentCurrencyConstant()
	 */
	public void setCash(long userID, float value, Properties properties) throws IllegalArgumentException;
	
	/**
	 * Obtains the q-table of a user. 
	 * Note: this is one of the methods that uses a complex data type (i.e. a class) instead of primitive types.
	 * 
	 * By calling {@link AssetNetwork#getJunctionTree()}, {@link JunctionTree#getCliques()}, and then
	 * {@link Clique#getProbabilityFunction()} (may need cast to {@link unbbayes.prs.bn.ProbabilisticTable} in order
	 * to actually get the contents of the cells); it is possible to access the contents of the q-table. 
	 * Note: the values in the q-tables can be converted by using the formula  b*log(q), where b is {@link #getCurrentCurrencyConstant()}
	 * and the base of the log is {@link #getCurrentLogBase()}.
	 * 
	 * @param userID : (mandatory) the ID of the owner of the asset table.
	 * @param properties : this object stores system properties of the markov engine.
	 * Classes implementing this interface may use these properties to set
	 * values of some attributes/parameters not explicitly declared in this interface.
	 * @return an object representing the network of assets (actually, network of q-values). The network
	 * is associated with a junction tree (with cliques and separators), and such cliques and separators are associated
	 * with q-tables. The q-tables have logarithmic relationship to assets.
	 * @throws IllegalArgumentException when any argument was invalid (e.g. ids were invalid, sizes of the lists were unsynchronized, etc.).
	 */
	public AssetNetwork assetsCommittedByUser(long userID, Properties properties) throws IllegalArgumentException;
	
	/**
	 * Obtains the object representing the network identified by networkID.
	 * Note: this is one of the methods that uses a complex data type (i.e. a class) instead of primitive types. 
	 * @param networkID :  id of the network loaded by {@link #loadNetworkFromFile(File)} or {@link #createNewNetwork(Map, Map, Map, Map, Properties)}.
	 * @param properties : this object stores system properties of the markov engine.
	 * Classes implementing this interface may use these properties to set
	 * values of some attributes/parameters not explicitly declared in this interface.
	 * @return the actual object represented by networkID
	 * @throws IllegalArgumentException when any argument was invalid (e.g. invalid network ID).
	 */
	public ProbabilisticNetwork getBayesianNetwork(long networkID, Properties properties) throws IllegalArgumentException;
	
	/**
	 * Obtains the q-table of a user. This method encapsulates {@link #assetsCommittedByUser(long, Properties)}
	 * and offers a way to access assets without using the {@link AssetNetwork} class.
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
	 * @param networkID : id of the network loaded by {@link #loadNetworkFromFile(File)} or {@link #createNewNetwork(Map, Map, Map, Map, Properties)}.
	 * @param userID : the ID of the owner of the asset table.
	 * @param assumptionIDs : (optional) list (ordered collection) of question IDs assumed when obtaining the estimated assets. If specified,
	 * the questions (i.e. random variables) with these IDs will be assumed to be in the states specified in the argument "assumedStates".
	 * @param assumedStates : (mandatory if assumptionIDs is specified - must have the same size of assumptionIDs) indexes
	 * of states (i.e. choices - if boolean, then it is either 0 or 1) of assumptionIDs to be assumed.
	 * @param properties : this object stores system properties of the markov engine.
	 * Classes implementing this interface may use these properties to set
	 * values of some attributes/parameters not explicitly declared in this interface.
	 * @return the expected assets.
	 * @throws IllegalStateException when the network identified by networkID was not in a valid state (e.g. it was not loaded by {@link #loadNetworkFromFile(File)} or {@link #createNewNetwork(Map, Map, Map, Map, Properties)}),
	 * or a new user (i.e. set of asset cliques and separators) could not be created automatically.
	 * @throws IllegalArgumentException when any argument was invalid (e.g. inexistent question or state, or invalid assumptions).
	 */
	public float scoreUserEV(long networkID, long userID, List<Long> assumptionIDs, List<Integer> assumedStates, Properties properties) throws IllegalStateException, IllegalArgumentException;
	
    //Get Probabilities
	
	/**
	 * @return
	 * Obtains the probability of a question (i.e. random variable) given assumptions.
	 * The order is important for identifying the states (i.e. 1st value is for the 1st state, and so on).
	 * If the full info about the bayesian network is needed, use {@link #getBayesianNetwork(long, Properties)} instead.
	 * @param networkID : id of the network loaded by {@link #loadNetworkFromFile(File)} or {@link #createNewNetwork(Map, Map, Map, Map, Properties)}.
	 * @param questionID : id of the question to obtain probability.
	 * @param assumptionIDs : (optional) list (ordered collection) of question IDs assumed when obtaining the estimated assets. If specified,
	 * the questions (i.e. random variables) with these IDs will be assumed to be in the states specified in the argument "assumedStates".
	 * @param assumedStates : (mandatory if assumptionIDs is specified - must have the same size of assumptionIDs) indexes
	 * of states (i.e. choices - if boolean, then it is either 0 or 1) of assumptionIDs to be assumed.
	 * @param properties : this object stores system properties of the markov engine.
	 * Classes implementing this interface may use these properties to set
	 * values of some attributes/parameters not explicitly declared in this interface.
	 * @throws IllegalStateException when the network identified by networkID was not in a valid state (e.g. it was not loaded by {@link #loadNetworkFromFile(File)} or {@link #createNewNetwork(Map, Map, Map, Map, Properties)}),
	 * or a new user (i.e. set of asset cliques and separators) could not be created automatically.
	 * @throws IllegalArgumentException when any argument was invalid (e.g. inexistent question or state, or invalid assumptions).
	 */
	public List<Float> getProbList(long networkID, long questionID, List<Long> assumptionIDs, List<Integer> assumedStates, Properties properties) throws IllegalStateException, IllegalArgumentException;
    
	
	// TODO what is a trade balance?

	/**
	 * @return 
	 * Computed Balance amount to remove involvement in a trade
	 * @param networkID : id of the network loaded by {@link #loadNetworkFromFile(File)} or {@link #createNewNetwork(Map, Map, Map, Map, Properties)}.
	 * @param userID
	 * @param questionID : id of the question to obtain probability.
	 * @param assumptionIDs : (optional) list (ordered collection) of question IDs assumed when obtaining the estimated assets. If specified,
	 * the questions (i.e. random variables) with these IDs will be assumed to be in the states specified in the argument "assumedStates".
	 * @param assumedStates : (mandatory if assumptionIDs is specified - must have the same size of assumptionIDs) indexes
	 * of states (i.e. choices - if boolean, then it is either 0 or 1) of assumptionIDs to be assumed.
	 * @param properties : this object stores system properties of the markov engine.
	 * Classes implementing this interface may use these properties to set
	 * values of some attributes/parameters not explicitly declared in this interface.
	 * @throws IllegalStateException when the network identified by networkID was not in a valid state (e.g. it was not loaded by {@link #loadNetworkFromFile(File)} or {@link #createNewNetwork(Map, Map, Map, Map, Properties)}),
	 * or a new user (i.e. set of asset cliques and separators) could not be created automatically.
	 * @throws IllegalArgumentException when any argument was invalid (e.g. inexistent question or state, or invalid assumptions).
	 */
	public float computeTradeBalance(long networkID, long userID, long questionID, List<Long> assumptionIDs, List<Integer> assumedStates, Properties properties) throws IllegalStateException, IllegalArgumentException; 
	
	
}
