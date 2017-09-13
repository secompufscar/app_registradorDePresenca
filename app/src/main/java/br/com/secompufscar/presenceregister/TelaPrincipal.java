package br.com.secompufscar.presenceregister;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.greycellofp.tastytoast.TastyToast;
import com.microblink.activity.Pdf417ScanActivity;
import com.microblink.recognizers.blinkbarcode.pdf417.Pdf417RecognizerSettings;
import com.microblink.recognizers.settings.RecognitionSettings;
import com.microblink.recognizers.settings.RecognizerSettings;

import java.util.ArrayList;

import br.com.secompufscar.presenceregister.data.Atividade;
import br.com.secompufscar.presenceregister.data.DataBase;
import br.com.secompufscar.presenceregister.data.Presenca;

public class TelaPrincipal extends AppCompatActivity {

    private Button credenciamento_button, atividades_button, enviar_presencas_button, visualizar_inscricao_button;
    private TextView mensagem;
    private SharedPreferences myPrefs;
    private ProgressDialog uploadPD;

    private View contentView;
    private View loadingView;
    private View msgBar;
    private TastyToast msg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        visualizar_inscricao_button = (Button) findViewById(R.id.visualizar_inscr);
        visualizar_inscricao_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                RecognitionSettings recognitionSettings = new RecognitionSettings();
                recognitionSettings.setRecognizerSettingsArray(new RecognizerSettings[]{new Pdf417RecognizerSettings()});
                Intent intent = new Intent(getBaseContext(), DefaultScanActivity.class);
                intent.putExtra(Pdf417ScanActivity.EXTRAS_LICENSE_KEY, getResources().getString(R.string.LICENSE_KEY));
                intent.putExtra(Pdf417ScanActivity.EXTRAS_RECOGNITION_SETTINGS, recognitionSettings);
                intent.putExtra(DefaultScanActivity.EXTRA_ID_ATIVIDADE, "-1");
                startActivity(intent);
            }
        });

        credenciamento_button = (Button) findViewById(R.id.menu_credenciamento);
        credenciamento_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                RecognitionSettings recognitionSettings = new RecognitionSettings();
                recognitionSettings.setRecognizerSettingsArray(new RecognizerSettings[]{new Pdf417RecognizerSettings()});
                Intent intent = new Intent(getBaseContext(), DefaultScanActivity.class);
                intent.putExtra(Pdf417ScanActivity.EXTRAS_LICENSE_KEY, getResources().getString(R.string.LICENSE_KEY));
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

        if (myPrefs.getString("jsonAtividades", "").isEmpty()) {
            new JSONGetTask().execute();
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
        if (item.getItemId() == R.id.help) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.help_title);
            builder.setMessage("\nVisualizar inscrição - Apenas visualiza a inscrição, sem cadastrar NADA no servidor. \n\nCredenciamento - Valida a inscrição e registra a presença dela no evento  \n\nPresença nas atividades - Valida a inscrição em uma atividade selecionada");
            builder.setPositiveButton(R.string.help_opt, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface arg0, int arg1) {
                    Toast.makeText(getApplicationContext(), R.string.help_response, Toast.LENGTH_SHORT).show();
                }
            });
            builder.create().show();
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkPendencias() {
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

                SharedPreferences.Editor mEditor = myPrefs.edit();
                mEditor.putString("jsonAtividades", response);
                mEditor.apply();
            }
        }
    }

    private class PostTask extends AsyncTask<Void, String, ArrayList<Presenca>> {
        @Override
        protected void onPreExecute() {
            uploadPD.setMessage("Sincronizando registros de presenças");
            uploadPD.show();
        }

        protected ArrayList<Presenca> doInBackground(Void... strings) {

            return NetworkUtils.postAllPresencas(getBaseContext());
        }

        @Override
        protected void onPostExecute(ArrayList<Presenca> response) {

            if (response != null) {
                if (response.isEmpty()) {
                    if (DataBase.getDB().getAllEntries().size() == 0){
                        msg.makeText(TelaPrincipal.this, getString(R.string.sucesso), TastyToast.STYLE_CONFIRM).enableSwipeDismiss().show();
                    }
                } else {
                    String texto = String.valueOf(response.size()) + " presenças não válidas foram encontradas";
                    msg.makeText(TelaPrincipal.this, texto, TastyToast.STYLE_ALERT).enableSwipeDismiss().show();
                }
            } else {
                msg.makeText(TelaPrincipal.this, getString(R.string.erro), TastyToast.STYLE_ALERT).enableSwipeDismiss().show();
            }
            uploadPD.dismiss();
            checkPendencias();
        }
    }
}

