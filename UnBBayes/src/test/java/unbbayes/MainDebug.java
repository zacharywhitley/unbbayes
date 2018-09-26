/**
 * 
 */
package unbbayes;

import java.util.Locale;

import unbbayes.controller.MainController;
import unbbayes.util.Debug;
import unbbayes.util.ResourceController;

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
		// uncomment the below line and change the locale to test resources and locales
		Locale.setDefault(Locale.ENGLISH);
		ResourceController.newInstance().setDefaultLocale(Locale.ENGLISH);
		new MainController();
	}

}
