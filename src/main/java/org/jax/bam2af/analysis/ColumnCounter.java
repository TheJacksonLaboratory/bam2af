package org.jax.bam2af.analysis;

import com.sun.org.apache.regexp.internal.RE;

public class ColumnCounter {

    private final static int REF_INDEX=0;
    private final static int ALTBASE_A_INDEX=1;
    private final static int ALTBASE_C_INDEX=2;
    private final static int ALTBASE_G_INDEX=3;
    private final static int ALTBASE_T_INDEX=4;
    private final static int ALTBASE_N_INDEX=5;


    private int [] counts = new int[6];

    public ColumnCounter() {
    }

    public void ref() {
        counts[REF_INDEX]++;
    }

    public void altbase_A() {
        counts[ALTBASE_A_INDEX]++;
    }

    public void altbase_C() {
        counts[ALTBASE_C_INDEX]++;
    }

    public void altbase_G(){
        counts[ALTBASE_G_INDEX]++;
    }

    public void altbase_T() {
        counts[ALTBASE_T_INDEX]++;
    }

    public void altbase_N() {
        counts[ALTBASE_N_INDEX]++;
    }


    public void debugPrint() {
        System.out.print("Ref: "+counts[REF_INDEX]);
        if (counts[ALTBASE_A_INDEX]>0) System.out.print(", ALT_A="+counts[ALTBASE_A_INDEX]);
        if (counts[ALTBASE_C_INDEX]>0) System.out.print(", ALT_C="+counts[ALTBASE_C_INDEX]);
        if (counts[ALTBASE_G_INDEX]>0) System.out.print(", ALT_G="+counts[ALTBASE_G_INDEX]);
        if (counts[ALTBASE_T_INDEX]>0) System.out.print(", ALT_T="+counts[ALTBASE_T_INDEX]);
        System.out.println();
    }

}
