/**
 * 
 */
package unbbayes;

import unbbayes.controller.MainController;
import unbbayes.util.Debug;

/**
 * Starts UnBBayes in Debug mode
 * @author Shou Matsumoto
 *
 */
public class MainDebug {

	/**
	 * Starts UnBBayes in Debug mode
	 * @param args
	 */
	public static void main(String[] args) {
		Debug.setDebug(true);
		new MainController();
	}

}
