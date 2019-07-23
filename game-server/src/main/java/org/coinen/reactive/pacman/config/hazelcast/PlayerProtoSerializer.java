package org.coinen.reactive.pacman.config.hazelcast;

import java.io.IOException;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;
import org.coinen.pacman.Player;

public class PlayerProtoSerializer implements StreamSerializer<Player> {

    @Override
    public int getTypeId() {
        return 42;
    }

    @Override
    public void destroy() {

    }

    @Override
    public void write(ObjectDataOutput out, Player player) throws IOException {
        out.writeByteArray(player.toByteArray());
    }

    @Override
    public Player read(ObjectDataInput in) throws IOException {
        return Player.parseFrom(in.readByteArray());
    }
}
