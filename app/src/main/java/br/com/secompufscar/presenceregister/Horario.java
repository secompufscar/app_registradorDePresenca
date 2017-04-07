package br.com.secompufscar.presenceregister;

/**
 * Created by Italo on 29/08/2016.
 */
public class Horario {
    private int horas;
    private int minutos;

    public Horario(){}
    public Horario(int horas,int minutos){
        this.horas=horas;
        this.minutos=minutos;
    }
    public void setHorario(int horas,int minutos){
        this.horas=horas;
        this.minutos=minutos;
    }
    public int getHoras(){
        return horas;
    }
    public int getMinutos(){
        return minutos;
    }

}
