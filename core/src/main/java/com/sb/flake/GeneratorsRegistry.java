package com.sb.flake;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Registry for different Sunflake generators.
 * <p>
 *     Registering a generator is required if one desires to keep its worker ID in sync with the Sunflake context.
 * </p>
 */
public class GeneratorsRegistry {
    private static class InstanceHolder {
        private static final GeneratorsRegistry INSTANCE = new GeneratorsRegistry();
    }

    private final Map<String, FlakeGenerator> generators = new HashMap<>();


    private GeneratorsRegistry() {
    }

    public static GeneratorsRegistry getInstance() {
        return InstanceHolder.INSTANCE;
    }

    /**
     * Register a new generator with the given name.
     * <p>
     *     If the name is already registered, the existing generator will be replaced.
     * </p>
     * @param name the name of the generator
     * @param generator the generator instance
     */
    public void registerGenerator(String name, FlakeGenerator generator) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Generator name cannot be null or empty");
        }
        if (generator == null) {
            throw new IllegalArgumentException("Generator cannot be null");
        }
        synchronized (generators) {
            generators.put(name, generator);
        }
    }

    /**
     * Get the map of registered generators.
     * @return the map of registered generators.
     */
    public Map<String, FlakeGenerator> getGeneratorsMap() {
        return this.generators;
    }

    /**
     * Get a registered generator by name.
     * @param name the name of the generator
     * @return an Optional containing the generator if found, or empty if not found
     */
    public Optional<FlakeGenerator> getGenerator(String name) {
        return Optional.ofNullable(generators.get(name));
    }

    /**
     * Set the worker ID for all registered generators.
     * @param workerId the new worker ID
     */
    public synchronized void setWorkerId(long workerId) {
        for (FlakeGenerator generator : generators.values()) {
            generator.setWorkerId(workerId);
        }
    }
}
