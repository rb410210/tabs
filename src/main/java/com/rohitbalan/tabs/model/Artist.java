package com.rohitbalan.tabs.model;

import java.util.List;

public class Artist {
    private String name;
    private List<Tab> tabs;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Tab> getTabs() {
        return tabs;
    }

    public void setTabs(List<Tab> tabs) {
        this.tabs = tabs;
    }
}
