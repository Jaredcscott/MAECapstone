import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import org.pushingpixels.substance.api.skin.*;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;

/*
 * @author Jared Scott
 * Ver 1.7
 */
public class MAEGradingTool extends javax.swing.JFrame
{
    //===========================VARIABLE DECLARATION===========================\\
    //These variables are used across the different tools.
    //Decimal format for uniformity
    static DecimalFormat df = new DecimalFormat("#,###,##0.0");
    //This data file stores the persistance variables, it is saved upon exit and loaded upon start
    static String DATA_FILE = "lib\\Data.cfg"; //Change src to lib before building
    //This is the file where the team sorting tool stores the lowest team data
    static String TEAM_FILE = "lib\\lowestTeam.dat"; //Change src to lib before building
    //This is the filename for the output files within the team soritng tool. different teams will be numbered IE: Team1, Team2
    static String TEAM_OUTPUT = "Team";
    //This is the filename for thelowest sort from the team sorting tool.
    static String BEST_TEAM_OUTPUT = "Best Sort";
    //This is the icon for the Tool's main window
    static String ICON = "lib\\Images\\mae.png"; //Change src to lib before building
    //This is the locked icon for the Team sorting tool's lock functionality
    static String iconLocked = "lib\\Images\\iconLocked.jpg"; //Change src to lib before building
    //This is the unlocked icon for the Team sorting tool's lock functionality
    static String iconUnlocked = "lib\\Images\\iconUnlocked.jpg"; //Change src to lib before building

    
    //============================Class Declarations============================\\
    public MAEGradingTool()
    {       
        initComponents();
        setIconImage((new ImageIcon(ICON)).getImage());
        setResizable(false);
        //End of constructor for the Tool
    }
    
    //This class is used to scrape data from the Canvas file
    public static class StudentCanvas {
        String[] nameList;
        String data;
        int row; 
        
        public StudentCanvas(String[] nameList,int row,String data) {
            this.nameList = nameList;
            this.row = row;
            this.data = data;
        }
        
        public String toString() {
            return ("Name: " + Arrays.toString(this.nameList) + " Row: " + this.row);
        }
    }
    
    public static class Student {
        String[] nameListCan;
        int rowCan; 
        ArrayList<Double> data;
        Double score;
        String row;
        boolean test = false;
        double con;
        double prof;
        boolean selfDone;
        
        //This constructor is used for the Time Tracking Tool
        public Student(String[] nameListCan, int rowCan, ArrayList<Double> dataArray) {
            this.data = dataArray;
            this.nameListCan = nameListCan;
            this.rowCan = rowCan;
            this.score = 0.0;
            this.row = "";
        }
        //This constructor is used for the Time Tracking Tool
        public Student(String[] nameListCan, int rowCan,String row) {
            this.row = row;
            this.nameListCan = nameListCan;
            this.rowCan = rowCan;
            this.score = 0.0;
            this.data = new ArrayList<Double>();
        }
        //This constructor is used for the Review Scores Tool
        public Student(String[] nameListCan, int rowCan,double score) {
            this.nameListCan = nameListCan;
            this.rowCan = rowCan;
            this.score = score;
        }
        //This constructor is used for the Review Scores Tool
        public Student(String[] nameListCan, int rowCan,double score, double con, double prof, boolean selfDone) {
            this.nameListCan = nameListCan;
            this.rowCan = rowCan;
            this.score = score;
            this.con = con;
            this.prof = prof;
            this.selfDone = selfDone;
        }
        //This constructor is used for the Team Sorting Tool
        public Student(String[] nameListCan, int rowCan,String row, boolean test) {
            this.row = row;
            this.nameListCan = nameListCan;
            this.rowCan = rowCan;
            this.score = 0.0;
            this.data = new ArrayList<Double>();
            this.test = test;
        }
        
        //Used to set the score for the Student instance when used for the Review Scores Tool
        //Accounts for the rounding difference. 
        public void setScore() {
            double temp = this.con + this.prof;
            BigDecimal a = new BigDecimal("" +  temp); //Concatenation is needed to convert the double into a BigDecimal
            BigDecimal b = a.setScale(2, RoundingMode.UP); // => BigDecimal("1.23")
            this.score = b.doubleValue();
            if (!this.selfDone && ReviewScoresTool.toggleOverride) {
                this.score = 0.0;//Sets the score to 0 if the self assesment is incomplete and the checkbox is checked. 
            }
        }
        
        public String toString() {
            return ("Name: " + Arrays.toString(this.nameListCan) + " Row: " + this.rowCan + " Data: " + this.data + " Score: " + this.score);
        }
    }
        
    //==========================MAIN RUN METHODS FOR TOOLS======================\\
    private void runButtonActionPerformed(java.awt.event.ActionEvent evt) {
        TimeTrackingTool.runTime();
    }
    
    private void runButtonRActionPerformed(java.awt.event.ActionEvent evt) {
        ReviewScoresTool.runReview();
    }
    //===========================PERSISTANCE METHODS============================\\
    
