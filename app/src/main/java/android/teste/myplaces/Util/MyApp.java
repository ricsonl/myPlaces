package android.teste.myplaces.Util;

import android.app.Application;
import android.content.Context;

/**
 * Created by Lucas on 12/12/2016.
 */
public class MyApp extends Application {

    private static Context context;

    public void onCreate() {
        super.onCreate();
        MyApp.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return MyApp.context;
    }
}
