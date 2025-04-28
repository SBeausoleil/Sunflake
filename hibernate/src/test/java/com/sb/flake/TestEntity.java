package com.sb.flake;

import com.sb.flake.annotations.FlakeSequence;
import jakarta.persistence.*;

@Entity
@Table(name = "test_entity_foobar")
public class TestEntity {
    @Id
    @FlakeSequence
    private Long id;
    private String name;

    public TestEntity() {}

    public TestEntity(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
