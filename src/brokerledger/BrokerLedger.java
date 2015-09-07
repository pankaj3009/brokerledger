/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package brokerledger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 *
 * @author Pankaj
 */
public class BrokerLedger {

    static ArrayList<Position> openingPosition = new ArrayList<>();
    static ArrayList<Position> netPosition = new ArrayList<>();
    static ArrayList<Trade> trades = new ArrayList<>();
    static HashMap<String, SymbolMapping> mapping = new HashMap<>();
    static double openingLedger;
    static HashMap<String, String> input = new HashMap<>();
    static Date endDate;
    static ArrayList<OpenPositions> openPositions = new ArrayList<>();
    private static final Logger logger = Logger.getLogger(BrokerLedger.class.getName());

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException, IOException {
        if (!(args.length == 3 || args.length == 4 || args.length == 5)) {
            usage();
        } else {
            for (int i = 0; i < args.length; i++) {
                input.put(args[i].split("=")[0].toLowerCase(), args[i].split("=")[1].toLowerCase());
            }
        FileInputStream configFile;
        if (new File("logging.properties").exists()) {
            configFile = new FileInputStream("logging.properties");
            LogManager.getLogManager().readConfiguration(configFile);
        }
            //initialize variables
            new Trade().reader(input.get("trades").toString(), trades);
            ArrayList<SymbolMapping> tempMapping = new ArrayList<>();
            new SymbolMapping().reader(input.get("symbolmapping").toString(), tempMapping);
            for (SymbolMapping sm : tempMapping) {
                mapping.put(sm.brokerSymbol, sm);
            }
            openingLedger = Double.valueOf(input.get("openingledger").toString());
            if (input.get("openingpositions") == null) {
                openingPosition = new ArrayList<>();
            } else {
                new Position().reader(input.get("openingpositions").toString(), openingPosition);
            }

            if (input.get("enddate") == null) {
                endDate = new Date();
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                try {
                    endDate = sdf.parse(input.get("enddate").toString());
                } catch (ParseException ex) {
                    endDate = new Date();
                    logger.log(Level.SEVERE, null, ex);
                }
            }
            OpenPositions op = new OpenPositions(openingPosition, new ArrayList<Position>(),trades, mapping, null, openingLedger, 0);
            openPositions.add(op);
            while (op.positionClosingDate.before(endDate)) {
                op = new OpenPositions(openingPosition, op.netPosition,trades, mapping, op.positionClosingDate, op.ledgerBalance, op.ymtm);
                logger.log(Level.INFO, "Generated MTM for {0}", new Object[]{op.positionClosingDate});
                openPositions.add(op);
                
                
            }
            //write ymtm values to file
            Utilities.writeToFile("ledger.csv", new Date(), "Ledger Balance" + "," + "FutureMTM" + "," + "P&L Today");
            Utilities.writeToFile("ledger.csv", new Date(), openingLedger + "," + 0 + "," + 0);

            double lastLedgerBalance = openingLedger;
            for (OpenPositions p : openPositions) {
                double todayMovement = p.ledgerBalance - lastLedgerBalance;
                Utilities.writeToFile("ledger.csv", p.positionClosingDate, p.ledgerBalance + "," + todayMovement);
                lastLedgerBalance=p.ledgerBalance;
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
    
       static public ArrayList<Position> GetNetPosition(ArrayList<Position> openingPosition){
        ArrayList<Position>out=new ArrayList<>();
        for(Position p:openingPosition){
            boolean positionFound=false;
            for(Position pnet:out){
                if(pnet.brokerSymbol.equals(p.brokerSymbol)){
                    positionFound=true;
                    if(Math.abs(pnet.positionSize)<Math.abs(pnet.positionSize+p.positionSize)){
                        //trade on the same side as position
                        pnet.positionEntryPrice=(pnet.positionSize*pnet.positionEntryPrice+p.positionSize*p.positionEntryPrice)/(pnet.positionSize+p.positionSize);
                        //pnet.cost=pnet.cost+p.cost;
                        pnet.positionSize=pnet.positionSize+p.positionSize;
                        pnet.realizedpnl=pnet.realizedpnl-(p.cost);
                    }else{
                        int newPositionSize=pnet.positionSize+p.positionSize;
                        if(newPositionSize==0){
                            pnet.positionEntryPrice=0;
                            pnet.cost=0;
                            pnet.positionSize=0;
                            pnet.realizedpnl=pnet.realizedpnl-p.cost;
                        }else if(pnet.positionSize>0 && newPositionSize<0||pnet.positionSize<0 && newPositionSize>0){
                            //new trade has reversed position
                            pnet.realizedpnl=pnet.positionSize*(p.positionEntryPrice-pnet.positionEntryPrice)-p.cost;
                            pnet.positionSize=newPositionSize;
                        }else {
                            //position reduced
                            pnet.realizedpnl=p.positionSize*(pnet.positionEntryPrice-p.positionEntryPrice)-p.cost;
                            pnet.positionSize=newPositionSize;
                        }
                    }
                    
                }
            }
            if(!positionFound){
                Position p1=new Position();
                p1.realizedpnl=-p.cost;
                p1.brokerSymbol=p.brokerSymbol;
                p1.positionEntryPrice=p.positionEntryPrice;
                p1.positionSize=p.positionSize;
                out.add(p1);
                
                
            }
        }
        return out;
    }

}
