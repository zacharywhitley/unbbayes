package unbbayes.datamining.datamanipulation;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

public class CompactFileDialog extends JDialog
{
	private JButton sim,nao;
    private JPanel painelCentro;
	private Container container;
	private Component parent;
	private Loader loader;
	
	CompactFileDialog(Loader loader,Component parent)
	{   super(new JFrame(), "Arquivo compactado?", true);
		this.parent = parent;
		this.loader = loader;
		painelCentro = new JPanel();
		container = getContentPane();
		container.setLayout(new BorderLayout());
		sim = new JButton("Sim");
		nao = new JButton("Não");
		painelCentro.setLayout(new GridLayout(1,2,5,5));
		//painelCentro.setLayout(new BorderLayout());
		sim.addActionListener(ActionSim);
		nao.addActionListener(ActionNao);
		painelCentro.add(sim);
		painelCentro.add(nao);
		container.add(painelCentro,BorderLayout.CENTER);
		//container.add(new JLabel("Deseja utilizar arquivos compactados?"),BorderLayout.NORTH);
        //setBounds(100,100,230,65);
		setSize(200,60);
		
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

	ActionListener ActionSim = new ActionListener()
	{	public void actionPerformed(ActionEvent ae)
		{   dispose();
			CompactFileAttributeSelection caixa = new CompactFileAttributeSelection(loader,parent);
		}
	};

	ActionListener ActionNao = new ActionListener()
	{	public void actionPerformed(ActionEvent ae)
		{	dispose();
		}
	};
}