package org.jax.bam2af.analysis;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import htsjdk.samtools.*;
import org.jax.bam2af.exception.Bam2AfException;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The purpose of this class is to count the alleles present in the columns of an alignment
 * that is located at a specfic genomic interval. We go over each of the SAMRecords located
 * within the interval and count up REF and ALT. Note that we have an enumeration of possible ALT
 * Bases (ACGTN)
 */
public class IntervalAlleleCounter {



    private final String chromosome;

    private final int fromPosition;

    private final int toPosition;

    private final ColumnCounter[] columns;
    /** Key -- genomic position; value: corresponding index in {@link #columns}. */
    private final Map<Integer,Integer> idx2idxMap;


    public IntervalAlleleCounter(SAMRecordIterator iter, String chrom, int from, int to) {
        chromosome = chrom;
        fromPosition = from;
        toPosition = to;
        int len = to - from + 1;
        columns = new ColumnCounter[len];
        for (int k=0;k<len;k++) {
            columns[k]=new ColumnCounter();
        }

        ImmutableMap.Builder<Integer,Integer> builder = new ImmutableMap.Builder<>();
        // relate genomic position to index in columns array
        for (int i=0, j=from;j<=to;i++,j++) {
            builder.put(j,i);
        }
        this.idx2idxMap=builder.build();
        while (iter.hasNext()) {
            try {
                processRead(iter.next());
            } catch (Bam2AfException e) {
                e.printStackTrace();
            }
        }
        debugPrint();
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

        int alignmentstart = record.getAlignmentStart();
        int alignmentend = record.getAlignmentEnd();
        int alignmentLength=alignmentend-alignmentstart+1;
        int start = record.getStart();
        int end=record.getEnd();
        System.out.println("al-start="+alignmentstart+", al-end="+alignmentend +
            ", start="+start +", end="+end);
        int numBlocks=record.getAlignmentBlocks().size();
        System.out.println("Num blocks="+numBlocks);

        BaseCall calls[] = Cigar2MdMatcher.getPositions(cigar,md);
        if (calls.length != alignmentLength) {
            System.err.println("Could not process read, lengths were different ? insertion ???");
            return;
        }
        int j=0;
        for (int i=alignmentstart;i<alignmentend;i++) {
            if (! idx2idxMap.containsKey(i)) {
                continue; // outside of range
            }
            int idx = idx2idxMap.get(i);
            switch (calls[j]) {
                case REFBASE:
                    columns[idx].ref();
                    break;
                case ALTBASE_A:
                    columns[idx].altbase_A();
                    break;
                case ALTBASE_C:
                    columns[idx].altbase_C();
                    break;
                case ALTBASE_G:
                    columns[idx].altbase_G();
                    break;
                case ALTBASE_T:
                    columns[idx].altbase_T();
                    break;
                    // todo deletion, insertion

            }
        }

    }

    public void debugPrint() {
        for (int i=0;i<columns.length;i++) {
            System.out.println("##column "+i);
            columns[i].debugPrint();
        }
    }


}