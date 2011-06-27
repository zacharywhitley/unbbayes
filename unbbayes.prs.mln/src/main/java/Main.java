import java.sql.SQLException;
import java.text.ParseException;

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
	private Popup popup = null;
	
	public Main () {
		super();
		initialize();
	}
	
	
	private void initialize() {
		this.setSize(800, 400);
		this.setResizable(false);
		this.setLocationRelativeTo(null);
		this.setTitle("Tuffy-UnBBayes");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setContentPane(getJanela());
		this.setContentPane(getPopup());
	}


	public Janela getJanela() {
		if (janela == null) {
			janela = new Janela();
		}
		return janela;
	}

	public Popup getPopup() {
		if (popup == null) {
			popup = new Popup();
		}
		return popup;
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
