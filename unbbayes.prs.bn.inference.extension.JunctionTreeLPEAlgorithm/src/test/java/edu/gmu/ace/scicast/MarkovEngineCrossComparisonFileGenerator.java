package edu.gmu.ace.scicast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import junit.framework.TestCase;
import unbbayes.prs.INode;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.util.Debug;
import edu.gmu.ace.scicast.io.BNToChildrenMatrixConverter;

public class MarkovEngineCrossComparisonFileGenerator extends TestCase {

	/** Error margin used when comparing 2 probability values */
	public static final float PROB_ERROR_MARGIN = 0.0005f;

	private MarkovEngineImpl engine;
	
	public MarkovEngineCrossComparisonFileGenerator(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		engine = (MarkovEngineImpl) MarkovEngineImpl.getInstance(2, 100, 100, true, false, 0);
		// force engine to update assets too
		engine.getDefaultInferenceAlgorithm().setToUpdateAssets(true);	// force algorithm to handle assets too
		engine.setToAddArcsOnlyToProbabilisticNetwork(false);		// handle asset nets too
		engine.setToAddArcsWithoutReboot(false);					// reboot engine when arcs are added
		engine.setToExportOnlyCurrentSharedProbabilisticNet(true);	// do not consider history when exporting network
		assertTrue(engine.initialize());
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public final void testCliqueStabilityCPTNotInSameClique() {
		int iteration = 0;
		
		// this object is used in order to format the actions (e.g. trades) to a text format we used in Y2 for cross comparison
		Tracer tracer = new Tracer();
		tracer.setIterationNumber(iteration);
		
		// build the network
		long transactionKey = engine.startNetworkActions();
		engine.addQuestion(transactionKey, new Date(), 0, 2, null);
		tracer.getAddedQuestions().add(0L);
		tracer.getAddedQuestionsStateSize().add(2);
		engine.addQuestion(transactionKey, new Date(), 1, 2, null);
		tracer.getAddedQuestions().add(1L);
		tracer.getAddedQuestionsStateSize().add(2);
		engine.addQuestion(transactionKey, new Date(), 2, 2, null);
		tracer.getAddedQuestions().add(2L);
		tracer.getAddedQuestionsStateSize().add(2);
		engine.addQuestion(transactionKey, new Date(), 3, 2, null);
		tracer.getAddedQuestions().add(3L);
		tracer.getAddedQuestionsStateSize().add(2);
		engine.addQuestion(transactionKey, new Date(), 4, 2, null);
		tracer.getAddedQuestions().add(4L);
		tracer.getAddedQuestionsStateSize().add(2);
		engine.addQuestionAssumption(transactionKey, new Date(), 1, Collections.singletonList(0L), null);
		engine.addQuestionAssumption(transactionKey, new Date(), 0, Collections.singletonList(3L), null);
		engine.addQuestionAssumption(transactionKey, new Date(), 3, Collections.singletonList(4L), null);
		engine.addQuestionAssumption(transactionKey, new Date(), 2, Collections.singletonList(1L), null);
		engine.addQuestionAssumption(transactionKey, new Date(), 2, Collections.singletonList(3L), null);
		engine.addQuestionAssumption(transactionKey, new Date(), 2, Collections.singletonList(4L), null);
		engine.commitNetworkActions(transactionKey);
		
		
		tracer.setAddedCash(5000f);	
		engine.addCash(null, new Date(), 1, tracer.getAddedCash(), "Just make sure user 1 has enough cash");
		assertEquals(5100, engine.getCash(1, null, null), 0.1);
		
		tracer.setUserId(1);
		tracer.setQuestionId(4L);
		tracer.setTargetState(1);
		tracer.setEditLimit(engine.getEditLimits(tracer.getUserId(), tracer.getQuestionId(), tracer.getTargetState(), null, null));
		
		// use trades to initialize probabilities
		List<Float> newValues = new ArrayList<Float>(2);
		newValues.add(0.99f); newValues.add(1-newValues.get(0));
		tracer.setTargetProb(newValues);
		engine.addTrade(null, new Date(), " (N4) {  data = ( 0.99 0.01 ); }", tracer.getUserId(), tracer.getQuestionId(), tracer.getTargetProb() , null, null, true);
		tracer.setProbLists(engine.getProbLists(null, null, null));
		// store all users' score and cash
		for (Long usr : engine.getUserToAssetAwareAlgorithmMap().keySet()) {
			float score = engine.scoreUserEv(usr, null, null);
			float cash  = engine.getCash(usr, null, null);
			tracer.getUserScoreAndCash().add(
					tracer.new UserScoreAndCash(
							usr, 
							score , 
							cash )
			);
		}
		System.out.print(tracer.toString());
		
		tracer = new Tracer();
		tracer.setIterationNumber(++iteration);
		tracer.setUserId(1);
		tracer.setQuestionId(1L);
		tracer.setTargetState(1);
		tracer.setAssumptionIds(Collections.singletonList(0L));
		tracer.setAssumedStates(Collections.singletonList(0));
		tracer.setEditLimit(engine.getEditLimits(tracer.getUserId(), tracer.getQuestionId(), tracer.getTargetState(), tracer.getAssumptionIds(), tracer.getAssumedStates()));
		newValues = new ArrayList<Float>(2);
		newValues.add(0.1f); newValues.add(1-newValues.get(0));
		tracer.setTargetProb(newValues);
		engine.addTrade(null, new Date(), " (N1 | N0) {  data = (( 0.1 0.9 ) ( 0.6 0.4 )); }", tracer.getUserId(), tracer.getQuestionId(), tracer.getTargetProb() , tracer.getAssumptionIds(), tracer.getAssumedStates(), true);
		tracer.setProbLists(engine.getProbLists(null, null, null));
		// store all users' score and cash
		for (Long usr : engine.getUserToAssetAwareAlgorithmMap().keySet()) {
			float score = engine.scoreUserEv(usr, null, null);
			float cash  = engine.getCash(usr, null, null);
			tracer.getUserScoreAndCash().add(
					tracer.new UserScoreAndCash(
							usr, 
							score , 
							cash )
			);
		}
		System.out.print(tracer.toString());
		
		tracer = new Tracer();
		tracer.setIterationNumber(++iteration);
		tracer.setUserId(1);
		tracer.setQuestionId(1L);
		tracer.setTargetState(1);
		tracer.setAssumptionIds(Collections.singletonList(0L));
		tracer.setAssumedStates(Collections.singletonList(1));
		tracer.setEditLimit(engine.getEditLimits(tracer.getUserId(), tracer.getQuestionId(), tracer.getTargetState(), tracer.getAssumptionIds(), tracer.getAssumedStates()));
		newValues = new ArrayList<Float>(2);
		newValues.add(0.6f); newValues.add(1-newValues.get(0));
		tracer.setTargetProb(newValues);
		engine.addTrade(null, new Date(), " (N1 | N0) {  data = (( 0.1 0.9 ) ( 0.6 0.4 )); }", tracer.getUserId(), tracer.getQuestionId(), tracer.getTargetProb() , tracer.getAssumptionIds(), tracer.getAssumedStates(), true);
		tracer.setProbLists(engine.getProbLists(null, null, null));
		// store all users' score and cash
		for (Long usr : engine.getUserToAssetAwareAlgorithmMap().keySet()) {
			float score = engine.scoreUserEv(usr, null, null);
			float cash  = engine.getCash(usr, null, null);
			tracer.getUserScoreAndCash().add(
					tracer.new UserScoreAndCash(
							usr, 
							score , 
							cash )
			);
		}
		System.out.print(tracer.toString());
		
		tracer = new Tracer();
		tracer.setIterationNumber(++iteration);
		tracer.setUserId(1);
		tracer.setQuestionId(0L);
		tracer.setTargetState(1);
		tracer.setAssumptionIds(Collections.singletonList(3L));
		tracer.setAssumedStates(Collections.singletonList(0));
		tracer.setEditLimit(engine.getEditLimits(tracer.getUserId(), tracer.getQuestionId(), tracer.getTargetState(), tracer.getAssumptionIds(), tracer.getAssumedStates()));
		newValues = new ArrayList<Float>(2);
		newValues.add(0.3f); newValues.add(1-newValues.get(0));
		tracer.setTargetProb(newValues);
		engine.addTrade(null, new Date(), "(N0 | N3) {  data = (( 0.3 0.7 ) ( 0.9 0.1 ));", tracer.getUserId(), tracer.getQuestionId(), tracer.getTargetProb() , tracer.getAssumptionIds(), tracer.getAssumedStates(), true);
		tracer.setProbLists(engine.getProbLists(null, null, null));
		// store all users' score and cash
		for (Long usr : engine.getUserToAssetAwareAlgorithmMap().keySet()) {
			float score = engine.scoreUserEv(usr, null, null);
			float cash  = engine.getCash(usr, null, null);
			tracer.getUserScoreAndCash().add(
					tracer.new UserScoreAndCash(
							usr, 
							score , 
							cash )
			);
		}
		System.out.print(tracer.toString());
		
		tracer = new Tracer();
		tracer.setIterationNumber(++iteration);
		tracer.setUserId(1);
		tracer.setQuestionId(0L);
		tracer.setTargetState(1);
		tracer.setAssumptionIds(Collections.singletonList(3L));
		tracer.setAssumedStates(Collections.singletonList(1));
		tracer.setEditLimit(engine.getEditLimits(tracer.getUserId(), tracer.getQuestionId(), tracer.getTargetState(), tracer.getAssumptionIds(), tracer.getAssumedStates()));
		newValues = new ArrayList<Float>(2);
		newValues.add(0.9f); newValues.add(1-newValues.get(0));
		tracer.setTargetProb(newValues);
		engine.addTrade(null, new Date(), "(N0 | N3) {  data = (( 0.3 0.7 ) ( 0.9 0.1 ));", tracer.getUserId(), tracer.getQuestionId(), tracer.getTargetProb() , tracer.getAssumptionIds(), tracer.getAssumedStates(), true);
		tracer.setProbLists(engine.getProbLists(null, null, null));
		// store all users' score and cash
		for (Long usr : engine.getUserToAssetAwareAlgorithmMap().keySet()) {
			float score = engine.scoreUserEv(usr, null, null);
			float cash  = engine.getCash(usr, null, null);
			tracer.getUserScoreAndCash().add(
					tracer.new UserScoreAndCash(
							usr, 
							score , 
							cash )
			);
		}
		System.out.print(tracer.toString());
		
		tracer = new Tracer();
		tracer.setIterationNumber(++iteration);
		tracer.setUserId(1);
		tracer.setQuestionId(3L);
		tracer.setTargetState(1);
		tracer.setAssumptionIds(Collections.singletonList(4L));
		tracer.setAssumedStates(Collections.singletonList(0));
		tracer.setEditLimit(engine.getEditLimits(tracer.getUserId(), tracer.getQuestionId(), tracer.getTargetState(), tracer.getAssumptionIds(), tracer.getAssumedStates()));
		newValues = new ArrayList<Float>(2);
		newValues.add(0.2f); newValues.add(1-newValues.get(0));
		tracer.setTargetProb(newValues);
		engine.addTrade(null, new Date(), "(N3 | N4) {  data = (( 0.2 0.8 ) ( 0.4 0.6 ))", tracer.getUserId(), tracer.getQuestionId(), tracer.getTargetProb() , tracer.getAssumptionIds(), tracer.getAssumedStates(), true);
		tracer.setProbLists(engine.getProbLists(null, null, null));
		// store all users' score and cash
		for (Long usr : engine.getUserToAssetAwareAlgorithmMap().keySet()) {
			float score = engine.scoreUserEv(usr, null, null);
			float cash  = engine.getCash(usr, null, null);
			tracer.getUserScoreAndCash().add(
					tracer.new UserScoreAndCash(
							usr, 
							score , 
							cash )
			);
		}
		System.out.print(tracer.toString());
		
		tracer = new Tracer();
		tracer.setIterationNumber(++iteration);
		tracer.setUserId(1);
		tracer.setQuestionId(3L);
		tracer.setTargetState(1);
		tracer.setAssumptionIds(Collections.singletonList(4L));
		tracer.setAssumedStates(Collections.singletonList(1));
		tracer.setEditLimit(engine.getEditLimits(tracer.getUserId(), tracer.getQuestionId(), tracer.getTargetState(), tracer.getAssumptionIds(), tracer.getAssumedStates()));
		newValues = new ArrayList<Float>(2);
		newValues.add(0.4f); newValues.add(1-newValues.get(0));
		tracer.setTargetProb(newValues);
		engine.addTrade(null, new Date(), "(N3 | N4) {  data = (( 0.2 0.8 ) ( 0.4 0.6 ))", tracer.getUserId(), tracer.getQuestionId(), tracer.getTargetProb() , tracer.getAssumptionIds(), tracer.getAssumedStates(), true);
		tracer.setProbLists(engine.getProbLists(null, null, null));
		// store all users' score and cash
		for (Long usr : engine.getUserToAssetAwareAlgorithmMap().keySet()) {
			float score = engine.scoreUserEv(usr, null, null);
			float cash  = engine.getCash(usr, null, null);
			tracer.getUserScoreAndCash().add(
					tracer.new UserScoreAndCash(
							usr, 
							score , 
							cash )
			);
		}
		System.out.print(tracer.toString());
		
		// set CPT of 2, which has 3 parents
		List<Long> assumptions = new ArrayList<Long>();
		assumptions.add(4L);
		assumptions.add(3L);
		assumptions.add(1L);
		List<Integer> assumedStates = new ArrayList<Integer>();
		assumedStates.add(0);  assumedStates.add(0); assumedStates.add(0);
		tracer = new Tracer();
		tracer.setIterationNumber(++iteration);
		tracer.setUserId(1);
		tracer.setQuestionId(2L);
		tracer.setTargetState(1);
		tracer.setAssumptionIds(assumptions);
		tracer.setAssumedStates(assumedStates);
		tracer.setEditLimit(engine.getEditLimits(tracer.getUserId(), tracer.getQuestionId(), tracer.getTargetState(), tracer.getAssumptionIds(), tracer.getAssumedStates()));
		newValues = new ArrayList<Float>(2);
		newValues.add(0.1f);  newValues.add(1-newValues.get(0));
		tracer.setTargetProb(newValues);
		engine.addTrade(null, new Date(), "(N2 | N4 N3 N1)", tracer.getUserId(), tracer.getQuestionId(), tracer.getTargetProb() , tracer.getAssumptionIds(), tracer.getAssumedStates() , true);
		tracer.setProbLists(engine.getProbLists(null, null, null));
		// store all users' score and cash
		for (Long usr : engine.getUserToAssetAwareAlgorithmMap().keySet()) {
			float score = engine.scoreUserEv(usr, null, null);
			float cash  = engine.getCash(usr, null, null);
			tracer.getUserScoreAndCash().add(
					tracer.new UserScoreAndCash(
							usr, 
							score , 
							cash )
			);
		}
		System.out.print(tracer.toString());
		
		assumedStates.set(2,1);
		tracer = new Tracer();
		tracer.setIterationNumber(++iteration);
		tracer.setUserId(1);
		tracer.setQuestionId(2L);
		tracer.setTargetState(1);
		tracer.setAssumptionIds(assumptions);
		tracer.setAssumedStates(assumedStates);
		tracer.setEditLimit(engine.getEditLimits(tracer.getUserId(), tracer.getQuestionId(), tracer.getTargetState(), tracer.getAssumptionIds(), tracer.getAssumedStates()));
		newValues.set(0,newValues.get(1));  newValues.set(1,1-newValues.get(0));
		tracer.setTargetProb(newValues);
		engine.addTrade(null, new Date(), "(N2 | N4 N3 N1)", tracer.getUserId(), tracer.getQuestionId(), tracer.getTargetProb() , tracer.getAssumptionIds(), tracer.getAssumedStates() , true);
		tracer.setProbLists(engine.getProbLists(null, null, null));
		// store all users' score and cash
		for (Long usr : engine.getUserToAssetAwareAlgorithmMap().keySet()) {
			float score = engine.scoreUserEv(usr, null, null);
			float cash  = engine.getCash(usr, null, null);
			tracer.getUserScoreAndCash().add(
					tracer.new UserScoreAndCash(
							usr, 
							score , 
							cash )
			);
		}
		System.out.print(tracer.toString());
		
		assumedStates = new ArrayList<Integer>();
		assumedStates.add(0);  assumedStates.add(1); assumedStates.add(0);
		tracer = new Tracer();
		tracer.setIterationNumber(++iteration);
		tracer.setUserId(1);
		tracer.setQuestionId(2L);
		tracer.setTargetState(1);
		tracer.setAssumptionIds(assumptions);
		tracer.setAssumedStates(assumedStates);
		tracer.setEditLimit(engine.getEditLimits(tracer.getUserId(), tracer.getQuestionId(), tracer.getTargetState(), tracer.getAssumptionIds(), tracer.getAssumedStates()));
		newValues = new ArrayList<Float>(2);
		newValues.add(0.8f);  newValues.add(1-newValues.get(0));
		tracer.setTargetProb(newValues);
		engine.addTrade(null, new Date(), "(N2 | N4 N3 N1)", tracer.getUserId(), tracer.getQuestionId(), tracer.getTargetProb() , tracer.getAssumptionIds(), tracer.getAssumedStates() , true);
		tracer.setProbLists(engine.getProbLists(null, null, null));
		// store all users' score and cash
		for (Long usr : engine.getUserToAssetAwareAlgorithmMap().keySet()) {
			float score = engine.scoreUserEv(usr, null, null);
			float cash  = engine.getCash(usr, null, null);
			tracer.getUserScoreAndCash().add(
					tracer.new UserScoreAndCash(
							usr, 
							score , 
							cash )
			);
		}
		System.out.print(tracer.toString());
		
		assumedStates.set(2,1);
		tracer = new Tracer();
		tracer.setIterationNumber(++iteration);
		tracer.setUserId(1);
		tracer.setQuestionId(2L);
		tracer.setTargetState(1);
		tracer.setAssumptionIds(assumptions);
		tracer.setAssumedStates(assumedStates);
		tracer.setEditLimit(engine.getEditLimits(tracer.getUserId(), tracer.getQuestionId(), tracer.getTargetState(), tracer.getAssumptionIds(), tracer.getAssumedStates()));
		newValues.set(0,newValues.get(1));  newValues.set(1,1-newValues.get(0));
		tracer.setTargetProb(newValues);
		engine.addTrade(null, new Date(), "(N2 | N4 N3 N1)", tracer.getUserId(), tracer.getQuestionId(), tracer.getTargetProb() , tracer.getAssumptionIds(), tracer.getAssumedStates() , true);
		tracer.setProbLists(engine.getProbLists(null, null, null));
		// store all users' score and cash
		for (Long usr : engine.getUserToAssetAwareAlgorithmMap().keySet()) {
			float score = engine.scoreUserEv(usr, null, null);
			float cash  = engine.getCash(usr, null, null);
			tracer.getUserScoreAndCash().add(
					tracer.new UserScoreAndCash(
							usr, 
							score , 
							cash )
			);
		}
		System.out.print(tracer.toString());
		
		assumedStates = new ArrayList<Integer>();
		assumedStates.add(1);  assumedStates.add(0); assumedStates.add(0);
		tracer = new Tracer();
		tracer.setIterationNumber(++iteration);
		tracer.setUserId(1);
		tracer.setQuestionId(2L);
		tracer.setTargetState(1);
		tracer.setAssumptionIds(assumptions);
		tracer.setAssumedStates(assumedStates);
		tracer.setEditLimit(engine.getEditLimits(tracer.getUserId(), tracer.getQuestionId(), tracer.getTargetState(), tracer.getAssumptionIds(), tracer.getAssumedStates()));
		newValues = new ArrayList<Float>(2);
		newValues.add(0.3f);  newValues.add(1-newValues.get(0));
		tracer.setTargetProb(newValues);
		engine.addTrade(null, new Date(), "(N2 | N4 N3 N1)", tracer.getUserId(), tracer.getQuestionId(), tracer.getTargetProb() , tracer.getAssumptionIds(), tracer.getAssumedStates() , true);
		tracer.setProbLists(engine.getProbLists(null, null, null));
		// store all users' score and cash
		for (Long usr : engine.getUserToAssetAwareAlgorithmMap().keySet()) {
			float score = engine.scoreUserEv(usr, null, null);
			float cash  = engine.getCash(usr, null, null);
			tracer.getUserScoreAndCash().add(
					tracer.new UserScoreAndCash(
							usr, 
							score , 
							cash )
			);
		}
		System.out.print(tracer.toString());
		
		
		assumedStates.set(2,1);
		tracer = new Tracer();
		tracer.setIterationNumber(++iteration);
		tracer.setUserId(1);
		tracer.setQuestionId(2L);
		tracer.setTargetState(1);
		tracer.setAssumptionIds(assumptions);
		tracer.setAssumedStates(assumedStates);
		tracer.setEditLimit(engine.getEditLimits(tracer.getUserId(), tracer.getQuestionId(), tracer.getTargetState(), tracer.getAssumptionIds(), tracer.getAssumedStates()));
		newValues.set(0,newValues.get(1));  newValues.set(1,1-newValues.get(0));
		tracer.setTargetProb(newValues);
		engine.addTrade(null, new Date(), "(N2 | N4 N3 N1)", tracer.getUserId(), tracer.getQuestionId(), tracer.getTargetProb() , tracer.getAssumptionIds(), tracer.getAssumedStates() , true);
		tracer.setProbLists(engine.getProbLists(null, null, null));
		// store all users' score and cash
		for (Long usr : engine.getUserToAssetAwareAlgorithmMap().keySet()) {
			float score = engine.scoreUserEv(usr, null, null);
			float cash  = engine.getCash(usr, null, null);
			tracer.getUserScoreAndCash().add(
					tracer.new UserScoreAndCash(
							usr, 
							score , 
							cash )
			);
		}
		System.out.print(tracer.toString());
		
		assumedStates = new ArrayList<Integer>();
		assumedStates.add(1);  assumedStates.add(1); assumedStates.add(0);
		tracer = new Tracer();
		tracer.setIterationNumber(++iteration);
		tracer.setUserId(1);
		tracer.setQuestionId(2L);
		tracer.setTargetState(1);
		tracer.setAssumptionIds(assumptions);
		tracer.setAssumedStates(assumedStates);
		tracer.setEditLimit(engine.getEditLimits(tracer.getUserId(), tracer.getQuestionId(), tracer.getTargetState(), tracer.getAssumptionIds(), tracer.getAssumedStates()));
		newValues = new ArrayList<Float>(2);
		newValues.add(0.6f);  newValues.add(1-newValues.get(0));
		tracer.setTargetProb(newValues);
		engine.addTrade(null, new Date(), "(N2 | N4 N3 N1)", tracer.getUserId(), tracer.getQuestionId(), tracer.getTargetProb() , tracer.getAssumptionIds(), tracer.getAssumedStates() , true);
		tracer.setProbLists(engine.getProbLists(null, null, null));
		// store all users' score and cash
		for (Long usr : engine.getUserToAssetAwareAlgorithmMap().keySet()) {
			float score = engine.scoreUserEv(usr, null, null);
			float cash  = engine.getCash(usr, null, null);
			tracer.getUserScoreAndCash().add(
					tracer.new UserScoreAndCash(
							usr, 
							score , 
							cash )
			);
		}
		System.out.print(tracer.toString());
		
		assumedStates.set(2,1);
		tracer = new Tracer();
		tracer.setIterationNumber(++iteration);
		tracer.setUserId(1);
		tracer.setQuestionId(2L);
		tracer.setTargetState(1);
		tracer.setAssumptionIds(assumptions);
		tracer.setAssumedStates(assumedStates);
		tracer.setEditLimit(engine.getEditLimits(tracer.getUserId(), tracer.getQuestionId(), tracer.getTargetState(), tracer.getAssumptionIds(), tracer.getAssumedStates()));
		newValues.set(0,newValues.get(1));  newValues.set(1,1-newValues.get(0));
		tracer.setTargetProb(newValues);
		engine.addTrade(null, new Date(), "(N2 | N4 N3 N1)", tracer.getUserId(), tracer.getQuestionId(), tracer.getTargetProb() , tracer.getAssumptionIds(), tracer.getAssumedStates() , true);
		tracer.setProbLists(engine.getProbLists(null, null, null));
		// store all users' score and cash
		for (Long usr : engine.getUserToAssetAwareAlgorithmMap().keySet()) {
			float score = engine.scoreUserEv(usr, null, null);
			float cash  = engine.getCash(usr, null, null);
			tracer.getUserScoreAndCash().add(
					tracer.new UserScoreAndCash(
							usr, 
							score , 
							cash )
			);
		}
		System.out.print(tracer.toString());
		
		
		// make a trade that will supposedly generate dependency 1<--4
		assumptions = Collections.singletonList(4L);
		assumedStates = Collections.singletonList(0);
		tracer = new Tracer();
		tracer.setIterationNumber(++iteration);
		tracer.setUserId(1);
		tracer.setQuestionId(1L);
		tracer.setTargetState(1);
		tracer.setAssumptionIds(assumptions);
		tracer.setAssumedStates(assumedStates);
		tracer.setEditLimit(engine.getEditLimits(tracer.getUserId(), tracer.getQuestionId(), tracer.getTargetState(), tracer.getAssumptionIds(), tracer.getAssumedStates()));
		newValues = new ArrayList<Float>(2);
		newValues.add(0.1f); newValues.add(0.9f);
		tracer.setTargetProb(newValues);
		engine.addTrade(null, new Date(), "a trade that will generate arc 1<--4", tracer.getUserId(), tracer.getQuestionId(), tracer.getTargetProb() , tracer.getAssumptionIds(), tracer.getAssumedStates() , true);
		tracer.setProbLists(engine.getProbLists(null, null, null));
		// store all users' score and cash
		for (Long usr : engine.getUserToAssetAwareAlgorithmMap().keySet()) {
			float score = engine.scoreUserEv(usr, null, null);
			float cash  = engine.getCash(usr, null, null);
			tracer.getUserScoreAndCash().add(
					tracer.new UserScoreAndCash(
							usr, 
							score , 
							cash )
			);
		}
		System.out.print(tracer.toString());
		
		assumedStates = Collections.singletonList(1);
		tracer = new Tracer();
		tracer.setIterationNumber(++iteration);
		tracer.setUserId(1);
		tracer.setQuestionId(1L);
		tracer.setTargetState(1);
		tracer.setAssumptionIds(assumptions);
		tracer.setAssumedStates(assumedStates);
		tracer.setEditLimit(engine.getEditLimits(tracer.getUserId(), tracer.getQuestionId(), tracer.getTargetState(), tracer.getAssumptionIds(), tracer.getAssumedStates()));
		newValues = new ArrayList<Float>(2);
		newValues.add(0.8f); newValues.add(0.2f); 
		tracer.setTargetProb(newValues);
		engine.addTrade(null, new Date(), "a trade that will generate arc 1<--4", tracer.getUserId(), tracer.getQuestionId(), tracer.getTargetProb() , tracer.getAssumptionIds(), tracer.getAssumedStates() , true);
		tracer.setProbLists(engine.getProbLists(null, null, null));
		// store all users' score and cash
		for (Long usr : engine.getUserToAssetAwareAlgorithmMap().keySet()) {
			float score = engine.scoreUserEv(usr, null, null);
			float cash  = engine.getCash(usr, null, null);
			tracer.getUserScoreAndCash().add(
					tracer.new UserScoreAndCash(
							usr, 
							score , 
							cash )
			);
		}
		System.out.print(tracer.toString());
		
		// make a trade that will supposedly generate dependency arc 1<--3
		assumptions = Collections.singletonList(3L);
		assumedStates = Collections.singletonList(0);
		tracer = new Tracer();
		tracer.setIterationNumber(++iteration);
		tracer.setUserId(1);
		tracer.setQuestionId(1L);
		tracer.setTargetState(1);
		tracer.setAssumptionIds(assumptions);
		tracer.setAssumedStates(assumedStates);
		tracer.setEditLimit(engine.getEditLimits(tracer.getUserId(), tracer.getQuestionId(), tracer.getTargetState(), tracer.getAssumptionIds(), tracer.getAssumedStates()));
		newValues = new ArrayList<Float>(2);
		newValues.add(0.7f); newValues.add(0.3f); 
		tracer.setTargetProb(newValues);
		engine.addTrade(null, new Date(), "a trade that will generate arc 1<--3", tracer.getUserId(), tracer.getQuestionId(), tracer.getTargetProb() , tracer.getAssumptionIds(), tracer.getAssumedStates() , true);
		tracer.setProbLists(engine.getProbLists(null, null, null));
		// store all users' score and cash
		for (Long usr : engine.getUserToAssetAwareAlgorithmMap().keySet()) {
			float score = engine.scoreUserEv(usr, null, null);
			float cash  = engine.getCash(usr, null, null);
			tracer.getUserScoreAndCash().add(
					tracer.new UserScoreAndCash(
							usr, 
							score , 
							cash )
			);
		}
		System.out.print(tracer.toString());
		
		assumedStates = Collections.singletonList(1);
		tracer = new Tracer();
		tracer.setIterationNumber(++iteration);
		tracer.setUserId(1);
		tracer.setQuestionId(1L);
		tracer.setTargetState(1);
		tracer.setAssumptionIds(assumptions);
		tracer.setAssumedStates(assumedStates);
		tracer.setEditLimit(engine.getEditLimits(tracer.getUserId(), tracer.getQuestionId(), tracer.getTargetState(), tracer.getAssumptionIds(), tracer.getAssumedStates()));
		newValues = new ArrayList<Float>(2);
		newValues.add(0.4f); newValues.add(0.6f); 
		tracer.setTargetProb(newValues);
		engine.addTrade(null, new Date(), "a trade that will generate arc 1<--3", tracer.getUserId(), tracer.getQuestionId(), tracer.getTargetProb() , tracer.getAssumptionIds(), tracer.getAssumedStates() , true);
		tracer.setProbLists(engine.getProbLists(null, null, null));
		// store all users' score and cash
		for (Long usr : engine.getUserToAssetAwareAlgorithmMap().keySet()) {
			float score = engine.scoreUserEv(usr, null, null);
			float cash  = engine.getCash(usr, null, null);
			tracer.getUserScoreAndCash().add(
					tracer.new UserScoreAndCash(
							usr, 
							score , 
							cash )
			);
		}
		System.out.print(tracer.toString());
		
		// resolve 2
		// the tracer format requires us to make a trade before resolving the question...
		tracer = new Tracer();
		tracer.setIterationNumber(++iteration);
		tracer.setUserId(1);
		tracer.setQuestionId(2L);
		tracer.setTargetState(0);
		tracer.setEditLimit(engine.getEditLimits(tracer.getUserId(), tracer.getQuestionId(), tracer.getTargetState(), tracer.getAssumptionIds(), tracer.getAssumedStates()));
		tracer.setTargetProb(engine.getProbList(tracer.getQuestionId(), tracer.getAssumptionIds(), tracer.getAssumedStates()));
//		engine.addTrade(null, new Date(), "A trade in 3 that is not supposed to change anything", tracer.getUserId(), tracer.getQuestionId(), tracer.getTargetProb() , tracer.getAssumptionIds(), tracer.getAssumedStates() , true);
		tracer.setProbLists(engine.getProbLists(null, null, null));
		// store all users' score and cash
		for (Long usr : engine.getUserToAssetAwareAlgorithmMap().keySet()) {
			float score = engine.scoreUserEv(usr, null, null);
			float cash  = engine.getCash(usr, null, null);
			tracer.getUserScoreAndCash().add(
					tracer.new UserScoreAndCash(
							usr, 
							score , 
							cash )
					);
		}
		tracer.setToResolveQuestion(true);
		tracer.setResolvedState(tracer.getTargetState());
		engine.resolveQuestion(null, new Date(), tracer.getQuestionId(), tracer.getResolvedState());
		tracer.setProbListsAfterResolution(engine.getProbLists(null,null,null));
		// store all users' score and cash after resolution
		for (Long usr : engine.getUserToAssetAwareAlgorithmMap().keySet()) {
			float score = engine.scoreUserEv(usr, null, null);
			float cash  = engine.getCash(usr, null, null);
			tracer.getUserScoreAndCashAfterResolution().add(
					tracer.new UserScoreAndCash(
							usr, 
							score , 
							cash )
					);
		}
		System.out.print(tracer.toString());
		
		Map<Long, List<Float>> oldProbs = engine.getProbLists(null, null, null);
		
		// check that we can create additional node and arc with no problem
		transactionKey = engine.startNetworkActions();
		engine.addQuestion(transactionKey, new Date(), 5, 2, null);
		engine.addQuestionAssumption(transactionKey, new Date(), 5, Collections.singletonList(1L),null);
		engine.commitNetworkActions(transactionKey);
		assertNotNull(engine.getProbList(5, null, null));
		assertEquals(0.5f, engine.getProbList(5, null, null).get(0), PROB_ERROR_MARGIN);
		assertEquals(0.5f, engine.getProbList(5, null, null).get(1), PROB_ERROR_MARGIN);
		// make sure to print trace
		tracer = new Tracer();
		tracer.getAddedQuestions().add(5L);
		tracer.getAddedQuestionsStateSize().add(2);
		tracer.setIterationNumber(++iteration);
		tracer.setUserId(1);
		tracer.setQuestionId(5L);
		tracer.setTargetState(0);
		tracer.setEditLimit(engine.getEditLimits(tracer.getUserId(), tracer.getQuestionId(), tracer.getTargetState(), tracer.getAssumptionIds(), tracer.getAssumedStates()));
		// use a "dummy" trade that won't change probability
		tracer.setTargetProb(engine.getProbList(tracer.getQuestionId(), tracer.getAssumptionIds(), tracer.getAssumedStates()));
		tracer.setProbLists(engine.getProbLists(null, null, null));
		// store all users' score and cash
		for (Long usr : engine.getUserToAssetAwareAlgorithmMap().keySet()) {
			float score = engine.scoreUserEv(usr, null, null);
			float cash  = engine.getCash(usr, null, null);
			tracer.getUserScoreAndCash().add(
					tracer.new UserScoreAndCash(
							usr, 
							score , 
							cash )
					);
		}
		System.out.print(tracer.toString());
		
		// check that probability did not change
		Map<Long, List<Float>> newProbs = engine.getProbLists(null, null, null);
		assertEquals(newProbs.toString()+ " ; " + oldProbs.toString(), oldProbs.size(), newProbs.size()-1);
		for (Long id : oldProbs.keySet()) {
			if (id.longValue() == 1L) {continue;};	// ignore 1 because we traded onit
			List<Float> oldProb = oldProbs.get(id);
			List<Float> newProb = newProbs.get(id);
			assertEquals(newProbs.toString()+ " ; " + oldProbs.toString(), oldProb.size(), newProb.size());
			for (int i = 0; i < oldProb.size(); i++) {
				assertEquals(newProbs.toString()+ " ; " + oldProbs.toString(), oldProb.get(i), newProb.get(i), PROB_ERROR_MARGIN);
			}
		}
		
		
		// resolve 1
		
		// get prob conditioned to 1 = 0, then resolve 1 to 0, and compare posterior prob
		oldProbs = engine.getProbLists(null, Collections.singletonList(1L), Collections.singletonList(0));
		
		tracer = new Tracer();
		tracer.setIterationNumber(++iteration);
		tracer.setUserId(1);
		tracer.setQuestionId(1L);
		tracer.setTargetState(0);
		tracer.setEditLimit(engine.getEditLimits(tracer.getUserId(), tracer.getQuestionId(), tracer.getTargetState(), tracer.getAssumptionIds(), tracer.getAssumedStates()));
		// do a "virtual" trade that won't change probability
		tracer.setTargetProb(engine.getProbList(tracer.getQuestionId(), tracer.getAssumptionIds(), tracer.getAssumedStates()));
		tracer.setProbLists(engine.getProbLists(null, null, null));
		// store all users' score and cash
		for (Long usr : engine.getUserToAssetAwareAlgorithmMap().keySet()) {
			float score = engine.scoreUserEv(usr, null, null);
			float cash  = engine.getCash(usr, null, null);
			tracer.getUserScoreAndCash().add(
					tracer.new UserScoreAndCash(
							usr, 
							score , 
							cash )
					);
		}
		tracer.setToResolveQuestion(true);
		tracer.setResolvedState(tracer.getTargetState());
		engine.resolveQuestion(null, new Date(), tracer.getQuestionId(), tracer.getResolvedState());
		tracer.setProbListsAfterResolution(engine.getProbLists(null,null,null));
		// store all users' score and cash after resolution
		for (Long usr : engine.getUserToAssetAwareAlgorithmMap().keySet()) {
			float score = engine.scoreUserEv(usr, null, null);
			float cash  = engine.getCash(usr, null, null);
			tracer.getUserScoreAndCashAfterResolution().add(
					tracer.new UserScoreAndCash(
							usr, 
							score , 
							cash )
					);
		}
		System.out.print(tracer.toString());
		
		// check that old prob conditioned to 1 = 0 is equal to new prob after resolving 1 = 0
		newProbs = engine.getProbLists(null, null, null);
		assertEquals(newProbs.toString()+ " ; " + oldProbs.toString(), oldProbs.size() - 1, newProbs.size());
		for (Long id : newProbs.keySet()) {
			if (id.longValue() == 1L) {continue;};	// ignore 1 because we traded onit
			List<Float> oldProb = oldProbs.get(id);
			List<Float> newProb = newProbs.get(id);
			assertEquals(newProbs.toString()+ " ; " + oldProbs.toString(), oldProb.size(), newProb.size());
			for (int i = 0; i < oldProb.size(); i++) {
				assertEquals(newProbs.toString()+ " ; " + oldProbs.toString(), oldProb.get(i), newProb.get(i), PROB_ERROR_MARGIN);
			}
		}
	}
	
	/**
	 * Test method to be used to check a condition when trades on P(A|B), P(B|C), P(C|D), then P(D|A)
	 */
	public final void testLoop() {
		// prepare the tracer
		Tracer tracer = new Tracer();
		
		int iteration = 0;
		tracer.setIterationNumber(iteration);
		
		// generate 4 nodes with 2-5 states
		long transactionKey = engine.startNetworkActions();
		tracer.getAddedQuestions().add(0L);
		tracer.getAddedQuestionsStateSize().add(2);
		tracer.getAddedQuestions().add(1L);
		tracer.getAddedQuestionsStateSize().add(3);
		tracer.getAddedQuestions().add(2L);
		tracer.getAddedQuestionsStateSize().add(4);
		tracer.getAddedQuestions().add(3L);
		tracer.getAddedQuestionsStateSize().add(5);
		assertEquals(tracer.getAddedQuestions().size(), tracer.getAddedQuestionsStateSize().size());
		for (int i = 0; i < tracer.getAddedQuestions().size(); i++) {
			engine.addQuestion(transactionKey, new Date(), tracer.getAddedQuestions().get(i), tracer.getAddedQuestionsStateSize().get(i), null);
		}
		// connect 0->1->2->3<-0
		engine.addQuestionAssumption(transactionKey, new Date(), 1, Collections.singletonList(0L), null);
		engine.addQuestionAssumption(transactionKey, new Date(), 2, Collections.singletonList(1L), null);
		engine.addQuestionAssumption(transactionKey, new Date(), 3, Collections.singletonList(2L), null);
		engine.addQuestionAssumption(transactionKey, new Date(), 3, Collections.singletonList(0L), null);
		// do the change
		engine.commitNetworkActions(transactionKey);
		
		// make sure user has enough assets
		tracer.setUserId(1L);
		tracer.setAddedCash(1000f);
		engine.addCash(null, new Date(), tracer.getUserId(), tracer.getAddedCash(), "");
		
		// prepare trade P(0|1)
		tracer.setQuestionId(0L);
		tracer.setAssumptionIds(Collections.singletonList(1L));
		tracer.setAssumedStates(Collections.singletonList(0));
		List<Float> newValues = new ArrayList<Float>();
		newValues.add(.9f); newValues.add(.1f);
		tracer.setTargetProb(newValues);
		tracer.setTargetState(0);
		tracer.setEditLimit(engine.getEditLimits(tracer.getUserId(), tracer.getQuestionId(), tracer.getTargetState(), tracer.getAssumptionIds(), tracer.getAssumedStates()));
		// make the trade
		engine.addTrade(null, new Date(), "", tracer.getUserId(), tracer.getQuestionId(), tracer.getTargetProb(), tracer.getAssumptionIds(), tracer.getAssumedStates(), true);
		// collect prob, score and cash after the trade
		tracer.setProbLists(engine.getProbLists(null, null, null));	// marginals after trade
		// store all users' score and cash
		for (Long usr : engine.getUserToAssetAwareAlgorithmMap().keySet()) {
			float score = engine.scoreUserEv(usr, null, null);
			float cash  = engine.getCash(usr, null, null);
			tracer.getUserScoreAndCash().add(
					tracer.new UserScoreAndCash(
							usr, 
							score , 
							cash )
					);
		}
		
		// print trace and then reset it
		System.out.print(tracer.toString());
		tracer = new Tracer();
		tracer.setIterationNumber(++iteration);
		
		// prepare trade P(1|2) 
		tracer.setUserId(1L);
		tracer.setQuestionId(1L);
		tracer.setAssumptionIds(Collections.singletonList(2L));
		tracer.setAssumedStates(Collections.singletonList(0));
		newValues = new ArrayList<Float>();
		newValues.add(.1f); newValues.add(.2f); newValues.add(.7f);
		tracer.setTargetProb(newValues);
		tracer.setTargetState(0);
		tracer.setEditLimit(engine.getEditLimits(tracer.getUserId(), tracer.getQuestionId(), tracer.getTargetState(), tracer.getAssumptionIds(), tracer.getAssumedStates()));
		// make the trade
		engine.addTrade(null, new Date(), "", tracer.getUserId(), tracer.getQuestionId(), tracer.getTargetProb(), tracer.getAssumptionIds(), tracer.getAssumedStates(), true);
		// collect prob, score and cash after the trade
		tracer.setProbLists(engine.getProbLists(null, null, null));	// marginals after trade
		// store all users' score and cash
		for (Long usr : engine.getUserToAssetAwareAlgorithmMap().keySet()) {
			float score = engine.scoreUserEv(usr, null, null);
			float cash  = engine.getCash(usr, null, null);
			tracer.getUserScoreAndCash().add(
					tracer.new UserScoreAndCash(
							usr, 
							score , 
							cash )
					);
		}
		// print trace and then reset it
		System.out.print(tracer.toString());
		tracer = new Tracer();
		tracer.setIterationNumber(++iteration);
		
		// prepare trade P(2|3) 
		tracer.setUserId(1L);
		tracer.setQuestionId(2L);
		tracer.setAssumptionIds(Collections.singletonList(3L));
		tracer.setAssumedStates(Collections.singletonList(0));
		newValues = new ArrayList<Float>();
		newValues.add(.5f); newValues.add(.2f); newValues.add(.1f); newValues.add(.2f);
		tracer.setTargetProb(newValues);
		tracer.setTargetState(0);
		tracer.setEditLimit(engine.getEditLimits(tracer.getUserId(), tracer.getQuestionId(), tracer.getTargetState(), tracer.getAssumptionIds(), tracer.getAssumedStates()));
		// make the trade
		engine.addTrade(null, new Date(), "", tracer.getUserId(), tracer.getQuestionId(), tracer.getTargetProb(), tracer.getAssumptionIds(), tracer.getAssumedStates(), true);
		// collect prob, score and cash after the trade
		tracer.setProbLists(engine.getProbLists(null, null, null));	// marginals after trade
		// store all users' score and cash
		for (Long usr : engine.getUserToAssetAwareAlgorithmMap().keySet()) {
			float score = engine.scoreUserEv(usr, null, null);
			float cash  = engine.getCash(usr, null, null);
			tracer.getUserScoreAndCash().add(
					tracer.new UserScoreAndCash(
							usr, 
							score , 
							cash )
					);
		}
		// print trace and then reset it
		System.out.print(tracer.toString());
		tracer = new Tracer();
		tracer.setIterationNumber(++iteration);
		
		// prepare trade P(3|0)
		tracer.setUserId(1L);
		tracer.setQuestionId(3L);
		tracer.setAssumptionIds(Collections.singletonList(0L));
		tracer.setAssumedStates(Collections.singletonList(0));
		newValues = new ArrayList<Float>();
		newValues.add(.1f); newValues.add(.3f); newValues.add(.2f); newValues.add(.3f); newValues.add(.1f); 
		tracer.setTargetProb(newValues);
		tracer.setTargetState(0);
		tracer.setEditLimit(engine.getEditLimits(tracer.getUserId(), tracer.getQuestionId(), tracer.getTargetState(), tracer.getAssumptionIds(), tracer.getAssumedStates()));
		// make the trade
		engine.addTrade(null, new Date(), "", tracer.getUserId(), tracer.getQuestionId(), tracer.getTargetProb(), tracer.getAssumptionIds(), tracer.getAssumedStates(), true);
		// collect prob, score and cash after the trade
		tracer.setProbLists(engine.getProbLists(null, null, null));	// marginals after trade
		// store all users' score and cash
		for (Long usr : engine.getUserToAssetAwareAlgorithmMap().keySet()) {
			float score = engine.scoreUserEv(usr, null, null);
			float cash  = engine.getCash(usr, null, null);
			tracer.getUserScoreAndCash().add(
					tracer.new UserScoreAndCash(
							usr, 
							score , 
							cash )
					);
		}
		
		// resolve 3
		tracer.setToResolveQuestion(true);
		tracer.setResolvedState(tracer.getTargetState());
		engine.resolveQuestion(null, new Date(), tracer.getQuestionId(), tracer.getResolvedState());
		
		// collect prob, score and cash after the resolution
		tracer.setProbListsAfterResolution(engine.getProbLists(null, null, null));
		// store all users' score and cash after resolution
		for (Long usr : engine.getUserToAssetAwareAlgorithmMap().keySet()) {
			float score = engine.scoreUserEv(usr, null, null);
			float cash  = engine.getCash(usr, null, null);
			tracer.getUserScoreAndCashAfterResolution().add(
					tracer.new UserScoreAndCash(
							usr, 
							score , 
							cash )
					);
		}
		
		// print trace and then reset it
		System.out.print(tracer.toString());
		tracer = new Tracer();
		tracer.setIterationNumber(++iteration);
		
		// make a dummy trade (a trade that won't change anything) on 0 and resolve it
		tracer.setUserId(1L);
		tracer.setQuestionId(0L);
		tracer.setTargetProb(engine.getProbList(0, null, null));	// "change" the marginal to current marginal
		tracer.setTargetState(0);
		tracer.setEditLimit(engine.getEditLimits(tracer.getUserId(), tracer.getQuestionId(), tracer.getTargetState(), tracer.getAssumptionIds(), tracer.getAssumedStates()));
		// collect prob, score and cash after the "virtual" trade (unchanged)
		tracer.setProbLists(engine.getProbLists(null, null, null));	// marginals after trade
		// store all users' score and cash
		for (Long usr : engine.getUserToAssetAwareAlgorithmMap().keySet()) {
			float score = engine.scoreUserEv(usr, null, null);
			float cash  = engine.getCash(usr, null, null);
			tracer.getUserScoreAndCash().add(
					tracer.new UserScoreAndCash(
							usr, 
							score , 
							cash )
					);
		}
		
		// resolve 0
		tracer.setToResolveQuestion(true);
		tracer.setResolvedState(tracer.getTargetState());
		engine.resolveQuestion(null, new Date(), tracer.getQuestionId(), tracer.getResolvedState());
		
		// collect prob, score and cash after the resolution
		tracer.setProbListsAfterResolution(engine.getProbLists(null, null, null));
		// store all users' score and cash after resolution
		for (Long usr : engine.getUserToAssetAwareAlgorithmMap().keySet()) {
			float score = engine.scoreUserEv(usr, null, null);
			float cash  = engine.getCash(usr, null, null);
			tracer.getUserScoreAndCashAfterResolution().add(
					tracer.new UserScoreAndCash(
							usr, 
							score , 
							cash )
					);
		}
		
		// print trace
		System.out.print(tracer.toString());
		
		
	}
	/**
	 * Test method to be used for performance measurement
	 */
	public final void testPerformanceComplexNet() {
		// initialize parameters
		int numNodes = 2000;				// total quantity of nodes in the network
		int maxNumStates = 3;				// how many states a node can have at most. The minimum is expected to be 2, but this var should not be set below 2
		int maxNumParents = 10;				// maximum number of parents per node 
		float probTrade = 1f;				// how many nodes will be traded
		int maxUsers = 200;					// how many users to create
		boolean isToMakeAllTrades = true;	// if false, trades will be cancelled if they are taking too much time
		float probAddAssumption = 0.2f;		// what is the probability to add assumptions in trades. The higher, more assumptions are likely to be used.
		long seed = new Date().getTime();	// change this seed to fixed value if you want to replicate something
		
		String fileName = new Date().toString().replace(":", "").replace(' ', '_').trim()+"_seed" + seed;
		File net = new File(fileName + ".net");		// file to store generated net structure
		File matrix = new File(fileName + ".net.txt");	// file to store generated net structure as a matrix which will be loaded in matlab
		
		
		Random random = new Random(seed);	
		
		// stream where traces will be written to
		PrintStream out = null;
		try {
			out = new PrintStream(new FileOutputStream(new File(fileName  + ".out.txt")));
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
		
		Debug.setDebug(true);
		
		System.out.println("[test"+ numNodes + "Vars"+maxNumStates+"StatesTW"+maxNumParents+"10] Random seed = " + seed);
		
		Tracer tracer = new Tracer();
		
		// create the nodes
		long transactionKey = engine.startNetworkActions();
		for (int i = 0; i < numNodes; i++) {
			int numStatesToCreate = 2;	// how many states the new question (node) will have
			if (maxNumStates > 2) {
				// random quantity of states: from 2 to maxNumStates
				numStatesToCreate = 2+random.nextInt(maxNumStates-1);
			}
			engine.addQuestion(transactionKey, new Date(), i, numStatesToCreate, null);
			tracer.getAddedQuestions().add((long) i);
			tracer.getAddedQuestionsStateSize().add(numStatesToCreate);
		}
		
		// create arcs randomly
		for (int i = maxNumParents; i < numNodes; i+=maxNumParents) {
			List<Long> parentIds = new ArrayList<Long>(maxNumParents);
			for (int j = 0; j < maxNumParents; j++) {
				parentIds.add((long) (i-(1+j)));
			}
			engine.addQuestionAssumption(transactionKey, new Date(), i, parentIds, null);
		}
		// connect the last few questions (the rest which did not complete maxNumParent nodes)
		// there are (numNodes-1) % maxNumParents remaining unconnected nodes
		if ((numNodes-1) % maxNumParents != 0) {
			// there are remaining nodes
			List<Long> remainingNodesIds = new ArrayList<Long>((numNodes-1) % maxNumParents);
			for (long i = 0; i < ((numNodes-1) % maxNumParents); i++) {
				remainingNodesIds.add(numNodes-(2+i));
			}
			engine.addQuestionAssumption(transactionKey, new Date(), (numNodes-1), remainingNodesIds, null);
		}
		
		
		engine.commitNetworkActions(transactionKey);
		
		System.out.println(engine.getNetStatistics().toString());
		System.out.println("seed = " + seed);
		
		// check that the marginals are initialized
		for (Entry<Long, List<Float>> entry : engine.getProbLists(null, null, null).entrySet()) {
			for (int j = 0; j < entry.getValue().size(); j++) {
				assertEquals(entry.toString(),1f/entry.getValue().size(), entry.getValue().get(j), PROB_ERROR_MARGIN);
			}
		}
		
		// store current time in order to see how long the trades took
		long timeMillis = System.currentTimeMillis();
		
		// make trades on all variables
		long numTrades = 0;	// how many trades were performed (this can indicate how many iterations we performed)
		for (long questionId = 0; questionId < numNodes; questionId++) {
			ProbabilisticNode node = (ProbabilisticNode) engine.getProbabilisticNetwork().getNode(Long.toString(questionId));
			assertNotNull(node);
			
			if (!node.getParentNodes().isEmpty()) {
				if (random.nextFloat() > probTrade) {
					continue;
				}
			}
			// the trade to be performed
			List<Float> newValues = new ArrayList<Float>(node.getStatesSize());
			float sumForNormalization = 0f;
			for (int i = 0; i < node.getStatesSize(); i++) {
				newValues.add(random.nextFloat());
				sumForNormalization += newValues.get(i);
			}
			// normalize the newValues
			for (int i = 0; i < newValues.size(); i++) {
				newValues.set(i, newValues.get(i)/sumForNormalization);
			}
			
			List<Long> assumptionIds = new ArrayList<Long>(node.getParentNodes().size());
			List<Integer> assumedStates = new ArrayList<Integer>(node.getParentNodes().size());
			
			// randomly select assumptions
			for (INode parent : node.getParentNodes()) {
				// use prob to add assumption
				if (random.nextFloat() < probAddAssumption) {
					assumptionIds.add(Long.parseLong(parent.getName()));
					assumedStates.add(random.nextInt(parent.getStatesSize()));	// random state from 0 to parent.getStatesSize()-1
				}
			}
			
			long userID = Math.abs(random.nextLong()) % maxUsers;
			
			// give additional cash to user
			float manna = (float) (4000f + 6000f*random.nextDouble());
			engine.addCash(null, new Date(), userID, manna, "Making sure user has enough cash.");
			
			long time = System.currentTimeMillis();
			engine.addTrade(
					null, 
					new Date(), 
					"P(" + questionId + " | " + assumptionIds + "=" + assumedStates+") = " + newValues, 
					userID, 
					questionId, 
					newValues, 
					assumptionIds, 
					assumedStates, 
					true
					);
			
			// set up tracer accordingly to the operations I made
			tracer.setIterationNumber((int) numTrades);
			tracer.setUserId(userID);
			tracer.setAddedCash(manna);
			tracer.setQuestionId(questionId);
			tracer.setAssumptionIds(assumptionIds);
			tracer.setAssumedStates(assumedStates);
			tracer.setTargetProb(newValues);
			tracer.setTargetState(0);
			tracer.setEditLimit(engine.getEditLimits(userID, questionId, tracer.getTargetState(), assumptionIds, assumedStates));
			tracer.setProbLists(engine.getProbLists(null, null, null));	// marginals after trade
			// store all users' score and cash
			for (Long usr : engine.getUserToAssetAwareAlgorithmMap().keySet()) {
				float score = engine.scoreUserEv(usr, null, null);
				float cash  = engine.getCash(usr, null, null);
				tracer.getUserScoreAndCash().add(
						tracer.new UserScoreAndCash(
								usr, 
								score , 
								cash )
						);
			}
			
			// print trace and then reset it
			out.print(tracer.toString());
			tracer = new Tracer();
			
			numTrades++;	// next iteration is next trade
			
			if (!isToMakeAllTrades && System.currentTimeMillis() - time >= 1000) {
				// do not add more trades if it is more than 1s
				break;
			}
			if (!isToMakeAllTrades && System.currentTimeMillis() - time >= 100) {
				// trade on less questions if its more than half second
				probTrade = probTrade/2;
			}
		}
		
		System.out.println("Number of trades = " + numTrades);
		System.out.println("Elapsed time for trades (ms) = " + (System.currentTimeMillis() - timeMillis));
		
		// backup marginals, so that we can test them later
		Map<Long, List<Float>> marginals = engine.getProbLists(null, null, null);
		assertEquals(numNodes, marginals.size());
		
		System.out.println(engine.getNetStatistics().toString());
		System.out.println("seed = " + seed);
		
		try {
			// save net to a NET file
			engine.exportNetwork(net);
			// save net as a matrix (text file) too
			new BNToChildrenMatrixConverter().save(matrix, engine.getProbabilisticNetwork());
		} catch (Exception e) {
			out.close();
			throw new RuntimeException(e);
		}
		
		// check time to store network as a string
		long timeBefore = System.currentTimeMillis();
		String netAsString = engine.exportState();
		System.out.println("Time (ms) to export network = " + (System.currentTimeMillis() - timeBefore));
		System.out.println("seed = " + seed);
		
		assertNotNull(netAsString);
		assertFalse(netAsString.trim().isEmpty());
		
//		System.out.println(netAsString);
		
		// check time to load network from the obtained string
		timeBefore = System.currentTimeMillis();
		engine.importState(netAsString);
		System.out.println("Time (ms) to import network = " + (System.currentTimeMillis() - timeBefore));
		System.out.println("seed = " + seed);
		
		// will check if the loaded network matches previous network
		Map<Long, List<Float>> newMarginals = engine.getProbLists(null, null, null);
		
		// check number of nodes
		assertEquals(numNodes, newMarginals.size());
		
		// check if marginals matches the ones before export
		for (Entry<Long, List<Float>> oldEntry : marginals.entrySet()) {
			List<Float> oldMarginal = oldEntry.getValue();
			List<Float> newMarginal = newMarginals.get(oldEntry.getKey());
			// check non null and size of marginals
			assertNotNull(marginals.toString() + " ; " + newMarginals, oldMarginal);
			assertNotNull(marginals.toString() + " ; " + newMarginals, newMarginal);
			assertEquals(marginals.toString() + " ; " + newMarginals, oldMarginal.size(), newMarginal.size());
			// check content of marginals
			for (int i = 0; i < newMarginal.size(); i++) {
				assertEquals(marginals.toString() + " ; " + newMarginals, oldMarginal.get(i), newMarginal.get(i), PROB_ERROR_MARGIN);
			}
		}
		
		out.close();
	}

}
