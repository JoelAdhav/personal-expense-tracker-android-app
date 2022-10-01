package com.joel.personalexpensetracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Pie;
import com.anychart.enums.Align;
import com.anychart.enums.LegendLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.joda.time.DateTime;
import org.joda.time.MutableDateTime;
import org.joda.time.Weeks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class WeeklyAnalyticsActivity extends AppCompatActivity {

    private Toolbar settingsToolbar;

    private FirebaseAuth mAuth;
    private DatabaseReference expenseRef, personalRef;

    private TextView totalAmountWeek, educationAnalyticsAmount, houseAnalyticsAmount, foodAnalyticsAmount, healthAnalyticsAmount, apparelAnalyticsAmount;
    private TextView transportAnalyticsAmount, personalAnalyticsAmount, otherAnalyticsAmount, monthRatioSpending, monthSpendAmount;

    private RelativeLayout relativeLayoutEducation, relativeLayoutHouse, relativeLayoutFood, relativeLayoutHealth, relativeLayoutApparel, relativeLayoutTransport;
    private RelativeLayout relativeLayoutPersonal, relativeLayoutOther, linearLayoutAnalysis;

    private AnyChartView anyChartView;

    private TextView progressRatioEducation, progressRatioHouse, progressRatioFood, progressRatioHealth, progressRatioApparel, progressRatioTransport, progressRatioPersonal, progressRatioOther;
    private ImageView monthRatioSpendingImage, educationStatus, houseStatus, foodStatus, healthStatus, apparelStatus, transportStatus, personalStatus, otherStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weekly_analytics);


        settingsToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(settingsToolbar);
        getSupportActionBar().setTitle("Weekly Analytics");

        anyChartView = findViewById(R.id.anyChartView);

        mAuth = FirebaseAuth.getInstance();
        expenseRef = FirebaseDatabase.getInstance().getReference().child("expenses").child(mAuth.getCurrentUser().getUid());
        personalRef = FirebaseDatabase.getInstance().getReference().child("personal").child(mAuth.getCurrentUser().getUid());


        // Amount TextView

        totalAmountWeek = findViewById(R.id.totalAmountWeek);
        educationAnalyticsAmount = findViewById(R.id.educationAnalyticsAmount);
        houseAnalyticsAmount = findViewById(R.id.houseAnalyticsAmount);
        foodAnalyticsAmount = findViewById(R.id.foodAnalyticsAmount);
        healthAnalyticsAmount = findViewById(R.id.healthAnalyticsAmount);
        apparelAnalyticsAmount = findViewById(R.id.apparelAnalyticsAmount);
        transportAnalyticsAmount = findViewById(R.id.transportAnalyticsAmount);
        personalAnalyticsAmount = findViewById(R.id.personalAnalyticsAmount);
        otherAnalyticsAmount = findViewById(R.id.otherAnalyticsAmount);
        monthRatioSpending = findViewById(R.id.monthRatioSpending);
        monthSpendAmount = findViewById(R.id.monthSpendAmount);

        // Relative Layout

        relativeLayoutEducation = findViewById(R.id.relativeLayoutEducation);
        relativeLayoutHouse = findViewById(R.id.relativeLayoutHouse);
        relativeLayoutFood = findViewById(R.id.relativeLayoutFood);
        relativeLayoutHealth = findViewById(R.id.relativeLayoutHealth);
        relativeLayoutApparel = findViewById(R.id.relativeLayoutApparel);
        relativeLayoutTransport = findViewById(R.id.relativeLayoutTransport);
        relativeLayoutPersonal = findViewById(R.id.relativeLayoutPersonal);
        relativeLayoutOther = findViewById(R.id.relativeLayoutOther);
        linearLayoutAnalysis = findViewById(R.id.linearLayoutAnalysis);

        // Status ImageView

        educationStatus = findViewById(R.id.educationStatus);
        houseStatus = findViewById(R.id.houseStatus);
        foodStatus = findViewById(R.id.foodStatus);
        healthStatus = findViewById(R.id.healthStatus);
        apparelStatus = findViewById(R.id.apparelStatus);
        transportStatus = findViewById(R.id.transportStatus);
        personalStatus = findViewById(R.id.personalStatus);
        otherStatus = findViewById(R.id.otherStatus);
        monthRatioSpendingImage = findViewById(R.id.monthRatioSpendingImage);

        // Progress Ratio TextView

        progressRatioEducation = findViewById(R.id.progressRatioEducation);
        progressRatioHouse = findViewById(R.id.progressRatioHouse);
        progressRatioFood = findViewById(R.id.progressRatioFood);
        progressRatioHealth = findViewById(R.id.progressRatioHealth);
        progressRatioApparel = findViewById(R.id.progressRatioApparel);
        progressRatioTransport = findViewById(R.id.progressRatioTransport);
        progressRatioPersonal = findViewById(R.id.progressRatioPersonal);
        progressRatioOther = findViewById(R.id.progressRatioOther);

        anyChartView = findViewById(R.id.anyChartView);

        getTotalWeeklyEducationExpense();
        getTotalWeeklyHouseExpense();
        getTotalWeeklyFoodExpense();
        getTotalWeeklyHealthExpense();
        getTotalWeeklyApparelExpense();
        getTotalWeeklyTransportExpense();
        getTotalWeeklyPersonalExpense();
        getTotalWeeklyOtherExpense();
        getTotalWeekExpense();

        Timer t = new Timer();
        t.schedule(new TimerTask(){
            @Override
            public void run() {
                loadGraph();
                setStatusIcon();

            }
        },2000);
    }

    private void getTotalWeeklyEducationExpense() {
        MutableDateTime epoch = new MutableDateTime();
        epoch.setDate(0);
        DateTime now = new DateTime();
        Weeks weeks = Weeks.weeksBetween(epoch, now);

        String itemWeek = "Education"+weeks.getWeeks();

        mAuth = FirebaseAuth.getInstance();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("expenses").child(mAuth.getCurrentUser().getUid());
        Query query = ref.orderByChild("itemWeek").equalTo(itemWeek);
        personalRef = FirebaseDatabase.getInstance().getReference().child("personal").child(mAuth.getCurrentUser().getUid());

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    int totalAmount = 0;
                    for(DataSnapshot ds : snapshot.getChildren()){
                        Map<String, Object> map = (Map<String, Object>)ds.getValue();
                        Object total = map.get("amount");
                        int eTotal = Integer.parseInt(String.valueOf(total));
                        totalAmount += eTotal;
                        educationAnalyticsAmount.setText(" Spent: "+totalAmount);
                    }
                    personalRef.child("weekEducation").setValue(totalAmount);
                }else {
                    relativeLayoutEducation.setVisibility(View.GONE);
                    personalRef.child("weekEducation").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getTotalWeeklyHouseExpense() {
        MutableDateTime epoch = new MutableDateTime();
        epoch.setDate(0);
        DateTime now = new DateTime();
        Weeks weeks = Weeks.weeksBetween(epoch, now);

        String itemWeek = "House"+weeks.getWeeks();

        mAuth = FirebaseAuth.getInstance();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("expenses").child(mAuth.getCurrentUser().getUid());
        Query query = ref.orderByChild("itemWeek").equalTo(itemWeek);
        personalRef = FirebaseDatabase.getInstance().getReference().child("personal").child(mAuth.getCurrentUser().getUid());

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    int totalAmount = 0;
                    for(DataSnapshot ds : snapshot.getChildren()){
                        Map<String, Object> map = (Map<String, Object>)ds.getValue();
                        Object total = map.get("amount");
                        int eTotal = Integer.parseInt(String.valueOf(total));
                        totalAmount += eTotal;
                        houseAnalyticsAmount.setText(" Spent: "+totalAmount);
                    }
                    personalRef.child("weekHouse").setValue(totalAmount);
                }else {
                    relativeLayoutHouse.setVisibility(View.GONE);
                    personalRef.child("weekHouse").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getTotalWeeklyFoodExpense() {
        MutableDateTime epoch = new MutableDateTime();
        epoch.setDate(0);
        DateTime now = new DateTime();
        Weeks weeks = Weeks.weeksBetween(epoch, now);

        String itemWeek = "Food"+weeks.getWeeks();

        mAuth = FirebaseAuth.getInstance();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("expenses").child(mAuth.getCurrentUser().getUid());
        Query query = ref.orderByChild("itemWeek").equalTo(itemWeek);
        personalRef = FirebaseDatabase.getInstance().getReference().child("personal").child(mAuth.getCurrentUser().getUid());

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    int totalAmount = 0;
                    for(DataSnapshot ds : snapshot.getChildren()){
                        Map<String, Object> map = (Map<String, Object>)ds.getValue();
                        Object total = map.get("amount");
                        int eTotal = Integer.parseInt(String.valueOf(total));
                        totalAmount += eTotal;
                        foodAnalyticsAmount.setText(" Spent: "+totalAmount);
                    }
                    personalRef.child("weekFood").setValue(totalAmount);
                }else {
                    relativeLayoutFood.setVisibility(View.GONE);
                    personalRef.child("weekFood").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getTotalWeeklyHealthExpense() {
        MutableDateTime epoch = new MutableDateTime();
        epoch.setDate(0);
        DateTime now = new DateTime();
        Weeks weeks = Weeks.weeksBetween(epoch, now);

        String itemWeek = "Health"+weeks.getWeeks();

        mAuth = FirebaseAuth.getInstance();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("expenses").child(mAuth.getCurrentUser().getUid());
        Query query = ref.orderByChild("itemWeek").equalTo(itemWeek);
        personalRef = FirebaseDatabase.getInstance().getReference().child("personal").child(mAuth.getCurrentUser().getUid());

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    int totalAmount = 0;
                    for(DataSnapshot ds : snapshot.getChildren()){
                        Map<String, Object> map = (Map<String, Object>)ds.getValue();
                        Object total = map.get("amount");
                        int eTotal = Integer.parseInt(String.valueOf(total));
                        totalAmount += eTotal;
                        healthAnalyticsAmount.setText(" Spent: "+totalAmount);
                    }
                    personalRef.child("weekHealth").setValue(totalAmount);
                }else {
                    relativeLayoutHealth.setVisibility(View.GONE);
                    personalRef.child("weekHealth").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getTotalWeeklyApparelExpense() {
        MutableDateTime epoch = new MutableDateTime();
        epoch.setDate(0);
        DateTime now = new DateTime();
        Weeks weeks = Weeks.weeksBetween(epoch, now);

        String itemWeek = "Apparel"+weeks.getWeeks();

        mAuth = FirebaseAuth.getInstance();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("expenses").child(mAuth.getCurrentUser().getUid());
        Query query = ref.orderByChild("itemWeek").equalTo(itemWeek);
        personalRef = FirebaseDatabase.getInstance().getReference().child("personal").child(mAuth.getCurrentUser().getUid());

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    int totalAmount = 0;
                    for(DataSnapshot ds : snapshot.getChildren()){
                        Map<String, Object> map = (Map<String, Object>)ds.getValue();
                        Object total = map.get("amount");
                        int eTotal = Integer.parseInt(String.valueOf(total));
                        totalAmount += eTotal;
                        apparelAnalyticsAmount.setText(" Spent: "+totalAmount);
                    }
                    personalRef.child("weekApparel").setValue(totalAmount);
                }else {
                    relativeLayoutApparel.setVisibility(View.GONE);
                    personalRef.child("weekApparel").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getTotalWeeklyTransportExpense() {
        MutableDateTime epoch = new MutableDateTime();
        epoch.setDate(0);
        DateTime now = new DateTime();
        Weeks weeks = Weeks.weeksBetween(epoch, now);

        String itemWeek = "Transport"+weeks.getWeeks();

        mAuth = FirebaseAuth.getInstance();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("expenses").child(mAuth.getCurrentUser().getUid());
        Query query = ref.orderByChild("itemWeek").equalTo(itemWeek);
        personalRef = FirebaseDatabase.getInstance().getReference().child("personal").child(mAuth.getCurrentUser().getUid());

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    int totalAmount = 0;
                    for(DataSnapshot ds : snapshot.getChildren()){
                        Map<String, Object> map = (Map<String, Object>)ds.getValue();
                        Object total = map.get("amount");
                        int eTotal = Integer.parseInt(String.valueOf(total));
                        totalAmount += eTotal;
                        transportAnalyticsAmount.setText(" Spent: "+totalAmount);
                    }
                    personalRef.child("weekTransport").setValue(totalAmount);
                }else {
                    relativeLayoutTransport.setVisibility(View.GONE);
                    personalRef.child("weekTransport").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getTotalWeeklyPersonalExpense() {
        MutableDateTime epoch = new MutableDateTime();
        epoch.setDate(0);
        DateTime now = new DateTime();
        Weeks weeks = Weeks.weeksBetween(epoch, now);

        String itemWeek = "Personal"+weeks.getWeeks();

        mAuth = FirebaseAuth.getInstance();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("expenses").child(mAuth.getCurrentUser().getUid());
        Query query = ref.orderByChild("itemWeek").equalTo(itemWeek);
        personalRef = FirebaseDatabase.getInstance().getReference().child("personal").child(mAuth.getCurrentUser().getUid());

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    int totalAmount = 0;
                    for(DataSnapshot ds : snapshot.getChildren()){
                        Map<String, Object> map = (Map<String, Object>)ds.getValue();
                        Object total = map.get("amount");
                        int eTotal = Integer.parseInt(String.valueOf(total));
                        totalAmount += eTotal;
                        personalAnalyticsAmount.setText(" Spent: "+totalAmount);
                    }
                    personalRef.child("weekPersonal").setValue(totalAmount);
                }else {
                    relativeLayoutPersonal.setVisibility(View.GONE);
                    personalRef.child("weekPersonal").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getTotalWeeklyOtherExpense() {
        MutableDateTime epoch = new MutableDateTime();
        epoch.setDate(0);
        DateTime now = new DateTime();
        Weeks weeks = Weeks.weeksBetween(epoch, now);

        String itemWeek = "Other"+weeks.getWeeks();

        mAuth = FirebaseAuth.getInstance();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("expenses").child(mAuth.getCurrentUser().getUid());
        Query query = ref.orderByChild("itemWeek").equalTo(itemWeek);
        personalRef = FirebaseDatabase.getInstance().getReference().child("personal").child(mAuth.getCurrentUser().getUid());

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    int totalAmount = 0;
                    for(DataSnapshot ds : snapshot.getChildren()){
                        Map<String, Object> map = (Map<String, Object>)ds.getValue();
                        Object total = map.get("amount");
                        int eTotal = Integer.parseInt(String.valueOf(total));
                        totalAmount += eTotal;
                        otherAnalyticsAmount.setText(" Spent: "+totalAmount);
                    }
                    personalRef.child("weekOther").setValue(totalAmount);
                }else {
                    relativeLayoutOther.setVisibility(View.GONE);
                    personalRef.child("weekOther").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getTotalWeekExpense() {
        MutableDateTime epoch = new MutableDateTime();
        epoch.setDate(0);
        DateTime now = new DateTime();
        Weeks weeks = Weeks.weeksBetween(epoch, now);

        mAuth = FirebaseAuth.getInstance();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("expenses").child(mAuth.getCurrentUser().getUid());
        Query query = ref.orderByChild("week").equalTo(weeks.getWeeks());
        personalRef = FirebaseDatabase.getInstance().getReference().child("personal").child(mAuth.getCurrentUser().getUid());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists() && snapshot.getChildrenCount()>0){
                    int totalAmount = 0;
                    for(DataSnapshot ds : snapshot.getChildren()){
                        Map<String, Object> map = (Map<String, Object>)ds.getValue();
                        Object total = map.get("amount");
                        int eTotal = Integer.parseInt(String.valueOf(total));
                        totalAmount += eTotal;
                    }
                    totalAmountWeek.setText("Today's Spending : ₹"+totalAmount);
                    monthSpendAmount.setText("Total Spent : ₹"+totalAmount);
                }else {
                    anyChartView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadGraph() {
        personalRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    int educationTotal;
                    if(snapshot.hasChild("weekEducation")){
                        educationTotal = Integer.parseInt(snapshot.child("weekEducation").getValue().toString());
                    }else {
                        educationTotal = 0;
                    }

                    int houseTotal;
                    if(snapshot.hasChild("weekHouse")){
                        houseTotal = Integer.parseInt(snapshot.child("weekHouse").getValue().toString());
                    }else {
                        houseTotal = 0;
                    }

                    int foodTotal;
                    if(snapshot.hasChild("weekFood")){
                        foodTotal = Integer.parseInt(snapshot.child("weekFood").getValue().toString());
                    }else {
                        foodTotal = 0;
                    }

                    int healthTotal;
                    if(snapshot.hasChild("weekHealth")){
                        healthTotal = Integer.parseInt(snapshot.child("weekHealth").getValue().toString());
                    }else {
                        healthTotal = 0;
                    }

                    int apparelTotal;
                    if(snapshot.hasChild("weekApparel")){
                        apparelTotal = Integer.parseInt(snapshot.child("weekApparel").getValue().toString());
                    }else {
                        apparelTotal = 0;
                    }

                    int transportTotal;
                    if(snapshot.hasChild("weekTransport")){
                        transportTotal = Integer.parseInt(snapshot.child("weekTransport").getValue().toString());
                    }else {
                        transportTotal = 0;
                    }

                    int personalTotal;
                    if(snapshot.hasChild("weekPersonal")){
                        personalTotal = Integer.parseInt(snapshot.child("weekPersonal").getValue().toString());
                    }else {
                        personalTotal = 0;
                    }

                    int otherTotal;
                    if(snapshot.hasChild("weekOther")){
                        otherTotal = Integer.parseInt(snapshot.child("weekOther").getValue().toString());
                    }else {
                        otherTotal = 0;
                    }


                    Pie pie = AnyChart.pie();
                    List<DataEntry> data = new ArrayList<>();
                    data.add(new ValueDataEntry("Education", educationTotal));
                    data.add(new ValueDataEntry("House", houseTotal));
                    data.add(new ValueDataEntry("Food", foodTotal));
                    data.add(new ValueDataEntry("Health", healthTotal));
                    data.add(new ValueDataEntry("Apparel", apparelTotal));
                    data.add(new ValueDataEntry("Transport", transportTotal));
                    data.add(new ValueDataEntry("Personal", personalTotal));
                    data.add(new ValueDataEntry("Other", otherTotal));


                    pie.data(data);
                    pie.title("Weekly Analytics");
                    pie.labels().position("outside");
                    pie.legend().title().enabled(true);
                    pie.legend().title()
                            .text("Items Spents On").padding(0d, 0d, 10d, 0d);

                    pie.legend()
                            .position("center-bottom").itemsLayout(LegendLayout.HORIZONTAL).align(Align.CENTER);

                    anyChartView.setChart(pie);

                }else {
                    Toast.makeText(WeeklyAnalyticsActivity.this, "Child does not exist", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setStatusIcon() {
        personalRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    float educationTotal;
                    if (snapshot.hasChild("weekEducation")) {
                        educationTotal = Integer.parseInt(snapshot.child("weekEducation").getValue().toString());
                    } else {
                        educationTotal = 0;
                    }

                    float houseTotal;
                    if (snapshot.hasChild("weekHouse")) {
                        houseTotal = Integer.parseInt(snapshot.child("weekHouse").getValue().toString());
                    } else {
                        houseTotal = 0;
                    }


                    float foodTotal;
                    if (snapshot.hasChild("weekFood")) {
                        foodTotal = Integer.parseInt(snapshot.child("weekFood").getValue().toString());
                    } else {
                        foodTotal = 0;
                    }


                    float healthTotal;
                    if (snapshot.hasChild("weekHealth")) {
                        healthTotal = Integer.parseInt(snapshot.child("weekHealth").getValue().toString());
                    } else {
                        healthTotal = 0;
                    }


                    float apparelTotal;
                    if (snapshot.hasChild("weekApparel")) {
                        apparelTotal = Integer.parseInt(snapshot.child("weekApparel").getValue().toString());
                    } else {
                        apparelTotal = 0;
                    }


                    float transportTotal;
                    if (snapshot.hasChild("weekTransport")) {
                        transportTotal = Integer.parseInt(snapshot.child("weekTransport").getValue().toString());
                    } else {
                        transportTotal = 0;
                    }


                    float personalTotal;
                    if (snapshot.hasChild("weekPersonal")) {
                        personalTotal = Integer.parseInt(snapshot.child("weekPersonal").getValue().toString());
                    } else {
                        personalTotal = 0;
                    }


                    float otherTotal;
                    if (snapshot.hasChild("weekOther")) {
                        otherTotal = Integer.parseInt(snapshot.child("weekOther").getValue().toString());
                    } else {
                        otherTotal = 0;
                    }

                    float monthTotalSpentAmount;
                    if (snapshot.hasChild("week")) {
                        monthTotalSpentAmount = Integer.parseInt(snapshot.child("week").getValue().toString());
                    } else {
                        monthTotalSpentAmount = 0;
                    }


                    //Ratios

                    float educationRatio;
                    if (snapshot.hasChild("weekEducationRatio")) {
                        educationRatio = Integer.parseInt(snapshot.child("weekEducationRatio").getValue().toString());
                    } else {
                        educationRatio = 0;
                    }


                    float houseRatio;
                    if (snapshot.hasChild("weekHouseRatio")) {
                        houseRatio = Integer.parseInt(snapshot.child("weekHouseRatio").getValue().toString());
                    } else {
                        houseRatio = 0;
                    }

                    float foodRatio;
                    if (snapshot.hasChild("weekFoodRatio")) {
                        foodRatio = Integer.parseInt(snapshot.child("weekFoodRatio").getValue().toString());
                    } else {
                        foodRatio = 0;
                    }

                    float healthRatio;
                    if (snapshot.hasChild("weekHealthRatio")) {
                        healthRatio = Integer.parseInt(snapshot.child("weekHealthRatio").getValue().toString());
                    } else {
                        healthRatio = 0;
                    }

                    float apparelRatio;
                    if (snapshot.hasChild("weekApparelRatio")) {
                        apparelRatio = Integer.parseInt(snapshot.child("weekApparelRatio").getValue().toString());
                    } else {
                        apparelRatio = 0;
                    }

                    float transportRatio;
                    if (snapshot.hasChild("weekTransportRatio")) {
                        transportRatio = Integer.parseInt(snapshot.child("weekTransportRatio").getValue().toString());
                    } else {
                        transportRatio = 0;
                    }

                    float personalRatio;
                    if (snapshot.hasChild("weekPersonalRatio")) {
                        personalRatio = Integer.parseInt(snapshot.child("weekPersonalRatio").getValue().toString());
                    } else {
                        personalRatio = 0;
                    }

                    float otherRatio;
                    if (snapshot.hasChild("weekOtherRatio")) {
                        otherRatio = Integer.parseInt(snapshot.child("weekOtherRatio").getValue().toString());
                    } else {
                        otherRatio = 0;
                    }

                    float monthTotalAmountSpentRatio;
                    if (snapshot.hasChild("weeklyBudget")) {
                        monthTotalAmountSpentRatio = Integer.parseInt(snapshot.child("weeklyBudget").getValue().toString());
                    } else {
                        monthTotalAmountSpentRatio = 0;
                    }


                    float monthPercent = (monthTotalSpentAmount / monthTotalAmountSpentRatio) * 100;
                    if (monthPercent < 50) {
                        monthRatioSpending.setText(monthPercent + "% used of " + monthTotalAmountSpentRatio + ", Status:");
                        monthRatioSpendingImage.setImageResource(R.drawable.green);
                    } else if (monthPercent >= 50 && monthPercent < 100) {
                        monthRatioSpending.setText(monthPercent + "% used of " + monthTotalAmountSpentRatio + ", Status:");
                        monthRatioSpendingImage.setImageResource(R.drawable.blue);
                    } else {
                        monthRatioSpending.setText(monthPercent + "% used of " + monthTotalAmountSpentRatio + ", Status:");
                        monthRatioSpendingImage.setImageResource(R.drawable.red);
                    }


                    float housePercent = (houseTotal / houseRatio) * 100;
                    if (housePercent < 50) {
                        progressRatioHouse.setText(housePercent + "% used of " + houseRatio + ", Status:");
                        houseStatus.setImageResource(R.drawable.green);
                    } else if (housePercent >= 50 && housePercent < 100) {
                        progressRatioHouse.setText(housePercent + "% used of " + houseRatio + ", Status:");
                        houseStatus.setImageResource(R.drawable.blue);
                    } else {
                        progressRatioHouse.setText(housePercent + "% used of " + houseRatio + ", Status:");
                        houseStatus.setImageResource(R.drawable.red);
                    }

                    float educationPercent = (educationTotal / educationRatio) * 100;
                    if (educationPercent < 50) {
                        progressRatioEducation.setText(educationPercent + "% used of " + educationRatio + ", Status:");
                        educationStatus.setImageResource(R.drawable.green);
                    } else if (educationPercent >= 50 && educationPercent < 100) {
                        progressRatioEducation.setText(educationPercent + "% used of " + educationRatio + ", Status:");
                        educationStatus.setImageResource(R.drawable.blue);
                    } else {
                        progressRatioEducation.setText(educationPercent + "% used of " + educationRatio + ", Status:");
                        educationStatus.setImageResource(R.drawable.red);
                    }


                    float foodPercent = (foodTotal / foodRatio) * 100;
                    if (foodPercent < 50) {
                        progressRatioFood.setText(foodPercent + "% used of " + foodRatio + ", Status:");
                        foodStatus.setImageResource(R.drawable.green);
                    } else if (foodPercent >= 50 && foodPercent < 100) {
                        progressRatioFood.setText(foodPercent + "% used of " + foodRatio + ", Status:");
                        foodStatus.setImageResource(R.drawable.blue);
                    } else {
                        progressRatioFood.setText(foodPercent + "% used of " + foodRatio + ", Status:");
                        foodStatus.setImageResource(R.drawable.red);
                    }


                    float healthPercent = (healthTotal / healthRatio) * 100;
                    if (healthPercent < 50) {
                        progressRatioHealth.setText(healthPercent + "% used of " + healthRatio + ", Status:");
                        healthStatus.setImageResource(R.drawable.green);
                    } else if (healthPercent >= 50 && healthPercent < 100) {
                        progressRatioHealth.setText(healthPercent + "% used of " + healthRatio + ", Status:");
                        healthStatus.setImageResource(R.drawable.blue);
                    } else {
                        progressRatioHealth.setText(healthPercent + "% used of " + healthRatio + ", Status:");
                        healthStatus.setImageResource(R.drawable.red);
                    }


                    float apparelPercent = (apparelTotal / apparelRatio) * 100;
                    if (apparelPercent < 50) {
                        progressRatioApparel.setText(apparelPercent + "% used of " + apparelRatio + ", Status:");
                        apparelStatus.setImageResource(R.drawable.green);
                    } else if (apparelPercent >= 50 && apparelPercent < 100) {
                        progressRatioApparel.setText(apparelPercent + "% used of " + apparelRatio + ", Status:");
                        apparelStatus.setImageResource(R.drawable.blue);
                    } else {
                        progressRatioApparel.setText(apparelPercent + "% used of " + apparelRatio + ", Status:");
                        apparelStatus.setImageResource(R.drawable.red);
                    }


                    float transportPercent = (transportTotal / transportRatio) * 100;
                    if (transportPercent < 50) {
                        progressRatioTransport.setText(transportPercent + "% used of " + transportRatio + ", Status:");
                        transportStatus.setImageResource(R.drawable.green);
                    } else if (transportPercent >= 50 && transportPercent < 100) {
                        progressRatioTransport.setText(transportPercent + "% used of " + transportRatio + ", Status:");
                        transportStatus.setImageResource(R.drawable.blue);
                    } else {
                        progressRatioTransport.setText(transportPercent + "% used of " + transportRatio + ", Status:");
                        transportStatus.setImageResource(R.drawable.red);
                    }


                    float personalPercent = (personalTotal / personalRatio) * 100;
                    if (personalPercent < 50) {
                        progressRatioPersonal.setText(personalPercent + "% used of " + personalRatio + ", Status:");
                        personalStatus.setImageResource(R.drawable.green);
                    } else if (personalPercent >= 50 && personalPercent < 100) {
                        progressRatioPersonal.setText(personalPercent + "% used of " + personalRatio + ", Status:");
                        personalStatus.setImageResource(R.drawable.blue);
                    } else {
                        progressRatioPersonal.setText(personalPercent + "% used of " + personalRatio + ", Status:");
                        personalStatus.setImageResource(R.drawable.red);
                    }


                    float otherPercent = (otherTotal / otherRatio) * 100;
                    if (otherPercent < 50) {
                        progressRatioOther.setText(otherPercent + "% used of " + otherRatio + ", Status:");
                        otherStatus.setImageResource(R.drawable.green);
                    } else if (otherPercent >= 50 && otherPercent < 100) {
                        progressRatioOther.setText(otherPercent + "% used of " + otherRatio + ", Status:");
                        otherStatus.setImageResource(R.drawable.blue);
                    } else {
                        progressRatioOther.setText(otherPercent + "% used of " + otherRatio + ", Status:");
                        otherStatus.setImageResource(R.drawable.red);
                    }

                }else{
                    Toast.makeText(WeeklyAnalyticsActivity.this, "Status Error", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}