/**
 * 
 */
package edu.gmu.ace.daggre;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;
import unbbayes.io.NetIO;
import unbbayes.prs.Graph;
import unbbayes.prs.Node;
import unbbayes.prs.bn.IRandomVariable;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.inference.extension.AssetAwareInferenceAlgorithm;
import edu.gmu.ace.daggre.MarkovEngineImpl.ProbabilityAndAssetTablesMemento;
import edu.gmu.ace.daggre.ScoreSummary.SummaryContribution;

/**
 * @author Shou Matsumoto
 *
 */
public class MarkovEngineBruteForceTest extends TestCase {

	/** Error margin used when comparing 2 probability values */
	public static final float PROB_ERROR_MARGIN = 0.005f;

	/** Error margin used when comparing 2 probability values. {@link CPTBruteForceMarkovEngine} have less precision. */
	public static final float PROB_ERROR_MARGIN_CPT_BRUTE_FORCE = 0.05f;
	
	/** Error margin used when comparing 2 asset (score) values */
	public static final float ASSET_ERROR_MARGIN = 1f;

	/** Error margin used when comparing 2 asset (score) values. {@link CPTBruteForceMarkovEngine} have less precision.*/
	public static final float ASSET_ERROR_MARGIN_CPT_BRUTE_FORC = 5f;
	
	private List<MarkovEngineImpl> engines;

	/** This value indicates how many test iterations (5-point tests) will be performed by default*/
	private int howManyTradesToTest = (int) (Math.random() * 100);//100;


	private enum FivePointTestType {BELOW_LIMIT, ON_LOWER_LIMIT, BETWEEN_LIMITS, ON_UPPER_LIMIT, ABOVE_LIMIT}; 
	
	/** File names to be used in {@link #testFiles()}  */
	private String[] fileNames = {"disconnected.net", "fullyConnected.net", "fullyDisconnected.net"};

	

