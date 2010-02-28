

import java.util.Locale;

import unbbayes.util.Debug;

/**
 * This is just a stub in order to test this plugin
 * on UnBBayes
 * @author Shou Matsumoto
 * @author Danilo
 */
public class StartMonteCarlo {

	/**
	 * It just delegates to UnBBayes' main
	 * @param args
	 */
	public static void main(String[] args) {
		// change default locale
		Locale.setDefault(new Locale("en"));
		// enable debug mode 
		Debug.setDebug(true);
		// delegate to UnBBayes
		unbbayes.Main.main(args);
	}
}
