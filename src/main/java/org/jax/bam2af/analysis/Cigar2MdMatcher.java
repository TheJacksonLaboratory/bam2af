package org.jax.bam2af.analysis;

import htsjdk.samtools.*;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A convenience class that takes an MD string and a CIGAR object and
 * matches them. The class is intended to test the approach and may not be
 * efficient enough for final use -- TODO check me
 */
public class Cigar2MdMatcher {

    /** Byte typed variables for all normal bases. */
    public static final byte a = 'a', c = 'c', g = 'g', t = 't', n = 'n', A = 'A', C = 'C', G = 'G', T = 'T', N = 'N';
    public static final byte[] VALID_BASES_UPPER = new byte[]{A, C, G, T};
    public static final byte[] VALID_BASES_LOWER = new byte[]{a, c, g, t};
    private static final byte[] ACGTN_BASES = new byte[]{A, C, G, T, N};

    public static final byte ref='r',altA='a',altC='c',altG='G',altT='T',ins='-',del='d';

    /*
     * Regexp for MD string.
     *
     * \G = end of previous match.
     * (?:[0-9]+) non-capturing (why non-capturing?) group of digits.  For this number of bases read matches reference.
     *  - or -
     * Single reference base for case in which reference differs from read.
     *  - or -
     * ^one or more reference bases that are deleted in read.
     *
     */
    static final Pattern mdPat = Pattern.compile("\\G(?:([0-9]+)|([ACTGNactgn])|(\\^[ACTGNactgn]+))");



    public static BaseCall[] getPositions(Cigar cigar, String MD) {
        List<CigarElement> elementList = cigar.getCigarElements();
        int readlen = cigar.getReadLength();
        BaseCall [] positions=new BaseCall[readlen];
        byte[] calls=makeCallsFromAlignment(cigar,MD);
        for (int i=0;i<calls.length;i++) {
            switch (calls[i]) {
                case ref:
                    positions[i]=BaseCall.REFBASE;
                    break;
                case altA:
                    positions[i]=BaseCall.ALTBASE_A;
                    break;
                case altC:
                    positions[i]=BaseCall.ALTBASE_C;
                    break;
                case altG:
                    positions[i]=BaseCall.ALTBASE_G;
                    break;
                case altT:
                    positions[i]=BaseCall.ALTBASE_T;
                    break;
                case del:
                    positions[i]=BaseCall.ALTBASE_DEL;
                    break;
                case ins:
                    positions[i]=BaseCall.ALTBASE_INS;
                    break;
                 default:
                    positions[i]=BaseCall.ALTBASE_N;
                    break;
            }
        }
        return positions;
    }

