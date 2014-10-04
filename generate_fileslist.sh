#!/bin/bash

if [[ $# > 0 && $1 != "-w" ]]; then
	echo "Usage: $0 [-w]"
	echo -e "\t-w: overwrite previous files.txt"
	exit 0
fi

if [[ $1 == "-w" ]]; then 
	git ls-files | grep -e 'java$' | grep -v 'unused' > files.txt
else
	git ls-files | grep -e 'java$' | grep -v 'unused' 
fi
