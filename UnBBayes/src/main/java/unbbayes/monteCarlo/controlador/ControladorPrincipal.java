package unbbayes.monteCarlo.controlador;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.xml.bind.JAXBException;

import unbbayes.gui.SimpleFileFilter;
import unbbayes.io.BaseIO;
import unbbayes.io.LoadException;
import unbbayes.io.NetIO;
import unbbayes.io.XMLIO;
import unbbayes.monteCarlo.gui.TelaParametros;
import unbbayes.monteCarlo.simulacao.SimulacaoMonteCarlo;
import unbbayes.prs.bn.ProbabilisticNetwork;

/**
 * Classe que controla as a��es relativas a gera��o de amostras pelo algoritimo de montecarlo
 * 
 * @author Danilo
 */
public class ControladorPrincipal {
	
	private TelaParametros tp;
	private BaseIO io;
	ProbabilisticNetwork redeProbabilistica;	
	
	public ControladorPrincipal(){	
		
		getNet();
		
		tp = new TelaParametros();
		adicionarListeners();
	}	
	
	private void getNet(){			
		try{			
			String[] nets = new String[] { "net", "xml" };
			JFileChooser chooser = new JFileChooser(".");
			chooser.setMultiSelectionEnabled(false);				
			chooser.addChoosableFileFilter(
					//TODO utilizar resources...
				new SimpleFileFilter(nets,"Carregar .net, .xml"));
			int option = chooser.showOpenDialog(null);
			if (option == JFileChooser.APPROVE_OPTION) {
				if (chooser.getSelectedFile() != null) {
					String fileName = chooser.getSelectedFile().getName();
					if(fileName.endsWith(".net")){
						io = new NetIO();						
					}
					else{
						io = new XMLIO(); 
					}
					redeProbabilistica = io.load(chooser.getSelectedFile());
				}
			}
		}catch(LoadException le){
			le.printStackTrace();
		}catch(IOException ie){
			ie.printStackTrace();
		} catch (JAXBException je){
        	je.printStackTrace(); 
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