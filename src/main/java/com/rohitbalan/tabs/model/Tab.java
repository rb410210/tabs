package com.rohitbalan.tabs.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode
@ToString
@AllArgsConstructor
public class Tab {
    private String name;
    private String uri;
}
