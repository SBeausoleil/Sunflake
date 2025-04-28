package com.sb.flake;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class FlakeEntityEntityManagementTest extends AbstractHibernateTest {
    @Test
    void testSave() {
        var foo = new TestEntity("Hello world");
        session.persist(foo);
        assertNotNull(foo.getId());
    }

    @Test
    void testSaveMultiple_AllHaveDifferentIds() {
        var foo = new TestEntity("Hello");
        var bar = new TestEntity("world");
        session.persist(foo);
        session.persist(bar);
        assertNotNull(foo.getId());
        assertNotNull(bar.getId());
        assertNotEquals(foo.getId(), bar.getId());
    }
}
