/**
 * 
 */
package unbbayes.prs.mebn.compiler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
	
	
//	public void testTableGenerationNestedIf() {
//		ResidentNode distFromOwn = this.mebn.getDomainResidentNode("DistFromOwn");
//		
//		OrdinaryVariable st = distFromOwn.getOrdinaryVariableByName("st");
//		OrdinaryVariable tprev = distFromOwn.getMFrag().getOrdinaryVariableByName("tPrev");
//		OrdinaryVariable t = distFromOwn.getOrdinaryVariableByName("t");
//		
//		OVInstance st0 = OVInstance.getInstance(st, "ST0", st.getValueType());
//		OVInstance st1 = OVInstance.getInstance(st, "ST1", st.getValueType());
//		OVInstance t0 = OVInstance.getInstance(tprev, "T0", tprev.getValueType());
//		OVInstance t1 = OVInstance.getInstance(t, "T1", t.getValueType());
//		OVInstance t2 = OVInstance.getInstance(t, "T2", t.getValueType());
//		
//		ProbabilisticNetwork net = new ProbabilisticNetwork("TestGenerateCPT");
//		
//		SSBNNode distFromOwn_ST0_T0 = SSBNNode.getInstance(net, distFromOwn);
//		try {
//			distFromOwn_ST0_T0.addArgument(st0);
//			distFromOwn_ST0_T0.addArgument(t0);
//			List<Entity> valList = new ArrayList<Entity>(distFromOwn_ST0_T0.getActualValues());
//			distFromOwn_ST0_T0.setNodeAsFinding(valList.get(0));
//			
//		} catch (Exception e) {
//			e.printStackTrace();
//			fail(e.getMessage());
//		}
//		
//		SSBNNode distFromOwn_ST0_T1 = SSBNNode.getInstance(net, distFromOwn);
//		try {
//			distFromOwn_ST0_T1.addArgument(st0);
//			distFromOwn_ST0_T1.addArgument(t1);
//			distFromOwn_ST0_T1.addParent(distFromOwn_ST0_T0, false);
//			distFromOwn_ST0_T1.getProbNode().setName("ST0T1");
//		} catch (Exception e) {
//			e.printStackTrace();
//			fail(e.getMessage());
//		}
//		
//		SSBNNode distFromOwn_ST1_T0 = SSBNNode.getInstance(net, distFromOwn);
//		try {
//			distFromOwn_ST1_T0.addArgument(st1);
//			distFromOwn_ST1_T0.addArgument(t0);
//			distFromOwn_ST0_T1.addParent(distFromOwn_ST1_T0, false);
//			distFromOwn_ST0_T1.getProbNode().setName("ST1T0");
//		} catch (Exception e) {
//			e.printStackTrace();
//			fail(e.getMessage());
//		}
//		
//		SSBNNode distFromOwn_ST0_T1v2 = SSBNNode.getInstance(net, distFromOwn);
//		try {
//			distFromOwn_ST0_T1v2.addArgument(st0);
//			distFromOwn_ST0_T1v2.addArgument(t1);
//			distFromOwn_ST0_T1.addParent(distFromOwn_ST0_T1v2, false);
//			distFromOwn_ST0_T1v2.getProbNode().setName("ST0T2");
//		} catch (Exception e) {
//			e.printStackTrace();
//			fail(e.getMessage());
//		}
//		
//		
//		Compiler compiler = new Compiler(distFromOwn_ST0_T1.getResident(),distFromOwn_ST0_T1);
//		compiler.init(distFromOwn_ST0_T1);
//		
////		Compiler compiler = new Compiler(distFromOwn_ST0_T1v2.getResident(),distFromOwn_ST0_T1v2);
////		compiler.init(distFromOwn_ST0_T1v2);
//		
//		
//		
//		PotentialTable table = null;
//		
//		String code = 
//		"if any st have ( DistFromOwn = OutOfRange ) [" +
//				"if all st have (~ DistFromOwn = OutOfRange) " +
//				"[ OutOfRange = 0 , TorpedoRange = 0 , Phaser2Range = .0 , Phaser1Range = .0 , " +
//				"PulseCanonRange = .0 , Absurd = 1 ]" +
//				" else " +
//				"[ OutOfRange = MIN(0.3 * CARDINALITY(st); .99) , TorpedoRange = 1 - OutOfRange , Phaser2Range = .0 , Phaser1Range = .0 , " +
//				"PulseCanonRange = .0 , Absurd = 0 ]" +
//		"] else [" +
//				"if all st have (~ DistFromOwn = OutOfRange) " +
//				"[ PulseCanonRange = MIN(0.2 * CARDINALITY(st); .99) , Phaser2Range = 1 - PulseCanonRange , TorpedoRange = .0 , Phaser1Range = .0 , " +
//				"OutOfRange = .0 , Absurd = 0 ]" +
//				" else " +
//				"[ OutOfRange = MIN(0.01 ; .99) , Phaser1Range = 0 , Phaser2Range = .0 , TorpedoRange = .0 , " +
//				"PulseCanonRange = .0 , Absurd = 1 - OutOfRange ]" +
//		"]";
//		    
//		try {
//			compiler.parse(code);		
//		} catch (MEBNException e) {
//			e.printStackTrace();
//			System.out.println(code.substring(compiler.getIndex() - 10));
//			fail(e.getMessage() + " at [" +  compiler.getIndex() + "]");
//		} catch (Exception e) {
//			e.printStackTrace();
//			fail(e.getMessage());
//		}
//		try {
//			table = compiler.getCPT();
//		} catch (Exception e) {
//			e.printStackTrace();
//			fail(e.getMessage());
//		}
//		
//		
//		GUIPotentialTable guiCPT = new GUIPotentialTable(table);
//		guiCPT.showTable("VAI FUNCIONAR???!!");
//		
//		while(true);
//	}
	
