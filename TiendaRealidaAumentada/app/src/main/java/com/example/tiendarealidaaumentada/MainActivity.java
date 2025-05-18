package com.example.tiendarealidaaumentada;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int RC_SIGN_IN = 9001;

    private EditText editTextEmailAddress;
    private EditText editTextPassword;
    private Button btnIniciarSesion;
    private Button btnRegistrarse;
    private SignInButton btnGoogleSignIn;

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Configurar Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Inicializar vistas
        editTextEmailAddress = findViewById(R.id.editTextEmailAddress);
        editTextPassword = findViewById(R.id.editTextPassword);
        btnIniciarSesion = findViewById(R.id.btnIniciarSesion);
        btnRegistrarse = findViewById(R.id.btnRegistrarse);
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);

        // Listeners de los botones
        btnIniciarSesion.setOnClickListener(v -> signInWithEmailPassword());
        btnRegistrarse.setOnClickListener(v -> createUserWithEmailPassword());
        btnGoogleSignIn.setOnClickListener(v -> signInWithGoogle());
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            // El usuario ya está autenticado, puedes redirigirlo a la actividad principal
            // Por ejemplo:
            // Intent intent = new Intent(MainActivity.this, HomeActivity.class);
            // startActivity(intent);
            // finish();
        }
    }

    private void createUserWithEmailPassword() {
        String email = editTextEmailAddress.getText().toString();
        String password = editTextPassword.getText().toString();

        if (!email.isEmpty() && !password.isEmpty()) {
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(MainActivity.this, "Registro exitoso.",Toast.LENGTH_SHORT).show();
                            //updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Registro fallido.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    });
        } else {
            Toast.makeText(MainActivity.this, "Por favor, introduce email y contraseña.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void signInWithEmailPassword() {
        String email = editTextEmailAddress.getText().toString();
        String password = editTextPassword.getText().toString();

        if (!email.isEmpty() && !password.isEmpty()) {
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Inicio de sesión fallido.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    });
        } else {
            Toast.makeText(MainActivity.this, "Por favor, introduce email y contraseña.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.getIdToken());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                Toast.makeText(this, "Error al iniciar sesión con Google.", Toast.LENGTH_SHORT).show();
                updateUI(null);
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "firebaseAuthWithGoogle:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        updateUI(user);
                        // Redirige a la siguiente actividad aquí
                        // Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                        // startActivity(intent);
                        // finish();
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "firebaseAuthWithGoogle:failure", task.getException());
                        Toast.makeText(MainActivity.this, "Autenticación con Google fallida.",
                                Toast.LENGTH_SHORT).show();
                        updateUI(null);
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            // El usuario ha iniciado sesión correctamente
            Toast.makeText(MainActivity.this, "Inicio de sesión exitoso.",
                    Toast.LENGTH_SHORT).show();
            // Aquí puedes iniciar la siguiente actividad o realizar acciones después del login
            // Por ejemplo:
            // Intent intent = new Intent(this, HomeActivity.class);
            // startActivity(intent);
            // finish();
            Intent intent = new Intent(MainActivity.this, Home.class);
            startActivity(intent);
            finish();
        } else {
            // El usuario no ha iniciado sesión
            // Puedes mostrar un mensaje o actualizar la interfaz de usuario en consecuencia
        }
    }
}