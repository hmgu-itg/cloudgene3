package cloudgene.mapred.server.controller;

import cloudgene.mapred.server.services.NewsService;
import io.micronaut.http.annotation.Controller;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.http.annotation.Consumes;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.MediaType;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.security.annotation.Secured;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class NewsController {
	private static Logger log = LoggerFactory.getLogger(NewsController.class);
    
	@Inject
	protected NewsService newsService;

	@Post("/api/v2/users/deleteallnews")
	@Secured(SecurityRule.IS_ANONYMOUS)
	//TODO: only allow admin to add/delete news
	//@Secured(User.ROLE_ADMIN)
	public boolean deleteAllNews() {
	    return newsService.deleteAllNews();
	}

	@Post("/api/v2/users/deletenews/{ID}")
	@Consumes(MediaType.TEXT_PLAIN)
	@Secured(SecurityRule.IS_ANONYMOUS)
	//TODO: only allow admin to add/delete news
	//@Secured(User.ROLE_ADMIN)
	public boolean deleteNews(String ID) {
	    return newsService.deleteNews(Integer.parseInt(ID));
	}

	@Post("/api/v2/users/addnews/{text}")
	@Consumes(MediaType.TEXT_PLAIN)
	@Secured(SecurityRule.IS_ANONYMOUS)
	//TODO: only allow admin to add/delete news
	//@Secured(User.ROLE_ADMIN)
	public boolean addNews(String text) {
	    // log.info("addNews with text="+text);
		return newsService.addNews(text);
	}

	@Get("/api/v2/users/news")
	@Secured(SecurityRule.IS_ANONYMOUS)
	//@Secured(User.ROLE_ADMIN)
	public String getNews() {
	    // log.info("getNews");
		return newsService.getNews();
	}
	@Get("/api/v2/users/latestnews")
	@Secured(SecurityRule.IS_ANONYMOUS)
	//@Secured(User.ROLE_ADMIN)
	public String getLatestNews() {
		return newsService.getLatestNews();
	}

}
