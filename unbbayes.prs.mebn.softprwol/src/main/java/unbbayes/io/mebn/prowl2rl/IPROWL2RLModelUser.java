package unbbayes.io.mebn.prowl2rl;

import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.util.DefaultPrefixManager;

import unbbayes.io.mebn.owlapi.IPROWL2ModelUser;

public interface IPROWL2RLModelUser extends IPROWL2ModelUser{
	
	public static final String PROWL2RL_NAMESPACEURI =  "http://www.pr-owl.org/pr-owl2rl.owl";
	
	public static final PrefixManager PROWL2RL_DEFAULTPREFIXMANAGER = new DefaultPrefixManager(PROWL2RL_NAMESPACEURI + '#');
	
	/** Name of the object property linking mexpressions to arguments */
	public static final String HASARGUMENT_MEXPRESSIONDOMAIN = "hasArgumentA";
	public static final String HASARGUMENT_RANDOMVARIABLEDOMAIN = "hasArgumentB";
	
	/** Inverse of {@link #HASARGUMENT} */
	public static final String ISARGUMENTOF_MEXPRESSIONRANGE = "isArgumentOfA";
	public static final String ISARGUMENTOF_RANDOMVARIABLERANGE = "isArgumentOfB";
	
	/** Name of the object property linking nodes to its parents*/
	public static final String HASPARENT_RESIDENTNODERANGE = "hasParentA";
	public static final String HASPARENT_INPUTNODERANGE = "hasParentB";
	
	/** Inverse of {@link #HASPARENT}*/
	public static final String ISPARENTOF_RESIDENTNODEDOMAIN = "isParentOfA";
	public static final String ISPARENTOF_INPUTNODEDOMAIN = "isParentOfB";
	
	/**  Name of the object property linking resident nodes or random variables to probability distribution functions*/
	public static final String HASPROBABILITYDISTRIBUTIONA = "hasProbabilityDistributionA";
	public static final String HASPROBABILITYDISTRIBUTIONB = "hasProbabilityDistributionB";
}
