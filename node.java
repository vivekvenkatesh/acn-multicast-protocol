/* Node Class
 * ==========
 * Author: Vivek Venkatesh Ganesan
 * -------------------------------
 * Date: 04/14/2013
 * ----------------
 * Functions
 * ---------
 * 1. Represents a node in the network
 * 
 * 2. Every 5 seconds sends a Hello Message. 
 * 
 * 3. Every 10 seconds sends a Link State Advertisement. (LSA) 
 * 
 * 4. Identifies the entire topology based on the LSA. 
 * 
 * 5. Computes the shortest path between Source and Receiver. If (receiver) sends join messages. 
 * 
 * 6. If 'Sender' send data message once the shortest path tree estabilished. 
 *
 * */

import java.util.Scanner.*;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Set;
import java.util.HashSet;
import java.lang.Integer;
import java.lang.Character;
import java.util.ArrayList;

class node
{
	private static final int RUNTIME = 150; // Running time of the simulation
	private static final int MAXNODES = 10;
	int count = 0;
	int inputFileReadCount = 0;
	RandomAccessFile file;
	int id;	

	Set<Integer> incomingNeighbors = new HashSet<Integer>(); // IncomingNeighbors information from the Hello Protocol
	Set<Integer> networkNodes = new HashSet<Integer>();
	
	String timestamp; // Two digit time stamp for LinkState Advertisement
	int topology[][] = new int[MAXNODES][MAXNODES];  // The Topology (Adjacency Matrix) of the entire network
	int distanceMatrix[][] = new int[MAXNODES][MAXNODES]; // To construct the routing table for the network
	int next[][] = new int[MAXNODES][MAXNODES]; // For computing the path between any two pair of nodes

	int largestTimeStamp[] = new int[MAXNODES]; 
	int lastHello[] = new int[MAXNODES];
	int lastLSA[] = new int[MAXNODES];

	boolean isSender;
	boolean isReceiver;
	boolean isRealReceiver; 
	
	// Receiver Parameters
	ShortestPathTree spt[] = new ShortestPathTree[MAXNODES]; 
	
	class ShortestPathTree
	{
		int receiverSource; // Source for this receiver
		int receiverStartTime; // Start time for this receiver, that is when it starts to send the join message
		int receiverParentOnTree; // My Parent on the shortest Path Tree
		Set<Integer> receiverChildOnTree; 
		int lastJoinMessage; 
		
		ShortestPathTree()
		{
			receiverSource = -1;
			receiverStartTime = -1;
			receiverChildOnTree = new HashSet<Integer>();
			lastJoinMessage = -1;
		}
		
		void setReceiverSource(int src)
		{
			receiverSource = src; 
		}
		int getReceiverSource()
		{
			return receiverSource;
		}
		void setReceiverStartTime(int a)
		{
			receiverStartTime = a;
		}
		int getReceiverStartTime()
		{
			return receiverStartTime;
		}
		void setReceiverParentOnTree(int a)
		{
			receiverParentOnTree = a;
		}
		int getReceiverParentOnTree()
		{
			return receiverParentOnTree;
		}
		void addReceiverChildOnTree(int a)
		{
			receiverChildOnTree.add(a); 
		}
		Set getReceiverChildOnTree()
		{
			return receiverChildOnTree; 
		}
		void setLastJoinMessage(int a)
		{
			lastJoinMessage = a;
		}
		int getLastJoinMessage()
		{
			return lastJoinMessage; 
		}
	}
	
	// Sender Parameters
	
	int senderShortestPathTreeEstabilished; 
	int senderStartTime; 
	
	String senderDataMessage; 
	node(int i)
	{
		id = i;
		if(id >= 10)
		{
			System.out.println("Sorry, a node Id cannot be greater than 9. As maximum number of nodes is 10. ");
			System.exit(0); 
		}
		timestamp = "00";
		networkNodes.add(id);
		for(int k = 0; k<MAXNODES;k++)
		{
			for(int j=0;j<MAXNODES;j++)
			{
				next[k][j] = -1;
				topology[k][j] = 0;
				distanceMatrix[k][j] = 10000;
			}
		}

		for(int k=0;k<MAXNODES;k++)
		{
			largestTimeStamp[k] = -1;
			lastHello[k] = -1;
			lastLSA[k] = -1;
			spt[k] = new ShortestPathTree(); 
		}
	}