	/**
	 * @param name
	 */
	public MarkovEngineBruteForceTest(String name) {
		super(name);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		engines = new ArrayList<MarkovEngineImpl>();
		engines.add((MarkovEngineImpl) MarkovEngineImpl.getInstance(2f, 100f, 1000f));
//		engines.add((MarkovEngineImpl) MarkovEngineImpl.getInstance(2f, 100f, 1000f, false, true));
		engines.add(BruteForceMarkovEngine.getInstance(2f, 100f, 1000f));
//		engines.add(CPTBruteForceMarkovEngine.getInstance(2f, 100f, 1000f));
		for (MarkovEngineInterface engine : engines) {
			engine.initialize();
		}
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	/** Obtains a list of states of the assumptions randomly */
	private List<Integer> getRandomAssumptionStates(List<Long> assumptionIds) {
		List<Integer> ret = new ArrayList<Integer>(assumptionIds.size());
		for (Long questionId : assumptionIds) {
			ret.add((int)(Math.random() * engines.get(0).getProbabilisticNetwork().getNode(questionId.toString()).getStatesSize()));
		}
		return ret;
	}

	/** Obtains a list of assumptions randomly */
	private List<Long> getRandomAssumptions(Long questionId) {
		List<List<Long>> questionAssumptionGroups = engines.get(0).getQuestionAssumptionGroups();
		for (int i = 0; i < engines.size(); i++) {
			assertEquals(engines.get(i).toString(), questionAssumptionGroups, engines.get(i).getQuestionAssumptionGroups());
		}
		List<List<Long>> groupsNotContainingQuestion = new ArrayList<List<Long>>();
		for (List<Long> group : questionAssumptionGroups) {
			if (!group.contains(questionId)) {
				groupsNotContainingQuestion.add(group);
			}
		}
		questionAssumptionGroups.removeAll(groupsNotContainingQuestion);
		List<Long> randomGroup = questionAssumptionGroups.get((int) (Math.random() * questionAssumptionGroups.size()));
		assertNotNull(randomGroup);
		assertFalse(randomGroup.isEmpty());
		randomGroup.remove(questionId);
		int numToRemove = 0;
		if (randomGroup.size() > 2) {
			//If the clique size is bigger than 3 (including mainNode), the size of assumption set has to be at least 2.
			// how many nodes to remove
			numToRemove = (int)(Math.random() * (randomGroup.size() - 2));
		} else  {	// randomGroup.size() == 0, 1 or 2
			// how many nodes to remove
			numToRemove = (int)(Math.random() * (randomGroup.size()));
		}
		for (int i = 0; i < numToRemove; i++) {
			randomGroup.remove((int)(Math.random() * randomGroup.size()));
		}
		return randomGroup;
	}
	
	/** Obtains a list of questions randomly. How many random given states depends on network size. We choose floor(0.3*numberOfVariablesInTheNet). */
	private List<Long> getRandomQuestionsForConditionalAssets(Long questionToIgnore, List<Long> availableQuestions) {
		
		// quantity of questions to pick from questionsToNumberOfStatesMap
		int quantityOfQuestions = (int) (0.3*availableQuestions.size()); // floor(0.3*numberOfVariablesInTheNet)
		List<Long> ret = new ArrayList<Long>(quantityOfQuestions);	// value to be returned
		
		// do not pick questionToIgnore
		availableQuestions.remove(questionToIgnore);
		
		// pick question from availableQuestions until it fills quantityOfQuestions
		do {
			Long id = availableQuestions.get((int) (Math.random()*availableQuestions.size()));
			ret.add(id);
			availableQuestions.remove(id);
		} while (ret.size() < quantityOfQuestions);
		
		return ret;
	}
	
	/** generate an edit which conforms with one of the 5-point test type */
	private List<Float> generateEdit(Long questionId, int totalNumStates, int stateToConsider, 
			List<Float> editLimits, List<Float> priorProb, FivePointTestType type) {
		
		float probOfStateToConsider = Float.NaN;
		
		switch (type) {
		case BELOW_LIMIT:
			probOfStateToConsider = editLimits.get(0) * 0.8f; 
			break;
		case ON_LOWER_LIMIT:
			probOfStateToConsider = editLimits.get(0);
			break;
		case BETWEEN_LIMITS:
			do {
				float delta = (float) ((editLimits.get(1) - editLimits.get(0))*Math.random());
				probOfStateToConsider = editLimits.get(0) + delta;
			} while (probOfStateToConsider <= editLimits.get(0) || probOfStateToConsider >= editLimits.get(1));
			break;
		case ON_UPPER_LIMIT:
			probOfStateToConsider = editLimits.get(1);
			break;
		case ABOVE_LIMIT:
			probOfStateToConsider = editLimits.get(1) + ((1 - editLimits.get(1)) * 0.2f);
			break;
		}
		
		/*
		 * The probability of other states are:
		 * p*(1-P0)/(1-p0)
		 * Where p is the prior probability of the state,
		 * P0 is probOfStateToConsider (posterior prob), and
		 * p0 is the prior probability of stateToConsider 
		 */
		List<Float> ret = new ArrayList<Float>();
		for (int i = 0; i < totalNumStates; i++) {
			if (i == stateToConsider) {
				ret.add(probOfStateToConsider);
			} else {
				// p*(1-P0)/(1-p0)
				ret.add(priorProb.get(i) * (1 - probOfStateToConsider) / (1 - priorProb.get(stateToConsider)));
			}
		}
		
		return ret;
	}
	
	/** Execute the actual test given the point of test in the 5-point test */
	private void do5PointTest(Map<Long,Integer> questionsToNumberOfStatesMap, Long questionId, int stateOfEditLimit, 
			List<Float> editLimits, Long userId, List<Long> assumptionsOfTrade, 
			List<Integer> statesOfAssumption, FivePointTestType pointWithin5PointTest) {
		
		// value to be used in trade
		List<Float> newValues = this.generateEdit(
				questionId, 
				questionsToNumberOfStatesMap.get(questionId), 
				stateOfEditLimit, 
				editLimits, 
				engines.get(0).getProbList(questionId, assumptionsOfTrade, statesOfAssumption), 
				pointWithin5PointTest
			);
		
		// we need the current state in order to revert trade, or in order to check if assets of other users were modified
		Map<MarkovEngineImpl, ProbabilityAndAssetTablesMemento> mementos = new HashMap<MarkovEngineImpl, MarkovEngineImpl.ProbabilityAndAssetTablesMemento>();
		
		
		// set up conditions for conditional cash and score
		List<Long> assumptionIds = this.getRandomQuestionsForConditionalAssets(questionId, new ArrayList<Long>(questionsToNumberOfStatesMap.keySet()));
		List<Integer> assumedStates = this.getRandomAssumptionStates(assumptionIds);
		
		// compare marginals before the trade
		Map<Long, List<Float>> probBeforeTrade = engines.get(0).getProbLists(null, null, null);
		// assert that minimum before trade are the same
		float minBeforeTrade = engines.get(0).getCash(userId, null, null);
		float conditionalMinBeforeTrade = engines.get(0).getCash(userId, assumptionIds, assumedStates);
			
		// do trade in all engines
		for (int i = 0; i < engines.size(); i++) {
			MarkovEngineImpl engine = engines.get(i);
			
			mementos.put(engine, engine.getMemento());
			
			// compare marginals before the trade
			Map<Long, List<Float>> priorProbToCompare = engine.getProbLists(null, null, null);
			assertNotNull(engine.toString(), probBeforeTrade); assertNotNull(engine.toString(), priorProbToCompare);
			assertEquals(engine.toString(), probBeforeTrade.size(), priorProbToCompare.size());
			// compare marginals
			for (Long id : probBeforeTrade.keySet()) {
				List<Float> marginal = probBeforeTrade.get(id);
				List<Float> marginalToCompare = priorProbToCompare.get(id);
				assertEquals(engine.toString(), marginal.size(), marginalToCompare.size());
				for (int state = 0; state < marginal.size(); state++) {
					assertEquals(engine.toString() + ", question = " + id + ", state = " + state,  
							marginal.get(state), 
							marginalToCompare.get(state), 
							((engine instanceof CPTBruteForceMarkovEngine)?PROB_ERROR_MARGIN_CPT_BRUTE_FORCE:PROB_ERROR_MARGIN)
					);
				}
			}
			
			// assert that minimum before trade are the same
			if (Math.abs(minBeforeTrade - engine.getCash(userId, null, null)) >
					((engine instanceof CPTBruteForceMarkovEngine)?ASSET_ERROR_MARGIN_CPT_BRUTE_FORC:ASSET_ERROR_MARGIN)) {
				engines.get(0).getCash(userId, null, null);
				engine.getCash(userId, null, null);
			}
			assertEquals(engine.toString(), minBeforeTrade, engine.getCash(userId, null, null), 
					((engine instanceof CPTBruteForceMarkovEngine)?ASSET_ERROR_MARGIN_CPT_BRUTE_FORC:ASSET_ERROR_MARGIN));
			if (Math.abs(conditionalMinBeforeTrade - engine.getCash(userId, assumptionIds, assumedStates))
					> ((engine instanceof CPTBruteForceMarkovEngine)?ASSET_ERROR_MARGIN_CPT_BRUTE_FORC:ASSET_ERROR_MARGIN)) {
				engines.get(0).getCash(userId, assumptionIds, assumedStates);
				engine.getCash(userId, assumptionIds, assumedStates);
			}
			assertEquals(
					engine.toString() + userId + " , assumption=" + assumptionIds+ "=" + assumedStates, 
					conditionalMinBeforeTrade, 
					engine.getCash(userId, assumptionIds, assumedStates), 
					((engine instanceof CPTBruteForceMarkovEngine)?ASSET_ERROR_MARGIN_CPT_BRUTE_FORC:ASSET_ERROR_MARGIN));
			
			long transactionKey = engine.startNetworkActions();
			assertFalse(
					engine.toString() , 
					engine.addTrade(
							transactionKey, 
							new Date(), 
							"User " + userId + " trades on P(" + questionId + " | " + assumptionsOfTrade + " = " + statesOfAssumption + ") = " + newValues, 
							userId, 
							questionId, 
							newValues, 
							assumptionsOfTrade, 
							statesOfAssumption, 
							true		// allow negative, so that we can see that min went to negative
					).isEmpty()
			);
			engine.commitNetworkActions(transactionKey);
			
			ScoreSummary scoreSummaryOrig= engines.get(0).getScoreSummaryObject(userId, questionId, assumptionIds, assumedStates);
			if (i == 0) {
				continue;
			}
			
			float minimum = engines.get(0).getCash(userId, null, null);
			switch (pointWithin5PointTest) {
			case BELOW_LIMIT:
				assertTrue(engine.toString()+ ", min = " + minimum, minimum < 0);
				break;
			case ON_LOWER_LIMIT:
				assertEquals(engine.toString(), 0f, minimum, 
						((engine instanceof CPTBruteForceMarkovEngine)?ASSET_ERROR_MARGIN_CPT_BRUTE_FORC:ASSET_ERROR_MARGIN));
				break;
			case BETWEEN_LIMITS:
				assertTrue(engine.toString()+ ", min = " + minimum, minimum > 0);
				break;
			case ON_UPPER_LIMIT:
				if (Math.abs(minimum) > ((engine instanceof CPTBruteForceMarkovEngine)?ASSET_ERROR_MARGIN_CPT_BRUTE_FORC:ASSET_ERROR_MARGIN)) {
					engines.get(0).getCash(userId, null, null);
				}
				assertEquals(engine.toString(), 0f, minimum, 
						((engine instanceof CPTBruteForceMarkovEngine)?ASSET_ERROR_MARGIN_CPT_BRUTE_FORC:ASSET_ERROR_MARGIN));
				break;
			case ABOVE_LIMIT:
				assertTrue(engine.toString()+ ", min = " + minimum, minimum < 0);
				break;
			}
			
			// b.) marginal probability on individual variable.
			Map<Long, List<Float>> probabilities = engines.get(0).getProbLists(null, null, null);
			Map<Long, List<Float>> probToCompare = engine.getProbLists(null, null, null);
			assertNotNull(engine.toString(), probToCompare);
			assertEquals(engine.toString(), probabilities.size(), probToCompare.size());
			
			// compare marginals
			for (Long id : probabilities.keySet()) {
				List<Float> marginal = probabilities.get(id);
				List<Float> marginalToCompare = probToCompare.get(id);
				assertEquals(engine.toString(), marginal.size(), marginalToCompare.size());
				for (int state = 0; state < marginal.size(); state++) {
					assertEquals(engine.toString() + ", question = " + id + ", state = " + state,  
							marginal.get(state), 
							marginalToCompare.get(state), 
							((engine instanceof CPTBruteForceMarkovEngine)?PROB_ERROR_MARGIN_CPT_BRUTE_FORCE:PROB_ERROR_MARGIN)
					);
				}
			}
			// c.) min-q values after a user confirms a trade.
			if (Math.abs(minimum - engine.getCash(userId, null, null)) >
					((engine instanceof CPTBruteForceMarkovEngine)?ASSET_ERROR_MARGIN_CPT_BRUTE_FORC:ASSET_ERROR_MARGIN)) {
				engines.get(0).getCash(userId, null, null);
				engine.getCash(userId, null, null);
			}
			assertEquals(engine.toString(), minimum, engine.getCash(userId, null, null), 
					((engine instanceof CPTBruteForceMarkovEngine)?ASSET_ERROR_MARGIN_CPT_BRUTE_FORC:ASSET_ERROR_MARGIN));
			// e.) The expected score.
			assertEquals(
					engine.toString(), 
					engines.get(0).scoreUserEv(userId, null, null), 
					engine.scoreUserEv(userId, null, null), 
					((engine instanceof CPTBruteForceMarkovEngine)?ASSET_ERROR_MARGIN_CPT_BRUTE_FORC:ASSET_ERROR_MARGIN)
				);
			// f. ) conditional min-q and expected score on randomly given states. How many random given states depends on network size. We choose floor(0.3*numberOfVariablesInTheNet).
			if (Math.abs(engines.get(0).getCash(userId, assumptionIds, assumedStates) - engine.getCash(userId, assumptionIds, assumedStates))
					> ((engine instanceof CPTBruteForceMarkovEngine)?ASSET_ERROR_MARGIN_CPT_BRUTE_FORC:ASSET_ERROR_MARGIN)) {
				engines.get(0).getCash(userId, assumptionIds, assumedStates);
				engine.getCash(userId, assumptionIds, assumedStates);
			}
			assertEquals(
					engine.toString() + userId + " , assumption=" + assumptionIds+ "=" + assumedStates, 
					engines.get(0).getCash(userId, assumptionIds, assumedStates), 
					engine.getCash(userId, assumptionIds, assumedStates), 
					((engine instanceof CPTBruteForceMarkovEngine)?ASSET_ERROR_MARGIN_CPT_BRUTE_FORC:ASSET_ERROR_MARGIN)
				);
			assertEquals(
					engine.toString() + userId + " , " + assumptionIds + assumedStates, 
					engines.get(0).scoreUserEv(userId, assumptionIds, assumedStates), 
					engine.scoreUserEv(userId, assumptionIds, assumedStates), 
					((engine instanceof CPTBruteForceMarkovEngine)?ASSET_ERROR_MARGIN_CPT_BRUTE_FORC:ASSET_ERROR_MARGIN)
				);
			
			// g.) An userÅfs asset table is not changed when other user makes edit.
			ProbabilityAndAssetTablesMemento posteriorMemento = engine.getMemento();	// get current status of the engine
			for (AssetAwareInferenceAlgorithm algorithm : mementos.get(engine).getAssetTableMap().keySet()) {
				if (algorithm.getAssetNetwork().getName().equals(Long.toString(userId))) {
					continue;	// do not compare assets of user who made the trade
				}
				// extract the asset tables
				Map<IRandomVariable, PotentialTable> previousAssets  = mementos.get(engine).getAssetTableMap().get(algorithm);
				Map<IRandomVariable, PotentialTable> posteriorAssets = posteriorMemento.getAssetTableMap().get(algorithm);
				// compare assets
				assertEquals(engine.toString(), previousAssets.size(), posteriorAssets.size());
				for (IRandomVariable key : previousAssets.keySet()) {
					PotentialTable previousTable = previousAssets.get(key);
					PotentialTable posteriorTable = posteriorAssets.get(key);
					assertEquals(engine.toString(), previousTable.tableSize(), posteriorTable.tableSize());
					for (int tableIndex = 0; tableIndex < previousTable.tableSize(); tableIndex++) {
						assertEquals(engine.toString(), previousTable.getValue(tableIndex), posteriorTable.getValue(tableIndex));
					}
				}
			}
			
			ScoreSummary scoreSummaryObject = engine.getScoreSummaryObject(userId, null, assumptionIds, assumedStates);
			assertNotNull(scoreSummaryObject);
			assertEquals(
					engine.toString() + userId + " , assumption=" + assumptionIds+ "=" + assumedStates, 
					engines.get(0).getCash(userId, assumptionIds, assumedStates), 
					scoreSummaryObject.getCash(), 
					((engine instanceof CPTBruteForceMarkovEngine)?ASSET_ERROR_MARGIN_CPT_BRUTE_FORC:ASSET_ERROR_MARGIN)
				);
			assertEquals(
					engine.toString() + userId + " , assumption=" + assumptionIds+ "=" + assumedStates, 
					scoreSummaryOrig.getCash(), 
					scoreSummaryObject.getCash(), 
					((engine instanceof CPTBruteForceMarkovEngine)?ASSET_ERROR_MARGIN_CPT_BRUTE_FORC:ASSET_ERROR_MARGIN)
			);
			assertEquals(
					engine.toString() + userId + " , " + assumptionIds + assumedStates, 
					engines.get(0).scoreUserEv(userId, assumptionIds, assumedStates), 
					scoreSummaryObject.getScoreEV(), 
					((engine instanceof CPTBruteForceMarkovEngine)?ASSET_ERROR_MARGIN_CPT_BRUTE_FORC:ASSET_ERROR_MARGIN)
				);
			assertEquals(
					engine.toString() + userId + " , " + assumptionIds + assumedStates, 
					scoreSummaryOrig.getScoreEV(), 
					scoreSummaryObject.getScoreEV(), 
					((engine instanceof CPTBruteForceMarkovEngine)?ASSET_ERROR_MARGIN_CPT_BRUTE_FORC:ASSET_ERROR_MARGIN)
			);
			float sumOfScoreComponents = 0f;
			for (SummaryContribution contribution : scoreSummaryObject.getScoreComponents()) {
				sumOfScoreComponents += contribution.getContributionToScoreEV();
			}
			for (SummaryContribution contribution : scoreSummaryObject.getIntersectionScoreComponents()) {
				sumOfScoreComponents -= contribution.getContributionToScoreEV();
			}
			assertFalse(engine.toString() + userId + " , " + assumptionIds + assumedStates, Float.isNaN(sumOfScoreComponents));
			assertEquals(
					engine.toString() + userId + " , " + assumptionIds + assumedStates, 
					scoreSummaryObject.getScoreEV(), 
					sumOfScoreComponents, 
					((engine instanceof CPTBruteForceMarkovEngine)?ASSET_ERROR_MARGIN_CPT_BRUTE_FORC:ASSET_ERROR_MARGIN)
				);
		}
		// revert trades if this was not supposedly a valid trade
		if (pointWithin5PointTest != FivePointTestType.BETWEEN_LIMITS) {
			for (MarkovEngineImpl engine : mementos.keySet()) {
				engine.restoreMemento(mementos.get(engine));	// revert last trade
			}
		}
	}
	
	

	/** This is a common portion for each test case, regardless of the network. It assumes the {@link #engines} was filled with nodes and edges already */
	private void runTestAssumingInitializedNetwork(Map<Long,Integer> questionsToNumberOfStatesMap) {
		// Randomly create 3 user IDs
		List<Long> userIDs = new ArrayList<Long>(3);
		do {
			userIDs.clear();
			Set<Long> userFilter = new HashSet<Long>(3);
			userFilter.add((long) (Math.random()*0x0F));
			userFilter.add((long) (Math.random()*0x0F));
			userFilter.add((long) (Math.random()*0x0F));
			userIDs.addAll(userFilter);
		} while (userIDs.size() != 3);
		
		// make sure initial cash and expected score are 1000
		for (MarkovEngineInterface engine : engines) {
			for (Long userId : userIDs) {
				assertEquals(engine.toString(), 1000f, engine.getCash(userId, null, null), ASSET_ERROR_MARGIN);
				assertEquals(engine.toString(), 1000f, engine.scoreUserEv(userId, null, null), ASSET_ERROR_MARGIN);
			}
		}
		
		// Make sure getProbLists can list up all available questions, and initial marginal probabilities are the same for all engines
		Map<Long, List<Float>> probabilities = engines.get(0).getProbLists(null, null, null);
		assertEquals(questionsToNumberOfStatesMap.size(), probabilities.keySet().size());
		assertTrue(probabilities + " != " + questionsToNumberOfStatesMap, probabilities.keySet().containsAll(questionsToNumberOfStatesMap.keySet()));
		for (int i = 1; i < engines.size(); i++) {
			// make sure all engines are retrieving the same questions
			Map<Long, List<Float>> probabilitiesOfOtherEngines = engines.get(i).getProbLists(null, null, null);
			assertEquals(engines.get(i).toString(), probabilities.size(), probabilitiesOfOtherEngines.size());
			assertEquals(engines.get(i).toString(), questionsToNumberOfStatesMap.size(), probabilitiesOfOtherEngines.size());
			// if size is same and contains all, then the lists are equal (regardless of size).
			assertTrue(engines.get(i).toString() + ", " + questionsToNumberOfStatesMap, probabilitiesOfOtherEngines.keySet().containsAll(questionsToNumberOfStatesMap.keySet()));
			// also, make sure that probabilities are initialized equally
			for (Long id : probabilities.keySet()) {
				List<Float> probOfQuestion = probabilities.get(id);
				List<Float> probOfQuestionOfOtherEngines = probabilitiesOfOtherEngines.get(id);
				assertEquals(engines.get(i).toString() + ", ID = " + id, probabilities.size(), probabilitiesOfOtherEngines.size());
				for (int j = 0; j < probOfQuestion.size(); j++) {
					assertEquals(
							engines.get(i).toString() + ", ID = " + id, 
							probOfQuestion.get(j), 
							probOfQuestionOfOtherEngines.get(j), 
							PROB_ERROR_MARGIN
					);
				}
			}
		}
		
		
		// actually run the tests
		for (int iteration = 0; iteration < getHowManyTradesToTest(); iteration++) {
			// (1) Randomly choose one user; 
			long userId = userIDs.get((int) (Math.random() * userIDs.size()));
			
			// (2) Randomly choose one node, 
			List<Long> listOfQuestions = new ArrayList<Long>(questionsToNumberOfStatesMap.keySet());
			Long questionId = listOfQuestions.get((int) (Math.random() * listOfQuestions.size()));
			// state to consider when obtaining the limits of edit
			int stateOfEditLimit = this.getRandomAssumptionStates(Collections.singletonList(questionId)).get(0);
			
			// randomly choose assumption set in the same clique of the node. 
			List<Long> assumptionIds = this.getRandomAssumptions(questionId);
			List<Integer> assumedStates = this.getRandomAssumptionStates(assumptionIds);
			
			// obtain the bounds for the 5-point test
			List<Float> editLimits = engines.get(0).getEditLimits(userId, questionId, stateOfEditLimit, assumptionIds, assumedStates);
			for (int i = 1; i < engines.size(); i++) {
				List<Float> editLimitsOfOtherEngine = engines.get(i).getEditLimits(userId, questionId, stateOfEditLimit, assumptionIds, assumedStates);
				assertEquals(engines.get(i).toString(), editLimits.size(), editLimitsOfOtherEngine.size());
				for (int j = 0; j < editLimits.size(); j++) {
//					if (Math.abs(editLimits.get(j) - editLimitsOfOtherEngine.get(j)) >
//					((engines.get(i) instanceof CPTBruteForceMarkovEngine)?PROB_ERROR_MARGIN_CPT_BRUTE_FORCE:PROB_ERROR_MARGIN)) {
//						engines.get(0).getEditLimits(userId, questionId, stateOfEditLimit, assumptionIds, assumedStates);
//						engines.get(i).getEditLimits(userId, questionId, stateOfEditLimit, assumptionIds, assumedStates);
//					}
					assertEquals(engines.get(i).toString() + ", user=" + userId + ", question=" + questionId 
							+", state=" + stateOfEditLimit + "," +  assumptionIds + "=" + assumedStates, 
							editLimits.get(j), editLimitsOfOtherEngine.get(j), 
							((engines.get(i) instanceof CPTBruteForceMarkovEngine)?PROB_ERROR_MARGIN_CPT_BRUTE_FORCE:PROB_ERROR_MARGIN));
				}
			}
			
			
			System.out.println("Iteration " + iteration + ", question=" + questionId + ", state=" + stateOfEditLimit + 
					", limit="+ editLimits + ", user="  +userId + ", assumptions: " + assumptionIds + "=" + assumedStates);
			
			// a.) 5-point min-q values test regarding the edit bound; expect to see corresponding q<1, =1, >1 respectively.
			
			// (1) the probability close to but smaller than the lower bound;
			this.do5PointTest(questionsToNumberOfStatesMap, questionId, stateOfEditLimit, editLimits, userId, assumptionIds, assumedStates, FivePointTestType.BELOW_LIMIT);
			
			// (2) the probability exactly on the lower bound;
			this.do5PointTest(questionsToNumberOfStatesMap, questionId, stateOfEditLimit, editLimits, userId, assumptionIds, assumedStates, FivePointTestType.ON_LOWER_LIMIT);
			
			// (4) the probability exactly on the upper bound;
			this.do5PointTest(questionsToNumberOfStatesMap, questionId, stateOfEditLimit, editLimits, userId, assumptionIds, assumedStates, FivePointTestType.ON_UPPER_LIMIT);
			
			// (5) the probability close to but bigger than the upper bound;
			this.do5PointTest(questionsToNumberOfStatesMap, questionId, stateOfEditLimit, editLimits, userId, assumptionIds, assumedStates, FivePointTestType.ABOVE_LIMIT);
			
			// (3) random probability in between the bound;
			this.do5PointTest(questionsToNumberOfStatesMap, questionId, stateOfEditLimit, editLimits, userId, assumptionIds, assumedStates, FivePointTestType.BETWEEN_LIMITS);
		}	// end of for : iteration
	}


	

	/**
	 * Test method for 
	 * {@link edu.gmu.ace.daggre.MarkovEngineImpl#addTrade(long, java.util.Date, long, long, long, java.util.List, java.util.List, java.util.List, java.util.List, java.lang.Boolean)},
	 * {@link MarkovEngineImpl#getCash(long, List, List)}, 
	 * {@link MarkovEngineImpl#getEditLimits(long, long, int, List, List)},
	 * {@link MarkovEngineImpl#getProbList(long, List, List)},
	 * {@link MarkovEngineImpl#getAssetsIfStates(long, long, List, List)}.
	 * The tested data is equivalent to the one in 
	 * https://docs.google.com/document/d/179XTjD5Edj8xDBvfrAP7aDtvgDJlXbSDQQR-TlM5stw/edit.
	 * <br/>
	 * The DEF net looks like the following.		<br/>
	 * Note: uniform distribution for all nodes.	<br/>
	 * D--->F										<br/>
	 * | 											<br/>
	 * V  											<br/>
	 * E											<br/>
	 * 
	 * The sequence is:<br/>
	 * <br/>
	 * There are two cliques {D, E}, and {D, F}, initial asset tables have q-value as 100 in every cell. <br/>
	 *	Current marginal probabilities are:	<br/>
	 *	Variables    D             E              F	<br/> 
	 *	Marginals   [0.5 0.5]   [0.5 0.5]   [0.5 0.5]	<br/>
	 *	<br/>	
	 *	Trade-1: Tom would like to make a bet on E=e1, that has current probability as 0.5. First of all, we need to calculate TomÅfs edit limit (in this case, there is no assumption):	<br/> 
	 *	Given E=e1, min-q1 = 100	<br/>
	 *	Given E~=e1, min-q2 = 100	<br/>
	 *	From Equation (1), edit interval is [0.005, 0.995].	<br/> 
	 *	- Verification successful: after substituting the limits into edit, we do get min-q to be 1.	<br/> 
	 *	<br/>	
	 *	Trading sequence:	<br/> 
	 *	Tom1:	P(E=e1) = 0.5  to 0.55 (current)	<br/>
	 *	<br/>	
	 *	Variables   D              E                   F	<br/> 
	 *	Marginals   [0.5 0.5]   [0.55 0.45]   [0.5 0.5]	<br/>
	 *	<br/>	
	 *	TomÅfs min-q is 90, at the following 4 min-states (found by min-asset-propagation):	<br/> 
	 *	     D    E    F	<br/>
	 *	     1     2     1	<br/>
	 *	     1     2     2	<br/>
	 *	     2     2     1	<br/>
	 *	     2     2     2	<br/>
	 *	<br/>	
	 *	<br/>	
	 *	Trade-2: Now Tom would like to make another conditional bet on E=e1 given D=d1 (current P(E=e1|D=d1) = 0.55). Again, let us calculate his edit limits first (in this case, we have assumed variable D=d1). And note that TomÅfs asset tables are not the initial ones any more, but updated from last trade he did, now:	<br/> 
	 *	Given E=e1, and D=d1, min-q1 = 110	<br/>
	 *	Given E~=e1, and D=d1, min-q2 = 90	<br/>
	 *	From Equation (1), edit interval is [0.005, 0.995]	<br/>
	 *	- Verification successful: after substituting the limits into edit, we do get min-q to be 1.	<br/> 
	 *	<br/>	
	 *	Trading sequence:	<br/> 
	 *	Tom1:	P(E=e1) = 0.5  to 0.55	<br/>
	 *	Tom2:	P(E=e1|D=d1) = 0.55 to 0.9 (current)	<br/>
	 *	Variables   D              E                   F	<br/> 
	 *	Marginals   [0.5 0.5]   [0.725 0.275]   [0.5 0.5]	<br/>
	 *	<br/>	
	 *	TomÅfs min-q is 20, at the following two min-states (found by min-asset-propagation):	<br/> 
	 *	     D    E    F	<br/>
	 *	     1     2    1	<br/>
	 *	     1     2    2	<br/>
	 *	<br/>	
	 *	<br/>	
	 *	Trade-3: Joe came and intended to make a bet on E=e1 given D=d2 (current P(E=e1|D=d2) is 0.55). This will be his first edit, so he has initial asset tables before his trade.	<br/> 
	 *	Edit limit:	<br/>
	 *	Given E=e1, and D=d2, min-q1 = 100	<br/>
	 *	Given E~=e1, and D=d2, min-q2 = 100	<br/>
	 *	From Equation (1), edit interval is [0.0055, 0.9955]	<br/>
	 *	- Verification successful: after substituting the limits into edit, we do get min-q to be 1.	<br/> 
	 *	<br/>	
	 *	Trading sequence:	<br/> 
	 *	Tom1:	P(E=e1) = 0.5  to 0.55	<br/>
	 *	Tom2:	P(E=e1|D=d1) = 0.55 to 0.9	<br/> 
	 *	Joe1:	P(E=e1|D=d2) = 0.55 to 0.4 (current)	<br/> 
	 *	<br/>	  
	 *	Variables   D              E                   F	<br/> 
	 *	Marginals   [0.5 0.5]   [0.65 0.35]   [0.5 0.5]	<br/>
	 *	<br/>	
	 *	JoeÅfs min-q is 72.72727272727..., at the following two min-states (found by min-asset-propagation):	<br/> 
	 *	     D    E    F	<br/>
	 *	     2     1    1	<br/>
	 *	     2     1    2	<br/>
	 *	<br/>	
	 *	<br/>	
	 *	Trade-4: Now Amy is interested in changing P(F=f1|D=d1), which is currently 0.5. It will be her first edit, so she also has initial asset tables before the trade.	<br/> 
	 *	Edit limit:	<br/>
	 *	Given F=f1, and D=d1, min-q1 = 100	<br/>
	 *	Given F~=f1, and D=d1, min-q2 = 100	<br/>
 	 *	From Equation (1), edit interval is [0.005, 0.995]	<br/>
	 *	- Verification successful: after substituting the limits into edit, we do get min-q to be 1.	<br/> 
	 *	<br/>	
	 *	Trading sequence:	<br/> 
	 *	Tom1:	P(E=e1) = 0.5  to 0.55	<br/>
	 *	Tom2:	P(E=e1|D=d1) = 0.55 to 0.9	<br/> 
	 *	Joe1:	P(E=e1|D=d2) = 0.55 to 0.4	<br/> 
	 *	Amy1:	P(F=f1|D=d1) = 0.5 to 0.3 (current)	<br/>
	 *	<br/>	  
	 *	Variables   D              E                   F	<br/> 
	 *	Marginals   [0.5 0.5]   [0.65 0.35]   [0.4 0.6]	<br/>
	 *	<br/>	
	 *	AmyÅfs min-q is 60, at the following two min-states (found by min-asset-propagation):	<br/> 
	 *	     D    E    F	<br/>
	 *	     1     1    1	<br/>
	 *	     1     2    1	<br/>
	 *	<br/>	
	 *	<br/>	
	 *	Trade-5: Joe would like to trade again on P(F=f1|D=d2), which is currently 0.5.	<br/> 
	 *	Edit limit:	<br/>
	 *	Given F=f1, and D=d2, min-q1 = 72.727272727	<br/>
	 *	Given F~=f1, and D=d2, min-q2 = 72.727272727	<br/>
	 *	From Equation (1), edit interval is [0.006875, 0.993125]	<br/>
	 *	- Verification successful: after substituting the limits into edit, we do get min-q to be 1.	<br/> 
	 *	<br/>	
	 *	Trading sequence:	<br/> 
	 *	Tom1:	P(E=e1) = 0.5  to 0.55	<br/>
	 *	Tom2:	P(E=e1|D=d1) = 0.55 to 0.9	<br/> 
	 *	Joe1:	P(E=e1|D=d2) = 0.55 to 0.4	<br/> 
	 *	Amy1:	P(F=f1|D=d1) = 0.5 to 0.3	<br/> 
	 *	Joe2:	P(F=f1|D=d2) = 0.5 to 0.1	<br/>
	 *	<br/>	  
	 *	Variables   D              E                   F	<br/> 
	 *	Marginals   [0.5 0.5]   [0.65 0.35]   [0.2 0.8]	<br/>
	 *	<br/>	
	 *	JoeÅfs min-q is 14.54545454546, at the following unique min-states (found by min-asset-propagation):	<br/> 
	 *	     D    E    F	<br/>
	 *	     2     1    1	<br/>
	 *	<br/>	
	 *	At this point, the model DEF reaches the status that has the same CPTs as the starting CPTs for the experimental model DEF we used in our AAAI 2012 paper.	<br/> 
	 *	<br/>	
	 *	From now on, we run test cases described in the paper.	<br/> 
	 *	<br/>	
	 *	Trade-6: Eric would like to trade on P(E=e1), which is currently 0.65.	<br/> 
	 *	To decide long or short, S(E=e1) = 10, S(E~=e1)=10, no difference because this will be EricÅfs first trade.	<br/>
	 *	Edit limit:	<br/>
	 *	Given F=f1, and D=d2, min-q1 = 100	<br/>
	 *	Given F~=f1, and D=d2, min-q2 = 100	<br/>
	 *	From Equation (1), edit interval is [0.0065, 0.9965]	<br/>
	 *	- Verification successful: after substituting the limits into edit, we do get min-q to be 1.	<br/> 
	 *	<br/>	
	 *	Trading sequence:	<br/> 
	 *	Tom1:	P(E=e1) = 0.5  to 0.55	<br/>
	 *	Tom2:	P(E=e1|D=d1) = 0.55 to 0.9	<br/> 
	 *	Joe1:	P(E=e1|D=d2) = 0.55 to 0.4	<br/> 
	 *	Amy1:	P(F=f1|D=d1) = 0.5 to 0.3	<br/> 
	 *	Joe2:	P(F=f1|D=d2) = 0.5 to 0.1	<br/>
	 *	Eric1:	P(E=e1) = 0.65 to 0.8 (current)	<br/>
	 *	<br/>	  
	 *	Variables   D              E                   F	<br/> 
	 *	Marginals   [0.5824, 0.4176]   [0.8, 0.2]   [0.2165, 0.7835]	<br/>
	 *	<br/>	
	 *	EricÅfs expected score is S=10.1177.	<br/>
	 *	EricÅfs min-q is 57.142857, at the following two min-states (found by min-asset-propagation):	<br/> 
	 *	     D    E    F	<br/>
	 *	     2     2    1	<br/>
	 *	     2     2    2	<br/>
	 *	<br/>	
	 *	<br/>	
	 *	Trade-7: Eric would like to make another edit. This time, he is interested on changing P(D=d1|F=f2), which is currently 0.52.	<br/> 
	 *	To decide long or short, S(D=d1, F=f2) = 10.36915, S(D~=d1, F=f2)=9.7669.	<br/>
	 *	Edit limit:	<br/>
	 *	Given D=d1, and F=f2 , min-q1 = 57.142857	<br/>
	 *	Given D~=d1, and F=f2, min-q2 = 57.142857	<br/>
	 *	From Equation (1), edit interval is [0.0091059, 0.9916058]	<br/>
	 *	- Verification successful: after substituting the limits into edit, we do get min-q to be 1.	<br/> 
	 *	<br/>	
	 *	Trading sequence:	<br/> 
	 *	Tom1:	P(E=e1) = 0.5  to 0.55	<br/>
	 *	Tom2:	P(E=e1|D=d1) = 0.55 to 0.9	<br/> 
	 *	Joe1:	P(E=e1|D=d2) = 0.55 to 0.4	<br/> 
	 *	Amy1:	P(F=f1|D=d1) = 0.5 to 0.3	<br/> 
	 *	Joe2:	P(F=f1|D=d2) = 0.5 to 0.1	<br/>
	 *	Eric1:	P(E=e1) = 0.65 to 0.8	<br/> 
	 *	Eric2:	P(D=d1|F=f2) = 0.52 to 0.7 (current)	<br/>
	 *	<br/>	  
	 *	Variables   D                            E                             F	<br/> 
	 *	Marginals   [0.7232, 0.2768]   [0.8509, 0.1491]   [0.2165, 0.7835]	<br/>
	 *	<br/>	
	 *	EricÅfs expected score is now 10.31615.	<br/> 
	 *	EricÅfs min-q is 35.7393, at the following unique min-states (found by min-asset-propagation):	<br/> 
	 *	     D    E    F	<br/>
	 *	     2     2    2	<br/>
	 *	<br/> 
	 *  The transaction is committed at each trade (trades are not committed at once).
	 */	
	public final void testBasicDEF() {
		
		// crate transaction for generating the DEF network
		assertNotNull(engines);
		assertFalse(engines.isEmpty());
		
		for (MarkovEngineImpl engine : engines) {
			engine.setCurrentCurrencyConstant((float)(10.0/Math.log(100)));
			engine.setCurrentLogBase((float)Math.E);
			if (engine.isToUseQValues()) {
				engine.setDefaultInitialAssetTableValue(1f);
			} else {
				engine.setDefaultInitialAssetTableValue(0f);
			}
		}
		
		long transactionKey = engines.get(0).startNetworkActions();
		for (int i = 1; i < engines.size() ; i++) {
			assertEquals(engines.get(i).toString() ,transactionKey, engines.get(i).startNetworkActions());
		}
		for (MarkovEngineInterface engine : engines) {
			// create nodes D, E, F
			engine.addQuestion(transactionKey, new Date(), 0x0D, 2, null);	// question D has ID = hexadecimal D. CPD == null -> linear distro
			engine.addQuestion(transactionKey, new Date(), 0x0E, 2, null);	// question E has ID = hexadecimal E. CPD == null -> linear distro
			engine.addQuestion(transactionKey, new Date(), 0x0F, 2, null);	// question F has ID = hexadecimal F. CPD == null -> linear distro
			// create edge D->E 
			engine.addQuestionAssumption(transactionKey, new Date(), 0x0E, Collections.singletonList((long) 0x0D), null);	// cpd == null -> linear distro
			// create edge D->F
			engine.addQuestionAssumption(transactionKey, new Date(), 0x0F, Collections.singletonList((long) 0x0D), null);	// cpd == null -> linear distro
			// commit changes
			engine.commitNetworkActions(transactionKey);
		}
		
		// cannot reuse same transaction key
		for (MarkovEngineInterface engine : engines) {
			try {
				List<Float> newValues = new ArrayList<Float>(2);
				newValues.add(.9f);	newValues.add(.1f);
				engine.addTrade(transactionKey, new Date(), "To fail", Long.MAX_VALUE, (long)0x0D, newValues, null, null, false);
				fail("Should not allow to use commited transaction " + engine.toString());
			} catch (IllegalArgumentException e) {
				assertNotNull(e);
			}
		}
		
		// check joint probability
		try {
			List<Long> questionIds = new ArrayList<Long>();
			questionIds.add(0x0DL);questionIds.add(0x0EL);questionIds.add(0x0FL);
			List<Integer> states = new ArrayList<Integer>();
			states.add(0); states.add(0); states.add(0); 
			for (int i = 0; i < 8; i++) {
				states.set(0, i%2);	states.set(0, (i/2)%2); states.set(2, (i/4)%2);
				float jointProbability = engines.get(0).getJointProbability(questionIds, states);
				for (int j = 1; j < engines.size(); j++) {
					assertEquals(engines.get(j)+ ", state = "+ i, jointProbability, engines.get(j).getJointProbability(questionIds, states), PROB_ERROR_MARGIN);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		// Let's use ID = 0 for the user Tom 
		Map<String, Long> userNameToIDMap = new HashMap<String, Long>();
		userNameToIDMap.put("Tom", (long)0);
		
		for (MarkovEngineImpl engine : engines) {
			// By default, cash is initialized as 0 (i.e. min-q = 1)
			assertEquals(engine.toString(), 0, engine.getCash(userNameToIDMap.get("Tom"), null, null), ASSET_ERROR_MARGIN);
			assertEquals(engine.toString(), 1, engine.getQValuesFromScore(engine.getCash(userNameToIDMap.get("Tom"), null, null)), ASSET_ERROR_MARGIN);
		}
		
		for (MarkovEngineImpl engine : engines) {
			// add 100 q-values to new users
			transactionKey = engine.startNetworkActions();
			assertTrue(engine.toString(), engine.addCash(transactionKey, new Date(), userNameToIDMap.get("Tom"), engine.getScoreFromQValues(100f), "Initialize User's asset to 100"));
			engine.commitNetworkActions(transactionKey);
			// check that user's min-q value was changed to the correct value
			assertEquals(engine.toString(), 100, engine.getQValuesFromScore(engine.getCash(userNameToIDMap.get("Tom"), null, null)), ASSET_ERROR_MARGIN);
			assertEquals(engine.toString(), (engine.getScoreFromQValues(100)), (engine.getCash(userNameToIDMap.get("Tom"), null, null)), ASSET_ERROR_MARGIN);
		}
		
		// Tom bets P(E=e1) = 0.5  to 0.55 (unconditional soft evidence in E)
		
		// check whether probability prior to edit is really 0.5
		List<Float> probList;
		for (MarkovEngineImpl engine : engines) {
			probList = engine.getProbList(0x0E, null, null);
			assertEquals(engine.toString(), 2 , probList.size());
			assertEquals(engine.toString(), 0.5f , probList.get(0) , PROB_ERROR_MARGIN);
			assertEquals(engine.toString(), 0.5f , probList.get(1) , PROB_ERROR_MARGIN);
		}
		
		// edit interval of P(E=e1) should be [0.005, 0.995]
		List<Float> editInterval = null;
		for (MarkovEngineImpl engine : engines) {
			editInterval = engine.getEditLimits(userNameToIDMap.get("Tom"), 0x0E, 0, null, null);
			assertNotNull(engine.toString(), editInterval);
			assertEquals(engine.toString(), 2, editInterval.size());
			assertEquals(engine.toString(), 0.005f, editInterval.get(0), PROB_ERROR_MARGIN );
			assertEquals(engine.toString(), 0.995f, editInterval.get(1), PROB_ERROR_MARGIN );
		}
		
		
		// do edit
		List<Float> newValues = new ArrayList<Float>(2);
		newValues.add(0.55f);		// P(E=e1) = 0.55
		newValues.add(0.45f);		// P(E=e2) = 1 - P(E=e1) = 0.45
		
		transactionKey = engines.get(0).startNetworkActions();
		for (int i = 1; i < engines.size(); i++) {
			assertEquals(engines.get(i).toString(), transactionKey,engines.get(i).startNetworkActions());
		}
		for (MarkovEngineImpl engine : engines) {
			assertEquals(engine.toString(), 2, engine.addTrade(
					transactionKey, 
					new Date(), 
					"Tom bets P(E=e1) = 0.5  to 0.55", 
					userNameToIDMap.get("Tom"), 
					0x0E, 	// question E
					newValues,
					null, 	// no assumptions
					null, 	// no states of the assumptions
					false	// do not allow negative
			).size());
			engine.commitNetworkActions(transactionKey);
		}
		
		// cannot reuse same transaction key
		for (MarkovEngineImpl engine : engines) {
			try {
				newValues = new ArrayList<Float>(2);
				newValues.add(.9f);	newValues.add(.1f);
				engine.addTrade(transactionKey, new Date(), "To fail", Long.MAX_VALUE, (long)0x0D, newValues, null, null, false);
				fail("Should not allow to use commited transaction" + engine.toString());
			} catch (IllegalArgumentException e) {
				assertNotNull(e);
			}
		}
		
		// check that new marginal of E is [0.55 0.45], and the others have not changed (remains 50%)
		for (MarkovEngineImpl engine : engines) {
			probList = engine.getProbList(0x0D, null, null);
			assertEquals(engine.toString(), 0.5f, probList.get(0), PROB_ERROR_MARGIN);
			assertEquals(engine.toString(), 0.5f, probList.get(1), PROB_ERROR_MARGIN);
			probList = engine.getProbList(0x0E, null, null);
			assertEquals(engine.toString(), 0.55f, probList.get(0), PROB_ERROR_MARGIN);
			assertEquals(engine.toString(), 0.45f, probList.get(1), PROB_ERROR_MARGIN);
			probList = engine.getProbList(0x0F, null, null);
			assertEquals(engine.toString(), 0.5f, probList.get(0), PROB_ERROR_MARGIN);
			assertEquals(engine.toString(), 0.5f, probList.get(1), PROB_ERROR_MARGIN);
		}
		
		// check that Tom's min-q is 90 (and the cash is supposedly the log value of 90)
		float minCash = Float.NaN;	 
		for (MarkovEngineImpl engine : engines) {
			minCash = engine.getCash(userNameToIDMap.get("Tom"), null, null); // null means unconditional cash, which is supposedly the global minimum
			assertEquals(engine.toString(), (engine.getScoreFromQValues(90f)), (minCash), ASSET_ERROR_MARGIN);
			assertEquals(engine.toString(), 90f, (engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		}
		
		// check that LPE contains e2 and any values for D and F, by asserting that cash conditioned to such states are equals to the min
		// d, e, f are always going to be the assumption nodes in this test
		List<Long> assumptionIds = new ArrayList<Long>();
		assumptionIds.add((long) 0x0D);		// 1st node is D; assumedStates must follow this order
		assumptionIds.add((long) 0x0E);		// 2nd node is E; assumedStates must follow this order
		assumptionIds.add((long) 0x0F);		// 3rd node is F; assumedStates must follow this order
		
		// check combination d1, e1, f1 (not min)
		List<Integer> assumedStates = new ArrayList<Integer>();
		assumedStates.add(0);	// d1
		assumedStates.add(0);	// e1
		assumedStates.add(0);	// f1
		float cash;
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
			assertTrue(engine.toString()+" Obtained cash = " + cash, minCash < cash);
		}
		
		// check combination d1, e1, f2 (not min)
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 1);	// f2
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
			assertTrue(engine.toString()+" Obtained cash = " + cash, minCash < cash);
		}
		
		// check combination d1, e2, f1
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 0);	// f1
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
			assertEquals(minCash, cash, ASSET_ERROR_MARGIN);
		}

		// check combination d1, e2, f2
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 1);	// f2
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
			assertEquals(minCash, cash, ASSET_ERROR_MARGIN);
		}

