package com.sb.flake.entities;

import com.sb.flake.annotations.FlakeSequence;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class ParentEntityA {
    @Id
    @FlakeSequence
    private Long id;
    private int fieldA;
}
