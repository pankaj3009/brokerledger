/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package brokerledger;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Pankaj
 */
public class Trade implements ReaderWriterInterface {
    String fileName;
    String clientName;
    String contractNoteReference;
    Date tradeDate;
    String tradeNumber;
    String orderTime;
    String executionTime;
    String side;
    String symbol;
    int size;
    String price;
    String brokerage;
    String serviceTax;
    String stt;
    String otherLevies;
    String netamount;
    private static final Logger logger = Logger.getLogger(Trade.class.getName());

    public Trade(String fileName, String clientName, String contractNoteReference, String tradeDate, String tradeNumber, String orderTime, String executionTime, String side, String symbol, String size, String price, String brokerage, String serviceTax, String stt, String otherLevies, String netamount) {
        this.fileName = fileName;
        this.clientName = clientName;
        this.contractNoteReference = contractNoteReference;
        SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMdd");
        try {
            this.tradeDate = sdf.parse(tradeDate);
        } catch (ParseException ex) {
            Logger.getLogger(Trade.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.tradeNumber = tradeNumber;
        this.orderTime = orderTime;
        this.executionTime = executionTime;
        this.side = side;
        this.symbol = symbol;
        this.size = Integer.valueOf(size);
        this.price = price;
        this.brokerage = brokerage;
        this.serviceTax = serviceTax;
        this.stt = stt;
        this.otherLevies = otherLevies;
        this.netamount = netamount;
    }

    public Trade(String [] input) {
        if(input.length==16){
        this.fileName = input[0];
        this.clientName = input[1];
        this.contractNoteReference =input[2];
        SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMdd");
        try {
            this.tradeDate = sdf.parse(input[3]);
        } catch (ParseException ex) {
            Logger.getLogger(Trade.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.tradeNumber = input[4];
        this.orderTime = input[5];
        this.executionTime = input[6];
        this.side = input[7];
        this.symbol = input[8];
        this.size = this.side.equals("Buy")?Integer.valueOf(input[9]):-Integer.valueOf(input[9]);
        this.price = input[10];
        this.brokerage = input[11];
        this.serviceTax = input[12];
        this.stt = input[13];
        this.otherLevies = input[14];
        this.netamount = input[15];
        }
        else{
            System.out.println("Check trades input file. Row with file="+input[0]+ " does not have 16 columns");
        }
    }

    @Override
    public void reader(String inputfile, ArrayList target) {
               File inputFile = new File(inputfile);
        if (inputFile.exists() && !inputFile.isDirectory()) {
            try {
                List<String> existingFileLoad = Files.readAllLines(Paths.get(inputfile), StandardCharsets.UTF_8);
                existingFileLoad.remove(0);
                for (String s : existingFileLoad) {
                    if (!s.equals("")) {
                        String[] input = s.split(",");
                        target.add(new Trade(input));                        
                    }
                }
            } catch (IOException ex) {
                logger.log(Level.INFO, "101", ex);
            }
        }
    }
    
    public Trade(){
        
    }

    @Override
    public void writer(String fileName) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
