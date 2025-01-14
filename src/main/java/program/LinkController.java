package program;

import java.awt.Desktop; // используется для открытия URL в стандартном браузере пользователя.
import java.net.URI; // используется для работы с URL-адресами.
import java.util.Scanner;
import java.io.File;

public class LinkController {
    // Объявляем поля класса
    private final LinkService linkService; // сервис для управления короткими ссылками
    private final ConfigService configService; // сервис для доступа к параметрам
    private final Scanner scanner; // объект Scanner для чтения ввода пользователя из консоли
    private final AuthController authController;

    // Объявляем конструктор, который принимает объекты LinkService и ConfigService как параметры
    public LinkController(LinkService linkService,
                          ConfigService configService,
                          AuthController authController) {
        this.linkService = linkService;
        this.configService = configService;
        this.authController = authController;
        this.scanner = new Scanner(System.in);
    }

    /*
    Метод создания короткой ссылки (для авторизованного пользователя).
    */
    public void createShortLink(User currentUser) {
        // Если пользователь не авторизован — вместо вывода ошибки регистрируем и логиним
        if (currentUser == null) {
            System.out.println("Вы не авторизованы. Начнём регистрацию...");

            // Регистрируем пользователя
            authController.registerUser();

            // Обновляем currentUser
            currentUser = authController.getCurrentUser();
            if (currentUser == null) {
                System.out.println("Не удалось авторизоваться. Создание ссылки отменено.");
                return;
            }
        }

        // Считываем исходную ссылку
        System.out.println("Введите исходную ссылку (URL): ");
        String originalUrl = scanner.nextLine();

        System.out.println("Введите время жизни ссылки (в минутах): ");
        long linktl = Long.parseLong(scanner.nextLine());

        System.out.println("Введите лимит переходов: ");
        long linkmc = Long.parseLong(scanner.nextLine());

        Link link = linkService.createLink(
                currentUser.getUuid(),
                originalUrl,
                linktl,
                configService.getDefaultTlMinutes(),
                linkmc,
                configService.getDefaultMaxClicks()
        );

        String shortUrl = configService.getBaseUrl() + "/" + link.getShortCode();

        System.out.println("Ссылка создана!");
        System.out.println("Короткая ссылка: " + shortUrl);
        System.out.println("Время жизни (мин.): " + link.getTlMinutes());
        System.out.println("Лимит переходов: " + link.getMaxClicks());
    }

    /*
     Метод для отображения списка ссылок текущего пользователя.
     */
    public void listUserLinks(User currentUser) {
        if (currentUser == null) {
            System.out.println("Сначала авторизуйтесь!");
            return;
        }

        File folder = new File("links");
        File[] files = folder.listFiles();
        if (files == null) {
            System.out.println("Нет ссылок!");
            return;
        }

        // Проверяем есть ли ссылки и если нет, выводим ошибку
        boolean foundAny = false;
        /*
        Проходим по каждому файлу в папке и проверяем является ли файлом (а не директорией),
        и что его имя файла заканчивается на .txt
        */
        for (File f : files) {
            if (f.isFile() && f.getName().endsWith(".txt")) {
                Link link = linkService.loadLink(f.getName().replace(".txt", ""));
                /*
                Проверяем, что загруженная ссылка не null (т.е. успешно загружена)
                и что её ownerUuid совпадает с UUID текущего пользователя,
                а затем выводим информацию
                 */
                if (link != null && link.getOwnerUuid().equals(currentUser.getUuid())) {
                    foundAny = true;
                    String shortUrl = configService.getBaseUrl() + "/" + link.getShortCode();
                    System.out.println("ShortURL: " + shortUrl +
                            ", \nOriginal: " + link.getOriginalUrl() +
                            ", \nTime Limit (min): " + link.getTlMinutes() +
                            ", \nUsed: " + link.getCurrentClicks() +
                            ", \nLimit: " + link.getMaxClicks());
                }
            }
        }
        if (!foundAny) {
            System.out.println("У вас ещё нет ссылок.");
        }
    }

