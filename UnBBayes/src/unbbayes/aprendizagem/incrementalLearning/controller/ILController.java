/*
 *  UnbBayes
 *  Copyright (C) 2002 Universidade de Brasília
 *
 *  This file is part of UnbBayes.
 *
 *  UnbBayes is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  UnbBayes is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with UnbBayes; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package unbbayes.aprendizagem.incrementalLearning.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;

import unbbayes.aprendizagem.ConstructionController;
import unbbayes.aprendizagem.ProbabilisticController;
import unbbayes.aprendizagem.TVariavel;
import unbbayes.aprendizagem.incrementalLearning.io.ILIO;
import unbbayes.aprendizagem.incrementalLearning.util.ILToolkit;
import unbbayes.controller.FileController;
import unbbayes.controller.MainController;
import unbbayes.gui.SimpleFileFilter;
import unbbayes.io.BaseIO;
import unbbayes.io.NetIO;
import unbbayes.prs.Node;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.util.NodeList;

/**
 * 
 * 
 * @author Danilo Custódio da Silva
 */
public class ILController extends ILToolkit {

    private BaseIO io;

    private ProbabilisticNetwork pn;

    private ArrayList nijks;

    private ILIO ilio;

    private List  ssList = new ArrayList ();

    ConstructionController constructionController;

    public ILController(MainController controller) {
        /* Escolha do arquivo .net para a atualização da rede */
        File file = chooseFile(new String[] { "net" }, "Choose the priori net.");
        io = new NetIO();
        ilio = new ILIO();
        /* Recuperação da rede a partir de um arquivo .net */
        pn = ilio.getNet(file, io);
        /*
         * Escolher um outro arquivo, agora que contenha informções das
         * estatísticas suficientes
         */
        file = chooseFile(new String[] { "obj" }, "Choose the frontier set.");
        if (file != null) {
            ssList = ilio.getSuficStatistics(file);
        }
        file = chooseFile(new String[] { "txt" }, "Choose the training set.");
        constructionController = new ConstructionController(file,pn);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        dataBase = constructionController.getMatrix();
        vector = constructionController.getVector();
        caseNumber = constructionController.getCaseNumber();
        compacted = constructionController.isCompacted();        
        chooseBetterNet();
        /*Gives the probability of each node*/         
        ProbabilisticController probabilisticController = new ProbabilisticController
                                (getListaVariaveis(),dataBase, vector,caseNumber,controller, compacted);
        //paramRecalc();
        file = getFile();
        ilio.makeNetFile(file, io, pn);
        file = getFile();
        ilio.makeContFile(ssList, file);
    }
    
    /**
     * @return
     */
    private NodeList getListaVariaveis() {
        NodeList listaVariaveis = new NodeList();
        for(int i = 0; i< pn.getNodeCount(); i++){
            listaVariaveis.add(getTVariavel(pn.getNodeAt(i),true));
        }
        return listaVariaveis;
    }

    private void getFrontier() {
        ssList.removeAll(ssList);
        Node node;
        for (int i = 0; i < pn.getNodeCount(); i++) {
            node = pn.getNodeAt(i);
            makeNodeFrontier(node);
        }
    }

    private void makeNodeFrontier(Node node) {
        List lista = new ArrayList();
        makeNullFrontier(node, lista);
        makeAddFrontier(node, lista);
        makeRemoveFrontier(node, lista);
        ssList.add(lista);
    }

    private void makeNullFrontier(Node node, List lista) {
        Object[] frontier = new Object[3];
        frontier[0] = node.getName();
        frontier[1] = makeAddParentsStructure(node, null);
        frontier[2] = makeAddNijksStructure(node, null);        
        lista.add(frontier);
    }

    private void makeAddFrontier(Node node, List lista) {
        for (int i = 0; i < pn.getNodeCount(); i++) {
            if (!node.getDescription().equals(pn.getNodeAt(i).getDescription())
                    && !isParent(node, pn.getNodeAt(i)) && !isDescendent(node,pn.getNodeAt(i)) ){
                Object[] frontier = new Object[3];
                frontier[0] = node.getName();
                frontier[1] = makeAddParentsStructure(node, pn.getNodeAt(i));
                frontier[2] = makeAddNijksStructure(node, pn.getNodeAt(i));
                lista.add(frontier);
            }
        }
    }

