package com.example.hs_api;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Vector;

public class DeckActivity extends AppCompatActivity {
    //variable containing all decks created by the user
    private Decks decks;

    //activity to create/remove decks
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deck);

        //getting decks saved on the application as json
        try {
            decks = new Decks(this.getApplicationContext());
        } catch (IOException e) {e.printStackTrace();}
        catch (JSONException e) { e.printStackTrace(); }

        //deck creation interface
        EditText newDeckName = (EditText) findViewById(R.id.deckNameCreation);
        Spinner newDeckClass = (Spinner) findViewById(R.id.deckClassCreation);
        String[] deckClasses={"Hunter","Priest","Demon Hunter","Mage","Rogue","Warlock","Chaman","Warrior","Paladin","Druid"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,deckClasses);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        newDeckClass.setAdapter(spinnerAdapter);

        //listview showing decks created by the user
        MyAdapter deckAdapter = new MyAdapter();
        ListView listDecks = (ListView) findViewById(R.id.listDecks);
        listDecks.setAdapter(deckAdapter);
        for (Deck deck : decks.getDecks()) {
            deckAdapter.add(deck);
        }
        deckAdapter.notifyDataSetChanged();

        //button to create a deck
        Button buttonCreate = (Button) findViewById(R.id.buttonDeckCreation);
        buttonCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                decks.newDeck(newDeckName.getText().toString(),newDeckClass.getSelectedItem().toString());
                deckAdapter.add(decks.getDeck(decks.nbrDeck()-1));
                deckAdapter.notifyDataSetChanged();
                Log.i("click",decks.nbrDeck() + "");
            }
        });



    }

    //adapter class
    public class MyAdapter extends BaseAdapter {
        //decks printed in the listview
        private LinkedList<Deck> decksList = new LinkedList<>();

        public void add(Deck deck) {
            decksList.add(deck);
        }

        @Override
        public int getCount() {
            return decksList.size();
        }

        @Override
        public Object getItem(int position) {
            return decksList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            //printing the user's decks in the listview
            LayoutInflater inflater = LayoutInflater.from(DeckActivity.this);
            View viewMyLayout = inflater.inflate(R.layout.deck_layout, null);

            TextView viewName = (TextView) viewMyLayout.findViewById(R.id.deckName);
            viewName.setText(decksList.get(position).getName());
            TextView viewClass = (TextView) viewMyLayout.findViewById(R.id.deckClass);
            viewClass.setText(decksList.get(position).getDeckClass());
            Button buttonEdit = (Button) viewMyLayout.findViewById(R.id.buttonEdit);
            //button to edit an already existing deck
            buttonEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //change of activity
                    Intent myIntent = new Intent(DeckActivity.this, CollectionActivity.class);
                    myIntent.putExtra("isDeckbuilding",true);
                    Log.i("EXTRA",myIntent.getBooleanExtra("isDeckbuilding",false) + "");
                    myIntent.putExtra("Decks", decks);
                    myIntent.putExtra("idDeck",position);
                    startActivity(myIntent);
                }
            });
            //button to delete an already existing deck
            Button buttonDelete = (Button) viewMyLayout.findViewById(R.id.buttonDelete);
            buttonDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    decks.getDecks().remove(position);
                    decksList.remove(position);
                    notifyDataSetChanged();
                }
            });
            try {
                decks.saveDecks();
            } catch (JSONException e) { e.printStackTrace();}
            catch (IOException e) {e.printStackTrace();}
            return viewMyLayout;
        }
    }
}