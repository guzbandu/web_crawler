package web_crawler;

import java.io.Serializable;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

public class GraphStruct implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private Graph<String, DefaultEdge> graph;
	
	public GraphStruct() {
		graph = new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);
	}

	public Graph<String, DefaultEdge> getGraph() {
		return graph;
	}

	public void setGraph(Graph<String, DefaultEdge> graph) {
		this.graph = graph;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}
