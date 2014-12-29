/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package brokerledger;

import com.google.common.collect.TreeMultimap;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.kairosdb.client.HttpClient;
import org.kairosdb.client.builder.DataPoint;
import org.kairosdb.client.builder.QueryBuilder;
import org.kairosdb.client.builder.QueryMetric;
import org.kairosdb.client.response.QueryResponse;


/**
 *
 * @author Pankaj
 */
public class OpenPositions {
    HashMap<Symbol,Position> positions=new HashMap<>();
    Date positionClosingDate;
    double mtm;
    double todayPNL;
    double ledgerBalance;
    
    
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
        double newmtm=calculatePositionMTM(openingPosition);
        todayPNL=newmtm-mtm;
        mtm=newmtm;      
       
    }
    
    public double calculatePositionMTM(ArrayList<Position> openingPosition){
        double mtm = 0D;
        for (Position p : openingPosition) {
            if (p.positionMTMPrice == 0) {
                double mtmPrice = getSettlePrice(p.symbol, positionClosingDate);
                for (Position p1 : openingPosition) {
                    if (p1.symbol.equals(p.symbol)) {
                        p.positionMTMPrice = mtmPrice;
                    }
                }
            }
        }
        for (Position p:openingPosition){
            double tempmtm=p.positionSize*(p.positionMTMPrice-p.positionEntryPrice);
            mtm=mtm+tempmtm;
        }
        double ledgerMovement=calculateLedgerCashFlowOnPurchaseSale(openingPosition);
        ledgerBalance=ledgerBalance+mtm+ledgerMovement;
        return mtm;
        
        
    }

    public double calculateLedgerCashFlowOnPurchaseSale(ArrayList<Position> openingPosition){
        double balance=0;
        for(Position p:openingPosition){
            if (p.positionDate.equals(positionClosingDate) && p.symbol.strike!=null){
                balance=-p.positionSize*p.positionEntryPrice+balance;
            }
        }
        return balance;
    }
    
    public double getSettlePrice(Symbol s, Date d) {
         double settlePrice=0D;
         try{
         HttpClient client = new HttpClient("http://192.187.112.162:8085");
         String metric ;
         switch(s.type){
             case "STK":
                 metric="india.nse.equity.s2.daily.settle";
                 break;
             case "FUT":
                 metric="india.nse.option.s2.daily.settle";
                 break;
             case "OPT":
                 metric="india.nse.option.s2.daily.settle";
                 break;
             default:
                 metric=null;
                 break;
         }
                Date startDate=d;
                Date endDate=d;
                QueryBuilder builder = QueryBuilder.getInstance();
                builder.setStart(d)
                        .setEnd(d)
                        .addMetric(metric)
                        .addTag("symbol", s.symbol);
                if (!s.expiry.equals("")) {
                    builder.getMetrics().get(0).addTag("expiry", s.expiry);
                }
                if (!s.right.equals("")) {
                    builder.getMetrics().get(0).addTag("right", s.right);
                    builder.getMetrics().get(0).addTag("strike", s.strike);
                }

                builder.getMetrics().get(0).setLimit(1);
                builder.getMetrics().get(0).setOrder(QueryMetric.Order.DESCENDING);
                long time = new Date().getTime();
                QueryResponse response = client.query(builder);

                List<DataPoint> dataPoints = response.getQueries().get(0).getResults().get(0).getDataPoints();
                for (DataPoint dataPoint : dataPoints) {
                    long lastTime = dataPoint.getTimestamp();
                    Object value = dataPoint.getValue();
                    settlePrice=Double.parseDouble(value.toString());
                }  
         }catch (Exception e){
             
         }
        return settlePrice;
    }
    
    
}
