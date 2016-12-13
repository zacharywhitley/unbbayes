package utils;
import io.IModelCenterWrapperIO;
import io.ModelCenterMatrixStyleWrapperIO;
import io.ModelCenterWrapperIO;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import unbbayes.prs.INode;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.util.Debug;


/**
 * Wraps {@link SimulatedUserStatisticsCalculator} and {@link DirichletUserSimulator}.
 * {@link JavaSimulatorWrapper#main(String[])} virtually runs
 * {@link DirichletUserSimulator#main(String[])} and then {@link SimulatedUserStatisticsCalculator#main(String[])}
 * @author Shou Matsumoto
 *
 */
public class JavaSimulatorWrapper extends SimulatedUserStatisticsCalculator {
	private static int maxNumAttempt = 1000;
	
	public static final String NUMBER_INDICATORS_PROPERTY_NAME = "Number_of_Indicators";
	public static final String NUMBER_DETECTORS_PROPERTY_NAME = "Number_of_Detectors";
	public static final String NUMBER_USERS_PROPERTY_NAME = "Number_of_Users";
	public static final String TYPE_FUSION_PROPERTY_NAME = "Type_of_Fusion";
	public static final String PROBABILITIES_PROPERTY_NAME_PREFIX = "Probabilities";
	public static final String CLIQUE_PROPERTY_NAME_PREFIX = "Clique";
	public static final String HAS_ALERT_IN_PROB_PROPERTY_NAME = "Has_Alert_In_Prob";
	public static final String NUMBER_OF_RUNS_PROPERTY_NAME = "Number_of_Runs";
	
	private String numberOfIndicatorsPropertyName = NUMBER_INDICATORS_PROPERTY_NAME;
	private String numberOfDetectorsPropertyName = NUMBER_DETECTORS_PROPERTY_NAME;
	private String numberOfUsersPropertyName = NUMBER_USERS_PROPERTY_NAME;
	private String typeOfFusionPropertyName = TYPE_FUSION_PROPERTY_NAME;
	private String probabilitiesPropertyNamePrefix = PROBABILITIES_PROPERTY_NAME_PREFIX;
	private String hasAlertInProbPropertyName = HAS_ALERT_IN_PROB_PROPERTY_NAME;
	private String numberOfRunsPropertyName = NUMBER_OF_RUNS_PROPERTY_NAME;
	private String cliquesPropertyNamePrefix = CLIQUE_PROPERTY_NAME_PREFIX;

	private String cliquesFileName = "cliques.json";
	
	private SimulatedUserStatisticsCalculator simulatedUserStatisticsCalculator = new SimulatedUserStatisticsCalculator();
	private DirichletUserSimulator dirichletUserSimulator = new DirichletUserSimulator();
	
	
	private int numIndicators = -1;
	private int numDetectors = -1;
	
	private IModelCenterWrapperIO io = null;
	private List<List<Float>> wrapperProbabilities = new ArrayList<List<Float>>();

	private List<String> cliqueNames;


	
	
	/**
	 * Default constructor is kept protected to allow inheritance, 
	 * but it's not public in order to allow instantiation method to be changed without conscience of callers.
	 */
	protected JavaSimulatorWrapper() {
		this.setInput(new File("JavaSimulatorWrapper.in"));
		this.setOutput(new File("JavaSimulatorWrapper.out"));
		this.setNumOrganization(1);
	}

	/**
	 * Default constructor method
	 * @return a new instance
	 */
	public static JavaSimulatorWrapper getInstance() {
		return new JavaSimulatorWrapper();
	}
	
	/**
	 * @return the simulatedUserStatisticsCalculator
	 */
	public SimulatedUserStatisticsCalculator getSimulatedUserStatisticsCalculator() {
		return simulatedUserStatisticsCalculator;
	}

	/**
	 * @param simulatedUserStatisticsCalculator the simulatedUserStatisticsCalculator to set
	 */
	public void setSimulatedUserStatisticsCalculator(
			SimulatedUserStatisticsCalculator simulatedUserStatisticsCalculator) {
		this.simulatedUserStatisticsCalculator = simulatedUserStatisticsCalculator;
	}

	/**
	 * @return the dirichletUserSimulator
	 */
	public DirichletUserSimulator getDirichletUserSimulator() {
		return dirichletUserSimulator;
	}

	/**
	 * @param dirichletUserSimulator the dirichletUserSimulator to set
	 */
	public void setDirichletUserSimulator(DirichletUserSimulator dirichletUserSimulator) {
		this.dirichletUserSimulator = dirichletUserSimulator;
	}


	/**
	 * @param jointProbabilities
	 * @param output
	 * @param numSimulation
	 * @param isToPrintAlert
	 * @param alertScore
	 * @throws IOException
	 * @see DirichletUserSimulator#runSingleSimulation(java.util.List, java.io.File, int, boolean, int)
	 */
	public void runSingleSimulation(List<PotentialTable> jointProbabilities,
			File output, int numSimulation, boolean isToPrintAlert,
			int alertScore) throws IOException {
		this.getSimulatedUserStatisticsCalculator().runSingleSimulation(
				jointProbabilities, output, numSimulation, isToPrintAlert,
				alertScore);
	}

	/**
	 * @param variableMap
	 * @param vars
	 * @return
	 * @see ObjFunctionPrinter#getJointTable(java.util.Map, java.util.List)
	 */
	public PotentialTable getJointTable(Map<String, INode> variableMap,
			List<String> vars) {
		return this.getSimulatedUserStatisticsCalculator().getJointTable(
				variableMap, vars);
	}


	/**
	 * @param file
	 * @param numPopulationCorrelation
	 * @param numPopulationRCP
	 * @param isToPrintChiSquareHeader
	 * @throws IOException
	 * @see ExpectationPrinter#printExpectationFromFile(java.io.File, int, int, boolean)
	 */
	public void printExpectationFromFile(File file,
			int numPopulationCorrelation, int numPopulationRCP,
			boolean isToPrintChiSquareHeader) throws IOException {
		this.getSimulatedUserStatisticsCalculator().printExpectationFromFile(file,
				numPopulationCorrelation, numPopulationRCP,
				isToPrintChiSquareHeader);
	}

	/**
	 * @param variableMap
	 * @param matrix
	 * @param columnName
	 * @param rowName
	 * @return
	 * @see ObjFunctionPrinter#getPotentialTable(java.util.Map, int[][], java.lang.String, java.lang.String)
	 */
	public PotentialTable getPotentialTable(Map<String, INode> variableMap,
			int[][] matrix, String columnName, String rowName) {
		return this.getSimulatedUserStatisticsCalculator().getPotentialTable(
				variableMap, matrix, columnName, rowName);
	}

	/**
	 * @param variableMap
	 * @param correlations
	 * @param names
	 * @return
	 * @see ObjFunctionPrinter#getCorrelationTables(java.util.Map, int[][][], java.util.List)
	 */
	public List<PotentialTable> getCorrelationTables(
			Map<String, INode> variableMap, int[][][] correlations,
			List<String> names) {
		return this.getSimulatedUserStatisticsCalculator().getCorrelationTables(
				variableMap, correlations, names);
	}

	/**
	 * @param variableMap
	 * @param file
	 * @return
	 * @throws IOException
	 * @see ObjFunctionPrinter#getTablesFromNetFile(java.util.Map, java.io.File)
	 */
	public List<PotentialTable> getTablesFromNetFile(
			Map<String, INode> variableMap, File file) throws IOException {
		return this.getSimulatedUserStatisticsCalculator().getTablesFromNetFile(
				variableMap, file);
	}

	/**
	 * @param toParse
	 * @param query
	 * @see SimulatedUserStatisticsCalculator#fillQueriedVarAndStates(java.lang.String, SimulatedUserStatisticsCalculator.Query)
	 */
	public void fillQueriedVarAndStates(String toParse, Query query) {
		this.getSimulatedUserStatisticsCalculator().fillQueriedVarAndStates(toParse,
				query);
	}


	/**
	 * @param reference
	 * @param toReorder
	 * @see DirichletUserSimulator#reorderJointProbabilityTable(unbbayes.prs.bn.PotentialTable, unbbayes.prs.bn.PotentialTable)
	 */
	public void reorderJointProbabilityTable(PotentialTable reference,
			PotentialTable toReorder) {
		this.getSimulatedUserStatisticsCalculator().reorderJointProbabilityTable(
				reference, toReorder);
	}

	/**
	 * @param file
	 * @return
	 * @throws IOException
	 * @see SimulatedUserStatisticsCalculator#getJointProbabilityFromFile(java.io.File)
	 */
	public PotentialTable getJointProbabilityFromFile(File file)
			throws IOException {
		return this.getSimulatedUserStatisticsCalculator()
				.getJointProbabilityFromFile(file);
	}

