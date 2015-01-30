package bugacity.com.bugacity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.concurrent.ExecutionException;

import bugacity.com.bugacity.asynctasks.AsyncReserva;
import bugacity.com.bugacity.asynctasks.LongOperation;
import me.drakeet.materialdialog.MaterialDialog;


public class ReservationActivity extends ActionBarActivity {

    private String resultQrcCode = "";
    private TextView buga_id;
    private Button reservar;
    private TimePicker timePicker;
    private JSONObject jsonObjRes;
    private int hour;
    private int minute;
    private LinearLayout layoutNfc;
    private LinearLayout layoutReservations;
    private MaterialDialog mMaterialDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        scanBarcode();
        setContentView(R.layout.activity_reservation);

        layoutNfc  = (LinearLayout) findViewById(R.id.layout_nfc);
        layoutReservations  = (LinearLayout) findViewById(R.id.layout_reservation);

        buga_id = (TextView) findViewById(R.id.buga_id);
        reservar = (Button) findViewById(R.id.button_reservar);
        final Calendar c = Calendar.getInstance();
        hour = c.get(Calendar.HOUR_OF_DAY);
        minute = c.get(Calendar.MINUTE);

        timePicker = (TimePicker) findViewById(R.id.timePicker);
        timePicker.setIs24HourView(true);
        timePicker.setCurrentHour(hour);
        timePicker.setCurrentHour(minute);

        reservar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reservarBuga();
            }
        });

        layoutNfc.setVisibility(View.GONE);
        layoutReservations.setVisibility(View.VISIBLE);
    }

    public void scanBarcode() {
        new IntentIntegrator(this).initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(ReservationActivity.this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(ReservationActivity.this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
                resultQrcCode = result.getContents();
                setResult(resultQrcCode);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void setResult(String result) {
        buga_id.setText("");
        if (result != null) {
            try {
                jsonObjRes = new JSONObject(result);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                buga_id.setText(jsonObjRes.get("buga_id").toString());
            } catch (JSONException e) {
                buga_id.setText("[Não identificado]");
                e.printStackTrace();
            }
        }
        else{
            Toast.makeText(ReservationActivity.this, "Error reading QRCode!", Toast.LENGTH_LONG).show();
        }
    }

    public void reservarBuga(){
        final int hora_fim = timePicker.getCurrentHour();
        final int minuto_fim = timePicker.getCurrentMinute();
        Calendar agora = Calendar.getInstance();
        Calendar calendar_fim_estacionamento = Calendar.getInstance();
        calendar_fim_estacionamento.set(agora.get(Calendar.YEAR), agora.get(Calendar.MONTH), agora.get(Calendar.DAY_OF_MONTH), hora_fim, minuto_fim);

        if (agora.get(Calendar.HOUR_OF_DAY) > hora_fim) {
            calendar_fim_estacionamento.add(Calendar.DATE, 1);
        }
        else if (agora.get(Calendar.HOUR_OF_DAY)== hora_fim && agora.get(Calendar.MINUTE) >= minuto_fim) {
            calendar_fim_estacionamento.add(Calendar.DATE, 1);
        }
        final long time_difference_minut = (((calendar_fim_estacionamento.getTime().getTime() - agora.getTime().getTime())/1000)/60);
        String tempoParaFim="";
        if(time_difference_minut>60){
            tempoParaFim= String.valueOf((time_difference_minut/60));
            tempoParaFim+=" hora(s) e ";
            tempoParaFim+=(time_difference_minut%60);
            tempoParaFim+=" minuto(s)";
        }
        else{
            tempoParaFim=String.valueOf(time_difference_minut);
            tempoParaFim+=" minuto(s)";
        }

        int price = 1;
        if(time_difference_minut>60) {
            String [] split = tempoParaFim.split(" ");
            int hours = Integer.valueOf(split[0]);
            price = price * hours;
        }

        final int finalPrice = price;
        mMaterialDialog = new MaterialDialog(ReservationActivity.this)
                .setTitle("Route")
                .setMessage("You want to mark the end date of hire for " +tempoParaFim + " for "+price+"€ ?")
                .setPositiveButton("Yes", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!buga_id.getText().equals("")) {
                            String result = "";
                            String resultReserva = "";
                            String bugaId = buga_id.getText().toString();
                            try {
                                result = new LongOperation().execute(bugaId, Long.toString(time_difference_minut),String.valueOf(finalPrice)).get();
                                resultReserva = new AsyncReserva().execute(bugaId, String.valueOf(finalPrice), (hora_fim+":"+minuto_fim+":"+"00")).get();
                                if (result != null) {
                                    Toast.makeText(ReservationActivity.this, "Operation " + result+", "+resultReserva, Toast.LENGTH_LONG).show();
                                    Intent intent = new Intent (ReservationActivity.this, MapsActivity.class);
                                    startActivity(intent);
                                }
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            }
                        }
                        mMaterialDialog.dismiss();
                    }
                })
                .setNegativeButton("No", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mMaterialDialog.dismiss();
                    }
                });
        mMaterialDialog.show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent (ReservationActivity.this, MainActivity.class);
        startActivity(intent);
    }
}
