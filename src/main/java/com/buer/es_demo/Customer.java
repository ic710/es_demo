package com.buer.es_demo;

import lombok.Data;

import java.util.List;

@Data
public class Customer {

    private Long id;

    private String name;

    List<String> tags;
}
