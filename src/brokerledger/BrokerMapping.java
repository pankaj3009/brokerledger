/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package brokerledger;

/**
 *
 * @author Pankaj
 */
public class BrokerMapping {
    String brokerSymbol;
    Symbol symbol;

    public BrokerMapping(String brokerSymbol, Symbol symbol) {
        this.brokerSymbol = brokerSymbol;
        this.symbol = symbol;
    }
    
    
}
