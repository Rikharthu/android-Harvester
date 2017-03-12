package com.example.uberv.harvester;

public abstract class AccSearchHelper {

    public static enum PageType {
        Authorization, Search
    }

    public static boolean isNullOrEmpty(String str){
        return str==null || str.isEmpty() || str.equals("-");
    }

    public static String getSearchUrl(String company){
        return "https://search.accenture.com/?aid=ctl&k=clientname|st|"+company.trim()+"||a||cleangr1|st|&page=1";
    }

}
