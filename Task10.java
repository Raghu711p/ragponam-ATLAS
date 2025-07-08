class Graph01 {
    class Edge {
        int src, dest;
    }
    int vertices, edges;

    Edge[] edge;

    Graph01(int vertices, int edges) {
        this.vertices = vertices;
        this.edges = edges;

        edge = new Edge[edges];
        for(int i = 0; i < edges; i++) {
            edge[i] = new Edge();
        }
    }

    public static void main(String[] args) {
        int noVertices = 5;
        int noEdges = 8;
        Graph01 gObj = new Graph01(noVertices, noEdges);

        gObj.edge[0].src = 1;
        gObj.edge[0].dest = 2;
        gObj.edge[1].src = 1;
        gObj.edge[1].dest = 3;
        gObj.edge[2].src = 1;
        gObj.edge[2].dest = 4;
        gObj.edge[3].src = 2;
        gObj.edge[3].dest = 4;
        gObj.edge[4].src = 2;
        gObj.edge[4].dest = 5;
        gObj.edge[5].src = 3;
        gObj.edge[5].dest = 4;
        gObj.edge[6].src = 3;
        gObj.edge[6].dest = 5;
        gObj.edge[7].src = 4;
        gObj.edge[7].dest = 5;

        for(int i =0; i < noEdges; i++) {
            System.out.println(gObj.edge[i].src+ " - " + gObj.edge[i].dest);
        }
    }
}
