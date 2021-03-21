package com.example.hs_api;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button toCollection = findViewById(R.id.button);
        toCollection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //change of activity
                Intent myIntent = new Intent(MainActivity.this, CollectionActivity.class);
                startActivity(myIntent);
            }
        });

        Button toDecks = findViewById(R.id.button2);
        toDecks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //change of activity
                Intent myIntent = new Intent(MainActivity.this, DeckActivity.class);
                startActivity(myIntent);
            }
        });
    }
}