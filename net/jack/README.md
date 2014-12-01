Java Awful Client-server Kit 
==========================================
A simple networking framework written from scratch by silverweed.  
DISCLAIMER: this suite does not provide advanced security features, therefore you should avoid sending sensitive data while connected to a Poképon Server.

You can obtain a reliably secure connection using an SSH tunnel to encrypt the
connection: see the paragraph "Securing the Connection" below.

THE CONNECTION SYSTEM
------------------------------

* Connection: this is an abstract class which can be easily extended depending
   on the needs. It is actually a fully working class, which is abstract only 
   to enforce inheriting it to reimplement the DefaultConnectionExecutor, which
   does nothing in the base class. Since the primary meaning of a Connection
   is precisely dealing with the messages, it makes no sense using a pure
   Connection, because all the messages would just be discarded. Actually, one
   *could* use a Connection by using the addConnectionExecutor method to add
   custom executors, but its abstractness oblige the user to decide whether he's
   implementing a Client-side or a Server-side connection.

This is made simple by the following two classes:

* ClientConnection: it's a connection used by a client to handle messages coming
   from a server. It's meant to work with a BasicGUIClient, so the DefaultExecutor
   echoes all messages on the input-text area.

* ServerConnection: it's used by a MultiThreadedServer to handle messages coming 
   from the clients. A MultiThreadedServer generally instances one Connection per
   client and lets the Executor do the rest of the job.

The Connection System relies on the ConnectionExecutors. These are but classes which
implement the method execute(), which takes a String, processes it, and returns an int
to signal whether the next executor should process the input as well or not. In fact,
one generally chains multiple different Executors by attaching them to a Connection:
thus, any messages coming from the Connection socket will be processed in order by them.

* ConnectionExecutor: abstract class for an executor of a general Connection (both
   Client-side and Server-side).

* ClientConnectionExecutor: a ConnectionExecutor which also has a Client protected field,
   representing the same client of the ClientConnection to which it attaches.

* ServerConnectionExecutor: likewise, but with a protected MultiThreadedServer field.


So, basically, the process is:

- create a Connection (client or server-side, depending on the needs)
- create custom Executors
- attach them to the Connection via addConnectionExecutor(new MyConnectionExecutor())
- (you can also removeConnectionExecutor(executor))


Securing the Connection
-------------------------------------------------------
If you need to be sure your server-client connection is secure, using an SSH tunnel is
the most straightforward way to obtain this.

First, run an SSH server on the machine where your Poképon Server is hosted; then,
instead of directly connecting to the server, open an SSH tunnel to the server host:

<pre>ssh -fN -L 12344:(the PoképonServer IP):12344 user@serverhost</pre>

Then connect your Poképon Client to the tunnel's local port:

<pre>java -jar pokepon.jar client localhost:12344</pre>

And that's it! Your connection is now encrypted via SSL.
