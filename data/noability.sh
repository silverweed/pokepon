#!/bin/bash

DIR="$(dirname $(readlink -f $0))"

for FILE in $(ls $DIR/../pony/*java | grep -v Pony.java); do
	[[ $(grep possibleAbilities $FILE) ]] || echo $(basename $FILE)
done
