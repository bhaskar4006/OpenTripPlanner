package org.opentripplanner.routing.algorithm.kao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import org.opentripplanner.gtfs.GtfsContext;
import org.opentripplanner.routing.core.Edge;
import org.opentripplanner.routing.core.Graph;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.TraverseOptions;
import org.opentripplanner.routing.core.TraverseResult;
import org.opentripplanner.routing.core.Vertex;
import org.opentripplanner.routing.edgetype.Board;
import org.opentripplanner.routing.edgetype.Hop;
import org.opentripplanner.routing.edgetype.Traversable;

public class KaoGraph extends Graph {
    private static final long serialVersionUID = 3667189924531545548L;

    ArrayList<Edge> allhops;

    private GtfsContext _context;

    public KaoGraph() {
        allhops = new ArrayList<Edge>();
    }

    public void setGtfsContext(GtfsContext context) {
        _context = context;
    }

    public Edge addEdge(Vertex a, Vertex b, Traversable ep) {
        Edge ret = super.addEdge(a, b, ep);
        allhops.add(ret);
        return ret;
    }

    public ArrayList<EdgeOption> sortedEdges(Date time, long window) {
        ArrayList<EdgeOption> ret = new ArrayList<EdgeOption>();
        State state0 = new State(time.getTime());

        TraverseOptions options = new TraverseOptions();
        options.setGtfsContext(_context);

        for (int i = 0; i < allhops.size(); i++) {
            Edge edge = allhops.get(i);
            if (!(edge.payload instanceof Board))
                continue;
            Board board = (Board) edge.payload;

            TraverseResult wr = board.traverse(state0, options);

            if (wr != null) {
                for (Edge og : edge.tov.outgoing) {
                    if (og.payload instanceof Hop) {
                        edge = og;
                        wr = edge.traverse(wr.state, options);
                        break;
                    }
                }

                long timeToArrival = wr.state.getTime() - time.getTime();

                if (timeToArrival <= window) {
                    ret.add(new EdgeOption(edge, timeToArrival));
                }
            }
        }

        Collections.sort(ret);

        return ret;
    }

}