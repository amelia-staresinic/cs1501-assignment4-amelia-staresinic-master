import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Scanner;
import java.util.Stack;

public class PittGuard {
    private Map<Character, Boolean> vertices;
    private Digraph G = null;
    private static final int INFINITY = Integer.MAX_VALUE;
    private boolean directed = false;

    public static void main(String[] args) throws IOException{
        PittGuard pg = new PittGuard();
        String mode = null;
        String fileName = null;
        char src = ' ';
        char dst = ' ';
        //default is false

        //read args
        for(int i = 0; i<args.length; i++){
            switch(args[i]){
                case "--mode":
                    mode = args[++i];
                    break;
                case "--input":
                    fileName = args[++i];
                    break;
                case "--src":
                    src = args[++i].charAt(0);
                    break;
                case "--dst":
                    dst = args[++i].charAt(0);
                    break;
                case "--server":
                    src = args[++i].charAt(0);
                    break;
                case "--directed":
                    pg.directed = true;
                    break;
                default:
                    System.err.println("Error: unknown argument");
                    System.exit(1);
            }
        }
        //check requirements
        if(mode == null || fileName == null){
            System.err.println("Error: Missing arguments");
            System.exit(1);
        }
        //TASK 1: infect
        else if(mode.equals("infect")){
            if(src == ' ' || dst == ' '){
                System.err.println("Error: Missing arguments");
                System.exit(1);
            }
            else if(src == dst){
                System.out.println(0);
                System.exit(0);
            }
            else{
                pg.readFile(fileName);
                if(!pg.vertices.containsKey(src) || !pg.vertices.containsKey(dst)){
                    System.err.println("Error: source or destination does not exist in file");
                    System.exit(1);
                }
                else if(!pg.vertices.get(src) || !pg.vertices.get(dst)){
                    System.out.println(-1);
                    System.exit(0);
                }
                else{
                    pg.shortestHops(src, dst);
                }
            }
        }
        //TASK 2: patch
        else if(mode.equals("patch")){
            if(src == ' '){
                System.err.println("Error: server not specified");
                System.exit(1);
            }
            else{
                pg.readFile(fileName);
                if(pg.vertices.get(src)){
                    System.err.println("Error: server vulnerable");
                    System.exit(1);
                }
                double max = 0;
                for(Map.Entry<Character, Boolean> entry : pg.vertices.entrySet()){
                    //tests paths to all vulnerable destinations
                    if(entry.getValue()){
                        double dist = pg.shortestDistance(src, entry.getKey());
                        if (dist > max){
                            max = dist;
                        }
                    }
                }
                //print out max shortest path or INF (if vulnerable node unreachable)
                System.out.println(max);
            }
        }
    }
    
    private void readFile(String fileName) throws IOException {
        Scanner fileScan = new Scanner(new FileInputStream(fileName));
        //num nodes
        int v = Integer.parseInt(fileScan.nextLine());
        G = new Digraph(v);
        //skip comment line
        fileScan.nextLine();
        //save nodes and vulnerability
        vertices = new HashMap<>(v);
        for(int i=0; i<v; i++){
            char C = fileScan.next().charAt(0);
            boolean isVulnerable = fileScan.nextBoolean();
            vertices.put(C, isVulnerable);
            fileScan.nextLine();
        }
        //skip comment line
        fileScan.nextLine();

        while(fileScan.hasNext()){
            fileScan.nextLine();
            char from = fileScan.next().charAt(0);
            char to = fileScan.next().charAt(0);
            double latency = fileScan.nextInt();
            double encryptn = fileScan.nextInt();
            //check boolean values
            if(vertices.get(from) == null || vertices.get(to) == null){
                System.err.println("Error: node vulnerability not added, reconfigure file");
                System.exit(1);
            }
            double cost = latency * (1 + (3 - encryptn) / 10);
        
            G.addEdge(new DirectedEdge(from, to, cost));
            //no directed flag = edge goes both ways
            if(!directed){
                G.addEdge(new DirectedEdge(to, from, cost));
            }
        }
        fileScan.close();
    }

