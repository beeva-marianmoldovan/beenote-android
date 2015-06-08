package com.beeva.beenote.models;

import java.util.List;

/**
 * Created by marianclaudiu on 5/06/15.
 */
public class MetaNoteResponse {
    private String count;
    private List<Note> items;

    public MetaNoteResponse(String count, List<Note> items) {
        this.count = count;
        this.items = items;
    }

    public MetaNoteResponse() {

    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }

    public List<Note> getObjects() {
        return items;
    }

    public void setItems(List<Note> items) {
        this.items = items;
    }
}
