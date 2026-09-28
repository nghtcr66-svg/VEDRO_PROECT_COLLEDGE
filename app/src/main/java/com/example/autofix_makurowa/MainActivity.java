package com.example.autofix_makurowa;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private TextView tvClock;
    private TableLayout tableRepair, tablePaint;
    private final Handler clockHandler = new Handler(Looper.getMainLooper());
    private final SimpleDateFormat clockFmt = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault());

    private RequestRepo repo;
    private String orderRepair = "created_date DESC, created_time DESC";
    private String orderPaint = "created_date DESC";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        repo = new RequestRepo(this);
        repo.seedIfEmpty();

        tvClock = findViewById(R.id.tvClock);
        tableRepair = findViewById(R.id.tableRepair);
        tablePaint = findViewById(R.id.tablePaint);

        findViewById(R.id.btnCreateRepair).setOnClickListener(v ->
                startActivity(new Intent(this, create_repair.class)));
        findViewById(R.id.btnCreatePaint).setOnClickListener(v ->
                startActivity(new Intent(this, create_paint.class)));
        findViewById(R.id.btnStats).setOnClickListener(v ->
                startActivity(new Intent(this, stats.class)));

        ((ImageButton) findViewById(R.id.btnSortAZRepair)).setOnClickListener(v -> {
            orderRepair = "model COLLATE NOCASE ASC";
            renderRepair();
        });
        ((ImageButton) findViewById(R.id.btnSortDateRepair)).setOnClickListener(v -> {
            orderRepair = "created_date DESC, created_time DESC";
            renderRepair();
        });
        ((ImageButton) findViewById(R.id.btnSortAZPaint)).setOnClickListener(v -> {
            orderPaint = "model COLLATE NOCASE ASC";
            renderPaint();
        });
        ((ImageButton) findViewById(R.id.btnSortDatePaint)).setOnClickListener(v -> {
            orderPaint = "created_date DESC";
            renderPaint();
        });

        startClock();
    }

    @Override
    protected void onResume() {
        super.onResume();
        renderRepair();
        renderPaint();
    }

    private void startClock() {
        clockHandler.post(new Runnable() {
            @Override
            public void run() {
                if (tvClock != null) tvClock.setText(clockFmt.format(new Date()));
                clockHandler.postDelayed(this, 1000);
            }
        });
    }

    private void renderRepair() {
        if (tableRepair == null) return;
        tableRepair.removeAllViews();
        List<ContentValues> list = repo.listByType("REPAIR", orderRepair);
        LayoutInflater inf = LayoutInflater.from(this);
        int warn = ContextCompat.getColor(this, R.color.warn);

        for (ContentValues v : list) {
            TableRow row = (TableRow) inf.inflate(R.layout.row_repair, tableRepair, false);
            ((TextView) row.findViewById(R.id.tvModel)).setText(s(v.getAsString("model")));
            ((TextView) row.findViewById(R.id.tvDate)).setText(s(v.getAsString("created_date")));
            ((TextView) row.findViewById(R.id.tvTime)).setText(s(v.getAsString("created_time")));

            TextView st = row.findViewById(R.id.tvStatus);
            boolean done = "DONE".equals(v.getAsString("status"));
            st.setText(done ? "выполнено" : "в работе");
            st.setTextColor(done ? Color.parseColor("#4CAF50") : warn);

            final String id = v.getAsString("id");
            row.findViewById(R.id.btnEdit).setOnClickListener(x -> {
                Intent i = new Intent(this, create_repair.class);
                i.putExtra(create_repair.EXTRA_ID, id);
                startActivity(i);
            });
            row.findViewById(R.id.btnDone).setOnClickListener(x -> {
                repo.markDone(id, today(), now());
                renderRepair();
            });
            row.findViewById(R.id.btnDelete).setOnClickListener(x -> {
                repo.delete(id);
                renderRepair();
            });
            tableRepair.addView(row);
        }
    }

    private void renderPaint() {
        if (tablePaint == null) return;
        tablePaint.removeAllViews();
        List<ContentValues> list = repo.listByType("PAINT", orderPaint);
        LayoutInflater inf = LayoutInflater.from(this);
        int warn = ContextCompat.getColor(this, R.color.warn);

        for (ContentValues v : list) {
            TableRow row = (TableRow) inf.inflate(R.layout.row_paint, tablePaint, false);
            ((TextView) row.findViewById(R.id.tvModel)).setText(s(v.getAsString("model")));
            ((TextView) row.findViewById(R.id.tvColor)).setText(s(v.getAsString("color")));
            ((TextView) row.findViewById(R.id.tvDate)).setText(s(v.getAsString("created_date")));

            TextView st = row.findViewById(R.id.tvStatus);
            if (st != null) {
                boolean done = "DONE".equals(v.getAsString("status"));
                st.setText(done ? "выполнено" : "в работе");
                st.setTextColor(done ? Color.parseColor("#4CAF50") : warn);
            }

            final String id = v.getAsString("id");
            row.findViewById(R.id.btnEdit).setOnClickListener(x -> {
                Intent i = new Intent(this, create_paint.class);
                i.putExtra(create_paint.EXTRA_ID, id);
                startActivity(i);
            });
            row.findViewById(R.id.btnDone).setOnClickListener(x -> {
                repo.markDone(id, today(), now());
                renderPaint();
            });
            row.findViewById(R.id.btnDelete).setOnClickListener(x -> {
                repo.delete(id);
                renderPaint();
            });
            tablePaint.addView(row);
        }
    }

    private String today() { return new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(new Date()); }
    private String now() { return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date()); }
    private String s(String x) { return x == null ? "" : x; }
}
