/*
 * Copyright (C) 2010, 2011, 2012 by Arne Kesting, Martin Treiber, Ralph Germ, Martin Budden
 * <movsim.org@gmail.com>
 * -----------------------------------------------------------------------------------------
 * 
 * This file is part of
 * 
 * MovSim - the multi-model open-source vehicular-traffic simulator.
 * 
 * MovSim is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MovSim is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MovSim. If not, see <http://www.gnu.org/licenses/>
 * or <http://www.movsim.org>.
 * 
 * -----------------------------------------------------------------------------------------
 */

package org.movsim.roadmappings;

import java.util.ArrayList;

import org.movsim.simulator.roadnetwork.Lanes;
import org.movsim.simulator.vehicles.Vehicle;

/**
 * A RoadMapping maps a logical road position (given by a lane and a position on a road segment) onto a physical
 * position, that is an x,y coordinate (given in meters).
 */
public abstract class RoadMappingAbstract implements RoadMapping {

    /**
     * 
     * @param roadPos
     * @param lateralOffset
     *            offset from center of road, used mainly for drawing roadlines and road edges
     * @return a PosTheta object giving position and direction
     */
    @Override
    public abstract PosTheta map(double roadPos, double lateralOffset);

    // Immutable Properties
    protected final int laneCount;
    protected double laneWidth;
    protected double roadWidth;

    // trafficLaneMin and trafficLaneMax set the range of lanes for normal traffic in the road
    // segment lanes less than trafficLaneMin or greater than trafficLaneMax are exit or entrance
    // ramps.
    private int trafficLaneMin;
    private int trafficLaneMax;
    // Road
    protected double roadLength;
    protected int roadColor;
    protected static int defaultRoadColor = 8421505; // gray
    // Positioning
    // pre-allocate single posTheta for the road mapping. This is shared and reused, so must be used
    // carefully.
    protected final PosTheta posTheta = new PosTheta();
    protected double x0;
    protected double y0;
    // Clipping Region
    protected static final int POINT_COUNT = 4;

    protected final PolygonFloat polygonFloat = new PolygonFloat(POINT_COUNT);
    protected ArrayList<PolygonFloat> clippingPolygons;
    protected PolygonFloat outsideClippingPolygon;

    protected static final double DEFAULT_LANE_WIDTH = 2;

    /**
     * Constructor.
     * 
     * @param laneCount
     * @param x0
     * @param y0
     */
    protected RoadMappingAbstract(int laneCountRight, int laneCountLeft, double laneWidth, double x0, double y0) {
        this.laneCount = laneCountRight;
        this.x0 = x0;
        this.y0 = y0;
        trafficLaneMin = Lanes.LANE1; // most inner lane
        trafficLaneMax = laneCount;

        this.laneWidth = laneWidth;
        roadWidth = laneWidth * laneCount;
        roadColor = defaultRoadColor;
    }

    protected RoadMappingAbstract(int laneCount, double laneWidth, double x0, double y0) {
        this(laneCount, 1, laneWidth, x0, y0);
    }

    /**
     * Constructor.
     * 
     * @param laneCount
     * @param x0
     * @param y0
     */
    protected RoadMappingAbstract(int laneCount, double x0, double y0) {
        this(laneCount, DEFAULT_LANE_WIDTH, x0, y0);
    }

    /**
     * Called when the system is running low on memory, and would like actively running process to try to tighten their
     * belts.
     */
    protected void onLowMemory() {
        // By default does nothing. Subclasses may implement memory saving.
    }

    /**
     * Returns the default road color.
     * 
     * @return the default road color
     */
    public static int defaultRoadColor() {
        return RoadMappingAbstract.defaultRoadColor;
    }

    /**
     * Sets the default road color.
     * 
     * @param defaultRoadColor
     */
    public static void setDefaultRoadColor(int defaultRoadColor) {
        RoadMappingAbstract.defaultRoadColor = defaultRoadColor;
    }

    /**
     * Sets the minimum traffic lane. Lanes with <code>lane &lt; trafficLaneMin</code> are not traffic lanes and may be
     * treated differently, especially for lane changes.
     * 
     * @param trafficLaneMin
     */
    // @Override
    // public final void setTrafficLaneMin(int trafficLaneMin) {
    // assert trafficLaneMin >= Lanes.MOST_INNER_LANE;
    // this.trafficLaneMin = trafficLaneMin;
    // }

