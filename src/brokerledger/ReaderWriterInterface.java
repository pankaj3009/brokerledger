/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package brokerledger;

import java.util.ArrayList;

/**
 *
 * @author pankaj
 */
public interface ReaderWriterInterface {
    
    public void reader(String inputfile, ArrayList target,boolean removeFirstLine);
    public void writer (String fileName);
}

