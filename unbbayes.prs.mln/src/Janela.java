import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

import tuffy.learn.DNLearner;
import tuffy.main.NonPartInfer;
import tuffy.parse.CommandOptions;
import tuffy.util.Config;


public class Janela extends JPanel{
	
	private static final long serialVersionUID = 1L;

	private JSplitPane splitPaneH = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
	private JSplitPane splitPaneV1 = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
	private JSplitPane splitPaneV2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
	private JPanel inferPanel = new JPanel();
	private JPanel loadPanel = new JPanel();
	private JPanel ParametersPanel = new JPanel();
	private JTabbedPane tabbedPane1 = new JTabbedPane();
	private JTabbedPane tabbedPane2= new JTabbedPane();
	
	private JTextField arqMLN = null;
	private JTextField arqEvd = null;
	private JTextField arqQry = null;
	private JTextField arqOut = null;
	private JTextField cwPreds = null;
	
	private String MLNFile = "";
	
	private JComboBox jCB = null;
	
	private JCheckBox jCheckCw = null;
	private JCheckBox jCheckCw2 = null;
	
	private JScrollPane jSP = null;
	private JScrollPane jSPinfer = null;
	private JScrollPane jSPMLNLoad = null;
	private JScrollPane jSPMLNTree = null;
	private JScrollPane jSPMLNTree2 = null;
	private JScrollPane jSPParameters = null;
	
	private JTextArea jTA_MLN = null;
	private JTextArea jTAInfer = null;
	private JTextArea jTA_Tree = null;
	private JTextArea jTA_Parameters = null;
	
	private JButton jB = null;
	private JButton inferButton = null;
	private JButton MLNLoadButton = null;
	private JButton EvdLoadButton = null;
	private JButton QryLoadButton = null;
	private JButton MLNSaveButton = null;
	private JButton EvdSaveButton = null;
	private JButton QrySaveButton = null;
	
	ArrayList<Parameter> parameters = new ArrayList<Parameter>();
	
	private File loadedFile = null;
	
	private String result = "";
	
	GridBagConstraints c = new GridBagConstraints();
	GridBagConstraints c2 = new GridBagConstraints();
	
//	Janela janela = new Janela();
	
	public Janela() {
		super();
		initialize();
	}

	private void initialize() {
//		this.setLayout(new GridBagLayout());
//		c.fill = GridBagConstraints.HORIZONTAL;
//		c.insets = new Insets(10, 10, 10, 10);
		
		readConfiguration();
		parametersTable();
		paineis();
		labels();
		fields();
		simpleTree();
		scrollPane();
		comboboxes();
		buttons();
		tabbedPane();
		listeners();
	}
	
	private void paineis(){
		
		this.setLayout(new BorderLayout());
		this.setPreferredSize(new Dimension(1000,800));
		loadPanel.setLayout(new GridBagLayout());
		inferPanel.setLayout(new GridBagLayout());
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(10, 10, 10, 10);
		c2.fill = GridBagConstraints.HORIZONTAL;
		c2.insets = new Insets(10, 10, 10, 10);
		
		splitPaneH.setMaximumSize(new Dimension(900,700));
		
		c.gridx = 0; c.gridy = 0;
		add(splitPaneH, BorderLayout.CENTER);
		
//		loadPanel.setMinimumSize(new Dimension(400,300));
//		tabbedPane1.setMinimumSize(new Dimension(400,300));
//		inferPanel.setMinimumSize(new Dimension(400,300));
//		tabbedPane2.setMinimumSize(new Dimension(400,300));
		
		splitPaneH.setLeftComponent(splitPaneV1);
		splitPaneH.setRightComponent(splitPaneV2);
		
		splitPaneV1.setLeftComponent(loadPanel);
		splitPaneV1.setRightComponent(tabbedPane1);
		splitPaneV2.setLeftComponent(inferPanel);
		splitPaneV2.setRightComponent(tabbedPane2);
		
//		c.gridx = 0; c.gridy = 0;
//		splitPane.add(loadPanel, );
//		c.gridx = 1; c.gridy = 0;
//		splitPane.add(inferPanel, c);
//		c.gridx = 0; c.gridy = 1;
//		splitPane.add(tabbedPane, c);
		
	}

