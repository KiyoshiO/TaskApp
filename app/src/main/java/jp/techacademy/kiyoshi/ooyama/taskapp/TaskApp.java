package jp.techacademy.kiyoshi.ooyama.taskapp;

import android.app.Application;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by Ohyama on 2016/08/02.
 */
public class TaskApp extends Application {
    @Override
    public void onCreate(){
        super.onCreate();
        RealmConfiguration realmConfiguration=new RealmConfiguration.Builder(this).build();
        Realm.setDefaultConfiguration(realmConfiguration);
    }

}
