package br.com.secompufscar.presenceregister;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.microblink.activity.Pdf417ScanActivity;
import com.microblink.recognizers.blinkbarcode.pdf417.Pdf417RecognizerSettings;
import com.microblink.recognizers.settings.RecognitionSettings;
import com.microblink.recognizers.settings.RecognizerSettings;


import br.com.secompufscar.presenceregister.data.Atividade;

public class AtividadeDetalhes extends AppCompatActivity implements OnClickListener {
    public static final String EXTRA_POSITION = "position";
    public static final String EXTRA_DIA = "dia_semana";

    private TextView nome,ministrante,descricao,local,horarioInicio;
    private Button escanear;
    private Atividade atividade;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atividade_detalhes);
        int posicao = this.getIntent().getIntExtra(EXTRA_POSITION,0);
        String dia_semana = this.getIntent().getStringExtra(EXTRA_DIA);

        atividade = Atividades.atividadesHashMap.get(dia_semana).get(posicao);

        nome=(TextView)findViewById(R.id.atividade_detalhe_titulo);
        nome.setText(atividade.getTitulo());

        ministrante=(TextView)findViewById(R.id.atividade_detalhe_ministrantes);

        if(atividade.getMinistrantes() != null){
            ministrante.setText("Ministrante(s): " + atividade.getMinistrantes());
        } else{
            ministrante.setVisibility(View.GONE);
        }

        local=(TextView) findViewById(R.id.atividade_detalhe_local);

        if(atividade.getLocal() != null){
            local.setText("Local: " + atividade.getLocal());
        } else{
            local.setText(R.string.atividade_indisponivel_local);
        }

        horarioInicio=(TextView) findViewById(R.id.atividade_detalhe_horarios);

        if(atividade.getHorarios() != null){
            horarioInicio.setText("Horarios: " + atividade.getHorarios());
        } else{
            horarioInicio.setVisibility(View.GONE);
        }

        escanear=(Button) findViewById(R.id.Escanear);

        escanear.setOnClickListener(this);
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
        intent.putExtra(Pdf417ScanActivity.EXTRAS_LICENSE_KEY, NetworkUtils.LICENSE_KEY);
        intent.putExtra(DefaultScanActivity.EXTRA_ID_ATIVIDADE,String.valueOf(atividade.getId()));
        intent.putExtra(Pdf417ScanActivity.EXTRAS_RECOGNITION_SETTINGS, recognitionSettings);
        startActivity(intent);
    }

}
