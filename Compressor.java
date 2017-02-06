import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Comparator;

import javax.swing.JFileChooser;
/**
 * Class that allows compressing and decompressing of a file
 *
 * @author Shelley Garg, Dartmouth CS 10, Winter 2015
 */
public class Compressor {
	// maps characters to their corresponding frequencies
	private Map<Character, Integer> freqTable; 
	
	// tree holding characters and their corresponding frequencies ordered by frequency
	private static BinaryTree<FrequencyData> huffman;
	
	// holds each character and corresponding code
	private Map<Character, String> codeMap;
	
	//files that will be read 
	private String in_file, out_compressed_file;
	
	public Compressor(String file) {
		in_file = file;
	}
	
	/**
	 * fills in map with character/frequency data
	 */
	public void makeFrequencyTable() throws IOException{
		freqTable = new TreeMap<Character, Integer>();	
		//initialize reader to read in from given file name 
		BufferedReader input = new BufferedReader(new FileReader(in_file));
		try{
			//read integers from file and fill frequency table
			int nextCharInt;
			while (( nextCharInt = input.read()) !=-1){
			    Character nextChar = (char)nextCharInt; //cast integers to characters
			    //if character is already in table increment count
				if (freqTable.containsKey(nextChar)){
				    freqTable.put(nextChar,freqTable.get(nextChar)+1);
			    }
				//otherwise add key to map
			    else{
				    freqTable.put(nextChar, 1);
			    }
			}
		}
		finally{
			input.close();
		}
	}
	
	/**
	 * creates a Huffman tree by combining frequency data into a single tree utilizing a priority queue
	 */
	public void makeHuffman(){
		//new priority queue that uses a tree comparator to compare nodes if original file is not blank
		if (freqTable.size()!=0){
			PriorityQueue<BinaryTree<FrequencyData>> freqque = new PriorityQueue<BinaryTree<FrequencyData>>(freqTable.size(), new TreeComparator());
			
			for (Character nextchar : freqTable.keySet()){ //loop through each char in map
				FrequencyData fq = new FrequencyData(nextchar, freqTable.get(nextchar)); //combine the data into one object
				BinaryTree<FrequencyData> bt = new BinaryTree<FrequencyData>(fq); // make a singleton tree
				freqque.add(bt); // add new tree to priority queue	
			}
				
			//loop through the queue and combine singleton trees into a single tree
			while(freqque.size() > 1){
				BinaryTree<FrequencyData> t1 = freqque.remove();
				BinaryTree<FrequencyData> t2 = freqque.remove();
				FrequencyData freqtotal= new FrequencyData(null,(t1.data).getFrequency() + (t2.data).getFrequency());
				BinaryTree<FrequencyData> r = new BinaryTree<FrequencyData>(freqtotal, t1, t2);
				freqque.add(r);
			}
				
			huffman = freqque.remove();//save last element in queue as huffman
		}
		//if file is blank, the huffman is set as empty
		else{
			huffman = null;
		}
		
	}
	
	/**
	 * recurses through the huffman tree to create a unique code for each character
	 * maps the code to each character in a new map
	 */
	public void addToCodeMap(BinaryTree<FrequencyData> tree, String code){
		//if leaf save the character/frequency data it holds
		if (tree.isLeaf()){
			codeMap.put(((FrequencyData) tree.data).getCharacter(), code);
		}
		
		//otherwise loop through and add to code depending on whether or not there is a child in the given direction
		else{
			if (tree.hasLeft()){
				addToCodeMap(tree.getLeft(), code + '0');
			}
			if (tree.hasRight()){
				addToCodeMap(tree.getRight(), code + '1');
			}
		}
		
	}
	
