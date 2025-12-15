package com.mailSystem.demo.service.sort;


public class SortFactory {


    public static ISortStrategy getStrategy(String sortType) {
        System.out.println("🏭 FACTORY: Received Type: " + sortType); // شوف هيطبع إيه
        if (sortType == null) {
            return new SortByDate(false);
        }


        return switch (sortType.toUpperCase()) {
            case "DATE_NEWEST" -> new SortByDate(false);
            case "DATE_OLDEST" -> new SortByDate(true);
            case "PRIORITY_HIGH", "PRIORITY" -> new SortByPriority(false);
            case "PRIORITY_LOW" -> new SortByPriority(true);
            case "SENDER_ASC", "SENDER" -> new SortBySender(true);
            case "SENDER_DESC" -> new SortBySender(false);
            case "RECEIVERS_ASC", "RECEIVERS" -> new SortByReceivers(true);
            case "RECEIVERS_DESC" -> new SortByReceivers(false);
            case "SUBJECT_ASC", "SUBJECT" -> new SortBySubject(true);
            case "SUBJECT_DESC" -> new SortBySubject(false);
            default -> new SortByDate(false);
        };
    }
}