    private void makeRemoveFrontier(Node node, List lista) {
        for (int i = 0; i < pn.getNodeCount(); i++) {
            if (isParent(node, pn.getNodeAt(i))) {
                Object[] frontier = new Object[3];
                frontier[0] = node.getName();
                frontier[1] = makeRemoveParentsStructure(node, pn.getNodeAt(i));
                frontier[2] = makeRemoveNijksStructure(node, pn.getNodeAt(i));
                lista.add(frontier);
            }
        }
    }

    private List makeRemoveParentsStructure(Node node, Node lastParent) {
        List parentsName = new ArrayList();
        for (int i = 0; i < node.getParents().size(); i++) {
            String parentName = ((Node) node.getParents().get(i))
                    .getDescription();
            if (!parentName.equals(lastParent.getDescription())) {
                parentsName.add(parentName);
            }
        }
        return parentsName;
    }

    private int[][] makeRemoveNijksStructure(Node node, Node lastParent) {
        TVariavel v = getTVariavel(node, true);
        NodeList parents = v.getPais();
        for (int i = 0; i < parents.size(); i++) {
            Node parent = (Node) node.getParents().get(i);
            if (parent.getDescription().equals(lastParent.getDescription())) {
                parents.remove(lastParent);
                break;
            }
        }
        int[][] nijk = getFrequencies(v, parents);
        parents.add(getTVariavel(lastParent,true));
        return nijk;
    }

    private List makeAddParentsStructure(Node node, Node lastParent) {
        List parentsName = new ArrayList();
        for (int i = 0; i < node.getParents().size(); i++) {
            String parentName = ((Node) node.getParents().get(i))
                    .getDescription();
            parentsName.add(parentName);
        }
        if (lastParent != null) {
            parentsName.add(lastParent.getDescription());
        }
        return parentsName;
    }

    private int[][] makeAddNijksStructure(Node node, Node lastParent) {
        TVariavel v = getTVariavel(node, true);
        NodeList parents = v.getPais();
        if (lastParent != null) {
            parents.add(getTVariavel(lastParent, true));
        }
        int[][] nijk = getFrequencies(v, parents);
        if (lastParent != null) {
            parents.remove(parents.size() - 1);
        }
        return nijk;
    }

    private TVariavel getTVariavel(Node node, boolean pais) {
        for (int i = 0; i < constructionController.getVariables().size(); i++) {
            TVariavel v = (TVariavel) constructionController.getVariables().get(i);
            if (v.getName().equals(node.getName())) {
                NodeList listaPais = node.getParents();
                NodeList listaPaisAtual = new NodeList();
                for (int j = 0; j < node.getParents().size() && pais; j++) {
                    listaPaisAtual.add(getTVariavel(node.getParents().get(j), false));
                }
                if(listaPaisAtual.size() > 0 ){
                    v.setParents(listaPaisAtual);
                }
                return v;
            }
        }
        return null;
    }

    /*
     * Nome do nó Pais do nó Estatisticas suficientes guardadas0
     */
    private void chooseBetterNet() {
        findBestFrontier();        
        getFrontier();
    }

    /**
     *  
     */
    private void findBestFrontier() {
        float betterValue = Float.MIN_VALUE;
        Object[] frontierObject = null;
        Object[] betterFrontier = null;
        double g = -Double.MAX_VALUE;
        Node node;                
        for (int i = 0; i < ssList.size(); i++) {
            List lista = (List)ssList.get(i);
            for(int j = 0; j < lista.size(); j++){                
                frontierObject = (Object[]) lista.get(j);
                node = getNode((String) frontierObject[0]);
                if(j == 0){
                    if(isEquivalente(frontierObject, node)){
                        System.out.println("Equivalente - > " + frontierObject[0]);
                        break;
                    }else{
                        System.out.println("Não Equivalente - > " + frontierObject[0]);
                    }
                    g = g(getTVariavel(node,true), getParents(node,
                            (ArrayList) frontierObject[1]), (int[][]) frontierObject[2]);
                }else{
                    double gAux = g(getTVariavel(node,true), getParents(node,
                            (ArrayList) frontierObject[1]), (int[][]) frontierObject[2]);
                    if (gAux > g) {
                        betterFrontier = frontierObject;
                        g = gAux;
                    }                    
                }                
            }
            makeBetterNetwork(betterFrontier);            
            betterFrontier = null;
        }
    }
    
