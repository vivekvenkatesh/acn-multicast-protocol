/*Controller class
 *================
 *Author: Vivek Venkatesh Ganesan
 * ------------------------------
 *Functions
 *---------
 *1. Reads the Topology File and constructs the network
 * 
 *2. Monitors the output file of each node. 
 * 
 *3. If any new line is added to an output file in the network, it is placed in the corresponding input node.  
 *
 * */

import java.util.Set;
import java.util.HashSet;
import java.util.Scanner;
import java.io.*;


class controller
{
	private static final int RUNTIME = 150; // The running time of the simulation (in seconds) 
	private static final int MAXNODES = 10; // Since the maximum number of nodes is 10 

	// Maintain a Adjacency Matrix for Graph Representation
	// A Set Data strucuture to count the number of vertices in the toplogy file (We do not want to add repeating elements in the file)
	
	Set<Integer> vertex = new HashSet<Integer>();
	int adjacencyMatrix[][] = new int[MAXNODES][MAXNODES]; // As we have at most 10 nodes in our application
    int count;

	controller()
	{
		// Read from text file 'Topology' and determine the number of vertices (1st Pass)
		
		Scanner sc = null;
		try
		{
			sc = new Scanner(new File("topology")); // If you change this, change this at the bottom also
		} // End of Try
		catch(FileNotFoundException e)
		{
			e.printStackTrace();
		} // End of Catch
		while(sc.hasNextLine())
		{
			Scanner sd = new Scanner(sc.nextLine());
			boolean b;
			while(b = sd.hasNext())
			{
				String s = sd.next();
				int ver = Integer.parseInt(s);
				vertex.add(ver);
			}
		} // End of While
		
		sc.close(); // Close the scanner that read the file

		// Constructing the Adjacency Matrix (2nd Pass) Useful representation of a graph
		
		// Initialize the contents of the adjacency matrix to 0 initially
		
		for(int i=0; i< MAXNODES; i++)
		{
			for(int j=0; j<MAXNODES; j++)
			{
				adjacencyMatrix[i][j] = 0;
			}
		}
		try
		{
			sc = new Scanner(new File("topology"));
		} // End of Try
		catch(FileNotFoundException e)
		{
			e.printStackTrace();
		} // End of Catch
		while(sc.hasNextLine())
		{
			Scanner sd = new Scanner(sc.nextLine());
			boolean b;
			int from, to, count;
			from = to = 0;
			count = 0;
			while(b = sd.hasNext()) // This loop will run 2 times as each line has at most 2 entries
			{
				String s = sd.next();
				int ver = Integer.parseInt(s);
				if(count == 0)
				{
					from = ver;
					count++;
				}
				else
				{
					to = ver;
					count = 0;	
				}
			}
			adjacencyMatrix[from][to] = 1; 
		} // End of While
		
		sc.close(); // Close the scanner that read the file

		//printAdjacencyMatrix();

	} // End of Constructor

	void printAdjacencyMatrix()
	{
		// Print the Adjacency Matrix
		System.out.println("The Adjacency Matrix is ");
		for(int i : vertex)
		{
			for(int j : vertex)
			{
				System.out.print(" " + adjacencyMatrix[i][j]); 	
			}
			System.out.println();
		}
	}


	// Begin of Controller in the simulation
	
	void begin() throws IOException
	{
		count = 1;
		// Used to keep track if we have to create a new RandomAccessFile  everytime (We need to create new RandomAccessFile below only once)	
		RandomAccessFile readOutputFile[] = new RandomAccessFile[10]; /// 
		FileWriter writeInputFile[] = new FileWriter[MAXNODES];
		int track[] = new int[MAXNODES];
	
		for(int i=0;i<MAXNODES;i++)
			track[i] = 0;
	
		while(true)
		{
			try
			{
				Thread.sleep(1000);
			}
			catch(InterruptedException e)
			{
				System.out.println(e);
			}
			
			for(int i : vertex)
			{
				try
				{
					File f1 = new File("output_"+i);
					if(!f1.exists())
						continue;
					if(track[i] == 0)
					{
						readOutputFile[i] = new RandomAccessFile("output_"+i,"r"); 
						track[i] = 1; 

					}
					String latest = readOutputFile[i].readLine();
					while(!(latest == null))
					{

						// Suppose for Node 0, 1 and 3 is an incoming number
						// So at 10th sec, the controller will read both the hello and LSA packet of 1 and write it in input of 0
						// and then only go with the hello and LSA packet of 3, so hello messages and LSA won't be ordered in input file 
						
						// Read the latest line from the file and put it on the input file of its outgoing neighbors
						for(int p: vertex)
						{
							if(adjacencyMatrix[i][p] == 1)
							{
								
								writeInputFile[p] = new FileWriter(new File("input_"+p),true);
								writeInputFile[p].write(latest+"\n");
								writeInputFile[p].close();					
							}
							else
							{
								File temp = new File("input_" +p);
								if(!temp.exists())
									temp.createNewFile();
							}
						}
						latest = readOutputFile[i].readLine();	
						
					}
				}
				catch(FileNotFoundException e) // If file not found, go to next file, it may come later during intial creation
				{
					continue;
				}
			}

			if(count == RUNTIME)
			{
				for(int i : vertex)
				{
					// Close the file connections
					if(track[i] == 1)
						readOutputFile[i].close(); 
				}
				System.exit(0);
			}
			count++; // Used to keep track of time elapsed in the simulation
		}
	} // End of begin
	public static void main(String[] args) throws IOException
	{
		controller a = new controller();
		a.begin();
	}
} // End of Class
