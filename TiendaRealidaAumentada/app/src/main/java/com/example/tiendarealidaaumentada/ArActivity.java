package com.example.tiendarealidaaumentada;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
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
import com.google.ar.sceneform.ux.TransformableNode;
import com.google.ar.sceneform.ux.TransformationSystem;
import com.google.ar.sceneform.ux.SelectionVisualizer;
import com.google.ar.sceneform.ux.FootprintSelectionVisualizer;

import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.math.Quaternion;
import android.net.Uri;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ArActivity extends AppCompatActivity {

    private static final String TAG = "ArActivity";
    private ArSceneView arSceneView;
    private ModelRenderable toyCarRenderable;
    private boolean installRequested = false;
    private boolean modelLoaded = false;
    private String Modelo;
    private ImageButton btnBack;

    private TransformationSystem transformationSystem;
    private SelectionVisualizer selectionVisualizer;

    // Variables para rotación con un dedo
    private TransformableNode currentModelNode;
    private float lastTouchX;
    private boolean isRotating = false;
    private static final float ROTATION_SENSITIVITY = 2.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar);
        Log.d(TAG, "ArActivity onCreate iniciado.");

        arSceneView = findViewById(R.id.arSceneView);
        arSceneView.getPlaneRenderer().setVisible(true);
        Intent intent= getIntent();
        Modelo = intent.getStringExtra("Modelo1");
        Log.d(TAG, "Modelo recibido: " + (Modelo != null ? Modelo : "null"));

        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> regresar());

        // Inicializar SelectionVisualizer y luego TransformationSystem
        selectionVisualizer = new FootprintSelectionVisualizer();
        transformationSystem = new TransformationSystem(getResources().getDisplayMetrics(), selectionVisualizer);
        Log.d(TAG, "TransformationSystem inicializado.");

        clearModelCache();
        loadModel();

        // Configurar el listener para detectar planos
        arSceneView.getScene().addOnUpdateListener(this::onUpdateFrame);
        Log.d(TAG, "addOnUpdateListener para onUpdateFrame configurado.");

        arSceneView.getScene().setOnTouchListener((hitTestResult, motionEvent) -> {
            return handleTouch(hitTestResult, motionEvent);
        });
        Log.d(TAG, "setOnTouchListener personalizado configurado.");
    }

    private boolean handleTouch(com.google.ar.sceneform.HitTestResult hitTestResult, MotionEvent motionEvent) {
        int action = motionEvent.getActionMasked();
        String actionName = "";
        switch (action) {
            case MotionEvent.ACTION_DOWN: actionName = "DOWN"; break;
            case MotionEvent.ACTION_UP: actionName = "UP"; break;
            case MotionEvent.ACTION_MOVE: actionName = "MOVE"; break;
            case MotionEvent.ACTION_POINTER_DOWN: actionName = "POINTER_DOWN"; break;
            case MotionEvent.ACTION_POINTER_UP: actionName = "POINTER_UP"; break;
            case MotionEvent.ACTION_CANCEL: actionName = "CANCEL"; break;
            default: actionName = String.valueOf(action); break;
        }
        Log.d(TAG, "Scene OnTouchListener activado. Acción: " + actionName + " (raw: " + action + "), Pointers: " + motionEvent.getPointerCount());

        // Si hay un modelo colocado y es un toque de un solo dedo
        if (currentModelNode != null && motionEvent.getPointerCount() == 1) {
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    lastTouchX = motionEvent.getX();
                    isRotating = true;
                    Log.d(TAG, "Iniciando rotación. X inicial: " + lastTouchX);
                    break;

                case MotionEvent.ACTION_MOVE:
                    if (isRotating) {
                        float currentX = motionEvent.getX();
                        float deltaX = currentX - lastTouchX;

                        if (Math.abs(deltaX) > 3) {
                            rotateModel(deltaX);
                            lastTouchX = currentX;
                            Log.d(TAG, "Rotando modelo. DeltaX: " + deltaX);
                        }
                    }
                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    isRotating = false;
                    Log.d(TAG, "Finalizando rotación.");
                    break;
            }
            if (isRotating && motionEvent.getPointerCount() == 1) {
                return true;
            }
        }

        transformationSystem.onTouch(hitTestResult, motionEvent);
        return true;
    }

    private void rotateModel(float deltaX) {
        if (currentModelNode == null) return;

        // Calcular el ángulo de rotación basado en el movimiento horizontal
        float rotationAngle = deltaX * ROTATION_SENSITIVITY;

        // Obtener la rotación actual del nodo
        Quaternion currentRotation = currentModelNode.getLocalRotation();

        // Crear una nueva rotación alrededor del eje Y (vertical)
        Quaternion deltaRotation = Quaternion.axisAngle(new Vector3(0, 1, 0), rotationAngle);

        // Aplicar la rotación
        Quaternion newRotation = Quaternion.multiply(currentRotation, deltaRotation);
        currentModelNode.setLocalRotation(newRotation);

        Log.d(TAG, "Modelo rotado. Ángulo: " + rotationAngle + " grados");
    }

    private void clearModelCache() {
        try {
            if (toyCarRenderable != null) {
                toyCarRenderable = null;
            }
            modelLoaded = false;
            currentModelNode = null;
            Log.d(TAG, "Cache de modelos limpiado.");
        } catch (Exception e) {
            Log.e(TAG, "Error al limpiar cache: " + e.getMessage());
        }
    }

    private void regresar() {
        Log.d(TAG, "Regresando a Home.");
        cleanupResources();
        Intent intent = new Intent(ArActivity.this, Home.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void cleanupResources() {
        try {
            if (arSceneView != null && arSceneView.getScene() != null) {
                arSceneView.getScene().removeOnUpdateListener(this::onUpdateFrame);
                arSceneView.getScene().setOnTouchListener(null);
                Log.d(TAG, "Listeners de escena eliminados.");
            }
            if (toyCarRenderable != null) {
                toyCarRenderable = null;
            }
            modelLoaded = false;
            currentModelNode = null;
            Modelo = null;
            Log.d(TAG, "Recursos limpiados completamente.");
        } catch (Exception e) {
            Log.e(TAG, "Error al limpiar recursos: " + e.getMessage());
        }
    }

    private void loadModel() {
        String registryId = "Model_" + (Modelo != null ? Modelo : "Foxy") + "_" + System.currentTimeMillis();

        Log.d(TAG, "Iniciando carga del modelo. RegistryId: " + registryId);

        if (Modelo == null) {
            ModelRenderable.builder()
                    .setSource(this, RenderableSource.builder()
                            .setSource(this, Uri.parse("file:///android_asset/models/foxy.glb"), RenderableSource.SourceType.GLB)
                            .setScale(0.7f)
                            .setRecenterMode(RenderableSource.RecenterMode.ROOT)
                            .build())
                    .setRegistryId(registryId)
                    .build()
                    .thenAccept(renderable -> {
                        toyCarRenderable = renderable;
                        modelLoaded = true;
                        Log.d(TAG, "Modelo Foxy cargado exitosamente. Renderable: " + (toyCarRenderable != null));
                        Toast.makeText(this, "Modelo cargado. Busca una superficie plana.", Toast.LENGTH_SHORT).show();
                    })
                    .exceptionally(throwable -> {
                        Log.e(TAG, "Error al cargar el modelo Foxy", throwable);
                        Toast.makeText(this, "Error al cargar el modelo 3D", Toast.LENGTH_SHORT).show();
                        return null;
                    });
        }
        else {
            String ruta = "file:///android_asset/models/"+ Modelo + ".glb";
            float EscalaN = ObtenEscala();
            Log.d(TAG, "Ruta del modelo del producto: " + ruta);
            ModelRenderable.builder()
                    .setSource(this, RenderableSource.builder()
                            .setSource(this, Uri.parse(ruta), RenderableSource.SourceType.GLB)
                            .setScale(EscalaN)
                            .setRecenterMode(RenderableSource.RecenterMode.ROOT)
                            .build())
                    .setRegistryId(registryId)
                    .build()
                    .thenAccept(renderable -> {
                        toyCarRenderable = renderable;
                        modelLoaded = true;
                        Log.d(TAG, "Modelo " + Modelo + " cargado exitosamente. Renderable: " + (toyCarRenderable != null));
                        Toast.makeText(this, "Modelo cargado. Busca una superficie plana.", Toast.LENGTH_SHORT).show();
                    })
                    .exceptionally(throwable -> {
                        Log.e(TAG, "Error al cargar el modelo " + Modelo, throwable);
                        Toast.makeText(this, "Error al cargar el modelo 3D", Toast.LENGTH_SHORT).show();
                        return null;
                    });
        }
    }

    private float ObtenEscala() {
        List<String> modelosParaEscaladoG = new ArrayList<>();
        List<String> modelosParaEscaladoMG = new ArrayList<>();
        List<String> modelosParaEscaladoMMG = new ArrayList<>();
        List<String> modelosParaEscaladoP = new ArrayList<>();
        List<String> modelosParaEscaladoMP = new ArrayList<>();
        // Electronica
        modelosParaEscaladoMMG.add("cyberpunk_laptop");
        modelosParaEscaladoMG.add("hello tv");

        // Armas
        modelosParaEscaladoP.add("pistol__desert_eagle");
        modelosParaEscaladoMP.add("taser__zeus");
        modelosParaEscaladoMP.add("tool__hammer");

        // Arquitectura
        modelosParaEscaladoG.add("bar_sign_board");
        modelosParaEscaladoG.add("neon_sign_board_food");
        modelosParaEscaladoMMG.add("ikea_alex_drawer");
        modelosParaEscaladoG.add("wooden_chest");

        // Vehiculo
        modelosParaEscaladoG.add("atlantic_explorer_submarineglb");//se ve muy pequeño
        modelosParaEscaladoG.add("toyota_corolla_mk7");//se ve muy pequeño
        modelosParaEscaladoG.add("ibishu_pigeon");

        // Otros
        modelosParaEscaladoG.add("large_corner_sectional_sofa");
        modelosParaEscaladoG.add("sofa");

        if (modelosParaEscaladoMMG.contains(Modelo)){
            return 0.00025f;
        }
        else if (modelosParaEscaladoMG.contains(Modelo)){
            return 0.0005f;
        }
        else if (modelosParaEscaladoG.contains(Modelo)){
            return 0.05f;
        }
        else if (modelosParaEscaladoP.contains(Modelo)){
            return 1.5f;
        }
        else if (modelosParaEscaladoMP.contains(Modelo)){
            return 2f;
        }
        else {
            return 0.5f;
        }


    }

    private void onUpdateFrame(FrameTime frameTime) {
        if (!modelLoaded) {
            return;
        }

        Frame frame = arSceneView.getArFrame();
        if (frame == null) return;

        if (frame.getCamera().getTrackingState() != TrackingState.TRACKING) {
            return;
        }

        Point center = getScreenCenter();
        List<HitResult> hits = frame.hitTest(center.x, center.y);

        for (HitResult hit : hits) {
            Trackable trackable = hit.getTrackable();
            if ((trackable instanceof Plane && ((Plane) trackable).isPoseInPolygon(hit.getHitPose()))) {
                Log.d(TAG, "Plano detectado y colisión exitosa. Intentando colocar objeto.");
                placeObject(hit.createAnchor());
                modelLoaded = false;
                break;
            }
        }
    }

    private Point getScreenCenter() {
        View vw = findViewById(android.R.id.content);
        return new Point(vw.getWidth() / 2, vw.getHeight() / 2);
    }

    private void placeObject(Anchor anchor) {
        Log.d(TAG, "Iniciando colocación del objeto.");
        if (toyCarRenderable == null) {
            Log.e(TAG, "ERROR: toyCarRenderable es NULL. No se puede colocar el objeto.");
            Toast.makeText(this, "Error: Modelo no disponible para colocar.", Toast.LENGTH_LONG).show();
            return;
        }

        AnchorNode anchorNode = new AnchorNode(anchor);
        anchorNode.setParent(arSceneView.getScene());
        Log.d(TAG, "AnchorNode creado y adjuntado a la escena.");

        if (transformationSystem.getSelectedNode() != null) {
            transformationSystem.selectNode(null);
            Log.d(TAG, "Deseleccionando nodo anterior.");
        }

        TransformableNode modelNode = new TransformableNode(transformationSystem);

        modelNode.setParent(anchorNode);
        modelNode.setRenderable(toyCarRenderable);
        modelNode.setLocalPosition(new Vector3(0.0f, 0.1f, 0.0f));

        Log.d(TAG, "TransformableNode creado y renderable asignado. Renderable del nodo: " + (modelNode.getRenderable() != null));

        modelNode.getScaleController().setEnabled(true);
        modelNode.getTranslationController().setEnabled(true);
        modelNode.getRotationController().setEnabled(true);
        Log.d(TAG, "Controladores de escala, traslación y rotación habilitados.");

        modelNode.getScaleController().setMinScale(0.000025f);
        modelNode.getScaleController().setMaxScale(20f);
        Log.d(TAG, "Límites de escala establecidos: Min=0.01f, Max=500.0f.");

        currentModelNode = modelNode;

        transformationSystem.selectNode(modelNode);
        Log.d(TAG, "Nodo seleccionado en el TransformationSystem.");

        Toast.makeText(this, "¡Objeto colocado! Un dedo: rotar, dos dedos: escalar/mover.", Toast.LENGTH_LONG).show();
        Log.d(TAG, "Mensaje de Toast mostrado. Objeto colocado y transformable con rotación personalizada.");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume iniciado.");

        if (arSceneView == null) {
            Log.w(TAG, "arSceneView es null en onResume.");
            return;
        }

        if (arSceneView.getSession() == null) {
            Log.d(TAG, "Sesión ARCore no existe, intentando crear/reconfigurar.");
            try {
                switch (ArCoreApk.getInstance().requestInstall(this, !installRequested)) {
                    case INSTALL_REQUESTED:
                        installRequested = true;
                        Log.d(TAG, "Instalación de ARCore solicitada.");
                        return;
                    case INSTALLED:
                        Log.d(TAG, "ARCore ya instalado.");
                        break;
                }

                if (!CameraPermissionHelper.hasCameraPermission(this)) {
                    Log.w(TAG, "No hay permisos de cámara, solicitando.");
                    CameraPermissionHelper.requestCameraPermission(this);
                    return;
                }

                Session session = new Session(this);
                Config config = new Config(session);
                config.setUpdateMode(Config.UpdateMode.LATEST_CAMERA_IMAGE);
                session.configure(config);
                arSceneView.setupSession(session);
                Log.d(TAG, "Sesión ARCore configurada y adjuntada a ArSceneView.");

            } catch (UnavailableArcoreNotInstalledException e) {
                Log.e(TAG, "ARCore no instalado: " + e.getMessage());
                Toast.makeText(this, "Por favor instala ARCore", Toast.LENGTH_LONG).show();
                finish();
                return;
            } catch (UnavailableUserDeclinedInstallationException e) {
                Log.e(TAG, "Usuario rechazó instalación de ARCore: " + e.getMessage());
                Toast.makeText(this, "Por favor instala ARCore", Toast.LENGTH_LONG).show();
                finish();
                return;
            } catch (UnavailableApkTooOldException e) {
                Log.e(TAG, "APK de ARCore muy antigua: " + e.getMessage());
                Toast.makeText(this, "Por favor actualiza ARCore", Toast.LENGTH_LONG).show();
                finish();
                return;
            } catch (UnavailableSdkTooOldException e) {
                Log.e(TAG, "SDK de ARCore muy antigua: " + e.getMessage());
                Toast.makeText(this, "Por favor actualiza la aplicación", Toast.LENGTH_LONG).show();
                finish();
                return;
            } catch (UnavailableDeviceNotCompatibleException e) {
                Log.e(TAG, "Dispositivo no compatible con AR: " + e.getMessage());
                Toast.makeText(this, "Este dispositivo no soporta AR", Toast.LENGTH_LONG).show();
                finish();
                return;
            } catch (Exception e) {
                Log.e(TAG, "Error general al inicializar AR: " + e.getMessage(), e);
                Toast.makeText(this, "Error al inicializar AR: " + e.getMessage(), Toast.LENGTH_LONG).show();
                finish();
                return;
            }
        }

        try {
            arSceneView.resume();
            Log.d(TAG, "ArSceneView resumido.");
        } catch (CameraNotAvailableException ex) {
            Log.e(TAG, "Cámara no disponible: " + ex.getMessage());
            Toast.makeText(this, "Cámara no disponible", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause iniciado.");
        if (arSceneView != null) {
            arSceneView.pause();
            Log.d(TAG, "ArSceneView pausado.");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy iniciado.");
        cleanupResources();
        if (arSceneView != null) {
            arSceneView.destroy();
            Log.d(TAG, "ArSceneView destruido.");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        Log.d(TAG, "onRequestPermissionsResult recibido.");
        if (!CameraPermissionHelper.hasCameraPermission(this)) {
            Toast.makeText(this, "Se necesitan permisos de cámara para usar AR", Toast.LENGTH_LONG).show();
            if (!CameraPermissionHelper.shouldShowRequestPermissionRationale(this)) {
                CameraPermissionHelper.launchPermissionSettings(this);
                Log.d(TAG, "Permiso de cámara denegado persistentemente, lanzando configuración.");
            }
            finish();
        } else {
            Log.d(TAG, "Permiso de cámara concedido.");
        }
    }
}