import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Random;

/**
 * 
 * Generate test files of Procurement Fraud Ontology 
 * 
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 */
public class BenchmarkFileGenerator {

	private static final int QUANT_PERSON = 10000000; 
	private static final int PERC_CRIMINAL_HISTORY = 5; //1 percent have criminal history 
	private static final int PERC_ADMNISTRATIVE_HISTORY = 5; //1 for each 20 have admnistrative history
	
//	private static final int QUANT_MEMBER = 10; 
	
	private static final int QUANT_PROCUREMENT = 200000; 
	private static final int QUANT_PUBLIC_AGENCY = 1; 
	private static final int QUANT_ENTERPRISE = 500000; 
	
	private static final int AVG_NUMBER_MEMBER_PROCUREMENT = 4; 
//	private static final int AVG_NUMBER_MEMBER_ENTERPRISE = 10; 
	private static final int AVG_NUMBER_ENTERPRISES_PROCUREMENT = 4; 
	
	private static final int QUANT_PERSON_SAME_ADDRESS = 1000000; 
	
	private static int total_statements = 0; 
	
	private static final String PREFIX = "exp:"; 
	
	private static final String PREFIX_STRING = 
			"@prefix " + PREFIX + " <http://www.pr-owl.org/examples/pr-owl2/ProcurementFraud/ProcurementFraud.owl#>" + "."; 
	
