/**
 * 
 */
package unbbayes.prs.mebn.kb.extension.jpf;

import java.util.EventObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.java.plugin.PluginManager;
import org.java.plugin.registry.Extension;
import org.java.plugin.registry.ExtensionPoint;
import org.java.plugin.registry.PluginDescriptor;
import org.java.plugin.registry.Extension.Parameter;

import unbbayes.gui.mebn.extension.kb.IKBOptionPanelBuilder;
import unbbayes.prs.mebn.kb.extension.IKnowledgeBaseBuilder;
import unbbayes.util.Debug;
import unbbayes.util.extension.manager.UnBBayesPluginContextHolder;

/**
 * This class manages Knowledge Base (First-Order Logic engine)
 * for UnBBayes' MEBN module, using the default JPF plugin infrastructure.
 * @author Shou Matsumoto
 *
 */
public class KnowledgeBasePluginManager {
	
	private String corePluginID = "unbbayes.prs.mebn";
	private String kbExtensionPointID = "KnowledgeBase";
	private String paramNameKBBuilder = "class";
	private String paramNameName = "name";
	private String paramNameOptionPanelBuilder = "optionPanel";
	
	private PluginManager pluginManager = UnBBayesPluginContextHolder.newInstance().getPluginManager();
	
	private Map<IKnowledgeBaseBuilder, IKBOptionPanelBuilder> kbToOptionPanelMap = new HashMap<IKnowledgeBaseBuilder, IKBOptionPanelBuilder>();

	/**
	 * Default constructor.
	 * @deprecated use {@link #getInstance()}
	 */
	protected KnowledgeBasePluginManager() {
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
	public static KnowledgeBasePluginManager getInstance(boolean initialize) {
		KnowledgeBasePluginManager ret = new KnowledgeBasePluginManager();
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
	 * It also updates the {@link #getKbToOptionPanelMap()}
	 */
	public void reloadPlugins() {
		
		Map<IKnowledgeBaseBuilder, IKBOptionPanelBuilder> kbToOptionPanelMap = new HashMap<IKnowledgeBaseBuilder, IKBOptionPanelBuilder>();
		
    	try {
        	// let's republish plugins if they're not done yet
    		if (!UnBBayesPluginContextHolder.newInstance().isInitialized()) {
    			UnBBayesPluginContextHolder.newInstance().publishPlugins();
    		}

    	    // loads the "mebn" plugin, which declares general extension points for core (including algorithms)
    	    PluginDescriptor core = this.getPluginManager().getRegistry().getPluginDescriptor(this.getCorePluginID());
            
    	    // load the extension point for new algorithms (functionalities).
    	    ExtensionPoint point = this.getPluginManager().getRegistry().getExtensionPoint(core.getId(), this.getKbExtensionPointID());
        	
    	    // iterate over the connected extension points
    	    for (Iterator<Extension> it = point.getConnectedExtensions().iterator(); it.hasNext();) {
    			try {
    				Extension ext = it.next();
    	            PluginDescriptor descr = ext.getDeclaringPluginDescriptor();
    	            
    	            this.getPluginManager().activatePlugin(descr.getId());
    				
    				// extracting parameters
    				Parameter kbBuilderParam = ext.getParameter(this.getParamNameKBBuilder());
    				Parameter nameParam = ext.getParameter(this.getParamNameName());
    				Parameter optionPanelParam = ext.getParameter(this.getParamNameOptionPanelBuilder());
    				
    				// extracting builder class
    				
    				ClassLoader classLoader = this.getPluginManager().getPluginClassLoader(descr);
    				
    	            Class kbClass = classLoader.loadClass(kbBuilderParam.valueAsString());
    	            Class optionPanelClass = null;
    	            if ((optionPanelParam != null) && (optionPanelParam.valueAsString().length() > 0)) {
    	            	optionPanelClass = classLoader.loadClass(optionPanelParam.valueAsString());
    	            }
    				
    	            // intantiating plugin object
    		    	IKnowledgeBaseBuilder kbBuilder = (IKnowledgeBaseBuilder)kbClass.newInstance();
    		    	IKBOptionPanelBuilder optionPanelBuilder = null;
    		    	if (optionPanelClass != null) {
    		    		optionPanelBuilder = (IKBOptionPanelBuilder)optionPanelClass.newInstance();
    		    	}
    		    	
    		    	// setting name as extracted from plugin
    		    	kbBuilder.setName(nameParam.valueAsString());
    				
    				// filling the map
    				kbToOptionPanelMap.put(kbBuilder, optionPanelBuilder);
    			} catch (Throwable e) {
    				e.printStackTrace();
    				continue;
    			} 
    		}
		} catch (Throwable t) {
			Debug.println(this.getClass(), "Error filling Inference Algorithm's plugins", t);
		}
	    
    	this.setKbToOptionPanelMap(kbToOptionPanelMap);
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
	 * @return the kbExtensionPointID
	 */
	public String getKbExtensionPointID() {
		return kbExtensionPointID;
	}

	/**
	 * @param kbExtensionPointID the kbExtensionPointID to set
	 */
	public void setKbExtensionPointID(String kbExtensionPointID) {
		this.kbExtensionPointID = kbExtensionPointID;
	}

	/**
	 * @return the paramNameKBBuilder
	 */
	public String getParamNameKBBuilder() {
		return paramNameKBBuilder;
	}

	/**
	 * @param paramNameKBBuilder the paramNameKBBuilder to set
	 */
	public void setParamNameKBBuilder(String paramNameKBBuilder) {
		this.paramNameKBBuilder = paramNameKBBuilder;
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
	 * @return the kbToOptionPanelMap
	 */
	public Map<IKnowledgeBaseBuilder, IKBOptionPanelBuilder> getKbToOptionPanelMap() {
		return kbToOptionPanelMap;
	}

	/**
	 * @param kbToOptionPanelMap the kbToOptionPanelMap to set
	 */
	public void setKbToOptionPanelMap(
			Map<IKnowledgeBaseBuilder, IKBOptionPanelBuilder> kbToOptionPanelMap) {
		this.kbToOptionPanelMap = kbToOptionPanelMap;
	}

}
