package bloodbank.app.save.life.mk.Donors;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import bloodbank.app.save.life.mk.R;

public class DonorLoginRegisterActivity extends AppCompatActivity {

    private Button donorLoginBtn,donorRegisterButton;
    private TextView donorRegisterLink,donorStatus;
    private EditText emailDonor,passwordDonor;
    private ProgressDialog loadingBar;

    private FirebaseAuth mAuth;

    private DatabaseReference donorDatabaseRef;
    private String onlineDonorID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donor_login_register);

        mAuth=FirebaseAuth.getInstance();

        donorLoginBtn =findViewById(R.id.login_donor_btn);
        donorRegisterButton = findViewById(R.id.register_donor_btn);
        donorRegisterLink =findViewById(R.id.register_donor_link);
        donorStatus = findViewById(R.id.donor_status);
        loadingBar=new ProgressDialog(this);

        emailDonor =findViewById(R.id.email_donor);
        passwordDonor=findViewById(R.id.password_donor);




        donorRegisterButton.setVisibility(View.INVISIBLE);
        donorRegisterButton.setEnabled(false);

        donorRegisterLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                donorLoginBtn.setVisibility(View.INVISIBLE);
                donorRegisterLink.setVisibility(View.INVISIBLE);
                donorStatus.setText("Register Donor");

                donorRegisterButton.setVisibility(View.VISIBLE);
                donorRegisterButton.setEnabled(true);


            }
        });

        donorRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                String email = emailDonor.getText().toString();
                String password=passwordDonor.getText().toString();

                RegisterDonor(email,password);

            }
        });
        donorLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                String email = emailDonor.getText().toString();
                String password=passwordDonor.getText().toString();
                SignInDonor(email,password);

            }
        });
    }

    private void SignInDonor(String email, String password)
    {
        if (TextUtils.isEmpty(email))
        {
            Toast.makeText(DonorLoginRegisterActivity.this, "Please write Email..", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(password))
        {
            Toast.makeText(DonorLoginRegisterActivity.this, "Please write Password..", Toast.LENGTH_SHORT).show();
        }

        else
        {
            loadingBar.setTitle("Donor Login");
            loadingBar.setMessage("Please wait ,while we are checking your credentials...");
            loadingBar.show();

            mAuth.signInWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task)
                        {
                            if (task.isSuccessful())
                            {


                                Intent donorIntent = new Intent(DonorLoginRegisterActivity.this, DonorMapActivity.class);
                                startActivity(donorIntent);

                                Toast.makeText(DonorLoginRegisterActivity.this, "Donor Logged In Successfully", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();


                            }
                            else
                            {
                                Toast.makeText(DonorLoginRegisterActivity.this, "Login UnSuccessfully,Please Try Again", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }

                        }
                    });
        }
    }

    private void RegisterDonor(String email, String password)
    {
        if (TextUtils.isEmpty(email))
        {
            Toast.makeText(DonorLoginRegisterActivity.this, "Please write Email..", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(password))
        {
            Toast.makeText(DonorLoginRegisterActivity.this, "Please write Password..", Toast.LENGTH_SHORT).show();
        }

        else
        {
            loadingBar.setTitle("Donor Registration");
            loadingBar.setMessage("Please wait ,while we are registering your data...");
            loadingBar.show();

            mAuth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task)
                        {
                            if (task.isSuccessful())
                            {

                                onlineDonorID=mAuth.getCurrentUser().getUid();
                                donorDatabaseRef= FirebaseDatabase.getInstance().getReference()
                                        .child("Users").child("Donors").child(onlineDonorID);

                                donorDatabaseRef.setValue(true);

                                Intent donorIntent = new Intent(DonorLoginRegisterActivity.this,DonorMapActivity.class);
                                startActivity(donorIntent);

                                Toast.makeText(DonorLoginRegisterActivity.this, "Donor Registered Successfully", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();

                            }
                            else
                            {
                                Toast.makeText(DonorLoginRegisterActivity.this, "Registration UnSuccessfully,Please Try Again", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }

                        }
                    });
        }
    }
}
