/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package brokerledger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
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
        public static String newline = System.getProperty("line.separator");
    
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
      
    public static Date addSeconds(Date startDate, int seconds){
        Calendar entryCal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kolkata"));
        entryCal.setTime(startDate);
        Calendar exitCal = (Calendar) entryCal.clone();
        exitCal.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
        
        exitCal.add(Calendar.SECOND, seconds);
        return exitCal.getTime();
    }  
    
    public static String getDateString(Date date, String format){
        SimpleDateFormat sdf=new SimpleDateFormat(format);
        return sdf.format(date);
    }
    
    /**
     * Returns negative if d1 is before d2.Returns positive if d1 is after d2
     * @param d1
     * @param d2
     * @param format
     * @return
     * @throws ParseException 
     */
    public static int dateCompare(String d1,String d2,String format) throws ParseException{
        SimpleDateFormat sdf=new SimpleDateFormat(format);
        Date dd1=sdf.parse(d1);
        Date dd2=sdf.parse(d2);
        return dd1.compareTo(dd2);
    }
    
    /**
     * Returns negative if d1 is before d2.Returns positive if d1 is after d2
     * @param d1
     * @param d2
     * @param format
     * @return
     * @throws ParseException 
     */
    public static int dateCompare(String d1,Date d2,String format)  {
        
            int out=0;
        try {    SimpleDateFormat sdf=new SimpleDateFormat(format);
            Date dd1=sdf.parse(d1);
            out=dd1.compareTo(d2);
        } catch (ParseException ex) {
            Logger.getLogger(Utilities.class.getName()).log(Level.SEVERE, null, ex);
        }
        return out;
    }
    
      public static void writeToFile(String filename, Date date,String content) {
        try {
            File dir = new File("logs");
            File file = new File(dir, filename);

            //if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }
            //true = append file
            SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd");
            SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss");
            String dateString = dateFormatter.format(date);
            String timeString = timeFormatter.format(new java.util.Date());
            FileWriter fileWritter = new FileWriter(file, true);
            BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
            bufferWritter.write(dateString+","+content + newline);
            bufferWritter.close();
        } catch (IOException ex) {
        }
    }
    
    public static void writeToFile(String filename,String content) {
        try {
            File dir = new File("logs");
            File file = new File(dir, filename);

            //if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }
            //true = append file
            FileWriter fileWritter = new FileWriter(file, true);
            BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
            bufferWritter.write(content + newline);
            bufferWritter.close();
        } catch (IOException ex) {
        }
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