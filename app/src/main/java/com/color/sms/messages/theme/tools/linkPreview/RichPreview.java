package com.color.sms.messages.theme.tools.linkPreview;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.webkit.URLUtil;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URI;
import java.net.URISyntaxException;

class RichPreview {

    private MetaData metaData;
    private ResponseListener responseListener;
    private String url;

    RichPreview(ResponseListener responseListener) {
        this.responseListener = responseListener;
        metaData = new MetaData();
    }

    void getPreview(String url) {
        this.url = url;
        new getData().execute();
    }

    @SuppressLint("StaticFieldLeak")
    private class getData extends AsyncTask<Void, Void, Integer> {
        @Override
        protected Integer doInBackground(Void... params) {
            if (TextUtils.isEmpty(url)) return -1;
            Document doc;
            try {
                doc = Jsoup.connect(url)
                        .timeout(30 * 1000)
                        .get();

                Elements elements = doc.getElementsByTag("meta");

                // getTitle doc.select("meta[property=og:title]")
                String title = doc.select("meta[property=og:title]").attr("content");

                if (title == null || title.isEmpty()) {
                    title = doc.title();
                }
                metaData.setTitle(title);

                //getDescription
                String description = doc.select("meta[name=description]").attr("content");
                if (description.isEmpty()) {
                    description = doc.select("meta[name=Description]").attr("content");
                }
                if (description.isEmpty()) {
                    description = doc.select("meta[property=og:description]").attr("content");
                }
                if (description.isEmpty()) {
                    description = "";
                }
                metaData.setDescription(description);


                // getMediaType
                Elements mediaTypes = doc.select("meta[name=medium]");
                String type;
                if (mediaTypes.size() > 0) {
                    String media = mediaTypes.attr("content");

                    type = media.equals("image") ? "photo" : media;
                } else {
                    type = doc.select("meta[property=og:type]").attr("content");
                }
                metaData.setMediatype(type);


                //getImages
                Elements imageElements = doc.select("meta[property=og:image]");
                if (imageElements.size() > 0) {
                    String image = imageElements.attr("content");
                    if (!image.isEmpty()) {
                        metaData.setImageurl(resolveURL(url, image));
                    }
                }
                if (metaData.getImageurl().isEmpty()) {
                    String src = doc.select("link[rel=image_src]").attr("href");
                    if (!src.isEmpty()) {
                        metaData.setImageurl(resolveURL(url, src));
                    } else {
                        src = doc.select("link[rel=apple-touch-icon]").attr("href");
                        if (!src.isEmpty()) {
                            metaData.setImageurl(resolveURL(url, src));
                            metaData.setFavicon(resolveURL(url, src));
                        } else {
                            src = doc.select("link[rel=icon]").attr("href");
                            if (!src.isEmpty()) {
                                metaData.setImageurl(resolveURL(url, src));
                                metaData.setFavicon(resolveURL(url, src));
                            }
                        }
                    }
                }

                //Favicon
                String src = doc.select("link[rel=apple-touch-icon]").attr("href");
                if (!src.isEmpty()) {
                    metaData.setFavicon(resolveURL(url, src));
                } else {
                    src = doc.select("link[rel=icon]").attr("href");
                    if (!src.isEmpty()) {
                        metaData.setFavicon(resolveURL(url, src));
                    }
                }

                for (Element element : elements) {
                    if (element.hasAttr("property")) {
                        String str_property = element.attr("property").trim();
                        if (str_property.equals("og:url")) {
                            metaData.setUrl(element.attr("content"));
                        }
                        if (str_property.equals("og:site_name")) {
                            metaData.setSitename(element.attr("content"));
                        }
                    }
                }

                if (metaData.getUrl().equals("") || metaData.getUrl().isEmpty()) {
                    URI uri = null;
                    try {
                        uri = new URI(url);
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                    if (url == null) {
                        metaData.setUrl(url);
                    } else {
                        assert uri != null;
                        metaData.setUrl(uri.getHost());
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                return -1;
            }
            return 0;
        }

        @Override
        protected void onPostExecute(Integer aVoid) {
            super.onPostExecute(aVoid);
            if (aVoid == 0) {
                responseListener.onData(metaData);
            } else {
                responseListener.onError(new Exception("No Html Received from " + url + " Check your Internet "));
            }
        }
    }

    private String resolveURL(String url, String part) {
        if (URLUtil.isValidUrl(part)) {
            return part;
        } else {
            URI base_uri = null;
            try {
                base_uri = new URI(url);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            assert base_uri != null;
            base_uri = base_uri.resolve(part);
            return base_uri.toString();
        }
    }

}