    private void shortestHops(char src, char dst) {
        G.bfs(src);
        if(!G.marked[index(dst)]){
            //no possible route
            System.out.println(-1);
            System.exit(0);
        } else {
            Stack<Integer> path = new Stack<>();
            for (int x = index(dst); x != index(src); x = G.edgeTo[x])
            path.push(x);
            path.push(index(src));
            //shortest path has this many hops
            System.out.println((int)G.distTo[index(dst)]);
            System.exit(0);
        }
    }

    private double shortestDistance(char src, char dst) {
        int source = index(src);
        int destination = index(dst);
        G.dijkstras(src, dst);
        //no route possible for a vulnerable node
        if(!G.marked[destination]){
            return (double)INFINITY;
        } else {
            Stack<Integer> path = new Stack<>();
            for (int x = destination; x != source; x = G.edgeTo[x]){
                path.push(x);
            }
            //return shortest path cost
            return G.distTo[destination];  
        }
    }

    //turns chars into ints for indexing
    public int index(char c){
        return c - 'A';
    }

    private class Digraph {
        private final int v;
        private int e;
        private LinkedList<DirectedEdge>[] adj;
        private boolean[] marked; // marked[v] = is there an s-v path
        private int[] edgeTo; // edgeTo[v] = previous edge on shortest s-v path
        private double[] distTo; // distTo[v] = number of edges shortest s-v path
    
    
        public Digraph(int v) {
            if (v < 0) throw new RuntimeException("Number of vertices must be nonnegative");
            this.v = v;
            this.e = 0;
            @SuppressWarnings("unchecked")
            LinkedList<DirectedEdge>[] temp = (LinkedList<DirectedEdge>[]) new LinkedList[v];
            this.adj = temp;
            for (int i = 0; i < v; i++)
                adj[i] = new LinkedList<DirectedEdge>();
            }
    
        public void addEdge(DirectedEdge edge) {
            int from = index(edge.from());
            adj[from].add(edge);
            e++;
        }
    
        public Iterable<DirectedEdge> adj(int v) {
            return adj[v];
        }
    
        public void bfs(char source) {
            int src = index(source);
            marked = new boolean[this.v];
            distTo = new double[this.v];
            edgeTo = new int[this.v];
            Queue<Integer> q = new LinkedList<Integer>();
            for (int i = 0; i < v; i++){
                distTo[i] = INFINITY;
                marked[i] = false;
            }
            distTo[src] = 0;
            marked[src] = true;
            q.add(src);
            while (!q.isEmpty()) {
                int v = q.remove();
                for (DirectedEdge w : adj(v)) {
                    int to = index(w.to());
                    char toChar = w.to();
                    //check if node vulnerable
                    if(!vertices.get(toChar)){
                        continue;
                    }
                    if (!marked[to]) {
                        edgeTo[to] = v;
                        distTo[to] = distTo[v] + 1;
                        marked[to] = true;
                        q.add(to);
                    }
                }
            }
        }
        public void dijkstras(char source, char destination) {
            int src = index(source);
            int dst = index(destination);

            marked = new boolean[this.v];
            distTo = new double[this.v];
            edgeTo = new int[this.v];
            for (int i = 0; i < v; i++){
                distTo[i] = INFINITY;
                marked[i] = false;
            }
            distTo[src] = 0;
            marked[src] = true;
            int nMarked = 1;
            int current = src;
            while (nMarked < this.v) {
                for (DirectedEdge w : adj(current)) {
                    int wTo = index(w.to());
                    if (distTo[current]+w.cost() < distTo[wTo]) {
                        edgeTo[wTo] = current;
                        distTo[wTo] = distTo[current]+w.cost();
                    }
                }
                //Find the vertex with minimim path distance
                double min = INFINITY;
                current = -1;
                for(int i=0; i<distTo.length; i++){
                    if(marked[i])
                        continue;
                    if(distTo[i] < min){
                        min = distTo[i];
                        current = i;
                    }
                }
                if(current >= 0){
                    marked[current] = true;
                    nMarked++;
                } else //graph is disconnected
                    break;
            }
        }
    }
      
        private class DirectedEdge {
            private final char v;
            private final char w;
            private double cost;
    
            public DirectedEdge(char v, char w, double cost) {
                this.v = v;
                this.w = w;
                this.cost = cost;
            }
            public char from(){
                return v;
            }
            public char to(){
                return w;
            }
            public double cost(){
                return cost;
            }
        }
    
}