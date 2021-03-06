/*
 *  UnBBayes
 *  Copyright (C) 2002, 2008 Universidade de Brasilia - http://www.unb.br
 *
 *  This file is part of UnBBayes.
 *
 *  UnBBayes is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  UnBBayes is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with UnBBayes.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package unbbayes.datamining.gui.evaluation;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import unbbayes.controller.FileController;
import unbbayes.controller.IconController;
import unbbayes.controller.JavaHelperController;
import unbbayes.datamining.classifiers.BayesianNetwork;
import unbbayes.datamining.classifiers.Classifier;
import unbbayes.datamining.classifiers.CombinatorialNeuralModel;
import unbbayes.datamining.classifiers.DistributionClassifier;
import unbbayes.datamining.classifiers.NeuralNetwork;
import unbbayes.datamining.classifiers.decisiontree.DecisionTreeLearning;
import unbbayes.datamining.datamanipulation.InstanceSet;
import unbbayes.gui.FileIcon;
import unbbayes.gui.SimpleFileFilter;
import unbbayes.io.NetIO;
import unbbayes.prs.bn.ProbabilisticNetwork;

public class EvaluationMain extends JInternalFrame
{ 
	/** Serialization runtime version number */
	private static final long serialVersionUID = 0;		
	
	/** Resource file for localization */
	private ResourceBundle resource;
	private ImageIcon abrirIcon;
	private ImageIcon helpIcon;
	private JPanel contentPane;
	private JMenuBar jMenuBar1 = new JMenuBar();
	private JMenu jMenuFile = new JMenu();
	private JMenu jMenuHelp = new JMenu();
	private JMenuItem jMenuHelpAbout = new JMenuItem();
	private BorderLayout borderLayout1 = new BorderLayout();
	private JPanel jPanel41 = new JPanel();
	private TitledBorder titledBorder5;
	private Border border5;
	private EvaluationPanel jPanel2 = new EvaluationPanel(this);
	private JLabel statusBar = new JLabel();
	private BorderLayout borderLayout2 = new BorderLayout();
	private InstanceSet inst;
	private ProbabilisticNetwork net;
	private Classifier classifier;
	private File selectedFile;
	private boolean instOK = false;
	private JMenuItem jMenuFileExit = new JMenuItem();
	private JFileChooser fileChooser;
	private JToolBar jToolBar1 = new JToolBar();
	private JMenuItem jMenuItem2 = new JMenuItem();
	private JButton helpButton = new JButton();
	private JButton openButton = new JButton();

	/**Construct the frame*/
	public EvaluationMain()
	{ super("",true,true,true,true);
		resource = unbbayes.util.ResourceController.newInstance().getBundle(
				unbbayes.datamining.gui.evaluation.resources.EvaluationResource.class.getName());
		setTitle(resource.getString("title"));
		enableEvents(AWTEvent.WINDOW_EVENT_MASK);
		try
		{
			jbInit();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	/**Component initialization
	 * @throws Exception if any error
	 * */
	private void jbInit() throws Exception
	{
		IconController iconController = IconController.getInstance();
		abrirIcon = iconController.getOpenIcon();
		helpIcon = iconController.getHelpIcon();
		contentPane = (JPanel) this.getContentPane();
		titledBorder5 = new TitledBorder(border5,resource.getString("selectProgram"));
		border5 = BorderFactory.createLineBorder(new Color(153, 153, 153),1);
		contentPane.setLayout(borderLayout1);
		this.setSize(new Dimension(640,480));
		jMenuFile.setMnemonic(((Character)resource.getObject("fileMnemonic")).charValue());
		jMenuFile.setText(resource.getString("file"));
		jMenuHelp.setMnemonic(((Character)resource.getObject("helpMnemonic")).charValue());
		jMenuHelp.setText(resource.getString("help"));
		jMenuHelpAbout.setIcon(helpIcon);
		jMenuHelpAbout.setMnemonic(((Character)resource.getObject("helpTopicsMnemonic")).charValue());
		jMenuHelpAbout.setText(resource.getString("helpTopics"));
		jMenuHelpAbout.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				jMenuHelpAbout_actionPerformed(e);
			}
		});
		jPanel41.setLayout(borderLayout2);
		jPanel41.setBorder(titledBorder5);
		titledBorder5.setTitle(resource.getString("status"));
		statusBar.setText(resource.getString("welcome"));
		jMenuFileExit.setMnemonic(((Character)resource.getObject("fileExitMnemonic")).charValue());
		jMenuFileExit.setText(resource.getString("exit"));
		jMenuFileExit.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				jMenuFileExit_actionPerformed(e);
			}
		});
		jToolBar1.setFloatable(false);
		jMenuItem2.setIcon(abrirIcon);
		jMenuItem2.setMnemonic(((Character)resource.getObject("openModelMnemonic")).charValue());
		jMenuItem2.setText(resource.getString("openModelDialog"));
		jMenuItem2.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				jMenuItem2_actionPerformed(e);
			}
		});
		helpButton.setIcon(helpIcon);
		helpButton.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				helpButton_actionPerformed(e);
			}
		});
		openButton.setToolTipText(resource.getString("openModel"));
		openButton.setIcon(abrirIcon);
		openButton.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				openButton_actionPerformed(e);
			}
		});
		jMenuFile.add(jMenuItem2);
		jMenuFile.add(jMenuFileExit);
		jMenuHelp.add(jMenuHelpAbout);
		jMenuBar1.add(jMenuFile);
		jMenuBar1.add(jMenuHelp);
		this.setJMenuBar(jMenuBar1);
		contentPane.add(jPanel41,	BorderLayout.SOUTH);
		jPanel41.add(statusBar, BorderLayout.CENTER);
		contentPane.add(jPanel2,BorderLayout.CENTER);
		contentPane.add(jToolBar1, BorderLayout.NORTH);
		jToolBar1.add(openButton, null);
		jToolBar1.add(helpButton, null);
	}
	/**File | Exit action performed
	 * @param e One ActionEvent
	 * */
	public void jMenuFileExit_actionPerformed(ActionEvent e)
	{
		dispose();
	}
	/**Help | About action performed
	 * @param e One ActionEvent
	 * */
	public void jMenuHelpAbout_actionPerformed(ActionEvent e)
	{	 try
			{	 JavaHelperController.getInstance().openHelp(this);
			}
			catch (Exception evt)
			{	 statusBar.setText(resource.getString("error2")+evt.getMessage()+" "+this.getClass().getName());
			}
	}

	public void setStatusBar(String text)
	{	 statusBar.setText(text);
	}

	void openTest()
	{	 setCursor(new Cursor(Cursor.WAIT_CURSOR));
			String[] s1 = {"ARFF"};
			String[] s2 = {"TXT"};
			fileChooser = new JFileChooser(FileController.getInstance().getCurrentDirectory());
			fileChooser.setDialogTitle("Open Test Instance Set");
			fileChooser.setMultiSelectionEnabled(false);
			//adicionar FileView no FileChooser para desenhar �cones de arquivos
			fileChooser.setFileView(new FileIcon(this));
			fileChooser.addChoosableFileFilter(new SimpleFileFilter(s2, "TxtFiles (*.txt)"));
			fileChooser.addChoosableFileFilter(new SimpleFileFilter(s1, "ArffFiles (*.arff)"));
			int returnVal = fileChooser.showOpenDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION)
			{	 selectedFile = fileChooser.getSelectedFile();
					openFile(selectedFile);
					statusBar.setText("Test Instance Set opened successfully");
					FileController.getInstance().setCurrentDirectory(fileChooser.getCurrentDirectory());
			}
			else
			{	 statusBar.setText("Open Test Instance Set canceled");
					instOK = false;
			}
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	}

	private void openFile(File selectedFile)
	{	 try
			{
				inst = FileController.getInstance().getInstanceSet(selectedFile,this);
				if (inst!=null)
				{
					boolean bool = inst.checkNumericAttributes();
					if (bool == true)
							throw new Exception(resource.getString("numericAttributesException"));
					instOK = true;
					statusBar.setText("Test file opened sucessfully");
				}
				else
				{
					statusBar.setText("Opera��o cancelada");
				}

			}
			catch (NullPointerException npe)
			{	 statusBar.setText(resource.getString("errorDB")+npe.getMessage());
			}
			catch (FileNotFoundException fnfe)
			{	 statusBar.setText(resource.getString("fileNotFound")+fnfe.getMessage());
			}
			catch (IOException ioe)
			{	 statusBar.setText(resource.getString("errorOpen")+ioe.getMessage());
			}
			catch(Exception e)
			{	 statusBar.setText(resource.getString("error")+e.getMessage());
			}
	}

	void helpButton_actionPerformed(ActionEvent e)
	{	 jMenuHelpAbout_actionPerformed(e);
	}

	void openButton_actionPerformed(ActionEvent e)
	{	 jMenuItem2_actionPerformed(e);
	}

	void jMenuItem2_actionPerformed(ActionEvent evt) {
		classifier = null;
		openModel();
		if (instOK) {
			openTest();
		}
		if (instOK) {
			if(classifier == null) {
				try {
//					NaiveBayes classifier = new NaiveBayes(net,inst);
					BayesianNetwork bayesianNetwork = new BayesianNetwork(net,inst);
					classifier = bayesianNetwork;
				} catch (Exception e) {
					e.printStackTrace();
					statusBar.setText(e.getMessage());
					instOK = false;
				}
			}
		}
		if (instOK) {
			jPanel2.setModel(classifier,inst);
			this.setTitle("Evaluation - "+selectedFile.getName());
		}
	}

	private void openModel()
	{	 setCursor(new Cursor(Cursor.WAIT_CURSOR));
			String[] s2 = {"NET"};
			String[] s1 = {"ID3"};
			String[] s3 = {"CNM"};
			String[] s4 = {"BPN"};
			fileChooser = new JFileChooser(FileController.getInstance().getCurrentDirectory());
			fileChooser.setDialogTitle("Open model");
			fileChooser.setMultiSelectionEnabled(false);
			fileChooser.setFileView(new FileIcon(EvaluationMain.this));
			fileChooser.addChoosableFileFilter(new SimpleFileFilter(s2, "Networks (*.net)"));
			fileChooser.addChoosableFileFilter(new SimpleFileFilter(s1, "ID3 Models (*.id3)"));
			fileChooser.addChoosableFileFilter(new SimpleFileFilter(s3, "CNM Models (*.cnm)"));
			fileChooser.addChoosableFileFilter(new SimpleFileFilter(s4, "BPN Models (*.bpn)"));

			int returnVal = fileChooser.showOpenDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION)
			{	 selectedFile = fileChooser.getSelectedFile();
					setModelFromFile(selectedFile);
					instOK = true;
					statusBar.setText(resource.getString("modelOpened"));
					FileController.getInstance().setCurrentDirectory(fileChooser.getCurrentDirectory());
			}
			else
			{	 statusBar.setText("Open model canceled");
					instOK = false;
			}
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	}

	private void setModelFromFile(File f) {
		try {
			String fileName = f.getName();
			fileName.toLowerCase();
			if (fileName.regionMatches(true,fileName.length() - 4,".id3",0,4)) {
				ObjectInputStream in = new ObjectInputStream(new FileInputStream(f));
				classifier = (DecisionTreeLearning)in.readObject();
			} else if (fileName.regionMatches(true,fileName.length() - 4,".net",0,4)) {
				// the following code is wrong, so it was commented (the net file is NOT a serialized object)
//				ObjectInputStream in = new ObjectInputStream(new FileInputStream(f));
//				classifier = (DistributionClassifier)in.readObject();
				NetIO io = new NetIO();
				net = (ProbabilisticNetwork)io.load(f);
			} else if (fileName.regionMatches(true,fileName.length() - 4,".cnm",0,4)) {
				ObjectInputStream in = new ObjectInputStream(new FileInputStream(f));
				classifier = (CombinatorialNeuralModel)in.readObject();
			} else if(fileName.regionMatches(true,fileName.length() - 4,".bpn",0,4)) {
				ObjectInputStream in = new ObjectInputStream(new FileInputStream(f));
				classifier = (NeuralNetwork)in.readObject();
			} else {
				throw new IOException(resource.getString("fileExtensionNotKnown"));
			}
		} catch (NullPointerException npe) {
			statusBar.setText(resource.getString("errorDB")+npe.getMessage());
		} catch (FileNotFoundException fnfe) {
			statusBar.setText(resource.getString("fileNotFound")+fnfe.getMessage());
		} catch (IOException ioe) {
			statusBar.setText(resource.getString("errorOpen")+ioe.getMessage());
		} catch(Exception e) {
			statusBar.setText(resource.getString("error")+e.getMessage());
			e.printStackTrace();
		}
	}

}