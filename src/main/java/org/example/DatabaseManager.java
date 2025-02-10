package org.example;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {
    private Connection connection;

    // Подключение к базе данных
    public void connect() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:birthday.db");
        } catch (ClassNotFoundException e) {
            System.out.println("JDBC driver not found.");
            throw new RuntimeException(e);
        }

        if (connection != null) {
            createTableIfNotExists();
        }
    }

    // Создание таблицы, если она еще не существует
    private void createTableIfNotExists() throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeUpdate(
                "CREATE TABLE IF NOT EXISTS birthdays (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "name TEXT NOT NULL," +
                        "birthday DATE NOT NULL);"
        );
    }

    // Закрытие соединения с базой данных
    public void close() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }

    // Получение всех записей из базы данных
    public List<Person> getAllBirthdays() throws SQLException {
        List<Person> persons = new ArrayList<>();
        PreparedStatement stmt = connection.prepareStatement("SELECT * FROM birthdays");
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            int id = rs.getInt("id");
            String name = rs.getString("name");
            LocalDate birthday = LocalDate.parse(rs.getString("birthday"));

            persons.add(new Person(id, name, birthday));
        }

        return persons;
    }

    // Получение сегодняшних и ближайших дней рождения
    public List<Person> getTodayAndUpcomingBirthdays() throws SQLException {
        List<Person> upcomingPersons = new ArrayList<>();
        // Получаем текущую дату
        LocalDate today = LocalDate.now();
        // Преобразуем текущую дату в формат "ММ-ДД"
        String currentDay = today.format(DateTimeFormatter.ofPattern("MM-dd"));
        // Определяем следующий цикл через 365 дней
        LocalDate next31Days = today.plusDays(31);
        String next31DaysF = next31Days.format(DateTimeFormatter.ofPattern("MM-dd"));

        PreparedStatement stmt = connection.prepareStatement(
                "SELECT * FROM birthdays " +
                        "WHERE STRFTIME('%m-%d', birthday) BETWEEN ? AND ? " +
                        "ORDER BY STRFTIME('%m-%d', birthday) ASC"
        );
        stmt.setString(1, currentDay);
        stmt.setString(2, next31DaysF);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            int id = rs.getInt("id");
            String name = rs.getString("name");
            LocalDate birthday = LocalDate.parse(rs.getString("birthday"),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            upcomingPersons.add(new Person(id, name, birthday));
        }
        return upcomingPersons;
    }

    // Добавление новой записи в базу данных
    public void addBirthday(Person person) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO birthdays (name, birthday) VALUES (?, ?)"
        );
        stmt.setString(1, person.getName());
        stmt.setObject(2, person.getBirthday().toString());

        stmt.executeUpdate();
    }

    // Обновление существующей записи
    public void updateBirthday(Person person) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement(
                "UPDATE birthdays SET name = ?, birthday = ? WHERE id = ?"
        );
        stmt.setString(1, person.getName());
        stmt.setObject(2, person.getBirthday().toString());
        stmt.setInt(3, person.getId());

        stmt.executeUpdate();
    }

    // Удаление записи из базы данных
    public void deleteBirthday(int id) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement("DELETE FROM birthdays WHERE id = ?");
        stmt.setInt(1, id);

        stmt.executeUpdate();
    }
}
