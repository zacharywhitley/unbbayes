/**
 * 
 */
package unbbayes.gui.mebn.extension.ssmsbn;

import unbbayes.gui.mebn.extension.ssbn.LaskeyAlgorithmOptionPanelBuilder;
import unbbayes.prs.mebn.ssbn.extension.ssmsbn.laskeyalgorithm.LaskeySSMSBNGenerator;
import unbbayes.prs.mebn.ssbn.laskeyalgorithm.LaskeyAlgorithmParameters;
import unbbayes.prs.mebn.ssbn.pruner.impl.PruneStructureImpl;

/**
 * @author estevaoaguiar
 * @author rafaelmezzomo
 */
public class LaskeyAlgorithmOptionPanalBuilderSSMSBN extends
		LaskeyAlgorithmOptionPanelBuilder {

	/**
	 * Default constructor must be public. It is required for the plugin. 
	 */
	public LaskeyAlgorithmOptionPanalBuilderSSMSBN() {
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see unbbayes.gui.mebn.extension.ssbn.LaskeyAlgorithmOptionPanelBuilder#buildAlgorithm()
	 */
	protected void buildAlgorithm() {
		//Instantiating the SSMSBN algorithm.
		LaskeySSMSBNGenerator generator = (LaskeySSMSBNGenerator)LaskeySSMSBNGenerator.newInstance();
		//Setting the parameters of this option panel as the generator`s one.
		this.setParameters((LaskeyAlgorithmParameters)generator.getParameters()); 
		//Pointing this option panel to the above generator.
		setSSBNGenerator(generator);
		//Setting the pruners of this option panel as the generator`s one.
		this.setPruneStructure((PruneStructureImpl)generator.getPruneStructure());
		
	}

	/* (non-Javadoc)
	 * @see unbbayes.gui.mebn.extension.ssbn.LaskeyAlgorithmOptionPanelBuilder#commitChanges()
	 */
	public void commitChanges() {
		super.commitChanges();
		//Someone is reseting the parameters of generator, so we are forcing synchronization again.
		((LaskeySSMSBNGenerator)this.getSSBNGenerator()).setParameters(this.getParameters());
		((LaskeySSMSBNGenerator)this.getSSBNGenerator()).setPruneStructure(this.getPruneStructure());
	}
	
	

}
