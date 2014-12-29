/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package brokerledger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Pankaj
 */
public class BrokerLedger {

    static ArrayList<Position> openingPosition=new ArrayList<>();
    static ArrayList<Trade> trades=new ArrayList<>();;
    static HashMap<String, SymbolMapping> mapping=new HashMap<>();
    static double openingLedger;
    static HashMap<String, String> input=new HashMap<>();
    static Date endDate;
    static ArrayList<OpenPositions> openPositions=new ArrayList<>();
    private static final Logger logger = Logger.getLogger(BrokerLedger.class.getName());

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)  {
        if (!(args.length == 3||args.length == 4||args.length == 5)) {
            usage();
        } else {
            for (int i = 0; i < args.length; i++) {
                input.put(args[i].split("=")[0].toLowerCase(), args[i].split("=")[1].toLowerCase());
            }            
        
        //initialize variables
        new Trade().reader(input.get("trades").toString(), trades);
        ArrayList <SymbolMapping> tempMapping=new ArrayList<>();
        new SymbolMapping().reader(input.get("symbolmapping").toString(), tempMapping);
        for(SymbolMapping sm:tempMapping){
            mapping.put(sm.brokerSymbol, sm);
        }
        openingLedger=Double.valueOf(input.get("openingledger").toString());
        if(input.get("openingpositions")==null){
            openingPosition=new ArrayList<>();
        }else{
            new Position().reader(input.get("openingpositions").toString(), openingPosition);
        }
        
        if(input.get("enddate")==null){
            endDate=new Date();
        }else{
            SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMdd");
            try {
                endDate=sdf.parse(input.get("enddate").toString());
            } catch (ParseException ex) {
                endDate=new Date();
                logger.log(Level.SEVERE, null, ex);
            }
        }
        OpenPositions op=new OpenPositions(openingPosition,trades,mapping);
        openPositions.add(op);
        while(op.positionClosingDate.before(endDate)){
         op=new OpenPositions(openingPosition,trades,mapping);
         openPositions.add(op);
        }
        }
    }
    

    static void usage() {
        System.out.println("Provide the following space seperated name=values combinations. The tool expects 3 or 4 combinations as below:");
        System.out.println("Optional: openingpositions=openingpositions.csv");
        System.out.println("Optional: enddate=last calculate date in format yyyyMMdd");
        System.out.println("Mandatory: trades=tradefilename.csv");
        System.out.println("Mandatory: symbolmapping=symbolmapping.csv");
        System.out.println("Mandatory: openingledger=value");
        
    }
}
