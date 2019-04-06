import PlayerRepository from '../PlayerRepository';
import { Player } from 'game-idl';

export default class InMemoryPlayerRepository implements PlayerRepository {
    store: Map<string, Player> = new Map()

    findAll(): Player[] {
        return Array.from(this.store.values());
    }

    findOne(uuid: string): Player {
        return this.store.get(uuid);
    }

    update(uuid: string, supplier: (p: Player) => Player) {
        let user = this.store.get(uuid);
        user = supplier(user);
        this.store.set(uuid, user);
        return user;
    }

    save(uuid: string, supplier: () => Player): Player {
        const user = supplier();
        user.setUuid(uuid);
        this.store.set(uuid, user);
        return user;
    }

    delete(uuid: string): Player {
        const user = this.store.get(uuid);
        this.store.delete(uuid);
        return user;
    }
}
