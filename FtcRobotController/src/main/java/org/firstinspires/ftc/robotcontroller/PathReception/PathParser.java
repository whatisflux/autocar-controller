package org.firstinspires.ftc.robotcontroller.PathReception;

public class PathParser {
    private int current;
    private byte[] text;

    private static byte EDGE_START_BYTE = 0x65;
    private static byte WAYPOINT_START_BYTE = 0x70;
    private static byte EDGE_CLOSED_BYTE = 0x7a;

    PathParser(byte[] serialized) {
        text = serialized;
        current = 0;
    }

    Path Parse() {
        if (peek() != EDGE_START_BYTE) return null;
        advance(); // Consume e
        Edge edge1 = ConsumeEdge();

        if (peek() != EDGE_START_BYTE) return null;
        advance(); // Consume e
        Edge edge2 = ConsumeEdge();

        return new Path(edge1, edge2);
    }

    private Edge ConsumeEdge() {
        Edge edge = new Edge();

        while (peek() == WAYPOINT_START_BYTE) {
            advance(); // Consume p
            Waypoint waypoint = ConsumeWaypoint();
            edge.waypoints.add(waypoint);
        }

        if (peek() == EDGE_CLOSED_BYTE) edge.isClosed = true;

        return edge;
    }

    private Waypoint ConsumeWaypoint() {
        float x = ConsumeFloat();
        float y = ConsumeFloat();
        Boolean insideIsLeft = ConsumeBool();
        return new Waypoint(x, y, insideIsLeft);
    }

    private float ConsumeFloat() {
        int floatBits = 0;
        for (int iByte = 0; iByte < 4; iByte++) {
            byte nextByte = advance();
            floatBits += ((int) nextByte) << (8 * (3 - iByte));
        }
        return Float.intBitsToFloat(floatBits);
    }

    private Boolean ConsumeBool() {
        byte tok = advance();
        if (tok != 0) return true;
        else return false;
    }

    private byte advance() {
        if (!isAtEnd()) current++;
        return previous();
    }
    private byte peek() { return text[current]; }
    private byte previous() {
        return text[current-1];
    }
    private Boolean isAtEnd() {
        return current == text.length - 1;
    }
}
