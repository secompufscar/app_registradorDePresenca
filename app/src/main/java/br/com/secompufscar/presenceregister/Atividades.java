package br.com.secompufscar.presenceregister;

import android.content.SharedPreferences;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import br.com.secompufscar.presenceregister.data.Atividade;
import br.com.secompufscar.presenceregister.listasAtividade.ListaQuarta;
import br.com.secompufscar.presenceregister.listasAtividade.ListaQuinta;
import br.com.secompufscar.presenceregister.listasAtividade.ListaSegunda;
import br.com.secompufscar.presenceregister.listasAtividade.ListaSexta;
import br.com.secompufscar.presenceregister.listasAtividade.ListaTerca;

import static br.com.secompufscar.presenceregister.data.Atividade.inicializaAtividadesMap;

public class Atividades extends AppCompatActivity {

    public static HashMap<String, List<Atividade>> atividadesHashMap = inicializaAtividadesMap();
    private SharedPreferences myPrefs;

    private TabLayout tabLayout;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atividades);

        myPrefs = getSharedPreferences("Lista_de_Atividades", MODE_PRIVATE);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public void onResume(){
        super.onResume();
        atividadesHashMap = Atividade.atividadesParseJSON(myPrefs.getString("jsonAtividades", ""));
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        adapter.addFragment(new ListaSegunda(), "Seg");
        adapter.addFragment(new ListaTerca(), "Ter");
        adapter.addFragment(new ListaQuarta(), "Qua");
        adapter.addFragment(new ListaQuinta(), "Qui");
        adapter.addFragment(new ListaSexta(), "Sex");

        viewPager.setAdapter(adapter);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> fragmentList = new ArrayList<>();
        private final List<String> fragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {

            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            fragmentList.add(fragment);
            fragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return fragmentTitleList.get(position);
        }
    }
}
