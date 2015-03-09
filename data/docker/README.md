Running a PokeponServer in a Docker container
--------------------------------------------------
If you don't know what Docker is, you'd better [have a look here](https://docker.com).

TL;DR: it's a software which allows you to run a process - or a bunch of processes - in an
isolated environment, pretty much like a Virtual Machine, but not quite.
Docker containers are extremely lightweight, can be started and stopped in a few seconds (or less)
and can be replicated out of the box on any host equipped with Docker.

**What does this mean to me, that only want to run a PokeponServer?**

It means you can host a PokeponServer in a secure environment on your Linux machine without
bothering installing a dedicated VM. It also mean you can start and stop it as you wish,
or start several server instances in a jiffy, pretty much as much as your machine can handle,
but with the storage usage of a single one (yup, that's magic. And a clever use of AUFS 
layered filesystems).

How to use it
----------------------------------------------------

* The first requirement is a Linux host (Mac and Windows are viable too, but you'll need
an extra layer called [boot2docker](http://boot2docker.io/), which is actually
a minimal Linux VM, so you'd kinda lose all the advantage of not having to install a VM).
* If the step 1 is satisfied, the following step is installing Docker, either via a package
manager or, if you prefer the latest version, from the [Docker website](https://docs.docker.com/installation/#installation)
**Sidenote:** on Debian/Ubuntu, the package you need to apt-get is called `docker.io`,
while on RHEL-based distros is just `docker`.
* So, if you have successfully installed Docker, open a terminal and ensure its daemon is running.
If you installed directly the binary, start the daemon directly with:
```sudo nohup docker -d &```
or something like that. Otherwise, you can usually do
```sudo service docker start```
or
```sudo systemctl start docker```
* Once you have the daemon running, you can query it via the `docker` CLI. More specifically,
you'll want to build the PokeponServer image. To do this, you have 2 options:

1. **Recommended:** Build a new image from the Dockerfile in this directory. This will ensure you have the latest
  version of the game. The steps are the following:

  * Create a directory, e.g. `~/pokepon-docker`
  * Download the pokepon source code and put it in `~/pokepon-docker`, or whatever you called it
  * Copy the Dockerfile directly into `~/pokepon-docker`
  * Open a terminal, `cd` into that directory and issue the command: `docker build -t <image_name> .`
  * Once completed, you can start your server with `docker run -d -p 12344:12344 <image_name>`

2. Else, download the existing PokeponServer image from Docker Hub. This is as simple as running
  ```docker run -d -p 12344:12344 silverweed/pokepon_server:latest```
  (You'll need root permissions to do this, unless you add your user to the `docker` group,
  which is preferred: `groupadd docker && useradd -a -G docker your_username` - you'll
  need to logout and login back). This options is the least preferred, since the server image is often
  outdated.

For an explanation on the `docker run` flags, see [here](https://docs.docker.com/userguide/usingdocker/), or
run `docker run` with no arguments to see a help message.
