#!/bin/bash
if [[ ! -d pokepon ]]; then
	echo Please, execute this script from outside the pokepon directory.
	exit 1
fi
[[ -f pokepon-server.jar ]] && rm -f pokepon-server.jar
[[ -f manifest.mf ]] && rm -f manifest.mf
echo 'Name: pokepon-server
Created-By: silverweed91
Main-Class: pokepon.net.jack.server.PokeponServer
' > MANIFEST.MF
jar cvfm pokepon-server.jar MANIFEST.MF $(< pokepon/data/server_files.txt)
chmod +x pokepon-server.jar
