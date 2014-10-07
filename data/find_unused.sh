#!/bin/bash
# Finds unused classes with a stupid, flawed and inefficient algorithm
# author Giacomo Parolini

BASE=$(dirname $(readlink -f $0))/..
check_class() {
	CLASS="$1"
	while read FILE; do
		egrep "[ \t]*${CLASS}\.?" $FILE | \
			sed -rn -e 's/[^:]*:(.*)/\1/p' -e '/^\s*\/[/*]/ !{p}' -e 's/^\s*(.*)/\1/p' -e '/^\s*$/ !{p}'
	done < <(find $BASE -name \*java | grep -v unused | grep -v $CLASS)
}

while read PACKAGE CLASS; do
	PACKAGE=${PACKAGE/data\/\.\.\//}
	# don't consider classes in the following packages
	[[ ${PACKAGE%animation/} != $PACKAGE ]] && continue

	echo CHECKING $CLASS >&2

	if [[ ! $(check_class $CLASS) ]]; then
		echo ---\> $CLASS may be unused.
	fi

done < <(find $BASE -regextype posix-extended -regex '.*(battle|gui|net|player|enums|util|sound).*java' | perl -lne 'print "$1 $2" if /(.*\/)*(.*)\.java/')
