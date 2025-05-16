package com.example.restapp;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class FragmentB extends Fragment {

    private EditText inputNombre, inputVersion, inputEspacio, inputPrecio;
    private Button btnGuardar;

    public FragmentB() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_b, container, false);

        inputNombre = view.findViewById(R.id.inputNombre);
        inputVersion = view.findViewById(R.id.inputVersion);
        inputEspacio = view.findViewById(R.id.inputEspacio);
        inputPrecio = view.findViewById(R.id.inputPrecio);
        btnGuardar = view.findViewById(R.id.btnGuardar);

        btnGuardar.setOnClickListener(v -> confirmarRegistro());

        return view;
    }

    private void confirmarRegistro() {
        String nombre = inputNombre.getText().toString();
        String version = inputVersion.getText().toString();
        String espacio = inputEspacio.getText().toString();
        String precio = inputPrecio.getText().toString();

        if (nombre.isEmpty() || version.isEmpty() || espacio.isEmpty() || precio.isEmpty()) {
            Toast.makeText(getContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(getContext())
                .setTitle("Confirmar")
                .setMessage("¿Estás seguro de registrar este software?")
                .setPositiveButton("Sí", (dialog, which) -> guardarSoftware(nombre, version, espacio, precio))
                .setNegativeButton("No", null)
                .show();
    }

    private void guardarSoftware(String nombre, String version, String espacioStr, String precio) {
        int espacio = Integer.parseInt(espacioStr);

        new Thread(() -> {
            try {
                URL url = new URL("https://cruds-rest-software-production.up.railway.app/api/softwares");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                JSONObject json = new JSONObject();
                json.put("nombre", nombre);
                json.put("versionsoft", version);
                json.put("espaciomb", espacio);
                json.put("precio", precio);

                OutputStream os = conn.getOutputStream();
                os.write(json.toString().getBytes("UTF-8"));
                os.close();

                int responseCode = conn.getResponseCode();

                new Handler(Looper.getMainLooper()).post(() -> {
                    if (responseCode == HttpURLConnection.HTTP_CREATED || responseCode == HttpURLConnection.HTTP_OK) {
                        Toast.makeText(getContext(), "Software registrado con éxito", Toast.LENGTH_SHORT).show();
                        limpiarCampos();
                        volverAFragmentA();
                    } else {
                        Toast.makeText(getContext(), "Error al registrar: " + responseCode, Toast.LENGTH_LONG).show();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                new Handler(Looper.getMainLooper()).post(() ->
                        Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    private void limpiarCampos() {
        inputNombre.setText("");
        inputVersion.setText("");
        inputEspacio.setText("");
        inputPrecio.setText("");
    }

    private void volverAFragmentA() {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, new FragmentA());
        transaction.commit();
    }
}
