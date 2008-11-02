/**
 * 
 */
package unbbayes.gui.oobn;

import unbbayes.gui.NetworkWindow;
import unbbayes.prs.Network;
import unbbayes.prs.bn.SingleEntityNetwork;
import unbbayes.prs.oobn.IOOBNClass;
import unbbayes.util.Debug;

/**
 * @author Shou Matsumoto
 *
 */
public class OOBNClassWindow extends NetworkWindow {
	
	public static final long serialVersionUID = 0x00BFL;

	private OOBNClassWindow(Network net) {
		super(net);
	}
	
	private OOBNClassWindow(IOOBNClass oobnClass) {
		
		super(new SingleEntityNetwork(oobnClass.toString()));
		// TODO Auto-generated constructor stub
		Debug.println(this.getClass(), "OOBNClassWindow not yet implemented");
	}
	
	/**
	 * Obtains a new instance of OOBNNetworkWindow
	 * @param oobnClass
	 * @return a new instance
	 */
	public static OOBNClassWindow newInstance(IOOBNClass oobnClass) {
		return new OOBNClassWindow(oobnClass);
		
	}

}
