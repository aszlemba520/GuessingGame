import java.io.Serializable;
import java.util.ArrayList;

/**
 * Written by Alex Rymarz, Mike Hoye, and Drew Szlembarski
 * 
 * @param argv
 * @throws Exception
 */

public class Tree implements Serializable {

	private Node root;
	private int size;
	private boolean exists = false;

	/**
	 * Adds all the nodes in the tree into an arraylist, then returns that
	 * arraylist.
	 * 
	 * @param n
	 * @return
	 */
	public static ArrayList<Node> getNodes(Node n) {
		ArrayList<Node> nodes = new ArrayList<Node>();
		if (n == null) {
			return nodes;
		}
		nodes.add(n);
		nodes.addAll(getNodes(n.getLeft()));
		nodes.addAll(getNodes(n.getRight()));

		return nodes;
	}

	/**
	 * Sets the root of the tree
	 * 
	 * @param key
	 * @param value
	 */
	public void putRoot(String key, String value) {
		size++;
		root = new Node(key, value);
	}

	/**
	 * Returns the root of the tree
	 * 
	 * @return
	 */
	public Node getRoot() {
		return root;
	}

	/**
	 * Sets a new node as the left or right child of the node passed in
	 * depending on the user's input. Then it changes the question of the node
	 * passed in, to the question the user input, and takes the person of the
	 * node passed in and sets it to the left or right child depending on the
	 * users input. It also sets the users Session ID for the new left or right
	 * child node.
	 * 
	 * @param n
	 * @param key
	 * @param value
	 * @param userInput
	 * @param sessionID
	 * @return
	 */
	public Node put(Node n, String key, String value, String userInput, String sessionID) {
		if (n == null) {
			
			size++;
			return new Node(key, value);
			
		} else {
			
			size++;
			
			if (userInput.toLowerCase().equals("yes")) {
				
				Node temp = new Node(n.getPerson(), "");
				Node right = new Node(key, "");
				n.setLeft(temp);
				n.setRight(right);
				n.setQuestion(value);
				n.setPerson("");
				right.setConnectionInfo(sessionID);
				
			} else if (userInput.toLowerCase().equals("no")) {
				
				Node left = new Node(key, "");
				Node temp = new Node(n.getPerson(), "");
				n.setLeft(left);
				n.setRight(temp);
				n.setQuestion(value);
				n.setPerson("");
				left.setConnectionInfo(sessionID);
			}

		}
		return n;
	}

	/**
	 * Returns the size of the tree.
	 * 
	 * @return
	 */
	public int getSize() {
		return size;
	}

	/**
	 * Sets the boolean variable to true, indicating that a tree exists.
	 * 
	 * @return
	 */
	public void setExists() {
		exists = true;
	}

	/**
	 * Returns True is a tree object exists, False otherwise.
	 * 
	 * @return
	 */
	public boolean getExists() {
		return exists;
	}
}
