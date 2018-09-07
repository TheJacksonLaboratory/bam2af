package org.jax.bam2af.analysis;

import htsjdk.samtools.*;
import org.jax.bam2af.exception.Bam2AfException;

/**
 * The purpose of this class is to count the alleles present in the columns of an alignment
 * that is located at a specfic genomic interval. We go over each of the SAMRecords located
 * within the interval and count up REF and ALT. Note that we have an enumeration of possible ALT
 * Bases (ACGTN)
 */
public class IntervalAlleleCounter {

    enum Altbase {ALTBASE_A, ALTBASE_C, ALTBASE_G, ALTBASE_T, ALTBASE_N}


    private final String chromosome;

    private final int fromPosition;

    private final int toPosition;

    private final ColumnCounter[] columns;


    public IntervalAlleleCounter(SAMRecordIterator iter, String chrom, int from, int to) {
        chromosome = chrom;
        fromPosition = from;
        toPosition = to;
        int len = to - from + 1;
        columns = new ColumnCounter[len];
        while (iter.hasNext()) {
            try {
                processRead(iter.next());
            } catch (Bam2AfException e) {
                e.printStackTrace();
            }
        }
    }


    private void processRead(SAMRecord record) throws Bam2AfException {
        final String md = record.getStringAttribute(SAMTag.MD.name());
        if (md == null) {
            throw new Bam2AfException("Cannot create reference from SAMRecord with no MD tag, read: " + record.getReadName());
        }
        // Not sure how long output will be, but it will be no longer than this.
        // int maxOutputLength = 0;
        final Cigar cigar = record.getCigar();
        if (cigar == null) {
            throw new SAMException("Cannot create reference from SAMRecord with no CIGAR, read: " + record.getReadName());
        }
        int maxOutputLength = 0;
        for (final CigarElement cigarElement : cigar.getCigarElements()) {
            maxOutputLength += cigarElement.getLength();
        }
        System.out.println("len="+maxOutputLength+" MD="+md);
    }


}