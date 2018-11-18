package carson.com.server

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import spark.ResponseTransformer


abstract class Decoder : ResponseTransformer {
    private val gson : Gson = buildGson(GsonBuilder()).create()
    abstract fun buildGson(builder : GsonBuilder) : GsonBuilder
    protected open fun mutate(str :String):String = str
    override fun render(model: Any?): String {
        return mutate(gson.toJson(model))
    }
}



class PrettyDecoder : Decoder() {
    override fun buildGson(builder: GsonBuilder): GsonBuilder = builder.setPrettyPrinting()
    override fun mutate(str :String) :String = "<pre>$str</pre>"
}

class StandardDecoder : Decoder() {
    override fun buildGson(builder: GsonBuilder): GsonBuilder = builder
}