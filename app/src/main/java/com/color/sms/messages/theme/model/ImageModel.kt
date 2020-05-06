package com.color.sms.messages.theme.model

import com.color.sms.messages.theme.R
import com.color.sms.messages.theme.utils.MyApplication
import com.google.gson.Gson
import java.util.*


class ImageModel {
    private val GIF = "Gif"
    private val EMOJI = "Emoji"
    private val listGifName = arrayOf("Emoji Gif", "Cat Gif", "Cute Animal", "Girl Magic")

    data class Gif(val name: String, val firstImage: String)
    data class MyJson(val data: HashMap<String, HashMap<String, List<String>>>)

    private var myJson: MyJson? = null

    fun getListGif(): List<Gif> {
        val listReturn = arrayListOf<Gif>()
        for (name in listGifName)
            listReturn.add(Gif(name, gifList()!![name]!![0]))
        return listReturn
    }

    fun getListGif(name: String): List<String> {
        return gifList()!![name]!!
    }

    fun emojiList(): List<String> {
        return dataList.data[EMOJI]!!["item"]!!
    }

    private fun gifList(): HashMap<String, List<String>>? {
        return dataList.data[GIF]
    }

    private val dataList: MyJson
        get() {
            if (myJson == null) {
                myJson = getListEmoji()
            }
            return myJson!!
        }

    private fun getListEmoji(): MyJson {
        val res = MyApplication.getInstance().resources
        val resData = res.openRawResource(R.raw.image)
        val b = ByteArray(resData.available())
        resData.read(b)
        val gSon = Gson()
        return gSon.fromJson(String(b), MyJson::class.java)
    }
}
