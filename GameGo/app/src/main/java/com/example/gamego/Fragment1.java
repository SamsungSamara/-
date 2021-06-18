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
    public CheckBox checkBox;

    // Контрол с нарисованным полем
    private BoardView boardView;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        rbtn = (RadioButton) getView().findViewById(R.id.radioButton);
        rbtn2 = (RadioButton) getView().findViewById(R.id.radioButton2);
        rbtn3 = (RadioButton) getView().findViewById(R.id.radioButton3);
        radioGroup = (RadioGroup) getView().findViewById(R.id.radiogroup);
        checkBox = (CheckBox) getView().findViewById(R.id.checkBox);
        this.boardView = new BoardView(getContext());
        if(rbtn!= null & rbtn2!= null & rbtn3!= null & checkBox!=null) {
            switch (boardView.getCellCount()) {
                case 15:
                    radioGroup.check(R.id.radioButton2);
                    break;
                case 20:
                    radioGroup.check(R.id.radioButton3);
                    break;
                default:
                    radioGroup.check(R.id.radioButton);
            }
            if(boardView.areIslandsEnabled()){
                checkBox.setChecked(true);
            }
            else{
                checkBox.setChecked(false);
            }
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_1, container, false);

    }
}