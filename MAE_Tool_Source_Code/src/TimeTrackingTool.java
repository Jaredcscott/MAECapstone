import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import javax.swing.JOptionPane;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/*
 * @author Jared Scott
 */

public class TimeTrackingTool {
    static boolean overwrite = false; //Used to either pass over or overwrite existing data entries in the canvas file. 
    static boolean askedTime = false; //Used to indicate if the user has been asked to overwrite data. 
    static ArrayList<MAEGradingTool.Student> studentsMast = new ArrayList<MAEGradingTool.Student>(); //Master array for student data. 
    static ArrayList<StudentTime> studentsTime = new ArrayList<StudentTime>(); //Data scraped from the Time Tracking file. 
    static int startWeekTime; //Selected starting week (Time Tracking file).
    static int startWeekCanvas; //Selected starting week (Canvas).
    //Arrays used to scrape data. 
    static ArrayList<String> weeksArray = new ArrayList<String>(); 
    static ArrayList<Integer> timeWeekCols = new ArrayList<Integer>();
    static ArrayList<Integer> canvasWeekCols = new ArrayList<Integer>();
    static ArrayList<MAEGradingTool.StudentCanvas> studentsCanvas = new ArrayList<MAEGradingTool.StudentCanvas>();
    static String timeFile; //Time Tracking File Location.
    static String canvasFile;  //Canvas File Location.
    static int startWeek; //Output starting location.  
    static String[] firstRowC; //Used to store the first row of the canvas file.
    static String[] secondRowC; //Used to store the second row of the canvas file.
    static boolean inputValid = false; //Used for input validation when outputting to the canvas file. 
    static int maxVal; //Max value for student scores. 
    static int endWeekCanvas; //This is used to stop writing output to the canvas file. 
    
    //============================Class Declarations============================\\
    //This class is used to scrape data from the Edusource Time Overview Export
    public static class StudentTime {
        String[] nameList;
        ArrayList<Double> timeLog;
        public StudentTime(String[] nameList, ArrayList<Double> timeArray){
            this.nameList = nameList;
            this.timeLog = timeArray;
        }
        
        public String toString() {
            return ("Name: " + Arrays.toString(this.nameList) + " Time: " + this.timeLog);
        }
    }
    
