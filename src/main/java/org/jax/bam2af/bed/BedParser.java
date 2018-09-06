package org.jax.bam2af.bed;

import com.google.common.collect.ImmutableList;
import htsjdk.samtools.QueryInterval;
import htsjdk.samtools.util.Interval;
import org.jax.bam2af.exception.Bam2AfException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BedParser {

    private static final int MINIMUM_NUMBER_OF_BED_FIELDS=3;

    private final File bedFile;

    public BedParser(String bedPath){
       this.bedFile =new File(bedPath);
    }

    public BedParser(File file) {
        this.bedFile =file;
    }


    /** Parse */
    public  List<Interval>  parse() throws Bam2AfException {
        ImmutableList.Builder<Interval> builder = new ImmutableList.Builder<>();
        try {
            FileReader fr = new FileReader(this.bedFile);
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
                    Interval gi = new Interval(chrom,fromPos,toPos);
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


    /** Parse the BED file and provide an Array of QueryInterval objects -- as needed by the HTSJDK API*/
    public  QueryInterval[]  parse2QueryInterval(Map<String,Integer>chrom2index) throws Bam2AfException {
        List<QueryInterval> queryIntervalsList = new ArrayList<>();
        try {
            FileReader fr = new FileReader(this.bedFile);
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
                if (! chrom2index.containsKey(chrom)) {
                    throw new Bam2AfException("Could not find index for BED file chromosome: \"" + chrom +"\"");
                }
                try {
                    int index = chrom2index.get(chrom);
                    int fromPos=Integer.parseInt(A[1])+1; // convert to one-based
                    int toPos=Integer.parseInt(A[2])+1;
                    QueryInterval gi = new QueryInterval(index,fromPos,toPos);
                    queryIntervalsList.add(gi);
                } catch(NumberFormatException n) {
                    throw new Bam2AfException(String.format("Malformed BED line. Could not parse start pos (%s): %s",A[1],line));
                }
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return queryIntervalsList.toArray(new QueryInterval[queryIntervalsList.size()]);
    }


}
