import java.io.FileNotFoundException; 
import java.io.FileReader;
import java.util.*;
import java.io.IOException; 

public class Nabanagrams {
	static Set<String> validWords;
	static List<String> lettersLeft; //TODO: store as distribution array
	static List<String> freeLetters;
	static List<String> words;
	static int maxWordLength;
	static final int minWordLength = 3;
	static final int[] letterDistribution = {13, 3, 3, 6, 18, 3, 4, 3, 12, 2, 2, 5, 3, 8, 11, 3, 2, 9, 6, 9, 6, 3, 3, 2, 3, 2};
	static Map<String, String> wordMap;
	
	public static void main(String[] args) {
		System.out.println();
		System.out.println("---------------------------");	
		System.out.println("---N-A-B-A-N-A-G-R-A-M-S---");
		System.out.println("---------------------------");
		System.out.println();

		setup("dictionary.csv");
		while (true) {
			newGame();
		}
	}
	
	// One-time setup: reads in dictionary file and creates sorted word -> word mapping
	private static void setup(String dictionaryFilename) {
		System.out.println("Reading in dictionary file.....");
		Scanner s = null;
		try {
			s = new Scanner(new FileReader(dictionaryFilename));
		} catch (Exception E) {
			System.out.println("Could not read " + dictionaryFilename);
			System.exit(1);
		}
		// Create mapping from sorted words to words
		validWords = new HashSet<String>();
		wordMap = new HashMap<String, String>();
		maxWordLength = 0;
		while (s.hasNextLine()) {
			String word = s.nextLine().toUpperCase();
			if (word.length() >= minWordLength) {
				validWords.add(word);
				char[] chars = word.toCharArray();
				Arrays.sort(chars);
				String sortedWord = new String(chars);
				// May overwrite previous value, but that's ok
				wordMap.put(sortedWord, word);  
				if (word.length() > maxWordLength) {
					maxWordLength = word.length();
				}
			}
		}
	}
	
	// Starts a new game, returns when game ends
	private static void newGame() {
		System.out.println("Starting a new game...");
		System.out.println();
		
		// Setup game state for new game (TODO: make GameState object)
		lettersLeft = new LinkedList<String>();
		for (int i = 0; i < letterDistribution.length; i++){
			for (int j = 0; j < letterDistribution[i]; j++) {
				lettersLeft.add(Character.toString((char)(65 + i)));
			}
		}
		freeLetters = new ArrayList<String>();
		words = new ArrayList<String>();
		
		//User input loop
		Scanner s = new Scanner(System.in);
		while (true) {
			printGameState();	
			String input = s.nextLine();
			flipLetter(input);
			Combo newCombo = findCombo();
			while(newCombo != null) {
				applyCombo(newCombo);
				newCombo = findCombo();
			}
			System.out.println("Nothing found!");
			if (lettersLeft.size() == 0) {
				printGameState();
				System.out.println("Game over! Play again? (Y/N)");
				if (s.nextLine().toUpperCase().equals("Y")) {
					return;
				} else {
					System.exit(0);
				}
			}
		}
	}
	
	// Randomly removes a letter from lettersLeft and adds it to freeLetters	
	private static void flipLetter(String input) {
		String letter = null;
		if (input.equals("")) {
			int index = (int) Math.floor(Math.random() * lettersLeft.size());		
			letter = lettersLeft.get(index);
			lettersLeft.remove(index);
		} else {
			letter = input.toUpperCase();
		}
		freeLetters.add(letter);
		Collections.sort(freeLetters);
		System.out.println("New letter: " + letter);	
	}

	// Finds a valid combo from words and freeLetters, or null if there is none
	private static Combo findCombo() {
		List<String> strings = new ArrayList<String>();
		// TODO: give priority to new word letters?
		for (String letter : freeLetters) {
			strings.add(letter);
		}
		for (String word : words) {
			strings.add(word);
		}
		
		ComboFinder cf = new ComboFinder(strings, minWordLength, maxWordLength);
		while (cf.hasNext()) {
			Combo combo = cf.next();
			if (wordMap.get(combo.sortedWord) != null) {
				return combo;
			}
		}
		return null;
	}
	
