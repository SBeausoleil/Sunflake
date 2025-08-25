package edu.sbeausoleil.log121.testapp;

import com.sb.flake.annotations.FlakeSequence;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDateTime;

@Entity
public class Coffee {
    @Id
    @FlakeSequence
    //@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private LocalDateTime created;

    protected Coffee() {} // Hibernate
    public Coffee(String name) {
        this.name = name;
        this.created = LocalDateTime.now();
    }
}