    /**
     * Returns the minimum traffic lane.
     * 
     * @return the minimum traffic lane
     */
    // @Override
    // public final int trafficLaneMin() {
    // return trafficLaneMin;
    // }

    /**
     * Sets the maximum traffic lane. Lanes with <code>lane &gt; trafficLaneMax</code> are not traffic lanes and may be
     * treated differently, especially for lane changes.
     * 
     * @param trafficLaneMax
     */
    // @Override
    // public final void setTrafficLaneMax(int trafficLaneMax) {
    // this.trafficLaneMax = trafficLaneMax;
    // }

    /**
     * Returns the maximum traffic lane.
     * 
     * @return the maximum traffic lane
     */
    // @Override
    // public final int trafficLaneMax() {
    // return trafficLaneMax;
    // }

    /**
     * Convenience function, returns the start position of the road.
     * 
     * @return start position of the road
     */
    @Override
    public PosTheta startPos() {
        return startPos(0.0);
    }

    /**
     * Convenience function, returns the start position of the road for a given lateral offset.
     * 
     * @param lateralOffset
     * 
     * @return start position of the road for given lateral offset
     */
    @Override
    public PosTheta startPos(double lateralOffset) {
        return map(0.0, lateralOffset);
    }

    /**
     * Convenience function, returns the end position of the road.
     * 
     * @return end position of the road
     */
    @Override
    public PosTheta endPos() {
        return endPos(0.0);
    }

    /**
     * Convenience function, returns the end position of the road for a given lateral offset.
     * 
     * @param lateralOffset
     * 
     * @return end position of the road for given lateral offset
     */
    @Override
    public PosTheta endPos(double lateralOffset) {
        return map(roadLength, lateralOffset);
    }

    /**
     * Returns the end position of the ramp lane.
     * 
     * @return end position of the ramp lane
     */
    public PosTheta endPosRamp() {
        double lateralOffset = laneOffset(laneCount);
        return endPos(lateralOffset);
    }

    /**
     * Map a longitudinal position on the road onto a position and direction in real space.
     * 
     * @param roadPos
     * @return posTheta giving position and direction in real space
     */
    @Override
    public PosTheta map(double roadPos) {
        return map(roadPos, 0.0);
    }

    /**
     * Returns the length of the road.
     * 
     * @return road length, in meters
     */
    @Override
    public final double roadLength() {
        return roadLength;
    }

    /**
     * Returns the width of the road.
     * 
     * @return road width, in meters
     */
    @Override
    public final double roadWidth() {
        return roadWidth;
    }

    /**
     * Sets the road color.
     * 
     * @param color
     */
    @Override
    public final void setRoadColor(int color) {
        this.roadColor = color;
    }

    /**
     * Returns the road color.
     * 
     * @return road color
     */
    @Override
    public final int roadColor() {
        return roadColor;
    }

    /**
     * Returns the width of the lanes.
     * 
     * @return the width of the lanes, in meters
     */
    @Override
    public final double laneWidth() {
        return laneWidth;
    }

    /**
     * Returns the number of lanes.
     * 
     * @return number of lanes
     */
    @Override
    public int laneCount() {
        return laneCount;
    }

    /**
     * Returns the offset of the center of the lane. Fractional lanes are supported to facilitate the drawing of
     * vehicles in the process of changing lanes.
     * 
     * @param lane
     * @return the offset of the center of the lane
     */
    protected double laneOffset(double lane) {
        if (laneCount == 1) {
            // TODO hack here, should be not necessary if relative to centerline
            return 0;
        }
        // TODO correct mapping from laneIndex to real lateral coordinate !!!
        double offset = lane == Lanes.NONE ? 0.0 : (Math.abs(lane) - 0.5) * laneWidth;
        return lane < 0 ? -offset : offset;
        // (0.5 * (1 - laneCount) + (lane - 1)) *
        // return (0.5 * (trafficLaneMin + laneCount - 1) - lane) * laneWidth;
    }

    /**
     * Returns the offset of the center of the lane.
     * 
     * @param lane
     * @return the offset of the center of the lane
     */
    @Override
    public final double laneOffset(int lane) {
        return laneOffset((double) lane);
    }

    /**
     * Returns the offset of the inside edge of the lane.
     * 
     * @param lane
     * @return the offset of the inside edge of the lane
     */
    @Override
    public final double laneInsideEdgeOffset(int lane) {
        return (0.5 * (1 - laneCount + 1) + (lane - 1)) * laneWidth;
    }

