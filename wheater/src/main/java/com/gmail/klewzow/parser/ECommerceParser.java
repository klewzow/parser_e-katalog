package com.gmail.klewzow.parser;

import com.gmail.klewzow.interfaces.LoggerInterface;
import lombok.extern.log4j.Log4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Log4j
@Component
public class ECommerceParser {

    private Document document;
    private Pattern pattern = Pattern.compile("[1-9]*$");
    private LoggerInterface loger;
    private String baseUrl;
    private String url;
    private final ProductParse productParse ;
    public ECommerceParser( ProductParse productParse) {
        this.productParse = productParse;
    }
    public boolean go(String url, LoggerInterface logger, File file, File folder) throws ExecutionException, InterruptedException, IOException {
        this.baseUrl = this.url = url;
        this.loger = logger;
        document = getNewDocument(url);
        if (document != null) {
            List<String> pages = pageValue(this.document, this.pattern);
            List<String> productLink = productLinks(productCard(this.document));
            loggerThread(productLink, document, pages.size());
            //-----------------------------------------------------------------------------------------------------
            List<String> allLinks = (List<String>) completableFutureLinks(pageValue(this.document, this.pattern)).get();
            loger.burn("Всего товаров : " + allLinks.size());
            ForkJoinPool forkJoinPool = new ForkJoinPool(16);
            log.debug(allLinks);
//            for (String allLink : allLinks) {
//                System.out.println(allLink);
//            }
            CompletableFuture<List<Map<String, String>>> welcomeText = CompletableFuture.supplyAsync(() -> {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    throw new IllegalStateException(e);
                }
                return allLinks.stream().parallel().map(el -> {
                    try {
                        return new ProductParse().parse(el, folder);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }).collect(Collectors.toList());
            }, forkJoinPool);

// Block and wait for the future to complete.
            List<Map<String, String>> list = welcomeText.get();
            log.debug(list);
            CompletableFuture completableFuture = new CompletableFuture();
            completableFuture.complete(completableFuture);

        } else {
            loger.burn("Page not Found.");
            return true;
        }


        loger.burn("Done");
        return true;
    }


    private static <T> Document getPage(String url) throws IOException {
        Document page = Jsoup.parse(Jsoup.connect(url).get().html());
        return page;
    }

    // К-во страниц
    private List<String> pageValue(Document doc, Pattern pattern) {
        List<String> pages = new ArrayList<>();
        Elements listPagerDiv = doc.select("div[class=common-table-div s-width]").select("table").select("tbody").select("tr[valign=top]").select("td[class=main-part-content]").select(" [class=list-pager-div]").select("div[class=ib page-num]").select("a:last-child");


        int res = 0;
        if (listPagerDiv.size() > 0) {
            Matcher val = pattern.matcher(listPagerDiv.first().attr("href"));
            if (val.find()) {
                try {
                    res = Integer.parseInt(val.group());
                    for (int i = 0; i <= res; i++) {
                        pages.add(baseUrl + i + "/");
                    }
                } catch (NumberFormatException e) {
                    return pages;
                }
            }
        } else {
            pages.add(url);
        }
        loger.burn("К-во страниц : " + pages.size());
        return pages;
    }

    //Карточка товара
    private Elements productCard(Document document) {
        return document.getElementsByAttributeValue("id", "list_form1").select("table[class=model-short-block]");
    }

    // Ссылки на товары
    private List<String> productLinks(Elements cards) {
        List<String> linkTo = new ArrayList<>();

        Elements modelShorts = cards.select("td[class=model-short-info]").select("table").select("tbody").select("td:not([class])").select("a");
        for (Element el : modelShorts) {
            linkTo.add((document.baseUri() + el.attr("href")));
        }
        return linkTo;
    }

    //Title
    private Element title(Document document) {
        return document.select("head").select("title").first();
    }
//Get Document

    private Document getNewDocument(String url) {
        try {
            return Jsoup.parse(Jsoup.connect(url).get().html());
        } catch (Exception e) {
            loger.burn("Error get Request e commerce parser 33...");
            return null;
        }
    }

    //Logging
    private void logging(List<String> productLink, Element title, int val) {
        loger.burn("Товаров на странице : " + productLink.size());
        loger.burn("Рубрика : " + title.text());

    }

    private void loggerThread(List<String> productLink, Document document, int val) {
        Thread loggers = new Thread(new Runnable() {
            @Override
            public void run() {
                logging(productLink, title(document), val);
            }
        }, "LoggerThread");

        loggers.start();
    }

    private CompletableFuture completableFutureLinks(List<String> pages) {

        return CompletableFuture.supplyAsync(() -> {
            return pages.stream().parallel().map(page -> {
                        try {
                            return Jsoup.parse(Jsoup.connect(page).get().html());
                        } catch (IOException e) {
                            return new Document("<title>Not found</title>" + page);
                        }
                    }).map(el -> productLinks(productCard(el))).flatMap(List::stream)

                    .collect(Collectors.toList());

        }, new ForkJoinPool());

    }
}
