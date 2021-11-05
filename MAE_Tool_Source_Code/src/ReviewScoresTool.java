import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


/*
 * @author Jared Scott
 */
public class ReviewScoresTool {
    //REVIEW SCORES TOOL
    static boolean askedReview = false;
    static ArrayList<JTextField> percents = new ArrayList<JTextField>(); //Holds all of the percent objects on the UI
    static boolean overwriteRev = false; //Used to either pass over or overwrite exsisting data entries in the canvas file.
    static boolean askedRev = false; //Used to indicate if the user has been asked to overwrite data.
    static ArrayList<MAEGradingTool.Student> studentsMastRev = new ArrayList<MAEGradingTool.Student>(); //Master array for student data.
    static ArrayList<StudentReview> studentsReview = new ArrayList<StudentReview>(); //Data scraped from the Reviews Survey file.
    //Selected starting week (Canvas).
    static String startWeekCanvasRevS; 
    static int startWeekCanvasRev;
    //Arrays used to scrape data.
    static ArrayList<MAEGradingTool.StudentCanvas> studentsCanvasR = new ArrayList<MAEGradingTool.StudentCanvas>();
    static ArrayList<String> quesArray = new ArrayList<String>();
    static ArrayList<Integer> quesCols = new ArrayList<Integer>();
    static ArrayList<String> strings = new ArrayList<String>();
    static ArrayList<Integer> possiblePoints = new ArrayList<Integer>();
    static boolean inputValidRev = false; //Used for input validation when outputing to the canvas file.
    static double maxValRev; //Max value for student scores.
    static HashMap canvasDest = new HashMap();
    static int totalCount = 0;
    static double[] percentVals = new double[10]; //Used to calculate total percentage. 
    static double maxScore = 0; //Used to calculate the students overall rating. 
    static int totalPoss = 0; //Used to calculate the students overall rating
    static String reviewFile; //Review Data File Location
    static String canvasFileR;  //Canvas File Location
    static int contributionIndex;  
    static int professionalismIndex;
    static int contributionSelfIndex;
    static int responseRateIndex;
    static int professionalismSelfIndex;
    static boolean toggleOverride;
    
    //============================Class Declarations============================\\
    //This class is used to scrape data from the Edusource Survey Export
    public static class StudentReview {
        String[] nameList;
        ArrayList<Double> reviewScores;
        double score;
        double contribution;
        double professionalism;
        boolean selfDone;
        
        public StudentReview(String[] nameList, double cont, double prof, boolean selfDone){
            this.nameList = nameList;
            this.score = 0.0;
            this.contribution = cont;
            this.professionalism  = prof;
            this.selfDone = selfDone;
            this.setScore();
        }
        
        public void setScore() {
            double temp = this.contribution + this.professionalism;
            BigDecimal a = new BigDecimal("" +  temp); //Concatenation is needed to convert the double into a BigDecimal
            BigDecimal b = a.setScale(2, RoundingMode.UP);
            this.score = b.doubleValue();
            if (!selfDone && toggleOverride) {
                this.score = 0;//Sets the score to 0 if the self assesment is incomplete and the checkbox is checked. 
            }
        }
        public String toString() {
            return ("Name: " + Arrays.toString(this.nameList) + " Time: " + this.reviewScores);
        }
    }
    
