package unbbayes.aprendizagem;


import javax.swing.*;
import unbbayes.util.NodeList;
import java.util.*;
import java.awt.*;
import java.awt.event.*;

public class TTelaEscolhaVariaveis extends JDialog{

    private JPanel painelEscolha;
    private JScrollPane scrollPane;
    private JPanel painelCentro;
    private JPanel painelBotao;
    private NodeList vetorVariaveis;
    private JButton ok;

    public TTelaEscolhaVariaveis(NodeList variaveis){
        super(new Frame(),"",true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        Container container = getContentPane();
        vetorVariaveis = variaveis;
        painelEscolha = new JPanel();
        int tamanho = variaveis.size();
        painelEscolha.setLayout(new GridLayout(variaveis.size(),1,3,3));
        for(int i = 0;  i < tamanho ; i++){
            TVariavel variavel = (TVariavel)variaveis.get(i);
            painelEscolha.add(new JCheckBox(variavel.getName(),true));
        }
        ok           = new JButton("Ok");
        painelCentro = new JPanel(new GridLayout(1,2,10,10));
        scrollPane   = new JScrollPane(painelEscolha);
        painelCentro.add(scrollPane);
        painelBotao = new JPanel();
        painelBotao.add(ok);
        painelCentro.add(painelBotao);
        ok.addActionListener(okListener);
        container.add(new JLabel("Escolha as variáveis"),BorderLayout.NORTH);
        container.add(painelCentro,BorderLayout.CENTER);
        setResizable(false);
        setBounds(100,100,250,250);
        setVisible(true);
    }

    ActionListener okListener = new ActionListener(){
        public void actionPerformed(ActionEvent ae){
            int tamanho = painelEscolha.getComponentCount();
            for(int i = 0 ; i < tamanho; i++){
                TVariavel variavel = (TVariavel)vetorVariaveis.get(i);
                variavel.setParticipa(((JCheckBox)painelEscolha.getComponent(i)).isSelected());
                //System.out.println("State = " + variavel.participa());
            }
            dispose();
        }
    };
}