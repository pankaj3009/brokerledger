/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package brokerledger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Pankaj
 */
public class Trade {
    String fileName;
    String clientName;
    String contractNoteReference;
    Date tradeDate;
    String tradeNumber;
    String orderTime;
    String executionTime;
    String side;
    String symbol;
    String size;
    String price;
    String brokerage;
    String serviceTax;
    String stt;
    String otherLevies;
    String netamount;

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
        this.size = size;
        this.price = price;
        this.brokerage = brokerage;
        this.serviceTax = serviceTax;
        this.stt = stt;
        this.otherLevies = otherLevies;
        this.netamount = netamount;
    }

}