    private boolean isEquivalente(Object[] fronteira, Node node){
        int[][] novo = getFrequencies(getTVariavel(node,true), getParents(node,(ArrayList) fronteira[1]));
        int[][] velho = (int[][]) fronteira[2];
        int numeroCelulas = 0;
        double valorQuiQuadrado = 0.0d;
        double numeroCasos = (double)getTotalCases(novo);
        double fator = numeroCasos/(double)getTotalCases(velho);
        System.out.println("Numero de caso = " + numeroCasos);
        System.out.println("Numero de caso Antigo = " + getTotalCases(velho));
        System.out.println("Nó " + node.getName());        
        double intervalosNovos[] = null;
        double intervalosAntigos[] = null;
        double maiorDiferenca = Double.MIN_VALUE;
        
        for(int i = 0; i < velho.length; i++){            
            if(i == 0){
                numeroCelulas = velho.length*velho[i].length;
                intervalosNovos = new double[numeroCelulas];
                intervalosAntigos = new double[numeroCelulas];
            }            
            for(int j = 0; j < velho[i].length && velho[i][j] > 0; j++){
                int indice = (i*velho[i].length)+j;
                if(i >= novo.length){                                        
                    intervalosNovos[indice] = 0;
                    intervalosAntigos[indice] = (double)velho[i][j]*fator/(double)numeroCasos;
                }else{                    
                    if(j >= novo[i].length){
                        intervalosNovos[indice] = 0;
                        intervalosAntigos[indice] = (double)velho[i][j]*fator/(double)numeroCasos;                        
                    }else{
                        intervalosNovos[indice] = (double)novo[i][j]/(double)numeroCasos;
                        intervalosAntigos[indice] = (double)velho[i][j]*fator/(double)numeroCasos;                        
                    }
                }
                if(i != 0  || j != 0){
                    intervalosNovos[indice] += intervalosNovos[indice-1];                    
                    intervalosAntigos[indice] += intervalosAntigos[indice-1];
                }
                if(Math.abs(intervalosNovos[indice] - intervalosAntigos[indice]) > maiorDiferenca){
                    maiorDiferenca = Math.abs(intervalosNovos[indice] - intervalosAntigos[indice]);
                }
            }
        }
        double tabela = 1.92/Math.sqrt(numeroCasos);
        System.out.println("Numero de caso = " + numeroCasos);
        System.out.println("Valor da tabela = " + tabela);
        System.out.println("Valor da maior diferenca = " + maiorDiferenca);
        if(maiorDiferenca < tabela){
            return true;
        }
        /* QUI QUADRADO
        for(int i = 0; i < velho.length; i++){            
            if(i == 0){
                numeroCelulas = velho.length*velho[i].length;
            }            
            for(int j = 0; j < velho[i].length; j++){
                if(velho[i][j] == 0){
                    continue;
                }
                if(i >= novo.length){
                    valorQuiQuadrado +=  Math.pow(Math.abs((double)(0 - velho[i][j]*fator )),2)/(double)velho[i][j]*fator ;
                    //System.out.println("0 - " + velho[i][j] );
                }else{                    
                    if(j >= novo[i].length){
                        valorQuiQuadrado +=  Math.pow(Math.abs((double)(0 - velho[i][j]*fator )),2)/(double)velho[i][j]*fator ;
                        //System.out.println("0 - " + velho[i][j] );
                    }else{
                        valorQuiQuadrado +=  Math.pow(Math.abs((double)(novo[i][j]- velho[i][j]*fator )),2)/(double)velho[i][j]*fator ;
                        //System.out.println(novo[i][j] + " - " + velho[i][j]*fator +"---" + velho[i][j]);
                    }
                }                 
            }
            System.out.println("Valor Qui quadrado = " + valorQuiQuadrado);
        }
        double confianca = TabelaQuiQuadrado.getArea(numeroCelulas - 1,0.05);
        System.out.println("Confianca = " + confianca + "Valor Qui quadrado = " + valorQuiQuadrado);        
        if(confianca > valorQuiQuadrado){
            return true;
        }*/
        return false;
    }
    