//	/**
//	 * Test method for {@link unbbayes.prs.mebn.compiler.Compiler#main(java.lang.String[])}.
//	 */
//	public void testMain() {
//		ProbabilisticNetwork rede = new ProbabilisticNetwork("MEBN Table Test");
//
//		ListaConjunto conjuntos = new ListaConjunto(new String[] { "OperatorSpecies",
//				"HarmPot" });
//
//		ProbabilisticNode dangerToSelf = new ProbabilisticNode();
//		dangerToSelf.setName("DangerToSelf");
//		dangerToSelf.setDescription("Danger to self");
//		dangerToSelf.appendState("Un");
//		dangerToSelf.appendState("Hi");
//		dangerToSelf.appendState("Me");
//		dangerToSelf.appendState("Lo");
//		PotentialTable auxTabPot = dangerToSelf.getPotentialTable();
//		auxTabPot.addVariable(dangerToSelf);
//		rede.addNode(dangerToSelf);
//
//		ProbabilisticNode opSpec = new ProbabilisticNode();
//		opSpec.setName("OperatorSpecies");
//		opSpec.setDescription("Operator Specie");
//		opSpec.appendState("Cardassian");
//		opSpec.appendState("Unknown");
//		opSpec.appendState("Friend");
//		opSpec.appendState("Klingon");
//		opSpec.appendState("Romulan");
//		auxTabPot = opSpec.getPotentialTable();
//		auxTabPot.addVariable(opSpec);
//		rede.addNode(opSpec);
//
//		Edge auxArco = new Edge(opSpec, dangerToSelf);
//		rede.addEdge(auxArco);
//
//		ProbabilisticNode harmPotential = new ProbabilisticNode();
//		harmPotential.setName("HarmPotential");
//		harmPotential.setDescription("Harm Potential");
//		harmPotential.appendState("True");
//		harmPotential.appendState("False");
//		auxTabPot = harmPotential.getPotentialTable();
//		auxTabPot.addVariable(harmPotential);
//		rede.addNode(harmPotential);
//
//		auxArco = new Edge(harmPotential, dangerToSelf);
//		rede.addEdge(auxArco);
//
//		conjuntos.conjuntos.add(new Conjunto(
//				new Node[] { opSpec, harmPotential }));
//
//		opSpec = new ProbabilisticNode();
//		opSpec.setName("OpSpec2");
//		opSpec.setDescription("Operator Specie 2");
//		opSpec.appendState("Cardassian");
//		opSpec.appendState("Unknown");
//		opSpec.appendState("Friend");
//		opSpec.appendState("Klingon");
//		opSpec.appendState("Romulan");
//		auxTabPot = opSpec.getPotentialTable();
//		auxTabPot.addVariable(opSpec);
//		rede.addNode(opSpec);
//
//		auxArco = new Edge(opSpec, dangerToSelf);
//		rede.addEdge(auxArco);
//
//		harmPotential = new ProbabilisticNode();
//		harmPotential.setName("HarmPotential2");
//		harmPotential.setDescription("Harm Potential 2");
//		harmPotential.appendState("True");
//		harmPotential.appendState("False");
//		auxTabPot = harmPotential.getPotentialTable();
//		auxTabPot.addVariable(harmPotential);
//		rede.addNode(harmPotential);
//
//		auxArco = new Edge(harmPotential, dangerToSelf);
//		rede.addEdge(auxArco);
//
//		conjuntos.conjuntos.add(new Conjunto(
//				new Node[] { opSpec, harmPotential }));
//
//		PotentialTable tab = dangerToSelf.getPotentialTable();
//		for (int i = 0; i < tab.tableSize();) {
//			int[] coord = tab.voltaCoord(i);
//			int countSTi = 0;
//			int countSTj = 0;
//			int countSTk = 0;
//			int countSTl = 0;
//			int countSTm = 0;
//
//			for (int j = 0; j < conjuntos.conjuntos.size(); ++j) {
//				int opSpecIndex = conjuntos.mapa.get("OperatorSpecies");
//				int harmPotIndex = conjuntos.mapa.get("HarmPot");
//				boolean ehCarda = coord[1 + (j * conjuntos.tamanhoConjunto)
//						+ opSpecIndex] == 0; // 0 ? o indice do cardassian
//				boolean ehTrue = coord[1 + (j * conjuntos.tamanhoConjunto)
//						+ harmPotIndex] == 0; // 0 ? o indice do true
//				if (ehCarda && ehTrue) {
//					// STi
//					countSTi++;
//				}
//
//				boolean ehRomu = coord[1 + (j * conjuntos.tamanhoConjunto)
//						+ opSpecIndex] == 4; // 4 ? o indice do romulan
//				if (ehRomu && ehTrue) {
//					// STj
//					countSTj++;
//				}
//
//				boolean ehUnk = coord[1 + (j * conjuntos.tamanhoConjunto)
//						+ opSpecIndex] == 1; // 1 ? o indice do unk
//
//				if (ehUnk && ehTrue) {
//					// stk
//					countSTk++;
//				}
//
//				boolean ehklin = coord[1 + (j * conjuntos.tamanhoConjunto)
//						+ opSpecIndex] == 3; // 3 ? o indice do klin
//
//				if (ehklin && ehTrue) {
//					// stl
//					countSTl++;
//				}
//
//				boolean ehfri = coord[1 + (j * conjuntos.tamanhoConjunto)
//						+ opSpecIndex] == 2; // 2 ? o indice do fri
//
//				if (ehfri && ehTrue) {
//					// stm
//					countSTm++;
//				}
//			}			
//			
////			dangerToSelf.appendState("Un");
////			dangerToSelf.appendState("Hi");
////			dangerToSelf.appendState("Me");
////			dangerToSelf.appendState("Lo");			
//			
//			if (countSTi > 0) {
//				double unValue = 0.9 + Math.min(0.1, 0.025 * countSTi);
//				tab.setValue(i, (float)unValue);
//				
//				double hiValue = (1-unValue) * 0.8;
//				tab.setValue(i+1, (float)hiValue);
//				
//				double meValue = (1-unValue) * 0.2;
//				tab.setValue(i+2, (float) meValue);
//				
//				tab.setValue(i+3, 0);
//			} else if (countSTj > 0) {
//				
//			}
//			
//			i += dangerToSelf.getStatesSize();
//		}
//
//		
//		GUIPotentialTable guiCPT = new GUIPotentialTable(tab);
//		guiCPT.showTable("VAI FUNCIONAR!");
//		
//		// while (true);
//		
//		/*
//		Compiler c = new Compiler(null);
//		c.init(TABLE_TO_PARSE); 
//		try  {
//			c.parse();
//		} catch (MEBNException e) {
//			fail(e.getMessage() + ": "+ e.getClass().getName());
//		}
//		*/
//		 
//	}
//	
//	
	
