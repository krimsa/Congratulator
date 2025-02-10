package org.example;

import java.io.IOException;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        DatabaseManager dbManager = new DatabaseManager();
        Menu menu = new Menu(dbManager);

        try {
            dbManager.connect();
            menu.showTodayAndUpcomingBirthdays();  // По умолчанию выводим ближайшие ДР
            menu.showMainMenu();
            dbManager.close();
        } catch (SQLException | IOException e) {
            System.err.println("Ошибка: " + e.getMessage());
        }
    }
}