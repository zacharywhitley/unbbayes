/**
 * 
 */
package unbbayes.prs.mebn.compiler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import unbbayes.TextModeRunner;
import unbbayes.io.mebn.UbfIO;
import unbbayes.io.mebn.exceptions.IOMebnException;
import unbbayes.prs.Node;
import unbbayes.prs.bn.JunctionTreeAlgorithm;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.compiler.exception.InvalidProbabilityRangeException;
import unbbayes.prs.mebn.compiler.exception.NoDefaultDistributionDeclaredException;
import unbbayes.prs.mebn.exception.MEBNException;
import unbbayes.prs.mebn.kb.powerloom.PowerLoomKB;
import unbbayes.prs.mebn.ssbn.LiteralEntityInstance;
import unbbayes.prs.mebn.ssbn.OVInstance;
import unbbayes.prs.mebn.ssbn.Query;
import unbbayes.util.Debug;

/*
 * Just a data type/class used by some tests
 */
class Conjunto {
	public Node[] pais;

	public Conjunto(Node[] pais) {
		this.pais = pais;
	}
}

/*
 * Just a data type used by some tests
 */
class ListaConjunto {
	public List<Conjunto> conjuntos = new ArrayList<Conjunto>();

	public Map<String, Integer> mapa = new HashMap<String, Integer>();

	public int tamanhoConjunto;

	public ListaConjunto(String[] nomes) {
		this.tamanhoConjunto = nomes.length;
		for (int i = 0; i < nomes.length; ++i) {
			mapa.put(nomes[i], i);
		}
	}
}

/**
 * @author user
 *
 */
public class CompilerTest extends TestCase {
	

	private MultiEntityBayesianNetwork mebn = null; 
	private unbbayes.prs.mebn.compiler.Compiler tableParser = null;
	
	public CompilerTest(String arg0) {
		super(arg0);
		
		
	}

	protected void setUp() throws Exception {
		super.setUp();
		Debug.setDebug(false);
		
		UbfIO ubfIO = UbfIO.getInstance(); 
		
		System.out.println("-----Load file test-----"); 
		
		try{
			mebn = ubfIO.loadMebn(new File("src/test/resources/mebn/StarTrek.ubf")); 
			Debug.println("LOAD COMPLETE"); 
		}
		catch (IOMebnException e){
			e.printStackTrace();
			fail("ERROR IO PROWL!!!!!!!!!"); 
			
		}
		catch (IOException e){			
			e.printStackTrace();
			fail("ERROR IO!!!!!!!!!"); 
		}
		
		
		
		tableParser = Compiler.getInstance((ResidentNode)mebn.getNode("DangerToSelf"));
		assertNotNull(tableParser);
		
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		this.mebn = null;
		this.tableParser = null;
		System.gc();
	}

	
	/*
	 * Test method for 'unbbayes.prs.mebn.compiler.MEBNTableParser.parse()'
	 */
	public void testNormalConsistencyCheck() {
		
		//		 should go all right
		String tableString =  
			" if any st.t have( OperatorSpecies = Cardassian & HarmPotential = true ) " + 
			"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = .99 ]  " +
			" else if any st.z.t have( OperatorSpecies = Romulan & HarmPotential = true ) " +
			"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = .99 ]  " +
			" else if any STj have( OperatorSpecies = Unknown & HarmPotential = true ) " + 
			"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = .99 ]  " +
			" else if any sr.st have( OperatorSpecies = Klingon & HarmPotential = true ) " +
			"  [ Un = 0.10 , Hi = 0.15 , Medium = .15 , Low = .6 ] " +
			" else if any st.z have( OperatorSpecies = Friend & HarmPotential = true ) " +
			"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = .99 ] " +
			" else [ Un = 0 , Hi = 0 , Medium = 0 , Low = 1 ] ";
		
		
		try  {
			tableParser.parse(tableString);
		} catch (MEBNException e) {
			e.printStackTrace();
			fail(e.getMessage());
			
		} 
		
		//tableParser.getTempTable();
		
	}
	
	/*
	 * Test method for 'unbbayes.prs.mebn.compiler.MEBNTableParser.parse()'
	 */
	public void testNormalConsistencyCheckWithParenthesisAtBoolExpression() {
		
		//		 should go all right
		String tableString =  
			" if any st.t have( OperatorSpecies = Cardassian & (HarmPotential = true) ) " + 
			"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = .99 ]  " +
			" else if any st.z.t have( (OperatorSpecies = Romulan & HarmPotential = true) ) " +
			"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = .99 ]  " +
			" else if any STj have( (OperatorSpecies = Unknown & HarmPotential = true) | (OperatorSpecies = Unknown & HarmPotential = true) ) " + 
			"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = .99 ]  " +
			" else if any sr.st have( OperatorSpecies = Klingon & ~ HarmPotential = true ) " +
			"  [ Un = 0.10 , Hi = 0.15 , Medium = .15 , Low = .6 ] " +
			" else if any st.z have( OperatorSpecies = Friend & HarmPotential = true ) " +
			"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = .99 ] " +
			" else [ Un = 0 , Hi = 0 , Medium = 0 , Low = 1 ] ";
		
		
		try  {
			tableParser.parse(tableString);
		} catch (MEBNException e) {
			e.printStackTrace();
			fail(e.getMessage());
			
		} 
		
		//tableParser.getTempTable();
		
	}
	
	/*
	 * Test if parser detect no default distribution table
	 * or else clause undeclared
	 *
	 */
	public void testConsistencyNoDefDistro() {
		//		 Should fail, no default distro
		String tableString =  
			" if any st have( OperatorSpecies = Cardassian & HarmPotential = true ) " + 
			"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = .99 ]  " +
			" else if any st have( OperatorSpecies = Romulan & HarmPotential = true ) " +
			"  [ Un = 0 , Hi = MIN(0;1) , Medium = .01 , Low = .99 ]  " +
			" else if any st have( OperatorSpecies = Unknown & HarmPotential = true ) " + 
			"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = MAX(0;.99) ]  " +
			" else if any xyz have( OperatorSpecies = Klingon & HarmPotential = true ) " +
			"  [ Un = 0.10 , Hi = CARDINALITY(xyz)*0.1 , Medium = .15 , Low = .75 ] " +
			" else if any st have( OperatorSpecies = Friend & HarmPotential = true ) " +
			"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = .99 ] " ;
		
			
		try  {
			tableParser.parse(tableString);
		} catch (NoDefaultDistributionDeclaredException e) {
			// pass
		} catch (MEBNException e) {
			e.printStackTrace();
			fail(e.getMessage() + " at " + tableString.substring(tableParser.getIndex()-1,tableParser.getIndex()+10));
			
			System.exit(1);
		} 
		
		
		//		 Should fail, no else clause
		tableString =  
			" if any STi have( OperatorSpecies = Cardassian & HarmPotential = true ) " + 
			"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = .99 ]  " +
			" if any STj have( OperatorSpecies = Romulan & HarmPotential = true ) " +
			"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = .99 ]  " +
			" if any STj have( OperatorSpecies = Unknown & HarmPotential = true ) " + 
			"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = .99 ]  " +
			" if any STk have( OperatorSpecies = Klingon & HarmPotential = true ) " +
			"  [ Un = 0.10 , Hi = 0.15 , Medium = .15 , Low = .6 ] " +
			" else if any STl have( OperatorSpecies = Friend & HarmPotential = true ) " +
			"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = .99 ] " ;
		
			
		try  {
			Debug.println("=> PARSING A NO-ELSE CLAUSE TABLE");
			tableParser.parse(tableString);
		} catch (NoDefaultDistributionDeclaredException e) {
			// pass
		} catch (MEBNException e) {
			fail(e.getMessage()+ ", " + e.getClass().getName());
		} 
		
	}
	
