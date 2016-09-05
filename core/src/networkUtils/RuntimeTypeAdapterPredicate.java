package networkUtils;


import com.google.gson.JsonElement;

public abstract class RuntimeTypeAdapterPredicate {

    public abstract String process(JsonElement element);

}
