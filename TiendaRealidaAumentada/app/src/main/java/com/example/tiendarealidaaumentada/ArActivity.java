package com.example.tiendarealidaaumentada;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.ar.core.Anchor;
import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Camera;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import android.graphics.Point;

import com.google.ar.core.PointCloud;
import com.google.ar.core.Session;
import com.google.ar.core.Trackable;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.assets.RenderableSource;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Renderable;
import android.net.Uri;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ArActivity extends AppCompatActivity {

    private static final String TAG = "ArActivity";
    private ArSceneView arSceneView;
    private ModelRenderable toyCarRenderable;
    private boolean installRequested = false;
    private boolean modelLoaded = false;
    private String Modelo;
    private float dynamicScale = 1.0f; // Factor de escala calculado dinámicamente

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar);

        arSceneView = findViewById(R.id.arSceneView);
        arSceneView.getPlaneRenderer().setVisible(true);
        Intent intent= getIntent();
        Modelo = intent.getStringExtra("Modelo1");

        // Calcular factor de escala basado en el dispositivo
        calculateDynamicScale();

        // Cargar el modelo 3D
        loadModel();

        // Configurar el listener para detectar planos
        arSceneView.getScene().addOnUpdateListener(this::onUpdateFrame);
    }

    /**
     * Calcula un factor de escala dinámico basado en las características del dispositivo
     */
    private void calculateDynamicScale() {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();

        // Obtener dimensiones de pantalla en píxeles
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;
        float density = displayMetrics.density;

        // Calcular diagonal de pantalla en pulgadas
        float widthInches = screenWidth / (displayMetrics.xdpi);
        float heightInches = screenHeight / (displayMetrics.ydpi);
        double diagonalInches = Math.sqrt(Math.pow(widthInches, 2) + Math.pow(heightInches, 2));

        // Factor base según el tamaño de pantalla
        float baseScale;
        if (diagonalInches < 5.0) {
            // Pantallas pequeñas (< 5")
            baseScale = 0.003f;
        } else if (diagonalInches < 7.0) {
            // Pantallas medianas (5" - 7")
            baseScale = 0.004f;
        } else {
            // Pantallas grandes (> 7")
            baseScale = 0.006f;
        }

        // Ajustar según la densidad de pantalla
        float densityFactor = Math.min(density / 2.0f, 1.5f);

        // Calcular escala final
        dynamicScale = baseScale * densityFactor;

        Log.d(TAG, String.format("Pantalla: %.1f\" | Densidad: %.1f | Escala: %.4f",
                diagonalInches, density, dynamicScale));
    }

    private void loadModel() {
        //si existe el modelo del producto carga el modelo del producto y si no cargara a foxy el pirata
        if (Modelo == null) {
            ModelRenderable.builder()
                    .setSource(this, RenderableSource.builder()
                            .setSource(this, Uri.parse("file:///android_asset/models/foxy.glb"), RenderableSource.SourceType.GLB)
                            .setScale(dynamicScale * 10f) // Escala ajustada dinámicamente (foxy necesita más escala)
                            .setRecenterMode(RenderableSource.RecenterMode.ROOT)
                            .build())
                    .setRegistryId("Duck")
                    .build()
                    .thenAccept(renderable -> {
                        toyCarRenderable = renderable;
                        modelLoaded = true;
                        Log.d(TAG, "Modelo Foxy cargado exitosamente con escala: " + (dynamicScale * 10f));
                        Toast.makeText(this, "Modelo cargado. Busca una superficie plana.", Toast.LENGTH_SHORT).show();
                    })
                    .exceptionally(throwable -> {
                        Log.e(TAG, "Error al cargar el modelo", throwable);
                        Toast.makeText(this, "Error al cargar el modelo 3D", Toast.LENGTH_SHORT).show();
                        return null;
                    });
        }
        else {
            String ruta = "file:///android_asset/models/"+ Modelo + ".glb";
            // Cargar el modelo ToyCar.glb desde assets
            ModelRenderable.builder()
                    .setSource(this, RenderableSource.builder()
                            .setSource(this, Uri.parse(ruta), RenderableSource.SourceType.GLB)
                            .setScale(dynamicScale) // Escala ajustada dinámicamente
                            .setRecenterMode(RenderableSource.RecenterMode.ROOT)
                            .build())
                    .setRegistryId("Duck")
                    .build()
                    .thenAccept(renderable -> {
                        toyCarRenderable = renderable;
                        modelLoaded = true;
                        Log.d(TAG, "Modelo " + Modelo + " cargado exitosamente con escala: " + dynamicScale);
                        Toast.makeText(this, "Modelo cargado. Busca una superficie plana.", Toast.LENGTH_SHORT).show();
                    })
                    .exceptionally(throwable -> {
                        Log.e(TAG, "Error al cargar el modelo", throwable);
                        Toast.makeText(this, "Error al cargar el modelo 3D", Toast.LENGTH_SHORT).show();
                        return null;
                    });
        }
    }

    private void onUpdateFrame(FrameTime frameTime) {
        if (!modelLoaded) return;

        Frame frame = arSceneView.getArFrame();
        if (frame == null) return;

        if (frame.getCamera().getTrackingState() != TrackingState.TRACKING) return;

        // Realizar hit test al centro de la pantalla
        Point center = getScreenCenter();
        List<HitResult> hits = frame.hitTest(center.x, center.y);

        for (HitResult hit : hits) {
            Trackable trackable = hit.getTrackable();
            if ((trackable instanceof Plane && ((Plane) trackable).isPoseInPolygon(hit.getHitPose()))) {
                placeObject(hit.createAnchor());
                modelLoaded = false; // para evitar colocarlo múltiples veces
                break;
            }
        }
    }

    private Point getScreenCenter() {
        View vw = findViewById(android.R.id.content);
        return new Point(vw.getWidth() / 2, vw.getHeight() / 2);
    }

    private void placeObject(Anchor anchor) {
        AnchorNode anchorNode = new AnchorNode(anchor);
        anchorNode.setParent(arSceneView.getScene());

        Node modelNode = new Node();
        modelNode.setParent(anchorNode);
        modelNode.setRenderable(toyCarRenderable);
        modelNode.setLocalPosition(new Vector3(0.0f, 0.1f, 0.0f));

        // Escala adicional basada en el tamaño de pantalla (más conservadora)
        float finalScale = Math.min(0.8f * (float)(getScreenDiagonalInches() / 6.0), 1.2f);
        modelNode.setLocalScale(new Vector3(finalScale, finalScale, finalScale));

        Log.d(TAG, "Objeto colocado con escala final: " + finalScale);
        Toast.makeText(this, "¡Objeto colocado!", Toast.LENGTH_SHORT).show();
    }

    /**
     * Obtiene el tamaño diagonal de la pantalla en pulgadas
     */
    private double getScreenDiagonalInches() {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float widthInches = displayMetrics.widthPixels / displayMetrics.xdpi;
        float heightInches = displayMetrics.heightPixels / displayMetrics.ydpi;
        return Math.sqrt(Math.pow(widthInches, 2) + Math.pow(heightInches, 2));
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (arSceneView == null) {
            return;
        }

        if (arSceneView.getSession() == null) {
            // Si la sesión no existe, intentar crear una nueva
            try {
                switch (ArCoreApk.getInstance().requestInstall(this, !installRequested)) {
                    case INSTALL_REQUESTED:
                        installRequested = true;
                        return;
                    case INSTALLED:
                        break;
                }

                // Verificar permisos de cámara
                if (!CameraPermissionHelper.hasCameraPermission(this)) {
                    CameraPermissionHelper.requestCameraPermission(this);
                    return;
                }

                // Crear la sesión AR
                Session session = new Session(this);

                // 🛠️ Establecer configuración obligatoria para Sceneform
                Config config = new Config(session);
                config.setUpdateMode(Config.UpdateMode.LATEST_CAMERA_IMAGE); // ← CLAVE
                session.configure(config);

                // Pasar la sesión configurada al ArSceneView
                arSceneView.setupSession(session);

            } catch (UnavailableArcoreNotInstalledException
                     | UnavailableUserDeclinedInstallationException e) {
                Toast.makeText(this, "Por favor instala ARCore", Toast.LENGTH_LONG).show();
                finish();
                return;
            } catch (UnavailableApkTooOldException e) {
                Toast.makeText(this, "Por favor actualiza ARCore", Toast.LENGTH_LONG).show();
                finish();
                return;
            } catch (UnavailableSdkTooOldException e) {
                Toast.makeText(this, "Por favor actualiza la aplicación", Toast.LENGTH_LONG).show();
                finish();
                return;
            } catch (UnavailableDeviceNotCompatibleException e) {
                Toast.makeText(this, "Este dispositivo no soporta AR", Toast.LENGTH_LONG).show();
                finish();
                return;
            } catch (Exception e) {
                Toast.makeText(this, "Error al inicializar AR: " + e.getMessage(), Toast.LENGTH_LONG).show();
                finish();
                return;
            }
        }

        try {
            arSceneView.resume();
        } catch (CameraNotAvailableException ex) {
            Toast.makeText(this, "Cámara no disponible", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (arSceneView != null) {
            arSceneView.pause();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (arSceneView != null) {
            arSceneView.destroy();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (!CameraPermissionHelper.hasCameraPermission(this)) {
            Toast.makeText(this, "Se necesitan permisos de cámara para usar AR", Toast.LENGTH_LONG).show();
            if (!CameraPermissionHelper.shouldShowRequestPermissionRationale(this)) {
                // El usuario denegó el permiso y marcó "No volver a preguntar"
                CameraPermissionHelper.launchPermissionSettings(this);
            }
            finish();
        }
    }
}