	/*
	 * Tests if a single node state is between [0,1]
	 */
	public void testConsistencySingleState() {

		//		 Should fail, some state is above 1
		String tableString =  
			" if any STi have( OperatorSpecies = Cardassian & HarmPotential = true ) " + 
			"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = .99 ]  " +
			" else if any STj have( OperatorSpecies = Romulan & HarmPotential = true ) " +
			"  [ Un = 2 , Hi = 0 , Medium = .01 , Low = .99 ]  " +
			" else if any STj have( OperatorSpecies = Unknown & HarmPotential = true ) " + 
			"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = .99 ]  " +
			" else if any STk have( OperatorSpecies = Klingon & HarmPotential = true ) " +
			"  [ Un = 0.10 , Hi = 0.15 , Medium = .15 , Low = .6 ] " +
			" else if any STl have( OperatorSpecies = Friend & HarmPotential = true ) " +
			"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = .99 ] " +
			" else [ Un = 0 , Hi = 0 , Medium = 0 , Low = 1 ] ";
		
		
		try  {
			tableParser.parse(tableString);
		} catch (InvalidProbabilityRangeException e) {
			// pass
		} catch (MEBNException e) {
			fail(e.getMessage());
		} 

		//		 Should fail, some state is below 0
		tableString =  
			" if any STi have( OperatorSpecies = Cardassian & HarmPotential = true ) " + 
			"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = .99 ]  " +
			" else if any STj have( OperatorSpecies = Romulan & HarmPotential = true ) " +
			"  [ Un = 2 , Hi = 0 , Medium = .01 , Low = .99 ]  " +
			" else if any STj have( OperatorSpecies = Unknown & HarmPotential = true ) " + 
			"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = .99 ]  " +
			" else if any STk have( OperatorSpecies = Klingon & HarmPotential = true ) " +
			"  [ Un = 0.10 , Hi = 0.15 , Medium = .15 , Low = .60 ] " +
			" else if any STl have( OperatorSpecies = Friend & HarmPotential = true ) " +
			"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = .99 ] " +
			" else [ Un = 0 , Hi = 0.5 , Medium = -0.5 , Low = 1 ] ";
		
		
		try  {
			tableParser.parse(tableString);
		} catch (InvalidProbabilityRangeException e) {
			// pass
		} catch (MEBNException e) {
			fail(e.getMessage());
		} 
		
		
	}
	
	
	public void testProbDistroSum() {

		
		//		 Should fail sum is above 1 in default distro
		String tableString =  
			" if any STi have( OperatorSpecies = Cardassian & HarmPotential = true ) " + 
			"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = .99 ]  " +
			" else if any STj have( OperatorSpecies = Romulan & HarmPotential = true ) " +
			"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = .99 ]  " +
			" else if any STj have( OperatorSpecies = Unknown & HarmPotential = true ) " + 
			"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = .99 ]  " +
			" else if any STk have( OperatorSpecies = Klingon & HarmPotential = true ) " +
			"  [ Un = 0.10 , Hi = 0.15 , Medium = .15 , Low = .6 ] " +
			" else if any STl have( OperatorSpecies = Friend & HarmPotential = true ) " +
			"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = .99 ] " +
			" else [ Un = 0 , Hi = 0.5 , Medium = 0 , Low = 1 ] ";
		
		
		
		try  {
			tableParser.parse(tableString);
		} catch (InvalidProbabilityRangeException e) {
			// pass
		} catch (MEBNException e) {
			fail(e.getMessage());
		} 
		
		
		//		 Should fail sum is above 1
		tableString =  
			" if any STi have( OperatorSpecies = Cardassian & HarmPotential = true ) " + 
			"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = .99 ]  " +
			" else if any STj have( OperatorSpecies = Romulan & HarmPotential = true ) " +
			"  [ Un = 0 , Hi = 0.2 , Medium = .01 , Low = .99 ]  " +
			" else if any STj have( OperatorSpecies = Unknown & HarmPotential = true ) " + 
			"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = .99 ]  " +
			" else if any STk have( OperatorSpecies = Klingon & HarmPotential = true ) " +
			"  [ Un = 0.10 , Hi = 0.15 , Medium = .15 , Low = .65 ] " +
			" else if any STl have( OperatorSpecies = Friend & HarmPotential = true ) " +
			"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = .99 ] " +
			" else [ Un = 0 , Hi = 0 , Medium = 0 , Low = 1 ] ";
		
		try  {
			tableParser.parse(tableString);
		} catch (InvalidProbabilityRangeException e) {
			// pass
		} catch (MEBNException e) {
			fail(e.getMessage());
		} 
		
		
		//		 Should fail sum is below 1
		tableString =  
			" if any STi have( OperatorSpecies = Cardassian & HarmPotential = true ) " + 
			"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = .99 ]  " +
			" else if any STj have( OperatorSpecies = Romulan & HarmPotential = true ) " +
			"  [ Un = 0 , Hi = 0 , Medium = .0 , Low = .99 ]  " +
			" else if any STj have( OperatorSpecies = Unknown & HarmPotential = true ) " + 
			"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = .99 ]  " +
			" else if any STk have( OperatorSpecies = Klingon & HarmPotential = true ) " +
			"  [ Un = 0.10 , Hi = 0.15 , Medium = .15 , Low = .6 ] " +
			" else if any STl have( OperatorSpecies = Friend & HarmPotential = true ) " +
			"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = .99 ] " +
			" else [ Un = 0 , Hi = 0 , Medium = 0 , Low = 1 ] ";
		
		try  {
			tableParser.parse(tableString);
		} catch (InvalidProbabilityRangeException e) {
			// pass
		} catch (MEBNException e) {
			fail(e.getMessage());
		} 
		
		
		//		 Should fail sum is below 1 in default distribution
		tableString =  
			" if any STi have( OperatorSpecies = Cardassian & HarmPotential = true ) " + 
			"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = .99 ]  " +
			" else if any STj have( OperatorSpecies = Romulan & HarmPotential = true ) " +
			"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = .99 ]  " +
			" else if any STj have( OperatorSpecies = Unknown & HarmPotential = true ) " + 
			"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = .99 ]  " +
			" else if any STk have( OperatorSpecies = Klingon & HarmPotential = true ) " +
			"  [ Un = 0.10 , Hi = 0.15 , Medium = .15 , Low = .6 ] " +
			" else if any STl have( OperatorSpecies = Friend & HarmPotential = true ) " +
			"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = .99 ] " +
			" else [ Un = 0 , Hi = 0 , Medium = 0 , Low = 0 ] ";
		
		try  {
			tableParser.parse(tableString);
		} catch (InvalidProbabilityRangeException e) {
			// pass
		} catch (MEBNException e) {
			fail(e.getMessage());
		} 
		
		
		
	}
	
