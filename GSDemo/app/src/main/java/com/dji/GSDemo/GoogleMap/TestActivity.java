package com.dji.GSDemo.GoogleMap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Timer;
import java.util.TimerTask;

public class TestActivity extends AppCompatActivity {

    EditText xT, yT, zT;
    Button pushB;
    double alta = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        xT = findViewById(R.id.xT);
        yT = findViewById(R.id.yT);
        zT = findViewById(R.id.zT);
        pushB = findViewById(R.id.pushB);



        double a = getTrimmedLatZ(33.62819301581383);
        double b = getTrimmedLngX(73.0965740558498683);
//        Toast.makeText(this, a+"\n"+b, Toast.LENGTH_SHORT).show();


        pushB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (xT.getText().toString().equals("") || yT.getText().toString().equals("") || zT.getText().toString().equals("")){
                    Toast.makeText(TestActivity.this, "Please enter all coordinates", Toast.LENGTH_SHORT).show();
                }
                else{
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("coordinates").child("dronelocation");
                    alta += 10;
                    reference.child("x").setValue(getTrimmedLngX(Double.valueOf(xT.getText().toString())));
                    reference.child("z").setValue(getTrimmedLatZ(Double.valueOf(zT.getText().toString())));
                    reference.child("y").setValue(alta);
                }
            }
        });

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            int num = 0;
            @Override
            public void run() {
                num += 5;
                Toast.makeText(TestActivity.this, "Hello", Toast.LENGTH_SHORT).show();
                handler.postDelayed(this, 3000);//Repeated delay after first one
            }
        }, 1000);//First time delay

    }

    public void giveMeTrimmedValue(double coordinate){
        String b = String.valueOf(coordinate);
        String c = b.charAt(6) + "" + b.charAt(7);
        Toast.makeText(this, c, Toast.LENGTH_SHORT).show();

    }

    public double getTrimmedLatZ(double coordinate){
        String b = String.valueOf(coordinate);
        String c = b.charAt(8) + "" + b.charAt(9) + "" + b.charAt(10);
        double d = Double.valueOf(c);
        return d;
    }

    public double getTrimmedLngX(double coordinate){
        String b = String.valueOf(coordinate);
        String c = b.charAt(7) + "" + b.charAt(8) + "" + b.charAt(9);
        double d = Double.valueOf(c);
        return d;
    }

    public double getTrimmedAltY(double coordinate){
        String b = String.valueOf(coordinate);
        String c = b.charAt(7) + "" + b.charAt(8) + "" + b.charAt(9);
        double d = Double.valueOf(c);
        return d;
    }
}
