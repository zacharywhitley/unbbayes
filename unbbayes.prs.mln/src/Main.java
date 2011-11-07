import java.awt.Container;
import java.sql.SQLException;
import java.text.ParseException;

import javax.swing.JDialog;
import javax.swing.JFrame;


/**
 * The Main.
 */
public class Main extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static Main instance;
	private Janela janela = null;
	
	public Main () {
		super();
		initialize();
	}
	
	
	private void initialize() {
		this.setSize(1200, 700);
		this.setResizable(false);
		this.setLocationRelativeTo(null);
		this.setTitle("Tuffy-UnBBayes");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setContentPane(getJanela());
	}
	
	public void criarPopup(Container container){
		this.setEnabled(false);
		JDialog dialog = new JDialog();
		dialog.setContentPane(container);
		dialog.setAlwaysOnTop(true);
	}
	
	public Janela getJanela() {
		if (janela == null) {
			janela = new Janela();
		}
		return janela;
	}


	private static Main getInstance() {
		if (instance == null) {
			instance = new Main();
		}
		return instance;
	}
	
	

	public static void main(String[] args) throws SQLException, ParseException {
		Main.getInstance().setVisible(true);		
	}	
}
