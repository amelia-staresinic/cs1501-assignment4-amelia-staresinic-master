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
        String server = null;
        char src = ' ';
        char dst = ' ';
        //default is false

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
                    server = args[++i];
                    break;
                case "--directed":
                    pg.directed = true;
                    break;
                default:
                    System.err.println("Error: unknown argument");
                    System.exit(1);
            }
        }
        if(mode == null || fileName == null){
            System.err.println("Error: Missing arguments");
            System.exit(1);
        }
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
        else if(mode.equals("patch")){
            if(server == null){
                System.err.println("Error: Missing arguments");
                System.exit(1);
            }
            else{
                pg.readFile(fileName); //add parsing of latency and encryption to this
                //dijstrkas shortest path
            }
        }

        //read vertices from file, save isVulnerable for each node
        //read args, call infect with src and dst int after checking vulnerability
        //readFile adds all edges to Digraph (add something to check directed/undirected??)

        //if mode = infect, task 1
        //if mode = patch, task 2
        
        //each node has vulnerable, T or F
        //if src !vulnerable or dst !vulnerable return -1
        //if src == dst return 0
    }
    
    private void readFile(String fileName) throws IOException {
        Scanner fileScan = new Scanner(new FileInputStream(fileName));
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
        fileScan.nextLine();

        while(fileScan.hasNext()){
            //only add edge if both are vulnerable
            char from = fileScan.next().charAt(0);
            char to = fileScan.next().charAt(0);
            if(vertices.get(from) == null || vertices.get(to) == null){
                System.err.println("Error: vulnerability not added, reconfigure file");
                System.exit(1);
            }
            if(vertices.get(from) && vertices.get(to)){
                G.addEdge(new DirectedEdge(from, to));
                if(!directed){
                    G.addEdge(new DirectedEdge(to, from));
                }
            }
            fileScan.nextLine();
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
            System.out.println(G.distTo[index(dst)]);
            System.exit(0);
        }
    }

    public int index(char c){
        return c - 'A';
    }

    private class Digraph {
        private final int v;
        private int e;
        private LinkedList<DirectedEdge>[] adj;
        private boolean[] marked; // marked[v] = is there an s-v path
        private int[] edgeTo; // edgeTo[v] = previous edge on shortest s-v path
        private int[] distTo; // distTo[v] = number of edges shortest s-v path
    
    
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
            distTo = new int[this.v];
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
                    if (!marked[to]) {
                        edgeTo[to] = v;
                        distTo[to] = distTo[v] + 1;
                        marked[to] = true;
                        q.add(to);
                    }
                }
            }
        }
    }
      
        private class DirectedEdge {
            private final char v;
            private final char w;
            
    
            public DirectedEdge(char v, char w) {
            this.v = v;
            this.w = w;
            }
            public char from(){
            return v;
            }
            public char to(){
            return w;
            }
        }
    
}


