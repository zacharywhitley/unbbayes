package linfca;

import java.io.IOException;

import org.jdom.Element;
import org.jdom.output.XMLOutputter;

/**
 * @author administrador
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class XMLUtil {
	public static void print(Element e) throws IOException {
		XMLOutputter xml = new XMLOutputter(" ", true);
		xml.output(e, System.out);		
	}	
}
