package com.example.hs_api;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Decks {
    ArrayList<Deck> decks;
    Context context;
    String path;

    Decks(Context context, String path) throws IOException, JSONException {
        this.context = context;
        this.path = path;
        decks = new ArrayList<>();

        File file = new File(context.getFilesDir(),path);
        if (file.exists()) {
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            StringBuilder stringBuilder = new StringBuilder();
            String line = bufferedReader.readLine();
            while (line != null) {
                stringBuilder.append(line).append("\n");
                line = bufferedReader.readLine();
            }
            bufferedReader.close();
            JSONObject fileContent = new JSONObject(stringBuilder.toString());
            JSONArray savedDecks = fileContent.getJSONArray("Decks");

            String name;
            String deckClass;
            JSONArray jsonCards;
            ArrayList<String> cards;
            ArrayList<String> nbrCards;
            for (int i = 0; i < savedDecks.length(); i++) {
                cards = new ArrayList<>();
                nbrCards = new ArrayList<>();
                name = savedDecks.getJSONObject(i).getString("name");
                deckClass = savedDecks.getJSONObject(i).getString("deckClass");
                jsonCards = savedDecks.getJSONObject(i).getJSONArray("Cards");
                for (int j = 0; j < jsonCards.length(); j++) {
                    cards.add(jsonCards.getJSONObject(j).getString("id"));
                    nbrCards.add(jsonCards.getJSONObject(j).getString("nbr"));
                }
                decks.add(new Deck(name, deckClass, cards, nbrCards));
            }
        }
        else {
            file.mkdir();

        }
    }

    private void saveDecks() throws JSONException, IOException {
        JSONArray jsonDecks = new JSONArray();
        JSONObject jsonDeck;
        JSONArray jsonCards;
        JSONObject jsonCard;
        for (Deck deck : decks) {
            jsonDeck = new JSONObject();
            jsonCards = new JSONArray();
            for(int i = 0; i < deck.getCards().size(); i++) {
                jsonCard = new JSONObject();
                jsonCard.put("id",deck.getCards().get(i));
                jsonCard.put("nbr",deck.getNbrCards().get(i));
                jsonCards.put(jsonCard);
            }
            jsonDeck.put("name",deck.getName());
            jsonDeck.put("deckClass",deck.getDeckClass());
            jsonDeck.put("Cards",jsonCards);
            jsonDecks.put(jsonDeck);
        }
        JSONObject jsonFile = new JSONObject();
        jsonFile.put("Decks",jsonDecks);
        File file = new File(context.getFilesDir(),path);
        if (file.exists()) {
            file.delete();
        }
        file.mkdir();
        File gpxfile = new File(file, "sample");
        FileWriter writer = new FileWriter(gpxfile);
        writer.append(jsonFile.toString());
        writer.flush();
        writer.close();
    }
}

class Deck {
    private String name;
    private String deckClass;
    private ArrayList<String> cards;
    private ArrayList<String> nbrCards;

    public Deck(String name,String deckClass) {
        this.name = name;
        this.deckClass = deckClass;
        cards = new ArrayList<>();
        nbrCards = new ArrayList<>();
    }

    public Deck(String name, String deckClass, ArrayList<String> cards, ArrayList<String> nbrCards) {
        this.name = name;
        this.deckClass = deckClass;
        this.cards = cards;
        this.nbrCards = nbrCards;

    }

    public ArrayList<String> getCards() {
        return cards;
    }

    public List<String> getNbrCards() {
        return nbrCards;
    }

    public String getName() {
        return name;
    }

    public String getDeckClass() {
        return deckClass;
    }
}
