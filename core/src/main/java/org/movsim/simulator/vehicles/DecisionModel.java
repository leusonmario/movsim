package org.movsim.simulator.vehicles;

import java.util.List;

import org.movsim.simulator.roadnetwork.RoadNetwork;
import org.movsim.simulator.roadnetwork.RoadSegment;
import org.movsim.simulator.roadnetwork.routing.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DecisionModel {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(DecisionModel.class);

    public static boolean doDiverge(double beta, RoadSegment roadSegment, List<Route> alternatives) {

        double sum = 0;
        double temp = 0;
        double probability = -1;

        for (Route route : alternatives) {
            sum += Math.exp(beta * RoadNetwork.instantaneousTravelTime(route));
            temp = Math.exp(beta * RoadNetwork.instantaneousTravelTime(route));
        }

        if (sum != 0) {
            probability = temp / sum;
        }

        //LOG.debug("inst travel alternativ1={}, alternative2={}", probability, (1 - probability));

        if (Math.random() > probability) {
            return true;
        }

        return false;

    }

}
