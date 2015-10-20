import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;


public class TestPeer {

	private static Socket sock;
	private static String fileName;
	private static BufferedReader stdin;
	private static PrintStream os;
	private static Map<Integer, String> portList = new ConcurrentHashMap<Integer, String>();
	private static Map<Integer, String> hostNameList = new ConcurrentHashMap<Integer, String>();
	private static Map<String, String> KeyMap = new ConcurrentHashMap<String, String>();
	private static List<Socket> PeerSockets = new ArrayList<Socket>();
	private static int flag = 0;
	public TestPeer(){

		Properties prop = new Properties();
		InputStream input = null;
		Socket s = null;

		try {

			input = new FileInputStream("config.properties");

			// load a properties file
			prop.load(input);

			// get the property value and print it out

			for(int i=0; i<prop.getProperty("hostname").split(";").length;i++){
				portList.put(i, prop.getProperty("port").split(";")[i]);
				hostNameList.put(i, prop.getProperty("hostname").split(";")[i]);
			}


		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void main(String[] args) throws IOException {
		init();
		startPeer();
		startClient();
	}

	public static void startClient() {

		(new Thread() {
			@Override
			public void run() {

				char choice = 1;
				do{

					System.out.println("1. Put Key");
					System.out.println("2. Get Key");
					System.out.println("3. Delete Key");
					System.out.println("4. Exit");
					System.out.println("Make selection: ");
					try {
						choice = (char) System.in.read();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					switch (choice) {
					case '1':
						try {
							PutKey();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						break;
					case '2':
						try {
							GetKey();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						break;
					case '3':
						try {
							DelKey();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						break;
					}
				}while(choice!='4');
			}
		}).start();
	}

	static void PutKey() throws IOException{
		initPeerSockets();
		Socket sock1;
		String key = "";
		String val = "abc";

		long start = System.nanoTime();
		for(int i=600000; i<700000;i++){
			System.out.println(i);
			key = String.valueOf(i);

			String message = key+":"+val;
			int NodeId = getNode(key);
			sock1 = PeerSockets.get(NodeId);
			PrintStream os  = new PrintStream(sock1.getOutputStream());
			os.println("1");
			os.flush();
			os.println(message);
			os.flush();
		}
		long end = System.nanoTime();
		System.out.println("time with single thread: " + (end - start) / 100000);


	}

	static void initPeerSockets(){

		if(flag == 0){
			for(int i=0;i<portList.size();i++){
				try {
					PeerSockets.add(new Socket(InetAddress.getLocalHost(),Integer.parseInt(portList.get(i))));
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		flag = 1;
	}
	static int getNode(String key){
		int hash = hashIt(key);
		int node = hash%8;
		return node;
	}
	static int hashIt(String key){
		int hash = 0,i,len;
		char chr;
		if (key.length() == 0) return hash;
		for (i = 0, len = key.length(); i < len; i++) {
			chr   = key.charAt(i);
			hash  = hash * 31 + chr;
			hash |= 0; // Convert to 32bit integer
		}
		return Math.abs(hash);
	}

	static void GetKey() throws IOException{
		initPeerSockets();
		Socket sock1;
		String key ="";
		long start = System.nanoTime();
		for(int i=600000; i<700000;i++){
			System.out.println(i);
			key = String.valueOf(i);
			int NodeId = getNode(key);
			sock1 = PeerSockets.get(NodeId);
			InputStream is = null;
			try {
				is = sock1.getInputStream();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			PrintStream os  = new PrintStream(sock1.getOutputStream());
			os.println("2");
			os.flush();
			os.println(key);
			os.flush();
			String value = br.readLine();
			System.out.println("Value: "+value);
		}
		long end = System.nanoTime();
		System.out.println("time with single thread: " + (end - start) / 100000);
	}

	static void DelKey() throws IOException{
		initPeerSockets();
		Socket sock1;
		String key ="";
		long start = System.nanoTime();
		for(int i=600000; i<700000;i++){
			System.out.println(i);
			key = String.valueOf(i);
			int NodeId = getNode(key);
			sock1 = PeerSockets.get(NodeId);
			InputStream is = null;
			try {
				is = sock1.getInputStream();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			PrintStream os  = new PrintStream(sock1.getOutputStream());
			os.println("3");
			os.flush();
			os.println(key);
			os.flush();
			String value = br.readLine();
			System.out.println(value);
		}
		long end = System.nanoTime();
		System.out.println("time with single thread: " + (end - start) / 100000);
	}
	public static void startPeer() {
		(new Thread() {
			@Override
			public void run() {
				ServerSocket PeerSocket = null;
				try {
					System.out.println("\nPeer");
					PeerSocket = new ServerSocket(Integer.parseInt(portList.get(5)),0,InetAddress.getLocalHost());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				while(true) 
				{                        
					// Accept incoming connections. 
					Socket clientSocket1 = null;
					try {
						clientSocket1 = PeerSocket.accept();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					PeerServiceThread cliThread = null;
					try {
						cliThread = new PeerServiceThread(clientSocket1);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					cliThread.start();
				}            
			}
		}).start();
	}

	static class PeerServiceThread extends Thread 
	{ 
		Socket clientSocket1;
		boolean m_bRunThread = true; 
		private BufferedReader in = null;
		OutputStream os;

		public PeerServiceThread() 
		{ 
			super(); 
		} 

		PeerServiceThread(Socket s) throws IOException 
		{ 
			clientSocket1 = s; 

		} 

		public void run() 
		{
			try {
				in = new BufferedReader(new InputStreamReader(
						clientSocket1.getInputStream()));



				os = clientSocket1.getOutputStream();

				OutputStreamWriter osw = new OutputStreamWriter(os);
				BufferedWriter bw = new BufferedWriter(osw);

				String clientSelection;
				while ((clientSelection = in.readLine()) != null) {
					switch (clientSelection) {
					case "1":
						String Message = "";
						try {
							Message = in.readLine();

						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						String key = Message.split(":")[0];
						String value = Message.split(":")[1];
						KeyMap.put(key, value);
						Iterator<Map.Entry<String, String>> i = KeyMap.entrySet().iterator(); 
						break;

					case "2":
						key = "";
						try {
							key = in.readLine();

						} catch (IOException e) {
							// TODO Auto-generated catch block
							System.out.println("Cannot read get key from client");
							e.printStackTrace();
						}
						if(KeyMap.containsKey(key)){
							value = KeyMap.get(key);
							bw.write(value+"\n");
							bw.flush();
						}else{
							bw.write("Not Found!!!"+"\n");
							bw.flush();
						}
						break;
					case "3":
						key = "";
						try {
							key = in.readLine();

						} catch (IOException e) {
							// TODO Auto-generated catch block
							System.out.println("Cannot read get key from client");
							e.printStackTrace();
						}
						if(KeyMap.containsKey(key)){
							KeyMap.remove(key);
							bw.write("Deleted!"+"\n");
							bw.flush();
						}else{
							bw.write("Not Found!!!"+"\n");
							bw.flush();
						}
						break;
					}
				}
			} catch (IOException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
		}
	}



	public static void init(){
		new TestPeer();
	}

}
