package com.example.lista_medica2dointento;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

// Eliminado "implements MainActivity.TextSizeUpdatable"
public class ChatFragment extends Fragment {
    private RecyclerView rvChat;
    private EditText etMensaje;
    private Button btnEnviar;
    private ProgressBar progressBar;
    private ChatAdapter chatAdapter;
    private List<MensajeChat> mensajes;
    private View rootView;
    private OkHttpClient client;
    private Call currentCall;
    private boolean isFragmentActive = true;
    private int lastVisiblePosition = 0; // Para guardar la posición del scroll

    // Endpoint Groq (compatible OpenAI) solicitado por el usuario
    private static final String OPENAI_API_URL = "https://api.groq.com/openai/v1/chat/completions";
    // Recuerda manejar tu API Key de forma segura, por ejemplo, desde gradle.properties o un archivo no versionado.
    private static final String OPENAI_API_KEY = "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";
    private static final String KEY_MESSAGES = "messages";
    private static final String KEY_SCROLL_POSITION = "scroll_position";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_chat, container, false);
        rvChat = rootView.findViewById(R.id.rv_chat);
        etMensaje = rootView.findViewById(R.id.et_mensaje);
        btnEnviar = rootView.findViewById(R.id.btn_enviar);

        progressBar = new ProgressBar(getContext(), null, android.R.attr.progressBarStyle);
        progressBar.setVisibility(View.GONE);
        ((ViewGroup) rootView).addView(progressBar, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        mensajes = new ArrayList<>();
        if (savedInstanceState != null) {
            ArrayList<MensajeChat> savedMessages = savedInstanceState.getParcelableArrayList(KEY_MESSAGES);
            if (savedMessages != null) {
                mensajes.addAll(savedMessages);
            }
            lastVisiblePosition = savedInstanceState.getInt(KEY_SCROLL_POSITION, 0);
        }

        chatAdapter = new ChatAdapter(mensajes);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        rvChat.setLayoutManager(layoutManager);
        rvChat.setAdapter(chatAdapter);

        client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        btnEnviar.setOnClickListener(v -> enviarMensaje());
        etMensaje.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                enviarMensaje();
                return true;
            }
            return false;
        });

        if (lastVisiblePosition > 0) {
            rvChat.scrollToPosition(lastVisiblePosition);
        }

        // Llamada a aplicarTamanoTexto() eliminada
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        isFragmentActive = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        isFragmentActive = false;
        LinearLayoutManager layoutManager = (LinearLayoutManager) rvChat.getLayoutManager();
        if (layoutManager != null) {
            lastVisiblePosition = layoutManager.findLastVisibleItemPosition();
        }
        btnEnviar.setEnabled(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        isFragmentActive = true;
        btnEnviar.setEnabled(true);
        if (lastVisiblePosition > 0) {
            rvChat.scrollToPosition(lastVisiblePosition);
        }
        // Llamada a aplicarTamanoTexto() eliminada
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(KEY_MESSAGES, new ArrayList<>(mensajes));
        outState.putInt(KEY_SCROLL_POSITION, lastVisiblePosition);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (currentCall != null) {
            currentCall.cancel();
        }
        rootView = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        new Thread(() -> {
            if (client != null) {
                client.dispatcher().cancelAll();
                client.dispatcher().executorService().shutdown();
                client.connectionPool().evictAll();
            }
        }).start();
    }

    // Método aplicarTamanoTexto() eliminado

    private void enviarMensaje() {
        String texto = etMensaje.getText().toString().trim();
        if (!texto.isEmpty() && isFragmentActive && isAdded()) {
            mensajes.add(new MensajeChat(texto, MensajeChat.TIPO_USUARIO));
            chatAdapter.notifyItemInserted(mensajes.size() - 1);
            rvChat.scrollToPosition(mensajes.size() - 1);
            etMensaje.setText("");
            progressBar.setVisibility(View.VISIBLE);
            btnEnviar.setEnabled(false);

            // Considera mover la API key a un lugar más seguro como gradle.properties
            if (OPENAI_API_KEY.equals("sk-tu-clave-real-aqui") || OPENAI_API_KEY.isEmpty()) { 
                simularRespuesta(texto);
                return;
            }
            consultarOpenAI(texto);
        }
    }

    private void simularRespuesta(String mensajeUsuario) {
        rvChat.postDelayed(() -> {
            if (isFragmentActive && isAdded() && getActivity() != null) {
                String respuesta = "Respuesta simulada: Gracias por tu consulta sobre '" + mensajeUsuario + "'. Recuerda, esto es una recomendación, no un diagnóstico. En caso de dudas, acude a tu hospital más cercano.";
                mensajes.add(new MensajeChat(respuesta, MensajeChat.TIPO_IA));
                chatAdapter.notifyItemInserted(mensajes.size() - 1);
                rvChat.scrollToPosition(mensajes.size() - 1);
                progressBar.setVisibility(View.GONE);
                btnEnviar.setEnabled(true);
            }
        }, 1000);
    }

    private void consultarOpenAI(String mensajeUsuario) {
        String systemPrompt = "Eres un asistente médico amigable que responde únicamente a saludos iniciales, pregunta basicas de contexto o preguntas relacionadas con la salud, medicina, síntomas, tratamientos o prevención. Si la pregunta no está relacionada con estos temas, responde: 'Lo siento, solo puedo ayudarte con preguntas relacionadas con la salud o medicina. ¿Tienes alguna consulta médica?' Todas tus respuestas deben incluir esta advertencia al final: 'Esta es una recomendación, no un diagnóstico. En caso de dudas o síntomas graves, acude a tu hospital más cercano.' Usa un tono profesional pero cálido y accesible.";

        JSONObject jsonBody = new JSONObject();
        try {
            // Usar modelo de Groq solicitado
            jsonBody.put("model", "openai/gpt-oss-120b");
            JSONArray messagesArray = new JSONArray(); // Renombrado para evitar confusión
            messagesArray.put(new JSONObject().put("role", "system").put("content", systemPrompt));
            messagesArray.put(new JSONObject().put("role", "user").put("content", mensajeUsuario));
            jsonBody.put("messages", messagesArray);
            jsonBody.put("max_tokens", 500);
            jsonBody.put("temperature", 0.7);
        } catch (Exception e) {
            mostrarError("Error al preparar la solicitud a la IA");
            return;
        }

        RequestBody body = RequestBody.create(jsonBody.toString(), MediaType.parse("application/json"));

        // Log del request para depuración: URL y body (útil para investigar 404)
        android.util.Log.d("ChatFragment", "Enviando request a: " + OPENAI_API_URL + " body= " + jsonBody.toString());

        Request request = new Request.Builder()
                .url(OPENAI_API_URL)
                .header("Authorization", "Bearer " + OPENAI_API_KEY)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .post(body)
                .build();

        currentCall = client.newCall(request);
        currentCall.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if (isFragmentActive && isAdded() && getActivity() != null) {
                    requireActivity().runOnUiThread(() -> mostrarError("Error de conexión con la IA: " + e.getMessage()));
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (isFragmentActive && isAdded() && getActivity() != null) {
                    try {
                        String responseBody = response.body() != null ? response.body().string() : null;
                        int code = response.code();
                        // Log completo para que puedas copiarlo si hace falta
                        android.util.Log.d("ChatFragment", "IA response code=" + code + " body=" + responseBody);

                        if (response.isSuccessful() && responseBody != null) {
                            try {
                                JSONObject jsonResponse = new JSONObject(responseBody);
                                String respuestaIA = parseAIResponse(jsonResponse);
                                if (respuestaIA == null) {
                                    // No se pudo extraer con heurística
                                    final String rb = responseBody;
                                    requireActivity().runOnUiThread(() -> mostrarError("Respuesta de IA en formato inesperado. Revisa logcat. Código: " + code));
                                    android.util.Log.e("ChatFragment", "Formato de respuesta no reconocido: " + rb);
                                } else {
                                    final String finalRespuesta = respuestaIA;
                                    requireActivity().runOnUiThread(() -> {
                                        mensajes.add(new MensajeChat(finalRespuesta, MensajeChat.TIPO_IA));
                                        chatAdapter.notifyItemInserted(mensajes.size() - 1);
                                        rvChat.scrollToPosition(mensajes.size() - 1);
                                        progressBar.setVisibility(View.GONE);
                                        btnEnviar.setEnabled(true);
                                    });
                                }
                            } catch (Exception e) {
                                requireActivity().runOnUiThread(() -> mostrarError("Error al procesar la respuesta de la IA: " + e.getMessage()));
                                android.util.Log.e("ChatFragment", "Error parseando JSON de la IA", e);
                            }
                        } else {
                            // Mostrar cuerpo y código para depuración, especialmente 404
                            final String shortBody = responseBody != null ? (responseBody.length() > 800 ? responseBody.substring(0, 800) + "..." : responseBody) : "(sin cuerpo)";
                            requireActivity().runOnUiThread(() -> mostrarError("Error en la respuesta de la IA: " + response.message() + " (Código: " + code + ") - " + shortBody));
                            android.util.Log.e("ChatFragment", "Error response from IA code=" + code + " body=" + responseBody);
                        }
                    } finally {
                        response.close();
                    }
                }
            }
        });
    }

    private void mostrarError(String mensaje) {
        if (isFragmentActive && isAdded() && getActivity() != null) {
            progressBar.setVisibility(View.GONE);
            btnEnviar.setEnabled(true);
            Toast.makeText(getContext(), mensaje, Toast.LENGTH_LONG).show();
        }
    }

    // Heurística para extraer texto de respuesta de varios proveedores (OpenAI/Groq variantes)
    private String parseAIResponse(JSONObject json) {
        try {
            // 1) OpenAI Chat-completions: choices[0].message.content
            if (json.has("choices")) {
                JSONArray choices = json.getJSONArray("choices");
                if (choices.length() > 0) {
                    JSONObject c0 = choices.getJSONObject(0);
                    // message.content
                    if (c0.has("message") && c0.getJSONObject("message").has("content")) {
                        return c0.getJSONObject("message").getString("content");
                    }
                    // text (algunos proveedores devuelven text)
                    if (c0.has("text")) {
                        return c0.getString("text");
                    }
                    // 'delta' streaming case: try to gather 'content' inside message
                    if (c0.has("delta") && c0.getJSONObject("delta").has("content")) {
                        return c0.getJSONObject("delta").getString("content");
                    }
                }
            }

            // 2) Groq / otros: comprobar keys comunes
            if (json.has("output") && json.get("output") instanceof JSONObject) {
                JSONObject out = json.getJSONObject("output");
                if (out.has("text")) return out.getString("text");
                if (out.has("result")) return out.getString("result");
            }
            if (json.has("results") && json.get("results") instanceof JSONArray) {
                JSONArray res = json.getJSONArray("results");
                if (res.length() > 0) {
                    JSONObject r0 = res.getJSONObject(0);
                    if (r0.has("output_text")) return r0.getString("output_text");
                    if (r0.has("text")) return r0.getString("text");
                }
            }

            // 3) As fallback, devolver 'raw' si existe 'generated_text' u otros
            if (json.has("generated_text")) return json.getString("generated_text");

        } catch (Exception e) {
            android.util.Log.e("ChatFragment", "Error en parseAIResponse", e);
        }
        return null;
    }
}
