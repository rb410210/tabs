package com.rohitbalan.tabs.model;

import java.util.Set;

public class Artist {
    private String name;
    private Set<Tab> tabs;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Tab> getTabs() {
        return tabs;
    }

    public void setTabs(Set<Tab> tabs) {
        this.tabs = tabs;
    }
}
