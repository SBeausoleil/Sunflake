package com.sb.flake;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class FlakeEntityEntityManagementTest extends AbstractHibernateTest {
    @Test
    void testSave() {
        var foo = new TestEntity("Hello world");
        session.persist(foo);
        assertNotNull(foo.getId());
    }
}