//	public void testGenerateCPT() {
//		
//		ResidentNode harmPotential = this.mebn.getDomainResidentNode("HarmPotential");
//		ResidentNode starshipClass = this.mebn.getDomainResidentNode("StarshipClass");
//		ResidentNode distFromOwn = this.mebn.getDomainResidentNode("DistFromOwn");
//		
//		OrdinaryVariable st = harmPotential.getOrdinaryVariableByName("st");
//		OrdinaryVariable t = harmPotential.getOrdinaryVariableByName("t");
//		
//		OVInstance st0 = OVInstance.getInstance(st, "ST0", st.getValueType());
//		OVInstance t0 = OVInstance.getInstance(t, "T0", t.getValueType());
//		OVInstance st1 = OVInstance.getInstance(st, "ST1", st.getValueType());
//		OVInstance t1 = OVInstance.getInstance(t, "T1", t.getValueType());
//		
//		ProbabilisticNetwork net = new ProbabilisticNetwork("TestGenerateCPT");
//		
//		SSBNNode harmPotential_ST0_T0 = SSBNNode.getInstance(net, harmPotential);
//		try {
//			harmPotential_ST0_T0.addArgument(st0);
//			harmPotential_ST0_T0.addArgument(t0);
//		} catch (Exception e) {
//			e.printStackTrace();
//			fail(e.getMessage());
//		}
//		
//		SSBNNode distFromOwn_ST0_T0 = SSBNNode.getInstance(net, distFromOwn);
//		try {
//			distFromOwn_ST0_T0.addArgument(st0);
//			distFromOwn_ST0_T0.addArgument(t0);
//			harmPotential_ST0_T0.addParent(distFromOwn_ST0_T0, true);
//		} catch (Exception e) {
//			e.printStackTrace();
//			fail(e.getMessage());
//		}
//		//net.addEdge(new Edge(distFromOwn_ST0_T0.getProbNode(),harmPotential_ST0_T0.getProbNode()));
//		
//		SSBNNode distFromOwn_ST1_T0 = SSBNNode.getInstance(net, distFromOwn);
//		try {
//			distFromOwn_ST1_T0.addArgument(st1);
//			distFromOwn_ST1_T0.addArgument(t0);
//			harmPotential_ST0_T0.addParent(distFromOwn_ST1_T0, true);
//		} catch (Exception e) {
//			e.printStackTrace();
//			fail(e.getMessage());
//		}
//		
//		SSBNNode distFromOwn_ST0_T1 = SSBNNode.getInstance(net, distFromOwn);
//		try {
//			distFromOwn_ST0_T1.addArgument(st0);
//			distFromOwn_ST0_T1.addArgument(t1);
//			harmPotential_ST0_T0.addParent(distFromOwn_ST0_T1, true);
//		} catch (Exception e) {
//			e.printStackTrace();
//			fail(e.getMessage());
//		}
//		
//		SSBNNode starshipClass_ST0 = SSBNNode.getInstance(net, starshipClass);
//		try {
//			starshipClass_ST0.addArgument(st0);
//			harmPotential_ST0_T0.addParent(starshipClass_ST0, true);
//		} catch (Exception e) {
//			e.printStackTrace();
//			fail(e.getMessage());
//		}
//		//net.addEdge(new Edge(starshipClass_ST0.getProbNode(),harmPotential_ST0_T0.getProbNode()));
//		
//		
//		Compiler compiler = new Compiler(harmPotential,harmPotential_ST0_T0);
//		
//		compiler.init(harmPotential_ST0_T0);
//		
//		PotentialTable table = null;
//		
//		String code = "if any st.t have (DistFromOwn = Absurd | StarshipClass = Absurd )"
//			+ "[false = 0 , true = 0 , absurd = 1]"
//			+ "else if any st.t have (DistFromOwn = Phaser1Range)" 
//			+ "[false = MIN(MAX(CARDINALITY(st.t) * 0.3 ; 0.1) ; 1) , true = 1 - false , absurd = 0] "
//			+ "else if all st have (DistFromOwn = Phaser2Range)" 
//			+ "[false = MIN(MAX(CARDINALITY(st) * 0.2 ; 0.1) ; 1) , true = 1 - false , absurd = 0] "
//			+ " else if all asdf have (StarshipClass = WarBird) "
//			+ "[false = MIN(CARDINALITY(z)* .1; 1) ,  true = 1 - false , absurd = 0]"
//			+ "else if all st have (StarshipClass = Explorer) "
//			+ "[false = MIN(CARDINALITY(st)* .2; 1) ,  true = 1 - false , absurd = 0]"
//			+ "else if any st have (DistFromOwn = OutOfRange) "
//			+ "[true = MIN(CARDINALITY(asdf)* .3; 1) ,  false = 1 - true , absurd = 0]"
//			+ "else if all st have (StarshipClass = Frigate) "
//			+ "[false = MIN(CARDINALITY(st)* .4; 1) ,  true = 1 - false , absurd = 0]"
//			+ "else if all st have (StarshipClass = Cruiser) "
//			+ "[false = MIN(CARDINALITY(st)* .5; 1) ,  true = 1 - false , absurd = 0]"
//			+ "else if all st have (StarshipClass = Freighter) "
//			+ "[false = MIN(CARDINALITY(st)* .6; 1) ,  true = 1 - false , absurd = 0]"
//			+ "else [false = 0.33 ,  true = 0.33 , absurd = 1 - (false + true)]";
//			
//		try {
//			compiler.parse(code);		
//		} catch (Exception e) {
//			e.printStackTrace();
//			fail(e.getMessage());
//		}
//		System.out.println("\n\n\nCode Parsed!!\n\n\n");
//		try {
//			table = compiler.getCPT();
//		} catch (Exception e) {
//			e.printStackTrace();
//			fail(e.getMessage());
//		}
//		
//		
//		GUIPotentialTable guiCPT = new GUIPotentialTable(table);
//		guiCPT.showTable("VAI FUNCIONAR???");
//		
//		while(true);
//		
//	}
//	
	
