package silio.pokedex;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import me.sargunvohra.lib.pokekotlin.client.PokeApi;
import me.sargunvohra.lib.pokekotlin.client.PokeApiClient;
import me.sargunvohra.lib.pokekotlin.model.Pokemon;
import me.sargunvohra.lib.pokekotlin.model.PokemonSpecies;
import me.sargunvohra.lib.pokekotlin.model.PokemonType;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    private RecyclerView mRecyclerView;
    private PokemonCardAdapter mAdapter;
    private GridLayoutManager mLayoutManager;
    // could be put in a sort of Utils class, that'd be nice
    private final String baseURL = "http://pokeapi.co/api/v2/";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* Auto generated */

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        /* Auto generated end*/

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRecyclerView.smoothScrollToPosition(0);

            }
        });

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        // true for now, don't feel like using it tho
        //mRecyclerView.setHasFixedSize(true);

        // "good" enough way of solving portrait orientation problems
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
            mLayoutManager = new GridLayoutManager(this, 2);
        else
            mLayoutManager = new GridLayoutManager(this, 3);

        mRecyclerView.setLayoutManager(mLayoutManager);


        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int visibility = (mLayoutManager.findFirstCompletelyVisibleItemPosition() != 0) ? View.VISIBLE : View.GONE;
                fab.setVisibility(visibility);
            }
        });

        // Get Pokémons

        try {
            new RequestPokemonCardsTask().execute();
        } catch(Exception e) {
        }

        //testPokemon.add(new PokemonCard(1,"Charmander","Fire"));
        //testPokemon.add(new PokemonCard(2,"Charizard","Fire","Flying"));
        // don't wanna jinx it hahaha
        //testPokemon.add(new PokemonCard(3,"Jynx","Ice","Psychic"));

        // specify an adapter (see also next example)
        //^adapter is specified in RequestPokemonCardsTask^

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    public void setFloatingActionButton(final View view) {
        FloatingActionButton actionButton = (android.support.design.widget.FloatingActionButton) view.findViewById(R.id.fab);
        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayoutManager layoutManager = (LinearLayoutManager) mRecyclerView
                        .getLayoutManager();
                layoutManager.scrollToPositionWithOffset(0, 0);
            }
        });
    }


    // ActionBar Stuff

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        final MenuItem searchViewItem = menu.findItem(R.id.action_search);
        if (searchViewItem != null) {
            tintMenuIcon(MainActivity.this, searchViewItem, android.R.color.white);
        }

        final SearchView searchViewAndroidActionBar = (SearchView) MenuItemCompat.getActionView(searchViewItem);
        searchViewAndroidActionBar.setMaxWidth( Integer.MAX_VALUE );

        searchViewAndroidActionBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                searchViewAndroidActionBar.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                if ( TextUtils.isEmpty ( newText ) ) {
                    mAdapter.getFilter().filter("");
                } else {
                    mAdapter.getFilter().filter(newText);
                }
                return true;
            }

        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_search) {

            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    // Easy way to bypass Theme shenanigans
    public static void tintMenuIcon(Context context, MenuItem item, @ColorRes int color) {
        Drawable normalDrawable = item.getIcon();
        Drawable wrapDrawable = DrawableCompat.wrap(normalDrawable);
        DrawableCompat.setTint(wrapDrawable, context.getResources().getColor(color));

        item.setIcon(wrapDrawable);
    }

    // NavBar Stuff

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    // gets card info from @assets/pokemon_cards.json
    public String loadJSONFromAsset() {
        String json = null;
        try {

            InputStream is = getAssets().open("pokemon_cards.json");

            int size = is.available();

            byte[] buffer = new byte[size];

            is.read(buffer);

            is.close();

            json = new String(buffer, "UTF-8");


        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;

    }

    // gets first 151 pokemon card info from file
    // not in separate class 'cause private references are useful
    private class RequestPokemonCardsTask extends AsyncTask<Void, Integer, List<PokemonCard>> {

        List<PokemonCard> pokemonCardList = new ArrayList<>();

        protected List<PokemonCard> doInBackground(Void... params) {

            try {
                JSONArray pokemonCardArray = new JSONArray(loadJSONFromAsset());

                for (int i=0; i< pokemonCardArray.length(); i++) {
                    JSONObject pokemonCard = (JSONObject) pokemonCardArray.get(i);
                    int id = pokemonCard.getInt("id");
                    String name = pokemonCard.getString("name");
                    JSONArray types = pokemonCard.getJSONArray("types");
                    String primaryType = types.getString(0);
                    String secondaryType;
                    Uri spriteURI = Uri.parse(pokemonCard.getString("spriteURI"));
                    if(types.length() > 1 ) {
                        secondaryType = types.getString(1);
                        pokemonCardList.add( new PokemonCard(id, name,  secondaryType, primaryType, spriteURI));
                    }
                    else
                        pokemonCardList.add( new PokemonCard(id, name, primaryType, spriteURI));
                }

            }
            catch (JSONException e){
                e.printStackTrace();
            }
            return pokemonCardList;
        }

        @Override
        protected void onPostExecute(List<PokemonCard> pokemonCardList) {
            super.onPostExecute(pokemonCardList);
            mAdapter = new PokemonCardAdapter(pokemonCardList);
            mRecyclerView.setAdapter(mAdapter);
        }
    }

}
