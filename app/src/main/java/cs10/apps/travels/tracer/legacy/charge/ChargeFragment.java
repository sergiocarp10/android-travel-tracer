package cs10.apps.travels.tracer.legacy.charge;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Calendar;

import cs10.apps.travels.tracer.R;
import cs10.apps.travels.tracer.utils.Utils;
import cs10.apps.common.android.ui.CS_Fragment;
import cs10.apps.travels.tracer.databinding.FragmentChargeBinding;
import cs10.apps.travels.tracer.databinding.ViewCircularButtonBinding;
import cs10.apps.travels.tracer.db.MiDB;
import cs10.apps.travels.tracer.pages.month_summary.db.RecargaDao;
import cs10.apps.travels.tracer.model.Recarga;

public class ChargeFragment extends CS_Fragment implements ChargeButtonCallback {
    private FragmentChargeBinding binding;
    private static final String[] DEFAULT_VALUES = {"$200", "$500", "$750", "$1000", "$1500"};
    private final ChargeButton[] buttons = new ChargeButton[DEFAULT_VALUES.length];
    private int valueSelected;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentChargeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final ViewCircularButtonBinding[] btn = new ViewCircularButtonBinding[]{
                binding.btn1, binding.btn2, binding.btn3, binding.btn4, binding.btn5};

        for (int i=0; i<buttons.length; i++){
            buttons[i] = new ChargeButton(btn[i], i);
            buttons[i].setLabel(DEFAULT_VALUES[i]);
            buttons[i].setCallback(this);
            buttons[i].deselect();
        }

        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH) + 1;
        int year = calendar.get(Calendar.YEAR);
        binding.etDate.setText(Utils.dateFormat(day, month, year));

        binding.confirmButton.setOnClickListener(view1 -> onConfirm());
    }

    @Override
    public void onResume() {
        super.onResume();

        doInBackground(() -> {
            Recarga last = MiDB.getInstance(getContext()).recargaDao().getLastInserted();
            if (last != null) doInForeground(() -> completeLastInsertedInfo(last));
        });
    }

    private void completeLastInsertedInfo(Recarga last) {
        binding.lastChargeInfo.setText(getString(R.string.last_charge_info, Math.round(last.getMount()),
                        Utils.dateFormat(last.getDay(), last.getMonth(), last.getYear())));
    }

    @Override
    public void updateChargeSelected(int value, int index){
        valueSelected = value;

        for (int i=0; i<buttons.length; i++){
            if (i != index) buttons[i].deselect();
        }
    }

    public void onConfirm(){
        if (valueSelected == 0){
            Toast.makeText(getContext(), "Seleccione un monto para continuar", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] params = binding.etDate.getText().toString().split("/");
        if (params.length < 3){
            binding.etDate.setError("Ingrese una fecha válida");
            return;
        }

        try {
            Recarga recarga = new Recarga();
            recarga.setDay(Integer.parseInt(params[0]));
            recarga.setMonth(Integer.parseInt(params[1]));
            recarga.setYear(Integer.parseInt(params[2]));
            recarga.setMount(valueSelected);

            // guardar en base de datos
            new Thread(() -> {
                RecargaDao dao = MiDB.getInstance(getContext()).recargaDao();
                dao.insert(recarga);

                // mostrar pantalla de inicio
                if (getActivity() != null) getActivity().runOnUiThread(() -> getActivity().onBackPressed());
            }).start();
        } catch (NumberFormatException e){
            binding.etDate.setError("Ingrese una fecha válida");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}