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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class DailyAnalyticsActivity extends AppCompatActivity {

    private Toolbar settingsToolbar;

    private FirebaseAuth mAuth;
    private DatabaseReference expenseRef, personalRef;

    private TextView totalAmountToday, educationAnalyticsAmount, houseAnalyticsAmount, foodAnalyticsAmount, healthAnalyticsAmount, apparelAnalyticsAmount;
    private TextView transportAnalyticsAmount, personalAnalyticsAmount, otherAnalyticsAmount, monthRatioSpending, monthSpendAmount;

    private RelativeLayout relativeLayoutEducation, relativeLayoutHouse, relativeLayoutFood, relativeLayoutHealth, relativeLayoutApparel, relativeLayoutTransport;
    private RelativeLayout relativeLayoutPersonal, relativeLayoutOther, linearLayoutAnalysis;

    private AnyChartView anyChartView;

    private TextView progressRatioEducation, progressRatioHouse, progressRatioFood, progressRatioHealth, progressRatioApparel, progressRatioTransport, progressRatioPersonal, progressRatioOther;
    private ImageView educationStatus, houseStatus, foodStatus, healthStatus, apparelStatus, transportStatus, personalStatus, otherStatus, monthRatioSpendingImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_analytics);

        settingsToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(settingsToolbar);
        getSupportActionBar().setTitle("Today's Analytics");

        anyChartView = findViewById(R.id.anyChartView);

        mAuth = FirebaseAuth.getInstance();
        expenseRef = FirebaseDatabase.getInstance().getReference().child("expenses").child(mAuth.getCurrentUser().getUid());
        personalRef = FirebaseDatabase.getInstance().getReference().child("personal").child(mAuth.getCurrentUser().getUid());


        // Amount TextView

        totalAmountToday = findViewById(R.id.totalAmountToday);
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
        
        
        getTotalDailyEducationExpense();
        getTotalDailyHouseExpense();
        getTotalDailyFoodExpense();
        getTotalDailyHealthExpense();
        getTotalDailyApparelExpense();
        getTotalDailyTransportExpense();
        getTotalDailyPersonalExpense();
        getTotalDailyOtherExpense();
        getTotalDayExpense();

        Timer t = new Timer();
        t.schedule(new TimerTask(){
            @Override
            public void run() {
                     loadGraph();
                     setStatusIcon();

            }
        },2000);

    }

    private void getTotalDailyEducationExpense() {
        MutableDateTime epoch = new MutableDateTime();
        epoch.setDate(0);
        DateTime now = new DateTime();

        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Calendar cal = Calendar.getInstance();
        String date = dateFormat.format(cal.getTime());
        String itemDay = "Education"+date;

        mAuth = FirebaseAuth.getInstance();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("expenses").child(mAuth.getCurrentUser().getUid());
        personalRef = FirebaseDatabase.getInstance().getReference("personal").child(mAuth.getCurrentUser().getUid());
        Query query = ref.orderByChild("itemDay").equalTo(itemDay);

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
                    personalRef.child("dayEducation").setValue(totalAmount);
                }else {
                    relativeLayoutEducation.setVisibility(View.GONE);
                    personalRef.child("dayEducation").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getTotalDailyHouseExpense() {

        MutableDateTime epoch = new MutableDateTime();
        epoch.setDate(0);
        DateTime now = new DateTime();

        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Calendar cal = Calendar.getInstance();
        String date = dateFormat.format(cal.getTime());
        String itemDay = "House"+date;

        mAuth = FirebaseAuth.getInstance();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("expenses").child(mAuth.getCurrentUser().getUid());
        personalRef = FirebaseDatabase.getInstance().getReference().child("personal").child(mAuth.getCurrentUser().getUid());
        Query query = ref.orderByChild("itemDay").equalTo(itemDay);

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
                    personalRef.child("dayHouse").setValue(totalAmount);
                }else {
                    relativeLayoutHouse.setVisibility(View.GONE);
                    personalRef.child("dayHouse").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getTotalDailyFoodExpense() {
        MutableDateTime epoch = new MutableDateTime();
        epoch.setDate(0);
        DateTime now = new DateTime();

        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Calendar cal = Calendar.getInstance();
        String date = dateFormat.format(cal.getTime());
        String itemDay = "Food"+date;

        mAuth = FirebaseAuth.getInstance();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("expenses").child(mAuth.getCurrentUser().getUid());
        Query query = ref.orderByChild("itemDay").equalTo(itemDay);
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
                    personalRef.child("dayFood").setValue(totalAmount);
                }else {
                    relativeLayoutFood.setVisibility(View.GONE);
                    personalRef.child("dayFood").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getTotalDailyHealthExpense() {
        MutableDateTime epoch = new MutableDateTime();
        epoch.setDate(0);
        DateTime now = new DateTime();

        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Calendar cal = Calendar.getInstance();
        String date = dateFormat.format(cal.getTime());
        String itemDay = "Health"+date;

        mAuth = FirebaseAuth.getInstance();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("expenses").child(mAuth.getCurrentUser().getUid());
        personalRef = FirebaseDatabase.getInstance().getReference().child("personal").child(mAuth.getCurrentUser().getUid());
        Query query = ref.orderByChild("itemDay").equalTo(itemDay);

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
                    personalRef.child("dayHealth").setValue(totalAmount);
                }else {
                    relativeLayoutHealth.setVisibility(View.GONE);
                    personalRef.child("dayHealth").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void getTotalDailyApparelExpense() {
        MutableDateTime epoch = new MutableDateTime();
        epoch.setDate(0);
        DateTime now = new DateTime();

        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Calendar cal = Calendar.getInstance();
        String date = dateFormat.format(cal.getTime());
        String itemDay = "Apparel"+date;

        mAuth = FirebaseAuth.getInstance();
        personalRef = FirebaseDatabase.getInstance().getReference().child("personal").child(mAuth.getCurrentUser().getUid());
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("expenses").child(mAuth.getCurrentUser().getUid());
        Query query = ref.orderByChild("itemDay").equalTo(itemDay);

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
                    personalRef.child("dayApparel").setValue(totalAmount);
                }else {
                    relativeLayoutApparel.setVisibility(View.GONE);
                    personalRef.child("dayApparel").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getTotalDailyTransportExpense() {
        MutableDateTime epoch = new MutableDateTime();
        epoch.setDate(0);
        DateTime now = new DateTime();

        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Calendar cal = Calendar.getInstance();
        String date = dateFormat.format(cal.getTime());
        String itemDay = "Transport"+date;

        mAuth = FirebaseAuth.getInstance();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("expenses").child(mAuth.getCurrentUser().getUid());
        personalRef = FirebaseDatabase.getInstance().getReference().child("personal").child(mAuth.getCurrentUser().getUid());
        Query query = ref.orderByChild("itemDay").equalTo(itemDay);

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
                    personalRef.child("dayTransport").setValue(totalAmount);
                }else {
                    relativeLayoutTransport.setVisibility(View.GONE);
                    personalRef.child("dayTransport").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void getTotalDailyPersonalExpense() {
        MutableDateTime epoch = new MutableDateTime();
        epoch.setDate(0);
        DateTime now = new DateTime();

        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Calendar cal = Calendar.getInstance();
        String date = dateFormat.format(cal.getTime());
        String itemDay = "Personal"+date;

        mAuth = FirebaseAuth.getInstance();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("expenses").child(mAuth.getCurrentUser().getUid());
        personalRef = FirebaseDatabase.getInstance().getReference().child("personal").child(mAuth.getCurrentUser().getUid());
        Query query = ref.orderByChild("itemDay").equalTo(itemDay);

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
                    personalRef.child("dayPersonal").setValue(totalAmount);
                }else {
                    relativeLayoutPersonal.setVisibility(View.GONE);
                    personalRef.child("dayPersonal").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void getTotalDailyOtherExpense() {
        MutableDateTime epoch = new MutableDateTime();
        epoch.setDate(0);
        DateTime now = new DateTime();

        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Calendar cal = Calendar.getInstance();
        String date = dateFormat.format(cal.getTime());
        String itemDay = "Other"+date;

        mAuth = FirebaseAuth.getInstance();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("expenses").child(mAuth.getCurrentUser().getUid());
        personalRef = FirebaseDatabase.getInstance().getReference().child("personal").child(mAuth.getCurrentUser().getUid());
        Query query = ref.orderByChild("itemDay").equalTo(itemDay);

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
                        otherAnalyticsAmount.setText("Spent"+totalAmount);
                    }
                    personalRef.child("dayOther").setValue(totalAmount);
                }else {
                    relativeLayoutOther.setVisibility(View.GONE);
                    personalRef.child("dayOther").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void getTotalDayExpense() {
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Calendar cal = Calendar.getInstance();
        String date = dateFormat.format(cal.getTime());
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("expenses").child(mAuth.getCurrentUser().getUid());
        Query query = ref.orderByChild("date").equalTo(date);

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
                    totalAmountToday.setText("Today's Spending : ₹"+totalAmount);
                    monthSpendAmount.setText("Total Spent : ₹"+totalAmount);
                }else {
                    totalAmountToday.setText("You've not spent Today");
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
                    if(snapshot.hasChild("dayEducation")){
                        educationTotal = Integer.parseInt(snapshot.child("dayEducation").getValue().toString());
                    }else {
                        educationTotal = 0;
                    }

                    int houseTotal;
                    if(snapshot.hasChild("dayHouse")){
                        houseTotal = Integer.parseInt(snapshot.child("dayHouse").getValue().toString());
                    }else {
                        houseTotal = 0;
                    }

                    int foodTotal;
                    if(snapshot.hasChild("dayFood")){
                        foodTotal = Integer.parseInt(snapshot.child("dayFood").getValue().toString());
                    }else {
                        foodTotal = 0;
                    }

                    int healthTotal;
                    if(snapshot.hasChild("dayHealth")){
                        healthTotal = Integer.parseInt(snapshot.child("dayHealth").getValue().toString());
                    }else {
                        healthTotal = 0;
                    }

                    int apparelTotal;
                    if(snapshot.hasChild("dayApparel")){
                        apparelTotal = Integer.parseInt(snapshot.child("dayApparel").getValue().toString());
                    }else {
                        apparelTotal = 0;
                    }

                    int transportTotal;
                    if(snapshot.hasChild("dayTransport")){
                        transportTotal = Integer.parseInt(snapshot.child("dayTransport").getValue().toString());
                    }else {
                        transportTotal = 0;
                    }

                    int personalTotal;
                    if(snapshot.hasChild("dayPersonal")){
                        personalTotal = Integer.parseInt(snapshot.child("dayPersonal").getValue().toString());
                    }else {
                        personalTotal = 0;
                    }

                    int otherTotal;
                    if(snapshot.hasChild("dayOther")){
                        otherTotal = Integer.parseInt(snapshot.child("dayOther").getValue().toString());
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
                    pie.title("Daily Analytics");
                    pie.labels().position("outside");
                    pie.legend().title().enabled(true);
                    pie.legend().title()
                            .text("Items Spents On").padding(0d, 0d, 10d, 0d);

                    pie.legend()
                            .position("center-bottom").itemsLayout(LegendLayout.HORIZONTAL).align(Align.CENTER);

                    anyChartView.setChart(pie);

                }else {
                    Toast.makeText(DailyAnalyticsActivity.this, "Child does not exist", Toast.LENGTH_SHORT).show();
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
                    if (snapshot.hasChild("dayEducation")) {
                        educationTotal = Integer.parseInt(snapshot.child("dayEducation").getValue().toString());
                    } else {
                        educationTotal = 0;
                    }

                    float houseTotal;
                    if (snapshot.hasChild("dayHouse")) {
                        houseTotal = Integer.parseInt(snapshot.child("dayHouse").getValue().toString());
                    } else {
                        houseTotal = 0;
                    }


                    float foodTotal;
                    if (snapshot.hasChild("dayFood")) {
                        foodTotal = Integer.parseInt(snapshot.child("dayFood").getValue().toString());
                    } else {
                        foodTotal = 0;
                    }


                    float healthTotal;
                    if (snapshot.hasChild("dayHealth")) {
                        healthTotal = Integer.parseInt(snapshot.child("dayHealth").getValue().toString());
                    } else {
                        healthTotal = 0;
                    }


                    float apparelTotal;
                    if (snapshot.hasChild("dayApparel")) {
                        apparelTotal = Integer.parseInt(snapshot.child("dayApparel").getValue().toString());
                    } else {
                        apparelTotal = 0;
                    }


                    float transportTotal;
                    if (snapshot.hasChild("dayTransport")) {
                        transportTotal = Integer.parseInt(snapshot.child("dayTransport").getValue().toString());
                    } else {
                        transportTotal = 0;
                    }


                    float personalTotal;
                    if (snapshot.hasChild("dayPersonal")) {
                        personalTotal = Integer.parseInt(snapshot.child("dayPersonal").getValue().toString());
                    } else {
                        personalTotal = 0;
                    }


                    float otherTotal;
                    if (snapshot.hasChild("dayOther")) {
                        otherTotal = Integer.parseInt(snapshot.child("dayOther").getValue().toString());
                    } else {
                        otherTotal = 0;
                    }

                    float monthTotalSpentAmount;
                    if (snapshot.hasChild("today")) {
                        monthTotalSpentAmount = Integer.parseInt(snapshot.child("today").getValue().toString());
                    } else {
                        monthTotalSpentAmount = 0;
                    }


                    //Ratios

                    float educationRatio;
                    if (snapshot.hasChild("dayEducationRatio")) {
                        educationRatio = Integer.parseInt(snapshot.child("dayEducationRatio").getValue().toString());
                    } else {
                        educationRatio = 0;
                    }


                    float houseRatio;
                    if (snapshot.hasChild("dayHouseRatio")) {
                        houseRatio = Integer.parseInt(snapshot.child("dayHouseRatio").getValue().toString());
                    } else {
                        houseRatio = 0;
                    }

                    float foodRatio;
                    if (snapshot.hasChild("dayFoodRatio")) {
                        foodRatio = Integer.parseInt(snapshot.child("dayFoodRatio").getValue().toString());
                    } else {
                        foodRatio = 0;
                    }

                    float healthRatio;
                    if (snapshot.hasChild("dayHealthRatio")) {
                        healthRatio = Integer.parseInt(snapshot.child("dayHealthRatio").getValue().toString());
                    } else {
                        healthRatio = 0;
                    }

                    float apparelRatio;
                    if (snapshot.hasChild("dayApparelRatio")) {
                        apparelRatio = Integer.parseInt(snapshot.child("dayApparelRatio").getValue().toString());
                    } else {
                        apparelRatio = 0;
                    }

                    float transportRatio;
                    if (snapshot.hasChild("dayTransportRatio")) {
                        transportRatio = Integer.parseInt(snapshot.child("dayTransportRatio").getValue().toString());
                    } else {
                        transportRatio = 0;
                    }

                    float personalRatio;
                    if (snapshot.hasChild("dayPersonalRatio")) {
                        personalRatio = Integer.parseInt(snapshot.child("dayPersonalRatio").getValue().toString());
                    } else {
                        personalRatio = 0;
                    }

                    float otherRatio;
                    if (snapshot.hasChild("dayOtherRatio")) {
                        otherRatio = Integer.parseInt(snapshot.child("dayOtherRatio").getValue().toString());
                    } else {
                        otherRatio = 0;
                    }

                    float monthTotalAmountSpentRatio;
                    if (snapshot.hasChild("dailyBudget")) {
                        monthTotalAmountSpentRatio = Integer.parseInt(snapshot.child("dailyBudget").getValue().toString());
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
                    Toast.makeText(DailyAnalyticsActivity.this, "Status Error", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}