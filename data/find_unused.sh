#!/bin/bash
# Finds unused classes with a stupid, flawed and inefficient algorithm
# author silverweed

while [[ $# > 0 ]]; do
	case $1 in
		-p) PAR=true; QUIET=true; shift ;;
		*) echo "Usage: $0 [-p]"; exit 0 ;;
	esac
done

BASE=$(dirname $(readlink -f $0))/..
check_class() {
	CLASS="$1"
	OUT=$(while read FILE; do
		egrep "[ \t]*${CLASS}\.?" $FILE | \
			sed -rn -e 's/[^:]*:(.*)/\1/p' -e '/^\s*\/[/*]/ !{p}' -e 's/^\s*(.*)/\1/p' -e '/^\s*$/ !{p}'
	done < <(find $BASE -name \*java | grep -v unused | grep -v $CLASS))
	[[ $OUT ]] || echo $CLASS may be unused.
}

while read PACKAGE CLASS; do
	PACKAGE=${PACKAGE/data\/\.\.\//}
	# don't consider classes in the following packages
	[[ ${PACKAGE%animation/} != $PACKAGE ]] && continue

	[[ $QUIET ]] || echo Checking $CLASS ... >&2

	if [[ $PAR ]]; then 
		# your processor won't easily forgive you.
		check_class $CLASS &
	else
		check_class $CLASS
	fi

done < <(find $BASE -regextype posix-extended -regex '.*(battle|gui|net|player|enums|util|sound).*java' | perl -lne 'print "$1 $2" if /(.*\/)*(.*)\.java/')
