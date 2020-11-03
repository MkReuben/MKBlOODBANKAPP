package bloodbank.app.save.life.mk;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;


    public class Splash extends AppCompatActivity {

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_splash);

            Thread splash = new Thread(){
                @Override
                public void run() {
                    try {
                        sleep(3000);
                        Intent go = new Intent(Splash.this,MainActivity.class);
                        startActivity(go);
                        finish();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };
            splash.start();
        }
    }
