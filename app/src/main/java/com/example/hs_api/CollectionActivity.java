package com.example.hs_api;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.okhttp.OkHttpClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Vector;

public class CollectionActivity extends AppCompatActivity {
    //if true the user is currently building a deck if not he is browsing the card collection
    private Boolean isDeckBuilding;
    //variable containing all the decks created by the user
    private Decks decks;
    //id of the deck currently being built
    private Integer idDeck;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection);

        //getting extras from the intent to check if the user is deckbuilding or browsing collection
        isDeckBuilding = (Boolean) getIntent().getBooleanExtra("isDeckbuilding",false);
        if (isDeckBuilding) {
            decks = (Decks) getIntent().getSerializableExtra("Decks");
            idDeck = getIntent().getIntExtra("idDeck",0);
        }

        //instanciating the adapter
        MyAdapter adapter = new MyAdapter();
        ListView list_img = (ListView) findViewById(R.id.list);
        //linking it to the image list object
        list_img.setAdapter(adapter);
        //instanciating a queue
        RequestQueue queue = MySingleton.getInstance(this.getApplicationContext()).getRequestQueue();
        //requesting the API
        new AsyncHSAPI(adapter).execute();
    }

    //adapter class
    public class MyAdapter extends BaseAdapter {

        private Vector<String> urls = new Vector<>();
        private Vector<String> texts = new Vector<>();

        public void add(String url,String txt) {
            urls.add(url);
            texts.add(txt);
        }

        @Override
        public int getCount() {
            return urls.size();
        }

        @Override
        public Object getItem(int position) {
            return urls.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            //inflating the layout used to prompt images
            LayoutInflater inflater = LayoutInflater.from(CollectionActivity.this);
            View viewMyLayout;
            Response.Listener<Bitmap> rep_listener;
            //if the user is deckbuilding ths layout is used
            if (isDeckBuilding) {
                viewMyLayout = inflater.inflate(R.layout.deckbuilding_layout, null);
                ImageView imgview = (ImageView) viewMyLayout.findViewById(R.id.imgCard2);
                TextView textView = (TextView) viewMyLayout.findViewById(R.id.textView2);
                Button buttonAdd = (Button) viewMyLayout.findViewById(R.id.buttonAdd);
                Button buttonRemove = (Button) viewMyLayout.findViewById(R.id.buttonRemove);
                //listener in charge of changing the layout according to the differents cards
                rep_listener = response -> {
                    imgview.setImageBitmap(response);
                    textView.setText(texts.get(position) + " : " + decks.getDeck(idDeck).getNbrCard(texts.get(position)) + " / 2");
                    //button to add a card to the current deck
                    buttonAdd.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //adding the card to the Deck
                            decks.getDeck(idDeck).addCard(texts.get(position));
                            notifyDataSetChanged();
                            try {
                                //save the changes in the json
                                decks.saveDecks();
                            } catch (JSONException e) {e.printStackTrace();}
                            catch (IOException e) {e.printStackTrace();}
                        }
                    });
                    //button to remove a card from the current deck
                    buttonRemove.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //removing the card from the Deck
                            decks.getDeck(idDeck).removeCard(texts.get(position));
                            notifyDataSetChanged();
                            try {
                                //save the changes in the json
                                decks.saveDecks();
                            } catch (JSONException e) {e.printStackTrace();}
                            catch (IOException e) {e.printStackTrace();}
                        }
                    });
                };
            } else {
                //if the user is broswing the collection this layout is used
                viewMyLayout = inflater.inflate(R.layout.collection_layout, null);
                ImageView imgview = (ImageView) viewMyLayout.findViewById(R.id.imgCard);
                TextView textView = (TextView) viewMyLayout.findViewById(R.id.textView3);
                //listener in charge of changing the layout according to the differents cards
                rep_listener = response -> {
                    imgview.setImageBitmap(response);
                    textView.setText(texts.get(position));

                };
            }


            //instanciating the image request for the image situed at "position" in the vector
            ImageRequest imageRequest = new ImageRequest(urls.get(position), rep_listener, 0, 0,ImageView.ScaleType.CENTER_CROP,Bitmap.Config.RGB_565, null);
            //adding the request to the queue
            MySingleton.getInstance(CollectionActivity.this).addToRequestQueue(imageRequest);
            return viewMyLayout;
        }
    }

    //asynchronous task in charge of connecting to the API
    public class AsyncHSAPI extends AsyncTask<String, Void, JSONObject> {
        private MyAdapter adapter;

        public AsyncHSAPI(MyAdapter adapter) {
            this.adapter = adapter;
        }

        @Override
        protected JSONObject doInBackground(String... strings) {
            //connecting to the API
            OkHttpClient client = new OkHttpClient();
            //request to get all cards
            com.squareup.okhttp.Request request = new com.squareup.okhttp.Request.Builder()
                    .url("https://omgvamp-hearthstone-v1.p.rapidapi.com/cards?collectible=1")
                    .get()
                    .addHeader("x-rapidapi-key", "eb38ca5bd9msh4950b7f7597ffbep17c9cdjsn48403645905c")
                    .addHeader("x-rapidapi-host", "omgvamp-hearthstone-v1.p.rapidapi.com")
                    .build();

            try {
                com.squareup.okhttp.Response response = client.newCall(request).execute();

                String json = response.body().string();
                //getting all cards as a json
                return new JSONObject(json);
            } catch (IOException | JSONException e) {e.printStackTrace();}


            return null;
        }

        protected void onPostExecute(JSONObject json) {
            String url = null;
            String artist = null;
            String flavor = null;
            String cardClass = null;
            String idCard = null;
            String rarity = null;
            String[] expansions = new String[]{"Basic","Rise of Shadows","Saviors of Uldum","Descent of Dragons","Ashes of Outland","Scholomance Academy"};
            try {
                //filtering all cards by expansion
                for(String expansion : expansions) {
                    JSONArray expansionJson = json.getJSONArray(expansion);
                    //getting all card in an expansion
                    for (int i = 0; i < expansionJson.length(); i++) {
                        //checking that we don't get hero card (can't ba placed in a deck) and if the user is deckbuilding, only card of the deck's class get through
                        if ((!expansionJson.getJSONObject(i).getString("type").equals("Hero") && !isDeckBuilding) ||
                                (!expansionJson.getJSONObject(i).getString("type").equals("Hero") && isDeckBuilding &&
                                        (expansionJson.getJSONObject(i).getString("playerClass").equals("Neutral") ||
                                                expansionJson.getJSONObject(i).getString("playerClass").equals(decks.getDeck(idDeck).getDeckClass())))) {
                            //if the user is deckbuilding only the necessary information are saved
                            if (isDeckBuilding) {
                                url = expansionJson.getJSONObject(i).getString("img");
                                idCard = expansionJson.getJSONObject(i).getString("name");
                                rarity = expansionJson.getJSONObject(i).getString("rarity");
                                adapter.add(url,idCard);
                            } else {
                                //if the user is browsing the collection more information are saved
                                url = expansionJson.getJSONObject(i).getString("img");
                                artist = expansionJson.getJSONObject(i).getString("artist");
                                flavor = expansionJson.getJSONObject(i).getString("flavor");
                                cardClass = expansionJson.getJSONObject(i).getString("playerClass");
                                adapter.add(url, "expansion : " + expansion + "\n" + "class : " + cardClass + "\n" + "artist : " + artist + "\n" + "flavor : " + flavor);
                            }
                        }
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            adapter.notifyDataSetChanged();
        }

    }

    //copied singleton class in charge of creating the queue
    public static class MySingleton {
        private static MySingleton instance;
        private RequestQueue requestQueue;
        private ImageLoader imageLoader;
        private static Context ctx;

        private MySingleton(Context context) {
            ctx = context;
            requestQueue = getRequestQueue();

            imageLoader = new ImageLoader(requestQueue,
                    new ImageLoader.ImageCache() {
                        private final LruCache<String, Bitmap>
                                cache = new LruCache<String, Bitmap>(20);

                        @Override
                        public Bitmap getBitmap(String url) {
                            return cache.get(url);
                        }

                        @Override
                        public void putBitmap(String url, Bitmap bitmap) {
                            cache.put(url, bitmap);
                        }
                    });
        }

        public static synchronized MySingleton getInstance(Context context) {
            if (instance == null) {
                instance = new MySingleton(context);
            }
            return instance;
        }

        public RequestQueue getRequestQueue() {
            if (requestQueue == null) {
                // getApplicationContext() is key, it keeps you from leaking the
                // Activity or BroadcastReceiver if someone passes one in.
                requestQueue = Volley.newRequestQueue(ctx.getApplicationContext());
            }
            return requestQueue;
        }

        public <T> void addToRequestQueue(Request<T> req) {
            getRequestQueue().add(req);
        }

        public ImageLoader getImageLoader() {
            return imageLoader;
        }
    }


}