#!/bin/bash

# count lines of code 

if [[ $# -gt 0 ]]; then 
	if [[ $1 =~ \-.* ]]; then
		echo "Usage: $0 [ext] [dir]"
		exit 0
	fi
	EXT="$1"
	if [[ $# -gt 1 ]]; then
		RTDIR="$2"
	else RTDIR="."
	fi
else EXT="*"; RTDIR="."
fi

countlines() {
	pushd $1 1>/dev/null 
	LN=0
	for j in `ls`; do
		[[ -L $j ]] && continue
		if [[ $j != ${j%\.$EXT} ]]; then
			let LN+=$(wc -l $j | awk '{print $1}')
		elif [[ -d $j ]]; then
			let LN+=$(countlines $j)
		fi
	done
	echo $LN
}

LNS=0

echo "Counting lines in .$EXT files in $RTDIR"

TMP=$(wc -l *.$EXT 2>/dev/null | awk '{print $1}')
if [[ ! $TMP =~ [0-9]+ ]]; then TMP=0; fi
printf "%-15s: %d\n" $RTDIR $TMP
let LNS+=$TMP
for i in `ls -d $RTDIR/*/`; do
	TMP=$(countlines $i)
	printf "%-15s: %d\n" ${i%/} $TMP
	let LNS+=$TMP
done

printf "%-15s: %d\n" "Total:" $LNS

# or:
#wc -l */*.java */*/*.java */*/*/*.java
