package br.com.secompufscar.presenceregister;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.microblink.activity.Pdf417ScanActivity;
import com.microblink.recognizers.blinkbarcode.pdf417.Pdf417RecognizerSettings;
import com.microblink.recognizers.settings.RecognitionSettings;
import com.microblink.recognizers.settings.RecognizerSettings;

import java.util.HashMap;
import java.util.List;

import br.com.secompufscar.presenceregister.data.Atividade;
import br.com.secompufscar.presenceregister.data.DataBase;

import static br.com.secompufscar.presenceregister.data.Atividade.inicializaAtividadesMap;

public class TelaPrincipal extends AppCompatActivity {

    public static HashMap<String, List<Atividade>> atividadesHashMap = inicializaAtividadesMap();

    private Button credenciamento_button, atividades_button, enviar_presencas_button;
    private TextView mensagem;
    private SharedPreferences myPrefs;
    private ProgressDialog uploadPD;

    private View contentView;
    private View loadingView;
    private View msgBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        NetworkUtils.inicializeNetworkUtils(getResources().getString(R.string.LICENSE_KEY));

        setContentView(R.layout.activity_tela_principal);

        contentView = findViewById(R.id.botoes_grid);
        loadingView = findViewById(R.id.loading_spinner);
        loadingView.setVisibility(View.GONE);

        msgBar = findViewById(R.id.msg_bar);
        msgBar.setVisibility(View.GONE);

        uploadPD = new ProgressDialog(this);

        enviar_presencas_button = (Button) findViewById(R.id.msg_button);
        enviar_presencas_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                new PostTask().execute();
            }
        });

        mensagem = (TextView) findViewById(R.id.msg);

        credenciamento_button = (Button) findViewById(R.id.menu_credenciamento);
        credenciamento_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                RecognitionSettings recognitionSettings = new RecognitionSettings();
                recognitionSettings.setRecognizerSettingsArray(new RecognizerSettings[]{new Pdf417RecognizerSettings()});
                Intent intent = new Intent(getBaseContext(), DefaultScanActivity.class);
                intent.putExtra(Pdf417ScanActivity.EXTRAS_LICENSE_KEY, NetworkUtils.LICENSE_KEY);
                intent.putExtra(Pdf417ScanActivity.EXTRAS_RECOGNITION_SETTINGS, recognitionSettings);
                intent.putExtra(DefaultScanActivity.EXTRA_ID_ATIVIDADE, "0");
                startActivity(intent);
            }
        });

        atividades_button = (Button) findViewById(R.id.atividades_button);
        atividades_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(getBaseContext(), Atividades.class));
            }
        });

        myPrefs = getSharedPreferences("Lista_de_Atividades", MODE_PRIVATE);

        String jsonAtividades = myPrefs.getString("jsonAtividades", "");

        if (jsonAtividades.isEmpty()) {
            new JSONGetTask().execute();
        } else {
            atividadesHashMap = Atividade.atividadesParseJSON(jsonAtividades);
        }

        DataBase.setInstance(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPendencias();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.upload_menu, menu);//Menu Resource, Menu
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.refresh_button) {
            new JSONGetTask().execute();

        }
        else {
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

    private void checkPendencias(){
        int nro_pendencias = DataBase.getDB().getCountPresencas();
        if (nro_pendencias > 0) {
            msgBar.setVisibility(View.VISIBLE);
            mensagem.setText(getResources().getString(R.string.msg_dados_pendentes, nro_pendencias));
        } else {
            msgBar.setVisibility(View.GONE);
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

            if (response != null && !response.isEmpty()) {
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

    class PostTask extends AsyncTask<Void, String, Boolean> {

        @Override
        protected void onPreExecute(){
                uploadPD.setMessage("Sincronizando registros de presenças");
                uploadPD.show();
        }

        protected Boolean doInBackground(Void... strings) {

            return NetworkUtils.postAllPresencas(getBaseContext());
        }

        @Override
        protected void onPostExecute(Boolean response) {
            if(response){
                Toast.makeText(getBaseContext(), "Sucesso", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getBaseContext(), "Erro", Toast.LENGTH_SHORT).show();
            }
            uploadPD.dismiss();
            checkPendencias();
        }
    }
}


