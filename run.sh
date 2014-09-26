#!/bin/bash

JAVA='/usr/lib/jvm/java-7-openjdk-amd64/bin/java'
[[ -x $JAVA ]] || JAVA='java'
if [[ ! -x $(which $JAVA) ]]; then
	echo "Sorry, couldn't find java on this machine. Please install Java 7 or later." >&2
	exit 1
fi
if [[ $# -eq 0 ]]; then
	echo "Usage: $0 [-j JAVA] <p | c | b | t | ct | cl | prova | s | dex | type | cov> [opts]"
	echo -e "Useful options are:\n\tt: teambuilder\n\tct: CLI-teambuilder\n\ts: server\n\tcl: client\n\tdex: ponydex\n\ttype: typechart\n\tcov: coverage"
	exit 1
fi

while [[ $# -gt 0 ]]; do
	if [[ -n $PRG ]]; then
		ARGS="$ARGS $1"
		shift
	else
		case $1 in
			-j)
				shift
				JAVA=$1
				shift
				;;
			-p|-d)
				LAUNCHERARGS="$LAUNCHERARGS $1"
				shift
				;;
			p)
				PRG="PokeponTest"
				shift
				;;
			c)
				PRG="CheckTypings"
				shift
				;;
			cl)
				PRG="PokeponClient"
				LAUNCHERARGS="$LAUNCHERARGS --package pokepon.net.jack.client"
				shift
				;;
			b)
				PRG="BattleTest"
				shift
				;;
			t)
				PRG="TeamBuilderTest"
				shift
				;;
			ct)
				PRG="TeamBuilderTest"
				ARGS="$ARGS -cli"
				shift
				;;
			f|dex)
				PRG="FastPonydex"
				shift
				;;
			cov)
				PRG="Coverage"
				shift
				;;
			prova)
				PRG="Prova"
				LAUNCHERARGS="$LAUNCHERARGS --package pokepon.player"
				shift
				;;
			type)
				PRG="TypeDealer"
				LAUNCHERARGS="$LAUNCHERARGS --package pokepon.battle"
				shift
				;;
			s)
				PRG="PokeponServer"
				LAUNCHERARGS="$LAUNCHERARGS --package pokepon.net.jack.server"
				shift
				;;
			*)
				ARGS="$ARGS $1"
				shift
				;;
		esac
	fi
done

$JAVA pokepon.main.PokeponLauncher $LAUNCHERARGS $PRG $ARGS
