package com.sb.flake;

import com.sb.flake.annotations.FlakePreset;
import com.sb.flake.annotations.FlakeSequence;
import jakarta.persistence.*;

@Entity
@Table(name = "test_entity")
public class TestEntity {
    @Id
    //@GeneratedValue(strategy = GenerationType.IDENTITY)
    @FlakeSequence(preset = FlakePreset.SNOWFLAKE)
    private Long id;
    private String name;

    TestEntity() {}

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