    private void saveState(){
        try {
            FileWriter writer = new FileWriter(DATA_FILE, false);  
            writer.write("maxValTxt=" + this.maxValTxt.getText() + "\n");
            writer.write("zeroOnSelfUndone=" + this.toggleOverride.isSelected() + "\n");
            writer.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    private void loadState(){
        try {
            File file = new File(DATA_FILE); 
            Scanner sc = new Scanner(file);
            while(sc.hasNextLine()){
                String line = sc.nextLine();
                String[] args = line.split("=");
                //Used to handle the persistance variables. 
                switch (args[0]) {
                    case "maxValTxt":
                        if(args.length == 1){
                            maxValTxt.setText(null);
                        }
                        else{
                            maxValTxt.setText(args[1]);
                        }
                        break;
                    case "zeroOnSelfUndone":
                        if(args.length == 1){
                            this.toggleOverride.setSelected(false);
                        }
                        else{
                            this.toggleOverride.setSelected(Boolean.parseBoolean(args[1]));
                        }
                        break;
                    default:
                        System.out.println("Invalid data entry");
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    //===============================MAIN METHOD================================\\
    //INITIALIZES THE TOOL AND SETS UP THE INITAL STATE
    public static void main(String args[])
    {
        java.awt.EventQueue.invokeLater(new Runnable()
        {
            public void run()
            {
                try
                {
                    UIManager.setLookAndFeel(new SubstanceGraphiteGlassLookAndFeel());
                }
                catch (Exception ex)
                {
                    System.out.println("Substance failed to initialize");
                }
                MAEGradingTool app = new MAEGradingTool();
                app.loadState();
                teamScroll.getVerticalScrollBar().setUnitIncrement(16);
                reviewScoresScroll.getVerticalScrollBar().setUnitIncrement(16);
                app.setVisible(true);
                app.toggleOverrideActionPerformed(null);
                System.out.println("Toggled Start: " + ReviewScoresTool.toggleOverride);
                assigned.setVisible(false);
                sorting.setVisible(false);
                //sorting.setVisible(false);
                saved.setText("       ");
                display.setLayout(new BoxLayout(display, BoxLayout.Y_AXIS));
                display.setAlignmentX(MAEGradingTool.display.LEFT_ALIGNMENT);
                teamScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                reviewScoresScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                teamScroll.getVerticalScrollBar().addAdjustmentListener(new java.awt.event.AdjustmentListener(){
                    public void adjustmentValueChanged(java.awt.event.AdjustmentEvent ae){
                        SwingUtilities.invokeLater(new Runnable(){
                            public void run(){
                                teamScroll.repaint();
                            }
                        });
                    }
                });
                teamScroll.getHorizontalScrollBar().addAdjustmentListener(new java.awt.event.AdjustmentListener(){
                    public void adjustmentValueChanged(java.awt.event.AdjustmentEvent ae){
                        SwingUtilities.invokeLater(new Runnable(){
                            public void run(){
                                teamScroll.repaint();
                            }
                        });
                    }
                });
            }
        });
    }
    
    private void formWindowClosing(java.awt.event.WindowEvent evt)
    {
        this.saveState();
        System.exit(-1);
    }
    
    //Methods Used to generate the file browsers
    private void browseCanvasActionPerformed(java.awt.event.ActionEvent evt) {
        //Logic for the review file browser within the Time Tracking tool.
        //Making the Desktop the default location.        
        String userDir = System.getProperty("user.home");
        final JFileChooser fc = new JFileChooser(userDir + "/Desktop");
        FileNameExtensionFilter filter = new FileNameExtensionFilter("csv", "csv");
        fc.setFileFilter(filter);
        fc.setDialogTitle("Canvas File Location");
        fc.showOpenDialog(new JFrame());
        if (fc.getSelectedFile() != null) {
            canvasLoc.setText("" + fc.getSelectedFile());
            TimeTrackingTool.canvasFile = "" + fc.getSelectedFile();
        }
        if (!"".equals(timeLoc.getText()) && !"".equals(canvasLoc.getText())) {
            TimeTrackingTool.scan("canvas");
        }
    }
    
    private void browseTimeActionPerformed(java.awt.event.ActionEvent evt) {
        //Logic for the time file browser within the Time Tracking tool.
        //Making the Desktop the default location.
        String userDir = System.getProperty("user.home");
        final JFileChooser fc = new JFileChooser(userDir + "/Desktop");
        FileNameExtensionFilter filter = new FileNameExtensionFilter("xlsx", "xlsx");
        fc.setFileFilter(filter);
        fc.setDialogTitle("Time File Location");
        fc.showOpenDialog(new JFrame());
        if (fc.getSelectedFile() != null) {
            timeLoc.setText("" + fc.getSelectedFile());
            TimeTrackingTool.timeFile = "" + fc.getSelectedFile();
        }
        if (!"".equals(timeLoc.getText()) && !"".equals(canvasLoc.getText())) {
            TimeTrackingTool.scan("time");
        }
    }
    
    private void browseCanvas1ActionPerformed(java.awt.event.ActionEvent evt) {
        //Logic for the canvas file browser within the Reviews tool.
        //Making the Desktop the default location.
        String userDir = System.getProperty("user.home");
        final JFileChooser fc = new JFileChooser(userDir + "/Desktop");
        //Filtering by file type
        FileNameExtensionFilter filter = new FileNameExtensionFilter("csv", "csv");
        fc.setFileFilter(filter);
        fc.setDialogTitle("Canvas File Location");
        fc.showOpenDialog(new JFrame());
        if (fc.getSelectedFile() != null) {
            canvasLocR.setText("" + fc.getSelectedFile());
            ReviewScoresTool.canvasFileR = "" + fc.getSelectedFile();
        }
        if (!"".equals(reviewsLoc.getText()) && !"".equals(canvasLocR.getText())) {
            ReviewScoresTool.scanReview();
        }
    }
    
    private void browseReviewsActionPerformed(java.awt.event.ActionEvent evt) {
        //Logic for the review file browser within the Reviews tool.
        //Making the Desktop the default location.
        String userDir = System.getProperty("user.home");
        final JFileChooser fc = new JFileChooser(userDir + "/Desktop");
        //Filtering by file type.
        FileNameExtensionFilter filter = new FileNameExtensionFilter("xlsx", "xlsx");
        fc.setFileFilter(filter);
        fc.setDialogTitle("Survey File Location");
        fc.showOpenDialog(new JFrame());
        if (fc.getSelectedFile() != null) {
            reviewsLoc.setText("" + fc.getSelectedFile());
            ReviewScoresTool.reviewFile = "" + fc.getSelectedFile();
        }
        if (!"".equals(reviewsLoc.getText()) && !"".equals(canvasLocR.getText())) {
            ReviewScoresTool.scanReview();
        }
    }
        
    //Used to handle manual file entry
    private void canvasLocRActionPerformed(java.awt.event.ActionEvent evt) {
        ReviewScoresTool.scanReview(); //Scans the file if the file location is changed.
    }

    private void canvasLocActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_canvasLocActionPerformed
        //TimeTrackingTool.scan("canvas"); //Scans the file if the file location is changed.
    }//GEN-LAST:event_canvasLocActionPerformed

    private void timeLocActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_timeLocActionPerformed
       // TimeTrackingTool.scan("time"); //Scans the file if the file location is changed.
    }//GEN-LAST:event_timeLocActionPerformed

    private void reviewsLocActionPerformed(java.awt.event.ActionEvent evt) {                                           
        ReviewScoresTool.scanReview(); //Scans the file if the file location is changed.
    }
    
    //Used to handle selection from drop down menus
    private void canvasWeekDestSelActionPerformed(java.awt.event.ActionEvent evt) {                                                  
        ReviewScoresTool.getSelection();
    }                                                     
    private void canvasWeekSelectionActionPerformed(java.awt.event.ActionEvent evt) {
        //Logic for the canvas week selection menu.
        if (!(canvasWeekSelection.getSelectedItem().toString().equals(""))) {
            String selection = canvasWeekSelection.getSelectedItem().toString();
            TimeTrackingTool.startWeekCanvas = Integer.parseInt(selection.substring(2,selection.length())); 
        }
        System.out.println("Start Canvas Week: " + TimeTrackingTool.startWeekCanvas + " Cols: " + TimeTrackingTool.canvasWeekCols.toString());
        TimeTrackingTool.askedTime = false;
    }

    private void timeWeekSelectionActionPerformed(java.awt.event.ActionEvent evt) {
        //Logic for the Time Tracking week selection menu.
        if (!(timeWeekSelection.getSelectedItem().toString().equals(""))) {
            String[] selection = timeWeekSelection.getSelectedItem().toString().split(" ");
            TimeTrackingTool.startWeek = Integer.parseInt(selection[1]);
            TimeTrackingTool.startWeekTime = Integer.parseInt(TimeTrackingTool.weeksArray.get(TimeTrackingTool.startWeek - 1).split(" ")[2]);
        }
        TimeTrackingTool.askedTime = false;
    }
    
    private void toggleOverrideActionPerformed(java.awt.event.ActionEvent evt) {
        if (toggleOverride.isSelected()){
            ReviewScoresTool.toggleOverride = true;
        }
        else {
            ReviewScoresTool.toggleOverride = false;
        }
    }
    
    private void saveActionPerformed(java.awt.event.ActionEvent evt) {                                     
        TeamSortingTool.saveMatch();
    }   
    
    //Used to set the membership limits of teams within the Team sorting tool. 
    private void setMemLimitsActionPerformed(java.awt.event.ActionEvent evt) {
        if ((!(maxMembers.getText().equals("")) && !(minMembers.getText().equals(""))) && TeamSortingTool.matchingScanned) {
            String selection = String.valueOf(teamsSel.getSelectedItem());
            int maxMembersInt = 5;
            int minMembersInt = 3;
            String message = "Invalid entry:";
            boolean maxValid = false;
            boolean minValid = false;
            try{
                maxMembersInt = Integer.parseInt(maxMembers.getText());
                maxValid = true;
            }
            catch (Exception e) {
                message += "\nMax Team Members";
            }
            try{
                minMembersInt = Integer.parseInt(minMembers.getText());
                minValid = true;
            }
            catch (Exception e) {
                message += "\nMin Team Members";
            }
            if (!(message.equals("Invalid entry:"))) {
                JOptionPane.showMessageDialog(null, message);
            }
            if(maxValid && minValid) {
                for (TeamSortingTool.Team team : TeamSortingTool.teamsArray ) {
                    if (team.name.equals(selection)) {
                        team.setTeamMaxMembers(maxMembersInt);
                        team.setTeamMinMembers(minMembersInt);
                    }
                }
            }
        }
        TeamSortingTool.updateDisplay(true);
    }

    private void sortActionPerformed(java.awt.event.ActionEvent evt) {
        if (TeamSortingTool.matchingScanned) {
            sorting.setVisible(true);
            int totalPreferenceScore;
            SwingWorker worker = new SwingWorker<String, String>() {
                @Override
                protected String doInBackground() throws Exception {
                    int totalPreferenceScore = TeamSortingTool.startSort();
                    return "" + totalPreferenceScore; //Returns the text to be set on the JTextArea
                }
                @Override
                protected void done() {
                    super.done();
                    sorting.setVisible(false);
                    try {
                        int totalPreferenceScore = Integer.parseInt(get()); //Set the textArea the text given from the long running task
                        if (totalPreferenceScore <= TeamSortingTool.lowestScore) {
                            if (TeamSortingTool.lowestScore > (TeamSortingTool.studentsMatching.size() - 10)){
                                TeamSortingTool.lowestScore = totalPreferenceScore;
                                lowestLabel.setText("Current Lowest Sort Score: "  + TeamSortingTool.lowestScore);
                                Utilities.storeObjects();
                            }
                        }
                        if (!TeamSortingTool.exit){
                            currentSort.setText("Current Sort Score: "  + totalPreferenceScore);
                        }
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                    TeamSortingTool.updateDisplay(true);
                }
            };
            worker.execute();
        }
    }
    
    private void assignActionPerformed(java.awt.event.ActionEvent evt) {
        if (TeamSortingTool.matchingScanned) {
            Thread displayAssigned = new Thread(){
                public void run(){
                    assigned.setVisible(true);
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(MAEGradingTool.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    assigned.setVisible(false);
                }
            };
            displayAssigned.start();
            for ( int i = 0; i < TeamSortingTool.studentsMatching.size(); i++){
                TeamSortingTool.StudentMatching student = TeamSortingTool.studentsMatching.get(i); 
                if (studentSel.getSelectedItem().toString().contains(student.name)) {
                    if (!(student.assigned)) {
                        for (TeamSortingTool.Team team : TeamSortingTool.teamsArray) {
                            if (team.name.equals(teamsSel.getSelectedItem().toString())) {
                                team.addMember(student);
                                student.locked = true;
                                student.checkbox.setSelected(true);
                                BufferedImage img = null;
                                try {
                                    img = ImageIO.read(new File(MAEGradingTool.iconLocked));
                                } 
                                catch (IOException e) {
                                    e.printStackTrace();
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
                        for (TeamSortingTool.Team team : TeamSortingTool.teamsArray) {
                            if (team.name.equals(teamsSel.getSelectedItem().toString())) {
                                team.addMember(student);
                                student.locked = true;
                                student.checkbox.setSelected(true);
                                BufferedImage img = null;
                                try {
                                    img = ImageIO.read(new File(MAEGradingTool.iconLocked));
                                } 
                                catch (IOException e) {
                                    e.printStackTrace();
                                }
                                student.checkbox.setIcon(new ImageIcon(img));
                            }
                        }
                    }
                }
            }
            TeamSortingTool.updateDisplay(true);
        }
    }

    private void teamsSelActionPerformed(java.awt.event.ActionEvent evt) {
        if (TeamSortingTool.matchingScanned) {
            String selection = String.valueOf(teamsSel.getSelectedItem());
            for (TeamSortingTool.Team team : TeamSortingTool.teamsArray ) {
                if (team.name.equals(selection)) {
                    maxMembers.setText("" + team.maxMembers);
                    minMembers.setText("" + team.minMembers);
                }
            }
        }
    }

    private void browseMatchingActionPerformed(java.awt.event.ActionEvent evt) {
        //Logic for the matching data file browser within the Team Sort tool.
        TeamSortingTool.NA_PREFERENCE_VALUE = 6; //Indicates the default preference score for N/A's within the Data file
        TeamSortingTool.TARGET_PREFERENCE = 0; //See line 482
        TeamSortingTool.sorting = false; //Used to identify if the application is currenlt in a sorting state
        TeamSortingTool.matchingFile =  null; //Matching Data File Location
        TeamSortingTool.inputValidMat = false; //Used for input validation when outputing to the canvas file.
        //Arrays used to scrape data.
        TeamSortingTool.studentsMatching = new ArrayList<TeamSortingTool.StudentMatching>();
        TeamSortingTool.teamsArray = new ArrayList<TeamSortingTool.Team>();
        TeamSortingTool.popularityScores = new ArrayList<Integer>(); //Low score is better
        TeamSortingTool.noteIndex = 0; //Used to scrape the notes from the data set. 
        TeamSortingTool.matchingScanned = false; //Used to ensure the data file has been scanned prior to performing any action upon it. 
        TeamSortingTool.printedTeams = 1; //Used as the initial counter for the team data output file.
        TeamSortingTool.firstSort = true;
        TeamSortingTool.lowestScore = Integer.MAX_VALUE;
        TeamSortingTool.curScore = Integer.MAX_VALUE;
        TeamSortingTool.countBig = 0;
        TeamSortingTool.heap = new Utilities.MinHeap(501);
        TeamSortingTool.exit = false;
        TeamSortingTool.heapLow = 99999;
        TeamSortingTool.sortLow = null;
        TeamSortingTool.minHeap = new PriorityQueue<TeamSortingTool.Sort>(502);
        TeamSortingTool.masterLow = 99999;
        TeamSortingTool.masterLowCnt = 5;
        TeamSortingTool.masterSort = null;
        TeamSortingTool.lastSort1 = null;
        TeamSortingTool.lastSort2 = null;
        TeamSortingTool.lastSort3 = null;
        TeamSortingTool.curCount = 0;
        TeamSortingTool.updateSort = false;
        TeamSortingTool.lowestFound = false;
        TeamSortingTool.doneMast = null;

        TeamSortingTool.first = true;
        TeamSortingTool.studentsMatching.clear();
        TeamSortingTool.teamsArray.clear();
        teamsSel.removeAllItems();
        display.removeAll();
        lowestLabel.setText("");
        currentSort.setText("");
        TeamSortingTool.heap = new Utilities.MinHeap(501);
        //Making the Desktop the default location.
        String userDir = System.getProperty("user.home");
        final JFileChooser fc = new JFileChooser(userDir + "/Desktop");
        FileNameExtensionFilter filter = new FileNameExtensionFilter("xlsx", "xlsx");
        fc.setFileFilter(filter);
        fc.setDialogTitle("Matching File Location");
        fc.showOpenDialog(new JFrame());
        if (fc.getSelectedFile() != null) {
            matchingLoc.setText("" + fc.getSelectedFile());
            TeamSortingTool.matchingFile = "" + fc.getSelectedFile();
        }
        if (!"".equals(matchingLoc.getText())) {
            TeamSortingTool.scanMatch();
        }
        String selection = String.valueOf(teamsSel.getSelectedItem());
        for (TeamSortingTool.Team team : TeamSortingTool.teamsArray ) {
            if (team.name.equals(selection)) {
                maxMembers.setText("" + team.maxMembers);
                minMembers.setText("" + team.minMembers);
            }
        }
        TeamSortingTool.updateDisplay(false);
    }

    private void clearTeamsActionPerformed(java.awt.event.ActionEvent evt) {
        TeamSortingTool.clearTeams(true);
    }
    
    private void recallActionPerformed(java.awt.event.ActionEvent evt) {
        try (FileInputStream in = new FileInputStream(new File(TEAM_FILE))){
            ObjectInputStream oIn = new ObjectInputStream(in);
            int teamsCount = TeamSortingTool.teamsArray.size();
            TeamSortingTool.teamsArray.clear();
            TeamSortingTool.studentsMatching.clear();
            for (int i = 0; i < teamsCount; i++) {
                TeamSortingTool.teamsArray.add((TeamSortingTool.Team) oIn.readObject());
            }
            for (TeamSortingTool.Team team : TeamSortingTool.teamsArray) {
                for(TeamSortingTool.StudentMatching student : team.members) {
                    TeamSortingTool.studentsMatching.add(student);
                }
                Collections.sort(TeamSortingTool.studentsMatching);
                for(TeamSortingTool.HelperStudent student : team.prefStudents) {
                    student.addButton = student.createButton();
                }
            }
            for (TeamSortingTool.StudentMatching student : TeamSortingTool.studentsMatching) {
                JCheckBox studentBox = new JCheckBox(student.name + " Preferece score: " + student.teamPriority);
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
            in.close();
            oIn.close();
            currentSort.setText("");
            TeamSortingTool.updateDisplay(true);
        }
        catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();         
        }
    }
    
    // Variable declaration - do not modify
    public javax.swing.JLabel Title;
    protected static javax.swing.JButton assign;
    protected static javax.swing.JLabel assigned;
    private javax.swing.JButton browseCanvas;
    private javax.swing.JButton browseCanvas1;
    private javax.swing.JButton browseMatching;
    private javax.swing.JButton browseReviews;
    private javax.swing.JButton browseTime;
    private javax.swing.JLabel canvasLab;
    private javax.swing.JLabel canvasLab1;
    protected static javax.swing.JTextField canvasLoc;
    protected static javax.swing.JTextField canvasLocR;
    protected static javax.swing.JComboBox<String> canvasWeekDestSel;
    protected static javax.swing.JComboBox<String> canvasWeekSelection;
    protected static javax.swing.JButton clearTeams;
    protected static javax.swing.JLabel currentSort;
    protected static javax.swing.JPanel display;
    private javax.swing.JLabel labelCanvasDest;
    private javax.swing.JLabel labelCanvasWeek;
    private javax.swing.JLabel labelTimeWeek;
    protected static javax.swing.JLabel lowestLabel;
    private static javax.swing.JTabbedPane mainTab;
    private javax.swing.JLabel matchingLab;
    protected static javax.swing.JTextField matchingLoc;
    private javax.swing.JPanel matchingTool;
    private javax.swing.JLabel maxLimit;
    protected static javax.swing.JTextField maxMembers;
    private javax.swing.JLabel maxValLab;
    protected static javax.swing.JTextField maxValTxt;
    private javax.swing.JLabel memberLimits;
    private javax.swing.JLabel minLimit;
    protected static javax.swing.JTextField minMembers;
    protected static javax.swing.JButton recall;
    private javax.swing.JPanel reviewScores;
    private static javax.swing.JScrollPane reviewScoresScroll;
    private javax.swing.JLabel reviewsLab;
    protected static javax.swing.JTextField reviewsLoc;
    private javax.swing.JButton runButton;
    private javax.swing.JButton runButtonR;
    private javax.swing.JButton save;
    protected static javax.swing.JLabel saved;
    protected static javax.swing.JLabel scanned;
    private static javax.swing.JButton setMemLimits;
    protected static javax.swing.JButton sort;
    protected static javax.swing.JLabel sorting;
    private static javax.swing.JLabel spacer;
    private javax.swing.JLabel studentLab;
    protected static javax.swing.JComboBox<String> studentSel;
    protected static javax.swing.JScrollPane teamScroll;
    private javax.swing.JLabel teamsLab;
    protected static javax.swing.JComboBox<String> teamsSel;
    private javax.swing.JLabel timeLab;
    protected static javax.swing.JTextField timeLoc;
    private javax.swing.JPanel timeTracking;
    protected static javax.swing.JComboBox<String> timeWeekSelection;
    private javax.swing.JCheckBox toggleOverride;
    // End of variable declaration
    @SuppressWarnings("unchecked")
    //===============================Setting Up The Form And Components================================\\
    private void initComponents() {
        Title = new javax.swing.JLabel();
        sorting = new javax.swing.JLabel();
        mainTab = new javax.swing.JTabbedPane();
        timeTracking = new javax.swing.JPanel();
        timeLab = new javax.swing.JLabel();
        timeLoc = new javax.swing.JTextField();
        canvasLoc = new javax.swing.JTextField();
        browseCanvas = new javax.swing.JButton();
        browseTime = new javax.swing.JButton();
        timeWeekSelection = new javax.swing.JComboBox<String>();
        labelCanvasWeek = new javax.swing.JLabel();
        runButton = new javax.swing.JButton();
        labelTimeWeek = new javax.swing.JLabel();
        canvasWeekSelection = new javax.swing.JComboBox<String>();
        canvasLab = new javax.swing.JLabel();
        maxValLab = new javax.swing.JLabel();
        maxValTxt = new javax.swing.JTextField();
        reviewScoresScroll = new javax.swing.JScrollPane();
        reviewScores = new javax.swing.JPanel();
        canvasLab1 = new javax.swing.JLabel();
        canvasLocR = new javax.swing.JTextField();
        browseCanvas1 = new javax.swing.JButton();
        reviewsLab = new javax.swing.JLabel();
        reviewsLoc = new javax.swing.JTextField();
        browseReviews = new javax.swing.JButton();
        runButtonR = new javax.swing.JButton();
        labelCanvasDest = new javax.swing.JLabel();
        canvasWeekDestSel = new javax.swing.JComboBox<String>();
        scanned = new javax.swing.JLabel();
        toggleOverride = new javax.swing.JCheckBox();
        matchingTool = new javax.swing.JPanel();
        matchingLab = new javax.swing.JLabel();
        matchingLoc = new javax.swing.JTextField();
        browseMatching = new javax.swing.JButton();
        teamsSel = new javax.swing.JComboBox<String>();
        teamsLab = new javax.swing.JLabel();
        studentSel = new javax.swing.JComboBox<String>();
        studentLab = new javax.swing.JLabel();
        assign = new javax.swing.JButton();
        assigned = new javax.swing.JLabel();
        sort = new javax.swing.JButton();
        maxMembers = new javax.swing.JTextField();
        setMemLimits = new javax.swing.JButton();
        save = new javax.swing.JButton();
        spacer = new javax.swing.JLabel();
        saved = new javax.swing.JLabel();
        memberLimits = new javax.swing.JLabel();
        minMembers = new javax.swing.JTextField();
        maxLimit = new javax.swing.JLabel();
        minLimit = new javax.swing.JLabel();
        teamScroll = new javax.swing.JScrollPane();
        display = new javax.swing.JPanel();
        clearTeams = new javax.swing.JButton();
        lowestLabel = new javax.swing.JLabel();
        recall = new javax.swing.JButton();
        currentSort = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("MAE Capstone Grading Tool");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        Title.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        Title.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        Title.setText("MAE Capstone Grading Tool ");
        getContentPane().add(Title, new org.netbeans.lib.awtextra.AbsoluteConstraints(171, 11, 487, -1));

        sorting.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        sorting.setText("Sorting");
        getContentPane().add(sorting, new org.netbeans.lib.awtextra.AbsoluteConstraints(700, 10, 60, 30));

        mainTab.setMinimumSize(new java.awt.Dimension(805, 450));
        mainTab.setPreferredSize(new java.awt.Dimension(805, 450));

        timeTracking.setMinimumSize(new java.awt.Dimension(790, 290));
        timeTracking.setPreferredSize(new java.awt.Dimension(790, 290));
        timeTracking.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        timeLab.setText("Time Tracking Export from Edusourced (E.g. Time_Overview_Export_20200331.xlsx):");
        timeTracking.add(timeLab, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 120, 570, 40));

        timeLoc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                timeLocActionPerformed(evt);
            }
        });
        timeTracking.add(timeLoc, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 150, 570, 40));

        canvasLoc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                canvasLocActionPerformed(evt);
            }
        });
        timeTracking.add(canvasLoc, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 80, 570, 40));

        browseCanvas.setText("Browse");
        browseCanvas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseCanvasActionPerformed(evt);
            }
        });
        timeTracking.add(browseCanvas, new org.netbeans.lib.awtextra.AbsoluteConstraints(630, 80, 140, 40));

        browseTime.setText("Browse");
        browseTime.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseTimeActionPerformed(evt);
            }
        });
        timeTracking.add(browseTime, new org.netbeans.lib.awtextra.AbsoluteConstraints(630, 150, 140, 40));

        timeWeekSelection.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { }));
        timeWeekSelection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                timeWeekSelectionActionPerformed(evt);
            }
        });
        timeTracking.add(timeWeekSelection, new org.netbeans.lib.awtextra.AbsoluteConstraints(370, 260, 230, 30));

        labelCanvasWeek.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        labelCanvasWeek.setText("Select Week From Canvas");
        timeTracking.add(labelCanvasWeek, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 230, 210, 30));

        runButton.setText("Run");
        runButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runButtonActionPerformed(evt);
            }
        });
        timeTracking.add(runButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(630, 230, 140, 60));

        labelTimeWeek.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        labelTimeWeek.setText("Select Week From Time Record");
        timeTracking.add(labelTimeWeek, new org.netbeans.lib.awtextra.AbsoluteConstraints(370, 230, 240, 30));

        canvasWeekSelection.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { }));
        canvasWeekSelection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                canvasWeekSelectionActionPerformed(evt);
            }
        });
        timeTracking.add(canvasWeekSelection, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 260, 200, 30));

        canvasLab.setText("Export from Canvas gradebook  (E.g. 2020-03-31T1238_Grades-Spring_2020_MAE-4800-0011.csv):");
        timeTracking.add(canvasLab, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 50, 570, 40));

        maxValLab.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        maxValLab.setText("Max Score");
        timeTracking.add(maxValLab, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 230, 70, 30));

        maxValTxt.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        timeTracking.add(maxValTxt, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 260, 70, 30));

        mainTab.addTab("Time Tracking", timeTracking);

        reviewScoresScroll.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        reviewScoresScroll.setMaximumSize(new java.awt.Dimension(790, 1000));
        reviewScoresScroll.setPreferredSize(new java.awt.Dimension(790, 655));

        reviewScores.setMaximumSize(new java.awt.Dimension(770, 770));
        reviewScores.setMinimumSize(new java.awt.Dimension(770, 770));
        reviewScores.setName(""); // NOI18N
        reviewScores.setPreferredSize(new java.awt.Dimension(770, 770));
        reviewScores.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        canvasLab1.setText("Export from Canvas gradebook  (E.g. 2020-03-31T1238_Grades-Spring_2020_MAE-4800-0011.csv):");
        reviewScores.add(canvasLab1, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 10, 570, 40));

        canvasLocR.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                canvasLocRActionPerformed(evt);
            }
        });
        reviewScores.add(canvasLocR, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 40, 570, 40));

        browseCanvas1.setText("Browse");
        browseCanvas1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseCanvas1ActionPerformed(evt);
            }
        });
        reviewScores.add(browseCanvas1, new org.netbeans.lib.awtextra.AbsoluteConstraints(630, 40, 140, 40));

        reviewsLab.setText("Peer Average Export from Edusourced (E.g. Survey_Export_20200415.xlsx):");
        reviewScores.add(reviewsLab, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 70, 570, 40));

        reviewsLoc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reviewsLocActionPerformed(evt);
            }
        });
        reviewScores.add(reviewsLoc, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 100, 570, 40));

        browseReviews.setText("Browse");
        browseReviews.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseReviewsActionPerformed(evt);
            }
        });
        reviewScores.add(browseReviews, new org.netbeans.lib.awtextra.AbsoluteConstraints(630, 100, 140, 40));

        runButtonR.setText("Run");
        runButtonR.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runButtonRActionPerformed(evt);
            }
        });
        reviewScores.add(runButtonR, new org.netbeans.lib.awtextra.AbsoluteConstraints(630, 170, 140, 50));

        labelCanvasDest.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        labelCanvasDest.setText("Select Destination In Canvas");
        reviewScores.add(labelCanvasDest, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 150, 210, 20));

        canvasWeekDestSel.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { }));
        canvasWeekDestSel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                canvasWeekDestSelActionPerformed(evt);
            }
        });
        reviewScores.add(canvasWeekDestSel, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 180, 200, 30));

        scanned.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        scanned.setText(" ");
        reviewScores.add(scanned, new org.netbeans.lib.awtextra.AbsoluteConstraints(540, 180, 80, 30));

        toggleOverride.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        toggleOverride.setText("Score of Zero if Self Assesment Undone");
        toggleOverride.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toggleOverrideActionPerformed(evt);
            }
        });
        reviewScores.add(toggleOverride, new org.netbeans.lib.awtextra.AbsoluteConstraints(240, 180, 290, 30));

        reviewScoresScroll.setViewportView(reviewScores);

        mainTab.addTab("Review Scores", reviewScoresScroll);

        matchingTool.setMaximumSize(new java.awt.Dimension(790, 650));
        matchingTool.setMinimumSize(new java.awt.Dimension(790, 650));
        matchingTool.setPreferredSize(new java.awt.Dimension(790, 650));
        matchingTool.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        matchingLab.setText("Student Matching Report from Edusourced (E.g. Student_Matching_Report_20200608.xlsx):");
        matchingTool.add(matchingLab, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 0, -1, 28));
        matchingTool.add(matchingLoc, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 32, 540, 25));

        browseMatching.setText("Browse");
        browseMatching.setMaximumSize(new java.awt.Dimension(125, 25));
        browseMatching.setMinimumSize(new java.awt.Dimension(125, 25));
        browseMatching.setPreferredSize(new java.awt.Dimension(125, 25));
        browseMatching.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseMatchingActionPerformed(evt);
            }
        });
        matchingTool.add(browseMatching, new org.netbeans.lib.awtextra.AbsoluteConstraints(590, 30, 210, -1));

        teamsSel.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { }));
        teamsSel.setMaximumSize(new java.awt.Dimension(200, 30));
        teamsSel.setMinimumSize(new java.awt.Dimension(200, 30));
        teamsSel.setPreferredSize(new java.awt.Dimension(200, 30));
        teamsSel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                teamsSelActionPerformed(evt);
            }
        });
        matchingTool.add(teamsSel, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 120, 220, 22));

        teamsLab.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        teamsLab.setText("Select Team");
        matchingTool.add(teamsLab, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 120, -1, -1));

        studentSel.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { }));
        studentSel.setMaximumSize(new java.awt.Dimension(200, 30));
        studentSel.setMinimumSize(new java.awt.Dimension(200, 30));
        studentSel.setPreferredSize(new java.awt.Dimension(200, 30));
        matchingTool.add(studentSel, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 80, 220, 22));

        studentLab.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        studentLab.setText("Select Student");
        matchingTool.add(studentLab, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 80, -1, -1));

        assign.setText("Assign");
        assign.setMargin(new java.awt.Insets(0, 0, 0, 0));
        assign.setMaximumSize(new java.awt.Dimension(70, 22));
        assign.setMinimumSize(new java.awt.Dimension(70, 22));
        assign.setPreferredSize(new java.awt.Dimension(70, 22));
        assign.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                assignActionPerformed(evt);
            }
        });
        matchingTool.add(assign, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 80, 70, 22));

        assigned.setFont(new java.awt.Font("Tahoma", 0, 9)); // NOI18N
        assigned.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        assigned.setText("Assigned!");
        matchingTool.add(assigned, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 60, 50, 20));

        sort.setText("Sort Teams");
        sort.setMaximumSize(new java.awt.Dimension(125, 25));
        sort.setMinimumSize(new java.awt.Dimension(125, 25));
        sort.setPreferredSize(new java.awt.Dimension(125, 25));
        sort.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sortActionPerformed(evt);
            }
        });
        matchingTool.add(sort, new org.netbeans.lib.awtextra.AbsoluteConstraints(590, 110, 100, -1));
        matchingTool.add(maxMembers, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 120, 35, 20));

        setMemLimits.setText("Set");
        setMemLimits.setMargin(new java.awt.Insets(0, 0, 0, 0));
        setMemLimits.setMaximumSize(new java.awt.Dimension(70, 22));
        setMemLimits.setMinimumSize(new java.awt.Dimension(70, 22));
        setMemLimits.setPreferredSize(new java.awt.Dimension(70, 22));
        setMemLimits.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setMemLimitsActionPerformed(evt);
            }
        });
        matchingTool.add(setMemLimits, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 120, 70, 22));

        save.setText("Save");
        save.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveActionPerformed(evt);
            }
        });
        matchingTool.add(save, new org.netbeans.lib.awtextra.AbsoluteConstraints(700, 80, 100, 25));

        spacer.setFont(new java.awt.Font("Tahoma", 0, 9)); // NOI18N
        spacer.setText("        ");
        matchingTool.add(spacer, new org.netbeans.lib.awtextra.AbsoluteConstraints(232, 63, -1, -1));

        saved.setFont(new java.awt.Font("Tahoma", 0, 9)); // NOI18N
        saved.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        saved.setText("Saved!");
        matchingTool.add(saved, new org.netbeans.lib.awtextra.AbsoluteConstraints(700, 60, 100, 10));

        memberLimits.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        memberLimits.setText("Member Limits");
        matchingTool.add(memberLimits, new org.netbeans.lib.awtextra.AbsoluteConstraints(330, 100, 90, 20));
        matchingTool.add(minMembers, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 120, 35, 20));

        maxLimit.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        maxLimit.setText("Max");
        matchingTool.add(maxLimit, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 100, 35, 20));

        minLimit.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        minLimit.setText("Min");
        matchingTool.add(minLimit, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 100, 35, 20));

        teamScroll.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        teamScroll.setMaximumSize(new java.awt.Dimension(9999, 9999));
        teamScroll.setMinimumSize(new java.awt.Dimension(790, 260));
        teamScroll.setOpaque(false);
        teamScroll.setPreferredSize(new java.awt.Dimension(790, 260));

        javax.swing.GroupLayout displayLayout = new javax.swing.GroupLayout(display);
        display.setLayout(displayLayout);
        displayLayout.setHorizontalGroup(
            displayLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        displayLayout.setVerticalGroup(
            displayLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 356, Short.MAX_VALUE)
        );

        teamScroll.setViewportView(display);

        matchingTool.add(teamScroll, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 150, 810, 360));

        clearTeams.setText("Clear Teams");
        clearTeams.setMaximumSize(new java.awt.Dimension(125, 25));
        clearTeams.setMinimumSize(new java.awt.Dimension(125, 25));
        clearTeams.setPreferredSize(new java.awt.Dimension(125, 25));
        clearTeams.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearTeamsActionPerformed(evt);
            }
        });
        matchingTool.add(clearTeams, new org.netbeans.lib.awtextra.AbsoluteConstraints(700, 110, 100, -1));
        matchingTool.add(lowestLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(420, 80, 160, 20));

        recall.setText("Recall Lowest");
        recall.setMaximumSize(new java.awt.Dimension(125, 25));
        recall.setMinimumSize(new java.awt.Dimension(125, 25));
        recall.setPreferredSize(new java.awt.Dimension(125, 25));
        recall.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                recallActionPerformed(evt);
            }
        });
        matchingTool.add(recall, new org.netbeans.lib.awtextra.AbsoluteConstraints(590, 80, 100, -1));
        matchingTool.add(currentSort, new org.netbeans.lib.awtextra.AbsoluteConstraints(420, 60, 390, 20));

        mainTab.addTab("Team Sort", matchingTool);

        getContentPane().add(mainTab, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 46, 816, 540));

        setSize(new java.awt.Dimension(852, 633));
        setLocationRelativeTo(null);
    }
}
