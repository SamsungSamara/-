package com.example.gamego;


import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Color;
import android.opengl.Visibility;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech;
// Утилиты разные
public class Util {
    // Говорим голосом, используем для отладки флаг
    public static boolean useVoiceForDebug = false;
    // Говорилка
    public static TextToSpeech Tts;


    // Говорим
    public static void speak(String text) {
        try {
            // ставим в очередь, 1=add, 0=falsh
            Tts.speak(text, 1, null);
        } catch (Exception ex) {
        }
    }


    // Ставим x y для view
    public static void setXY(View v, float x, float y) {
        // Если требуемая координата x отличается от позиции view
        if (Math.abs(v.getX() - x) > 0.001) {
            // Ставим новую позицию x
            v.setX(x);
        }
        // Если требуемая координата y отличается от позиции view
        if (Math.abs(v.getY() - y) > 0.001) {
            // Ставим новую позицию y
            v.setY(y);
        }
    }
    public static void enableVoice(boolean enabled){
        useVoiceForDebug = enabled;
    }

    // Показываем отладочную информацию
    public static void debug(String text) {
// Если отлаживаем голосом то говорим
        if (useVoiceForDebug) {
            // Говорим
            Util.speak(text);
        }
    }

}
