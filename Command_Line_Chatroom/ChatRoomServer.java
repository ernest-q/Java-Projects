import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This is a server class that accepts a port number to start a multi-threaded
 * server, multi-roomed, chat room with multiple clients able to connect. There
 * is a nested inner class called chat service that takes care of each users inputs
 * and calls method of the outer class to then broadcast back down to the other users
 * instances of the chat service to be able to receive messages from other users.
 * 

 * @author Ernest J. Quant
 *
 */
public class ChatRoomServer {
	
	
	private ArrayList<ArrayList<ChatService>> serviceListList = new ArrayList<ArrayList<ChatService>>();
	private ArrayList<ArrayList<String>> userListList = new ArrayList<ArrayList<String>>();
	private ArrayList<ChatService> serviceList = new ArrayList<ChatService>();	
	private ArrayList<ChatService> serviceList2 = new ArrayList<ChatService>();
	private ArrayList<ChatService> serviceList3 = new ArrayList<ChatService>();
	private ArrayList<String> users = new ArrayList<String>();
	private ArrayList<String> users2 = new ArrayList<String>();
	private ArrayList<String> users3 = new ArrayList<String>();
	private ReentrantLock serviceLock = new ReentrantLock();
	private ReentrantLock userLock = new ReentrantLock();
	private int portNum;
	
	/**
	 * The constructor for the server that takes a port number and then
	 * needs to be started by using the start server method.
	 * 
	 * @param aPortNum		The port number passed and used by the server.
	 * @throws Exception
	 */
	public ChatRoomServer(int aPortNum) throws Exception {
		
		portNum = aPortNum;
	}
	
	
	/**
	 * The server start method that adds the three user lists to the user list list
	 * and does the same for the service lists, which represent each of the 3
	 * different chat rooms. It also prints a message to the person running the server
	 * that it is now accepting users and also alerts them when a user is connected.
	 * This makes a different thread for each users chat service trying to join and then
	 * continues listening to other users wanting to join.
	 * 
	 * @throws Exception
	 */
	public void startServer() throws Exception {
		
		ServerSocket server = new ServerSocket(portNum);
		
		userListList.add(users);
		userListList.add(users2);
		userListList.add(users3);
		
		serviceListList.add(serviceList);
		serviceListList.add(serviceList2);
		serviceListList.add(serviceList3);

		System.out.println("Waiting for users to join...");
		while(true) {
				Socket s = server.accept();
				System.out.println("User connected");
				ChatService cs = new ChatService(s);
				Thread t = new Thread(cs);
				t.start();	
		}
	}
	

	/**
	 * This receives a message from the chat service class, the user name of the
	 * instance of that chat service and the chat number that the user selected.
	 * using this information it broadcasts back down to everyone, but the user
	 * sending the message, the message that the user wants to broadcast. Also
	 * used to broadcast the join and exiting messages to other users.
	 * 
	 * @param username	The user name of this current ChatService instance.
	 * @param message	The message that is passed a user to be broadcasted.
	 * @param chatNum	The specific chat room number the user selected.
	 */
	public void messageOut(String username, String message, int chatNum) {
		for(ChatService cs : serviceListList.get(chatNum-1)) {
			if(!cs.getUserName().equals(username)) {
				cs.messageSend(username, message);
			}
		}
	}
	
	
	
//***************************** NESTED CLASS *****************************//	
	
	
	/**
	 * The Chat service class that contains is basically the individual sessions
	 * of each active user and processes the information that the user passes to this
	 * class, to then pass it up to the main chat room server class and also to be 
	 * worked upon and broadcast up and then back down, the messages that the
	 * user wants to send.
	 * 
	 * @author Ernest J. Quant
	 *
	 */
	class ChatService implements Runnable {
		
