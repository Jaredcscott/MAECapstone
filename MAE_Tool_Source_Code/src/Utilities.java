import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;

/*
 * @author jared
 */
public class Utilities {
    
    //===============================MISC METHODS===============================\\
    public static boolean compareNames(String[] name1, String[] name2) {
        //Method used to compare two names to see if they are the same. 
        int simScore = 0;
        //Is the last name and the first name the same? 
        if ((name1[0].equals(name2[0])) && (name1[name1.length-1].equals(name2[name2.length-1]))){
            return true;
        }
        if (name1[0].equals(name2[0])) {
            //Last name is the same
            simScore += 5;
            //Checking first name
            if(!(name1[name1.length-1].equals(name2[name2.length-1]))){
                return false;
            }
        }
        else {
            //Checking all of the words in the names against each other
            if (name1[0].equals("Zack") && name2[0].equals("Zachary")) {
                if (name1[1].equals("Bell") && name2[1].equals("Bell")) {
                    return true;
                }
            }
            if (name1.length == 3) {
                if (name1[2].equals("Van") && name1[1].equals(name2[0])) {
                    return true;
                }
            }
            if(!(name1[name1.length-1].equals(name2[name2.length-1])) && name2.length == 2){
                return false;
            }
            
            for (String seg : name2) {
                if (seg.equals(name1[0])) {
                    simScore += 5;
                }
            }
            if (simScore == 0) {
                return false;
            }
        }
        
        for (int i = 1; i < name1.length ; i++ ) {
            for (int j = 0; j < name2.length; j++) {
                if (name1[i].equals(name2[j])) {
                    simScore += 2;
                }
            }
        }
        if (simScore >= 7) {
            //Names are similar enough to consider them a match
            return true;
        }
        else {
            return false;
        }
    }
    
    public static String[] getRow(String fileNameCanvas,int rowInt) {
        //Pulls a single row from a data file 
        String row = "";
        try {   
            //Setting up a reader
            BufferedReader csvReader = new BufferedReader(new FileReader(fileNameCanvas));
            int count = 0;
            //Counting to the desired row
            while (count < rowInt) {
                count += 1;
                row = csvReader.readLine();
                if (count == rowInt) {
                    break;
                }  
            }
            csvReader.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        if (row == null) {
            //Empty row
            return new String[0];
        }
        else {
           return row.split(","); //Returning the desired row. 
        }
    }
    
    static void storeObjects() {
        //Used to store data to a file for persistance 
        try {
            FileOutputStream out = new FileOutputStream(MAEGradingTool.TEAM_FILE);
            ObjectOutputStream oOut = new ObjectOutputStream(out);
            for (TeamSortingTool.Team team : TeamSortingTool.teamsArray) {
               oOut.writeObject(team);
            }
            out.close();
            oOut.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        try {
            String fileName = MAEGradingTool.BEST_TEAM_OUTPUT + ".txt";
            FileWriter writer = new FileWriter(fileName, false);
            String output = "";
            int totalPreference = 0;
            for (TeamSortingTool.Team team : TeamSortingTool.teamsArray) {
                totalPreference += team.preferenceScore;
            }
            output += "\nSum of all preference scores (Lower is better): " + totalPreference + "\n";
            for (TeamSortingTool.Team team : TeamSortingTool.teamsArray) {
                output += team.toString();
            }
            writer.write(output);
            writer.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public static class MinHeap { 
        public Node[] Heap; 
        private int size; 
        private int maxsize; 

        private static final int FRONT = 1;

        public MinHeap(int maxsize) 
        { 
            this.maxsize = maxsize; 
            this.size = 0; 
            Heap = new Node[this.maxsize + 1]; 
            Heap[0] = new Node(new TeamSortingTool.Sort(new ArrayList<TeamSortingTool.Team>())); 
        } 
        
        public class Node {
            TeamSortingTool.Sort sort;
            private int score;
            public Node(TeamSortingTool.Sort sort){
                this.sort = sort;
                this.score = this.sort.getScore();
                
                if (this.score > 0 && (TeamSortingTool.sortLow == null || this.score < TeamSortingTool.sortLow.getScore())) {
                    TeamSortingTool.sortLow = this.sort;
                    System.out.println("NEW LOW!!: " + this.sort.getScore());
                }
            }
        }

        // Function to return the position of 
        // the parent for the node currently 
        // at pos 
        private int parent(int pos) 
        { 
            return pos / 2; 
        } 

        // Function to return the position of the 
        // left child for the node currently at pos 
        private int leftChild(int pos) 
        { 
            return (2 * pos); 
        } 

        // Function to return the position of 
        // the right child for the node currently 
        // at pos 
        private int rightChild(int pos) 
        { 
            return (2 * pos) + 1; 
        } 

        // Function that returns true if the passed 
        // node is a leaf node 
        private boolean isLeaf(int pos) 
        { 
            if (pos >= (size / 2) && pos <= size) { 
                return true; 
            } 
            return false; 
        } 

        // Function to swap two nodes of the heap 
        private void swap(int fpos, int spos) 
        { 
            Node tmp; 
            tmp = Heap[fpos]; 
            Heap[fpos] = Heap[spos]; 
            Heap[spos] = tmp; 
        } 

        // Function to heapify the node at pos 
        private void minHeapify(int pos) 
        { 

            // If the node is a non-leaf node and greater 
            // than any of its child 
            if (!isLeaf(pos)) { 
                if (Heap[pos].score > Heap[leftChild(pos)].score 
                    || Heap[pos].score > Heap[rightChild(pos)].score) { 

                    // Swap with the left child and heapify 
                    // the left child 
                    if (Heap[leftChild(pos)].score > Heap[rightChild(pos)].score) { 
                        swap(pos, leftChild(pos)); 
                        minHeapify(leftChild(pos)); 
                    } 

                    // Swap with the right child and heapify 
                    // the right child 
                    else { 
                        swap(pos, rightChild(pos)); 
                        minHeapify(rightChild(pos)); 
                    } 
                } 
            } 
        } 

        // Function to insert a node into the heap 
        public void insert(TeamSortingTool.Sort element) 
        { 
            Node curNode = new Node(element);
            if (size >= maxsize) { 
                return; 
            } 
            Heap[++size] = curNode; 
            int current = size; 

            while (Heap[current].score < Heap[parent(current)].score) { 
                swap(current, parent(current)); 
                current = parent(current); 
            } 
        } 
        
        public Node[] getHeap() 
        { 
            return this.Heap;
        } 

        // Function to print the contents of the heap 
        public void print() 
        { 
            for (int i = 1; i <= size / 2; i++) { 
                System.out.print(" PARENT : " + Heap[i].score 
                                 + " LEFT CHILD : " + Heap[2 * i].score 
                                 + " RIGHT CHILD :" + Heap[2 * i + 1].score); 
                System.out.println();
            }
            
        } 

        // Function to build the min heap 
        public void minHeap() 
        { 
            for (int pos = (size / 2); pos >= 1; pos--) { 
                minHeapify(pos); 
            } 
        } 

        // Function to remove and return the minimum 
        // element from the heap 
        public TeamSortingTool.Sort removeMin() 
        { 
            TeamSortingTool.Sort popped = Heap[FRONT].sort;
            Heap[FRONT] = Heap[size--]; 
            minHeapify(FRONT);
            return popped; 
        } 
    } 
}
