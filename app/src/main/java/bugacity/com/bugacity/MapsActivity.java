package bugacity.com.bugacity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import bugacity.com.bugacity.asynctasks.AsyncJson;
import bugacity.com.bugacity.directions.Route;
import bugacity.com.bugacity.directions.Routing;
import bugacity.com.bugacity.directions.RoutingListener;
import bugacity.com.bugacity.utils.MarkerObject;
import me.drakeet.materialdialog.MaterialDialog;

public class MapsActivity extends FragmentActivity implements RoutingListener, LocationListener {

    private GoogleMap mMap;
    private static final double LAT_AVEIRO = 40.641346;
    private static final double LONG_AVEIRO = -8.653275;
    private MaterialDialog mMaterialDialog;
    private LatLng currentLocation;
    private PolylineOptions polyoptions = new PolylineOptions();
    private Polyline polylineFinal;
    private MarkerObject clObject = new MarkerObject();
    private static final String URL_BUGACITY_JSON = "http://192.168.160.62:8080/ServiceMap/webresources/generic/getPontosTuristicos";
    private MarkerOptions markerCurrentLocation = new MarkerOptions();
    private ArrayList<MarkerObject> markersList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Button close = (Button) findViewById(R.id.button_close);
        close.bringToFront();
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(polylineFinal!=null){
                    polylineFinal.remove();
                    Toast.makeText(MapsActivity.this,"Clear routes!",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(MapsActivity.this,"You don't have any route in the map!",Toast.LENGTH_SHORT).show();
                }
            }
        });



        setUpMapIfNeeded();
        readJsonService();
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(final Marker marker) {
                mMaterialDialog = new MaterialDialog(MapsActivity.this)
                        .setTitle("Route")
                        .setMessage("You want to trace a route from your current locations to "+marker.getTitle())
                        .setPositiveButton("OK", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Routing routing = new Routing(Routing.TravelMode.BIKING);
                                routing.registerListener(MapsActivity.this);
                                routing.execute(currentLocation, marker.getPosition());
                                mMaterialDialog.dismiss();
                            }
                        })
                        .setNegativeButton("CANCEL", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mMaterialDialog.dismiss();
                            }
                        });
                mMaterialDialog.show();
            }
        });
    }

    private void setUpMapIfNeeded() {
        if (mMap == null) {
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {
        CameraUpdate center;
        if(currentLocation==null){
            center = CameraUpdateFactory.newLatLng(new LatLng(LAT_AVEIRO,LONG_AVEIRO));
        }
        else{
            center = CameraUpdateFactory.newLatLng(currentLocation);
        }

        CameraUpdate zoom=CameraUpdateFactory.zoomTo(14);
        mMap.moveCamera(center);
        mMap.animateCamera(zoom);
    }

    private void addMarker(LatLng location, String title, String snippet, int icon){
        mMap.addMarker(new MarkerOptions()
                .position(location)
                .title(title)
                .snippet(snippet)
                .icon(BitmapDescriptorFactory.fromResource(icon)));
        mMap.setInfoWindowAdapter(new MyInfoWindowAdapter());
    }


    @Override
    public void onRoutingFailure() {

    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(PolylineOptions mPolyOptions, Route route) {
        polyoptions.color(Color.CYAN);
        polyoptions.width(10);
        polyoptions.addAll(mPolyOptions.getPoints());
        polylineFinal = mMap.addPolyline (polyoptions);
    }

    @Override
    public void onLocationChanged(Location location) {
        currentLocation = new LatLng(location.getLatitude(),location.getLongitude());
        markerCurrentLocation.position(currentLocation)
                .title("BugaCity")
                .snippet("You are here!")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.current_location));
        clObject.setName("BugaCity");
        clObject.setDescrition("You are here!");
        clObject.setLatlng(currentLocation);
        markersList.add(clObject);
        mMap.addMarker(markerCurrentLocation);
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d("Latitude", "disable");
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d("Latitude","enable");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d("Latitude","status");
    }

    public String readJsonService() {
        String result = "";
        try {
            result = new AsyncJson().execute(URL_BUGACITY_JSON).get();
//            result = ("[{\"id\":\"42\",\"name\":\"Estação Antiga de Aveiro\",\"zona\":\"Aveiro\",\"tipo\":\"Edificio\",\"latitude\":\"40.6442\",\"longitude\":\"-8.64076\",\"linkImage\":\"http:\\/\\/www.travel-in-portugal.com\\/sites\\/default\\/files\\/photos\\/aveiro-old-station.jpg\",\"descricao\":\"O Edifício apresenta uma fachada totalmente decorada de azulejos policromos (em tons azuis e amarelos que representam várias cenas ferroviárias naturais e de cultura e actividades tradicionais). O edifício é composto por três secções; uma parte central de três pisos que inclui três portas amplas ao nível do solo e duas partes laterais simétricas.Foi construída no estilo tradicional (estilo denominado de Casa Portuguesa).\"},{\"id\":\"43\",\"name\":\"Mosteiro de Jesus ou Museu de Santa Joana \",\"zona\":\"Santa Joana\",\"tipo\":\"Museu\",\"latitude\":\"40.6371\",\"longitude\":\"8.65182\",\"linkImage\":\"http:\\/\\/upload.wikimedia.org\\/wikipedia\\/commons\\/6\\/68\\/Mosteiro_de_Jesus_-_museu_de_Aveiro.jpg\",\"descricao\":\"Fundado por D. Brites Leitoa em 1458 como convento de religiosas dominicanas. Por Breve do papa Pio II datado de 1461 o convento foi integrado na Observância(aceite como cumprindo todas a regras estritas) podendo portanto as religiosas professar legitimamente como monjas de acordo com a Regra de vida Dominicana.\"},{\"id\":\"44\",\"name\":\"Pelourinho de Esgueira\",\"zona\":\"Esgueira\",\"tipo\":\"Pelourinho\",\"latitude\":\"40.6486\",\"longitude\":\"-8.62806\",\"linkImage\":\"http:\\/\\/www.jf-esgueira.pt\\/site\\/images\\/Pelourinho.jpg\",\"descricao\":\"Monumento construído no século XVIII que se encontra classificado como Imóvel de Interesse Público desde 1933.\"}]");
            try {
                JSONArray obj = new JSONArray(result);
                Log.e("My App", obj.toString());
                JSONObject mJsonObject = new JSONObject();
                LatLng latlong;
                MarkerObject markerObject;

                for (int i = 0; i < obj.length(); i++) {
                    mJsonObject = obj.getJSONObject(i);
                    Log.e("JSON", mJsonObject.getString("id"));
                    Log.e("JSON", mJsonObject.getString("name"));
                    Log.e("JSON", mJsonObject.getString("descricao"));
                    Log.e("JSON", mJsonObject.getString("latitude"));
                    Log.e("JSON", mJsonObject.getString("longitude"));
                    Log.e("JSON", mJsonObject.getString("tipo"));
                    Log.e("JSON", mJsonObject.getString("zona"));
                    Log.e("JSON", mJsonObject.getString("linkImage"));
                    latlong = new LatLng(mJsonObject.getDouble("latitude"), mJsonObject.getDouble("longitude"));
                    markerObject = new MarkerObject(mJsonObject.getString("descricao"), mJsonObject.getString("name"), mJsonObject.getString("zona"), mJsonObject.getString("tipo"), latlong, mJsonObject.getString("linkImage"));
                    markersList.add(markerObject);
                    addMarker(latlong, mJsonObject.getString("name"), (mJsonObject.getString("tipo") + " | " + mJsonObject.getString("zona")), R.drawable.marker);
                }

            } catch (Throwable t) {
                Log.e("My App", "Could not parse malformed JSON: \"" + t + "\"");
            }

        } catch (Exception ex) {
            Log.e("MapsActivity", "ESTOIROU " + ex.getMessage());
        }
        return result;
    }


    class MyInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
        private final View myContentsView;

        MyInfoWindowAdapter() {
            myContentsView = getLayoutInflater().inflate(R.layout.custom_infowindow, null);
        }

        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }

        @Override
        public View getInfoContents(Marker marker) {
            boolean flag = true;
            TextView tvname = ((TextView) myContentsView.findViewById(R.id.title));
            TextView tvtipozona = ((TextView) myContentsView.findViewById(R.id.tipozona));
            TextView tvSnippet = ((TextView) myContentsView.findViewById(R.id.snippet));
            ImageView image = ((ImageView) myContentsView.findViewById(R.id.imagewindow));

            for(int i=0;i<markersList.size();i++){
                if(markersList.get(i).getLatlng().equals(marker.getPosition())){
                    flag = false;
                    tvname.setText(markersList.get(i).getName());
                    tvtipozona.setText(markersList.get(i).getType()+" | "+markersList.get(i).getZone());
                    tvSnippet.setText(markersList.get(i).getDescrition());
//                    try {
//                        Bitmap bitmap = BitmapFactory.decodeStream((InputStream) new URL().getContent());
//                        image.setImageBitmap(bitmap);
//                    } catch (MalformedURLException e) {
//                        e.printStackTrace();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                        Log.e("BITMAP ERROR", e.toString());
//                    }
                    Picasso.with(MapsActivity.this).load(markersList.get(i).getImage()).into(image);

                }
            }
            if(flag){
                tvname.setText("BugaCity");
                tvtipozona.setText("");
                tvSnippet.setText("You are here!");
                Picasso.with(MapsActivity.this).load(R.drawable.notifications).into(image);

//                image.setImageDrawable(getResources().getDrawable(R.drawable.notifications));
            }
            return myContentsView;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(MapsActivity.this,MainActivity.class);
        startActivity(intent);
    }
}
