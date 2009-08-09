/*
 *  UnBBayes
 *  Copyright (C) 2002, 2008 Universidade de Brasilia - http://www.unb.br
 *
 *  This file is part of UnBBayes.
 *
 *  UnBBayes is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  UnBBayes is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with UnBBayes.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package unbbayes.io.mebn;

import java.util.ArrayList;
import java.util.List;

import unbbayes.prs.Node;
import unbbayes.prs.mebn.Argument;
import unbbayes.prs.mebn.BuiltInRV;
import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.InputNode;
import unbbayes.prs.mebn.MFrag;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.MultiEntityNode;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.ResidentNode;

public class TestMebnIO {
	
	public static final String STARSHIP16FILEEXAMPLE = "examples/mebn/Starship16.owl"; 
	public static final String STARSHIP16FILEEXAMPLESAVED = "examples/mebn/Starship16_RES.owl"; 
	
	private static ArrayList<Argument> listArgument = new ArrayList<Argument>(); 
	
	/** 
	 * Trace the structure of mebn 
	 * @param mebn 
	 * */
	
	public static void traceMebnStructure(MultiEntityBayesianNetwork mebn){
		
		System.out.println("\n\n--------------------");
		System.out.println("Test MEBN Structure "); 
		System.out.println("-------------------- \n");		
		
		/* trace MTheory */
		
		System.out.println("-----> MTheory: " + mebn.getName() + "\n");
		
		System.out.println("-> mFragList: "); 
		List<MFrag> listMFrag = mebn.getMFragList(); 
		for(MFrag mFrag: listMFrag){
			System.out.println(mFrag.getName()); 
		}
		
		/* trace MFrag */
		for(MFrag mFrag: listMFrag){
			
			MFrag domainMFrag = (MFrag)mFrag; 
			
			System.out.println("\n\n-----> MFrag: " + domainMFrag.getName() + "\n");
			
			System.out.println("\n-> nodeList: "); 
			ArrayList<Node> nodeList = domainMFrag.getNodeList(); 
			int sizeNodeList = nodeList.size(); 
			for(int i = 0; i < sizeNodeList ; i++){
				System.out.println(nodeList.get(i).getName()); 
			}				
			
			System.out.println("\n-> contextNodeList: "); 
			List<ContextNode> contextNodeList = domainMFrag.getContextNodeList(); 
			for(ContextNode contextNode: contextNodeList){
				System.out.println(contextNode.getName()); 
			}						
			
			System.out.println("\n-> domainResidentNodeList: "); 
			List<ResidentNode> domainResidentNodeList = domainMFrag.getResidentNodeList(); 
			for(ResidentNode residentNode: domainResidentNodeList){
				System.out.println(residentNode.getName()); 
			}				
			
			System.out.println("\n-> GenerativeInputNodeList: "); 
			List<InputNode> generativeInputNodeList = domainMFrag.getInputNodeList(); 
			for(InputNode generativeInputNode: generativeInputNodeList){
				System.out.println(generativeInputNode.getName()); 
			}	
			
			System.out.println("\n-> OrdinaryVariableList: "); 
			List<OrdinaryVariable> ordinaryVariableList = domainMFrag.getOrdinaryVariableList(); 
			for(OrdinaryVariable ordinaryVariable: ordinaryVariableList){
				System.out.println(ordinaryVariable.getName()); 
			}			
		}
		
		/* trace inside MFrag */
		for(MFrag mFrag: listMFrag){
			
			MFrag domainMFrag = (MFrag) mFrag;  
			
			/* trace context node */
			List<ContextNode> listContextNode = domainMFrag.getContextNodeList(); 
			for(ContextNode contextNode: listContextNode){
				System.out.println("\n\n-----> ContextNode: " + contextNode.getName() + "\n");
				
				System.out.println("\n-> mFrag: "); 
				MFrag mFragTeste = contextNode.getMFrag(); 
				System.out.println(mFragTeste.getName()); 							
				
				System.out.println("\n-> argumentList: "); 
				List<Argument> argumentList = contextNode.getArgumentList(); 
				for(Argument argument: argumentList){
					System.out.println(argument.getName()); 
					listArgument.add(argument); 
				}				
				
			}						
			
			/* trace domain resident node */
			List<ResidentNode> domainResidentNodeList = domainMFrag.getResidentNodeList(); 
			for(ResidentNode domainResidentNode: domainResidentNodeList){
				System.out.println("\n\n-----> DomainResident: " + domainResidentNode.getName() + "\n");
				
				System.out.println("\n-> mFrag: "); 
				MFrag mFragTeste = domainResidentNode.getMFrag(); 
				System.out.println(mFragTeste.getName()); 				
				
				System.out.println("\n-> innerTermOfList: "); 
				List<MultiEntityNode> innerTermOfList = domainResidentNode.getInnerTermOfList(); 
				for(MultiEntityNode node: innerTermOfList){
					System.out.println(node.getName()); 
				}				
				
				System.out.println("\n-> innerTermFromList: "); 
				List<MultiEntityNode> innerTermFromList = domainResidentNode.getInnerTermFromList(); 
				for(MultiEntityNode node: innerTermFromList){
					System.out.println(node.getName()); 
				}				
				
				System.out.println("\n-> residentNodeFatherList: "); 
				List<ResidentNode> domainResidentNodeFatherList = domainResidentNode.getResidentNodeFatherList(); 
				for(ResidentNode resident: domainResidentNodeFatherList){
					System.out.println(resident.getName()); 
				}	
				
				System.out.println("\n-> inputNodeFatherList: "); 
				List<InputNode> inputNodeFatherList = domainResidentNode.getParentInputNodesList(); 
				for(InputNode input: inputNodeFatherList){
					System.out.println(input.getName()); 
				}	
				
				System.out.println("\n-> residentNodeChildList: "); 
				List<ResidentNode> domainResidentNodeChildList = domainResidentNode.getResidentNodeChildList(); 
				for(ResidentNode resident: domainResidentNodeChildList){
					System.out.println(resident.getName()); 
				}	
				
				System.out.println("\n-> inputInstanceFromList: "); 
				List<InputNode> inputInstanceFromList = domainResidentNode.getInputInstanceFromList(); 
				for(InputNode input: inputInstanceFromList){
					System.out.println(input.getName()); 
				}	
				
				System.out.println("\n-> argumentList: "); 
				List<Argument> argumentList = domainResidentNode.getArgumentList(); 
				for(Argument argument: argumentList){
					System.out.println(argument.getName()); 
					listArgument.add(argument); 
				}
			}				
			
			/* trace input node */
			List<InputNode> generativeInputNodeList = domainMFrag.getInputNodeList(); 
			for(InputNode generativeInputNode: generativeInputNodeList){
				System.out.println("\n\n-----> Generative Input: " + generativeInputNode.getName() + "\n");
				
				System.out.println("\n-> mFrag: "); 
				MFrag mFragTeste = generativeInputNode.getMFrag(); 
				System.out.println(mFragTeste.getName()); 				
				
				System.out.println("\n-> innerTermOfList: "); 
				List<MultiEntityNode> innerTermOfList = generativeInputNode.getInnerTermOfList(); 
				for(MultiEntityNode node: innerTermOfList){
					System.out.println(node.getName()); 
				}				
				
				System.out.println("\n-> innerTermFromList: "); 
				List<MultiEntityNode> innerTermFromList = generativeInputNode.getInnerTermFromList(); 
				for(MultiEntityNode node: innerTermFromList){
					System.out.println(node.getName()); 
				}				
				
				System.out.println("\n-> ResidentNodeChildList: "); 
				List<ResidentNode> residentNodeChildList = generativeInputNode.getResidentNodeChildList(); 
				for(ResidentNode resident: residentNodeChildList){
					System.out.println(resident.getName()); 
				}	
				
				System.out.println("\n-> inputInstanceOf: "); 

                Object inputOf = generativeInputNode.getInputInstanceOf(); 
				if(inputOf != null){
					if (inputOf instanceof BuiltInRV){
						   System.out.println("" + ((BuiltInRV)inputOf).getName()); 	
					}
					else{
						   System.out.println("" + ((ResidentNode)inputOf).getName()); 							
					}
				}
				
				System.out.println("\n-> argumentList: "); 
				List<Argument> argumentList = generativeInputNode.getArgumentList(); 
				for(Argument argument: argumentList){
					System.out.println(argument.getName()); 
					listArgument.add(argument); 
				}				
				
			}
			
			/* trace ordinary variable */
			List<OrdinaryVariable> ordinaryVariableList = domainMFrag.getOrdinaryVariableList(); 
			for(OrdinaryVariable ordinaryVariable: ordinaryVariableList){
				System.out.println("\n\n-----> ordinaryVariable: " + ordinaryVariable.getName() + "\n");
				System.out.println("MFrag: " + ordinaryVariable.getMFrag().getName()); 
			}			
			
			
		}	
		
		/* trace arguments */ 
		
		for (Argument argument: listArgument){
			
			System.out.println("\n\n-----> Argument: " + argument.getName() + "\n");
			if (argument.isSimpleArgRelationship()){
				System.out.println("Type: SimpleArgRelationship");
				System.out.println("OVariable: " + argument.getOVariable().getName()); 
			}
			else{
					System.out.println("Type: ArgRelationship");
					if(argument.getArgumentTerm() != null){
					   System.out.println("ArgTerm: " + argument.getArgumentTerm().getName()); 
					}
					else{
					   System.out.println("ERRO: ArgTerm nao preenchido!!!"); 						
					}
			}
			
		}
		
		/* BuiltInRV don't checked */
		
	}
	
	/* Run Tests */
	/*
	
	public static void main(String[] args) {
		
		MultiEntityBayesianNetwork mebn = null; 
		
		PrOwlIO prOwlIO = new PrOwlIO(); 
		
		System.out.println("-----Load file test-----"); 
		
		File file = new File(STARSHIP16FILEEXAMPLE); 
		
		try{
			mebn = prOwlIO.loadMebn(file); 
			System.out.println("Load concluido"); 
		}
		catch (IOMebnException e){
			System.out.println("ERROR IO PROWL!!!!!!!!!"); 
			e.printStackTrace();
		}
		catch (IOException e){
			System.out.println("ERROR IO!!!!!!!!!"); 
			e.printStackTrace();
		}
		
		System.out.println("Load finished!"); 
		
		try{
			traceMebnStructure(mebn);
		}
		catch(Exception e){
			e.printStackTrace(); 
		}
		
		try{
		   prOwlIO.saveMebn(STARSHIP16FILEEXAMPLESAVED, mebn);
		}
		catch(Exception e){
			e.printStackTrace(); 
		}

	} *///main	
}
