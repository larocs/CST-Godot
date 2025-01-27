package com.example.godotrl;

import com.example.godotrl.containers.AcceptContainer;
import com.example.godotrl.cst.AgentMind;
import com.example.godotrl.util.Action;
import com.example.godotrl.util.RequestType;
import com.example.godotrl.util.SensorData;
import com.example.godotrl.util.Vector2;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController
public class MindController {

    private final String EMPTY_SENSOR_INFO = "{" +
            "   \"innersense\": {\"x\": \"0.0\", \"y\": \"0.0\"}," +
            "   \"vision\": []" +
            "}";

    private AgentMind agentMind = null;
    private long id = 0;

    @GetMapping("/initialize")
    public AcceptContainer initialize() {
        id += 1;

        if (agentMind == null) {
            agentMind = new AgentMind();
            agentMind.initialize();
        } else {
            agentMind.resetMOs();
        }

        return new AcceptContainer(id, "Mind initialized", RequestType.INFO);
    }

    @PostMapping("/updatesensors")
    public AcceptContainer updateSensors(@RequestBody SensorData info) {
        id += 1;

        // Update data
        agentMind.updateSensor(info.innersense, (ArrayList<Vector2>) info.vision);

        return new AcceptContainer(id, "InnerSense: " + info.innersense + " - Vision: " + info.vision, RequestType.SENSOR);
    }

    @GetMapping("/getmotordata")
    public AcceptContainer getMotorData() {
        id += 1;

        if (agentMind.canGetActionData()) {
            return new AcceptContainer(id, String.valueOf(agentMind.getActionData()), RequestType.MOTOR);
        }
        return new AcceptContainer(id, Action.NO_ACTION.toString(), RequestType.MOTOR);
    }

    @GetMapping("/logwin")
    public AcceptContainer logWin() {
        id += 1;

        agentMind.win();

        return new AcceptContainer(id, "Win logged", RequestType.WIN);
    }

    @GetMapping("/loglose")
    public AcceptContainer logDefeat() {
        id += 1;

        agentMind.lose();

        return new AcceptContainer(id, "Defeat logged", RequestType.LOSE);
    }

    @GetMapping("/end")
    public AcceptContainer end() {
        id += 1;

        return new AcceptContainer(id, agentMind.canEndMessage(), RequestType.END);
    }
}
