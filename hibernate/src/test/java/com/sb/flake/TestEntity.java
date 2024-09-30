package com.sb.flake;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import org.hibernate.annotations.GenericGenerator;

@Entity
public class TestEntity {
    @Id
    @GeneratedValue(generator = "snowflake")
    private Long id;
    private String name;

    public TestEntity() {}

    public TestEntity(String name) {
        this.name = name;
    }
}
