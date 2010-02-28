package unbbayes.gui.oobn.extension;

import java.util.ResourceBundle;

import unbbayes.controller.oobn.OOBNController;
import unbbayes.gui.oobn.OOBNWindow;
import unbbayes.prs.oobn.IObjectOrientedBayesianNetwork;
import unbbayes.prs.oobn.impl.ObjectOrientedBayesianNetwork;
import unbbayes.util.extension.UnBBayesModule;
import unbbayes.util.extension.builder.NamedWindowBuilder;

/**
 * This class instantiates a {@link OOBNWindow} using most basic parameters.
 * @author Shou Matsumoto
 *
 */
public class OOBNWindowBuilder extends NamedWindowBuilder {
	
	private ResourceBundle resource;
	
	/**
	 * Default constructor
	 */
	public OOBNWindowBuilder() {
		super();
		
		// Load resource file from this package. This is not done statically to make it hot-pluggable
		this.setResource( 
					unbbayes.util.ResourceController.newInstance().getBundle(
							unbbayes.gui.oobn.resources.OOBNGuiResource.class.getName())
				);
		
		this.setName(this.resource.getString("NewOOBNName"));
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.util.extension.UnBBayesModuleBuilder#buildUnBBayesModule()
	 */
	public UnBBayesModule buildUnBBayesModule() {
		IObjectOrientedBayesianNetwork oobn = ObjectOrientedBayesianNetwork.newInstance(this.getName());
		OOBNController controller = OOBNController.newInstance(oobn);
		return (UnBBayesModule)controller.getPanel();
	}

	/**
	 * @return the resource
	 */
	public ResourceBundle getResource() {
		return resource;
	}

	/**
	 * @param resource the resource to set
	 */
	public void setResource(ResourceBundle resource) {
		this.resource = resource;
	}
	
}
