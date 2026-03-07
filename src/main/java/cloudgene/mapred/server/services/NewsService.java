package cloudgene.mapred.server.services;

import java.util.List;
import cloudgene.mapred.apps.Application;
import cloudgene.mapred.core.News;
import cloudgene.mapred.database.NewsDao;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class NewsService {
	private static Logger log = LoggerFactory.getLogger(NewsService.class);
	@Inject
	protected cloudgene.mapred.server.Application application;
    
    public boolean deleteNews(int ID) {
        NewsDao dao = new NewsDao(application.getDatabase());
        return dao.delete(ID);
    };
    
    public boolean deleteAllNews() {
        NewsDao dao = new NewsDao(application.getDatabase());
        return dao.deleteAll();
    };
    
    public boolean addNews(String text) {
	News n=new News();
	n.setText(text);
        NewsDao dao = new NewsDao(application.getDatabase());
        return dao.insert(n);
    };
    
    public String getNews() {
        NewsDao dao = new NewsDao(application.getDatabase());
        List<News> news = dao.findAll();
        JSONArray jsonArray = new JSONArray();
	SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	SimpleDateFormat formatter2 = new SimpleDateFormat("MMMM dd, yyyy");
	try {
	    for (News n : news) {
                JSONObject object = new JSONObject();
		object.put("ID",Integer.toString(n.getId()));
		String ts=n.getTimestamp();
                object.put("time",ts);
                object.put("date",formatter2.format(formatter.parse(ts)));
                object.put("text",n.getText());
                jsonArray.add(object);
	    }
        } catch (ParseException e) {
            log.error("Invalid date format: "+e);
        }

        return jsonArray.toString();
    };
    
    public String getLatestNews() {
        NewsDao dao = new NewsDao(application.getDatabase());
        News news = dao.findLatest();
        JSONArray jsonArray = new JSONArray();
	JSONObject object = new JSONObject();
	object.put("ID",Integer.toString(news.getId()));
	object.put("time",news.getTimestamp());
	object.put("text",news.getText());
	jsonArray.add(object);
        JSONObject object1 = new JSONObject();
        object1.put("data", jsonArray);

        return object1.toString();
    };
}
