package android.teste.myplaces.Controller;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.teste.myplaces.R;
import android.content.Intent;
import android.os.Bundle;
import android.teste.myplaces.Util.SingletonDatabase;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.map_act_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent it;
        switch (item.getItemId()) {
            case R.id.optMapBack:
                finish();
                return true;
            case R.id.optMapManage:
                it = new Intent(this, ManageActivity.class);
                startActivityForResult(it, 2);// precisará atualizar o mapa na volta (devido a possíveis alterações no BD)
                return true;
            case R.id.optMapReport:
                it = new Intent(this, ReportActivity.class);
                startActivity(it);
                return true;
            case R.id.mapTypeNormal:
                if(map != null)
                    map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                return true;
            case R.id.mapTypeHybrid:
                if(map != null)
                    map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map  = googleMap;
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        if(checkPermission())
            map.setMyLocationEnabled(true);

        LatLng newLoc = new LatLng(getIntent().getDoubleExtra("lat", 0), getIntent().getDoubleExtra("lng", 0));
        CameraUpdate upd = CameraUpdateFactory.newLatLngZoom(newLoc, 15);
        map.animateCamera(upd);

        map.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {

                LinearLayout info = new LinearLayout(getBaseContext());
                info.setOrientation(LinearLayout.VERTICAL);

                TextView title = new TextView(getBaseContext());
                title.setTextColor(Color.BLACK);
                title.setGravity(Gravity.CENTER);
                title.setTypeface(null, Typeface.BOLD);
                title.setText(marker.getTitle());

                TextView snippet = new TextView(getBaseContext());
                snippet.setTextColor(Color.GRAY);
                snippet.setText(marker.getSnippet());

                info.addView(title);
                info.addView(snippet);

                return info;
            }
        });

        try {
            Cursor c = SingletonDatabase.getInstance().buscar("Checkin", new String[]{"Local", "qtdVisitas", "cat", "latitude", "longitude"}, "", "");
            while(c.moveToNext()){
                String local = c.getString(c.getColumnIndex("Local"));
                String categ = c.getString(c.getColumnIndex("cat"));
                int qtd = c.getInt(c.getColumnIndex("qtdVisitas"));
                double lat = Double.parseDouble(c.getString(c.getColumnIndex("latitude")));
                double lng = Double.parseDouble(c.getString(c.getColumnIndex("longitude")));

                LatLng pos = new LatLng(lat, lng);

                map.addMarker(new MarkerOptions().position(pos).title(local).snippet(String.format(getResources().getString(R.string.markerSnippet), categ, qtd)));
            }
            c.close();
        } catch (Exception e){
            Toast.makeText(getBaseContext(), getResources().getString(R.string.toastError), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent it) {
        super.onActivityResult(requestCode, resultCode, it);
        if(resultCode == 2) {// está voltando da ManageActivity
            try{
                map.clear();// limpa os marcadores para adicionar novamente
                Cursor c = SingletonDatabase.getInstance().buscar("Checkin", new String[]{"Local", "qtdVisitas", "cat", "latitude", "longitude"}, "", "");
                while(c.moveToNext()){
                    String local = c.getString(c.getColumnIndex("Local"));
                    String categ = c.getString(c.getColumnIndex("cat"));
                    int qtd = c.getInt(c.getColumnIndex("qtdVisitas"));
                    double lat = Double.parseDouble(c.getString(c.getColumnIndex("latitude")));
                    double lng = Double.parseDouble(c.getString(c.getColumnIndex("longitude")));

                    LatLng pos = new LatLng(lat, lng);

                    map.addMarker(new MarkerOptions().position(pos).title(local).snippet(String.format(getResources().getString(R.string.markerSnippet), categ, qtd)));
                }
                c.close();

                Intent itFromMain = getIntent();
                setResult(2, itFromMain);// seta o resultado para a main_activity saber que o BD pode ter sido alterado

            } catch (Exception e){
                Toast.makeText(getBaseContext(), getResources().getString(R.string.toastError), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public boolean checkPermission() {
        int FirstPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION);
        int SecondPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION);

        return FirstPermissionResult == PackageManager.PERMISSION_GRANTED &&
                SecondPermissionResult == PackageManager.PERMISSION_GRANTED;
    }

}
