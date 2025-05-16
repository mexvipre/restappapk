package com.example.restapp;

import android.app.AlertDialog;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class SoftwareAdapter extends RecyclerView.Adapter<SoftwareAdapter.SoftwareViewHolder> {

    private final ArrayList<Software> softwareList;

    public SoftwareAdapter(ArrayList<Software> softwareList) {
        this.softwareList = softwareList;
    }

    @NonNull
    @Override
    public SoftwareViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_software, parent, false);
        return new SoftwareViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SoftwareViewHolder holder, int position) {
        Software software = softwareList.get(position);
        holder.tvId.setText("ID: " + software.getId());
        holder.tvNombre.setText(software.getNombre());
        holder.tvDescripcion.setText(software.getDescripcion());

        // Listener para botón eliminar
        holder.btnEliminar.setOnClickListener(v -> {
            new AlertDialog.Builder(holder.itemView.getContext())
                    .setTitle("¿Eliminar software?")
                    .setMessage("¿Estás seguro de que deseas eliminar el software con ID " + software.getId() + "?")
                    .setPositiveButton("Sí", (dialog, which) -> eliminarSoftwarePorId(software.getId(), position, holder))
                    .setNegativeButton("No", null)
                    .show();
        });

        // Listener para botón editar
        holder.btnEditar.setOnClickListener(v -> mostrarDialogEditar(software, position, holder));
    }

    private void eliminarSoftwarePorId(int id, int position, SoftwareViewHolder holder) {
        new Thread(() -> {
            try {
                URL url = new URL("https://cruds-rest-software-production.up.railway.app/api/softwares/" + id);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("DELETE");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                int responseCode = conn.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        softwareList.remove(position);
                        notifyItemRemoved(position);
                        Toast.makeText(holder.itemView.getContext(), "Software eliminado", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    mostrarToast(holder, "Error al eliminar: Código " + responseCode);
                }

                conn.disconnect();

            } catch (Exception e) {
                e.printStackTrace();
                mostrarToast(holder, "Error de conexión");
            }
        }).start();
    }

    private void mostrarToast(SoftwareViewHolder holder, String mensaje) {
        new Handler(Looper.getMainLooper()).post(() ->
                Toast.makeText(holder.itemView.getContext(), mensaje, Toast.LENGTH_SHORT).show()
        );
    }

    private void mostrarDialogEditar(Software software, int position, SoftwareViewHolder holder) {
        View dialogView = LayoutInflater.from(holder.itemView.getContext()).inflate(R.layout.dialog_edit_software, null);
        EditText editNombre = dialogView.findViewById(R.id.editNombre);
        EditText editVersion = dialogView.findViewById(R.id.editVersion);
        EditText editEspacio = dialogView.findViewById(R.id.editEspacio);
        EditText editPrecio = dialogView.findViewById(R.id.editPrecio);

        // Autocompletar campos
        editNombre.setText(software.getNombre());
        editVersion.setText(software.getVersionsoft());
        editEspacio.setText(software.getEspaciomb());
        editPrecio.setText(software.getPrecio());

        new AlertDialog.Builder(holder.itemView.getContext())
                .setTitle("Editar Software")
                .setView(dialogView)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String nuevoNombre = editNombre.getText().toString().trim();
                    String nuevaVersion = editVersion.getText().toString().trim();
                    String nuevoEspacio = editEspacio.getText().toString().trim();
                    String nuevoPrecio = editPrecio.getText().toString().trim();

                    if (nuevoNombre.isEmpty() || nuevaVersion.isEmpty() || nuevoEspacio.isEmpty() || nuevoPrecio.isEmpty()) {
                        Toast.makeText(holder.itemView.getContext(), "Por favor, rellena todos los campos", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    new AlertDialog.Builder(holder.itemView.getContext())
                            .setTitle("Confirmar actualización")
                            .setMessage("¿Deseas guardar los cambios realizados?")
                            .setPositiveButton("Sí", (confDialog, confWhich) -> {
                                software.setNombre(nuevoNombre);
                                software.setVersionsoft(nuevaVersion);
                                software.setEspaciomb(nuevoEspacio);
                                software.setPrecio(nuevoPrecio);

                                actualizarSoftwarePorId(software, position, holder);
                            })
                            .setNegativeButton("No", null)
                            .show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void actualizarSoftwarePorId(Software software, int position, SoftwareViewHolder holder) {
        new Thread(() -> {
            try {
                URL url = new URL("https://cruds-rest-software-production.up.railway.app/api/softwares/" + software.getId());
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("PUT");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setDoOutput(true);
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                String jsonInputString = "{"
                        + "\"nombre\":\"" + software.getNombre() + "\","
                        + "\"versionsoft\":\"" + software.getVersionsoft() + "\","
                        + "\"espaciomb\":\"" + software.getEspaciomb() + "\","
                        + "\"precio\":\"" + software.getPrecio() + "\""
                        + "}";

                try (java.io.OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                int responseCode = conn.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        String descripcion = "Versión: " + software.getVersionsoft() +
                                " | Espacio: " + software.getEspaciomb() + " MB" +
                                " | Precio: $" + software.getPrecio();
                        software.setDescripcion(descripcion);
                        softwareList.set(position, software);
                        notifyItemChanged(position);
                        Toast.makeText(holder.itemView.getContext(), "Software actualizado", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    mostrarToast(holder, "Error al actualizar: Código " + responseCode);
                }

                conn.disconnect();

            } catch (Exception e) {
                e.printStackTrace();
                mostrarToast(holder, "Error de conexión");
            }
        }).start();
    }


    @Override
    public int getItemCount() {
        return softwareList.size();
    }

    public static class SoftwareViewHolder extends RecyclerView.ViewHolder {
        TextView tvId, tvNombre, tvDescripcion;
        Button btnEliminar, btnEditar;

        public SoftwareViewHolder(@NonNull View itemView) {
            super(itemView);
            tvId = itemView.findViewById(R.id.tvId);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcion);
            btnEliminar = itemView.findViewById(R.id.btnEliminar);
            btnEditar = itemView.findViewById(R.id.btnEditar);
        }
    }
}
