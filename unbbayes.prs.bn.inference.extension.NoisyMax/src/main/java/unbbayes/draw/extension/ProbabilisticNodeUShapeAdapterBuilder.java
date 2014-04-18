/**
 * 
 */
package unbbayes.draw.extension;


/**
 * This class simply builds a {@link ProbabilisticNodePluginUShapeAdapter}
 * @author Shou Matsumoto
 *
 */
public class ProbabilisticNodeUShapeAdapterBuilder implements IPluginUShapeBuilder {

	/**
	 * Default constructor is made public to allow instantiation by plugin mechanism.
	 */
	public ProbabilisticNodeUShapeAdapterBuilder() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see unbbayes.draw.extension.IPluginUShapeBuilder#build()
	 */
	public IPluginUShape build() throws IllegalAccessException,
			InstantiationException {
		return new ProbabilisticNodePluginUShapeAdapter();
	}
	

}
