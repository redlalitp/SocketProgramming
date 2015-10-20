import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Writer;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.io.ByteArrayOutputStream;

public class Peer {
	
	static int clientPort;
	static int peerPort;
	//static String content;
	public static void main(String[] args) throws IOException {

		switch(args[0].trim()){
		case "-c":
			clientPort = Integer.parseInt(args[1]);
			peerPort = Integer.parseInt(args[3]);
			break;
		case "-p":	
			clientPort = Integer.parseInt(args[3]);
			peerPort = Integer.parseInt(args[1]);
			break;
		default:
			System.out.println("\nInvalid option.\n -p : Peer\n -c : Client\nuse: java Peer -c <clientport> -p <pperport>");	
		}

		startPeer();

		startClient();
	}

	public static void startClient() {

		(new Thread() {
			@Override
			public void run() {
				
				char choice = 1;
				do{

					System.out.println("1. Register");
					System.out.println("2. Search file");
					System.out.println("3. Exit");
					System.out.println("Make selection: ");
					try {
						choice = (char) System.in.read();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					switch (choice) {
					case '1':
						Register();
						break;
					case '2':
						try {
							Search();
						} catch (UnknownHostException e) {
							System.out.println("Unknown host");	
							e.printStackTrace();
						} catch (IOException e) {
							System.out.println("Problem with IO");
							e.printStackTrace();
						}
						break;
					}
				}while(choice!='3');
			}
				
		}).start();
		}
		
		static void Register(){
			try {
				//Writer writer = new BufferedWriter(new OutputStreamWriter(
			    //        new FileOutputStream("output.txt"), "utf-8"));
				Socket sock = new Socket(InetAddress.getLocalHost(),Peer.clientPort);
				//content += "Client connected to Index Server";
				//writer.flush();
				PrintStream os  = new PrintStream(sock.getOutputStream());

				os.println("1");
				os.flush();

				BufferedOutputStream bos = null;

				bos = new BufferedOutputStream(sock.getOutputStream());

				DataOutputStream dos = new DataOutputStream(bos); 
				System.out.println(Peer.peerPort);
				os.println(Peer.peerPort);
				os.flush();

				File dir = new File("./files");
				File[] filesList = dir.listFiles();

				//dos.writeInt(filesList.length);
				os.println(filesList.length);
				os.flush();
				System.out.println(filesList.length);
				//content +="\nNumber of files registred: "+filesList.length+"\n\n";
				//writer.flush();
				for(File file : filesList)
				{
					String name = file.getName();
					//dos.writeUTF(name);
					os.println(name);


				}
				
				//writer.close();
				dos.close();
				sock.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}


		}
		
		static void Search() throws UnknownHostException, IOException{
			Socket sock = new Socket(InetAddress.getLocalHost(),Peer.clientPort);
			BufferedReader stdin;
			PrintStream os  = new PrintStream(sock.getOutputStream());
			BufferedReader in;
			/*try {
				sock = new Socket("localhost", Peer.clientPort);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
			try {
				stdin = new BufferedReader(new InputStreamReader(
						sock.getInputStream()));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			InputStream is = null;
			try {
				is = sock.getInputStream();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			try {
				os = new PrintStream(sock.getOutputStream());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			os.println("2");
			Scanner input = new Scanner(System.in);
			input.nextLine();	
			System.out.println("Enter file name: ");
			try {	
				String fileName = input.nextLine();
				os.println(fileName);
				String message;

				message = br.readLine();
				System.out.println(message);
				message = br.readLine();
				System.out.println(message);
				message = br.readLine();
				String strArray[] = message.split(":");
				for(int i=0;i<strArray.length;i++)
					System.out.println(strArray[i]);
				System.out.print("Select Peer to download file\nEnter -1 to cancel: ");
				int peer = System.in.read();
				os.println(peer);
				if(peer == -1){
					message = br.readLine();
					System.out.println(message);
				}
				String port = br.readLine();
				Socket sock1 = new Socket(InetAddress.getLocalHost(),Integer.parseInt(port));
				System.out.println("Connection established...");
				os = new PrintStream(sock1.getOutputStream());
				os.println(fileName);
				is = sock1.getInputStream();

				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				byte[] aByte = new byte[1];
				int bytesRead;
				//final String fileOutput = "files\\"+fileName;
				final String fileOutput = "./files/"+fileName;
				if (is != null) {

					FileOutputStream fos = null;
					BufferedOutputStream bos1 = null;

					fos = new FileOutputStream(fileOutput);
					bos1 = new BufferedOutputStream(fos);
					bytesRead = is.read(aByte, 0, aByte.length);

					do {
						baos.write(aByte);
						bytesRead = is.read(aByte);
					} while (bytesRead != -1);

					bos1.write(baos.toByteArray());
					bos1.flush();
					bos1.close();
					sock1.close();
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		public static void startPeer() {
			(new Thread() {
				@Override
				public void run() {
					ServerSocket PeerSocket = null;
					try {
						System.out.println("\nPeer");
						PeerSocket = new ServerSocket(Peer.peerPort,0,InetAddress.getLocalHost());
						System.out.println("\nPeers up, waiting...");
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

						PeerServiceThread cliThread = new PeerServiceThread(clientSocket1);
						System.out.println("Peer connected");
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

			public PeerServiceThread() 
			{ 
				super(); 
			} 

			PeerServiceThread(Socket s) 
			{ 
				clientSocket1 = s; 

			} 

			public void run() 
			{ 
				BufferedOutputStream outToClient = null;
				String fileToSend = null;

				try {
					in = new BufferedReader(new InputStreamReader(clientSocket1.getInputStream()));
					outToClient = new BufferedOutputStream(clientSocket1.getOutputStream());
					fileToSend = in.readLine();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				if (outToClient != null) {
					//File myFile = new File( "files\\"+fileToSend );
					File myFile = new File( "./files/"+fileToSend );
					byte[] mybytearray = new byte[(int) myFile.length()];

					FileInputStream fis = null;

					try {
						fis = new FileInputStream(myFile);
					} catch (FileNotFoundException ex) {
						System.out.println("File "+myFile+" not found!\n"+ex.getStackTrace());
					}
					BufferedInputStream bis = new BufferedInputStream(fis);

					try {
						bis.read(mybytearray, 0, mybytearray.length);
						outToClient.write(mybytearray, 0, mybytearray.length);
						outToClient.flush();
						outToClient.close();

						// File sent, exit the main method
					} catch (IOException ex) {
						System.out.println("Error sending file! trace:"+ex.getStackTrace());
					}
				} 


			}
		}

	}

