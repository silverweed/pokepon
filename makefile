all: files.txt
	javac @files.txt

files.txt:
	./generate_fileslist.sh -w
