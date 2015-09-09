import java.util.Locale;

/**
 * This is just a stub in order to test this plugin on UnBBayes
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
		// change default locale
		Locale.setDefault(new Locale("en"));

		unbbayes.Main.main(args);
	}

}
