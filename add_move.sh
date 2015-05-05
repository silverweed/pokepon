#!/bin/bash

if [[ $1 == "-h" || $1 == "--help" ]]; then
	{
		echo "Usage: $0 Move Name [, Move Name 2 ...] $ Pony1.java Pony2.java ..."
		echo "(commas and $ must be separated from names by spaces)"
	} >&2
	exit 0
fi

moves=()
move=$1
shift
while [[ $1 != "$" ]]; do
	if [[ $1 == "," ]]; then
		moves[${#moves[@]}]="$move"
		move=""
	else
		if [[ $move == "" ]]; then move="$1"
		else move="$move $1"
		fi
	fi
	shift
done
moves[${#moves[@]}]="$move"

shift

for file in $@; do
	if [[ $(basename $file) == "Pony.java" ]]; then continue; fi
	for ((i=0; i<${#moves[@]}; ++i)); do
		move="${moves[i]}"
		if [[ -n $(grep learnableMoves $file | grep "$move") ]]; then
			echo "${file%\.java} already knows $move: skipping." >&2
			continue
		fi
		sed -i "/learnableMoves/{s/.*/&\n\t\tlearnableMoves.put(\"$move\",1);/;:a;n;ba}" $file
	done
done
