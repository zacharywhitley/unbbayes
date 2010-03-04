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
package unbbayes.controller;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.awt.print.PageFormat;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;

import unbbayes.evaluation.controller.EvaluationController;
import unbbayes.gui.FileIcon;
import unbbayes.gui.NetworkWindow;
import unbbayes.gui.SimpleFileFilter;
import unbbayes.io.BaseIO;
import unbbayes.io.extension.jpf.PluginAwareFileExtensionIODelegator;
import unbbayes.prs.Edge;
import unbbayes.prs.Graph;
import unbbayes.prs.Network;
import unbbayes.prs.Node;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.bn.SingleEntityNetwork;
import unbbayes.prs.hybridbn.ContinuousNode;
import unbbayes.util.extension.bn.inference.IInferenceAlgorithm;

/**
 * This class is responsible for delegating instructions that is going to be 
 * executed in a SingleEntityNetwork or MultiEntityBayesianNetwork. Insert node 
 * and propagate evidences, for instance.
 *
 * @author     Rommel Novaes Carvalho
 * @author     Michael S. Onishi
 * @created    27 de Junho de 2001
 * @version    1.5 2006/09/12
 */

public class NetworkController implements KeyListener {

    private NetworkWindow screen;
    private SingleEntityNetwork singleEntityNetwork;
    
    
    private SENController senController;
    
    private BaseIO baseIO;
    
    // TODO ROMMEL - CHANGE THIS!! NEW MODELING!!
    public IInferenceAlgorithm getInferenceAlgorithm() {
    	if (senController != null) {
    		return senController.getInferenceAlgorithm();
    	}
    	return null;
	}

	public void setInferenceAlgorithm(IInferenceAlgorithm inferenceAlgorithm) {
		if (senController != null) {
    		senController.setInferenceAlgorithm(inferenceAlgorithm);
    	}
	}
    
	/** Load resource file from this package */
    private static ResourceBundle resource = unbbayes.util.ResourceController.newInstance().getBundle(
    		unbbayes.controller.resources.ControllerResources.class.getName());
    
    
    
    /**
     * This is the default constructor, initializing nothing.
     * This is made protected in order to make it easier to extend.
     */
    protected NetworkController() {}
    
    

    /***************** BEGIN CONTROLLING SINGLE ENTTITY NETWORK *********************/
    
    /**
     *  Constructs a controller for SingleEntityNetwork.
     *
     */
    public NetworkController(SingleEntityNetwork singleEntityNetwork, NetworkWindow screen) {
        this.singleEntityNetwork = singleEntityNetwork;
        this.screen = screen;
        this.senController = new SENController(singleEntityNetwork, screen);
        this.setBaseIO(PluginAwareFileExtensionIODelegator.newInstance());
    }
    
    public SENController getSENController(){
    	return this.senController; 
    }
    
    /**
     *  Get the single entity network.
     *
     * @return The single entity network.
     */
    public SingleEntityNetwork getSingleEntityNetwork() {
    	//TODO VERIFICAR SE POSSO RETIRAR ESSE M�TODO!!
        return this.singleEntityNetwork;
    }
    
    /**
     * Get the network being controlled.
     * @return The network being controlled.
     */
    public Network getNetwork() {
    	return singleEntityNetwork;
    }
    
    /**
     * Obtains the network in a Graph format
     * @return
     */
    public Graph getGraph(){
    	return singleEntityNetwork;
    }

    /**
     * Initialize the junction tree beliefs.
     */
    public void initialize() {
    	if (senController != null) senController.initialize();
    }
    
    /**
	 * Creates and shows the panel to edit the node's table.
	 * @param node The table owner.
	 */
	public void createTable(Node node) {
		if (node == null) {
			return;
		}
		if (node.getType() == Node.CONTINUOUS_NODE_TYPE) {
			createContinuousDistribution((ContinuousNode)node);
		} else {
			createDiscreteTable(node);
		}
	}
    

	/**
	 * Creates and shows the panel where the user can edit the 
	 * continuous node normal distribution.
	 * @param node The continuous node to create the distribution pane for.
	 */
	public void createContinuousDistribution(ContinuousNode node) {
		if (senController != null) senController.createContinuousDistribution(node);
	}
	
