/**
 * 
 */
package unbbayes.gui.oobn;

import unbbayes.controller.NetworkController;
import unbbayes.gui.EditNet;
import unbbayes.gui.NetworkWindow;

/**
 * @author Shou Matsumoto
 *
 */
public class EditOOBN extends EditNet {

	/**
	 * @param window
	 * @param _controller
	 */
	protected EditOOBN(NetworkWindow window, NetworkController _controller) {
		super(window, _controller);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param window
	 * @param _controller
	 */
	public static EditOOBN newInstance(NetworkWindow window, NetworkController _controller) {
		return new EditOOBN( window,  _controller) ;
	}
	
}