    /**
     * Produce reference bases from an aligned SAMRecord with MD string and Cigar.
     *
     * am rec                               Must contain non-empty CIGAR and MD attribute.
     * includeReferenceBasesForDeletions If true, include reference bases that are deleted in the read.
     *                                          This will make the returned array not line up with the read if there are deletions.
     * @return References bases corresponding to the read.  If there is an insertion in the read, reference contains
     * '-'.  If the read is soft-clipped, reference contains '0'.  If there is a skipped region and
     * includeReferenceBasesForDeletions==true, reference will have Ns for the skipped region.
     */
    public static byte[] makeCallsFromAlignment(final Cigar cigar,final String md) {
        //final String md = rec.getStringAttribute(SAMTag.MD.name());
        final boolean includeReferenceBasesForDeletions=true;
        if (md == null) {
            throw new SAMException("Cannot create reference from SAMRecord with no MD tag, read: " );
        }
        // Not sure how long output will be, but it will be no longer than this.
        int maxOutputLength = 0;
        // = rec.getCigar();
        if (cigar == null) {
            throw new SAMException("Cannot create reference from SAMRecord with no CIGAR, read:");
        }
        for (final CigarElement cigarElement : cigar.getCigarElements()) {
            maxOutputLength += cigarElement.getLength();
        }
        final byte[] ret = new byte[maxOutputLength];
        int outIndex = 0;

        final Matcher match = mdPat.matcher(md);
        int curSeqPos = 0;

        int savedBases = 0;
        //final byte[] seq = rec.getReadBases();
        for (final CigarElement cigEl : cigar.getCigarElements()) {
            final int cigElLen = cigEl.getLength();
            final CigarOperator cigElOp = cigEl.getOperator();


            if (cigElOp == CigarOperator.SKIPPED_REGION) {
                // We've decided that MD tag will not contain bases for skipped regions, as they
                // could be megabases long, so just put N in there if caller wants reference bases,
                // otherwise ignore skipped regions.
                if (includeReferenceBasesForDeletions) {
                    for (int i = 0; i < cigElLen; ++i) {
                        ret[outIndex++] = N;
                    }
                }
            }
            // If it consumes reference bases, it's either a match or a deletion in the sequence
            // read.  Either way, we're going to need to parse through the MD.
            else if (cigElOp.consumesReferenceBases()) {
                // We have a match region, go through the MD
                int basesMatched = 0;

                // Do we have any saved matched bases?
                while ((savedBases > 0) && (basesMatched < cigElLen)) {
                    ret[outIndex++] = ref;//seq[curSeqPos++];
                    savedBases--;
                    basesMatched++;
                }

                while (basesMatched < cigElLen) {
                    boolean matched = match.find();
                    if (matched) {
                        String mg;
                        if (((mg = match.group(1)) != null) && (!mg.isEmpty())) {
                            // It's a number , meaning a series of matches
                            final int num = Integer.parseInt(mg);
                            for (int i = 0; i < num; i++) {
                                if (basesMatched < cigElLen) {
                                    ret[outIndex++] = ref; // exact alignemnt of sequence, no mismatched nucleotide
                                } else {
                                    savedBases++;
                                }
                                basesMatched++;
                            }
                        } else if (((mg = match.group(2)) != null) && (!mg.isEmpty())) {
                            // It's a single nucleotide, meaning a mismatch
                            if (basesMatched < cigElLen) {
                                ret[outIndex++] = charToMismatchedNtByte(mg.charAt(0));
                                curSeqPos++;
                            } else {
                                throw new IllegalStateException("Should never happen.");
                            }
                            basesMatched++;
                        } else if (((mg = match.group(3)) != null) && (!mg.isEmpty())) {
                            // It's a deletion, starting with a caret
                            // don't include caret
//                            if (includeReferenceBasesForDeletions) {
//                                final byte[] deletedBases = stringToBytes(mg);
//                                System.arraycopy(deletedBases, 1, ret, outIndex, deletedBases.length - 1);
//                                outIndex += deletedBases.length - 1;
//                            }
                            int delLen=mg.length()-1; // dont include caret
                            for (int i=0;i<delLen;i++) {
                                ret[outIndex++]=del;
                            }
                            basesMatched += mg.length() - 1;

                            // Check just to make sure.
                            if (basesMatched != cigElLen) {
                                throw new SAMException("Got a deletion in CIGAR (" + cigar + ", deletion " + cigElLen +
                                        " length) with an unequal ref insertion in MD (" + md + ", md " + basesMatched + " length");
                            }
                            if (cigElOp != CigarOperator.DELETION) {
                                throw new SAMException("Got an insertion in MD (" + md + ") without a corresponding deletion in cigar (" + cigar + ")");
                            }

                        } else {
                            matched = false;
                        }
                    }

                    if (!matched) {
                        throw new SAMException("Illegal MD pattern: " + md +
                                " with CIGAR " + cigar.toString());
                    }
                }

            } else if (cigElOp.consumesReadBases()) {
                // We have an insertion in read
                for (int i = 0; i < cigElLen; i++) {
                    final char c = (cigElOp == CigarOperator.SOFT_CLIP) ? '0' : '-';
                    ret[outIndex++] = ins;//charToByte(c);
                    curSeqPos++;
                }
            } else {
                // It's an op that consumes neither read nor reference bases.  Do we just ignore??
            }

        }
        if (outIndex < ret.length) {
            final byte[] shorter = new byte[outIndex];
            System.arraycopy(ret, 0, shorter, 0, outIndex);
            return shorter;
        }
        return ret;
    }

