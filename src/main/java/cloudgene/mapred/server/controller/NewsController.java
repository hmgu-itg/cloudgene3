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
import cloudgene.mapred.core.User;
import io.micronaut.security.authentication.Authentication;
import cloudgene.mapred.server.auth.AuthenticationService;

@Controller
public class NewsController {
	private static Logger log = LoggerFactory.getLogger(NewsController.class);
    
	@Inject
	protected NewsService newsService;

	@Inject
	protected AuthenticationService authenticationService;

	@Post("/api/v2/users/deleteallnews")
	@Secured(User.ROLE_ADMIN)
	public boolean deleteAllNews(Authentication authentication) {
	    return newsService.deleteAllNews();
	}

	@Post("/api/v2/users/deletenews/{ID}")
	@Consumes(MediaType.TEXT_PLAIN)
	@Secured(User.ROLE_ADMIN)
	public boolean deleteNews(Authentication authentication,String ID) {
	    return newsService.deleteNews(Integer.parseInt(ID));
	}

	@Post("/api/v2/users/addnews/{text}")
	@Consumes(MediaType.TEXT_PLAIN)
	@Secured(User.ROLE_ADMIN)
	public boolean addNews(Authentication authentication,String text) {
		User admin = authenticationService.getUserByAuthentication(authentication);
	        log.info("addNews with text="+text+"\n"+admin.getId()+" "+admin.getMail());
		return newsService.addNews(text);
	}

	@Get("/api/v2/users/news")
	@Secured(SecurityRule.IS_ANONYMOUS)
	public String getNews() {
		return newsService.getNews();
	}

	@Get("/api/v2/users/latestnews")
	@Secured(SecurityRule.IS_ANONYMOUS)
	public String getLatestNews() {
		return newsService.getLatestNews();
	}

}
