package com.example.toppostsreddit;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.provider.MediaStore;
import android.view.View;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.toppostsreddit.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSION = 1;
    private static final String[] PERMISSIONS = {android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private GridView gridView;
    private RedditPostAdapter adapter;
    private ArrayList<RedditPost> posts;
    private int currentPage = 1;
    private int totalPosts = 0;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gridView = findViewById(R.id.grid_view);
        adapter = new RedditPostAdapter(this);
        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                RedditPost post = (RedditPost) adapter.getItem(position);
                if (post != null && post.getImageUrl() != null) {
                    openImage(post.getImageUrl());
                }
            }
        });

        setOnScrollListener();

        if (savedInstanceState != null) {
            currentPage = savedInstanceState.getInt("currentPage");
            posts = savedInstanceState.<RedditPost>getParcelableArrayList("posts");
            if (posts != null) {
                adapter.addPosts(posts);
            }
        } else {
            loadPosts();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("currentPage", currentPage);
        outState.putParcelableArrayList("posts", posts);
    }

    @SuppressLint("StaticFieldLeak")
    private void loadPosts() {
        new AsyncTask<Void, Void, List<RedditPost>>() {

            @Override
            protected List<RedditPost> doInBackground(Void... voids) {
                List<RedditPost> posts = new ArrayList<>();

                try {
                    URL url = new URL("https://www.reddit.com/r/programming/top.json?t=day");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");

                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        InputStream inputStream = connection.getInputStream();
                        String response = convertInputStreamToString(inputStream);

                        JSONObject jsonObject = new JSONObject(response);
                        JSONObject dataObject = jsonObject.getJSONObject("data");
                        JSONArray childrenArray = dataObject.getJSONArray("children");

                        for (int i = 0; i < childrenArray.length(); i++) {
                            JSONObject postObject = childrenArray.getJSONObject(i).getJSONObject("data");
                            String author = postObject.getString("author");
                            String created = postObject.getString("created_utc");
                            String thumbnail = postObject.getString("thumbnail");
                            int numComments = postObject.getInt("num_comments");

                            RedditPost post = new RedditPost(author, created, thumbnail, numComments);
                            posts.add(post);
                        }
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }

                return posts;
            }

            @Override
            protected void onPostExecute(List<RedditPost> posts) {
                adapter.addPosts(posts);
            }
        }.execute();
    }

    private String convertInputStreamToString(InputStream inputStream) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            stringBuilder.append(new String(buffer, 0, bytesRead));
        }
        return stringBuilder.toString();
    }


    private void parseResponse(String response) throws JSONException {
        JSONObject jsonResponse = new JSONObject(response);
        JSONObject dataObject = jsonResponse.getJSONObject("data");
        JSONArray childrenArray = dataObject.getJSONArray("children");

        totalPosts = dataObject.getInt("total_posts");

        ArrayList<RedditPost> newPosts = new ArrayList<>();

        for (int i = 0; i < childrenArray.length(); i++) {
            JSONObject childObject = childrenArray.getJSONObject(i);
            JSONObject postDataObject = childObject.getJSONObject("data");

            String author = postDataObject.getString("author");
            String timestamp = postDataObject.getString("created_utc");
            String thumbnail = postDataObject.optString("thumbnail");
            int commentCount = postDataObject.getInt("num_comments");

            RedditPost post = new RedditPost(author, timestamp, thumbnail, commentCount);
            newPosts.add(post);
        }

        posts = newPosts;
        adapter.addPosts(posts);
    }

    private void setOnScrollListener() {
        gridView.setOnScrollListener(new EndlessScrollListener() {
            @Override
            public boolean onLoadMore(int page, int totalItemsCount) {
                if (totalItemsCount < totalPosts) {
                    currentPage++;
                    loadPosts();
                    return true;
                } else {
                    return false;
                }
            }
        });
    }

    private void openImage(String imageUrl) {
        Uri imageUri = Uri.parse(imageUrl);
        Intent intent = new Intent(Intent.ACTION_VIEW, imageUri);
        startActivity(intent);
    }

    private String performApiRequest(String url) {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String response = null;

        try {
            URL apiUrl = new URL(url);
            urlConnection = (HttpURLConnection) apiUrl.openConnection();
            urlConnection.setRequestMethod("GET");

            int responseCode = urlConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream inputStream = urlConnection.getInputStream();
                if (inputStream != null) {
                    StringBuilder stringBuilder = new StringBuilder();
                    reader = new BufferedReader(new InputStreamReader(inputStream));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        stringBuilder.append(line);
                    }
                    response = stringBuilder.toString();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return response;

    }

    private abstract class EndlessScrollListener implements GridView.OnScrollListener {
        private int visibleThreshold = 5;
        private int currentPage = 0;
        private int previousTotalItemCount = 0;
        private boolean loading = true;

        public EndlessScrollListener() {
        }

        public EndlessScrollListener(int visibleThreshold) {
            this.visibleThreshold = visibleThreshold;
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            if (loading && totalItemCount > previousTotalItemCount) {
                loading = false;
                previousTotalItemCount = totalItemCount;
            }

            if (!loading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {
                currentPage++;
                loading = onLoadMore(currentPage, totalItemCount);
            }
        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
        }

        public abstract boolean onLoadMore(int page, int totalItemsCount);
    }

    private void saveImageToGallery(Bitmap bitmap) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_PERMISSION);
        } else {
            String savedImageURL = MediaStore.Images.Media.insertImage(
                    getContentResolver(),
                    bitmap,
                    "Reddit_Image",
                    "Image saved from Reddit"
            );

            if (savedImageURL != null) {
                Toast.makeText(this, "Image saved to gallery", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                saveImageToGallery(bitmap);
            } else {
                Toast.makeText(this, "Permission denied. Cannot save image.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}