/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package brokerledger;

/**
 *
 * @author Pankaj
 */
public class Symbol {
    
    String symbol;
    String expiry;
    String type;
    String strike;
    String right;

    public Symbol(String symbol, String expiry, String type, String strike, String right) {
        this.symbol = symbol;
        this.expiry = expiry;
        this.type = type;
        if( !strike.equals("0")){
        this.strike = strike;
        }
        if(!right.equals("XX")){
        this.right = right;
        }
    }
    
    
}
