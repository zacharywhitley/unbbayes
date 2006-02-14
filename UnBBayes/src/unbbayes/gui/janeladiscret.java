package unbbayes.gui;

/**
 * Janela para discretização múltipla
 * @author gabriel guimaraes - Aluno de IC 2005-2006
 * @Orientador Marcelo Ladeira
 */

import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JFrame;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ResourceBundle;
import unbbayes.datamining.gui.preprocessor.dalgo2;

import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.JButton;

import unbbayes.aprendizagem.ConstructionController;
import unbbayes.controller.FileController;
import unbbayes.util.NodeList;
import javax.swing.JCheckBox;
import java.awt.GridLayout;
import java.awt.Dimension;
import javax.swing.JProgressBar;


public class janeladiscret extends JFrame {

	private JPanel jContentPane = null;
	public static final long serialVersionUID=1;
	private FileController fileController;
	public int[][] matriz;
	public dalgo2 discretizador;
	public NodeList variaveis;
	public int[] vetor;
	int linhas;

	private static ResourceBundle resource =
		ResourceBundle.getBundle("unbbayes.gui.resources.GuiResources");
	private JPanel jPanel3 = null;
	private JButton jButton2 = null;
	private JButton jButton = null;
	private JButton jButton5 = null;
	private JPanel jPanel = null;
	private JLabel jLabel = null;
	private JComboBox listavar = null;
	private JLabel jLabel1 = null;
	private JComboBox discretlist = null;
	private JPanel jPanel1 = null;
	private JCheckBox peso = null;
	private JCheckBox qui2 = null;
	private JPanel jPanel2 = null;
	private JLabel jLabel2 = null;
	private JButton jButton3 = null;
	private JProgressBar jProgressBar = null;
	private JButton jButton4 = null;
	private JPanel jPanel4 = null;
	private JLabel jLabel3 = null;
	/**
	 * This is the default constructor
	 */
	public janeladiscret() {
		super();
		initialize();
		fileController = FileController.getInstance();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private janeladiscret geti(){
		return this;
	}
	private void initialize() {
		this.setSize(594, 248);
		this.setContentPane(getJContentPane());
		this.setTitle("Discret");
		
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			GridLayout gridLayout = new GridLayout();
			gridLayout.setRows(5);
			gridLayout.setColumns(1);
			jContentPane = new JPanel();
			jContentPane.setLayout(gridLayout);
			jContentPane.add(getJPanel(), null);
			jContentPane.add(getJPanel1(), null);
			jContentPane.add(getJPanel2(), null);
			jContentPane.add(getJPanel3(), null);
			jContentPane.add(getJPanel4(), null);
		}
		return jContentPane;
	}

	private Object makeObj(final String item)  {
	     return new Object() { public String toString() { return item; } };
	   }