	/**
	 * reads in a text file and writes out as bits to a new file 
	 */
	public void compress() throws IOException{
		//run methods that compile a map with frequencies and then create a tree of character/frequency data
		makeFrequencyTable();
		makeHuffman();
		
		//if the huffman tree is empty, file must be blank
		if(huffman == null){
			System.err.println("File is blank, nothing to compress.");
		}
		
		//if there is more than one type of character in the file, create a code map
		else if(huffman.size()>1){
			codeMap = new TreeMap<Character, String>();	
			addToCodeMap(huffman, "");
		}
		
		//if only one node in huffman tree-- only one character in file
		else if (huffman.size()==1){
			//assign unique code to single character
			codeMap = new TreeMap<Character, String>();	
			codeMap.put(huffman.getData().getCharacter(), "0");
		}
		
		//create name of new file to be written out to
		out_compressed_file = in_file.substring(0, in_file.length()-4) + "_compressed.txt";
		//initialize writer and reader
		BufferedReader input = new BufferedReader(new FileReader(in_file));
		BufferedBitWriter bitOutput = new BufferedBitWriter(out_compressed_file);
		
		try{
			//read in from file character by character, get code and write code bit by bit out to new file
			int nextCharInt;
			while ((nextCharInt = input.read()) != -1){
			    
				Character nextChar = (char)nextCharInt; //cast the integer read in to character
				String code = codeMap.get(nextChar); //get the corresponding code and write to new file 
				//loop through string and write out correspondingbits
				for(int i = 0; i<code.length(); i++){
					char charBit = (code.charAt(i));
					int bit = charBit - '0';//convert bit to char
					bitOutput.writeBit(bit);
				}
			}
		}
		
		finally{
			input.close();
			bitOutput.close();
		}
		
	}
	
	/**
	 * reads bit by bit through compressed file and matches corresponding character to a code using the codeMap previously created
	 */
	public void decompress() throws IOException{
		//new file name created
		String out_file = in_file.substring(0, in_file.length()-4) + "_decompressed.txt";
		
		BufferedBitReader bitInput = new BufferedBitReader(out_compressed_file);
		BufferedWriter output = new BufferedWriter(new FileWriter(out_file));
		
		try{
			//create element to iterate through the map and set it equal to root
			BinaryTree<FrequencyData>iterator = huffman;
			
			int nextBit;
			while ((nextBit = bitInput.readBit()) != -1){
				//in case only one node in tree
				if (iterator.isLeaf()){
					output.write(iterator.getData().getCharacter());
		    		iterator = huffman;
				}
				//move left or right depending on bit read in 
				else if (nextBit == 0){
			    	//leaf means get corresponding character and set iterator back to root
			    	if (iterator.getLeft().isLeaf()){
			    		output.write(iterator.getLeft().getData().getCharacter());
			    		iterator = huffman;
			    	//get next character on the left
			    	}else{
			    		iterator = iterator.getLeft();
			    	}
			    }
			    //same as left
			    else if (nextBit == 1){
			    	if (iterator.getRight().isLeaf()){
			    		output.write(iterator.getRight().getData().getCharacter());
			    		iterator = huffman;
			    	}else{
			    		iterator = iterator.getRight();	
			    	}
			    }
			}
			
		}finally{
			bitInput.close();
			output.close();
		}
		
	}
		
	/**
	 * Puts up a fileChooser and gets path name for file to be opened.
	 * Returns an empty string if the user clicks "cancel".
	 * @return path name of the file chosen  
	 */
	public static String getFilePath() {
	    JFileChooser fc = new JFileChooser("."); // start at current directory

	    int returnVal = fc.showOpenDialog(null);
	    if(returnVal == JFileChooser.APPROVE_OPTION) {
	        File file = fc.getSelectedFile();
	        String pathName = file.getAbsolutePath();
	        return pathName;
	    }
	    else {
	        return "";  
	    }
	}

	public static void main(String[] args) {
		Compressor c = new Compressor(getFilePath());
		try{
			c.compress();
		}
		catch(IOException e){
			System.err.println("Cannot Compress:" + e.getMessage());
		}
		try{
			c.decompress();
		}
		catch(IOException e){
			System.err.println("Cannot Decompress:" + e.getMessage());
		}
	}
}
