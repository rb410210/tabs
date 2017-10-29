package com.rohitbalan.tabs.model;

public class Tab {
    private String name;
    private String uri;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public Tab(String name, String uri) {
        this.name = name;
        this.uri = uri;
    }

    @Override
    public String toString() {
        return "Tab{" +
                "name='" + name + '\'' +
                ", uri='" + uri + '\'' +
                '}';
    }
}
