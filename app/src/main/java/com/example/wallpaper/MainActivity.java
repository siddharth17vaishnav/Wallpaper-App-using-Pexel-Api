package com.example.wallpaper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.EditText;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.Permission;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    SwipeRefreshLayout refresh;
    RecyclerView recyclerView;
    WallpaperAdapter wallpaperAdapter;
    List<WallpaperModel> wallpaperModelList;
    int pagenumber=1;
    Boolean isScrolling=false;
    int currentItems,totalItems,scrollOutItems;
    String url ="https://api.pexels.com/v1/curated/?page="+pagenumber+"&per_page=85";
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        refresh=findViewById(R.id.refresh);
        progressDialog=new ProgressDialog(this);
        recyclerView=findViewById(R.id.recycleview);
        wallpaperModelList=new ArrayList<>();
        wallpaperAdapter=new WallpaperAdapter(this,wallpaperModelList);

        recyclerView.setAdapter(wallpaperAdapter);
        final GridLayoutManager gridLayoutManager=new GridLayoutManager(this,2);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL){
                    isScrolling=true;
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                currentItems=gridLayoutManager.getChildCount();
                totalItems=gridLayoutManager.getItemCount();
                scrollOutItems=gridLayoutManager.findFirstCompletelyVisibleItemPosition();

                if (isScrolling && (currentItems+scrollOutItems==totalItems)){
                    isScrolling=false;

                    fetchwallpaper();

                }
            }
        });
        fetchwallpaper();

        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchwallpaper();
                refresh.setRefreshing(false);

            }
        });

    }
    public void fetchwallpaper(){
        progressDialog.setMessage("Loading");
        progressDialog.setCancelable(false);

        progressDialog.show();
        StringRequest request= new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject jsonObject=new JSONObject(response);
                            JSONArray jsonArray=jsonObject.getJSONArray("photos");
                            int length=jsonArray.length();
                            for(int i=0;i<length;i++){
                                JSONObject object=jsonArray.getJSONObject(i);
                                int id=object.getInt("id");

                                JSONObject objectImages=object.getJSONObject("src");
                                String originalUrl=objectImages.getString("original");
                                String mediumUrl=objectImages.getString("medium");

                                WallpaperModel wallpaperModel=new WallpaperModel(id,originalUrl,mediumUrl);
                                wallpaperModelList.add(wallpaperModel);
                            }
                            wallpaperAdapter.notifyDataSetChanged();
                            pagenumber++;

                        } catch (JSONException e) {
                            progressDialog.dismiss();
                        }
                        progressDialog.dismiss();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params =new HashMap<>();
                params.put("Authorization","563492ad6f917000010000011cc5e9f3bc7f495c91ed34fe5bf2dade");
                return params;
            }
        };

        RequestQueue requestQueue= Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(request);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId()==R.id.nav_search){

            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            final EditText editText = new EditText(this);
            editText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

            alert.setMessage("Enter Category e.g. Nature");
            alert.setTitle("Search Wallpaper");

            alert.setView(editText);

            alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                    String query = editText.getText().toString().toLowerCase();

                    url = "https://api.pexels.com/v1/search/?page="+pagenumber+"&per_page=80&query="+query;
                    wallpaperModelList.clear();
                    pagenumber++;
                    fetchwallpaper();

                }
            });

            alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });

            alert.show();



        }

        return super.onOptionsItemSelected(item);
    }
}