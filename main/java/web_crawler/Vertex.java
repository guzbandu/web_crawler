package web_crawler;

import java.io.Serializable;

public class Vertex implements Serializable {
	private static final long serialVersionUID = 1L;
	String docId;
	String url;
	
	public Vertex(String docId, String url) {
		this.docId = docId;
		this.url = url;
	}

	public String getDocId() {
		return docId;
	}

	public void setDocId(String docId) {
		this.docId = docId;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

}
