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
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.PrinterException;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;

import org.shetline.io.GIFOutputStream;

import unbbayes.evaluation.controller.EvaluationController;
import unbbayes.gui.FileIcon;
import unbbayes.gui.NetworkWindow;
import unbbayes.gui.SimpleFileFilter;
import unbbayes.gui.continuous.ContinuousNormalDistributionPane;
import unbbayes.prs.Edge;
import unbbayes.prs.Graph;
import unbbayes.prs.Network;
import unbbayes.prs.Node;
import unbbayes.prs.bn.ContinuousNode;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.bn.SingleEntityNetwork;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.exception.CycleFoundException;
import unbbayes.prs.mebn.exception.MEBNConstructionException;
import unbbayes.prs.mebn.exception.MFragDoesNotExistException;
import unbbayes.prs.mebn.ssbn.AbstractSSBNGenerator;

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
    private MultiEntityBayesianNetwork multiEntityBayesianNetwork;
    
    private SENController senController;
    private MEBNController mebnController;
    
    // TODO CHANGE THIS!! NEW MODELING!!
    // True if it is to use junction tree for compiling (exact)
    // False if it is to use likelihood weighting for compiling (approximation)
    public boolean isUseJunctionTree() {
    	if (senController != null) {
    		return senController.isUseJunctionTree();
    	}
    	return true;
	}

	public void setUseJunctionTree(boolean useJunctionTree) {
		if (senController != null) {
    		senController.setUseJunctionTree(useJunctionTree);
    	}
	}
    
	/** Load resource file from this package */
    private static ResourceBundle resource = ResourceBundle.getBundle("unbbayes.controller.resources.ControllerResources");
    
    
    /***************** BEGIN CONTROLLING MULTI ENTTITY BAYESIAN NETWORK *********************/
    
    /**
     *  Constructs a controller for MultiEntityNetwork.
     *
     */
    public NetworkController(MultiEntityBayesianNetwork multiEntityBayesianNetwork, NetworkWindow screen) {
        this.multiEntityBayesianNetwork = multiEntityBayesianNetwork;
        this.screen = screen;
        this.mebnController = new MEBNController(multiEntityBayesianNetwork, screen);
    }
    
    public void insertDomainMFrag(){
    	if(mebnController!= null){
    		try{
    			mebnController.insertDomainMFrag(); 
    		} catch (Exception e){
    			e.printStackTrace(); 
    		}
    	}
    }    
    
    /**
     * Insert a new context node in the MultiEntityBayesianNetwork with 
     * the standard label and descritpion.
     *
     * @param x The x position of the new node.
     * @param y The y position of the new node.
     */
    
    public void insertResidentNode(double x, double y) throws MFragDoesNotExistException{
    	if (mebnController != null)
			mebnController.insertDomainResidentNode(x,y);
    }
        
    public void insertInputNode(double x, double y) throws MFragDoesNotExistException{
    	if (mebnController != null)
			mebnController.insertGenerativeInputNode(x,y);
    }
    
    public void insertContextNode(double x, double y) throws MFragDoesNotExistException{
    	if (mebnController != null)
				mebnController.insertContextNode(x,y);
    }
    
    public MEBNController getMebnController(){
    	return mebnController; 
    }
    
    public void setResetButtonActive(){
    	if(mebnController != null){
    		mebnController.setResetButtonActive(); 
    	}
    }
    
    /***************** END CONTROLLING MULTI ENTTITY BAYESIAN NETWORK *********************/
    
    
    
    

    /***************** BEGIN CONTROLLING SINGLE ENTTITY NETWORK *********************/
    
    /**
     *  Constructs a controller for SingleEntityNetwork.
     *
     */
    public NetworkController(SingleEntityNetwork singleEntityNetwork, NetworkWindow screen) {
        this.singleEntityNetwork = singleEntityNetwork;
        this.screen = screen;
        this.senController = new SENController(singleEntityNetwork, screen);
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
    	if (singleEntityNetwork != null) {
    		return singleEntityNetwork;
    	}
    	if (multiEntityBayesianNetwork != null) {
    		return multiEntityBayesianNetwork;
    	}
    	return null;
    }
    
    public Graph getGraph(){
    	
    	if (singleEntityNetwork != null) {
    		return singleEntityNetwork;
    	}
    	
    	if (multiEntityBayesianNetwork != null) {
    		if(!mebnController.isShowSSBNGraph()){
    			if (multiEntityBayesianNetwork.getCurrentMFrag()!= null){
    				return multiEntityBayesianNetwork.getCurrentMFrag();
    			}else{
    				return multiEntityBayesianNetwork;
    			}
    		}
    		else{
    			 return mebnController.getSpecificSituationBayesianNetwork();
    		}
    	}
    	
    	return null;   	
    }

    /**
     * Initialize the junction tree beliefs.
     */
    public void initialize() {
    	if (senController != null) senController.initialize();
    	else{
    		if(mebnController != null){
    			 mebnController.initialize(); 
    		}
    	}
    }
    
    /**
	 * Creates and shows the panel to edit the node's table.
	 * @param node The table owner.
	 */
	public void createTable(Node node) {
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
    	else{
    		if(mebnController != null){
    			mebnController.propagate();
    		}
    	}
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
    public void insertContinuousNode(double x, double y) {
    	if (senController != null) senController.insertContinuousNode(x,y);
    }

    /**
     * Insert a new probabilistic node in the SingleEntityNetwork with 
     * the standard label and description.
     *
     * @param x The x position of the new node.
     * @param y The y position of the new node.
     */
    public void insertProbabilisticNode(double x, double y) {
    	if (senController != null) senController.insertProbabilisticNode(x,y);
    }


    /**
     * Insert a new decision node in the SingleEntityNetwork with
     * the standard label and description.
     *
     * @param x The x position of the new node.
     * @param y The y position of the new node.
     */
    public void insertDecisionNode(double x, double y) {
    	if (senController != null) senController.insertDecisionNode(x, y);
    }

    /**
     * Insert a new utility node in the SingleEntityNetwork with
     * the standard label and description.
     *
     * @param x The x position of the new node.
     * @param y The y position of the new node.
     */
    public void insertUtilityNode(double x, double y) {
    	if (senController != null) senController.insertUtilityNode(x, y);
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
    public void insertEdge(Edge edge) throws MEBNConstructionException, CycleFoundException, Exception{
    	if (senController != null) senController.insertEdge(edge); 
    	else{
    		if (mebnController!= null) mebnController.insertEdge(edge); 
    	}
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
    private void deleteSelected(Object selected) {
    	if (senController != null) senController.deleteSelected(selected);
    	else if (mebnController != null) mebnController.deleteSelected(selected);
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

        if (e.getKeyCode() == KeyEvent.VK_DELETE) {
            Object selecionado = screen.getGraphPane().getSelected();
            deleteSelected(selecionado);
            for (int i = 0; i < screen.getGraphPane().getSelectedGroup().size(); i++) {
                selecionado = screen.getGraphPane().getSelectedGroup().get(i);
                deleteSelected(selecionado);
            }
        }
        screen.getGraphPane().update();
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
     * Convert a component to an image.
     * @param component The component to be converted.
     * @param r The area to consider of the given component.
     */
    private BufferedImage graphicsToImage(Component component, Rectangle r){
        BufferedImage buffImg = new BufferedImage(component.getWidth(),

        component.getHeight(), BufferedImage.TYPE_BYTE_INDEXED);
        component.setVisible(true);
        Graphics g = (Graphics)buffImg.createGraphics();

        component.paint(g);
        g.dispose();
        return(buffImg);
    }
    
    /**
     * Save the network image to a file.
     */
    public void saveNetImage() {
        String gif[] = { "GIF" };
        JFileChooser chooser = new JFileChooser(FileController.getInstance().getCurrentDirectory());
        chooser.setMultiSelectionEnabled(false);

        //adicionar FileView no FileChooser para desenhar �cones de arquivos
        chooser.setFileView(new FileIcon(screen));
        chooser.addChoosableFileFilter(new SimpleFileFilter( gif, resource.getString("imageFileFilter")));

        int opcao = chooser.showSaveDialog(screen);
        if (opcao == JFileChooser.APPROVE_OPTION) {
            try {
                GIFOutputStream out = new GIFOutputStream(new BufferedOutputStream(new FileOutputStream(chooser.getSelectedFile().getPath() + ".gif")));
                Rectangle r = calculateNetRectangle();
                out.write(graphicsToImage(screen.getGraphPane().getGraphViewport(), r));
                out.flush();
                out.close();
                FileController.getInstance().setCurrentDirectory(chooser.getCurrentDirectory());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Save the table image to a file.
     */
    public void saveTableImage() {
        String gif[] = { "GIF" };
        JFileChooser chooser = new JFileChooser(FileController.getInstance().getCurrentDirectory());
        chooser.setMultiSelectionEnabled(false);


        //adicionar FileView no FileChooser para desenhar �cones de arquivos
        chooser.setFileView(new FileIcon(screen));
        chooser.addChoosableFileFilter(new SimpleFileFilter( gif, resource.getString("imageFileFilter")));

        int opcao = chooser.showSaveDialog(screen);
        if (opcao == JFileChooser.APPROVE_OPTION) {
            try {
                GIFOutputStream out = new GIFOutputStream(new BufferedOutputStream(new FileOutputStream(chooser.getSelectedFile().getPath() + ".gif")));
                out.write(graphicsToImage(screen.getTable(), null));
                out.flush();
                out.close();
                FileController.getInstance().setCurrentDirectory(chooser.getCurrentDirectory());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     *  Show every single step taken during the compilation of the 
     *  SingleEntityNetwork.
     */
    public void showLog() {
        screen.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        final JTextArea texto = new JTextArea();

        texto.setEditable(false);
        if (singleEntityNetwork != null) {
        	texto.setText(singleEntityNetwork.getLog());
        } else {
        	texto.setText(AbstractSSBNGenerator.logManager.getLog());
        }
        
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
        dialog.setLocationRelativeTo(null); 
        dialog.pack();
        dialog.setVisible(true);
        screen.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }

    /**
     * Warning dialog (only ssbn of mebn)
     */
    public void openWarningDialog(){
    	if(multiEntityBayesianNetwork != null){
    		mebnController.openWarningDialog(); 
    	}
    }
    
    public void closeWarningDialog(){
    	if(multiEntityBayesianNetwork != null){
    		mebnController.closeWarningDialog(); 
    	}
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
    private void printLog(final JTextArea textArea) {
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
        double raio = Node.getWidth()/2;
        maiorX += raio;
        maiorY += raio;
        menorX -= raio;
        menorY -= raio;
        return new Rectangle(menorX, menorY, maiorX - menorX, maiorY - menorY);
    }
    
    public void selectNode(Node node){
    	if (multiEntityBayesianNetwork != null){
    		mebnController.selectNode(node); 
    		screen.getGraphPane().selectObject(node); 
    	}
    }
    
    public void unselectAll(){
    	if (multiEntityBayesianNetwork != null){
    		mebnController.unselectNodes(); 
    	}    	
    }
    
    /****************** END GENERIC METHODS *********************/
}