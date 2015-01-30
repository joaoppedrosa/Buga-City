package bugacity.com.bugacity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.concurrent.ExecutionException;

import bugacity.com.bugacity.asynctasks.AsyncReserva;
import bugacity.com.bugacity.asynctasks.LongOperation;
import me.drakeet.materialdialog.MaterialDialog;


public class NfcActivity extends ActionBarActivity {

    private static final String REG_ID = "APA91bEbL_42IuLrszjPOYWSSH-O4hYmAoS1kS8NfBK2t6lVfQS8wywsM6slHSiykOgHVQnxca7BjH3mIUxw33-c_eJRMxhuieWDSN7sQysPEBQq7DliEyaPqrXrY8vRJnlQiUgaVbKlcLMXHXH6CvfyrX2_6_N5_w";
    public static final String TAG = "NfcDemo";
    public static final String MIME_TEXT_PLAIN = "text/plain";
    private TextView mTextView;
    private TimePicker timePicker;
    private LinearLayout layoutNfc;
    private LinearLayout layoutReservations;
    public JSONObject jsonObjRes;
    private int hour;
    private int minute;
    private TextView buga_id;
    private Button reservar;
    private MaterialDialog mMaterialDialog;
    private SharedPreferences sharedpreferences;
    public static final String MyPREFERENCES = "MyPrefs" ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation);

        mTextView = (TextView) findViewById(R.id.textView_explanation);
        layoutNfc  = (LinearLayout) findViewById(R.id.layout_nfc);
        layoutReservations  = (LinearLayout) findViewById(R.id.layout_reservation);
        buga_id = (TextView) findViewById(R.id.buga_id);
        final Calendar c = Calendar.getInstance();
        hour = c.get(Calendar.HOUR_OF_DAY);
        minute = c.get(Calendar.MINUTE);

        timePicker = (TimePicker) findViewById(R.id.timePicker);
        timePicker.setIs24HourView(true);
        timePicker.setCurrentHour(hour);
        timePicker.setCurrentHour(minute);
        reservar = (Button) findViewById(R.id.button_reservar);

        reservar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Toast.makeText(NfcActivity.this, "Reservar", Toast.LENGTH_LONG).show();
                reservarBuga();
            }
        });

        layoutNfc.setVisibility(View.VISIBLE);
        layoutReservations.setVisibility(View.GONE);
        handleIntent(getIntent());
    }

    private void handleIntent(Intent intent) {

        String action = intent.getAction();
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            String type = intent.getType();
            if (MIME_TEXT_PLAIN.equals(type)) {
                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                new NdefReaderTask().execute(tag);
            } else {
                Log.d(TAG, "Wrong mime type: " + type);
            }

        } else if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            String[] techList = tag.getTechList();
            String searchedTech = Ndef.class.getName();

            for (String tech : techList)
                if (searchedTech.equals(tech)) {
                    new NdefReaderTask().execute(tag);
                    break;
                }
        }
    }

    private class NdefReaderTask extends AsyncTask<Tag, Void, String> {
        @Override
        protected String doInBackground(Tag... params) {
            Tag tag = params[0];
            Ndef ndef = Ndef.get(tag);
            if (ndef == null) {
                // NDEF is not supported by this Tag.
                return null;
            }
            NdefMessage ndefMessage = ndef.getCachedNdefMessage();
            NdefRecord[] records = ndefMessage.getRecords();
            for (NdefRecord ndefRecord : records) {
                if (ndefRecord.getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
                    try {
                        return readText(ndefRecord);
                    } catch (UnsupportedEncodingException e) {
                        Log.e(TAG, "Unsupported Encoding", e);
                    }
                }
            }
            return null;
        }

        private String readText(NdefRecord record) throws UnsupportedEncodingException {
            byte[] payload = record.getPayload();
            String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";
            int languageCodeLength = payload[0] & 0063;
            return new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(NfcActivity.this, result, Toast.LENGTH_LONG).show();
            layoutNfc.setVisibility(View.GONE);
            layoutReservations.setVisibility(View.VISIBLE);

            if (result != null) {


                buga_id.setText("");

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
                Toast.makeText(NfcActivity.this, "Error reading NFC Tag!", Toast.LENGTH_LONG).show();
            }
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
        mMaterialDialog = new MaterialDialog(NfcActivity.this)
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
                                resultReserva = new AsyncReserva().execute(bugaId, String.valueOf(finalPrice), (hora_fim + ":" + minuto_fim + ":" + "00")).get();
                                if (result != null) {
                                    Toast.makeText(NfcActivity.this, "Operation " + result + ", " + resultReserva, Toast.LENGTH_LONG).show();
                                    Intent intent = new Intent(NfcActivity.this, MapsActivity.class);
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
        Intent intent = new Intent (NfcActivity.this, MainActivity.class);
        startActivity(intent);
    }
}
