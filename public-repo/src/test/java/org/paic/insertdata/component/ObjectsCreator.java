package org.paic.insertdata.component;

import com.paicbd.smsc.dto.UtilsRecords;

public class ObjectsCreator {
    private ObjectsCreator() {
    }

    public static UtilsRecords.Cdr getDefaultCdr() {
        return new UtilsRecords.Cdr(
                "1734454595605",
                "1734454593830",
                "1734454595603",
                "MESSAGE",
                "1734454582187-9026385306105",
                "HTTP",
                "3",
                "SP",
                "SS7",
                "2",
                "GW",
                "1",
                "",
                "SENT",
                "",
                "1773",
                "1775",
                "0",
                "60",
                "1322888089",
                "1",
                "1",
                "0987654321",
                "1",
                "1",
                "",
                "73",
                "0",
                "8",
                "22220",
                "200",
                "8",
                "55566768",
                "730169999999212",
                "55566768",
                "",
                "22220",
                "First 20 chars",
                "0",
                "",
                "0",
                "",
                "",
                "",
                "",
                "1734454582187-9026385306105"
        );
    }
}
