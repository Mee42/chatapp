package carson.com.utils

import com.google.gson.Gson
import com.google.gson.GsonBuilder

val pretty :Gson = GsonBuilder().setPrettyPrinting().create()

val gson : Gson = GsonBuilder().create()