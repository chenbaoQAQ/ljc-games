package ljc.model;

import ljc.entity.UnitConfig;
import ljc.entity.UserGeneral;
import lombok.Data;
import java.util.Map;
@Data
public class Army {
    private UserGeneral leader;

    private Map<UnitConfig, Integer> troopMap;
}
