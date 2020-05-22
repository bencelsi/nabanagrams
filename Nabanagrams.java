import java.io.FileNotFoundException; 
import java.io.FileReader;
import java.io.IOException;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.Stack;


// TODO - don't ask to take turn if  -  2 or fewer letters or only 1 word on board
// say 'too short' not 'not valid' is valid 2 letter
// don't take first option, take best option

// TERMINOLOGY - 
// Chunk: 	a group of tiles that can't be broken up. words and free tiles are both 'chunks' 
// Combo: 	a group of at least 2 chunks.  Some combos can be stolen to form a new word

public class Nabanagrams {
	private static Set<String> dictionary;
	private static int maxWordLength;
	private static final int minWordLength = 3;
	private static Map<String, String> wordMap;
	private static GameState game;
	private static final String dictionaryFile = "dictionary.csv";
	
	public static void main(String[] args) {
		System.out.println("\n---------------------------");
		System.out.println("---N-A-B-A-N-A-G-R-A-M-S---");
		System.out.println("---------------------------\n");
		setup();
		Scanner s = new Scanner(System.in);
		while (true) {
			if (game != null && !game.isOver) {
				System.out.println("Press enter to resume your game");
			}
			System.out.println("+ to start a new game");
			System.out.println("* to start a new game in sandbox mode");
			System.out.println("! to quit\n");
			String input = s.nextLine();
			if (input.equals("+")) {
				game = new GameState(false);
				enterGame(game, s);
			} else if (input.equals("*")) {
				game = new GameState(true);
				enterSandbox(game, s);
			} else if (input.equals("!")) {
				System.exit(0);
			} else if (input.equals("") && game != null && !game.isOver) {
				if (game.isSandbox) {
					enterSandbox(game, s);
				} else {
					enterGame(game, s);
				}
			}
		}
	}

	// One-time setup: reads in dictionary file and creates sorted word -> word mapping
	private static void setup() {
		Scanner s = null;
		try {
			s = new Scanner(new FileReader(dictionaryFile));
		} catch (Exception e) {
			System.out.println("Could not find file " + dictionaryFile);
			System.exit(1);	
		}

		// Create mapping from sorted words to words
		dictionary = new HashSet<String>();
		wordMap = new HashMap<String, String>();
		maxWordLength = 0;
		while (s.hasNextLine()) {
			String word = s.nextLine().toUpperCase();
			if (word.length() >= minWordLength) {
				dictionary.add(word);
				char[] chars = word.toCharArray();
				Arrays.sort(chars);
				wordMap.put(new String(chars), word);  
				if (word.length() > maxWordLength) {
					maxWordLength = word.length();
				}
			}
		}
	}

	// Starts a new game, returns when game ends
	private static void enterGame(GameState game, Scanner s) {
		while (true) {
			game.print();
			if (game.myTurn) {
				System.out.print("Press 'enter' to let me take a turn, ");
			} else if (game.lettersLeft() > 0) {
				System.out.print("Press 'enter' to flip a new letter, ");
			}
			System.out.println("or enter a word to steal it");
			System.out.println("? for more options\n");
			String input = s.nextLine().toUpperCase();
			System.out.println("--------------------------------------\n");

			if (input.equals("")) {
				if (game.myTurn) {
					List<String> newCombo = findCombo(game, null);
					if (newCombo == null) {
						System.out.println("I can't find anything!");
					} else while (newCombo != null) {
						String newWord = wordMap.get(sort(newCombo));
						game.stealWord(newCombo, newWord, true);
						newCombo = findCombo(game, null);
					}
					if (game.lettersLeft() == 0) {
						game.end();
						return;
					}
				} else if (game.lettersLeft() > 0) {
					game.flipLetter();
				}
				game.myTurn = !game.myTurn;
			} else if (input.equals("?")) {
				return;
			} else {
				List<String> newCombo = findCombo(game, input);
				if (newCombo != null) {
					game.stealWord(newCombo, input, false);
				}
			}
		}
	}

