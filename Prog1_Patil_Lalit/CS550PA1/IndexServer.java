
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;


public class IndexServer {
	static String content = "";
	ServerSocket IServerSocket;
	private static HashMap<Integer,String> PeerList = new HashMap< Integer,String>();
	private static HashMap<Integer, File[]> PeerFileList = new HashMap<Integer, File[]>();
	private static File[] files;


	public IndexServer(int port) 
	{ 
		try
		{ 
			IServerSocket = new ServerSocket(port,0,InetAddress.getLocalHost());
			System.out.println("INDEX SERVER IS UP AND RUNNING... ");
			content += "Index server started\n";
		} 
		catch(IOException ioe) 
		{ 
			System.out.println("Error!"); 
			System.exit(-1); 
		} 


		while(true) 
		{                        
			try
			{ 
				// Accept incoming connections. 
				Socket clientSocket = IServerSocket.accept(); 
				ClientServiceThread cliThread = new ClientServiceThread(clientSocket);
				cliThread.start();

			} 
			catch(IOException ioe) 
			{ 
				System.out.println("Somethings gone wrong, couldnt connect client. Stack Trace :"); 
				ioe.printStackTrace(); 
			} 

		}



	} 

	public static void main (String[] args) 
	{ 
		new IndexServer(Integer.parseInt(args[0]));        
	} 



	class ClientServiceThread extends Thread 
	{ 	
		
		Socket clientSocket;
		private PrintStream os;
		private BufferedReader in = null;

		public ClientServiceThread() 
		{ 
			super(); 
		} 

		ClientServiceThread(Socket s) 
		{ 
			clientSocket = s; 

		} 
		
		
		
			
		
		
		public void run() 
		{            

			try{
				Writer writer;
				
					writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("output.txt"), "utf-8"));
				
				in = new BufferedReader(new InputStreamReader(
						clientSocket.getInputStream()));
				
				OutputStream os = clientSocket.getOutputStream();
				OutputStreamWriter osw = new OutputStreamWriter(os);
				BufferedWriter bw = new BufferedWriter(osw);

				String clientSelection;
				while ((clientSelection = in.readLine()) != null) {
					switch (clientSelection) {
					case "1":

						String peerSockValue = in.readLine();
						String fCount = in.readLine();
						int filesCount = Integer.parseInt(fCount);
						File[] files = new File[filesCount];

						for(int i = 0; i < filesCount; i++)
						{
							String fileName = in.readLine();
							files[i] = new File(fileName);
						}

						int ID = new IdGenerator().newID();
						PeerList.put(ID,peerSockValue );
						System.out.println("\nClient registred : "+ ID);
						content += "client "+ID +"connected.\n";
						content += "Number of files registered: "+filesCount+"\n\n";
						PeerFileList.put(ID,files);
						files = new File[0];
						break;


					case "2":

						List<File> searchfiles = new ArrayList<File>();
						List<Integer> peerArray = new ArrayList<Integer>();
						String fileName = in.readLine();
						content += "New file search query: "+fileName+"\n";
						if(IndexServer.PeerFileList.containsValue(fileName));
						{
							for(int i=0;i<PeerFileList.size();i++){
								for(int k=0;k<PeerFileList.get(i).length;k++)
									searchfiles.add(PeerFileList.get(i)[k]);
								for(int j=0;j<searchfiles.size();j++){
									if(fileName.equals(searchfiles.get(j).toString())){
										peerArray.add(i);
									}
								}
								searchfiles.clear();
							}
						}

						content += "Search result:\n";
						if(peerArray.size()>0){

							bw.write("\nFile Available at peer number: \n");
							bw.flush();
							String response = "";
							for(int i= 0;i<peerArray.size();i++){
								response = response + peerArray.get(i)+"\t port "+ PeerList.get(peerArray.get(i))+":";

							}
							bw.write(response+"\n");
							bw.flush();
							content += response.replace(":", "\\n");
							int peer = Integer.parseInt(in.readLine())-'0';
							content += "Client selected peer to download from: "+peer+"\n"; 
							try{
								if(peer != -1){

									bw.write(PeerList.get(peer).toString());
									bw.newLine();
									bw.flush();
								}
								content += "File downloaded\n";
							}catch(NullPointerException n){
								System.out.println("Download Cancelled");
								content += "Download cancelled by client\n";
								bw.write("Download Cancelled\n");
								bw.flush();
							}

						}else{

							bw.write("\nFile does not exist!\n");
							bw.flush();
							content += "File does not exist\n";
						}

					} 


				} 
				writer.write(content/*.replaceAll("\\n", System.lineSeparator())*/);
				writer.flush();
				writer.close();
			}catch(IOException ioe) 
			{ 
				ioe.printStackTrace(); 
			} 
		}
	}
}
