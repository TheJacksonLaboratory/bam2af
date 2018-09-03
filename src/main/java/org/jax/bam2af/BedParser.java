package org.jax.bam2af;

import org.jax.bam2af.exception.Bam2AfException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class BedParser {

    private static final int MINIMUM_NUMBER_OF_BED_FIELDS=3;


    public BedParser(String bedPath){}


    /** Parse */
    private void parse(String path) throws Bam2AfException {
        try {
            FileReader fr = new FileReader(path);
            BufferedReader br = new BufferedReader(fr);
            String line;
            while ((line = br.readLine()) != null) {
                String A[] = line.split("\t");
                if (A.length < MINIMUM_NUMBER_OF_BED_FIELDS) {
                    throw new Bam2AfException(String.format("Malformed BED file line : %s (at least %d fields required but we got %d",
                            line,MINIMUM_NUMBER_OF_BED_FIELDS,A.length));
                }
                String chrom=A[0];
                int pos;
                try {
                    pos=Integer.parseInt(A[1])+1; // convert to one-based
                } catch(NumberFormatException n) {
                    throw new Bam2AfException(String.format("Malformed BED line. Could not parse start pos (%s): %s",A[1],line));
                }
                String accession=A[3]; // something like rs123456 or custom name
                String strand=A[5];
                if (! strand.equals("+") && ! strand.equals("-")) {
                    throw new Bam2AfException(String.format("Malformed BED line. Strand was %s. Line=%s",strand,line));
                }
                boolean isNoncoding=false; // needed for interface but not used

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
