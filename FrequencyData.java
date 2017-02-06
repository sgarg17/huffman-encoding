/**
 * Data type used in compressor class
 *
 * @author Shelley Garg, Dartmouth CS 10, Winter 2015
 */
public class FrequencyData {
	private Character character; 
	private Integer frequency;
	
	public FrequencyData(Character character, Integer frequency) {
		this.character = character;
		this.frequency = frequency;
	}
	
	
	public Character getCharacter(){
		return character;
	}
	
	public Integer getFrequency(){
		return frequency;
	}
	
	
	public String toString(){
		return character + " : " + frequency;
	}
}
