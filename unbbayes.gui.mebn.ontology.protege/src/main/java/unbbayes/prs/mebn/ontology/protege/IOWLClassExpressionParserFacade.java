/**
 * 
 */
package unbbayes.prs.mebn.ontology.protege;

import org.semanticweb.owlapi.model.OWLClassExpression;

/**
 * Classes implementing this interface are facades to methods
 * that parses a string expression and generates instances of 
 * {@link OWLClassExpression}.
 * These functionalities are useful if you want to
 * use OWLAPI and reasoners to obtain OWL objects.
 * @author Shou Matsumoto
 *
 */
public interface IOWLClassExpressionParserFacade {

	/**
	 * Parses an expression to generate an {@link OWLClassExpression}.
	 * This is useful to execute string expressions in OWL reasoners.
	 * @param expression
	 * @return
	 * @throws IllegalArgumentException : if the input is invalid
	 */
	public OWLClassExpression parseExpression(String expression);
}