		private PrintWriter out;
		private Scanner in;
		private Socket s;
		private String userName;
		private int selectedChat;

		
		/**
		 * The constructor class for the Chat Service that accepts the new socket
		 * for each of the different users connecting to the port of the server.
		 * 
		 * @param aSocket		The users socket that is being utilized for communication.
		 * @throws Exception
		 */
		public ChatService(Socket aSocket) throws Exception {
			
			s = aSocket;
		}
		
		
		/**
		 * This is the message that is being broadcasted from another user
		 * or a message from the server indicating whether a user is coming or
		 * going from the selected chat room.
		 * 
		 * @param name		The user name of this current ChatService instance.
		 * @param message	The message that was broadcasted to other users.
		 */
		public void messageSend(String name, String message) {

			if(message.equals(" has joined the chat!"))
				out.println("\n=== " + name + message + " ===\n");
			else if(message.equals(" has left the chat!")) {
				out.println("\n=== " + name + message + " ===\n");
			}
			else {
				out.println(name + ": " + message);
			}
			out.flush();
		}
		
		
		/**
		 * This is a method used by the main Chat Room Server class to get the user name
		 * from the nested Chat Service class.
		 * 
		 * @return		Returns the user name of the user in this ChatService instance.
		 */
		public String getUserName() {
			
			return userName;
		}
		
		
		/**
		 * This is the run method, since this implements runnable. This
		 * makes a new input using the scanner class and input stream class, it also
		 * makes a new output by using the print writer class and the output stream class.
		 * It presents the user with the first message to enter LOGIN or LOGOUT to either
		 * enter or exit the chat, and then calls the doService method that processes the
		 * input.
		 */
		public void run() {
			
			try {
				try {
					in = new Scanner(s.getInputStream());
					out = new PrintWriter(s.getOutputStream());
					out.print("Enter \"LOGIN\" to contine or \"LOGOUT\" to exit: ");
					out.flush();
					doService();
				}
				finally {
					s.close();
				}
			}
			catch(IOException exception) {
				exception.printStackTrace();
			}	
		}
		
		
		/**
		 * This method processes the input and sees if the user entered LOGOUT and will
		 * disconnect them from the chat or from the connection, depending on when they enter
		 * LOGOUT. This also calls messageOut to then broadcast the message of this specific
		 * user to other users, or to inform them of them entering or exiting the chat, as long
		 * as the user is logged in and that the message isn't LOGOUT.
		 * 
		 * @throws IOException
		 */
		public void doService() throws IOException {
			/*!!! Commented out some conditions due to confusion about null and trouble implementing it!!!*/
			
			while(true)
			{
				if(!in.hasNext()) return;
				String command = in.nextLine();
				if(command.equals("LOGOUT") /*|| command.equals("")*/) {
					//return;
					executeCommand("LOGOUT");
				}
				if(userIsLogged(userName)) {
					String newMessage = null;
					while(userIsLogged(userName) && (newMessage !="LOGOUT" /*|| !newMessage.contentEquals("")*/))
					{
						newMessage = in.nextLine();
						if(newMessage.equals("LOGOUT") /*|| newMessage.contentEquals("")*/) {
							executeCommand("LOGOUT");
							return;
						}
						else {
							messageOut(userName,newMessage,selectedChat);
						}
					}
				}
				else executeCommand(command);
			}
		}
		
		
		/*
		 * This method processes the command passed to it and then guides the user depending
		 * on what they enter. It asks them for the chat room they want to join, whether its 1,
		 * 2, or 3. It asks the user for the desired user name, making sure it isn't a user name
		 * isn't already being used. If everything is fine it gives a welcome and goodbye message
		 * to the user.
		 * 
		 * @param command	The command that was entered by the user after connecting.
		 */
		public void executeCommand(String command) {
			
			if (command.equals("LOGIN")) {

				int enteredNum;
				do {
					out.print("Enter an integer an integer in range [1-3] to select a chat room: ");
					out.flush();
					while(!in.hasNextInt()) {
						out.print("Not an int, enter an int in range [1-3]: ");
						out.flush();
						in.next();
					}
					enteredNum = in.nextInt();
				}
				while(enteredNum < 0 || enteredNum > 4);
				selectedChat = enteredNum;
				
				
				
				out.print("Enter Username: ");
				out.flush();
				String enteredUserName = in.next();
				if(userIsLogged(enteredUserName)) {
					do {
						out.print("Enter a different username: ");
						out.flush();
						enteredUserName = in.next();
					}while(userIsLogged(enteredUserName));
				}
				userName = enteredUserName;
				
				addUser(userName,selectedChat);

				out.println("\n\nWELCOME TO CHATROOM #" + selectedChat + "!");
				out.println("-To disconnect of the chat enter \"LOGOUT\"-\n\n");
				out.flush();
				messageOut(userName," has joined the chat!",selectedChat);
			}
			else if (command.equals("LOGOUT")) {
				messageOut(userName," has left the chat!",selectedChat);
				
				removeUser(userName,selectedChat);
				
				out.println("\n\nYOU HAVE EXITED CHATROOM #" + selectedChat +"!");
				out.flush();
				return;
			}
			else if (!command.equals("LOGIN") && !command.equals("LOGOUT")) {
				do {
					out.print("Enter \"LOGIN\" to contine or \"LOGOUT\" to exit: ");
					out.flush();
					command = in.nextLine();
				}
				while(!command.equals("LOGIN") && !command.equals("LOGOUT"));
				executeCommand(command);
			}
			out.flush();
		}
		
		
		/**
		 * This adds the user from the selected chat room by adding the user name
		 * to the user name list of that chat room and by adding the chat service instance
		 * of that user to the chat service list. The -1 is because it's an array starting at
		 * 0 but the first chat room number is 1. Utilizes the reentrant locks since it is
		 * writing to a shared resource and we want to prevent race conditions.
		 * 
		 * @param username 		The user name of this current ChatService instance.
		 * @param selectedChat	The number of the chat room that the user picked.
		 */
		public void addUser(String username,int selectedChat) {	
			
			userLock.lock();
			try {
				userListList.get(selectedChat-1).add(username);
			}
			finally {
				userLock.unlock();
			}
			
			serviceLock.lock();
			try {
				serviceListList.get(selectedChat-1).add(this);
			}
			finally {
				serviceLock.unlock();
			}
		}
		
		
		/**
		 * This removes the user from the selected chat room by removing the user name
		 * from the user name list of that chat room and by removing the chat service instance
		 * of that user in that chat service list. The -1 is because it's an array starting at
		 * 0 but the first chat room number is 1.Utilizes the reentrant locks since it is
		 * writing to a shared resource and we want to prevent race conditions.
		 * 
		 * @param username 		The user name of this current ChatService instance
		 * @param selectedChat	The number of the chat room that the user picked.
		 */
		public void removeUser(String username, int selectedChat) {
			
			userLock.lock();
			try {
				userListList.get(selectedChat-1).remove(username);
			}
			finally {
				userLock.unlock();
			}
			
			serviceLock.lock();
			try {
				serviceListList.get(selectedChat-1).remove(this);

			}
			finally {
				serviceLock.unlock();
			}
		}
		
		
		/**
		 * Checks if the user is logged in to any of the chat rooms by iterating
		 * through the different user lists of each chat room. If the user is 
		 * present in one of the lists then it will return True, if not it will
		 * return False.
		 * 
		 * @param username 	The user name of this current ChatService instance
		 * @return 			Returns True if the user is logged in, false if not.
		 */
		public boolean userIsLogged(String username) {
			
			boolean isLogged = false;
			
			for(int i = 0; i < userListList.size(); i++) {
				if(userListList.get(i).contains(username)) {
					isLogged = true;
				}
			}
			return isLogged;	
		}
		
		
	}
}
