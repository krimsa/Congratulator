package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.time.MonthDay;
import java.util.Comparator;

public class Menu {
    private final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    private final DatabaseManager dbManager;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public Menu(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

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

    private void showAllBirthdays() throws SQLException {
        List<Person> allBirthdays = dbManager.getAllBirthdays();

        if (allBirthdays.isEmpty()) {
            System.out.println("Список пуст.");
            return;
        }

        // Сортировка по месяцу и дню (игнорируя год)
        allBirthdays.sort(Comparator.comparing(p -> MonthDay.from(p.getBirthday())));

        // Получаем текущий месяц и день для сравнения
        MonthDay currentMd = MonthDay.now();

        for (Person p : allBirthdays) {
            MonthDay personMd = MonthDay.from(p.getBirthday());
            String prefix;

            if (personMd.equals(currentMd)) {
                prefix = "] "; // Сегодняшний день рождения
            } else if (personMd.isBefore(currentMd)) {
                prefix = "- "; // День рождения уже прошел в этом году
            } else {
                prefix = "  "; // Предстоящий день рождения (пробелы для выравнивания)
            }

            System.out.printf("%d. %s%s - %s\n",
                    p.getId(),
                    prefix,
                    p.getName(),
                    p.getBirthday().format(FORMATTER));
        }
    }

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

    private void addNewBirthday() throws IOException, SQLException {
        System.out.print("Имя: ");
        String name = reader.readLine();

        boolean validInput = false;
        LocalDate birthday = null;

        do {
            try {
                System.out.print("Дата рождения (ДД.ММ.ГГГГ): ");
                String dateStr = reader.readLine();

                birthday = LocalDate.parse(dateStr, FORMATTER);
                validInput = true;
            } catch (DateTimeParseException e) {
                System.out.println("Некорректный формат даты. Пожалуйста, введите дату в формате ДД.ММ.ГГГГ.");
            }
        } while (!validInput);

        Person person = new Person(-1, name, birthday);
        dbManager.addBirthday(person);

        System.out.println("День рождения добавлен.");
    }

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

        boolean validInput = false;
        LocalDate newDate = null;

        do {
            try {
                System.out.print("Новая дата рождения [" + oldPerson.getBirthday().format(FORMATTER) + "]: ");
                String newDateStr = reader.readLine();

                newDate = LocalDate.parse(newDateStr, FORMATTER);
                validInput = true;
            } catch (DateTimeParseException e) {
                System.out.println("Некорректный формат даты. Пожалуйста, введите дату в формате ДД.ММ.ГГГГ.");
            }
        } while (!validInput);

        oldPerson.setBirthday(newDate);

        dbManager.updateBirthday(oldPerson);

        System.out.println("Изменения сохранены.");
    }

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