/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package brokerledger;

import com.google.common.collect.TreeMultimap;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    Date positionClosingDate;
    double ymtm;
    ArrayList<Position> netPosition;
    double todayMTMPNL;
    double todayRealizedPNL;
    double ledgerBalance;
    private static final Logger logger = Logger.getLogger(OpenPositions.class.getName());
    
    
    public OpenPositions(Date endDate){
        //Read CSV
    }
    
    /**
     * Creates openPositions for next day, if available.Else returns with the last positionClosingDate.
     * @param openingPosition
     * @param holidays 
     */
    public OpenPositions(ArrayList<Position> openingPosition,ArrayList<Position> netPosition,ArrayList<Trade> trades, HashMap<String,SymbolMapping> mapping, Date lastProcessedDate, double ledgerBalance, double ytdMTM){
    //get max of openingPositionDate
        this.ledgerBalance=ledgerBalance;
        this.ymtm=ytdMTM;
        this.netPosition=netPosition;
        Date d=lastProcessedDate==null?new Date(0):lastProcessedDate;
        if(openingPosition.size()>0){
        for(Position p:openingPosition){
            if(d.before(p.positionDate)){
                d=p.positionDate;
            }
        }
                positionClosingDate=Utilities.nextGoodDay(d);

        }else{
            for(Trade t:trades){
                if(d.equals(new Date(0))){
                    d=t.tradeDate;
                }else{
                    if(d.after(t.tradeDate)){
                        d=t.tradeDate;
                    }
                }
            }
                    positionClosingDate=d;
        }
       
        for(Trade t:trades){
            if (t.tradeDate.equals(positionClosingDate)){
               Position p=new Position();
                p.brokerSymbol=t.symbol;
                p.positionSize=t.size;
                p.positionDate=t.tradeDate;
                p.positionEntryPrice=Double.valueOf(t.price);
                p.cost=Double.valueOf(t.serviceTax)+Double.valueOf(t.brokerage)+Double.valueOf(t.stt)+Double.valueOf(t.otherLevies);
                openingPosition.add(p);
            }
        }
        SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMdd");
        for(Position p:openingPosition){
            Symbol s=mapping.get(p.brokerSymbol).symbol;
            Date symbolExpiry=null;
            try {
                symbolExpiry = sdf.parse(s.expiry);
            } catch (ParseException ex) {
                Logger.getLogger(OpenPositions.class.getName()).log(Level.SEVERE, null, ex);
            }
            if(symbolExpiry.after(positionClosingDate)){
                p.mtmUpdated=false;
                
            }
        }
        if(this.netPosition.isEmpty()){
            //add opening positions
            for(Position p:openingPosition){
            if(p.positionDate.before(positionClosingDate))
                this.netPosition.add(p);
                
            }
        }
        for(Position p:this.netPosition){
            p.yrealizedpnl=p.realizedpnl;
            p.realizedpnl=0;
        }
        this.netPosition=GetNetPosition(openingPosition,mapping); 
        todayRealizedPNL=calculateRealizedPNL(this.netPosition,mapping);
        
        double futuresMTMToday=calculateFuturesMTM(this.netPosition,mapping);
        todayMTMPNL=futuresMTMToday-ymtm;
        double ledgerMovement=calculateLedgerCashFlowOnPurchaseSale(openingPosition,mapping);
        this.ledgerBalance=this.ledgerBalance+todayMTMPNL+todayRealizedPNL+ledgerMovement;
        ymtm=futuresMTMToday;
       
    }
    
    public ArrayList<Position> GetNetPosition(ArrayList<Position> openingPosition,HashMap<String,SymbolMapping> mapping){
        ArrayList<Position>out=new ArrayList<>();
        for(Position p:openingPosition){
            boolean positionFound=false;
            if(p.positionDate.equals(positionClosingDate)){
            for(Position pnet:this.netPosition){
                if(pnet.brokerSymbol.equals(p.brokerSymbol)){
                    positionFound=true;
                    if(Math.abs(pnet.positionSize)<Math.abs(pnet.positionSize+p.positionSize)){
                        //trade on the same side as position
                        pnet.positionEntryPrice=(pnet.positionSize*pnet.positionEntryPrice+p.positionSize*p.positionEntryPrice)/(pnet.positionSize+p.positionSize);
                        //pnet.cost=pnet.cost+p.cost;
                        pnet.positionSize=pnet.positionSize+p.positionSize;
                        pnet.realizedpnl=pnet.realizedpnl-(p.cost);
                        logger.log(Level.INFO,"Position added. Date:{0},Symbol:{1},New PositionSize:{2},RealizedPNL:{3}",new Object[]{positionClosingDate,pnet.brokerSymbol,pnet.positionSize,pnet.realizedpnl});
                            
                        break;
                    }else{
                        int newPositionSize=pnet.positionSize+p.positionSize;
                        if(newPositionSize==0){
                            pnet.realizedpnl=pnet.realizedpnl+p.positionSize*(pnet.positionEntryPrice-p.positionEntryPrice)-p.cost;
                            pnet.positionEntryPrice=0;
                            pnet.cost=0;
                            pnet.positionSize=0;
                            logger.log(Level.INFO,"Position squared. Date:{0},Symbol:{1},New PositionSize:{2},RealizedPNL:{3}",new Object[]{positionClosingDate,pnet.brokerSymbol,pnet.positionSize,pnet.realizedpnl});
                            break;
                        }else if(pnet.positionSize>0 && newPositionSize<0||pnet.positionSize<0 && newPositionSize>0){
                            //new trade has reversed position
                            pnet.realizedpnl=pnet.realizedpnl+pnet.positionSize*(p.positionEntryPrice-pnet.positionEntryPrice)-p.cost;
                            pnet.positionSize=newPositionSize;
                            logger.log(Level.INFO,"Position Reversed. Date:{0},Symbol:{1},New PositionSize:{2},RealizedPNL:{3}",new Object[]{positionClosingDate,pnet.brokerSymbol,pnet.positionSize,pnet.realizedpnl});
                            break;
                        }else {
                            //position reduced
                            pnet.realizedpnl=pnet.realizedpnl+p.positionSize*(pnet.positionEntryPrice-p.positionEntryPrice)-p.cost;
                            pnet.positionSize=newPositionSize;
                            logger.log(Level.INFO,"Position Reduced. Date:{0},Symbol:{1},New PositionSize:{2},RealizedPNL:{3}",new Object[]{positionClosingDate,pnet.brokerSymbol,pnet.positionSize,pnet.realizedpnl});
                            break;
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
                /*
                double cumRealized=0;
                for(Position p2:this.netPosition){
                    if(p2.brokerSymbol.equals(p1.brokerSymbol)){
                        cumRealized=p2.yrealizedpnl;
                    }
                }
                p1.yrealizedpnl=cumRealized;
                */
                this.netPosition.add(p1);
                
                
            }
            }
        }
        return this.netPosition;
    }
    
    public double calculateRealizedPNL(ArrayList<Position> netPosition,HashMap<String,SymbolMapping> mapping){
        double out=0;
          for (Position p : netPosition) {
              out=out+p.realizedpnl-p.yrealizedpnl;
          }      
        return out;
    }
    
    public double calculateFuturesMTM(ArrayList<Position> openingPosition,HashMap<String,SymbolMapping> mapping){
        double mtm = 0D;
        for (Position p : openingPosition) {
            //if (!p.mtmUpdated) {
                Symbol s=mapping.get(p.brokerSymbol).symbol;
                double mtmPrice = getSettlePrice(s, positionClosingDate);
                logger.log(Level.INFO,"Settle Price for {0} for date {1} = {2}",new Object[]{p.brokerSymbol,positionClosingDate,mtmPrice});
                for (Position p1 : openingPosition) {
                    Symbol s1=mapping.get(p1.brokerSymbol).symbol;
                    if (s1.equals(s)) {
                        if(mtmPrice>=0){
                        p1.positionMTMPrice = mtmPrice;
                //        p1.mtmUpdated=true;
                   }else{
              //          p1.mtmUpdated=true;
                        //logger.log(Level.INFO,"Updated MTM for {0},{1},{2}",new Object[]{p.brokerSymbol,p.positionSize,p.positionEntryPrice});
                    }
                    }
                }
            //}
        }
        for (Position p:openingPosition){
            Symbol s=mapping.get(p.brokerSymbol).symbol;
            if(!(p.positionSize>0 && s.right!=null))
            {//only calculate MTM for option sales and futures
            double tempmtm=p.positionSize*(p.positionMTMPrice-p.positionEntryPrice)-p.cost;
            mtm=mtm+tempmtm;
            }
        }
        return mtm;
        
        
    }

    public double calculateLedgerCashFlowOnPurchaseSale(ArrayList<Position> openingPosition,HashMap<String,SymbolMapping>mapping){
        double balance=0;
        for(Position p:openingPosition){
            Symbol s=mapping.get(p.brokerSymbol).symbol;
            if (s.right!=null && p.positionDate.equals(positionClosingDate) ){
                balance=-p.positionSize*p.positionEntryPrice+balance-p.cost;
                for(Position pnet:netPosition){
                    if(pnet.brokerSymbol.equals(p.brokerSymbol)&& !pnet.mtmUpdated){
                        balance=balance-pnet.realizedpnl-pnet.yrealizedpnl;
                        pnet.mtmUpdated=true;
                    }
                }
            }
        }
        return balance;
    }
    
    public double getSettlePrice(Symbol s, Date d) {
         double settlePrice=-1;
         try{
         HttpClient client = new HttpClient("http://192.187.112.162:8085");
         String metric ;
         switch(s.type){
             case "STK":
                 metric="india.nse.equity.s4.daily.settle";
                 break;
             case "FUT":
                 metric="india.nse.future.s4.daily.settle";
                 break;
             case "OPT":
                 metric="india.nse.option.s4.daily.settle";
                 break;
             default:
                 metric=null;
                 break;
         }
                Date startDate=d;
                Date endDate=d;
                QueryBuilder builder = QueryBuilder.getInstance();
                builder.setStart(d)
                        .setEnd(Utilities.addSeconds(d, 1))
                        .addMetric(metric)
                        .addTag("symbol", s.symbol.toLowerCase());
                if (s.expiry!=null) {
                    builder.getMetrics().get(0).addTag("expiry", s.expiry);
                }
                if (s.right!=null) {
                    builder.getMetrics().get(0).addTag("option", s.right);
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
             logger.log(Level.INFO,null,e);
         }
        return settlePrice;
    }
    
    
}
