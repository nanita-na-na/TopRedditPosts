package com.example.toppostsreddit;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class RedditPostAdapter extends BaseAdapter {
    private Context context;
    private List<RedditPost> posts;

    public RedditPostAdapter(MainActivity mainActivity) {

    }

    @Override
    public int getCount() {
        return posts.size();
    }

    @Override
    public Object getItem(int position) {
        return posts.get(position);
    }

    @Override
    public long getItemId(int position) {
        return posts.get(position).getNumComments();
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;

        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.content_main, null);
        }

        RedditPost post = posts.get(position);

        TextView authorTextView = view.findViewById(R.id.result_info);
        TextView timeAgoTextView = view.findViewById(R.id.result_info);
        ImageView thumbnailImageView = view.findViewById(R.id.result_info);
        TextView commentsTextView = view.findViewById(R.id.result_info);

        authorTextView.setText(post.getAuthor());
        timeAgoTextView.setText(post.getTimeAgo());
        commentsTextView.setText(String.valueOf(post.getNumComments()));

        String thumbnailUrl = post.getThumbnailUrl();
        if (thumbnailUrl != null && !thumbnailUrl.isEmpty()) {
            new DownloadImageTask(thumbnailImageView).execute(thumbnailUrl);
        } else {
            thumbnailImageView.setImageResource(R.drawable.ic_launcher_background);
        }
        return view;
    }

    public void addPosts(List<RedditPost> newPosts) {
        posts.addAll(newPosts);
        notifyDataSetChanged();
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

        private ImageView imageView;
        public DownloadImageTask(ImageView imageView) {
            this.imageView = imageView;
        }

        @Override
        protected Bitmap doInBackground(String... urls) {
            String imageUrl = urls[0];
            Bitmap bitmap = null;
            try {
                URL url = new URL(imageUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream input = connection.getInputStream();
                bitmap = BitmapFactory.decodeStream(input);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
            } else {
                imageView.setImageResource(R.drawable.ic_launcher_background);
            }
        }
    }
}