    //=============================Scanning Methods=============================\\
    public static void scan(String type){
        try{
            //Removes items from the drop down menu first
           MAEGradingTool.timeWeekSelection.removeAllItems();
        }
        catch (Exception e) {
           e.printStackTrace();
        }
        try{
            //Removes items from the drop down menu first
           MAEGradingTool.canvasWeekSelection.removeAllItems();
        }
        catch (Exception e) {
        }
        //Clearing data arrays for a fresh scan
        clearArrays();
        try {
            //Pulling file locations 
            String time = MAEGradingTool.timeLoc.getText();
            String canvas = MAEGradingTool.canvasLoc.getText();
            File temp1 = new File(MAEGradingTool.timeLoc.getText());
            File temp2 = new File(MAEGradingTool.canvasLoc.getText());
            //Input validation
            if ( (time.equals("")) || (canvas.equals(""))) {
                JOptionPane.showMessageDialog(null, "Please enter both file locations");
                inputValid = false;
            }
            else if (!(time.endsWith(".xlsx")) || !(canvas.endsWith(".csv"))) {
                if (!(time.endsWith(".xlsx"))) {
                    JOptionPane.showMessageDialog(null, "Invalid Time File: Must be '.xlsx' file type");
                    inputValid = false;
                }
                if (!(canvas.endsWith(".csv"))) {
                    JOptionPane.showMessageDialog(null, "Invalid Canvas File: Must be '.csv' file type");
                    inputValid = false;
                }
            }
            else if (!(temp1.exists()) || !(temp2.exists())) {
                if (!(temp1.exists())) {
                    JOptionPane.showMessageDialog(null, "Invalid Time File: Location not found");
                    inputValid = false;
                }
                if (!(temp2.exists())) {
                    JOptionPane.showMessageDialog(null, "Invalid Canvas File: Location not found");
                    inputValid = false;
                }
            }
            else {
                //Input is valid, process the files 
                inputValid = true;
                //Pulling data from Time file
                getColsTime(MAEGradingTool.timeLoc.getText());
                //Pulling data from the canvas file
                getNamesCSV(MAEGradingTool.canvasLoc.getText());
                //Matching names between the canvas file and the time data file 
                for (MAEGradingTool.StudentCanvas studentC : studentsCanvas) {
                    boolean matchFound = false;
                    //Looping through students to match names 
                    for(StudentTime studentT : studentsTime) {
                        if (Utilities.compareNames(studentC.nameList, studentT.nameList)) {
                            //Adding students to the master array
                            studentsMast.add(new MAEGradingTool.Student(studentC.nameList,studentC.row,studentT.timeLog));
                            matchFound = true;
                            break;
                        }
                    }
                    if (!(matchFound)) {
                        //Adding in the original canvas row if no match is found 
                        studentsMast.add(new MAEGradingTool.Student(studentC.nameList,studentC.row, studentC.data));
                    }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void getNamesCSV(String fileNameCanvas) {
        try {
            //Setting up a reader for the canvas file 
            BufferedReader csvReader = new BufferedReader(new FileReader(fileNameCanvas));
            String row;
            int rowCount = -1;
            while ((row = csvReader.readLine()) != null) { //Reading file line by line
                rowCount += 1;
                if (rowCount == 0) {
                    String[] rowList = row.split(",");
                    int colCount = 0;
                    //Looping through column
                    for (String col : rowList) {
                        if (col.startsWith("TR")) {
                            String[] colList = col.split(" ");
                            MAEGradingTool.canvasWeekSelection.addItem(colList[0]);
                            canvasWeekCols.add(colCount);
                        }
                        else if (col.startsWith("W")) {
                            canvasWeekCols.add(colCount);
                            endWeekCanvas = colCount;
                            break;
                        }
                        colCount += 1;
                    }
                }
                String[] data = row.split(","); //Splitting row into data array
                ArrayList<String> nameArray = new ArrayList<String>(); //Used to scrape name data from cells
                int index = 0;
                while(data[index].startsWith("\"") || data[index].endsWith("\"")){
                    nameArray.add(data[index].replace(",","").replace("\"", ""));
                    index++;
                }
                String nameString = "";
                for (String seg : nameArray){
                    nameString += seg;
                }
                if (data[0].equals("Student") ||  data[0].equals("    Points Possible")) {
                    continue;
                }
                //Rearranging the name 
                String[] nList = nameString.split(" ");
                String nameStringForm = "";
                String temp = nList[0];
                for (int i = 1; i < nList.length ; i++ ) {
                    nameStringForm += nList[i] + " ";
                }
                nameStringForm += temp;
                MAEGradingTool.StudentCanvas curStudent = new MAEGradingTool.StudentCanvas(nameStringForm.split(" "), rowCount, row);
                studentsCanvas.add(curStudent);
            }
            csvReader.close();
        }
        catch (Exception e) {
            System.out.println(e);
        }
    }
        
    public static void getColsTime(String fileNameTime) {
        try {
            FileInputStream fisT = new FileInputStream(fileNameTime); //Creates an input stream for the xlsx/xls file.      
            Workbook workbookTime = null; //Instantiates a Workbook instance of an xlsx/xls file.
            //Determines the file type and constructs the appropriate workbook object.
            if(fileNameTime.toLowerCase().endsWith("xlsx")) {  
                workbookTime = new XSSFWorkbook(fisT);
            }
            else if(fileNameTime.toLowerCase().endsWith("xls")) {
                workbookTime = new HSSFWorkbook(fisT);
            }
            else {
                throw new Exception("Invalid File");
            }
            int numberOfSheets = workbookTime.getNumberOfSheets(); 
            for(int i=0; i < numberOfSheets; i++) { //Looping through each of the sheets
                Sheet sheet = workbookTime.getSheetAt(i);
                Iterator<Row> rowIterator = sheet.iterator();
                int rowCount = 0;
                while (rowIterator.hasNext()) {
                    Row row = rowIterator.next(); //Get the row object for examination.
                    if (rowCount == 0) {
                        int col = 0;
                        for (Cell cell : row) {
                            //Crawling through the row until the desired week is found within the Time Tracking file. 
                            if( cell.toString().startsWith("Week") && cell.toString().contains("Total")) {
                                String[] cellS = cell.toString().split(" ");
                                weeksArray.add(cellS[0] + " " + cellS[1] + " " + col);
                            }
                            col += 1;
                        }
                        //Scraping the data, and adding the weeks into the week selection drop down menu. 
                        for (String w : weeksArray) {
                            String[] week = w.split(" ");
                            MAEGradingTool.timeWeekSelection.addItem(week[0] + " " + week[1]);
                            timeWeekCols.add(Integer.parseInt(week[2]));
                        }
                    }
                    Cell curCell = row.getCell(0);
                    if (curCell.toString().startsWith("--")) { //Identifies the Student name and scrapes the data.
                        String[] name = ((String) curCell.toString().subSequence(2, curCell.toString().length())).split(" ");
                        ArrayList<Double> times = new ArrayList<Double>();
                        for (Integer col: timeWeekCols) {
                            if (row.getCell(col) != null){
                                Double cellVal = row.getCell(col).getNumericCellValue();
                                times.add(cellVal);
                            }  
                        }
                        StudentTime curStudent = new StudentTime(name, times);
                        studentsTime.add(curStudent);
                    }
                    rowCount += 1;
                } //end of rows iterator
            } //end of sheets for loop
            fisT.close(); //close file input stream
        } 
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void runTime() {
        //Main method for outputting data to the canvas file within the Time Tracking Tool. 
        boolean maxValValid;
        startWeekCanvas = Integer.parseInt(MAEGradingTool.canvasWeekSelection.getSelectedItem().toString().substring(2,MAEGradingTool.canvasWeekSelection.getSelectedItem().toString().length()));
        String[] selection = MAEGradingTool.timeWeekSelection.getSelectedItem().toString().split(" ");
        startWeek = Integer.parseInt(selection[1]);
        startWeekTime = Integer.parseInt(TimeTrackingTool.weeksArray.get(TimeTrackingTool.startWeek - 1).split(" ")[2]);
        try {
            //Attempting to grab the max score value from the input field. 
            try {
                maxVal = Integer.parseInt(MAEGradingTool.maxValTxt.getText());
                maxValValid = true;
            }
            catch (Exception e) {
                maxValValid = false;
            }
            //Determining if both input sources are valid. 
            if (inputValid && maxValValid) {
                ArrayList<String> output = new ArrayList<String>(); //Array used to store output strings. 
                try {
                    String sourceFile = canvasFile;
                    int choice = 0;
                    output.add(String.join(",", Utilities.getRow(sourceFile,1)) + "\n");
                    output.add(String.join(",", Utilities.getRow(sourceFile,2)) + "\n");
                    
                    int startIndex = canvasWeekCols.get(startWeekCanvas);
                    System.out.println("Start Canvas Week: " + startWeekCanvas + " Start Index: " + startIndex + " Cols: " + canvasWeekCols.toString());
                    for (MAEGradingTool.Student student : studentsMast) {
                        //Iterating through the master student array and adding their data to the canvas output file. 
                        if(student.data.isEmpty()) {
                            output.add(student.row + "\n");
                        }
                        else {
                            //Grabbing the students row from the canvas file. 
                            String[] canvasRow = Utilities.getRow(sourceFile, student.rowCan + 1);
                            
                            //Checking if there is exsistant data in the canvas file location, 
                            //If there is data present in that cell ask the user if they want to overwrite
                            //The user will only be asked to over write exsistant data a single time.
                            if ((canvasRow[startIndex] == null || "".equals(canvasRow[startIndex]))) {
                                if (student.data.get(startWeek - 1) > maxVal) {
                                    canvasRow[startIndex] = "" + maxVal; //Updating the value within the canvas file.
                                }
                                else {
                                    canvasRow[startIndex] = "" + student.data.get(startWeek - 1); //Updating the value within the canvas file.
                                }
                            }
                            else {
                                //Asking the user if they want to over write existing data.
                                if(!(askedTime)){
                                    choice = JOptionPane.showOptionDialog(null, "Overwrite Data?", "Data Present In Column", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
                                    askedTime = true;
                                }
                                if (choice == JOptionPane.YES_OPTION) {
                                    if (student.data.get(startWeek - 1) > maxVal) {
                                        canvasRow[startIndex] = "" + maxVal;
                                    }
                                    else {
                                        canvasRow[startIndex] = "" + student.data.get(startWeek - 1);
                                    }
                                    askedTime = true;
                                }
                                else {
                                    askedTime = true;
                                }
                            }
                            output.add(String.join(",", canvasRow) + "\n");
                        }
                    }
                    JOptionPane.showMessageDialog(null, "Success! Original Canvas File Updated");
                    FileWriter writer = new FileWriter(canvasFile); //Creating a writer to write to output file.
                    //Writing output to canvas file. 
                    for (String line : output) {
                        writer.write(line);
                    }
                    writer.close();
                    //Reseting the boolean values. 
                    overwrite = false;
                    askedTime = false;
                }
                catch (Exception e) {
                    JOptionPane.showMessageDialog(null, "Error while writing to output file.\nPlease check output file name and destination.");
                    e.printStackTrace();
                }
            }
            else {
                String message = "Error:\n";
                if (!maxValValid) {
                    message = message + "Please enter valid max score.\n";
                }
                if (!inputValid){
                    message = message + "Please enter valid file locations.\n";
                }
                JOptionPane.showMessageDialog(null, message);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void clearArrays(){
        studentsMast.clear(); //Master array for student data. 
        studentsTime.clear(); //Data scraped from the Time Tracking file. 
        weeksArray.clear(); 
        timeWeekCols.clear();
        canvasWeekCols.clear();
        studentsCanvas.clear();
    }
}
