package linfca.test;

import org.jdom.Element;
import linfca.*;
import linfca.gerencia.usuario.*;
import linfca.gerencia.usuario.ValidarUsuarioFeature;

import junit.framework.TestCase;
import linfca.Feature;
import linfca.gerencia.usuario.ValidarUsuarioFeature;

public class ValidarUsuarioFeatureTest extends TestCase {

	public ValidarUsuarioFeatureTest(String name) {
		super(name);
	}
	
		
	public void testValido() throws Exception {
		Feature f = new ValidarUsuarioFeature();
		Element in = new Element("in");
		Element login = new Element("identificacao");
		login.setText("michael");
		in.getChildren().add(login);
		
		Element senha = new Element("senha");
		senha.setText("michael");
		in.getChildren().add(senha);
				
	    Element out = f.process(in);
	    assertNull(out.getChild("false"));
	}
}
