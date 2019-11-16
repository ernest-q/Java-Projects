/**
 * This is merely the main class where you make a new ChatRoomServer
 * object and initialize it with the port number 8888 and then
 * start the server with .startServer to begin running the chat server.
 * 
 * @author Ernest J. Quant
 *
 */

public class ChatRoomServerMain {

	public static void main(String[] args) throws Exception {
		
		// The port number being used for the server.
		final int SBAP_PORT = 8888;
		
		/* Constructing a new server using the port number and then
		*  starting the server itself.
		*/
		ChatRoomServer server = new ChatRoomServer(SBAP_PORT);
		server.startServer();
	}
}
