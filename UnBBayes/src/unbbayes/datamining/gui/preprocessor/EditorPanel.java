package unbbayes.datamining.gui.preprocessor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.text.BadLocationException;
import unbbayes.fronteira.*;
import javax.swing.event.*;

public class EditorPanel extends JPanel
{ private ImageIcon image1;
  private BorderLayout borderLayout1 = new BorderLayout();
  private JScrollPane jScrollPane1 = new JScrollPane();
  private JTextArea jTextArea1 = new JTextArea();
  private JPanel jPanel1 = new JPanel();
  private PreprocessorMain reference;
  private JPanel jPanel2 = new JPanel();
  private BorderLayout borderLayout2 = new BorderLayout();
  private JPanel jPanel3 = new JPanel();
  private JButton jButton1 = new JButton();
  private GridLayout gridLayout1 = new GridLayout();
  private JPanel jPanel4 = new JPanel();
  private JPanel jPanel5 = new JPanel();
  private JPanel jPanel6 = new JPanel();
  private JPanel jPanel7 = new JPanel();
  private JPanel jPanel8 = new JPanel();
  private JLabel jLabel1 = new JLabel();
  private JLabel jLabel2 = new JLabel();
  private BorderLayout borderLayout3 = new BorderLayout();
  private BorderLayout borderLayout4 = new BorderLayout();

  public EditorPanel(PreprocessorMain reference)
  { this.reference = reference;
    try
    {
      jbInit();
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }
  private void jbInit() throws Exception
  { image1 = new ImageIcon("icones/salvar.gif");
    this.setLayout(borderLayout1);
    jTextArea1.addInputMethodListener(new java.awt.event.InputMethodListener()
    {
      public void inputMethodTextChanged(InputMethodEvent e)
      {
      }
      public void caretPositionChanged(InputMethodEvent e)
      {
        jTextArea1_caretPositionChanged(e);
      }
    });
    jTextArea1.addCaretListener(new javax.swing.event.CaretListener()
    {
      public void caretUpdate(CaretEvent e)
      {
        jTextArea1_caretUpdate(e);
      }
    });
    jPanel1.setLayout(borderLayout2);
    jButton1.setIcon(image1);
    jButton1.setMnemonic('S');
    jButton1.setText("Save Information...");
    jButton1.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jButton1_actionPerformed(e);
      }
    });
    jPanel2.setLayout(gridLayout1);
    gridLayout1.setColumns(5);
    gridLayout1.setHgap(5);
    gridLayout1.setVgap(5);
    jLabel1.setBorder(BorderFactory.createLoweredBevelBorder());
    jLabel1.setText("Lin 0");
    jLabel2.setBorder(BorderFactory.createLoweredBevelBorder());
    jLabel2.setText("Col 0");
    jPanel8.setLayout(borderLayout3);
    jPanel4.setLayout(borderLayout4);
    this.add(jScrollPane1,  BorderLayout.CENTER);
    this.add(jPanel1,  BorderLayout.SOUTH);
    jPanel1.add(jPanel2,  BorderLayout.NORTH);
    jPanel2.add(jPanel4, null);
    jPanel4.add(jLabel1, BorderLayout.CENTER);
    jPanel2.add(jPanel8, null);
    jPanel8.add(jLabel2, BorderLayout.CENTER);
    jPanel2.add(jPanel7, null);
    jPanel2.add(jPanel6, null);
    jPanel2.add(jPanel5, null);
    jPanel1.add(jPanel3,  BorderLayout.CENTER);
    jPanel3.add(jButton1, null);
    jScrollPane1.getViewport().add(jTextArea1, null);
  }

  public void setText(String text)
  {   jTextArea1.setText(text);
  }

  void jButton1_actionPerformed(ActionEvent e)
  {   setCursor(new Cursor(Cursor.WAIT_CURSOR));
      JFileChooser fileChooser = new JFileChooser(reference.getCurrentDirectory());
      fileChooser.setMultiSelectionEnabled(false);
      //adicionar FileView no FileChooser para desenhar ícones de arquivos
      fileChooser.setFileView(new FileIcon(EditorPanel.this));

      if (reference.getFileExtension() == PreprocessorMain.TXT_EXTENSION)
      {   String[] s2 = {"TXT"};
          fileChooser.addChoosableFileFilter(new SimpleFileFilter(s2, "TxtFiles (*.txt)"));
      }
      else
      {   String[] s2 = {"ARFF"};
          fileChooser.addChoosableFileFilter(new SimpleFileFilter(s2, "ArffFiles (*.arff)"));
      }
      int returnVal = fileChooser.showSaveDialog(this);
      if (returnVal == JFileChooser.APPROVE_OPTION)
      {   File selectedFile = fileChooser.getSelectedFile();
          try
          {   String fileName = selectedFile.getName();
              if (reference.getFileExtension() == PreprocessorMain.TXT_EXTENSION)
              {   if (!fileName.regionMatches(true,fileName.length() - 4,".txt",0,4))
                  {   selectedFile = new File(selectedFile.getAbsolutePath()+".txt");
                  }
              }
              else
              {   if (!fileName.regionMatches(true,fileName.length() - 5,".arff",0,4))
                  {   selectedFile = new File(selectedFile.getAbsolutePath()+".arff");
                  }
              }
              OutputStream w = new BufferedOutputStream(new FileOutputStream(selectedFile));
              PrintWriter pw = new PrintWriter(w, true);
              int lineCount = jTextArea1.getLineCount();
              for(int i=0; i<lineCount; i++)
              {   int startLine = jTextArea1.getLineStartOffset(i);
                  int endLine = jTextArea1.getLineEndOffset(i);
                  pw.println(jTextArea1.getText(startLine,(endLine-startLine-1)));
              }
              pw.flush();
              w.close();
              reference.setStatusBar("File saved. "+fileName);
          }
          catch (BadLocationException ble)
          {   reference.setStatusBar("Bad location "+ble.getMessage());
          }
          catch (IOException ioe)
          {   reference.setStatusBar("Error writing file "+selectedFile.getName()+" "+ioe.getMessage());
          }
      }
      setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
  }

  void jTextArea1_caretPositionChanged(InputMethodEvent e)
  {}

  void jTextArea1_caretUpdate(CaretEvent e)
  {   try
      { int line = jTextArea1.getLineOfOffset(e.getDot());
        jLabel1.setText("Lin "+line);
        jLabel2.setText("Col "+(e.getDot()-jTextArea1.getLineStartOffset(line)));
      }
      catch (Exception ex)
      {}
  }
}