	/**
	 * Creates and shows the panel where the user can edit the discrete 
	 * node table.
	 * @param node The discrete node to create the table pan for.
	 */
	public void createDiscreteTable(Node node) {
		if (senController != null) senController.createDiscreteTable(node);
	}

    /**
     * Construct a potential table of the given node.
     *
     * @param node The node to get the data for the table.
     */
    public JTable makeTable(final Node node) {
    	if (senController != null) return senController.makeTable(node);
    	return null;
    }

    /**
     *  Propagate the evidences of the SingleEntityNetwork.
     */
    public void propagate() {
    	if (senController != null) senController.propagate();
    }

    /**
     *  Compile the SingleEntityNetwork.
     *
     * @return True if it compiles with no error and false otherwise.
     */
    public boolean compileNetwork() {
    	if (senController != null) return senController.compileNetwork();
    	return false;
    }
    
    /**
     * Change the GUI to allow PN evaluation.
     */
    public void evaluateNetwork() {
    	if (singleEntityNetwork != null && singleEntityNetwork instanceof ProbabilisticNetwork) {
    		EvaluationController evaluationController = new EvaluationController((ProbabilisticNetwork)singleEntityNetwork);
    		screen.changeToPNEvaluationPane(evaluationController.getView());
    	} else {
    		JOptionPane.showMessageDialog(screen, "Evaluation can only be done in probabilistic networks.", "Evaluation Error", JOptionPane.ERROR_MESSAGE);
    	}
    }

    /**
     * Insert a new continuous node in the SingleEntityNetwork with 
     * the standard label and description.
     *
     * @param x The x position of the new node.
     * @param y The y position of the new node.
     */
    public Node insertContinuousNode(double x, double y) {
    	if (senController != null) return senController.insertContinuousNode(x,y);
    	return null;
    }

    /**
     * Insert a new probabilistic node in the SingleEntityNetwork with 
     * the standard label and description.
     *
     * @param x The x position of the new node.
     * @param y The y position of the new node.
     */
    public Node insertProbabilisticNode(double x, double y) {
    	if (senController != null) return senController.insertProbabilisticNode(x,y);
    	return null;
    }


    /**
     * Insert a new decision node in the SingleEntityNetwork with
     * the standard label and description.
     *
     * @param x The x position of the new node.
     * @param y The y position of the new node.
     */
    public Node insertDecisionNode(double x, double y) {
    	if (senController != null) return senController.insertDecisionNode(x, y);
    	return null;
    }

    /**
     * Insert a new utility node in the SingleEntityNetwork with
     * the standard label and description.
     *
     * @param x The x position of the new node.
     * @param y The y position of the new node.
     */
    public Node insertUtilityNode(double x, double y) {
    	if (senController != null) return senController.insertUtilityNode(x, y);
    	return null;
    }

    /**
     * Show the explanation properties for the given node.
     * @param node The node to show the explanation properties.
     */
    public void showExplanationProperties(ProbabilisticNode node) {
    	if (senController != null) senController.showExplanationProperties(node);
    }
    
    /***************** END CONTROLLING SINGLE ENTTITY NETWORK *********************/

    
    /***************** BEGIN CONTROLLING BOTH *********************/
    
    /**
     * Insert a new edge in the network.
     *
     * @param edge The new edge to be inserted.
     */
    public boolean insertEdge(Edge edge) throws Exception{
    	if (senController != null) return senController.insertEdge(edge); 
    	return false;
    }
    
    /**
     *  Insert a new state for the given node.
     *
     * @param node The selected node to insert the new state.
     */
    public void insertState(Node node) {
    	if (senController != null) senController.insertState(node);
    }

    /**
     *  Remove the last state from the given node.
     *
     * @param node The selected node to remove the last state.
     */
    public void removeState(Node node) {
    	if (senController != null) senController.removeState(node);
    }

    /**
     * Delete the selected object from the network.
     * @param selected The selected object to delete.
     */
    //by young
    public void deleteSelected(Object selected) {
    	if (senController != null) senController.deleteSelected(selected);
    }
    
