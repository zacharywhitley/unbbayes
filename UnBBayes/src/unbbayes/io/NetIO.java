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

package unbbayes.io;

import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.tree.*;

import unbbayes.jprs.jbn.*;
import unbbayes.util.*;

/**
 * Classe que manipula a entrada e saída de arquivos NET.
 * @author Rommel N. Carvalho
 * @author Michael S. Onishi
 * @author Mário Henrique Paes Vieira (mariohpv@bol.com.br)
 * @version 1.0
 */
public class NetIO implements BaseIO {

	/** Load resource file from this package */
	private static ResourceBundle resource =
		ResourceBundle.getBundle("unbbayes.io.resources.IoResources");

	private static final String ERROR_NET = resource.getString("errorNet");

	/**
	 *  Carrega a rede de um arquivo no formato NET.
	 *
	 * @param  input  arquivo a ser lido.
	 * @return rede carregada.
	 * @throws LoadException caso existam erros na fase de carregamento da rede
	 * @throws IOException caso existam erros no processo de manipulação de arquivos.
	 */
	public ProbabilisticNetwork load(File input)
		throws LoadException, IOException {
		ProbabilisticNetwork net = new ProbabilisticNetwork();
		ITabledVariable auxIVTab = null;
		PotentialTable auxTabPot = null;

		BufferedReader r = new BufferedReader(new FileReader(input));
		StreamTokenizer st = new StreamTokenizer(r);
		st.resetSyntax();

		st.wordChars('A', 'Z');
		st.wordChars('a', '}');
		st.wordChars('\u00A0', '\u00FF'); // letras com acentos
                st.wordChars('_', '_');
		st.wordChars('-', '-');
		st.wordChars('0', '9');
		st.wordChars('.', '.');
                st.wordChars('%','%');
		st.ordinaryChars('(', ')');
		st.eolIsSignificant(false);
		st.quoteChar('"');
		//st.commentChar('%');

		proximo(st);
		if (st.sval.equals("net")) {
			proximo(st);

			if (st.sval.equals("{")) {
				proximo(st);
				while (!st.sval.equals("}")) {
					if (st.sval.equals("name")) {
						proximo(st);
						net.setName(st.sval);
					} else if (st.sval.equals("node_size")) {
						proximo(st);
						proximo(st);
						net.setRadius(Double.parseDouble(st.sval) / 2);
					} else if (st.sval.equals("tree"))
                                        {   proximo(st);
                                            StringBuffer sb = new StringBuffer(st.sval);
                                            DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
                                            loadHierarchicTree(sb,root);

                                            // construct tree
                                            DefaultTreeModel model = new DefaultTreeModel(root);
                                            HierarchicTree hierarchicTree = new HierarchicTree(model);

                                            net.setHierarchicTree(hierarchicTree);
                                        } else if (st.sval.equals("HR_Color_Utility")) {
						proximo(st);
						UtilityNode.setColor(Integer.parseInt(st.sval));
					} else if (st.sval.equals("HR_Color_Decision")) {
						proximo(st);
						DecisionNode.setColor(Integer.parseInt(st.sval));
					}
					proximo(st);
				}
			}
		} else {
			throw new LoadException(
				ERROR_NET + resource.getString("LoadException"));
		}

		while (proximo(st) != StreamTokenizer.TT_EOF) {
			if (st.sval.equals("node")
				|| st.sval.equals("decision")
				|| st.sval.equals("utility")) {
				Node auxNo = null;
				if (st.sval.equals("node")) {
					auxNo = new ProbabilisticNode();
				} else if (st.sval.equals("decision")) {
					auxNo = new DecisionNode();
				} else { // utility
					auxNo = new UtilityNode();
				}

				proximo(st);
				auxNo.setName(st.sval);
				proximo(st);
				if (st.sval.equals("{")) {
					proximo(st);
					while (!st.sval.equals("}")) {
						if (st.sval.equals("label")) {
							proximo(st);
							auxNo.setDescription(st.sval);
							proximo(st);
						} else if (st.sval.equals("position")) {
							proximo(st);
							int x = Integer.parseInt(st.sval);
							proximo(st);
							if (x <= 0) {
								x = Node.getLargura();
							}
							int y = Integer.parseInt(st.sval);
							if (y <= 0) {
								y = Node.getAltura();
							}
							auxNo.getPosicao().setLocation(x, y);
							proximo(st);
						} else if (st.sval.equals("states")) {
							while (proximo(st) == '"') {
								auxNo.appendState(st.sval);
							}
						}
                                                else if (st.sval.equals("%descricao"))
                                                {	proximo(st);
							auxNo.setExplanationDescription(unformatString(st.sval));
                                                        auxNo.setInformationType(Node.EXPLANATION_TYPE);
                                                        readTillEOL(st);
                                                        proximo(st);
						}
                                                else if (st.sval.equals("%frase"))
                                                {	proximo(st);
							ExplanationPhrase explanationPhrase = new ExplanationPhrase();
                                                        explanationPhrase.setNode(st.sval);
                                                        proximo(st);
							try
                                                        {   explanationPhrase.setEvidenceType(Integer.parseInt(st.sval));
                                                        }
                                                        catch (Exception ex)
                                                        {   throw new LoadException(
								ERROR_NET
									+ " l."
									+ st.lineno()
									+ resource.getString("LoadException2")
									+ st.sval);
                                                        }
                                                        proximo(st);
                                                        explanationPhrase.setPhrase(unformatString(st.sval));
							auxNo.addExplanationPhrase(explanationPhrase);
                                                        readTillEOL(st);
                                                        proximo(st);
						}
                                                else {
							throw new LoadException(
								ERROR_NET
									+ " l."
									+ st.lineno()
									+ resource.getString("LoadException2")
									+ st.sval);
						}
					}
					net.addNode(auxNo);
				} else {
					throw new LoadException(
						ERROR_NET
							+ " l."
							+ st.lineno()
							+ resource.getString("LoadException3"));
				}
			} else if (st.sval.equals("potential")) {
				proximo(st);
				Node auxNo1 = net.getNode(st.sval);

				if (auxNo1 instanceof ITabledVariable) {
					auxIVTab = (ITabledVariable) auxNo1;
					auxTabPot = auxIVTab.getPotentialTable();
					auxTabPot.addVariable(auxNo1);
				}

				proximo(st);
				if (st.sval.equals("|")) {
					proximo(st);
				}

				Node auxNo2;
				Edge auxArco;
				while (!st.sval.startsWith("{")) {
					auxNo2 = net.getNode(st.sval);
					auxArco = new Edge(auxNo2, auxNo1);
					net.addEdge(auxArco);
					proximo(st);
				}

				if (auxNo1 instanceof ITabledVariable) {
					int sizeVetor = auxTabPot.variableCount() / 2;
					for (int k = 1; k <= sizeVetor; k++) {
						Object temp = auxTabPot.getVariableAt(k);
						auxTabPot.setVariableAt(
							k,
							auxTabPot.getVariableAt(
								auxTabPot.variableCount() - k));
						auxTabPot.setVariableAt(
							auxTabPot.variableCount() - k,
							(Node) temp);
					}
				}
				if (st.sval.length() == 1) {
					proximo(st);
				}

				int nDim = 0;

				while (!st.sval.endsWith("}")) {
					if (st.sval.equals("data")) {
						if (auxNo1.getType() == Node.DECISION_NODE_TYPE) {
							throw new LoadException(
								ERROR_NET
									+ " l."
									+ st.lineno()
									+ resource.getString("LoadException4"));
						}
						proximo(st);
						while (!st.sval.equals("}"))
                                                {	if (st.sval.equals("%"))
                                                        {   readTillEOL(st);
                                                        }
                                                        else
                                                        {   auxTabPot.setValueAt(nDim++,Double.parseDouble(st.sval));
                                                        }
                                                        proximo(st);
						}
					} else {
						throw new LoadException(
							ERROR_NET
								+ " l."
								+ st.lineno()
								+ resource.getString("LoadException5"));
					}
				}
			}
		}
		r.close();
		return net;
	}

