package com.sb.flake.util;

import com.sb.flake.WorkerIdSupplier;
import systems.helius.commons.SmartProperties;

import java.lang.reflect.InvocationTargetException;

public final class WorkerIdSupplierUtil {
    private WorkerIdSupplierUtil() {}

    public static WorkerIdSupplier getWorkerIdSupplier(String className, SmartProperties sunflakeProperties)
            throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class<?> clazz = Class.forName(className);
        try {
            return (WorkerIdSupplier) clazz.getMethod("getInstance", SmartProperties.class)
                    .invoke(null, sunflakeProperties);
        } catch (NoSuchMethodException e) {
            throw new NoSuchMethodException("Defined class " + className + " does not respect the contract of " +
                    "defining a \"public static WorkerIdSupplier getInstance(SmartProperties)\".");
        }
    }
}
