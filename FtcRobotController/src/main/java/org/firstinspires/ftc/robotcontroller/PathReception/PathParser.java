package org.firstinspires.ftc.robotcontroller.PathReception;


import org.firstinspires.ftc.robotcontroller.JavaSucks.ByteUtil;

import java.util.Arrays;

public class PathParser {
    private int current;
    private byte[] text;

    private static byte EDGE_START_BYTE = 0x65;
    private static byte WAYPOINT_START_BYTE = 0x70;
    private static byte EDGE_CLOSED_BYTE = 0x7a;


    public PathParser(byte[] serialized) {
        text = serialized;
        current = 0;
    }

    public Path Parse() {
//        System.out.println(Arrays.toString(text));

        if (peek() != EDGE_START_BYTE) {
            throw new Error("Expected 'e', found " + peek());
        }
        advance(); // Consume e
        Edge edge1 = ConsumeEdge();

        if (peek() != EDGE_START_BYTE) {
            throw new Error("Expected 'e', found " + peek());
        }
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

        if (peek() == EDGE_CLOSED_BYTE) {
            advance();
            edge.isClosed = true;
        }

        return edge;
    }

    private Waypoint ConsumeWaypoint() {
        float x = ConsumeFloat();
        float y = ConsumeFloat();
        Boolean insideIsLeft = ConsumeBool();
        return new Waypoint(x, y, insideIsLeft);
    }

    /**
     * Consumes a float
     * @return boolean
     */
    private float ConsumeFloat() {
        int floatBits = 0;
        for (int iByte = 0; iByte < 4; iByte++) {
            byte nextByte = advance();
            System.out.println("Byte " + iByte + ": " + (int) nextByte);
            floatBits += ((ByteUtil.toUnsignedInt(nextByte))) << (8 * (3 - iByte));
        }
        float val =  Float.intBitsToFloat(floatBits);
        return val;
    }

    /**
     * Consumes a bool
     * @return boolean
     */
    private Boolean ConsumeBool() {
        byte tok = advance();
        if (tok != 0) return true;
        else return false;
    }

    private byte advance() {
        if (!isAtEnd()) current++;
        return previous();
    }
    private byte peek() {
        return text[current];
    }
    private byte previous() {
        return text[current-1];
    }
    private Boolean isAtEnd() {
        return current == text.length - 1;
    }
}
