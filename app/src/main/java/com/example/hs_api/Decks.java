package com.example.hs_api;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

//class representing decks saved as variable in the application
public class Decks implements Serializable {
    //all decks
    LinkedList<Deck> decks;
    //path to the json file where decks are stored
    String path;

    Decks(Context context) throws IOException, JSONException {

        this.path = context.getApplicationInfo().dataDir;
        decks = new LinkedList<>();
        JSONArray savedDecks = new JSONArray();
        //checking if there is already a storage file
        File checkFile = new File(path+ "/Decks.txt");
        if(!checkFile.exists()) {
            //if the is not it load a sample json from the application's assets
            JSONObject fileContent = new JSONObject(getJsonFromAssets(context,"Decks"));
            savedDecks = fileContent.getJSONArray("Decks");
        } else {
            //if there is a storage file it is read then stored in Decks
            String myData = "";
            //opening and reading the file
            try {
                FileInputStream fis = new FileInputStream(path + "/Decks.txt");
                DataInputStream in = new DataInputStream(fis);
                BufferedReader br =
                        new BufferedReader(new InputStreamReader(in));
                String strLine;
                while ((strLine = br.readLine()) != null) {
                    myData = myData + strLine;
                }
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            savedDecks = new JSONObject(myData).getJSONArray("Decks");
        }

        //once the json file is opened it is then translated to create a Decks instance
        String name;
        String deckClass;
        JSONArray jsonCards;
        LinkedList<String> cards;
        LinkedList<String> nbrCards;
        //reading every decks
        for (int i = 0; i < savedDecks.length(); i++) {
            cards = new LinkedList<>();
            nbrCards = new LinkedList<>();
            name = savedDecks.getJSONObject(i).getString("name");
            deckClass = savedDecks.getJSONObject(i).getString("deckClass");
            jsonCards = savedDecks.getJSONObject(i).getJSONArray("Cards");
            //reading every cards
            for (int j = 0; j < jsonCards.length(); j++) {
                cards.add(jsonCards.getJSONObject(j).getString("id"));
                nbrCards.add(jsonCards.getJSONObject(j).getString("nbr"));
            }
            decks.add(new Deck(name, deckClass, cards, nbrCards));
        }

    }

    //method to save the Decks instance of the application in a json file
    public void saveDecks() throws JSONException, IOException {
        JSONArray jsonDecks = new JSONArray();
        JSONObject jsonDeck;
        JSONArray jsonCards;
        JSONObject jsonCard;
        //for each decks
        for (Deck deck : decks) {
            jsonDeck = new JSONObject();
            jsonCards = new JSONArray();
            //for each cards
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
        //creating/filling the file
        try {
            File checkFile = new File(path+  "/Decks.txt");
            if(checkFile.exists()) {
                checkFile.delete();
            }
            checkFile.createNewFile();
            FileWriter file = new FileWriter(path +  "/Decks.txt");
            file.write(jsonFile.toString());
            file.flush();
            file.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public void newDeck(String name,String deckClass) {
        decks.add(new Deck(name,deckClass));
    }

    public LinkedList<Deck> getDecks() {
        return decks;
    }

    public Integer nbrDeck() {
        return decks.size();
    }

    public Deck getDeck(Integer position) {
        return decks.get(position);
    }

    //method to get the sample json from assets
    static String getJsonFromAssets(Context context, String fileName) {
        String jsonString;
        try {
            InputStream is = context.getAssets().open(fileName);

            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            jsonString = new String(buffer, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return jsonString;
    }
}

//class describing a deck
class Deck implements Serializable {
    private String name;
    private String deckClass;
    //name of each card
    private LinkedList<String> cards;
    //number of each card
    private LinkedList<String> nbrCards;
    private LinkedList<Boolean> isLegendary;

    public Deck(String name, String deckClass) {
        this.name = name;
        this.deckClass = deckClass;
        cards = new LinkedList<>();
        nbrCards = new LinkedList<>();
    }

    public Deck(String name, String deckClass, LinkedList<String> cards, LinkedList<String> nbrCards) {
        this.name = name;
        this.deckClass = deckClass;
        this.cards = cards;
        this.nbrCards = nbrCards;

    }

    public LinkedList<String> getCards() {
        return cards;
    }

    public LinkedList<String> getNbrCards() {
        return nbrCards;
    }

    public String getName() {
        return name;
    }

    public String getDeckClass() {
        return deckClass;
    }

    //number of copies of a card
    public Integer getNbrCard(String card) {
        for (int i = 0; i < cards.size(); i++) {
            if (cards.get(i).equals(card)) {

                return Integer.parseInt(nbrCards.get(i));
            }
        }
        return 0;
    }

    public void addCard(String card) {
        boolean isCardInDeck = false;
        int sumAllCards = nbrCards();
        if (sumAllCards < 30) {
            for (int i = 0; i < cards.size(); i++) {
                if (cards.get(i).equals(card)) {
                    isCardInDeck = true;
                    nbrCards.set(i, "2");
                }
            }
            if (!isCardInDeck) {
                cards.add(card);
                nbrCards.add("1");
            }
        }
    }

    public void removeCard(String card) {
        for (int i = 0; i < cards.size(); i++) {
            if (cards.get(i).equals(card)) {
                if (nbrCards.get(i).equals("1")) {
                    nbrCards.remove(i);
                    cards.remove(i);
                } else if (nbrCards.get(i).equals("2")) {
                    nbrCards.set(i,"1");
                }
            }
        }
    }

    //number total of cards in the deck
    public Integer nbrCards() {
        int sum = 0;
        for (int i = 0; i < cards.size(); i++) {
            sum += Integer.parseInt(nbrCards.get(i));
        }
        return sum;
    }


}
