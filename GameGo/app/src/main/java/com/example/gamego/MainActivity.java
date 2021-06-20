package com.example.gamego;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.icu.util.Calendar;
import android.opengl.Visibility;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Array;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static com.example.gamego.BoardView.CELL_COUNT;

// Основная активность приложения
public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {
    // Использовать старый дизайн флаг
    public static final boolean USE_OLD_DESIGN = false;
    //Ссылка на этот класс
    public static MainActivity Instance;
    // Контрол с нарисованным полем
    public BoardView boardView;
    // Статус игры TextView;
    private TextView gameStatusTextView;
    //Уровень игры TextView;
    public TextView LevelTextView;
    // Отменяем ход кнопка
    private Button moveBackButton;
    // Новая игра кнопка
    private Button newGameButton;
    // Контрол диалогового фрагмента
    Fragment1 fragment1;
    // Контрол диалогового фрагмента 2
    Fragment2 fragment2;
    // Контрол диалогового фрагмента 3
    Fragment3 fragment3;
    // Переменная уровня true - первый уровень false = второй
    public boolean firstLevelOn;
    // объект SharedPreferences
    public static SharedPreferences sharedPreferences;
    // календарь для точной даты
    public Calendar c;
    // массивы слов для озвучивания результата игры
    public static String[] WinArray;
    public static String[] LoseArray;
    public static String[] GoodMove;
    public static String[] NiceMove;
    public static String[] NewGameWords;
    // Создание активности
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Вызываем конструктор суперкласса
        super.onCreate(savedInstanceState);
        // Запоминаем ссылку на активность
        Instance = this;
        // Инициализируем tts движок
        initTtsEngine();
        String s = BoardView.readFile();
        if (s!=null) {
            firstLevelOn = (Integer.parseInt(s.substring(0, 1)) == 1);
            BoardView.useIslands = (Integer.parseInt(s.substring(1, 2)) == 1);
            CELL_COUNT = Integer.parseInt(s.substring(2, 4));
            Util.useVoiceForDebug = (Integer.parseInt(s.substring(4, 5)) == 1);
        }
        // Ловим исключение
        try {
            // Если использовать старый дизайн
            if (USE_OLD_DESIGN) {
                // Создаем отрисовку доски
                this.boardView = new BoardView(this);
                // Ставим свой view
                setContentView(boardView);
            }
// Иначе используем layout
            else {
                // Ставим layout из ресурсов
                setContentView(R.layout.activity_main);
                // получаем содержимое массивов слов из ресурсов
                WinArray = ((String) getResources().getString(R.string.WinArray)).split("\\|");
                LoseArray = ((String) getResources().getString(R.string.LoseArray)).split("\\|");
                GoodMove = ((String) getResources().getString(R.string.GoodMove)).split("\\|");
                NiceMove = ((String) getResources().getString(R.string.NiceMove)).split("\\|");
                NewGameWords = ((String)getResources().getString(R.string.NewGameWords)).split("\\|");
                // Ищем контрол с полем
                this.boardView = (BoardView) findViewById(R.id.boardView4);
                // Ищем кнопку отменить ход
                this.moveBackButton = (Button) findViewById(R.id.MoveBackButton);
                // Ищем кнопку начать новую игру
                this.newGameButton = (Button) findViewById(R.id.NewGameButton);
                LevelTextView = (TextView)findViewById(R.id.LevelTextView);
                // Ставим уровень по умолчанию
                LevelTextView.setText("Уровень: " + (getLevel()==1 ? "Легкий" : "Сложный"));
                // Ищем TextView
                this.gameStatusTextView = (TextView) findViewById(R.id.GameStatusTextView);
                gameStatusTextView.getPaint().setAntiAlias(true);
                // Запускаем обновление интерфейса с задержкой
                updateUIDelayed();
            }
        }
        // поймали исключение
        catch (Exception ex) {
            // Показываем сообщение об ошибке с текстом исключения
            Toast.makeText(this, "Случилась ошибка в создании приложения: " + ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_items, menu);
        return true;
    }

    public void onSettings(){
        // Cоздаем объект класса Фрагмент1
        fragment1 = new Fragment1();
        // Показываем фрагмент
        fragment1.show(getSupportFragmentManager(), "Fragment");
    }
    public void onHelp(MenuItem item){
        // Cоздаем объект класса Фрагмент2
        fragment2 = new Fragment2();
        // Показываем фрагмент
        fragment2.show(getSupportFragmentManager(), "Fragment");

    }
    // кликнули на иконку меню "рейтинг"
    public void onData(MenuItem item){
        // Cоздаем объект класса Фрагмент2
        fragment3 = new Fragment3();
        // Показываем фрагмент
        fragment3.show(getSupportFragmentManager(), "Fragment");

    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.settings:
                onSettings();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    public void onExit(View view){
        // Закрываем фрагмент
        this.getSupportFragmentManager().beginTransaction().remove(fragment1).commit();
    }
    public void onExit3(View view){
        // Закрываем фрагмент
        this.getSupportFragmentManager().beginTransaction().remove(fragment3).commit();
    }
    // Ставим первый уровень
    public void levelDown(View view) {
        // ставим первый уровень
        firstLevelOn = true;
        // запишем настройки в файл
        boardView.writeFile();
        // выведем уровень на экран
        LevelTextView.setText("Уровень: " + (getLevel()==1?"Легкий":"Сложный"));
    }
    // Ставим второй уровень
    public void levelUp(View view) {
        // По умолчанию второй уровень
        firstLevelOn = false;
        // запишем настройки в файл
        boardView.writeFile();
        // выведем уровень на экран
        LevelTextView.setText("Уровень: " + (getLevel()==1?"Легкий":"Сложный"));
    }
    // Обработка нажатия на кнопку 10x10
    public void onTen(View view){
        // поставим размер поля
        boardView.setCellCount(10);
        // запишем настройки в файл
        boardView.writeFile();
    }
    // Обработка нажатия на кнопку 15x15
    public void onFif(View view){
        // поставим размер поля
        boardView.setCellCount(15);
        // запишем настройки в файл
        boardView.writeFile();
    }
    // Обработка нажатия на кнопку 20x20
    public void onTwen(View view){
        // поставим размер поля
        boardView.setCellCount(20);
        // запишем настройки в файл
        boardView.writeFile();
    }

    // Обработка нажатия на чекбокс включение/выключение островков
    public void onIslandsClick(View view){
        CheckBox checkBox = (CheckBox) view;
        checkBox.isChecked();
        // передаем значение типа bool в функцию islandsenable
        boardView.islandsEnable(checkBox.isChecked());
        // запишем настройки в файл
        boardView.writeFile();
    }
    // нажали на кнопку озвучивание
    public void onSpeakClick(View view){
        CheckBox checkBox = (CheckBox) view;
        checkBox.isChecked();
        // размешим голосовую отладку в util
        Util.enableVoice(checkBox.isChecked());
        // запишем в файл
        boardView.writeFile();
    }
    // Запускаем обновление интерфейса с задержкой
    private void updateUIDelayed() {
        // Создаем поток
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    // Ждем 500 миллисекунды
                    Thread.sleep(500);
                    // обновляем user interface (UI)
                    updateUI(null, false);
                }
                // Случилась ошибка но ее перехватят другие методы
                catch(Exception ex){ }
            }
        });
        // Запускаем поток
        t.start();
    }

    // обновляем user interface (UI)
    public void updateUI(String gameStatus, boolean showMoveBackButton) {
        // Запускаем обновление интерфейса в основном потоке приложения
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //Ловим исключение
                try {
                    // Если есть текстовое поле и есть статус игры
                    if (gameStatusTextView != null && gameStatus != null) {
                        // Обновляем текст статуса игры
                        gameStatusTextView.setText(gameStatus);
                        // Перерисовываем
                        gameStatusTextView.invalidate();
                    }
// Если есть кнопка отменить ход
                    if (moveBackButton != null) {
                        // Ставим видимость кнопки отменить ход
                        moveBackButton.setVisibility(showMoveBackButton ? View.VISIBLE : View.INVISIBLE);
                        // Перерисовываем
                        moveBackButton.invalidate();
                    }
                    DisplayMetrics displayMetrics = new DisplayMetrics();
                    getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                    float dy = displayMetrics.heightPixels/10;
                    // Двигаем Новая игра кнопка
                    Util  .setXY(newGameButton, displayMetrics.widthPixels/6, dy);
                    // Двигаем Отмена хода кнопка
                    Util.setXY(moveBackButton, displayMetrics.widthPixels*5/6 - moveBackButton.getWidth() , dy);
                    // Двигаем Статус игры TextView
                    gameStatusTextView.measure(0,0);
                    Util.setXY(gameStatusTextView, (displayMetrics.widthPixels - gameStatusTextView.getMeasuredWidth())/2 , 0);
// Двигаем поле
                    Util.setXY(boardView, 0, 0.5f * newGameButton.getHeight());
                }
// Поймали исключение
                catch (Exception ex) {
                    // Показываем сообщение об ошибке с текстом исключения
                    Toast.makeText(MainActivity.Instance, "" + "ошибка в обновлении интерфейса: " + ex.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    // Начинаем новую игру
    public void newGameButtonClick(View v) {
// Ловим исключение
        try {
            // Начинаем новую игру
            this.boardView.newGame();
        }
// Поймали исключение
        catch (Exception ex) {
            // Показываем сообщение об ошибке с текстом исключения
            Toast.makeText(this, "Случилась ошибка в начинаем новую игру: " + ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // Отменяем ход
    public void moveBackButtonClick(View v) {
// Ловим исключение
        try {
            // Отменяем ход
            this.boardView.moveBack();
        }
// Поймали исключение
        catch (Exception ex) {
            // Показываем сообщение об ошибке с текстом исключения
            Toast.makeText(this, "Случилась ошибка в отмене хода: " + ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // Требуется для интерфейса ттс
    @Override
    public void onInit(int status) {
// Ничего не делаем, надо оставить упстым
    }

    // Инициализируем tts движок
    private void initTtsEngine() {
        // Имя класса tts
        String className = "com.google.android.tts";
        // Устанавливаем ттс по умолчанию
        Util.Tts = new TextToSpeech(this, this, className);
    }
    public int getLevel(){
        if(firstLevelOn){
            return 1;
        }
        else return 2;
    }
    public void saveRating(){
        // Запускаем обновление интерфейса в основном потоке приложения
        MainActivity.Instance.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //Ловим исключение
                try {
                    // если нет победителя и не ничья то выходим
                    if(boardView.getWinner()==0 & boardView.moves.size()+boardView.islands.size()!= CELL_COUNT*CELL_COUNT){return;}
                    // экземпляр календаря
                    c = Calendar.getInstance();
                    // получаем sharedPreferences
                    sharedPreferences = getPreferences(MODE_PRIVATE);
                    SharedPreferences.Editor ed = sharedPreferences.edit();
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    // получаем текущую дату и время
                    String formattedDate = df.format(c.getTime());
                    // если ничья
                    if (boardView.getWinner()==0){
                        // записываем время и результат игры
                        ed.putString("    " + formattedDate,  "Ничья");
                    }
                    // если есть победитель
                    else {ed.putString("    " + formattedDate,  ((boardView.getWinner() == 1) ? getResources().getString(R.string.White) : getResources().getString(R.string.Black)));}
                    ed.commit();
                }
                catch (Exception ex) {
                    // Показываем сообщение об ошибке с текстом исключения
                    Toast.makeText(getApplicationContext(), "Случилась ошибка в обновлении интерфейса: " + ex.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
    public String system() {
        // получаем sharedPreferences
        sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        Map savedRating = MainActivity.sharedPreferences.getAll();
        // преобразовываем в строку
        String str = savedRating.toString();
        // если есть фигурные скобки в строке
        if (str.contains("{")) {
            // заменяем фигурные скобки на пустую строку
            str = str.replace("{", "");
        }
        // если есть фигурные скобки в строке
        if (str.contains("}")) {
            // заменяем фигурные скобки на пустую строку
            str = str.replace("}", "");
        }
        // если есть равно скобки в строке
        while (str.contains("=")) {
            // заменяем равно на пустую строку
            str = str.replace("=", "  ");
        }
        //  while (str.contains(",")) {
        //     str = str.replace(",", "");
        // }
        // делим строку по символу запятой
        String[] lines = str.split(",");
        String s;
        // результирующая строка, которую будем возвращать
        String finalString = "";
        // если строка существует и рейтинг ненулевого размера
        if (lines != null & savedRating.size() != 0) {
            // получаем массив дат и времени
            long[] dateTimes = new long[lines.length];
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                line = line.replace("-", "").replace("-", "");
                line = line.replace(" ", "");
                line = line.replace(":", "").replace(":", "");
                line = line.substring(0, 14);
                long num = Long.parseLong(line);
                dateTimes[i] = num;
            }
            Arrays.sort(dateTimes);
            long TempArr[] = new long[dateTimes.length];
            TempArr = dateTimes.clone();
            for (int i = dateTimes.length - 1; i >= 0; i--) {
                dateTimes[dateTimes.length - 1 - i] = TempArr[i];
            }

            for (int i = 0; i < lines.length; i++) {
                for (int k = 0; k < lines.length; k++) {
                    String line = lines[k];
                    line = line.replace("-", "").replace("-", "");
                    line = line.replace(" ", "");
                    line = line.replace(":", "").replace(":", "");
                    line = line.substring(0, 14);
                    long num = Long.parseLong(line);
                    if (num == dateTimes[i]) {
                        finalString = finalString + lines[k] + "\n";
                    }
                }
            }
        }
        return finalString;
    }
}