//public void testGenerateCPT2() {
//		
//	ResidentNode harmPotential = this.mebn.getDomainResidentNode("HarmPotential");
//	ResidentNode starshipClass = this.mebn.getDomainResidentNode("StarshipClass");
//	ResidentNode distFromOwn = this.mebn.getDomainResidentNode("DistFromOwn");
//		
//		OrdinaryVariable st = harmPotential.getOrdinaryVariableByName("st");
//		OrdinaryVariable t = harmPotential.getOrdinaryVariableByName("t");
//		OrdinaryVariable tother = new OrdinaryVariable("tother",t.getValueType(), t.getMFrag());
//		
//		OVInstance st0 = OVInstance.getInstance(st, "ST0", st.getValueType());
//		OVInstance t0 = OVInstance.getInstance(t, "T0", t.getValueType());
//		//OVInstance st1 = OVInstance.getInstance(st, "ST1", st.getValueType());
//		OVInstance t1 = OVInstance.getInstance(t, "T1", t.getValueType());
//		
//		ProbabilisticNetwork net = new ProbabilisticNetwork("TestGenerateCPT");
//		
//		SSBNNode harmPotential_ST0_T0 = SSBNNode.getInstance(net, harmPotential);
//		try {
//			harmPotential_ST0_T0.addArgument(st0);
//			harmPotential_ST0_T0.addArgument(t0);
//			harmPotential_ST0_T0.getName();
//		} catch (Exception e) {
//			e.printStackTrace();
//			fail(e.getMessage());
//		}
//		
//		SSBNNode distFromOwn_ST0_T0 = SSBNNode.getInstance(net, distFromOwn);
//		try {
//			distFromOwn_ST0_T0.addArgument(st0);
//			distFromOwn_ST0_T0.addArgument(t0);
//			distFromOwn_ST0_T0.getName();
//			harmPotential_ST0_T0.addParent(distFromOwn_ST0_T0, true);
//		} catch (Exception e) {
//			e.printStackTrace();
//			fail(e.getMessage());
//		}
//		//net.addEdge(new Edge(distFromOwn_ST0_T0.getProbNode(),harmPotential_ST0_T0.getProbNode()));
//		
//		
//		
//		SSBNNode distFromOwn_ST0_T1 = SSBNNode.getInstance(net, distFromOwn);
//		try {
//			distFromOwn_ST0_T1.addArgument(st0);
//			distFromOwn_ST0_T1.addArgument(t1);
//			distFromOwn_ST0_T1.getName();
//			harmPotential_ST0_T0.addParent(distFromOwn_ST0_T1, true);
//		} catch (Exception e) {
//			e.printStackTrace();
//			fail(e.getMessage());
//		}
//		
//		SSBNNode starshipClass_ST0 = SSBNNode.getInstance(net, starshipClass);
//		try {
//			starshipClass_ST0.addArgument(st0);
//			starshipClass_ST0.getName();
//			harmPotential_ST0_T0.addParent(starshipClass_ST0, true);
//		} catch (Exception e) {
//			e.printStackTrace();
//			fail(e.getMessage());
//		}
//		//net.addEdge(new Edge(starshipClass_ST0.getProbNode(),harmPotential_ST0_T0.getProbNode()));
//		
//		
//		Compiler compiler = new Compiler(harmPotential,harmPotential_ST0_T0);
//		
//		compiler.init(harmPotential_ST0_T0);
//		
//		PotentialTable table = null;
//		
//		String code = "if any st.t have (DistFromOwn = Absurd | StarshipClass = Absurd )"
//	+ " [false = 0 , true = 0 , absurd = 1]"
//	+ " else if any st.t have (DistFromOwn = Phaser1Range)" 
//	+ " 	[false = MIN(MAX(CARDINALITY(st.t) * 0.3 ; 0.1) ; 1) , true = 1 - false , absurd = 0]" 
//	+ " else if all st have (DistFromOwn = Phaser2Range)"
//	+ " 	[false = MIN(MAX(CARDINALITY(st) * 0.2 ; 0.1) ; 1) , true = 1 - false , absurd = 0] "
//	+ " else if all asdf have (StarshipClass = WarBird) "
//	+ " 	[false = MIN(CARDINALITY(z)* .1; 1) ,  true = 1 - false , absurd = 0]"
//	+ " else if all st have (StarshipClass = Explorer | ~ DistFromOwn = OutOfRange) "
//	+ " 	[false = MIN(CARDINALITY(st)* .2; 1) ,  true = 1 - false , absurd = 0]"
//	+ " else [false = 0.33 ,  true = 0.33 , absurd = 1 - (false + true)]";
//			
//		try {
//			compiler.parse(code);		
//		} catch (Exception e) {
//			e.printStackTrace();
//			fail(e.getMessage());
//		}
//		System.out.println("\n\n\nCode Parsed!!\n\n\n");
//		try {
//			table = compiler.getCPT();
//		} catch (Exception e) {
//			e.printStackTrace();
//			fail(e.getMessage());
//		}
//		
//		
//		GUIPotentialTable guiCPT = new GUIPotentialTable(table);
//		guiCPT.showTable(harmPotential_ST0_T0.getName());
//		
//		//while(true);
//		
//	}
//
//
//
//public void testGenerateCPT3() {
//		
//	ResidentNode harmPotential = this.mebn.getDomainResidentNode("HarmPotential");
//	ResidentNode starshipClass = this.mebn.getDomainResidentNode("StarshipClass");
//	ResidentNode distFromOwn = this.mebn.getDomainResidentNode("DistFromOwn");
//		
//		OrdinaryVariable st = harmPotential.getOrdinaryVariableByName("st");
//		OrdinaryVariable t = harmPotential.getOrdinaryVariableByName("t");
//		OrdinaryVariable tother = new OrdinaryVariable("tother",t.getValueType(), t.getMFrag());
//		
//		OVInstance st0 = OVInstance.getInstance(st, "ST0", st.getValueType());
//		OVInstance t0 = OVInstance.getInstance(t, "T0", t.getValueType());
//		//OVInstance st1 = OVInstance.getInstance(st, "ST1", st.getValueType());
//		OVInstance t1 = OVInstance.getInstance(tother, "T1", t.getValueType());
//		
//		ProbabilisticNetwork net = new ProbabilisticNetwork("TestGenerateCPT");
//		
//		SSBNNode harmPotential_ST0_T0 = SSBNNode.getInstance(net, harmPotential);
//		try {
//			harmPotential_ST0_T0.addArgument(st0);
//			harmPotential_ST0_T0.addArgument(t0);
//			harmPotential_ST0_T0.getName();
//		} catch (Exception e) {
//			e.printStackTrace();
//			fail(e.getMessage());
//		}
//		
//		SSBNNode distFromOwn_ST0_T0 = SSBNNode.getInstance(net, distFromOwn);
//		try {
//			distFromOwn_ST0_T0.addArgument(st0);
//			distFromOwn_ST0_T0.addArgument(t0);
//			distFromOwn_ST0_T0.getName();
//			harmPotential_ST0_T0.addParent(distFromOwn_ST0_T0, true);
//		} catch (Exception e) {
//			e.printStackTrace();
//			fail(e.getMessage());
//		}
//		//net.addEdge(new Edge(distFromOwn_ST0_T0.getProbNode(),harmPotential_ST0_T0.getProbNode()));
//		
//		
//		
//		SSBNNode distFromOwn_ST0_T1 = SSBNNode.getInstance(net, distFromOwn);
//		try {
//			distFromOwn_ST0_T1.addArgument(st0);
//			distFromOwn_ST0_T1.addArgument(t1);
//			distFromOwn_ST0_T1.getName();
//			harmPotential_ST0_T0.addParent(distFromOwn_ST0_T1, true);
//		} catch (Exception e) {
//			e.printStackTrace();
//			fail(e.getMessage());
//		}
//		
//		SSBNNode starshipClass_ST0 = SSBNNode.getInstance(net, starshipClass);
//		try {
//			starshipClass_ST0.addArgument(st0);
//			starshipClass_ST0.getName();
//			harmPotential_ST0_T0.addParent(starshipClass_ST0, true);
//		} catch (Exception e) {
//			e.printStackTrace();
//			fail(e.getMessage());
//		}
//		//net.addEdge(new Edge(starshipClass_ST0.getProbNode(),harmPotential_ST0_T0.getProbNode()));
//		
//		
//		Compiler compiler = new Compiler(harmPotential,harmPotential_ST0_T0);
//		
//		compiler.init(harmPotential_ST0_T0);
//		
//		PotentialTable table = null;
//		
//		String code = "if any st.t have (DistFromOwn = Absurd | StarshipClass = Absurd )"
//	+ " [false = 0 , true = 0 , absurd = 1]"
//	+ " else if any st.t have (DistFromOwn = Phaser1Range)" 
//	+ " 	[false = MIN(MAX(CARDINALITY(st.t) * 0.3 ; 0.1) ; 1) , true = 1 - false , absurd = 0]" 
//	+ " else if all st have (DistFromOwn = Phaser2Range)"
//	+ " 	[false = MIN(MAX(CARDINALITY(st) * 0.2 ; 0.1) ; 1) , true = 1 - false , absurd = 0] "
//	+ " else if all asdf have (StarshipClass = WarBird) "
//	+ " 	[false = MIN(CARDINALITY(z)* .1; 1) ,  true = 1 - false , absurd = 0]"
//	+ " else if all st have (StarshipClass = Explorer | ~ DistFromOwn = OutOfRange) "
//	+ " 	[false = MIN(CARDINALITY(st)* .4; 1) ,  true = 1 - false , absurd = 0]"
//	+ " else [false = 0.33 ,  true = 0.33 , absurd = 1 - (false + true)]";
//			
//		try {
//			compiler.parse(code);		
//		} catch (Exception e) {
//			e.printStackTrace();
//			fail(e.getMessage());
//		}
//		System.out.println("\n\n\nCode Parsed!!\n\n\n");
//		try {
//			table = compiler.getCPT();
//		} catch (Exception e) {
//			e.printStackTrace();
//			fail(e.getMessage());
//		}
//		
//		
//		GUIPotentialTable guiCPT = new GUIPotentialTable(table);
//		guiCPT.showTable(harmPotential_ST0_T0.getName());
//		
//		//while(true);
//		
//	}
//	
	
