package jp.techacademy.kiyoshi.ooyama.taskapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Ohyama on 2016/08/09.
 */
public class TaskAlarmReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent){
        Log.d("TaskApp","onRecive");

    }
}