    /**
     * Produce reference bases from an aligned SAMRecord with MD string and Cigar.
     *
     * @param rec                               Must contain non-empty CIGAR and MD attribute.
     * @param includeReferenceBasesForDeletions If true, include reference bases that are deleted in the read.
     *                                          This will make the returned array not line up with the read if there are deletions.
     * @return References bases corresponding to the read.  If there is an insertion in the read, reference contains
     * '-'.  If the read is soft-clipped, reference contains '0'.  If there is a skipped region and
     * includeReferenceBasesForDeletions==true, reference will have Ns for the skipped region.
     */
    public static byte[] makeReferenceFromAlignment(final SAMRecord rec, final boolean includeReferenceBasesForDeletions) {
        final String md = rec.getStringAttribute(SAMTag.MD.name());
        if (md == null) {
            throw new SAMException("Cannot create reference from SAMRecord with no MD tag, read: " + rec.getReadName());
        }
        // Not sure how long output will be, but it will be no longer than this.
        int maxOutputLength = 0;
        final Cigar cigar = rec.getCigar();
        if (cigar == null) {
            throw new SAMException("Cannot create reference from SAMRecord with no CIGAR, read: " + rec.getReadName());
        }
        for (final CigarElement cigarElement : cigar.getCigarElements()) {
            maxOutputLength += cigarElement.getLength();
        }
        final byte[] ret = new byte[maxOutputLength];
        int outIndex = 0;

        final Matcher match = mdPat.matcher(md);
        int curSeqPos = 0;

        int savedBases = 0;
        final byte[] seq = rec.getReadBases();
        for (final CigarElement cigEl : cigar.getCigarElements()) {
            final int cigElLen = cigEl.getLength();
            final CigarOperator cigElOp = cigEl.getOperator();


            if (cigElOp == CigarOperator.SKIPPED_REGION) {
                // We've decided that MD tag will not contain bases for skipped regions, as they
                // could be megabases long, so just put N in there if caller wants reference bases,
                // otherwise ignore skipped regions.
                if (includeReferenceBasesForDeletions) {
                    for (int i = 0; i < cigElLen; ++i) {
                        ret[outIndex++] = N;
                    }
                }
            }
            // If it consumes reference bases, it's either a match or a deletion in the sequence
            // read.  Either way, we're going to need to parse through the MD.
            else if (cigElOp.consumesReferenceBases()) {
                // We have a match region, go through the MD
                int basesMatched = 0;

                // Do we have any saved matched bases?
                while ((savedBases > 0) && (basesMatched < cigElLen)) {
                    ret[outIndex++] = seq[curSeqPos++];
                    savedBases--;
                    basesMatched++;
                }

                while (basesMatched < cigElLen) {
                    boolean matched = match.find();
                    if (matched) {
                        String mg;
                        if (((mg = match.group(1)) != null) && (!mg.isEmpty())) {
                            // It's a number , meaning a series of matches
                            final int num = Integer.parseInt(mg);
                            for (int i = 0; i < num; i++) {
                                if (basesMatched < cigElLen) {
                                    ret[outIndex++] = seq[curSeqPos++];
                                } else {
                                    savedBases++;
                                }
                                basesMatched++;
                            }
                        } else if (((mg = match.group(2)) != null) && (!mg.isEmpty())) {
                            // It's a single nucleotide, meaning a mismatch
                            if (basesMatched < cigElLen) {
                                ret[outIndex++] = charToByte(mg.charAt(0));
                                curSeqPos++;
                            } else {
                                throw new IllegalStateException("Should never happen.");
                            }
                            basesMatched++;
                        } else if (((mg = match.group(3)) != null) && (!mg.isEmpty())) {
                            // It's a deletion, starting with a caret
                            // don't include caret
                            if (includeReferenceBasesForDeletions) {
                                final byte[] deletedBases = stringToBytes(mg);
                                System.arraycopy(deletedBases, 1, ret, outIndex, deletedBases.length - 1);
                                outIndex += deletedBases.length - 1;
                            }
                            basesMatched += mg.length() - 1;

                            // Check just to make sure.
                            if (basesMatched != cigElLen) {
                                throw new SAMException("Got a deletion in CIGAR (" + cigar + ", deletion " + cigElLen +
                                        " length) with an unequal ref insertion in MD (" + md + ", md " + basesMatched + " length");
                            }
                            if (cigElOp != CigarOperator.DELETION) {
                                throw new SAMException("Got an insertion in MD (" + md + ") without a corresponding deletion in cigar (" + cigar + ")");
                            }

                        } else {
                            matched = false;
                        }
                    }

                    if (!matched) {
                        throw new SAMException("Illegal MD pattern: " + md + " for read " + rec.getReadName() +
                                " with CIGAR " + rec.getCigarString());
                    }
                }

            } else if (cigElOp.consumesReadBases()) {
                // We have an insertion in read
                for (int i = 0; i < cigElLen; i++) {
                    final char c = (cigElOp == CigarOperator.SOFT_CLIP) ? '0' : '-';
                    ret[outIndex++] = charToByte(c);
                    curSeqPos++;
                }
            } else {
                // It's an op that consumes neither read nor reference bases.  Do we just ignore??
            }

        }
        if (outIndex < ret.length) {
            final byte[] shorter = new byte[outIndex];
            System.arraycopy(ret, 0, shorter, 0, outIndex);
            return shorter;
        }
        return ret;
    }

    private static byte charToByte(char nt) {
        switch (nt) {
            case 'a':
            case 'A':
                return a;
            case 'c':
            case 'C':
                return c;
            case 'g':
            case 'G':
                return g;
            case 't':
            case 'T':
                return t;
             default:
                 return n;
        }
    }

    private static byte charToMismatchedNtByte(char nt) {
        switch (nt) {
            case 'a':
            case 'A':
                return altA;
            case 'c':
            case 'C':
                return altC;
            case 'g':
            case 'G':
                return altG;
            case 't':
            case 'T':
                return altT;
            default:
                return n;
        }
    }

    private static byte[] stringToBytes(String s) {
        byte[] b = new byte[s.length()];
        for (int i=0;i<s.length();i++) {
            b[i]=charToByte(s.charAt(i));
        }
        return b;
    }




    private static boolean isMatch(String MdPart) {
        int len=MdPart.length();
        for (int i=0;i<len;i++) {
            if (! Character.isDigit(MdPart.charAt(i)) ){
                return false;
            }
        }
        return true;
    }


    public Cigar2MdMatcher(Cigar cigar, String MD) {

    }

}