	private static void enterSandbox(GameState game, Scanner s){
		while (true) {
			game.print();
			System.out.println("Enter words or individual letters to add them to the table");
			System.out.println("Press enter to let me try to find new words");
			System.out.println("? for more options\n");
			String input = s.nextLine().toUpperCase();
			if (input.equals("")) {
				List<String> newCombo = findCombo(game, null);
				if (newCombo == null) {
					System.out.println("I can't find anything!");
				} else while (newCombo != null) {
					String newWord = wordMap.get(sort(newCombo));
					game.stealWord(newCombo, newWord, true);
					newCombo = findCombo(game, null);
				}
			} else if (input.equals("?")) {
				return;
			} else {
				game.addLetters(input);
			}
		}
	}

	// Finds a valid combo from words and freeLetters, or returns null if there is none
	public static List<String> findCombo(GameState game, String goal) {
		String sortedGoal = null;
		if (goal != null) {
			if (!dictionary.contains(goal)) {
				System.out.println(goal + " is not a valid word");
				return null;
			}
			sortedGoal = sort(new ArrayList<String>(Arrays.asList(goal)));
		}

		List<String> chunks = new ArrayList<String>();
		for (String letter : game.freeLetters) {
			chunks.add(letter);
		}
		for (String word : game.yourWords) {
			chunks.add(word);
		}
		for (String word : game.myWords) { 
			chunks.add(word);
		}
		
		ComboIterator comboIterator = new ComboIterator(chunks, minWordLength, maxWordLength);
		while (comboIterator.hasNext()) {
			List<String> combo = comboIterator.next();
			String sortedCombo = sort(combo);
			if ((goal == null && wordMap.containsKey(sortedCombo)) || (goal != null && sortedGoal.equals(sortedCombo))) {
				return combo;
			}
		}
		if (goal != null) {
			System.out.println("There is no way you can steal " + goal);
		}
		return null;
	}

	private static String sort(List<String> chunks) {
		StringBuilder sb = new StringBuilder();
		for (String chunk : chunks) {
			sb.append(chunk);
		}
		char[] chars = sb.toString().toCharArray();
		Arrays.sort(chars);
		return new String(chars);
	}
}


public class GameState {
	public List<String> myWords = new ArrayList<String>();
	public List<String> yourWords = new ArrayList<String>();
	public List<String> freeLetters = new ArrayList<String>();
	public List<String> lettersLeft = new LinkedList<String>();
	public int myPoints = 0;
	public int yourPoints = 0;
	public boolean myTurn  = false;
	public boolean isOver = false;
	public final boolean isSandbox;
	private final int[] letterDistribution = {13, 3, 3, 6, 18, 3, 4, 3, 12, 2, 2, 5, 3, 8, 11, 3, 2, 9, 6, 9, 6, 3, 3, 2, 3, 2};
	
	public GameState(boolean isSandbox) {
		this.isSandbox = isSandbox;
		if (!isSandbox) { 
			for (int i = 0; i < letterDistribution.length; i++) {
				for (int j = 0; j < letterDistribution[i]; j++) {
					lettersLeft.add(Character.toString((char)(65 + i)));
				}
			}
		}
	}
	
	public void flipLetter() {
		int index = (int) Math.floor(Math.random() * lettersLeft());
		String letter = lettersLeft.get(index);
		lettersLeft.remove(index);
		freeLetters.add(letter);
		Collections.sort(freeLetters);
		System.out.println("New letter:\t" + letter);
	}

	public int lettersLeft() {
		return lettersLeft.size();
	}

	public void print() {
		printList("\nFree letters", freeLetters);
		System.out.println("(" + lettersLeft() + " left)\n");
		if (isSandbox) {
			printList("Words (" + myPoints + " pts)", myWords);	
		} else {
			printList("Your words (" + yourPoints + " pts)", yourWords);
			printList("My words (" + myPoints + " pts)" , myWords);
		}
		System.out.println();
	}

