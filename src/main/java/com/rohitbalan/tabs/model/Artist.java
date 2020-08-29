package com.rohitbalan.tabs.model;

import lombok.Data;

import java.util.Set;

@Data
public class Artist {
    private String name;
    private Set<Tab> tabs;
}
