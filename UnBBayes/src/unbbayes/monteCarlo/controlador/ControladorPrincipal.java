package unbbayes.monteCarlo.controlador;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import unbbayes.gui.SimpleFileFilter;
import unbbayes.io.BaseIO;
import unbbayes.io.LoadException;
import unbbayes.io.NetIO;
import unbbayes.monteCarlo.gui.TelaParametros;
import unbbayes.monteCarlo.simulacao.SimulacaoMonteCarlo;
import unbbayes.prs.bn.ProbabilisticNetwork;

/**
 * Classe que controla as ações relativas a geração de amostras pelo algoritimo de montecarlo
 * 
 * @author Danilo
 */
public class ControladorPrincipal {
	
	private TelaParametros tp;
	private BaseIO io;
	ProbabilisticNetwork redeProbabilistica;	
	
	public ControladorPrincipal(){	
		io = new NetIO();
		getNet();		
		tp = new TelaParametros();	
		adicionarListeners();	
	}	
	
	private void getNet(){			
		try{			
			String[] nets = new String[] { "net" };
			JFileChooser chooser = new JFileChooser(".");
			chooser.setMultiSelectionEnabled(false);				
			chooser.addChoosableFileFilter(
				new SimpleFileFilter(nets,"Carregar .net"));
			int option = chooser.showOpenDialog(null);
			if (option == JFileChooser.APPROVE_OPTION) {
				if (chooser.getSelectedFile() != null) {
					redeProbabilistica = io.load(chooser.getSelectedFile());
				}
			}
		}catch(LoadException le){
			le.printStackTrace();
		}catch(IOException ie){
			ie.printStackTrace();
		}
	}
	
	public void adicionarListeners(){
		tp.adicionaOKListener(okListener);
	}
	
	private int validaNatural(String n){
		try{
			int numero = Integer.parseInt(n);
			if(numero >= 0){			
				return numero;
			}
			return -1;
		}catch(NumberFormatException nfe){
			return -1;			
		}		
	}
	
	ActionListener okListener = new ActionListener(){
		public void actionPerformed(ActionEvent ae){
			int n = validaNatural(tp.getNumeroCasos());
			if(n>= 0){								
				new SimulacaoMonteCarlo(redeProbabilistica,n);
				tp.dispose();
			}else{			
				JOptionPane.showMessageDialog(null,"O numero de casos deve ser um natural","ERROR",JOptionPane.ERROR_MESSAGE);
			}
		}	
	};
	
	

}