	private void labels() {
		c.gridx = 0; c.gridy = 0;
		inferPanel.add(new JLabel("ComboBox local"), c);
		
		c.gridx = 0; c.gridy = 5;
		inferPanel.add(new JLabel("Infer Results"), c);
		
		c.gridx = 0; c.gridy = 0;
		loadPanel.add(new JLabel("MLN File"), c);
		
		c.gridx = 0; c.gridy = 1;
		loadPanel.add(new JLabel("Evidency File"), c);
		
		c.gridx = 0; c.gridy = 2;
		loadPanel.add(new JLabel("Query"), c);

		c.gridx = 0; c.gridy = 0;
		ParametersPanel.add(new JLabel("Closed World Predicates"), c);
	}
	
	private void fields() {
		c.gridwidth = 4;
		c.gridx = 1; c.gridy = 0;
		arqMLN = getJTextField(arqMLN, "samples/smoke/prog.mln");
		loadPanel.add(arqMLN, c);

		c.gridx = 1; c.gridy = 1;
		arqEvd = getJTextField(arqEvd, "samples/smoke/evidence.db");
		loadPanel.add(arqEvd, c);
		
		c.gridx = 1; c.gridy = 2;
		arqQry = getJTextField(arqQry, "samples/smoke/query.db");
		loadPanel.add(arqQry, c);
		c.gridwidth = 1;

		// Parameters
		c.gridx = 1; c.gridy = 0;
		cwPreds = getJTextField(cwPreds, "");
		ParametersPanel.add(cwPreds, c);
		c.gridwidth = 1;
	}
	
	private void readConfiguration(){
		try {
//	        BufferedReader in = new BufferedReader(new FileReader(arqMLN.getText()));
//	        System.out.println("arqMLN: " + arqMLN.getText() + "MLNFile: " + MLNFile);
	    	BufferedReader in = new BufferedReader(new FileReader("samples/smoke/parâmetros.txt"));
	    	String[] lineParam;
	    	String line = "";
	    	String comment = "//";
	    	while (in.ready()) {
	    		Parameter parameter = new Parameter();
	    		line = in.readLine();
	    		lineParam = line.split(";", 4);

	    		parameter.setAttribute(lineParam[0]);
				parameter.setDescription(lineParam[1]);
				if (lineParam[2].equals("String")) parameter.setVariableType(Parameter.VariableType.String);
				else if (lineParam[2].equals("Integer")) parameter.setVariableType(Parameter.VariableType.Integer);
				else if (lineParam[2].equals("Float")) parameter.setVariableType(Parameter.VariableType.Float);
				else if (lineParam[2].equals("Boolean")) parameter.setVariableType(Parameter.VariableType.Boolean);
				parameter.setDefaultValue(lineParam[3]);
				
				parameters.add(parameter);
				
	    	}
	    	in.close();
	    } catch (IOException ex) {
	    }
	}
	
	private void parametersTable(){
		
		
		for (Parameter param : parameters) {
			System.out.println(param.getAttribute());
			System.out.println(param.getDescription());
			if(param.getVariableType().equals(Parameter.VariableType.String)){
				System.out.println("[estrutura <->]");
			}
			if(param.getVariableType().equals(Parameter.VariableType.Integer)){
				System.out.println("[campo numérico]");
			}
			if(param.getVariableType().equals(Parameter.VariableType.Float)){
				System.out.println("[campo numérico]");
			}
			if(param.getVariableType().equals(Parameter.VariableType.Boolean)){
				System.out.println("[checkbox]");
			}
			System.out.println(param.getDefaultValue());
			System.out.println("---------------------------------------");
		}
	}		
	
	private void comboboxes() {
		c.gridwidth = 4;
		c.gridx = 1; c.gridy = 0;
		inferPanel.add(getComboBox(), c);
		c.gridwidth = 1;
	}
	
	private void checkboxes() {
		jCheckCw2 = getCheckBox(jCheckCw2, "outro");
		c.gridx = 0; c.gridy = 1;
		ParametersPanel.add(jCheckCw2, c);
	}
	
