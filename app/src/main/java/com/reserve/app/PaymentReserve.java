package com.usc.ezekiel_day1;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class PaymentReserve extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_reserve);

        String rateInfo = getIntent().getStringExtra("rate_info");

        TextView rateTextView = findViewById(R.id.rate_text_view); // make sure this ID exists in your layout
        if (rateInfo != null) {
            rateTextView.setText(rateInfo);
        }
    }
}
