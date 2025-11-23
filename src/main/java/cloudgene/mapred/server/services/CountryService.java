package cloudgene.mapred.server.services;

import java.util.List;
//import java.util.Vector;

import cloudgene.mapred.apps.Application;
// import cloudgene.mapred.apps.ApplicationRepository;
//import cloudgene.mapred.core.Group;
//import cloudgene.mapred.core.User;
import cloudgene.mapred.core.Country;
import cloudgene.mapred.database.CountryDao;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class CountryService {
	@Inject
	protected cloudgene.mapred.server.Application application;
    
    /* all countries */
    public String getAllCountries() {
        CountryDao dao = new CountryDao(application.getDatabase());
        List<Country> countries = dao.findAll();
        JSONArray jsonArray = new JSONArray();
        for (Country country : countries) {
            if (country.getDisplay() == true) {
                JSONObject object = new JSONObject();
                object.put("name", country.getName());
                object.put("display", country.getDisplay());
                object.put("allowed", country.getAllowed());
                jsonArray.add(object);
            }
        }
        JSONObject object = new JSONObject();
        object.put("data", jsonArray);

        return object.toString();
    };

    /* allowed countries */
    public String getAllowedCountries() {
        CountryDao dao = new CountryDao(application.getDatabase());
        List<Country> countries = dao.findAll();
        JSONArray jsonArray = new JSONArray();
        for (Country country : countries) {
            if (country.getAllowed() == true) {
                JSONObject object = new JSONObject();
                object.put("name", country.getName());
                object.put("display", country.getDisplay());
                object.put("allowed", country.getAllowed());
                jsonArray.add(object);
            }
        }
        JSONObject object = new JSONObject();
        object.put("data", jsonArray);

        return object.toString();
    };

    // /* get countries */
    //     public String getCountries(String query, String page, int pageSize) {
    // 	    //User user = getAuthUser();

    // 		// if (user == null) {
    // 		// 	setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
    // 		// 	return new StringRepresentation("The request requires user authentication.");
    // 		// }

    // 		// if (!user.isAdmin()) {
    // 		// 	setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
    // 		// 	return new StringRepresentation("The request requires administration rights.");
    // 		// }

    // 		//String page = getQueryValue("page");
    // 		//int pageSize = DEFAULT_PAGE_SIZE;

    // 		int offset = 0;
    // 		if (page != null) {

    // 			offset = Integer.valueOf(page);
    // 			if (offset < 1) {
    // 				offset = 1;
    // 			}
    // 			offset = (offset - 1) * pageSize;
    // 		}

    // 		CountryDao dao = new CountryDao(application.getDatabase());

    // 		List<Country> countries = null;
    // 		int count = 0;
    // 		//String query = getQueryValue("query");
    // 		if (query != null && !query.isEmpty()) {
    // 			countries = dao.findByQuery(query);
    // 			page = "1";
    // 			count = countries.size();
    // 			pageSize = count;
    // 		} else {
    // 			if (page != null) {
    // 				countries = dao.findAll(offset, pageSize);
    // 				count = dao.findAll().size();
    // 			} else {
    // 				countries = dao.findAll();
    // 				page = "1";
    // 				count = countries.size();
    // 				pageSize = count;
    // 			}
    // 		}

    // 		JSONArray jsonArray = new JSONArray();
    //     for (Country country : countries) {
    //         JSONObject object = new JSONObject();
    //         object.put("name", country.getName());
    //         object.put("display", country.getDisplay());
    //         object.put("allowed", country.getAllowed());
    //         jsonArray.add(object);
    //     }

    // 		JSONObject object = PageUtil.createPageObject(Integer.parseInt(page), pageSize, count);
    // 		object.put("data", jsonArray);

    // 		return new StringRepresentation(object.toString());
    // }

}