	void setSender(String msg)
	{
		isSender = true;
		senderShortestPathTreeEstabilished = 0;
		senderDataMessage = msg;
		senderStartTime = -1;
	}
	void setReceiver(int src)  
	{
		isReceiver = true;
		spt[src].setReceiverSource(src);
		
	}
	void setRealReceiverTrue() 
	{
		isRealReceiver = true;
	}

	int getNodeId()
	{
		return id;
	}
	
	// Function to generate string required for displaying incoming neigbhors in the file using a specified format

	String parseIncomingNeighbors(String inputString)
	{
		String temp="";
		for(int i=0;i<inputString.length();i++)
		{
			if(!(inputString.charAt(i) == '[' || inputString.charAt(i) == ' ' || inputString.charAt(i) == ',' || inputString.charAt(i) == ']'))
			{
				temp = temp + inputString.charAt(i) + " ";
			}
		}
		return temp;
	}
	
	void incrementTimeStamp()
	{
		int a;
		a = Integer.parseInt(timestamp);
		a++;
		if(a>=10)
			timestamp = Integer.toString(a);
		else
			timestamp = "0" + Integer.toString(a);
	}

	// ALL PAIR SHORTEST PATH ALGORITHM IS USED HERE TO COMPUTE THE SHORTEST PATH BETWEEN ANY TWO NODES
	
	void routingTableUpdate()
	{
		for(int i : networkNodes)
		{
			for(int j : networkNodes)
			{		
				//next[i][j] = -1;				
				if(topology[i][j] == 1)
					distanceMatrix[i][j] = 1;
				else
					distanceMatrix[i][j] = 10000;  
			}
		}
		for(int i : networkNodes)
		{
			distanceMatrix[i][i] = 0;
		}		
	    for(int k : networkNodes)
		{
			for(int i : networkNodes)
			{
				for(int j : networkNodes)
				{	
					if((distanceMatrix[i][k] + distanceMatrix[k][j]) < distanceMatrix[i][j])
					{
						distanceMatrix[i][j] = distanceMatrix[i][k] + distanceMatrix[k][j]; 	
						next[i][j] = k; 
					}
				}
			}
		}
	}	
	String findPath(int i,int j)
	{
			if(distanceMatrix[i][j] == 10000)
				return "No Path";
			int intermediate = next[i][j];
			if(intermediate == -1)
				return " ";
			else
				return findPath(i, intermediate) + intermediate + findPath(intermediate, j);
	}
	ArrayList path(int a, int b)
	{
		ArrayList<Integer> p = new ArrayList<Integer>();
		if(a==b)
		{
			p.add(a);
			p.add(b);
			return p;
		}
		String s = findPath(a,b);
		p.add(a);	
		
		for(int i=0;i<s.length();i++)
		{
			if(!(s.charAt(i) == ' '))
				p.add(Character.digit(s.charAt(i),10));
		}
		p.add(b);
		return p;
	}
	void sendHello() throws IOException
	{
		// Periodically Send Hello Message to outgoing neighbors
		// Write the output message to the node's output file

		FileWriter f = new FileWriter(new File("output_"+id),true);
		f.write("hello " + id+ "\n");
		f.close();
	}

