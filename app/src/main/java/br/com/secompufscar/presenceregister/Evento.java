package br.com.secompufscar.presenceregister;

import android.provider.ContactsContract;
import android.text.format.DateFormat;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * Created by Italo on 20/03/2016.
 */
public class Evento {
    private String nome;
    private String ministrante;
    private int eventoID;
    private Date data;
    private String horarioInicio;
    private String horarioFim;
    private String local;
    private String urlImagem;
    private String descricao;

    public static ArrayList<Evento> listaEventos=new ArrayList<>();

    public Evento(String nome,String ministrante,int eventoID,Date data){
        this.nome=nome;
        this.ministrante=ministrante;
        this.eventoID=eventoID;
        this.data=data;
    }

    public Evento(String nome,int ID,Date data){
        this.nome=nome;
        this.eventoID=ID;
        this.data=data;
    }
    public Evento(){
          nome="";
          ministrante="";
          horarioInicio="";
          horarioFim="";
          local="";
          urlImagem="";
          descricao="";
    }

    public void setURLImagem(String urlImagem){
        this.urlImagem=urlImagem;
    }

    public void setDescricao(String descricao){

        this.descricao=descricao;
    }

    public void setLocal(String local){
        this.local=local;
    }
    public String getHorarioInicio(){
        return horarioInicio;
    }
    public void setHorarioInicio(String horarioInicio){
        this.horarioInicio=horarioInicio;
    }
    public void setHorarioFim(String horarioFim){
        this.horarioFim=horarioFim;
    }
    public void setNome(String nome){
        this.nome=nome;
    }
    public void setMinistrante(String ministrante){
        this.ministrante=ministrante;
    }
    public void setData(String datal){
        GregorianCalendar cal = new GregorianCalendar();
        int ano=2016;
        int dia=Integer.valueOf(datal.substring(0, datal.indexOf("/") ));
        int mes=Integer.valueOf(datal.substring(datal.indexOf("/") + 1,datal.length()))-1;
        Log.d("data",ano+" "+mes+"  "+dia+"  "+"\n");
        cal.set(ano, mes, dia,12,00,00);
        data= cal.getTime();

    }


    public String getNome(){
        return nome;
    }

    public String getMinistrante(){
        return ministrante;
    }

    public int getEventoID(){
        return eventoID;
    }

    public Date getData(){
        return data;
    }

    public int getDayOfWeek(){
        GregorianCalendar calendar=new GregorianCalendar();
        calendar.setTime(getData());
        return calendar.get(GregorianCalendar.DAY_OF_WEEK);
    }

    public static  ArrayList<Evento> getEventsInADayOfWeek(int dayOfWeek,ArrayList<Evento> listaEventos){
        ArrayList<Evento> listaAux=new ArrayList<>();
        for(int i=0;i<listaEventos.size();i++){
            if(dayOfWeek==listaEventos.get(i).getDayOfWeek())
                listaAux.add(listaEventos.get(i));
        }
        return listaAux;
    }
    public void setEventoID(int id){
        eventoID=id;
    }

    static public Evento jsonObjectToEvento(JSONObject jsonObject){
        Evento tempEvento=new Evento();
        try {


            tempEvento.setNome(jsonObject.getString("nome_atividade"));
            tempEvento.setData(jsonObject.getString("data_inicio_atividade"));
            tempEvento.setEventoID(jsonObject.getInt("id_atividade"));
            tempEvento.setHorarioInicio(jsonObject.getString("hora_inicio_atividade"));
            tempEvento.setDescricao(jsonObject.getString("descricao_atividade"));
            tempEvento.setHorarioFim(jsonObject.getString("hora_fim_atividade"));
            tempEvento.setMinistrante(jsonObject.getString("ministrante_atividade"));
            tempEvento.setLocal(jsonObject.getString("local_atividade"));
            tempEvento.setURLImagem(jsonObject.getString("foto_atividade"));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return tempEvento;
    }
    public String getDescricao(){
        return descricao;
    }
    public String getLocal(){
        return local;
    }
    public String encodeAsJSON(){
        GregorianCalendar calendar=new GregorianCalendar();
        calendar.setTime(getData());
        String data=String.valueOf(calendar.get(GregorianCalendar.DAY_OF_MONTH))+"/"+String.valueOf(calendar.get(GregorianCalendar.MONTH));
        if(descricao!=null)
            descricao=descricao.replace("\"","\\\"");
        if(local!=null)
            local=local.replace("\"","\\\"");
        nome=nome.replace("\"","\\\"");
        if(ministrante!=null)
            ministrante=ministrante.replace("\"","\\\"");

        String JSONString =
                "{ \"data_inicio_atividade\": \""+data+"\","
              +  "\"foto_atividade\": \""+urlImagem+" \","
              +  "\"hora_inicio_atividade\": \""+horarioInicio+"\","
              +  "\"local_atividade\": \""+local+"\","
              +  "\"nome_atividade\": \""+nome+"\","
              +  "\"hora_fim_atividade\": \""+horarioFim+"\","
              +  "\"ministrante_atividade\": \""+ministrante+"\","
              +  "\"id_atividade\": "+eventoID+","
              +  "\"descricao_atividade\": \""+descricao+"\"}";
        Log.d("Json",JSONString);

        return JSONString;
    }

}
