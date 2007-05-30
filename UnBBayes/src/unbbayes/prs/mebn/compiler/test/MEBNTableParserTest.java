package unbbayes.prs.mebn.compiler.test;

import java.io.File;
import java.io.IOException;

import unbbayes.io.mebn.PrOwlIO;
import unbbayes.io.mebn.exceptions.IOMebnException;
import unbbayes.prs.mebn.DomainResidentNode;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.MultiEntityNode;
import unbbayes.prs.mebn.compiler.MEBNTableParser;
import unbbayes.prs.mebn.compiler.exception.InvalidProbabilityRangeException;
import unbbayes.prs.mebn.compiler.exception.NoDefaultDistributionDeclaredException;
import unbbayes.prs.mebn.compiler.exception.SomeStateUndeclaredException;
import unbbayes.prs.mebn.exception.MEBNException;
import unbbayes.prs.mebn.table.TableParser;
import unbbayes.util.Debug;
import junit.framework.TestCase;

public class MEBNTableParserTest extends TestCase {
	
	private MultiEntityBayesianNetwork mebn = null; 
	
	public MEBNTableParserTest(String arg0) {
		super(arg0);
		
	}

	protected void setUp() throws Exception {
		super.setUp();

		
		
		
		
	
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/*
	 * Test method for 'unbbayes.prs.mebn.compiler.MEBNTableParser.getInstance(DomainResidentNode)'
	 */
	public void testGetInstanceDomainResidentNode() {
		// TODO Auto-generated method stub
		fail("Not yet implemented"); // TODO
	}

	/*
	 * Test method for 'unbbayes.prs.mebn.compiler.MEBNTableParser.getInstance(MultiEntityBayesianNetwork, DomainResidentNode)'
	 */
	public void testGetInstanceMultiEntityBayesianNetworkDomainResidentNode() {
		// TODO Auto-generated method stub
		fail("Not yet implemented"); // TODO
	}

	/*
	 * Test method for 'unbbayes.prs.mebn.compiler.MEBNTableParser.getInstance(MultiEntityBayesianNetwork, DomainResidentNode, AbstractCompiler)'
	 */
	public void testGetInstanceMultiEntityBayesianNetworkDomainResidentNodeAbstractCompiler() {
		// TODO Auto-generated method stub
		fail("Not yet implemented"); // TODO
	}

	/*
	 * Test method for 'unbbayes.prs.mebn.compiler.MEBNTableParser.init(String)'
	 */
	public void testInit() {
		// TODO Auto-generated method stub
		fail("Not yet implemented"); // TODO
	}

	/*
	 * Test method for 'unbbayes.prs.mebn.compiler.MEBNTableParser.parse()'
	 */
	public void testParse() {
		Debug.setDebug(true);
		
		PrOwlIO prOwlIO = new PrOwlIO(); 
		
		System.out.println("-----Load file test-----"); 
		
		try{
			mebn = prOwlIO.loadMebn(new File("examples/mebn/StarshipTableParser.owl")); 
			System.out.println("Load concluido"); 
		}
		catch (IOMebnException e){
			e.printStackTrace();
			fail("ERROR IO PROWL!!!!!!!!!"); 
			
		}
		catch (IOException e){			
			e.printStackTrace();
			fail("ERROR IO!!!!!!!!!"); 
		}
		
		
		// should go all right
		String tableString =  
			" if any STi have( OpSpec = Cardassian & HarmPotential = true ) " + 
			"  [ Un = 0 , Hi = 0 , Me = .01 , Lo = .99 ]  " +
			" else if any STj have( OpSpec = Romulan & HarmPotential = true ) " +
			"  [ Un = 0 , Hi = 0 , Me = .01 , Lo = .99 ]  " +
			" else if any STj have( OpSpec = Unknown & HarmPotential = true ) " + 
			"  [ Un = 0 , Hi = 0 , Me = .01 , Lo = .99 ]  " +
			" else if any STk have( OpSpec = Klingon & HarmPotential = true ) " +
			"  [ Un = 0.10 , Hi = 0.15 , Me = .15 , Lo = .65 ] " +
			" else if any STl have( OpSpec = Friend & HarmPotential = true ) " +
			"  [ Un = 0 , Hi = 0 , Me = .01 , Lo = .99 ] " +
			" else [ Un = 0 , Hi = 0 , Me = 0 , Lo = 1 ] ";
		
		MEBNTableParser tableParser = MEBNTableParser.getInstance(mebn, (DomainResidentNode)mebn.getNode("DangerToSelf"));
		
		assertNotNull(tableParser);
		
		try  {
			tableParser.parse(tableString);
		} catch (MEBNException e) {
			fail(e.getMessage());
		} 
		
		
		// Should fail, no default distro
		tableString =  
			" if any STi have( OpSpec = Cardassian & HarmPotential = true ) " + 
			"  [ Un = 0 , Hi = 0 , Me = .01 , Lo = .99 ]  " +
			" else if any STj have( OpSpec = Romulan & HarmPotential = true ) " +
			"  [ Un = 0 , Hi = 0 , Me = .01 , Lo = .99 ]  " +
			" else if any STj have( OpSpec = Unknown & HarmPotential = true ) " + 
			"  [ Un = 0 , Hi = 0 , Me = .01 , Lo = .99 ]  " +
			" else if any STk have( OpSpec = Klingon & HarmPotential = true ) " +
			"  [ Un = 0.10 , Hi = 0.15 , Me = .15 , Lo = .65 ] " +
			" else if any STl have( OpSpec = Friend & HarmPotential = true ) " +
			"  [ Un = 0 , Hi = 0 , Me = .01 , Lo = .99 ] " ;
		
		tableParser = MEBNTableParser.getInstance(mebn, (DomainResidentNode)mebn.getNode("DangerToSelf"));
		
		assertNotNull(tableParser);
		
		try  {
			tableParser.parse(tableString);
		} catch (NoDefaultDistributionDeclaredException e) {
			// pass
		} catch (MEBNException e) {
			fail(e.getMessage());
		} 
		
		
		//		 Should fail sum is above 1
		tableString =  
			" if any STi have( OpSpec = Cardassian & HarmPotential = true ) " + 
			"  [ Un = 0 , Hi = 0 , Me = .01 , Lo = .99 ]  " +
			" else if any STj have( OpSpec = Romulan & HarmPotential = true ) " +
			"  [ Un = 0 , Hi = 0 , Me = .01 , Lo = .99 ]  " +
			" else if any STj have( OpSpec = Unknown & HarmPotential = true ) " + 
			"  [ Un = 0 , Hi = 0 , Me = .01 , Lo = .99 ]  " +
			" else if any STk have( OpSpec = Klingon & HarmPotential = true ) " +
			"  [ Un = 0.10 , Hi = 0.15 , Me = .15 , Lo = .65 ] " +
			" else if any STl have( OpSpec = Friend & HarmPotential = true ) " +
			"  [ Un = 0 , Hi = 0 , Me = .01 , Lo = .99 ] " +
			" else [ Un = 0 , Hi = 0.5 , Me = 0 , Lo = 1 ] ";
		
		tableParser = MEBNTableParser.getInstance(mebn, (DomainResidentNode)mebn.getNode("DangerToSelf"));
		
		assertNotNull(tableParser);
		
		try  {
			tableParser.parse(tableString);
		} catch (InvalidProbabilityRangeException e) {
			// pass
		} catch (MEBNException e) {
			fail(e.getMessage());
		} 
		
		
		//		 Should fail sum is above 1
		tableString =  
			" if any STi have( OpSpec = Cardassian & HarmPotential = true ) " + 
			"  [ Un = 0 , Hi = 0 , Me = .01 , Lo = .99 ]  " +
			" else if any STj have( OpSpec = Romulan & HarmPotential = true ) " +
			"  [ Un = 0 , Hi = 0.2 , Me = .01 , Lo = .99 ]  " +
			" else if any STj have( OpSpec = Unknown & HarmPotential = true ) " + 
			"  [ Un = 0 , Hi = 0 , Me = .01 , Lo = .99 ]  " +
			" else if any STk have( OpSpec = Klingon & HarmPotential = true ) " +
			"  [ Un = 0.10 , Hi = 0.15 , Me = .15 , Lo = .65 ] " +
			" else if any STl have( OpSpec = Friend & HarmPotential = true ) " +
			"  [ Un = 0 , Hi = 0 , Me = .01 , Lo = .99 ] " +
			" else [ Un = 0 , Hi = 0 , Me = 0 , Lo = 1 ] ";
		
		tableParser = MEBNTableParser.getInstance(mebn, (DomainResidentNode)mebn.getNode("DangerToSelf"));
		
		assertNotNull(tableParser);
		
		try  {
			tableParser.parse(tableString);
		} catch (InvalidProbabilityRangeException e) {
			// pass
		} catch (MEBNException e) {
			fail(e.getMessage());
		} 
		
		
		//		 Should fail, some state is above 1
		tableString =  
			" if any STi have( OpSpec = Cardassian & HarmPotential = true ) " + 
			"  [ Un = 0 , Hi = 0 , Me = .01 , Lo = .99 ]  " +
			" else if any STj have( OpSpec = Romulan & HarmPotential = true ) " +
			"  [ Un = 2 , Hi = 0 , Me = .01 , Lo = .99 ]  " +
			" else if any STj have( OpSpec = Unknown & HarmPotential = true ) " + 
			"  [ Un = 0 , Hi = 0 , Me = .01 , Lo = .99 ]  " +
			" else if any STk have( OpSpec = Klingon & HarmPotential = true ) " +
			"  [ Un = 0.10 , Hi = 0.15 , Me = .15 , Lo = .65 ] " +
			" else if any STl have( OpSpec = Friend & HarmPotential = true ) " +
			"  [ Un = 0 , Hi = 0 , Me = .01 , Lo = .99 ] " +
			" else [ Un = 0 , Hi = 0 , Me = 0 , Lo = 1 ] ";
		
		tableParser = MEBNTableParser.getInstance(mebn, (DomainResidentNode)mebn.getNode("DangerToSelf"));
		
		assertNotNull(tableParser);
		
		try  {
			tableParser.parse(tableString);
		} catch (InvalidProbabilityRangeException e) {
			// pass
		} catch (MEBNException e) {
			fail(e.getMessage());
		} 
		
		
		

		//		 Should fail, some state is below 0
		tableString =  
			" if any STi have( OpSpec = Cardassian & HarmPotential = true ) " + 
			"  [ Un = 0 , Hi = 0 , Me = .01 , Lo = .99 ]  " +
			" else if any STj have( OpSpec = Romulan & HarmPotential = true ) " +
			"  [ Un = 2 , Hi = 0 , Me = .01 , Lo = .99 ]  " +
			" else if any STj have( OpSpec = Unknown & HarmPotential = true ) " + 
			"  [ Un = 0 , Hi = 0 , Me = .01 , Lo = .99 ]  " +
			" else if any STk have( OpSpec = Klingon & HarmPotential = true ) " +
			"  [ Un = 0.10 , Hi = 0.15 , Me = .15 , Lo = .65 ] " +
			" else if any STl have( OpSpec = Friend & HarmPotential = true ) " +
			"  [ Un = 0 , Hi = 0 , Me = .01 , Lo = .99 ] " +
			" else [ Un = 0 , Hi = 0.5 , Me = -0.5 , Lo = 1 ] ";
		
		tableParser = MEBNTableParser.getInstance(mebn, (DomainResidentNode)mebn.getNode("DangerToSelf"));
		
		assertNotNull(tableParser);
		
		try  {
			tableParser.parse(tableString);
		} catch (InvalidProbabilityRangeException e) {
			// pass
		} catch (MEBNException e) {
			fail(e.getMessage());
		} 
		
		
	}

	/*
	 * Test method for 'unbbayes.prs.mebn.compiler.MEBNTableParser.parse(String)'
	 */
	public void testParseString() {
		// TODO Auto-generated method stub
		fail("Not yet implemented"); // TODO
	}

	/*
	 * Test method for 'unbbayes.prs.mebn.compiler.MEBNTableParser.getCompiler()'
	 */
	public void testGetCompiler() {
		// TODO Auto-generated method stub
		fail("Not yet implemented"); // TODO
	}

	/*
	 * Test method for 'unbbayes.prs.mebn.compiler.MEBNTableParser.setCompiler(AbstractCompiler)'
	 */
	public void testSetCompiler() {
		// TODO Auto-generated method stub
		fail("Not yet implemented"); // TODO
	}

	/*
	 * Test method for 'unbbayes.prs.mebn.compiler.MEBNTableParser.getMebn()'
	 */
	public void testGetMebn() {
		// TODO Auto-generated method stub
		fail("Not yet implemented"); // TODO
	}

	/*
	 * Test method for 'unbbayes.prs.mebn.compiler.MEBNTableParser.setMebn(MultiEntityBayesianNetwork)'
	 */
	public void testSetMebn() {
		// TODO Auto-generated method stub
		fail("Not yet implemented"); // TODO
	}

	/*
	 * Test method for 'unbbayes.prs.mebn.compiler.MEBNTableParser.getNode()'
	 */
	public void testGetNode() {
		// TODO Auto-generated method stub
		fail("Not yet implemented"); // TODO
	}

	/*
	 * Test method for 'unbbayes.prs.mebn.compiler.MEBNTableParser.setNode(DomainResidentNode)'
	 */
	public void testSetNode() {
		// TODO Auto-generated method stub
		fail("Not yet implemented"); // TODO
	}

}
