package com.joel.personalexpensetracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class SelectAnalyticsActivity extends AppCompatActivity {

    ImageView todayAnalyticsCardView,weekAnalyticsCardView,monthAnalyticsCardView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_analytics);

        todayAnalyticsCardView = findViewById(R.id.todayAnalyticsCardView);
        monthAnalyticsCardView = findViewById(R.id.monthAnalyticsCardView);
        weekAnalyticsCardView = findViewById(R.id.weekAnalyticsCardView);



        todayAnalyticsCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SelectAnalyticsActivity.this, DailyAnalyticsActivity.class);
                startActivity(intent);
            }
        });


        monthAnalyticsCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SelectAnalyticsActivity.this, MonthlyAnalyticsActivity.class);
                startActivity(intent);
            }
        });

        weekAnalyticsCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SelectAnalyticsActivity.this, WeeklyAnalyticsActivity.class);
                startActivity(intent);
            }
        });


    }
}