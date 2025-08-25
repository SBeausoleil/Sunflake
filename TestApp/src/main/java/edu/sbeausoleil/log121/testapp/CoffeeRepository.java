package edu.sbeausoleil.log121.testapp;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CoffeeRepository extends JpaRepository<Coffee, Long> {
}