	/**
	 * @param tables
	 * @see ExpectationPrinter#printFormattedTables(java.util.List)
	 */
	public void printFormattedTables(List<PotentialTable> tables) {
		this.getSimulatedUserStatisticsCalculator().printFormattedTables(tables);
	}

	/**
	 * @param table
	 * @see ExpectationPrinter#printFormattedTable(unbbayes.prs.bn.PotentialTable)
	 */
	public void printFormattedTable(PotentialTable table) {
		this.getSimulatedUserStatisticsCalculator().printFormattedTable(table);
	}

	/**
	 * @param variableMap
	 * @param tables
	 * @param indicatorNames
	 * @param threatName
	 * @return
	 * @see ObjFunctionPrinter#getThreatTables(java.util.Map, int[][][], java.util.List, java.lang.String)
	 */
	public List<PotentialTable> getThreatTables(Map<String, INode> variableMap,
			int[][][] tables, List<String> indicatorNames, String threatName) {
		return this.getSimulatedUserStatisticsCalculator().getThreatTables(
				variableMap, tables, indicatorNames, threatName);
	}

	/**
	 * @param observed
	 * @param expected
	 * @param isToUseAverageAsExpectedValue
	 * @return
	 * @see ExpectationPrinter#getChiSqure(unbbayes.prs.bn.PotentialTable, unbbayes.prs.bn.PotentialTable, boolean)
	 */
	public double getChiSqure(PotentialTable observed, PotentialTable expected,
			boolean isToUseAverageAsExpectedValue) {
		return this.getSimulatedUserStatisticsCalculator().getChiSqure(observed,
				expected, isToUseAverageAsExpectedValue);
	}

	/**
	 * @param variableMap
	 * @param tables
	 * @param indicatorNameList
	 * @param detectorNameList
	 * @return
	 * @see ObjFunctionPrinter#getDetectorTables(java.util.Map, int[][][], java.util.List, java.util.List)
	 */
	public List<PotentialTable> getDetectorTables(
			Map<String, INode> variableMap, int[][][] tables,
			List<String> indicatorNameList, List<String> detectorNameList) {
		return this.getSimulatedUserStatisticsCalculator().getDetectorTables(
				variableMap, tables, indicatorNameList, detectorNameList);
	}

	/**
	 * @throws IOException
	 * @see SimulatedUserStatisticsCalculator#run()
	 */
	public void run() throws IOException {
		this.getSimulatedUserStatisticsCalculator().run();
	}

	/**
	 * @param jointTable
	 * @return
	 * @see ObjFunctionPrinter#getJointTableDescription(unbbayes.prs.bn.PotentialTable)
	 */
	public String getJointTableDescription(PotentialTable jointTable) {
		return this.getSimulatedUserStatisticsCalculator()
				.getJointTableDescription(jointTable);
	}

	/**
	 * @param tables
	 * @param jointTable
	 * @see ExpectationPrinter#fillTablesFromJointProbability(java.util.List, unbbayes.prs.bn.PotentialTable)
	 */
	public void fillTablesFromJointProbability(List<PotentialTable> tables,
			PotentialTable jointTable) {
		this.getSimulatedUserStatisticsCalculator().fillTablesFromJointProbability(
				tables, jointTable);
	}

	/**
	 * @param primaryTables
	 * @param auxiliaryTables
	 * @param jointTable
	 * @return
	 * @see ObjFunctionPrinter#getObjFunction(java.util.List, java.util.List, unbbayes.prs.bn.PotentialTable)
	 */
	public String getObjFunction(List<PotentialTable> primaryTables,
			List<PotentialTable> auxiliaryTables, PotentialTable jointTable) {
		return this.getSimulatedUserStatisticsCalculator().getObjFunction(
				primaryTables, auxiliaryTables, jointTable);
	}

	/**
	 * @param file
	 * @param queries
	 * @throws IOException
	 * @see SimulatedUserStatisticsCalculator#fillQueriesFromFile(java.io.File, java.util.List)
	 */
	public void fillQueriesFromFile(File file, List<Query> queries)
			throws IOException {
		this.getSimulatedUserStatisticsCalculator().fillQueriesFromFile(file,
				queries);
	}

	/**
	 * @return
	 * @see DirichletUserSimulator#getNumUsers()
	 */
	public int getNumUsers() {
		return this.getSimulatedUserStatisticsCalculator().getNumUsers();
	}

	/**
	 * @param numUsers
	 * @see DirichletUserSimulator#setNumUsers(int)
	 */
	public void setNumUsers(int numUsers) {
		this.getSimulatedUserStatisticsCalculator().setNumUsers(numUsers);
	}

	/**
	 * @return
	 * @see DirichletUserSimulator#getNumOrganization()
	 */
	public int getNumOrganization() {
		return this.getSimulatedUserStatisticsCalculator().getNumOrganization();
	}

	/**
	 * @param numOrganization
	 * @see DirichletUserSimulator#setNumOrganization(int)
	 */
	public void setNumOrganization(int numOrganization) {
		this.getSimulatedUserStatisticsCalculator().setNumOrganization(numOrganization);
	}

//	/**
//	 * @return
//	 * @see DirichletUserSimulator#getInput()
//	 */
//	public File getInput() {
//		return this.getSimulatedUserStatisticsCalculator().getInput();
//	}
//
//	/**
//	 * @param input
//	 * @see DirichletUserSimulator#setInput(java.io.File)
//	 */
//	public void setInput(File input) {
//		this.getSimulatedUserStatisticsCalculator().setInput(input);
//	}

//	/**
//	 * @return
//	 * @see DirichletUserSimulator#getOutput()
//	 */
//	public File getOutput() {
//		return this.getSimulatedUserStatisticsCalculator().getOutput();
//	}
//
//	/**
//	 * @param output
//	 * @see DirichletUserSimulator#setOutput(java.io.File)
//	 */
//	public void setOutput(File output) {
//		this.getSimulatedUserStatisticsCalculator().setOutput(output);
//	}

	/**
	 * @return
	 * @see DirichletUserSimulator#isToPrintAlert()
	 */
	public boolean isToPrintAlert() {
		return this.getSimulatedUserStatisticsCalculator().isToPrintAlert();
	}

	/**
	 * @param isToPrintAlert
	 * @see DirichletUserSimulator#setToPrintAlert(boolean)
	 */
	public void setToPrintAlert(boolean isToPrintAlert) {
		this.getSimulatedUserStatisticsCalculator().setToPrintAlert(isToPrintAlert);
	}

	/**
	 * @return
	 * @see DirichletUserSimulator#getCountAlert()
	 */
	public int getCountAlert() {
		return this.getSimulatedUserStatisticsCalculator().getCountAlert();
	}

	/**
	 * @param countAlert
	 * @see DirichletUserSimulator#setCountAlert(int)
	 */
	public void setCountAlert(int countAlert) {
		this.getSimulatedUserStatisticsCalculator().setCountAlert(countAlert);
	}

	/**
	 * @return
	 * @see DirichletUserSimulator#isToUseDirichletMultinomial()
	 */
	public boolean isToUseDirichletMultinomial() {
		return this.getSimulatedUserStatisticsCalculator()
				.isToUseDirichletMultinomial();
	}

	/**
	 * @param isToUseDirichletMultinomial
	 * @see DirichletUserSimulator#setToUseDirichletMultinomial(boolean)
	 */
	public void setToUseDirichletMultinomial(boolean isToUseDirichletMultinomial) {
		this.getSimulatedUserStatisticsCalculator()
				.setToUseDirichletMultinomial(isToUseDirichletMultinomial);
	}

	/**
	 * @return
	 * @see ExpectationPrinter#getDefaultJointProbabilityInputFileName()
	 */
	public String getDefaultJointProbabilityInputFileName() {
		return this.getSimulatedUserStatisticsCalculator()
				.getDefaultJointProbabilityInputFileName();
	}

	/**
	 * @param defaultJointProbabilityInputFileName
	 * @see ExpectationPrinter#setDefaultJointProbabilityInputFileName(java.lang.String)
	 */
	public void setDefaultJointProbabilityInputFileName(
			String defaultJointProbabilityInputFileName) {
		this.getSimulatedUserStatisticsCalculator()
				.setDefaultJointProbabilityInputFileName(defaultJointProbabilityInputFileName);
	}

	/**
	 * @return
	 * @see ExpectationPrinter#getProbabilityColumnName()
	 */
	public String getProbabilityColumnName() {
		return this.getSimulatedUserStatisticsCalculator()
				.getProbabilityColumnName();
	}

	/**
	 * @param probabilityColumnName
	 * @see ExpectationPrinter#setProbabilityColumnName(java.lang.String)
	 */
	public void setProbabilityColumnName(String probabilityColumnName) {
		this.getSimulatedUserStatisticsCalculator()
				.setProbabilityColumnName(probabilityColumnName);
	}

