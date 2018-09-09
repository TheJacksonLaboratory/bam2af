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
    /** A regular expression to break down blocks of the MD field */
    private final static String MDfieldRegEx = "([A-Z]|\\d+)";
    /** Pattern used for the MD field. */
    private final static Pattern MDfieldPattern =  Pattern.compile(MDfieldRegEx);


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
        ImmutableMap.Builder<Integer,Integer> builder = new ImmutableMap.Builder<>();
        // relate genomic position to index in columns array
        for (int i=0, j=from;j<=to;i++,j++) {
            builder.put(j,i);
        }
        this.idx2idxMap=builder.build();
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
        int start = record.getStart();
        int end=record.getEnd();
        System.out.println("al-start="+alignmentstart+", al-end="+alignmentend +
            ", start="+start +", end="+end);
        int numBlocks=record.getAlignmentBlocks().size();
        System.out.println("Num blocks="+numBlocks);
        int maxOutputLength = 0;
        for (final CigarElement cigarElement : cigar.getCigarElements()) {
            maxOutputLength += cigarElement.getLength();
            System.out.println(cigarElement.toString() +": "+ cigarElement.getLength());
        }
        System.out.println("len="+maxOutputLength+" MD="+md);
        Matcher m = MDfieldPattern.matcher(md);
        if (m.matches()) {
            int c= m.groupCount();
            for (int i=0;i<c;i++) {
                String group = m.group(i);
                System.out.println("MATHCER="+group);
            }

        }
    }


}