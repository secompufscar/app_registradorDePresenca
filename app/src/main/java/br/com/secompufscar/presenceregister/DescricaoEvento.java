package br.com.secompufscar.presenceregister;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.microblink.activity.Pdf417ScanActivity;
import com.microblink.recognizers.blinkbarcode.pdf417.Pdf417RecognizerSettings;
import com.microblink.recognizers.settings.RecognitionSettings;
import com.microblink.recognizers.settings.RecognizerSettings;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

public class DescricaoEvento extends AppCompatActivity implements OnClickListener {
    private TextView nome,ministrante,descricao,local,horarioInicio;
    private Button escanear;
    private final String LICENSE_KEY="IQWMRXJD-C4TYRTMF-6M3YXYFF-RG7NYYKH-ZXCS2JJR-KDPAOZKE-RQKKOQOM-ZA2IHPNL";
    private  Evento evento=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_descricao_evento);
        String JSONString = this.getIntent().getStringExtra("JSONInfo");

        try {
             evento= Evento.jsonObjectToEvento(new JSONObject(JSONString));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        nome=(TextView)findViewById(R.id.DENome);
        ministrante=(TextView)findViewById(R.id.DEPalestrante);
        descricao=(TextView) findViewById(R.id.DEDescricao);
        local=(TextView) findViewById(R.id.LocalTF);
        horarioInicio=(TextView) findViewById(R.id.horarioDeInicio);
        escanear=(Button) findViewById(R.id.Escanear);
        escanear.setOnClickListener(this);

        nome.setText(evento.getNome());
        if(evento.getMinistrante().isEmpty())
            ministrante.setVisibility(View.GONE);
        else
            ministrante.setText("Ministrante(s): " + evento.getMinistrante());
        if(evento.getLocal().isEmpty())
            local.setVisibility(View.GONE);
        else
            local.setText("Local: " + evento.getLocal());

        GregorianCalendar calendar=new GregorianCalendar();
        calendar.setTime(evento.getData());

        horarioInicio.setText("Horario: "+evento.getHorarioInicio());
        descricao.setText(evento.getDescricao());

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_descricao_evento, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        // prepare recognition settings
        // enable PDF417 recognizer and QR code recognizer from ZXing

        RecognitionSettings recognitionSettings = new RecognitionSettings();
        // add settings objects to recognizer settings array
        // Pdf417Recognizer and ZXingRecognizer will be used in the recognition process
        recognitionSettings.setRecognizerSettingsArray(new RecognizerSettings[]{new Pdf417RecognizerSettings()});
        // create intent for custom scan activity
        Intent intent = new Intent(this, DefaultScanActivity.class);
        // add license that allows creating custom camera overlay
        intent.putExtra(Pdf417ScanActivity.EXTRAS_LICENSE_KEY, LICENSE_KEY);
        intent.putExtra("CodigoEvento",String.valueOf(evento.getEventoID()));
        intent.putExtra(Pdf417ScanActivity.EXTRAS_RECOGNITION_SETTINGS, recognitionSettings);

        startActivity(intent);

    }

}
