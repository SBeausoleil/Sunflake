package com.sb.flake;

import com.sb.flake.entities.TestEntityA;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class FlakeEntityEntityManagementTest extends AbstractHibernateTest {
    @Test
    void testSave() {
        var foo = new TestEntityA("Hello world");
        session.persist(foo);
        assertNotNull(foo.getId());
    }

    @Test
    void testSaveMultiple_AllHaveDifferentIds() {
        var foo = new TestEntityA("Hello");
        var bar = new TestEntityA("world");
        session.persist(foo);
        session.persist(bar);
        assertNotNull(foo.getId());
        assertNotNull(bar.getId());
        assertNotEquals(foo.getId(), bar.getId());
    }
}
