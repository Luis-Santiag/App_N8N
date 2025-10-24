package com.example.lista_medica2dointento;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<MensajeChat> mensajes;

    public ChatAdapter(List<MensajeChat> mensajes) {
        this.mensajes = mensajes;
    }

    @Override
    public int getItemViewType(int position) {
        return mensajes.get(position).getTipo();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MensajeChat.TIPO_USUARIO) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mensaje_usuario, parent, false);
            return new UsuarioViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mensaje_ia, parent, false);
            return new IAViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MensajeChat mensaje = mensajes.get(position);
        if (holder instanceof UsuarioViewHolder) {
            ((UsuarioViewHolder) holder).tvMensaje.setText(mensaje.getTexto());
        } else if (holder instanceof IAViewHolder) {
            ((IAViewHolder) holder).tvMensaje.setText(mensaje.getTexto());
        }
    }

    @Override
    public int getItemCount() {
        return mensajes.size();
    }

    static class UsuarioViewHolder extends RecyclerView.ViewHolder {
        TextView tvMensaje;
        UsuarioViewHolder(View itemView) {
            super(itemView);
            tvMensaje = itemView.findViewById(R.id.tv_mensaje_usuario);
        }
    }

    static class IAViewHolder extends RecyclerView.ViewHolder {
        TextView tvMensaje;
        IAViewHolder(View itemView) {
            super(itemView);
            tvMensaje = itemView.findViewById(R.id.tv_mensaje_ia);
        }
    }
}

