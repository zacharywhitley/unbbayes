/**
 * 
 */
package unbbayes.prs.mebn.ontology.protege;

import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntaxEditorParser;
import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLOntology;

import unbbayes.util.Debug;

/**
 * This is the default implementation of {@link IOWLClassExpressionParserFacade}.
 * By default, it will parse manchester owl syntax.
 * If {@link #getOWLModelManager()} != null, then it will delegate queries to {@link #getOWLModelManager()}.
 * If this facade should delegate to protege API, then use {@link #getInstance(OWLModelManager)} to instantiate this class.
 * If this facade should delegate to OWLAPI (using {@link ManchesterOWLSyntaxEditorParser}), then use {@link #getInstance(OWLOntology)}
 * to instantiate this class.
 * @author Shou Matsumoto
 *
 */
public class OWLClassExpressionParserFacade implements IOWLClassExpressionParserFacade {

	private OWLModelManager owlModelManager = null;
	
	private OWLOntology owlOntology;
	
	/**
	 * The default constructor is not private in order to allow inheritance
	 * @deprecated use {@link #getInstance()} or {@link #getInstance(OWLModelManager)}
	 */
	protected OWLClassExpressionParserFacade() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Default construction method using parameters.
	 * @param owlOntology : if this facade must delegate queries to OWLAPI,
	 * specify the protege's model manager here. If this facade should delegate to Protege,
	 * then use {@link #getInstance(OWLModelManager)} instead.
	 * @return
	 */
	public static IOWLClassExpressionParserFacade getInstance(OWLOntology owlOntology) {
		OWLClassExpressionParserFacade ret = new OWLClassExpressionParserFacade();
		ret.setOwlOntology(owlOntology);
		return ret;
	}
	
	/**
	 * Default construction method using parameters.
	 * @param protegeOWLModelManager : if this facade must delegate queries to protege,
	 * specify the protege's model manager here. If this facade should delegate to OWLAPI,
	 * then use {@link #getInstance(OWLOntology)} instead.
	 * @return
	 */
	public static IOWLClassExpressionParserFacade getInstance(OWLModelManager protegeOWLModelManager) {
		OWLClassExpressionParserFacade ret = new OWLClassExpressionParserFacade();
		ret.setOWLModelManager(protegeOWLModelManager);
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.mebn.ontology.protege.IOWLClassExpressionParserFacade#parseExpression(java.lang.String)
	 */
	public OWLClassExpression parseExpression(String expression) {
		try {
			if (this.getOWLModelManager() != null) {
				// if we can access protege, use it.
				try {
					Debug.println(this.getClass(), "Using protege to parse expression: " + expression);
				} catch (Throwable e) {
					e.printStackTrace();
				}
				return this.getOWLModelManager().getOWLExpressionCheckerFactory().getOWLClassExpressionChecker().createObject(expression);
			} else {
				// it seems that we are using OWLAPI directly. Assume manchester sintax expression
				try {
					Debug.println(this.getClass(), "Using OWLAPI's manchester owl syntax to parse expression: " + expression);
				} catch (Throwable e) {
					e.printStackTrace();
				}
				ManchesterOWLSyntaxEditorParser parser = new ManchesterOWLSyntaxEditorParser(this.getOwlOntology().getOWLOntologyManager().getOWLDataFactory(), expression);
				return parser.parseClassExpression();
			}
		} catch (Exception e) {
			throw new IllegalArgumentException(expression, e);	// tell superclass that this is an unknown exception
		}
		// unreachable code
//		return null;
	}

	/**
	 * 
	 * Main access point to protege's API.
	 * If this class expression uses protege, then set the {@link OWLModelManager}.
	 * @return the owlModelManager
	 */
	public OWLModelManager getOWLModelManager() {
		return owlModelManager;
	}

	/**
	 * Main access point to protege's API.
	 * If this class expression uses protege, then set the {@link OWLModelManager}.
	 * @param owlModelManager the owlModelManager to set
	 */
	public void setOWLModelManager(OWLModelManager owlModelManager) {
		this.owlModelManager = owlModelManager;
	}

	/**
	 * OBS. If {@link #getOWLModelManager()} != null, then this method will delegate to {@link #getOWLModelManager()}.
	 * @return the owlOntology
	 */
	public OWLOntology getOwlOntology() {
		if (this.getOWLModelManager() != null) {
			return this.getOWLModelManager().getActiveOntology();
		}
		return owlOntology;
	}

	/**
	 * OBS. If {@link #getOWLModelManager()} != null, then this method will delegate to {@link #getOWLModelManager()}.
	 * @param owlOntology the owlOntology to set
	 */
	public void setOwlOntology(OWLOntology owlOntology) {
		if (this.getOWLModelManager() != null) {
			this.getOWLModelManager().setActiveOntology(owlOntology);
			return;
		}
		this.owlOntology = owlOntology;
	}

}