	public void stealWord(List<String> combo, String newWord, boolean mine) {
		for (String chunk : combo) {
			System.out.print(chunk + " ");
			if (chunk.length() == 1) {
				int index = Collections.binarySearch(freeLetters, chunk);
				freeLetters.remove(index);
			} else {
				int index = Collections.binarySearch(yourWords, chunk);
				if (index >= 0) {
					yourPoints -= (yourWords.get(index).length() - 2);
					yourWords.remove(index);
				} else {
					index = Collections.binarySearch(myWords, chunk);
					myPoints -= (myWords.get(index).length() - 2);
					myWords.remove(index);
				}
			}
		}
		System.out.println("---> " + newWord);
		if (mine) {
			myWords.add(newWord);
			myPoints += (newWord.length() - 2);
			Collections.sort(myWords);
		} else {
			yourWords.add(newWord);
			yourPoints += (newWord.length() - 2);
			Collections.sort(yourWords);
		}
	}
	
	public void addLetters(String str) {
		if (!str.matches("^[a-zA-Z ]+")) {
			System.out.println("Input must contain only letters");
			return;
		}
		String[] strs = str.split("\\s+");
		for (String s : strs) {
			if (s.length() == 1) {
				freeLetters.add(s);
			} else {
				myWords.add(s);
			}
		}
		Collections.sort(myWords);
		Collections.sort(freeLetters);
	}

	public void end() {
		System.out.println("\n--------------------------------------");
		System.out.println("--------------------------------------");
		System.out.println("GAME OVER!");
		if (myPoints == yourPoints) {
			System.out.println("It's a tie! We must grudgingly acknowledge that once again our skills have proven equal to each other's.");
		} else if (myPoints > yourPoints) {
			System.out.println("I win! You must grudgingly acknowledge that once again my skills have proven superior to yours.");
		} else {
			System.out.println("You win! I must grudgingly acknowledge that once again your skills have proven superior to mine.");

		}
		System.out.println("--------------------------------------");
		System.out.println("--------------------------------------\n");
		isOver = true;
	}

	private void printList(String label, List<String> list){
		System.out.print(label + ":\t");
		if (list.size() == 0) {
			System.out.print("(none)");
		} else {
			System.out.print(String.join(" ", list));
		}
		System.out.println();
	}
}


class ComboIterator implements Iterator<List<String>> {
	private List<String> chunks; 		// The given words and free letters to consider
	private int minWordLength;			// the shortest a word can be
	private int maxWordLength;			// the longest a word can be (based on the dictionary)
	private int comboLetters = 0;		// the number of letters in comboStack
	private int chunkToAdd = 0;			// the index of the next chunk to add to comboStack
	private Stack<Integer> comboStack;	// Stack of indexes that represent chunks in the next combo to return
	
	public ComboIterator(List<String> chunks, int minWordLength, int maxWordLength) {
		this.chunks = chunks;
		this.minWordLength = minWordLength;
		this.maxWordLength = maxWordLength;
		this.comboStack = new Stack<Integer>();
		Collections.sort(chunks, Comparator.comparing(a -> a.length()));
		prepareNext();
	}

	public boolean hasNext() {
		return comboStack != null;
	}

	//returns the next vaild combo represented by comboStack
	public List<String> next() {
		Stack<Integer> comboStackClone = (Stack<Integer>) comboStack.clone();
		List<String> result = new ArrayList<String>();
		while (!comboStackClone.isEmpty()) {
			result.add(chunks.get(comboStackClone.pop()));
		}
		prepareNext();
		return result;
	}

	// change comboStack to next valid combo
	private void prepareNext() {
		while (comboStack != null) {
			if (chunkToAdd < chunks.size() && comboLetters + chunks.get(chunkToAdd).length() <= maxWordLength) { // if we can add chunkToAdd, add it
				comboStack.push(chunkToAdd);
				comboLetters += chunks.get(chunkToAdd).length();
				chunkToAdd++;
				if (comboLetters >= minWordLength && comboStack.size() > 1) { // if the combo is long enough, it is valid so return
					return;
				}
			} else if (comboStack.isEmpty()) { // we can't add a chunk and the combo is empty, there are no more possible combos
				comboStack = null;
			} else { // we can't add chunkToAdd, so pop the last chunk and set chunkToAdd to its successor (incrementing it)
				int poppedChunk = comboStack.pop();
				comboLetters -= chunks.get(poppedChunk).length();
				chunkToAdd = poppedChunk + 1;
			}
		}	
	}
}