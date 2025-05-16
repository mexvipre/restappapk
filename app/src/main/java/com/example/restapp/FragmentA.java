package com.example.restapp;

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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class FragmentA extends Fragment {

    private RecyclerView recyclerView;
    private SoftwareAdapter adapter;
    private final ArrayList<Software> softwareList = new ArrayList<>();
    private EditText inputIdBuscar;
    private Button btnBuscar;

    public FragmentA() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_a, container, false);

        recyclerView = rootView.findViewById(R.id.recyclerSoftwares);
        inputIdBuscar = rootView.findViewById(R.id.inputIdBuscar);
        btnBuscar = rootView.findViewById(R.id.btnBuscar);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new SoftwareAdapter(softwareList);
        recyclerView.setAdapter(adapter);

        fetchSoftwares();

        btnBuscar.setOnClickListener(v -> {
            String idText = inputIdBuscar.getText().toString().trim();
            if (idText.isEmpty()) {
                fetchSoftwares();
            } else {
                fetchSoftwareById(idText);
            }
        });

        return rootView;
    }

    private void fetchSoftwares() {
        new Thread(() -> {
            String json = getSoftwaresFromApi();
            if (json == null || json.isEmpty()) {
                showToastOnMainThread("No se pudo obtener la lista de softwares");
                return;
            }

            try {
                JSONArray array = new JSONArray(json);
                ArrayList<Software> tempList = new ArrayList<>();

                for (int i = 0; i < array.length(); i++) {
                    JSONObject obj = array.getJSONObject(i);

                    int id = obj.optInt("id", 0);
                    String nombre = obj.optString("nombre", "Sin nombre");
                    String version = obj.optString("versionsoft", "N/A");
                    int espacio = obj.optInt("espaciomb", 0);
                    String precio = obj.optString("precio", "0.00");

                    String descripcion = "Versión: " + version +
                            " | Espacio: " + espacio + " MB\n" +
                            "Precio: $" + precio;


                    Software software = new Software(id, nombre, descripcion, version, String.valueOf(espacio), precio);
                    tempList.add(software);
                }

                new Handler(Looper.getMainLooper()).post(() -> {
                    softwareList.clear();
                    softwareList.addAll(tempList);
                    adapter.notifyDataSetChanged();
                });

            } catch (Exception e) {
                e.printStackTrace();
                showToastOnMainThread("Error al procesar datos de softwares");
            }
        }).start();
    }

    private void fetchSoftwareById(String id) {
        new Thread(() -> {
            String urlString = "https://cruds-rest-software-production.up.railway.app/api/softwares/" + id;
            StringBuilder result = new StringBuilder();

            try {
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                int responseCode = conn.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    reader.close();

                    JSONObject obj = new JSONObject(result.toString());

                    if (obj.length() == 0) {
                        showToastOnMainThread("No se encontró software con ID: " + id);
                        clearListOnMainThread();
                        return;
                    }

                    int idSoftware = obj.optInt("id", 0);
                    String nombre = obj.optString("nombre", "Sin nombre");
                    String version = obj.optString("versionsoft", "N/A");
                    int espacio = obj.optInt("espaciomb", 0);
                    String precio = obj.optString("precio", "0.00");


                    String descripcion = "Versión: " + version +
                            " | Espacio: " + espacio + " MB\n" +
                            "Precio: $" + precio;

                    Software software = new Software(idSoftware, nombre, descripcion, version, String.valueOf(espacio), precio);

                    new Handler(Looper.getMainLooper()).post(() -> {
                        softwareList.clear();
                        softwareList.add(software);
                        adapter.notifyDataSetChanged();
                    });

                } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                    showToastOnMainThread("No se encontró software con ID: " + id);
                    clearListOnMainThread();

                } else {
                    showToastOnMainThread("Error al buscar software (Código: " + responseCode + ")");
                    clearListOnMainThread();
                }

                conn.disconnect();

            } catch (Exception e) {
                e.printStackTrace();
                showToastOnMainThread("Error de conexión al buscar software");
                clearListOnMainThread();
            }
        }).start();
    }

    private String getSoftwaresFromApi() {
        String urlString = "https://cruds-rest-software-production.up.railway.app/api/softwares";
        StringBuilder result = new StringBuilder();

        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            int responseCode = conn.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                reader.close();
            } else {
                showToastOnMainThread("Error al obtener lista de softwares (Código: " + responseCode + ")");
            }

            conn.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
            showToastOnMainThread("Error de conexión al obtener lista de softwares");
        }

        return result.toString();
    }

    private void showToastOnMainThread(String message) {
        new Handler(Looper.getMainLooper()).post(() ->
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show());
    }

    private void clearListOnMainThread() {
        new Handler(Looper.getMainLooper()).post(() -> {
            softwareList.clear();
            adapter.notifyDataSetChanged();
        });
    }
}
