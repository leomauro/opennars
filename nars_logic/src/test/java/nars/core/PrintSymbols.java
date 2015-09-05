package nars.core;

import nars.Op;/**
 *
 * @author me
 */


public class PrintSymbols {

    public static void main(String[] args) {


        int symbols = 0;
        
        System.out.println("string" + "\t\t" + "rel?" + "\t\t" + "\t\t" + "opener?" + "\t\t" + "closer?");
        for (Op i : Op.values()) {
            System.out.println(i.str + "\t\t" + i.type + "\t\t" + i.opener + "\t\t" + i.closer);

            symbols++;
        }
        System.out.println("symbols=" + symbols);
    }
}
