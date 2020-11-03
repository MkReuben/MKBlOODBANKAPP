package bloodbank.app.save.life.mk.Recipients;

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

public class RecipientLoginRegisterActivity extends AppCompatActivity
{

    private Button recepientLoginBtn,recipientRegisterButton;
    private TextView recepientRegisterLink,recepientStatus;
    private EditText emailRecepient,passwordRecepient;

    private ProgressDialog loadingBar;

    private FirebaseAuth mAuth;
    private DatabaseReference recipientDatabaseRef;
    private String onlineRecipientID;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipient_login_register);

        mAuth=FirebaseAuth.getInstance();
        loadingBar=new ProgressDialog(this);



        recepientLoginBtn =findViewById(R.id.login_recepient_btn);
        recipientRegisterButton = findViewById(R.id.register_recepient_btn);
        recepientRegisterLink =findViewById(R.id.register_recepient_link);
        recepientStatus = findViewById(R.id.recepient_status);
        emailRecepient =findViewById(R.id.email_recepient);
        passwordRecepient=findViewById(R.id.password_recepient);

        recipientRegisterButton.setVisibility(View.INVISIBLE);
        recipientRegisterButton.setEnabled(false);

        recepientRegisterLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                recepientLoginBtn.setVisibility(View.INVISIBLE);
                recepientRegisterLink.setVisibility(View.INVISIBLE);
                recepientStatus.setText("Register Recipient");

                recipientRegisterButton.setVisibility(View.VISIBLE);
                recipientRegisterButton.setEnabled(true);


            }
        });

        recipientRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {

                String email = emailRecepient.getText().toString();
                String password =passwordRecepient.getText().toString();

                RegisterRecipient(email,password);

            }
        });
        recepientLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                String email = emailRecepient.getText().toString();
                String password =passwordRecepient.getText().toString();

                SignInRecepient(email,password);

            }
        });
    }

    private void SignInRecepient(String email, String password)
    {
        if (TextUtils.isEmpty(email))
        {
            Toast.makeText(RecipientLoginRegisterActivity.this, "Please write Email..", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(password))
        {
            Toast.makeText(RecipientLoginRegisterActivity.this, "Please write Password..", Toast.LENGTH_SHORT).show();
        }

        else {
            loadingBar.setTitle("Recipient Login");
            loadingBar.setMessage("Please wait ,while we are checking your credentials...");
            loadingBar.show();

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful())
                            {
                                Intent intent = new Intent(RecipientLoginRegisterActivity.this, RecipientMapActivity.class);
                                startActivity(intent);

                                Toast.makeText(RecipientLoginRegisterActivity.this, "Recipient Logged in Successfully", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();

                            } else {
                                Toast.makeText(RecipientLoginRegisterActivity.this, "Login UnSuccessfully,Please Try Again", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }

                        }
                    });
        }
    }

    private void RegisterRecipient(String email, String password)
    {
        if (TextUtils.isEmpty(email))
        {
            Toast.makeText(RecipientLoginRegisterActivity.this, "Please write Email..", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(password))
        {
            Toast.makeText(RecipientLoginRegisterActivity.this, "Please write Password..", Toast.LENGTH_SHORT).show();
        }

        else {
            loadingBar.setTitle("Recipient Registration");
            loadingBar.setMessage("Please wait ,while we are registering your data...");
            loadingBar.show();

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful())
                            {
                                onlineRecipientID=mAuth.getCurrentUser().getUid();
                                recipientDatabaseRef= FirebaseDatabase.getInstance().getReference()
                                        .child("Users").child("Recipients").child(onlineRecipientID);

                                recipientDatabaseRef.setValue(true);

                                Intent intent = new Intent(RecipientLoginRegisterActivity.this,RecipientMapActivity.class);
                                startActivity(intent);


                                Toast.makeText(RecipientLoginRegisterActivity.this, "Recipient Registered Successfully", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            } else {
                                Toast.makeText(RecipientLoginRegisterActivity.this, "Registration UnSuccessfully,Please Try Again", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }

                        }
                    });
        }
    }
}
