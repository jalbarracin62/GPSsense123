package com.example.senales.gpssense;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import static android.location.LocationManager.*;

public class ActivarActivity extends AppCompatActivity implements LocationListener {

    private TextView mTextMessage;
    private boolean control = true;
    private Context contexto;
    private LocationManager locationManager;
    private ListView scrollAlmacenado;
    private ListView scrollDatoActual;
    private String mensaje2segundos = "Latitud:\nLongitud:\nPrecisión:\nAltitud:";
    private ArrayList<String> listItems=new ArrayList<String>();
    private ArrayList<String> listItems2=new ArrayList<String>();
    private ArrayAdapter<String> adapter;
    private ArrayAdapter<String> adapter2;
    private Timer timer;
    private TimerTask timerTask;
    private Handler handler = new Handler();
    private SQLiteDatabase db;
    private FeedReaderDbHelper mDbHelper;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    stopTimer();
                    Intent intent = new Intent(contexto, FullscreenActivity.class);
                    startActivity(intent);
                    return true;
                case R.id.navigation_dashboard:
                    if(control){
                        startTimer();
                        control = false;
                    }else{
                        stopTimer();
                        control = true;
                    }
                    return true;
                case R.id.navigation_notifications:
                    //mTextMessage.setText(R.string.title_notifications);
                    guardarElementoEnBaseDatos();

                    return true;
            }
            return false;
        }

    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activar);
        contexto = getApplicationContext();
        mTextMessage = (TextView) findViewById(R.id.textView2);
        scrollAlmacenado = (ListView) findViewById(R.id.datasave);
        scrollDatoActual = (ListView) findViewById(R.id.dataregistered);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        adapter=new ArrayAdapter<String>(contexto,android.R.layout.simple_list_item_1);
        scrollAlmacenado.setAdapter(adapter);
        adapter2=new ArrayAdapter<String>(contexto,android.R.layout.simple_list_item_1);
        scrollDatoActual.setAdapter(adapter2);

        mDbHelper = new FeedReaderDbHelper(contexto);
        db = mDbHelper.getWritableDatabase();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //Requiere permisos para Android 6.0

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 225);
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, this);

    }

    private void startTimer(){
        timer = new Timer();
        timerTask = new TimerTask() {
            public void run() {
                handler.post(new Runnable() {
                    public void run(){
                        adapter.add(mensaje2segundos);
                        scrollAlmacenado.smoothScrollToPosition(adapter.getCount()-1);
                    }
                });
            }
        };
        timer.schedule(timerTask, 2000, 2000);
    }

    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer.purge();
        }
    }

    private void guardarElementoEnBaseDatos(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String currentDateandTime = sdf.format(new Date());
        String dataTotal = currentDateandTime+"\n" + mensaje2segundos;
        ContentValues values = new ContentValues();
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_TITLE, dataTotal);
        long newRowId = db.insert(FeedReaderContract.FeedEntry.TABLE_NAME, null, values);
        adapter2.add(dataTotal);
    }


    @Override
    public void onLocationChanged(Location location) {
        mensaje2segundos = "Latitud:\n"+location.getLatitude()+"\nLongitud:\n"+location.getLongitude()+"\nPrecisión:\n"+location.getAccuracy()+"\nAltitud: "+location.getAltitude();
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
