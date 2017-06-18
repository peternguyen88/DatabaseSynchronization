package com.zeth;

public class Test {
    public static void main(String[] args) {
        String options = "(a) F";

        System.out.println(trimOption(options));
    }

    private static String trimOption(String option){
        return option.replaceFirst("\\(.\\) ","");
    }
}
