#!/bin/bash
# compiles a tables with some stats about moves

# get options
while [[ $# -gt 0 ]]; do
	case $1 in
		type)
			SORT_BY="type"
			shift
			;;
		movetype|mt)
			SORT_BY="movetype"
			shift
			;;
		damage|dmg)
			SORT_BY="damage"
			shift
			;;
		accuracy|acc)
			SORT_BY="accuracy"
			shift
			;;
		-s)
			DAMAGING_ONLY=true
			shift
			;;
		-m)
			SHOW_MISSING=true
			shift
			;;
		*)
			echo "Usage: $0 [type|movetype|damage|accuracy] [-s (only damaging moves)] [-m (show missing types)]" >&2
			exit 1
			;;
	esac
done
DIR="$(dirname $(readlink -f $0))"
MOVEDIR="$DIR/../move"
[[ $SHOW_MISSING == true ]] && {
	TYPES=$(awk 'BEGIN{FS="\("}/^[[:space:]]*[A-Z]+\(/{print $1}' $DIR/../enums/Type.java 2>/dev/null)
	EX_TYPES=""
}

declare -A DATA
MOVES=""

# Collect data
for MOVE in $(ls $MOVEDIR); do
	MOVE="$MOVEDIR/$(basename $MOVE)"
	[[ $MOVE =~ class$ || ${MOVE#$MOVEDIR} == "Move.java" || -d $MOVE ]] && continue
	TYPE=$(awk '/^[[:space:]]*type/ { print substr($3, 6, length($3) - 6) }' $MOVE | head -1)
	[[ $SHOW_MISSING == true ]] && EX_TYPES="$EX_TYPES $TYPE"
	MOVETYPE=$(awk '/^[[:space:]]*moveType/ { print substr($3, 15, length($3) - 15) }' $MOVE | head -1)
	ACCURACY=$(awk '/^[[:space:]]*accuracy/ { print substr($3, 0, length($3) - 1) }' $MOVE | head -1)
	if [[ $ACCURACY == -1 ]]; then
		ACCURACY="inf"
	elif [[ -z $ACCURACY || ! $ACCURACY =~ [0-9]+ ]]; then
		ACCURACY='?'
	fi
	if [[ $MOVETYPE == "STATUS" ]]; then
		DAMAGE=0
	else
		DAMAGE=$(awk '/^[[:space:]]*baseDamage/ { print substr($3, 0, length($3) - 1) }' $MOVE | head -1)
		if [[ -z $DAMAGE ]]; then
			DAMAGE=0
		elif [[ ! $DAMAGE =~ [0-9]+ ]]; then
			DAMAGE='?'
		fi
	fi
	[[ $DAMAGE == 0 && $DAMAGING_ONLY == true ]] && continue
	if (( ${#TYPE} < 5 || ${#MOVETYPE} < 7 )); then continue; fi
	# insert data into DATA map
	DATA[$MOVE,type]=$TYPE
	DATA[$MOVE,movetype]=$MOVETYPE
	DATA[$MOVE,damage]=$DAMAGE
	DATA[$MOVE,accuracy]=$ACCURACY
	MOVES="$MOVES $MOVE"
done

case ${SORT_BY:-damage} in
	damage) SORT="sort -g -r -k3" ;;
	accuracy) SORT="sort -g -r -k4" ;;
	movetype) SORT="sort -g -k2" ;;
	type) SORT="sort -g -k1" ;;
esac

# Print data
printf "%-15s %-15s %-3s %-3s\n" "type" "movetype" "dmg" "prc"
for MOVE in $MOVES; do
	if [[ ! $(basename $MOVE) =~ java$ ]]; then
		echo ERROR: move is $MOVE
		continue
	fi
	printf "%-15s %-15s %-3s %-3s %-20s\n" ${DATA[$MOVE,type]} ${DATA[$MOVE,movetype]} ${DATA[$MOVE,damage]} ${DATA[$MOVE,accuracy]} $(basename $MOVE)
done | eval $SORT

if [[ $SHOW_MISSING == true ]]; then
	MSG_TYPES=""
	for i in $TYPES; do
		[[ -n $(echo $EX_TYPES | grep -v $i) ]] && MSG_TYPES="$MSG_TYPES $i"
	done
	echo "Missing types: $MSG_TYPES"
fi
