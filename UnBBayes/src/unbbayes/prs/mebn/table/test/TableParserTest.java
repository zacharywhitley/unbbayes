/**
 * 
 */
package unbbayes.prs.mebn.table.test;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;
import unbbayes.io.mebn.PrOwlIO;
import unbbayes.io.mebn.exceptions.IOMebnException;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.MultiEntityNode;
import unbbayes.prs.mebn.exception.MEBNException;
import unbbayes.prs.mebn.table.TableParser;
import unbbayes.prs.mebn.table.exception.InvalidConditionantException;
import unbbayes.prs.mebn.table.exception.NoDefaultDistributionDeclaredException;
import unbbayes.prs.mebn.table.exception.SomeStateUndeclaredException;

/**
 * @author user
 *
 */
public class TableParserTest extends TestCase {

	/**
	 * @param arg0
	 */
	public TableParserTest(String arg0) {
		super(arg0);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	

	/**
	 * Test method for {@link unbbayes.prs.mebn.table.TableParser}.
	 */
	public void testTableParser() {
		MultiEntityBayesianNetwork mebn = null; 
		
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
		
		String tableString =  
			" if any STi have( OpSpec == Cardassian and HarmPotential == true )then " + 
			"  [ Un = 0 , Hi = 0 , Me = .01 , Lo = .99 ]  " +
			" else if any STj have( OpSpec == Romulan and HarmPotential == true )then " +
			"  [ Un = 0 , Hi = 0 , Me = .01 , Lo = .99 ]  " +
			" else if any STj have( OpSpec == Unknown and HarmPotential == true )then " + 
			"  [ Un = 0 , Hi = 0 , Me = .01 , Lo = .99 ]  " +
			" else if any STk have( OpSpec == Klingon and HarmPotential == true )then " +
			"  [ Un = 0.10 , Hi = 0.15 , Me = .15 , Lo = .65 ] " +
			" else if any STl have( OpSpec == Friend and HarmPotential == true )then " +
			"  [ Un = 0 , Hi = 0 , Me = .01 , Lo = .99 ] " +
			" else [ Un = 0 , Hi = 0 , Me = 0 , Lo = 1 ] ";
		
		TableParser tableParser = new TableParser(mebn, (MultiEntityNode)mebn.getNode("DangerToSelf"));
		
		assertNotNull(tableParser);
		
		try  {
			tableParser.parseTable(tableString);
		} catch (MEBNException e) {
			fail(e.getMessage());
		} 
		
		
		// Fail, no default distro
		
		tableString =  
			" if any STi have( OpSpec == Cardassian and HarmPotential == true )then " + 
			"  [ Un = 0 , Hi = 0 , Me = .01 , Lo = .99 ]  " +
			" else if any STj have( OpSpec == Romulan and HarmPotential == true )then " +
			"  [ Un = 0 , Hi = 0 , Me = .01 , Lo = .99 ]  " +
			" else if any STj have( OpSpec == Unknown and HarmPotential == true )then " + 
			"  [ Un = 0 , Hi = 0 , Me = .01 , Lo = .99 ]  " +
			" else if any STk have( OpSpec == Klingon and HarmPotential == true )then " +
			"  [ Un = 0.10 , Hi = 0.15 , Me = .15 , Lo = .65 ] " +
			" else if any STl have( OpSpec == Friend and HarmPotential == true )then " +
			"  [ Un = 0 , Hi = 0 , Me = .01 , Lo = .99 ] " ;
			
		try  {
			tableParser.parseTable(tableString);
			fail("NoDefaultDistributionDeclaredException expected");
		} catch (NoDefaultDistributionDeclaredException e) {
			// passed
		} catch (MEBNException e) {
			fail(e.getMessage());
		}
		
		
		// Fail, not every states has respective probability
		
		tableString =  
			" if any STi have( OpSpec == Cardassian and HarmPotential == true )then " + 
			"  [ Un = 0 , Hi = 0 , Me = .01 , Lo = .99 ]  " +
			" else if any STj have( OpSpec == Romulan and HarmPotential == true )then " +
			"  [ Un = 0 , Hi = 0 , Me = .01 , Lo = .99 ]  " +
			" else if any STj have( OpSpec == Unknown and HarmPotential == true )then " + 
			"  [ Un = 0 , Hi = 0 , Me = .01 , Lo = .99 ]  " +
			" else if any STk have( OpSpec == Klingon and HarmPotential == true )then " +
			"  [ Un = 0.10 , Hi = 0.15 , Me = .15 , Lo = .65 ] " +
			" else if any STl have( OpSpec == Friend and HarmPotential == true )then " +
			"  [ Un = 0 , Hi = 0 , Me = .01 , Lo = .99 ] " +
			" else [ Un = 0 , Hi = .5 , Me = 0.5] ";
			
		try  {
			tableParser.parseTable(tableString);
			fail("SomeStateUndeclaredException expected");
		} catch (SomeStateUndeclaredException e) {
			// passed
		} catch (MEBNException e) {
			fail(e.getMessage());
		}
		
		
// Fail, not every states has respective probability
		
		tableString =  
			" if any STi have( OpSpec == Cardassian and HarmPotential == true )then " + 
			"  [ Un = 0 , Hi = 0 , Me = .01 , Lo = .99 ]  " +
			" else if any STj have( OpSpec == Romulan and HarmPotential == true )then " +
			"  [ Un = 0 , Hi = 0 , Me = .01 , Lo = .99 ]  " +
			" else if any STj have( OpSpec == Unknown and HarmPotential == true )then " + 
			"  [ Un = 0 , Me = .01 , Lo = .99 ]  " +
			" else if any STk have( OpSpec == Klingon and HarmPotential == true )then " +
			"  [ Un = 0.10 , Hi = 0.15 , Me = .15 , Lo = .65 ] " +
			" else if any STl have( OpSpec == Friend and HarmPotential == true )then " +
			"  [ Un = 0 , Hi = 0 , Me = .01 , Lo = .99 ] " +
			" else [ Un = 0 , Hi = .5 , Me = 0.5 , Lo = 0] ";
			
		try  {
			tableParser.parseTable(tableString);
			fail("SomeStateUndeclaredException expected");
		} catch (SomeStateUndeclaredException e) {
			// passed
		} catch (MEBNException e) {
			fail(e.getMessage());
		}
		
	}

}
