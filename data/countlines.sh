#!/bin/bash
# count lines of code, excluding comments and empty lines.
# (c) silverweed - released under Public Domain

DIR='.'
while [[ $# > 0 ]]; do
	case $1 in
	-h) echo "Usage: $0 [-e ext] [dir]" >&2; exit 0 ;;
	-e) shift; EXT=$1; shift ;;
	*) DIR=$1; shift ;;
	esac
done

find $DIR -type f -name \*$EXT -exec egrep -cv '(^\s*$|^\s*//|^\s*\*|^\s*/\*.*(?!\*/)$)' {} + |
	tee >(awk -v FS=':' '{printf "%-5d %s\n", $2, $1}' | sort -g >&2) |
	cut -f2 -d: |
	awk '{ n += $1 } END { print n }'
