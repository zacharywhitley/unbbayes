package unbbayes.io.mebn;

import java.util.List;

import unbbayes.controller.ReservedWordsList;

public class UbfIOReservedWords implements ReservedWordsList{

	public List<String> getReservedWordsList() {
		return PrOWLMapping.getReservedWords();
	}

}
