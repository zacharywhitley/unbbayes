package unbbayes.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;

import unbbayes.controller.IconController;
import unbbayes.controller.WindowController;


/**
 * <p>Title: UnBBayes</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2001</p>
 * <p>Company: UnB</p>
 * @author Rommel Novaes Carvalho, Michael Onishi
 * @version 1.0
 */

public class EditNet extends JPanel {

    private final NetWindow netWindow;

    private final WindowController controller;
    private final IconController iconController = IconController.getInstance();
    private final JSplitPane centerPanel;

    private final JPanel topPanel;
    private final JToolBar jtbEdition;

    private final JButton compile;
    private final JButton arc;
    private final JButton printNet;
    private final JButton previewNet;
    private final JButton saveNetImage;

        /** Load resource file from this package */
          private static ResourceBundle resource = ResourceBundle.getBundle("unbbayes.gui.resources.GuiResources");

    public EditNet(NetWindow _netWindow,
                            WindowController _controller) {
        super();
        this.netWindow     = _netWindow;
        this.controller    = _controller;
        this.setLayout(new BorderLayout());

        topPanel    = new JPanel(new GridLayout(0,1));
        jtbEdition  = new JToolBar();
        centerPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

        //criar bot�es que ser�o usados nos toolbars
        compile           = new JButton(iconController.getCompileIcon());
        arc               = new JButton(iconController.getArcIcon());
        printNet          = new JButton(iconController.getPrintNetIcon());
        previewNet        = new JButton(iconController.getPrintPreviewNetIcon());
        saveNetImage      = new JButton(iconController.getSaveNetIcon());

        //setar tooltip para esses bot�es
        compile.setToolTipText(resource.getString("compileToolTip"));
        arc.setToolTipText(resource.getString("arcToolTip"));
        printNet.setToolTipText(resource.getString("printNetToolTip"));
        previewNet.setToolTipText(resource.getString("previewNetToolTip"));
        saveNetImage.setToolTipText(resource.getString("saveNetImageToolTip"));

        //ao clicar no bot�o compile, chama-se o m�todo de compila��o da rede e
        //atualiza os toolbars
        compile.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (! controller.compileNetwork()) {
                    return;
                }
                netWindow.changeToNetCompilation();
            }
        });

        //ao clicar no bot�o arc setamos as vari�veis booleanas e os estados dos but�es
        arc.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                netWindow.getIGraph().setbSelect(false);
                netWindow.getIGraph().setbArc(true);
            }
        });

        // action para imprimir a rede
        printNet.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                controller.printNet(netWindow.getIGraph(), controller.calculateNetRectangle());
            }
        });

        // action para visualizar a rede.
        previewNet.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                controller.previewPrintNet(netWindow.getIGraph(), controller.calculateNetRectangle());
            }
        });


        saveNetImage.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                controller.saveNetImage();
            }
        });


        //colocar bot�es e controladores do look-and-feel no toolbar e esse no topPanel
        jtbEdition.add(printNet);
        jtbEdition.add(previewNet);
        jtbEdition.add(saveNetImage);

        jtbEdition.addSeparator();

        jtbEdition.add(arc);
        jtbEdition.add(compile);

        topPanel.add(jtbEdition);

        //setar os tamanho de cada jsp(tabela e graph) para os seus PreferredSizes
        centerPanel.resetToPreferredSizes();

        //adiciona containers para o contentPane
        this.add(topPanel, BorderLayout.NORTH);
        this.add(centerPanel, BorderLayout.CENTER);
        setVisible(true);

    }

    /**
     *  Retorna o painel do centro onde fica o graph e a table.
     *
     *@return    retorna o centerPanel (<code>JSplitPane</code>)
     *@see       JSplitPane
     */
    public JSplitPane getCenterPanel() {
      return this.centerPanel;
    }

    public JButton getArc() {
        return this.arc;
    }

    public JButton getCompile() {
        return this.compile;
    }

    public JButton getPreviewNet() {
        return this.previewNet;
    }

    public JButton getPrintNet() {
        return this.printNet;
    }

    public JButton getSaveNetImage() {
        return this.saveNetImage;
    }
}