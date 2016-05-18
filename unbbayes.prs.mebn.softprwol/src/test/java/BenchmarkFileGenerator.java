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

	private static final int QUANT_PERSON = 1000; 
	private static final int PERC_CRIMINAL_HISTORY = 99; //1 per cent have criminal history 
	private static final int PERC_ADMNISTRATIVE_HISTORY = 20; 
	
//	private static final int QUANT_MEMBER = 10; 
	
	private static final int QUANT_PROCUREMENT = 20; 
	private static final int QUANT_PUBLIC_AGENCY = 1; 
	private static final int QUANT_ENTERPRISE = 50; 
	
	private static final int AVG_NUMBER_MEMBER_PROCUREMENT = 5; 
	private static final int AVG_NUMBER_MEMBER_ENTERPRISE = 10; 
	private static final int AVG_NUMBER_ENTERPRISES_PROCUREMENT = 5; 
	
	private static final int QUANT_PERSON_SAME_ADDRESS = 100; 
	
	private static final String PREFIX = "exp:"; 
	
	private static final String PREFIX_STRING = 
			"@prefix " + PREFIX + " <http://www.pr-owl.org/examples/pr-owl2/ProcurementFraud/ProcurementFraud.owl#>" + "."; 
	
	public static void main(String[] args){
		
		Random gerador = new Random();
		
		System.out.println("Generate test file");
		
		File file = new File("teste.ttl");
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
		
		int count_criminal_history = 0; 
		int count_admnistrative_history = 0; 
		
		//persons 
		for(int i = 0; i < QUANT_PERSON; i ++){
			String name = PREFIX + "person" + i; 
			out.println(name + " rdf:type " + PREFIX + "Person" + "."); 
			
			//hasCriminalHistory
			if(count_criminal_history == PERC_CRIMINAL_HISTORY){
				out.println(name + " " + 
			                PREFIX + "hasCriminalHistory" + " " + 
			                PREFIX + "Convicted" + "."); 
				count_criminal_history = 0; 
			}else{
				out.println(name + " " + 
		                PREFIX + "hasCriminalHistory" + " " + 
		                PREFIX + "NeverInvestigated" + "."); 
			}
			count_criminal_history++; 
			
			//hasAdmnistrativeHistory
			if(count_admnistrative_history == PERC_ADMNISTRATIVE_HISTORY){
				out.println(name + " " + 
			                PREFIX + "hasAdmnistrativeHistory" + " " + 
			                PREFIX + "Convicted" + "."); 
				count_admnistrative_history = 0; 
			}else{
				out.println(name + " " + 
		                PREFIX + "hasAdmnistrativeHistory" + " " + 
		                PREFIX + "NeverInvestigated" + "."); 
			}
			count_admnistrative_history++; 
			
			//hasAnnualIncoming
			int num = gerador.nextInt(100); 
			
			//Usa as probabilidades definidas pelo Rommel na sua dissertação

			String state = ""; 

			if(num < 40){
				state = "Lower10k"; 
			}else{
				if(num < 70){
					state = "From10kTo30k"; 
				}else{
					if(num < 90){
						state = "From30kTo60k"; 
					}else{
						if(num < 99){
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
			count_admnistrative_history = 0; 


			num = gerador.nextInt(100); 
			state = ""; 

			if(num < 10){
				state = "NoEducation"; 
			}else{
				if(num < 50){
					state = "MiddleSchool"; 
				}else{
					if(num < 80){
						state = "HighSchool"; 
					}else{
						if(num < 95){
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
		}
		
		//sameAddress
		for (int j = 0; j < QUANT_PERSON_SAME_ADDRESS; j++){ 
			out.println(PREFIX + "person" + gerador.nextInt(QUANT_PERSON) + " " +  
					PREFIX + "liveAtSameAddress" + " " + 
					PREFIX + "person" + gerador.nextInt(QUANT_PERSON) + "."); 
		}

		//public agency 
		for(int i = 0; i < QUANT_PUBLIC_AGENCY; i ++){
			String name = PREFIX + "agency" + i; 
			out.println(name + " rdf:type " + PREFIX + "PublicAgency" + "."); 
		}
		
		//People member of comitee (last numbers) 
		int qtdMemberComitee = QUANT_PROCUREMENT * AVG_NUMBER_MEMBER_PROCUREMENT; 
		int indexPerson = QUANT_PERSON - qtdMemberComitee; 
	
		//procurements 
		for(int i = 0; i <= QUANT_PROCUREMENT; i ++){
			String name = PREFIX + "procurement" + i; 
			out.println(name + " rdf:type " + PREFIX + "Procurement" + "."); 
			
			//isProcurementFinished  
			int num = gerador.nextInt(2); 
			
			if(num == 0){
				out.println(name + " " + PREFIX + "isProcurementFinished" + " " + "\"true\"^^xsd:boolean" + "."); 
			}else{
				out.println(name + " " + PREFIX + "isProcurementFinished" + " " + "\"false\"^^xsd:boolean" + "."); 
			}
			
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
			
			//isMemberOfComitee
			for(int j = 0; j < AVG_NUMBER_MEMBER_PROCUREMENT; j++ ){
				out.println(PREFIX+ "person" + indexPerson + " " + PREFIX + "isMemberOfCommittee" + " " + name + ".");
				indexPerson++; 
			}
			
			//hasProcurementOwner
			//Por enquanto apenas uma agencia... 
			out.println(name + " " + PREFIX + "hasProcurementOwner" + " " + PREFIX + "agency0" + ".");
			
			
			//Empresas Participantes
			//Considerar aqui 5 empresas por licitação. 
			
			int arrayEnterprises[] = new int[AVG_NUMBER_ENTERPRISES_PROCUREMENT]; 
			for(int j = 0; j < AVG_NUMBER_ENTERPRISES_PROCUREMENT; j++){
				int numEnterprise = gerador.nextInt(QUANT_ENTERPRISE); 
				arrayEnterprises[j] = numEnterprise; 
				out.println(PREFIX + "enterprise" + numEnterprise + " " + PREFIX + "isParticipantIn" + " " + name + ".");
			}
			
			//Select one of the concorrents to be the winner. 
			int winner = gerador.nextInt(AVG_NUMBER_ENTERPRISES_PROCUREMENT); 
			out.println( name + " " + PREFIX + "hasWinnerOfProcurement" + " " + PREFIX + "enterprise" + arrayEnterprises[winner] + ".");
			
		}
		
		
		//enterprises 
		
//		int qtdMemberEnterprise = QUANT_ENTERPRISE * AVG_NUMBER_MEMBER_ENTERPRISE; 
		indexPerson = 0; 
		
		for(int i = 0; i <= QUANT_ENTERPRISE; i ++){
			String name = PREFIX + "enterprise" + i; 
			out.println(name + " rdf:type " + PREFIX + "Enterprise" + "."); 
			
			//isSuspended
			int num = gerador.nextInt(10); 
			if(num == 0){
				out.println(name + " " + PREFIX + "isSuspended" + " " + "\"true\"^^xsd:boolean" + "."); 
			}else{
				out.println(name + " " + PREFIX + "isSuspended" + " " + "\"false\"^^xsd:boolean" + "."); 
			}
			
			out.println(PREFIX + "person" + indexPerson + " " + PREFIX + "isResponsibleFor" + " " + name + ".");
			indexPerson++;
			
		}

		
		
		//People owner of enterprise (first numbers) 
		
		out.close();
		
		System.out.println("Program finished");
		
	}
	
	
}
