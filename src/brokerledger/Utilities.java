/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package brokerledger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Pankaj
 */
public class Utilities {
    
    
    static ArrayList<Calendar> holidays=new ArrayList<>();
    
    public Utilities(String holidayFileName) throws FileNotFoundException{
        List<String> input=Utilities.readAllLines(holidayFileName, true);
        for(String i: input){
            Calendar c=Calendar.getInstance(TimeZone.getTimeZone("Asia/Kolkata"));
            c.set(Calendar.YEAR, Integer.valueOf(i.substring(0, 4)));
            c.set(Calendar.MONTH, Integer.valueOf(i.substring(4, 6)));
            c.set(Calendar.DATE, Integer.valueOf(i.substring(6, 8)));
            holidays.add(c);
        }
    }
        
      public static Date nextGoodDay(Date startDate) {
        Calendar entryCal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kolkata"));
        entryCal.setTime(startDate);
        Calendar exitCal = (Calendar) entryCal.clone();
        exitCal.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
        
        exitCal.add(Calendar.DATE, 1);
        //if(minuteAdjust==0){
        //    exitCal.add(Calendar.SECOND, 1);
        //}
        while(exitCal.get(Calendar.DAY_OF_WEEK) == 7 || exitCal.get(Calendar.DAY_OF_WEEK) == 1||holidays.contains(exitCal)){
            exitCal.add(Calendar.DATE, 1);
        }
        return exitCal.getTime();
    }
      
    public static List<String> readAllLines(String inputfile, boolean header) {
        List<String> load=new ArrayList<>();
        File inputFile = new File(inputfile);
        if (inputFile.exists() && !inputFile.isDirectory()) {            
            try {
                List<String> existingSymbolsLoad = Files.readAllLines(Paths.get(inputfile), StandardCharsets.UTF_8);
                if(header){
                existingSymbolsLoad.remove(0);
                }                
            } catch (IOException ex) {
                Logger.getLogger(Utilities.class.getName()).log(Level.SEVERE, null, ex);
            }
    }
    return load;    
}
    
}