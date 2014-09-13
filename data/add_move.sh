#!/bin/bash

if [[ $1 == "-h" || $1 == "--help" ]]; then
	echo "Usage: $0 Move Name $ Pony1.java Pony2.java ..."
	exit 0
fi

move=$1
shift
while [[ $1 != "$" ]]; do
	move="$move $1"
	shift
done

shift

for file in $@; do
	if [[ $(basename $file) == "Pony.java" ]]; then continue; fi
	if [[ -n $(grep learnableMoves $file | grep "$move") ]]; then
		echo "${file%\.java} already knows $move: skipping." >&2
		continue
	fi
	sed -i "/learnableMoves/{s/.*/&\n\t\tlearnableMoves.put(\"$move\",1);/;:a;n;ba}" $file
done
