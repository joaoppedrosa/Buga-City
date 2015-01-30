package bugacity.com.bugacity.asynctasks;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;

/**
 * Created by Joao on 29/01/2015.
 */
public class AsyncReserva extends AsyncTask<String, Void, String> {
    @Override
    protected String doInBackground(String... strings) {
        String idBuda = strings[0];
        String price = strings[1];
        String time = strings[2];

        final Calendar c = Calendar.getInstance();
        int dia = c.get(Calendar.DAY_OF_MONTH);
        int ano = c.get(Calendar.YEAR);
        int mes = c.get(Calendar.MONTH)+1;

        String url = "http://192.168.160.62:8080/ServiceMap/webresources/generic/inserirReserva?idBuga="+idBuda+"&tempoReserva="+ano+"%20"+dia+"%20"+mes+"%20"+time+"&custoTotal="+price;
        StringBuilder builder = new StringBuilder();
        HttpClient client = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(url);
        if (strings.length > 0) {
            try {
                HttpResponse response = client.execute(httpGet);
                StatusLine statusLine = response.getStatusLine();
                int statusCode = statusLine.getStatusCode();
                if (statusCode == 200) {
                    HttpEntity entity = response.getEntity();
                    InputStream content = entity.getContent();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(content));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        builder.append(line);
                    }

                } else {
                    Log.e("MapActivity", "Failed to download file");
                }
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return builder.toString();
    }
}