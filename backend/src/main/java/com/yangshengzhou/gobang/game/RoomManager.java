package com.yangshengzhou.gobang.game;

import com.yangshengzhou.gobang.entity.Room;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class RoomManager {
    private final ConcurrentHashMap<String, Room> rooms = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, String> userIdToRoomId = new ConcurrentHashMap<>();

    public void add(Room room, int userId1, int userId2) {
        rooms.put(room.getRoomId(), room);
        userIdToRoomId.put(userId1, room.getRoomId());
        userIdToRoomId.put(userId2, room.getRoomId());
    }

    public void remove(Room room, int userId1, int userId2) {
        if (room == null) {
            return;
        }
        rooms.remove(room.getRoomId());
        userIdToRoomId.remove(userId1);
        userIdToRoomId.remove(userId2);
    }

    public Room getRoomByRoomId(String roomId) {
        return rooms.get(roomId);
    }

    public Room getRoomByUserId(int userId) {
        String roomId = userIdToRoomId.get(userId);
        if (roomId == null) {
            return null;
        }
        return rooms.get(roomId);
    }

    /** 某棋种进行中的房间数 */
    public int countRooms(String gameType) {
        int count = 0;
        for (Room room : rooms.values()) {
            if (gameType.equals(room.getGameType())) {
                count++;
            }
        }
        return count;
    }

    /** 某棋种在对局中的玩家数 */
    public int countUsers(String gameType) {
        int count = 0;
        for (Room room : rooms.values()) {
            if (!gameType.equals(room.getGameType())) {
                continue;
            }
            if (room.getUser1() != null) {
                count++;
            }
            if (room.getUser2() != null) {
                count++;
            }
        }
        return count;
    }
}
