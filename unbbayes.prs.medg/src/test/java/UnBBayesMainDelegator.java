

import java.net.URISyntaxException;
import java.util.Locale;

import unbbayes.prs.mebn.ssbn.util.SSBNDebugInformationUtil;
import unbbayes.util.Debug;

/**
 * This is just a stub in order to test this plugin
 * on UnBBayes
 * @author Shou Matsumoto
 *
 */
public class UnBBayesMainDelegator {

	/**
	 * Default empty constructor
	 */
	protected UnBBayesMainDelegator() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * It just delegates to UnBBayes' main
	 * @param args
	 */
	public static void main(String[] args) {
		System.setProperty("log4j.debug", "true");
		try {
//			System.setProperty("log4j.configuration", new java.io.File(UnBBayesMainDelegator.class.getClassLoader().getResource("log4j.properties").toURI()).getAbsolutePath());
			System.setProperty("log4j.configuration", "./log4j.properties");
		} catch (Exception e) {
			e.printStackTrace();
		}
		// change default locale
		Locale.setDefault(new Locale("en"));
		// enable debug mode
		SSBNDebugInformationUtil.setEnabled(true);
		Debug.setDebug(true);
		try {
			// print "working" directory
			Debug.println(new java.io.File(UnBBayesMainDelegator.class.getResource("./").toURI()).getAbsolutePath());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		// delegate to UnBBayes
		unbbayes.Main.main(args);
	}

}
