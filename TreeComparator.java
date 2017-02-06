import java.util.Comparator;
/**
 * Class that allows for comparing of FrequencyData objects
 *
 * @author Shelley Garg, Dartmouth CS 10, Winter 2015
 */
public class TreeComparator implements Comparator<BinaryTree<FrequencyData>> {

	public TreeComparator() {
	}
	
	public int compare(BinaryTree<FrequencyData> f1, BinaryTree<FrequencyData> f2) {
		
		if (f1.data.getFrequency() > f2.data.getFrequency()){
			return 1;
		}
		else if (f1.data.getFrequency() < f2.data.getFrequency()){
			return -1;
		}
		return 0;
	}
}
