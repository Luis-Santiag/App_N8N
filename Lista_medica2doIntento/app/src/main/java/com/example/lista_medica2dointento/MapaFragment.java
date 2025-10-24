package com.example.lista_medica2dointento;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.CircularBounds;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.api.net.PlacesStatusCodes;
import com.google.android.libraries.places.api.net.SearchNearbyRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MapaFragment extends Fragment implements OnMapReadyCallback {

    private static final String TAG = "MapaFragment";
    private static final int DEFAULT_ZOOM = 15;
    private static final LatLng DEFAULT_LOCATION = new LatLng(14.6349, -90.5069); // Ciudad de Guatemala
    private static final String TYPE_HOSPITAL = "Hospital";
    private static final String TYPE_PHARMACY = "Farmacia";
    private static final int SEARCH_RADIUS_METERS = 5000; // 5 km radio de búsqueda

    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;
    private PlacesClient placesClient;
    private Location currentLocation;
    private List<Marker> currentPlaceMarkers = new ArrayList<>();

    // Vistas del Layout
    private View rootView;
    private Button btnHospitales;
    private Button btnFarmacias;
    private LinearLayout layoutResultados;
    private TextView tvTituloMapa;
    private TextView tvResultadosCercanos;

    private static class PlaceInfo {
        String name;
        LatLng location;
        String type;
        double distanceKm;
        String address;
        String phoneNumber;

        PlaceInfo(String name, LatLng location, String type, double distanceKm, String address, String phoneNumber) {
            this.name = name;
            this.location = location;
            this.type = type;
            this.distanceKm = distanceKm;
            this.address = address;
            this.phoneNumber = phoneNumber;
        }
    }

    private final ActivityResultLauncher<String[]> locationPermissionRequest =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permissions -> {
                Boolean fineLocationGranted = permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                Boolean coarseLocationGranted = permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);

                if (Boolean.TRUE.equals(fineLocationGranted)) {
                    Log.d(TAG, "Permiso ACCESS_FINE_LOCATION concedido");
                    getDeviceLocationAndSetupMap();
                } else if (Boolean.TRUE.equals(coarseLocationGranted)) {
                    Log.d(TAG, "Permiso ACCESS_COARSE_LOCATION concedido");
                    getDeviceLocationAndSetupMap();
                } else {
                    Log.w(TAG, "Permiso de ubicación denegado");
                    Toast.makeText(getContext(), "Permiso de ubicación denegado. Mostrando ubicación por defecto.", Toast.LENGTH_LONG).show();
                    setupMapWithDefaultLocation();
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_mapa, container, false);

        // Inicialización de todas las vistas
        btnHospitales = rootView.findViewById(R.id.btn_hospitales);
        btnFarmacias = rootView.findViewById(R.id.btn_farmacias);
        layoutResultados = rootView.findViewById(R.id.layout_resultados);
        tvTituloMapa = rootView.findViewById(R.id.tv_titulo_mapa);
        tvResultadosCercanos = rootView.findViewById(R.id.tv_resultados_cercanos);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Inicializar Places API
        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), "AIzaSyAVnS3jfN7_Xbjtgei9nsb0mrU0Ar5-ROY"); // Reemplaza con tu clave de API real
        }
        placesClient = Places.createClient(requireContext());

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_container);
        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
            getChildFragmentManager().beginTransaction().replace(R.id.map_container, mapFragment).commit();
        }
        mapFragment.getMapAsync(this);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnHospitales.setOnClickListener(v -> {
            if (googleMap == null || currentLocation == null) {
                Toast.makeText(getContext(), "Ubicación no disponible", Toast.LENGTH_SHORT).show();
                return;
            }
            searchNearbyPlaces(Place.Type.HOSPITAL, TYPE_HOSPITAL, "Hospitales Cercanos");
        });

        btnFarmacias.setOnClickListener(v -> {
            if (googleMap == null || currentLocation == null) {
                Toast.makeText(getContext(), "Ubicación no disponible", Toast.LENGTH_SHORT).show();
                return;
            }
            searchNearbyPlaces(Place.Type.PHARMACY, TYPE_PHARMACY, "Farmacias Cercanas");
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.googleMap = map;
        Log.d(TAG, "Mapa listo.");

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "onMapReady: Permisos ya concedidos. Obteniendo ubicación.");
            getDeviceLocationAndSetupMap();
        } else {
            Log.d(TAG, "onMapReady: Permisos no concedidos. Solicitando...");
            locationPermissionRequest.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    private void getDeviceLocationAndSetupMap() {
        Log.d(TAG, "getDeviceLocationAndSetupMap: Obteniendo ubicación del dispositivo.");
        try {
            if (googleMap == null) {
                Log.e(TAG, "getDeviceLocationAndSetupMap: googleMap no está listo todavía.");
                return;
            }
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                googleMap.setMyLocationEnabled(true);
                googleMap.getUiSettings().setMyLocationButtonEnabled(true);

                Task<Location> locationResult = fusedLocationClient.getLastLocation();
                locationResult.addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {
                        currentLocation = task.getResult();
                        if (currentLocation != null) {
                            Log.d(TAG, "Última ubicación conocida: " + currentLocation.getLatitude() + ", " + currentLocation.getLongitude());
                            LatLng currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, DEFAULT_ZOOM));
                        } else {
                            Log.d(TAG, "Última ubicación conocida es null. Usando ubicación por defecto.");
                            setupMapWithDefaultLocation();
                        }
                    } else {
                        Log.e(TAG, "Excepción al obtener la ubicación: ", task.getException());
                        setupMapWithDefaultLocation();
                    }
                });
            } else {
                Log.w(TAG, "getDeviceLocationAndSetupMap: Permisos no concedidos aquí, usando default.");
                setupMapWithDefaultLocation();
            }
        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException en getDeviceLocationAndSetupMap: " + e.getMessage());
            setupMapWithDefaultLocation();
        }
    }

    private void setupMapWithDefaultLocation() {
        if (googleMap == null) {
            Log.e(TAG, "setupMapWithDefaultLocation: googleMap no está listo todavía.");
            return;
        }
        Log.d(TAG, "Configurando mapa con ubicación por defecto: " + DEFAULT_LOCATION.toString());
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, DEFAULT_ZOOM));
        try {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                googleMap.setMyLocationEnabled(false);
                googleMap.getUiSettings().setMyLocationButtonEnabled(false);
            }
        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException al intentar deshabilitar MyLocationEnabled en default: " + e.getMessage());
        }
    }

    private void searchNearbyPlaces(Place.Type placeType, String markerType, String typeTitle) {
        LatLng currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        CircularBounds bounds = CircularBounds.newInstance(currentLatLng, SEARCH_RADIUS_METERS);

        List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG);

        SearchNearbyRequest request = SearchNearbyRequest.builder(bounds, placeFields)
                .setIncludedTypes(Collections.singletonList(placeType.name().toLowerCase()))
                .build();

        placesClient.searchNearby(request).addOnSuccessListener(response -> {
            List<Place> placesFromSearch = response.getPlaces();
            if (placesFromSearch.isEmpty()) {
                tvResultadosCercanos.setText("No hay " + typeTitle.toLowerCase() + " para mostrar.");
                Toast.makeText(getContext(), "No se encontraron " + typeTitle.toLowerCase() + ".", Toast.LENGTH_SHORT).show();
                return;
            }

            fetchPlaceDetails(placesFromSearch, markerType, typeTitle);
        }).addOnFailureListener(exception -> {
            Log.e(TAG, "Error en búsqueda cercana: " + exception.getMessage());
            Toast.makeText(getContext(), "Error al buscar " + typeTitle.toLowerCase(), Toast.LENGTH_SHORT).show();
        });
    }

    private void fetchPlaceDetails(List<Place> placesFromSearch, String markerType, String typeTitle) {
        List<PlaceInfo> placeInfos = new ArrayList<>();

        for (Place place : placesFromSearch) {
            List<Place.Field> detailFields = Arrays.asList(Place.Field.ADDRESS, Place.Field.PHONE_NUMBER);
            FetchPlaceRequest detailRequest = FetchPlaceRequest.newInstance(place.getId(), detailFields);

            placesClient.fetchPlace(detailRequest).addOnSuccessListener(detailResponse -> {
                Place detailedPlace = detailResponse.getPlace();
                double distanceKm = calculateDistance(currentLocation, place.getLatLng()) / 1000.0; // En km
                String address = detailedPlace.getAddress() != null ? detailedPlace.getAddress() : "Dirección no disponible";
                String phone = detailedPlace.getPhoneNumber() != null ? detailedPlace.getPhoneNumber() : "Teléfono no disponible";

                placeInfos.add(new PlaceInfo(place.getName(), place.getLatLng(), markerType, distanceKm, address, phone));

                if (placeInfos.size() == placesFromSearch.size()) {
                    // Ordenar por distancia
                    Collections.sort(placeInfos, Comparator.comparingDouble(p -> p.distanceKm));
                    addMarkersAndAdjustCamera(placeInfos, markerType);
                    displayPlacesInList(placeInfos, typeTitle);
                }
            }).addOnFailureListener(exception -> {
                Log.e(TAG, "Error al obtener detalles: " + exception.getMessage());
            });
        }
    }

    private double calculateDistance(Location start, LatLng end) {
        Location endLocation = new Location("");
        endLocation.setLatitude(end.latitude);
        endLocation.setLongitude(end.longitude);
        return start.distanceTo(endLocation);
    }

    private void addMarkersAndAdjustCamera(List<PlaceInfo> places, String type) {
        if (googleMap == null || places.isEmpty()) {
            Log.w(TAG, "No se pueden añadir marcadores. Mapa no listo o lista de lugares vacía.");
            return;
        }
        for (Marker marker : currentPlaceMarkers) {
            marker.remove();
        }
        currentPlaceMarkers.clear();

        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        for (PlaceInfo place : places) {
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(place.location)
                    .title(place.name)
                    .snippet(place.type + " - " + String.format("%.2f km", place.distanceKm));
            Marker marker = googleMap.addMarker(markerOptions);
            if (marker != null) {
                currentPlaceMarkers.add(marker);
                boundsBuilder.include(place.location);
            }
        }

        if (!currentPlaceMarkers.isEmpty()) {
            LatLngBounds bounds = boundsBuilder.build();
            int padding = 100;
            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
        }
    }

    private void displayPlacesInList(List<PlaceInfo> places, String typeTitle) {
        if (layoutResultados == null || tvResultadosCercanos == null) {
            Log.e(TAG, "Vistas para la lista de resultados no inicializadas. Verifica los IDs en fragment_mapa.xml.");
            if (getContext() != null) {
                Toast.makeText(getContext(), "Error interno al mostrar lista.", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        layoutResultados.removeAllViews();

        if (places.isEmpty()) {
            tvResultadosCercanos.setText("No hay " + typeTitle.toLowerCase() + " para mostrar.");
            Toast.makeText(getContext(), "No hay " + typeTitle.toLowerCase() + " disponibles.", Toast.LENGTH_SHORT).show();
            return;
        }
        tvResultadosCercanos.setText(typeTitle);

        LayoutInflater inflater = LayoutInflater.from(getContext());
        for (PlaceInfo place : places) {
            // Crear una "tarjeta" simple con TextView para cada lugar
            TextView placeCard = new TextView(getContext());
            placeCard.setText(String.format("%s\nDistancia: %.2f km\nDirección: %s\nTeléfono: %s",
                    place.name, place.distanceKm, place.address, place.phoneNumber));
            placeCard.setTextSize(16);
            placeCard.setTextColor(0xFF000000); // Texto negro
            placeCard.setBackgroundColor(0xFFE0E0E0); // Fondo gris claro para simular tarjeta
            placeCard.setPadding(16, 16, 16, 16);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 0, 16); // Margen inferior entre tarjetas
            placeCard.setLayoutParams(params);

            layoutResultados.addView(placeCard);
        }
    }
}