	/**
	 * This method initializes jPanel3	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel3() {
		if (jPanel3 == null) {
			jPanel3 = new JPanel();
			jPanel3.setName("");
			jPanel3.setPreferredSize(new java.awt.Dimension(100,22));
			jPanel3.add(getJButton2(), null);
			jPanel3.add(getJButton(), null);
			jPanel3.add(getJButton5(), null);
		}
		return jPanel3;
	}

	/**
	 * This method initializes jButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJButton2() {
		if (jButton2 == null) {
			jButton2 = new JButton();
			jButton2.setText("Escolher arquivo");
			jButton2.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					String[] nets = new String[] { "txt" };
					JFileChooser chooser = new JFileChooser(fileController.getCurrentDirectory());
					chooser.setMultiSelectionEnabled(false);
					chooser.addChoosableFileFilter(
						new SimpleFileFilter(
							nets,
							resource.getString("textFileFilter")));
					int option = chooser.showOpenDialog(janeladiscret.this);
					File file;
					if (option == JFileChooser.APPROVE_OPTION) {
						file = chooser.getSelectedFile();
	                                        fileController.setCurrentDirectory(chooser.getCurrentDirectory());
						//new ConstructionController(file, controller);
	                    ConstructionController construtor = new ConstructionController(file);
	                    int ln=construtor.getMatrix().length;
	                    int cl=construtor.getMatrix()[1].length;
	                    matriz = new int[ln][cl];
	                    matriz=construtor.getMatrix();
	                    variaveis=construtor.variablesVector;
	                    jButton.setEnabled(true);
	                    //jButton5.setEnabled(true);
	                    listavar.setEnabled(true);
	                    discretlist.setEnabled(true);
	                    int i;
						int j=variaveis.size()-1;
						for(i=0;i<j+1;i++){
						listavar.addItem(makeObj(variaveis.get(i).getDescription()));	
							
						}
						discretlist.addItem(makeObj("Multipla"));
						//peso.setSelected(true);
						//qui2.setSelected(true);
						
					}//mouse ev
				}//jbutt
			}
			
			);//get jbutt;
		
			//return jButton2;
	}
		return jButton2;
}


	/**
	 * This method initializes jButton1	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJButton() {
		if (jButton == null) {
			jButton = new JButton();
			jButton.setEnabled(false);
			jButton.setText("Discretizar");
			jButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					discretizador=new dalgo2();
					discretizador.Setmatrix(matriz);
					discretizador.Setvariables(variaveis);
					discretizador.pesogeral=qui2.isSelected();;
					discretizador.dowh=peso.isSelected();
					discretizador.SetController(geti());
					discretizador.setLimitePerda(new Float(jProgressBar.getValue())/(new Float(100)));
					discretizador.setPriority(Thread.MIN_PRIORITY);
					discretizador.start();
					
					
				}
			});
		}
		return jButton;
	}

	/**
	 * This method initializes jButton2	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJButton5() {
		if (jButton5 == null) {
			jButton5 = new JButton();
			jButton5.setEnabled(false);
			jButton5.setText("Salvar arquivo novo..");
			jButton5.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					JFileChooser chooser = new JFileChooser(fileController.getCurrentDirectory());
					chooser.setMultiSelectionEnabled(false);
					//int tt=discretizador.variables.size();
					//matriz=discretizador.originalmatrix;
					int option = chooser.showSaveDialog(janeladiscret.this);
					if (option == JFileChooser.APPROVE_OPTION) {
					try{
						BufferedWriter arq = new BufferedWriter(new FileWriter(chooser.getSelectedFile().getPath()));
						String linha="";
						
						int nv=discretizador.variables.size();
						int i,j;
						for(i=0;i<nv-1;i++){
						linha=linha+discretizador.variables.get(i).getName()+" ";}
						linha=linha+discretizador.variables.get(nv-1).getName();
						arq.write(linha);
						arq.newLine();
						
						for (i=0;i<linhas;i++){
							linha="";
							for(j=0;j<nv-1;j++){
								linha=linha+discretizador.variables.get(j).getStateAt((discretizador.originalmatrix[i][j]))+" ";
							}
							linha=linha+discretizador.variables.get(j).getStateAt((discretizador.originalmatrix[i][nv-1]));
							arq.write(linha);
							arq.newLine();
							}
						arq.close();
						}
					catch(Exception ee){
						System.out.println("erro "+ee.getMessage());
					}
					}
					
				}
			});
		}
		return jButton5;
	}

	public void setdalgoresp(NodeList vv, int mline, int[][] mt){
		this.variaveis=vv;
		this.linhas=mline;
		this.matriz=mt;
		this.jButton5.setEnabled(true);
	}
	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel() {
		if (jPanel == null) {
			jLabel1 = new JLabel();
			jLabel1.setText("Tipo de discretização");
			jLabel = new JLabel();
			jLabel.setText("Variável dependente");
			jPanel = new JPanel();
			jPanel.setComponentOrientation(java.awt.ComponentOrientation.LEFT_TO_RIGHT);
			jPanel.setPreferredSize(new java.awt.Dimension(121,35));
			jPanel.add(jLabel, null);
			jPanel.add(getListavar(), null);
			jPanel.add(jLabel1, null);
			jPanel.add(getDiscretlist(), null);
		}
		return jPanel;
	}

	/**
	 * This method initializes jComboBox	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getListavar() {
		if (listavar == null) {
			listavar = new JComboBox();
			listavar.setEnabled(false);
			listavar.setToolTipText("");
			listavar.setPreferredSize(new Dimension(100, 25));
		}
		return listavar;
	}

	/**
	 * This method initializes jComboBox1	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getDiscretlist() {
		if (discretlist == null) {
			discretlist = new JComboBox();
			discretlist.setEnabled(false);
			discretlist.setToolTipText("Escolha o tipo de discretização a ser aplicada");
			discretlist.setPreferredSize(new Dimension(100, 25));
		}
		return discretlist;
	}

	/**
	 * This method initializes jPanel1	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel1() {
		if (jPanel1 == null) {
			jPanel1 = new JPanel();
			jPanel1.add(getPeso(), null);
			jPanel1.add(getQui2(), null);
		}
		return jPanel1;
	}

	/**
	 * This method initializes jCheckBox	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getPeso() {
		if (peso == null) {
			peso = new JCheckBox();
			peso.setText("Considerar peso");
		}
		return peso;
	}

	/**
	 * This method initializes jCheckBox1	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getQui2() {
		if (qui2 == null) {
			qui2 = new JCheckBox();
			qui2.setText("Peso Geral");
		}
		return qui2;
	}

	/**
	 * This method initializes jPanel2	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel2() {
		if (jPanel2 == null) {
			jLabel2 = new JLabel();
			jLabel2.setText("Aceitar perda de até");
			jPanel2 = new JPanel();
			jPanel2.add(jLabel2, null);
			jPanel2.add(getJButton3(), null);
			jPanel2.add(getJProgressBar(), null);
			jPanel2.add(getJButton4(), null);
		}
		return jPanel2;
	}

	/**
	 * This method initializes jButton3	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJButton3() {
		if (jButton3 == null) {
			jButton3 = new JButton();
			jButton3.setText("<");
			jButton3.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					jProgressBar.setValue(jProgressBar.getValue()-1);
				}
			});
		}
		return jButton3;
	}

	/**
	 * This method initializes jProgressBar	
	 * 	
	 * @return javax.swing.JProgressBar	
	 */
	private JProgressBar getJProgressBar() {
		if (jProgressBar == null) {
			jProgressBar = new JProgressBar();
			jProgressBar.setStringPainted(true);
			jProgressBar.setValue(5);
		}
		return jProgressBar;
	}

	/**
	 * This method initializes jButton4	
	 * 	
	 * @return javax.swing.JButton	
	 */
	public synchronized void mensagem(String msg){
		 		jLabel3.setText(msg);
		notify();
	}
	private JButton getJButton4() {
		if (jButton4 == null) {
			jButton4 = new JButton();
			jButton4.setText(">");
			jButton4.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					jProgressBar.setValue(jProgressBar.getValue()+1);
				}
			});
		}
		return jButton4;
	}

	/**
	 * This method initializes jPanel4	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel4() {
		if (jPanel4 == null) {
			jLabel3 = new JLabel();
			jLabel3.setText("parado");
			jPanel4 = new JPanel();
			jPanel4.add(jLabel3, null);
		}
		return jPanel4;
	}

	}  //  @jve:decl-index=0:visual-constraint="24,0"