	void sendLinkStateAdvertisement()
	{
		String incoming;
		if(!incomingNeighbors.isEmpty())
			incoming = parseIncomingNeighbors(incomingNeighbors.toString());
		else
			incoming = ""; 
		try
		{
			largestTimeStamp[id] = Integer.parseInt(timestamp); 
			FileWriter f = new FileWriter(new File("output_"+id), true);
			f.write("linkstate " + id + " " + timestamp + " " + incoming + "\n");
			f.close();
		}
		catch(IOException e)
		{
			System.out.println(e);
		}
		
	}
	void sendDataMessage() // Send data message after Shortest Path Tree Estabilished
	{
		String dataMessage = "data " + id + " " + id + " " + senderDataMessage; 
		try
		{ 
			FileWriter f = new FileWriter(new File("output_"+id), true);
			f.write(dataMessage + "\n");
			f.close();
		}
		catch(IOException e)
		{
			System.out.println(e);
		}

	}
	void sendJoinMessage(int src)
	{
		ArrayList<Integer> joinPath = new ArrayList<Integer>();
		joinPath = path(spt[src].getReceiverSource(), id);
		spt[src].setReceiverParentOnTree(joinPath.get(joinPath.size() - 2));
		ArrayList<Integer> pathToParentOnTree = new ArrayList<Integer>(path(id,spt[src].getReceiverParentOnTree())); 
		if(!pathToParentOnTree.contains(-1))
		{
		pathToParentOnTree.remove(0);
		pathToParentOnTree.remove(pathToParentOnTree.size() - 1); 
		String joinMessage = parseIncomingNeighbors(pathToParentOnTree.toString());
			try
			{
				FileWriter f1 = new FileWriter(new File("output_"+id), true);
				f1.write("join " + id +" " + spt[src].getReceiverSource() + " " + spt[src].getReceiverParentOnTree() + " " + joinMessage + "\n");
				f1.close();
			}
			catch(IOException e)
			{
				System.out.println(e); 
			}
		}
	}
	void checkForHello() // If an hello message is not received from a neighbor or more than 10 secs, then node failed
	{
		for(int i: incomingNeighbors)
		{
			if((count - lastHello[i] > 10) && (lastHello[i] != -1)) // This node has failed
			{
				for(int k=0;k<MAXNODES;k++)
					topology[i][k] = 0;
				for(int k=0;k<MAXNODES;k++)
					topology[k][i] = 0;
				networkNodes.remove(i); 
				routingTableUpdate();
			}
		}
		if(count>10)
			incomingNeighbors.retainAll(networkNodes);	
	}

