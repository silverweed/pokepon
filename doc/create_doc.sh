#!/bin/bash

JAVADOC=/usr/lib/jvm/java-7-openjdk-amd64/bin/javadoc
[[ -x $JAVADOC ]] || JAVADOC='javadoc'
[[ -x $(which $JAVADOC) ]] || {
	echo "Can't find $JAVADOC on this machine, please install Java JDK >= 1.7"
	exit 1
}

$JAVADOC -subpackages pokepon -private -author
