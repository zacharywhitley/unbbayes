package unbbayes.datamining.gui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.*;

import unbbayes.controlador.*;
import unbbayes.datamining.datamanipulation.*;
import unbbayes.datamining.gui.decisiontree.*;
import unbbayes.datamining.gui.evaluation.*;
import unbbayes.datamining.gui.explanation.*;
import unbbayes.datamining.gui.metaphor.*;
import unbbayes.datamining.gui.naivebayes.*;
import unbbayes.datamining.gui.preprocessor.*;
import unbbayes.datamining.gui.neuralmodel.*;
import unbbayes.fronteira.*;

public class InvokerMain extends JFrame
{
  private JPanel contentPane;
  private BorderLayout borderLayout1 = new BorderLayout();
  private MDIDesktopPane desktop = new MDIDesktopPane();
  private JMenuBar jMenuBar1 = new JMenuBar();
  private JMenu jMenu1 = new JMenu();
  private JMenuItem jMenuItem1 = new JMenuItem();
  private JMenuItem jMenuItem2 = new JMenuItem();
  private JMenuItem jMenuItem3 = new JMenuItem();
  private JMenuItem jMenuItem4 = new JMenuItem();
  private JMenu jMenu3 = new JMenu();
  private JMenu jMenu4 = new JMenu();
  private JMenu jMenu5 = new JMenu();
  private JMenuItem jMenuItem7 = new JMenuItem();
  private JMenuItem jMenuItem8 = new JMenuItem();
  private JMenuItem jMenuItem9 = new JMenuItem();
  private JMenuItem jMenuItem10 = new JMenuItem();
  private JMenuItem jMenuItem11 = new JMenuItem();
  private JMenuItem jMenuItem12 = new JMenuItem();
  private ImageIcon metalIcon;
  private ImageIcon motifIcon;
  private ImageIcon windowsIcon;
  private ImageIcon cascadeIcon;
  private ImageIcon tileIcon;
  private ImageIcon helpIcon;
  private ImageIcon opcaoglobalIcon;
  private JMenu jMenu6 = new JMenu();
  private JMenuItem jMenuItem5 = new JMenuItem();
  private int defaultStates = 40;
  private int confidenceLimit = 100;
  private String defaultLanguage = "Portuguese";
  private String defaultLaf = "Windows";
  /** Carrega o arquivo de recursos para internacionalização da localidade padrão */
  private ResourceBundle resource;
  private JMenuItem jMenuItem6 = new JMenuItem();
  private JMenuItem jMenuItem13 = new JMenuItem();
  private JMenuItem jMenuItem14 = new JMenuItem();


