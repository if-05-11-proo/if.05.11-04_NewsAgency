package at.htlleonding.newsagency;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class NewsAgencyTest {
    @Test
    void testArticleConstructorAndToString() {
        Article article = new Article("Rapids Frustbewältigung in Altach", "Die Hütteldorfer leisteten Wiedergutmachung für den jüngsten Umfaller, feierten mit 3:0 einen seltenen Sieg im Ländle. Auch St. Pölten und Sturm Graz jubelten.", LocalDateTime.of(2019, 11, 9, 17, 52, 0), "https://www.diepresse.com/5719582/Bundesliga_Rapids-Frustbewaeltigung-in-Altach?from=rss", NewsCategory.SPORTS);

        assertEquals("Rapids Frustbewältigung in Altach", article.getTitle());
        assertEquals("Die Hütteldorfer leisteten Wiedergutmachung für den jüngsten Umfaller, feierten mit 3:0 einen seltenen Sieg im Ländle. Auch St. Pölten und Sturm Graz jubelten.", article.getDescription());
        assertEquals("https://www.diepresse.com/5719582/Bundesliga_Rapids-Frustbewaeltigung-in-Altach?from=rss", article.getUrl());
        assertEquals("2019-11-09/17-52-00", article.getPublishingDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd/HH-mm-ss")));
        assertEquals(NewsCategory.SPORTS, article.getCategory());

        assertEquals("(SPORTS) Rapids Frustbewältigung in Altach - veroeffentlicht 09.11.2019 17:52", article.toString());
    }

    @Test
    void testArticleFactoryAndComparison() {
        Article article = ArticleFactory.createFromString("Energie: Wasserstoff statt Dieselrauch   ;  https://www.diepresse.com/5691450/Energie_Wasserstoff-statt-Dieselrauch?from=rss;Bei Schwerfahrzeugen, aber auch Staplern könnte die Brennstoffzelle eine interessante Alternative zu Diesel und Batterie sein. Noch mangelt es allerdings an Fahrzeugen, Infrastruktur und Akzeptanz.;  2019-09-17T17:56:00 ;  LIFESTYLE");

        assertEquals("Energie: Wasserstoff statt Dieselrauch", article.getTitle());
        assertEquals("Bei Schwerfahrzeugen, aber auch Staplern könnte die Brennstoffzelle eine interessante Alternative zu Diesel und Batterie sein. Noch mangelt es allerdings an Fahrzeugen, Infrastruktur und Akzeptanz.", article.getDescription());
        assertEquals("https://www.diepresse.com/5691450/Energie_Wasserstoff-statt-Dieselrauch?from=rss", article.getUrl());
        assertEquals("2019-09-17/17-56-00", article.getPublishingDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd/HH-mm-ss")));
        assertEquals(NewsCategory.LIFESTYLE, article.getCategory());

        assertEquals("(LIFESTYLE) Energie: Wasserstoff statt Dieselrauch - veroeffentlicht 17.09.2019 17:56", article.toString());

        Article newerArticle = ArticleFactory.createFromString("Russland kapselt sich vom Internet ab [premium];https://www.diepresse.com/5715533/Neues-Gesetz_Russland-kapselt-sich-vom-Internet-ab?from=rss;Wladimir Putin treibt seine Vision vom autonomen russischen Staatsnetz voran. Gegner fürchten digitale Zensur wie in China. Noch ist unklar, ob Moskau die technischen Mittel dafür hat.;2019-11-01T16:24:00;TECH");

        assertEquals(true, article.compareTo(newerArticle) < 0);
        assertEquals(true, newerArticle.compareTo(article) > 0);

        Article newestArticle = ArticleFactory.createFromString("Machtkampf in Bolivien - Staatssender besetzt, Polizei meutert;https://www.diepresse.com/5719721/Staatskrise_Machtkampf-in-Bolivien-Staatssender-besetzt-Polizei?from=rss;Auch Polizisten schließen sich den Protesten gegen den linken Präsidenten Evo Morales an. Gegner orten Wahlbetrug und fordern seinen Rücktritt. Zwei staatliche Sender wurden besetzt.;2019-11-10T08:47:00;POLITICS");

        assertEquals(true, article.compareTo(newestArticle) < 0);
        assertEquals(true, newestArticle.compareTo(article) > 0);
        assertEquals(true, newerArticle.compareTo(newestArticle) < 0);
        assertEquals(true, newestArticle.compareTo(newerArticle) > 0);
    }

    @Test
    void testNewsAgency() {
        NewsAgency diePresse = new NewsAgency();

        assertEquals(false, diePresse.hasMoreArticles());
        assertEquals(null, diePresse.getLatestArticle());
        diePresse.publishArticle();
        assertEquals(null, diePresse.getLatestArticle());

        diePresse.readFromFile("data/does_not_exist.csv");
        diePresse.readFromFile("data/presse.csv");

        assertEquals(true, diePresse.hasMoreArticles());
        assertEquals(null, diePresse.getLatestArticle());

        diePresse.publishArticle();

        Article article = diePresse.getLatestArticle(); //first article
        assertEquals("(LIFESTYLE) Mitsubishi: Abschied in Grau und Schwarz - veroeffentlicht 17.09.2019 17:39", article.toString());

        article = diePresse.getLatestArticle(); //no publish - same article should be returned
        assertEquals("(LIFESTYLE) Mitsubishi: Abschied in Grau und Schwarz - veroeffentlicht 17.09.2019 17:39", article.toString());

        diePresse.publishArticle();

        article = diePresse.getLatestArticle(); //second article
        assertEquals("(LIFESTYLE) Renault Master: Aufgepeppt mit Pkw-Komfort - veroeffentlicht 17.09.2019 17:42", article.toString());

        HashMap<NewsCategory, Integer> categoryCounter = new HashMap<>();
        categoryCounter.put(NewsCategory.ECONOMY, 0);
        categoryCounter.put(NewsCategory.LIFESTYLE, 2);
        categoryCounter.put(NewsCategory.POLITICS, 0);
        categoryCounter.put(NewsCategory.SPORTS, 0);
        categoryCounter.put(NewsCategory.TECH, 0);
        int articleCounter = 2;

        while(diePresse.hasMoreArticles()) {
            diePresse.publishArticle();

            article = diePresse.getLatestArticle();

            categoryCounter.merge(article.getCategory(), 1, Integer::sum);
            articleCounter++;
        }

        //last article
        assertEquals("(POLITICS) Machtkampf in Bolivien - Staatssender besetzt, Polizei meutert - veroeffentlicht 10.11.2019 08:47", article.toString());

        assertEquals(20, categoryCounter.get(NewsCategory.ECONOMY));
        assertEquals(20, categoryCounter.get(NewsCategory.LIFESTYLE));
        assertEquals(20, categoryCounter.get(NewsCategory.POLITICS));
        assertEquals(20, categoryCounter.get(NewsCategory.SPORTS));
        assertEquals(20, categoryCounter.get(NewsCategory.TECH));
        assertEquals(100, articleCounter);
    }
}