package networkUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

public class MessageTypePredicate extends RuntimeTypeAdapterPredicate {

	public MessageTypePredicate() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public String process(JsonElement element) {
        JsonObject obj = element.getAsJsonObject();
        int m_type = obj.get("m_type").getAsInt();

        switch(m_type) {
            case 0: return "NormalMessage";
        	case 1: return "UsernameMessage";
        	case 2: return "LobbyMessage";
        	case 3: return "ChallengeMessage";
            case 4: return "InGameMessage";
            case 5: return "BackMessage";
        }
        return "NormalMessage";
    }
}
