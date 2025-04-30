package com.usc.ezekiel_day1;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class Reserve extends AppCompatActivity {

    private EditText searchInput;
    private Button bookBtn1, bookBtn2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reserve); 
        
        searchInput = findViewById(R.id.search_input);
        bookBtn1 = findViewById(R.id.book_btn_1);
        bookBtn2 = findViewById(R.id.book_btn_2);
        
        bookBtn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Reserve.this, "Booked: House of Lechon", Toast.LENGTH_SHORT).show();
            }
        });

        bookBtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Reserve.this, "Booked: Motorcycle Parking", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
