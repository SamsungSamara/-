package com.example.gamego;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.text.style.IconMarginSpan;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import androidx.annotation.Nullable;
import androidx.constraintlayout.solver.widgets.Rectangle;
import androidx.fragment.app.Fragment;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountedCompleter;

import static android.content.Context.MODE_PRIVATE;
import static android.content.Context.MODE_WORLD_READABLE;


// Рисуем поле и обрабатываем нажатия на экран
public class BoardView extends View implements Runnable {
    // Количество клеток по одной стороне доски, 10X10
    public static int CELL_COUNT = 10;
    // Список ходов игроков
    public ArrayList<Point> moves = new ArrayList<Point>();
    // Список ходов игроков
    public static boolean useIslands = true;
    public ArrayList<Point> islands = createIsland();
    public static BoardView Instance;
    // Время последнего касания
    private long lastTouchEventTime;
    // Игра против компьютера флаг
    private boolean isPlayerVsComputer = true;
    // Поток для хода компьютера
    private Thread computerMoveThread;
    // Генератор случайных чисел
    private static Random rand = new Random();

    // Конструктор
    public BoardView(Context context) {
        // Вызываем конструктор суперкласса
        super(context);
    }

    // Конструктор
    public BoardView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        // Вызываем конструктор суперкласса
        super(context, attrs, defStyle);

    }

    // Конструктор
    public BoardView(Context context, @Nullable AttributeSet attrs) {
        // Вызываем конструктор суперкласса
        super(context, attrs);

    }

    // Нажали на экран
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {

// ловим исключение
        try {
            // Синхронизируем
            synchronized (this.moves) {
                // Если есть поток вычисления хода компьютера
                if (this.computerMoveThread != null) {
                    // Выходим
                    return true;
                }
                // Время в миллисекундах
                long timeMs = System.currentTimeMillis();
// Если прошло меньше 1000 миллисекунды
                if (timeMs - this.lastTouchEventTime < 1000) {
                    // Выходим
                    return true;
                }
// Запоминаем время касания
                this.lastTouchEventTime = timeMs;
// Если коснулись экрана
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    // Точка касания на экране x и y
                    float px = event.getX(), py = event.getY();
// Вызываем обработку нажатия
                    this.onTouch(px, py);
                }
            }
        }
        // поймали исключение
        catch (Exception ex) {
            // Показываем сообщение об ошибке с текстом исключения
            Toast.makeText(getContext(), "Случилась ошибка в обработке нажатия " + ex.getMessage(), Toast.LENGTH_LONG).show();
        }
        // Возвращаем true
        return true;
    }

    // Обрабатываем нажатие на экран
    private void onTouch(float px, float py) {
// Ширина и высота экрана
        int w = this.getWidth(), h = this.getHeight();
        // Добавим нажали в отладочную информацию
        // Util.debug("нажали");
// Размер квадратной доски с полями
        float boardSize = 0.95f * Math.min(w, h);
// Размер клетки
        float cellSize = boardSize / CELL_COUNT;
        // Координата X левого верхнего угла
        float offsetX = (w - boardSize) / 2;
// Координата Y левого верхнего угла
        // float offsetY = MainActivity.USE_OLD_DESIGN ? (h - boardSize) / 2 : 0;
        float offsetY =  (h - boardSize)/2;

// Если использовать старый дизайн
        if (MainActivity.USE_OLD_DESIGN) {
// Нажали в районе левой кнопки
            if (px >= 0 && px <= 2 * cellSize && py >= 0 && py <= 2 * cellSize) {
// Нажимаем на кнопку "ШАГ НАЗАД"
                moveBack();
                // Выходим из функции
                return;
            }
// Нажали в районе правой кнопки
            if (px <= w && px >= w - 2 * cellSize && py >= 0 && py <= 2 * cellSize) {
// Нажимаем на кнопку "НОВАЯ ИГРА"
                newGame();
                // Выходим из функции
                return;
            }
        }
        // Нажатая клетка x
        int touchX = (int) Math.floor((px - offsetX) / cellSize);
        // Нажатая клетка y
        int touchY = (int) Math.floor((py - offsetY) / cellSize);
// Если нажали за пределами поля или есть победитель
        if (touchX < 0 || touchX >= CELL_COUNT || touchY < 0 || touchY >= CELL_COUNT || getWinner() != 0) {
            // Добавим нажали за пределы поля в отладочную информацию
            //Util.debug("Нажали за пределами поля.");
// выходим
            return;
        }
// Ход
        Point move = getDebugMove(new Point(touchX, touchY));
        // Добавим номер клетки в отладочную информацию
        // Util.debug("Клетка " + move.x + " " + move.y);
// Если клетка касания не пустая
        if (!isFreeCell(move)) {
            // Добавим клетка не пустая в отладочную информацию
            //  Util.debug("Клетка не пустая");
            // Выходим
            return;
        }
// Если есть победитель
        if (getWinner() != 0) {
            // Добавим игра закончена в отладочную информацию
            //Util.debug("Игра уже закончена");
// Выходим
            return;
        }
// Добавим ход в список
        this.moves.add(move);
        // обновляем user interface (UI)
        this.updateUI();
        // Выведем результат игры
        showAlertDialog();
        MainActivity.Instance.saveRating();
        writeFile();
        // Если играем с компьютером и нет победителя
        if (this.isPlayerVsComputer && getWinner() == 0) {
            // Создаем поток
            this.computerMoveThread = new Thread(this);
            // Запускаем поток
            this.computerMoveThread.start();
        }
        else if(!this.isPlayerVsComputer) {
            // меняем координаты острова
            changeIsland();
        }

    }

    // Предопределенный ход игрока
    private Point getDebugMove(Point move) {
// Если в режиме отладки ходов
        if (false) {
// Первый ход
            if (moves.size() == 0) {
                return new Point(5, 5);
            }
// Второй ход
            if (moves.size() == 2) {
                return new Point(5, 6);
            }
// Третий ход
            if (moves.size() == 4) {
                return new Point(5, 7);
            }
// Четвертый ход
            if (moves.size() == 6) {
                return new Point(5, 8);
            }
        }
// Возвращаем ход
        return move;
    }

    // Клетка пустая флаг
    private boolean isFreeCell(Point pt) {
// Список не содержит клетки
        if(moves.contains(pt) ){
            return false;
        }
        if( islands!= null & islands.contains(pt)){
            return false;
        }
        return true;
    }

    // Возвращаем победителя или 0 если его нет
    public int getWinner() {
// Массив соответствующий клеткам доски
        int[][] grid = new int[CELL_COUNT][CELL_COUNT];
// Проходим по ходам
        for (int i = 0; i < this.moves.size(); i++) {
// Ход игрока, координаты клетки, где Х и Y от 0-9
            Point move = this.moves.get(i);
// Ставим номер игрока
            grid[move.x][move.y] = (i % 2) == 0 ? 1 : 2;
// Проверяем пять ячеек
            int k = CELL_COUNT - 5;
// Проверяем клетки по горизонтали
            for (int y = 0; y < CELL_COUNT; y++) {
                for (int x = 0; x <= k; x++) {
// Номер игрока
                    int c = grid[x][y];
                    // Если пять в ряд
                    if (c > 0 && grid[x + 1][y] == c && grid[x + 2][y] == c && grid[x + 3][y] == c && grid[x + 4][y] == c) {
                        // Возвращаем номер игрока
                        return c;
                    }
                }
            }
// Проверяем по вертикали
            for (int x = 0; x < CELL_COUNT; x++) {
                for (int y = 0; y <= k; y++) {
// Номер игрока
                    int c = grid[x][y];
                    // Если пять в ряд
                    if (c > 0 && grid[x][y + 1] == c && grid[x][y + 2] == c && grid[x][y + 3] == c && grid[x][y + 4] == c) {
                        // Возвращаем номер игрока
                        return c;
                    }
                }
            }
// Диагональ вправо вниз
            for (int x = 0; x <= k; x++) {
                for (int y = 0; y <= k; y++) {
// Номер игрока
                    int c = grid[x][y];
                    // Если пять в ряд
                    if (c > 0 && grid[x + 1][y + 1] == c && grid[x + 2][y + 2] == c && grid[x + 3][y + 3] == c && grid[x + 4][y + 4] == c) {
                        // Возвращаем номер игрока
                        return c;
                    }
                }
            }
// Диагональ вправо вверх
            for (int x = 0; x <= k; x++) {
                for (int y = 4; y < CELL_COUNT; y++) {
// Номер игрока
                    int c = grid[x][y];
                    // Если пять в ряд
                    if (c > 0 && grid[x + 1][y - 1] == c && grid[x + 2][y - 2] == c && grid[x + 3][y - 3] == c && grid[x + 4][y - 4] == c) {
                        // Возвращаем номер игрока
                        return c;
                    }
                }
            }
        }
        // Возвращаем 0, нет победителя
        return 0;
    }

    // Перерисовка
    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
