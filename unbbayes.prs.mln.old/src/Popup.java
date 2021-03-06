import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.management.ManagementFactory;
import java.net.UnknownHostException;
import java.sql.SQLException;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import tuffy.learn.DNLearner;
import tuffy.main.NonPartInfer;
import tuffy.parse.CommandOptions;
import tuffy.util.Config;


public class Popup extends JPanel{
	
	private static final long serialVersionUID = 1L;
	private JTextField arqMLN = null;
	private JTextField arqEvd = null;
	private JTextField arqQry = null;
	private JTextField arqOut = null;
	
	private JTextArea jTextArea = null;
	
	private JComboBox jCB = null;
	
	private JButton jB = null;
	private JButton inferButton = null;
	
	GridBagConstraints c = new GridBagConstraints();
	
	public Popup(String result) {
		super();
		initialize(result);
	}

	private void initialize(String result) {
		this.setLayout(new GridBagLayout());
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(10, 10, 10, 10);
		
		textArea(result);
//		labels();
//		fields();
//		comboboxes();
//		buttons();
//		listeners();
	}

	private void labels() {
		c.gridx = 0; c.gridy = 0;
		this.add(new JLabel("ComboBox local"), c);
		
		c.gridx = 0; c.gridy = 1;
		this.add(new JLabel("Arquivo MLN"), c);
		
		c.gridx = 0; c.gridy = 2;
		this.add(new JLabel("Arquivo de Evid�ncias"), c);
		
		c.gridx = 0; c.gridy = 3;
		this.add(new JLabel("Arquivo de Query"), c);
		
		c.gridx = 0; c.gridy = 4;
		this.add(new JLabel("Arquivo de Sa�da"), c);	
		
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
	
	private void textArea(String result) {
		c.gridx = 1; c.gridy = 0;
		jTextArea = getJTextArea(jTextArea, result);
		this.add(jTextArea, c);
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
				System.out.println("\nresultado da combobox: \n" + jCB.getSelectedItem());
				
				System.out.println("*** Welcome to mine " + Config.product_name + "!");
//				CommandOptions options = NewUI.parseCommand(argsArray);
				CommandOptions options = new CommandOptions();
				Config.db_url = "jdbc:postgresql://localhost:5432/tuffydb";
				Config.db_username = "tuffer";
				Config.db_password = "strongPasswoRd";
				String pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
				String user = System.getProperty("user.name").toLowerCase().replaceAll("\\W", "_");
				String machine = null;
				try {
					machine = java.net.InetAddress.getLocalHost().getHostName().toLowerCase().replaceAll("\\W", "_");
				} catch (UnknownHostException e2) {
					e2.printStackTrace();
				}
				System.out.println(machine);
				
				String prod = Config.product_line;
				Config.db_schema += prod + "_" + machine + "_" + user + "_" + pid;
				
				if(jCB.getSelectedItem().equals("Marginal")){
					options.marginal = true;
				}
				if(jCB.getSelectedItem().equals("Dual")){
					options.dual = true;
				}
				options.fprog = arqMLN.getText();
				options.fevid = arqEvd.getText();
				options.fquery = arqQry.getText();
				options.fout = arqOut.getText();
				
				if(!options.isDLearningMode){
					System.out.println("disablePartition" + options.disablePartition);
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
	
	private JTextArea getJTextArea(JTextArea jTA, String result) {
		if (jTA == null) {
			jTA = new JTextArea();
			jTA.setText(result);
		}
		return jTA;
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