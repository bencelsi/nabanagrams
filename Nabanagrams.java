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
	
	public static void main(String[] args) {
		System.out.println("---------------------------");	
		System.out.println("---N-A-B-A-N-A-G-R-A-M-S---");
		System.out.println("---------------------------");
		
		setup("dictionary.csv");
		while (true) {
			newGame();
			System.out.println("Game over! Thanks for playing");
		}
	}

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
		while (s.hasNextLine()) {
			String word = s.nextLine().toUpperCase();
			if (word.length() > 2) {
				words.add(word);
				char[] chars = word.toCharArray();
				Arrays.sort(chars);
				String sortedWord = new String(chars);
				// May overwrite previous value, but that's ok
				wordMap.put(sortedWord, word);  
			}
		}
	}
	
	
	public static void newGame() {
		System.out.println("Starting a new game...");
		System.out.println();
		lettersLeft = new LinkedList<String>();
		for (int i = 0; i < letterDistribution.length; i++){
			for (int j = 0; j < letterDistribution[i]; j++) {
				lettersLeft.add(Character.toString((char)(65 + i)));
			}
		}
		freeLetters = new ArrayList<String>();
		yourWords = new ArrayList<String>();
		myWords = new ArrayList<String>();
		
		Scanner s = new Scanner(System.in);
		while (true) {
			printGameState();	
			String input = s.nextLine();
			if (input.equals("")) {
				flipLetter();
			}
			String newWord = findWord();
		
			while(newWord != null) {
				System.out.println("I found a word! " + newWord);
				newWord = findWord();
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

	public static String findWord() {
		System.out.println("Collecting strings.....");
		List<String> strings = new ArrayList<String>();
		for (String word : myWords) {
			strings.add(word);
		}
		for (String word : yourWords) {
			strings.add(word);
		}
		for (String letter : freeLetters) {
			strings.add(letter);
		}
		
		System.out.println("Generating permutations.....");
		List<List<String>> permutations = new ArrayList<List<String>>();
		generatePermutations(strings, permutations);

		System.out.println("Looking for match.....");
		for (List<String> permutation : permutations) {			
			String sortedPermutation = concatenateSort(permutation);
			String newWord = wordMap.get(sortedPermutation);
			if (newWord != null) {	
				for (String string : permutation) {
					if (string.length() == 1) {
						int index = Collections.binarySearch(freeLetters, string);
						freeLetters.remove(index);
					} else {
						int index = Collections.binarySearch(myWords, string);
						myWords.remove(index);
					}
				}
				myWords.add(newWord);
				Collections.sort(myWords);
				return newWord;
			};
		}
		return null;
	}

	// Stores all possible permutations of given list of strings into result
	private static void generatePermutations(List<String> strings, List<List<String>> result) {
		List<String> allStrings = new ArrayList<String>();		
		if (strings.size() <= 1) {
			return;
		}
		result.add(strings);
		Set<String> hasBeenRemoved = new HashSet<String>();
		for (int i = 0; i < strings.size(); i++) {
			String temp = strings.get(i);
			if (!hasBeenRemoved.contains(temp)) {
				strings.remove(i);
				generatePermutations(strings, result);
				strings.add(i, temp); 
				allStrings.add(temp);
				hasBeenRemoved.add(temp);
			}
		};
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
