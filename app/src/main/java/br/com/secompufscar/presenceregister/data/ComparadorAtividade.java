package br.com.secompufscar.presenceregister.data;

import java.util.Comparator;


public class ComparadorAtividade implements Comparator<Atividade> {
    @Override
    public int compare(Atividade o1, Atividade o2) {
        return o1.getDataHoraInicio().compareTo(o2.getDataHoraInicio());
    }
}