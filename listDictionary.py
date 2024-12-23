import os

dictionaryFile = "dictionary.txt"
outputFile = "dictionary"
variable = "dictionary"

if os.path.exists(outputFile + ".js"):
    os.remove(outputFile + ".js")

f = open(outputFile + ".js", "a")
f.write("let " + variable + " = new Set([\n")
with open(dictionaryFile) as file:
    for line in file:
        print(line.rstrip())
        f.write("\"" + line.rstrip() + "\",\n")
f.write("])")

f.close()