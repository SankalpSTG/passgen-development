package com.smartn.passgen;


import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.core.view.MenuItemCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class Dashboard extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    Database DB;
    ListView listView;
    ArrayList<ListItem> newsItemList;
    ListAdapter adapter;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        //Databse code
        DB=new Database(this);

        //drawer code
        drawerLayout = (DrawerLayout) findViewById(R.id.dashbord);
        toggle=new ActionBarDrawerToggle(this, drawerLayout ,R.string.opend,R.string.closed);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //Navigation
        NavigationView nView=(NavigationView)findViewById(R.id.nav_view);
        nView.setNavigationItemSelectedListener(this);

        //floating add button
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i=new Intent(Dashboard.this,AddPassword.class);
                startActivity(i);
            }
        });

        refresh();

        /*//listview
        newsItemList = new ArrayList<>();
        listView = (ListView) findViewById(R.id.user_list);

        Cursor res = DB.getAllDataPassword();
        if (res.getCount() == 0) {
            Toast.makeText(Dashbord.this, "No data found", Toast.LENGTH_SHORT).show();
            return;
        }
        while (res.moveToNext()) {
            String website,username,password;
            website= res.getString(0);
            username= res.getString(1);
            password = res.getString(2);
            ListItem news = new ListItem();
            news.website = website;
            news.username = username;
            news.password = password;
            newsItemList.add(news);
        }
        adapter = new ListAdapter(Dashbord.this, newsItemList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListItem currentNews = newsItemList.get(position);
                Intent intent = new Intent(Dashbord.this, UpdatePassword.class);
                intent.putExtra("putitem", currentNews);
                startActivity(intent);
            }
        });*/

    }
    public void refresh()
    {
        //server all values
        //server code
        Cursor res = DB.getAllDataMaster();
        res.moveToFirst();
        String unicid = res.getString(0);
        Log.d("Dashbord","getting unic id from databse "+unicid);
        String auth=RandomString.getAlphaNumericString(10);
        String device = Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ANDROID_ID);
        Call<ResponseBody> call= RetrofitClient.getInstance()
                .getApiPassword()
                .search_user(unicid,auth,device);
        Log.d("Add Password","Getting Respons");
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.d("Android","onresponse method");
                String respo = "";
                try {
                    newsItemList = new ArrayList<>();
                    listView = (ListView) findViewById(R.id.user_list);

                    respo = response.body().string();
                    JSONObject respoJ = new JSONObject(respo);
                    JSONArray ja_data = respoJ.getJSONArray("data");
                    int length = ja_data.length();
                    for(int i=0; i < length ; i++){
                        try {
                            JSONObject jsonObj = ja_data.getJSONObject(i);
                            String name=jsonObj.getString("name");
                            String pass=jsonObj.getString("password");
                            String url=jsonObj.getString("url");
                            ListItem news = new ListItem();
                            news.website = url;
                            news.username = name;
                            news.password = pass;
                            newsItemList.add(news);
                            Log.d("Dashboard", "Json Data : " + name+"\n"+pass+"\n"+url);
                        }
                        catch (ArrayIndexOutOfBoundsException e)
                        {
                            length--;
                        }
                    }
                    adapter = new ListAdapter(Dashboard.this, newsItemList);
                    listView.setAdapter(adapter);
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            ListItem currentNews = newsItemList.get(position);

                            Intent intent = new Intent(Dashboard.this, UpdatePassword.class);
                            intent.putExtra("putitem", currentNews);
                            startActivity(intent);
                            finish();
                        }
                    });

                    /*int code = respoJ.getInt("error_code");
                    String message = respoJ.getString("message");
                    String data=respoJ.getString("data");
                    if(code==100) {
                    }*/
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(getApplicationContext(),
                        "Registration Failed!!!",
                        Toast.LENGTH_SHORT).show();
                return;
            }
        });

    }



    //searchbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.menu,menu);

        MenuItem searchItem =menu.findItem(R.id.item_search);
        SearchView searchView =(SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                newsItemList.clear();
                //server all values
                //server code
                Cursor res = DB.getAllDataMaster();
                res.moveToFirst();
                String unicid = res.getString(0);
                Log.d("Dashbord","getting unic id from databse "+unicid);
                String auth=RandomString.getAlphaNumericString(10);
                String device = Settings.Secure.getString(getContentResolver(),
                        Settings.Secure.ANDROID_ID);
                Call<ResponseBody> call= RetrofitClient.getInstance()
                        .getApiPassword()
                        .search_user_url(unicid,auth,device,newText);
                Log.d("Add Password","Getting Respons");
                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        Log.d("Android","onresponse method");
                        String respo = "";
                        try {
                            newsItemList = new ArrayList<>();
                            listView = (ListView) findViewById(R.id.user_list);

                            respo = response.body().string();
                            JSONObject respoJ = new JSONObject(respo);
                            JSONArray ja_data = respoJ.getJSONArray("data");
                            int length = ja_data.length();
                            for(int i=0; i < length ; i++){
                                try {
                                    JSONObject jsonObj = ja_data.getJSONObject(i);
                                    String name=jsonObj.getString("name");
                                    String pass=jsonObj.getString("password");
                                    String url=jsonObj.getString("url");
                                    ListItem news = new ListItem();
                                    news.website = url;
                                    news.username = name;
                                    news.password = pass;
                                    newsItemList.add(news);
                                    Log.d("Dashboard", "Json Data : " + name+"\n"+pass+"\n"+url);
                                }
                                catch (ArrayIndexOutOfBoundsException e)
                                {
                                    length--;
                                }
                            }
                            adapter = new ListAdapter(Dashboard.this, newsItemList);
                            listView.setAdapter(adapter);
                            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    ListItem currentNews = newsItemList.get(position);

                                    Intent intent = new Intent(Dashboard.this, UpdatePassword.class);
                                    intent.putExtra("putitem", currentNews);
                                    startActivity(intent);
                                    finish();
                                }
                            });

                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Toast.makeText(getApplicationContext(),
                                "Registration Failed!!!",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                });



                adapter = new ListAdapter(Dashboard.this, newsItemList);
                listView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                return true;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    //cpoy to clipbord
    public void copyClipbord(View view)
    {
        // Creates a new text clip to put on the clipboard
        final android.content.ClipboardManager clipboardManager = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("Source Text", "Hello mayuresh");
        clipboardManager.setPrimaryClip(clipData);
        Toast.makeText(getApplicationContext(),
                "Password Copied",
                Toast.LENGTH_SHORT).show();

    }

    //lifeCycle methods
//    @Override
//    public void onBackPressed() {
//        finishAffinity();
//        System.exit(0);
//    }

    //toggle code
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if(toggle.onOptionsItemSelected(item))
        {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //Navigation drawer action
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id=item.getItemId();
        if (id == R.id.dashbord) {
        } else if (id == R.id.scanweb) {
            Intent i=new Intent(Dashboard.this,ScanCodeActivity.class);
            startActivity(i);
        } else if (id == R.id.settings) {
            Intent i=new Intent(Dashboard.this,SettingActivity.class);
            startActivity(i);
        }else if (id== R.id.logout){
            DB.deleteMasterPassword();
            Intent i=new Intent(Dashboard.this,SignIn.class);
            startActivity(i);
        } else if (id == R.id.help) {
            Intent i=new Intent(Dashboard.this,IntroActivity.class);
            startActivity(i);
        } else if (id == R.id.about) {
            //Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto","agamyaguitarclass@gmail.com", null));
            //emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Song Request");
            //emailIntent.putExtra(Intent.EXTRA_TEXT, "Song Name: ");
            //startActivity(Intent.createChooser(emailIntent, "Sending email..."));
        }
        //overridePendingTransition(R.anim.pull_in_right, R.anim.pull_in_left);
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}
