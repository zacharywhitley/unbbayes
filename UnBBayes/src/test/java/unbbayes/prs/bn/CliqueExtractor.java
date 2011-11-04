package unbbayes.prs.bn;

/**
 * Objects of this class encapsulates a {@link TreeVariable} and
 * enables access to the associated clique (which is protected in the
 * {@link TreeVariable}).
 * @author Shou Matsumoto
 *
 */
public class CliqueExtractor extends TreeVariable {
	private TreeVariable encapsulatedVariable;

	/**
	 * Constructor initializing field.
	 *  Objects of this class encapsulates a {@link TreeVariable} and
	 * enables access to the associated clique (which is protected in the
	 * {@link TreeVariable}).
	 * @param encapsulatedVariable : the variable to encapsulate
	 */
	public CliqueExtractor(TreeVariable encapsulatedVariable) {
		this.encapsulatedVariable = encapsulatedVariable;
		
	}
	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.INode#getType()
	 */
	public int getType() {return this.getEncapsulatedVariable().getType();}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.bn.TreeVariable#marginal()
	 */
	protected void marginal() {this.getEncapsulatedVariable().marginal();}
	
	
	/**
	 * Forces the associated clique to be visible to other classes.
	 * @see unbbayes.prs.bn.TreeVariable#getAssociatedClique()
	 */
	public IRandomVariable getAssociatedClique() {
		return this.getEncapsulatedVariable().getAssociatedClique();
	}
	
	/**
	 * @return the encapsulatedVariable
	 */
	public TreeVariable getEncapsulatedVariable() {
		return encapsulatedVariable;
	}
	/**
	 * @param encapsulatedVariable the encapsulatedVariable to set
	 */
	public void setEncapsulatedVariable(TreeVariable encapsulatedVariable) {
		this.encapsulatedVariable = encapsulatedVariable;
	}
}