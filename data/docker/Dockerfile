# Dockerfile for the Pokepon Server; to be run from outside the pokepon rootdir.
# 10 Oct 2014, silverweed
FROM debian:jessie
MAINTAINER silverweed <silverweed1991@gmail.com>

RUN apt-get update && apt-get install -y openjdk-7-jre
RUN useradd -g users pokepon

EXPOSE 12344
ADD pokepon /opt/pokepon
RUN chown -R pokepon:users /opt/pokepon
USER pokepon
ENV CLASSPATH /opt
ENTRYPOINT ["/usr/bin/java", "pokepon.net.jack.server.PokeponServer"]