    //=============================Scanning Methods=============================\\
    public static void scanReview(){
        MAEGradingTool.scanned.setText(" ");
        try {
            //I found this reduces errors with the drop down selection menus 
            MAEGradingTool.canvasWeekDestSel.removeAllItems();
            //Clears the data arrays for a new scan
            clearArrays();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        try {
            //Grabbing file locations
            String review = MAEGradingTool.reviewsLoc.getText();
            String canvas = MAEGradingTool.canvasLocR.getText();
            File temp1 = new File(review);
            File temp2 = new File(canvas);
            //Input Validation 
            if ( (review.equals("")) || (canvas.equals(""))) {
                JOptionPane.showMessageDialog(null, "Please enter both file locations");
                inputValidRev = false;
            }
            else if (!(review.endsWith(".xlsx")) || !(canvas.endsWith(".csv"))) {
                if (!(review.endsWith(".xlsx"))) {
                    JOptionPane.showMessageDialog(null, "Invalid Review File: Must be '.xlsx' file type");
                    inputValidRev = false;
                }
                if (!(canvas.endsWith(".csv"))) {
                    JOptionPane.showMessageDialog(null, "Invalid Canvas File: Must be '.csv' file type");
                    inputValidRev = false;
                }
            }
            else if (!(temp1.exists()) || !(temp2.exists())) {
                if (!(temp1.exists())) {
                    JOptionPane.showMessageDialog(null, "Invalid Review File: Location not found");
                    inputValidRev = false;
                }
                if (!(temp2.exists())) {
                    JOptionPane.showMessageDialog(null, "Invalid Canvas File: Location not found");
                    inputValidRev = false;
                }
            }
            else {
                //Input is valid, processing files. 
                inputValidRev = true;
                MAEGradingTool.scanned.setText("Scanned"); //Setting scanned alert for user
                getNamesCSVReview(canvas); //Pulling data from canvas file
                getColsReview(review); //Pulling data from reviews file
                //Matching names between canvas and reviews file
                //Looping through canvas students
                for (MAEGradingTool.StudentCanvas studentC : studentsCanvasR) {
                    boolean matchFound = false; 
                    //Looping through the review file students 
                    for(StudentReview studentR : studentsReview) {
                        //Comparing names
                        if (Utilities.compareNames(studentC.nameList, studentR.nameList)) {
                            //Adding student to master array if a match is found.
                            studentsMastRev.add(new MAEGradingTool.Student(studentC.nameList,studentC.row,studentR.score, studentR.contribution, studentR.professionalism, studentR.selfDone));
                            matchFound = true;
                            break;
                        }
                    }
                    //Adding original canvas row if a match is not found.
                    if (!(matchFound)) {
                        studentsMastRev.add(new MAEGradingTool.Student(studentC.nameList,studentC.row, studentC.data, true));
                    }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static String percentile(ArrayList<Double> dataArray) {
        Collections.sort(dataArray);
        
        double vals = dataArray.size();
        double curPercent =  0;
        int count = 1;
        for  (double score : dataArray) {
            curPercent = count / vals;
            if (curPercent > .80) {
                return score + "";
            }
            count += 1;
        }
        return "Error: Check data file for null values";
    }
    
    public static void runReview() {
        //Main method for outputing data to the canvas file within the Reviews Scores Tool.
        boolean percentsValid = true;
        try {
            getSelection();
            maxValRev = Double.valueOf(Utilities.getRow(canvasFileR,2)[startWeekCanvasRev]);
            //Checking to make sure that input and percentage values are valid.
            if (inputValidRev && percentsValid) {
                //Array to hold output strings. 
                ArrayList<String> output = new ArrayList<String>();
                try {
                    String sourceFile;
                    sourceFile = canvasFileR;
                    int  choice = 0;
                    //Adding first and second row of canvas file back into the output array.
                    output.add(String.join(",", Utilities.getRow(sourceFile,1)) + "\n");
                    output.add(String.join(",", Utilities.getRow(sourceFile,2)) + "\n");
                    int startIndex = startWeekCanvasRev;
                    ArrayList<Double> percentileArray = new ArrayList<Double>();
                    for (MAEGradingTool.Student student : studentsMastRev) {
                        //Iterating through the students in the master array and adding their data to the output file.
                        if(student.test == true) {
                            output.add(student.row + "\n");
                        }
                        else {
                            student.setScore();
                            //Calculates the students score
                            String[] canvasRow = new String[1];
                            //Grabbing the students row from the canvas file. 
                            try {
                                canvasRow = Utilities.getRow(canvasFileR, (student.rowCan + 1));
                            }
                            catch (Exception e){
                                e.printStackTrace();
                            }
                            //Checking if there is exsistant data in the canvas file location, 
                            //If there is data present in that cell ask the user if they want to overwrite
                            //The user will only be asked to over write exsistant data a single time. 
                            if (canvasRow[startIndex] == null || "".equals(canvasRow[startIndex])) {
                                if (student.score > maxValRev) {
                                    canvasRow[startIndex] = "" + maxValRev;
                                }
                                else {
                                    if (((student.score + "").length() > 3 && student.score < 10.0) || ((student.score + "").length() > 4 && (student.score < 100.0 && student.score > 10.0)) ) {
                                        student.score = Double.parseDouble(("" + student.score).substring(0, ("" + student.score).length() -1));
                                    }
                                    canvasRow[startIndex] = "" + student.score;
                                }
                            }
                            else {
                                //Asking the user if they want to overwrite existing data.
                                if (!(askedReview)) {
                                    choice = JOptionPane.showOptionDialog(null, "Overwrite Data?", "Data Present In Column", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
                                    askedReview = true;
                                }
                                if (choice == JOptionPane.YES_OPTION) {
                                    if (student.score > maxValRev) {
                                        canvasRow[startIndex] = "" + maxValRev;
                                    }
                                    else {
                                        if (((student.score + "").length() > 3 && student.score < 10.0) || ((student.score + "").length() > 4 && (student.score < 100.0 && student.score > 10.0)) ) {
                                            student.score = Double.parseDouble(("" + student.score).substring(0, ("" + student.score).length() -1));
                                            
                                        }
                                        canvasRow[startIndex] = "" + student.score;
                                    }
                                    askedReview = true;
                                }
                                else {
                                    askedReview = true;
                                }
                            }
                            output.add(String.join(",", canvasRow) + "\n");
                        }
                        percentileArray.add(student.score);
                    }
                    String percentile80 = percentile(percentileArray);
                    MAEGradingTool.percentileText.setText("80th Percentile: " + percentile80);
                    MAEGradingTool.percentileText.setVisible(true);
                    //Writing output to file.
                    try {
                        FileWriter writer = new FileWriter(canvasFileR); //Creating a writer to write to output file.
                        for (String line : output) {
                            writer.write(line);
                        }
                        writer.close();
                        JOptionPane.showMessageDialog(null, "Success! Original Canvas File Updated\n80th Percentile of Scores: " +  percentile80);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                    
                    //Reseting values. 
                    overwriteRev = false;
                    askedRev = false;
                }
                catch (Exception e) {
                    JOptionPane.showMessageDialog(null, "Error while writing to output file.\nPlease check output file name and destination.");
                    e.printStackTrace();
                }
            }
            else {
                String message = "Error:\n";
                if (!inputValidRev){
                    message = message + "Please enter valid file locations.\n";
                }
                if (!percentsValid){
                    message = message + "Please enter only numeric values for the percentages.\n";
                }
                JOptionPane.showMessageDialog(null, message);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void getNamesCSVReview(String fileNameCanvas) {
        //Removing the items from the drop down selection menu
        MAEGradingTool.canvasWeekDestSel.removeAllItems();
        try {
            //Creating a reader for the canvas file
            BufferedReader csvReader = new BufferedReader(new FileReader(fileNameCanvas));
            String row;
            int rowCount = -1;
            //Looping through the rows reading the data
            while ((row = csvReader.readLine()) != null) {
                rowCount += 1;
                String[] rowList = row.split(","); //Spliting the csv
                if (rowCount == 0) { //First row
                    int colCount = 0;
                    //Looping through columns within the row
                    for (String col : rowList) {
                        //Setting tracking booleans
                        boolean hasPeer = false;
                        boolean hasEval = false;
                        String[] colList = col.split(" "); //Splitting the cell contents
                        //Looping through words to find peer and evaluation within the same cell 
                        for (String word : colList) {
                            if("peer".equals(word.toLowerCase())) {
                                hasPeer = true;
                            }
                            else if("evaluation".equals(word.toLowerCase())) {
                                hasEval = true;
                            }
                        }
                        if (hasPeer && hasEval) { //Cell has both 'peer' and 'evaluation'
                                //Tracking the column numbers
                                MAEGradingTool.canvasWeekDestSel.addItem(colList[0]);
                                canvasDest.put(colList[0], colCount + 1);
                                strings.add(colList[0]);
                        }
                        colCount += 1;
                    }
                }
                //Splitting the row into a data array
                String[] data = row.split(",");
                ArrayList<String> nameArray = new ArrayList<String>(); //To store the student name
                int index = 0;
                //Filling name array with name data from multiple cells
                while(data[index].startsWith("\"") || data[index].endsWith("\"")){
                    nameArray.add(data[index].replace(",","").replace("\"", ""));
                    index++;
                }
                String nameString = "";
                //Combining name data into a string
                for (String seg : nameArray){
                    nameString += seg;
                }
                if (data[0].equals("Student") || data[0].equals("    Points Possible")) {
                    continue;
                }
                //List used to rearrange name components 
                String[] nList = nameString.split(" ");
                String nameStringForm = "";
                String temp = nList[0];
                for (int i = 1; i < nList.length ; i++ ) {
                    nameStringForm += nList[i] + " ";
                }
                //Formatted name string
                nameStringForm += temp;
                MAEGradingTool.StudentCanvas curStudent = new MAEGradingTool.StudentCanvas(nameStringForm.split(" "), rowCount, row);
                studentsCanvasR.add(curStudent);
            }
            csvReader.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void getColsReview(String fileNameRev) {
        int numQues = 0;
        try {
            FileInputStream fisT = new FileInputStream(fileNameRev); //Creates an input stream for the xlsx/xls file.      
            Workbook workbookRev = null; //Instatiates a Workbook instance of an xlsx/xls file.
            //Determines the file type and constructs the appropriate workbook object.
            if(fileNameRev.toLowerCase().endsWith("xlsx")) { 
                    workbookRev = new XSSFWorkbook(fisT);
            }
            else if(fileNameRev.toLowerCase().endsWith("xls")) {
                    workbookRev = new HSSFWorkbook(fisT);
            }
            else {
                throw new Exception("Invalid File");
            }
            Sheet sheet = workbookRev.getSheetAt(0); //Grabs the first sheet from the workbook
            Iterator<Row> rowIterator = sheet.iterator(); 
            int rowCount = 0;
            Row row = rowIterator.next(); //Grabs the row object
            if (rowCount == 0) {
                int col = 0;
                for (Cell cell : row) {
                    //Crawls through the cells in the row idcentifying the questions from the survey.
                    //Scrapes this data and stores it fopr further evaluation. 
                    String[] cellList = cell.toString().split(" ");
                    if (cell.toString().toLowerCase().equals("users responded out of users prompted")) { 
                        responseRateIndex = col;
                    }
                    if (cell.toString().toLowerCase().equals("contribution peer average")) { 
                        contributionIndex = col;
                    }
                    if (cell.toString().toLowerCase().equals("professionalism peer average")) { 
                        professionalismIndex = col;
                    }
                    if (cell.toString().toLowerCase().equals("professionalism self average")) { 
                        professionalismSelfIndex = col;
                    }
                    if (cell.toString().toLowerCase().equals("contribution self average")) { 
                        contributionSelfIndex = col;
                    }
                    if (cellList[0].toLowerCase().equals("rate") && (cell.toString().endsWith("- Peer Average"))) { //**NOTE: THIS NEEDS THE QUESTION TO START WITH THE WORD RATE.**
                        numQues += 1;
                        quesArray.add(cell.toString());
                        quesCols.add(col);
                    }
                    col += 1;
                }
            }
            double cellVal; //Used to pull the cell vlaue from the workbook.
            while (rowIterator.hasNext()) {
                ArrayList<Double> reviewScores = new ArrayList<Double>();
                row = rowIterator.next(); //Grabs the row object
                if ((row.getCell(responseRateIndex).toString().split("/")[0] + "").equals("0")) {
                    continue;
                }
                if ((row.getCell(0).toString().startsWith("ALL PROJECTS AVERAGE"))) {
                    continue;
                }
                Cell curCell = row.getCell(2);
                String[] nameArray = curCell.toString().split(" ");
                //Scraping the data
                for (int num : quesCols) {
                    curCell = row.getCell(num);
                    cellVal = Double.parseDouble(curCell.toString());
                    if (cellVal > maxScore) {
                        maxScore = cellVal;
                    }
                    reviewScores.add(cellVal);
                }
                //This whole next section is what handles the rounding within the reviews scores data file. 
                //This was done to match the rounding scheme found on Edusourced.com
                boolean conUp = false;
                boolean profUp = false;
                boolean conSelfDone = false;
                boolean profSelfDone = false;
                if ((row.getCell(contributionIndex).toString().length() - 1) >  0 && Integer.parseInt("" + row.getCell(contributionIndex).toString().charAt(row.getCell(contributionIndex).toString().length() - 1)) >= 5){
                    conUp = true;
                }
                if ((row.getCell(professionalismIndex).toString().length() -1) > 0 && Integer.parseInt("" + row.getCell(professionalismIndex).toString().charAt(row.getCell(professionalismIndex).toString().length() -1)) >= 5){
                    profUp = true;
                }
                double contribution;
                //Formating the value for contribution
                if (conUp) {
                    BigDecimal a = new BigDecimal("" +  Double.parseDouble(row.getCell(contributionIndex).toString()));
                    BigDecimal b = a.setScale(1, RoundingMode.UP); 
                    contribution = b.doubleValue();
                }
                else {
                    BigDecimal a = new BigDecimal("" +  Double.parseDouble(row.getCell(contributionIndex).toString()));
                    BigDecimal b = a.setScale(1, RoundingMode.DOWN); 
                    contribution = b.doubleValue();
                }
                //Formating the value for professionalism
                double professionalism;
                if(profUp){
                    BigDecimal a = new BigDecimal("" +  Double.parseDouble(row.getCell(professionalismIndex).toString()));
                    BigDecimal b = a.setScale(1, RoundingMode.UP); 
                    professionalism = b.doubleValue();
                }
                else {
                    BigDecimal a = new BigDecimal("" +  Double.parseDouble(row.getCell(professionalismIndex).toString()));
                    BigDecimal b = a.setScale(1, RoundingMode.DOWN); 
                    professionalism = b.doubleValue();
                }
                if(!"".equals(row.getCell(contributionSelfIndex).toString())){
                    conSelfDone = true;
                }
                if(!"".equals(row.getCell(professionalismSelfIndex).toString())){
                    profSelfDone = true;
                }
                boolean selfDone = conSelfDone && profSelfDone;
                studentsReview.add(new StudentReview(nameArray,contribution,professionalism,selfDone));
                rowCount += 1;
            }
            
            fisT.close(); //close file input stream
        } 
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void getSelection(){
        if (MAEGradingTool.canvasWeekDestSel.getItemCount() > 0 && !(MAEGradingTool.canvasWeekDestSel.getSelectedItem().toString().equals("")) && !canvasDest.isEmpty()) {
            String selection = MAEGradingTool.canvasWeekDestSel.getSelectedItem().toString();
            startWeekCanvasRevS = selection;
            startWeekCanvasRev = (int) canvasDest.get(startWeekCanvasRevS);
            totalPoss = Integer.parseInt(Utilities.getRow(canvasFileR,2)[startWeekCanvasRev].substring(0, 2));
        }
    }
    
    public static boolean isNumeric(String strNum) {
        //This method will tell you if the given string contains only numeric chars
        if (strNum == null) {
            return false;
        }
        try {
            double d = Double.parseDouble(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }
    
    public static double sum(double[] array) {
        //Summing method for arrays
        double sum = 0;
        for (double num : array){
            sum += num;
        }
        return sum;
    }
    
    public static void clearArrays(){
        studentsMastRev.clear(); //Master array for student data.
        studentsReview.clear(); //Data scraped from the Reviews Survey file.
        studentsCanvasR.clear();
        quesArray.clear();
        quesCols.clear();
        strings.clear();
        possiblePoints.clear();
    }
}

