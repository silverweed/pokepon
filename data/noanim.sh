#!/bin/bash

DIR="$(dirname $(readlink -f $0))"

for FILE in $(ls $DIR/../move/*java | grep -v Move.java); do
	[[ $(grep animation $FILE) ]] || echo $(basename $FILE)
done
