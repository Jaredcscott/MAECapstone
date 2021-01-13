  __  __          ______    _____                _                    
 |  \/  |   /\   |  ____|  / ____|              | |                   <br>
 | \  / |  /  \  | |__    | |     __ _ _ __  ___| |_ ___  _ __   ___  <br>
 | |\/| | / /\ \ |  __|   | |    / _` | '_ \/ __| __/ _ \| '_ \ / _ \ <br>
 | |  | |/ ____ \| |____  | |___| (_| | |_) \__ \ || (_) | | | |  __/ <br>
 |_|  |_/_/    \_\______|  \_____\__,_| .__/|___/\__\___/|_| |_|\___| <br>
                                      | |                             <br>
                                      |_|                             <br>                   
   _____               _ _               _______          _           <br>
  / ____|             | (_)             |__   __|        | |          <br>
 | |  __ _ __ __ _  __| |_ _ __   __ _     | | ___   ___ | |          <br>
 | | |_ | '__/ _` |/ _` | | '_ \ / _` |    | |/ _ \ / _ \| |          <br>
 | |__| | | | (_| | (_| | | | | | (_| |    | | (_) | (_) | |          <br>
  \_____|_|  \__,_|\__,_|_|_| |_|\__, |    |_|\___/ \___/|_|          <br>
                                  __/ |                               <br>
                                 |___/                                <br>                       
## This tool uses the Java Runtime Environment. In order for it to work properly you will need to have 
## Java installed on your system. 

Here are links to installation media: 
Windows 10: https://www.java.com/en/download/win10.jsp
Linux: https://java.com/en/download/help/linux_x64_install.xml
Mac OS: https://java.com/en/download/faq/java_mac.xml

  _____ _             _____            _   _             _____         _ <br>
 |_   _(_)_ __  ___  |_   _| _ __ _ __| |_(_)_ _  __ _  |_   _|__  ___| |<br>
   | | | | '  \/ -_)   | || '_/ _` / _| / / | ' \/ _` |   | |/ _ \/ _ \ |<br>
   |_| |_|_|_|_\___|   |_||_| \__,_\__|_\_\_|_||_\__, |   |_|\___/\___/_|<br>
                                                 |___/                   <br>
To use the Time Tracking Tool:
    1. Run the file called "MAE_Grading_Tool.jar".
    2. If you have not done so already, download your Canvas and Time files. 
    3. Use the "Browse" buttons to navigate to and select your Canvas and Time files.
    4. When valid files have been provided. The tool's week drop down menus should be 
       filled with selections.
    5. Using the drop-down menus, select the week you wish to update in canvas, and 
       the week from the time-tracking sheet that contains the desired information.
    6. Be sure to enter a numeric value for the "Max Score". 
    7. Click "Run".
    8. If applicable choose yes or no to overwriting data.      

The original Canvas file provided will be updated with the desired information. 

  ___         _              ___                      _____         _ <br>
 | _ \_____ _(_)_____ __ __ / __| __ ___ _ _ ___ ___ |_   _|__  ___| |<br>
 |   / -_) V / / -_) V  V / \__ \/ _/ _ \ '_/ -_|_-<   | |/ _ \/ _ \ |<br>
 |_|_\___|\_/|_\___|\_/\_/  |___/\__\___/_| \___/__/   |_|\___/\___/_|<br>
                                                                      <br>
To use the Review Scores Tool:
    1. Run the file called "MAE_Grading_Tool.jar".
    2. If you have not done so already, download your Canvas and Survey Export files. 
    3. Use the "Browse" buttons to navigate to and select your Canvas and Survey 
       Export files.
    4. When valid files have been provided. The tool's Canvas entry drop down menu 
       should be filled with selections.
    5. Using the drop-down menu, select the Canvas entry you wish to update, and choose 
       if you want a score of 0 for non-respondents.
    6. Click "Run".
    7. If applicable choose yes or no to overwriting data.
    
The original Canvas file provided will be updated with the desired information.

  _____                 ___          _   _             _____         _ <br>
 |_   _|__ __ _ _ __   / __| ___ _ _| |_(_)_ _  __ _  |_   _|__  ___| |<br>
   | |/ -_) _` | '  \  \__ \/ _ \ '_|  _| | ' \/ _` |   | |/ _ \/ _ \ |<br>
   |_|\___\__,_|_|_|_| |___/\___/_|  \__|_|_||_\__, |   |_|\___/\___/_|<br>
                                               |___/                   <br>
To use the Team Sorting Tool:
    1. Run the file called "MAE_Grading_Tool.jar".
    2. If you have not done so already, download your Student Matching Report file. 
    3. Use the "Browse" buttons to navigate to and select your Student Matching Report file.
    4. When valid files have been provided. The tool's Student and Team drop down menu's 
       should be filled with selections.
    5. Using the student drop-down menu, you may manually assign students to different teams. 
       The selected student will be assigned to the selected team.  
    6. Using the Team drop-down menu, you can choose the team for a student, or you can 
       adjust the min and max team member limits. 
    7. Once manual assignments have been made. You can press "Sort Teams" to produce a 
       sort given the manual assignments made. 
    8. Once a sort has been produced, the application will store the lowest (best) scoring sort. 
       This can be recalled by pressing "Recall Lowest". 
    9. If a sort is not the lowest sort, but you would like to save it this can be done by 
       pressing "Save", the team information will be saved in a file "Team#.txt"
    10. Pressing "Clear Teams" will remove all UNLOCKED students from teams, leaving the 
        locked students assigned to their teams. 
    11. A list of students which a preference for a team is given after the team information.
        The format of the information is <Student Name> : <Preference for this team> 
        The "Add" button can be used to add these students directly. The student will be 
        locked upon assignment. 
    12. The overall preference score of a sort will be displayed after the sorting has been completed. 
    13. If a reasonable sort cannot be achieved the application will alert the user. Often this can 
        be resolved by manually assigning students or by adjusting team member limits.  
    14. By running sort a few times a relative minimum score sort can be achieved. 
    

To run the project with debugging information, go to the folder and
type the following:

java -jar "MAE_Grading_Tool.jar" 

Ver 1.7: Jared Scott
