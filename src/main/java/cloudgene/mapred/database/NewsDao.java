package cloudgene.mapred.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cloudgene.mapred.core.News;
import cloudgene.mapred.database.util.Database;
import cloudgene.mapred.database.util.IRowMapper;
import cloudgene.mapred.database.util.JdbcDataAccessObject;

public class NewsDao extends JdbcDataAccessObject {
    private static final Log log = LogFactory.getLog(CountryDao.class);

    public NewsDao(Database database) {
	super(database);
    }

    public boolean insert(News n) {
	StringBuilder sql = new StringBuilder();
	sql.append("insert into `news` (news_text) ");
	sql.append("values (?)");

	try {
	    Object[] params = new Object[1];
	    params[0] = n.getText();
	    int id = insert(sql.toString(), params);
	} catch (SQLException e) {
	    return false;
	}
	return true;
    }

    @SuppressWarnings("unchecked")
    public News findLatest() {
	StringBuffer sql = new StringBuffer();

	sql.append("select * ");
	sql.append("from `news` ");
	sql.append("order by id desc limit 1");

	List<News> result = new Vector<News>();

	try {
	    result = query(sql.toString(), new NewsMapper());
	    log.debug("find latest news successful; size = " + result.size());
	} catch (SQLException e1) {
	    log.error("find latest news failed", e1);
	}
	return result.get(0);
    }

    @SuppressWarnings("unchecked")
    public List<News> findAll() {
	StringBuffer sql = new StringBuffer();

	sql.append("select * ");
	sql.append("from `news`");

	List<News> result = new Vector<News>();

	try {
	    result = query(sql.toString(), new NewsMapper());
	    log.debug("find all news successful; size = " + result.size());
	} catch (SQLException e1) {
	    log.error("find all news failed", e1);
	}
	return result;
    }

    @SuppressWarnings("unchecked")
    public List<News> findAll(int offset, int limit) {
	StringBuffer sql = new StringBuffer();

	sql.append("select * ");
	sql.append("from `news` ");
	sql.append("limit ?,?");

	Object[] params = new Object[2];
	params[0] = offset;
	params[1] = limit;

	List<News> result = new Vector<News>();

	try {
	    result = query(sql.toString(), params, new NewsMapper());
	    log.debug("find all news successful; size = " + result.size());
	} catch (SQLException e1) {
	    log.error("find all news failed", e1);
	}
	return result;
    }

    public static class NewsMapper implements IRowMapper {
	@Override
	public News mapRow(ResultSet rs, int row) throws SQLException {
	    News news = new News();
	    news.setId(rs.getInt("news.id"));
	    news.setTimestamp(rs.getString("news.news_time"));
	    news.setText(rs.getString("news.news_text"));
	    return news;
	}
    }
}
