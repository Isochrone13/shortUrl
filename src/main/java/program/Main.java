package program;

import java.io.File;
import java.util.Scanner;

public class Main {
    private static final String USERS_FILE_PATH = "users.txt"; // файл со списком пользователей
    private static final String CONFIG_FILE_PATH = "config.txt"; // файл с настройками
    private static final String LINKS_FOLDER_PATH = "links";  // папка для хранения ссылок

    public static void main(String[] args) {
        // Создадим папку для ссылок, если не существует
        File linksFolder = new File(LINKS_FOLDER_PATH);
        if (!linksFolder.exists()) {
            linksFolder.mkdirs();
        }

        // Инициализация сервисов
        ConfigService configService = new ConfigService(CONFIG_FILE_PATH);
        UserService userService = new UserService(USERS_FILE_PATH);
        LinkService linkService = new LinkService(LINKS_FOLDER_PATH);

        // Инициализация контроллеров
        AuthController authController = new AuthController(userService);
        LinkController linkController = new LinkController(linkService, configService, authController);

        // Инициализируем объект Scanner для чтения ввода пользователя из консоли
        Scanner scanner = new Scanner(System.in);

        while (true) {
            // Перед каждым действием удаляем устаревшие ссылки
            linkService.cleanupExpiredLinks();

            // Выводим меню
            System.out.println("\nГлавное меню:");
            System.out.println("1. Создать пользователя");
            System.out.println("2. Авторизоваться");
            System.out.println("3. Создать короткую ссылку");
            System.out.println("4. Список моих ссылок");
            System.out.println("5. Перейти по ссылке");
            System.out.println("6. Редактировать лимит переходов");
            System.out.println("7. Удалить ссылку");
            System.out.println("8. Выйти из аккаунта");
            System.out.println("0. Выйти");
            System.out.print("Выберите пункт меню: ");

            // Запрашиваем выбор пункта меню
            String choice = scanner.nextLine();

            // Вызываем метод для выбранного пункта меню
            switch (choice) {
                case "1":
                    authController.registerUser();
                    break;
                case "2":
                    authController.loginUser();
                    break;
                case "3":
                    linkController.createShortLink(authController.getCurrentUser());
                    break;
                case "4":
                    linkController.listUserLinks(authController.getCurrentUser());
                    break;
                case "5":
                    linkController.goToLink();
                    break;
                case "6":
                    linkController.editLinkLimit(authController.getCurrentUser());
                    break;
                case "7":
                    linkController.deleteLink(authController.getCurrentUser());
                    break;
                case "8":
                    authController.logoutUser();
                    break;
                case "0":
                    System.out.println("Выход из программы...");
                    System.exit(0);
                    break;
                default:
                    System.out.println("Неверный пункт меню, повторите попытку.");
            }
        }
    }
}