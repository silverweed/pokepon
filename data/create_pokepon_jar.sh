#!/bin/bash
if [[ ! -d pokepon ]]; then
	echo Please, execute this script from outside the pokepon directory.
	exit 1
fi
[[ -f pokepon.jar ]] && rm -f pokepon.jar
[[ -f manifest.mf ]] && rm -f manifest.mf
echo 'Name: pokepon
Specification-Title: Pokepon - a Pokemon-like battle simulator with ponies
Spefification-Version: 0.01 (pre-alpha)
Specification-Vendor: silverweed91
Implementation-Title: pokepon
Implementation-Vendor: silverweed91
Main-Class: pokepon.main.QuickLauncher
' > manifest.mf
TMPFILE=$(mktemp -u)
find ./pokepon -name \*class | egrep -v 'unused' > $TMPFILE
jar cvfm pokepon.jar manifest.mf $(< $TMPFILE) pokepon/net/server.conf pokepon/resources
rm -f $TMPFILE
#jar cvfm pokepon.jar manifest.mf pokepon/*/*class pokepon/*/*/*class pokepon/*/*/*/*class pokepon/net/server.conf pokepon/* pokepon/resources/*
