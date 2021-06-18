package com.example.gamego;

import android.graphics.Point;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// Вычисляем ход компьютера
public class ComputerMove {
    // Генератор случайных чисел
    private static Random rand = new Random();
    // Массив соответствующий клеткам доски
    private int[][] grid;


    // Конструктор
    public ComputerMove(ArrayList<Point> moves, ArrayList<Point> islands) {
        // Массив соответствующий клеткам доски
        grid = new int[BoardView.CELL_COUNT][BoardView.CELL_COUNT];
// Проходим по ходам
        for (int i = 0; i < moves.size(); i++) {
// Ход игрока, координаты клетки, где Х и Y от 0-9
            Point move = moves.get(i);
// Ставим номер игрока
            grid[move.x][move.y] = (i % 2) == 0 ? 1 : 2;
        }
        // Проходим по островкам
        for (int i = 0; i < islands.size(); i++) {
// Ход игрока, координаты клетки, где Х и Y от 0-9
            Point move = islands.get(i);
// Ставим номер игрока
            grid[move.x][move.y] = 3;
        }
    }

    // Клетка пустая флаг
    public boolean isFreeCell(int x, int y) {
// Ячейка массива имеет значение 0, то есть нет фишки
        return grid[x][y] == 0;
    }

    // Возвращаем важность направления, массив из цвет, занято клеток, доступно всего
    private int getScoreDirection(int x, int y, int dx, int dy) {
        // Получаем информацию по направлению в одну сторону: цвет, занято, общая длина
        int[] score1 = getScoreDirectionHalf(x, y, dx, dy);
        // Первое направление цвет, количество занятых клеток, общее возможное количество
        int color1 = score1[0], busy1 = score1[1], total1 = score1[2];
// Получаем информацию по направлению в другую сторону: цвет, занято, общая длина
        int[] score2 = getScoreDirectionHalf(x, y, -dx, -dy);
        // Второе направление цвет, количество занятых клеток, общее возможное количество
        int color2 = score2[0], busy2 = score2[1], total2 = score2[2];
        // Если общая длина меньше 4
        if (total1 + total2 < 4) {
            // Возвращаем 0, так как 5 в ряд не выставить
            return 0;
        }
        // Если цвета одинаковые
        if (color1 == color2) {
// Возвращаем 0 если клетки не заняты или сумму занятых клеток
            return color1 == 0 ? 0 : (busy1 + busy2);
        }
        // Если одно из направлений свободно
        if (color1 == 0 || color2 == 0) {
            // Возвращаем сумму занятых
            return busy1 + busy2;
        }
        // Направления заняты разными цветами, возвращаем  максимум занятых клеток если там общее количество больше равно 4
        return Math.max(total1 >= 4 ? busy1 : 0, total2 >= 4 ? busy2 : 0);
    }

    // Возвращаем важность половины направления, число от 0 до 4
    private int[] getScoreDirectionHalf(int x, int y, int dx, int dy) {
        // Общая возможная длина фишек
        int total = 0;
// Сколько занято
        int busy = 0;
// Цвет фишек
        int color = 0;
        // Проходим в одну сторону
        for (int i = 1; i <= 4; i++) {
            // Складываем x
            x += dx;
// Если за пределами поля
            if (x < 0 || x >= BoardView.CELL_COUNT) {
                // Прерываем цикл
                break;
            }
// Складываем y
            y += dy;
            // Если за пределами поля
            if (y < 0 || y >= BoardView.CELL_COUNT) {
                // Прерываем цикл
                break;
            }
            // Номер игрока
            int p = grid[x][y];
            // Если островок
            if(p ==3){
                // Прерываем цикл
                break;
            }
            // Если клетка пустая
            if (p == 0) {
                // Увеличиваем общую возможную длину
                total++;
            }
            // Иначе не пустая клетка
            else {
// Если еще не было фишек
                if (color == 0) {
                    // Если были пустые клетки
                    if (total > 0) {
                        // Прерываем цикл
                        break;
                    }
                    // Увеличиваем общую возможную длину
                    total++;
                    // Запоминаем цвет
                    color = p;
                    // Ставим количество занятых клеток равным 1
                    busy = 1;
                }
                // Иначе фишка уже была
                else {
// Если фишка другого цвета
                    if (p != color) {
                        // Прерываем цикл
                        break;
                    }
                    // Увеличиваем общую возможную длину
                    total++;
                    // Увеличиваем количество занятых клеток
                    busy++;
                }
            }
        }
// Возвращаем информацию о направлении
        return new int[]{color, busy, total};
    }

    // Возвращаем важность клетки, число от 1 до нескольких тысяч.
    // Если нет соседей то вернет 4 (1*на 4 направления)
    // Если рядом одна чужая фишка то вернет 13 (10+3).
    // Если рядом две чужие фишки в ряд то вернет 103 (100+3)
    // Если рядом три и две в ряд то вернет 1102 (1000+100+1*2)
    private int
    getScore(int x, int y) {
        // Важность направления
        int score = 0;
        // Массив направлений по x
        int[] dx = {1, 0, 1, 1};
        // Массив направлений по y
        int[] dy = {0, 1, 1, -1};
        // Проходим по направлениям
        for (int i = 0; i < 4; i++) {
            // Важность направления
            int n = getScoreDirection(x, y, dx[i], dy[i]);
            // Если значение больше 4 то ставим 4
            n = (n > 4 ? 4 : n);
            // Добавим к важности степень десятки
            score += Math.pow(10, n);
        }
        // Возвращаем важность клетки
        return score;
    }

    // Вычисляем и возвращаем наилучший ход
    public Point GetBestMove() {
// Список возможных ходов
        List<Point> possibleMoves = new ArrayList<Point>();
        // Наилучшее значение важности хода
        int bestScore = 0;
// Сканируем клетки по x
        for (int x = 0; x < BoardView.CELL_COUNT; x++) {
// Сканируем клетки по y
            for (int y = 0; y < BoardView.CELL_COUNT; y++) {
// Если клетка пустая
                if (isFreeCell(x, y)) {
                    // Получаем важность клетки
                    int score = getScore(x, y);
                    // Если стоит первый уровень (легкий)
                    if (score > 150 & MainActivity.Instance.getLevel() == 1){
                        score = 150;
                    }
                    // Если важность еще лучше
                    if (score > bestScore ) {
                        // Запоминаем новую наилучшую важность клетки
                        bestScore = score;
                        // Очищаем список возможных ходов
                        possibleMoves.clear();
                    }
                    // Если важность равна наилучшей
                    if (bestScore == score) {
// Добавим клетку в список возможных ходов
                        possibleMoves.add(new Point(x, y));
                    }
                }
            }
        }
        // Добавим лучшую важность и количество таких ходов в отладочную информацию
        Util.debug("важность " + bestScore + " всего ходов " + possibleMoves.size());
        // Индекс случайного хода
        if (possibleMoves.size()>0) {
            int randomIndex = this.rand.nextInt(possibleMoves.size());
            //z int randomIndex = 0;
            // Случайный ход
            Point pmove = possibleMoves.get(randomIndex);
            // Добавим ход компьютера в отладочную информацию
            Util.debug("Ход компьютера " + pmove.x + " " + pmove.y);
            // Возвращаем ход
            return pmove;
        }
        else{return null;}
    }

}
