//: pokepon.net.jack/client/ClientConnectionExecutor.java

package pokepon.net.jack.client;

import pokepon.net.jack.*;

abstract class ClientConnectionExecutor extends ConnectionExecutor {

	Client client;
	
	public void setClient(Client client) {
		this.client = client;
	}
}
