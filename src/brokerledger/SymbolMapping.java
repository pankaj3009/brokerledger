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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Pankaj
 */
public class SymbolMapping implements ReaderWriterInterface {
    String brokerSymbol;
    Symbol symbol;
    private static final Logger logger = Logger.getLogger(SymbolMapping.class.getName());

    public SymbolMapping(String brokerSymbol, Symbol symbol) {
        this.brokerSymbol = brokerSymbol;
        this.symbol = symbol;
    }
    public SymbolMapping(){
        
    }
    public SymbolMapping(String []input){
        if(input.length==6){
        this.brokerSymbol=input[0];
        Symbol tempSymbol=new Symbol(input[1],input[2],input[3],input[4],input[5]);
        this.symbol=tempSymbol;
        }else{
            System.out.println("Number of columns in symbol mapping does not equal 5");
        }
    }

    @Override
    public void reader(String inputfile, ArrayList target,boolean removeFirstLine) {
       File inputFile = new File(inputfile);
        if (inputFile.exists() && !inputFile.isDirectory()) {
            try {
                List<String> existingFileLoad = Files.readAllLines(Paths.get(inputfile), StandardCharsets.UTF_8);
                if(removeFirstLine){
                existingFileLoad.remove(0);
                }
                for (String s : existingFileLoad) {
                    if (!s.equals("")) {
                        String[] input = s.split(",");
                        target.add(new SymbolMapping(input));                        
                    }
                }
            } catch (IOException ex) {
                logger.log(Level.INFO, "101", ex);
            }
        }
    }

    @Override
    public void writer(String fileName) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    
}
