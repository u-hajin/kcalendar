package com.kkwakjavacoding.kcalendar.firebase

import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class Database {

    val id = UserID.getWidevineID()
    val database = Firebase.database.getReference("userToken").child(id)
    
}
