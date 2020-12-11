package android.teste.myplaces.Controller;

import android.content.ContentValues;
import android.database.Cursor;
import android.location.Criteria;
import android.teste.myplaces.R;
import android.teste.myplaces.Util.SingletonDatabase;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LocationListener {

    public LocationManager lm;
    public Criteria criteria;
    public String provider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        configuraCriterioLocation();

        try{
            // configurando o autocomplete do campo de nome do local
            List<String> places = new ArrayList<>();
            Cursor c = SingletonDatabase.getInstance().buscar("Checkin", new String[]{"Local"},"","");
            while(c.moveToNext()){
                int name = c.getColumnIndex("Local");
                places.add(c.getString(name));
            }
            c.close();

            AutoCompleteTextView tv = (AutoCompleteTextView) findViewById(R.id.edtName);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.my_dropdown, R.id.dropdownTxt, places);
            adapter.setDropDownViewResource(R.layout.my_dropdown);
            tv.setAdapter(adapter);

            // configurando o dropdown para categoria do local
            java.util.ArrayList<String> categs = new java.util.ArrayList<>();

            c = SingletonDatabase.getInstance().buscar("Categoria", new String[]{"nome"},"","");
            while(c.moveToNext()){
                int name = c.getColumnIndex("nome");
                categs.add(c.getString(name));
            }
            c.close();

            Spinner s = findViewById(R.id.edtCateg);
            adapter = new ArrayAdapter<String>(this, R.layout.my_dropdown, R.id.dropdownTxt, categs);
            adapter.setDropDownViewResource(R.layout.my_dropdown);
            s.setAdapter(adapter);
            s.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                }
                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                }
            });

        } catch (Exception e){
            Toast.makeText(getBaseContext(), getResources().getString(R.string.toastError), Toast.LENGTH_SHORT).show();
        }

        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        requestLocationPermissionIfNeeded();
    }

    public void checkin(View v){
        if(!checkPermission()){ // verifica se as permissoes ainda nao foram dadas
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);// pede as permissoes e chama onRequestPermissionsResult como callback
            return;
        } else {
            EditText nam = findViewById(R.id.edtName);
            Spinner cat = findViewById(R.id.edtCateg);

            String placeName = nam.getText().toString();
            String placeCateg = cat.getSelectedItem().toString();

            TextView latTxt = findViewById(R.id.outPosLat);
            TextView longTxt = findViewById(R.id.outPosLong);
            String lat = latTxt.getText().toString();
            String lng = longTxt.getText().toString();

            if(!placeName.equals("") && !placeCateg.equals("")){
                try{
                    Cursor c = SingletonDatabase.getInstance().buscar("Checkin", new String[]{"qtdVisitas"}, "Local = '"+ placeName +"'", "");

                    if(c.getCount() == 0){ // se o local ja existe no banco de dados
                        if(lat.equals(getResources().getString(R.string.latLngUndefined)) || lng.equals(getResources().getString(R.string.latLngUndefined))){ // se a localizacao do usuario ainda nao foi obtida
                            Toast.makeText(getBaseContext(), getResources().getString(R.string.toastLocationNotDef), Toast.LENGTH_LONG).show();
                            return;
                        }

                        ContentValues valores = new ContentValues();
                        valores.put("Local", placeName);
                        valores.put("qtdVisitas", 1);
                        valores.put("cat", placeCateg);
                        valores.put("latitude", lat);
                        valores.put("longitude", lng);
                        SingletonDatabase.getInstance().inserir("Checkin", valores);

                    }else{

                        c.moveToNext();
                        int qtdIdx = c.getColumnIndex("qtdVisitas");
                        int qtd = c.getInt(qtdIdx);
                        ContentValues valores = new ContentValues();
                        valores.put("qtdVisitas", qtd+1);
                        SingletonDatabase.getInstance().atualizar("Checkin", valores, "Local = '"+ placeName +"'");

                    }
                    c.close();

                    nam.setText("");
                    cat.setSelection(0);

                    recreate();
                    Toast.makeText(getBaseContext(), getResources().getString(R.string.toastChangesSuccess), Toast.LENGTH_SHORT).show();

                } catch (Exception e){
                    Toast.makeText(getBaseContext(), getResources().getString(R.string.toastError), Toast.LENGTH_SHORT).show();
                }
                return;

            } else {
                Toast.makeText(getBaseContext(), getResources().getString(R.string.toastFillFields), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public boolean checkPermission() {
        int FirstPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION);
        int SecondPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION);

        return FirstPermissionResult == PackageManager.PERMISSION_GRANTED &&
                SecondPermissionResult == PackageManager.PERMISSION_GRANTED;
    }

    public void requestLocationPermissionIfNeeded(){
        if(!checkPermission()){ // verifica se as permissoes ainda nao foram dadas
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);// pede as permissoes e chama onRequestPermissionsResult como callback
        } else {
            provider = lm.getBestProvider(criteria, true);
            if (provider == null) {
                //
            } else {
                lm.requestLocationUpdates(provider, 5000, 0, (LocationListener) this);// configura para atualizar a localização do usuário a cada 5 segundos
            }
        }
    }

    public void configuraCriterioLocation() {
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        criteria = new Criteria();

        PackageManager packageManager = getPackageManager();
        boolean hasGPS = packageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS);

        if (hasGPS) {
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
        } else {
            criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_act_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent it;
        switch (item.getItemId()) { // verifica qual item do menu foi clicado
            case R.id.optMainMap:
                if(!checkPermission()){ // verifica se as permissoes ainda nao foram dadas
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);// pede as permissoes e chama onRequestPermissionsResult como callback
                    return false;
                }

                TextView latTxt = findViewById(R.id.outPosLat);
                TextView longTxt = findViewById(R.id.outPosLong);

                if(!latTxt.getText().toString().equals(getResources().getString(R.string.latLngUndefined)) && !longTxt.getText().toString().equals(getResources().getString(R.string.latLngUndefined))){ // se a localizacao do usuario ainda nao foi obtida
                    double lat = Double.parseDouble(latTxt.getText().toString());
                    double lng = Double.parseDouble(longTxt.getText().toString());
                    it = new Intent(this, MapActivity.class);
                    it.putExtra("lat", lat);
                    it.putExtra("lng", lng);
                    startActivityForResult(it, 2);// precisará atualizar a lista de nomes e categorias na volta (devido a possíveis alterações no BD)
                    return true;
                }
                Toast.makeText(getBaseContext(), getResources().getString(R.string.toastLocationNotDef), Toast.LENGTH_LONG).show();
                return false;

            case R.id.optMainManage:
                it = new Intent(this, ManageActivity.class);
                startActivityForResult(it, 2);// precisará atualizar a lista de nomes e categorias na volta (devido a possíveis alterações no BD)
                return true;

            case R.id.optMainReport:
                it = new Intent(this, ReportActivity.class);
                startActivity(it);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        TextView latTxt = findViewById(R.id.outPosLat);
        TextView longTxt = findViewById(R.id.outPosLong);

        latTxt.setText(Double.toString(location.getLatitude()));
        longTxt.setText(Double.toString(location.getLongitude()));

        latTxt.setTextColor(getResources().getColor(R.color.colorAccent));
        longTxt.setTextColor(getResources().getColor(R.color.colorAccent));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults){
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) { // se o usuario deu as permissoes
                    if (checkPermission()) {
                        provider = lm.getBestProvider(criteria, true);
                        if (provider == null) {
                            //
                        } else {
                            lm.requestLocationUpdates(provider, 5000, 0, (LocationListener) this);// configura para atualizar a localização do usuário a cada 5 segundos;
                        }
                    }
                } else {
                    Toast.makeText(getBaseContext(), getResources().getString(R.string.toastLocationPermission), Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent it) {
        super.onActivityResult(requestCode, resultCode, it);
        if(resultCode == 2) {// o BD pode ter sido alterado
            recreate();
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}