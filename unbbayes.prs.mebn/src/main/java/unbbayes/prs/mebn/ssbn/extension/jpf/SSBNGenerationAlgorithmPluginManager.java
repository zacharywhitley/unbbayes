/**
 * 
 */
package unbbayes.prs.mebn.ssbn.extension.jpf;

import java.util.EventObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.java.plugin.PluginManager;
import org.java.plugin.registry.Extension;
import org.java.plugin.registry.ExtensionPoint;
import org.java.plugin.registry.PluginDescriptor;
import org.java.plugin.registry.Extension.Parameter;

import unbbayes.gui.mebn.extension.ssbn.ISSBNOptionPanelBuilder;
import unbbayes.prs.mebn.ssbn.extension.ISSBNGeneratorBuilder;
import unbbayes.util.Debug;
import unbbayes.util.extension.manager.UnBBayesPluginContextHolder;

/**
 * This class manages Situation Specific Bayesian Network
 * generation algorithms' (MEBN compilation) plugins
 * for UnBBayes' MEBN module, using the default JPF plugin infrastructure.
 * @author Shou Matsumoto
 *
 */
public class SSBNGenerationAlgorithmPluginManager {
	
	private String corePluginID = "unbbayes.prs.mebn";
	private String ssbnExtensionPointID = "SSBN";
	private String paramNameSSBNGeneratorBuilder = "class";
	private String paramNameName = "name";
	private String paramNameOptionPanelBuilder = "optionPanel";
	
	private PluginManager pluginManager = UnBBayesPluginContextHolder.newInstance().getPluginManager();
	
	private Map<ISSBNGeneratorBuilder, ISSBNOptionPanelBuilder> ssbnToOptionPanelMap = new HashMap<ISSBNGeneratorBuilder, ISSBNOptionPanelBuilder>();

	/**
	 * Default constructor.
	 * @deprecated use {@link #getInstance()}
	 */
	protected SSBNGenerationAlgorithmPluginManager() {
		// fill listener triggered when we press "reload plugin"
		UnBBayesPluginContextHolder.newInstance().addListener(new UnBBayesPluginContextHolder.OnReloadActionListener() {
			public void onReload(EventObject eventObject) {
				reloadPlugins();
			}
		});
	}
	
	/**
	 * This is the default constructor method.
	 * @param initialize : if set to true, the plugins will be loaded and initialized 
	 * ({@link #initialize()} will be called).
	 * If false, {@link #initialize()} will not be called. Please, note that
	 * you may call {@link #initialize()} afterwards.
	 * @return
	 */
	public static SSBNGenerationAlgorithmPluginManager getInstance(boolean initialize) {
		SSBNGenerationAlgorithmPluginManager ret = new SSBNGenerationAlgorithmPluginManager();
		if (initialize) {
			ret.initialize();
		}
		return ret;
	}
	
	/**
	 * This method will initialize this manager
	 */
	public void initialize() {
		this.reloadPlugins();
	}
	
