import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Stack;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/*
 * @author Jared Scott
 */
public class TeamSortingTool {
    //TEAM SORTING TOOL
    static int NA_PREFERENCE_VALUE = 6; //Indicates the default preference score for N/A's within the Data file
    static int TARGET_PREFERENCE; //See line 482
    static boolean sorting = false; //Used to identify if the application is currenlt in a sorting state
    static String matchingFile; //Matching Data File Location
    static boolean inputValidMat = false; //Used for input validation when outputing to the canvas file.
    //Arrays used to scrape data.
    static ArrayList<StudentMatching> studentsMatching = new ArrayList<StudentMatching>();
    static ArrayList<Team> teamsArray = new ArrayList<Team>();
    static ArrayList<Integer> popularityScores = new ArrayList<Integer>(); //Low score is better
    static int noteIndex; //Used to scrape the notes from the data set. 
    static boolean matchingScanned = false; //Used to ensure the data file has been scanned prior to performing any action upon it. 
    static int printedTeams = 1; //Used as the initial counter for the team data output file.
    static boolean firstSort = true;
    static int lowestScore = Integer.MAX_VALUE;
    static int curScore = Integer.MAX_VALUE;
    static int countBig = 0;
    static Utilities.MinHeap heap = new Utilities.MinHeap(501);
    static boolean exit = false;
    static int heapLow = 99999;
    static Sort sortLow;
    static PriorityQueue<Sort> minHeap = new PriorityQueue<Sort>(502);
    static int masterLow = 99999;
    static int masterLowCnt = 5;
    static Sort masterSort = null;
    static Sort lastSort1 = null;
    static Sort lastSort2 = null;
    static Sort lastSort3 = null;
    static int curCount = 0;
    static boolean updateSort = false;
    static boolean lowestFound = false;
    static Sort doneMast = null;
    
    static boolean first = true;
    
    
    //This class is used to store all data related to a team for the Matching tool 
    public static class Team implements Serializable, Cloneable { 
        String name;
        ArrayList<StudentMatching> members;
        ArrayList<HelperStudent> prefStudents;
        int minMembers;
        int maxMembers;
        int preferenceScore;
        int column;
        int popularity;
        
        public Team(String name,int minMembers , int maxMembers, int col){
            this.name = name;
            this.members = new ArrayList<StudentMatching>();
            this.prefStudents = new ArrayList<HelperStudent>();
            this.minMembers = minMembers;
            this.maxMembers = maxMembers;
            this.preferenceScore = 0;
            this.column = col;
        }
        
        public Object clone() throws CloneNotSupportedException
        {
            return super.clone();
        }
        
        public void setTeamMinMembers(int minNumMembers) {
            //Adjusts the teams min member limit
            this.minMembers = minNumMembers;
        }
        
        public void setTeamMaxMembers(int maxNumMembers){
            //Adjusts the teams max member limit
            this.maxMembers = maxNumMembers;
        }
        
        public void setPopularity(int popularity) {
            //Adjusts the popularity of the team
            this.popularity = popularity;
        }
        
        public void removeMember(StudentMatching member) {
            //Removes the member from the team
            if(this.members.contains(member)){
               this.members.remove(member);
               member.unsetTeam();
               for(String teamString : member.teamPriorities) {
                    if(teamString.split(",")[0].equals(this.name)) {
                        this.preferenceScore -= Integer.parseInt(teamString.split(",")[1]);
                    }
                }
            }
            if (!sorting){
                //Updating display with new team info
                updateDisplay(true);
            }
        }
        
        public void addMember(StudentMatching member) {
            //Adds a member to the team
            if(!(this.members.contains(member)) && !(member.assigned) ){
               this.members.add(member);
               member.setTeam(this.name);
               for(String teamString : member.teamPriorities) {
                    if(teamString.split(",")[0].equals(this.name)) {
                        this.preferenceScore += Integer.parseInt(teamString.split(",")[1]);
                    }
                }
            }
            if (!sorting){
                //Updating display with new team info
                updateDisplay(true);
            }
        }
        
        public ArrayList<String> getHeader() {
            //Method used to retrieve information regarding a team in the form of a formatted output string array. 
            ArrayList<String> teamStrings = new ArrayList<String>();
            teamStrings.add("                 Preference Score: " + this.preferenceScore);
            teamStrings.add("\nTeamname:      " + this.name); 
            teamStrings.add("\nMin Members: " + this.minMembers + "     Max Members: " + this.maxMembers + "  |  Members: " + this.members.size());
            teamStrings.add("\nMembers: \n");
            return teamStrings;
        }
        
