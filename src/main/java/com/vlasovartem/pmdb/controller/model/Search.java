package com.vlasovartem.pmdb.controller.model;

/**
 * Created by artemvlasov on 02/12/15.
 */
public class Search {
    private String title;
    private boolean found;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isFound() {
        return found;
    }

    public void setFound(boolean found) {
        this.found = found;
    }
}
