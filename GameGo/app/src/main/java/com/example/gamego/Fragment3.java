package com.example.gamego;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.Map;


public class Fragment3 extends DialogFragment {

    public TextView textView;
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        textView = (TextView) getView().findViewById(R.id.textViewFragment3);
        textView.setMovementMethod(new ScrollingMovementMethod());
        loadRating();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_3, container,false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    public void loadRating() {
        // Запускаем обновление интерфейса в основном потоке приложения
        MainActivity.Instance.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //Ловим исключение
                try {
                    if (textView != null) {
                        textView.setText(MainActivity.Instance.system());
                    }

                }
                catch (Exception ex) {
                    // Показываем сообщение об ошибке с текстом исключения
                    Toast.makeText(MainActivity.Instance, "Случилась ошибка в обновлении интерфейса: " + ex.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
