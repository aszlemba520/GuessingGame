import java.io.Serializable;
import java.net.InetAddress;

/**
 * Written by Alex Rymarz, Mike Hoye, and Drew Szlembarski
 * 
 * @param argv
 * @throws Exception
 */
class Node implements Serializable {
	
	private String person;
	private String question;
	private String info;
	private Node left, right;

	public Node() {
		person = "";
		question = "";
	}

	public Node(String key, String value) {
		person = key;
		question = value;
	}

	/**
	 * Returns this nodes question.
	 * 
	 * @return
	 */
	public String getQuestion() {
		return question;
	}

	/**
	 * Returns this nodes person.
	 * 
	 * @return
	 */
	public String getPerson() {
		return person;
	}

	/**
	 * Returns this nodes right child.
	 * 
	 * @return
	 */
	public Node getRight() {
		return right;
	}

	/**
	 * Returns this nodes left child.
	 * 
	 * @return
	 */
	public Node getLeft() {
		return left;
	}

	/**
	 * Sets the left child of this node.
	 * 
	 * @return
	 */
	public void setLeft(Node l) {
		left = l;
	}

	/**
	 * Sets the right child of this node.
	 * 
	 * @return
	 */
	public void setRight(Node r) {
		right = r;
	}

	/**
	 * Sets the question of this node.
	 * 
	 * @return
	 */
	public void setQuestion(String str) {
		question = str;
	}

	/**
	 * Sets the person of this node.
	 * 
	 * @return
	 */
	public void setPerson(String p) {
		person = p;
	}

	/**
	 * Sets the SessionID of this node.
	 * 
	 * @return
	 */
	public void setConnectionInfo(String n) {
		info = n;
	}

	/**
	 * Returns the Session ID of this node.
	 * 
	 * @return
	 */
	public String getConnectionInfo() {
		return info;
	}
}