	/**
	 * @return
	 * @see ExpectationPrinter#getColumnsToIgnore()
	 */
	public String[] getColumnsToIgnore() {
		return this.getSimulatedUserStatisticsCalculator().getColumnsToIgnore();
	}

	/**
	 * @param columnsToIgnore
	 * @see ExpectationPrinter#setColumnsToIgnore(java.lang.String[])
	 */
	public void setColumnsToIgnore(String[] columnsToIgnore) {
		this.getSimulatedUserStatisticsCalculator()
				.setColumnsToIgnore(columnsToIgnore);
	}

	/**
	 * @return
	 * @see ExpectationPrinter#isToPrintExpectationTable()
	 */
	public boolean isToPrintExpectationTable() {
		return this.getSimulatedUserStatisticsCalculator()
				.isToPrintExpectationTable();
	}

	/**
	 * @param isToPrintExpectationTable
	 * @see ExpectationPrinter#setToPrintExpectationTable(boolean)
	 */
	public void setToPrintExpectationTable(boolean isToPrintExpectationTable) {
		this.getSimulatedUserStatisticsCalculator()
				.setToPrintExpectationTable(isToPrintExpectationTable);
	}

	/**
	 * @param primaryTables
	 * @param auxiliaryTables
	 * @param jointTable
	 * @return
	 * @see ObjFunctionPrinter#get1WayLikelihoodSubtraction(java.util.List, java.util.List, unbbayes.prs.bn.PotentialTable)
	 */
	public String get1WayLikelihoodSubtraction(
			List<PotentialTable> primaryTables,
			List<PotentialTable> auxiliaryTables, PotentialTable jointTable) {
		return this.getSimulatedUserStatisticsCalculator()
				.get1WayLikelihoodSubtraction(primaryTables, auxiliaryTables,
						jointTable);
	}

	/**
	 * @return
	 * @see ExpectationPrinter#is1stLineForNames()
	 */
	public boolean is1stLineForNames() {
		return this.getSimulatedUserStatisticsCalculator().is1stLineForNames();
	}

	/**
	 * @param is1stLineForNames
	 * @see ExpectationPrinter#set1stLineForNames(boolean)
	 */
	public void set1stLineForNames(boolean is1stLineForNames) {
		super.set1stLineForNames(is1stLineForNames);
		if (this.getSimulatedUserStatisticsCalculator() != null) {
			this.getSimulatedUserStatisticsCalculator().set1stLineForNames(is1stLineForNames);
		}
	}

	/**
	 * @return
	 * @see ExpectationPrinter#getDefaultIndexOfProbability()
	 */
	public int getDefaultIndexOfProbability() {
		return this.getSimulatedUserStatisticsCalculator()
				.getDefaultIndexOfProbability();
	}

	/**
	 * @param defaultIndexOfProbability
	 * @see ExpectationPrinter#setDefaultIndexOfProbability(int)
	 */
	public void setDefaultIndexOfProbability(int defaultIndexOfProbability) {
		this.getSimulatedUserStatisticsCalculator()
				.setDefaultIndexOfProbability(defaultIndexOfProbability);
	}

	/**
	 * @return
	 * @see ExpectationPrinter#isToUseAverageAsExpected()
	 */
	public boolean isToUseAverageAsExpected() {
		return this.getSimulatedUserStatisticsCalculator()
				.isToUseAverageAsExpected();
	}

	/**
	 * @param isToUseAverageAsExpected
	 * @see ExpectationPrinter#setToUseAverageAsExpected(boolean)
	 */
	public void setToUseAverageAsExpected(boolean isToUseAverageAsExpected) {
		this.getSimulatedUserStatisticsCalculator()
				.setToUseAverageAsExpected(isToUseAverageAsExpected);
	}

	/**
	 * @return
	 * @see SimulatedUserStatisticsCalculator#getConfidence()
	 */
	public float getConfidence() {
		return this.getSimulatedUserStatisticsCalculator().getConfidence();
	}

	/**
	 * @param confidence
	 * @see SimulatedUserStatisticsCalculator#setConfidence(float)
	 */
	public void setConfidence(float confidence) {
		this.getSimulatedUserStatisticsCalculator().setConfidence(confidence);
	}

	/**
	 * @return
	 * @see SimulatedUserStatisticsCalculator#getQueries()
	 */
	public List<Query> getQueries() {
		return this.getSimulatedUserStatisticsCalculator().getQueries();
	}

	/**
	 * @param queries
	 * @see SimulatedUserStatisticsCalculator#setQueries(java.util.List)
	 */
	public void setQueries(List<Query> queries) {
		this.getSimulatedUserStatisticsCalculator().setQueries(queries);
	}

	/**
	 * @return
	 * @see SimulatedUserStatisticsCalculator#isToAdd1ToCounts()
	 */
	public boolean isToAdd1ToCounts() {
		return this.getSimulatedUserStatisticsCalculator().isToAdd1ToCounts();
	}

	/**
	 * @param isToAdd1ToCounts
	 * @see SimulatedUserStatisticsCalculator#setToAdd1ToCounts(boolean)
	 */
	public void setToAdd1ToCounts(boolean isToAdd1ToCounts) {
		this.getSimulatedUserStatisticsCalculator()
				.setToAdd1ToCounts(isToAdd1ToCounts);
	}

	/**
	 * @return
	 * @see SimulatedUserStatisticsCalculator#isToPrintSummary()
	 */
	public boolean isToPrintSummary() {
		return this.getSimulatedUserStatisticsCalculator().isToPrintSummary();
	}

	/**
	 * @param isToPrintSummary
	 * @see SimulatedUserStatisticsCalculator#setToPrintSummary(boolean)
	 */
	public void setToPrintSummary(boolean isToPrintSummary) {
		this.getSimulatedUserStatisticsCalculator()
				.setToPrintSummary(isToPrintSummary);
	}

	/**
	 * @return
	 * @see SimulatedUserStatisticsCalculator#getQueryAlias()
	 */
	public Map<String, String> getQueryAlias() {
		return this.getSimulatedUserStatisticsCalculator().getQueryAlias();
	}

	/**
	 * @param queryAlias
	 * @see SimulatedUserStatisticsCalculator#setQueryAlias(java.util.Map)
	 */
	public void setQueryAlias(Map<String, String> queryAlias) {
		this.getSimulatedUserStatisticsCalculator().setQueryAlias(queryAlias);
	}

	/**
	 * @param problemID
	 * @see SimulatedUserStatisticsCalculator#setProblemID(java.lang.String)
	 */
	public void setProblemID(String problemID) {
		this.getSimulatedUserStatisticsCalculator().setProblemID(problemID);
	}

	/**
	 * @param tables
	 * @return
	 * @see ObjFunctionPrinter#getMarginalCounts(java.util.List)
	 */
	public Map<String, int[]> getMarginalCounts(List<PotentialTable> tables) {
		return this.getSimulatedUserStatisticsCalculator().getMarginalCounts(tables);
	}

	/**
	 * @param correlationTables
	 * @param threatTables
	 * @param jointTable
	 * @param isStrictlyGreater
	 * @param value
	 * @return
	 * @see ObjFunctionPrinter#getNonZeroRestrictions(java.util.List, java.util.List, unbbayes.prs.bn.PotentialTable, boolean, float)
	 */
	public String getNonZeroRestrictions(
			List<PotentialTable> correlationTables,
			List<PotentialTable> threatTables, PotentialTable jointTable,
			boolean isStrictlyGreater, float value) {
		return this.getSimulatedUserStatisticsCalculator().getNonZeroRestrictions(
				correlationTables, threatTables, jointTable, isStrictlyGreater,
				value);
	}

	/**
	 * @param indicatorCorrelations
	 * @param detectorCorrelations
	 * @param threatIndicatorMatrix
	 * @param detectorIndicatorMatrix
	 * @throws IOException
	 * @see ObjFunctionPrinter#printAll(int[][][], int[][][], int[][][], int[][][])
	 */
	public void printAll(int[][][] indicatorCorrelations,
			int[][][] detectorCorrelations, int[][][] threatIndicatorMatrix,
			int[][][] detectorIndicatorMatrix) throws IOException {
		this.getSimulatedUserStatisticsCalculator().printAll(indicatorCorrelations,
				detectorCorrelations, threatIndicatorMatrix,
				detectorIndicatorMatrix);
	}

