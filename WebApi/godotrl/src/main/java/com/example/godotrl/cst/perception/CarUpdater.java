package com.example.godotrl.cst.perception;

import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.MemoryObject;
import com.example.godotrl.util.Updater;
import com.example.godotrl.util.Vector2;

import java.util.ArrayList;

public class CarUpdater extends Codelet {
    MemoryObject positionMO = null;
    MemoryObject knownCarsMO = null;
    MemoryObject updateMO = null;

    static final double MAX_DISTANCE = 5;

    @Override
    public void accessMemoryObjects() {
        positionMO = (MemoryObject) getInput("POSITION");
        knownCarsMO = (MemoryObject) getOutput("KNOWN_CARS");
        updateMO = (MemoryObject) getOutput("UPDATE");
    }

    @Override
    public void calculateActivation() {

    }

    @Override
    public void proc() {
        if (((Updater) updateMO.getI()).updateCarUpdate()) {
            ArrayList<Vector2> memoryList = (ArrayList<Vector2>) knownCarsMO.getI();
            Vector2 pos = (Vector2) positionMO.getI();

            int i = 0;
            while (i < memoryList.size()) {
                Vector2 car = memoryList.get(i);

                car.add(new Vector2(1, 0));
                if (car.manhattanDistance(pos) > MAX_DISTANCE) {
                    memoryList.remove(i);
                } else {
                    i += 1;
                }
            }
        }
    }
}
