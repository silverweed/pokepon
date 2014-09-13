//: pokepon.net.jack/server/Server.java

package pokepon.net.jack.server;

import pokepon.net.jack.*;

import java.net.*;
import java.io.*;

/** Interface for basic server */
public interface Server {

	public void start() throws IOException;
	public Socket accept() throws IOException;
	public boolean shutdown();
	public String getName();
}
