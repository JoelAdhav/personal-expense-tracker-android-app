package com.joel.personalexpensetracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class HistoryActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener{

    private RecyclerView historyRecyclerview;
    private Button search;
    private TextView totalAmountSpentHistory;
    private TodayItemsAdapter todayItemsAdapter;
    private List<Data> myDataList;
    private Toolbar toolbar;

    private FirebaseAuth mAuth;
    private DatabaseReference expenseRef, personalRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("History");


        search = findViewById(R.id.search);
        totalAmountSpentHistory = findViewById(R.id.totalAmountSpentHistory);

        mAuth = FirebaseAuth.getInstance();


        historyRecyclerview = findViewById(R.id.historyRecyclerview);
        LinearLayoutManager layoutManager =  new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        historyRecyclerview.setLayoutManager(layoutManager);

        myDataList = new ArrayList<>();
        todayItemsAdapter = new TodayItemsAdapter(HistoryActivity.this, myDataList);
        historyRecyclerview.setAdapter(todayItemsAdapter);


        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showDatePickerDialog();
            }
        });


    }
    private void showDatePickerDialog(){
        DatePickerDialog datePickerdialog = new DatePickerDialog(
                this,
                this,
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        );
        datePickerdialog.show();
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {

        int months = month + 1;
        String date = dayOfMonth+"-"+"0"+months+"-"+year;

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("expenses").child(mAuth.getCurrentUser().getUid());
        Query query = ref.orderByChild("date").equalTo(date);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                myDataList.clear();
                for(DataSnapshot ds : snapshot.getChildren()){
                    Data data = ds.getValue(Data.class);
                    myDataList.add(data);
                }
                todayItemsAdapter.notifyDataSetChanged();
                historyRecyclerview.setVisibility(View.VISIBLE);

                int totalAmount = 0;
                for(DataSnapshot dataSnap : snapshot.getChildren()){
                    Map<String, Object> map = (Map<String, Object>)dataSnap.getValue();
                    Object total = map.get("amount");
                    int eTotal = Integer.parseInt(String.valueOf(total));
                    totalAmount += eTotal;
                    if(totalAmount>0){
                        totalAmountSpentHistory.setVisibility(View.VISIBLE);
                        totalAmountSpentHistory.setText("This day you Spent : "+totalAmount);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}