//: pokepon.net.jack/client/Client.java

package pokepon.net.jack.client;

import pokepon.net.jack.*;

import java.net.*;

/** SimpleClient that can also send and receive messages */
public interface Client {

	public void setName(String name);
	public String getName();

}