	// Applies the given combo to the game state (removes the components and adds the new word)
	private static boolean applyCombo(Combo combo) {
		System.out.print("I found a word! ");
		for (String str : combo.strings) {
			System.out.print(str + " ");
			if (str.length() == 1) {
				int index = Collections.binarySearch(freeLetters, str);
				freeLetters.remove(index);
			} else {
				int index = Collections.binarySearch(words, str);
				words.remove(index);
			}
		}
		String newWord = wordMap.get(combo.sortedWord);
		System.out.println("---> " + newWord);
		words.add(newWord);
		Collections.sort(words);
		return true;
	}

	// Prints the current state of the game and prompts the user to flip a letter or add a word or string
	private static void printGameState() {
                System.out.println();
                System.out.print("Words: \t");
                for (String word : words) {
                        System.out.print(word + " ");
                }
                System.out.println();
                System.out.print("Free letters: \t ");
                for (String letter : freeLetters) {
                        System.out.print(letter + " ");
                }
		System.out.println();
		System.out.println("(Letters left: " + lettersLeft.size() + ")");
                System.out.println();
                System.out.println("Press 'enter' to flip a new letter, or enter a letter or word to add it");
                System.out.println();
        }
}

public class Combo {
	public List<String> strings;
	public String sortedWord;

	public Combo(List<String> strings) {
		this.strings = strings;
		StringBuilder sb = new StringBuilder();
                for (String str : strings) {
                        sb.append(str);
                }
                char[] chars = sb.toString().toCharArray();
                Arrays.sort(chars);
                sortedWord = new String(chars);
	}
}

public class ComboFinder implements Iterator<Combo> {
	int minLength;
	int maxLength;
	List<String> strings;
	List<Integer> next;
	int numLetters;
	int nextElement;
	// TODO: use a stack of strings, and a stack of ints to represent their indexes (?)
	Stack<Integer> s;
	
	public ComboFinder(List<String> strings, int minLength, int maxLength) {
		this.strings = strings;
		this.minLength = minLength;
		this.maxLength = maxLength;
		numLetters = 0;
		nextElement = 0;
		if (strings.size() >= 2) {
			next = new LinkedList<Integer>();
			prepareNext();
		} else {
			next = null;
		}
	}

	public boolean hasNext() {
		return next != null;
	}

	public Combo next() {
		//Convert the list of indexes into an actual list of strings.
		List<String> list = new ArrayList<String>(next.size());
		for (Integer i : next) {
			list.add(strings.get(i));
		}
		Combo result = new Combo(list);
		prepareNext();
		return result;
	}

	private void prepareNext() {
		if (next == null) {
			return;
		}
		do {
			if (numLetters >= maxLength || nextElement >= strings.size()) {
				if (next.get(next.size() - 1) == strings.size() - 1) {		//while the last element is the last possible string
					numLetters -= strings.get(strings.size() - 1).length();		// lower the 
					next.remove(next.size() - 1);				// remove the element
				}
				
				// We've gone through every combo
				if (next.size() == 0) {
					next = null;
					return;
				}
				
				// Increment the last element in the combo, update numLetters accordingly
				int lastIndex = next.size() - 1;
				int oldLength = strings.get(next.get(lastIndex)).length();
				nextElement = next.get(lastIndex) + 1;
				next.set(lastIndex, nextElement); //increment last element
				int newLength = strings.get(nextElement).length();
				numLetters += (newLength - oldLength);
				nextElement++;
			} else {
				// We can add the nextElement to 
				next.add(nextElement);
				numLetters += strings.get(nextElement).length();
				nextElement++;
			}
		} while (next.size() <= 1 || numLetters > maxLength || numLetters < minLength);
	}
}


