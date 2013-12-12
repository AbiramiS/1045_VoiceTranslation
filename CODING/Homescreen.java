package com.example.voicerecognition;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class Homescreen extends Activity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home_screen);
		Thread timer=new Thread(){
			public void run() {
				try {
					sleep(5000);
				} catch(InterruptedException e){
					e.printStackTrace();
				}finally {
					Intent openHome=new Intent("com.example.voicerecognition.MainActivity");
					startActivity(openHome);
				}
			}
		};
		timer.start();
	}

	

}
