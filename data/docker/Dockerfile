# Dockerfile for the Pokepon Server; to be run from outside the pokepon rootdir.
# 10 Oct 2014, silverweed
from debian:jessie
maintainer silverweed <silverweed1991@gmail.com>

run apt-get update && apt-get install -y openjdk-7-jre
add pokepon /opt/pokepon
run useradd -g users pokepon
run chown -R pokepon:users /opt/pokepon

env CLASSPATH /opt
expose 12344
entrypoint ["/usr/bin/java"]
cmd ["pokepon.net.jack.server.PokeponServer"]