	/**
	 *  Salva a rede em um arquivo no formato NET básico.
	 *
	 * @param  output arquivo onde a rede será salva.
	 * @param net rede a ser salva.
	 */
	public void save(File output, ProbabilisticNetwork net) {
		try {
			PrintStream arq = new PrintStream(new FileOutputStream(output));
			arq.println("net");
			arq.println("{");
			arq.println(
				"     node_size = ("
					+ (int) net.getRadius()
					+ " "
					+ (int) net.getRadius()
					+ ");");
			arq.println("     name = \"" + net.getName() + "\";");
                        String tree = saveHierarchicTree(net.getHierarchicTree());
                        if (tree != null)
                            arq.println("     tree = \"" + tree + "\";");
                        arq.println("     HR_Color_Utility = \"" + 10 + "\";");
			arq.println("     HR_Color_Decision = \"" + 30 + "\";");
			arq.println("}");
			arq.println();

			int sizeNos = net.noVariaveis();
			Node auxNo1;
			for (int c1 = 0; c1 < sizeNos; c1++) {
				auxNo1 = (Node) net.getNodeAt(c1);
				if (auxNo1.getType() == Node.PROBABILISTIC_NODE_TYPE) {
					arq.print("node");
				} else if (auxNo1.getType() == Node.DECISION_NODE_TYPE) {
					arq.print("decision");
				} else { // TVU
					arq.print("utility");
				}

				arq.println(" " + auxNo1.getName());
				arq.println("{");
				arq.println(
					"     label = \"" + auxNo1.getDescription() + "\";");
				arq.println(
					"     position = ("
						+ (int) auxNo1.getPosicao().getX()
						+ " "
						+ (int) auxNo1.getPosicao().getY()
						+ ");");

				if (!(auxNo1.getType() == Node.UTILITY_NODE_TYPE)) {
					StringBuffer auxString =
						new StringBuffer("\"" + auxNo1.getStateAt(0) + "\"");

					int sizeEstados = auxNo1.getStatesSize();
					for (int c2 = 1; c2 < sizeEstados; c2++) {
						auxString.append(" \"" + auxNo1.getStateAt(c2) + "\"");
					}
					arq.println(
						"     states = (" + auxString.toString() + ");");
				}
                                if ((!auxNo1.getExplanationDescription().equals("")) && (auxNo1.getInformationType() == Node.EXPLANATION_TYPE))
                                {   String explanationDescription = formatString(auxNo1.getExplanationDescription());
                                    arq.println("     %descricao \"" + explanationDescription + "\"");
                                }
                                if (auxNo1.getInformationType() == Node.EXPLANATION_TYPE)
                                {   ArrayMap arrayMap = auxNo1.getPhrasesMap();
                                    int size = arrayMap.size();
                                    ArrayList keys = arrayMap.getKeys();
                                    for (int i=0; i<size; i++)
                                    {   Object key = keys.get(i);
                                        ExplanationPhrase explanationPhrase = (ExplanationPhrase)arrayMap.get(key);
                                        arq.println("     %frase \"" + explanationPhrase.getNode() + "\" " + "\"" + explanationPhrase.getEvidenceType() + "\" " + "\"" + formatString(explanationPhrase.getPhrase()) + "\"");
                                    }
                                }

				arq.println("}");
				arq.println();
			}
			/*
			 * fim da escrita das variaveis!
			 * agora vamos à escrita dos potenciais!
			 */
			for (int c1 = 0; c1 < net.noVariaveis(); c1++) {
				auxNo1 = (Node) net.getNodeAt(c1);

				NodeList auxListVa = auxNo1.getParents();

				arq.print("potential (" + auxNo1.getName());

				int sizeVa = auxListVa.size();
				if (sizeVa > 0) {
					arq.print(" |");
				}
				for (int c2 = 0; c2 < sizeVa; c2++) {
					Node auxNo2 = (Node) auxListVa.get(c2);
					arq.print(" " + auxNo2.getName());
				}
				arq.println(")");
				arq.println("{");
				if (auxNo1 instanceof ITabledVariable) {
					PotentialTable auxTabPot =
						((ITabledVariable) auxNo1).getPotentialTable();
					auxListVa.clear();
					for (int i = 0; i < auxTabPot.variableCount(); i++) {
						auxListVa.add(auxTabPot.getVariableAt(i));
					}
					/*
					if (auxNo1 instanceof UtilityNode) {
					   auxListVa.remove(0);
					}
					*/

					arq.print(" data = ");
					int[] coord = new int[auxListVa.size()];
					boolean[] paren = new boolean[auxListVa.size()];

					int sizeDados = auxTabPot.tableSize();
					for (int c2 = 0; c2 < sizeDados; c2++) {
						coord = auxTabPot.voltaCoord(c2);

						int sizeVa1 = auxListVa.size();
						for (int c3 = 0; c3 < sizeVa1; c3++) {
							if ((coord[c3] == 0) && (!paren[c3])) {
								arq.print("(");
								paren[c3] = true;
							}
						}
						arq.print(" " + auxTabPot.getValue(c2));
						if ((c2 % auxNo1.getStatesSize())
							== auxNo1.getStatesSize() - 1) {
							arq.print(" ");
						}

						int celulas = 1;

						int sizeVa2 = auxListVa.size();
						Node auxNo2;
						for (int c3 = 0; c3 < sizeVa2; c3++) {
							auxNo2 = (Node) auxListVa.get(c3);
							celulas *= auxNo2.getStatesSize();
							if (((c2 + 1) % celulas) == 0) {
								arq.print(")");
								if (c3 == auxListVa.size() - 1) {
									arq.print(";");
								}
								paren[c3] = false;
							}
						}

						if (((c2 + 1) % auxNo1.getStatesSize()) == 0) {
							//                            arq.println("\t%");
							arq.println();
						}
					}
				}

				arq.println("}");
				arq.println();
			}
			arq.close();
		} catch (FileNotFoundException e) {
			System.err.println(resource.getString("FileNotFoundException"));
		}

	}

