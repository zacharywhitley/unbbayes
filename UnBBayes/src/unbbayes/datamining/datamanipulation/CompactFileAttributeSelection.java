package unbbayes.datamining.datamanipulation;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class CompactFileAttributeSelection extends JDialog
{
	private JButton ok,cancelar;
	private JComboBox variavel;
    private JPanel painelCentro;
	private JPanel centerPanel;
	private Container container;
	private Component parent;
	private Loader loader;

	CompactFileAttributeSelection(Loader loader,Component parent)
	{	super(new Frame(), "Atributo com o número de repetições", true);
        this.parent = parent;
		this.loader = loader;
	  	container = getContentPane();
		container.setLayout(new BorderLayout());
        ok = new JButton("OK");
	  	cancelar = new JButton("Cancelar");
	  	variavel = new JComboBox();
	  	painelCentro = new JPanel();
		centerPanel = new JPanel();
	  	variavel.setMaximumRowCount(5);
        Attribute aux;
	  	int numAttributes = loader.getInstances().numAttributes();
		for (int i = 0 ; i < numAttributes ;i++ )
	  	{	aux = (Attribute)loader.getInstances().getAttribute(i);
	    	variavel.addItem(aux.getAttributeName());
	  	}
	  	painelCentro.setLayout(new GridLayout(1,3,5,5));
	  	ok.addActionListener(ActionOk);
	  	cancelar.addActionListener(ActionCancelar);
	  	//painelCentro.add(variavel);
		centerPanel.add(variavel);
	  	painelCentro.add(ok);
	  	painelCentro.add(cancelar);
	  	container.add(painelCentro,BorderLayout.SOUTH);
		container.add(centerPanel,BorderLayout.CENTER);
	  	//container.add(new JLabel("Informe a variavel que contem o numero de repeticoes"),BorderLayout.NORTH);
        //setBounds(100,100,320,65);
	  	setSize(260,100);
		
		//Center the window
    	Dimension screenSize = parent.getSize();
    	Dimension frameSize = getSize();
    	if (frameSize.height > screenSize.height)
    	{	frameSize.height = screenSize.height;
    	}
    	if (frameSize.width > screenSize.width)
    	{	frameSize.width = screenSize.width;
    	}
    	Point loc = parent.getLocation();
      	setLocation((screenSize.width - frameSize.width) / 2 + loc.x, (screenSize.height - frameSize.height) / 2 + loc.y);
    	
		setVisible(true);
	}

	ActionListener ActionOk = new ActionListener()
	{	public void actionPerformed(ActionEvent ae)
		{   int selectedAttribute = variavel.getSelectedIndex();
			loader.setCounterAttribute(selectedAttribute);
			Options.getInstance().setCompactedFile(true);
			dispose();
		}
	};

	ActionListener ActionCancelar = new ActionListener()
	{	public void actionPerformed(ActionEvent ae)
		{	dispose();
		}
	};
}

