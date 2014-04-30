import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Written by Alex Rymarz, Mike Hoye, and Drew Szlembarski
 * 
 * @param argv
 * @throws Exception
 */
public class Game implements Runnable {

	private volatile static Tree tree = new Tree();
	private Socket socket;
	private BufferedReader inFromClient;
	private PrintWriter outToClient;
	private RandomAccessFile raf = new RandomAccessFile("SAVE.txt", "rw");
	boolean played = false;
	private String userName = "";
	private static ReentrantLock lock = new ReentrantLock();
	private static BlockingQueue<String> messages = new LinkedBlockingQueue<String>();
	private String ID = UUID.randomUUID().toString();
	private ArrayList<String> userMessages = new ArrayList<String>();

	public Game(Socket s) throws IOException {
		this.socket = s;
		inFromClient = new BufferedReader(new InputStreamReader(
				socket.getInputStream()));
		outToClient = new PrintWriter(socket.getOutputStream());
		createTree();
	}

	/**
	 * This method creates the tree if the SAVE.txt file is empty or does not
	 * exist. Otherwise, it sets the tree to the results of readObject().
	 * 
	 * @throws IOException
	 */
	public void createTree() throws IOException {
		if (raf.length() == 0) {
			tree.putRoot("Obama", "");
			writeObject(tree);
		}
		tree = readObject();
	}

	/**
	 * This method writes the tree object to SAVE.txt.
	 * 
	 * @param tree2
	 * @throws IOException
	 */
	public void writeObject(Tree tree2) throws IOException {
		ObjectOutputStream oos = new ObjectOutputStream(
				new BufferedOutputStream(new FileOutputStream("SAVE.txt")));
		oos.writeObject(tree2);
		oos.close();
	}

	/**
	 * This method reads in the tree object from SAVE.txt.
	 * 
	 * @return Tree
	 */
	private static Tree readObject() {
		ObjectInputStream ois = null;
		Tree t = null;

		try {
			ois = new ObjectInputStream(new BufferedInputStream(
					new FileInputStream("SAVE.txt")));

		} catch (FileNotFoundException e1) {
			e1.printStackTrace();

		} catch (IOException e1) {

			e1.printStackTrace();
		}

		try {

			try {

				t = (Tree) ois.readObject();

			} catch (IOException e) {

				e.printStackTrace();
			}
		} catch (ClassNotFoundException e) {

			e.printStackTrace();
		}

		try {
			ois.close();

		} catch (IOException e) {

			e.printStackTrace();
		}
		return t;
	}

	/**
	 * This method asks the player his/her name, assigns the input to the
	 * userName datamember.
	 * 
	 * @return String
	 * @throws IOException
	 */
	public String askName() throws IOException {
		if (!played) {
			outToClient.println("What is your name?");
			outToClient.flush();

			userName = inFromClient.readLine().trim();
		}
		return userName;
	}