    /***************** END CONTROLLING BOTH *********************/
    
    
    
    /****************** BEGIN KEY LISTENER METHODS *********************/
    
    /**
     * It does nothing when a key is typed.
     *
     * @param e The <code>KeyEvent</code> that is passed from the <code>KeyListener</code>
     * @see KeyEvent
     * @see KeyListener
     */
    public void keyTyped(KeyEvent e) { }


    /**
     *  Delete all selected objects of the network when the key (KeyEvent.VK_DELETE) is
     *  pressed.
     *
     * @param e The <code>KeyEvent</code> that is passed from the <code>KeyListener</code>
     * @see KeyEvent
     * @see KeyListener
     */
    public void keyPressed(KeyEvent e) {

    	//by young
      /*  if (e.getKeyCode() == KeyEvent.VK_DELETE) {
            Object selecionado = screen.getGraphPane().getSelected();
            deleteSelected(selecionado);
            
            for (int i = 0; i < screen.getGraphPane().getSelectedGroup().size(); i++) {
                selecionado = screen.getGraphPane().getSelectedGroup().get(i);
                deleteSelected(selecionado);
            }
        }
        screen.getGraphPane().update();*/
    }
    
    /**
     * It does nothing when a key is released.
     *
     * @param e The <code>KeyEvent</code> that is passed from the <code>KeyListener</code>
     * @see KeyEvent
     * @see KeyListener
     */
    public void keyReleased(KeyEvent e) {
    }
    
    /****************** END KEY LISTENER METHODS *********************/
    
    /****************** BEGIN GENERIC METHODS *********************/
    
    /**
     *  Get the network window.
     *
     * @return    The network window.
     */
    public NetworkWindow getScreen() {
        return this.screen;
    }

    /**
     * Save the network image to a file.
     */
    public void saveNetImage() {
        String images[] = { "PNG", "JPG", "GIF", "BMP" };
        JFileChooser chooser = new JFileChooser(FileHistoryController.getInstance().getCurrentDirectory());
        chooser.setMultiSelectionEnabled(false);

        chooser.setFileView(new FileIcon(screen));
        chooser.addChoosableFileFilter(new SimpleFileFilter( images, resource.getString("imageFileFilter")));

        int opcao = chooser.showSaveDialog(screen);
        if (opcao == JFileChooser.APPROVE_OPTION) {
        	Rectangle r = calculateNetRectangle();
        	Component comp = screen.getGraphPane().getGraphViewport();
        	File file = new File(chooser.getSelectedFile().getPath());
        	saveComponentAsImage(comp, r.width, r.height, file);
        	FileHistoryController.getInstance().setCurrentDirectory(chooser.getCurrentDirectory());
        }
    }
    
    protected void saveComponentAsImage(Component comp, int width, int height, File file) {
    	BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        Graphics2D g2d = bufferedImage.createGraphics();

        comp.paint(g2d);
        g2d.dispose();
        RenderedImage rendImage = bufferedImage;
        
        boolean wrongName = false;

        String fileName = file.getName();
        if (fileName.length() > 4) {
        	String fileExt = fileName.substring(fileName.length() - 3);
        	try {
    	        if (fileExt.equalsIgnoreCase("png")) {
    				ImageIO.write(rendImage, "png", file);
    	        } else if (fileExt.equalsIgnoreCase("jpg")) {
    	        	ImageIO.write(rendImage, "jpg", file);
    	        } else if (fileExt.equalsIgnoreCase("gif")) {
    	        	ImageIO.write(rendImage, "gif", file);
    	        } else if (fileExt.equalsIgnoreCase("bmp")) {
    	        	ImageIO.write(rendImage, "bmp", file);
    	        } else {
    	        	wrongName = true;
    	        }
    		} catch (IOException e1) {
    			// TODO SHOW MESSAGE TO USER
    			e1.printStackTrace();
    		}
        }  else {
        	wrongName = true;
        }
        
        if (wrongName) {
        	// TODO SHOW MESSAGE TO USER
        }
        
    }

