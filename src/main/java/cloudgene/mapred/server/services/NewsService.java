package cloudgene.mapred.server.services;

import java.util.List;

import cloudgene.mapred.apps.Application;
import cloudgene.mapred.core.News;
import cloudgene.mapred.database.NewsDao;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class NewsService {
	@Inject
	protected cloudgene.mapred.server.Application application;
    
    public String getNews() {
        NewsDao dao = new NewsDao(application.getDatabase());
        List<News> news = dao.findAll();
        JSONArray jsonArray = new JSONArray();
        for (News n : news) {
                JSONObject object = new JSONObject();
                object.put("time",n.getTimestamp());
                object.put("text",n.getText());
                jsonArray.add(object);
        }
        // JSONObject object = new JSONObject();
        // object.put("data", jsonArray);

        return jsonArray.toString();
    };
    
    public String getLatestNews() {
        NewsDao dao = new NewsDao(application.getDatabase());
        News news = dao.findLatest();
        JSONArray jsonArray = new JSONArray();
	JSONObject object = new JSONObject();
	object.put("time",news.getTimestamp());
	object.put("text",news.getText());
	jsonArray.add(object);
        JSONObject object1 = new JSONObject();
        object1.put("data", jsonArray);

        return object1.toString();
    };
}
