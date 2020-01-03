import java.io.FileNotFoundException; 
import java.io.FileReader;
import java.util.*;
import java.io.IOException; 

public class Nabanagrams {
	static Set<String> words = new HashSet<String>();
	static Map<String, String> wordMap = new HashMap<String, String>();
	static List<String> lettersLeft; //TODO: store as distribution array
	static List<String> freeLetters;
	static List<String> yourWords;
	static List<String> myWords;
	static final int[] letterDistribution = {13, 3, 3, 6, 18, 3, 4, 3, 12, 2, 2, 5, 3, 8, 11, 3, 2, 9, 6, 9, 6, 3, 3, 2, 3, 2};
	static int maxWordLength;

	public static void main(String[] args) {
		System.out.println();
		System.out.println("---------------------------");	
		System.out.println("---N-A-B-A-N-A-G-R-A-M-S---");
		System.out.println("---------------------------");
		System.out.println();

		setup("dictionary.csv");
		while (true) {
			newGame();
			System.out.println("Game over! Thanks for playing");
		}
	}
	
	//One-time setup: reads in dictionary file and creates sorted word -> word mapping
	public static void setup(String dictionaryFilename) {
		System.out.println("Reading in dictionary file.....");
		Scanner s = null;
		try {
			s = new Scanner(new FileReader(dictionaryFilename));
		} catch (Exception E) {
			System.out.println("Could not read " + dictionaryFilename);
			System.exit(1);
		}
		// Create mapping from sorted words to words
		words = new HashSet<String>();
		wordMap = new HashMap<String, String>();
		maxWordLength = 0;
		while (s.hasNextLine()) {
			String word = s.nextLine().toUpperCase();
			if (word.length() > 2) {
				words.add(word);
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
	
	//Starts a new game, returns when game ends
	public static void newGame() {
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
		yourWords = new ArrayList<String>();
		myWords = new ArrayList<String>();
		
		//User input loop
		Scanner s = new Scanner(System.in);
		while (true) {
			printGameState();	
			String input = s.nextLine();
			flipLetter();
			List<String> newCombo = findCombo();
			while(newCombo != null) {
				applyCombo(newCombo);
				newCombo = findCombo();
			}
			System.out.println("Nothing found!");
			if (lettersLeft.size() == 0) {
				return;
			}
		}
	}
	
	public static void flipLetter() {
		int index = (int) Math.floor(Math.random() * lettersLeft.size());		
		String letter = lettersLeft.get(index);
		lettersLeft.remove(index);
		freeLetters.add(letter);
		Collections.sort(freeLetters);
		System.out.println("New letter: " + letter);	
	}

	public static List<String> findCombo() {
		//System.out.print("Collecting strings... ");
		List<String> strings = new ArrayList<String>();
		for (String letter : freeLetters) {
			strings.add(letter);
		}
		for (String word : myWords) {
			strings.add(word);
		}
		for (String word : yourWords) {
			strings.add(word);
		}
		
		ComboFinder cf = new ComboFinder(strings, 3, maxWordLength);
		while (cf.hasNext()) {
			List<String> combo = cf.next();
			if (combo.size() >= 2 && checkCombo(combo)) {
				return combo;
			}
		}	
		return null;
	}

	private static boolean checkCombo(List<String> combo) {
		String sortedPermutation = concatenateSort(combo);	
		String newWord = wordMap.get(sortedPermutation);
		if (newWord != null) {
			return true;
		}
		return false;
	}

	public static boolean applyCombo(List<String> combo) {
		for (String str : combo) {		
			if (str.length() == 1) {
				int index = Collections.binarySearch(freeLetters, str);
				freeLetters.remove(index);
			} else {
				int index = Collections.binarySearch(myWords, str);
				myWords.remove(index);
			}
		}	
		String sortedWord = concatenateSort(combo);
		String newWord = wordMap.get(sortedWord);
		myWords.add(newWord);
		Collections.sort(myWords);
		System.out.print("I found a word! ");
		for (String str : combo) {
			System.out.print(str + " ");
		}
		System.out.println("---> " + newWord);
		return true;
		
	}

	// Stores all possible permutations of given list of strings into result
	private static void generatePermutations(List<String> strings, List<List<String>> result) {
		if (strings.size() <= 1) {
			return;
		}
		List<String> allStrings = new ArrayList<String>();		
		result.add(strings);
		Set<String> hasBeenRemoved = new HashSet<String>();
		for (int i = 0; i < strings.size(); i++) {
			String temp = strings.get(i);
			allStrings.add(strings.get(i));
			if (!hasBeenRemoved.contains(temp)) {
				strings.remove(i);
				generatePermutations(strings, result);
				strings.add(i, temp);
				hasBeenRemoved.add(temp);
			}
		}
		
		result.add(allStrings);
	}

	private static String concatenateSort(List<String> strings) {
		StringBuilder builder = new StringBuilder();
		for (String string : strings) {
			builder.append(string);
		}
		char[] chars = builder.toString().toCharArray();
		Arrays.sort(chars);
		return new String(chars);
	}

	public static void printGameState() {
                System.out.println();
		System.out.print("Your words: \t");
                for (String word : yourWords) {
                        System.out.print(word + " ");
                }
                System.out.println();
                System.out.print("My words: \t");
                for (String word : myWords) {
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
                System.out.println("Type a word to steal, or press 'enter' to flip a new letter");
                System.out.println();
        }
}


//TODO: create NewCombo object - with 


public class ComboFinder implements Iterator<List<String>> {
	List<String> strings;
	List<Integer> next;
	int numLetters;
	int nextElement;
	int minLength;
	int maxLength;

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

	public List<String> next() {
		List<String> result = new ArrayList<String>(next.size());
		for (Integer i : next) {
			result.add(strings.get(i));
		}
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
					numLetters -= strings.get(strings.size() - 1).length();		// lower
					next.remove(next.size() - 1);				// remove the element
				}
				if (next.size() == 0) {
					next = null;
					return;
				}

				int lastIndex = next.size() - 1;
				int oldLength = strings.get(next.get(lastIndex)).length();
				nextElement = next.get(lastIndex) + 1;
				next.set(lastIndex, nextElement); //increment last element
				int newLength = strings.get(nextElement).length();
				numLetters += (newLength - oldLength);
				nextElement++;
			} else {						
				next.add(nextElement);
				numLetters += strings.get(nextElement).length();
				nextElement++;
			}
		} while (next.size() <= 1 || numLetters > maxLength || numLetters < minLength);
	}
}
