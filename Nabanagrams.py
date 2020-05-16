import sys

maxWordLength = 0
minWordLength = 3
game = None


def main():
	print
	print "---------------------------"
	print "---N-A-B-A-N-A-G-R-A-M-S---"
	print "---------------------------"
	print
	maxWordLength = 0
	minWordLength = 3
	setup("dictionary.csv", minWordLength, maxWordLength)
	while True:
		if game is not None:
			print("Press enter to resume your game")
		print("+ to start a new game")
		print("* to start a new game in sandbox mode")
		print("! to quit")
		print()
		input = raw_input()
		if input == "+":
			enterGame(Game(False))
		elif input == "*":
			enterSandbox(Game(True))
		elif input == "!":
			print("Bye!")
			sys.exit(0)
		elif input == "" and game != None:
			if game.isSandbox:
				enterSandbox(game)
			else:
				enterGame(game)

def setup(dictionaryFilename, minWordLength, maxWordLength):
	f = open(dictionaryFilename)
	dictionary = set()
	wordMap = dict()
	for word in f:
		word = word.upper()
		if len(word) >= minWordLength:
			dictionary.add(word)
			sortedWord = list(word)
			sortedWord.sort()
			wordMap[''.join(sortedWord)] = word
			if len(word) > maxWordLength:
				maxWordLsength = len(word)

def enterGame(game):
	while True:
		game.printState()
		if game.myTurn:
			print "Press 'enter' to let me take a turn"
		else:
			print "Press 'enter' to flip a new letter"
		print "Enter a word to steal it"
		print "? for more options"
		print
		input = raw_input()
		print "---------------------------------------"
		print
		if input == "":
			if game.myTurn:
				newCombo = findCombo(game, None)
				if newCombo is None:
					print "I can't find anything!"
				else:
					while newCombo != None:
						newWord = wordMap[concatenateSort(newCombo)]
						game.formWord(newCombo, newWord, True)
						newCombo = findCombo(game, None)
				if game.lettersLeft() == 0:
					print "Game over!"
					game.printState()
					game = None
					print "---------------------------------------"
			elif game.lettersLeft() > 0:
				game.flipLetter()
			game.myTurn = not game.myTurn
		elif input == "?":
			return
		else:
			newCombo = findCombo(game, input)
			if newCombo is not None:
				game.formWord(newCombo, input, false)

def enterSandbox(game):
	while True:
		game.printState()
		input = raw_input()
		if input == "":
			newCombo = findCombo(game, None)
			if newCombo is None:
				print "I can't find anything!"
			else:
				while newCombo != None:
					newWord = wordMap[concatenateSort(newCombo)]
					game.formWord(newCombo, newWord, True)
					newCombo = findCombo(game, None)
		elif input == "?":
			return
		else:
			game.addLetters(input)

def findCombo(game, goal):
	if goal is not None:
		if not goal in dictionary:
			print goal + " is not a valid word"
			return None
		sortedGoal = concatenateSort(goal)

	strings = set()
	for letter in game.freeLetters:
		strings.add(letter)
	for word in game.yourWords:
		stings.add(word)
	for word in game.myWords:
		stings.add(word)

	comboFinder = ComboFinder(strings, minWordLength, maxWordLength)
	for combo in comboFinder:
		sortedCombo = sortStrings(combo)
		if (goal is None and sortedCombo in wordMap) or (goal is not None and sortedCombo == sortedGoal):
			return combo


def sortStrings(strings):
	string = ''.join(strings)
	return ''.join(sorted(string))


class Game:
	myWords = []
	yourWords = []
	myPoints = 0
	yourPoints = 0
	myTurn = False
	freeLetters = []
	lettersLeft = []
	letterDistribution = {13, 3, 3, 6, 18, 3, 4, 3, 12, 2, 2, 5, 3, 8, 11, 3, 2, 9, 6, 9, 6, 3, 3, 2, 3, 2};

	def __init__(self, isSandbox):
		print(self.myPoints)
		self.isSandbox = isSandbox
		if not self.isSandbox:
			for amount in self.letterDistribution:
				for i in range(0, amount):
					bla = []
					bla.append(chr(i + 65))

	def flipLetter(self):
		index = random.randint(0, lettersLeft())
		letter = self.lettersLeft[index]
		self.freeLetters.add(letter)
		sort(self.freeLetters)
		print("New letter:\t" + letter)

	def lettersLeft(self):
		l = []
		return len(l)
	
	def printState(self):
		print("\n")
		if self.isSandbox:
			self.printList("Words (" + str(self.myPoints) + " pts)", self.myWords)
			self.printList("Free letters", freeLetters);
		else:
			self.printList("Your words (" + str(self.yourPoints) + " pts)", self.yourWords);
			self.printList("My words (" + str(self.myPoints) + " pts)", self.myWords);
			self.printList("Free letters", str(self.freeLetters));
			if self.lettersLeft == 0:
				print("GAME OVER!")
				if self.mayPoints == self.yourPoints:
					print("It's a tie! We must grudingly acknowledge that once again our skills have proven equal to each other's.")
				elif self.myPoints > self.yourPoints:
					print("I win! You must grudingly acknowledge that once again my skills have proven superior to yours.")
				else:
					print("You win! I must grudgingly acknowledge that once again your skills have proven superior to mine.")
			else:
				print(str(self.lettersLeft()) + " letters left");
		print("\n")

	def printList(self, label, items):
		print(label + ":\t");
		if len(items) == 0:
			print("none\n")
		else:
			print(" ".join(items))
		print("\n")

	def stealWord(self, combo, newWord, mine):
		for str in combo:
			print(str + " ")
			if len(str) == 1:
				self.freeLetters.remove(str)
			else:
				try:
					self.yourWords.remove(str)
					self.yourPoints -= len(self.yourWords[index]) - 2
				except:
					self.myPoints.remove(str)
					self.myPoints -= len(self.myWords[index]) - 2
		print("---> " + newWord)
		if mine:
			self.myWords.add(newWord)
			self.myPoints += len(newWord) - 2
			sort(self.myWords)
		else:
			self.yourWords.add(self.newWord)
			self.yourPoints += len(self.newWord) - 2
			sort(self.yourWords)

	def addLetters(self, input):
		if not bool(re.match("^[a-zA-Z ]+", input)):
			print("Input must only contain letters")
			return
		strs = input.split("\\s+")
		for str in strs:
			if len(str) == 1:
				self.freeLetters.append(str)
			else:
				self.myWords.append(str)
		sort(self.myWords)
		sort(self.freeLetters)


class Combos:


class ComboIterator:



if __name__ == "__main__":
	main()