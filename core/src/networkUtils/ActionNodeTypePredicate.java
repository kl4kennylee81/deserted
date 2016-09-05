package networkUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

public class ActionNodeTypePredicate extends RuntimeTypeAdapterPredicate {

	@Override
	public String process(JsonElement element) {
        JsonObject obj = element.getAsJsonObject();
        int an_type = obj.get("m_type").getAsInt();
        
        switch(an_type){
        case 0: return "MessageActionNode";
        case 1: return "GameActionNode";
        }
        
        return null;
	}
}