	public static void main(String[] args){
		
		Random gerador = new Random();
		
		System.out.println("Generate test file");
		
		File file = new File("Base_" + QUANT_PERSON + ".ttl");
		System.out.println("File: " + file.getAbsolutePath());
		
		PrintStream out = null; 
		
		try {
			out = new PrintStream(new FileOutputStream(file));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Error creating file.");
			System.exit(0); 
		}
		
		out.println(PREFIX_STRING);
		
		int randomNum = gerador.nextInt(100); 
		
		//persons 
		for(int i = 0; i < QUANT_PERSON; i ++){
			String name = PREFIX + "person" + i; 
			out.println(name + " rdf:type " + PREFIX + "Person" + "."); 
			total_statements++;
			
			//hasCriminalHistory
			if(randomNum <= PERC_CRIMINAL_HISTORY){
				out.println(name + " " + 
			                PREFIX + "hasCriminalHistory" + " " + 
			                PREFIX + "Convicted" + "."); 
			}else{
				out.println(name + " " + 
		                PREFIX + "hasCriminalHistory" + " " + 
		                PREFIX + "NeverInvestigated" + "."); 
			}
			total_statements++;
			
			randomNum = gerador.nextInt(100); 
			//hasAdmnistrativeHistory
			if(randomNum <= PERC_ADMNISTRATIVE_HISTORY){
				out.println(name + " " + 
			                PREFIX + "hasAdmnistrativeHistory" + " " + 
			                PREFIX + "Convicted" + "."); 
			}else{
				out.println(name + " " + 
		                PREFIX + "hasAdmnistrativeHistory" + " " + 
		                PREFIX + "NeverInvestigated" + "."); 
			}
			total_statements++;
			
			//hasAnnualIncoming
			randomNum = gerador.nextInt(100); 
			
			//Use the likelihoods defined into the Rommel's dissertation

			String state = ""; 

			if(randomNum < 40){
				state = "Lower10k"; 
			}else{
				if(randomNum < 70){
					state = "From10kTo30k"; 
				}else{
					if(randomNum < 90){
						state = "From30kTo60k"; 
					}else{
						if(randomNum < 99){
							state = "From60kTo100k"; 
						}else{
							state = "Greater100k"; 
						}
					}
				}
			}

			out.println(name + " " + 
					PREFIX + "hasAnnualIncoming" + " " + 
					PREFIX + state + "."); 
			total_statements++;


			randomNum = gerador.nextInt(100); 
			state = ""; 

			if(randomNum < 10){
				state = "NoEducation"; 
			}else{
				if(randomNum < 50){
					state = "MiddleSchool"; 
				}else{
					if(randomNum < 80){
						state = "HighSchool"; 
					}else{
						if(randomNum < 95){
							state = "Undergraduate"; 
						}else{
							state = "Graduate"; 
						}
					}
				}
			}

			out.println(name + " " + 
					PREFIX + "hasEducationLevel" + " " + 
					PREFIX + state + "."); 
			total_statements++;
		}
		
		System.out.println("Peoples: " + QUANT_PERSON);
		
		//sameAddress
		for (int j = 0; j < QUANT_PERSON_SAME_ADDRESS; j++){ 
			out.println(PREFIX + "person" + gerador.nextInt(QUANT_PERSON) + " " +  
					PREFIX + "liveAtSameAddress" + " " + 
					PREFIX + "person" + gerador.nextInt(QUANT_PERSON) + "."); 
			total_statements++;
		}

		System.out.println("Peoples at same addres (pairs): " + QUANT_PERSON_SAME_ADDRESS);
		
		//public agency 
		for(int i = 0; i < QUANT_PUBLIC_AGENCY; i ++){
			String name = PREFIX + "agency" + i; 
			out.println(name + " rdf:type " + PREFIX + "PublicAgency" + "."); 
			total_statements++;
		}
		System.out.println("Agencies: " + QUANT_PUBLIC_AGENCY);
		
		//People member of committee (last numbers) 
		int qtdMemberComitee = QUANT_PROCUREMENT * AVG_NUMBER_MEMBER_PROCUREMENT;
		int indexPerson = QUANT_PERSON - qtdMemberComitee; 
		
		System.out.println("Members of committee: " + qtdMemberComitee);
		System.out.println("First person member of committee: " + indexPerson);
		
		//procurements 
		for(int i = 0; i < QUANT_PROCUREMENT; i ++){
			String name = PREFIX + "procurement" + i; 
			out.println(name + " rdf:type " + PREFIX + "Procurement" + "."); 
			total_statements++;
			
			//isProcurementFinished  
			int num = gerador.nextInt(2); 
			
			if(num == 0){
				out.println(name + " " + PREFIX + "isProcurementFinished" + " " + "\"true\"^^xsd:boolean" + "."); 
			}else{
				out.println(name + " " + PREFIX + "isProcurementFinished" + " " + "\"false\"^^xsd:boolean" + "."); 
			}
			total_statements++;
			
			//hasValue
			num = gerador.nextInt(5); 
			String value = ""; 
			switch(num){
			case 0: 
				value = "Lower10k"; 
				break; 
			case 1: 
				value = "From10kTo100k"; 
				break; 
			case 2:
				value = "From100kTo500k"; 
				break; 
			case 3: 
				value = "From500kTo1000k"; 
				break; 
			case 4: 
				value = "Greater1000k"; 
				break; 
			}
			
			out.println(name + " " + PREFIX + "hasValue" + " " + PREFIX + value + ".");
			total_statements++;
			
			//isMemberOfComitee
			for(int j = 0; j < AVG_NUMBER_MEMBER_PROCUREMENT; j++ ){
				out.println(PREFIX+ "person" + indexPerson + " " + PREFIX + "isMemberOfCommittee" + " " + name + ".");
				indexPerson++; 
				total_statements++;
			}
			
			
			//hasProcurementOwner
			out.println(name + " " + PREFIX + "hasProcurementOwner" + " " + PREFIX + "agency0" + ".");
			total_statements++;
			
			
			//Enterprises participants
			
			int arrayEnterprises[] = new int[AVG_NUMBER_ENTERPRISES_PROCUREMENT]; 
			
			for(int j = 0; j < AVG_NUMBER_ENTERPRISES_PROCUREMENT; j++){
				
				int numEnterprise = -1;  
				
				//This loop avoid that the same enterprise number be used two times
				boolean numberNew = false; 
				
				while(!numberNew){
					numEnterprise = gerador.nextInt(QUANT_ENTERPRISE); 
					numberNew = true; 
					for(int k = 0; k < j; k ++){
						if(arrayEnterprises[k] == numEnterprise){
							numberNew = false; 
						}
					}
				}
				
				arrayEnterprises[j] = numEnterprise; 
				out.println(PREFIX + "enterprise" + numEnterprise + " " + PREFIX + "isParticipantIn" + " " + name + ".");
				total_statements++;
			}
			
			//Select one of the concorrents to be the winner. 
			int winner = gerador.nextInt(AVG_NUMBER_ENTERPRISES_PROCUREMENT); 
			out.println( name + " " + PREFIX + "hasWinnerOfProcurement" + " " + PREFIX + "enterprise" + arrayEnterprises[winner] + ".");
			total_statements++;
			
		}
		
		System.out.println("Procurements: " + QUANT_PROCUREMENT);
		System.out.println("Members of Committee: " + qtdMemberComitee);
		
		//enterprises 
		
//		int qtdMemberEnterprise = QUANT_ENTERPRISE * AVG_NUMBER_MEMBER_ENTERPRISE; 
		indexPerson = 0; 
		
		for(int i = 0; i <= QUANT_ENTERPRISE; i ++){
			String name = PREFIX + "enterprise" + i; 
			out.println(name + " rdf:type " + PREFIX + "Enterprise" + "."); 
			total_statements++;
			
			//isSuspended
			//Consider 1% of the enterprises suspended
			int num = gerador.nextInt(100); 
			if(num == 0){
				out.println(name + " " + PREFIX + "isSuspended" + " " + "\"true\"^^xsd:boolean" + "."); 
			}else{
				out.println(name + " " + PREFIX + "isSuspended" + " " + "\"false\"^^xsd:boolean" + "."); 
			}
			total_statements++;
			
			out.println(PREFIX + "person" + indexPerson + " " + PREFIX + "isResponsibleFor" + " " + name + ".");
			total_statements++;
			indexPerson++;
			
		}
		
		System.out.println("Enterprises: " + QUANT_ENTERPRISE );
		
		//People owner of enterprise (first numbers) 
		
		out.close();
		
		System.out.println("Total of Statements: " + total_statements);
		System.out.println("Program finished");
		
	}
	
	
}