	private void scrollPane() {
		jTAInfer = getJTextArea(jTAInfer);
		c.gridx = 1; c.gridy = 5;
		c.gridheight = 7;
		c.gridwidth = 4;
		jSPinfer = getJScrollPane(jTAInfer);
		inferPanel.add(jSPinfer, c);
		c.gridheight = 1;
		c.gridwidth = 1;

		jTA_Parameters = getJTextArea(jTA_Parameters);
		jCheckCw = getCheckBox(jCheckCw, "Closed World");
		jCheckCw2 = getCheckBox(jCheckCw2, "outro");
		
		
//		c2.gridx = 0; c.gridy = 0;
//		ParametersPanel.add(jCheckCw, c);
//		c.gridx = 0; c.gridy = 2;
//		ParametersPanel.add(jCheckCw2, c);
//		inferPanel.add(ParametersPanel, c);

		c.gridx = 1; c.gridy = 5;
		c.gridheight = 7;
		c.gridwidth = 4;
		//jSPParameters = getJScrollPane(jTA_Parameters);
		inferPanel.add(ParametersPanel, c);
		c.gridheight = 1;
		c.gridwidth = 1;
		
		jTA_MLN = getJTextArea(jTA_MLN);
		c.gridx = 0; c.gridy = 2;
		c.gridheight = 7;
		c.gridwidth = 5;
		jSPMLNLoad = getJScrollPane(jTA_MLN);
		loadPanel.add(jSPMLNLoad, c);
		c.gridheight = 1;
		
		jTA_Tree = getJTextArea(jTA_Tree);
		c.gridx = 0; c.gridy = 9;
		c.gridheight = 7;
		c.gridwidth = 5;
		jSPMLNTree = getJScrollPane(jTA_Tree);
		loadPanel.add(jSPMLNTree, c);
		c.gridheight = 1;
		c.gridwidth = 1;
	}
	
	private void parameters(){
		
	}
	
	private void buttons() {
		c.gridx = 1; c.gridy = 4;
		inferButton = getJButton("Infer");
		inferButton.setEnabled(false);
		inferPanel.add(inferButton, c);
		
		c.gridx = 5; c.gridy = 0;
		MLNLoadButton = getJButton("Load");
		loadPanel.add(MLNLoadButton, c);

		c.gridx = 5; c.gridy = 1;
		EvdLoadButton = getJButton("Load");
		loadPanel.add(EvdLoadButton, c);
		
		c.gridx = 5; c.gridy = 2;
		QryLoadButton = getJButton("Load");
		loadPanel.add(QryLoadButton, c);
		
		c.gridx = 6; c.gridy = 0;
		MLNSaveButton = getJButton("Save");
		loadPanel.add(MLNSaveButton, c);
		
		c.gridx = 6; c.gridy = 1;
		EvdSaveButton = getJButton("Save");
		loadPanel.add(EvdSaveButton, c);
		
		c.gridx = 6; c.gridy = 2;
		QrySaveButton = getJButton("Save");
		loadPanel.add(QrySaveButton, c);
		
	}
	
	private void tabbedPane(){
		
		tabbedPane1.addTab("tree", null, jSPMLNTree,	"mln");
		tabbedPane1.setMnemonicAt(0, KeyEvent.VK_2);
		
		tabbedPane1.addTab("tree2", null, jSPMLNTree2, "mln2");
		tabbedPane1.setMnemonicAt(0, KeyEvent.VK_3);
		
		tabbedPane2.addTab("inference", null, jSPinfer, "inference");
		tabbedPane2.setMnemonicAt(0, KeyEvent.VK_1);

		tabbedPane2.addTab("mln", null, jSPMLNLoad, "mln");
		tabbedPane2.setMnemonicAt(0, KeyEvent.VK_4);
		
		tabbedPane2.addTab("Parameters", null, ParametersPanel, "parameters");
		tabbedPane2.setMnemonicAt(0, KeyEvent.VK_5);
		
		c.gridx = 0; c.gridy = 5;
		c.gridheight = 7;
		c.gridwidth = 4;
//		loadPanel.add(tabbedPane);
		c.gridheight = 1;
		c.gridwidth = 1;
	}
	
	public void simpleTree() {
		Object[] hierarchy =
	    { "javax.swing",
	      "javax.swing.border",
	      "javax.swing.colorchooser",
	      "javax.swing.event",
	      "javax.swing.filechooser",
	      new Object[] { "javax.swing.plaf",
	                     "javax.swing.plaf.basic",
	                     "javax.swing.plaf.metal",
	                     "javax.swing.plaf.multi" },
	      "javax.swing.table",
	      new Object[] { "javax.swing.text",
	                     new Object[] { "javax.swing.text.html",
	                                    "javax.swing.text.html.parser" },
	                     "javax.swing.text.rtf" },
	      "javax.swing.tree",
	      "javax.swing.undo" };
		  DefaultMutableTreeNode root = processHierarchy(hierarchy);
		  JTree tree = new JTree(root);
		  jSPMLNTree2 = new JScrollPane(tree);
	}
	
