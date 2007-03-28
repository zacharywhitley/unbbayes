package unbbayes.util;

public class Debug {
	
	private static boolean debug;
	
	public static void setDebug(boolean debug) {
		Debug.debug = debug;
		
	}
	
	public static void print(String message) {
		if (debug)
			System.out.print(message);
	}
	
	public static void println(String message) {
		if (debug)
			System.out.println(message);
	}

}
