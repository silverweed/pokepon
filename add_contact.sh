#!/bin/bash

move=$1
shift
while [[ $# > 0 ]]; do
	move="$move $1"
	shift
done

shift
for file in $move; do
	echo $file
	if [[ $(basename $file) == "Move" ]]; then continue; fi
	if [[ -n $(grep contactMove $file) ]]; then
		echo "${file%\.java} already knows $move: skipping." >&2
		continue
	fi
	sed -i "/moveType/{s/.*/&\n\t\tcontactMove = true;/;:a;n;ba}" $file
done
