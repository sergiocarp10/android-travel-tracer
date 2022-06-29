package cs10.apps.travels.tracer.ui.stops;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import cs10.apps.common.android.CS_Fragment;
import cs10.apps.travels.tracer.R;
import cs10.apps.travels.tracer.Utils;
import cs10.apps.travels.tracer.adapter.LocatedArrivalAdapter;
import cs10.apps.travels.tracer.databinding.FragmentArrivalsBinding;
import cs10.apps.travels.tracer.db.DynamicQuery;
import cs10.apps.travels.tracer.db.MiDB;
import cs10.apps.travels.tracer.model.Viaje;
import cs10.apps.travels.tracer.model.roca.ArriboTren;
import cs10.apps.travels.tracer.model.roca.HorarioTren;
import cs10.apps.travels.tracer.model.roca.RamalSchedule;
import cs10.apps.travels.tracer.ui.service.ServiceDetail;
import cs10.apps.travels.tracer.viewmodel.LocatedArrivalVM;

public class StopArrivalsFragment extends CS_Fragment {
    private FragmentArrivalsBinding binding;
    private LocatedArrivalAdapter adapter;
    private String stopName;

    // ViewModel
    private LocatedArrivalVM locatedArrivalVM;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentArrivalsBinding.inflate(inflater, container, false);
        locatedArrivalVM = new ViewModelProvider(this).get(LocatedArrivalVM.class);

        locatedArrivalVM.getStopName().observe(getViewLifecycleOwner(), s ->
                binding.tvTitle.setText(getString(R.string.next_ones_in, s))
        );

        locatedArrivalVM.getProximity().observe(getViewLifecycleOwner(), proximity ->
                binding.tvSubtitle.setText(getString(R.string.proximity_porcentage, Math.round(proximity*100)))
        );

        locatedArrivalVM.getArrivals().observe(getViewLifecycleOwner(), arrivals -> {
            int ogSize = adapter.getItemCount();
            adapter.setList(arrivals);

            if (ogSize == 0) adapter.notifyItemRangeInserted(0, arrivals.size());
            else adapter.notifyDataSetChanged();
        });

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new LocatedArrivalAdapter(new LinkedList<>(), arriboTren -> {
            onServiceSelected(arriboTren.getServiceId(), arriboTren.getRamal());
            return null;
        });

        binding.recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recycler.setAdapter(adapter);

        // get arguments
        Bundle args = getArguments();

        if (args != null){
            stopName = args.getString("stopName");
            locatedArrivalVM.getStopName().postValue(stopName);
            locatedArrivalVM.getProximity().postValue(args.getDouble("proximity"));
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        doInBackground(() -> {
            MiDB miDB = MiDB.getInstance(getContext());
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int m = calendar.get(Calendar.MINUTE);
            int now = hour * 60 + m;

            List<Viaje> arrivals = DynamicQuery.getNextBusArrivals(getContext(), stopName);
            List<RamalSchedule> trenes = DynamicQuery.getNextTrainArrivals(getContext(), stopName);

            for (RamalSchedule tren : trenes){
                ArriboTren v = new ArriboTren();
                int target = tren.getHour() * 60 + tren.getMinute();
                HorarioTren end = miDB.servicioDao().getFinalStation(tren.getService());

                v.setTipo(1);
                v.setRamal(tren.getRamal());
                v.setStartHour(tren.getHour());
                v.setStartMinute(tren.getMinute());
                v.setServiceId(tren.getService());
                v.setNombrePdaFin(Utils.simplify(end.getStation()));
                v.setNombrePdaInicio(tren.getCabecera());
                v.setRecorrido(miDB.servicioDao().getRecorridoUntil(tren.getService(), now, target));
                v.setRecorridoDestino(miDB.servicioDao().getRecorridoFrom(tren.getService(), target));
                v.setEndHour(end.getHour());
                v.setEndMinute(end.getMinute());
                v.restartAux();
                arrivals.add(v);
            }

            Collections.sort(arrivals);

            doInForeground(() -> locatedArrivalVM.getArrivals().postValue(arrivals));
        });
    }

    private void onServiceSelected(long id, String ramal) {
        Intent intent = new Intent(getActivity(), ServiceDetail.class);
        intent.putExtra("station", stopName);
        intent.putExtra("ramal", ramal);
        intent.putExtra("id", id);
        startActivity(intent);
    }
}