	/**
	 * @param indicatorCorrelationsMatrix
	 * @param detectorCorrelationsMatrix
	 * @param threatIndicatorTableMatrix
	 * @param detectorIndicatorTableMatrix
	 * @param printer
	 * @throws IOException
	 * @see ObjFunctionPrinter#printAll(int[][][], int[][][], int[][][], int[][][], java.io.PrintStream)
	 */
	public void printAll(int[][][] indicatorCorrelationsMatrix,
			int[][][] detectorCorrelationsMatrix,
			int[][][] threatIndicatorTableMatrix,
			int[][][] detectorIndicatorTableMatrix, PrintStream printer)
			throws IOException {
		this.getSimulatedUserStatisticsCalculator().printAll(
				indicatorCorrelationsMatrix, detectorCorrelationsMatrix,
				threatIndicatorTableMatrix, detectorIndicatorTableMatrix,
				printer);
	}

	/**
	 * @param names
	 * @return
	 * @see ObjFunctionPrinter#getNameList(java.lang.String[])
	 */
	public List<String> getNameList(String[] names) {
		return this.getSimulatedUserStatisticsCalculator().getNameList(names);
	}

	/**
	 * @param n
	 * @return
	 * @see ObjFunctionPrinter#factorial(int)
	 */
	public int factorial(int n) {
		return this.getSimulatedUserStatisticsCalculator().factorial(n);
	}

	/**
	 * @param n
	 * @param k
	 * @return
	 * @see ObjFunctionPrinter#combinatorial(int, int)
	 */
	public int combinatorial(int n, int k) {
		return this.getSimulatedUserStatisticsCalculator().combinatorial(n, k);
	}

	/**
	 * @return
	 * @see ObjFunctionPrinter#isToBreakLineOnObjectFunction()
	 */
	public boolean isToBreakLineOnObjectFunction() {
		return this.getSimulatedUserStatisticsCalculator()
				.isToBreakLineOnObjectFunction();
	}

	/**
	 * @param isToBreakLineOnObjectFunction
	 * @see ObjFunctionPrinter#setToBreakLineOnObjectFunction(boolean)
	 */
	public void setToBreakLineOnObjectFunction(
			boolean isToBreakLineOnObjectFunction) {
		this.getSimulatedUserStatisticsCalculator()
				.setToBreakLineOnObjectFunction(isToBreakLineOnObjectFunction);
	}

	/**
	 * @return
	 * @see ObjFunctionPrinter#isToSubtract1WayLikelihood()
	 */
	public boolean isToSubtract1WayLikelihood() {
		return this.getSimulatedUserStatisticsCalculator()
				.isToSubtract1WayLikelihood();
	}

	/**
	 * @param isToSubtract1WayLikelihood
	 * @see ObjFunctionPrinter#setToSubtract1WayLikelihood(boolean)
	 */
	public void setToSubtract1WayLikelihood(boolean isToSubtract1WayLikelihood) {
		this.getSimulatedUserStatisticsCalculator()
				.setToSubtract1WayLikelihood(isToSubtract1WayLikelihood);
	}

	/**
	 * @param jointTable
	 * @return
	 * @see ObjFunctionPrinter#getJointProbsIndexesToIgnore(unbbayes.prs.bn.PotentialTable)
	 */
	public Collection<Integer> getJointProbsIndexesToIgnore(
			PotentialTable jointTable) {
		return this.getSimulatedUserStatisticsCalculator()
				.getJointProbsIndexesToIgnore(jointTable);
	}

	/**
	 * @return
	 * @see ObjFunctionPrinter#getJointProbsIndexesToConsider()
	 */
	public Integer[] getJointProbsIndexesToConsider() {
		return this.getSimulatedUserStatisticsCalculator()
				.getJointProbsIndexesToConsider();
	}

	/**
	 * @param jointProbsToConsider
	 * @see ObjFunctionPrinter#setJointProbsIndexesToConsider(java.lang.Integer[])
	 */
	public void setJointProbsIndexesToConsider(Integer[] jointProbsToConsider) {
		this.getSimulatedUserStatisticsCalculator()
				.setJointProbsIndexesToConsider(jointProbsToConsider);
	}

	/**
	 * @return
	 * @see ObjFunctionPrinter#isToPrintJointProbabilityDescription()
	 */
	public boolean isToPrintJointProbabilityDescription() {
		return this.getSimulatedUserStatisticsCalculator()
				.isToPrintJointProbabilityDescription();
	}

	/**
	 * @param isToPrintJointProbabilitySpecification
	 * @see ObjFunctionPrinter#setToPrintJointProbabilityDescription(boolean)
	 */
	public void setToPrintJointProbabilityDescription(
			boolean isToPrintJointProbabilitySpecification) {
		super.setToPrintJointProbabilityDescription(isToPrintJointProbabilitySpecification);
		if (this.getSimulatedUserStatisticsCalculator() != null) {
			this.getSimulatedUserStatisticsCalculator().setToPrintJointProbabilityDescription(isToPrintJointProbabilitySpecification);
		}
	}

	/**
	 * @return
	 * @see ObjFunctionPrinter#getAuxiliaryTableWeightSymbol()
	 */
	public String getAuxiliaryTableWeightSymbol() {
		return this.getSimulatedUserStatisticsCalculator()
				.getAuxiliaryTableWeightSymbol();
	}

	/**
	 * @param auxiliaryTableWeightSymbol
	 * @see ObjFunctionPrinter#setAuxiliaryTableWeightSymbol(java.lang.String)
	 */
	public void setAuxiliaryTableWeightSymbol(String auxiliaryTableWeightSymbol) {
		this.getSimulatedUserStatisticsCalculator()
				.setAuxiliaryTableWeightSymbol(auxiliaryTableWeightSymbol);
	}

	/**
	 * @return
	 * @see ObjFunctionPrinter#getPrimaryTableWeightSymbol()
	 */
	public String getPrimaryTableWeightSymbol() {
		return this.getSimulatedUserStatisticsCalculator()
				.getPrimaryTableWeightSymbol();
	}

	/**
	 * @param primaryTableWeightSymbol
	 * @see ObjFunctionPrinter#setPrimaryTableWeightSymbol(java.lang.String)
	 */
	public void setPrimaryTableWeightSymbol(String primaryTableWeightSymbol) {
		this.getSimulatedUserStatisticsCalculator()
				.setPrimaryTableWeightSymbol(primaryTableWeightSymbol);
	}

	/**
	 * @return
	 * @see ObjFunctionPrinter#getProblemID()
	 */
	public String getProblemID() {
		return this.getSimulatedUserStatisticsCalculator().getProblemID();
	}

	/**
	 * @return
	 * @see ObjFunctionPrinter#isToConsiderDetectors()
	 */
	public boolean isToConsiderDetectors() {
		return this.getSimulatedUserStatisticsCalculator().isToConsiderDetectors();
	}

	/**
	 * @param isToConsiderDetectors
	 * @see ObjFunctionPrinter#setToConsiderDetectors(boolean)
	 */
	public void setToConsiderDetectors(boolean isToConsiderDetectors) {
		this.getSimulatedUserStatisticsCalculator()
				.setToConsiderDetectors(isToConsiderDetectors);
	}

	/**
	 * @return
	 * @see ObjFunctionPrinter#getPrimaryTableSpecialCasesWeightSymbol()
	 */
	public Map<String, String> getPrimaryTableSpecialCasesWeightSymbol() {
		return this.getSimulatedUserStatisticsCalculator()
				.getPrimaryTableSpecialCasesWeightSymbol();
	}

	/**
	 * @param primaryTableSpecialCasesWeightSymbol
	 * @see ObjFunctionPrinter#setPrimaryTableSpecialCasesWeightSymbol(java.util.Map)
	 */
	public void setPrimaryTableSpecialCasesWeightSymbol(
			Map<String, String> primaryTableSpecialCasesWeightSymbol) {
		this.getSimulatedUserStatisticsCalculator()
				.setPrimaryTableSpecialCasesWeightSymbol(primaryTableSpecialCasesWeightSymbol);
	}

	/**
	 * @return
	 * @see ObjFunctionPrinter#getAuxiliaryTableDirectoryName()
	 */
	public String getAuxiliaryTableDirectoryName() {
		return this.getSimulatedUserStatisticsCalculator()
				.getAuxiliaryTableDirectoryName();
	}

	/**
	 * @param auxiliaryTableDirectoryName
	 * @see ObjFunctionPrinter#setAuxiliaryTableDirectoryName(java.lang.String)
	 */
	public void setAuxiliaryTableDirectoryName(
			String auxiliaryTableDirectoryName) {
		this.getSimulatedUserStatisticsCalculator()
				.setAuxiliaryTableDirectoryName(auxiliaryTableDirectoryName);
	}

	/**
	 * @return
	 * @see ObjFunctionPrinter#getPrimaryTableDirectoryName()
	 */
	public String getPrimaryTableDirectoryName() {
		return this.getSimulatedUserStatisticsCalculator()
				.getPrimaryTableDirectoryName();
	}

	/**
	 * @param primaryTableDirectoryName
	 * @see ObjFunctionPrinter#setPrimaryTableDirectoryName(java.lang.String)
	 */
	public void setPrimaryTableDirectoryName(String primaryTableDirectoryName) {
		this.getSimulatedUserStatisticsCalculator()
				.setPrimaryTableDirectoryName(primaryTableDirectoryName);
	}

