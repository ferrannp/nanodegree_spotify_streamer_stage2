package com.fnp.spotifystreamerstage2;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.fnp.spotifystreamerstage2.adapters.ArtistArrayAdapter;

import kaaes.spotify.webapi.android.models.Artist;


/**
 * A placeholder fragment containing a simple view.
 */
public class ArtistFragment extends Fragment {

    private ListView mListView;
    private ArtistArrayAdapter mArtistArrayAdapter;
    private EditText mSearchEditText;
    private LinearLayout mWarningView;
    private TextView mWarningTitle, mWarningMessage;
    private ArtistSelectedCallback mArtistSelectedCallback;

    public ArtistFragment() {
    }

    public interface ArtistSelectedCallback{
        void onArtistSelected(String id, String name);
    }

    @Override
    public void onAttach (Activity activity){
        super.onAttach(activity);
        mArtistSelectedCallback = (ArtistSelectedCallback) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_main, container, false);
        mListView = (ListView) v.findViewById(R.id.listview);
        mSearchEditText = (EditText) v.findViewById(R.id.search_edit_text);
        mWarningView = (LinearLayout) v.findViewById(R.id.warning_view);
        mWarningTitle = (TextView) v.findViewById(R.id.warning_title);
        mWarningMessage = (TextView) v.findViewById(R.id.warning_message);

        mArtistArrayAdapter = new ArtistArrayAdapter(getActivity(), R.layout.artist_item);
        if(savedInstanceState != null){
            addArtists(); //We might have already (in case Activity got destroyed)
        }
        mListView.setAdapter(mArtistArrayAdapter);

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mSearchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //This condition will avoid query for the same in cases like a device rotation
                if (!s.toString().equals(MainActivity.getNetworkFragment().getCurrentArtistName())) {
                    MainActivity.getNetworkFragment().searchArtists(s.toString());
                    if (((MainActivity) getActivity()).isTwoPane()) {
                        //If user types while an artist has been selected, we want to clean the
                        //Top tracks panel so it looks consistent
                        ((MainActivity) getActivity()).clearTracks();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Artist artist = mArtistArrayAdapter.getItem(position);

                if (mArtistSelectedCallback != null) { //Tablet
                    mArtistSelectedCallback.onArtistSelected(artist.id, artist.name);
                }
            }
        });
    }

    @Override
    public void onDetach (){
        super.onDetach();
        mArtistSelectedCallback = null;
    }

    private void addArtists(){
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
            mArtistArrayAdapter.addAll(MainActivity.getNetworkFragment().getArtistList());
        }else{
            for(Artist artist:MainActivity.getNetworkFragment().getArtistList()){
                mArtistArrayAdapter.add(artist);
            }
        }
    }

    public void onNetworkSuccess(){

        mArtistArrayAdapter.clear(); //Clear old values
        if(((MainActivity)getActivity()).isTwoPane()){
            mListView.clearChoices(); //Clear choices (if there are)
        }
        addArtists();

        if(mArtistArrayAdapter.getCount() == 0){
            mWarningTitle.setText(String.format(getString(R.string.no_results_found),
                    mSearchEditText.getText().toString()));
            mWarningMessage.setText(getString(R.string.refine_search));
        }

        if(mArtistArrayAdapter.getCount() == 0 && mWarningView.getVisibility() == View.GONE){
            //Good request but no results
            mWarningView.setVisibility(View.VISIBLE);
        }
        else if(mWarningView.getVisibility() == View.VISIBLE && mArtistArrayAdapter.getCount() > 0){
            mWarningView.setVisibility(View.GONE);
        }
    }

    public void onNetworkError(String message){
        mArtistArrayAdapter.clear();
        if(((MainActivity)getActivity()).isTwoPane()){
            mListView.clearChoices(); //Clear choices (if there are)
        }

        if(mSearchEditText.length() > 0){
            mWarningTitle.setText(String.format(getString(R.string.no_results_found),
                    mSearchEditText.getText().toString()));
            mWarningMessage.setText(message);

            if (mWarningView.getVisibility() == View.GONE) {
                mWarningView.setVisibility(View.VISIBLE);
            }
        }else if(mWarningView.getVisibility() == View.VISIBLE){
            mWarningView.setVisibility(View.GONE);
        }
    }
}
