/**
 * 
 */
package unbbayes.util;

import java.io.Serializable;
import java.util.Properties;

/**
 * @author Shou Matsumoto
 * A static holder for application.property file.
 * The default place to look for is a file named "application.property" at application's root folder. 
 * If not found, it will also search at the classloader path.
 * If not found, it will also search the src/main/resources,
 * and if that fails too, it will search for src/.
 */
public class ApplicationPropertyHolder implements
		Serializable {

	private static final long serialVersionUID = -1670788505984056699L;
	
	private static Properties property;
	private static String applicationPropertyPath = "application.properties";
	
	

	static {
		// initialize property using default values
		property = new Properties();
		try {
			property.load(ApplicationPropertyHolder.class.getClassLoader().getResourceAsStream(new java.io.File(applicationPropertyPath).toURI().getPath()));
		} catch (Exception e) {
			try {
				property.load(ApplicationPropertyHolder.class.getClassLoader().getResourceAsStream(applicationPropertyPath));
			} catch (Exception e2) {
				try {
					applicationPropertyPath = "src/main/resources/" + applicationPropertyPath;
					property.load(ApplicationPropertyHolder.class.getClassLoader().getResourceAsStream(applicationPropertyPath));
				} catch (Exception e3) {
					try {
						applicationPropertyPath = "src/" + applicationPropertyPath;
						property.load(ApplicationPropertyHolder.class.getClassLoader().getResourceAsStream(applicationPropertyPath));
					} catch (Exception e4) {
						e.printStackTrace();
						e2.printStackTrace();
						e3.printStackTrace();
						e4.printStackTrace();
					}
				}
			}
		}
	}
	


	/**
	 * @return the property
	 */
	public static Properties getProperty() {
		return property;
	}

	/**
	 * @param property the property to set
	 */
	public static void setProperty(Properties property) {
		ApplicationPropertyHolder.property = property;
	}

	/**
	 * @return the applicationPropertyPath
	 */
	protected static String getApplicationPropertyPath() {
		return applicationPropertyPath;
	}

	/**
	 * @param applicationPropertyPath the applicationPropertyPath to set
	 */
	protected static void setApplicationPropertyPath(String applicationPropertyPath) {
		ApplicationPropertyHolder.applicationPropertyPath = applicationPropertyPath;
	}
	
	

}