	public void testNormalConsistencyCheckWithFunctions() {
		//		 should go all right
		String tableString =  
			" if any st have( OperatorSpecies = Cardassian & HarmPotential = true ) " + 
			"  [ Un = 0 , Hi = 0 , Medium = MIN ( CARDINALITY (OperatorSpecies) * 2 ; .2 ) , Low = 1 - Medium ]  " +
			" else if any st.t have( OperatorSpecies = Romulan & HarmPotential = true ) " +
			"  [ Un = 0 , Hi = 0 , Medium = (.005 + .005) , Low = .99 ]  " +
			" else if any STj have( OperatorSpecies = Unknown & HarmPotential = true ) " + 
			"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = (((1 - Medium) - Un) - Hi ) ]  " +
			" else if any st.z have( OperatorSpecies = Klingon & HarmPotential = true ) " +
			"  [ Un = 0.10 , Hi = 0.15 , Medium = .15 , Low = .6 ] " +
			" else if any st.sr.z have( OperatorSpecies = Friend & HarmPotential = true ) " +
			"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = .99 ] " +
			" else [ Un = 0 , Hi = 0 , Medium = MAX (.5 ; CARDINALITY (HarmPotential)) , Low = 1 - Medium ] ";
		
		
		try  {
			tableParser.parse(tableString);
		} catch (MEBNException e) {
			e.printStackTrace();
			fail(e.getMessage() + " at index " + tableParser.getIndex());
		} 
		
	}
	
	
	public void testNoIfStatement() {
		//		 should go all right
		String tableString =  
			" [ Un = 0 , Hi = 0 , Medium = MIN ( -3 * -2 ; .2 ) , Low = 1 - Medium ]  ";		
		try  {
			tableParser.parse(tableString);
		} catch (MEBNException e) {
			e.printStackTrace();
			fail(e.getMessage() + " at index " + tableParser.getIndex());
			
		} 
		
	}
	
	
	public void testNestedStatements() {
		String tableString =  
			" if any st.t have( OperatorSpecies = Cardassian & (HarmPotential = true) )  " + 
			"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = .99 ]  " +
			" else if any st.z.t have( (OperatorSpecies = Romulan & HarmPotential = true) ) " +
			"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = .99 ]  " +
			" else if any STj have( (OperatorSpecies = Unknown & HarmPotential = true) | (OperatorSpecies = Unknown & HarmPotential = true) ) " + 
			"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = .99 ]  " +
			" else if any sr.st have( OperatorSpecies = Klingon & ~ HarmPotential = true ) " +
			"  [ [ Un = 0.10 , Hi = 0.15 , Medium = .15 , Low = .6 ] ] " +
			" else if any st.z have( OperatorSpecies = Friend & HarmPotential = true ) " +
			"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = .99 ] " +
			" else [ Un = 0 , Hi = 0 , Medium = 0 , Low = 1 ] ";
		
		
		try  {
			tableParser.parse(tableString);
			fail("No nested statement is allowed");
		} catch (MEBNException e) {
			//OK
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		//assertNotNull(tableParser.getTempTable());
		
	}
	
	
	public void testNestedIfs() {
		String tableString =  
		" if any st0 have( (OperatorSpecies = Unknown & HarmPotential = true) | (OperatorSpecies = Unknown & HarmPotential = true) ) " + 
			"  [ Un = 0 , Hi = .1 , Medium = .0 , Low = .9 ]  " +
		" else if any st1 have( OperatorSpecies = Cardassian & (HarmPotential = true) )  [ " + 
			" if any st11 have ( ~ OperatorSpecies = Cardassian ) " +
			"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = .99 ]  " +
			" else [ Un = MIN ( .9 ; CARDINALITY(st.t) ) , Hi = 0 , Medium = 0 , Low = 1 - Un ] " +
		" ] else if any st2 have( (OperatorSpecies = Romulan & HarmPotential = true) ) " +
		"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = .99 ]  " +
		" else if any st3 have( (OperatorSpecies = Unknown & HarmPotential = true) | (OperatorSpecies = Unknown & HarmPotential = true) ) " + 
		"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = .99 ]  " +
		" else if any st4 have( OperatorSpecies = Klingon & ~ HarmPotential = true ) [ " +
			" if any st41 have ( ~ OperatorSpecies = Klingon ) [ " +
				" if any st411 have ( ~ OperatorSpecies = Cardassian ) " +
				"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = .99 ]  " +
				" else [ Un = MIN ( .9 ; CARDINALITY(sr) ) , Hi = 0 , Medium = 0 , Low = 1 - Un ] " +
			" ] else if any st42 have ( ~ OperatorSpecies = Klingon ) [ " +
				" if any st421 have ( ~ OperatorSpecies = Cardassian ) " +
				"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = .99 ]  " +
				" else [ Un = MIN ( .9 ; CARDINALITY(sr) ) , Hi = 0 , Medium = 0 , Low = 1 - Un ] " +
			" ] else [ Un = MIN ( .9 ; CARDINALITY(sr.st) ) , Hi = 0 , Medium = 0 , Low = 1 - Un ] "  +
		" ] else if any st5 have( OperatorSpecies = Friend & HarmPotential = true ) " +
		"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = .99 ] " +
		" else [ " +
			" if any stelse have ( ~ OperatorSpecies = Cardassian ) " +
			"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = .99 ]  " +
			" else [ Un = 0 , Hi = 0 , Medium = 0 , Low = 1 ] " +
		" ] ";
		
		
		try  {
			tableParser.parse(tableString);			
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		
	}
	
	
	public void testNestedDefaultDistro() {
		String tableString =  
			"[ if any st1 have( OperatorSpecies = Cardassian & (HarmPotential = true) )  [ " + 
				" if any st11 have ( ~ OperatorSpecies = Cardassian ) " +
				"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = .99 ]  " +
				" else [ Un = MIN ( .9 ; CARDINALITY(st.t) ) , Hi = 0 , Medium = 0 , Low = 1 - Un ] " +
			" ] else if any st2 have( (OperatorSpecies = Romulan & HarmPotential = true) ) " +
			"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = .99 ]  " +
			" else if any st3 have( (OperatorSpecies = Unknown & HarmPotential = true) | (OperatorSpecies = Unknown & HarmPotential = true) ) " + 
			"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = .99 ]  " +
			" else if any st4 have( OperatorSpecies = Klingon & ~ HarmPotential = true ) [ " +
				" if any st41 have ( ~ OperatorSpecies = Klingon ) [ " +
					" if any st411 have ( ~ OperatorSpecies = Cardassian ) " +
					"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = .99 ]  " +
					" else [ Un = MIN ( .9 ; CARDINALITY(sr) ) , Hi = 0 , Medium = 0 , Low = 1 - Un ] " +
				" ] else if any st42 have ( ~ OperatorSpecies = Klingon ) [ " +
					" if any st421 have ( ~ OperatorSpecies = Cardassian ) " +
					"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = .99 ]  " +
					" else [ Un = MIN ( .9 ; CARDINALITY(sr) ) , Hi = 0 , Medium = 0 , Low = 1 - Un ] " +
				" ] else [ Un = MIN ( .9 ; CARDINALITY(sr.st) ) , Hi = 0 , Medium = 0 , Low = 1 - Un ] "  +
			" ] else if any st5 have( OperatorSpecies = Friend & HarmPotential = true ) " +
			"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = .99 ] " +
			" else [ " +
				" if any stelse have ( ~ OperatorSpecies = Cardassian ) " +
				"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = .99 ]  " +
				" else [ Un = 0 , Hi = 0 , Medium = 0 , Low = 1 ] " +
			" ] ]";
		
		
		try  {
			tableParser.parse(tableString);			
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		
	}
	
	public void testUnnormalized() {
		
		// make a backup, so that we can restore later
		boolean backup = tableParser.isToNormalize();
		
		// force this compiler not to consider normalized number (i.e. start considering non-probability values)
		tableParser.setToNormalize(false);
		assertFalse(tableParser.isToNormalize());
		
		String tableString =  
				"[ if any st1 have( OperatorSpecies = Cardassian & (HarmPotential = true) )  [ " + 
						" if any st11 have ( ~ OperatorSpecies = Cardassian ) " +
						"  [ Un = 0 , Hi = 0 , Medium = 100 , Low = .9 ]  " +
						" else [ Un = MIN ( .9 ; CARDINALITY(st.t) ) , Hi = 1000 , Medium = -10 , Low = 1 - Un ] " +
						" ] else if any st2 have( (OperatorSpecies = Romulan & HarmPotential = true) ) " +
						"  [ Un = -10 , Hi = 100 , Medium = .01 , Low = -.99 ]  " +
						" else if any st3 have( (OperatorSpecies = Unknown & HarmPotential = true) | (OperatorSpecies = Unknown & HarmPotential = true) ) " + 
						"  [ Un = 0 , Hi = -10 , Medium = -.01 , Low = .99 ]  " +
						" else if any st4 have( OperatorSpecies = Klingon & ~ HarmPotential = true ) [ " +
						" if any st41 have ( ~ OperatorSpecies = Klingon ) [ " +
						" if any st411 have ( ~ OperatorSpecies = Cardassian ) " +
						"  [ Un = 0 , Hi = 9999 , Medium = .01 , Low = .99 ]  " +
						" else [ Un = MIN ( 1 ; 1+CARDINALITY(sr) ) , Hi = 0 , Medium = 0 , Low = 1 - Un ] " +
						" ] else if any st42 have ( ~ OperatorSpecies = Klingon ) [ " +
						" if any st421 have ( ~ OperatorSpecies = Cardassian ) " +
						"  [ Un = 0 , Hi = 0 , Medium = .0 , Low = 0 ]  " +
						" else [ Un = MIN ( .9 ; CARDINALITY(sr) ) , Hi = 0 , Medium = 0 , Low = 1 - Un ] " +
						" ] else [ Un = MIN ( .9 ; CARDINALITY(sr.st) ) , Hi = 0 , Medium = 0 , Low = 1 - Un ] "  +
						" ] else if any st5 have( OperatorSpecies = Friend & HarmPotential = true ) " +
						"  [ Un = 10  ] " +
						" else [ " +
						" if any stelse have ( ~ OperatorSpecies = Cardassian ) " +
						"  [ Un = -100  ]  " +
						" else [ Un = 0  ] " +
						" ] ]";
		
		
		try  {
			tableParser.parse(tableString);			
		} catch (Exception e) {
			e.printStackTrace();
			fail(tableParser.getIndex() + ", "+e.getMessage());
			
		} finally {
			tableParser.setToNormalize(backup);
		}
		
	}
	
	public void testEmbeddedIdentity() {
		UbfIO io = UbfIO.getInstance();
		
		MultiEntityBayesianNetwork mebn = null;
		try {
			mebn = io.loadMebn(new File("./src/test/resources/mebn/EmbeddedIDTest.ubf"));
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		assertNotNull(mebn);
		
		TextModeRunner runner = new TextModeRunner();
		
		ResidentNode resident = mebn.getDomainResidentNode("EmbeddedID");
		OrdinaryVariable ov = resident.getOrdinaryVariableByName("ov");
		
		String stateToCheck = "c";
		
		OVInstance ovInstance = OVInstance.getInstance(ov , LiteralEntityInstance.getInstance(stateToCheck, ov.getValueType()));
		Query query = new Query(resident, Collections.singletonList(ovInstance ));
		ProbabilisticNetwork result = null;
		try {
			result = runner.executeQueryLaskeyAlgorithm(Collections.singletonList(query), PowerLoomKB.getNewInstanceKB(), mebn);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		assertNotNull(result);
		assertEquals(1, result.getNodeCount());
		ProbabilisticNode node = (ProbabilisticNode) result.getNodeAt(0);
		assertNotNull(node);
		
		// find the state c
		int indexOfStateC = 0;
		for (; indexOfStateC < node.getStatesSize(); indexOfStateC++) {
			if (node.getStateAt(indexOfStateC).equalsIgnoreCase(stateToCheck)) {
				break;
			}
		}
		assertTrue(indexOfStateC < node.getStatesSize());
		assertEquals(stateToCheck, node.getStateAt(indexOfStateC));
		
		assertEquals(.9, node.getMarginalAt(indexOfStateC), .00005);
		
		
		stateToCheck = "a";
		
		ovInstance = OVInstance.getInstance(ov , LiteralEntityInstance.getInstance(stateToCheck, ov.getValueType()));
		query = new Query(resident, Collections.singletonList(ovInstance ));
		result = null;
		try {
			result = runner.executeQueryLaskeyAlgorithm(Collections.singletonList(query), PowerLoomKB.getNewInstanceKB(), mebn);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		assertNotNull(result);
		assertEquals(1, result.getNodeCount());
		node = (ProbabilisticNode) result.getNodeAt(0);
		assertNotNull(node);
		
		// find the state c
		indexOfStateC = 0;
		for (; indexOfStateC < node.getStatesSize(); indexOfStateC++) {
			if (node.getStateAt(indexOfStateC).equalsIgnoreCase(stateToCheck)) {
				break;
			}
		}
		assertTrue(indexOfStateC < node.getStatesSize());
		assertEquals(stateToCheck, node.getStateAt(indexOfStateC));
		
		assertEquals(.9, node.getMarginalAt(indexOfStateC), .00005);
		
		
		stateToCheck = "b";
		
		ovInstance = OVInstance.getInstance(ov , LiteralEntityInstance.getInstance(stateToCheck, ov.getValueType()));
		query = new Query(resident, Collections.singletonList(ovInstance ));
		result = null;
		try {
			result = runner.executeQueryLaskeyAlgorithm(Collections.singletonList(query), PowerLoomKB.getNewInstanceKB(), mebn);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		assertNotNull(result);
		assertEquals(1, result.getNodeCount());
		node = (ProbabilisticNode) result.getNodeAt(0);
		assertNotNull(node);
		
		// find the state c
		indexOfStateC = 0;
		for (; indexOfStateC < node.getStatesSize(); indexOfStateC++) {
			if (node.getStateAt(indexOfStateC).equalsIgnoreCase(stateToCheck)) {
				break;
			}
		}
		assertTrue(indexOfStateC < node.getStatesSize());
		assertEquals(stateToCheck, node.getStateAt(indexOfStateC));
		
		assertEquals(.9, node.getMarginalAt(indexOfStateC), .00005);
		
		stateToCheck = "d";
		
		ovInstance = OVInstance.getInstance(ov , LiteralEntityInstance.getInstance(stateToCheck, ov.getValueType()));
		query = new Query(resident, Collections.singletonList(ovInstance ));
		result = null;
		try {
			result = runner.executeQueryLaskeyAlgorithm(Collections.singletonList(query), PowerLoomKB.getNewInstanceKB(), mebn);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		assertNotNull(result);
		assertEquals(1, result.getNodeCount());
		node = (ProbabilisticNode) result.getNodeAt(0);
		assertNotNull(node);
		
		// find the state c
		indexOfStateC = 0;
		for (; indexOfStateC < node.getStatesSize(); indexOfStateC++) {
			if (node.getStateAt(indexOfStateC).equalsIgnoreCase(stateToCheck)) {
				break;
			}
		}
		assertTrue(indexOfStateC < node.getStatesSize());
		assertEquals(stateToCheck, node.getStateAt(indexOfStateC));
		
		assertEquals(.9, node.getMarginalAt(indexOfStateC), .00005);
		
		stateToCheck = "e";
		
		ovInstance = OVInstance.getInstance(ov , LiteralEntityInstance.getInstance(stateToCheck, ov.getValueType()));
		query = new Query(resident, Collections.singletonList(ovInstance ));
		result = null;
		try {
			result = runner.executeQueryLaskeyAlgorithm(Collections.singletonList(query), PowerLoomKB.getNewInstanceKB(), mebn);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		assertNotNull(result);
		assertEquals(1, result.getNodeCount());
		node = (ProbabilisticNode) result.getNodeAt(0);
		assertNotNull(node);
		
		// find the state c
		indexOfStateC = 0;
		for (; indexOfStateC < node.getStatesSize(); indexOfStateC++) {
			if (node.getStateAt(indexOfStateC).equalsIgnoreCase(stateToCheck)) {
				break;
			}
		}
		assertTrue(indexOfStateC < node.getStatesSize());
		assertEquals(stateToCheck, node.getStateAt(indexOfStateC));
		
		assertEquals(1f, node.getMarginalAt(indexOfStateC), .00005);
		
	}
	
	public void testLooseStrongOV() {
		UbfIO io = UbfIO.getInstance();
		
		MultiEntityBayesianNetwork mebn = null;
		try {
			mebn = io.loadMebn(new File("./src/test/resources/mebn/VarSetNameTest.ubf"));
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		assertNotNull(mebn);
		
		TextModeRunner runner = new TextModeRunner();
		
		ResidentNode resident = mebn.getDomainResidentNode("NodeDistance");
		OrdinaryVariable n1 = resident.getOrdinaryVariableByName("n1");
		OrdinaryVariable n2 = resident.getOrdinaryVariableByName("n2");
		
		
		List<OVInstance> queryArguments = new ArrayList<OVInstance>(2);
		queryArguments.add(OVInstance.getInstance(n1 , LiteralEntityInstance.getInstance("N1", n1.getValueType())));
		queryArguments.add(OVInstance.getInstance(n2 , LiteralEntityInstance.getInstance("N2", n2.getValueType())));
		Query query = new Query(resident, queryArguments);
		ProbabilisticNetwork result = null;
		try {
			result = runner.executeQueryLaskeyAlgorithm(Collections.singletonList(query), PowerLoomKB.getNewInstanceKB(), mebn);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		assertNotNull(result);
		
		// extract node to check for probabilities
		ProbabilisticNode node = (ProbabilisticNode) result.getNode("NodeDistance__N1_N2");
		assertNotNull(node);
		
		// extract nodes to insert finding
		ProbabilisticNode isLocatedIn__N1_R1 = (ProbabilisticNode) result.getNode("isLocatedIn__N1_R1");
		assertNotNull(isLocatedIn__N1_R1);
		ProbabilisticNode isLocatedIn__N1_R2 = (ProbabilisticNode) result.getNode("isLocatedIn__N1_R2");
		assertNotNull(isLocatedIn__N1_R2);
		ProbabilisticNode isLocatedIn__N2_R1 = (ProbabilisticNode) result.getNode("isLocatedInCopy__N2_R1");
		assertNotNull(isLocatedIn__N2_R1);
		ProbabilisticNode isLocatedIn__N2_R2 = (ProbabilisticNode) result.getNode("isLocatedInCopy__N2_R2");
		assertNotNull(isLocatedIn__N2_R2);
		
		// check that indexes of states are OK
		assertEquals("true", isLocatedIn__N1_R1.getStateAt(0));
		assertEquals("true", isLocatedIn__N1_R2.getStateAt(0));
		assertEquals("true", isLocatedIn__N2_R1.getStateAt(0));
		assertEquals("true", isLocatedIn__N2_R2.getStateAt(0));
		assertEquals("false", isLocatedIn__N1_R1.getStateAt(1));
		assertEquals("false", isLocatedIn__N1_R2.getStateAt(1));
		assertEquals("false", isLocatedIn__N2_R1.getStateAt(1));
		assertEquals("false", isLocatedIn__N2_R2.getStateAt(1));
		
		// prepare a junction tree algorithm in order to add findings to node
		JunctionTreeAlgorithm algorithm = new JunctionTreeAlgorithm(result);
		algorithm.run();
		
		// check the case when IP nodes N1 and N2 are in different regions
		
		// set that N1 is located in R1, and N2 in R2
		isLocatedIn__N1_R1.addFinding(0);
		isLocatedIn__N1_R2.addFinding(1);
		isLocatedIn__N2_R1.addFinding(1);
		isLocatedIn__N2_R2.addFinding(0);
		
		// propagate finding
		algorithm.propagate();
		
		// find the state "long"
		int indexOfLong = 0;
		for (; indexOfLong < node.getStatesSize(); indexOfLong++) {
			if (node.getStateAt(indexOfLong).equalsIgnoreCase("long")) {
				break;
			}
		}
		assertTrue(indexOfLong < node.getStatesSize());
		assertEquals("long", node.getStateAt(indexOfLong));
		
		// check that the distance between the IP nodes is long now
		assertEquals(1, node.getMarginalAt(indexOfLong), .00005);
		
		algorithm.reset();  // now, reset the probability
		
		// set that N1 is located in R2, and N2 in R1
		isLocatedIn__N1_R1.addFinding(1);
		isLocatedIn__N1_R2.addFinding(0);
		isLocatedIn__N2_R1.addFinding(0);
		isLocatedIn__N2_R2.addFinding(1);
		
		// propagate finding
		algorithm.propagate();
		
		// check that the distance between the IP nodes is long
		assertEquals(1, node.getMarginalAt(indexOfLong), .00005);
		
		algorithm.reset();  // reset the probability now
		
		// do the same check with a configuration when both N1 and N2 are in same regions
		
		// set both IP nodes to R1
		isLocatedIn__N1_R1.addFinding(0);
		isLocatedIn__N1_R2.addFinding(1);
		isLocatedIn__N2_R1.addFinding(0);
		isLocatedIn__N2_R2.addFinding(1);
		
		// propagate finding
		algorithm.propagate();
		
		// find the state "long"
		int indexOfShort = 0;
		for (; indexOfShort < node.getStatesSize(); indexOfShort++) {
			if (node.getStateAt(indexOfShort).equalsIgnoreCase("short")) {
				break;
			}
		}
		assertTrue(indexOfShort < node.getStatesSize());
		assertEquals("short", node.getStateAt(indexOfShort));
		
		// check that the distance between the IP nodes is short now
		assertEquals(1, node.getMarginalAt(indexOfShort), .00005);
		
		algorithm.reset();  // reset the probability now
		
		// set both IP nodes to R2
		isLocatedIn__N1_R1.addFinding(1);
		isLocatedIn__N1_R2.addFinding(0);
		isLocatedIn__N2_R1.addFinding(1);
		isLocatedIn__N2_R2.addFinding(0);
		
		// propagate finding
		algorithm.propagate();
		
		// check that the distance between the IP nodes is still short
		assertEquals(1, node.getMarginalAt(indexOfShort), .00005);
		
	}
	
	/**
	 * This method tests support for noes with no arguments as parent of current node (whose LPD is being compiled)
	 */
	public void testNoArgumentNode() {
		UbfIO io = UbfIO.getInstance();
		
		MultiEntityBayesianNetwork mebn = null;
		try {
			mebn = io.loadMebn(new File("./src/test/resources/noArgumentNode.ubf"));
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		assertNotNull(mebn);
		
		TextModeRunner runner = new TextModeRunner();
		
		ResidentNode resident = mebn.getDomainResidentNode("MyNode");
		OrdinaryVariable x = resident.getOrdinaryVariableByName("x");
		
		
		List<OVInstance> queryArguments = new ArrayList<OVInstance>(2);
		queryArguments.add(OVInstance.getInstance(x , LiteralEntityInstance.getInstance("a", x.getValueType())));
		Query query = new Query(resident, queryArguments);
		ProbabilisticNetwork result = null;
		try {
			result = runner.executeQueryLaskeyAlgorithm(Collections.singletonList(query), PowerLoomKB.getNewInstanceKB(), mebn);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		assertNotNull(result);
		
		// extract node to check for probabilities
		ProbabilisticNode node = (ProbabilisticNode) result.getNode("MyNode__a");
		assertNotNull(node);
		
		// find the state "absurd"
		int indexOfAbsurd = 0;
		for (; indexOfAbsurd < node.getStatesSize(); indexOfAbsurd++) {
			if (node.getStateAt(indexOfAbsurd).equalsIgnoreCase("absurd")) {
				break;
			}
		}
		assertTrue(indexOfAbsurd < node.getStatesSize());
		assertEquals("absurd", node.getStateAt(indexOfAbsurd));
		
		// check that it is not absurd 100% (with error margin 0.00005)
		assertFalse("Marginal = " + node.getMarginalAt(indexOfAbsurd), node.getMarginalAt(indexOfAbsurd) >= 1-0.00005);
		
		assertEquals(0.7999, node.getMarginalAt(0), 0.00005);
		assertEquals(0.2, node.getMarginalAt(1), 0.00005);
		assertEquals(1.0E-4, node.getMarginalAt(2), 0.00005);
		
	}
	
	/**
	 * This method tests support for user-defined variables being dynamically defined
	 * @throws MEBNException
	 */
	@SuppressWarnings("deprecation")
	public void testUserDefinedVariable() throws MEBNException {
		UbfIO io = UbfIO.getInstance();
		
		MultiEntityBayesianNetwork mebn = null;
		try {
			mebn = io.loadMebn(new File("./src/test/resources/twoNodeExample.ubf"));
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		assertNotNull(mebn);
		
		// extract the node to change lpd
		ResidentNode resident = mebn.getDomainResidentNode("RX2");
		
		// change the lpd of the node to some invalid script
		resident.setTableFunction(
					"if any x have (RX1 = true) [ "
				+		"var1 = 0.9, true = var1, false = 1 - var1"
				+	"] else if any x have (RX1 = false) [ "
				+		"true = 1, "
				+		"if all x have (RX1 = false) [ "
				+			"var2 = 0.1, true = var2, false = 1 - true"
				+		"] else [ "
				+			"false = 0.75, true = 1-false"
				+		"]  "
				+	"] else [ "
				+		"var3 = 1, absurd = var3"
				+	"] "
				);
		
		resident.getCompiler().init(resident.getTableFunction());
		try {
			resident.getCompiler().parse();
			fail("Should throw compilation error");
		} catch (MEBNException e1) {
			// make sure correct exception message is used
			assertTrue(e1.getMessage().contains(
						unbbayes.util.ResourceController.newInstance().getBundle(
								unbbayes.prs.mebn.compiler.resources.Resources.class.getName()
						).getString("NonUserDefinedVariablesFoundBeforeIfClause")
					)
				);
		}
		// change the lpd of the node to some invalid script
		resident.setTableFunction(
				"if any x have (RX1 = true) [ "
						+		"var1 = 0.9, true = var1, false = 1 - var1"
						+	"] else if any x have (RX1 = false) [ "
						+		"if all x have (RX1 = false) [ "
						+			"var2 = 0.1, true = var2, false = 1 - true"
						+		"] else [ "
						+			"false = 0.75, true = 1-false"
						+		"]  "
						+		"true = 1, "
						+	"] else [ "
						+		"var3 = 1, absurd = var3"
						+	"] "
				);
		
		resident.getCompiler().init(resident.getTableFunction());
		try {
			resident.getCompiler().parse();
			fail("Should throw compilation error");
		} catch (MEBNException e1) {
			// OK
		}
		// change the lpd of the node to some invalid script
		resident.setTableFunction(
				"if any x have (RX1 = true) [ "
						+		"var1 = 0.9, true = var1, false = 1 - var1"
						+	"] else if any x have (RX1 = false) [ "
						+		"if all x have (RX1 = false) [ "
						+			"var2 = 0.1, true = var2, false = 1 - true"
						+		"] else [ "
						+			"false = 0.75, true = 1-false"
						+		"]  "
						+		"var2 = 1, "
						+	"] else [ "
						+		"var3 = 1, absurd = var3"
						+	"] "
				);
		
		resident.getCompiler().init(resident.getTableFunction());
		try {
			resident.getCompiler().parse();
			fail("Should throw compilation error");
		} catch (MEBNException e1) {
			// OK
		}
		
		// change the lpd of the node to some valid script
		resident.setTableFunction(
				"if any x have (RX1 = true) [ "
						+		"var1 = 0.9, true = var1, false = 1 - var1"
						+	"] else if any x have (RX1 = false) [ "
						+		"if all x have (RX1 = false) [ "
						+			"var2 = 0.1, true = var2, false = 1 - true"
						+		"] else [ "
						+			"false = 0.75, true = 1-false"
						+		"]  "
						+	"] else [ "
						+		"var3 = 1, absurd = var3"
						+	"] "
				);
		
		resident.getCompiler().init(resident.getTableFunction());
		resident.getCompiler().parse();	// now should pass
		
		// change the lpd of the node to some valid script
		resident.setTableFunction(
				"if any x have (RX1 = true) [ "
						+		"var1 = 0.9, true = var1, false = 1 - var1"
						+	"] else if any x have (RX1 = false) [ "
						+		"var2 = 0.1, "
						+		"if all x have (RX1 = false) [ "
						+			"true = var2, false = 1 - true"
						+		"] else [ "
						+			"false = 0.85-var2, true = 1-false"
						+		"]  "
						+	"] else [ "
						+		"var3 = 1, absurd = var3"
						+	"] "
				);
		
		resident.getCompiler().init(resident.getTableFunction());
		resident.getCompiler().parse();	// now should pass
		
		// check invalid order of vars
		resident.setTableFunction(
				"if any x have (RX1 = true) [ "
						+		"var = true, true = 0.9, false = 1 - var"
						+	"] else if any x have (RX1 = false) [ "
						+		"var = 0.2, "
						+		"if all x have (RX1 = false) [ "
						+			"false = 1 - true, true = var"
						+		"] else [ "
						+			"false = 0.85-var, true = 1-false"
						+		"]  "
						+	"] else [ "
						+		"var = 1, absurd = var"
						+	"] "
				);
		
		resident.getCompiler().init(resident.getTableFunction());
		try {
			resident.getCompiler().parse();
			fail("Should throw compilation error");
		} catch (MEBNException e1) {
			// OK
		}
		
		// check name scope of nested ifs
		resident.setTableFunction(
				"if any x have (RX1 = true) [ "
						+		"var = 0.9, true = var, false = 1 - var"
						+	"] else if any x have (RX1 = false) [ "
						+		"var = 0.2, "
						+		"if all x have (RX1 = false) [ "
						+			"true = var, false = 1 - true"
						+		"] else [ "
						+			"false = 0.85-var, true = 1-false"
						+		"]  "
						+	"] else [ "
						+		"var = 1, absurd = var"
						+	"] "
				);
		
		resident.getCompiler().init(resident.getTableFunction());
		resident.getCompiler().parse();	// now should pass
		
		// run a query to make sure compiler generates correct CPT when SSBN is generated
		TextModeRunner runner = new TextModeRunner();
		
		OrdinaryVariable x = resident.getOrdinaryVariableByName("x");
		List<OVInstance> queryArguments = new ArrayList<OVInstance>(2);
		queryArguments.add(OVInstance.getInstance(x , LiteralEntityInstance.getInstance("a", x.getValueType())));
		Query query = new Query(resident, queryArguments);
		ProbabilisticNetwork result = null;
		try {
			result = runner.executeQueryLaskeyAlgorithm(Collections.singletonList(query), PowerLoomKB.getNewInstanceKB(), mebn);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		assertNotNull(result);
		
		// extract node to check for probabilities
		ProbabilisticNode node = (ProbabilisticNode) result.getNode("RX2__a");
		assertNotNull(node);
		PotentialTable table = node.getProbabilityFunction();
		assertNotNull(table);
		
		// prepare an index that will store what are the positions of false/true/absurd states for current node
		int stateIndex[] = new int[3];
		Arrays.fill(stateIndex, -1);
		for (int i = 0; i < node.getStatesSize(); i++) {
			if (node.getStateAt(i).equalsIgnoreCase("false")) {
				stateIndex[0] = i;
			} else if (node.getStateAt(i).equalsIgnoreCase("true")) {
				stateIndex[1] = i;
			} else {
				stateIndex[2] = i;
			} 
		}
		assertTrue(stateIndex[0] >= 0);
		assertTrue(stateIndex[1] >= 0);
		assertTrue(stateIndex[2] >= 0);
		
		assertEquals(1, node.getParentNodes().size());
		int stateIndexParent[] = new int[3];
		Arrays.fill(stateIndexParent, -1);
		for (int i = 0; i < node.getParentNodes().get(0).getStatesSize(); i++) {
			if (node.getParentNodes().get(0).getStateAt(i).equalsIgnoreCase("false")) {
				stateIndexParent[0] = i;
			} else if (node.getParentNodes().get(0).getStateAt(i).equalsIgnoreCase("true")) {
				stateIndexParent[1] = i;
			} else {
				stateIndexParent[2] = i;
			} 
		}
		assertTrue(stateIndexParent[0] >= 0);
		assertTrue(stateIndexParent[1] >= 0);
		assertTrue(stateIndexParent[2] >= 0);
		
		int[] coord = table.getMultidimensionalCoord(0);
		assertNotNull(coord);
		assertEquals(2, coord.length);
		
		coord[1] = stateIndexParent[1];	// set parent to true
		
		coord[0] = stateIndex[1];		// set current node to true
		assertEquals(0.9, table.getValue(coord), 0.00005);
		coord[0] = stateIndex[0];		// set current node to false
		assertEquals(0.1, table.getValue(coord), 0.00005);
		coord[0] = stateIndex[2];		// set current node to absurd
		assertEquals(0, table.getValue(coord), 0.00005);
		
		coord[1] = stateIndexParent[0];	// set parent to false
		
		coord[0] = stateIndex[1];		// set current node to true
		assertEquals(0.2, table.getValue(coord), 0.00005);
		coord[0] = stateIndex[0];		// set current node to false
		assertEquals(0.8, table.getValue(coord), 0.00005);
		coord[0] = stateIndex[2];		// set current node to absurd
		assertEquals(0, table.getValue(coord), 0.00005);
		
		coord[1] = stateIndexParent[2];	// set parent to absurd
		
		coord[0] = stateIndex[1];		// set current node to true
		assertEquals(0, table.getValue(coord), 0.00005);
		coord[0] = stateIndex[0];		// set current node to false
		assertEquals(0, table.getValue(coord), 0.00005);
		coord[0] = stateIndex[2];		// set current node to absurd
		assertEquals(1, table.getValue(coord), 0.00005);
		
	}
	
	/**
	 * This method tests support for user-defined variables with string values
	 * @throws MEBNException
	 */
	@SuppressWarnings("deprecation")
	public void testStringVariable() throws MEBNException {
		UbfIO io = UbfIO.getInstance();
		
		MultiEntityBayesianNetwork mebn = null;
		try {
			mebn = io.loadMebn(new File("./src/test/resources/twoNodeExample.ubf"));
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		assertNotNull(mebn);
		
		// extract the node to change lpd
		ResidentNode resident = mebn.getDomainResidentNode("RX2");
		
		// change the lpd of the node to some invalid script
		resident.setTableFunction(
				"if any x have (RX1 = true) [ "
						+		"var1 = \"myString\", true = var1, false = 1 - true"
						+	"] else if any x have (RX1 = false) [ "
						+		"if all x have (RX1 = false) [ "
						+			"var2 = \"([a-zA-Z].*)+\", true = var2, false = 1 - true"
						+		"] else [ "
						+			"false = 0.75, true = 1-false"
						+		"]  "
						+	"] else [ "
						+		"absurd = 1"
						+	"] "
				);
		
		resident.getCompiler().init(resident.getTableFunction());
		try {
			resident.getCompiler().parse();
			fail("Should throw compilation error");
		} catch (MEBNException e1) {
			// make sure correct exception message is used
			assertTrue(e1.getMessage().contains(
					unbbayes.util.ResourceController.newInstance().getBundle(
							unbbayes.prs.mebn.compiler.resources.Resources.class.getName()
							).getString("NonNumericProbAssignment")
						)
					);
		}
		
		// change the lpd of the node to some valid script
		resident.setTableFunction(
				"if any x have (RX1 = true) [ "
						+		"var = \"myString\", true = .9, false = 1 - true"
						+	"] else if any x have (RX1 = false) [ "
						+		"var = \"([a-zA-Z].*)+\", "
						+		"if all x have (RX1 = false) [ "
						+			"true = .2, false = 1 - true"
						+		"] else [ "
						+			"false = 0.85-.2, true = 1-false"
						+		"]  "
						+	"] else [ "
						+		"var = \"asdf\", absurd = 1"
						+	"] "
				);
		
		resident.getCompiler().init(resident.getTableFunction());
		resident.getCompiler().parse();	// now should pass
		
		// run a query to make sure compiler generates correct CPT when SSBN is generated
		TextModeRunner runner = new TextModeRunner();
		
		OrdinaryVariable x = resident.getOrdinaryVariableByName("x");
		List<OVInstance> queryArguments = new ArrayList<OVInstance>(2);
		queryArguments.add(OVInstance.getInstance(x , LiteralEntityInstance.getInstance("a", x.getValueType())));
		Query query = new Query(resident, queryArguments);
		ProbabilisticNetwork result = null;
		try {
			result = runner.executeQueryLaskeyAlgorithm(Collections.singletonList(query), PowerLoomKB.getNewInstanceKB(), mebn);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		assertNotNull(result);
		
		// extract node to check for probabilities
		ProbabilisticNode node = (ProbabilisticNode) result.getNode("RX2__a");
		assertNotNull(node);
		PotentialTable table = node.getProbabilityFunction();
		assertNotNull(table);
		
		// prepare an index that will store what are the positions of false/true/absurd states for current node
		int stateIndex[] = new int[3];
		Arrays.fill(stateIndex, -1);
		for (int i = 0; i < node.getStatesSize(); i++) {
			if (node.getStateAt(i).equalsIgnoreCase("false")) {
				stateIndex[0] = i;
			} else if (node.getStateAt(i).equalsIgnoreCase("true")) {
				stateIndex[1] = i;
			} else {
				stateIndex[2] = i;
			} 
		}
		assertTrue(stateIndex[0] >= 0);
		assertTrue(stateIndex[1] >= 0);
		assertTrue(stateIndex[2] >= 0);
		
		assertEquals(1, node.getParentNodes().size());
		int stateIndexParent[] = new int[3];
		Arrays.fill(stateIndexParent, -1);
		for (int i = 0; i < node.getParentNodes().get(0).getStatesSize(); i++) {
			if (node.getParentNodes().get(0).getStateAt(i).equalsIgnoreCase("false")) {
				stateIndexParent[0] = i;
			} else if (node.getParentNodes().get(0).getStateAt(i).equalsIgnoreCase("true")) {
				stateIndexParent[1] = i;
			} else {
				stateIndexParent[2] = i;
			} 
		}
		assertTrue(stateIndexParent[0] >= 0);
		assertTrue(stateIndexParent[1] >= 0);
		assertTrue(stateIndexParent[2] >= 0);
		
		int[] coord = table.getMultidimensionalCoord(0);
		assertNotNull(coord);
		assertEquals(2, coord.length);
		
		coord[1] = stateIndexParent[1];	// set parent to true
		
		coord[0] = stateIndex[1];		// set current node to true
		assertEquals(0.9, table.getValue(coord), 0.00005);
		coord[0] = stateIndex[0];		// set current node to false
		assertEquals(0.1, table.getValue(coord), 0.00005);
		coord[0] = stateIndex[2];		// set current node to absurd
		assertEquals(0, table.getValue(coord), 0.00005);
		
		coord[1] = stateIndexParent[0];	// set parent to false
		
		coord[0] = stateIndex[1];		// set current node to true
		assertEquals(0.2, table.getValue(coord), 0.00005);
		coord[0] = stateIndex[0];		// set current node to false
		assertEquals(0.8, table.getValue(coord), 0.00005);
		coord[0] = stateIndex[2];		// set current node to absurd
		assertEquals(0, table.getValue(coord), 0.00005);
		
		coord[1] = stateIndexParent[2];	// set parent to absurd
		
		coord[0] = stateIndex[1];		// set current node to true
		assertEquals(0, table.getValue(coord), 0.00005);
		coord[0] = stateIndex[0];		// set current node to false
		assertEquals(0, table.getValue(coord), 0.00005);
		coord[0] = stateIndex[2];		// set current node to absurd
		assertEquals(1, table.getValue(coord), 0.00005);
		
	}
	
	/**
	 * This method tests 2 types of normalization: normalization of probabilities
	 * summing up to a value below 1 and some states not being declared (in this case
	 * the remaining probabilities must be distributed uniformely to the other states);
	 * and normalization of probabilities summing up to a value above 1 and some
	 * states not being declared (in this case, undeclared states should be set to 0
	 * and the probabilities must be normalized)
	 * @throws Exception 
	 */
	public void testNormalization() throws Exception {
		UbfIO io = UbfIO.getInstance();
		
		MultiEntityBayesianNetwork mebn = null;
		try {
			mebn = io.loadMebn(new File("./src/test/resources/twoNodeExample.ubf"));
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		assertNotNull(mebn);
		
		// extract the node to change lpd
		ResidentNode resident = mebn.getDomainResidentNode("RX2");
		
		// change the lpd of the node to some valid script
		resident.setTableFunction(
				"if any x have (RX1 = true) [ "
						+		"true = 0.8, absurd = 0"
						+	"] else if any x have (RX1 = false) [ "
						+		"if all x have (RX1 = false) [ "
						+			"true = 3, false = 1"
						+		"] else [ "
						+			"false = 0.75, true = 1-false"
						+		"]  "
						+	"] else [ "
						+		"absurd = 0.3334"
						+	"] "
				);
		
		resident.getCompiler().init(resident.getTableFunction());
		resident.getCompiler().parse();	// now should pass
		
		// change the lpd of the node to some valid script
		resident.setTableFunction(
				"if any x have (RX1 = true) [ "
						+		"true = 0.8, absurd = 0"
						+	"] else if any x have (RX1 = false) [ "
						+		"if all x have (RX1 = false) [ "
						+			"true = MAX(3,1), false = MIN(3,1)"
						+		"] else [ "
						+			"true = 3, false = 1"
						+		"]  "
						+	"] else [ "
						+		"absurd = 0.3334"
						+	"] "
				);
		
		resident.getCompiler().init(resident.getTableFunction());
		resident.getCompiler().parse();	// now should pass
		
		
		// run a query to make sure compiler generates correct CPT when SSBN is generated
		TextModeRunner runner = new TextModeRunner();
		
		OrdinaryVariable x = resident.getOrdinaryVariableByName("x");
		List<OVInstance> queryArguments = new ArrayList<OVInstance>(2);
		queryArguments.add(OVInstance.getInstance(x , LiteralEntityInstance.getInstance("a", x.getValueType())));
		Query query = new Query(resident, queryArguments);
		ProbabilisticNetwork result = null;
		result = runner.executeQueryLaskeyAlgorithm(Collections.singletonList(query), PowerLoomKB.getNewInstanceKB(), mebn);
		assertNotNull(result);
		
		// extract node to check for probabilities
		ProbabilisticNode node = (ProbabilisticNode) result.getNode("RX2__a");
		assertNotNull(node);
		PotentialTable table = node.getProbabilityFunction();
		assertNotNull(table);
		
		// prepare an index that will store what are the positions of false/true/absurd states for current node
		int stateIndex[] = new int[3];
		Arrays.fill(stateIndex, -1);
		for (int i = 0; i < node.getStatesSize(); i++) {
			if (node.getStateAt(i).equalsIgnoreCase("false")) {
				stateIndex[0] = i;
			} else if (node.getStateAt(i).equalsIgnoreCase("true")) {
				stateIndex[1] = i;
			} else {
				stateIndex[2] = i;
			} 
		}
		assertTrue(stateIndex[0] >= 0);
		assertTrue(stateIndex[1] >= 0);
		assertTrue(stateIndex[2] >= 0);
		
		assertEquals(1, node.getParentNodes().size());
		int stateIndexParent[] = new int[3];
		Arrays.fill(stateIndexParent, -1);
		for (int i = 0; i < node.getParentNodes().get(0).getStatesSize(); i++) {
			if (node.getParentNodes().get(0).getStateAt(i).equalsIgnoreCase("false")) {
				stateIndexParent[0] = i;
			} else if (node.getParentNodes().get(0).getStateAt(i).equalsIgnoreCase("true")) {
				stateIndexParent[1] = i;
			} else {
				stateIndexParent[2] = i;
			} 
		}
		assertTrue(stateIndexParent[0] >= 0);
		assertTrue(stateIndexParent[1] >= 0);
		assertTrue(stateIndexParent[2] >= 0);
		
		int[] coord = table.getMultidimensionalCoord(0);
		assertNotNull(coord);
		assertEquals(2, coord.length);
		
		coord[1] = stateIndexParent[1];	// set parent to true
		
		coord[0] = stateIndex[1];		// set current node to true
		assertEquals(0.8, table.getValue(coord), 0.00005);
		coord[0] = stateIndex[0];		// set current node to false
		assertEquals(0.2, table.getValue(coord), 0.00005);
		coord[0] = stateIndex[2];		// set current node to absurd
		assertEquals(0, table.getValue(coord), 0.00005);
		
		coord[1] = stateIndexParent[0];	// set parent to false
		
		coord[0] = stateIndex[1];		// set current node to true
		assertEquals(0.75, table.getValue(coord), 0.00005);
		coord[0] = stateIndex[0];		// set current node to false
		assertEquals(0.25, table.getValue(coord), 0.00005);
		coord[0] = stateIndex[2];		// set current node to absurd
		assertEquals(0, table.getValue(coord), 0.00005);
		
		coord[1] = stateIndexParent[2];	// set parent to absurd
		
		coord[0] = stateIndex[1];		// set current node to true
		assertEquals(0.3333, table.getValue(coord), 0.00005);
		coord[0] = stateIndex[0];		// set current node to false
		assertEquals(0.3333, table.getValue(coord), 0.00005);
		coord[0] = stateIndex[2];		// set current node to absurd
		assertEquals(0.3334, table.getValue(coord), 0.00005);
		
	}
	
	

}
