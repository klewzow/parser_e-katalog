package com.gmail.klewzow.parser;

import com.gmail.klewzow.controller.ParseController;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.RecursiveAction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ProductParse extends RecursiveAction {
    private Document document;

    private final Map<String, String> map = new HashMap<>();
    private String link = "";
    private File folder;
    Logger logger = LoggerFactory.getLogger(ProductParse.class);

    public ProductParse() {
    }

    @Override
    protected void compute() {
        if (document == null) return;
        for (int i = 0; i <= 6; ++i) {
            try {
                Thread.currentThread().join(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            try {
                getData(folder, i);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        logger.debug("start thread");
        System.out.println(map.get("title"));

    }

    public ProductParse(String link, File folder) {

        this.document = getPage(link);

        this.link = link;
        this.folder = folder;
    }

    public Map<String, String> parse(String link, File folder) throws IOException {
        logger.debug("start thread");
        this.link = link;
        this.folder = folder;
        this.document = getPage(link);
        for (int i = 0; i <= 6; ++i) {
            try {
                getData(folder, i);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        ParseController.tick();
        return map;
    }

    public void getData(File folder, int key) throws IOException {

        Elements body = document
                .select("body")
                .select("div[class=common-table-div s-width]");
        Elements descTopTable = body
                .select("table")
                .select("tbody")
                .select("tr")
                .select("td[class=main-part-content]");

        switch (key) {
            case 0:
                //"title"
                Elements title = body
                        .select("div[class=common-table-div s-width]")
                        .select("table")
                        .select("tbody")
                        .select("tr[valign=top]")
                        .select("td[class=main-part-content]")
                        .select("div[id=top-page-title]")
                        .select("h1[class=t2 no-mobile]");
                map.put("title", title.text());
                break;
            case 1:
                //"img"
                Elements img = descTopTable
                        .select("table[class=desc-top-table]")
                        .select("tbody")
                        .select("tr")
                        .select("td")
                        .select("div")
                        .select("div[class=i15-container]")
                        .select("div[class=item-img-div]")
                        .select("div[class=img200 h]")
                        .select("img[id]");
                //------------------------------------------------------------
                String urlString = img.attr("src").startsWith("http") ? img.attr("src") : "https://s.ek.ua/jpg/2090047.jpg" + img.attr("src");
                URL url = new URL(urlString);
                HttpURLConnection urlCon;

                Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", Integer.parseInt("8080")));


                File tmp = new File(folder.getAbsolutePath() + "/" + UUID.randomUUID() + ".jpg");
                try (
                        BufferedInputStream bis = new BufferedInputStream(url.openConnection().getInputStream());
                        BufferedOutputStream bos = new BufferedOutputStream(Files.newOutputStream(tmp.toPath()))
                ) {
                    byte[] buffer = new byte[1024];
                    int count;
                    while ((count = bis.read(buffer, 0, 1024)) != -1) {
                        bos.write(buffer, 0, count);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                map.put("img", img.attr("src"));
                //------------------------------------------------------------
                break;

            case 2:
                //"price"
                Elements priceGeneral = descTopTable
                        .select("table[class=desc-top-table]")
                        .select("tbody")
                        .select("td")
                        .select("td[class=desc-short-prices-td]")
                        .select("div[class=desc-short-prices-div]");

                Elements priceMinMax = descTopTable
                        .select("table[class=desc-top-table]")
                        .select("div[class=desc-short-prices]")
                        .select("a[class=ib]")
                        .select("div[class=desc-big-price ib]")
                        .select("span");
                if (priceMinMax.size() >= 1) {
                    map.put("priceMinMax", (priceMinMax.get(0).attr("content") + ":" + priceMinMax.get(1).attr("content")));
                }
                Elements pricesInShops = priceGeneral
                        .select("div[class=desc-short-prices]")
                        .select("div[class]")
                        .select("table[class=desc-hot-prices]")
                        .select("tbody")
                        .select("tr");

                for (Element pricesInShop : pricesInShops) {
                    Elements name = pricesInShop
                            .select("td[class^=model] > div[class=sn-div2] > a > u");
                    Elements price = pricesInShop
                            .select("td[class^=model-shop-price] > a > span");
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < name.size(); i++) {
                        sb.append(name.get(i).text()).append(":").append(price.get(i).text().replaceAll("&nbsp", ""));
                    }
                    map.put("pricesInShops", sb.toString());
                }
                break;

            case 3:
                //"color"
                Elements color = descTopTable
                        .select("div[class=item-block]")
                        .select("div:not([class])")
                        .select("div[class=item-color-plate no-mobile]")
                        .select("div[title]");
                StringBuilder sb = new StringBuilder();
                for (Element element : color) {
                    sb.append(element.attr("title")).append(":");
                }
                map.put("color", sb.toString());
                break;
            case 4:
                //"memory"
                Elements memory = descTopTable
                        .select("div[class=item-block]")
                        .select("div")
                        .select("div[class$=inline]");
                Pattern pattern = Pattern.compile("[1-9]+.[a-z]+;..");
                Matcher matcher = pattern.matcher(memory.html());
                HashSet<String> mem = new HashSet<>();
                while (matcher.find()) {
                    mem.add(matcher.group().replaceAll("&nbsp;", " "));
                }
                map.put("memory", mem.toString());
                break;
            case 5:
                //"spec"
                Elements spec = descTopTable
                        .select("div > div[class=m-c-f2] div");
                StringBuilder sbSpec = new StringBuilder();
                for (Element element : spec) {
                    sbSpec.append(element.attr("title").replaceAll("&nbsp;", " ")).append(":");
                }
                map.put("spec", sbSpec.toString());
                break;
            case 6:
                //"text"
                Elements text = descTopTable
                        .select("div[class=item-block]")
                        .select("div#eski-i-other-txt-1 div");
                StringBuilder sbText = new StringBuilder();
                for (Element element : text) {
                    sbText.append(element.text());
                }
                map.put("text", sbText.toString());
                break;
            default:
                System.out.println("something is wrong\n");
                break;
        }
    }

    private Document getPage(String url) {

        try {
            return Jsoup.parse(Jsoup.connect(url).get().html());
        } catch (IOException e) {
            return new Document("<title>Not found</title>");
        }
    }


}
