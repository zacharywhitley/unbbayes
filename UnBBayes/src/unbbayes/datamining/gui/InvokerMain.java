package unbbayes.datamining.gui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URL;
import java.util.*;
import javax.help.*;
import javax.swing.*;
import unbbayes.datamining.gui.decisiontree.DecisionTreeMain;
import unbbayes.datamining.gui.naivebayes.NaiveBayesMain;
import unbbayes.datamining.gui.evaluation.EvaluationMain;
import unbbayes.datamining.gui.preprocessor.PreprocessorMain;
import unbbayes.datamining.datamanipulation.Options;
import unbbayes.fronteira.MDIDesktopPane;

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
  private JMenu jMenu2 = new JMenu();
  private JMenu jMenu3 = new JMenu();
  private JMenu jMenu4 = new JMenu();
  private JMenu jMenu5 = new JMenu();
  private JMenuItem jMenuItem7 = new JMenuItem();
  private JMenuItem jMenuItem8 = new JMenuItem();
  private JMenuItem jMenuItem9 = new JMenuItem();
  private JMenuItem jMenuItem10 = new JMenuItem();
  private JMenuItem jMenuItem11 = new JMenuItem();
  private JMenuItem jMenuItem12 = new JMenuItem();
  private ImageIcon image1;
  private ImageIcon image2;
  private ImageIcon image3;
  private ImageIcon image4;
  private ImageIcon image5;
  private ImageIcon image6;
  private JRadioButtonMenuItem jRadioButtonMenuItem3 = new JRadioButtonMenuItem();
  private JRadioButtonMenuItem jRadioButtonMenuItem4 = new JRadioButtonMenuItem();
  private JMenu jMenu6 = new JMenu();
  private JMenuItem jMenuItem5 = new JMenuItem();
  private int defaultStates = 40;
  private int confidenceLimit = 100;
  private String defaultLanguage = "Portuguese";
  private String defaultLaf = "Windows";
  /** Carrega o arquivo de recursos para internacionalização da localidade padrão */
  private ResourceBundle resource;


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
    image1 = new ImageIcon(getClass().getResource("/icones/metal.gif"));
    image2 = new ImageIcon(getClass().getResource("/icones/motif.gif"));
    image3 = new ImageIcon(getClass().getResource("/icones/windows.gif"));
    image4 = new ImageIcon(getClass().getResource("/icones/cascade.gif"));
    image5 = new ImageIcon(getClass().getResource("/icones/tile.gif"));
    image6 = new ImageIcon(getClass().getResource("/icones/help.gif"));
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
    jMenu2.setMnemonic(((Character)resource.getObject("languageMnemonic")).charValue());
    jMenu2.setText(resource.getString("language"));
    jMenu3.setMnemonic(((Character)resource.getObject("lafMnemonic")).charValue());
    jMenu3.setText(resource.getString("lookAndFeel"));
    jMenu4.setMnemonic(((Character)resource.getObject("windowMnemonic")).charValue());
    jMenu4.setText(resource.getString("window"));
    jMenu5.setMnemonic(((Character)resource.getObject("helpMnemonic")).charValue());
    jMenu5.setText(resource.getString("help"));
    jMenuItem7.setIcon(image6);
    jMenuItem7.setMnemonic(((Character)resource.getObject("helpTopicsMnemonic")).charValue());
    jMenuItem7.setText(resource.getString("helpTopics"));
    jMenuItem7.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jMenuItem7_actionPerformed(e);
      }
    });
    jMenuItem8.setIcon(image1);
    jMenuItem8.setMnemonic('M');
    jMenuItem8.setText("Metal");
    jMenuItem8.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jMenuItem8_actionPerformed(e);
      }
    });
    jMenuItem9.setIcon(image2);
    jMenuItem9.setMnemonic('O');
    jMenuItem9.setText("Motif");
    jMenuItem9.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jMenuItem9_actionPerformed(e);
      }
    });
    jMenuItem10.setIcon(image3);
    jMenuItem10.setMnemonic('W');
    jMenuItem10.setText("Windows");
    jMenuItem10.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jMenuItem10_actionPerformed(e);
      }
    });
    jMenuItem11.setIcon(image4);
    jMenuItem11.setMnemonic(((Character)resource.getObject("cascadeMnemonic")).charValue());
    jMenuItem11.setText(resource.getString("cascade"));
    jMenuItem11.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jMenuItem11_actionPerformed(e);
      }
    });
    jMenuItem12.setIcon(image5);
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
    jRadioButtonMenuItem3.setText(resource.getString("english"));
    jRadioButtonMenuItem3.setMnemonic(((Character)resource.getObject("englishMnemonic")).charValue());
    jRadioButtonMenuItem3.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jRadioButtonMenuItem3_actionPerformed(e);
      }
    });
    jRadioButtonMenuItem4.setText(resource.getString("portuguese"));
    //jRadioButtonMenuItem4.setSelected(true);
    jRadioButtonMenuItem4.setMnemonic(((Character)resource.getObject("portugueseMnemonic")).charValue());
    jRadioButtonMenuItem4.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jRadioButtonMenuItem4_actionPerformed(e);
      }
    });
    jMenu6.setMnemonic(((Character)resource.getObject("globalOptionsMnemonic")).charValue());
    jMenu6.setText(resource.getString("globalOptions"));
    jMenuItem5.setMnemonic(((Character)resource.getObject("preferencesMnemonic")).charValue());
    jMenuItem5.setText(resource.getString("preferences"));
    jMenuItem5.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jMenuItem5_actionPerformed(e);
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
    jMenuBar1.add(jMenu2);
    jMenuBar1.add(jMenu3);
    jMenuBar1.add(jMenu6);
    jMenuBar1.add(jMenu4);
    jMenuBar1.add(jMenu5);
    jMenu1.add(jMenuItem1);
    jMenu1.add(jMenuItem2);
    jMenu1.add(jMenuItem3);
    jMenu1.add(jMenuItem4);
    jMenu2.add(jRadioButtonMenuItem3);
    jMenu2.add(jRadioButtonMenuItem4);
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
                {   jRadioButtonMenuItem4.setSelected(false);
                    jRadioButtonMenuItem3.setSelected(true);
                    Locale.setDefault(new Locale("en",""));
                    defaultLanguage = language;
                }
                else if (language.equals("Potuguese"))
                {   jRadioButtonMenuItem4.setSelected(true);
                    jRadioButtonMenuItem3.setSelected(false);
                    Locale.setDefault(new Locale("pt",""));
                    defaultLanguage = language;
                }
            }
            // Opção de look and feel
            String laf = r.readLine();
            if ((laf.substring(0,16)).equals("Look and Feel = "))
            {   laf = laf.substring(16);
                if (laf.equals("Metal"))
                {   setLnF("javax.swing.plaf.metal.MetalLookAndFeel");
                    defaultLaf = laf;
                }
                else if (laf.equals("Motif"))
                {   setLnF("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
                    defaultLaf = laf;
                }
                else if (laf.equals("Windows"))
                {   setLnF("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
                    defaultLaf = laf;
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
  {   setCursor(new Cursor(Cursor.WAIT_CURSOR));
      try
      {   URL helpSetURL = new URL("file:./help/Data_Mining.hs");
          HelpSet set = new HelpSet(null, helpSetURL);
          JHelp help = new JHelp(set);
          JFrame f = new JFrame();
          f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
          f.setContentPane(help);
          f.setSize(500,400);
          f.setVisible(true);
      }
      catch (Exception evt)
      {   evt.printStackTrace();
      }
      setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
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
      jRadioButtonMenuItem3.setSelected(true);
      jRadioButtonMenuItem4.setSelected(false);
  }

  void jRadioButtonMenuItem4_actionPerformed(ActionEvent e)
  {   Locale.setDefault(new Locale("pt",""));
      jRadioButtonMenuItem3.setSelected(false);
      jRadioButtonMenuItem4.setSelected(true);
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
}