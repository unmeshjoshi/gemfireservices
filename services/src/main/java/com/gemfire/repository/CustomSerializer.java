package com.gemfire.repository;

import org.apache.geode.cache.Declarable;
import org.apache.geode.pdx.PdxReader;
import org.apache.geode.pdx.PdxSerializer;
import org.apache.geode.pdx.PdxWriter;

import java.lang.reflect.Field;
import java.math.BigDecimal;

public class CustomSerializer implements PdxSerializer, Declarable {
    @Override
    public boolean toData(Object o, PdxWriter out) {
        try {
            serialize(o, out);
            return true;
        } catch (IllegalAccessException e) {
            return false;
        }
    }

    @Override
    public Object fromData(Class<?> clazz, PdxReader in) {
        return null;
    }

    private void serialize(Object transaction, PdxWriter pdxWriter) throws IllegalAccessException {
        Class<?> aClass = transaction.getClass();
        Field[] fields = aClass.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            field.setAccessible(true);
            if (field.get(transaction) == null) {
                pdxWriter.writeObject(field.getName(), field.get(transaction));
            } else if (field.getType().equals(String.class)) {
                pdxWriter.writeString(field.getName(), field.get(transaction).toString());
            } else if (field.getType().equals(BigDecimal.class)) {
                pdxWriter.writeString(field.getName(), field.get(transaction).toString());
            } else if (field.getType().equals(Long.class)) {
                pdxWriter.writeLong(field.getName(), Long.parseLong(field.get(transaction).toString()));
            } else {
                pdxWriter.writeObject(field.getName(), field.get(transaction));
            }
        }
    }
}
