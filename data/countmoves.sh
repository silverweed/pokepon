#!/bin/bash

echo Number of moves per pony:
for i in ../pony/*java; do
	[[ $i =~ Pony.java ]] && continue
	printf "%-20s %d\n" $(basename ${i%\.java}) $(grep -c learnableMoves $i)
done | sort -rgk 2