	/**
	 * If this is the players first play through, this method will ask them
	 * their name. After, it will ask them if they wish to play the game. If it
	 * is not their first play through it just asks if they want to play the
	 * game.
	 */
	public void run() {
		try {
			askName();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		try {
			socket.setSoTimeout(0);
		} catch (SocketException e1) {
			e1.printStackTrace();
		}
		outToClient
				.println("Would you like to play a celebrity guessing game? (Enter quit to end game)");
		outToClient.flush();
		try {
			String response = inFromClient.readLine().trim().toLowerCase();
			String finalResponse = getUserAnswer(response);
			if (finalResponse.trim().contains("yes")) {

				tree.setExists();
				startGame(tree.getRoot());

			} else {
				outToClient.println("Quitting game...");
				outToClient.flush();
				this.socket.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method adds a new node to the existing tree. Then it writes the tree
	 * for later use.
	 * 
	 * @param n
	 * @throws IOException
	 * @throws SocketTimeoutException
	 */
	public void writeNewNode(Node n) throws IOException, SocketTimeoutException {
		lock.lock();
		try {
			socket.setSoTimeout(20000);
			outToClient.println("Who are you thinking of?");
			outToClient.flush();
			String person = inFromClient.readLine().trim();

			socket.setSoTimeout(20000);
			outToClient
					.println("Ask a yes/no question that would distguish between "
							+ n.getPerson() + " and " + person + "?");
			outToClient.flush();
			String question = inFromClient.readLine().trim();

			socket.setSoTimeout(20000);
			outToClient.println("Would an answer of yes indicate " + person + "?");
			outToClient.flush();
			String in = "";
			String response = inFromClient.readLine().trim().toLowerCase();
			String finalResponse = getUserAnswer(response);

			if (finalResponse.trim().contains("yes")) {
				in = "yes";
			} else {
				in = "no";
			}

			tree.put(n, person, question, in, ID);
			writeObject(tree);
			lock.unlock();

			outToClient.println("Thank you for adding " + person
					+ " to the database.");
			outToClient.flush();
			played = true;

		} catch (SocketTimeoutException ste) {

			outToClient.println("You took too long to add your new person!");
			played = true;
			if (lock.isLocked()) {
				lock.unlock();
			}
			socket.setSoTimeout(0);
		}
		socket.setSoTimeout(0);
		run();
	}

	/**
	 * This method checks to see if the node is a leaf. If it is, it locks the
	 * node and asks the user if the nodes person is who they are thinking of.
	 * If it is not the person they are thinking of it sends them to
	 * writeNewNode which then creates a new node. If the node is not a leaf, it
	 * sends them to askQuestion().
	 * 
	 * @param n
	 * @throws IOException
	 */
	public void startGame(Node n) throws IOException {
		checkMessages();
		if (n.getLeft() != null || n.getRight() != null) {
			askQuestion(n);
		}
		String msg = "";
		lock.lock();
		try {
			
			if (n.getLeft() == null && n.getRight() == null) {
				
				socket.setSoTimeout(20000);
				outToClient.println("Is your character " + n.getPerson() + "?");
				outToClient.flush();
				String response = inFromClient.readLine().trim().toLowerCase();
				String finalResponse = getUserAnswer(response);
				
				if (finalResponse.trim().contains("yes")) {
					socket.setSoTimeout(0);
					try {
						if (n.getConnectionInfo() != null) {
							
							String address = n.getConnectionInfo();
							String inString = userName
									+ " thought of your celebrity "
									+ n.getPerson() + " " + address + ".";
							messages.put(inString);
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					outToClient.println("I am so smart. Would you like to play again?");
					outToClient.flush();
					lock.unlock();
					response = inFromClient.readLine().trim().toLowerCase();
					finalResponse = getUserAnswer(response);
					if (finalResponse.trim().contains("yes")) {
						played = true;
						run();
					} else {
						
						outToClient.println("Quitting game...");
						outToClient.flush();
						socket.close();
					}
					
				} else {
					socket.setSoTimeout(0);
					try {
						
						lock.unlock();
						writeNewNode(n);
						
					} catch (SocketTimeoutException ste) {
						
						outToClient.println("You took too long, you have been timed out!");
						played = true;
						lock.unlock();
						run();
						ste.printStackTrace();
					}

				}
			}

			else {
				
				socket.setSoTimeout(0);
				lock.unlock();
				askQuestion(n);
			}
		} catch (SocketTimeoutException e) {
			
			outToClient.println("You took too long, you have been timed out!");
			played = true;
			lock.unlock();
			run();
			e.printStackTrace();
		}
	}

	/**
	 * Asks the user a question to narrow down the list of possible celebrities.
	 * If the user answers yes, they move to the right child node. If they
	 * answer no, they move to the left child node.
	 * 
	 * @param n
	 * @throws IOException
	 */
	public void askQuestion(Node n) throws IOException {

		outToClient.println(n.getQuestion());
		outToClient.flush();
		
		String response = inFromClient.readLine().trim().toLowerCase();
		String finalResponse = getUserAnswer(response);
		
		if (finalResponse.trim().contains("yes")) {
			
			startGame(n.getRight());
			
		} else if (finalResponse.trim().contains("no")) {
			
			startGame(n.getLeft());
			
		} else {
			
			outToClient.println("Your answer needs to be yes or no. Try again.");
			askQuestion(n);
		}
	}

	/**
	 * This method iterates over the static message queue, and adds any messages
	 * that belong to this game instance to the userMessages array.
	 */
	private void checkMessages() {
		String msg = "";
		int i = 0;
		ArrayList<String> auxilaryMessages = new ArrayList<String>();
		
		while (i < messages.size()) {
			// process msg
			msg = messages.poll();
			String[] msgSplit = msg.split("\\s");
			if (msg.contains(ID) && !msgSplit[0].equals(userName)) {

				userMessages.add(msg);

			} else {
				auxilaryMessages.add(msg);
			}
			i++;
		}
		try {
			for (String s : auxilaryMessages) {
				if (!s.equals(""))
					messages.put(s);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		displayMessage(userMessages);

	}

	/**
	 * This method displays all the messages in the userMessage array.
	 * 
	 * @param msgs
	 */
	private void displayMessage(ArrayList<String> msgs) {
		String outMsg = "";
		if (!msgs.isEmpty()) {

			for (String msg : msgs) {
				outMsg = "";
				String[] msgSplit = msg.split("\\s");

				for (int i = 0; i < msgSplit.length - 1; i++) {
					outMsg += msgSplit[i] + " ";
				}
				outToClient.println(outMsg);
				outToClient.flush();
			}
			userMessages.clear();
		}
	}

	/**
	 * Returns a yes or no string regardless of backspaces typed in the telnet
	 * prompt.
	 * 
	 * @param response
	 * @return
	 */
	private String getUserAnswer(String response) {
		String[] splitResponse = response.split("\b");
		String finalResponse = " ";
		for (String s : splitResponse) {
			if (s.trim().equals("yes") || s.trim().equals("no")) {
				finalResponse = s;
			}
		}
		return finalResponse;
	}
}