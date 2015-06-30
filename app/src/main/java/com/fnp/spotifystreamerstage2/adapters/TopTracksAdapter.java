package com.fnp.spotifystreamerstage2.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.fnp.spotifystreamerstage2.R;
import com.squareup.picasso.Picasso;

import kaaes.spotify.webapi.android.models.Track;

public class TopTracksAdapter extends ArrayAdapter<Track> {

    private int resId;

    public TopTracksAdapter(Context context, int resId) {
        super(context, resId);
        this.resId = resId;
    }

    public static class ViewHolder{
        public final ImageView albumImageView;
        public final TextView albumTextView;
        public final TextView trackTextView;

        public ViewHolder (View view){
            albumImageView = (ImageView) view.findViewById(R.id.album_imageview);
            albumTextView = (TextView) view.findViewById(R.id.album_textview);
            trackTextView = (TextView) view.findViewById(R.id.track_textview);
        }
    }

    public View getView(int position, View convertView, final ViewGroup parent) {

        ViewHolder viewHolder;
        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(resId, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Track track = getItem(position);

        if (track.album.images.size() > 0) {
            String imageUrl;
            //Try to load medium image if not, the first available
            imageUrl = (track.album.images.size() > 1) ? track.album.images.get(1).url
                    : track.album.images.get(0).url;

            Picasso.with(getContext())
                    .load(imageUrl)
                    .resizeDimen(R.dimen.cover_size, R.dimen.cover_size)
                    .into(viewHolder.albumImageView);
        }else{ //Placeholder if no image
            viewHolder.albumImageView.setImageDrawable(getContext()
                    .getResources().getDrawable(R.drawable.ic_album_white_36dp));
        }

        viewHolder.albumTextView.setText(track.album.name);
        viewHolder.trackTextView.setText(track.name);

        return convertView;
    }
}
