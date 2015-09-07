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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Pankaj
 */
public class Position implements ReaderWriterInterface {
    //Symbol symbol;

    String brokerSymbol;
    Integer positionSize;
    double positionEntryPrice;
    double positionMTMPrice;
    Date positionDate;
    double cost;
    boolean mtmUpdated=false;
    private static final Logger logger = Logger.getLogger(Position.class.getName());
    double realizedpnl;
    double yrealizedpnl;

    public Position() {
    }

    public Position(String[] input) {
        try {
            brokerSymbol = input[0];
            positionSize = Integer.valueOf(input[1]);
            positionEntryPrice = Double.valueOf(input[2]);
            positionMTMPrice = Double.valueOf(input[3]);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

            positionDate = sdf.parse(input[4]);
            cost = Double.valueOf(input[5]);
        } catch (ParseException ex) {
            logger.log(Level.SEVERE, null, ex);
        }

    }

    @Override
    public void reader(String inputfile, ArrayList target) {
        File inputFile = new File(inputfile);
        if (inputFile.exists() && !inputFile.isDirectory()) {
            try {
                List<String> existingFileLoad = Files.readAllLines(Paths.get(inputfile), StandardCharsets.UTF_8);
                existingFileLoad.remove(0);
                for (String s : existingFileLoad) {
                    if (!s.equals("")) {
                        String[] input = s.split(",");
                        target.add(new Position(input));
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
