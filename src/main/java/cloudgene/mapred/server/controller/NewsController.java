package cloudgene.mapred.server.controller;

import cloudgene.mapred.server.services.NewsService;
import io.micronaut.http.annotation.Controller;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.http.annotation.Consumes;
import io.micronaut.http.annotation.Get;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.security.annotation.Secured;
import jakarta.inject.Inject;

@Controller
public class NewsController {
	@Inject
	protected NewsService newsService;

	@Post("/api/v2/users/addnews")
	@Consumes(MediaType.TEXT_PLAIN)
	@Secured(SecurityRule.IS_ANONYMOUS)
	//TODO: only allow admin to add/delete news
	//@Secured(User.ROLE_ADMIN)
	public boolean addNews( @Parameter("text") String  text) {
		return newsService.addNews(text);
	}
	@Get("/api/v2/users/news")
	@Secured(SecurityRule.IS_ANONYMOUS)
	//@Secured(User.ROLE_ADMIN)
	public String getNews() {
		return newsService.getNews();
	}
	@Get("/api/v2/users/latestnews")
	@Secured(SecurityRule.IS_ANONYMOUS)
	//@Secured(User.ROLE_ADMIN)
	public String getLatestNews() {
		return newsService.getLatestNews();
	}

}