        public String toString() {
            //Displays all relevant team info to user
            int lenTeamName = this.name.length();
            String sp = "     ";
            String teamString = "                 Preference Score: " + this.preferenceScore ;
            teamString += "\nTeamname:      " + this.name + 
                    "\nMin Members: " + this.minMembers + "     Max Members: " + this.maxMembers + "  |  Members: " + this.members.size() + "\nMembers: \n";
            for (StudentMatching student : this.members) {
                teamString += "     " + student.name + "  |  " + sp + "Preference Score: " + student.teamPriority + "\n";
            }
            teamString += "\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n";
            return teamString;
        } 
    }
    
    //This class is used to scrape data from the Edusourced Student Matching Report
    public static class StudentMatching implements Comparable< StudentMatching >, Serializable, Cloneable{
        String name;
        String note; //Note from the data file. May contain team preference information
        ArrayList<String> teamPriorities; //This students team preferences 
        String assignedTeam; //Currently assigned team
        Boolean assigned; //Is the student assigned?
        String teamPriority; //Currently assigned team's position in the teamPriorities array
        Boolean locked; //Has the student been locked to its assigned team
        JCheckBox checkbox; //The students checkbox component
        JButton button; //Remove button
        
        public StudentMatching(String name, ArrayList<String> priorities, String note){
            this.name = name;
            this.teamPriorities = priorities;
            this.note = note;
            this.assignedTeam = "";
            this.assigned = false;
            this.locked = false;
            this.addComponants(this);
        }
        
