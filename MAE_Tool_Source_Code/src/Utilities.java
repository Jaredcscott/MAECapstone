import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;

/*
 * @author Jared Scott
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
        //Used to store data to a file for persistence 
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
}
