package br.com.secompufscar.presenceregister;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.microblink.activity.Pdf417ScanActivity;
import com.microblink.recognizers.blinkbarcode.pdf417.Pdf417RecognizerSettings;
import com.microblink.recognizers.settings.RecognitionSettings;
import com.microblink.recognizers.settings.RecognizerSettings;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;

public class TelaPrincipal extends AppCompatActivity implements View.OnClickListener{
    private Button bPalestras,bMinicursos,bWorkshops,bMesaRedonda;
    private  String jsonEventos;
    private Context context=this;
    private LinearLayout lLayout;
    private ImageView loadAnimation;
    private SharedPreferences myPrefs;
    private final String EVENTOSURL="https://secompufscar.com.br/2016/app";
    private final String LICENSE_KEY="IQWMRXJD-C4TYRTMF-6M3YXYFF-RG7NYYKH-ZXCS2JJR-KDPAOZKE-RQKKOQOM-ZA2IHPNL";
    private  MenuItem uploadMI;
    private ProgressDialog uploadPD;
    private Cursor cursor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tela_principal);

        lLayout= (LinearLayout) findViewById(R.id.LinearLayout);

        bPalestras= (Button) findViewById(R.id.button);
        bPalestras.setOnClickListener(this);

        bMinicursos= (Button) findViewById(R.id.minicursos);
        bMinicursos.setOnClickListener(this);

        bWorkshops = (Button) findViewById(R.id.workshops);
        bWorkshops.setOnClickListener(this);
        bMesaRedonda= (Button) findViewById(R.id.mesasRedondas);
        bMesaRedonda.setOnClickListener(this);

        myPrefs=getSharedPreferences("Lista_de_Atividades",MODE_PRIVATE);

        jsonEventos=myPrefs.getString("jsonAtividades", "");
        if(jsonEventos.isEmpty()){
            JSONGetTask task = new JSONGetTask();
            task.execute(EVENTOSURL);
        }

    }
    protected void onStart(){
        super.onStart();
        if(uploadMI!=null) {
            DataBase db = new DataBase(this);
            cursor = db.getAllEntries();
            if (cursor.getCount() != 0) {
                uploadMI.setEnabled(true);
                uploadMI.setIcon(R.mipmap.upload_icon_vermelho);
            }
        }
    }
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.upload_menu, menu);//Menu Resource, Menu
        uploadMI=(MenuItem) menu.findItem(R.id.upload_button);
        DataBase db=new DataBase(this);
        cursor=db.getAllEntries();
        if(cursor.getCount()==0){
            uploadMI.setEnabled(false);
            uploadMI.setIcon(R.mipmap.upload_icon);
        }else{
            uploadMI.setEnabled(true);
            uploadMI.setIcon(R.mipmap.upload_icon_vermelho);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.refresh_button){

            new JSONGetTask().execute(EVENTOSURL);

        }else if(item.getItemId()==R.id.upload_button){
            DataBase db=new DataBase(this);
            cursor =db.getAllEntries();
            cursor.moveToFirst();

            Log.d("dbtestes",String.valueOf(cursor.getCount()));
            cursor=db.getAllEntries();
            cursor.moveToFirst();
            uploadPD= ProgressDialog.show(context, "Sincronizando registros de presenças", "");
            uploadPD.setMax(cursor.getCount());
            uploadPD.setProgress(0);
            new PostTask().execute( cursor.getString(cursor.getColumnIndex(DataBase.EVENTO_ID)),
                    cursor.getString(cursor.getColumnIndex(DataBase.HORARIO)),
                    cursor.getString(cursor.getColumnIndex(DataBase.PARTICIPANTE_ID)),
                    cursor.getString(cursor.getColumnIndex(DataBase.ID)));

        }else{
            RecognitionSettings recognitionSettings = new RecognitionSettings();
            recognitionSettings.setRecognizerSettingsArray(new RecognizerSettings[]{new Pdf417RecognizerSettings()});
            Intent intent = new Intent(this, DefaultScanActivity.class);
            intent.putExtra(Pdf417ScanActivity.EXTRAS_LICENSE_KEY, LICENSE_KEY);
            intent.putExtra(Pdf417ScanActivity.EXTRAS_RECOGNITION_SETTINGS, recognitionSettings);

            if(item.getItemId()==R.id.menu_credenciamento){
                intent.putExtra("CodigoEvento","0");

            }else if(item.getItemId()==R.id.menu_verificar){
                intent.putExtra("CodigoEvento","-1");

            }
            startActivity(intent);

        }
        return  super.onOptionsItemSelected(item);
    }
    @Override
    public void onClick(View v) {
        if(bPalestras==v){
            Intent intent=new Intent(this, ListaEventos.class);
            intent.putExtra("Evento","palestras");
            intent.putExtra("JSONString", jsonEventos);
            startActivity(intent);
        }else if(bWorkshops.equals(v)){
            Intent intent=new Intent(this, ListaEventos.class);
            intent.putExtra("Evento","workshops");
            intent.putExtra("JSONString", jsonEventos);
            startActivity(intent);
        }else if(bMinicursos.equals(v)){
            Intent intent=new Intent(this, ListaEventos.class);
            intent.putExtra("Evento","minicursos");
            intent.putExtra("JSONString", jsonEventos);
            startActivity(intent);
        }else if(bMesaRedonda.equals(v)){
            Intent intent=new Intent(this, ListaEventos.class);
            intent.putExtra("Evento","mesasredondas");
            intent.putExtra("JSONString", jsonEventos);
            startActivity(intent);
        }
    }

    class JSONGetTask extends AsyncTask<String,String, String>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            lLayout.setVisibility(View.GONE);

            loadAnimation =(ImageView) findViewById(R.id.loadAnimationIV);
            loadAnimation.setBackgroundResource(R.drawable.loading_animation);

            AnimationDrawable frameAnimation = (AnimationDrawable) loadAnimation.getBackground();
            frameAnimation.start();
        }

        @Override
        protected String doInBackground(String... urls) {
            HttpURLConnection httpConnection=null;
            BufferedReader reader= null;
            String retorno="";
            try{
                URL url=new URL(urls[0]);
                httpConnection=(HttpURLConnection) url.openConnection();
                httpConnection.connect();

                InputStream stream=httpConnection.getInputStream();
                reader =new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer=new StringBuffer();
                String line ;
                while((line=reader.readLine())!=null){
                    buffer.append(line);
                }

                retorno= buffer.toString();

            } catch (IOException e) {
                e.printStackTrace();
            }finally {

                if (httpConnection != null){
                    httpConnection.disconnect();
                }

                if(reader!=null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
            return retorno;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            loadAnimation.setVisibility(View.GONE);
            lLayout.setVisibility(View.VISIBLE);
            if(!s.isEmpty()) {
                jsonEventos = s;
                SharedPreferences.Editor e = myPrefs.edit();
                e.putString("jsonAtividades", s);
                e.commit();
            }
        }
    }
    class PostTask extends AsyncTask<String,String,String> {

        @Override
        protected void onPreExecute(){

        }
        protected void onPostExecute(String s){
            if(!s.equals(null)&&!s.equals("timeOutException")){
                uploadPD.setProgress(uploadPD.getProgress()+1);
                DataBase db=new DataBase(context);
                db.deleteEntry(cursor.getInt(cursor.getColumnIndex(DataBase.ID)));
            }
            if(!cursor.isLast()&&!s.equals("timeOutException")) {
                cursor.moveToNext();
                new PostTask().execute(cursor.getString(cursor.getColumnIndex(DataBase.EVENTO_ID)),
                        cursor.getString(cursor.getColumnIndex(DataBase.HORARIO)),
                        cursor.getString(cursor.getColumnIndex(DataBase.PARTICIPANTE_ID)),
                        cursor.getString(cursor.getColumnIndex(DataBase.ID)));
            }else{
                uploadPD.dismiss();
                DataBase db=new DataBase(context);
                cursor= db.getAllEntries();
                if(cursor.getCount()==0){
                    uploadMI.setEnabled(false);
                    uploadMI.setIcon(R.mipmap.upload_icon);
                    Toast.makeText(context,"Sucesso!",Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(context,"A conexão com o servidor falhou",Toast.LENGTH_SHORT).show();
                }
            }
        }
        protected String doInBackground(String... strings) {
            String url = "https://secompufscar.com.br/2016/app/";
            String charset = "UTF-8";  // Or in Java 7 and later, use the constant: java.nio.charset.StandardCharsets.UTF_8.name()
            String param1 =strings[0];

            String param2 = strings[2];

            String query = null;
            String responseBody=null;
            try {
                JSONObject jsonObject= jsonObject=new JSONObject(strings[1]);
                query = String.format("id_usuario=%s&id_atividade=%s&dia=%s&mes=%s&horas=%s&minutos=%s&segundos=%s",
                        URLEncoder.encode(param1, charset),
                        URLEncoder.encode(param2, charset),
                        jsonObject.getString("dia"),
                        jsonObject.getString("mes"),
                        jsonObject.getString("horas"),
                        jsonObject.getString("minutos"),
                        jsonObject.getString("segundos")
                );
            } catch (UnsupportedEncodingException e) {
                Log.d("datedebug", e.getMessage());

            } catch (JSONException e) {
                e.printStackTrace();
            }

            URLConnection connection = null;
            OutputStream output= null;
            try {
                connection = new URL(url).openConnection();
                connection.setDoOutput(true); // Triggers POST.
                connection.setRequestProperty("Accept-Charset", charset);
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + charset);
                connection.setConnectTimeout(2000);
                output = connection.getOutputStream();
                output.write(query.getBytes(charset));

                InputStream response = connection.getInputStream();
                Scanner scanner = new Scanner(response);
                responseBody = scanner.useDelimiter("\\A").next();
                Log.d("httpconnection", responseBody);


            } catch (MalformedURLException e) {
                Log.d("httpconnectionerror1", e.getMessage());
            } catch (UnsupportedEncodingException e) {
                Log.d("httpconnectionerror2", e.getMessage());
            } catch(SocketTimeoutException e){
                Log.d("httpconnectionesocket", e.getMessage());
                responseBody= "timeOutException";
            } catch (IOException e) {
                Log.d("httpconnectionerror3", e.getMessage());
                responseBody= "timeOutException";
            } finally{
                try {
                    if(output!=null)
                        output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return responseBody;
        }
    }
}


