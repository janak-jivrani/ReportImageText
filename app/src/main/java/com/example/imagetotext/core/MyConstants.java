package com.example.imagetotext.core;

import java.util.Comparator;

public class MyConstants {
    public static final int AlignLeft = 0;
    public static final int AlignCenter = 1;
    public static final int AlignRight = 2;

    public static Comparator<TextOCR> topSorting = new Comparator<TextOCR>() {

        public int compare(TextOCR s1, TextOCR s2) {
            /*For ascending order*/
            int top1 = (s1.topMin + s1.topMax) / 2;
            int top2 = (s2.topMin + s2.topMax) / 2;
            return top1 - top2;
        }
    };
    /*public static Comparator<TextOCRTableColumns> tableLeftSorting = new Comparator<TextOCRTableColumns>() {

        public int compare(TextOCRTableColumns s1, TextOCRTableColumns s2) {
            *//*For ascending order*//*
            return s1.left - s2.left;
        }
    };*/

}
