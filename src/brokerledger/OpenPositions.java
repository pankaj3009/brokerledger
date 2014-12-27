/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package brokerledger;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


/**
 *
 * @author Pankaj
 */
public class OpenPositions {
    HashMap<Symbol,Position> positions=new HashMap<>();
    Date positionClosingDate;
    Double mtm;
    
    
    public OpenPositions(Date endDate){
        //Read CSV
    }
    
    /**
     * Creates openPositions for next day, if available.Else returns with the last positionClosingDate.
     * @param openingPosition
     * @param holidays 
     */
    public OpenPositions(ArrayList<Position> openingPosition, ArrayList<Trade> trades, HashMap<String,BrokerMapping> mapping){
    //get max of openingPositionDate
        Date d=new Date(0);
        for(Position p:openingPosition){
            if(d.before(p.positionDate)){
                d=p.positionDate;
            }
        }
        positionClosingDate=Utilities.nextGoodDay(d);
        Double openingMTM=0D;
        
        for(Trade t:trades){
            if (t.tradeDate.equals(positionClosingDate)){
                Symbol s=mapping.get(t.symbol).symbol;
                Position p=new Position();
                p.symbol=s;
                p.positionSize=t.side.equals("Sell")?-Integer.valueOf(t.size):Integer.valueOf(t.size);
                p.positionDate=t.tradeDate;
                p.positionEntryPrice=Double.valueOf(t.price);
                p.cost=Double.valueOf(t.serviceTax)+Double.valueOf(t.brokerage)+Double.valueOf(t.serviceTax);
                openingPosition.add(p);
            }
        }
        
       
    }
    
    public double calculatePositionMTM(ArrayList<Position> openingPosition){
        double mtm=0D;
        
        
        return mtm;
    }

    
    
}
