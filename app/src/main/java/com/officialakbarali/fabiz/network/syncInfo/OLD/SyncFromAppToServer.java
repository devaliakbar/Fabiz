package com.officialakbarali.fabiz.network.syncInfo.OLD;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.database.Cursor;
import android.os.Bundle;

import com.officialakbarali.fabiz.R;
import com.officialakbarali.fabiz.data.db.FabizContract;
import com.officialakbarali.fabiz.data.db.FabizProvider;
import com.officialakbarali.fabiz.network.syncInfo.adapter.SyncFromAppAdapter;
import com.officialakbarali.fabiz.network.syncInfo.data.SyncLogDetail;

import java.util.ArrayList;
import java.util.List;

public class SyncFromAppToServer extends AppCompatActivity {
    private SyncFromAppAdapter syncFromAppAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync_from_app_to_server);

        syncFromAppAdapter = new SyncFromAppAdapter(this);
        RecyclerView syncFromAppRecycler = findViewById(R.id.sync_from_recycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        syncFromAppRecycler.setLayoutManager(layoutManager);
        syncFromAppRecycler.setHasFixedSize(true);
        syncFromAppRecycler.setAdapter(syncFromAppAdapter);

        fetchSyncLogData();
    }

    private void fetchSyncLogData() {
        FabizProvider provider = new FabizProvider(this, false);
        Cursor
                syncCursor = provider.query(FabizContract.SyncLog.TABLE_NAME,
                new String[]{FabizContract.SyncLog.COLUMN_TABLE_NAME, FabizContract.SyncLog.COLUMN_ROW_ID, FabizContract.SyncLog.COLUMN_OPERATION},
                null, null, null);

        List<SyncLogDetail> syncLogsList = new ArrayList<>();
        while (syncCursor.moveToNext()) {
            syncLogsList.add(new SyncLogDetail(syncCursor.getString(syncCursor.getColumnIndexOrThrow(FabizContract.SyncLog.COLUMN_ROW_ID)),
                    syncCursor.getString(syncCursor.getColumnIndexOrThrow(FabizContract.SyncLog.COLUMN_TABLE_NAME)),
                    syncCursor.getString(syncCursor.getColumnIndexOrThrow(FabizContract.SyncLog.COLUMN_OPERATION))));
        }
        syncFromAppAdapter.swapAdapter(syncLogsList);
    }
}