    private double calcularConfianca(double nivelConfianca,int grausLiberdade){
        double confianca = (1/(Math.pow(2,(double)grausLiberdade/2)* fat((grausLiberdade/2)-1)))*Math.pow(nivelConfianca,(grausLiberdade/2)-1)*Math.exp(-nivelConfianca/2);
        return confianca;
    }
    
    private double fat(int n){
        if( n <= 100){
            return fatorial(n);       	
        } else{
        	   return stirling(n);       	       	
        }	   	
    }
    
    private double fatorial(int n){
        double f = 1;
        for(int i = 1 ; i <= n ; i++){
        	   f *= i;       	
        }   	
        return f;
    }
    
    private double stirling(int n){
        return Math.sqrt(2*Math.PI*n)*Math.pow(n,n)*Math.exp(-n);
    }

    private NodeList getParents(Node node, ArrayList parents) {
        NodeList parentsAux = new NodeList();
        TVariavel v = getTVariavel(node,true);
        for (int i = 0; i < parents.size(); i++) {
            String parent = (String) parents.get(i);
            for (int j = 0; j < pn.getNodeCount(); j++) {
                if (pn.getNodeAt(j).getName().equals(parent)) {
                    parentsAux.add(getTVariavel(pn.getNodeAt(j),true));
                    break;
                }
            }
        }
        return parentsAux;
    }

    private Node getNode(String nodeName) {
        for (int i = 0; i < pn.getNodeCount(); i++) {
            if (pn.getNodeAt(i).getDescription().equals(nodeName)) {
                return pn.getNodeAt(i);
            }
        }
        return null;

    }

    private void makeBetterNetwork(Object[] betterNet) {
        if (betterNet != null) {
            System.out.println("Mudou Familia do Nó " + betterNet[0]);
            Node node = getNode((String) betterNet[0]);            
            NodeList parents = getParents(node, (ArrayList) betterNet[1]);
            
            node.getParents().removeAll(node.getParents());            
            node.getParents().addAll(parents);
            //hillClibing(betterNet);
        }
    }

    private void hillClibing(Object[] frontierObject) {
        Node node = getNode((String) frontierObject[0]);
        Node pai = null;
        int[][] news = null;
        double g = g(getTVariavel(node, true), getParents(node,
                (ArrayList) frontierObject[1]),
                (int[][]) frontierObject[2]);
        for (int i = 0; i < pn.getNodeCount(); i++) {
            if (!pn.getNodeAt(i).getName().equals(node.getName())) {
                Node nodeAux = pn.getNodeAt(i);                
                /* Verifica se é pai */
                if (!isParent(node, nodeAux) && !isDescendent(node,nodeAux)) {
                    /* Verifica se há uma melhora na pontuacao para aquele nó */
                    node.getParents().add(nodeAux);
                    /* Atualiza pais da fronteira */
                    ((ArrayList) frontierObject[1]).add(nodeAux.getName());
                    /* Novas estatisticas suficientes */
                    int[][] aux = factoreJ(frontierObject);                    
                    double gAux = g(getTVariavel(node,true), getParents(node,
                            (ArrayList) frontierObject[1]), aux);
                    if (gAux > g) {
                        pai = nodeAux;
                        news = aux;
                        g = gAux;
                    }
                    node.getParents().remove(node.getParents().size() - 1);
                    ((ArrayList) frontierObject[1])
                            .remove(((ArrayList) frontierObject[1]).size() - 1);
                }
            }
        }
        if (pai != null) {
            node.getParents().add(pai);
            ((ArrayList) frontierObject[1]).add(node.getName());
            frontierObject[2] = news;
            //hillClibing(frontierObject);
        }
        return;
    }
    
    private boolean isDescendent(Node node, Node nodeTest){
        NodeList filhos = node.getChildren();
        for(int i = 0; i < filhos.size(); i++){
            if(filhos.get(i).getName().equalsIgnoreCase(nodeTest.getName()) || isDescendent(filhos.get(i),nodeTest)){
                return true;
            }
        }
        return false;
    }
    