  //Construct the frame
  public InvokerMain()
  {	enableEvents(AWTEvent.WINDOW_EVENT_MASK);
    try
    {
      jbInit();
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }
  //Component initialization
  private void jbInit() throws Exception
  { openDefaultOptions();
    resource = ResourceBundle.getBundle("unbbayes.datamining.gui.resources.GuiResource");
    metalIcon = new ImageIcon(getClass().getResource("/icones/metal.gif"));
    motifIcon = new ImageIcon(getClass().getResource("/icones/motif.gif"));
    windowsIcon = new ImageIcon(getClass().getResource("/icones/windows.gif"));
    cascadeIcon = new ImageIcon(getClass().getResource("/icones/cascade.gif"));
    tileIcon = new ImageIcon(getClass().getResource("/icones/tile.gif"));
    helpIcon = new ImageIcon(getClass().getResource("/icones/help.gif"));
    opcaoglobalIcon = new ImageIcon(getClass().getResource("/icones/opcaoglobal.gif"));
    contentPane = (JPanel) this.getContentPane();
    contentPane.setLayout(borderLayout1);
    jMenuItem2.setMnemonic(((Character)resource.getObject("id3Mnemonic")).charValue());
    jMenuItem2.setText(resource.getString("id3Classifier"));
    jMenuItem2.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jMenuItem2_actionPerformed(e);
      }
    });
    jMenuItem3.setMnemonic(((Character)resource.getObject("naiveBayesMnemonic")).charValue());
    jMenuItem3.setText(resource.getString("naiveBayesClassifier"));
    jMenuItem3.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jMenuItem3_actionPerformed(e);
      }
    });
    jMenuItem4.setMnemonic(((Character)resource.getObject("evaluationMnemonic")).charValue());
    jMenuItem4.setText(resource.getString("evaluation"));
    jMenuItem4.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jMenuItem4_actionPerformed(e);
      }
    });
    jMenu3.setMnemonic(((Character)resource.getObject("lafMnemonic")).charValue());
    jMenu3.setText(resource.getString("lookAndFeel"));
    jMenu4.setMnemonic(((Character)resource.getObject("windowMnemonic")).charValue());
    jMenu4.setText(resource.getString("window"));
    jMenu5.setMnemonic(((Character)resource.getObject("helpMnemonic")).charValue());
    jMenu5.setText(resource.getString("help"));
    jMenuItem7.setIcon(helpIcon);
    jMenuItem7.setMnemonic(((Character)resource.getObject("helpTopicsMnemonic")).charValue());
    jMenuItem7.setText(resource.getString("helpTopics"));
    jMenuItem7.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jMenuItem7_actionPerformed(e);
      }
    });
    jMenuItem8.setIcon(metalIcon);
    jMenuItem8.setMnemonic('M');
    jMenuItem8.setText("Metal");
    jMenuItem8.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jMenuItem8_actionPerformed(e);
      }
    });
    jMenuItem9.setIcon(motifIcon);
    jMenuItem9.setMnemonic('O');
    jMenuItem9.setText("Motif");
    jMenuItem9.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jMenuItem9_actionPerformed(e);
      }
    });
    jMenuItem10.setIcon(windowsIcon);
    jMenuItem10.setMnemonic('W');
    jMenuItem10.setText("Windows");
    jMenuItem10.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jMenuItem10_actionPerformed(e);
      }
    });
    jMenuItem11.setIcon(cascadeIcon);
    jMenuItem11.setMnemonic(((Character)resource.getObject("cascadeMnemonic")).charValue());
    jMenuItem11.setText(resource.getString("cascade"));
    jMenuItem11.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jMenuItem11_actionPerformed(e);
      }
    });
    jMenuItem12.setIcon(tileIcon);
    jMenuItem12.setMnemonic(((Character)resource.getObject("tileMnemonic")).charValue());
    jMenuItem12.setText(resource.getString("tile"));
    jMenuItem12.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jMenuItem12_actionPerformed(e);
      }
    });
    jMenu1.setMnemonic(((Character)resource.getObject("selectMnemonic")).charValue());
    jMenuItem1.setMnemonic(((Character)resource.getObject("preprocessorMnemonic")).charValue());
    //jRadioButtonMenuItem4.setSelected(true);
    jMenu6.setMnemonic(((Character)resource.getObject("globalOptionsMnemonic")).charValue());
    jMenu6.setText(resource.getString("globalOptions"));
    jMenuItem5.setIcon(opcaoglobalIcon);
    jMenuItem5.setMnemonic(((Character)resource.getObject("preferencesMnemonic")).charValue());
    jMenuItem5.setText(resource.getString("preferences"));
    jMenuItem5.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jMenuItem5_actionPerformed(e);
      }
    });
    jMenuItem6.setText("Explanation");
    jMenuItem6.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jMenuItem6_actionPerformed(e);
      }
    });
    jMenuItem13.setText("Metaphor");
    jMenuItem13.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jMenuItem13_actionPerformed(e);
      }
    });
    jMenuItem14.setText("Combinatorial Neural Model");
    jMenuItem14.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        jMenuItem14_actionPerformed(e);
      }
    });
    contentPane.add(new JScrollPane(desktop), BorderLayout.CENTER);
    this.setJMenuBar(jMenuBar1);
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    this.setSize(screenSize);
    this.setTitle("Data Mining");
    jMenu1.setText(resource.getString("selectProgram"));
    jMenuItem1.setText(resource.getString("instancesPreprocessor"));
    jMenuItem1.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jMenuItem1_actionPerformed(e);
      }
    });
    jMenuBar1.add(jMenu1);
    jMenuBar1.add(jMenu3);
    jMenuBar1.add(jMenu6);
    jMenuBar1.add(jMenu4);
    jMenuBar1.add(jMenu5);
    jMenu1.add(jMenuItem1);
    jMenu1.add(jMenuItem2);
    jMenu1.add(jMenuItem3);
    jMenu1.add(jMenuItem4);
    jMenu1.add(jMenuItem6);
    jMenu1.add(jMenuItem13);
    jMenu1.add(jMenuItem14);
    jMenu5.add(jMenuItem7);
    jMenu3.add(jMenuItem8);
    jMenu3.add(jMenuItem9);
    jMenu3.add(jMenuItem10);
    jMenu4.add(jMenuItem11);
    jMenu4.add(jMenuItem12);
    jMenu4.addSeparator();
    jMenu6.add(jMenuItem5);
    Options.getInstance().setNumberStatesAllowed(defaultStates);
    Options.getInstance().setConfidenceLimit(confidenceLimit);
    if (defaultLaf.equals("Metal"))
    {   setLnF("javax.swing.plaf.metal.MetalLookAndFeel");
    }
    else if (defaultLaf.equals("Motif"))
    {   setLnF("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
    }
    else if (defaultLaf.equals("Windows"))
    {   setLnF("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
    }
  }
  //Overridden so we can exit when window is closed
  protected void processWindowEvent(WindowEvent e)
  {
    super.processWindowEvent(e);
    if (e.getID() == WindowEvent.WINDOW_CLOSING)
    {
      System.exit(0);
    }
  }

  private void openDefaultOptions()
  { try
    {   BufferedReader r = new BufferedReader(new FileReader(new File("DataMining.ini")));
        String header = r.readLine();
        if (header.equals("[data mining]"))
        {   // Número de estados permitidos
            String states = r.readLine();
            if ((states.substring(0,17)).equals("Maximum states = "))
            {   defaultStates = Integer.parseInt(states.substring(17));
            }
            // Intervalo de confiança
            String confidence = r.readLine();
            if ((confidence.substring(0,19)).equals("Confidence limit = "))
            {   confidenceLimit = Integer.parseInt(confidence.substring(19));
            }
            // Opção de língua
            String language = r.readLine();
            if ((language.substring(0,11)).equals("Language = "))
            {   language = language.substring(11);
                if (language.equals("English"))
                {   Locale.setDefault(new Locale("en",""));
                    defaultLanguage = language;
                }
                else if (language.equals("Potuguese"))
                {   Locale.setDefault(new Locale("pt",""));
                    defaultLanguage = language;
                }
            }
            // Opção de look and feel
            String laf = r.readLine();
            if ((laf.substring(0,16)).equals("Look and Feel = "))
            {   laf = laf.substring(16);
                if (laf.equals("Metal"))
                {   defaultLaf = laf;
                }
                else if (laf.equals("Motif"))
                {   defaultLaf = laf;
                }
                else if (laf.equals("Windows"))
                {   defaultLaf = laf;
                }
            }
        }
    }
    catch (Exception e)
    {}
  }

  void jMenuItem1_actionPerformed(ActionEvent e)
  {   PreprocessorMain editor = new PreprocessorMain();
      desktop.add(editor);
  }

  private void setLnF(String lnfName)
  {   try
      {   UIManager.setLookAndFeel(lnfName);
          SwingUtilities.updateComponentTreeUI(this);
      }
      catch (UnsupportedLookAndFeelException ex1)
      {   System.err.println(resource.getString("unsupportedLookAndFeelException")+lnfName);
      }
      catch (ClassNotFoundException ex2)
      {   System.err.println(resource.getString("classNotFoundException")+lnfName);
      }
      catch (InstantiationException ex3)
      {   System.err.println(resource.getString("instanciationException")+lnfName);
      }
      catch (IllegalAccessException ex4)
      {   System.err.println(resource.getString("illegalAccessException")+lnfName);
      }
  }

  //se apertar nesse botão o look and feel do metal é acionado
  void jMenuItem8_actionPerformed(ActionEvent e)
  {   setCursor(new Cursor(Cursor.WAIT_CURSOR));
      setLnF("javax.swing.plaf.metal.MetalLookAndFeel");
      setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
  }

  //se apertar nesse botão o look and feel do motif é acionado
  void jMenuItem9_actionPerformed(ActionEvent e)
  {   setCursor(new Cursor(Cursor.WAIT_CURSOR));
      setLnF("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
      setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
  }

  //se apertar nesse botão o look and feel do windows é acionado
  void jMenuItem10_actionPerformed(ActionEvent e)
  {   setCursor(new Cursor(Cursor.WAIT_CURSOR));
      setLnF("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
      setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
  }

  void jMenuItem11_actionPerformed(ActionEvent e)
  {   desktop.cascadeFrames();
  }

  void jMenuItem12_actionPerformed(ActionEvent e)
  {   desktop.tileFrames();
  }

  void jMenuItem7_actionPerformed(ActionEvent e)
  {   try
      {   FileController.getInstance().openHelp(this);
      }
      catch (Exception evt)
      {   System.out.println("Error= "+evt.getMessage()+" "+this.getClass().getName());
      }
  }

  void jMenuItem2_actionPerformed(ActionEvent e)
  {   DecisionTreeMain id3 = new DecisionTreeMain();
      desktop.add(id3);
      /*counterComponent++;
      JMenuItem jMenuItem = new JMenuItem(counterComponent+" Id3");
      final int number = desktop.getComponentCount()-1;
      final JMenuItem aux = jMenuItem;
      final DecisionTreeMain aux2 = id3;
      jMenuItem.addActionListener(new java.awt.event.ActionListener()
      {   public void actionPerformed(ActionEvent e)
          {   ((DecisionTreeMain)desktop.getComponent(number)).dispose();
              jMenu4.remove(aux);
              //System.out.println(number+1+" "+counterComponent);
              //counterComponent--;
          }
      });
      jMenu4.add(jMenuItem);*/
  }

  void jMenuItem3_actionPerformed(ActionEvent e)
  {   NaiveBayesMain naive = new NaiveBayesMain();
      desktop.add(naive);
  }

  void jRadioButtonMenuItem3_actionPerformed(ActionEvent e)
  {   Locale.setDefault(new Locale("en",""));
  }

  void jRadioButtonMenuItem4_actionPerformed(ActionEvent e)
  {   Locale.setDefault(new Locale("pt",""));
  }

  void jMenuItem4_actionPerformed(ActionEvent e)
  {   EvaluationMain evaluation = new EvaluationMain();
      desktop.add(evaluation);
  }

  void jMenuItem5_actionPerformed(ActionEvent e)
  {   GlobalOptions options = new GlobalOptions();
      options.setDefaultOptions(Options.getInstance().getNumberStatesAllowed(),Options.getInstance().getConfidenceLimit(),defaultLanguage,defaultLaf);
      desktop.add(options);
  }

  void jMenuItem6_actionPerformed(ActionEvent e)
  {   ExplanationMain metaphor = new ExplanationMain();
      JInternalFrame jif = new JInternalFrame("Explanação", true, true, true, true);
      jif.getContentPane().add(metaphor);
      desktop.add(jif);
  }

  void jMenuItem13_actionPerformed(ActionEvent e)
  {   MetaphorMain metaphor = new MetaphorMain();
      JInternalFrame jif = new JInternalFrame("Metáfora Médica", true, true, true, true);
      jif.getContentPane().add(metaphor);
      desktop.add(jif);
  }

  void jMenuItem14_actionPerformed(ActionEvent e) {
      NeuralModelMain cnm = new NeuralModelMain();
      desktop.add(cnm);
  }
}