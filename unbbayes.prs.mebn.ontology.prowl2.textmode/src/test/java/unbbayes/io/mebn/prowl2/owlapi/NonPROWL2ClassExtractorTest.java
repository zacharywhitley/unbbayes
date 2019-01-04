package unbbayes.io.mebn.prowl2.owlapi;

import junit.framework.TestCase;

public class NonPROWL2ClassExtractorTest extends TestCase {

	INonPROWLClassExtractor extractor;
	
	public NonPROWL2ClassExtractorTest(String name) {
		super(name);
	}
	

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		extractor = NonPROWL2ClassExtractorImpl.getInstance();
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		extractor = null;
	}

	public final void testGetNonPROWLClasses() {
		
		
		fail("Not yet implemented"); // TODO
		
	}
	public final void testGetPROWLClasses() {
		fail("Not yet implemented"); // TODO
	}


}