	/**
	 * @return
	 * @see ObjFunctionPrinter#getIndicatorNames()
	 */
	public String[] getIndicatorNames() {
		return this.getSimulatedUserStatisticsCalculator().getIndicatorNames();
	}

	/**
	 * @param indicatorNames
	 * @see ObjFunctionPrinter#setIndicatorNames(java.lang.String[])
	 */
	public void setIndicatorNames(String[] indicatorNames) {
		this.getSimulatedUserStatisticsCalculator()
				.setIndicatorNames(indicatorNames);
	}

	/**
	 * @return
	 * @see ObjFunctionPrinter#getDetectorNames()
	 */
	public String[] getDetectorNames() {
		return this.getSimulatedUserStatisticsCalculator().getDetectorNames();
	}

	/**
	 * @param detectorNames
	 * @see ObjFunctionPrinter#setDetectorNames(java.lang.String[])
	 */
	public void setDetectorNames(String[] detectorNames) {
		this.getSimulatedUserStatisticsCalculator().setDetectorNames(detectorNames);
	}

	/**
	 * @return
	 * @see ObjFunctionPrinter#getThreatName()
	 */
	public String getThreatName() {
		return this.getSimulatedUserStatisticsCalculator().getThreatName();
	}

	/**
	 * @param threatName
	 * @see ObjFunctionPrinter#setThreatName(java.lang.String)
	 */
	public void setThreatName(String threatName) {
		this.getSimulatedUserStatisticsCalculator().setThreatName(threatName);
	}
	


	/**
	 * 
	 * @param input
	 * @throws IOException 
	 */
	public void loadWrapperInput(File input) throws IOException {
		Debug.println(getClass(), "Loading wrapper file: " + input.getAbsolutePath());

		// load from file
		this.getIO().readWrapperFile(input);
		
		if (this.getIO().getProperties() != null) {
			try {
				// use java reflection in order to automatically detect setter names and fill properties
				Map<String, Entry<Object, Method>> settersByName = new HashMap<String, Entry<Object, Method>>();	// organize methods by name
				if (getDirichletUserSimulator() != null) {
					for (Method method : getSimulatedUserStatisticsCalculator().getClass().getMethods()) {
						if (method.getName().startsWith("set") && method.getParameterTypes().length == 1) {
							// only consider simple setters in the format "setX(T value)"
							settersByName.put(method.getName(), Collections.singletonMap((Object)getDirichletUserSimulator(), method).entrySet().iterator().next());
						}
					}
				}
				if (getSimulatedUserStatisticsCalculator() != null) {
					for (Method method : getSimulatedUserStatisticsCalculator().getClass().getMethods()) {
						if (method.getName().startsWith("set") && method.getParameterTypes().length == 1) {
							// only consider simple setters in the format "setX(T value)"
							settersByName.put(method.getName(), Collections.singletonMap((Object)getSimulatedUserStatisticsCalculator(), method).entrySet().iterator().next());
						}
					}
				}
				for (Method method : this.getClass().getMethods()) {
					if (method.getName().startsWith("set") && method.getParameterTypes().length == 1) {
						// only consider simple setters in the format "setX(T value)"
						settersByName.put(method.getName(), Collections.singletonMap((Object)this, method).entrySet().iterator().next());
					}
				}
				
				// look for methods with same name
				for (Entry<String, String> entry : this.getIO().getProperties().entrySet()) {
					try {
						String methodName = "set" + entry.getKey();
						Entry<Object, Method> invokerMethodPair = settersByName.get(methodName);	// key is invoker, value is method
						if (invokerMethodPair != null) {
							Class<?> type = invokerMethodPair.getValue().getParameterTypes()[0];	// extract the type of the 1st parameter
							if (String.class.isAssignableFrom(type)) {
								invokerMethodPair.getValue().invoke(invokerMethodPair.getKey(), (entry.getValue()));
							} else if (int.class.isAssignableFrom(type)) {
								invokerMethodPair.getValue().invoke(invokerMethodPair.getKey(), Integer.parseInt(entry.getValue()));
							} else if (float.class.isAssignableFrom(type)) {
								invokerMethodPair.getValue().invoke(invokerMethodPair.getKey(), Float.parseFloat(entry.getValue()));
							} else if (Integer.class.isAssignableFrom(type)) {
								invokerMethodPair.getValue().invoke(invokerMethodPair.getKey(), new Integer(entry.getValue()));
							} else if (Float.class.isAssignableFrom(type)) {
								invokerMethodPair.getValue().invoke(invokerMethodPair.getKey(), new Float(entry.getValue()));
							} else if (double.class.isAssignableFrom(type)) {
								invokerMethodPair.getValue().invoke(invokerMethodPair.getKey(), Double.parseDouble(entry.getValue()));
							} else if (long.class.isAssignableFrom(type)) {
								invokerMethodPair.getValue().invoke(invokerMethodPair.getKey(), Long.parseLong(entry.getValue()));
							} else if (Double.class.isAssignableFrom(type)) {
								invokerMethodPair.getValue().invoke(invokerMethodPair.getKey(), new Double(entry.getValue()));
							} else if (Long.class.isAssignableFrom(type)) {
								invokerMethodPair.getValue().invoke(invokerMethodPair.getKey(), new Long(entry.getValue()));
							} else {
								invokerMethodPair.getValue().invoke(invokerMethodPair.getKey(), type.cast(entry.getValue()));
							}
						}
					} catch (Throwable t) {
						Debug.println(getClass(), t.getMessage(), t);
					}
				}
			} catch (Throwable t) {
				Debug.println(getClass(), t.getMessage(), t);
			}
		}
		
		// update attributes accordingly from what we read from file
		
		if (this.getIO().getProperty(getNumberOfIndicatorsPropertyName()) != null) {
			this.setNumIndicators(Integer.parseInt(this.getIO().getProperty(getNumberOfIndicatorsPropertyName())));
		}
		Debug.println(getClass(), "Num indicators = " + this.getNumIndicators());
		
		if (this.getIO().getProperty(getNumberOfDetectorsPropertyName()) != null) {
			this.setNumDetectors(Integer.parseInt(this.getIO().getProperty(getNumberOfDetectorsPropertyName())));
		}
		Debug.println(getClass(), "Num detectors = " + this.getNumDetectors());
		
		if (this.getIO().getProperty(getNumberOfUsersPropertyName()) != null) {
			this.setNumUsers(Integer.parseInt(this.getIO().getProperty(getNumberOfUsersPropertyName())));
			if (getNumUsers() < 1) {
				throw new IllegalArgumentException(getNumberOfUsersPropertyName() + " expected to be 1 or above, but found " + getNumUsers());
			}
		}
		Debug.println(getClass(), "Num users = " + this.getNumUsers());
		
		String typeOfFusion = this.getIO().getProperty(getTypeOfFusionPropertyName());
		Debug.println(getClass(), "Type of fusion = " + typeOfFusion);
		if (typeOfFusion != null) {
			this.setCountAlert(this.convertFusionTypeToCountAlert(typeOfFusion));
		}
		Debug.println(getClass(), "Count alert = " + this.getCountAlert());
		
		if (this.getIO().getProperty(getHasAlertInProbPropertyName()) != null) {
			this.setToReadAlert(Integer.parseInt(this.getIO().getProperty(getHasAlertInProbPropertyName())) != 0 );
		}
		Debug.println(getClass(), "Read alert = " + this.isToReadAlert());
		
		if (this.getIO().getProperty(getNumberOfRunsPropertyName()) != null) {
			this.setNumOrganization(Integer.parseInt(this.getIO().getProperty(getNumberOfRunsPropertyName())) );
			if (getNumOrganization() < 1) {
				throw new IllegalArgumentException(getNumberOfRunsPropertyName() + " expected to be 1 or above, but found " + getNumOrganization());
			}
		}
		Debug.println(getClass(), "Num organization = " + this.getNumOrganization());
		
		// reset the probability vector
		getWrapperProbabilities().clear();
		
		// read property in the format Probability = p1,p2,...,pk.
		String probMatrixString = this.getIO().getProperty(getProbabilitiesPropertyNamePrefix());
		if (probMatrixString != null) {
			Debug.println(getClass(), "Reading " + getProbabilitiesPropertyNamePrefix());
			
			// convert the comma-separated string to a list. This list is linear, but may represent multiple probability vectors
			List<Float> probMatrixList = this.getFloatListFromCommaSeparatedString(probMatrixString);
			
			// estimate what is the expected size of a single joint probability vector (i.e. calculate the number of joint states).
			int jointStateSize = 2 * (this.isToReadAlert()?2:1);	// size of threat = 2, and if we need to read alert from prob vector, then consider alert (size 2) as well
			for (int i = 0; i < this.getNumIndicators() + this.getNumDetectors(); i++) {	// consider joint size of indicators and detectors
				jointStateSize *= 2;	// assume that indicators and detectors have size 2
			}
			
			// make sure the size of matrix of probabilities is a multiple of joint state size
			if (probMatrixList.size() % jointStateSize != 0) {
				throw new IllegalArgumentException("Expected joint state size of " + this.getNumIndicators() + " indicators, "
						+ this.getNumDetectors() + " detectors," + (this.isToReadAlert()?" threat, and alert":" and threat") 
						+ " should be a multple of " + jointStateSize + ", but probability matrix had size of " + probMatrixList.size());
			}
			
			// split the probability matrix to multiple probability list
			for (int i = 0; i < probMatrixList.size(); i += jointStateSize) {
				List<Float> subList = probMatrixList.subList(i, i + jointStateSize);
				Debug.println(getClass(), "Adding probability (sub)list of size " + subList.size());
				getWrapperProbabilities().add(subList);
			}
			
		}
		
		// read properties in the format Probability<N> = p1,p2,...,pk
		for (int i = 1; i <= Integer.MAX_VALUE; i++) {
			// read property, which is a comma-separated string with probability distribution
			String property = this.getIO().getProperty(getProbabilitiesPropertyNamePrefix() + i);
			Debug.println(getClass(), getProbabilitiesPropertyNamePrefix() + i + " = " + property);
			if (property == null || property.trim().isEmpty()) {
				break;
			}
			
			// convert comma-separated string to a list of float and puts to wrapper probabilities
			getWrapperProbabilities().add(this.getFloatListFromCommaSeparatedString(property));
		}
		
		if (getWrapperProbabilities().size() < 1) {
			throw new IOException("No valid probability declaration was found.");
		}
		
		// read properties in the format Clique<N>: <name>
		for (int i = 1; i <= Integer.MAX_VALUE; i++) {
			// read property, which is a comma-separated string with probability distribution
			String property = this.getIO().getProperty(getCliquesPropertyNamePrefix() + i);
			Debug.println(getClass(), getCliquesPropertyNamePrefix() + i + " = " + property);
			if (property == null || property.trim().isEmpty()) {
				break;
			}
			
			// convert comma-separated string to a list of float and puts to wrapper probabilities
			getCliqueNames().add(property.replaceAll("\\s", ""));	// make sure not to use spaces
		}
		
		
		if (getCliqueNames().isEmpty()) {
			// make sure all probabilities have same size if we are not dealing with cliques
			int sizeOfFirstProbDistro = getWrapperProbabilities().get(0).size();
			for (int i = 1; i < getWrapperProbabilities().size(); i++) {
				if (getWrapperProbabilities().get(i).size() != sizeOfFirstProbDistro) {
					throw new IOException("Size of 1st probability distribution was " + sizeOfFirstProbDistro
							+ ", but size of probability distribution " + i + " was " + getWrapperProbabilities().get(i).size());
				}
			}
		} else {
			// we are dealing with cliques. Make sure we have a probability distribution for each clique
			if (getWrapperProbabilities().size() < getCliqueNames().size()) {
				throw new IOException("Found " + getCliqueNames().size() + " cliques, but " + getWrapperProbabilities().size() + " probabilities.");
			}
		}
		
	}
	
