package com.elsys.safebanking.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.elsys.safebanking.validation.EPinPolicy;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import java.lang.reflect.Field;
import org.junit.jupiter.api.Test;

class UserMappingTest {

    @Test
    void userTableAndEPinColumnMatchSchemaContract() throws Exception {
        Table table = User.class.getAnnotation(Table.class);
        assertEquals("users", table.name());

        Field ePinHash = User.class.getDeclaredField("ePinHash");
        Column column = ePinHash.getAnnotation(Column.class);

        assertEquals("e_pin", column.name());
        assertTrue(column.length() >= EPinPolicy.HASHED_COLUMN_LENGTH);
        assertTrue(column.nullable());
    }
}
