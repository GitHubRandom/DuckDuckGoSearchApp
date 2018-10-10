package io.duckduckgosearch.app;

public class HistoryItem {

    private String term, date;

    public HistoryItem(String term, String date) {
        this.term = term;
        this.date = date;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
