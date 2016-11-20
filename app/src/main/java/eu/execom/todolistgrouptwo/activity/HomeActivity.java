package eu.execom.todolistgrouptwo.activity;

import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.util.List;

import eu.execom.todolistgrouptwo.R;
import eu.execom.todolistgrouptwo.adapter.TaskAdapter;
import eu.execom.todolistgrouptwo.database.wrapper.TaskDAOWrapper;
import eu.execom.todolistgrouptwo.database.wrapper.UserDAOWrapper;
import eu.execom.todolistgrouptwo.model.Task;
import eu.execom.todolistgrouptwo.model.User;
import eu.execom.todolistgrouptwo.preference.UserPreferences_;

/**
 * Home {@link AppCompatActivity Activity} for navigation and listing all tasks.
 */
@EActivity(R.layout.activity_home)
public class HomeActivity extends AppCompatActivity {

    /**
     * Used for logging purposes.
     */
    private static final String TAG = HomeActivity.class.getSimpleName();

    /**
     * Used for identifying results from different activities.
     */
    protected static final int ADD_TASK_REQUEST_CODE = 42;
    protected static final int LOGIN_REQUEST_CODE = 420; // BLAZE IT

    /**
     * Tasks are kept in this list during a user session.
     */
    private List<Task> tasks;

    private User user;

    /**
     * {@link FloatingActionButton FloatingActionButton} for starting the
     * {@link AddTaskActivity AddTaskActivity}.
     */
    @ViewById
    FloatingActionButton addTask;

    /**
     * {@link ListView ListView} for displaying the tasks.
     */
    @ViewById
    ListView listView;

    /**
     * {@link TaskAdapter Adapter} for providing data to the {@link ListView listView}.
     */
    @Bean
    TaskAdapter adapter;

    @Bean
    UserDAOWrapper userDAOWrapper;

    @Bean
    TaskDAOWrapper taskDAOWrapper;

    @Pref
    UserPreferences_ userPreferences;

    @AfterViews
    @Background
    void checkUser() {
        if (!userPreferences.userId().exists()) {
            LoginActivity_.intent(this).startForResult(LOGIN_REQUEST_CODE);
            return;
        }

        user = userDAOWrapper.findById(userPreferences.userId().get());
        tasks = taskDAOWrapper.findByUser(user);

        initData();
    }

    /**
     * Loads tasks from the {@link android.content.SharedPreferences SharedPreferences}
     * and sets the adapter.
     */
    @UiThread
    void initData() {
        listView.setAdapter(adapter);
        adapter.setTasks(tasks);

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == SCROLL_STATE_IDLE) {
                    // button show
                    addTask.show();
                } else {
                    // button hide
                    addTask.hide();
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });
    }

    /**
     * Called when the {@link FloatingActionButton FloatingActionButton} is clicked.
     */
    @Click
    void addTask() {
        Log.i(TAG, "Add task clicked!");
        AddTaskActivity_.intent(this).startForResult(ADD_TASK_REQUEST_CODE);
    }

    /**
     * Called when the {@link AddTaskActivity AddTaskActivity} finishes.
     *
     * @param resultCode Indicates whether the activity was successful.
     * @param task         The new task.
     */
    @OnActivityResult(ADD_TASK_REQUEST_CODE)
    void onResult(int resultCode, @OnActivityResult.Extra String task) {
        if (resultCode == RESULT_OK) {
            Toast.makeText(this, task, Toast.LENGTH_SHORT).show();
            final Gson gson = new Gson();
            final Task newTask = gson.fromJson(task, Task.class);

            tasks.add(newTask);
            newTask.setUser(user);
            adapter.addTask(newTask);

            taskDAOWrapper.create(newTask);
        }
    }

    @OnActivityResult(LOGIN_REQUEST_CODE)
    void onLogin(int resultCode, @OnActivityResult.Extra("user_id") Long id) {
        if (resultCode == RESULT_OK) {
            userPreferences.userId().put(id);
            checkUser();
        }
    }
}