    /**
     * Save the table image to a file.
     */
    public void saveTableImage() {
    	String images[] = { "PNG", "JPG", "GIF", "BMP" };
        JFileChooser chooser = new JFileChooser(FileHistoryController.getInstance().getCurrentDirectory());
        chooser.setMultiSelectionEnabled(false);

        chooser.setFileView(new FileIcon(screen));
        chooser.addChoosableFileFilter(new SimpleFileFilter( images, resource.getString("imageFileFilter")));

        int opcao = chooser.showSaveDialog(screen);
        if (opcao == JFileChooser.APPROVE_OPTION) {
        	// TODO MAKE IT SHOW THE HEADER ALSO
        	Component comp = screen.getTable();
        	File file = new File(chooser.getSelectedFile().getPath());
        	saveComponentAsImage(comp, comp.getWidth(), comp.getHeight(), file);
        	FileHistoryController.getInstance().setCurrentDirectory(chooser.getCurrentDirectory());
        }
    }
    
    /**
     * This method is called inside {@link #showLog()} to retrieve the 
     * content of LOG. Extend this method in order to customize the log message
     * (e.g. customize where the log content is stored, and how to retrieve it)
     * @return a non null string. If {@link #singleEntityNetwork} is null, it returns
     * an empty string.
     */
    protected String getLogContent() {
    	if (singleEntityNetwork != null) {
    		return singleEntityNetwork.getLog();
    	}
    	return "";
    }

