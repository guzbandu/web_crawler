package web_crawler;

import java.io.Serializable;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

public class GraphStruct implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private Graph<Vertex, DefaultEdge> graph;	
	
	public GraphStruct() {
		graph = new DefaultDirectedGraph<Vertex, DefaultEdge>(DefaultEdge.class);
	}

	public synchronized Graph<Vertex, DefaultEdge> getGraph() {
		return graph;
	}

	public synchronized void setGraph(Graph<Vertex, DefaultEdge> graph) {
		this.graph = graph;
	}
	
	public synchronized void addEdge(Vertex w, Vertex x) {
		this.graph.addEdge(w, x);
	}
	
	public synchronized void addVertex(Vertex w) {
		this.graph.addVertex(w);
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}