	/**
	 * Converts a comma-separated string to a list of float numbers.
	 * @param property : string containing float numbers separated by commas.
	 * @return list of float obtained by splitting the comma-separated string.
	 * @see #loadWrapperInput(File)
	 * @see Float#parseFloat(String)
	 * @see String#split(String)
	 */
	protected List<Float> getFloatListFromCommaSeparatedString(String property) {
		
		List<Float> probability = new ArrayList<Float>();
		
		try {
			String[] split = property.split(",");
//			Debug.println(getClass(), "Parsing probabilities...");
//			Debug.print("[");
			for (String string : split) {
				if (string == null || string.trim().isEmpty()) {
					continue;	// ignore null entries
				}
				probability.add(Float.parseFloat(string));
//				Debug.print(string+"\t");
			}
//			Debug.println("]");
//			Debug.println(getClass(), "Finished parsing probabilities. Size = " + probability.size());
		} catch (RuntimeException e) {
			Debug.println(getClass(), e.getMessage(), e);
		}
		
		return probability;
	}

	/**
	 * Converts a fusionType code to how many detectors should be considered in alert.
	 * Negative values can be used for special types of alerts.
	 * @param fusionType
	 * @return
	 */
	public int convertFusionTypeToCountAlert(String fusionType) {
		if (fusionType == null || fusionType.trim().isEmpty()) {
			return Integer.MIN_VALUE;
		}
		int ret = Integer.MIN_VALUE;
		try {
			ret = 1 + ((int) (Float.parseFloat(fusionType)));
		} catch (NumberFormatException e) {
			Debug.println(getClass(), "Could not parse type of fusion: " + fusionType, e);
			return Integer.MIN_VALUE;
		}
		return ret;
	}

	/**
	 * 
	 * @return
	 * @throws IOException 
	 */
	public File writeCurrentProbabilityToDirectory() throws IOException {
		File tempFolder = Files.createTempDirectory("Probabilities_").toFile();
		tempFolder.deleteOnExit();
		Debug.println(getClass(), "Created temporary directory for probabilities: " + tempFolder.getAbsolutePath());
		
		List<String> cliqueNames = getCliqueNames();
		if (cliqueNames == null) {
			cliqueNames = Collections.EMPTY_LIST;
		} else if (!cliqueNames.isEmpty()) {
			Debug.println(getClass(), "Saving temporary files for clique potentials...");
		}
		List<List<Float>> probs = getWrapperProbabilities();
		for (int i = 0; i < getWrapperProbabilities().size(); i++) {
			List<Float> probability = probs.get(i);
			String fileNamePrefix = "prob";
			if (!cliqueNames.isEmpty()) {
				// if there are cliques, use clique name as prefix of file names
				fileNamePrefix = cliqueNames.get(i);
			}
			File tempFile = File.createTempFile(fileNamePrefix + "_" + i, ".csv", tempFolder);
			tempFile.deleteOnExit();
			PrintStream printer = new PrintStream(new FileOutputStream(tempFile , false));	// overwrites if file exists
			for (Float prob : probability) {
				printer.println(prob + ",");
			}
			printer.close();
			Debug.println(getClass(), "Created temporary file for probability " + i + ": " + tempFile.getAbsolutePath());
		}
		return tempFolder;
	}

	
	/**
	 * @see IModelCenterWrapperIO#convertToWrapperOutput(File, File)
	 * @see #getIO()
	 */
	public void convertToWrapperOutput(File input, File output) throws IOException {
		IModelCenterWrapperIO io = this.getIO();
		if (io != null) {
			this.getIO().convertToWrapperOutput(input, output);
		}
	}



	/**
	 * @return the numIndicators
	 */
	public int getNumIndicators() {
		return numIndicators;
	}

	/**
	 * @param numIndicators the numIndicators to set
	 */
	public void setNumIndicators(int numIndicators) {
		this.numIndicators = numIndicators;
	}

	/**
	 * @return the numDetectors
	 */
	public int getNumDetectors() {
		return numDetectors;
	}

	/**
	 * @param numDetectors the numDetectors to set
	 */
	public void setNumDetectors(int numDetectors) {
		this.numDetectors = numDetectors;
	}
	


	/**
	 * @return the io
	 */
	public IModelCenterWrapperIO getIO() {
		if (io == null) {
			io = ModelCenterMatrixStyleWrapperIO.getInstance();
		}
		return io;
	}

	/**
	 * @param io the io to set
	 */
	public void setIO(IModelCenterWrapperIO io) {
		this.io = io;
	}

	/**
	 * @return the numberOfIndicatorsPropertyName
	 */
	public String getNumberOfIndicatorsPropertyName() {
		return this.numberOfIndicatorsPropertyName;
	}

	/**
	 * @param numberOfIndicatorsPropertyName the numberOfIndicatorsPropertyName to set
	 */
	public void setNumberOfIndicatorsPropertyName(
			String numberOfIndicatorsPropertyName) {
		this.numberOfIndicatorsPropertyName = numberOfIndicatorsPropertyName;
	}

	/**
	 * @return the numberOfDetectorsPropertyName
	 */
	public String getNumberOfDetectorsPropertyName() {
		return this.numberOfDetectorsPropertyName;
	}

	/**
	 * @param numberOfDetectorsPropertyName the numberOfDetectorsPropertyName to set
	 */
	public void setNumberOfDetectorsPropertyName(
			String numberOfDetectorsPropertyName) {
		this.numberOfDetectorsPropertyName = numberOfDetectorsPropertyName;
	}

