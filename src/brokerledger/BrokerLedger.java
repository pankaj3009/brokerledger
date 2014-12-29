/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package brokerledger;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Pankaj
 */
public class BrokerLedger {

    static ArrayList<Position> openingPosition;
    static ArrayList<Trade> trades;
    static HashMap<String, BrokerMapping> mapping;
    static double openingLedger;
    static HashMap<String, String> input;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args.length != 3||args.length != 4) {
            usage();
        } else {
            for (int i = 0; i < args.length; i++) {
                input.put(args[i].split("=")[0].toLowerCase(), args[i].split("=")[1].toLowerCase());
            }            
        }
    }

    static void usage() {
        System.out.println("Provide the following space seperated name=values combinations. The tool expects 3 or 4 combinations as below:");
        System.out.println("Optional: openingpositions=openingpositions.csv");
        System.out.println("Mandatory: trades=tradefilename.csv");
        System.out.println("Mandatory: symbolmapping=symbolmapping.csv");
        System.out.println("Mandatory: openingLedger=value");
    }
}
