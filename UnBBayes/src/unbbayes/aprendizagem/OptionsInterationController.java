
package unbbayes.aprendizagem;

public class OptionsInterationController {
	
	private TVariavel variable;
	private OptionsWindow frame;
	
	public OptionsInterationController(OptionsWindow frame, TVariavel variable){
		this.frame = frame;
		this.variable = variable;		
	}
	
	public void applyEvent(String text){		
		if(! text.equals("")){
            try{
                int max = Integer.parseInt(text);
                variable.setNumeroMaximoPais(max);
                frame.dispose();
            }catch (NumberFormatException e){
                System.out.println("Digite um inteiro valido");
            }
        }		
	}
	
	public void cancelEvent(){
		frame.dispose();				
	}

}