	/**
	 * @return the numberOfUsersPropertyName
	 */
	public String getNumberOfUsersPropertyName() {
		return this.numberOfUsersPropertyName;
	}

	/**
	 * @param numberOfUsersPropertyName the numberOfUsersPropertyName to set
	 */
	public void setNumberOfUsersPropertyName(String numberOfUsersPropertyName) {
		this.numberOfUsersPropertyName = numberOfUsersPropertyName;
	}

	/**
	 * @return the typeOfFusionPropertyName
	 */
	public String getTypeOfFusionPropertyName() {
		return this.typeOfFusionPropertyName;
	}

	/**
	 * @param typeOfFusionPropertyName the typeOfFusionPropertyName to set
	 */
	public void setTypeOfFusionPropertyName(String typeOfFusionPropertyName) {
		this.typeOfFusionPropertyName = typeOfFusionPropertyName;
	}

	/**
	 * @return the probabilitiesPropertyNamePrefix
	 */
	public String getProbabilitiesPropertyNamePrefix() {
		return this.probabilitiesPropertyNamePrefix;
	}

	/**
	 * @param probabilitiesPropertyNamePrefix the probabilitiesPropertyNamePrefix to set
	 */
	public void setProbabilitiesPropertyNamePrefix(
			String probabilitiesPropertyNamePrefix) {
		this.probabilitiesPropertyNamePrefix = probabilitiesPropertyNamePrefix;
	}

	/**
	 * @return the probabilityPropertyName
	 * @deprecated see {@link ModelCenterWrapperIO#getProbabilityPropertyName()}
	 */
	public String getProbabilityPropertyName() {
		IModelCenterWrapperIO io = this.getIO();
		if ( ( io != null ) && ( io instanceof ModelCenterWrapperIO ) ) {
			// delegate to ModelCenterWrapperIO
			return ((ModelCenterWrapperIO) io).getProbabilityPropertyName();
		}
		return ModelCenterWrapperIO.PROBABILITY_PROPERTY_NAME;	// return the default value
	}

	/**
	 * @param probabilityPropertyName the probabilityPropertyName to set
	 * @deprecated see {@link ModelCenterWrapperIO#getProbabilityPropertyName()}
	 */
	public void setProbabilityPropertyName(String probabilityPropertyName) {
		IModelCenterWrapperIO io = this.getIO();
		if ( ( io != null ) && ( io instanceof ModelCenterWrapperIO ) ) {
			// delegate to ModelCenterWrapperIO
			((ModelCenterWrapperIO) io).setProbabilityPropertyName(probabilityPropertyName);
		}
	}
	

	/**
	 * @return the probabilities : probabilities read from input file.
	 * @see #loadWrapperInput(File)
	 * @see #getCliqueNames()
	 */
	public List<List<Float>> getWrapperProbabilities() {
		return wrapperProbabilities;
	}

	/**
	 * @param probabilities : probabilities read from input file.
	 * @see #loadWrapperInput(File)
	 * @see #getCliqueNames()
	 */
	public void setWrapperProbabilities(List<List<Float>> probabilities) {
		this.wrapperProbabilities = probabilities;
	}

	/**
	 * @return the maxNumAttempt
	 */
	public static int getMaxNumAttempt() {
		return maxNumAttempt;
	}

	/**
	 * @param maxNumAttempt the maxNumAttempt to set
	 */
	public static void setMaxNumAttempt(int maxNumAttempt) {
		JavaSimulatorWrapper.maxNumAttempt = maxNumAttempt;
	}

	/**
	 * @return the hasAlertInProbPropertyName : name of the property in JavaSimulatorWrapper.in file 
	 * which indicates whether the Alert variable is included in the probability distribution (1)
	 * or not included (0).
	 * @see #loadWrapperInput(File)
	 */
	public String getHasAlertInProbPropertyName() {
		return hasAlertInProbPropertyName;
	}

	/**
	 * @param hasAlertInProbPropertyName : name of the property in JavaSimulatorWrapper.in file 
	 * which indicates whether the Alert variable is included in the probability distribution (1)
	 * or not included (0).
	 * @see #loadWrapperInput(File)
	 */
	public void setHasAlertInProbPropertyName(String hasAlertInProbPropertyName) {
		this.hasAlertInProbPropertyName = hasAlertInProbPropertyName;
	}

	/**
	 * @return the numberOfRunsPropertyName
	 */
	public String getNumberOfRunsPropertyName() {
		return numberOfRunsPropertyName;
	}

	/**
	 * @param numberOfRunsPropertyName the numberOfRunsPropertyName to set
	 */
	public void setNumberOfRunsPropertyName(String numberOfRunsPropertyName) {
		this.numberOfRunsPropertyName = numberOfRunsPropertyName;
	}
	

	/**
	 * Delegates to {@link #getSimulatedUserStatisticsCalculator()}
	 * @return the stratifiedSampleNumTotal
	 * @see #getStratifiedSampleNumAlert()
	 */
	public int getStratifiedSampleNumTotal() {
		return getSimulatedUserStatisticsCalculator().getStratifiedSampleNumTotal();
	}


	/**
	 * Delegates to {@link #getSimulatedUserStatisticsCalculator()}
	 * @param stratifiedSampleNumTotal the stratifiedSampleNumTotal to set
	 * @see #getStratifiedSampleNumAlert()
	 */
	public void setStratifiedSampleNumTotal(int stratifiedSampleNumTotal) {
		getSimulatedUserStatisticsCalculator().setStratifiedSampleNumTotal(stratifiedSampleNumTotal);
	}


	/**
	 * Delegates to {@link #getSimulatedUserStatisticsCalculator()}
	 * @return the stratifiedSampleNumAlert : number of stratified samples to consider with Alert=true. 
	 * 100 minus this number will be sampled for Alert = false. Use this argument in order to increase variance.
	 * @see #getStratifiedSampleNumTotal()
	 */
	public int getStratifiedSampleNumAlert() {
		return getSimulatedUserStatisticsCalculator().getStratifiedSampleNumAlert();
	}


	/**
	 * Delegates to {@link #getSimulatedUserStatisticsCalculator()}.
	 * @param stratifiedSampleNumAlert : number of stratified samples to consider with Alert=true. 
	 * 100 minus this number will be sampled for Alert = false. Use this argument in order to increase variance.
	 * @see #getStratifiedSampleNumTotal()
	 */
	public void setStratifiedSampleNumAlert(int stratifiedSampleNumAlert) {
		getSimulatedUserStatisticsCalculator().setStratifiedSampleNumAlert(stratifiedSampleNumAlert);
	}
	
	/**
	 * Delegates to {@link #getSimulatedUserStatisticsCalculator()}
	 * @see SimulatedUserStatisticsCalculator#setSubSamplingMode(SubSamplingMode)
	 */
	public void setSubSamplingModeName(String subSamplingModeName) {
		getSimulatedUserStatisticsCalculator().setSubSamplingMode(SubSamplingMode.valueOf(subSamplingModeName));
	}


	/**
	 * @return the cliquesFileName
	 */
	public String getCliquesFileName() {
		return cliquesFileName;
	}

	/**
	 * @param cliquesFileName the cliquesFileName to set
	 */
	public void setCliquesFileName(String cliquesFileName) {
		this.cliquesFileName = cliquesFileName;
	}

	/**
	 * @return the cliqueNames : if non-empty, then the i-th element of {@link #getWrapperProbabilities()}
	 * represents the clique potential of the i-th clique in this list.
	 */
	public List<String> getCliqueNames() {
		if (cliqueNames == null) {
			cliqueNames = new ArrayList<String>();
		}
		return cliqueNames;
	}

	/**
	 * @param cliqueNames : if non-empty, then the i-th element of {@link #getWrapperProbabilities()}
	 * represents the clique potential of the i-th clique in this list.
	 */
	public void setCliqueNames(List<String> cliqueNames) {
		this.cliqueNames = cliqueNames;
	}

	/**
	 * @return the cliquesPropertyNamePrefix
	 */
	public String getCliquesPropertyNamePrefix() {
		return cliquesPropertyNamePrefix;
	}

	/**
	 * @param cliquesPropertyNamePrefix the cliquesPropertyNamePrefix to set
	 */
	public void setCliquesPropertyNamePrefix(String cliquesPropertyNamePrefix) {
		this.cliquesPropertyNamePrefix = cliquesPropertyNamePrefix;
	}

