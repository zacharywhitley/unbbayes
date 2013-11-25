/**
 * 
 */
package com.Tuuyi.TuuyiOntologyServer;

import java.util.List;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.apache.commons.collections.primitives.IntIterator;

import com.Tuuyi.TuuyiOntologyServer.generatedClasses.TuuyiOntologyServer.Term;

import junit.framework.TestCase;

/**
 * Test unit for some methods in {@link OntologyClient}
 * @author Shou Matsumoto
 *
 */
public class OntologyClientTest extends TestCase {
	
	/** The object to be tested. A new instance shall be created at each test, so instantiation happens at {@link #setUp()} */
	private OntologyClient client = null;

	/**
	 * @param name
	 */
	public OntologyClientTest(String name) {
		super(name);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		// instantiate the object to be tested
		client = new OntologyClient();
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		// just make sure the same instance is not going to be used anymore
		client = null;
	}
	

	/**
	 * Test method for {@link com.Tuuyi.TuuyiOntologyServer.OntologyClient#getTermDescendants(int)}.
	 */
	public final void testGetTermDescendants() {
		// ID 52354 is supposedly Category:Science
		List<Integer> descendantIds = client.getTermDescendants(52354);
		
		// basic checks
		assertNotNull(descendantIds);
		assertTrue(descendantIds.size() > 0);
		
		// make sure at least Category:Scientific_disciplines is included in immediate descendant
		boolean hasScientificDiscipline = false;
		for (Integer id : descendantIds) {
			Term term = client.getTermById(id);
			if (term.getSimpleName().equalsIgnoreCase("Category:Scientific_disciplines")) {
				hasScientificDiscipline = true;
				break;
			}
		}
		assertTrue(hasScientificDiscipline);
	}

	/**
	 * Test method for {@link com.Tuuyi.TuuyiOntologyServer.OntologyClient#getTermBySimpleName(java.lang.String)}.
	 */
	public final void testGetTermBySimpleName() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.Tuuyi.TuuyiOntologyServer.OntologyClient#getTermById(int)}.
	 */
	public final void testGetTermById() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.Tuuyi.TuuyiOntologyServer.OntologyClient#getTermAncestors(int)}.
	 */
	public final void testGetTermAncestors() {
		fail("Not yet implemented"); // TODO
	}
	

	/**
	 * Test method for {@link com.Tuuyi.TuuyiOntologyServer.OntologyClient#mapText(java.lang.String, com.Tuuyi.TuuyiOntologyServer.generatedClasses.TuuyiOntologyServer.LexicalForm.Context)}.
	 */
	public final void testMapText() {
		fail("Not yet implemented"); // TODO
	}


}
