package com.example.sign.data.remote

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*

class FirebaseManager {
    // Explicitly using getInstance() to ensure it connects
    private val database = FirebaseDatabase.getInstance().reference
    private var currentUser: String? = null

    init {
        // Test connection
        database.child("connection_test").setValue("Connected at ${System.currentTimeMillis()}")
    }

    fun setUser(username: String) {
        currentUser = username
        database.child("users").child(username).child("status").setValue("Online")
        database.child("users").child(username).child("status").onDisconnect().setValue("Offline")
    }

    /**
     * Logs any activity (Sign or Speech) to the history
     */
    fun logActivity(fromUser: String, message: String, isEmergency: Boolean = false) {
        val alertId = database.child("alerts").push().key ?: return
        val timestamp = SimpleDateFormat("HH:mm, dd MMM", Locale.getDefault()).format(Date())
        
        val alertData = mapOf(
            "from" to fromUser,
            "message" to message,
            "timestamp" to timestamp,
            "type" to if (isEmergency) "EMERGENCY" else "GENERAL"
        )
        
        // Save to alerts node so parent sees it live
        database.child("alerts").child(alertId).setValue(alertData)
    }

    /**
     * Sends an emergency alert to the parent app
     */
    fun sendEmergencyAlert(fromUser: String, message: String) {
        logActivity(fromUser, message, isEmergency = true)
    }

    /**
     * Listens for any new emergency alerts (For the Parent App)
     */
    fun listenForAlerts(onAlertReceived: (message: String, from: String) -> Unit) {
        database.child("alerts").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Get the most recent alert
                val lastAlert = snapshot.children.lastOrNull()
                if (lastAlert != null) {
                    val message = lastAlert.child("message").getValue(String::class.java) ?: ""
                    val from = lastAlert.child("from").getValue(String::class.java) ?: "Unknown"
                    onAlertReceived(message, from)
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun makeCall(toUser: String, channelName: String, callType: String) {
        val callData = mapOf(
            "from" to currentUser,
            "channel" to channelName,
            "type" to callType,
            "status" to "ringing"
        )
        database.child("calls").child(toUser).setValue(callData)
    }

    fun listenForIncomingCalls(onIncomingCall: (from: String, channel: String, type: String) -> Unit) {
        currentUser?.let { user ->
            database.child("calls").child(user).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val status = snapshot.child("status").getValue(String::class.java)
                    if (status == "ringing") {
                        val from = snapshot.child("from").getValue(String::class.java) ?: ""
                        val channel = snapshot.child("channel").getValue(String::class.java) ?: ""
                        val type = snapshot.child("type").getValue(String::class.java) ?: "Video"
                        onIncomingCall(from, channel, type)
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
        }
    }

    fun respondToCall(fromUser: String, accept: Boolean) {
        if (accept) {
            // database.child("calls").child(currentUser!!).child("status").setValue("connected")
        } else {
            // database.child("calls").child(currentUser!!).removeValue()
        }
    }
    
    fun endCall(otherUser: String) {
        // database.child("calls").child(otherUser).removeValue()
        // currentUser?.let { database.child("calls").child(it).removeValue() }
    }
}