    /**
     *  Show every single step taken during the compilation of the 
     *  SingleEntityNetwork.
     */
    public JDialog showLog() {
        this.getScreen().setCursor(new Cursor(Cursor.WAIT_CURSOR));
        final JTextArea texto = new JTextArea();

        texto.setEditable(false);
        texto.setText(this.getLogContent());
        
        texto.moveCaretPosition(0);
        texto.setSelectionEnd(0);

//            texto.setRows(linhas);
        texto.setSize(texto.getPreferredSize());
        texto.append("\n");
//            arq.close();

        final JDialog dialog = new JDialog();
        JScrollPane jspTexto = new JScrollPane(texto);
        jspTexto.setPreferredSize(new Dimension(450, 400));

        IconController iconController = IconController.getInstance();
        JPanel panel = new JPanel(new BorderLayout());
        JButton botaoImprimir = new JButton(iconController.getPrintIcon());
        botaoImprimir.setToolTipText(resource.getString("printLogToolTip"));
        JButton botaoVisualizar = new JButton(iconController.getVisualizeIcon());
        botaoVisualizar.setToolTipText(resource.getString("previewLogToolTip"));
        botaoImprimir.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    printLog(texto);
                }
            });
        botaoVisualizar.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    previewPrintLog(texto, dialog);
                }
            });

        panel.add(jspTexto, BorderLayout.CENTER);

        JPanel topPane = new JPanel(new FlowLayout(FlowLayout.CENTER));
        topPane.add(botaoImprimir);
        topPane.add(botaoVisualizar);
        panel.add(topPane, BorderLayout.NORTH);

        JPanel bottomPane = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton botaoOK = new JButton(resource.getString("closeButtonLabel"));
        botaoOK.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    dialog.dispose();
                }
            });


        bottomPane.add(botaoOK);
        panel.add(bottomPane, BorderLayout.SOUTH);

        dialog.getContentPane().add(panel);
        dialog.setTitle(resource.getString("logDialogTitle")); 
        this.getScreen().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        
        return dialog; 
    }

    /**
     * Open Warning dialog.
     * Currently, this is only a stub.
     */
    public void openWarningDialog(){
    	System.out.println("Not implemented yet");
    }
    
    /**
     * Close current warning dialog.
     * This is a stub yet.
     */
    public void closeWarningDialog(){
    	System.out.println("Not implemented yet");
    }
    
    
    /**
     * Preview the log printing.
     */
    public void previewPrintLog(final JTextArea texto, final JDialog dialog) {
        screen.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        Thread t = new Thread(new Runnable() {
          public void run() {
            PrintText it = new PrintText(texto,
                new PageFormat());
            PrintPreviewer pp = new PrintPreviewer(
                it, 0);

            JDialog dlg = new JDialog(dialog,
                resource.getString("previewLogToolTip"));
            dlg.getContentPane().add(pp);
            dlg.setSize(640, 480);
            dlg.setVisible(true);
          }
        });

        t.start();
        screen.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }


    /**
     * Preview the table printing.
     */
    public void previewPrintTable() {
        screen.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        Thread t = new Thread(new Runnable() {
          public void run() {
            List<JTable> tabelas = new ArrayList<JTable>();
            List<Object> donos = new ArrayList<Object>();
            List temp = screen.getGraphPane().getSelectedGroup();
            if (temp.size() == 0) {
               tabelas.add(screen.getTable());
               donos.add(screen.getTableOwner());
            }  else {
                for (int i = 0; i< temp.size(); i++) {
                    if (temp.get(i) instanceof Node) {
                        donos.add(((Node)temp.get(i)).toString());
                        tabelas.add(makeTable((Node)temp.get(i)));
                    }
                }
            }

            PrintTable tp = new PrintTable(tabelas, donos, new PageFormat());
            PrintPreviewer pp = new PrintPreviewer(
                tp, 0);
            JDialog dlg = new JDialog();
            dlg.getContentPane().add(pp);
            dlg.setSize(400, 300);
            dlg.setVisible(true);
          }
        });
        t.start();
        screen.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }


    /**
     * Preview the net printing.
     */
    public void previewPrintNet(final JComponent rede, final Rectangle retangulo) {
        screen.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        Thread t = new Thread(new Runnable() {
          public void run() {
            String rotulo = JOptionPane.showInputDialog(screen, resource.getString("askTitle"), resource.getString("informationText"), JOptionPane.INFORMATION_MESSAGE);
            if (rotulo == null) {
                rotulo = "";
            }
            PrintNet it = new PrintNet(rotulo, rede, retangulo, new PageFormat());
            PrintPreviewer pp = new PrintPreviewer(
                it, 0);

            JDialog dlg = new JDialog();
            dlg.getContentPane().add(pp);
            dlg.setSize(640, 480);
            dlg.setVisible(true);
          }
        });

        t.start();
        screen.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }


    /**
     * Print the given area of the given network.
     *
     * @param network A component representing the graphical 
     * representation of the network to be printed.
     * @param rectangle The area to be printed.
     */
    public void printNet(final JComponent network, final Rectangle rectangle) {
        screen.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        Thread t = new Thread(new Runnable() {
          public void run() {
            String rotulo = JOptionPane.showInputDialog(screen, resource.getString("askTitle"), resource.getString("informationText"), JOptionPane.INFORMATION_MESSAGE);
            if (rotulo == null) {
                rotulo = "";
            }
            PrintNet it = new PrintNet(rotulo, network, rectangle, new PageFormat());
            PrintMonitor pm = new PrintMonitor(it);
            try {
              pm.performPrint(true);
            } catch (PrinterException pe) {
              JOptionPane.showMessageDialog(
                  screen,
                  resource.getString("printException") +
                  pe.getMessage());
            }
          }
        });
        t.start();
        screen.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }


    /**
     * Print the table.
     */
    public void printTable() {
        screen.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        Thread t = new Thread(new Runnable() {
          public void run() {
            List<JTable> tabelas = new ArrayList<JTable>();
            List<Object> donos = new ArrayList<Object>();
            List temp = screen.getGraphPane().getSelectedGroup();
            if (temp.size() == 0) {
               tabelas.add(screen.getTable());
               donos.add(screen.getTableOwner());
            }  else {
                for (int i = 0; i< temp.size(); i++) {
                    if (temp.get(i) instanceof Node) {
                        donos.add(((Node)temp.get(i)).toString());
                        tabelas.add(makeTable((Node)temp.get(i)));
                    }
                }
            }
            PrintTable impressora = new PrintTable(tabelas, donos, new PageFormat());
            PrintMonitor pm = new PrintMonitor(impressora);
            try {
              pm.performPrint(true);
            } catch (PrinterException pe) {
              JOptionPane.showMessageDialog(
                  screen,
                  resource.getString("printException") +
                  pe.getMessage());
            }
          }
        });
        t.start();
        screen.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }


    /**
     * Print the log contained in the given <code>JTextArea</code>.
     *
     * @param textArea The text area containing the log.
     */
    protected void printLog(final JTextArea textArea) {
        Thread t = new Thread(new Runnable() {
          public void run() {
            PrintText it = new PrintText(textArea,
                new PageFormat());
            PrintMonitor pm = new PrintMonitor(it);
            try {
              pm.performPrint(true);
            } catch (PrinterException pe) {
              JOptionPane.showMessageDialog(
                  screen,
                  resource.getString("printException") +
                  pe.getMessage());
            }
          }
        });
        t.start();
    }

    /**
     * Method responsible for calculating the network border. If there are
     * selected objects, the resulting rectangle consider only these objects.
     * Otherwise, all objects from the network are considered.
     */
    public Rectangle calculateNetRectangle() {
    	
    	//by young
    	/*
    	ArrayList<Node> nos;
        List vetorAux = screen.getGraphPane().getSelectedGroup();

        if (vetorAux.size() == 0) {
            nos = new ArrayList<Node>();
            for (int i = 0; i < singleEntityNetwork.getNodeCount(); i++) {
            	nos.add(i, singleEntityNetwork.getNodeAt(i));
            }
        } else {
            nos = new ArrayList<Node>();
            for (int i = 0; i < vetorAux.size(); i++) {
                if (vetorAux.get(i) instanceof Node) {
                    nos.add((Node)vetorAux.get(i));
                }
            }
        }
        int maiorX = 0;
        int menorX = Integer.MAX_VALUE;
        int maiorY = 0;
        int menorY = Integer.MAX_VALUE;
        Node noAux;
        Point2D pontoAux;
        int xAux;
        int yAux;
        for (int i = 0; i < nos.size(); i++) {
            noAux = (Node)nos.get(i);
            pontoAux = noAux.getPosition();
            xAux = (int)pontoAux.getX();
            yAux = (int)pontoAux.getY();
            if (xAux > maiorX) {
                maiorX = xAux;
            }
            if (xAux < menorX) {
                menorX = xAux;
            }
            if (yAux > maiorY) {
                maiorY = yAux;
            }
            if (yAux < menorY) {
                menorY = yAux;
            }
        }
        //by young
         
        double nodeWidth = noAux.getWidth();
        maiorX += nodeWidth;
        maiorY += nodeWidth;
        menorX -= nodeWidth;
        menorY -= nodeWidth;
        
        return new Rectangle(menorX, menorY, maiorX - menorX, maiorY - menorY);*/
    	
    	return new Rectangle(0, 0, (int)screen.getGraphPane().getBiggestPoint().x, (int)screen.getGraphPane().getBiggestPoint().y);
        
        
    }
    
    /**
     * Returns the selected node.
     * @return the selected node.
     */
    public Node getSelectedNode(){
    	System.out.println("Node selection is currently only available for subclasses");
    	return null;
    }
    
    /**
     * Selects a node
     * @param node
     */
    public void selectNode(Node node){
    	System.out.println("Node selection is currently only available for subclasses");
    }
    
    /**
     * Unselects all graphical elements
     */
    public void unselectAll(){
    	System.out.println("Node selection is currently only available for subclasses");
    }

	/**
	 * This is the class responsible for storing the network controlled by this controller.
	 * {@link #setBaseIO(BaseIO)} must be set to a correct controller depending to what type of
	 * network this controller is dealing.
	 * @return the baseIO
	 */
	public BaseIO getBaseIO() {
		return baseIO;
	}

	/**
	 * This is the class responsible for storing the network controlled by this controller.
	 * {@link #setBaseIO(BaseIO)} must be set to a correct controller depending to what type of
	 * network this controller is dealing.
	 * @param baseIO the baseIO to set
	 */
	public void setBaseIO(BaseIO baseIO) {
		this.baseIO = baseIO;
	}

	/**
	 * @param screen the screen to set
	 */
	public void setScreen(NetworkWindow screen) {
		this.screen = screen;
	}
    
    /****************** END GENERIC METHODS *********************/
}