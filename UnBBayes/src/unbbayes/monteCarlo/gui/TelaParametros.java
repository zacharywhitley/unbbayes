package unbbayes.monteCarlo.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * @author Danilo
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class TelaParametros extends JFrame {
	
	private JPanel painelPrincipal; 
	private JPanel painelNCasos;
	private JLabel lNCasos;
	private JTextField txtNCasos;
	private JButton btnOK;
	
	
	public TelaParametros(){
		super("Simulação de Monte Carlo");
		Container c = getContentPane();		
		c.add(criarTela());		
		pack();
		setVisible(true);
	}
	
	private JPanel criarTela(){
		painelPrincipal = new JPanel(new BorderLayout());
		painelPrincipal.add(getPainelNCasos());
		return painelPrincipal;		
	}
	
	private JPanel getPainelNCasos(){
		painelNCasos = new JPanel(new GridLayout(1,3,5,5));
		lNCasos = new JLabel("Número de Casos : ");
		txtNCasos = new JTextField();		
		btnOK = new JButton("OK");
		painelNCasos.add(lNCasos);
		painelNCasos.add(txtNCasos);
		painelNCasos.add(btnOK);
		return painelNCasos;
	}	
	
	public String getNumeroCasos(){
		return txtNCasos.getText();		
	}
	
	public void adicionaOKListener(ActionListener al){
		btnOK.addActionListener(al);		
	}	
}
