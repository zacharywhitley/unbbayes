package unbbayes.monteCarlo.controlador;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JOptionPane;
import javax.xml.bind.JAXBException;

import unbbayes.io.BaseIO;
import unbbayes.io.LoadException;
import unbbayes.io.NetIO;
import unbbayes.io.XMLIO;
import unbbayes.monteCarlo.gui.TelaParametros;
import unbbayes.monteCarlo.simulacao.SimulacaoMonteCarlo;
import unbbayes.prs.bn.ProbabilisticNetwork;

/**
 * Classe que controla as ações relativas a geração de amostras pelo algoritimo de montecarlo
 * 
 * @author Danilo
 */
public class MonteCarloController {
	
	private TelaParametros tp;
	private BaseIO io;
	ProbabilisticNetwork redeProbabilistica;	
	
	public MonteCarloController(File file){
        String fileName = file.getName();
        if(fileName.endsWith(".net")){
            io = new NetIO();                       
        } else{
            io = new XMLIO(); 
        }
        try {
            redeProbabilistica = io.load(file);
            tp = new TelaParametros();
            adicionarListeners();
        } catch (LoadException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JAXBException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
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