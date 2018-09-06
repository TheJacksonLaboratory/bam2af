package org.jax.bam2af.analysis;

import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SAMRecordIterator;
import org.apache.spark.sql.sources.In;

/**
 * The purpose of this class is to count the alleles present in the columns of an alignment
 * that is located at a specfic genomic interval. We go over each of the SAMRecords located
 * within the interval and count up REF and ALT. Note that we have an enumeration of possible ALT
 * Bases (ACGTN)
 */
public class IntervalAlleleCounter {

    enum Altbase {ALTBASE_A,ALTBASE_C,ALTBASE_G,ALTBASE_T,ALTBASE_N}


    private final String chromosome;

    private final int fromPosition;

    private final int toPosition;

    private ColumnCounter[] columns;



    public IntervalAlleleCounter(SAMRecordIterator iter, String chrom, int from, int to) {
        chromosome=chrom;
        fromPosition=from;
        toPosition=to;
        int len = from-to+1;
        columns=new ColumnCounter[len];
        processReads(iter);
    }



    private void processReads(SAMRecordIterator iter) {
        while (iter.hasNext()) {
            SAMRecord record = iter.next();
        }
    }
}
