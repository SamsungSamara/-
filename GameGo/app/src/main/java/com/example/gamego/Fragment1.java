package com.example.gamego;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.io.BufferedReader;

public class Fragment1 extends DialogFragment {
    public RadioButton rbtn, rbtn2, rbtn3;
    public RadioGroup radioGroup;
    public CheckBox checkBox, checkBox1;

    // Контрол с нарисованным полем
    private BoardView boardView;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // получаем ресурсы радиокнопки 1
        rbtn = (RadioButton) getView().findViewById(R.id.radioButton);
        // получаем ресурсы радиокнопки 2
        rbtn2 = (RadioButton) getView().findViewById(R.id.radioButton2);
        // получаем ресурсы радиокнопки 3
        rbtn3 = (RadioButton) getView().findViewById(R.id.radioButton3);
        // получаем ресурсы радиогруппы
        radioGroup = (RadioGroup) getView().findViewById(R.id.radiogroup);
        // получаем ресурсы чекбоксов
        checkBox = (CheckBox) getView().findViewById(R.id.checkBox);
        checkBox1 = (CheckBox) getView().findViewById(R.id.checkBox1);
        this.boardView = new BoardView(getContext());
        // если существуют все интересующие нас объекты интерфейса
        if(rbtn!= null & rbtn2!= null & rbtn3!= null & checkBox!=null & checkBox1!=null) {
            // получаем размер поля
            switch (boardView.getCellCount()) {
                // если 15 то отмечаем соответсвующую кнопку в радиогруппе
                case 15:
                    radioGroup.check(R.id.radioButton2);
                    break;
                // если 20 то отмечаем соответсвующую кнопку в радиогруппе
                case 20:
                    radioGroup.check(R.id.radioButton3);
                    break;
                // если 10 то отмечаем соответсвующую кнопку в радиогруппе
                default:
                    radioGroup.check(R.id.radioButton);
            }
            // если разрешены островки то ставим галочку
            if(boardView.areIslandsEnabled()){
                checkBox.setChecked(true);
            }
            // иначе уберем галочку
            else{
                checkBox.setChecked(false);
            }
            // галочка если размешено использование голосового движка
            if (Util.useVoiceForDebug){
                checkBox1.setChecked(true);
            }
            // иначе уберем галочку
            else {
                checkBox1.setChecked(false);
            }
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_1, container, false);

    }
}