	void checkForLSA() // If LSA not received for 30 seconds
	{
		Set<Integer> j = new HashSet<Integer>(networkNodes);
		for(int i: networkNodes)
		{
			if((count - lastLSA[i] > 30) && (lastLSA[i] != -1) && (i!=id)) // This node has failed
			{
				for(int k=0;k<MAXNODES;k++)
					topology[i][k] = 0;
				for(int k=0;k<MAXNODES;k++)
					topology[k][i] = 0;
				j.remove(i);
				routingTableUpdate();
			}
		}
		networkNodes.retainAll(j);	
	}
	void checkForJoin()
	{
		for(int i=0;i<MAXNODES;i++)
		{
			if(spt[i].getReceiverSource() != -1 && spt[i].getReceiverStartTime() != -1)
			{
				if((count - spt[i].getLastJoinMessage() > 20) && (spt[i].getLastJoinMessage() != -1)) // No Join message from its children
				{
					spt[i].setReceiverSource(-1);
					spt[i].setReceiverStartTime(-1);
					spt[i].getReceiverChildOnTree().clear();
					spt[i].setLastJoinMessage(-1);
				}
			}
		}
	}
	void readInputFile() throws IOException
	{
	  checkForHello();
	  checkForLSA();
	  checkForJoin();
	  File f = new File("input_"+id);
	  if(f.exists())
	  {
		if(inputFileReadCount == 0)
		{
			file = new RandomAccessFile("input_"+id,"r");
			inputFileReadCount = 1;
		}
		String latest = file.readLine();
		while(!(latest==null))
		{
			file.seek(file.getFilePointer());
			if(latest.charAt(0) == 'h') // If it is an hello message
			{
				String temp;
				temp = Character.toString(latest.charAt(6));
				int tempNeighbor = Integer.parseInt(temp);
				
				incomingNeighbors.add(tempNeighbor);
				// Update the incoming neighbor info in the topology 

				topology[tempNeighbor][id] = 1; // IMPORTANT TO GET WHOLE VIEW OF NETWORK	
				lastHello[tempNeighbor] = count; 			

			}
			if(latest.charAt(0) == 'l') // If this is a link state advertisement
			{
				// Forward it to your outgoing neigbhors
				// Process the LSA if the timestamp of the source from which it is received is greater than the largest seen TimeStamp so far
				// Keep track of the topology of the entire network
				// linkstate 9 00
				
				int sourceIdSeen = Character.digit(latest.charAt(10),10); // That is one that sends the LSA message to this node  
				String temp;	
				temp = Character.toString(latest.charAt(12)) + Character.toString(latest.charAt(13)); 
				int timeStampSeen = Integer.parseInt(temp);
				if(sourceIdSeen != id)
				{
					lastLSA[sourceIdSeen] = count; 
				}
				if(timeStampSeen > largestTimeStamp[sourceIdSeen] && !(sourceIdSeen == id))
				{ 	
					// Update the largest timestamp for that source
					// Pass this LSA message to the outgoing neighbors
					networkNodes.add(sourceIdSeen); // To keep track of the number of nodes in the network
					largestTimeStamp[sourceIdSeen] = timeStampSeen; 
					FileWriter f1 = new FileWriter(new File("output_"+id), true);
					f1.write(latest + "\n");
					f1.close();
					String incomingNeighborsOfSource = latest.substring(15); // Get the incoming neigbor info of the source that sends this LSA message and update the topology accordingly
					if(!(incomingNeighborsOfSource == null || incomingNeighborsOfSource == " ")) // Check if there is any incoming neighbor for the source that sent the LSA message to this node
					{
						for(int i=0;i<incomingNeighborsOfSource.length();i++)
						{
							if(!(incomingNeighborsOfSource.charAt(i) == ' '))
							{
								int currentReadIncomingNeighbor = Character.digit(incomingNeighborsOfSource.charAt(i),10);
								topology[currentReadIncomingNeighbor][sourceIdSeen] = 1; // Update the topology based on the LSA messages that is seen 							
							}
						}
					}
				}
			} // End of Check for LSA message
			if(latest.charAt(0) == 'j') // This is a join message
			{
				int lengthOfJoinMessage = latest.length();
				int pid = Character.getNumericValue(latest.charAt(9));
				int sid = Character.getNumericValue(latest.charAt(7));
				int idOfJoinSender = Character.getNumericValue(latest.charAt(5));
				spt[sid].setLastJoinMessage(count);
				if(lengthOfJoinMessage > 11)
				{
					int nextHopId = Character.getNumericValue(latest.charAt(11));
					
					// Check if the nextHopId mentioned is that of my ID
					
					if(id == nextHopId)
					{
						// I have to forward the message along the path mentioned in the message
						String forwardJoinMessage; 
			
						forwardJoinMessage = "join " + idOfJoinSender + " " + sid + " " + pid + " " + latest.substring(13);  
					
						
						FileWriter f1 = new FileWriter(new File("output_"+id), true);
						f1.write(forwardJoinMessage + "\n");
						f1.close();
						
					}
				}
				else // Means that the Join Message has reached the parent on the tree or the source
				{
					
					if(sid == id)
					{
						// This is the source
						if(isSender)
						{
							senderShortestPathTreeEstabilished = 1; 
							if(senderStartTime == -1)
								senderStartTime = count; 
						}
					}
					else if(pid == id)
					{
						// Repeat the process by finding a route to the source from my node
						setReceiver(sid); 
						spt[sid].addReceiverChildOnTree (idOfJoinSender);
					}
				}
				
				
				
			} // End of Check for Join Message
			if(latest.charAt(0) == 'd')
			{
				if(isReceiver && !isSender)
				{
					
					int receivingFrom = Character.getNumericValue(latest.charAt(5));
					int receivingSource = Character.getNumericValue(latest.charAt(7));
					if(receivingFrom == spt[receivingSource].getReceiverParentOnTree() && receivingSource == spt[receivingSource].getReceiverSource())
					{
						String forwardDataMessage;
						forwardDataMessage = "data " + id + " " + receivingSource + " " + latest.substring(9); 
						if(!spt[receivingSource].getReceiverChildOnTree().isEmpty()) // If EMPTY then it is the leaf on the shortest path tree
						{
							try
							{
								FileWriter f1 = new FileWriter(new File("output_"+id), true);
								f1.write(forwardDataMessage + "\n");
								f1.close();
							}
							catch(IOException e)
							{
								System.out.println(e); 
							}
						}
						else // This is the child (real receiver) on the tree
						{ 
							if(isRealReceiver == true)
							{
								String finalDataFile = id + "_received_from_" + receivingSource;
								String finalDataMessage = "data " + receivingFrom + " " + receivingSource + " " + latest.substring(9);
								try
								{
									FileWriter f1 = new FileWriter(new File(finalDataFile), true);
									f1.write(finalDataMessage + "\n");
									f1.close();
								}
								catch(IOException e)
								{
									System.out.println(e); 
								}
							}
						}
					}
				}
			} // End of Check for Data Message
			latest = file.readLine();
		}	
	  }
	  routingTableUpdate();  // Update the Routing Table computed So far
	  
	} // End of Read Input File

