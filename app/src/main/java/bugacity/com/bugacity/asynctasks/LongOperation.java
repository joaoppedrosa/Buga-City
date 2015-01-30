package bugacity.com.bugacity.asynctasks;

import android.os.AsyncTask;

import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Joao on 29/01/2015.
 */
public class LongOperation extends AsyncTask<String, Void, String> {

    private static final String REG_ID = "APA91bEbL_42IuLrszjPOYWSSH-O4hYmAoS1kS8NfBK2t6lVfQS8wywsM6slHSiykOgHVQnxca7BjH3mIUxw33-c_eJRMxhuieWDSN7sQysPEBQq7DliEyaPqrXrY8vRJnlQiUgaVbKlcLMXHXH6CvfyrX2_6_N5_w";
    private static final String REG_IDT = "APA91bGV1P_9lu7N0ZoHdb_dZauzn5kE8VM8_W6pw5OGiyvlVaMEg5HhMJ0NBHeceFirSVzEo9EhGv8HocvxQokWm51qQfeDl1x0-DSQSgMq23U5qHUr2qU5diNpO5401L8eAKVQ5GawQNYbeHeenycgXmeydbshpQ";


    protected String getASCIIContentFromEntity(HttpEntity entity) throws IllegalStateException, IOException {
        InputStream in = entity.getContent();
        StringBuffer out = new StringBuffer();
        int n = 1;
        while (n>0) {
            byte[] b = new byte[4096];
            n =  in.read(b);
            if (n>0) out.append(new String(b, 0, n));
        }
        return out.toString();
    }


    @Override
    protected String doInBackground(String... params) {
        String bugaID = (params[0]);
        int tempoParaFim= Integer.parseInt(params[1]);
        int minParaFim = 1;
        String price = (params[2]);
        HttpClient httpClient = new DefaultHttpClient();
        HttpContext localContext = new BasicHttpContext();


        String SOAP_ACTION = "http://192.168.160.62:8080/WebServiceGCM/GCMService?xsd=1";
        String NAMESPACE = "http://GCM/";

        String URL = "http://192.168.160.62:8080/WebServiceGCM/GCMService?wsdl";
        String METHOD_NAME = "programMessage";
        HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);

        SoapObject request_warning = new SoapObject(NAMESPACE, METHOD_NAME);
        request_warning.addProperty("regId", REG_ID);
        request_warning.addProperty("uuidFromUser", "00000000-0000-0000-0000-000000000000");
        request_warning.addProperty("time", tempoParaFim-minParaFim);
        request_warning.addProperty("message", "A reserva da buga com o id "+bugaID+" acaba em "+minParaFim+" minutos");

        envelope.setOutputSoapObject(request_warning);
        String result_uuid="";
        try {
            androidHttpTransport.call(SOAP_ACTION, envelope);

            result_uuid = envelope.getResponse().toString();
        } catch (Exception ex) {
            String d = "";
        }

        SoapObject request_finalMessage = new SoapObject(NAMESPACE, METHOD_NAME);
        request_finalMessage.addProperty("regId", REG_ID);
        request_finalMessage.addProperty("uuidFromUser", "00000000-0000-0000-0000-000000000000");
        request_finalMessage.addProperty("time", tempoParaFim);
        request_finalMessage.addProperty("message", "A reserva terminou, precisa de pagar "+price+" €");

        envelope.setOutputSoapObject(request_finalMessage);

        String result_uuid_finalMessage="";
        try {
            androidHttpTransport.call(SOAP_ACTION, envelope);

            result_uuid = envelope.getResponse().toString();
        } catch (Exception ex) {
            String d = "";
        }
        return "executed";
    }


    @Override
    protected void onPostExecute(String result) {
    }

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected void onProgressUpdate(Void... values) {
    }
}