	private int proximo(StreamTokenizer st) throws IOException {
		do {
			st.nextToken();
		} while (
			(st.ttype != StreamTokenizer.TT_WORD)
				&& (st.ttype != '"')
				&& (st.ttype != StreamTokenizer.TT_EOF));
		return st.ttype;
	}

    private void loadHierarchicTree(StringBuffer sb,DefaultMutableTreeNode root)
    {   int size = sb.length();
        DefaultMutableTreeNode nextRoot = null;
        Stack stack = new Stack();
        for (int i=0;i<size;i++)
        {   char c = sb.charAt(i);
            if (c == '(')
            {   if (nextRoot != null)
                {   stack.push(root);
                    root = nextRoot;
                }
            }
            else if (c == ')')
            {   if (stack.size() > 0)
                {   root = (DefaultMutableTreeNode)stack.pop();
                }
            }
            else if (c == ',')
            {
            }
            else
            {   StringBuffer newWord = new StringBuffer();
                while ((c != '(')&&(c != ')')&&(c != ','))
                {   newWord.append(c);
                    i++;
                    c = sb.charAt(i);
                }
                i--;
                DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(newWord);
                nextRoot = newNode;
                root.add(newNode);
            }
        }
    }

    private String saveHierarchicTree(HierarchicTree hierarchicTree)
    {   TreeModel model = hierarchicTree.getModel();
        StringBuffer sb = new StringBuffer();
        TreeNode root = (TreeNode)model.getRoot();
        int childCount = model.getChildCount(root);
        if (childCount == 0)
        {   return null;
        }
        else
        {   sb.append('(');
            for (int i=0; i<childCount; i++)
            {   processTreeNode((TreeNode)model.getChild(root,i),sb,model);
                if (i != (childCount-1))
                {   sb.append(',');
                }
            }
            sb.append(')');
            return sb.toString();
        }
    }

    private void processTreeNode(TreeNode node,StringBuffer sb,TreeModel model)
    {   sb.append(node.toString());
        if (!node.isLeaf())
        {   sb.append('(');
            int childCount = model.getChildCount(node);
            for (int i=0; i<childCount; i++)
            {   processTreeNode((TreeNode)model.getChild(node,i),sb,model);
                if (i != (childCount-1))
                {   sb.append(',');
                }
            }
            sb.append(')');
        }
    }

    /**
    * Reads and skips all tokens before next end of line token.
    *
    * @param tokenizer Stream tokenizer
    * @throws IOException EOF not found
    */
    private void readTillEOL(StreamTokenizer tokenizer) throws IOException
    {	while (tokenizer.nextToken() != StreamTokenizer.TT_EOL)
        {};
    	tokenizer.pushBack();
    }

    private String formatString(String string)
    {   return string.replace('\n','#');
    }

    private String unformatString(String string)
    {   return string.replace('#','\n');
    }
}