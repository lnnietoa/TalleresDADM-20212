package com.example.reto10webservice;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {

    private static final int CONNECTION_TIMEOUT = 60000;
    private static final int DATARETRIEVAL_TIMEOUT = 60000;
    private static final String uribase = "https://www.datos.gov.co/resource/5wck-szir.json?";

    private static final String[] departamentos_labels = {
            "Amazonas", "Antioquia", "Arauca", "Atlántico",
            "Bogotá D.C.", "Bolívar", "Boyacá", "Caldas",
            "Caquetá", "Casanare", "Cauca", "Cesar",
            "Chocó", "Córdoba", "Cundinamarca", "Guainía",
            "Guaviare", "Huila", "La Guajira", "Magdalena",
            "Meta", "Nariño", "Norte de Santander", "Putumayo",
            "Quindío", "Risaralda", "San Andrés y Providencia", "Santander",
            "Sucre", "Tolima", "Valle del Cauca", "Vaupés", "Vichada"
    };

    private static final String[][] departamentos_values = {
            {"AMAZONAS"}, {"ANTIOQUIA"}, {"ARAUCA"},
            {"ATLANTICO", "ATLÁNTICO"}, {"BOGOTA D.C", "BOGOTÁ D.C", "BOGOTA D.C.", "BOGOTÁ D.C."},
            {"BOLIVAR", "BOLÍVAR"}, {"BOYACA", "BOYACÁ"}, {"CALDAS"}, {"CAQUETA", "CAQUETÁ"},
            {"CASANARE"}, {"CAUCA"}, {"CESAR"}, {"CHOCO", "CHOCÓ"}, {"CORDOBA", "CÓRDOBA"},
            {"CUNDINAMARCA"}, {"GUAINIA", "GUAINÍA"}, {"GUAVIARE"}, {"HUILA"},
            {"LA GUAJIRA", "GUAJIRA"}, {"MAGDALENA"}, {"META"},
            {"NARINIO", "NARINO", "NARIÑO"}, {"NORTE DE SANTANDER"}, {"PUTUMAYO"},
            {"QUINDIO", "QUINDÍO"}, {"RISARALDA"},
            {"ARCHIPIÉLAGO DE SA", "Archipiélago de San Andrés Providencia y Santa Catalina",
                    "SAN ANDRES Y PROVI", "SAN ANDRES Y PROVIDENCIA", "SAN ANDRÉS Y PROVIDENCIA"},
            {"SANTANDER"}, {"SUCRE"}, {"TOLIMA"}, {"VALLE DEL CAUCA"},
            {"VAUPES", "VAUPÉS"}, {"VICHADA"}
    };

    private static final String[] year = {"2015", "2016", "2017", "2018", "2019", "2020"};
    private static final String[] nivel = {"Pregrado", "Posgrado"};

    ProgressDialog pd;
    Context context;
    Button nivelButton, departamentoButton, yearButton, buscarButton;
    String departamento = departamentos_labels[0], niveles = nivel[0], years = year[0];
    String idNivel;
    TextView infoTextView;
    String totalMatriculados;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        infoTextView = findViewById(R.id.totalTextView);
        departamentoButton = findViewById(R.id.departamentoButton);
        nivelButton = findViewById(R.id.nivelButton);
        yearButton = findViewById(R.id.yearButton);
        buscarButton = findViewById(R.id.buscarButton);

        buscarButton.setOnClickListener(v -> new JsonTask().execute());

        AlertDialog.Builder departamentoPopup = new AlertDialog.Builder(this);
        AlertDialog.Builder nivelPopup = new AlertDialog.Builder(this);
        AlertDialog.Builder yearPopup = new AlertDialog.Builder(this);

        departamentoPopup.setTitle("Escoge el departamento que deseas consultar");
        nivelPopup.setTitle("Escoge el nivel academico que deseas consultar");
        yearPopup.setTitle("Escoge el año que deseas consultar");

        int checkedItem = 0;
        departamentoPopup.setSingleChoiceItems(departamentos_labels, checkedItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // user checked an item
                departamento = departamentos_labels[which];
                departamentoButton.setText(departamento);
            }
        });

        departamentoPopup.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // user clicked OK
            }
        });

        departamentoPopup.setNegativeButton("Cancel", null);

        nivelPopup.setSingleChoiceItems(nivel, checkedItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // user checked an item
                niveles = nivel[which];
                nivelButton.setText(niveles);
            }
        });

        nivelPopup.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // user clicked OK
            }
        });

        nivelPopup.setNegativeButton("Cancel", null);

        yearPopup.setSingleChoiceItems(year, checkedItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // user checked an item
                years = year[which];
                yearButton.setText(years);
            }
        });

        yearPopup.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // user clicked OK
            }
        });

        yearPopup.setNegativeButton("Cancel", null);


        AlertDialog departamentoDialog = departamentoPopup.create();
        AlertDialog nivelDialog = nivelPopup.create();
        AlertDialog yearDialog = yearPopup.create();

        departamentoButton.setOnClickListener(v -> departamentoDialog.show());
        nivelButton.setOnClickListener(v -> nivelDialog.show());
        yearButton.setOnClickListener(v -> yearDialog.show());

    }

    public static JSONArray requestWebService(String serviceUrl) {
        disableConnectionReuseIfNecessary();

        HttpURLConnection urlConnection = null;
        try {
            // create connection
            URL urlToRequest = new URL(serviceUrl);
            urlConnection = (HttpURLConnection)  urlToRequest.openConnection();
            urlConnection.setConnectTimeout(CONNECTION_TIMEOUT);
            urlConnection.setReadTimeout(DATARETRIEVAL_TIMEOUT);

            // handle issues
            int statusCode = urlConnection.getResponseCode();
            if (statusCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                // handle unauthorized (if service requires user login)
                System.out.println("Error de autorización");
            }

            else if (statusCode != HttpURLConnection.HTTP_OK) {
                // handle any other errors, like 404, 500,..
                System.out.println("Error Miscelaneo");
            }

            // create JSON object from content
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            return new JSONArray(getResponseText(in));
        }

        catch (MalformedURLException e) { System.out.println("URL is invalid"); }
        catch (SocketTimeoutException e) { System.out.println("data retrieval or connection timed out"); }
        catch (IOException e) { System.out.println("could not read response body"); }
        catch (JSONException e) { System.out.println("response body is no valid JSON string"); }
        finally { if (urlConnection != null) { urlConnection.disconnect(); } }
        return null;
    }

    private static void disableConnectionReuseIfNecessary() {
        // see HttpURLConnection API doc
        if (Integer.parseInt(Build.VERSION.SDK) < Build.VERSION_CODES.FROYO) {
            System.setProperty("http.keepAlive", "false");
        }
    }

    private static String getResponseText(InputStream inStream) {
        // very nice trick from
        // http://weblogs.java.net/blog/pat/archive/2004/10/stupid_scanner_1.html
        return new Scanner(inStream).useDelimiter("\\A").next();
    }

    public void getData(String uribase, String query) {

        infoTextView.setText(" ");
        JSONArray serviceResult = requestWebService(uribase + query);

        try {
            totalMatriculados = serviceResult.getJSONObject(0).getString("sum_matriculados_2015").toString();
            //Log.d("TEST",  Integer.toString(i) + " :: " + data.get(i).contacto);
        }

        catch (JSONException e) { Log.e("Error", e.toString()); }
    }

    private class JsonTask extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(MainActivity.this);
            pd.setMessage("Please wait");
            pd.setCancelable(false);
            pd.show();
        }

        protected String doInBackground(String... params) {
            for (int i = 0; i < departamentos_labels.length; i++){
                if(departamentos_labels[i].equals(departamento)){
                    departamento = departamentos_values[i][0];
                    break;
                }
            }

            if(niveles.equals(nivel[0])){
                idNivel = "1";
            }else{
                idNivel = "2";
            }
            String query = "$select=sum(matriculados_2015)&$where=departamento_de_oferta_del_programa=%27"+departamento+"%27%20and%20id_nivel%20=%27"+idNivel+"%27%20and%20a_o%20=%20%27"+years+"%27";
            try { getData(uribase, query); }
            catch (Throwable e) { e.printStackTrace(); }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (pd.isShowing()) { pd.dismiss(); }
            infoTextView.setText("El numero de Total de Estudiantes Matriculados en "
                    + departamento + " para " + niveles +  " en el año " + years +
                    " fue de: " + totalMatriculados);
        }
    }

}