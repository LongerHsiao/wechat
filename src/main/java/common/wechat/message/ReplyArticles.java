package common.wechat.message;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "xml")
public class ReplyArticles extends ReplyMessage {

	public String ToUserName;
	public String FromUserName;
	public long CreateTime;
	public String MsgType = "news";
	public int ArticleCount;
	public Articles Articles;

	/**
	 * 
	 * 
	 * @param title
	 * @param description
	 * @param picUrl
	 * @param url
	 */
	public void addArticles(String title, String description, String picUrl, String url) {
		if (Articles == null) {
			Articles = new Articles();
		}
		ArticleItem item = new ArticleItem();
		item.Title = title;
		item.Description = description;
		item.PicUrl = picUrl;
		item.Url = url;
		Articles.item.add(item);

		ArticleCount = Articles.item.size();
	}
}

@XmlRootElement
class Articles {
	public List<ArticleItem> item = new ArrayList<ArticleItem>();
}

@XmlRootElement
class ArticleItem {
	public String Title;
	public String Description;
	public String PicUrl;
	public String Url;
}