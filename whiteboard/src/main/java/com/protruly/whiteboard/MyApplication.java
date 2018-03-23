package com.protruly.whiteboard;

import android.app.Application;
import android.content.Context;

public class MyApplication extends Application{
   
	 public static volatile Context applicationContext = null;
	
	
	@Override
    public void onCreate() {
        super.onCreate();
        applicationContext = getApplicationContext();

//        LitePal.initialize(this);
    }
}