// ловим исключение
        try {
// Вызываем метод onDraw() из суперкласса
            super.onDraw(canvas);
// Заливаем  полотно
            Drawable d = getResources().getDrawable(R.drawable.blue_gradient, null);
            d.setBounds(0, 0, this.getWidth(), this.getHeight());
            d.draw(canvas);
            // canvas.drawColor(getResources().getColor(R.color.background_color));
// Ширина и высота экрана
            int w = this.getWidth(), h = this.getHeight();
// Размер квадратной доски с клетками
            float boardSize = 0.95f * Math.min(w, h);
// Размер клетки
            float cellSize = boardSize / CELL_COUNT;
// Радиус фишки
            float r = 0.45f * cellSize;
// Полклетки
            float cellSize2 = cellSize / 2;
// Координата X левого верхнего угла
            float offsetX = (w - boardSize) / 2;
// Координата Y левого верхнего угла
            //  float offsetY = MainActivity.USE_OLD_DESIGN ? (h - boardSize) / 2 : 0;
            float offsetY =  (h - boardSize) / 2;
            // Цвет для острова
            Paint islandPaint = new Paint();
// Цвет для подсветки клетки
            Paint highlightPaint = new Paint();
            // Цвет для сетки
            Paint gridPaint = new Paint();
// Базовый цвет - сетки
            Paint shadowPaint = new Paint(0);
// Цвет первого игрока (белый)
            Paint paint1 = new Paint(0);
// Цвет второго игрока (черный)
            Paint paint2 = new Paint(0);
            gridPaint.setColor(getResources().getColor(R.color.grid_paint));
            // Ставим цвет островков
            islandPaint.setColor(getResources().getColor(R.color.island_paint));
// Ставим цвет подсветки темный бежевый
            highlightPaint.setColor(getResources().getColor(R.color.highlight_color));
// Ставим базовый цвет черный
            shadowPaint.setColor(getResources().getColor(R.color.shadow_color));
// Ставим цвет первого игрока
            paint1.setColor(getResources().getColor(R.color.player1_color));
// Ставим цвет второго игрока
            paint2.setColor(getResources().getColor(R.color.player2_color));
// Ставим стиль paint (заполняем фигуры)
            shadowPaint.setStyle(Paint.Style.STROKE);
            gridPaint.setStyle(Paint.Style.STROKE);
            // Ставим стиль paint для островков
            islandPaint.setStyle(Paint.Style.FILL);
// Поставим ширину кисти
            gridPaint.setStrokeWidth(4f);
            shadowPaint.setStrokeWidth(5f);
            paint2.setAntiAlias(true);
            paint2.setTextSize(55.0f);
// Если использовать старый дизайн
            if (MainActivity.USE_OLD_DESIGN) {
// Если есть ходы
                if (this.moves.size() != 0) {
                    // Картинка вернуться на ход назад
                    Bitmap returnBack = BitmapFactory.decodeResource(getResources(), R.drawable.return_back);
                    // Придаем картинке "ШАГ НАЗАД" нормальный размер и рисуем
                    canvas.drawBitmap(Bitmap.createScaledBitmap(returnBack, (int) (2 * cellSize), (int) (2 * cellSize), true), 0, 0, paint2);
                }
                // Рисуем текст
                canvas.drawText(getGameStatus(), (int) (2 * cellSize), (int) (2 * cellSize) - 55, paint2);
                // Картинка новая игра
                Bitmap newGame = BitmapFactory.decodeResource(getResources(), R.drawable.new_game);
                // Придаем картинке "НОВАЯ ИГРА" нормальный размер и рисуем
                canvas.drawBitmap(Bitmap.createScaledBitmap(newGame, (int) (2 * cellSize), (int) (2 * cellSize), true), w - 2 * cellSize, 0, paint2);
            }
            //shadowPaint.setMaskFilter(new BlurMaskFilter(8, BlurMaskFilter.Blur.INNER));

            // Рисуем рамку вокруг клеток
            canvas.drawRect(offsetX, offsetY, offsetX + boardSize, offsetY + boardSize, gridPaint);
// Проходим по линиям сетки
            for (int k = 0; k < CELL_COUNT; k++) {
// рисуем вертикальные линии сетки
                canvas.drawLine(offsetX + k * cellSize + cellSize2, offsetY, offsetX + k * cellSize + cellSize2, offsetY + boardSize, gridPaint);
// Рисуем горизонтальные линии
                canvas.drawLine(offsetX, offsetY + k * cellSize + cellSize2, offsetX + boardSize, offsetY + k * cellSize + cellSize2, gridPaint);
            }

            if (islands !=null & useIslands) {
                // Проходим по островкам
                for (int i = 0; i < this.islands.size(); i++) {
//  координаты клетки
                    Point move = this.islands.get(i);
// Экранная координата X центра шарика
                    float ballCenterX = offsetX + cellSize * islands.get(i).x + cellSize / 2;
// Экранная координата Y центра шарика
                    float ballCenterY = offsetY + cellSize * islands.get(i).y + cellSize / 2;
// Рисуем шарик
                    canvas.drawCircle(ballCenterX, ballCenterY, r, islandPaint);

                }
            }
// Проходим по ходам
            for (int i = 0; i < this.moves.size(); i++) {
// Ход игрока, координаты клетки, где Х и Y от 0-9
                Point move = this.moves.get(i);
// Экранная координата X центра шарика
                float ballCenterX = offsetX + cellSize * move.x + cellSize / 2;
// Экранная координата Y центра шарика
                float ballCenterY = offsetY + cellSize * move.y + cellSize / 2;
// Рисуем шарик
                canvas.drawCircle(ballCenterX, ballCenterY, r, (i % 2) == 0 ? paint1 : paint2);
            }
            // Если есть ходы
            if (this.moves.size() > 0) {
                // Последний ход
                Point lastMove = this.moves.get(this.moves.size() - 1);
                // Центр клетки x
                float centerX = offsetX + lastMove.x * cellSize + cellSize2;
                // Центр клетки y
                float centerY = offsetY + lastMove.y * cellSize + cellSize2;
                // Подсвечиваем последний ход
                canvas.drawCircle(centerX, centerY, r * 0.5f, highlightPaint);
            }
        }
        // поймали исключение
        catch (Exception ex) {
            // Показываем сообщение об ошибке с текстом исключения
            Toast.makeText(getContext(), "Случилась ошибка в отрисовке " + ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // Отменяем последний ход
    public void moveBack() {
        // Если ходов нет
        if (this.moves.size() == 0) {
            // Выходим
            return;
        }
        // Добавим отменяем ход в отладочную информацию
        Util.debug("Отменяем ход");
        // Если играем против компьютера
        if (this.isPlayerVsComputer) {
            // Если есть как минимум два хода
            if (this.moves.size() >= 2) {
                // Удаляем последний ход из списка
                this.moves.remove(this.moves.size() - 1);
                //Удаляем из списка еще один ход
                this.moves.remove(this.moves.size() - 1);
            }
        }
        // Иначе
        else {
            // Если как минимум один ход
            if (this.moves.size() >= 1) {
                // удаляем один ход
                this.moves.remove(this.moves.size() - 1);
            }
        }
        // обновляем user interface (UI)
        this.updateUI();
    }
    // Создаем островки
    public ArrayList createIsland(){
        if (!useIslands & islands!=null){
            islands.clear();
            return new ArrayList();
        }
        // Создаем коллекцию островков
        islands = new ArrayList<>();
        // Получаем координаты пяти островков
        for(int i = 0; i<5; i++) {
            // Получаем рандомное число от 0 - 9 для x-координаты
            int x = rand.nextInt(CELL_COUNT);
            // Получаем рандомное число от 0 - 9 для y-координаты
            int y = rand.nextInt(CELL_COUNT);
            // Добавляем координаты в коллекцию
            islands.add(new Point(x,y));
        }
// вернем коллекцию из координат островков
        return islands;
    }
    // Меняем координаты островков
    public void changeIsland(){
        if(!useIslands){
            return;
        }
        if(moves.size() % 4 != 0){
            return;
        }
        // 50 раз пытаемся добавить случайную координату
        for(int i = 0; i < 500 ; i++) {
            // Получаем случайную x-координату
            int x = rand.nextInt(CELL_COUNT);
            // Получаем случайную y-координату
            int y = rand.nextInt(CELL_COUNT);
            // Если клетка пустая
            if (isFreeCell(new Point(x, y))) {
                // Добавляем случайную координату
                islands.add(new Point(x, y));
                // Удаляем первый объект коллекции
                islands.remove(0);
                // Прерываем цикл
                break;
            }
        }
    }
    // Новая игра
    public void newGame() {
        int idx = rand.nextInt(MainActivity.NewGameWords.length);
        // Добавим начинаем новую игру в отладочную информацию
        Util.debug(MainActivity.NewGameWords[idx]);
        // Создаем островки
        createIsland();
        // Если есть ходы
        if (this.moves.size() > 0) {
            // Очищаем список ходов
            this.moves.clear();
        }
        // Иначе ходов нет
        else {
            // Переворачиваем значение флага Игра против компьютера
            this.isPlayerVsComputer = !this.isPlayerVsComputer;
        }
        // обновляем user interface (UI)
        this.updateUI();
// Показываем сообщение игрок против компьютера или игрок против другого игрока
        Toast.makeText(getContext(), this.isPlayerVsComputer ? getResources().getString(R.string.PlayerVsComputer) : getResources().getString(R.string.PlayerVsPlayer), Toast.LENGTH_LONG).show();
    }

    // обновляем user interface (UI)
    private void updateUI() {
// Перерисовываем поле
        invalidate();
// обновляем текст статуса игры и кнопку отмена
        MainActivity.Instance.updateUI(getGameStatus(), this.moves.size() > 0);
    }

    // Возвращаем текстовый статус игры
    private String getGameStatus() {
        // победитель
        int winner = getWinner();
        // Если никто еще не выиграл
        if (winner == 0) {
            // Если заполнили все клетки поля
            if (this.moves.size() + this.islands.size() == CELL_COUNT * CELL_COUNT) {
                String draw = getResources().getString(R.string.Draw);
                Util.debug(draw);
                // Возвращаем ничья
                return draw;
            }
            // Если игрок против компьютера
            if (this.isPlayerVsComputer) {
                // Возвращаем ваш ход или компьютер думает
                return this.moves.size() % 2 == 0 ? getResources().getString(R.string.YourTurn) : getResources().getString(R.string.ComputerThinks);
            }
            // Иначе игрок против игрока
            else {
                // Возвращаем ход 1 игрока или ход 2 игрока
                return this.moves.size() % 2 == 0 ? getResources().getString(R.string.TurnPlayer1) : getResources().getString(R.string.TurnPlayer2);
            }
        }
        // Если игрок против компьютера
        if (this.isPlayerVsComputer) {
            if (winner == 1){
                return getResources().getString(R.string.YouWon);
            }
            else{
                return getResources().getString(R.string.YouLost);
            }
        }
        // Иначе игрок против игрока
        else {
            if (winner == 1){
                return getResources().getString(R.string.WinPlayer1);
            }
            else{
                return getResources().getString(R.string.WinPlayer2);
            }
        }
    }
    // Метод для  потока вычисления хода компьютера
    @Override
    public void run() {
// ловим исключение
        try {
            // Время в миллисекундах
            long timeMs = System.currentTimeMillis();
            // Делаем ход компьютера
            Point move = new ComputerMove(this.moves, this.islands).GetBestMove();
            // добавляем ход компьюетра в список ходов
            this.moves.add(move);
            // меняем координаты острова
            changeIsland();
            // Сколько компьютер думал в миллисекундах
            timeMs = System.currentTimeMillis() - timeMs;
// Если думали меньше 1 секунды то приостанавливаем поток на 1 секунду
            if (timeMs < 1000) {
                Thread.sleep(1000 - timeMs);
            }
            // обновляем user interface (UI)
            this.updateUI();
            // показываем диалоговое окно
            showAlertDialog();
            // Сохраняем рейтинг
            MainActivity.Instance.saveRating();
        }
// Поймали исключение, ничего не делаем
        catch (InterruptedException e) {
        }
// Завершаем ловлю исключения
        finally {
            // Удаляем ссылку на поток обдумывания хода компьютером
            this.computerMoveThread = null;
        }
    }
    public void setCellCount(int size){
        CELL_COUNT = size;
        moves.add(new Point(0,0));
        newGame();
    }
    // Вкл-выкл островки
    public void islandsEnable(boolean enabled){
        useIslands = enabled;
        // если в аргументах true
        if (enabled){
            createIsland();
        }
        // в аргументах false
        else {
            if (islands!=null) {
                // Очищаем массив островов
                islands.clear();
            }
        }
        // перерисовываем
        invalidate();
    }
    // Сколько клеток
    public int getCellCount(){
        return CELL_COUNT;
    }
    // оно почему то всегда возращает true
    public boolean areIslandsEnabled(){
        islands.size();
        return (useIslands);
    }
    public void showAlertDialog(){
        // Запускаем обновление интерфейса в основном потоке приложения
        MainActivity.Instance.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //Ловим исключение
                try {
                    showAlert2();
                }
                catch (Exception ex) {
                    // Показываем сообщение об ошибке с текстом исключения
                    Toast.makeText(MainActivity.Instance, "Случилась ошибка в обновлении интерфейса: " + ex.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
    // Показываем диалоговое окно с результатами игры
    public void showAlert2() {
// Создаем alertDialog
        String title = getResources().getString(R.string.GameResult);
        int winner = getWinner();
        String message = getGameStatus();
        if (winner == 0 & moves.size() + islands.size() != CELL_COUNT * CELL_COUNT) {
            return;
        }
        int wi = rand.nextInt(MainActivity.WinArray.length);
        int li = rand.nextInt(MainActivity.LoseArray.length);
        // Если игрок против компьютера
        if (this.isPlayerVsComputer) {
            if (winner == 1) {
                Util.debug(MainActivity.WinArray[wi]);
            } else {
                Util.debug(MainActivity.LoseArray[li]);
            }
        }
        // Иначе игрок против игрока
        else {
            if (winner == 1) {
                Util.debug(MainActivity.WinArray[wi]);
            } else {
                Util.debug(MainActivity.LoseArray[li]);
            }
        }
        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.Instance);
        // Не ставим заголовок если он равен нулю иначе поставим
        if (title != null) {
            alert.setTitle(title);
        }
        // ВЫводим сообщение
        alert.setMessage(message);
        // Рисуем кнопку ок
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        // создаем окно
        alert.create().show();
    }


    public static void writeFile(){
        try {
            FileOutputStream fos = MainActivity.Instance.openFileOutput("sample.txt", MainActivity.Instance.MODE_PRIVATE);
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            // записываем строку в файл
            osw.write(MainActivity.Instance.getLevel()  + (useIslands ?"1":"0") + CELL_COUNT + (Util.useVoiceForDebug? "1":"0"));
            osw.flush();
            osw.close();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static String readFile(){
        try {
            FileInputStream fIn = MainActivity.Instance.openFileInput("sample.txt");
            InputStreamReader isr = new InputStreamReader(fIn);
            String s = MainActivity.Instance.getLevel() + (useIslands ? "1" : "0") + CELL_COUNT + (Util.useVoiceForDebug? "1":"0");
            char[] inputBuffer = new char[s.length()];
            isr.read(inputBuffer);
            String readString = new String(inputBuffer);
            fIn.close();
            isr.close();
            return readString;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
