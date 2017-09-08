/**
 * 
 */
package unbbayes.prs.medg.ssid;

import java.util.List;

import unbbayes.prs.mebn.ssbn.SSBN;

/**
 * @author Shou Matsumoto
 * @deprecated classes involving {@link unbbayes.prs.medg.ssid.SSID} and {@link unbbayes.prs.medg.ssid.SSIDNode} should be avoided and use {@link unbbayes.prs.medg.ssid.SSIDGenerator} to generate ID from SSBN directly
 */
public class SSID extends SSBN {

	/**
	 * 
	 */
	public SSID() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see unbbayes.prs.mebn.ssbn.SSBN#getSsbnNodeList()
	 */
	public List<SSIDNode> getSSIDNodeList() {
		return (List)super.getSsbnNodeList();
	}
	
	

}