    private int[][] factoreJ(Object[] frontierObject) {
        ArrayList parents = (ArrayList) frontierObject[1];
        int[][] matrix = (int[][]) frontierObject[2];
        int[][] cloneMatrix = new int[matrix.length][matrix[0].length];
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                cloneMatrix[i][j] = matrix[i][j];
            }
        }
        /* Pega o ultimo pai */
        Node node = getNode((String) parents.get(parents.size() - 1));
        /* Cria um vetor com as instancias do novo pai */
        int[][] old = new int[matrix.length][matrix[0].length
                * node.getStatesSize()];
        long jota = 0;
        for (int i = 0; i < ssList.size(); i++) {
            Object[] fronteira = (Object[]) ssList.get(i);
            long[] marginalVector = makeMarginal((int[][]) fronteira[2]);
            /*
             * Encontrou-se um nó com a distribuição marginal da variável
             * escolhida
             */
            if (fronteira[0].equals(parents.get(parents.size() - 1))) {
                long totalCases = getTotalCases((int[][]) fronteira[2]);
                long caseNumber = totalCases;
                for (int j = 0; j < totalCases; j++) {
                    double[] marginal = getMarginal(marginalVector, caseNumber);
                    int estado = getEstado(marginal);
                    marginalVector[estado]--;
                    double[] distribution = makeDistribution(cloneMatrix,
                            caseNumber);
                    int acumulado = getEstado(distribution);
                    jota = acumulado * node.getStatesSize() + estado;
                    old[(int) jota / old[0].length][(int) jota % old[0].length]++;
                    cloneMatrix[(int) acumulado / cloneMatrix[0].length][(int) acumulado
                            % cloneMatrix[0].length]--;
                    caseNumber--;
                }
                break;
            }
        }
        return old;
    }

    private long[] makeMarginal(int[][] matrix) {
        long[] marginalVector = new long[matrix.length];
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                marginalVector[i] += matrix[i][j];
            }
        }
        return marginalVector;
    }

    private double[] makeDistribution(int[][] matrix, long totalCases) {
        if (matrix.length > 0) {
            int k = 0;
            double[] distribution = new double[matrix.length * matrix[0].length];
            for (int i = 0; i < matrix.length; i++) {
                for (int j = 0; j < matrix[i].length; j++, k++) {
                    distribution[k] = (double)matrix[i][j] / (double)totalCases;
                }
            }
            return distribution;
        } else {
            return null;
        }        
    }

    private long getTotalCases(int[][] array) {
        long totalCases = 0;
        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < array[i].length; j++) {
                totalCases += array[i][j];
            }
        }
        return totalCases;
    }

    /**
     * Pega a distribuição marginal associada ao array. O tamanho do vetor é
     * igual ao número de linhas do array.
     * 
     * @param array
     * @return
     */
    private double[] getMarginal(long[] array, long totalCases) {
        double[] marginais = new double[array.length];
        for (int i = 0; i < array.length; i++) {
            marginais[i] = (double)array[i] / (double)totalCases;
        }
        return marginais;
    }

    private int getEstado(double[] coluna) {
        double[][] faixa;
        double numero = Math.random();
        faixa = criarFaixasIntervalo(coluna);
        for (int i = 0; i < faixa.length; i++) {
            if (i == 0) {
                if (numero <= faixa[i][1] || faixa[i][1] == 0.0) {
                    return i;
                }
                continue;
            } else {
                if (numero <= faixa[i][1] && numero > faixa[i][0]) {
                    return i;
                }
            }
        }
        return -1;
    }

    private double[][] criarFaixasIntervalo(double[] coluna) {
        double[][] faixa = new double[coluna.length][2];
        double atual = 0.0d;
        for (int i = 0; i < coluna.length; i++) {
            faixa[i][0] = atual;
            faixa[i][1] = coluna[i] + atual;
            atual = faixa[i][1];
        }
        return faixa;
    }

    private boolean isParent(Node node, Node nodeAux) {
        int numberParents = node.getParents().size();
        for (int i = 0; i < numberParents; i++) {
            Node parentNode = (Node) node.getParents().get(i);
            if (parentNode.getDescription().equals(nodeAux.getDescription())) {
                return true;
            }
        }
        return false;
    }

    private void paramRecalc() {
        for (int i = 0; i < pn.getNodeCount(); i++) {
            TVariavel node = getTVariavel(pn.getNodeAt(i),true);
            int[][] news = getFrequencies(node, node.getPais());
            getProbability(news, node);
        }
    }

    private File getFile() {
        FileController fileController = FileController.getInstance();
        JFileChooser chooser = new JFileChooser(fileController
                .getCurrentDirectory());
        chooser.setMultiSelectionEnabled(false);
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        // adicionar FileView no FileChooser para desenhar ícones de
        // arquivos
        int option = chooser.showSaveDialog(null);
        if (option == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (file != null) {
                return file;
            }
        }
        return null;
    }

    private File chooseFile(String[] tipos, String title) {
        try {
            FileController fileController = FileController.getInstance();            
            JFileChooser chooser = new JFileChooser(fileController
                    .getCurrentDirectory());
            chooser.setMultiSelectionEnabled(false);
            chooser.addChoosableFileFilter(new SimpleFileFilter(tipos, tipos[0]));
            chooser.setDialogTitle(title);
            int option = chooser.showOpenDialog(null);
            if (option == JFileChooser.APPROVE_OPTION) {
                /* Seta o arquivo escolhido */
                return chooser.getSelectedFile();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }
}

/*
 * Os objetos vindos do arquivo de fronteira sao: Nome da variavel Nome dos pais
 * da variavel matriz de contadores
 * 
 * 
 * 
 * private Object[] getBetterFamilyPonctuation(TVariavel node){ Object[]
 * frontierObject = (Object[])ssList.get(0); double g = Double.MIN_VALUE;
 * Object[] betterFrontier;
 * while(node.getDescription().equals((String)frontierObject[0])){ double gAux =
 * g((TVariavel)node,getParents(node,(ArrayList)frontierObject[1]),(int[][])frontierObject[2]);
 * if(gAux > g){ betterFrontier = frontierObject; g = gAux; } frontierObject =
 * new Object[]{(Object[])ssList.get(0)}; } } return frontierObject; }
 * 
 * 
 * private Object[] getChangedNode(TVariavel node, int[][]nijk){ Object f[] =
 * new Object[2]; f[0] = getAddFrontier(node,nijk); f[1] =
 * getRemoveFrontier(node,nijk); Object[] o0 = (Object[])f[0]; Object[] o1 =
 * (Object[])f[1]; if(((Float)o0[1]).floatValue() >
 * ((Float)o1[1]).floatValue()){ return new Object[]{o0,new Byte((byte)1)}; }
 * return new Object[]{o1, new Byte((byte)0)}; }
 * 
 * private Object[] getAddFrontier(TVariavel node, int[][] nijk){ NodeList
 * parents = node.getParents(); float betterValue = Float.MIN_VALUE; float g =
 * 0.0f; TVariavel betterNode = null; for(int i = 0 ; i < pn.getNodeCount();
 * i++){ TVariavel iNode = (TVariavel)pn.getNodeAt(i);
 * if(iNode.getName().equals(node.getName()) || isParent(iNode,node)){
 * parents.add(iNode); g = (float)g((TVariavel)node,parents,nijk); } if(g >
 * betterValue){ betterValue = g; betterNode = iNode; } parents.remove(iNode); }
 * return new Object[]{betterNode,new Float(betterValue)}; }
 */

/*
 * private Object[] getRemoveFrontier(TVariavel node,int[][]nijk){ NodeList
 * parents = node.getParents(); int numberParents = parents.size(); float
 * betterValue = Float.MIN_VALUE; float g = 0.0f; TVariavel betterNode = null;
 * TVariavel iNode; for(int i = 0; i < numberParents; i++){ iNode =
 * (TVariavel)parents.remove(i); g = (float)g((TVariavel)node,parents,nijk);
 * if(g > betterValue){ betterValue = g; betterNode = iNode; }
 * parents.add(iNode); } return new Object[]{betterNode,new Float(betterValue)}; }
 * 
 * private Object getReverseFrontier(TVariavel node){ int numberParents =
 * node.getParents().size(); for(int i = 0; i < numberParents; i++){ } return
 * null; }
 * 
 * private void getNet(){ try{ String[] nets = new String[] { "net" };
 * JFileChooser chooser = new JFileChooser(".");
 * chooser.setMultiSelectionEnabled(false); chooser.addChoosableFileFilter( new
 * SimpleFileFilter(nets,"Carregar .net")); int option =
 * chooser.showOpenDialog(null); if (option == JFileChooser.APPROVE_OPTION) { if
 * (chooser.getSelectedFile() != null) { file = chooser.getSelectedFile(); pn =
 * io.load(file); } } }catch(LoadException le){ le.printStackTrace();
 * }catch(IOException ie){ ie.printStackTrace(); } }
 */

