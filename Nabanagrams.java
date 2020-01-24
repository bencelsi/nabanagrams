import java.io.FileNotFoundException; 

import java.io.FileReader;
import java.util.*;
import java.io.IOException; 

public class Nabanagrams {
	static Set<String> dictionary;
	static int maxWordLength;
	static final int minWordLength = 3;
	static Map<String, String> wordMap;
	static Game game;
	static boolean myTurn = false;
	
	public static void main(String[] args) {
		System.out.println();
		System.out.println("---------------------------");	
		System.out.println("---N-A-B-A-N-A-G-R-A-M-S---");
		System.out.println("---------------------------");
		System.out.println();
		
		setup("dictionary.csv");
		Scanner s = new Scanner(System.in);
		while (true) {
			if (game != null) {
				System.out.println("Press enter to resume your game");
			}
			System.out.println("+ to start a new game");
			System.out.println("- to start a new game in playground mode");
			System.out.println("! to exit");
			System.out.println();
			String input = s.nextLine().toUpperCase();
			if (input.equals("") && game != null) {
				if (game.isPlayground) {
					playgroundLoop(game, s);
				} else {
					gameLoop(game, s);
				}
			} else if (input.equals("+")) {
				game = new Game(false);
				myTurn = false;
				gameLoop(game, s);
			} else if (input.equals("-")) {
				game = new Game(true);
				playgroundLoop(game, s);				
			} else if (input.equals("!")) {
				System.out.println("Bye!");
				System.exit(0);
			}
		}
	}
	
