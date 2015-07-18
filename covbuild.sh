#!/bin/bash

getbuild() {
	git describe --always --long --dirty
}

getreadiness() {
	tail cov-int/build-log.txt | grep compilation | cut -f2 -d\( | cut -f1 -d%
}

PATH=$PATH:$HOME/Public/cov-analysis-linux64-7.6.0/bin 
rm -f pokepon.tgz 
if (cov-build --dir cov-int make); then
	tar cvfz pokepon.tgz cov-int
fi

if [[ $? == 0 ]]; then
	R=$(getreadiness)
	if ((R < 85)); then
		echo "[ ERROR ] only ${R}% compilation units are ready: build will fail."
		exit 2
	else
		echo "[ OK ] ${R}% of the compilation units are ready."
	fi
	echo "Submit new build?"
	select ANS in "Submit" "Abort"; do
		case $ANS in
			Submit)
				echo Submitting... >&2
				set -x
				curl	--form token=$(< ./cov-token) \
					--form email=silverweed1991@gmail.com \
					--form file=@./pokepon.tgz \
					--form version="0.1" \
					--form description="Pokepon build rev.$(getbuild)" \
					https://scan.coverity.com/builds?project=pokepon
				set +x
				echo Done. >&2
				exit 0
				;;
			*)
				echo Aborted. >&2
				exit 1
				;;
		esac
	done
fi