    /*
    Метод перехода по короткой ссылке:
    */
    public void goToLink() {
        System.out.print("Введите короткую ссылку (shortURL или полный URL): ");
        String input = scanner.nextLine();

        // Если пользователь ввёл полный URL, вырежем часть до кода ссылки
        String baseUrlWithSlash = configService.getBaseUrl() + "/";
        if (input.startsWith(baseUrlWithSlash)) {
            // Оставим только кусок после последнего слэша
            input = input.substring(baseUrlWithSlash.length());
        }

        // Теперь в input — только код ссылки
        Link link = linkService.loadLink(input);
        if (link == null) {
            System.out.println("Ссылка не найдена!");
            return;
        }

        // Проверяем срок жизни
        long now = System.currentTimeMillis();
        long age = now - link.getCreateTimeMs();
        long ttlMillis = link.getTlMinutes() * 60_000;
        if (age >= ttlMillis) {
            System.out.println("Ссылка просрочена!");
            linkService.deleteLink(link.getShortCode());
            return;
        }

        // Проверяем лимит переходов
        if (link.getCurrentClicks() >= link.getMaxClicks()) {
            System.out.println("Лимит переходов исчерпан)!");
            return;
        }

        // Увеличиваем счётчик переходов
        link.setCurrentClicks(link.getCurrentClicks() + 1);
        linkService.saveLinkToFile(link);

        // Открываем ссылку в браузере
        try {
            Desktop.getDesktop().browse(new URI(link.getOriginalUrl()));
            System.out.println("Переход выполнен: " + link.getOriginalUrl());
        } catch (Exception e) {
            System.out.println("Не удалось открыть ссылку в браузере: " + e.getMessage());
        }
    }

    /*
    Метод для редактирования лимита переходов.
    */
    public void editLinkLimit(User currentUser) {
        if (currentUser == null) {
            System.out.println("Сначала авторизуйтесь!");
            return;
        }

        System.out.print("Введите короткую ссылку или код ссылки): ");
        String input = scanner.nextLine().trim();

        // Если введён URL короткой ссылки, убираем часть base URL
        String baseUrlWithSlash = configService.getBaseUrl() + "/";
        String shortCode;
        if (input.startsWith(baseUrlWithSlash)) {
            shortCode = input.substring(baseUrlWithSlash.length());
        } else {
            shortCode = input;
        }

        // Загружаем ссылку через LinkService
        Link link = linkService.loadLink(shortCode);
        if (link == null) {
            System.out.println("Ссылка не найдена!");
            return;
        }

        // Проверяем, является ли текущий пользователь владельцем ссылки
        if (!link.getOwnerUuid().equals(currentUser.getUuid())) {
            System.out.println("Вы не являетесь владельцем этой ссылки!");
            return;
        }

        System.out.print("Введите новый лимит переходов: ");
        long newLimit;
        try {
            newLimit = Long.parseLong(scanner.nextLine());
            if (newLimit <= 0) {
                System.out.println("Лимит переходов должен быть положительным числом.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Некорректный ввод лимита переходов.");
            return;
        }

        // Обновляем лимит переходов, учитывая значение из конфигурации
        long finalLimit = Math.max(newLimit, configService.getDefaultMaxClicks());
        link.setMaxClicks(finalLimit);
        linkService.saveLinkToFile(link);

        System.out.println("Лимит переходов обновлён до: " + finalLimit);
    }

    /*
    Метод удаления ссылки.
    */
    public void deleteLink(User currentUser) {
        if (currentUser == null) {
            System.out.println("Сначала авторизуйтесь!");
            return;
        }

        System.out.print("Введите короткую ссылку для удаления: ");
        String input = scanner.nextLine().trim();

        // Если введён URL короткой ссылки, убираем часть base URL
        String baseUrlWithSlash = configService.getBaseUrl() + "/";
        String shortCode;
        if (input.startsWith(baseUrlWithSlash)) {
            shortCode = input.substring(baseUrlWithSlash.length());
        } else {
            shortCode = input;
        }

        // Загружаем ссылку через linkService
        Link link = linkService.loadLink(shortCode);
        if (link == null) {
            System.out.println("Ссылка не найдена!");
            return;
        }

        // Проверяем, является ли текущий пользователь владельцем ссылки
        if (!link.getOwnerUuid().equals(currentUser.getUuid())) {
            System.out.println("Вы не являетесь владельцем этой ссылки!");
            return;
        }

        // Удаляем ссылку через linkService
        linkService.deleteLink(shortCode);
        System.out.println("Ссылка удалена!");
    }
}
