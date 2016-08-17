package utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import unbbayes.prs.INode;
import unbbayes.prs.bn.PotentialTable;

public class ObjFunctionPrinterTest extends TestCase {
	
	ObjFunctionPrinter printer = null;

	public ObjFunctionPrinterTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		printer = new ObjFunctionPrinter();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		printer = null;
	}

	public final void testGetThreatTables() {
		String[] indicatorNames = {"I1","I2","I3","I4"};
		String[] detectorNames = {"D1","D2","D3","D4","D5","D6"};
		printer.setIndicatorNames(indicatorNames);
		printer.setDetectorNames(detectorNames);
		printer.setThreatName("Threat");
		printer.setAlertName("Alert");
		
		Map<String, INode> variableMap = new HashMap<String, INode>();
		List<String> allVars = new ArrayList<String>();
		allVars.add(printer.getThreatName());
		List<String> indicatorNamesList = printer.getNameList(printer.getIndicatorNames());
		assertNotNull(indicatorNamesList);
		assertEquals(indicatorNames.length, indicatorNamesList.size());
		allVars.addAll(indicatorNamesList);
		List<String> detectorNamesList = printer.getNameList(printer.getDetectorNames());
		assertNotNull(detectorNamesList);
		assertEquals(detectorNames.length, detectorNamesList.size());
		allVars.addAll(detectorNamesList);
		allVars.add(printer.getAlertName());
		PotentialTable jointTable = printer.getJointTable(variableMap, allVars);
		assertNotNull(jointTable);
		assertEquals(2 + indicatorNames.length + detectorNames.length, jointTable.getVariablesSize());
		assertFalse(variableMap.isEmpty());
		assertEquals(jointTable.getVariablesSize(),variableMap.size());
		
		List<PotentialTable> threatTables = printer.getThreatTables(variableMap, null, indicatorNamesList, printer.getThreatName());
		
		assertNotNull(threatTables);
		assertEquals(indicatorNames.length, threatTables.size());
		
		for (PotentialTable threatTable : threatTables) {
			assertEquals(variableMap.get(printer.getThreatName()), threatTable.getVariableAt(1));
			assertEquals(variableMap.get(threatTable.getVariableAt(0).getName()), threatTable.getVariableAt(0));
			assertTrue(variableMap.get(threatTable.getVariableAt(0).getName()) == threatTable.getVariableAt(0));
			assertTrue(threatTable.toString(), indicatorNamesList.contains(threatTable.getVariableAt(0).getName()));
		}
	}

	public final void testGetDetectorTables() {
		String[] indicatorNames = {"I1","I2","I3","I4", "I5", "I6"};
		String[] detectorNames = {"D1","D2","D3","D4","D5","D6"};
		printer.setIndicatorNames(indicatorNames);
		printer.setDetectorNames(detectorNames);
		printer.setThreatName("Threat");
		printer.setAlertName("Alert");
		
		Map<String, INode> variableMap = new HashMap<String, INode>();
		List<String> allVars = new ArrayList<String>();
		allVars.add(printer.getThreatName());
		List<String> indicatorNamesList = printer.getNameList(printer.getIndicatorNames());
		assertNotNull(indicatorNamesList);
		assertEquals(indicatorNames.length, indicatorNamesList.size());
		allVars.addAll(indicatorNamesList);
		List<String> detectorNamesList = printer.getNameList(printer.getDetectorNames());
		assertNotNull(detectorNamesList);
		assertEquals(detectorNames.length, detectorNamesList.size());
		allVars.addAll(detectorNamesList);
		allVars.add(printer.getAlertName());
		PotentialTable jointTable = printer.getJointTable(variableMap, allVars);
		assertNotNull(jointTable);
		assertEquals(2 + indicatorNames.length + detectorNames.length, jointTable.getVariablesSize());
		assertFalse(variableMap.isEmpty());
		assertEquals(jointTable.getVariablesSize(),variableMap.size());
		
		List<PotentialTable> detectorTables = printer.getDetectorTables(variableMap, null, indicatorNamesList, detectorNamesList);
		
		assertNotNull(detectorTables);
		assertEquals(indicatorNames.length, detectorTables.size());
		
		for (PotentialTable table : detectorTables) {
			assertEquals(variableMap.get(table.getVariableAt(0).getName()), table.getVariableAt(0));
			assertTrue(variableMap.get(table.getVariableAt(0).getName()) == table.getVariableAt(0));
			assertTrue(table.toString(), detectorNamesList.contains(table.getVariableAt(0).getName()));
			
			assertEquals(variableMap.get(table.getVariableAt(1).getName()), table.getVariableAt(1));
			assertTrue(variableMap.get(table.getVariableAt(1).getName()) == table.getVariableAt(1));
			assertTrue(table.toString(), indicatorNamesList.contains(table.getVariableAt(1).getName()));
		}
	}

}
