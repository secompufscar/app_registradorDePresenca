package br.com.secompufscar.presenceregister;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ExpandableListView;

import com.microblink.recognizers.blinkbarcode.pdf417.Pdf417RecognizerSettings;
import com.microblink.recognizers.settings.RecognizerSettings;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class ListaEventos extends AppCompatActivity implements ExpandableListView.OnChildClickListener{
    private HashMap<Integer,ArrayList<Evento>> dados;
    private ExpandableListView expandableView;
    ExpandableAdapter eAdapter;

    private ArrayList<Evento> listaEventos;
    private ArrayList<Integer> daysOfWeek;
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.eventos_lista);
        Intent thisIntent=this.getIntent();
        String jsonString=thisIntent.getStringExtra("JSONString");
        listaEventos=new ArrayList<>();
        try {
            JSONObject jsonObj=new JSONObject(jsonString);
            String arrayName=thisIntent.getStringExtra("Evento");
            JSONArray tempJSONArray=jsonObj.getJSONArray(arrayName);
            for(int j=0;!tempJSONArray.isNull(j);j++){
                Evento tempEvento=Evento.jsonObjectToEvento(tempJSONArray.getJSONObject(j)) ;
                listaEventos.add(tempEvento);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        setTitle("Eventos");
        expandableView=(ExpandableListView) findViewById(R.id.expandableListView);
        dados=new HashMap<>();
        daysOfWeek=new ArrayList<>();

        for(int i=1;i<8;i++){
            ArrayList<Evento> aux= Evento.getEventsInADayOfWeek(i,listaEventos);
            if(aux.size()>0) {
                dados.put(i, aux);
                daysOfWeek.add(i);
            }
        }
        expandableView.setOnChildClickListener(this);
        eAdapter=new ExpandableAdapter(this,dados,daysOfWeek);
        expandableView.setAdapter(eAdapter);
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {


        Evento evento=Evento.getEventsInADayOfWeek( daysOfWeek.get(groupPosition) ,listaEventos ).get(childPosition);
        Intent intent =new Intent(this,AtividadeDetalhes.class);
        intent.putExtra("JSONInfo",evento.encodeAsJSON());
        startActivity(intent);

/*// Intent for Pdf417ScanActivity Activity
        Intent intent = new Intent(this, Pdf417ScanActivity.class);

// set your licence key
// obtain your licence key at http://microblink.com/login or
// contact us at http://help.microblink.com
        intent.putExtra(Pdf417ScanActivity.EXTRAS_LICENSE_KEY, "7TZ4ZHGC-BOHUGR4N-PKSARCSV-KJYOGUIA-PQ5F62VW-M727L5PV-6X27L5PV-6X22I5M2");

        RecognitionSettings settings = new RecognitionSettings();
// setup array of recognition settings (described in chapter "Recognition
// settings and results")
        settings.setRecognizerSettingsArray(setupSettingsArray());
        intent.putExtra(Pdf417ScanActivity.EXTRAS_RECOGNITION_SETTINGS, settings);

// Starting Activity
        startActivityForResult(intent, 1);
        auxGroupPosition=groupPosition;
        auxChildPosition=childPosition;
        auxView=v;*/
        return false;
    }

    private RecognizerSettings[] setupSettingsArray() {
        Pdf417RecognizerSettings sett = new Pdf417RecognizerSettings();
        // disable scanning of white barcodes on black background
        sett.setInverseScanning(false);
        // allow scanning of barcodes that have invalid checksum
        sett.setUncertainScanning(true);
        // disable scanning of barcodes that do not have quiet zone
        // as defined by the standard
        sett.setNullQuietZoneAllowed(false);

        // now add sett to recognizer settings array that is used to configure
        // recognition
        return new RecognizerSettings[] { sett };
    }

}

