import os

# List names of photos in /photos into photos.js (or designated directory)

outputFile = "dictionary"
dictionaryFile = "dictionary.txt"
photos = []

if os.path.exists(outputFile + ".js"):
    os.remove(outputFile + ".js")

f = open(outputFile + ".js", "a")
f.write("let " + outputFile + " = new Set([\n")
with open(dictionaryFile) as file:
    for line in file:
        print(line.rstrip())
        f.write("\"" + line.rstrip() + "\",\n")
f.write("])")
f.close()