	void begin() throws IOException  // Beginning of Node process in the simulation
	{
		count = 1;
		while(true) // This part of code gets continuously executed during the runtime of simulation 
		{
			try
			{
				Thread.sleep(1000);
			}
			catch(InterruptedException e)
			{
				System.out.println(e);
			}
			if(count % 5 == 0) // Send an hello message every 5 seconds
				sendHello();
			if(count % 10 == 0) // Send Link State Advertisement every 10 secongs
			{
				sendLinkStateAdvertisement(); 
				incrementTimeStamp();
			}
			if(isSender)
			{
				if(senderShortestPathTreeEstabilished == 1 && ((count - senderStartTime) % 10 == 0 ))
				{
					sendDataMessage();
				}
			}
			if(isReceiver)
			{
					for(int i=0;i<MAXNODES;i++)
					{
						if(spt[i].getReceiverSource() != -1)
						{
							ArrayList<Integer> temp = new ArrayList<Integer>(path(spt[i].getReceiverSource(),id));
							if((spt[i].getReceiverStartTime() == -1) && !temp.contains(-1))
							{
								if(!path(id,temp.get(temp.size()-2)).contains(-1)) 
								// Make sure this node has a path to the parent on the tree
								{	
									spt[i].setReceiverStartTime(count); 
								}
							}
							if((spt[i].getReceiverStartTime() != -1) && ((count - spt[i].getReceiverStartTime())%10) == 0 )
									sendJoinMessage(i);
						}
					}
			}
			
			readInputFile(); // Read the input file to see if there is any message
			
			if(count == RUNTIME)
			{				
				//printNewtorkTopology();
				System.exit(0);
			}
			count++;
		}
	}
	void printDistanceMatrix()
	{	
		System.out.println("The Distance Matrix seen from "+id+"is ");
		for(int i : networkNodes)
		{
			for(int j : networkNodes)
			{
				System.out.print(" " + distanceMatrix[i][j]); 	
			}
			System.out.println();
		}
	}
	void printNewtorkTopology()
	{
		System.out.println("The Network Topology seen from "+id+"is ");
		for(int i : networkNodes)
		{
			for(int j : networkNodes)
			{
				System.out.print(topology[i][j] + " ");
			}
			System.out.println();
		}
	}

	public static void main(String[] args) throws IOException
	{
		// Just process the command line arguments to identify if it is a sender / receiver / an independent process
		node a = null; 
		if(args.length == 0)
		{
			System.out.println("Incorrect Arguments! Please verify your scenario file or command");
			System.exit(0);
		} // End of If
		else if(args.length == 1)
		{
			String temp = args[0];
			a = new node(Integer.parseInt(temp));
		} // End of Else If
		else if(args.length > 1)
		{
			if(args.length != 3)
			{
				System.out.println("Incorrect Arguments! Please verify your scenario file or command");
				System.exit(0);
			}
			else
			{
				if(args[1].equals("sender"))
				{
					// This is a sender
					a = new node(Integer.parseInt(args[0]));
					a.setSender(args[2]);
				} // End of If
				else if(args[1].equals("receiver"))
				{
					// This is a receiver 
					a = new node(Integer.parseInt(args[0]));
					a.setRealReceiverTrue(); 
					try
					{
						a.setReceiver(Integer.parseInt(args[2]));
					}
					catch(NumberFormatException e)
					{
						System.out.println("Please enter valid source id for this receiver!");
						System.exit(0);
					}
				} // End of Else If
			} // End of Else
		} // End of Else If
		
		a.begin(); // Begin the simulation


	} // End of Main
} // End of class

