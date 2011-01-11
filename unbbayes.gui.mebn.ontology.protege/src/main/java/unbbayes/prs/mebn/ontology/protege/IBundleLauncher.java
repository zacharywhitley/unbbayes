/**
 * 
 */
package unbbayes.prs.mebn.ontology.protege;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Properties;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

/**
 * Classes implementing this interface will launch protege in 
 * the UnBBayes' application context.
 * @author Shou Matsumoto
 *
 */
public interface IBundleLauncher {
	
	/**
	 * The default place to look for a MEBN ontology when protege is started up
	 * @return a URI
	 * @see #startProtegeBundles()
	 */
	public URI getDefaultOntolgyURI();
	
	/**
	 * The default place to look for a MEBN ontology when protege is started up
	 * @param ontologyURI : a URI
	 * @see #startProtegeBundles()
	 */
	public void setDefaultOntolgyURI(URI ontologyURI);
	
	/**
	 * This is the properties passed to OSGi framework when starting bundles.
	 * @return the launch property
	 */
	public Properties getLaunchProperties();
	

	/**
	 * This is the properties passed to OSGi framework when starting bundles.
	 * @return the launch property
	 */
	public void setLaunchProperties(Properties launchProperties);
	
	/**
	 * It starts up all osgi bundles in {@link #getProtegeBundleDir()}
	 * just like the protege 4.1 and starts them up.
	 * @return the successfully initialized (started) bundles.
	 * @throws IOException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public Collection<Bundle> startProtegeBundles() throws BundleException, IOException, InstantiationException, IllegalAccessException;
	
	/**
	 * Obtains the bundle representing the protege 4.1 application.
	 * It must be one of the bundles started in {@link #startProtegeBundles()}
	 * @return the protege application bundle.
	 * @see #startProtegeBundles()
	 */
	public Bundle getProtegeBundle();
	
	/**
	 * Obtains the protege manager (the main controller of protege).
	 * It usually delegates to {@link #getProtegeBundle()}.
	 * @return
	 */
	public Object getProtegeManager();
	
	/**
	 * A path to a directory where protege must look for plug-ins.
	 * @return the directory location 
	 */
	public String getProtegePluginDir();

	/**
	 * A path to a directory where protege must look for plug-ins.
	 * @param pluginDir the directory location to set
	 */
	public void setProtegePluginDir(String pluginDir);

	/**
	 * A path to a directory where {@link #startProtegeBundles()} will look for
	 * bundles. It should contain the protege 4.1 application.
	 * @return the path to the directory
	 */
	public String getProtegeBundleDir();
	

	/**
	 * A path to a directory where {@link #startProtegeBundles()} will look for
	 * bundles. It should contain the protege 4.1 application.
	 * @param bundleDir the path to the directory
	 */
	public void setProtegeBundleDir(String bundleDir);
	
	/**
	 * The names of bundles to be loaded by {@link #startProtegeBundles()}
	 * @return the names
	 */
	public String[] getCoreBundleNames();
	
	/**
	 * The names of bundles to be loaded by {@link #startProtegeBundles()}
	 * @param coreBundleNames : the names
	 */
	public void setCoreBundleNames(String[] coreBundleNames);
	
}
