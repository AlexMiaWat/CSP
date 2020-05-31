package russianapp.centralserviceportal.administrative;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.internal.NavigationMenu;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import russianapp.centralserviceportal.administrative.csp.CentralServicePortalManager;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    CentralServicePortalManager cspMng;
    DrawerLayout drawer;
    public NavigationView navigationView;
    Toolbar toolbar;
    public WebView webView;
    public TextView timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Central Service Portal
        String provider = "http://rusappclub.sytes.net:8090";
        String application = "CSP";
        cspMng = new CentralServicePortalManager(this, provider, application);

        webView = findViewById(R.id.webView);
        webView.loadUrl(provider);
        timer = findViewById(R.id.timer);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view ->
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show());

        // Панель инструментов
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Меню контейнер
        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // Меню объект
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Меню шапка
        View headerView = findViewById(R.id.header_view);
        headerView.findViewById(R.id.imageView).setOnClickListener(v -> {
            webView.loadUrl(provider);
            drawer.closeDrawer(GravityCompat.START);});
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        Menu menu = navigationView.getMenu();
        menu.clear();
        navigationView.inflateMenu(R.menu.activity_main_drawer);
        navigationView.invalidate();

        menu = navigationView.getMenu();
        menu.getItem(((NavigationMenu) menu).findItemIndex(item.getItemId())).setCheckable(true);
        menu.getItem(((NavigationMenu) menu).findItemIndex(item.getItemId())).setChecked(true);

        switch (item.getItemId()){
            case R.id.nav_firstConnection: {
                cspMng.doServiceTask("firstConnection");
                break;

            } case R.id.nav_gallery: {

                break;
            } default: {
                // do nothing
            }
        }

        drawer.closeDrawer(GravityCompat.START);

        return false;
    }
}
