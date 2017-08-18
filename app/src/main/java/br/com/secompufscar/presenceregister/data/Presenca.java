package br.com.secompufscar.presenceregister.data;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class Presenca {
    private String id_participante, id_atividade, horario;
    private int id_presenca;

    @Override
    public String toString(){
        return String.valueOf(this.id_presenca) + ", Atividade: " + this.id_atividade
                + ", Participante: " + this.id_participante
                + ", Horario: " + this.horario;
    }

    public static String getCurrentTime(){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        return formatter.format(new Date());
    }

    public Presenca(){
        id_participante = "";
        id_atividade = "";
        horario = "";
    }

    public Presenca(String id_presenca, String id_participante, String id_atividade, String horario){
        this.id_presenca = Integer.parseInt(id_presenca);
        this.id_atividade = id_atividade;
        this.id_participante = id_participante;
        this.horario = horario;
    }

    public void setId(String id){
        this.id_presenca = Integer.parseInt(id);;
    }

    public void setIdParticipante(String id){
        this.id_participante = id;
    }

    public void setIdAtividade(String id){
        this.id_atividade = id;
    }

    public void setHorario(String horario){
        this.horario = horario;
    }

    public int getId(){
        return this.id_presenca;
    }

    public String getIdParticipante(){
        return this.id_participante;
    }

    public String getIdAtividade(){
        return this.id_atividade;
    }

    public String getHorario(){
        return this.horario;
    }
}
