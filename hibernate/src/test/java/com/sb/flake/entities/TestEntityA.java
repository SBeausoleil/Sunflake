package com.sb.flake.entities;

import com.sb.flake.annotations.FlakeSequence;
import jakarta.persistence.*;

@Entity
public class TestEntityA {
    @Id
    @FlakeSequence
    private Long id;
    private String name;

    public TestEntityA() {}

    public TestEntityA(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
