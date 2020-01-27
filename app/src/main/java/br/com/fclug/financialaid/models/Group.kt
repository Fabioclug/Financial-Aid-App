package br.com.fclug.financialaid.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by Fabioclug on 2016-09-23.
 */
@Parcelize
data class Group(override var id: Long, var name: String, var creator: User,
                 var members: List<User>, var groupBalances: List<TransactionSplit>,
                 var isOnline: Boolean) : Parcelable, UniqueObject() {

    //TODO: remove additional constructors after Kotlin migration is complete
    constructor(id: Long, name: String, creator: User, isOnline: Boolean) :
            this(id, name, creator, ArrayList<User>(), ArrayList<TransactionSplit>(), isOnline)

    constructor(name: String, creator: User, members: List<User>, isOnline: Boolean) :
            this(-1, name, creator, members, ArrayList<TransactionSplit>(), isOnline)

    constructor(groupJsonData: JSONObject) : this(groupJsonData.getLong("group_id"),
            groupJsonData.getString("name"), User(""), ArrayList<User>(),
            ArrayList<TransactionSplit>(), true) {
        val creator = groupJsonData.getString("creator")
        setGroupBalances(groupJsonData.getJSONArray("members"), creator, true)
    }

    val membersNumber: Int
        get() = members.size

    val membersDictionary: HashMap<String, User>
        get() {
            val groupMembers = HashMap<String, User>()
            for (u in members) {
                groupMembers[u.username] = u
            }
            return groupMembers
        }

    @Throws(JSONException::class)
    private fun setGroupBalances(membersObject: JSONArray, creator: String, setMembers: Boolean) {
        val memberList: MutableList<User> = ArrayList()
        val memberCredits: MutableList<TransactionSplit> = ArrayList()

        for (j in 0 until membersObject.length()) {
            val member = membersObject.getJSONObject(j)
            val user = OnlineUser(member.getString("username"), member.getString("name"))
            if (user.username == creator) {
                this.creator = user
            }
            memberList.add(user)
            memberCredits.add(TransactionSplit(user, member.getLong("value")))
        }
        if (setMembers) {
            members = memberList
        }
        groupBalances = memberCredits
    }
}