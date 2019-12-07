package com.officialakbarali.fabiz.network.syncInfo.data;

public class SyncLogDetail {
    private String rawId;
    private String tableName;
    private String operation;

    public SyncLogDetail(String rawId, String tableName, String operation) {
        this.rawId = rawId;
        this.tableName = tableName;
        this.operation = operation;
    }


    public String getRawId() {
        return rawId;
    }

    public String getOperation() {
        return operation;
    }

    public String getTableName() {
        return tableName;
    }
}