//public void testDistFromOwn() {
//		
//	ResidentNode distFromOwn = this.mebn.getDomainResidentNode("DistFromOwn");
//		
//		OrdinaryVariable st = distFromOwn.getOrdinaryVariableByName("st");
//		OrdinaryVariable tprev = distFromOwn.getMFrag().getOrdinaryVariableByName("tPrev");
//		OrdinaryVariable t = distFromOwn.getOrdinaryVariableByName("t");
//		
//		OVInstance st0 = OVInstance.getInstance(st, "ST0", st.getValueType());
//		OVInstance st1 = OVInstance.getInstance(st, "ST1", st.getValueType());
//		OVInstance t0 = OVInstance.getInstance(tprev, "T0", tprev.getValueType());
//		OVInstance t1 = OVInstance.getInstance(t, "T1", t.getValueType());
//		
//		ProbabilisticNetwork net = new ProbabilisticNetwork("TestGenerateCPT");
//		
//		SSBNNode distFromOwn_ST0_T0 = SSBNNode.getInstance(net, distFromOwn);
//		try {
//			distFromOwn_ST0_T0.addArgument(st0);
//			distFromOwn_ST0_T0.addArgument(t0);
//			List<Entity> valList = new ArrayList<Entity>(distFromOwn_ST0_T0.getActualValues());
//			distFromOwn_ST0_T0.setNodeAsFinding(valList.get(0));
//			
//		} catch (Exception e) {
//			e.printStackTrace();
//			fail(e.getMessage());
//		}
//		
//		SSBNNode distFromOwn_ST0_T1 = SSBNNode.getInstance(net, distFromOwn);
//		try {
//			distFromOwn_ST0_T1.addArgument(st0);
//			distFromOwn_ST0_T1.addArgument(t1);
//			distFromOwn_ST0_T1.addParent(distFromOwn_ST0_T0, false);
//			distFromOwn_ST0_T1.getProbNode().setName("ST0T1");
//		} catch (Exception e) {
//			e.printStackTrace();
//			fail(e.getMessage());
//		}
//		
//		SSBNNode distFromOwn_ST1_T0 = SSBNNode.getInstance(net, distFromOwn);
//		try {
//			distFromOwn_ST1_T0.addArgument(st1);
//			distFromOwn_ST1_T0.addArgument(t0);
//			distFromOwn_ST0_T1.addParent(distFromOwn_ST1_T0, false);
//			distFromOwn_ST0_T1.getProbNode().setName("ST1T0");
//		} catch (Exception e) {
//			e.printStackTrace();
//			fail(e.getMessage());
//		}
//		
//		
//		Compiler compiler = new Compiler(distFromOwn_ST0_T1.getResident(),distFromOwn_ST0_T1);
//		
//		compiler.init(distFromOwn_ST0_T1);
//		
//		PotentialTable table = null;
//		
//		String code = "if any st have ( DistFromOwn = OutOfRange ) " +
//				"[ OutOfRange = .6 , TorpedoRange = .3 , Phaser2Range = .05 , Phaser1Range = .04 , " +
//				"PulseCanonRange = .01 , Absurd = 0 ]"
//		+" else if any st have ( DistFromOwn = TorpedoRange ) " +
//				"[ OutOfRange = .25 , TorpedoRange = .4 , Phaser2Range = .25 , Phaser1Range = .07 , " +
//				"PulseCanonRange = .03 , Absurd = 0 ] " 
//		+ "else if any st have ( DistFromOwn = Phaser2Range )" +
//				" [ OutOfRange = .06 , TorpedoRange = .25 , Phaser2Range = .4 , " +
//				"Phaser1Range = .25 , PulseCanonRange = .04 , Absurd = 0 ]"
//		+ "else if any st have ( DistFromOwn = Phaser1Range ) " +
//				"[ OutOfRange = .03 , TorpedoRange = .07 , Phaser2Range = .25 , Phaser1Range = .4 ," +
//				"PulseCanonRange = .25 , Absurd = 0 ]"
//		+ "else if any st have ( DistFromOwn = PulseCanonRange ) " +
//				"[ OutOfRange = .01 , TorpedoRange = .04 , Phaser2Range = .1 , Phaser1Range = .35 , " +
//				" PulseCanonRange = .5 , Absurd = 0 ]"
//		+ "else " +
//				"[ OutOfRange = 0 , TorpedoRange = 0 , Phaser2Range = 0 , Phaser1Range = 0 , " +
//				"PulseCanonRange = 0 , Absurd = 1 ]";
//		    
//		try {
//			compiler.parse(code);		
//		} catch (Exception e) {
//			e.printStackTrace();
//			fail(e.getMessage());
//		}
//		try {
//			table = compiler.getCPT();
//		} catch (Exception e) {
//			e.printStackTrace();
//			fail(e.getMessage());
//		}
//		
//		
//		GUIPotentialTable guiCPT = new GUIPotentialTable(table);
//		guiCPT.showTable("VAI FUNCIONAR???!!");
//		
//		while(true);
//		
//	}
//
//
//	public void startBusyLoopToPauseLastTest() {
//		// this is just to let the last test not to exit (keep the test suite running)
//		assertEquals(0f, 0);
//		while(true);
//		
//	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.compiler.Compiler#init(java.lang.String)}.
	 */
	/*
	public void testInit() {
		fail("Not yet implemented"); // TODO
	}
	*/
	/**
	 * Test method for {@link unbbayes.prs.mebn.compiler.Compiler#parse()}.
	 */
	/*
	public void testParse() {
		fail("Not yet implemented"); // TODO
		
	}
	*/
}