        public void addComponants(StudentMatching student) {          
            JCheckBox studentBox = new JCheckBox(student.name + " Preference score: " + student.teamPriority);
            BufferedImage img = null;
            try {
                img = ImageIO.read(new File(MAEGradingTool.iconUnlocked));
            } 
            catch (IOException e) {
                e.printStackTrace();
            }
            studentBox.setIcon(new ImageIcon(img));
            studentBox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    student.toggleLocked();
                }
            });
            student.checkbox = studentBox;
        }
        
        public void setTeam(String team){
            //Assigns the student to the provided team
            this.assignedTeam = team;
            this.assigned = true;
            for(String teamString : teamPriorities) {
                if(teamString.split(",")[0].equals(team)) {
                    teamPriority = teamString.split(",")[1];
                }
            }
        }
        
        public void unsetTeam(){
            //Unsets the studnets team
            if(!this.locked && !sorting) {
                this.assignedTeam = "";
                this.assigned = false;
                this.refreshComponants();
            }
            else if (sorting) {
                this.assignedTeam = "";
                this.assigned = false;
            }
        }
        
        public void toggleLocked() {
            //Changes the locked status of the student as well as changing the visible icon to the user.
            if (this.locked == true) {
                this.locked = false;
                this.checkbox.setSelected(false);
                BufferedImage img = null;
                try {
                    img = ImageIO.read(new File(MAEGradingTool.iconUnlocked));
                } 
                catch (IOException e) {
                    e.printStackTrace();
                }
                this.checkbox.setIcon(new ImageIcon(img));
            }
            else {
                this.locked = true;
                this.checkbox.setSelected(true);
                BufferedImage img = null;
                try {
                    img = ImageIO.read(new File(MAEGradingTool.iconLocked));
                } 
                catch (IOException e) {
                    e.printStackTrace();
                }
                this.checkbox.setIcon(new ImageIcon(img));
            }
        }
        
        public void addToDisplay(){
            //Adding the student to the user display
            JPanel memberBox = new JPanel();
            memberBox.setLayout(new BoxLayout(memberBox, BoxLayout.X_AXIS));
            memberBox.setAlignmentX(memberBox.LEFT_ALIGNMENT);
            this.checkbox.setText(this.name + " Preference score: " + this.teamPriority);
            memberBox.add(this.checkbox);
            MAEGradingTool.display.add(memberBox);
            
        }
        
        public String toString() {
            return ("Name: " + this.name + " Priorities: " + this.teamPriorities + " Note: " + this.note);
        }
        
        @Override
        public int compareTo(StudentMatching other) {
            //Used to sort the names
            return this.name.compareTo(other.name);
        }
        
        public void refreshComponants(){
            addComponants(this);
        }
    }
    
    //Helper class to display the student at the bottom of the team 
    public static class HelperStudent implements Serializable, Cloneable{
        StudentMatching student;
        String teamName;
        JButton addButton;
        Team team;
        public HelperStudent(Team team, StudentMatching student){
            this.teamName = team.name;
            this.student = student;
            this.addButton = createButton();
            this.team = team;
        }
        
        public JButton createButton(){
            //Creates the add button for this student 
            JButton add = new JButton("Add"); 
            add.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (!(student.assigned)) {
                        for (Team team1 : teamsArray) {
                            if (team1.name.equals(team.name)) {
                                team1.addMember(student);
                                student.locked = true;
                                student.checkbox.setSelected(true);
                                BufferedImage img = null;
                                try {
                                    img = ImageIO.read(new File(MAEGradingTool.iconLocked));
                                } 
                                catch (IOException ex) {
                                    ex.printStackTrace();
                                }
                                student.checkbox.setIcon(new ImageIcon(img));
                            }
                        }
                    }
                    else {  
                        student.locked = false;
                        for (TeamSortingTool.Team team : TeamSortingTool.teamsArray) {
                            if (team.name.equals(student.assignedTeam)) {
                                team.removeMember(student);
                            }
                        }
                        for (TeamSortingTool.Team team1 : TeamSortingTool.teamsArray) {
                            if (team1.name.equals(team.name)) {
                                team1.addMember(student);
                                student.locked = true;
                                student.checkbox.setSelected(true);
                                BufferedImage img = null;
                                try {
                                    img = ImageIO.read(new File(MAEGradingTool.iconLocked));
                                } 
                                catch (IOException ex) {
                                    ex.printStackTrace();
                                }
                                student.checkbox.setIcon(new ImageIcon(img));
                            }
                        }
                    }
                }
            });
            return add;
        }
    }
    
    public static class Sort implements Comparable<Sort>, Cloneable {
        private ArrayList<Team> teams;
        private int sortScore;
        public Sort(ArrayList<Team> teams) {
            this.teams = new ArrayList<Team>();
            for (Team team : teams) {
                this.teams.add(team);
            }
        }
        
        public int getScore() {
            int score = 0;
            for (Team team : this.teams) {
                score += team.preferenceScore;
            }
            this.sortScore = score;
            return this.sortScore;
        }
        
        public String toString() {
            //Displays all relevant team info to user
            String  sortString = "Score: " + this.sortScore + "\n" +  this.teams.toString() + "\n" + this.getScore();
            return sortString;
        } 
        @Override
        public int compareTo(Sort other) {
            return this.getScore() >= other.getScore() ? -1 : 0;
        }
        
        public Object clone() throws CloneNotSupportedException
        {
            return super.clone();
        }
    }
    
    public static class SortSorter implements Comparator<Sort> {
        public SortSorter() {
            
        }
        @Override
        public int compare(Sort sort1, Sort sort2) {
            return sort1.compareTo(sort2);
        }

    }
    //=============================Scanning Methods=============================\\
    public static void scanMatch(){
        try {
            //Pulling data file location 
            String matching = MAEGradingTool.matchingLoc.getText();
            File temp1 = new File(matching);
            //Input validation
            if ((matching.equals(""))) {
                JOptionPane.showMessageDialog(null, "Invalid Matching File: Must be '.xlsx' file type");
                inputValidMat = false;
            }
            else if (!(temp1.exists())) {
                if (!(temp1.exists())) {
                    JOptionPane.showMessageDialog(null, "Invalid Review File: Location not found");
                    inputValidMat = false;
                }
            }
            else {
                //Input is valid 
                inputValidMat = true;
                //Scanning the data file
                getColsMatch(matching);
                //Sorting the students by first name 
                Collections.sort(studentsMatching);
                //This method fills the drop down selection menu with the students 
                getStudents();
            }
            matchingScanned = true;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void getStudents() {
        //Clears the component first
        MAEGradingTool.studentSel.removeAllItems();
        //Looping through master student array 
        for (StudentMatching studentM : studentsMatching) {
            String entry = studentM.name;
            if (studentM.assigned) {
                int nameLen = studentM.name.length();
                int count = 0;
                while (count < (23 - nameLen)) {
                    //Produces a formatted entry string
                    entry += " ";
                    count += 1;
                }
                entry += "Assigned";
            }
            //Adding formatted student to the drop down menu 
            MAEGradingTool.studentSel.addItem(entry);
        }
    }
    
    public static void getColsMatch(String fileNameMat) {
        try {
            FileInputStream fisT = new FileInputStream(fileNameMat); //Creates an input stream for the xlsx/xls file.      
            Workbook workbookMat = null; //Instatiates a Workbook instance of an xlsx/xls file.
            //Determines the file type and constructs the appropriate workbook object.
            if(fileNameMat.toLowerCase().endsWith("xlsx")) {
                    workbookMat = new XSSFWorkbook(fisT);
            }
            else if(fileNameMat.toLowerCase().endsWith("xls")) {
                    workbookMat = new HSSFWorkbook(fisT);
            }
            else {
                throw new Exception("Invalid File");
            }
            Sheet sheet = workbookMat.getSheetAt(0); //Grabs the first sheet.
            Iterator<Row> rowIterator = sheet.iterator();
            int rowCount = 0;
            ArrayList<String> teamStrings = new ArrayList<String>(); //Array to hold the team names.
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next(); //Grabs the row object.
                //Scrapes the data from the Student Preference Survey
                if (rowCount == 0) {
                    int col = 0;
                    for (Cell cell : row) {
                        //Identifies the 'Note' entry
                        if (cell.getStringCellValue().startsWith("In the following")) {
                            noteIndex = col + 1;
                            break;
                        }
                        else{
                            //Constructing the team objects. 
                            String  cellVal = cell.getStringCellValue();
                            String[] cellValSplit = cellVal.split(",");
                            if(cellValSplit.length > 1) {
                                cellVal = "";
                                for (String wordSegment : cellValSplit) {
                                    cellVal += wordSegment;
                                }
                            }
                            teamStrings.add(cellVal);
                            MAEGradingTool.teamsSel.addItem(cellVal);
                            teamsArray.add(new Team(cellVal,3,5,col));
                            popularityScores.add(0);
                        }
                        col += 1;
                    }
                }
                if (rowCount > 0) {  
                    ArrayList<String> priorities = new ArrayList<String>();
                    String nameString = "";
                    String note = "";
                    //Scraping the data
                    for (int i = 0; i <= noteIndex; i++) {
                        if (i == 0) { 
                            nameString = row.getCell(i).getStringCellValue();
                        }
                        else if (i < noteIndex) {
                            if (row.getCell(i).getStringCellValue().equals("N/A")){
                                popularityScores.set(i - 1, (popularityScores.get(i - 1) + 1));
                                priorities.add(teamStrings.get(i - 1) + "," + NA_PREFERENCE_VALUE);
                            }
                            else {
                                priorities.add(teamStrings.get(i - 1) + "," + row.getCell(i).getStringCellValue());
                            }
                        }
                        else if (i == noteIndex) {
                            if (row.getCell(i) != null) {
                                    note = row.getCell(i).getStringCellValue();
                            }
                        }
                    }
                    for (int i = 0; i < teamsArray.size(); i++){
                        teamsArray.get(i).setPopularity(popularityScores.get(i)); 
                    }
                    //Constructing the Student object and inserting it into the main student array.
                    StudentMatching curStudent = new StudentMatching(nameString, priorities ,note);
                    studentsMatching.add(curStudent);
                }
                rowCount += 1;
            } //end of rows iterator
            for (Team team : teamsArray){
                for (StudentMatching student : studentsMatching) {
                    for(String pref: student.teamPriorities) {
                        String[] prefSplit = pref.split(",");
                        if((Integer.parseInt(prefSplit[1]) <= 3) && (prefSplit[0].equals(team.name))) {
                            team.prefStudents.add(new HelperStudent(team,student));
                        }
                        else {
                        }
                    }
                }
            }
            TARGET_PREFERENCE = (teamsArray.size() * 7) + 5; //--------------------------------------------------TARGET_PREFERENCE IS SET 
            fisT.close(); //close file input stream
        } 
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void saveMatch() {
        //Storing a particular sort to an output file for later
        if (matchingScanned) {
            //Displaying 'Saved!' to the user
            Thread displaySaved = new Thread(){
                public void run(){
                    MAEGradingTool.saved.setText("Saved!");
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(MAEGradingTool.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    //Reseting the value of the saved display string
                    MAEGradingTool.saved.setText("       ");
                }
            };
            displaySaved.start();
            try {
                //Creating file name
                String fileName = MAEGradingTool.TEAM_OUTPUT + printedTeams + ".txt";
                FileWriter writer = new FileWriter(fileName, false);
                String output = "";
                int totalPreference = 0;
                //Summing preferences 
                for (Team team : teamsArray) {
                    totalPreference += team.preferenceScore;
                }
                output += "\nSum of all preference scores (Lower is better): " + totalPreference + "\n";
                for (Team team : teamsArray) {
                    output += team.toString();
                }
                //Writing to output file
                writer.write(output);
                writer.close();
                printedTeams++;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
    
    public static void updateDisplay(boolean stay){
        //Used to update the display window for the user
        if(matchingScanned) {
            //Clearing the display first
            MAEGradingTool.display.removeAll();
            String prefString = "";
            int totalPreference = 0;
            //Summing the preference scores
            for (Team team : teamsArray) {
                totalPreference += team.preferenceScore;
            }
            prefString += "\nSum of all preference scores (Lower is better): " + totalPreference + "\n";
            JLabel prefLabel = new JLabel(prefString);
            MAEGradingTool.display.add(prefLabel);
            //Looping through teams
            for (Team team : teamsArray) {
                //Looping through students on the team
                for (HelperStudent student : team.prefStudents) {
                    //Adding buttons to current display
                    student.addButton = student.createButton();
                }
                //Adding team information
                for (String headerLine : team.getHeader()){
                    JLabel teamInfo = new JLabel(headerLine);
                    teamInfo.setAlignmentX(teamInfo.LEFT_ALIGNMENT);
                    MAEGradingTool.display.add(teamInfo);
                }
                JLabel line = new JLabel("\n");
                MAEGradingTool.display.add(line);
                ArrayList<StudentMatching> studentsToAdd = new ArrayList<StudentMatching>(); //Needed to avoid concurrent reference issues.
                for (StudentMatching student : team.members) {
                    studentsToAdd.add(student);
                }
                for (StudentMatching student : studentsToAdd) {
                    student.addToDisplay();
                }
                //Each team has its own JPanel for formatting 
                JPanel prefBox = new JPanel();
                prefBox.setLayout(new BoxLayout(prefBox, BoxLayout.X_AXIS));
                prefBox.setAlignmentX(prefBox.LEFT_ALIGNMENT);
                String prefsString = "";
                int count = 0;
                //Displaying all students who prefer this team
                for(HelperStudent studentH : team.prefStudents){
                    prefsString += studentH.student.name + "  :  ";
                    for (String pref : studentH.student.teamPriorities) {
                        if (pref.split(",")[0].equals(team.name)){
                            prefsString += pref.split(",")[1] + " ";
                            prefsString += !studentH.student.assigned ? "" : "  Assigned "; //Checking to see if the student is already adssigned, adding an indicator if they are.
                            JLabel prefs = new JLabel(prefsString);
                            prefBox.add(prefs);
                            prefBox.add(studentH.addButton); //Add in the button 
                            prefsString =  "  ";
                            prefs = new JLabel(prefsString);
                            prefBox.add(prefs);
                            count += 1;
                        }
                    }
                    if (count == 3) { //Adds four students per row
                        MAEGradingTool.display.add(prefBox);
                        prefBox = new JPanel();
                        prefBox.setLayout(new BoxLayout(prefBox, BoxLayout.X_AXIS));
                        prefBox.setAlignmentX(prefBox.LEFT_ALIGNMENT);
                        prefsString = "";
                        count = 0;
                    }
                }
                if (count > 0){
                    //Only adds non empty teams 
                    MAEGradingTool.display.add(prefBox);
                    prefBox = new JPanel();
                    prefBox.setLayout(new BoxLayout(prefBox, BoxLayout.X_AXIS));
                    prefBox.setAlignmentX(prefBox.LEFT_ALIGNMENT);
                }
                //Separation string
                JLabel endOfTeam = new JLabel("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");
                endOfTeam.setAlignmentX(MAEGradingTool.display.LEFT_ALIGNMENT);
                MAEGradingTool.display.add(endOfTeam);      
            }
            if (!sorting) {
                //Sort the students within the master array
                Collections.sort(studentsMatching);
                //Reload the drop down menu
                getStudents();
            }
            MAEGradingTool.display.repaint(); //Repaints the output to the display
            if (!stay) {
                //reverts back to the top unless the stay feature is active
                JScrollBar sb = MAEGradingTool.teamScroll.getVerticalScrollBar();
                sb.setValue( sb.getMinimum() );
            } 
        }
    }
    
    public static int startSort() throws CloneNotSupportedException{
        int sortScore = 999;
        exit = false;
        int count = 0;
        int boogCount = 0;
        boolean done = false;
        List<Sort> sortList = new ArrayList<Sort>();
        while (count < 500) {
            sortScore = sortTeams(sortList);
            count += 1;
            if (count >= 500) {
                done = true;
            }
            if (exit) {
                done = true;
                break;
            }
        }
        try {
            Sort minSort;
            if(doneMast != null) {
                minSort = (Sort) doneMast.clone();
            }
            else {
                minSort = (Sort) heap.removeMin();
            }
            teamsArray = (ArrayList<Team>) minSort.teams.clone();
            return minSort.getScore();
        }
        catch (Exception e) {
            return 999;
        }
    }
    public static int sortTeams(List<Sort> sorts) throws CloneNotSupportedException{
        MAEGradingTool.currentSort.setText("");
        sorting = true;
        int totPrefScore = 999999; //Trying to minimize, start large
        int target_preference = TARGET_PREFERENCE;
        int lowestPrefScore = lowestScore;
        Stack<Sort> localLow = new Stack<Sort>();
        while (totPrefScore > target_preference) {
            clearTeams(false); //Clear the teams 
            addMembers(); //Add an initial selection of team members by chosen priorities
            balanceTeams(); //Balances the teams with some inteligence 
            cleanUp(); //Ensures the sort is valid and all students have been assigned. 
            //Summing the preference scores 
            if (exit) {
                ArrayList<Team> copy = new ArrayList<Team>();
                for (Team team : teamsArray) {
                    copy.add((Team) team.clone());
                }
                doneMast = new Sort(copy);
                if(doneMast.getScore() > TARGET_PREFERENCE) {
                    MAEGradingTool.currentSort.setText("No ideal solution found. Adjust team limits for better sort quality");
                    TARGET_PREFERENCE = doneMast.getScore()-15; 
                }
                else {
                    MAEGradingTool.currentSort.setText("Current Sort Score: "  + doneMast.getScore());
                }
                return doneMast.getScore();
            }
            totPrefScore = 0;
            for (Team team : teamsArray) {
                totPrefScore += team.preferenceScore;
            }
            //adjusting the target preference score to hopefully get a better sorting for additional sorts. 
//            System.out.println("HERE: " + totPrefScore + " : target" + target_preference);
            if (totPrefScore - target_preference > 100) {
                target_preference += 25;
            }
            else if (totPrefScore - target_preference > 50){
                target_preference += 5;
            }
            else if (totPrefScore - target_preference > 15){
                target_preference += 3;
            }
            else if (totPrefScore - target_preference < 0){
                target_preference -= 1;
            }
            else if (target_preference > (lowestPrefScore + 5)) {
                target_preference -= 1;
            }
            else if (totPrefScore - target_preference < -15){
                target_preference -= 2;
            }
            else if (Math.abs(target_preference - lowestPrefScore) == 1) {
                target_preference -= 1;
            }
            else {
                target_preference += 1; //This ensures that the program will not enter an infinite loop.
            }
            if (totPrefScore <= lowestPrefScore) {
                lowestPrefScore = totPrefScore;
            }
        }
        ArrayList<Team> copy = new ArrayList<Team>();
        for (Team team : teamsArray) {
            copy.add((Team) team.clone());
        }
        Sort curSort1 = new Sort(copy);
        if (masterSort == null) {
            masterSort = (Sort) curSort1.clone();
            masterLow = curSort1.getScore();
            lastSort1 = masterSort;
            curCount += 1;
        }
        else {
            if (masterSort.getScore() >= curSort1.getScore()) {
                masterSort = (Sort) curSort1.clone();
                masterLow = curSort1.getScore();
                masterLowCnt -= 1;
                updateSort = true; 
            }
            if (updateSort) {
                updateSort = false;
                if (curCount == 3) {
                    lastSort3 = (Sort) lastSort2.clone();
                    lastSort2 = (Sort) lastSort1.clone();
                    lastSort1 = (Sort) masterSort.clone();
                }
                else if (curCount == 2) {
                    lastSort3 = (Sort) lastSort2.clone();
                    lastSort2 = (Sort) lastSort1.clone();
                    lastSort1 = (Sort) masterSort.clone();
                    curCount += 1;
                }
                else if (curCount == 1) {
                    lastSort2 = (Sort) lastSort1.clone();
                    lastSort1 = (Sort) masterSort.clone();
                    curCount += 1;
                }
                if(curCount == 3) {
//                    System.out.println(lastSort1.getScore() + " : " + lastSort2.getScore() + " : " + lastSort3.getScore());
                    if (lastSort1.getScore() == lastSort3.getScore() && lastSort1.getScore() == lastSort2.getScore()) {
                        if ( doneMast == null ) {
                            doneMast = (Sort) masterSort.clone();
                        }
                        else {
                            if (doneMast.getScore() > masterSort.getScore()) {
                                doneMast = (Sort) masterSort.clone();
                                return doneMast.getScore();
                            }
                        }
                    }
                }
            }
        }    
        if (!exit) {
            countBig = 0;
            sorting = false;
            int score = 0;
            for (Team team : teamsArray) {
                score += team.preferenceScore;
            }
            Sort curSort = new Sort(teamsArray);
            if (score < heapLow) {
                heapLow = score;
                sortLow = curSort;
            }
            heap.insert(curSort);
            masterLowCnt -= 1;
            if (masterLowCnt <= 0 ) {
                return curSort1.getScore();
            }
            else {
                return sortTeams(sorts);
            }
        }
        else {
            countBig = 0;
            sorting = false;
            return 999;
        }
        
    }
    
    public static void cleanUp(){
        int count = 0;
        countBig = 0;
        boolean allAssigned = false;
        while(!allAssigned) {
            allAssigned = true;
            for(StudentMatching student : studentsMatching) {
                if(student.assigned == false) {
                    allAssigned = false;
                    for(String pref : student.teamPriorities) {
                        if (Integer.parseInt(pref.split(",")[1]) <= 3 && !student.assigned){
                           for(Team team : teamsArray){
                               if(team.name.equals(pref.split(",")[0]) && (team.members.size() < team.maxMembers)){
                                   team.addMember(student);
                                   break;
                               }
                            } 
                        }
                    }
                }
            }
            count += 1;
            if(count > 5000) {
                int numSlots = 0;
                for (Team team : teamsArray) {
                    numSlots += team.maxMembers;
                }
                if(numSlots < studentsMatching.size()){
                    MAEGradingTool.currentSort.setText("Not Enough Team Member Spots");
                    break;
                }
                for(StudentMatching student : studentsMatching) {
                    if(student.assigned == false) {
                        allAssigned = false;
                        for(String pref : student.teamPriorities) {
                            if (Integer.parseInt(pref.split(",")[1]) <= 5 && !student.assigned){
                               for(Team team : teamsArray){
                                   if(team.name.equals(pref.split(",")[0]) && (team.members.size() < team.maxMembers)){
                                       team.addMember(student);
                                       break;
                                   }
                                } 
                            }
                        }
                    }
                }
                if(count > 15000) {
                    int studentCount = studentsMatching.size();
                    for(StudentMatching student : studentsMatching) {
                        if(student.assigned == false) {
                            allAssigned = false;
                            int teamsMinTot = 0;
                            int teamsMaxTot = 0;
                            int assignedCount = 0;
                            ArrayList<StudentMatching> unassignedStudents = new ArrayList<StudentMatching>();        
                            for(Team team : teamsArray){
                                teamsMinTot += team.minMembers;
                                teamsMaxTot += team.maxMembers;     
                                int unassigned = 0;
                                for (StudentMatching student1 : studentsMatching) {
                                    if (!student1.assigned) {
                                        unassigned += 1; 
                                        unassignedStudents.add(student1);
                                    }
                                    else {
                                        assignedCount += 1;
                                    }
                                }
                                int noneCount = 0;
                                for (StudentMatching student1 : unassignedStudents){
                                    boolean noneFound = true;
                                    for (String teamPri : student1.teamPriorities) {
                                        String[] teamArray = teamPri.split(",");
                                        if (Integer.parseInt(teamArray[1]) <=3) {
                                            for (Team team1 : teamsArray) {
                                                if(teamPri.split(",")[0].equals(team1.name) && team1.members.size() < team1.maxMembers) {
                                                    team1.addMember(student1);
                                                }
                                            }
                                        }
                                    }
                                    if (!student1.assigned) {
                                        for (Team team2 : teamsArray) {
                                            if(team2.members.size() < team2.maxMembers) {
                                                team2.addMember(student1);
                                                noneFound = false;
                                                break;
                                            }     
                                        }
                                    }
                                    if (noneFound){
                                            noneCount += 1;
                                    }
                                }
                                for (StudentMatching student1 : studentsMatching) {
                                    if (!student1.assigned) {
                                        unassigned += 1; 
                                    }
                                    else {
                                        assignedCount += 1;
                                    }
                                }
                                
                                if (unassigned == 0) {
                                    sorting = false;
                                    exit = true;
                                    allAssigned = true;
                                    return;
                                }
                                if (noneCount == unassignedStudents.size()) {
                                    MAEGradingTool.currentSort.setText("Adjust team member ranges and try again. No reasonable solution found.");                                    
                                    countBig += 1;
                                    exit = true;
                                    break;
                                }
                            }
                            
                            
                        }
                        if (exit) {
                            break;
                        }
                    }
                }
            }
            if (exit) {
                clearTeams(false);
                break;
            }
        }
    }
    
    public static void balanceTeams(){
        //Counting how many students are needed to balance the sort.
        int needed = 0;
        for(Team team : teamsArray) {
            if(team.members.size() < team.minMembers) {
                needed += (team.minMembers - team.members.size());
            }
        }
        //Looping through the teams 
        for(Team team : teamsArray) {
            if((team.members.size() > team.minMembers) && needed > 0) {
                Collections.shuffle(team.members);
                boolean removableStudentFound = false; //Boolean used to find an elible student 
                ArrayList<StudentMatching> removeArray = new ArrayList<StudentMatching>();
                //Looping through students looking for potential members to take. 
                for (int index = 0; index < team.members.size(); index++) {
                    StudentMatching student = team.members.get(index);
                    if(!(student.locked) && needed != 0) {
                        removableStudentFound = true;
                        removeArray.add(student);
                        needed -= 1;
                    }
                }
                for (StudentMatching student : removeArray) {
                    team.removeMember(student);
                }
            }
        }
        boolean allAssigned = false;
        //Checking that all students have been assigned 
        for(Team team : teamsArray) {
            while((team.members.size() < team.minMembers) && !allAssigned) {
                Integer countOfAssigned = 0;
                for (StudentMatching student : studentsMatching) {
                    if(!(student.assigned)) {
                        team.addMember(student);
                        break;
                    }
                    else {
                        countOfAssigned += 1;
                    }
                }
                if(countOfAssigned == studentsMatching.size()) {
                    allAssigned = true;
                    break;
                }
            }
        }
    }
    
    public static void addMembers() {
        //Assigning least popular teams any prefered members.
        for (Team team : teamsArray){
            if(team.popularity > 15){
                for (StudentMatching student : studentsMatching) {
                    int preference = Integer.parseInt(student.teamPriorities.get(team.column).split(",")[1]);
                    if(preference < NA_PREFERENCE_VALUE && (team.members.size() < team.minMembers)) {
                        team.addMember(student);
                    }
                }
            }
        }
        Collections.shuffle(studentsMatching); //Suffling the students first for fairness 
        //This loop gives, in random order, each person exactly what they want if possible
        ArrayList<Integer> nums = new ArrayList<Integer>(); //Array representing the priority scores  
        for (int i = 1; i < 6 ; i ++) {
            nums.add(i);
        }
        for(int num : nums) { //For each priority in ascending order
            //Looping through students 
            for(StudentMatching student : studentsMatching) {
                if(!(student.assigned)) { //Is the student assigned? 
                    //Looping through the currently selected students team priorites  
                    for(String teamString : student.teamPriorities) { 
                        //Finding the team matching the current priority score.
                        if (teamString.split(",")[1].equals("" + num)) {
                            for (Team team : teamsArray) {
                                if((team.name.equals(teamString.split(",")[0])) && (team.members.size() < team.maxMembers) && (team.preferenceScore < (team.maxMembers * 2))) {
                                    team.addMember(student);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    //Used to clear the teams for the Team Sorting Tool. 
    public static void clearTeams(boolean update) {
        //Used to clean the slate before a new sort
        for (Team team : teamsArray) {
            //Removing all team members
            ArrayList<StudentMatching> removeArray = new ArrayList<StudentMatching>(); //Needed to avoid concurrent reference issues. 
            for(StudentMatching student : team.members) {
                if (student.locked == false) {
                    removeArray.add(student);
                }
            }
            for (StudentMatching student : removeArray) {
                team.removeMember(student);
            }
            int preferenceScore = 0;
            for (StudentMatching student : team.members) {
                preferenceScore += Integer.parseInt(student.teamPriority);
            }
            //Updating the team preference score
            team.preferenceScore = preferenceScore;
        }
        if(update) {
            getStudents(); //Updates the drop down mennu. 
            updateDisplay(false); //Updates the display
        }
    }
}
