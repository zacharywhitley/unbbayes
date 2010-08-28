package unbbayes.prs.prm.builders;

import unbbayes.prs.prm.ForeignKey;
import unbbayes.prs.prm.IForeignKey;

public interface IForeignKeyBuilder {

	/**
	 * Build an instance of {@link ForeignKey} using
	 * {@link ForeignKeyBuilder#getName()} + {@link ForeignKeyBuilder#getCount()}
	 * as its name;
	 * @return
	 */
	public abstract IForeignKey buildForeignKey();

}