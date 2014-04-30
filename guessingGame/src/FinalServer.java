import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class FinalServer {

	/**
	 * Written by Alex Rymarz, Mike Hoye, and Drew Szlembarski
	 * 
	 * @param argv
	 * @throws Exception
	 */
	public static void main(String argv[]) throws Exception {

		ServerSocket inSock = new ServerSocket(6001);

		while (true) 
		{
			Socket connSock = null;
			try {

				connSock = inSock.accept();
				Game s = new Game(connSock);
				Thread t = new Thread(s);
				t.start();

			} catch (SocketException e) {

				System.err.println(e);
				e.printStackTrace();
				continue;
			}
		}
	}
}