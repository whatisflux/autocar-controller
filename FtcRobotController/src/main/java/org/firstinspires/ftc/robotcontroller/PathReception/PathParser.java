package org.firstinspires.ftc.robotcontroller.PathReception;

public class PathParser {
    private int current;
    private String text;

    PathParser(String serialized) {
        text = serialized;
        current = 0;
    }

    Path Parse() {
        if (peek() != 'e') return null;
        advance(); // Consume e
        Edge edge1 = ConsumeEdge();

        if (peek() != 'e') return null;
        advance(); // Consume e
        Edge edge2 = ConsumeEdge();

        return new Path(edge1, edge2);
    }

    private Edge ConsumeEdge() {
        Edge edge = new Edge();

        while (peek() == 'p') {
            advance(); // Consume p
            Waypoint waypoint = ConsumeWaypoint();
            edge.waypoints.add(waypoint);
        }

        if (peek() == 'z') edge.isClosed = true;

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
            char nextByte = advance();
            floatBits += ((int) nextByte) << (8 * (3 - iByte));
        }
        return Float.intBitsToFloat(floatBits);
    }

    private Boolean ConsumeBool() {
        char tok = advance();
        if (tok != (char)0) return true;
        else return false;
    }

    private char advance() {
        if (!isAtEnd()) current++;
        return previous();
    }
    private char peek() {
        return text.charAt(current);
    }
    private char previous() {
        return text.charAt(current - 1);
    }
    private Boolean isAtEnd() {
        return current == text.length() - 1;
    }
}
