package jp.techacademy.kiyoshi.ooyama.taskapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;

public class MainActivity extends AppCompatActivity {

    public final static String EXTRA_TASK ="jp.techacademy.kiyoshi.ooyama.taskapp";
    private Realm mRealm;
    private RealmResults<Task> mTaskRealmResults;
    private RealmChangeListener mRealmListener = new RealmChangeListener() {
        @Override
        public void onChange() {
            reloadListView();
        }
    };

    private ListView mListView;
    private TaskAdapter mTaskAdapter;
    private EditText editText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText=(EditText)findViewById(R.id.edt);
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                // EditTextのフォーカスが外れた場合
                if (hasFocus == false) {
                    softKeyOff(v);
                    //抽出
                    execCategoryFilter();
                }
            }
        });


        //タスク追加
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,InputActivity.class);
                startActivity(intent);
            }
        });

        //カテゴリ検索
        Button button=(Button)findViewById(R.id.categoryFilter);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                execCategoryFilter();
                softKeyOff(v);
            }
        });


        //カテゴリ検索クリア
        Button buttonClear=(Button)findViewById(R.id.categoryFilterClear);
        buttonClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editText.setText("");
                mTaskRealmResults = mRealm.where(Task.class).findAll();
                mTaskRealmResults.sort("date", Sort.DESCENDING);
                reloadListView();
            }
        });



        // Realmの設定
        mRealm = Realm.getDefaultInstance();
        mTaskRealmResults = mRealm.where(Task.class).findAll();
        mTaskRealmResults.sort("date", Sort.DESCENDING);
        mRealm.addChangeListener(mRealmListener);

        //ListView設定
        mTaskAdapter = new TaskAdapter(MainActivity.this);
        mListView = (ListView) findViewById(R.id.listView1);

        //ListViewをタップしたときの処理
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 入力・編集する画面に遷移させる
                Task task = (Task) parent.getAdapter().getItem(position);
                Intent intent = new Intent(MainActivity.this, InputActivity.class);
                intent.putExtra(EXTRA_TASK, task);
                startActivity(intent);
            }
        });

        // ListViewを長押ししたときの処理
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                // タスクを削除する
                final Task task = (Task) parent.getAdapter().getItem(position);
                // ダイアログを表示する
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("削除");
                builder.setMessage(task.getTitle() + "を削除しますか");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        RealmResults<Task> results = mRealm.where(Task.class).equalTo("id", task.getId()).findAll();
                        mRealm.beginTransaction();
                        results.clear();
                        mRealm.commitTransaction();

                        reloadListView();
                    }
                });
                builder.setNegativeButton("CANCEL", null);

                AlertDialog dialog = builder.create();
                dialog.show();
                return true;
            }
        });

        if (mTaskRealmResults.size() == 0) {
            // アプリ起動時にタスクの数が0であった場合は表示テスト用のタスクを作成する
            addTaskForTest();
        }
        reloadListView();
    }

    private void softKeyOff(View v){
        // ソフトキーボードを非表示にする
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        Log.d("TaskApp","ソフトキーオフ");
    }

    private void execCategoryFilter(){
        String filterCategory=editText.getText().toString();
        Log.d("TaskApp",filterCategory);
        mTaskRealmResults = mRealm.where(Task.class).equalTo("category",filterCategory).findAll();
        reloadListView();
    }


    private void reloadListView() {

        ArrayList<Task> taskArrayList = new ArrayList<>();

        for (int i = 0; i < mTaskRealmResults.size(); i++) {
            Task task = new Task();

            task.setId(mTaskRealmResults.get(i).getId());
            task.setTitle(mTaskRealmResults.get(i).getTitle());
            task.setContents(mTaskRealmResults.get(i).getContents());
            task.setDate(mTaskRealmResults.get(i).getDate());
            task.setCategory(mTaskRealmResults.get(i).getCategory());

            taskArrayList.add(task);
        }

        mTaskAdapter.setTaskArrayList(taskArrayList);
        mListView.setAdapter(mTaskAdapter);
        mTaskAdapter.notifyDataSetChanged();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        mRealm.close();
    }

    private void addTaskForTest() {
        Task task = new Task();
        task.setTitle("作業");
        task.setContents("プログラムを書いてPUSHする");
        task.setDate(new Date());
        task.setId(0);
        mRealm.beginTransaction();
        mRealm.copyToRealmOrUpdate(task);
        mRealm.commitTransaction();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