	/**
	 * This method reloads all plugins.
	 * It also updates the {@link #getSSBNToOptionPanelMap()}
	 */
	public void reloadPlugins() {
		
		Map<ISSBNGeneratorBuilder, ISSBNOptionPanelBuilder> ssbnToOptionPanelMap = new HashMap<ISSBNGeneratorBuilder, ISSBNOptionPanelBuilder>();
		
    	try {
        	// let's republish plugins if they're not done yet
    		if (!UnBBayesPluginContextHolder.newInstance().isInitialized()) {
    			UnBBayesPluginContextHolder.newInstance().publishPlugins();
    		}

    	    // loads the "mebn" plugin, which declares general extension points for core (including algorithms)
    	    PluginDescriptor core = this.getPluginManager().getRegistry().getPluginDescriptor(this.getCorePluginID());
            
    	    // load the extension point for new algorithms (functionalities).
    	    ExtensionPoint point = this.getPluginManager().getRegistry().getExtensionPoint(core.getId(), this.getSSBNExtensionPointID());
        	
    	    // iterate over the connected extension points
    	    for (Iterator<Extension> it = point.getConnectedExtensions().iterator(); it.hasNext();) {
    			try {
    				Extension ext = it.next();
    	            PluginDescriptor descr = ext.getDeclaringPluginDescriptor();
    	            
    	            if (this.getPluginManager().isBadPlugin(descr)) {
    	            	continue;
    	            }
    	            
    	            this.getPluginManager().activatePlugin(descr.getId());
    				
    				// extracting parameters
    				Parameter ssbnBuilderParam = ext.getParameter(this.getParamNameSSBNGeneratorBuilder());
    				Parameter nameParam = ext.getParameter(this.getParamNameName());
    				Parameter optionPanelParam = ext.getParameter(this.getParamNameOptionPanelBuilder());
    				
    				// extracting builder class
    				
    				ClassLoader classLoader = this.getPluginManager().getPluginClassLoader(descr);
    				
    	            Class ssbnClass = classLoader.loadClass(ssbnBuilderParam.valueAsString());
    	            Class optionPanelClass = null;
    	            if ((optionPanelParam != null) && (optionPanelParam.valueAsString().length() > 0)) {
    	            	optionPanelClass = classLoader.loadClass(optionPanelParam.valueAsString());
    	            }
    				
    	            // intantiating plugin object
    	            ISSBNGeneratorBuilder ssbnBuilder = (ISSBNGeneratorBuilder)ssbnClass.newInstance();
    	            ISSBNOptionPanelBuilder optionPanelBuilder = null;
    		    	if (optionPanelClass != null) {
    		    		optionPanelBuilder = (ISSBNOptionPanelBuilder)optionPanelClass.newInstance();
    		    	}
    		    	
    		    	// setting name as extracted from plugin
    		    	ssbnBuilder.setName(nameParam.valueAsString());
    				
    				// filling the map
    				ssbnToOptionPanelMap.put(ssbnBuilder, optionPanelBuilder);
    			} catch (Throwable e) {
    				e.printStackTrace();
    				continue;
    			} 
    		}
		} catch (Throwable t) {
			Debug.println(this.getClass(), "Error filling SSBN Algorithm's plugins", t);
		}
	    
    	this.setSSBNToOptionPanelMap(ssbnToOptionPanelMap);
	}

	/**
	 * Obtains the currently used plugin manager
	 * @return
	 */
	public PluginManager getPluginManager() {
		return this.pluginManager;
	}
	
	/**
	 * Sets the plugin manager to use
	 * @param manager
	 */
	public void setPluginManager(PluginManager manager) {
		this.pluginManager = manager;
	}

	/**
	 * @return the corePluginID
	 */
	public String getCorePluginID() {
		return corePluginID;
	}

	/**
	 * @param corePluginID the corePluginID to set
	 */
	public void setCorePluginID(String corePluginID) {
		this.corePluginID = corePluginID;
	}

	/**
	 * @return the ssbnExtensionPointID
	 */
	public String getSSBNExtensionPointID() {
		return ssbnExtensionPointID;
	}

	/**
	 * @param ssbnExtensionPointID the ssbnExtensionPointID to set
	 */
	public void setSSBNExtensionPointID(String ssbnExtensionPointID) {
		this.ssbnExtensionPointID = ssbnExtensionPointID;
	}

	/**
	 * @return the paramNameSSBNGeneratorBuilder
	 */
	public String getParamNameSSBNGeneratorBuilder() {
		return paramNameSSBNGeneratorBuilder;
	}

	/**
	 * @param paramNameSSBNGeneratorBuilder the paramNameSSBNGeneratorBuilder to set
	 */
	public void setParamNameSSBNGeneratorBuilder(String paramNameSSBNGeneratorBuilder) {
		this.paramNameSSBNGeneratorBuilder = paramNameSSBNGeneratorBuilder;
	}

	/**
	 * @return the paramNameName
	 */
	public String getParamNameName() {
		return paramNameName;
	}

	/**
	 * @param paramNameName the paramNameName to set
	 */
	public void setParamNameName(String paramNameName) {
		this.paramNameName = paramNameName;
	}

	/**
	 * @return the paramNameOptionPanelBuilder
	 */
	public String getParamNameOptionPanelBuilder() {
		return paramNameOptionPanelBuilder;
	}

	/**
	 * @param paramNameOptionPanelBuilder the paramNameOptionPanelBuilder to set
	 */
	public void setParamNameOptionPanelBuilder(String paramNameOptionPanelBuilder) {
		this.paramNameOptionPanelBuilder = paramNameOptionPanelBuilder;
	}

	/**
	 * @return the ssbnToOptionPanelMap
	 */
	public Map<ISSBNGeneratorBuilder, ISSBNOptionPanelBuilder> getSSBNToOptionPanelMap() {
		return ssbnToOptionPanelMap;
	}

	/**
	 * @param ssbnToOptionPanelMap the ssbnToOptionPanelMap to set
	 */
	public void setSSBNToOptionPanelMap(
			Map<ISSBNGeneratorBuilder, ISSBNOptionPanelBuilder> ssbnToOptionPanelMap) {
		this.ssbnToOptionPanelMap = ssbnToOptionPanelMap;
	}

}
