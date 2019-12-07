package com.officialakbarali.fabiz.network.syncInfo.data;

public class SyncLogData {

    private int id;
    private String rawId;
    private String tableName;
    private String operation;
    private String opCode;
    private String timestamp;

    public SyncLogData(int id, String rawId, String tableName, String operation, String opCode, String timestamp) {
        this.id = id;
        this.rawId = rawId;
        this.tableName = tableName;
        this.operation = operation;
        this.opCode = opCode;
        this.timestamp = timestamp;
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

    public int getId() {
        return id;
    }

    public String getOpCode() {
        return opCode;
    }

    public String getTimestamp() {
        return timestamp;
    }
}

