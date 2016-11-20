package eu.execom.todolistgrouptwo.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;
import android.widget.Toast;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.EditorAction;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import eu.execom.todolistgrouptwo.R;
import eu.execom.todolistgrouptwo.database.wrapper.UserDAOWrapper;
import eu.execom.todolistgrouptwo.model.User;

@EActivity(R.layout.activity_login)
public class LoginActivity extends AppCompatActivity {

    public static final int REGISTER_RESULT = 1;

    @Bean
    UserDAOWrapper userDAOWrapper;

    @ViewById
    EditText username;

    @ViewById
    EditText password;

    @EditorAction(R.id.password)
    @Click
    void login() {
        final String username = this.username.getText().toString();
        final String password = this.password.getText().toString();

        tryLogin(username, password);
    }

    @Background
    void tryLogin(String username, String password) {
        final User user = userDAOWrapper.findByUsernameAndPassword(username, password);

        if (user == null) {
            showLoginError();
        } else {
            loginSuccess(user.getId());
        }
    }

    @UiThread
    void showLoginError() {
        Toast.makeText(this,
                "Invalid username and password combination.",
                Toast.LENGTH_SHORT)
                .show();
    }

    @Click
    void register() {
        RegisterActivity_.intent(this).startForResult(REGISTER_RESULT);
    }

    @OnActivityResult(value = REGISTER_RESULT)
    void loginUser(int resultCode, @OnActivityResult.Extra("user_id") Long id) {
        if (resultCode == RESULT_OK) {
            loginSuccess(id);
        }
    }

    @UiThread
    void loginSuccess(Long id) {
        final Intent intent = new Intent();
        intent.putExtra("user_id", id);

        setResult(RESULT_OK, intent);
        finish();
    }
}
