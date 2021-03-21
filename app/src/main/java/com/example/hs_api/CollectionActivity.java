package com.example.hs_api;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection);

        //instanciating the adapter
        MyAdapter adapter = new MyAdapter();
        ListView list_img = (ListView) findViewById(R.id.list);
        //linking it to the image list object
        list_img.setAdapter(adapter);
        //instanciating a queue
        RequestQueue queue = MySingleton.getInstance(this.getApplicationContext()).getRequestQueue();
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
            View viewMyLayout = inflater.inflate(R.layout.collection_layout, null);
            ImageView imgview = (ImageView) viewMyLayout.findViewById(R.id.imgCard);
            TextView textView = (TextView) viewMyLayout.findViewById(R.id.textView3);
            //listener in charge of changing the bitmaps in the application
            Response.Listener<Bitmap> rep_listener = response -> {
                imgview.setImageBitmap(response);
                textView.setText(texts.get(position));
                Log.i("bitmap","up img");
            };

            //instanciating the image request for the image situed at "position" in the vector
            ImageRequest imageRequest = new ImageRequest(urls.get(position), rep_listener, 0, 0,ImageView.ScaleType.CENTER_CROP,Bitmap.Config.RGB_565, null);
            //adding the request to the queue
            MySingleton.getInstance(CollectionActivity.this).addToRequestQueue(imageRequest);
            return viewMyLayout;
        }
    }

    //asynchronous task in charge of getting images' urls
    public class AsyncHSAPI extends AsyncTask<String, Void, JSONObject> {
        private MyAdapter adapter;

        public AsyncHSAPI(MyAdapter adapter) {
            this.adapter = adapter;
        }

        @Override
        protected JSONObject doInBackground(String... strings) {
            OkHttpClient client = new OkHttpClient();

            com.squareup.okhttp.Request request = new com.squareup.okhttp.Request.Builder()
                    .url("https://omgvamp-hearthstone-v1.p.rapidapi.com/cards?collectible=1")
                    .get()
                    .addHeader("x-rapidapi-key", "eb38ca5bd9msh4950b7f7597ffbep17c9cdjsn48403645905c")
                    .addHeader("x-rapidapi-host", "omgvamp-hearthstone-v1.p.rapidapi.com")
                    .build();

            try {
                com.squareup.okhttp.Response response = client.newCall(request).execute();

                String json = response.body().string();
                Log.i("JSON", json);
                return new JSONObject(json);
            } catch (IOException | JSONException e) {e.printStackTrace();}


            return null;
        }

        protected void onPostExecute(JSONObject json) {
            String url = null;
            String artist = null;
            String flavor = null;
            String cardClass = null;
            //printing the json containing the images in the logs
            String[] expansions = new String[]{"Rise of Shadows","Saviors of Uldum","Descent of Dragons","Ashes of Outland","Scholomance Academy"," Madness at the Darkmoon Faire"};
            try {
                for(String expansion : expansions) {
                    JSONArray expansionJson = json.getJSONArray(expansion);
                    for (int i = 0; i < expansionJson.length(); i++) {
                        Log.i("JFL", expansionJson.getJSONObject(i).toString());
                        if (!expansionJson.getJSONObject(i).getString("type").equals("Hero")) {
                            url = expansionJson.getJSONObject(i).getString("img");
                            artist = expansionJson.getJSONObject(i).getString("artist");
                            flavor = expansionJson.getJSONObject(i).getString("flavor");
                            cardClass = expansionJson.getJSONObject(i).getString("playerClass");
                            adapter.add(url,"expansion : " + expansion + "\n" + "class : " + cardClass + "\n" + "artist : " + artist + "\n" + "flavor : " + flavor);
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