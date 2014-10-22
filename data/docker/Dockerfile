# Dockerfile for the Pokepon Server; to be run from outside the pokepon rootdir.
# 10 Oct 2014, silverweed
from debian:jessie
maintainer silverweed <silverweed1991@gmail.com>

run apt-get update && apt-get install -y openjdk-7-jre
run useradd -g users pokepon

expose 12344
add pokepon /opt/pokepon
run chown -R pokepon:users /opt/pokepon
user pokepon
env CLASSPATH /opt
entrypoint ["/usr/bin/java", "pokepon.net.jack.server.PokeponServer"]
