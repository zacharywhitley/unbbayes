package unbbayes.aprendizagem;

import javax.swing.*;
import java.util.*;
import java.awt.*;



public class LogAprendizagem extends JDialog{

    private JTextArea caixaLog;
    private JScrollPane painel;


    public LogAprendizagem(java.util.List variaveis) {
        super(new Frame(),"Log",true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        Container container = getContentPane();
        caixaLog = new JTextArea();
        int tamanho = variaveis.size();
        caixaLog.setText("Lista com os pais de cada variável\n\n");
        for(int i = 0 ; i < tamanho; i++){
                TVariavel variavel = (TVariavel)variaveis.get(i);
                caixaLog.append("Variável : "+variavel.getName()+"\n");
                java.util.List pais = variavel.getPais();
                for(int j = 0 ; j < pais.size(); j++){
                    TVariavel variavel2 = (TVariavel)pais.get(j);
                    int k = j+1;
                    caixaLog.append("\tPai "+k+" : "+variavel2.getName()+"\n");
                }
                if(pais.size() == 0 ){
                    caixaLog.append("\tNão possui pais\n");
                }
        }
        painel = new JScrollPane(caixaLog);
        container.add(painel);
        setResizable(true);
        setBounds(100,100,500,500);
        setVisible(true);
    }
}