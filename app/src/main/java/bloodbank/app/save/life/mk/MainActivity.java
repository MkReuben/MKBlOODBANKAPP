package bloodbank.app.save.life.mk;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import bloodbank.app.save.life.mk.Donors.DonorLoginRegisterActivity;
import bloodbank.app.save.life.mk.Recipients.RecipientLoginRegisterActivity;

public class MainActivity extends AppCompatActivity {


    private TextView  WelcomeDonorBtn,WelcomeRecepientBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        WelcomeDonorBtn = findViewById(R.id.donor_tv);
        WelcomeRecepientBtn =findViewById(R.id.recipient_tv);

        WelcomeRecepientBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {

                Intent LoginRegisterRecepientIntent = new Intent(MainActivity.this, RecipientLoginRegisterActivity.class);
                startActivity(LoginRegisterRecepientIntent);

            }
        });
        WelcomeDonorBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent LoginRegisterDonorIntent = new Intent(MainActivity.this, DonorLoginRegisterActivity.class);
                startActivity(LoginRegisterDonorIntent);

            }
        });


    }
}
