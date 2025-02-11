package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class Menu {
    private final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    private final DatabaseManager dbManager;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public Menu(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    // Метод для отображения главного меню
    public void showMainMenu() throws IOException, SQLException {
        boolean running = true;

        while (running) {
            System.out.println("\nМеню:");
            System.out.println("1. Показать все дни рождения");
            System.out.println("2. Показать сегодняшние и ближайшие дни рождения");
            System.out.println("3. Добавить день рождения");
            System.out.println("4. Изменить день рождения");
            System.out.println("5. Удалить день рождения");
            System.out.println("6. Выход");

            System.out.print("Выберите пункт: ");
            String choice = reader.readLine();

            switch (choice) {
                case "1":
                    showAllBirthdays();
                    break;
                case "2":
                    showTodayAndUpcomingBirthdays();
                    break;
                case "3":
                    addNewBirthday();
                    break;
                case "4":
                    editBirthday();
                    break;
                case "5":
                    deleteBirthday();
                    break;
                case "6":
                    running = false;
                    break;
                default:
                    System.out.println("Неверный выбор. Попробуйте снова.");
            }
        }
    }

    // Метод для показа всех дней рождения
    private void showAllBirthdays() throws SQLException {
        List<Person> allBirthdays = dbManager.getAllBirthdays();

        if (allBirthdays.isEmpty()) {
            System.out.println("Список пуст.");
        } else {
            for (Person p : allBirthdays) {
                System.out.printf("%d. %s - %s\n", p.getId(), p.getName(),
                        p.getBirthday().format(FORMATTER));
            }
        }
    }

    // Метод для показа сегодняшних и ближайших дней рождения
    void showTodayAndUpcomingBirthdays() throws SQLException {
        List<Person> upcomingBirthdays = dbManager.getTodayAndUpcomingBirthdays();

        if (upcomingBirthdays.isEmpty()) {
            System.out.println("Нет предстоящих дней рождения.");
        } else {
            for (Person p : upcomingBirthdays) {
                System.out.printf("%d. %s - %s\n", p.getId(), p.getName(),
                        p.getBirthday().format(FORMATTER));
            }
        }
    }

    // Метод для добавления нового дня рождения
    private void addNewBirthday() throws IOException, SQLException {
        System.out.print("Имя: ");
        String name = reader.readLine();

        System.out.print("Дата рождения (ДД.ММ.ГГГГ): ");
        String dateStr = reader.readLine();

        LocalDate birthday = LocalDate.parse(dateStr, FORMATTER);

        Person person = new Person(-1, name, birthday);
        dbManager.addBirthday(person);

        System.out.println("День рождения добавлен.");
    }

    // Метод для редактирования существующего дня рождения
    private void editBirthday() throws IOException, SQLException {
        System.out.print("ID записи для изменения: ");
        int id = Integer.parseInt(reader.readLine());

        Person oldPerson = findById(id);
        if (oldPerson == null) {
            System.out.println("Запись с таким ID не найдена.");
            return;
        }

        System.out.print("Новое имя [" + oldPerson.getName() + "]: ");
        String newName = reader.readLine();
        if (!newName.isBlank()) {
            oldPerson.setName(newName);
        }

        System.out.print("Новая дата рождения [" + oldPerson.getBirthday().format(FORMATTER) + "]: ");
        String newDateStr = reader.readLine();
        if (!newDateStr.isBlank()) {
            LocalDate newDate = LocalDate.parse(newDateStr, FORMATTER);
            oldPerson.setBirthday(newDate);
        }

        dbManager.updateBirthday(oldPerson);

        System.out.println("Изменения сохранены.");
    }

    // Метод для удаления дня рождения
    private void deleteBirthday() throws IOException, SQLException {
        System.out.print("ID записи для удаления: ");
        int id = Integer.parseInt(reader.readLine());

        Person personToDelete = findById(id);
        if (personToDelete == null) {
            System.out.println("Запись с таким ID не найдена.");
            return;
        }

        dbManager.deleteBirthday(id);

        System.out.println("Запись удалена.");
    }

    // Вспомогательный метод для поиска записи в БД по ID
    private Person findById(int id) throws SQLException {
        List<Person> allBirthdays = dbManager.getAllBirthdays();

        for (Person p : allBirthdays) {
            if (p.getId() == id) {
                return p;
            }
        }

        return null;
    }
}