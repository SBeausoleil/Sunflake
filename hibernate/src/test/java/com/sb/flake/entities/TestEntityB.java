package com.sb.flake.entities;

import com.sb.flake.annotations.FlakeSequence;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table
public class TestEntityB {
    @Id
    @FlakeSequence
    private Long id;
    private int value;

    protected TestEntityB() {}

    public TestEntityB(int value) {
        this.value = value;
    }

    public Long getId() {
        return id;
    }

    public int getValue() {
        return value;
    }
}
