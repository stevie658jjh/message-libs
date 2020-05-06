package lib.message.theme.activity.search.model

import android.net.Uri

class Search {
    enum class NAME {
        Videos,
        Images,
        Links
    }

    data class Category(var icon: Int, var name: NAME)

    data class MMS(var image: Uri, var date: Long, var address: String)

    data class Contact(var idConversation: Long, var address: String)

    data class ContactConversation(var idConversation: Long, var contact: lib.message.theme.model.Contact)
}