    /**
     * Set a clipping region based on the road position and length. Simple implementation at the moment: only one
     * clipping region is supported.
     * 
     * @param pos
     *            position of the clipping region on the road
     * @param length
     *            length of the clipping region
     */
    @Override
    public void addClippingRegion(double pos, double length) {
        if (clippingPolygons == null) {
            clippingPolygons = new ArrayList<>();
        }
        if (outsideClippingPolygon == null) {
            // !!! TODO - this is temporary code, need to fix clipping region
            // set up the outside clip polygon
            final float LARGE_NUMBER = 100000.0f;
            outsideClippingPolygon = new PolygonFloat(POINT_COUNT);
            outsideClippingPolygon.xPoints[0] = -LARGE_NUMBER;
            outsideClippingPolygon.yPoints[0] = -LARGE_NUMBER;
            outsideClippingPolygon.xPoints[1] = LARGE_NUMBER;
            outsideClippingPolygon.yPoints[1] = -LARGE_NUMBER;
            outsideClippingPolygon.xPoints[2] = LARGE_NUMBER;
            outsideClippingPolygon.yPoints[2] = LARGE_NUMBER;
            outsideClippingPolygon.xPoints[3] = -LARGE_NUMBER;
            outsideClippingPolygon.yPoints[3] = LARGE_NUMBER;
        }
        final PolygonFloat clippingPolygon = new PolygonFloat(POINT_COUNT);
        final double offset = 1.5 * laneCount * laneWidth;
        PosTheta posTheta;
        posTheta = map(pos + length, -offset);
        clippingPolygon.xPoints[0] = (float) posTheta.x;
        clippingPolygon.yPoints[0] = (float) posTheta.y;
        posTheta = map(pos + length, offset);
        clippingPolygon.xPoints[1] = (float) posTheta.x;
        clippingPolygon.yPoints[1] = (float) posTheta.y;
        posTheta = map(pos, offset);
        clippingPolygon.xPoints[2] = (float) posTheta.x;
        clippingPolygon.yPoints[2] = (float) posTheta.y;
        posTheta = map(pos, -offset);
        clippingPolygon.xPoints[3] = (float) posTheta.x;
        clippingPolygon.yPoints[3] = (float) posTheta.y;
        clippingPolygons.add(clippingPolygon);
    }

    /**
     * Returns an arraylist of the clipping polygons, or null if no clipping set.
     * 
     * @return arraylist of the clipping polygons, or null if no clipping set.
     */
    @Override
    public ArrayList<PolygonFloat> clippingPolygons() {
        return clippingPolygons;
    }

    /**
     * Returns the outside clipping polygon.
     * 
     * @return the outside clipping polygon
     */
    @Override
    public PolygonFloat outsideClippingPolygon() {
        return outsideClippingPolygon;
    }

    @Override
    public PolygonFloat mapFloat(PosTheta posTheta, double length, double width) {

        final double lca = length * posTheta.cosTheta;
        final double wsa = width * posTheta.sinTheta;
        final double xbr = posTheta.x - 0.5 * (lca - wsa);
        polygonFloat.xPoints[0] = (float) (xbr + lca); // front right
        polygonFloat.xPoints[1] = (float) (xbr + lca - wsa); // front left
        polygonFloat.xPoints[2] = (float) (xbr - wsa); // back left
        polygonFloat.xPoints[3] = (float) xbr; // back right

        final double lsa = length * posTheta.sinTheta;
        final double wca = width * posTheta.cosTheta;
        final double ybr = posTheta.y + 0.5 * (lsa + wca);
        polygonFloat.yPoints[0] = (float) (ybr - lsa); // front right
        polygonFloat.yPoints[1] = (float) (ybr - wca - lsa); // front left
        polygonFloat.yPoints[2] = (float) (ybr - wca); // back left
        polygonFloat.yPoints[3] = (float) ybr; // back right
        return polygonFloat;
    }

    /**
     * Returns a polygon with its vertices at the corners of the subject vehicle.
     * 
     * @param vehicle
     * @param time
     *            current simulation time
     * @return polygon representing vehicle
     */
    @Override
    public PolygonFloat mapFloat(Vehicle vehicle, double time) {
        final PosTheta posTheta = map(vehicle.physicalQuantities().getMidPosition(),
                laneOffset(vehicle.getContinousLane()));
        return mapFloat(posTheta, vehicle.physicalQuantities().getLength(), vehicle.physicalQuantities().getWidth());
    }

    @Override
    public boolean isPeer() {
        return false;
    }

}
