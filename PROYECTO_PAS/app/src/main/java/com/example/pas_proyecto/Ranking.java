package com.example.pas_proyecto;
import android.os.Bundle;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Ranking extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranking);

        Firebase.getAllUsersAndLostObjects(userData -> {

            TableLayout tableLayout = findViewById(R.id.rankingTable);


            List<Map.Entry<String, Long>> entries = new ArrayList<>(userData.entrySet());
            entries.sort((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()));
            String[][] rankings = new String[entries.size()][3];
            for (int i = 0; i < entries.size(); i++) {
                rankings[i][0] = String.valueOf(i + 1);
                rankings[i][1] = entries.get(i).getKey();
                rankings[i][2] = String.valueOf(entries.get(i).getValue());
            }

            for (String[] rowData : rankings) {
                TableRow row = new TableRow(this);
                for (String cellData : rowData) {
                    TextView textView = new TextView(this);
                    textView.setText(cellData);
                    textView.setPadding(16, 8, 16, 8);
                    row.addView(textView);
                }
                tableLayout.addView(row);
            }
        });
    }
}


