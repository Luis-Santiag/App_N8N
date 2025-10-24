package com.example.lista_medica2dointento;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class NotasAdapter extends RecyclerView.Adapter<NotasAdapter.NotaViewHolder> {
    private List<NotaItem> notas;
    private OnNotaEliminarListener eliminarListener;
    private OnNotaCompartirListener compartirListener;

    public interface OnNotaEliminarListener {
        void onEliminarNota(NotaItem nota, int position);
    }

    public interface OnNotaCompartirListener {
        void onCompartirNota(NotaItem nota);
    }

    public NotasAdapter(List<NotaItem> notas) {
        this.notas = notas;
    }

    public void setOnNotaEliminarListener(OnNotaEliminarListener listener) {
        this.eliminarListener = listener;
    }

    public void setOnNotaCompartirListener(OnNotaCompartirListener listener) {
        this.compartirListener = listener;
    }

    @NonNull
    @Override
    public NotaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_nota, parent, false);
        return new NotaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotaViewHolder holder, int position) {
        NotaItem nota = notas.get(position);
        holder.tvTipo.setText(nota.tipo);
        holder.tvAsunto.setText(nota.asunto);
        holder.tvFecha.setText(nota.fecha);
        String resumen = nota.contenido.length() > 50 ? nota.contenido.substring(0, 50) + "..." : nota.contenido;
        holder.tvResumen.setText(resumen);
        holder.tvContenidoCompleto.setVisibility(View.GONE);

        if (nota.tipo.equals("Nota Médica")) {
            // Mostrar solo la descripción o detalles
            holder.tvContenidoCompleto.setText("Descripción o detalles:\n" + nota.contenido);
        } else if (nota.tipo.equals("Receta")) {
            String[] lineas = nota.contenido.split("\\n");
            String medicamentos = lineas.length > 0 ? lineas[0] : "";
            String indicaciones = lineas.length > 1 ? lineas[1] : "";
            holder.tvContenidoCompleto.setText("Indicaciones:\n" + indicaciones.replace("Indicaciones: ", ""));
            holder.tvResumen.setText(medicamentos);
        } else {
            holder.tvContenidoCompleto.setText(nota.contenido);
        }

        holder.itemView.setBackgroundColor(0xFFF2F2F2);
        holder.tvAsunto.setTextSize(18);
        holder.tvFecha.setTextSize(16);
        holder.tvResumen.setTextSize(14);
        holder.tvContenidoCompleto.setTextSize(13);

        holder.itemView.setOnClickListener(v -> {
            if (holder.tvContenidoCompleto.getVisibility() == View.VISIBLE) {
                holder.tvContenidoCompleto.setVisibility(View.GONE);
            } else {
                holder.tvContenidoCompleto.setVisibility(View.VISIBLE);
            }
        });

        holder.btnEliminar.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION && eliminarListener != null) {
                eliminarListener.onEliminarNota(notas.get(adapterPosition), adapterPosition);
            }
        });

        holder.btnCompartir.setOnClickListener(v -> {
            if (compartirListener != null) {
                compartirListener.onCompartirNota(nota);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notas.size();
    }

    public void removeNota(int position) {
        if (position >= 0 && position < notas.size()) {
            notas.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, notas.size());
        }
    }

    static class NotaViewHolder extends RecyclerView.ViewHolder {
        TextView tvTipo, tvAsunto, tvFecha, tvResumen, tvContenidoCompleto;
        View btnEliminar, btnCompartir;

        NotaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTipo = itemView.findViewById(R.id.tv_tipo_nota);
            tvAsunto = itemView.findViewById(R.id.tv_asunto_nota);
            tvFecha = itemView.findViewById(R.id.tv_fecha_nota);
            tvResumen = itemView.findViewById(R.id.tv_resumen_nota);
            tvContenidoCompleto = itemView.findViewById(R.id.tv_contenido_completo);
            btnEliminar = itemView.findViewById(R.id.btn_eliminar_nota);
            btnCompartir = itemView.findViewById(R.id.btn_compartir_nota);
        }
    }
}