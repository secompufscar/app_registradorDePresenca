package br.com.secompufscar.presenceregister.listasAtividade;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import br.com.secompufscar.presenceregister.AtividadeDetalhes;
import br.com.secompufscar.presenceregister.Atividades;
import br.com.secompufscar.presenceregister.AtividadesAdapter;
import br.com.secompufscar.presenceregister.ClickListener;
import br.com.secompufscar.presenceregister.R;
import br.com.secompufscar.presenceregister.RecyclerTouchListener;
import br.com.secompufscar.presenceregister.TelaPrincipal;
import br.com.secompufscar.presenceregister.data.Atividade;

public class ListaSegunda extends Fragment {

    private static final String dia_semana = "SEG";


    public static List<Atividade> atividadeList = new ArrayList<>();
    private RecyclerView recycler_atividades;
    private AtividadesAdapter adapter;

    public ListaSegunda() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_lista_atividades, container, false);
        recycler_atividades = (RecyclerView) view.findViewById(R.id.recycler_atividades);

        adapter = new AtividadesAdapter(getActivity(), atividadeList);
        recycler_atividades.setAdapter(adapter);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        recycler_atividades.setLayoutManager(mLayoutManager);

        recycler_atividades.addOnItemTouchListener(new RecyclerTouchListener(getActivity().getApplicationContext(), recycler_atividades, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Atividade atividade = atividadeList.get(position);

                if (atividade.getTitulo().toLowerCase().equals("credenciamento")) {
                    Toast.makeText(getContext(), "Utilize o botão de credenciamento na tela principal", Toast.LENGTH_SHORT).show();
                } else {
                    Context context = view.getContext();
                    Intent detalhesAtividade = new Intent(context, AtividadeDetalhes.class);
                    detalhesAtividade.putExtra(AtividadeDetalhes.EXTRA_POSITION, position);
                    detalhesAtividade.putExtra(AtividadeDetalhes.EXTRA_DIA, atividade.getHorarioDiaSemana());
                    context.startActivity(detalhesAtividade);
                }
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        atividadeList.clear();
        try {
            atividadeList.addAll(Atividades.atividadesHashMap.get(dia_semana));
        } catch (Exception e) {
            e.printStackTrace();
        }
        adapter.notifyDataSetChanged();
    }
}
