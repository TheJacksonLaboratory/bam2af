package org.jax.bam2af.bed;

import com.google.common.collect.ImmutableList;
import org.jax.bam2af.exception.Bam2AfException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class BedParser {

    private static final int MINIMUM_NUMBER_OF_BED_FIELDS=3;

    private final String bedFilePath;

    public BedParser(String bedPath){
       this.bedFilePath=bedPath;
    }


    /** Parse */
    public  List<GenomicInterval>  parse() throws Bam2AfException {
        ImmutableList.Builder<GenomicInterval> builder = new ImmutableList.Builder<>();
        try {
            FileReader fr = new FileReader(this.bedFilePath);
            BufferedReader br = new BufferedReader(fr);
            String line;
            while ((line = br.readLine()) != null) {
                if ( line.isEmpty()) {
                    continue; // skip empty lines that might be found at the end of file etc.
                }
                String A[] = line.split("\t");
                if (A.length < MINIMUM_NUMBER_OF_BED_FIELDS) {
                    throw new Bam2AfException(String.format("Malformed BED file line : %s (at least %d fields required but we got %d",
                            line,MINIMUM_NUMBER_OF_BED_FIELDS,A.length));
                }
                String chrom=A[0];
                try {
                    int fromPos=Integer.parseInt(A[1])+1; // convert to one-based
                    int toPos=Integer.parseInt(A[2])+1;
                    GenomicInterval gi = new GenomicInterval(chrom,fromPos,toPos);
                    builder.add(gi);
                } catch(NumberFormatException n) {
                    throw new Bam2AfException(String.format("Malformed BED line. Could not parse start pos (%s): %s",A[1],line));
                }
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return builder.build();
    }

}
