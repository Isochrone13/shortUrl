package program;

import java.io.*;
import java.util.*;

// Сервис для работы с пользователями: чтение/запись в файл, поиск и т.п.
public class UserService {
    // Объявляем типы полей класса:
    private final String filePath; // путь к файлу c данными пользователей (users.txt)
    private final List<User> users; // список пользователей, загруженных из файла для быстрого доступа

    /*
    Объявляем конструктор класса, который инициализирует путь к файлу,
    создаёт пустой список пользователей и загружает существующих пользователей из файла.
    */
    public UserService(String filePath) {
        this.filePath = filePath;
        this.users = new ArrayList<>();
        loadUsersFromFile();
    }

    /*
    Метод ищет пользователя в списке users по заданному логину, игнорируя регистр символов.
    Если пользователь найден, возвращает объект User, иначе — null.
    */
    public User findByLogin(String login) {
        for (User user : users) {
            if (user.getLogin().equalsIgnoreCase(login)) {
                return user;
            }
        }
        return null;
    }

    /*
    Метод ищет пользователя в списке users по заданному UUID.
    Если пользователь найден, возвращает объект User, иначе — null.
    */
    public User findByUuid(String uuid) {
        for (User user : users) {
            if (user.getUuid().equals(uuid)) {
                return user;
            }
        }
        return null;
    }

    /*
    Метод добавляет нового пользователя в список users и сохраняет обновлённый список в файл.
     */
    public void addUser(User user) {
        users.add(user);
        saveUsersToFile();
    }

    /*
    Метод загружает пользователей из файла, указанного в filePath
    */

    private void loadUsersFromFile() {
        File file = new File(filePath);
        // Проверяем, существует ли файл
        if (!file.exists()) {
            return;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            /*
            Читаем файл построчно в цикле
            Разделяем каждую строку по символу ";", ожидая три части: uuid, login, password.
            Если количество частей равно 3, создаём новый объект User и добавляем его в список.
             */
            while ((line = br.readLine()) != null) {
                String[] arr = line.split(";");
                if (arr.length == 3) {
                    String uuid = arr[0];
                    String login = arr[1];
                    String password = arr[2];
                    users.add(new User(uuid, login, password));
                }
            }
        } catch (IOException e) {
            System.out.println("Ошибка чтения файла users.txt: " + e.getMessage());
        }
    }

    /*
    Метод сохраняет текущий список пользователей из памяти в файл, указанный в filePath.
     */
    private void saveUsersToFile() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath, false))) {
            for (User user : users) {
                bw.write(user.getUuid() + ";" + user.getLogin() + ";" + user.getPassword());
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("Ошибка записи файла users.txt: " + e.getMessage());
        }
    }
}