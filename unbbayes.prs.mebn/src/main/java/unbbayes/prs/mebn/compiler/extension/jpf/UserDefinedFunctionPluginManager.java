/**
 * 
 */
package unbbayes.prs.mebn.compiler.extension.jpf;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.Iterator;
import java.util.List;

import org.java.plugin.PluginManager;
import org.java.plugin.registry.Extension;
import org.java.plugin.registry.Extension.Parameter;
import org.java.plugin.registry.ExtensionPoint;
import org.java.plugin.registry.PluginDescriptor;

import unbbayes.prs.mebn.compiler.extension.IUserDefinedFunctionBuilder;
import unbbayes.util.Debug;
import unbbayes.util.extension.manager.UnBBayesPluginContextHolder;

/**
 * This is a manager of plug-ins related to user-defined LPD functions.
 * @author Shou Matsumoto
 */
public class UserDefinedFunctionPluginManager {
	
	private String corePluginID = "unbbayes.prs.mebn";
	private String userDefinedLPDFunctionExtensionPointID = "UserDefinedLPDFunction";
	private String paramNameUserDefinedLPDFunctionBuilder = "class";
	private String paramName = "name";
	
	private PluginManager pluginManager = UnBBayesPluginContextHolder.newInstance().getPluginManager();
	
	private List<IUserDefinedFunctionBuilder> functionBuilders;
	
	/**
	 * Default constructor.
	 * @deprecated use {@link #getInstance()}
	 */
	protected UserDefinedFunctionPluginManager() {
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
	public static UserDefinedFunctionPluginManager getInstance(boolean initialize) {
		UserDefinedFunctionPluginManager ret = new UserDefinedFunctionPluginManager();
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
	 * It also updates the {@link #getFunctionBuilders()}
	 */
	public void reloadPlugins() {
		
    	try {
    		getFunctionBuilders().clear();
    		
        	// let's republish plugins if they're not done yet
    		if (!UnBBayesPluginContextHolder.newInstance().isInitialized()) {
    			UnBBayesPluginContextHolder.newInstance().publishPlugins();
    		}

    	    // loads the "mebn" plugin, which declares general extension points for core (including algorithms)
    	    PluginDescriptor core = this.getPluginManager().getRegistry().getPluginDescriptor(this.getCorePluginID());
            
    	    // load the extension point for new user-defined LPD functions.
    	    ExtensionPoint point = this.getPluginManager().getRegistry().getExtensionPoint(core.getId(), this.getUserDefinedLPDFunctionExtensionPointID());
        	
    	    // iterate over the connected extension points
    	    for (Iterator<Extension> it = point.getConnectedExtensions().iterator(); it.hasNext();) {
    			try {
    				Extension ext = it.next();
    	            PluginDescriptor descr = ext.getDeclaringPluginDescriptor();
    	            
    	            if (this.getPluginManager().isBadPlugin(descr)) {
    	            	continue;
    	            }
    	            
    	            this.getPluginManager().activatePlugin(descr.getId());
    				
    				// extracting parameter
    				Parameter builderParam = ext.getParameter(this.getParamNameUserDefinedLPDFunctionBuilder());
    				
    				// extracting builder class
    				ClassLoader classLoader = this.getPluginManager().getPluginClassLoader(descr);
    				
    	            Class builderClass = classLoader.loadClass(builderParam.valueAsString());
    				
    	            // intantiating plugin object
    	            IUserDefinedFunctionBuilder builder = (IUserDefinedFunctionBuilder)builderClass.newInstance();
    		    	
    				// filling the list
    				getFunctionBuilders().add(builder);
    			} catch (Throwable e) {
    				e.printStackTrace();
    				continue;
    			} 
    		}
		} catch (Throwable t) {
			Debug.println(this.getClass(), "Error filling user-defined LPD functions' plugins", t);
		}
	    
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
	 * @return ID of this plug-in.  Default is "UserDefinedLPDFunction"
	 */
	public String getUserDefinedLPDFunctionExtensionPointID() {
		return userDefinedLPDFunctionExtensionPointID;
	}

	/**
	 * @param userDefinedLPDFunctionExtensionPointID : ID of this plug-in.  Default is "UserDefinedLPDFunction"
	 */
	public void setUserDefinedLPDFunctionExtensionPointID(String userDefinedLPDFunctionExtensionPointID) {
		this.userDefinedLPDFunctionExtensionPointID = userDefinedLPDFunctionExtensionPointID;
	}

	/**
	 * @return name of parameter to specify class of {@link IUserDefinedFunctionBuilder}.
	 * Default is "class".
	 */
	public String getParamNameUserDefinedLPDFunctionBuilder() {
		return paramNameUserDefinedLPDFunctionBuilder;
	}

	/**
	 * @param paramNameUserDefinedLPDFunctionBuilder : Name of parameter to specify class of {@link IUserDefinedFunctionBuilder}.
	 * Default is "class".
	 */
	public void setParamNameUserDefinedLPDFunctionBuilder(String paramNameUserDefinedLPDFunctionBuilder) {
		this.paramNameUserDefinedLPDFunctionBuilder = paramNameUserDefinedLPDFunctionBuilder;
	}

	/**
	 * @return the paramName
	 */
	public String getParamName() {
		return paramName;
	}

	/**
	 * @param paramName the paramNameName to set
	 */
	public void setParamName(String paramName) {
		this.paramName = paramName;
	}

	/**
	 * @return the functionBuilders
	 */
	public List<IUserDefinedFunctionBuilder> getFunctionBuilders() {
		if (functionBuilders == null) {
			functionBuilders = new ArrayList<IUserDefinedFunctionBuilder>();
		}
		return functionBuilders;
	}

	/**
	 * @param functionBuilders the functionBuilders to set
	 */
	public void setFunctionBuilders(List<IUserDefinedFunctionBuilder> functionBuilders) {
		this.functionBuilders = functionBuilders;
	}


}
