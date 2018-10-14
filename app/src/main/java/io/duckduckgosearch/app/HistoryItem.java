package io.duckduckgosearch.app;

import java.util.Date;

public class HistoryItem {

    private String term;
    private Date date;

    public HistoryItem(String term, Date date) {
        this.term = term;
        this.date = date;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