	/**
	 * Runs {@link DirichletUserSimulator#main(String[])} and then {@link SimulatedUserStatisticsCalculator#main(String[])}.
	 * However, by default it will read a file called JavaSimulatorWrapper.in and write a file called JavaSimulatorWrapper.out
	 * in the same directory of this program.
	 * <br/>
	 * <br/>
	 * The content of JavaSimulatorWrapper.in shall look like:
	 * <pre>
#
# JavaSimulatorWrapper input file
#

Number_of_Indicators:	2
Number_of_Detectors:	1
Number_of_Users:	4263
Type_of_Fusion:	1
Probabilities1:	0.0625,0.0625,0.0625,0.0625,0.0625,0.0625,0.0625,0.0625,0.0625,0.0625,0.0625,0.0625,0.0625,0.0625,0.0625,0.0625
Probabilities2:	1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0
Probabilities3:	0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1
	 * </pre>
	 * <br/>
	 * The content of JavaSimulatorWrapper.out will look like:
	 * <pre>
# JavaSimulatorWrapper Results
# Tue Jul 12 11:47:42 EDT 2016
#
# calculated parameters
Probability=0.54347825,0.7352941,0.002134218,0.11557789,0.45454544,0.096330285,0.07339449

	 * </pre>
	 * <br/>
	 * <br/>
	 * 
	 * @param args
	 */
	@SuppressWarnings("static-access")
	public static void main(String[] args) {
		CommandLineParser parser = new DefaultParser();
		Options options = new Options();
		options.addOption("d","debug", false, "Enables debug mode.");
		options.addOption("i","input", true, "File to read. If not specified, JavaSimulatorWrapper.in (in same directory of the program) will be used.");
		options.addOption("o","output", true, "File to write. If not specified, JavaSimulatorWrapper.out (in same directory of the program) will be used.");
		options.addOption("cliques","clique-structure-file-name", true, "Name of file (JSON format) to load clique structrue.");
		options.addOption("h","help", false, "Help.");
		
		CommandLine cmd = null;
		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			e.printStackTrace();
			return;
		}
		
		if (cmd == null) {
			System.err.println("Invalid command line");
			return;
		}
		

		if (cmd.hasOption("h")) {
			for (Option option : options.getOptions()) {
				System.out.println("-" + option.getOpt() + (option.hasArg()?(" <" + option.getLongOpt() +">"):"") + " : " + option.getDescription());
			}
			return;
		}
		
		Debug.setDebug(cmd.hasOption("d"));
		
		JavaSimulatorWrapper wrapper = JavaSimulatorWrapper.getInstance();
		if (cmd.hasOption("i")) {
			wrapper.setInput(new File(cmd.getOptionValue("i")));
		}
		if (cmd.hasOption("o")) {
			wrapper.setOutput(new File(cmd.getOptionValue("o")));
		}
		if (cmd.hasOption("cliques")) {
			wrapper.setCliquesFileName(cmd.getOptionValue("cliques"));
		}
		
		try {
			wrapper.loadWrapperInput(wrapper.getInput());
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		
		// attempt several times until we don't have any exception
		int numAttempt;
		for (numAttempt = 0; numAttempt < getMaxNumAttempt(); numAttempt++) {
			File tempProbDirectory = null;
			File tempDirichletOutput = null;
			File tempQuestionOutput = null;
			try {
				tempProbDirectory = wrapper.writeCurrentProbabilityToDirectory();
				
				// generate temporary file to store results of dirichlet-multinomial simulation
				if (wrapper.getNumOrganization() <= 1) {
					tempDirichletOutput = File.createTempFile("Users_", ".csv");
					Debug.println(JavaSimulatorWrapper.class, "Created temporary file for users: " + tempDirichletOutput.getAbsolutePath());
				} else {
					tempDirichletOutput = Files.createTempDirectory("Users_").toFile();
					Debug.println(JavaSimulatorWrapper.class, "Created temporary folder for users: " + tempDirichletOutput.getAbsolutePath());
				}
				tempDirichletOutput.deleteOnExit();
				
				// set up arguments for dirichlet-multinomial simulator
				String[] dirichletArgs = new String[Debug.isDebugMode()?24:23];
				
				// -i "RCP3-full" -o "test.csv" -u 4263 -n 1000 -numI 4 -numD 4 -a 2 -d 
				dirichletArgs[0] = "-i";
				dirichletArgs[1] = "\"" + tempProbDirectory.getAbsolutePath() +"\"";
				dirichletArgs[2] = "-o";
				dirichletArgs[3] = "\"" + tempDirichletOutput.getAbsolutePath() +"\"";
				dirichletArgs[4] = "-u";
				dirichletArgs[5] = ""+wrapper.getNumUsers();
				dirichletArgs[6] = "-n";
				dirichletArgs[7] = ""+wrapper.getNumOrganization();	// number of replications
				dirichletArgs[8] = "-numI";	
				dirichletArgs[9] = ""+wrapper.getNumIndicators();	
				dirichletArgs[10] = "-numD";	
				dirichletArgs[11] = ""+wrapper.getNumDetectors();	
				dirichletArgs[12] = "-a";	
				dirichletArgs[13] = ""+wrapper.getCountAlert();	
				dirichletArgs[14] = wrapper.isToReadAlert()?"-ra":"";	
				dirichletArgs[15] = "-id";	
				dirichletArgs[16] = wrapper.isToUseDirichletMultinomial()?"Dirichlet":"Samples";
				dirichletArgs[17] = "-threat";	
				dirichletArgs[18] = "Threat";
				if (wrapper.getCliqueNames() == null || wrapper.getCliqueNames().isEmpty()) {
					// make sure the cliques argument is not used if no clique was read
					dirichletArgs[19] = "";	
					dirichletArgs[20] = "";
				} else {
					dirichletArgs[19] = "-cliques";	
					dirichletArgs[20] = wrapper.getCliquesFileName();
				}
				dirichletArgs[21] = "-cliques";	
				dirichletArgs[22] = wrapper.getCliquesFileName();
				if (Debug.isDebugMode() && dirichletArgs.length >= 24) {
					dirichletArgs[23] = "-d";	
				}
				
				if (Debug.isDebugMode()) {
					for (String arg : dirichletArgs) {
						Debug.println(JavaSimulatorWrapper.class, "Argument for dirichlet-multinomial simulator: " + arg);
					}
				}

				
				// run dirichlet multinomial sampler
				wrapper.getDirichletUserSimulator().main(dirichletArgs);
				
				// generate temporary file to store results of calculator of probabilities of questions
				tempQuestionOutput = File.createTempFile("Probabilities_Questions_", ".csv");
				tempQuestionOutput.deleteOnExit();
				Debug.println(JavaSimulatorWrapper.class, "Created temporary file for RCP answers: " + tempQuestionOutput.getAbsolutePath());
				
				// set up arguments to calculate probabilities of questions
				String[] questionArgs = new String[Debug.isDebugMode()?7:6];
				
				// -i "test.csv" -o "Probabilities_test.csv" -numI 4 -d
				questionArgs[0] = "-i";
				questionArgs[1] = "\"" + tempDirichletOutput.getAbsolutePath() +"\"";;
				questionArgs[2] = "-o";
				questionArgs[3] = "\"" + tempQuestionOutput.getAbsolutePath() +"\"";;
				questionArgs[4] = "-numI";	
				questionArgs[5] = ""+wrapper.getNumIndicators();	
				if (Debug.isDebugMode() && questionArgs.length >= 7) {
					questionArgs[6] = "-d";	
				}
				
				if (Debug.isDebugMode()) {
					for (String arg : questionArgs) {
						Debug.println(JavaSimulatorWrapper.class, "Argument for statistics calculator: " + arg);
					}
				}
				
				wrapper.getSimulatedUserStatisticsCalculator().main(questionArgs);
				
				
				// convert output to JavaSimulatorWrapper.out format;
				
				wrapper.convertToWrapperOutput(tempQuestionOutput, wrapper.getOutput());
				
				
				// make sure we delete the temp files (although we created them as temporary)
				
				tempProbDirectory.delete();
				
				if (tempDirichletOutput.isDirectory()) {
					for (File innerFile : tempDirichletOutput.listFiles()) {
						innerFile.delete();
					}
				}
				tempDirichletOutput.delete();
				
				tempQuestionOutput.delete();
				
			} catch (Exception e) {
				if (tempProbDirectory != null){
					tempProbDirectory.delete();
				}
				if (tempDirichletOutput != null){
					if (tempDirichletOutput.isDirectory()) {
						for (File innerFile : tempDirichletOutput.listFiles()) {
							innerFile.delete();
						}
					}
					tempDirichletOutput.delete();
				}
				if (tempQuestionOutput != null){
					tempQuestionOutput.delete();
				}
				
				if (numAttempt + 1 >= maxNumAttempt) {
					// Failed the last attempt. Just let the caller know about the last exception
					throw new RuntimeException("Failed max number of attempts. This might be caused by bad input probabilities: " + e.getMessage(),e);
				}
				
				Debug.println(JavaSimulatorWrapper.class, e.getMessage(), e);
				continue;	// try next attempt if exception was thrown
			}
			
			break;	// finish attempt if everything was fine
		}
	}

	
}