	private void listeners() {

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
				
				if(!cwPreds.getText().equals("")){
					options.cwaPreds = cwPreds.getText();
				}
				if(jCB.getSelectedItem().equals("Marginal")){
					options.marginal = true;
				}
				if(jCB.getSelectedItem().equals("Dual")){
					options.dual = true;
				}
				options.fprog = MLNFile;
				options.fevid = arqEvd.getText();
				options.fquery = arqQry.getText();
				options.fout = "out.txt";
				
				if(!options.isDLearningMode){
					System.out.println("disablePartition" + options.disablePartition);
					// INFERENCE
					if(!options.disablePartition){
						result += (String) new NewPartInfer().run(options);
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
//				janela.criarPopup(container);
				jTAInfer.setText(result);
			}
		});
		
		MLNLoadButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser();
				 // restringe a amostra a diretorios apenas
				fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			    fc.setCurrentDirectory (new File ("."));

			    int res = fc.showOpenDialog(null);
			    //if(res!=null){
            	if(res == JFileChooser.APPROVE_OPTION){
            		loadedFile = fc.getSelectedFile();
            		System.out.println(loadedFile);
            		MLNFile = loadedFile.toString();
            		System.out.println(MLNFile);
            		if(MLNFile != ""){
            			inferButton.setEnabled(true);
            		}
            	}
			    try {
//			        BufferedReader in = new BufferedReader(new FileReader(arqMLN.getText()));
			    	BufferedReader in = new BufferedReader(new FileReader(MLNFile));
			    	String str = "";
			    	while (in.ready()) {
			    		str += in.readLine() + "\n";
//			                process(str);
			    	}
			    	jTA_MLN.setText(str);
			    	in.close();
			    } catch (IOException ex) {
			    }
			    try {
//			        BufferedReader in = new BufferedReader(new FileReader(arqMLN.getText()));
//			        System.out.println("arqMLN: " + arqMLN.getText() + "MLNFile: " + MLNFile);
			    	BufferedReader in = new BufferedReader(new FileReader(MLNFile));
			    	String str = "";
			    	String line = "";
			    	String comment = "//";
			    	while (in.ready()) {
			    		line = in.readLine() + "\n";
			    		if (line.length() == 1);
			    		else if (line.substring(0, 2).equals(comment));
			    		else {
			    			str += line;
			    			System.out.println("str: " + str);
//			                process(str);
			    		}
			    		System.out.println("line: " + line);
			    	}
			    	jTA_Tree.setText(str);
			    	in.close();
			    } catch (IOException ex) {
			    }
			}
		});

		MLNSaveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
			        BufferedWriter out = new BufferedWriter(new FileWriter(MLNFile));
			        out.write(jTA_MLN.getText());
			        out.close();
			    } catch (IOException ex) {
			    }
			}
		});
	}

	
	private JTextField getJTextField(JTextField jTF, String nome) {
		if (jTF == null) {
			jTF = new JTextField(15);
			jTF.setText(nome);
		}
		return jTF;
	}
	
	private JTextArea getJTextArea(JTextArea jTA) {
		if (jTA == null) {
			jTA = new JTextArea();
		}
		return jTA;
	}
	
	public JScrollPane getJScrollPane(JTextArea jTA) {
		jSP = new JScrollPane(getJTextArea(jTA));
		jSP.setPreferredSize(new Dimension(250,180));
		
		return jSP;
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

	private JCheckBox getCheckBox(JCheckBox jCheckBox, String text) {
		if (jCheckBox == null) {
			jCheckBox = new JCheckBox();
			
			jCheckBox.setText(text);
		}
		return jCheckBox;
	}
	
	private JButton getJButton(String name) {
		jB = new JButton(name);

		return jB;
	}
	
	private DefaultMutableTreeNode processHierarchy(Object[] hierarchy) {
	    DefaultMutableTreeNode node =
	      new DefaultMutableTreeNode(hierarchy[0]);
	    DefaultMutableTreeNode child;
	    for(int i=1; i<hierarchy.length; i++) {
	      Object nodeSpecifier = hierarchy[i];
	      if (nodeSpecifier instanceof Object[])  // Ie node with children
	        child = processHierarchy((Object[])nodeSpecifier);
	      else
	        child = new DefaultMutableTreeNode(nodeSpecifier); // Ie Leaf
	      node.add(child);
	    }
	    return(node);
	  }

//	private void process(String str) {
//		String substr = "";
//		if (str.substring(0, 1) == "//"){
//			
//		}
//	}
	
}
