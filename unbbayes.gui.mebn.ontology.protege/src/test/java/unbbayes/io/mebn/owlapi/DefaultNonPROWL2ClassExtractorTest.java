package unbbayes.io.mebn.owlapi;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;

import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import unbbayes.io.exception.LoadException;
import unbbayes.io.mebn.UbfIO2;
import unbbayes.io.mebn.protege.IProtegeStorageImplementorDecorator;
import unbbayes.prs.Graph;
import unbbayes.prs.mebn.IRIAwareMultiEntityBayesianNetwork;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.util.IBridgeImplementor;
import junit.framework.TestCase;

public class DefaultNonPROWL2ClassExtractorTest extends TestCase {
	
	INonPROWLClassExtractor extractor;

	public DefaultNonPROWL2ClassExtractorTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		extractor = DefaultNonPROWL2ClassExtractor.getInstance();
	}


	public final void testGetPROWLClasses() throws LoadException, IOException, URISyntaxException {
		
		UbfIO2 io = UbfIO2.getInstance();
		
		File input = new File(getClass().getResource("/VehicleIdentificationTBox/VehicleIdentificationTBox.ubf").toURI());
		assertTrue(input.exists());
		
		IRIAwareMultiEntityBayesianNetwork mebn = (IRIAwareMultiEntityBayesianNetwork) io.load(input);
		assertNotNull(mebn);
		
		IBridgeImplementor bridge = mebn.getStorageImplementor();
		assertNotNull(bridge);
		
		assertTrue(IProtegeStorageImplementorDecorator.class.isAssignableFrom(bridge.getClass()));
		IProtegeStorageImplementorDecorator decorator = (IProtegeStorageImplementorDecorator) bridge;
		
		OWLReasoner reasoner = decorator.getOWLReasoner();
		assertNotNull(reasoner);
		
		OWLOntology ontology = reasoner.getRootOntology();
		assertNotNull(ontology);
		
		Collection<OWLClassExpression> owlClasses = extractor.getPROWLClasses(ontology);
		assertNotNull(owlClasses);
		assertFalse(owlClasses.isEmpty());
		for (OWLClassExpression classExpression : owlClasses) {
			String iri = classExpression.asOWLClass().getIRI().toString();
			assertTrue(iri, iri.contains("http://www.pr-owl.org/pr-owl"));
		}
		
		owlClasses = extractor.getNonPROWLClasses(ontology);
		assertNotNull(owlClasses);
		assertFalse(owlClasses.isEmpty());
		for (OWLClassExpression classExpression : owlClasses) {
			String iri = classExpression.asOWLClass().getIRI().toString();
			assertFalse(iri, iri.contains("http://www.pr-owl.org/pr-owl"));
		}
		
		
		
		
		
		
	}

}
