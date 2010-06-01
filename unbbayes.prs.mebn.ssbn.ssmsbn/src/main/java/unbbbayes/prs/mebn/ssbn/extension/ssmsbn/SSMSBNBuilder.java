/**
 * 
 */
package unbbbayes.prs.mebn.ssbn.extension.ssmsbn;

/**
 * 
 * 
 * @author rafaelmezzomo
 * @author estevaoaguiar
 */
public class SSMSBNBuilder implements ISSMSBNBuilder{
	
	protected SSMSBNBuilder(){
		
	}
	/**
	 * Construction method
	 * @return SSMSBNBuilder()
	 */
	public static ISSMSBNBuilder newInstance(){
		return new SSMSBNBuilder();
	}
	/**
	 * Builder method
	 */
	public SSMSBN buildSSMSBN() {
		SSMSBN ssmsbn = SSMSBN.newInstance();
		return ssmsbn;
	}
	
}
