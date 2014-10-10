Running a PokeponServer in a Docker container
--------------------------------------------------
If you don't know what Docker is, you'd better <a href='https://docker.com'>have a look here</a>.

TL;DR: it's a software which allows you to run a process - or a bunch of processes - in an
isolated environment, pretty much like a Virtual Machine, but not quite.
Docker containers are extremely lightweight, can be started and stopped in a few seconds (or less)
and can be replicated out of the box on any host equipped with Docker.

<b>What does this mean to me, that only want to run a PokeponServer?</b>

It means you can host a PokeponServer in a secure environment on your Linux machine without
bothering installing a dedicated VM. It also mean you can start and stop it as you wish,
or start several server instances in a jiffy, pretty much as much as your machine can handle,
but with the storage usage of a single one (yup, that's magic. And a clever use of AUFS 
layered filesystems).

How to use it
----------------------------------------------------
* The first requirement is a Linux host (Mac and Windows are viable too, but you'll need
an extra layer called <a href='http://boot2docker.io/'>boot2docker</a>, which is actually
a minimal Linux VM, so you'd kinda lose all the advantage of not having to install a VM).
* If the step 1 is satisfied, the following step is installing Docker, either via a package
manager or, if you prefer the latest version, from the <a href='https://docs.docker.com/installation/#installation'>Docker website</a>
<b>Sidenote:</b> on Debian/Ubuntu, the package you need to apt-get is called <code>docker.io</code>,
while on RHEL-based distros is just <code>docker</code>.
* So, if you have successfully installed Docker, open a terminal and ensure its daemon is running.
If you installed directly the binary, start the daemon directly with:
<pre>sudo nohup docker -d &</pre>
or something like that. Otherwise, you can usually do
<pre>sudo service docker start</pre>
or
<pre>sudo systemctl start docker</pre>
* Once you have the daemon running, you can query it via the <code>docker</code> CLI. More specifically,
you'll want to build the PokeponServer image. To do this, you have 2 options:
<ol>
  <li>Just download the existing PokeponServer image from Docker Hub. This is as simple as running
  <pre>docker run -d -p 12344:12344 silverweed/pokepon_server:latest</pre>
  (You'll need root permissions to do this, unless you add your user to the <code>docker</code> group,
  which is preferred: <code>groupadd docker && useradd -a -G docker your_username</code> - you'll
  need to logout and login back).
  </li>
  <li>Build a new image from the Dockerfile in this directory. This will ensure you have the latest
  version of the game, but require an additional step.
  * Create a directory, e.g. <code>~/pokepon-docker</code>
  * Download the pokepon source code and put it in <code>~/pokepon-docker</code>, or whatever you called it
  * Copy the Dockerfile directly into <code>~/pokepon-docker</code>
  * Open a terminal, <code>cd</code> into that directory and issue the command: <code>docker build -t &lt;image_name&gt; .</code>
  * Once completed, you can start your server with <code>docker run -d -p 12344:12344 &lt;image_name&gt;</code>
  </li>
</ol>
For an explanation on the <code>docker run</code> flags, see <a href='https://docs.docker.com/userguide/usingdocker/'>here</a>, or
run <code>docker run</code> with no arguments to see a help message.
