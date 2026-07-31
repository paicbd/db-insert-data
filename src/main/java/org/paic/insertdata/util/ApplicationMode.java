package org.paic.insertdata.util;

import com.paicbd.smsc.utils.Generated;
import lombok.Getter;

@Getter
@Generated
public enum ApplicationMode {
    LOGS("logs"),
    DATABASE("database"),
    LOGS_DATABASE("logs_database");

    private final String value;

    ApplicationMode(final String value) {
        this.value = value;
    }
}
