package br.com.secompufscar.presenceregister;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by Italo on 20/03/2016.
 */
public class ExpandableAdapter extends BaseExpandableListAdapter {
    private HashMap<Integer,ArrayList<Evento>> dados;
    private ArrayList<Integer> listaDias;
    private Context context;
    public ExpandableAdapter(Context context,HashMap<Integer,ArrayList<Evento>> dados,ArrayList<Integer> listaDias){
        this.context=context;
        this.dados=dados;
        this.listaDias=listaDias;

    }
    @Override
    public int getGroupCount() {
        return dados.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return dados.get(listaDias.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return dados.get(listaDias.get(groupPosition));
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return dados.get(listaDias.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return 0;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.header_expandable_list, null);
        }

        TextView tv= (TextView) v.findViewById(R.id.expandableHeaderText);
        GregorianCalendar gregorianCalendar =new GregorianCalendar();
        gregorianCalendar.setTime(((ArrayList<Evento>) getGroup(groupPosition)).get(0).getData());
        tv.setText(gregorianCalendar.getDisplayName(GregorianCalendar.DAY_OF_WEEK,GregorianCalendar.LONG, Locale.getDefault()));

        return v;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        View v = convertView;

        if (v == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.child_expandable_list, null);
        }
        TextView nomeTV= (TextView) v.findViewById(R.id.eventoTV);
        TextView palestranteTV=(TextView) v.findViewById(R.id.palestranteTV);
        nomeTV.setText(dados.get(listaDias.get(groupPosition)).get(childPosition).getNome());
        palestranteTV.setText(dados.get(listaDias.get(groupPosition)).get(childPosition).getMinistrante());
        return v;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

}