	// One-time setup: reads in dictionary file and creates sorted word -> word mapping
	private static void setup(String dictionaryFilename) {
		Scanner s = null;
		try {
			s = new Scanner(new FileReader(dictionaryFilename));
		} catch (Exception E) {
			System.out.println("Could not find file " + dictionaryFilename);
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
	private static void gameLoop(Game game, Scanner s) {
		while (true) {
			game.printState();
			if (myTurn) {
				System.out.println("Press 'enter' to let me take a turn");
			} else if (game.lettersLeft() > 0) {
				System.out.println("Press 'enter' to flip a new letter");
			}
			System.out.println("Enter a word to steal it");
			System.out.println("? for more options");
			System.out.println();	
			String input = s.nextLine().toUpperCase();
			System.out.println("---------------------------------------");
			System.out.println();
			if (input.equals("")) {
				if (myTurn) {
					List<String> newCombo = findCombo(game, null);
                        		if (newCombo == null) {
                                		System.out.println("I can't find anything!");
                        		} else while (newCombo != null) {
						String newWord = wordMap.get(concatenateSort(newCombo));
						game.applyCombo(newCombo, newWord, true);
                        			newCombo = findCombo(game, null);
					}
					if (game.lettersLeft() == 0) {
						System.out.println("Game over!");
						game.printState();
						game = null;
						System.out.println("--------------------------------------");
						return;
					}
				} else if (game.lettersLeft() > 0) {
					game.flipLetter();
				}
				myTurn = !myTurn;
			} else if (input.equals("?")) {
				return;
			} else {
				List<String> newCombo = findCombo(game, input);
				if (newCombo != null) {
					game.applyCombo(newCombo, input, false);
				}
			}
		}
	}

	private static void playgroundLoop(Game game, Scanner s){
		while (true) {
			game.printState();
			String input = s.nextLine().toUpperCase();
			if (input.equals("")) {
				List<String> newCombo = findCombo(game, null);
        	                if (newCombo == null) {
                	        	System.out.println("I can't find anything!");
                        	} else while (newCombo != null) {
                        	        String newWord = wordMap.get(concatenateSort(newCombo));
                                	game.applyCombo(newCombo, newWord, true);
                       	 		newCombo = findCombo(game, null);
                        	}
			} else if (input.equals("?")) {
				return;
 			} else {
				game.addLetters(input);
			}
		}
	}
	
	// Finds a valid combo from words and freeLetters, or null if there is none
	private static List<String> findCombo(Game game, String goal) {
		String sortedGoal = null;
		if (goal != null) {
			if (!dictionary.contains(goal)) {
				System.out.println(goal + " is not a valid word");
				return null;
			}
			sortedGoal = concatenateSort(new ArrayList<String>(Arrays.asList(goal)));
		}

		List<String> strings = new ArrayList<String>();
		for (String letter : game.freeLetters) {
			strings.add(letter);
		}
		for (String word : game.yourWords) {
			strings.add(word);
		}
		for (String word : game.myWords) {
			strings.add(word);
		}
		
		ComboIterator ci = new ComboIterator(strings, minWordLength, maxWordLength);
		while (ci.hasNext()) {
			List<String> combo = ci.next();
			String sortedCombo = concatenateSort(combo);
			if ((goal == null && wordMap.get(sortedCombo) != null) || (goal != null && sortedGoal.equals(sortedCombo))) {
				return combo;
			}
		}
		if (goal != null) {
			System.out.println("There is no way you can steal " + goal);
		}
		return null;
	}

	private static String concatenateSort(List<String> strings) {
		StringBuilder sb = new StringBuilder();
                for (String str : strings) {
                        sb.append(str);
                }
                char[] chars = sb.toString().toCharArray();
                Arrays.sort(chars);
                return new String(chars);
	}
}

public class ComboIterator implements Iterator<List<String>> {
	int minWordLength;
	int maxWordLength;
	List<String> strings;
	int numLetters;
	int nextIndex;
	Stack<Integer> next;
	
	public ComboIterator(List<String> strings, int minWordLength, int maxWordLength) {
		this.strings = strings;
		this.minWordLength = minWordLength;
		this.maxWordLength = maxWordLength;
		numLetters = 0;
		nextIndex = 0;
		if (strings.size() >= 2) {
			next = new Stack<Integer>();
			prepareNext();
		} else {
			next = null;
		}
	}

	public boolean hasNext() {
		return next != null;
	}

	public List<String> next() {
		// Convert the stack of indexes into an actual list of strings.
		List<String> result = new ArrayList<String>(next.size());
		Stack<Integer> temp = new Stack<Integer>();
		while (!next.isEmpty()) {
			int index = next.pop();
			temp.push(index);
			result.add(strings.get(index));
		}
		while (!temp.isEmpty()) {
			next.push(temp.pop());
		}
		prepareNext();
		return result;
	}

	private void prepareNext() {
		if (next == null) {
			return;
		}
		do {
			if (numLetters < maxWordLength && nextIndex < strings.size()) {
				// We can add nextIndex to the stack
                                next.push(nextIndex);
                                numLetters += strings.get(nextIndex).length();
                                nextIndex++;
			} else {
				// We need to remove an index from the stack
				if (next.peek() == strings.size() - 1) {
					int poppedIndex = next.pop();
					numLetters -= strings.get(poppedIndex).length();
				}
				
				// We've gone through every combo
				if (next.size() == 0) {
					next = null;
					return;
				}
				
				// Increment the last index in the stack and update numLetters
				int poppedIndex = next.pop();
				numLetters -= strings.get(poppedIndex).length();
				nextIndex = poppedIndex + 1;	
				next.push(nextIndex);
				numLetters += strings.get(nextIndex).length();
				nextIndex++;
			}
		} while (numLetters < minWordLength || numLetters > maxWordLength || next.size() < 2);
	}
}

public class Game {
	public List<String> myWords;
	public List<String> yourWords;
	public List<String> freeLetters;
	public List<String> lettersLeft;
	static final int[] letterDistribution = {13, 3, 3, 6, 18, 3, 4, 3, 12, 2, 2, 5, 3, 8, 11, 3, 2, 9, 6, 9, 6, 3, 3, 2, 3, 2};
	public int myPoints;
	public int yourPoints;
	public boolean isPlayground;
	
	public Game(boolean isPlayground) {
		this.isPlayground = isPlayground;
		myWords = new ArrayList<String>();
		myPoints = 0;
		freeLetters = new ArrayList<String>();
		yourPoints = 0;
		myWords = new ArrayList<String>();
		yourWords = new ArrayList<String>();
		lettersLeft = new LinkedList<String>();
		if (!isPlayground) { 
                	for (int i = 0; i < letterDistribution.length; i++){
                        	for (int j = 0; j < letterDistribution[i]; j++) {
					lettersLeft.add(Character.toString((char)(65 + i)));
                        	}
                	}
		}
	}
	
	public void flipLetter() {
		int index = (int) Math.floor(Math.random() * lettersLeft.size());
		String letter = lettersLeft.get(index);
		lettersLeft.remove(index);
		freeLetters.add(letter);
		Collections.sort(freeLetters);
		System.out.println("New letter:\t" + letter);
        }

	public int lettersLeft() {
		return lettersLeft.size();
	}

	public void printState() {
                System.out.println();
                System.out.println();
                if (isPlayground) {
			printList("Words (" + myPoints + " pts)", myWords);	
               		printList("Free letters", freeLetters);
		} else {
			printList("Your words (" + yourPoints + " pts)", yourWords);
                	printList("My words (" + myPoints + " pts)" , myWords);
               		printList("Free letters", freeLetters);
			if (lettersLeft() == 0) {
				System.out.println("GAME OVER!");
				if (myPoints == yourPoints) {
					System.out.println("It's a tie! We must grudingly acknowledge that once again our skills have proven equal to each other's.");
				} else if (myPoints > yourPoints) {
					System.out.println("I win! You must grudingly acknowledge that once again my skills have proven superior to yours.");
				} else {
					System.out.println("You win! I must grudgingly acknowledge that once again your skills have proven superior to mine.");
				}
			} else {
		        	System.out.println(lettersLeft() + " letters left");
	        	}
		}
	       	System.out.println();
        }

        private void printList(String label, List<String> list){
                System.out.print(label + ":\t");
                if (list.size() == 0) {
                        System.out.print("(none)");
                } else for (String word : list) {
                        System.out.print(" " + word);
                }
                System.out.println();
        }

	public void applyCombo(List<String> combo, String newWord, boolean mine) {
                for (String str : combo) {
                        System.out.print(str + " ");
                        if (str.length() == 1) {
                                int index = Collections.binarySearch(freeLetters, str);
				freeLetters.remove(index);
                        } else {
                                int index = Collections.binarySearch(yourWords, str);
                                if (index < 0) {
                                        index = Collections.binarySearch(myWords, str);
                                        myPoints -= (myWords.get(index).length() - 2);
					myWords.remove(index);
				} else {
                                        yourPoints -= (yourWords.get(index).length() - 2);
					yourWords.remove(index);
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
		if (!str.matches("^[a-zA-Z]")) {
			System.out.println("Input must contain only letters");
			return;
		}
		String[] strs = str.split(" ");
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
}
