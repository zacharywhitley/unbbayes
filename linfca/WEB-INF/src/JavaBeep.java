import java.applet.Applet;
import java.awt.Toolkit;

public class JavaBeep extends Applet {
	public void oneBeep() {
	    Toolkit.getDefaultToolkit().beep();
    }
    
    public static void main(String args[]) throws Exception {
    	Runtime rt = Runtime.getRuntime();
    	Process p = rt.exec("notepad.exe");    	
    	int retorno = p.waitFor();
    	System.out.println("retorno = " + retorno);
    }
}
