package unbbayes.prs.mebn.compiler;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;
import unbbayes.io.mebn.PrOwlIO;
import unbbayes.io.mebn.exceptions.IOMebnException;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.compiler.exception.InvalidProbabilityRangeException;
import unbbayes.prs.mebn.compiler.exception.NoDefaultDistributionDeclaredException;
import unbbayes.prs.mebn.exception.MEBNException;
import unbbayes.util.Debug;

public class MEBNTableParserTest extends TestCase {
	
	private MultiEntityBayesianNetwork mebn = null; 
	private unbbayes.prs.mebn.compiler.Compiler tableParser = null;
	
	public MEBNTableParserTest(String arg0) {
		super(arg0);
		Debug.setDebug(true);
		
		PrOwlIO prOwlIO = new PrOwlIO(); 
		
		System.out.println("-----Load file test-----"); 
		
		try{
			mebn = prOwlIO.loadMebn(new File("src/test/resources/testCases/mebn/StarTrek.owl")); 
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
		
	}

	protected void setUp() throws Exception {
		super.setUp();
		Debug.setDebug(true);
		assertNotNull(tableParser);
		
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	
	/*
	 * Test method for 'unbbayes.prs.mebn.compiler.MEBNTableParser.parse()'
	 */
	public void testNormalConsistencyCheck() {
		
		//		 should go all right
		String tableString =  
			" if any st.t have( OperatorSpecies = Cardassian | HarmPotential = true ) " + 
			"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = .99 ]  " +
			" else if any st.z.t have( OperatorSpecies = Romulan & HarmPotential = true ) " +
			"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = .99 ]  " +
			" else if any STj have( OperatorSpecies = Unknown & HarmPotential = true ) " + 
			"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = .99 ]  " +
			" else if any STk have( OperatorSpecies = Klingon & HarmPotential = true ) " +
			"  [ Un = 0.10 , Hi = 0.15 , Medium = .15 , Low = .65 ] " +
			" else if any STl have( OperatorSpecies = Friend & HarmPotential = true ) " +
			"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = .99 ] " +
			" else [ Un = 0 , Hi = 0 , Medium = 0 , Low = 1 ] ";
		
		
		try  {
			tableParser.parse(tableString);
		} catch (MEBNException e) {
			fail(e.getMessage());
		} 
		
		
		
	}
	
	/*
	 * Test if parser detect no default distribution table
	 * or else clause undeclared
	 *
	 */
	public void testConsistencyNoDefDistro() {
		//		 Should fail, no default distro
		String tableString =  
			" if any STi have( OperatorSpecies = Cardassian & HarmPotential = true ) " + 
			"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = .99 ]  " +
			" else if any STj have( OperatorSpecies = Romulan & HarmPotential = true ) " +
			"  [ Un = 0 , Hi = MIN(0;1) , Medium = .01 , Low = .99 ]  " +
			" else if any STj have( OperatorSpecies = Unknown & HarmPotential = true ) " + 
			"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = MAX(0;.99) ]  " +
			" else if any STk have( OperatorSpecies = Klingon & HarmPotential = true ) " +
			"  [ Un = 0.10 , Hi = CARDINALITY(OperatorSpecies)*0.1 , Medium = .15 , Low = 1- (Un + (Hi + Medium)) ] " +
			" else if any STl have( OperatorSpecies = Friend & HarmPotential = true ) " +
			"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = .99 ] " ;
		
			
		try  {
			tableParser.parse(tableString);
		} catch (NoDefaultDistributionDeclaredException e) {
			// pass
		} catch (MEBNException e) {
			fail(e.getMessage() + " at " + tableString.substring(tableParser.getIndex()-1,tableParser.getIndex()+10));
			e.printStackTrace();
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
			"  [ Un = 0.10 , Hi = 0.15 , Medium = .15 , Low = .65 ] " +
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

		//		 Should fail, some state is below 0
		tableString =  
			" if any STi have( OperatorSpecies = Cardassian & HarmPotential = true ) " + 
			"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = .99 ]  " +
			" else if any STj have( OperatorSpecies = Romulan & HarmPotential = true ) " +
			"  [ Un = 2 , Hi = 0 , Medium = .01 , Low = .99 ]  " +
			" else if any STj have( OperatorSpecies = Unknown & HarmPotential = true ) " + 
			"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = .99 ]  " +
			" else if any STk have( OperatorSpecies = Klingon & HarmPotential = true ) " +
			"  [ Un = 0.10 , Hi = 0.15 , Medium = .15 , Low = .65 ] " +
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
			"  [ Un = 0.10 , Hi = 0.15 , Medium = .15 , Low = .65 ] " +
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
		
		
		//		 Should fail sum is below 1 in default distribution
		tableString =  
			" if any STi have( OperatorSpecies = Cardassian & HarmPotential = true ) " + 
			"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = .99 ]  " +
			" else if any STj have( OperatorSpecies = Romulan & HarmPotential = true ) " +
			"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = .99 ]  " +
			" else if any STj have( OperatorSpecies = Unknown & HarmPotential = true ) " + 
			"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = .99 ]  " +
			" else if any STk have( OperatorSpecies = Klingon & HarmPotential = true ) " +
			"  [ Un = 0.10 , Hi = 0.15 , Medium = .15 , Low = .65 ] " +
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
			" if any STi have( OperatorSpecies = Cardassian & HarmPotential = true ) " + 
			"  [ Un = 0 , Hi = 0 , Medium = MIN ( CARDINALITY (OperatorSpecies) * 2 ; .2 ) , Low = 1 - Medium ]  " +
			" else if any STj have( OperatorSpecies = Romulan & HarmPotential = true ) " +
			"  [ Un = 0 , Hi = 0 , Medium = (.005 + .005) , Low = .99 ]  " +
			" else if any STj have( OperatorSpecies = Unknown & HarmPotential = true ) " + 
			"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = (((1 - Medium) - Un) - Hi ) ]  " +
			" else if any STk have( OperatorSpecies = Klingon & HarmPotential = true ) " +
			"  [ Un = 0.10 , Hi = 0.15 , Medium = .15 , Low = .65 ] " +
			" else if any STl have( OperatorSpecies = Friend & HarmPotential = true ) " +
			"  [ Un = 0 , Hi = 0 , Medium = .01 , Low = .99 ] " +
			" else [ Un = 0 , Hi = 0 , Medium = MAX (.5 ; CARDINALITY (HarmPotential)) , Low = 1 - Medium ] ";
		
		
		try  {
			tableParser.parse(tableString);
		} catch (MEBNException e) {
			fail(e.getMessage() + " at index " + tableParser.getIndex());
		} 
		
	}
	
	
	public void testNoIfStatement() {
		//		 should go all right
		String tableString =  
			" [ Un = 0 , Hi = 0 , Medium = MIN ( CARDINALITY (OperatorSpecies) * 2 ; .2 ) , Low = 1 - Medium ]  ";		
		try  {
			tableParser.parse(tableString);
		} catch (MEBNException e) {
			fail(e.getMessage() + " at index " + tableParser.getIndex());
		} 
		
	}

}
