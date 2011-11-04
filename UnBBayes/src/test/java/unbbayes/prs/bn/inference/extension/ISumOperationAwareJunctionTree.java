package unbbayes.prs.bn.inference.extension;

import unbbayes.prs.bn.Clique;
import unbbayes.prs.bn.PotentialTable;

public interface ISumOperationAwareJunctionTree {

	/**
	 * This operator is used in {@link #absorb(Clique, Clique)}
	 * to substitute the sum (marginal) operation. The
	 * sum (marginal) operation is used by {@link PotentialTable#getSumOperation()}.
	 * @param maxOperation the maxOperation to set
	 * @see PotentialTable#getSumOperation()
	 * @see PotentialTable#setSumOperation(unbbayes.prs.bn.PotentialTable.ISumOperation)
	 */
	public void setMaxOperation(PotentialTable.ISumOperation maxOperation);

	/**
	 * This operator is used in {@link #absorb(Clique, Clique)}
	 * to substitute the sum (marginal) operation. The
	 * sum (marginal) operation is used by {@link PotentialTable#getSumOperation()}.
	 * @return the maxOperation
	 * @see PotentialTable#getSumOperation()
	 * @see PotentialTable#setSumOperation(unbbayes.prs.bn.PotentialTable.ISumOperation)
	 */
	public PotentialTable.ISumOperation getMaxOperation();

}