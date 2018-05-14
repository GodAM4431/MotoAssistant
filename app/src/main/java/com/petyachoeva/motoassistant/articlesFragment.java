package com.petyachoeva.motoassistant;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import static com.google.android.gms.internal.zzs.TAG;

public class articlesFragment extends Fragment {

    ListView listViewArticles;
    FirebaseDatabase database;
    DatabaseReference ref;
    ArrayList<Article> dataForTheAdapter = new ArrayList<Article>();
    ArrayList<String> listArticlesNames;
    ArrayAdapter<String> adapter;
    Article article;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_articles, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.getActivity().setTitle("Useful articles");

        article = new Article();
        listViewArticles = (ListView) view.findViewById(R.id.ListViewArticles);
        database = FirebaseDatabase.getInstance();
        ref = database.getReference("articles");
        listArticlesNames = new ArrayList<>();

        adapter = new ArrayAdapter<String>(getContext(),android.R.layout.simple_list_item_1, listArticlesNames);

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot ds: dataSnapshot.getChildren()) {

                    article = ds.getValue(Article.class);
                    dataForTheAdapter.add(article);

                    listArticlesNames.add(article.getName());
                }
                listViewArticles.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "Failed to read value.", databaseError.toException());
            }
        });

        listViewArticles.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String url = dataForTheAdapter.get(position).getUrl();
                Uri uri = Uri.parse(url);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
    }
}
