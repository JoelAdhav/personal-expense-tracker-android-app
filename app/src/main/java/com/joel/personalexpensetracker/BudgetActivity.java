package com.joel.personalexpensetracker;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.joda.time.DateTime;
import org.joda.time.Months;
import org.joda.time.MutableDateTime;
import org.joda.time.Weeks;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;

public class BudgetActivity extends AppCompatActivity {

    private TextView totalBudgetAmountTv;
    private RecyclerView recyclerView;

    private ProgressBar progressBar;

    private DatabaseReference budgetRef, personalRef;
    private FirebaseAuth mAuth;
    private ProgressDialog loader;
    private FloatingActionButton fab;

    private String post_key = "";
    private String item = "";
    private int amount = 0;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget);

        progressBar = findViewById(R.id.progressBar);
        totalBudgetAmountTv = findViewById(R.id.totalBudgetAmountTv);
        recyclerView = findViewById(R.id.recyclerView);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        mAuth = FirebaseAuth.getInstance();
        budgetRef = FirebaseDatabase.getInstance().getReference().child("budget").child(mAuth.getCurrentUser().getUid());
        personalRef = FirebaseDatabase.getInstance().getReference().child("personal").child(mAuth.getCurrentUser().getUid());
        loader = new ProgressDialog(this);

        fab = findViewById(R.id.fab);




        budgetRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int totalAmount = 0;

                for(DataSnapshot snap: snapshot.getChildren()){
                    Data data = snap.getValue(Data.class);
                    totalAmount += data.getAmount();
                    String sTotal = String.valueOf("Month Budget: ₹" + totalAmount);
                    totalBudgetAmountTv.setText(sTotal);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addItem();
            }
        });

        budgetRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists() && snapshot.getChildrenCount()>0){
                    int totalAmount = 0;

                    for(DataSnapshot ds : snapshot.getChildren()){
                        Data data = ds.getValue(Data.class);
                        totalAmount += data.getAmount();
                        String mTotal = String.valueOf("Month Budget: " + totalAmount);
                        totalBudgetAmountTv.setText(mTotal);
                    }

                    int weeklyBudget = totalAmount/4;
                    int dailyBudget = totalAmount/30;
                    personalRef.child("budget").setValue(totalAmount);
                    personalRef.child("weeklyBudget").setValue(weeklyBudget);
                    personalRef.child("dailyBudget").setValue(dailyBudget);
                }else {
                    personalRef.child("budget").setValue(0);
                    personalRef.child("weeklyBudget").setValue(0);
                    personalRef.child("dailyBudget").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        getEducationBudgetRatio();
        getHouseBudgetRatio();
        getFoodBudgetRatio();
        getHealthBudgetRatio();
        getApparelBudgetRatio();
        getTransportBudgetRatio();
        getPersonalBudgetRatio();
        getOtherBudgetRatio();

    }

    private void getOtherBudgetRatio() {
        Query query = budgetRef.orderByChild("item").equalTo("Other");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    int eTotal = 0;
                    for(DataSnapshot ds : snapshot.getChildren()){
                        Map<String, Object> map = (Map<String, Object>)ds.getValue();
                        Object total = map.get("amount");
                        eTotal = Integer.parseInt(String.valueOf(total));
                    }

                    int dayOtherRatio = eTotal/30;
                    int weekOtherRatio = eTotal/4;
                    int monthOtherRatio = eTotal;

                    personalRef.child("dayOtherRatio").setValue(dayOtherRatio);
                    personalRef.child("weekOtherRatio").setValue(weekOtherRatio);
                    personalRef.child("monthOtherRatio").setValue(monthOtherRatio);

                }else {
                    personalRef.child("dayOtherRatio").setValue(0);
                    personalRef.child("weekOtherRatio").setValue(0);
                    personalRef.child("monthOtherRatio").setValue(0);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getPersonalBudgetRatio() {
        Query query = budgetRef.orderByChild("item").equalTo("Personal");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    int eTotal = 0;
                    for(DataSnapshot ds : snapshot.getChildren()){
                        Map<String, Object> map = (Map<String, Object>)ds.getValue();
                        Object total = map.get("amount");
                        eTotal = Integer.parseInt(String.valueOf(total));
                    }

                    int dayPersonalRatio = eTotal/30;
                    int weekPersonalRatio = eTotal/4;
                    int monthPersonalRatio = eTotal;

                    personalRef.child("dayPersonalRatio").setValue(dayPersonalRatio);
                    personalRef.child("weekPersonalRatio").setValue(weekPersonalRatio);
                    personalRef.child("monthPersonalRatio").setValue(monthPersonalRatio);

                }else {
                    personalRef.child("dayPersonalRatio").setValue(0);
                    personalRef.child("weekPersonalRatio").setValue(0);
                    personalRef.child("monthPersonalRatio").setValue(0);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getTransportBudgetRatio() {
        Query query = budgetRef.orderByChild("item").equalTo("Transport");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    int eTotal = 0;
                    for(DataSnapshot ds : snapshot.getChildren()){
                        Map<String, Object> map = (Map<String, Object>)ds.getValue();
                        Object total = map.get("amount");
                        eTotal = Integer.parseInt(String.valueOf(total));
                    }

                    int dayTransportRatio = eTotal/30;
                    int weekTransportRatio = eTotal/4;
                    int monthTransportRatio = eTotal;

                    personalRef.child("dayTransportRatio").setValue(dayTransportRatio);
                    personalRef.child("weekTransportRatio").setValue(weekTransportRatio);
                    personalRef.child("monthTransportRatio").setValue(monthTransportRatio);

                }else {
                    personalRef.child("dayTransportRatio").setValue(0);
                    personalRef.child("weekTransportRatio").setValue(0);
                    personalRef.child("monthTransportRatio").setValue(0);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getApparelBudgetRatio() {
        Query query = budgetRef.orderByChild("item").equalTo("Apparel");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    int eTotal = 0;
                    for(DataSnapshot ds : snapshot.getChildren()){
                        Map<String, Object> map = (Map<String, Object>)ds.getValue();
                        Object total = map.get("amount");
                        eTotal = Integer.parseInt(String.valueOf(total));
                    }

                    int dayApparelRatio = eTotal/30;
                    int weekApparelRatio = eTotal/4;
                    int monthApparelRatio = eTotal;

                    personalRef.child("dayApparelRatio").setValue(dayApparelRatio);
                    personalRef.child("weekApparelRatio").setValue(weekApparelRatio);
                    personalRef.child("monthApparelRatio").setValue(monthApparelRatio);

                }else {
                    personalRef.child("dayApparelRatio").setValue(0);
                    personalRef.child("weekApparelRatio").setValue(0);
                    personalRef.child("monthApparelRatio").setValue(0);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getHealthBudgetRatio() {
        Query query = budgetRef.orderByChild("item").equalTo("Health");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    int eTotal = 0;
                    for(DataSnapshot ds : snapshot.getChildren()){
                        Map<String, Object> map = (Map<String, Object>)ds.getValue();
                        Object total = map.get("amount");
                        eTotal = Integer.parseInt(String.valueOf(total));
                    }

                    int dayHealthRatio = eTotal/30;
                    int weekHealthRatio = eTotal/4;
                    int monthHealthRatio = eTotal;

                    personalRef.child("dayHealthRatio").setValue(dayHealthRatio);
                    personalRef.child("weekHealthRatio").setValue(weekHealthRatio);
                    personalRef.child("monthHealthRatio").setValue(monthHealthRatio);

                }else {
                    personalRef.child("dayHealthRatio").setValue(0);
                    personalRef.child("weekHealthRatio").setValue(0);
                    personalRef.child("monthHealthRatio").setValue(0);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getFoodBudgetRatio() {
        Query query = budgetRef.orderByChild("item").equalTo("Food");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    int eTotal = 0;
                    for(DataSnapshot ds : snapshot.getChildren()){
                        Map<String, Object> map = (Map<String, Object>)ds.getValue();
                        Object total = map.get("amount");
                        eTotal = Integer.parseInt(String.valueOf(total));
                    }

                    int dayFoodRatio = eTotal/30;
                    int weekFoodRatio = eTotal/4;
                    int monthFoodRatio = eTotal;

                    personalRef.child("dayFoodRatio").setValue(dayFoodRatio);
                    personalRef.child("weekFoodRatio").setValue(weekFoodRatio);
                    personalRef.child("monthFoodRatio").setValue(monthFoodRatio);

                }else {
                    personalRef.child("dayFoodRatio").setValue(0);
                    personalRef.child("weekFoodRatio").setValue(0);
                    personalRef.child("monthFoodRatio").setValue(0);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getHouseBudgetRatio() {
        Query query = budgetRef.orderByChild("item").equalTo("House");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    int eTotal = 0;
                    for(DataSnapshot ds : snapshot.getChildren()){
                        Map<String, Object> map = (Map<String, Object>)ds.getValue();
                        Object total = map.get("amount");
                        eTotal = Integer.parseInt(String.valueOf(total));
                    }

                    int dayHouseRatio = eTotal/30;
                    int weekHouseRatio = eTotal/4;
                    int monthHouseRatio = eTotal;

                    personalRef.child("dayHouseRatio").setValue(dayHouseRatio);
                    personalRef.child("weekHouseRatio").setValue(weekHouseRatio);
                    personalRef.child("monthHouseRatio").setValue(monthHouseRatio);

                }else {
                    personalRef.child("dayHouseRatio").setValue(0);
                    personalRef.child("weekHouseRatio").setValue(0);
                    personalRef.child("monthHouseRatio").setValue(0);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getEducationBudgetRatio() {
        Query query = budgetRef.orderByChild("item").equalTo("Education");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    int eTotal = 0;
                    for(DataSnapshot ds : snapshot.getChildren()){
                        Map<String, Object> map = (Map<String, Object>)ds.getValue();
                        Object total = map.get("amount");
                        eTotal = Integer.parseInt(String.valueOf(total));
                    }

                    int dayEducationRatio = eTotal/30;
                    int weekEducationRatio = eTotal/4;
                    int monthEducationRatio = eTotal;

                    personalRef.child("dayEducationRatio").setValue(dayEducationRatio);
                    personalRef.child("weekEducationRatio").setValue(weekEducationRatio);
                    personalRef.child("monthEducationRatio").setValue(monthEducationRatio);

                }else {
                    personalRef.child("dayEducationRatio").setValue(0);
                    personalRef.child("weekEducationRatio").setValue(0);
                    personalRef.child("monthEducationRatio").setValue(0);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void addItem() {

        AlertDialog.Builder myDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View myView = inflater.inflate(R.layout.input_layout, null);
        myDialog.setView(myView);

        final AlertDialog dialog = myDialog.create();
        dialog.setCancelable(false);

        final Spinner itemSpinner = myView.findViewById(R.id.itemSpinner);
        final EditText amount = myView.findViewById(R.id.amount);
        final Button cancel = myView.findViewById(R.id.cancel);
        final Button save = myView.findViewById(R.id.save);

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String budgetAmount = amount.getText().toString();
                String budgetItem = itemSpinner.getSelectedItem().toString();

                if(TextUtils.isEmpty(budgetAmount)){
                    amount.setError("Amount is Required");
                    return;
                }
                if(budgetItem.equals("Select item")){
                    Toast.makeText(BudgetActivity.this, "Select a valid Item", Toast.LENGTH_SHORT).show();
                }
                else{
                    loader.setMessage("Adding Budget Item");
                    loader.setCanceledOnTouchOutside(false);
                    loader.show();

                    String id = budgetRef.push().getKey();

                    DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                    Calendar cal = Calendar.getInstance();
                    String date = dateFormat.format(cal.getTime());

                    MutableDateTime epoch = new MutableDateTime();
                    epoch.setDate(0);
                    DateTime now = new DateTime();
                    Weeks weeks = Weeks.weeksBetween(epoch, now);
                    Months months = Months.monthsBetween(epoch, now);

                    String itemDay = budgetItem + date;
                    String itemWeek = budgetItem + weeks.getWeeks();
                    String itemMonth = budgetItem + months.getMonths();

                    Data data = new Data(budgetItem, date, null, id, itemDay, itemWeek, itemMonth, Integer.parseInt(budgetAmount), months.getMonths(), weeks.getWeeks());
                    budgetRef.child(id).setValue(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(BudgetActivity.this, "Budget Item Added Successfully", Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(BudgetActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                            }

                            loader.dismiss();
                        }
                    });
                }
                dialog.dismiss();
            }
        });



        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }





    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Data> options = new FirebaseRecyclerOptions.Builder<Data>().setQuery(budgetRef, Data.class).build();


        FirebaseRecyclerAdapter<Data, MyViewHolder> adapter = new FirebaseRecyclerAdapter<Data, MyViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MyViewHolder holder, int position, @NonNull Data model) {
                holder.setItemAmount("Amount : ₹"+ model.getAmount());
                holder.setDate("On "+ model.getDate());
                holder.setItemName("Budget Item : "+model.getItem());


                holder.notes.setVisibility(View.GONE);

                switch (model.getItem()){
                    case "Transport":
                        holder.custom.setImageResource(R.drawable.transport);
                        break;
                    case "Education":
                        holder.custom.setImageResource(R.drawable.education);
                        break;
                    case "House":
                        holder.custom.setImageResource(R.drawable.house);
                        break;
                    case "Food":
                        holder.custom.setImageResource(R.drawable.food);
                        break;
                    case "Health":
                        holder.custom.setImageResource(R.drawable.health);
                        break;
                    case "Apparel":
                        holder.custom.setImageResource(R.drawable.apparel);
                        break;
                    case "Personal":
                        holder.custom.setImageResource(R.drawable.personal);
                        break;
                    case "Other":
                        holder.custom.setImageResource(R.drawable.other);
                        break;
                }

                progressBar.setVisibility(View.GONE);

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        post_key = getRef(holder.getAdapterPosition()).getKey();
                        item = model.getItem();
                        amount = model.getAmount();

                        updateData();
                    }
                });


            }

            @NonNull
            @Override
            public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.retrieve_layout, parent, false);
                return new MyViewHolder(view);
            }
        };

        recyclerView.setAdapter(adapter);
        adapter.startListening();
        adapter.notifyDataSetChanged();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        View mView;
        public ImageView custom;
        public TextView notes, date;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            mView = itemView;
            custom = itemView.findViewById(R.id.custom);
            notes = itemView.findViewById(R.id.note);
            date = itemView.findViewById(R.id.date);

        }

        public void setItemName(String itemName){
            TextView item = mView.findViewById(R.id.item);
            item.setText(itemName);
        }

        public void setItemAmount(String itemAmount){
            TextView amount = mView.findViewById(R.id.amount);
            amount.setText(itemAmount);
        }

        public void setDate(String itemDate){
            TextView date = mView.findViewById(R.id.date);
            date.setText(itemDate);
        }

    }






    private void updateData(){
        AlertDialog.Builder myDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View mView = inflater.inflate(R.layout.update_layout, null);
        myDialog.setView(mView);

        final AlertDialog dialog = myDialog.create();

        final TextView mItem = mView.findViewById(R.id.itemName);
        final EditText mAmount = mView.findViewById(R.id.amount);
        final EditText mNote = mView.findViewById(R.id.note);

        mNote.setVisibility(View.GONE);

        mItem.setText(item);
        mAmount.setText(String.valueOf(amount));
        mAmount.setSelection(String.valueOf(amount).length());

        Button deleteBtn = mView.findViewById(R.id.deleteBtn);
        Button updateBtn = mView.findViewById(R.id.updateBtn);


        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                amount = Integer.parseInt(mAmount.getText().toString());


                DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                Calendar cal = Calendar.getInstance();
                String date = dateFormat.format(cal.getTime());


                MutableDateTime epoch = new MutableDateTime();
                epoch.setDate(0);
                DateTime now = new DateTime();
                Weeks weeks = Weeks.weeksBetween(epoch, now);
                Months months = Months.monthsBetween(epoch, now);

                String itemDay = item + date;
                String itemWeek = item + weeks.getWeeks();
                String itemMonth = item + months.getMonths();


                Data data = new Data(item, date, null, post_key, itemDay, itemWeek, itemMonth, amount, months.getMonths(), weeks.getWeeks());
                budgetRef.child(post_key).setValue(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(BudgetActivity.this, "Updated Successfully", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(BudgetActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                        }

                    }
                });

                dialog.dismiss();

            }
        });

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                budgetRef.child(post_key).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(BudgetActivity.this, "Deleted Successfully", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(BudgetActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                        }

                    }
                });

                dialog.dismiss();

            }
        });
        dialog.show();
    }
}