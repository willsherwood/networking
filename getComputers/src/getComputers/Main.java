package getComputers;

import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * 
 */
public class Main {

	public static abstract class SocketRunnable implements Runnable {
		public SocketRunnable(Socket s) {
			this.socket = s;
		}
		public final Socket socket;
		public abstract void run();
		@Override public String toString() {
			return socket.getInetAddress().getHostName();
		}
	}
	
	public static void main(String[] args) throws Throwable {
		System.out.println("*"+liveConnections());
	}

	public static List<SocketRunnable> liveConnections() throws Throwable {
		final List<SocketRunnable> list = new ArrayList<>();
		Process p = Runtime.getRuntime().exec("net view");
		Scanner s = new Scanner(p.getInputStream());
		List<String> possibleHosts = new ArrayList<>();
		while (s.hasNextLine()) {
			String t = s.nextLine();
			StringBuffer o = new StringBuffer();
			for (int i = 2; i < t.length(); i++) {
				if (t.charAt(i) == ' ')
					break;
				o.append(t.charAt(i));
			}
			possibleHosts.add(o.toString());
		}
		List<InetAddress> networkAddresses = new ArrayList<>();
		for (String pr : possibleHosts) {
			try {
				networkAddresses.add(InetAddress.getByName(pr));
			} catch (Exception e) {
			}
		}
		for (InetAddress a : networkAddresses) {
			Socket r = new Socket();
			Thread t = new Thread(new SocketRunnable(r) {
				public void run() {
					try {
						r.connect(new InetSocketAddress(a, 80), 100);
						System.out.println("\n\nCONNECTION AT " + r + "\n");
						PrintWriter out = new PrintWriter(r.getOutputStream());
						out.println("Sherwood");
						out.flush();
						Scanner in = new Scanner(r.getInputStream());
						if (in.hasNextLine()) {
							String line = in.nextLine();
							System.out.println("#"+line);
							if (line.equals("sherwood - handshake"))
								list.add(this);
						}
						in.close();
					} catch (Throwable t) {
						System.out.print(a + " no connect");
					}
				}
			});
			t.start();
		}
		return list;
	}
}
