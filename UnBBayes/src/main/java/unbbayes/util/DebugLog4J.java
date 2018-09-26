package unbbayes.util;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.helpers.Loader;

/**
 * Implements logs in UnBBayes by redirecting to log4j.
 * @deprecated use log4j instead.
 * @author Shou Matsumoto
 *
 */
public class DebugLog4J {
	
//	private static boolean debug;
	
	private static Logger log = Logger.getLogger(DebugLog4J.class);
	
	public static void setDebug(boolean debug) {
		if (debug) {
			try {
				System.out.println("Looking for log4j.xml at " + Loader.getResource("./"));
				System.out.println("System properties: ");
				System.getProperties().list(System.out);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
//		Debug.debug = debug;
		// if debug == true, turn debug logs on. Else, turn all logs off.
		log.setLevel(debug?Level.DEBUG:Level.OFF);
	}
	
	public static boolean isDebugMode(){
		return !(log.getLevel() == null || log.getLevel().equals(Level.OFF)); 
	}
	
	public static void print(char message) {
//		log.debug(message);
		log.log(Debug.class.getCanonicalName(), Level.DEBUG, message, null);
	}
	
	public static void print(String message) {
//		log.debug(message);
		log.log(Debug.class.getCanonicalName(), Level.DEBUG, message, null);
	}
	
	public static void println(String message) {
//		log.debug(message);
		log.log(Debug.class.getCanonicalName(), Level.DEBUG, message, null);
	}
	
	/**
	 * @deprecated use log4j.properties instead of specifying format on the fly
	 */
	public static void print(String format, String message) {
//		log.debug(message);
		log.log(Debug.class.getCanonicalName(), Level.DEBUG, message, null);
	}
	
	/**
	 * @deprecated use log4j.properties instead of specifying format on the fly
	 */
	public static void print(String format, Object ... message) {
		if (message != null) {
			for (Object m : message) {
//				log.debug(m);
				log.log(Debug.class.getCanonicalName(), Level.DEBUG, m, null);
			}
		}
	}
	
	public static void println(Class classOrigin, String message) {
		Logger log = Logger.getLogger(classOrigin);
		log.setLevel(log.getLevel());	// overload log level
//		log.debug("[DEBUG] " + message);
		log.log(Debug.class.getCanonicalName(), Level.DEBUG, "[DEBUG] " + message, null);
	}
	
	public static void println(Class classOrigin, String message, Throwable t) {
		Logger log = Logger.getLogger(classOrigin);
		log.setLevel(log.getLevel());	// overload log level
//		log.debug("[DEBUG] " + message, t);
		log.log(Debug.class.getCanonicalName(), Level.DEBUG, "[DEBUG] " + message, t);
	}

	/**
	 * @return the log
	 */
	public static Logger getLog() {
		return log;
	}

	/**
	 * @param _log the log to set
	 */
	public static void setLog(Logger _log) {
		log = _log;
	}
}