		// check combination d2, e1, f1 (not min)
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 0);	// f1
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
			assertTrue("Obtained cash = " + cash, minCash < cash);
		}
		
		// check combination d2, e1, f2 (not min)
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 1);	// f2
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
			assertTrue("Obtained cash = " + cash, minCash < cash);
		}

		// check combination d2, e2, f1
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 0);	// f1
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
			assertEquals(minCash, cash, ASSET_ERROR_MARGIN);
		}

		// check combination d2, e2, f2
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 1);	// f2
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
			assertEquals(minCash, cash, ASSET_ERROR_MARGIN);
		}
		
		// check minimal condition of LPE: e2
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Tom"), Collections.singletonList((long)0x0E), Collections.singletonList(1));
			assertEquals(minCash, cash, ASSET_ERROR_MARGIN);
			cash = engine.getCash(userNameToIDMap.get("Tom"), Collections.singletonList((long)0x0E), Collections.singletonList(0));
			assertTrue("Obtained cash = " + cash, minCash < cash);
		}
		
		// Tom bets P(E=e1|D=d1) = .55 -> .9
		
		// check whether probability prior to edit is really [e1d1, e2d1, e1d2, e2d2] = [.55, .45, .55, .45]
		assumptionIds = new ArrayList<Long>();		
		assumedStates = new ArrayList<Integer>();
		assumptionIds.add((long) 0x0D);	// assumption = D
		for (MarkovEngineImpl engine : engines) {
			probList = engine.getProbList(0x0E, assumptionIds, Collections.singletonList(0));
			assertEquals(2, probList.size());
			assertEquals(0.55f , probList.get(0),PROB_ERROR_MARGIN );	// P(E=e1|D=d1)
			assertEquals(0.45f , probList.get(1),PROB_ERROR_MARGIN );	// P(E=e2|D=d1)
			probList = engine.getProbList(0x0E, assumptionIds, Collections.singletonList(1));
			assertEquals(2, probList.size());
			assertEquals(0.55f , probList.get(0),PROB_ERROR_MARGIN );	// P(E=e1|D=d2)
			assertEquals(0.45f , probList.get(1),PROB_ERROR_MARGIN );	// P(E=e2|D=d2)
		}
		
		assumedStates.add(0);	// set d1 as assumed state
		
		// edit interval of P(E=e1|D=d1) should be [0.005, 0.995]
		for (MarkovEngineImpl engine : engines) {
			editInterval = engine.getEditLimits(userNameToIDMap.get("Tom"), 0x0E, 0, assumptionIds, assumedStates);	// (node == 0x0E && state == 0) == e1
			assertNotNull(editInterval);
			assertEquals(2, editInterval.size());
			assertEquals(0.005f, editInterval.get(0) ,PROB_ERROR_MARGIN);
			assertEquals(0.995f, editInterval.get(1) ,PROB_ERROR_MARGIN);
		}
		

		
		// set P(E=e1|D=d1) = 0.9 and P(E=e2|D=d1) = 0.1
		transactionKey = engines.get(0).startNetworkActions();
		for (int i = 1; i < engines.size(); i++) {
			assertEquals(transactionKey,engines.get(i).startNetworkActions());
		}
		newValues = new ArrayList<Float>();
		newValues.add(.9f);
		newValues.add(.1f);
		for (MarkovEngineImpl engine : engines) {
			assertEquals(2, engine.addTrade(
					transactionKey, 
					new Date(), 
					"Tom bets P(E=e1|D=d1) = 0.9", 
					userNameToIDMap.get("Tom"), 
					0x0E, 
					newValues, 
					assumptionIds, 
					assumedStates, 
					false
			).size());
			engine.commitNetworkActions(transactionKey);
		}
		
		// cannot reuse same transaction key
		for (MarkovEngineImpl engine : engines) {
			try {
				newValues = new ArrayList<Float>(2);
				newValues.add(.9f);	newValues.add(.1f);
				engine.addTrade(transactionKey, new Date(), "To fail", Long.MAX_VALUE, (long)0x0D, newValues, null, null, false);
				fail("Should not allow to use commited transaction");
			} catch (IllegalArgumentException e) {
				assertNotNull(e);
			}
		}
		
		
		// check that new marginal of E is [0.725 0.275] (this is expected value), and the others have not changed (remains 50%)
		for (MarkovEngineImpl engine : engines) {
			probList = engine.getProbList(0x0D, null, null);
			assertEquals(0.5f, probList.get(0), PROB_ERROR_MARGIN);
			assertEquals(0.5f, probList.get(1), PROB_ERROR_MARGIN);
			probList = engine.getProbList(0x0E, null, null);
			assertEquals(0.725f, probList.get(0), PROB_ERROR_MARGIN);
			assertEquals(0.275f, probList.get(1), PROB_ERROR_MARGIN);
			probList = engine.getProbList(0x0F, null, null);
			assertEquals(0.5f, probList.get(0), PROB_ERROR_MARGIN);
			assertEquals(0.5f, probList.get(1), PROB_ERROR_MARGIN);
		}
		
		// check that min-q is 20
		for (MarkovEngineImpl engine : engines) {
			minCash = engine.getCash(userNameToIDMap.get("Tom"), null, null);
			assertEquals((engine.getScoreFromQValues(20f)), (minCash), ASSET_ERROR_MARGIN);
			assertEquals(20f, (engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		}
		
		// check that LPE contains d1, e2 and any value F
		assumptionIds = new ArrayList<Long>();
		assumptionIds.add((long) 0x0D);		// 1st node is D; assumedStates must follow this order
		assumptionIds.add((long) 0x0E);		// 2nd node is E; assumedStates must follow this order
		assumptionIds.add((long) 0x0F);		// 3rd node is F; assumedStates must follow this order
		
		// check combination d1, e1, f1 (not min)
		assumedStates = new ArrayList<Integer>();
		assumedStates.add(0);	// d1
		assumedStates.add(0);	// e1
		assumedStates.add(0);	// f1
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
			assertTrue("Obtained cash = " + cash, minCash < cash);
		}
		
		// check combination d1, e1, f2 (not min)
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 1);	// f2
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
			assertTrue("Obtained cash = " + cash, minCash < cash);
		}
		
		// check combination d1, e2, f1
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 0);	// f1
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
			assertEquals(minCash, cash, ASSET_ERROR_MARGIN);
		}

		// check combination d1, e2, f2
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 1);	// f2
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
			assertEquals(minCash, cash, ASSET_ERROR_MARGIN);
		}

		// check combination d2, e1, f1 (not min)
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 0);	// f1
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
			assertTrue("Obtained cash = " + cash, minCash < cash);
		}
		
		// check combination d2, e1, f2 (not min)
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 1);	// f2
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
			assertTrue("Obtained cash = " + cash, minCash < cash);
		}

		// check combination d2, e2, f1 (not min)
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 0);	// f1
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
			assertTrue("Obtained cash = " + cash, minCash < cash);
		}

		// check combination d2, e2, f2 (not min)
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 1);	// f2
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
			assertTrue("Obtained cash = " + cash, minCash < cash);
		}

		// check minimal condition of LPE: d1, e2
		assumptionIds = new ArrayList<Long>();
		assumptionIds.add((long)0x0D);
		assumptionIds.add((long)0x0E);
		assumedStates = new ArrayList<Integer>();
		assumedStates.add(0);
		assumedStates.add(1);
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
			assertEquals(minCash, cash, ASSET_ERROR_MARGIN);
		}
		// check conditions that do not match LPE
		assumedStates.set(0,1);
		assumedStates.set(1,0);
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
			assertTrue("Obtained cash = " + cash, minCash < cash);
		}
		assumptionIds.clear();
		assumptionIds.add((long)0x0D);
		assumedStates.clear();
		assumedStates.add(1);
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
			assertTrue("Obtained cash = " + cash, minCash < cash);
		}
		assumptionIds.clear();
		assumptionIds.add((long)0x0E);
		assumedStates.clear();
		assumedStates.add(0);
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
			assertTrue("Obtained cash = " + cash, minCash < cash);
		}
		
		
		// Let's create user Joe, ID = 1.
		userNameToIDMap.put("Joe", (long) 1);
		
		// By default, cash is initialized as 0 (i.e. min-q = 1)
		for (MarkovEngineImpl engine : engines) {
			assertEquals(0, (engine.getCash(userNameToIDMap.get("Joe"), null, null)), ASSET_ERROR_MARGIN);
			assertEquals(1, (engine.getQValuesFromScore(engine.getCash(userNameToIDMap.get("Joe"), null, null))), ASSET_ERROR_MARGIN);
		}
		
		// add 100 q-values to new users
		transactionKey = engines.get(0).startNetworkActions();
		for (int i = 1; i < engines.size(); i++) {
			assertEquals(transactionKey,engines.get(i).startNetworkActions());
		}
		for (MarkovEngineImpl engine : engines) {
			assertTrue(engine.addCash(transactionKey, new Date(), userNameToIDMap.get("Joe"), engine.getScoreFromQValues(100f), "Initialize User's asset to 100"));
			engine.commitNetworkActions(transactionKey);
		}
		// check that user's min-q value was changed to the correct value
		for (MarkovEngineImpl engine : engines) {
			assertEquals(100, (engine.getQValuesFromScore(engine.getCash(userNameToIDMap.get("Joe"), null, null))), ASSET_ERROR_MARGIN);
			assertEquals((engine.getScoreFromQValues(100)), (engine.getCash(userNameToIDMap.get("Joe"), null, null)), ASSET_ERROR_MARGIN);
		}

		// Joe bets P(E=e1|D=d2) = .55 -> .4
		
		// check whether probability prior to edit is really [e1d1, e2d1, e1d2, e2d2] = [.9, .1, .55, .45]
		assumptionIds = new ArrayList<Long>();		
		assumedStates = new ArrayList<Integer>();
		assumptionIds.add((long) 0x0D);	// assumption = D
		for (MarkovEngineImpl engine : engines) {
			probList = engine.getProbList(0x0E, assumptionIds, Collections.singletonList(0));
			assertEquals(2, probList.size());
			assertEquals(0.9f , probList.get(0),PROB_ERROR_MARGIN );	// P(E=e1|D=d1)
			assertEquals(0.1f , probList.get(1),PROB_ERROR_MARGIN );	// P(E=e2|D=d1)
			probList = engine.getProbList(0x0E, assumptionIds, Collections.singletonList(1));
			assertEquals(2, probList.size());
			assertEquals(0.55f , probList.get(0),PROB_ERROR_MARGIN );	// P(E=e1|D=d2)
			assertEquals(0.45f , probList.get(1),PROB_ERROR_MARGIN );	// P(E=e2|D=d2)
		}
		
		assumedStates.add(1);	// set d2 as assumed state
		
		// edit interval of P(E=e1|D=d2) should be [0.0055, 0.9955]
		for (MarkovEngineImpl engine : engines) {
			editInterval = engine.getEditLimits(userNameToIDMap.get("Joe"), 0x0E, 0, assumptionIds, assumedStates);
			assertNotNull(editInterval);
			assertEquals(2, editInterval.size());
			assertEquals(0.0055f, editInterval.get(0) ,PROB_ERROR_MARGIN);
			assertEquals(0.9955f, editInterval.get(1) ,PROB_ERROR_MARGIN);
		}
		
		// set P(E=e1|D=d2) = 0.4 and P(E=e2|D=d2) = 0.6
		transactionKey = engines.get(0).startNetworkActions();
		for (int i = 1; i < engines.size(); i++) {
			assertEquals(transactionKey,engines.get(i).startNetworkActions());
		}
		newValues = new ArrayList<Float>();
		newValues.add(.4f);
		newValues.add(.6f);
		for (MarkovEngineImpl engine : engines) {
			assertEquals(2, engine.addTrade(
					transactionKey, 
					new Date(), 
					"Joe bets P(E=e1|D=d2) = 0.4", 
					userNameToIDMap.get("Joe"), 
					0x0E, 
					newValues, 
					assumptionIds, 
					assumedStates, 
					false
			).size());
			engine.commitNetworkActions(transactionKey);
		}
		
		// cannot reuse same transaction key
		for (MarkovEngineImpl engine : engines) {
			try {
				newValues = new ArrayList<Float>(2);
				newValues.add(.9f);	newValues.add(.1f);
				engine.addTrade(transactionKey, new Date(), "To fail", Long.MAX_VALUE, (long)0x0D, newValues, null, null, false);
				fail("Should not allow to use commited transaction " + engine.getClass());
			} catch (IllegalArgumentException e) {
				assertNotNull(e);
			}
		}
		
		for (MarkovEngineImpl engine : engines) {
			// check that new marginal of E is [0.65 0.35] (this is expected value), and the others have not changed (remains 50%)
			probList = engine.getProbList(0x0D, null, null);
			assertEquals(0.5f, probList.get(0), PROB_ERROR_MARGIN);
			assertEquals(0.5f, probList.get(1), PROB_ERROR_MARGIN);
			probList = engine.getProbList(0x0E, null, null);
			assertEquals(0.65f, probList.get(0), PROB_ERROR_MARGIN);
			assertEquals(0.35f, probList.get(1), PROB_ERROR_MARGIN);
			probList = engine.getProbList(0x0F, null, null);
			assertEquals(0.5f, probList.get(0), PROB_ERROR_MARGIN);
			assertEquals(0.5f, probList.get(1), PROB_ERROR_MARGIN);
		}
		
		// check that min-q is 72.727272...
		for (MarkovEngineImpl engine : engines) {
			minCash = engine.getCash(userNameToIDMap.get("Joe"), null, null);
			assertEquals((engine.getScoreFromQValues(72.727272f)), (minCash), ASSET_ERROR_MARGIN);
			assertEquals(72.727272f, (engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		}
		
		// check that LPE contains d2, e1 and any value F
		assumptionIds = new ArrayList<Long>();
		assumptionIds.add((long) 0x0D);		// 1st node is D; assumedStates must follow this order
		assumptionIds.add((long) 0x0E);		// 2nd node is E; assumedStates must follow this order
		assumptionIds.add((long) 0x0F);		// 3rd node is F; assumedStates must follow this order
		
		// check combination d1, e1, f1
		assumedStates = new ArrayList<Integer>();
		assumedStates.add(0);	// d1
		assumedStates.add(0);	// e1
		assumedStates.add(0);	// f1
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
			assertTrue("Obtained cash = " + cash, minCash < cash);
		}
		
		// check combination d1, e1, f2
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 1);	// f2
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
			assertTrue("Obtained cash = " + cash, minCash < cash);
		}
		
		// check combination d1, e2, f1
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 0);	// f1
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
			assertTrue("Obtained cash = " + cash, minCash < cash);
		}

		// check combination d1, e2, f2
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 1);	// f2
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
			assertTrue("Obtained cash = " + cash, minCash < cash);
		}

		// check combination d2, e1, f1 
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 0);	// f1
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
			assertEquals(minCash, cash, ASSET_ERROR_MARGIN);
		}
		
		// check combination d2, e1, f2 
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 1);	// f2
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
			assertEquals(minCash, cash, ASSET_ERROR_MARGIN);
		}

		// check combination d2, e2, f1
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 0);	// f1
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
			assertTrue("Obtained cash = " + cash, minCash < cash);
		}

		// check combination d2, e2, f2
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 1);	// f2
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
			assertTrue("Obtained cash = " + cash, minCash < cash);
		}
		
		// check minimal condition of LPE: d2, e1
		assumptionIds = new ArrayList<Long>();
		assumptionIds.add((long)0x0D);
		assumptionIds.add((long)0x0E);
		assumedStates = new ArrayList<Integer>();
		assumedStates.add(1);
		assumedStates.add(0);
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
			assertEquals(minCash, cash, ASSET_ERROR_MARGIN);
		}
		// check conditions that do not match LPE
		assumedStates.set(0,0);
		assumedStates.set(1,1);
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
			assertTrue("Obtained cash = " + cash, minCash < cash);
		}
		assumptionIds.clear();
		assumptionIds.add((long)0x0D);
		assumedStates.clear();
		assumedStates.add(0);
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
			assertTrue("Obtained cash = " + cash, minCash < cash);
		}
		assumptionIds.clear();
		assumptionIds.add((long)0x0E);
		assumedStates.clear();
		assumedStates.add(1);
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
			assertTrue("Obtained cash = " + cash, minCash < cash);
		}
		
		// Let's create user Amy, ID = 2.
		userNameToIDMap.put("Amy", (long) 2);
		
		// By default, cash is initialized as 0 (i.e. min-q = 1)
		for (MarkovEngineImpl engine : engines) {
			assertEquals(0, (engine.getCash(userNameToIDMap.get("Amy"), null, null)), ASSET_ERROR_MARGIN);
			assertEquals(1, (engine.getQValuesFromScore(engine.getCash(userNameToIDMap.get("Amy"), null, null))), ASSET_ERROR_MARGIN);
		}
		
		// add 100 q-values to new users
		transactionKey = engines.get(0).startNetworkActions();
		for (int i = 1; i < engines.size() ; i++) {
			assertEquals(engines.get(i).toString() ,transactionKey, engines.get(i).startNetworkActions());
		}
		for (MarkovEngineImpl engine : engines) {
			assertTrue(engine.addCash(transactionKey, new Date(), userNameToIDMap.get("Amy"), engine.getScoreFromQValues(100f), "Initialize User's asset to 100"));
			engine.commitNetworkActions(transactionKey);
		}
		// check that user's min-q value was changed to the correct value
		for (MarkovEngineImpl engine : engines) {
			assertEquals(100, (engine.getQValuesFromScore(engine.getCash(userNameToIDMap.get("Amy"), null, null))), ASSET_ERROR_MARGIN);
			assertEquals((engine.getScoreFromQValues(100)), (engine.getCash(userNameToIDMap.get("Amy"), null, null)), ASSET_ERROR_MARGIN);
		}

		// Amy bets P(F=f1|D=d1) = .5 -> .3
		
		// check whether probability prior to edit is really [f1d1, f2d1, f1d2, f2d2] = [.5, .5, .5, .5]
		assumptionIds = new ArrayList<Long>();		
		assumedStates = new ArrayList<Integer>();
		assumptionIds.add((long) 0x0D);	
		for (MarkovEngineImpl engine : engines) {
			probList = engine.getProbList(0x0F, assumptionIds, Collections.singletonList(0));
			assertEquals(2, probList.size());
			assertEquals(0.5f , probList.get(0),PROB_ERROR_MARGIN );
			assertEquals(0.5f , probList.get(1),PROB_ERROR_MARGIN );
			probList = engine.getProbList(0x0F, assumptionIds, Collections.singletonList(0));
			assertEquals(2, probList.size());
			assertEquals(0.5f , probList.get(0),PROB_ERROR_MARGIN );
			assertEquals(0.5f , probList.get(1),PROB_ERROR_MARGIN );
		}
		
		assumedStates.add(0);	// set d1 as assumed state
		
		// edit interval of P(F=f1|D=d1) should be [0.005, 0.995]
		for (MarkovEngineImpl engine : engines) {
			editInterval = engine.getEditLimits(userNameToIDMap.get("Amy"), 0x0F, 0, assumptionIds, assumedStates);
			assertNotNull(editInterval);
			assertEquals(2, editInterval.size());
			assertEquals(0.005f, editInterval.get(0) ,PROB_ERROR_MARGIN);
			assertEquals(0.995f, editInterval.get(1) ,PROB_ERROR_MARGIN);
		}
		
		
		// set P(F=f1|D=d1) = 0.3 and P(F=f2|D=d1) = 0.7  
		transactionKey = engines.get(0).startNetworkActions();
		for (int i = 1; i < engines.size() ; i++) {
			assertEquals(engines.get(i).toString() ,transactionKey, engines.get(i).startNetworkActions());
		}
		newValues = new ArrayList<Float>();
		newValues.add(.3f);
		newValues.add(.7f);
		for (MarkovEngineImpl engine : engines) {
			assertEquals(2, engine.addTrade(
					transactionKey, 
					new Date(), 
					"Amy bets P(F=f1|D=d1) = 0.3", 
					userNameToIDMap.get("Amy"), 
					0x0F, 
					newValues, 
					assumptionIds, 
					assumedStates, 
					false
			).size());
			engine.commitNetworkActions(transactionKey);
		}
		
		// cannot reuse same transaction key
		for (MarkovEngineImpl engine : engines) {
			try {
				newValues = new ArrayList<Float>(2);
				newValues.add(.9f);	newValues.add(.1f);
				engine.addTrade(transactionKey, new Date(), "To fail", Long.MAX_VALUE, (long)0x0D, newValues, null, null, false);
				fail("Should not allow to use commited transaction");
			} catch (IllegalArgumentException e) {
				assertNotNull(e);
			}
		}
		
		
		// check that new marginal of E is still [0.65 0.35] (this is expected value), F is [.4 .6], and the others have not changed (remains 50%)
		for (MarkovEngineImpl engine : engines) {
			probList = engine.getProbList(0x0D, null, null);
			assertEquals(0.5f, probList.get(0), PROB_ERROR_MARGIN);
			assertEquals(0.5f, probList.get(1), PROB_ERROR_MARGIN);
			probList = engine.getProbList(0x0E, null, null);
			assertEquals(0.65f, probList.get(0), PROB_ERROR_MARGIN);
			assertEquals(0.35f, probList.get(1), PROB_ERROR_MARGIN);
			probList = engine.getProbList(0x0F, null, null);
			assertEquals(0.4f, probList.get(0), PROB_ERROR_MARGIN);
			assertEquals(0.6f, probList.get(1), PROB_ERROR_MARGIN);
		}
		
		// check that min-q is 60...
		for (MarkovEngineImpl engine : engines) {
			minCash = engine.getCash(userNameToIDMap.get("Amy"), null, null);
			assertEquals((engine.getScoreFromQValues(60f)), (minCash), ASSET_ERROR_MARGIN);
			assertEquals(60f, (engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		}
		
		// check that LPE contains d1, f1 and any value E
		assumptionIds = new ArrayList<Long>();
		assumptionIds.add((long) 0x0D);		// 1st node is D; assumedStates must follow this order
		assumptionIds.add((long) 0x0E);		// 2nd node is E; assumedStates must follow this order
		assumptionIds.add((long) 0x0F);		// 3rd node is F; assumedStates must follow this order
		
		// check combination d1, e1, f1
		assumedStates = new ArrayList<Integer>();
		assumedStates.add(0);	// d1
		assumedStates.add(0);	// e1
		assumedStates.add(0);	// f1
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
			assertEquals(minCash, cash, ASSET_ERROR_MARGIN);
		}
		
		// check combination d1, e1, f2
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 1);	// f2
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
			assertTrue("Obtained cash = " + cash, minCash < cash);
		}
		
		// check combination d1, e2, f1
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 0);	// f1
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
			assertEquals(minCash, cash, ASSET_ERROR_MARGIN);
		}

		// check combination d1, e2, f2
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 1);	// f2
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
			assertTrue("Obtained cash = " + cash, minCash < cash);
		}

		// check combination d2, e1, f1 
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 0);	// f1
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
			assertTrue("Obtained cash = " + cash, minCash < cash);
		}
		
		// check combination d2, e1, f2 
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 1);	// f2
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
			assertTrue("Obtained cash = " + cash, minCash < cash);
		}

		// check combination d2, e2, f1
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 0);	// f1
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
			assertTrue("Obtained cash = " + cash, minCash < cash);
		}

		// check combination d2, e2, f2
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 1);	// f2
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
			assertTrue("Obtained cash = " + cash, minCash < cash);
		}
		
		// check minimal condition of LPE: d1, f1
		assumptionIds = new ArrayList<Long>();
		assumptionIds.add((long)0x0D);
		assumptionIds.add((long)0x0F);
		assumedStates = new ArrayList<Integer>();
		assumedStates.add(0);
		assumedStates.add(0);
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
			assertEquals(minCash, cash, ASSET_ERROR_MARGIN);
		}
		// check conditions that do not match LPE
		assumedStates.set(0,1);
		assumedStates.set(1,1);
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
			assertTrue("Obtained cash = " + cash, minCash < cash);
		}
		assumedStates.set(0,0);
		assumedStates.set(1,1);
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
			assertTrue("Obtained cash = " + cash, minCash < cash);
		}
		assumedStates.set(0,1);
		assumedStates.set(1,0);
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
			assertTrue("Obtained cash = " + cash, minCash < cash);
		}
		assumptionIds.clear();
		assumptionIds.add((long)0x0D);
		assumedStates.clear();
		assumedStates.add(1);
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
			assertTrue("Obtained cash = " + cash, minCash < cash);
		}
		assumptionIds.clear();
		assumptionIds.add((long)0x0F);
		assumedStates.clear();
		assumedStates.add(1);
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
			assertTrue("Obtained cash = " + cash, minCash < cash);
		}
		
		// Joe bets P(F=f1|D=d2) = .5 -> .1
		
		// check whether probability prior to edit is really [f1d1, f2d1, f1d2, f2d2] = [.3, .7, .5, .5]
		assumptionIds = new ArrayList<Long>();		
		assumedStates = new ArrayList<Integer>();
		assumptionIds.add((long) 0x0D);	
		for (MarkovEngineImpl engine : engines) {
			probList = engine.getProbList(0x0F, assumptionIds, Collections.singletonList(0));
			assertEquals(2, probList.size());
			assertEquals(0.3f , probList.get(0),PROB_ERROR_MARGIN );
			assertEquals(0.7f , probList.get(1),PROB_ERROR_MARGIN );
			probList = engine.getProbList(0x0F, assumptionIds, Collections.singletonList(1));
			assertEquals(2, probList.size());
			assertEquals(0.5f , probList.get(0),PROB_ERROR_MARGIN );
			assertEquals(0.5f , probList.get(1),PROB_ERROR_MARGIN );
		}
		
		assumedStates.add(1);	// set d2 as assumed state
		
		// edit interval of P(F=f1|D=d2) should be [0.006875, 0.993125]
		for (MarkovEngineImpl engine : engines) {
			editInterval = engine.getEditLimits(userNameToIDMap.get("Joe"), 0x0F, 0, assumptionIds, assumedStates);
			assertNotNull(editInterval);
			assertEquals(2, editInterval.size());
			assertEquals(0.006875f, editInterval.get(0) ,PROB_ERROR_MARGIN);
			assertEquals(0.993125, editInterval.get(1) ,PROB_ERROR_MARGIN);
		}
		
		
		// set P(F=f1|D=d2) = 0.1 and P(F=f2|D=d2) = 0.9
		transactionKey = engines.get(0).startNetworkActions();
		for (int i = 1; i < engines.size() ; i++) {
			assertEquals(engines.get(i).toString() ,transactionKey, engines.get(i).startNetworkActions());
		}
		newValues = new ArrayList<Float>();
		newValues.add(.1f);
		newValues.add(.9f);
		for (MarkovEngineImpl engine : engines) {
			assertEquals(2, engine.addTrade(
					transactionKey, 
					new Date(), 
					"Joe bets P(F=f1|D=d2) = 0.1", 
					userNameToIDMap.get("Joe"), 
					0x0F, 
					newValues, 
					assumptionIds, 
					assumedStates, 
					false
			).size());
			engine.commitNetworkActions(transactionKey);
		}
		
		// cannot reuse same transaction key
		for (MarkovEngineImpl engine : engines) {
			try {
				newValues = new ArrayList<Float>(2);
				newValues.add(.9f);	newValues.add(.1f);
				engine.addTrade(transactionKey, new Date(), "To fail", Long.MAX_VALUE, (long)0x0D, newValues, null, null, false);
				fail("Should not allow to use commited transaction");
			} catch (IllegalArgumentException e) {
				assertNotNull(e);
			}
		}
		
		
		// check that new marginal of E is still [0.65 0.35] (this is expected value), F is [.2 .8], and the others have not changed (remains 50%)
		for (MarkovEngineImpl engine : engines) {
			probList = engine.getProbList(0x0D, null, null);
			assertEquals(0.5f, probList.get(0), PROB_ERROR_MARGIN);
			assertEquals(0.5f, probList.get(1), PROB_ERROR_MARGIN);
			probList = engine.getProbList(0x0E, null, null);
			assertEquals(0.65f, probList.get(0), PROB_ERROR_MARGIN);
			assertEquals(0.35f, probList.get(1), PROB_ERROR_MARGIN);
			probList = engine.getProbList(0x0F, null, null);
			assertEquals(0.2f, probList.get(0), PROB_ERROR_MARGIN);
			assertEquals(0.8f, probList.get(1), PROB_ERROR_MARGIN);
		}
		
		// check probabilities given assumptions outside the same clique
		for (MarkovEngineImpl engine : engines) {
			probList = engine.getProbList(0x0E, Collections.singletonList((long)0x0F), Collections.singletonList(0));
			assertEquals(2, probList.size());
			assertEquals(0.775f, probList.get(0), PROB_ERROR_MARGIN);
			assertEquals(0.225f, probList.get(1), PROB_ERROR_MARGIN);
			probList = engine.getProbList(0x0E, Collections.singletonList((long)0x0F), Collections.singletonList(1));
			assertEquals(2, probList.size());
			assertEquals(0.6188f, probList.get(0), PROB_ERROR_MARGIN);
			assertEquals(0.3813f, probList.get(1), PROB_ERROR_MARGIN);
			probList = engine.getProbList(0x0F, Collections.singletonList((long)0x0E), Collections.singletonList(0));
			assertEquals(2, probList.size());
			assertEquals(0.2385f, probList.get(0), PROB_ERROR_MARGIN);
			assertEquals(0.7615f, probList.get(1), PROB_ERROR_MARGIN);
			probList = engine.getProbList(0x0F, Collections.singletonList((long)0x0E), Collections.singletonList(1));
			assertEquals(2, probList.size());
			assertEquals(0.1286f, probList.get(0), PROB_ERROR_MARGIN);
			assertEquals(0.8714f, probList.get(1), PROB_ERROR_MARGIN);
		}
		
		// check that min-q is 14.5454545...
		for (MarkovEngineImpl engine : engines) {
			minCash = engine.getCash(userNameToIDMap.get("Joe"), null, null);
			assertEquals((engine.getScoreFromQValues(14.5454545f)), (minCash), ASSET_ERROR_MARGIN);
			assertEquals(14.5454545f, (engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		}
		
		// check that LPE contains d2, e1, f1
		assumptionIds = new ArrayList<Long>();
		assumptionIds.add((long) 0x0D);		// 1st node is D; assumedStates must follow this order
		assumptionIds.add((long) 0x0E);		// 2nd node is E; assumedStates must follow this order
		assumptionIds.add((long) 0x0F);		// 3rd node is F; assumedStates must follow this order
		
		// check combination d1, e1, f1
		assumedStates = new ArrayList<Integer>();
		assumedStates.add(0);	// d1
		assumedStates.add(0);	// e1
		assumedStates.add(0);	// f1
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
			assertTrue("Obtained cash = " + cash, minCash < cash);
		}
		
		// check combination d1, e1, f2
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 1);	// f2
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
			assertTrue("Obtained cash = " + cash, minCash < cash);
		}
		
		// check combination d1, e2, f1
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 0);	// f1
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
			assertTrue("Obtained cash = " + cash, minCash < cash);
		}

		// check combination d1, e2, f2
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 1);	// f2
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
			assertTrue("Obtained cash = " + cash, minCash < cash);
		}

		// check combination d2, e1, f1 
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 0);	// f1
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
			assertEquals(minCash, cash, ASSET_ERROR_MARGIN);
		}
		
		// check combination d2, e1, f2 
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 1);	// f2
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
			assertTrue("Obtained cash = " + cash, minCash < cash);
		}

		// check combination d2, e2, f1
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 0);	// f1
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
			assertTrue("Obtained cash = " + cash, minCash < cash);
		}

		// check combination d2, e2, f2
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 1);	// f2
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
			assertTrue("Obtained cash = " + cash, minCash < cash);
		}


		// create new user Eric
		userNameToIDMap.put("Eric", (long) 3);
		// By default, cash is initialized as 0 (i.e. min-q = 1)
		for (MarkovEngineImpl engine : engines) {
			assertEquals(0, (engine.getCash(userNameToIDMap.get("Eric"), null, null)), ASSET_ERROR_MARGIN);
			assertEquals(1, (engine.getQValuesFromScore(engine.getCash(userNameToIDMap.get("Eric"), null, null))), ASSET_ERROR_MARGIN);
		}
		
		// add 100 q-values to new users
		transactionKey = engines.get(0).startNetworkActions();
		for (int i = 1; i < engines.size() ; i++) {
			assertEquals(engines.get(i).toString() ,transactionKey, engines.get(i).startNetworkActions());
		}
		for (MarkovEngineImpl engine : engines) {
			assertTrue(engine.addCash(transactionKey, new Date(), userNameToIDMap.get("Eric"), engine.getScoreFromQValues(100f), "Initialize User's asset to 100"));
			engine.commitNetworkActions(transactionKey);
			// check that user's min-q value was changed to the correct value
			assertEquals(100, (engine.getQValuesFromScore(engine.getCash(userNameToIDMap.get("Eric"), null, null))), ASSET_ERROR_MARGIN);
			assertEquals((engine.getScoreFromQValues(100)), (engine.getCash(userNameToIDMap.get("Eric"), null, null)), ASSET_ERROR_MARGIN);
		}

		
		// Eric bets P(E=e1) = .65 -> .8
		
		// check whether probability prior to edit is really = [.65, .35]
		assumptionIds = new ArrayList<Long>();		
		assumedStates = new ArrayList<Integer>();
		for (MarkovEngineImpl engine : engines) {
			probList = engine.getProbList(0x0E, assumptionIds, null);
			assertEquals(2, probList.size());
			assertEquals(0.65f , probList.get(0),PROB_ERROR_MARGIN );
			assertEquals(0.35f , probList.get(1),PROB_ERROR_MARGIN );
		}
		
		
		// edit interval of P(E=e1) should be [0.0065, 0.9965]
		for (MarkovEngineImpl engine : engines) {
			editInterval = engine.getEditLimits(userNameToIDMap.get("Eric"), 0x0E, 0, assumptionIds, assumedStates);
			assertNotNull(editInterval);
			assertEquals(2, editInterval.size());
			assertEquals(0.0065f, editInterval.get(0) ,PROB_ERROR_MARGIN);
			assertEquals(0.9965f, editInterval.get(1) ,PROB_ERROR_MARGIN);
		}
		
		// set P(E=e1) = 0.8 and P(E=e2) = 0.2
		transactionKey = engines.get(0).startNetworkActions();
		for (int i = 1; i < engines.size() ; i++) {
			assertEquals(engines.get(i).toString() ,transactionKey, engines.get(i).startNetworkActions());
		}
		newValues = new ArrayList<Float>();
		newValues.add(.8f);
		newValues.add(.2f);
		for (MarkovEngineImpl engine : engines) {
			assertEquals(2, engine.addTrade(
					transactionKey, 
					new Date(), 
					"Eric bets P(E=e1) = 0.8", 
					userNameToIDMap.get("Eric"), 
					0x0E, 
					newValues, 
					assumptionIds, 
					assumedStates, 
					false
			).size());
			engine.commitNetworkActions(transactionKey);
		}
		
		// cannot reuse same transaction key
		for (MarkovEngineImpl engine : engines) {
			try {
				newValues = new ArrayList<Float>(2);
				newValues.add(.9f);	newValues.add(.1f);
				engine.addTrade(transactionKey, new Date(), "To fail", Long.MAX_VALUE, (long)0x0D, newValues, null, null, false);
				fail("Should not allow to use commited transaction");
			} catch (IllegalArgumentException e) {
				assertNotNull(e);
			}
		}
		
		for (MarkovEngineImpl engine : engines) {
			assertEquals(10.1177, engine.scoreUserEv(userNameToIDMap.get("Eric"), null, null), PROB_ERROR_MARGIN);
		}
		
		// check that new marginal of E is [0.8 0.2] (this is expected value), F is [0.2165, 0.7835], and D is [0.5824, 0.4176]
		for (MarkovEngineImpl engine : engines) {
			probList = engine.getProbList(0x0D, null, null);
			assertEquals(0.5824f, probList.get(0), PROB_ERROR_MARGIN);
			assertEquals(0.4176f, probList.get(1), PROB_ERROR_MARGIN);
			probList = engine.getProbList(0x0E, null, null);
			assertEquals(0.8f, probList.get(0), PROB_ERROR_MARGIN);
			assertEquals(0.2f, probList.get(1), PROB_ERROR_MARGIN);
			probList = engine.getProbList(0x0F, null, null);
			assertEquals(0.2165f, probList.get(0), PROB_ERROR_MARGIN);
			assertEquals(0.7835f, probList.get(1), PROB_ERROR_MARGIN);
		}
		
		// check that min-q is 57.142857...
		for (MarkovEngineImpl engine : engines) {
			minCash = engine.getCash(userNameToIDMap.get("Eric"), null, null);
			assertEquals((engine.getScoreFromQValues(57.142857f)), (minCash), ASSET_ERROR_MARGIN);
			assertEquals(57.142857f, (engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		}
		
		// check that LPE contains e2 and any D or F
		assumptionIds = new ArrayList<Long>();
		assumptionIds.add((long) 0x0D);		// 1st node is D; assumedStates must follow this order
		assumptionIds.add((long) 0x0E);		// 2nd node is E; assumedStates must follow this order
		assumptionIds.add((long) 0x0F);		// 3rd node is F; assumedStates must follow this order
		
		// check combination d1, e1, f1
		assumedStates = new ArrayList<Integer>();
		assumedStates.add(0);	// d1
		assumedStates.add(0);	// e1
		assumedStates.add(0);	// f1
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
			assertTrue("Obtained cash = " + cash, minCash < cash);
		}
		
		// check combination d1, e1, f2
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 1);	// f2
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
			assertTrue("Obtained cash = " + cash, minCash < cash);
		}
		
		// check combination d1, e2, f1
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 0);	// f1
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
			assertEquals(minCash, cash, ASSET_ERROR_MARGIN);
		}

		// check combination d1, e2, f2
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 1);	// f2
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
			assertEquals(minCash, cash, ASSET_ERROR_MARGIN);
		}

		// check combination d2, e1, f1 
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 0);	// f1
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
			assertTrue("Obtained cash = " + cash, minCash < cash);
		}
		
		// check combination d2, e1, f2 
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 1);	// f2
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
			assertTrue("Obtained cash = " + cash, minCash < cash);
		}

		// check combination d2, e2, f1
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 0);	// f1
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
			assertEquals(minCash, cash, ASSET_ERROR_MARGIN);
		}

		// check combination d2, e2, f2
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 1);	// f2
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
			assertEquals(minCash, cash, ASSET_ERROR_MARGIN);
		}

		
		
		// Eric bets  P(D=d1|F=f2) = 0.52 -> 0.7
		
		// check whether probability prior to edit is really [d1f2, d2f2] = [.52, .48]
		assumptionIds = new ArrayList<Long>();		
		assumedStates = new ArrayList<Integer>();
		assumptionIds.add((long) 0x0F);	
		for (MarkovEngineImpl engine : engines) {
			probList = engine.getProbList(0x0D, assumptionIds, Collections.singletonList(1));
			assertEquals(2, probList.size());
			assertEquals(0.52f , probList.get(0),PROB_ERROR_MARGIN );
			assertEquals(0.48f , probList.get(1),PROB_ERROR_MARGIN );
		}
		
		assumedStates.add(1);	// set f2 as assumed state
		
		// edit interval of P(D=d1|F=f2) should be [0.0091059, 0.9916058]
		for (MarkovEngineImpl engine : engines) {
			editInterval = engine.getEditLimits(userNameToIDMap.get("Eric"), 0x0D, 0, assumptionIds, assumedStates);
			assertNotNull(editInterval);
			assertEquals(2, editInterval.size());
			assertEquals(0.0091059f, editInterval.get(0) ,PROB_ERROR_MARGIN);
			assertEquals(0.9916058f, editInterval.get(1) ,PROB_ERROR_MARGIN);
		}
		
		
		// set P(D=d1|F=f2) = 0.7 and P(D=d2|F=f2) = 0.3
		transactionKey = engines.get(0).startNetworkActions();
		for (int i = 1; i < engines.size() ; i++) {
			assertEquals(engines.get(i).toString() ,transactionKey, engines.get(i).startNetworkActions());
		}
		newValues = new ArrayList<Float>();
		newValues.add(.7f);
		newValues.add(.3f);
		for (MarkovEngineImpl engine : engines) {
			assertEquals(2, engine.addTrade(
					transactionKey, 
					new Date(), 
					"Eric bets P(D=d1|F=f2) = 0.7", 
					userNameToIDMap.get("Eric"), 
					0x0D, 
					newValues, 
					assumptionIds, 
					assumedStates, 
					false
			).size());
			engine.commitNetworkActions(transactionKey);
		}
		
		// cannot reuse same transaction key
		for (MarkovEngineImpl engine : engines) {
			try {
				newValues = new ArrayList<Float>(2);
				newValues.add(.9f);	newValues.add(.1f);
				engine.addTrade(transactionKey, new Date(), "To fail", Long.MAX_VALUE, (long)0x0D, newValues, null, null, false);
				fail("Should not allow to use commited transaction");
			} catch (IllegalArgumentException e) {
				assertNotNull(e);
			}
		}

		for (MarkovEngineImpl engine : engines) {
			assertEquals(10.31615, engine.scoreUserEv(userNameToIDMap.get("Eric"), null, null), PROB_ERROR_MARGIN);
		}
		
		
		// check that new marginal of E is [0.8509, 0.1491], F is  [0.2165, 0.7835], and D is [0.7232, 0.2768]
		for (MarkovEngineImpl engine : engines) {
			probList = engine.getProbList(0x0D, null, null);
			assertEquals(0.7232f, probList.get(0), PROB_ERROR_MARGIN);
			assertEquals(0.2768f, probList.get(1), PROB_ERROR_MARGIN);
			probList = engine.getProbList(0x0E, null, null);
			assertEquals(0.8509f, probList.get(0), PROB_ERROR_MARGIN);
			assertEquals(0.1491f, probList.get(1), PROB_ERROR_MARGIN);
			probList = engine.getProbList(0x0F, null, null);
			assertEquals(0.2165f, probList.get(0), PROB_ERROR_MARGIN);
			assertEquals(0.7835f, probList.get(1), PROB_ERROR_MARGIN);
		}
		
		
		// check that min-q is 35.7393...
		for (MarkovEngineImpl engine : engines) {
			minCash = engine.getCash(userNameToIDMap.get("Eric"), null, null);
			assertEquals((engine.getScoreFromQValues(35.7393f)), (minCash), ASSET_ERROR_MARGIN);
			assertEquals(35.7393f, (engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		}
		
		// check that LPE is d2, e2 and f2
		assumptionIds = new ArrayList<Long>();
		assumptionIds.add((long) 0x0D);		// 1st node is D; assumedStates must follow this order
		assumptionIds.add((long) 0x0E);		// 2nd node is E; assumedStates must follow this order
		assumptionIds.add((long) 0x0F);		// 3rd node is F; assumedStates must follow this order
		
		// check combination d1, e1, f1
		assumedStates = new ArrayList<Integer>();
		assumedStates.add(0);	// d1
		assumedStates.add(0);	// e1
		assumedStates.add(0);	// f1
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
			assertTrue("Obtained cash = " + cash, minCash < cash);
		}
		
		// check combination d1, e1, f2
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 1);	// f2
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
			assertTrue("Obtained cash = " + cash, minCash < cash);
		}
		
		// check combination d1, e2, f1
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 0);	// f1
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
			assertTrue("Obtained cash = " + cash, minCash < cash);
		}
		
		// check combination d1, e2, f2
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 1);	// f2
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
			assertTrue("Obtained cash = " + cash, minCash < cash);
		}
		
		// check combination d2, e1, f1 
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 0);	// f1
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
			assertTrue("Obtained cash = " + cash, minCash < cash);
		}
		
		// check combination d2, e1, f2 
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 1);	// f2
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
			assertTrue("Obtained cash = " + cash, minCash < cash);
		}
		
		// check combination d2, e2, f1
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 0);	// f1
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
			assertTrue("Obtained cash = " + cash, minCash < cash);
		}
		
		// check combination d2, e2, f2
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 1);	// f2
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
			assertEquals(minCash, cash, ASSET_ERROR_MARGIN);
		}
		

		// Eric makes a bet which makes his assets-q to go below 1, but the algorithm does not allow it
		
		// extract allowed interval of P(D=d1|F=f2), so that we can an edit incompatible with such interval
		assumptionIds = new ArrayList<Long>();		
		assumedStates = new ArrayList<Integer>();
		assumptionIds.add((long) 0x0F);	
		assumedStates.add(1);	// set f2 as assumed state
		for (MarkovEngineImpl engine : engines) {
			editInterval = engine.getEditLimits(userNameToIDMap.get("Eric"), 0x0D, 0, assumptionIds, assumedStates);
			assertNotNull(editInterval);
			assertEquals(2, editInterval.size());
			assertEquals(0.0091059f, editInterval.get(0) ,PROB_ERROR_MARGIN);
			assertEquals(0.9916058f, editInterval.get(1) ,PROB_ERROR_MARGIN);
		}
		
		
		// get history before transaction, so that we can make sure new transaction is not added into history
		for (MarkovEngineImpl engine : engines) {
			List<QuestionEvent> questionHistory = engine.getQuestionHistory(0x0DL, null, null);
			
			// check that final min-q of Tom is 20
			minCash = engine.getCash(userNameToIDMap.get("Tom"), null, null);
			assertEquals(20f, (engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
			assertEquals((engine.getScoreFromQValues(20f)), (minCash), ASSET_ERROR_MARGIN);
			
			// set P(D=d1|F=f2) to a value lower (1/10) than the lower bound of edit interval
			transactionKey = engine.startNetworkActions();
			newValues = new ArrayList<Float>();
			newValues.add(editInterval.get(0)/10);
			newValues.add(1-(editInterval.get(0)/10));
			assertNull(engine.addTrade(
					transactionKey, 
					new Date(), 
					"Eric bets P(D=d1|F=f2) = 0.7", 
					userNameToIDMap.get("Eric"), 
					0x0D, 
					newValues, 
					assumptionIds, 
					assumedStates, 
					false	// do not allow negative assets
			));
			// this is supposedly going to commit empty transaction
			engine.commitNetworkActions(transactionKey);
			// make sure history was not changed
			assertEquals(questionHistory, engine.getQuestionHistory(0x0DL, null, null));
		}

		// check that final min-q of Tom is 20
		for (MarkovEngineImpl engine : engines) {
			minCash = engine.getCash(userNameToIDMap.get("Tom"), null, null);
			assertEquals(20f, (engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
			assertEquals((engine.getScoreFromQValues(20f)), (minCash), ASSET_ERROR_MARGIN);
		}
		
		
		// check that marginals have not changed: E is [0.8509, 0.1491], F is  [0.2165, 0.7835], and D is [0.7232, 0.2768]
		for (MarkovEngineImpl engine : engines) {
			probList = engine.getProbList(0x0D, null, null);
			assertEquals(0.7232f, probList.get(0), PROB_ERROR_MARGIN);
			assertEquals(0.2768f, probList.get(1), PROB_ERROR_MARGIN);
			probList = engine.getProbList(0x0E, null, null);
			assertEquals(0.8509f, probList.get(0), PROB_ERROR_MARGIN);
			assertEquals(0.1491f, probList.get(1), PROB_ERROR_MARGIN);
			probList = engine.getProbList(0x0F, null, null);
			assertEquals(0.2165f, probList.get(0), PROB_ERROR_MARGIN);
			assertEquals(0.7835f, probList.get(1), PROB_ERROR_MARGIN);
		}
		
		
		// check that min-q has not changed
		for (MarkovEngineImpl engine : engines) {
			minCash = engine.getCash(userNameToIDMap.get("Eric"), null, null);
			assertEquals((engine.getScoreFromQValues(35.7393f)), (minCash), ASSET_ERROR_MARGIN);
			assertEquals(35.7393f, (engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		}
		
		// check that LPE has not changed - still d2, e2 and f2
		assumptionIds = new ArrayList<Long>();
		assumptionIds.add((long) 0x0D);		// 1st node is D; assumedStates must follow this order
		assumptionIds.add((long) 0x0E);		// 2nd node is E; assumedStates must follow this order
		assumptionIds.add((long) 0x0F);		// 3rd node is F; assumedStates must follow this order
		
		// check combination d1, e1, f1
		assumedStates = new ArrayList<Integer>();
		assumedStates.add(0);	// d1
		assumedStates.add(0);	// e1
		assumedStates.add(0);	// f1
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
			assertTrue("Obtained cash = " + cash, minCash < cash);
		}
		
		// check combination d1, e1, f2
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 1);	// f2
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
			assertTrue("Obtained cash = " + cash, minCash < cash);
		}
		
		// check combination d1, e2, f1
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 0);	// f1
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
			assertTrue("Obtained cash = " + cash, minCash < cash);
		}

		// check combination d1, e2, f2
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 1);	// f2
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
			assertTrue("Obtained cash = " + cash, minCash < cash);
		}

		// check combination d2, e1, f1 
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 0);	// f1
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
			assertTrue("Obtained cash = " + cash, minCash < cash);
		}
		
		// check combination d2, e1, f2 
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 1);	// f2
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
			assertTrue("Obtained cash = " + cash, minCash < cash);
		}

		// check combination d2, e2, f1
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 0);	// f1
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
			assertTrue("Obtained cash = " + cash, minCash < cash);
		}

		// check combination d2, e2, f2
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 1);	// f2
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
			assertEquals(minCash, cash, ASSET_ERROR_MARGIN);
		}
		

		// add question disconnected question C
		for (MarkovEngineImpl engine : engines) {
			transactionKey = engine.startNetworkActions();
			engine.addQuestion(transactionKey, new Date(), (long)0x0C, 2, null);
			engine.commitNetworkActions(transactionKey);
		}
		

		
		// check that probabilities and assets related to old node did not change
		
		// check that final marginal of E is [0.8509, 0.1491], F is  [0.2165, 0.7835], and D is [0.7232, 0.2768]
		for (MarkovEngineImpl engine : engines) {
			probList = engine.getProbList(0x0D, null, null);
			assertEquals(engine.toString(), 0.7232f, probList.get(0), PROB_ERROR_MARGIN);
			assertEquals(engine.toString(), 0.2768f, probList.get(1), PROB_ERROR_MARGIN);
			probList = engine.getProbList(0x0E, null, null);
			assertEquals(engine.toString(), 0.8509f, probList.get(0), PROB_ERROR_MARGIN);
			assertEquals(engine.toString(), 0.1491f, probList.get(1), PROB_ERROR_MARGIN);
			probList = engine.getProbList(0x0F, null, null);
			assertEquals(engine.toString(), 0.2165f, probList.get(0), PROB_ERROR_MARGIN);
			assertEquals(engine.toString(), 0.7835f, probList.get(1), PROB_ERROR_MARGIN);
		}
		
		// set assumptions to D,E,F, so that we can use it to calculate conditional min-q (in order to test consistency of LPE)
		assumptionIds = new ArrayList<Long>();
		assumptionIds.add((long) 0x0D);		// 1st node is D; assumedStates must follow this order
		assumptionIds.add((long) 0x0E);		// 2nd node is E; assumedStates must follow this order
		assumptionIds.add((long) 0x0F);		// 3rd node is F; assumedStates must follow this order
		// init list of states of the assumptions
		assumedStates = new ArrayList<Integer>();	
		assumedStates.add(0);	// d1
		assumedStates.add(0);	// e1
		assumedStates.add(0);	// f1
		
		// check that final min-q of Tom is 20
		for (MarkovEngineImpl engine : engines) {
			minCash = engine.getCash(userNameToIDMap.get("Tom"), null, null);
			assertEquals(engine.toString(), 20f, (engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
			assertEquals(engine.toString(), (engine.getScoreFromQValues(20f)), (minCash), ASSET_ERROR_MARGIN);
		}
		
		// check that final LPE of Tom contains d1, e2 and any value F
		
		// check combination d1, e1, f1 (not min)
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 0);	// f1
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
			assertTrue("Obtained cash = " + cash, minCash < cash);
		}
		
		// check combination d1, e1, f2 (not min)
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 1);	// f2
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
			assertTrue("Obtained cash = " + cash, minCash < cash);
		}
		
		// check combination d1, e2, f1
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 0);	// f1
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
			assertEquals(minCash, cash, ASSET_ERROR_MARGIN);
		}

		// check combination d1, e2, f2
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 1);	// f2
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
			assertEquals(minCash, cash, ASSET_ERROR_MARGIN);
		}

		// check combination d2, e1, f1 (not min)
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 0);	// f1
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
			assertTrue("Obtained cash = " + cash, minCash < cash);
		}
		
		// check combination d2, e1, f2 (not min)
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 1);	// f2
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
			assertTrue("Obtained cash = " + cash, minCash < cash);
		}

		// check combination d2, e2, f1 (not min)
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 0);	// f1
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
			assertTrue("Obtained cash = " + cash, minCash < cash);
		}

		// check combination d2, e2, f2 (not min)
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 1);	// f2
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
			assertTrue("Obtained cash = " + cash, minCash < cash);
		}

		
		// check that min-q of Amy is 60...
		for (MarkovEngineImpl engine : engines) {
			minCash = engine.getCash(userNameToIDMap.get("Amy"), null, null);
			assertEquals((engine.getScoreFromQValues(60f)), (minCash), ASSET_ERROR_MARGIN);
			assertEquals(60f, (engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		}
		
		// check that LPE of Amy contains d1, f1 and any value E
		
		// check combination d1, e1, f1
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 0);	// f1
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
			assertEquals(minCash, cash, ASSET_ERROR_MARGIN);
		}
		
		// check combination d1, e1, f2
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 1);	// f2
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
			assertTrue("Obtained cash = " + cash, minCash < cash);
		}
		
		// check combination d1, e2, f1
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 0);	// f1
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
			assertEquals(minCash, cash, ASSET_ERROR_MARGIN);
		}

		// check combination d1, e2, f2
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 1);	// f2
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
			assertTrue("Obtained cash = " + cash, minCash < cash);
		}

		// check combination d2, e1, f1 
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 0);	// f1
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
			assertTrue("Obtained cash = " + cash, minCash < cash);
		}
		
		// check combination d2, e1, f2 
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 1);	// f2
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
			assertTrue("Obtained cash = " + cash, minCash < cash);
		}

		// check combination d2, e2, f1
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 0);	// f1
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
			assertTrue("Obtained cash = " + cash, minCash < cash);
		}

		// check combination d2, e2, f2
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 1);	// f2
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
			assertTrue("Obtained cash = " + cash, minCash < cash);
		}
		
		
		// check that min-q of Joe is 14.5454545...
		for (MarkovEngineImpl engine : engines) {
			minCash = engine.getCash(userNameToIDMap.get("Joe"), null, null);
			assertEquals((engine.getScoreFromQValues(14.5454545f)), (minCash), ASSET_ERROR_MARGIN);
			assertEquals(14.5454545f, (engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		}
		
		// check that LPE of Joe contains d2, e1, f1
		
		// check combination d1, e1, f1
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 0);	// f1
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
			assertTrue("Obtained cash = " + cash, minCash < cash);
		}
		
		// check combination d1, e1, f2
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 1);	// f2
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
			assertTrue("Obtained cash = " + cash, minCash < cash);
		}
		
		// check combination d1, e2, f1
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 0);	// f1
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
			assertTrue("Obtained cash = " + cash, minCash < cash);
		}

		// check combination d1, e2, f2
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 1);	// f2
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
			assertTrue("Obtained cash = " + cash, minCash < cash);
		}

		// check combination d2, e1, f1 
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 0);	// f1
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
			assertEquals(minCash, cash, ASSET_ERROR_MARGIN);
		}
		
		// check combination d2, e1, f2 
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 1);	// f2
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
			assertTrue("Obtained cash = " + cash, minCash < cash);
		}

		// check combination d2, e2, f1
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 0);	// f1
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
			assertTrue("Obtained cash = " + cash, minCash < cash);
		}

		// check combination d2, e2, f2
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 1);	// f2
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
			assertTrue("Obtained cash = " + cash, minCash < cash);
		}
		
		// check that final min-q of Eric is 35.7393...
		for (MarkovEngineImpl engine : engines) {
			minCash = engine.getCash(userNameToIDMap.get("Eric"), null, null);
			assertEquals((engine.getScoreFromQValues(35.7393f)), (minCash), ASSET_ERROR_MARGIN);
			assertEquals(35.7393f, (engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		}
		
		// check that final LPE of Eric is d2, e2 and f2
		
		// check combination d1, e1, f1
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 0);	// f1
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
			assertTrue("Obtained cash = " + cash, minCash < cash);
		}
		
		// check combination d1, e1, f2
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 1);	// f2
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
			assertTrue("Obtained cash = " + cash, minCash < cash);
		}
		
		// check combination d1, e2, f1
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 0);	// f1
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
			assertTrue("Obtained cash = " + cash, minCash < cash);
		}
		
		// check combination d1, e2, f2
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 1);	// f2
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
			assertTrue("Obtained cash = " + cash, minCash < cash);
		}
		
		// check combination d2, e1, f1 
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 0);	// f1
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
			assertTrue("Obtained cash = " + cash, minCash < cash);
		}
		
		// check combination d2, e1, f2 
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 1);	// f2
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
			assertTrue("Obtained cash = " + cash, minCash < cash);
		}
		
		// check combination d2, e2, f1
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 0);	// f1
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
			assertTrue("Obtained cash = " + cash, minCash < cash);
		}
		
		// check combination d2, e2, f2
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 1);	// f2
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
			assertEquals(minCash, cash, ASSET_ERROR_MARGIN);
		}
		
		
		
		// Amy bets P(C=c1) = .5 -> .05
		
		// check whether probability of C prior to edit is really [.5, .5] no matter what combination of other nodes
		for (MarkovEngineImpl engine : engines) {
			probList = engine.getProbList(0x0C, null, null);
			assertEquals(2, probList.size());
			assertEquals(0.5f , probList.get(0),PROB_ERROR_MARGIN );
			assertEquals(0.5f , probList.get(1),PROB_ERROR_MARGIN );
			probList = engine.getProbList(0x0C, null, null);
			assertEquals(2, probList.size());
			assertEquals(0.5f , probList.get(0),PROB_ERROR_MARGIN );
			assertEquals(0.5f , probList.get(1),PROB_ERROR_MARGIN );
			probList = engine.getProbList(0x0C, null, null);
			assertEquals(2, probList.size());
			assertEquals(0.5f , probList.get(0),PROB_ERROR_MARGIN );
			assertEquals(0.5f , probList.get(1),PROB_ERROR_MARGIN );
			assumptionIds = new ArrayList<Long>(3);	assumedStates = new ArrayList<Integer>(3);
			assumptionIds.add((long)0x0D);	assumptionIds.add((long)0x0E);
			assumedStates.add((Math.random() < .5)?1:0); assumedStates.add((Math.random() < .5)?1:0);
			probList = engine.getProbList(0x0C, assumptionIds, assumedStates);
			assertEquals(2, probList.size());
			assertEquals(0.5f , probList.get(0),PROB_ERROR_MARGIN );
			assertEquals(0.5f , probList.get(1),PROB_ERROR_MARGIN );
			assumptionIds.set(0, (long)0x0D);	assumptionIds.set(1, (long)0x0F);
			assumedStates.set(0, (Math.random() < .5)?1:0); assumedStates.set(1, (Math.random() < .5)?1:0);
			probList = engine.getProbList(0x0C, assumptionIds, assumedStates);
			assertEquals(2, probList.size());
			assertEquals(0.5f , probList.get(0),PROB_ERROR_MARGIN );
			assertEquals(0.5f , probList.get(1),PROB_ERROR_MARGIN );
			assumptionIds.set(0, (long)0x0E);	assumptionIds.set(1, (long)0x0F);
			assumedStates.set(0, (Math.random() < .5)?1:0); assumedStates.set(1, (Math.random() < .5)?1:0);
			probList = engine.getProbList(0x0C, assumptionIds, assumedStates);
			assertEquals(2, probList.size());
			assertEquals(0.5f , probList.get(0),PROB_ERROR_MARGIN );
			assertEquals(0.5f , probList.get(1),PROB_ERROR_MARGIN );
			assumptionIds.set(0, (long)0x0E);	assumptionIds.set(1, (long)0x0F); assumptionIds.add((long)0x0D); 
			assumedStates.set(0, (Math.random() < .5)?1:0); assumedStates.set(1, (Math.random() < .5)?1:0); assumedStates.add((Math.random() < .5)?1:0);
			probList = engine.getProbList(0x0C, assumptionIds, assumedStates);
			assertEquals(2, probList.size());
			assertEquals(0.5f , probList.get(0),PROB_ERROR_MARGIN );
			assertEquals(0.5f , probList.get(1),PROB_ERROR_MARGIN );
		}
		
		// edit interval of P(C=c1) should be [0.0083, 0.9916667]
		for (MarkovEngineImpl engine : engines) {
			editInterval = engine.getEditLimits(userNameToIDMap.get("Amy"), 0x0C, 0, null, null);
			assertNotNull(editInterval);
			assertEquals(2, editInterval.size());
			assertEquals(engine.toString(), 0.0083f, editInterval.get(0) ,PROB_ERROR_MARGIN);
			assertEquals(engine.toString(), 0.9916667f, editInterval.get(1) ,PROB_ERROR_MARGIN);
		}
		
		// set P(C=c1) = 0.05 and   P(C=c1) = 0.95
		transactionKey = engines.get(0).startNetworkActions();
		for (int i = 1; i < engines.size() ; i++) {
			assertEquals(engines.get(i).toString() ,transactionKey, engines.get(i).startNetworkActions());
		}
		newValues = new ArrayList<Float>();
		newValues.add(.05f);
		newValues.add(.95f);
		for (MarkovEngineImpl engine : engines) {
			assertEquals(2, engine.addTrade(
					transactionKey, 
					new Date(), 
					"Amy bets P(C=c1) = 0.05", 
					userNameToIDMap.get("Amy"), 
					0x0C, 
					newValues, 
					null, 
					null, 
					false
			).size());
			engine.commitNetworkActions(transactionKey);
		}
		
		// cannot reuse same transaction key
		for (MarkovEngineImpl engine : engines) {
			try {
				newValues = new ArrayList<Float>(2);
				newValues.add(.9f);	newValues.add(.1f);
				engine.addTrade(transactionKey, new Date(), "To fail", Long.MAX_VALUE, (long)0x0D, newValues, null, null, false);
				fail("Should not allow to use commited transaction");
			} catch (IllegalArgumentException e) {
				assertNotNull(e);
			}
		}
		
		
		// check that marginals of C is [.05,.95], and others have not changed: E is [0.8509, 0.1491], F is  [0.2165, 0.7835], and D is [0.7232, 0.2768]
		for (MarkovEngineImpl engine : engines) {
			probList = engine.getProbList(0x0C, null, null);
			assertEquals(0.05f, probList.get(0), PROB_ERROR_MARGIN);
			assertEquals(0.95f, probList.get(1), PROB_ERROR_MARGIN);
			probList = engine.getProbList(0x0D, null, null);
			assertEquals(0.7232f, probList.get(0), PROB_ERROR_MARGIN);
			assertEquals(0.2768f, probList.get(1), PROB_ERROR_MARGIN);
			probList = engine.getProbList(0x0E, null, null);
			assertEquals(0.8509f, probList.get(0), PROB_ERROR_MARGIN);
			assertEquals(0.1491f, probList.get(1), PROB_ERROR_MARGIN);
			probList = engine.getProbList(0x0F, null, null);
			assertEquals(0.2165f, probList.get(0), PROB_ERROR_MARGIN);
			assertEquals(0.7835f, probList.get(1), PROB_ERROR_MARGIN);
		}
		
		// check that min-q is 6...
		for (MarkovEngineImpl engine : engines) {
			minCash = engine.getCash(userNameToIDMap.get("Amy"), null, null);
			assertEquals((engine.getScoreFromQValues(6f)), (minCash), ASSET_ERROR_MARGIN);
			assertEquals(6f, (engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		}
		
		// check that new LPE of Amy is independent of E
		for (MarkovEngineImpl engine : engines) {
			assertEquals(minCash, engine.getCash(userNameToIDMap.get("Amy"), Collections.singletonList(0x0EL), Collections.singletonList(0)), ASSET_ERROR_MARGIN);
			assertEquals(minCash, engine.getCash(userNameToIDMap.get("Amy"), Collections.singletonList(0x0EL), Collections.singletonList(1)), ASSET_ERROR_MARGIN);
		}
		
		// check that LPE is d1 c1 f1
		assumptionIds = new ArrayList<Long>();
		assumptionIds.add((long) 0x0D);		// 1st node is D; assumedStates must follow this order
		assumptionIds.add((long) 0x0C);		// 2nd node is C; assumedStates must follow this order
		assumptionIds.add((long) 0x0F);		// 3rd node is F; assumedStates must follow this order
		
		// check combination d1, c1, f1
		assumedStates = new ArrayList<Integer>();
		assumedStates.add(0);	// d1
		assumedStates.add(0);	// c1
		assumedStates.add(0);	// f1
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
			assertEquals(minCash, cash, ASSET_ERROR_MARGIN);
		}
		
		// check combination d1, c1, f2
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 0);	// c1
		assumedStates.set(2, 1);	// f2
		for (MarkovEngineImpl engine : engines) {		
			cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
			assertTrue("Obtained cash = " + cash, minCash < cash);
		}
		
		// check combination d1, c2, f1
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// c2
		assumedStates.set(2, 0);	// f1
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
			assertTrue("Obtained cash = " + cash, minCash < cash);
		}

		// check combination d1, c2, f2
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// c2
		assumedStates.set(2, 1);	// f2
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
			assertTrue("Obtained cash = " + cash, minCash < cash);
		}

		// check combination d2, c1, f1 
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// c1
		assumedStates.set(2, 0);	// f1
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
			assertTrue("Obtained cash = " + cash, minCash < cash);
		}
		
		// check combination d2, c1, f2 
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// c1
		assumedStates.set(2, 1);	// f2
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
			assertTrue("Obtained cash = " + cash, minCash < cash);
		}

		// check combination d2, c2, f1
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// c2
		assumedStates.set(2, 0);	// f1
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
			assertTrue("Obtained cash = " + cash, minCash < cash);
		}

		// check combination d2, c2, f2
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// c2
		assumedStates.set(2, 1);	// f2
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
			assertTrue("Obtained cash = " + cash, minCash < cash);
		}
		
		// check incomplete condition of LPE: c1
		for (MarkovEngineImpl engine : engines) {
			cash = engine.getCash(userNameToIDMap.get("Amy"), Collections.singletonList((long)0x0C), Collections.singletonList(0));
			assertEquals(minCash, cash, ASSET_ERROR_MARGIN);
			cash = engine.getCash(userNameToIDMap.get("Amy"), Collections.singletonList((long)0x0C), Collections.singletonList(1));
			assertTrue("Obtained cash = " + cash, minCash < cash);
		}
		

		// Eric makes a bet which makes his assets-q to go below 1, and the algorithm allows it
		
		// extract allowed interval of P(D=d1|F=f2), so that we can an edit incompatible with such interval
		assumptionIds = new ArrayList<Long>();		
		assumedStates = new ArrayList<Integer>();
		assumptionIds.add((long) 0x0F);	
		assumedStates.add(1);	// set f2 as assumed state
		for (MarkovEngineImpl engine : engines) {
			editInterval = engine.getEditLimits(userNameToIDMap.get("Eric"), 0x0D, 0, assumptionIds, assumedStates);
			assertNotNull(editInterval);
			assertEquals(2, editInterval.size());
			assertEquals(0.0091059f, editInterval.get(0) ,PROB_ERROR_MARGIN);
			assertEquals(0.9916058f, editInterval.get(1) ,PROB_ERROR_MARGIN);
		}
		
		
		List<Float> editLimits;
		for (MarkovEngineImpl engine : engines) {
			editLimits = engine.getEditLimits(userNameToIDMap.get("Eric"), 0x0DL, 0, assumptionIds, assumedStates);
			assertEquals(2, editLimits.size());
			assertTrue(editLimits.toString() , editLimits.get(0) > 0);
			assertTrue(editLimits.toString() , editLimits.get(0) < 1);
			assertTrue(editLimits.toString() , editLimits.get(1) > 0);
			assertTrue(editLimits.toString() , editLimits.get(1) < 1);
			assertTrue(editLimits.toString() , editLimits.get(0) < 0.7 && 0.7 < editLimits.get(1));
		}
		// check joint probability
		try {
			List<Long> questionIds = new ArrayList<Long>();
			questionIds.add(0x0DL);questionIds.add(0x0EL);questionIds.add(0x0FL);
			List<Integer> states = new ArrayList<Integer>();
			states.add(0); states.add(0); states.add(0); 
			for (int i = 0; i < 8; i++) {
				states.set(0, i%2);	states.set(0, (i/2)%2); states.set(2, (i/4)%2);
				float jointProbability = engines.get(0).getJointProbability(questionIds, states);
				for (int j = 1; j < engines.size(); j++) {
					assertEquals(engines.get(j)+ ", state = "+ i, jointProbability, engines.get(j).getJointProbability(questionIds, states), PROB_ERROR_MARGIN);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		// set P(D=d1|F=f2) to a value lower (1/10) than the lower bound of edit interval
		transactionKey = engines.get(0).startNetworkActions();
		for (int i = 1; i < engines.size() ; i++) {
			assertEquals(engines.get(i).toString() ,transactionKey, engines.get(i).startNetworkActions());
		}
		newValues = new ArrayList<Float>();
		newValues.add(editInterval.get(0)/10);
		newValues.add(1-(editInterval.get(0)/10));
		for (MarkovEngineImpl engine : engines) {
			assertEquals(2, engine.addTrade(
					transactionKey, 
					new Date(), 
					"Eric bets P(D=d1|F=f2) = 0.7", 
					userNameToIDMap.get("Eric"), 
					0x0D, 
					newValues, 
					assumptionIds, 
					assumedStates, 
					true	// allow negative assets
			).size());
			engine.commitNetworkActions(transactionKey);
		}
		
		// cannot reuse same transaction key
		for (MarkovEngineImpl engine : engines) {
			try {
				newValues = new ArrayList<Float>(2);
				newValues.add(.9f);	newValues.add(.1f);
				engine.addTrade(transactionKey, new Date(), "To fail", Long.MAX_VALUE, (long)0x0D, newValues, null, null, false);
				fail("Should not allow to use commited transaction");
			} catch (IllegalArgumentException e) {
				assertNotNull(e);
			}
		}
		
		
		// check that cash is smaller or equal to 0
		for (MarkovEngineImpl engine : engines) {
			minCash = engine.getCash(userNameToIDMap.get("Eric"), null, null);
			assertTrue("Obtained unexpected cash = " + minCash, minCash <= 0);
		}
		// check joint probability
		try {
			List<Long> questionIds = new ArrayList<Long>();
			questionIds.add(0x0DL);questionIds.add(0x0EL);questionIds.add(0x0FL);
			List<Integer> states = new ArrayList<Integer>();
			states.add(0); states.add(0); states.add(0); 
			for (int i = 0; i < 8; i++) {
				states.set(0, i%2);	states.set(0, (i/2)%2); states.set(2, (i/4)%2);
				float jointProbability = engines.get(0).getJointProbability(questionIds, states);
				for (int j = 1; j < engines.size(); j++) {
					assertEquals(engines.get(j)+ ", state = "+ i, jointProbability, engines.get(j).getJointProbability(questionIds, states), PROB_ERROR_MARGIN);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		// check min assets
		{
			assumptionIds = new ArrayList<Long>();
			assumptionIds.add(0x0DL);assumptionIds.add(0x0EL);assumptionIds.add(0x0FL);
			assumptionIds.add(0x0CL);
			assumedStates = new ArrayList<Integer>();
			assumedStates.add(0);assumedStates.add(0);assumedStates.add(0);
			assumedStates.add(0);
			for (int i = 0; i < 48; i++) {
				assumedStates.set(0,(i/1)%2);assumedStates.set(1,(i/2)%2);assumedStates.set(2,(i/4)%2);
				assumedStates.set(3,(i/8)%2);
				for (String user : userNameToIDMap.keySet()) {
					minCash = engines.get(0).getCash(userNameToIDMap.get(user), assumptionIds, assumedStates);
					for (int j = 1; j < engines.size(); j++) {
						assertEquals(engines.get(j) + ", index="+i + ", user=" + user, 
								minCash, engines.get(j).getCash(userNameToIDMap.get(user), assumptionIds, assumedStates), ASSET_ERROR_MARGIN);
					}
				}
			}
		} 
		
		
	}
	

	
	/**
	 * Test method for the DEF network. Testing the following conditions
	 * in 4 engines (respectively the Markov Engine - the official - Markov Engine with q-values,
	 * Brute force markov engine - product of cliques / product of separators, and Brute force
	 * markov engine - product of CPTs):
	 * <br/>
	 * a.) 5-point min-q values test regarding the edit bound; expect to see corresponding 
	 * q<1, =1, >1 respectively. <br/>
	 * b.) marginal probability on individual variable.<br/>
	 * c.) min-q values after a user confirms a trade.<br/>
	 * d.) min-q state. (not tested in this version)<br/>
	 * e.) The expected score.<br/>
	 * f. ) conditional min-q and expected score on randomly given states. 
	 * How many random given states depends on network size. 
	 * We choose floor(0.3*numberOfVariablesInTheNet).<br/>
	 * g.) An userÅfs asset table is not changed when other user makes edit.<br/>
	 * <br/>
	 * Methodology (using total of 3 users):<br/>
	 * (1) Randomly choose one user; <br/>
	 * (2) Randomly choose one node, and randomly choose assumption set in the same clique of the node. 
	 * If the clique size is bigger than 3, the size of assumption set has to be at least 2.<br/>
	 * <br/>
	 * <br/>
	 * Definition: 5-point min-q test is to verify the min-q values returned after trades are 
	 * made on specific edit around boundary over the edit limit. 
	 * In particular, we choose 5 point of probability edit: <br/>
	 * (1) the probability close to but smaller than the lower bound;<br/> 
	 * (2) the probability exactly on the lower bound;<br/> 
	 * (3) random probability in between the bound;<br/> 
	 * (4) the probability exactly on the upper bound;<br/> 
	 * (5) the probability close to but bigger than the upper bound;<br/> 
	 */
	public final void testDEF5Points() {
		
		// most basic assertion
		assertNotNull(engines);
		assertFalse(engines.isEmpty());
		
		// initialize network
		Map<Long,Integer> questionsToQuantityOfStatesMap = new HashMap<Long, Integer>();
		// generate the DEF network
		for (MarkovEngineInterface engine : engines) {
			long transactionKey = engine.startNetworkActions();
			// create nodes D, E, F
			engine.addQuestion(transactionKey, new Date(), 0x0D, 2, null);	// question D has ID = hexadecimal D. CPD == null -> linear distro
			questionsToQuantityOfStatesMap.put(0x0DL, 2);
			engine.addQuestion(transactionKey, new Date(), 0x0E, 2, null);	// question E has ID = hexadecimal E. CPD == null -> linear distro
			questionsToQuantityOfStatesMap.put(0x0EL,2);
			engine.addQuestion(transactionKey, new Date(), 0x0F, 2, null);	// question F has ID = hexadecimal F. CPD == null -> linear distro
			questionsToQuantityOfStatesMap.put(0x0FL,2);
			// create edge D->E 
			engine.addQuestionAssumption(transactionKey, new Date(), 0x0E, Collections.singletonList((long) 0x0D), null);	// cpd == null -> linear distro
			// create edge D->F
			engine.addQuestionAssumption(transactionKey, new Date(), 0x0F, Collections.singletonList((long) 0x0D), null);	// cpd == null -> linear distro
			// commit changes
			engine.commitNetworkActions(transactionKey);
		}
		
		assertNotNull(questionsToQuantityOfStatesMap);
		this.runTestAssumingInitializedNetwork(questionsToQuantityOfStatesMap);
	}	
	
	
	/**
	 * Test method for the networks loaded from files identified by the names in {@link #getFileNames()}.
	 * Testing the following conditions
	 * in 4 engines (respectively the Markov Engine - the official - Markov Engine with q-values,
	 * Brute force markov engine - product of cliques / product of separators, and Brute force
	 * markov engine - product of CPTs):
	 * <br/>
	 * a.) 5-point min-q values test regarding the edit bound; expect to see corresponding 
	 * q<1, =1, >1 respectively. <br/>
	 * b.) marginal probability on individual variable.<br/>
	 * c.) min-q values after a user confirms a trade.<br/>
	 * d.) min-q state. (not tested in this version)<br/>
	 * e.) The expected score.<br/>
	 * f. ) conditional min-q and expected score on randomly given states. 
	 * How many random given states depends on network size. 
	 * We choose floor(0.3*numberOfVariablesInTheNet).<br/>
	 * g.) An userÅfs asset table is not changed when other user makes edit.<br/>
	 * <br/>
	 * Methodology (using total of 3 users):<br/>
	 * (1) Randomly choose one user; <br/>
	 * (2) Randomly choose one node, and randomly choose assumption set in the same clique of the node. 
	 * If the clique size is bigger than 3, the size of assumption set has to be at least 2.<br/>
	 * <br/>
	 * <br/>
	 * Definition: 5-point min-q test is to verify the min-q values returned after trades are 
	 * made on specific edit around boundary over the edit limit. 
	 * In particular, we choose 5 point of probability edit: <br/>
	 * (1) the probability close to but smaller than the lower bound;<br/> 
	 * (2) the probability exactly on the lower bound;<br/> 
	 * (3) random probability in between the bound;<br/> 
	 * (4) the probability exactly on the upper bound;<br/> 
	 * (5) the probability close to but bigger than the upper bound;<br/> 
	 */
	public final void testFiles() {
		
		// most basic assertion
		assertNotNull(engines);
		assertFalse(engines.isEmpty());
		
		assertNotNull(getFileNames());
		assertFalse(getFileNames().length <= 0);
		
		NetIO io = new NetIO();
		
		Graph network = null;
		for (String fileName : getFileNames()) {
			System.out.println("Reading file " + fileName);
			try {
				network = io.load(new File(getClass().getClassLoader().getResource(fileName).getFile()));
			} catch (Exception e) {
				e.printStackTrace();
				fail(e.getMessage());
			} 
			assertNotNull(network);
			
			// initialize network based on network loaded from file
			Map<Long,Integer> questionsToQuantityOfStatesMap = new HashMap<Long, Integer>();
			// generate the DEF network
			for (MarkovEngineInterface engine : engines) {
				engine.initialize();
				long transactionKey = engine.startNetworkActions();
				// create nodes
				for (Node node : network.getNodes()) {
					engine.addQuestion(transactionKey, new Date(), Long.parseLong(node.getName()), node.getStatesSize(), null); // assume uniform distribution for all nodes 
					questionsToQuantityOfStatesMap.put(Long.parseLong(node.getName()), node.getStatesSize());
				}
				// create edges 
				for (Node node : network.getNodes()) {
					List<Long> parentIds = new ArrayList<Long>();
					for (Node parent : node.getParents()) {
						parentIds.add(Long.parseLong(parent.getName()));
					}
					if (parentIds.isEmpty() && Math.random() <= .5) {
						parentIds = null;
					}
					engine.addQuestionAssumption(transactionKey, new Date(), Long.parseLong(node.getName()), parentIds, null);	// cpd == null -> linear distro
				}
				// commit changes
				engine.commitNetworkActions(transactionKey);
			}
			
			assertNotNull(questionsToQuantityOfStatesMap);
			this.runTestAssumingInitializedNetwork(questionsToQuantityOfStatesMap);
		}
	}	
	
	
	

	/**
	 * This value indicates how many test iterations (5-point tests) will be performed by default
	 * @return the howManyTradesToTest
	 */
	public int getHowManyTradesToTest() {
		return howManyTradesToTest;
	}

	/**
	 * This value indicates how many test iterations (5-point tests) will be performed by default
	 * @param howManyTradesToTest the howManyTradesToTest to set
	 */
	public void setHowManyTradesToTest(int maxQuantityOfIterations) {
		this.howManyTradesToTest = maxQuantityOfIterations;
	}

	/**
	 * File names to be used in {@link #testFiles()} 
	 * @return the fileNames
	 */
	public String[] getFileNames() {
		return fileNames;
	}

	/**
	 * File names to be used in {@link #testFiles()} 
	 * @param fileNames the fileNames to set
	 */
	public void setFileNames(String[] fileNames) {
		this.fileNames = fileNames;
	}
	
}
