all: files.txt
	javac -Xlint:all -Xlint:-serial @files.txt

files.txt:
	./generate_fileslist.sh -w
