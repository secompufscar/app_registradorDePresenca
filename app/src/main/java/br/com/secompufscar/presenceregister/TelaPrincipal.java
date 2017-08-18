package br.com.secompufscar.presenceregister;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.AnimationDrawable;
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
import android.widget.Toast;

import com.microblink.activity.Pdf417ScanActivity;
import com.microblink.recognizers.blinkbarcode.pdf417.Pdf417RecognizerSettings;
import com.microblink.recognizers.settings.RecognitionSettings;
import com.microblink.recognizers.settings.RecognizerSettings;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import br.com.secompufscar.presenceregister.data.Atividade;
import br.com.secompufscar.presenceregister.data.DataBase;

import static br.com.secompufscar.presenceregister.data.Atividade.inicializaAtividadesMap;

public class TelaPrincipal extends AppCompatActivity implements View.OnClickListener {

    public static HashMap<String, List<Atividade>> atividadesHashMap = inicializaAtividadesMap();

    private Button credenciamento_button, atividades_button;
    private Context context = this;
    private SharedPreferences myPrefs;
    private MenuItem uploadMI;
    private ProgressDialog uploadPD;

    private View contentView;
    private View loadingView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        NetworkUtils.inicializeNetworkUtils(getResources().getString(R.string.LICENSE_KEY));

        setContentView(R.layout.activity_tela_principal);

        contentView = findViewById(R.id.botoes_grid);
        loadingView = findViewById(R.id.loading_spinner);
        loadingView.setVisibility(View.GONE);

        credenciamento_button = (Button) findViewById(R.id.menu_credenciamento);
        credenciamento_button.setOnClickListener(this);

        atividades_button = (Button) findViewById(R.id.atividades_button);
        atividades_button.setOnClickListener(this);

        myPrefs = getSharedPreferences("Lista_de_Atividades", MODE_PRIVATE);

        String jsonAtividades = myPrefs.getString("jsonAtividades", "");

        Log.d("TESTE oncreate", jsonAtividades);

        if (jsonAtividades.isEmpty()) {
            new JSONGetTask().execute();
        } else {
            atividadesHashMap = Atividade.atividadesParseJSON(jsonAtividades);
        }

        DataBase.setInstance(this);
    }

    protected void onStart() {
        super.onStart();
        if (uploadMI != null) {
            if (DataBase.getDB().getCountPresencas() != 0) {
                uploadMI.setEnabled(true);
                uploadMI.setIcon(R.mipmap.upload_icon_vermelho);
            }
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.upload_menu, menu);//Menu Resource, Menu
        uploadMI = (MenuItem) menu.findItem(R.id.upload_button);

        if (DataBase.getDB().getCountPresencas() == 0) {
            uploadMI.setEnabled(false);
            uploadMI.setIcon(R.mipmap.upload_icon);
        } else {
            uploadMI.setEnabled(true);
            uploadMI.setIcon(R.mipmap.upload_icon_vermelho);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.refresh_button) {
            new JSONGetTask().execute();

        } else if (item.getItemId() == R.id.upload_button) {

            uploadPD = ProgressDialog.show(context, "Sincronizando registros de presenças", "");
            uploadPD.setMax(DataBase.getDB().getCountPresencas());
            uploadPD.setProgress(0);

            new PostTask().execute();

        } else {
            RecognitionSettings recognitionSettings = new RecognitionSettings();
            recognitionSettings.setRecognizerSettingsArray(new RecognizerSettings[]{new Pdf417RecognizerSettings()});
            Intent intent = new Intent(this, DefaultScanActivity.class);
            intent.putExtra(Pdf417ScanActivity.EXTRAS_LICENSE_KEY, NetworkUtils.LICENSE_KEY);
            intent.putExtra(Pdf417ScanActivity.EXTRAS_RECOGNITION_SETTINGS, recognitionSettings);

            if (item.getItemId() == R.id.menu_verificar) {
                intent.putExtra(DefaultScanActivity.EXTRA_ID_ATIVIDADE, "-1");
            }
            startActivity(intent);

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        if (atividades_button.equals(v)) {
            startActivity(new Intent(this, Atividades.class));
        } else if (credenciamento_button.equals(v)) {
            RecognitionSettings recognitionSettings = new RecognitionSettings();
            recognitionSettings.setRecognizerSettingsArray(new RecognizerSettings[]{new Pdf417RecognizerSettings()});
            Intent intent = new Intent(this, DefaultScanActivity.class);
            intent.putExtra(Pdf417ScanActivity.EXTRAS_LICENSE_KEY, NetworkUtils.LICENSE_KEY);
            intent.putExtra(Pdf417ScanActivity.EXTRAS_RECOGNITION_SETTINGS, recognitionSettings);
            intent.putExtra(DefaultScanActivity.EXTRA_ID_ATIVIDADE, "0");
            startActivity(intent);
        }
    }

    class JSONGetTask extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            contentView.setVisibility(View.GONE);
            loadingView.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... urls) {

            return Atividade.getAtividadesFromHTTP(getBaseContext());
        }

        @Override
        protected void onPostExecute(String response) {
            contentView.setVisibility(View.VISIBLE);
            loadingView.setVisibility(View.GONE);

            if(response != null && !response.isEmpty()){
                Log.d("TESTE onPost", "salvando");
                SharedPreferences.Editor mEditor = myPrefs.edit();
                mEditor.putString("jsonAtividades", response);
                mEditor.apply();

                String jsonAtividades = myPrefs.getString("jsonAtividades", "");
                Log.d("TESTE onPost", jsonAtividades);

                atividadesHashMap = Atividade.atividadesParseJSON(response);
            }
        }
    }

    class PostTask extends AsyncTask<Void, String, String> {

        protected String doInBackground(Void... strings) {
            NetworkUtils.postAllPresencas(getBaseContext());
            return null;
        }

        protected void onPostExecute(String s) {
//            if (!s.equals(null) && !s.equals("timeOutException")) {
//                uploadPD.setProgress(uploadPD.getProgress() + 1);
//                DataBase db = new DataBase(context);
//                db.deleteEntry(cursor.getInt(cursor.getColumnIndex(DataBase.ID)));
//            }
        }
    }
}


