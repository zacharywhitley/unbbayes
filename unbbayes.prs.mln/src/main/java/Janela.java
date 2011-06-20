import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import tuffy.learn.DNLearner;
import tuffy.main.NonPartInfer;
import tuffy.main.PartInfer;
import tuffy.parse.CommandOptions;
import tuffy.util.Config;


public class Janela extends JPanel{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTextField arqMLN = null;
	private JTextField arqEvd = null;
	private JTextField arqQry = null;
	private JTextField arqOut = null;
	
	private JComboBox jCB = null;
	
	private JButton jB = null;
	private JButton inferButton = null;
	
	GridBagConstraints c = new GridBagConstraints();
	
	public Janela() {
		super();
		initialize();
	}

	private void initialize() {
		this.setLayout(new GridBagLayout());
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(10, 10, 10, 10);
		
		labels();
		fields();
		comboboxes();
		buttons();
		listeners();
	}

	private void labels() {
		c.gridx = 0; c.gridy = 0;
		this.add(new JLabel("ComboBox"), c);
		
		c.gridx = 0; c.gridy = 1;
		this.add(new JLabel("Arquivo MLN"), c);
		
		c.gridx = 0; c.gridy = 2;
		this.add(new JLabel("Arquivo de Evidências"), c);
		
		c.gridx = 0; c.gridy = 3;
		this.add(new JLabel("Arquivo de Query"), c);
		
		c.gridx = 0; c.gridy = 4;
		this.add(new JLabel("Arquivo de Saída"), c);	
		
	}
	
	private void fields() {
		c.gridx = 1; c.gridy = 1;
		arqMLN = getJTextField(arqMLN, "samples/smoke/prog.mln");
		this.add(arqMLN, c);
		
		c.gridx = 1; c.gridy = 2;
		arqEvd = getJTextField(arqEvd, "samples/smoke/evidence.db");
		this.add(arqEvd, c);
		
		c.gridx = 1; c.gridy = 3;
		arqQry = getJTextField(arqQry, "samples/smoke/query.db");
		this.add(arqQry, c);
		
		c.gridx = 1; c.gridy = 4;
		arqOut = getJTextField(arqOut, "out.txt");
		this.add(arqOut, c);
	}
	
	private void comboboxes() {
		c.gridx = 1; c.gridy = 0;
		this.add(getComboBox(), c);
	}
	
	private void buttons() {
		c.gridx = 1; c.gridy = 5;
		inferButton = getJButton("Infer");
		this.add(inferButton, c);
	}
	
	private void listeners() {
		//jB.addActionListener
		inferButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String args = "-marginal -i samples/smoke/prog.mln -e samples/smoke/evidence.db " +
						"-queryFile samples/smoke/query.db -r out.txt";
				String[] argsArray;
				argsArray = args.split(" ");
				for (final String arg : argsArray){
					System.out.println(arg);
				}
				
				System.out.println("*** Welcome to mine " + Config.product_name + "!");
//				CommandOptions options = NewUI.parseCommand(argsArray);
				CommandOptions options = new CommandOptions();
				Config.db_url = "tuffydb";
				System.out.println(arqMLN.getText());
				options.fprog = arqMLN.getText();
				options.fevid = arqEvd.getText();
				options.fquery = arqQry.getText();
				options.fout = arqOut.getText();
				if(!options.isDLearningMode){
					// INFERENCE
					if(!options.disablePartition){
						new NewPartInfer().run(options);
					}else{
						new NonPartInfer().run(options);
					}
				}else{
					//LEARNING
					DNLearner l = new DNLearner();
					try {
						l.run(options);
					} catch (SQLException e1) {
						e1.printStackTrace();
					}
				}
			}
		});
	}

	
	private JTextField getJTextField(JTextField jTF, String nome) {
		if (jTF == null) {
			jTF = new JTextField(30);
			jTF.setText(nome);
		}
		return jTF;
	}
	
	private JComboBox getComboBox() {
		if (jCB == null) {
			jCB = new JComboBox();
			
			jCB.addItem("Dual");
			jCB.addItem("Marginal");
			jCB.addItem("MAP Inference");
		}
		return jCB;
	}
	
	private JButton getJButton(String name) {
		if (jB == null) {
			jB = new JButton(name);
		}
		return jB;
	}

	
}
