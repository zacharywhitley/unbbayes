package unbbayes.gui.continuous;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;

import unbbayes.util.SortUtil;

public class ContinuousNormalDistributionPane extends JPanel {

	private static final long serialVersionUID = 1L;
	
	private String followsTitle;
	
	private JPanel followsPane;
	private JLabel followsLabel;
	
	private List<String> discreteParentNodeNameList;
	private List<String> continuousParentNodeNameList;
	
	private JPanel parentStateListPane;
	private List<JList> discreteParentNodeStateSelectionList;
	
	public ContinuousNormalDistributionPane(String followsTitle, List<String> discreteParentNodeNameList, List<String> continuousParentNodeNameList) {
		this.followsTitle = followsTitle;
		createFollowsPane();
		this.discreteParentNodeNameList = discreteParentNodeNameList;
		this.continuousParentNodeNameList = continuousParentNodeNameList;
		SortUtil.sort(this.discreteParentNodeNameList);
		SortUtil.sort(this.continuousParentNodeNameList);
		createParentStateListPane();
	}
	
	public void createFollowsPane() {
		followsPane = new JPanel();
		followsLabel = new JLabel(followsTitle);
		followsLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
		followsLabel.setForeground(Color.BLUE);
		followsPane.add(followsLabel);
	}
	
	public void setFollowsTitle(String followsTitle) {
		this.followsTitle = followsTitle;
		followsLabel.setText(this.followsTitle);
	}
	
	private void createParentStateListPane() {
		parentStateListPane = new JPanel();
		discreteParentNodeStateSelectionList = new ArrayList<JList>(discreteParentNodeNameList.size());
		JLabel label;
		JList list;
		for (String name : discreteParentNodeNameList) {
			label = new JLabel(name + " = ");
			list = new JList();
			discreteParentNodeStateSelectionList.add(list);
			parentStateListPane.add(label);
			parentStateListPane.add(list);
			parentStateListPane.add(new JLabel("\t"));
		}
		parentStateListPane.add(new JLabel("-\t"));
		for (String name : continuousParentNodeNameList) {
			parentStateListPane.add(new JLabel(name + "\t"));
		}
	}
	
	public void fillDiscreteParentStateSelection(String parentName, List<String> stateList) {
		JList list = null;
		for (int i = 0; i < discreteParentNodeNameList.size(); i++) {
			if (discreteParentNodeNameList.get(i).equals(parentName)) {
				list = discreteParentNodeStateSelectionList.get(i);
				break;
			}
		}
		if (list != null) {
			// TODO DEFAULTLISTMODEL HERE!
		}
	}

	public void setDiscreteAndContinuousParentNodeNameList(
			List<String> discreteParentNodeNameList, List<String> continuousParentNodeNameList) {
		this.discreteParentNodeNameList = discreteParentNodeNameList;
		this.continuousParentNodeNameList = continuousParentNodeNameList;
		SortUtil.sort(this.discreteParentNodeNameList);
		SortUtil.sort(this.continuousParentNodeNameList);
		createParentStateListPane();
	}
	
	public void setDiscreteParentNodeNameList(
			List<String> discreteParentNodeNameList) {
		this.discreteParentNodeNameList = discreteParentNodeNameList;
		SortUtil.sort(this.discreteParentNodeNameList);
		createParentStateListPane();
	}

	public void setContinuousParentNodeNameList(
			List<String> continuousParentNodeNameList) {
		this.continuousParentNodeNameList = continuousParentNodeNameList;
		SortUtil.sort(this.continuousParentNodeNameList);
		createParentStateListPane();
	}

}
