package com.sb.flake;

import java.util.HashMap;
import java.util.function.ToIntFunction;

public interface WorkerIdHasher<T> extends ToIntFunction<T> {
    /**
     * Checks if all the worker id seeds will provide unique masked hashes.
     * @param rules the rules that will be used by flake generators.
     * @param seeds the raw identifiers of the workers.
     * @throws NonUniqueException in case two or more seeds' hashes collide.
     */
    default void checkUniqueness(GenerationRules rules, Iterable<T> seeds) throws NonUniqueException {
        HashMap<Integer, T> generatedIds = new HashMap<>();
        for (T seed : seeds) {
            int hash = applyAsInt(seed) & rules.getWorkerIdMask();
            T alreadyIndexed = generatedIds.get(hash);
            if (alreadyIndexed != null) {
                throw new NonUniqueException(alreadyIndexed, seed, hash);
            }
            generatedIds.put(hash, seed);
        }
    }
}
